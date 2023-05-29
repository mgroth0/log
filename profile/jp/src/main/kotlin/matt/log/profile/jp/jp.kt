package matt.log.profile.jp

import com.jprofiler.api.controller.Controller
import matt.file.MFile
import matt.file.commons.TEMP_DIR
import matt.log.profile.real.ProfiledResult
import matt.log.profile.real.RealProfiler
import matt.log.warn.warn
import matt.shell.shell

class JProfiler(
    enableAll: Boolean = true,
    openAllSnapshots: Boolean = false
) : RealProfiler(enableAll = enableAll, openAllSnapshots = openAllSnapshots) {

    override fun <R> recordCPU(
        enable: Boolean,
        openSnapshot: Boolean,
        op: () -> R,
    ): ProfiledResult<R> {
        startCpuProfiling(enable = enable)
        val r = op()
        val snapshot = stopCpuProfiling(enable = enable, openSnapshot = openSnapshot)
        return ProfiledResult(r, snapshot)
    }

    override fun startCpuProfiling(
        enable: Boolean,
    ) {
        if (enable) {
            println("clearing CPU data...")
            println("starting CPU recording...")
            Controller.startCPURecording(true)
            println("running op in CPU recording...")
        }
    }

    override fun stopCpuProfiling(
        enable: Boolean,
        openSnapshot: Boolean,
    ): MFile? {
        var snapshot: MFile? = null
        if (enable) {
            snapshot = TEMP_DIR["tmp-${System.currentTimeMillis()}-cpu.jps"]
            println("capturing performance snapshot...")
            Controller.saveSnapshot(snapshot)
            if (openSnapshot) {
                println("opening snapshot $snapshot")
                shell("open", snapshot.abspath)
            } else {
                println("performanceSnapshotPath=${snapshot}")
            }

            println("stopping CPU recording...")
            Controller.stopCPURecording()
            println("stopped CPU recording")
        }
        return snapshot
    }


    override fun captureMemorySnapshot(
        enable: Boolean,
        openSnapshot: Boolean
    ) {
        warn("seems like this just gets CPU...")
        val snapshot: MFile?
        if (enable) {
            snapshot = TEMP_DIR["tmp-${System.currentTimeMillis()}-mem.jps"]
            println("capturing memory snapshot...")
            Controller.saveSnapshot(snapshot)
            Controller.stopCPURecording()
            if (openSnapshot) {
                println("opening snapshot $snapshot")
                shell("open", snapshot.abspath)
            } else {
                println("Own memory snapshot captured: $snapshot")
            }

        }
    }
}