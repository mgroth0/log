package matt.log.profile.jp

import com.jprofiler.api.controller.Controller
import matt.file.MFile
import matt.file.commons.TEMP_DIR
import matt.log.profile.real.ProfilerEngine
import matt.log.warn.warn
import matt.shell.shell


class JProfiler(
    private val snapshotFolder: MFile
) : ProfilerEngine {

    companion object {
        fun defaultSnapshotFolder() = TEMP_DIR["jprofiler"]
        fun defaultSnapshotFileAction(snapshotFile: MFile) = shell("open", snapshotFile.path)
    }

    override fun clearCpuDataAndStartCPURecording() {
        println("clearing CPU data...")
        println("starting CPU recording...")
        Controller.startCPURecording(true)
        println("running op in CPU recording...")
    }

    override fun saveCpuSnapshot(): MFile {
        var f: MFile
        do {
            f = snapshotFolder["tmp-${System.currentTimeMillis()}-cpu.jps"]
        } while (f.exists())
        snapshotFolder.mkdirs()
        Controller.saveSnapshot(f)
        return f
    }

    override fun stopCpuRecording() {
        Controller.stopCPURecording()
    }

    override fun captureMemorySnapshot(): MFile {

        warn("seems like this just gets CPU...")

        var f: MFile
        do {
            f = snapshotFolder["tmp-${System.currentTimeMillis()}-mem.jps"]
        } while (f.exists())
        snapshotFolder.mkdirs()
        Controller.saveSnapshot(f)
        Controller.stopCPURecording()
        return f

    }
}