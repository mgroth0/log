package matt.log.profile.yk

import com.yourkit.api.controller.Controller
import com.yourkit.api.controller.CpuProfilingSettings
import matt.file.MFile
import matt.file.commons.YOUR_KIT_APP_FOLDER
import matt.file.construct.mFile
import matt.lang.PROFILING_AGENT_CONNECTED_PROP
import matt.lang.function.Produce
import matt.shell.shell
import java.awt.Desktop


fun profilingAgentIsConnected() = System.getProperty(PROFILING_AGENT_CONNECTED_PROP)?.toBooleanStrict() ?: false

class ProfiledResult<R>(
    val result: R,
    val snapshot: MFile?
)

class YourKit(
    private val enableAll: Boolean = true,
    private val openAllSnapshots: Boolean = false
) {


    private val controller by lazy {
        println("building YourKit Controller...")
        Controller.newBuilder().self().build()
    }

    fun <R> recordCPU(
        enable: Boolean = enableAll,
        openSnapshot: Boolean = openAllSnapshots,
        op: Produce<R>,
    ): ProfiledResult<R> {
        startCpuProfiling(enable = enable)
        val r = op()
        val snapshot = stopCpuProfiling(enable = enable, openSnapshot = openSnapshot)
        return ProfiledResult(r, snapshot)
    }

    fun startCpuProfiling(
        enable: Boolean = enableAll,
    ) {
        if (enable) {
            println("clearing CPU data...")
            controller.clearCpuData()
            println("starting CPU recording...")
            controller.startSampling(CpuProfilingSettings())
            println("running op in CPU recording...")
        }
    }

    fun stopCpuProfiling(
        enable: Boolean = enableAll,
        openSnapshot: Boolean = openAllSnapshots,
    ): MFile? {
        var snapshot: MFile? = null
        if (enable) {
            println("capturing performance snapshot...")
            val performanceSnapshotPath = controller.capturePerformanceSnapshot()
            snapshot = mFile(performanceSnapshotPath)

            if (openSnapshot) {
                println("opening snapshot $performanceSnapshotPath")
                Desktop.getDesktop().open(snapshot)
            } else {
                println("performanceSnapshotPath=${performanceSnapshotPath}")
            }

            println("stopping CPU recording...")
            controller.stopCpuProfiling()
            println("stopped CPU recording")
        }
        return snapshot
    }


    fun captureMemorySnapshot(
        enable: Boolean = enableAll,
        openSnapshot: Boolean = openAllSnapshots
    ) {
        if (enable) {
            println("capturing memory snapshot...")
            val snapshotFilePath = controller.captureMemorySnapshot()
            if (openSnapshot) {
                println("opening snapshot $snapshotFilePath")
                /*https://www.yourkit.com/forum/viewtopic.php?t=43490*/
                shell("open", "-a", YOUR_KIT_APP_FOLDER.abspath, "-open", snapshotFilePath)
            } else {
                println("Own memory snapshot captured: $snapshotFilePath")
            }

        }
    }
}