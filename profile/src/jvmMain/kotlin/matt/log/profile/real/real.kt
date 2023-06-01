package matt.log.profile.real

import matt.file.MFile
import matt.file.construct.mFile
import matt.lang.PROFILING_AGENT_CONNECTED_PROP

fun profilingAgentIsConnected() = System.getProperty(PROFILING_AGENT_CONNECTED_PROP)?.toBooleanStrict() ?: false


class Profiler(
    val enableAll: Boolean = true,
    val engine: ProfilerEngine,
    val onSaveSnapshot: (MFile) -> Unit = {},
) {
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
    ): MFile? {
        var snapshot: MFile? = null
        if (enable) {
            println("capturing performance snapshot...")
            val performanceSnapshotPath = engine.saveCpuSnapshot()
            snapshot = mFile(performanceSnapshotPath)
            onSaveSnapshot(snapshot)
            println("stopping CPU recording...")
            engine.stopCpuRecording()
            println("stopped CPU recording")
        }
        return snapshot
    }

    fun captureMemorySnapshot(
        enable: Boolean = enableAll,
    ) {
        if (enable) {
            println("capturing memory snapshot...")
            val snapshotFilePath = engine.captureMemorySnapshot()
            onSaveSnapshot(snapshotFilePath)
        }
    }
}



interface ProfilerEngine {
    fun clearCpuDataAndStartCPURecording()
    fun saveCpuSnapshot(): MFile
    fun stopCpuRecording()
    fun captureMemorySnapshot(): MFile
}

class ProfiledResult<R>(
    val result: R,
    val snapshot: MFile?
)