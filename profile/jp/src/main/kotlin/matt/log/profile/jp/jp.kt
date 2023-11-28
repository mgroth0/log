package matt.log.profile.jp

import com.jprofiler.api.controller.Controller
import com.jprofiler.api.controller.HeapDumpOptions
import com.jprofiler.api.controller.TrackingOptions
import matt.file.commons.TEMP_DIR
import matt.file.context.ProcessContext
import matt.file.toJioFile
import matt.lang.file.toJFile
import matt.lang.jprof.JPROFILER_PROGRAMMATIC_ASYNC_SESSION_ID
import matt.lang.jprof.JPROFILER_PROGRAMMATIC_INSTRUMENTATION_SESSION_ID
import matt.lang.model.file.FsFile
import matt.lang.myPid
import matt.lang.profiling.IsProfilingWithJProfiler
import matt.lang.shutdown.preaper.ProcessReaper
import matt.log.profile.real.CpuProfilingTechnique
import matt.log.profile.real.CpuProfilingTechnique.Async
import matt.log.profile.real.CpuProfilingTechnique.Instrumentation
import matt.log.profile.real.ProfilerEngine
import matt.log.warn.warn
import matt.shell.ShellVerbosity
import matt.shell.shell

private var attachedJProfilerProgrammaticallyAtRuntime = false


class JProfiler(
    private val snapshotFolder: FsFile
) : ProfilerEngine {

    companion object {
        fun defaultSnapshotFolder() = TEMP_DIR["jprofiler"]
        context(ProcessReaper)
        fun defaultSnapshotFileAction(snapshotFile: FsFile) = shell("open", snapshotFile.path)
    }

    context(ProcessReaper)
    override fun openSnapshot(file: FsFile) {
        defaultSnapshotFileAction(file)
    }

    override fun wasAttachedAtStartup(): Boolean {
        return IsProfilingWithJProfiler
    }

    override fun wasAttachedProgrammaticallyAtRuntime(): Boolean {
        return attachedJProfilerProgrammaticallyAtRuntime
    }

    context(ProcessContext, ProcessReaper)
    override fun attach(
        technique: CpuProfilingTechnique
    ): String {
        val r = shell(
            files.jpenable.path,
            "-n",
            "--offline",
            "--pid=${myPid}",
            "--config",
            /*did not need to use pid before on heroku. But I probably should have. There is a risk of it choosing the wrong java process otherwise.*/
            files.jProfilerConfigFile.path,
            "--id",
            when (technique) {
                Instrumentation -> JPROFILER_PROGRAMMATIC_INSTRUMENTATION_SESSION_ID
                Async           -> JPROFILER_PROGRAMMATIC_ASYNC_SESSION_ID
            }.toString(),
            verbosity = ShellVerbosity.STREAM
        )
        attachedJProfilerProgrammaticallyAtRuntime = true
        return r
    }

    override fun clearCpuDataAndStartCPURecording() {
        println("clearing CPU data...")
        println("starting CPU recording...")
        Controller.startCPURecording(
            true,
            TrackingOptions().kotlinCoroutines(true)
        )
        println("running op in CPU recording...")
    }

    override fun saveCpuSnapshot(): FsFile {
        var f: FsFile
        do {
            f = snapshotFolder["tmp-${System.currentTimeMillis()}-cpu.jps"]
        } while (f.toJioFile().exists())
        snapshotFolder.toJioFile().mkdirs()
        Controller.saveSnapshot(f.toJFile())
        return f
    }

    override fun stopCpuRecording() {
        Controller.stopCPURecording()
    }

    override fun captureMemorySnapshot(): FsFile {

        warn("This does NOT work in tests. Very likely because JProfiler requires to be started properly with the JVM for that, whereas tests are not always forked or something. Yourkit should work for tests.")

        Controller.triggerHeapDump(
            HeapDumpOptions()
                .fullGc(true)
                .retainSoftReferences(false)
                .retainFinalizerReferences(false)
                .retainWeakReferences(false)
                .retainPhantomReferences(false)
                .primitiveData(true)
                .calculateRetainedSizes(true)
                .selectRecorded(true)
        )


        var f: FsFile
        do {
            f = snapshotFolder["tmp-${System.currentTimeMillis()}-mem.jps"]
        } while (f.toJioFile().exists())
        snapshotFolder.toJioFile().mkdirs()
        Controller.saveSnapshot(f.toJFile())


        return f

    }
}