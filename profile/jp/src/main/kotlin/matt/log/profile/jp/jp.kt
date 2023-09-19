package matt.log.profile.jp

import com.jprofiler.api.controller.Controller
import com.jprofiler.api.controller.HeapDumpOptions
import matt.lang.model.file.FsFile
import matt.file.commons.TEMP_DIR
import matt.file.toJioFile
import matt.lang.file.toJFile
import matt.log.profile.real.ProfilerEngine
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

        Controller.triggerHeapDump(
            HeapDumpOptions()
                .fullGc(true)
                .retainSoftReferences(true)
                .retainFinalizerReferences(false)
                .retainWeakReferences(false)
                .retainPhantomReferences(false)
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