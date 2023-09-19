package matt.log.profile.real

import matt.file.JioFile
import matt.file.toJioFile
import matt.lang.model.file.FsFile
import matt.lang.shutdown.preaper.ProcessReaper

class Profiler(
    val enableAll: Boolean = true,
    val engine: ProfilerEngine,
    val onSaveSnapshot: (JioFile) -> Unit = {},
) {

    /*This worked, but then gradle threw an exception when the require below failed...*/
    /*  companion object {
          private var instance: WeakReference<Profiler>? = null
          fun stopCpuProfilingAndShutdown() {
              instance!!.get()!!.stopCpuProfiling()
              *//*error("Shutting down")*//*
            exitProcess(0)
        }
    }*/

    /*init {
        require(instance == null || instance!!.get() == null)
        instance = WeakReference(this)
    }*/

    inline fun <R> recordCPU(
        enable: Boolean = enableAll,
        op: () -> R,
    ): ProfiledResult<R> {
        startCpuProfiling(enable = enable)
        val r = op()
        val snapshot = stopCpuProfiling(enable = enable)
        return ProfiledResult(r, snapshot)
    }

    fun startCpuProfiling(
        enable: Boolean = enableAll,
    ) {
        if (enable) {
            engine.clearCpuDataAndStartCPURecording()
        }
    }

    fun stopCpuProfiling(
        enable: Boolean = enableAll,
    ): FsFile? {
        var snapshot: FsFile? = null
        if (enable) {
            println("capturing performance snapshot...")
            val performanceSnapshotPath = engine.saveCpuSnapshot()
            snapshot = performanceSnapshotPath
            onSaveSnapshot(snapshot.toJioFile())
            println("stopping CPU recording...")
            engine.stopCpuRecording()
            println("stopped CPU recording")
        }
        return snapshot
    }

    fun captureMemorySnapshot(
        enable: Boolean = enableAll,
    ): FsFile? {
        if (enable) {
            println("capturing memory snapshot...")
            val snapshotFilePath = engine.captureMemorySnapshot()
            onSaveSnapshot(snapshotFilePath.toJioFile())
            return snapshotFilePath
        }
        return null
    }
}


interface ProfilerEngine {
    fun clearCpuDataAndStartCPURecording()
    fun saveCpuSnapshot(): FsFile
    fun stopCpuRecording()
    fun captureMemorySnapshot(): FsFile
    context(ProcessReaper)
    fun openSnapshot(file: FsFile)
}

class ProfiledResult<R>(
    val result: R,
    val snapshot: FsFile?
)