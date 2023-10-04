package matt.log.profile.jp

import com.jprofiler.api.controller.Controller
import com.jprofiler.api.controller.HeapDumpOptions
import matt.file.commons.TEMP_DIR
import matt.file.toJioFile
import matt.lang.file.toJFile
import matt.lang.model.file.FsFile
import matt.log.profile.real.ProfilerEngine
import matt.log.warn.warn
import matt.shell.context.ReapingShellExecutionContext
import matt.shell.shell


class JProfiler(
    private val snapshotFolder: FsFile
) : ProfilerEngine {

    companion object {
        fun defaultSnapshotFolder() = TEMP_DIR["jprofiler"]
        context(ReapingShellExecutionContext)
        fun defaultSnapshotFileAction(snapshotFile: FsFile) = shell("open", snapshotFile.path)
    }

    context(ReapingShellExecutionContext)
    override fun openSnapshot(file: FsFile) {
        defaultSnapshotFileAction(file)
    }

    override fun clearCpuDataAndStartCPURecording() {
        println("clearing CPU data...")
        println("starting CPU recording...")
        Controller.startCPURecording(true)
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