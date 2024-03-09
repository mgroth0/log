package matt.log.profile.real

import matt.file.JioFile
import matt.file.context.ProcessContext
import matt.file.toJioFile
import matt.lang.common.DoNothing
import matt.lang.jprof.JPROFILER_PROGRAMMATIC_ASYNC_SESSION_ID
import matt.lang.jprof.JPROFILER_PROGRAMMATIC_INSTRUMENTATION_SESSION_ID
import matt.lang.model.file.AnyFsFile
import matt.lang.shutdown.preaper.ProcessReaper
import matt.lang.sync.common.SimpleReferenceMonitor
import matt.model.profile.CpuProfilingTechnique
import matt.model.profile.CpuProfilingTechnique.Async
import matt.model.profile.CpuProfilingTechnique.Instrumentation



var attachedWith: JpEnableAttachMode? = null
    private set

class Profiler(
    val enableAll: Boolean = true,
    val engine: ProfilerEngine,
    val onSaveSnapshot: context(ProcessReaper) (JioFile) -> Unit = {}
) {

    /*

    // This worked, but then gradle threw an exception when the require below failed...

      companion object {
          private var instance: WeakReference<Profiler>? = null
          fun stopCpuProfilingAndShutdown() {
              instance!!.get()!!.stopCpuProfiling()
            exitProcess(0)
        }
    }

    init {
        require(instance == null || instance!!.get() == null)
        instance = WeakReference(this)
    }

     */

    context(ProcessReaper)
    inline fun <R> recordCPU(
        enable: Boolean = enableAll,
        op: () -> R
    ): ProfiledResult<R> {
        startCpuProfiling(enable = enable)
        val r = op()
        val snapshot = stopCpuProfiling(enable = enable)
        return ProfiledResult(r, snapshot)
    }

    fun startCpuProfiling(
        enable: Boolean = enableAll
    ) {
        if (enable) {
            engine.clearCpuDataAndStartCPURecording()
        }
    }

    context(ProcessReaper)
    fun stopCpuProfiling(
        enable: Boolean = enableAll
    ): AnyFsFile? {
        var snapshot: AnyFsFile? = null
        if (enable) {
            println("capturing performance snapshot...")
            val performanceSnapshotPath = engine.saveCpuSnapshot()
            snapshot = performanceSnapshotPath
            onSaveSnapshot(this@ProcessReaper, snapshot.toJioFile())
            println("stopping CPU recording...")
            engine.stopCpuRecording()
            println("stopped CPU recording")
        }
        return snapshot
    }

    context(ProcessReaper)
    fun captureMemorySnapshot(
        enable: Boolean = enableAll
    ): AnyFsFile? {
        if (enable) {
            println("capturing memory snapshot...")
            val snapshotFilePath = engine.captureMemorySnapshot()
            onSaveSnapshot(this@ProcessReaper, snapshotFilePath.toJioFile())
            return snapshotFilePath
        }
        return null
    }

    fun isAttached() = engine.wasAttachedAtStartup()
    context(ProcessContext, ProcessReaper)
    fun attach(mode: JpEnableAttachMode) = engine.attach(mode)


    context(ProcessContext, ProcessReaper)
    fun attachIfNeeded(mode: JpEnableAttachMode) {
        synchronized(ProfileAttachingMonitor) {
            check(attachedWith == null || attachedWith == mode)
            if (
                engine.wasAttachedAtStartup()
                || engine.wasAttachedProgrammaticallyAtRuntime()
            ) {
                DoNothing
            } else {
                attachedWith = mode
                attach(mode)
            }
        }
    }
}

private val ProfileAttachingMonitor = SimpleReferenceMonitor()


interface ProfilerEngine {
    fun clearCpuDataAndStartCPURecording()
    fun saveCpuSnapshot(): AnyFsFile
    fun stopCpuRecording()
    fun captureMemorySnapshot(): AnyFsFile
    context(ProcessReaper)
    fun openSnapshot(file: AnyFsFile)

    fun wasAttachedAtStartup(): Boolean
    fun wasAttachedProgrammaticallyAtRuntime(): Boolean
    context(ProcessContext, ProcessReaper)
    fun attach(mode: JpEnableAttachMode): String

    fun asProfiler(
        enableAll: Boolean
    ): Profiler
}

abstract class ProfilerEngineBase: ProfilerEngine {
    final override fun asProfiler(
        enableAll: Boolean
    ): Profiler =
        Profiler(
            enableAll = enableAll,
            engine = this,
            onSaveSnapshot = {
                openSnapshot(it)
            }
        )
}



class ProfiledResult<R>(
    val result: R,
    val snapshot: AnyFsFile?
)


sealed interface JpEnableAttachMode
data class GuiMode(
    val port: Int? = null
) : JpEnableAttachMode


data class OfflineMode(
    val config: AnyFsFile /*not technically required*/,
    val id: Int /*required if config is set and it has more than one session*/
) : JpEnableAttachMode {
    companion object {
        context(ProcessContext)
        fun forTechnique(technique: CpuProfilingTechnique) =
            OfflineMode(
                config = files.jProfilerConfigFile,
                id =
                    when (technique) {
                        Instrumentation -> JPROFILER_PROGRAMMATIC_INSTRUMENTATION_SESSION_ID
                        Async           -> JPROFILER_PROGRAMMATIC_ASYNC_SESSION_ID
                    }
            )
    }
}
