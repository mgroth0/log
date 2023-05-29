package matt.log.profile.real

import matt.file.MFile
import matt.lang.PROFILING_AGENT_CONNECTED_PROP

fun profilingAgentIsConnected() = System.getProperty(PROFILING_AGENT_CONNECTED_PROP)?.toBooleanStrict() ?: false


abstract class RealProfiler(
    val enableAll: Boolean = true,
    val openAllSnapshots: Boolean = false
) {
    abstract fun <R> recordCPU(
        enable: Boolean = enableAll,
        openSnapshot: Boolean = openAllSnapshots,
        op: () -> R,
    ): ProfiledResult<R>

    abstract fun startCpuProfiling(
        enable: Boolean = enableAll,
    )

    abstract fun stopCpuProfiling(
        enable: Boolean = enableAll,
        openSnapshot: Boolean = openAllSnapshots,
    ): MFile?

    abstract fun captureMemorySnapshot(
        enable: Boolean = enableAll,
        openSnapshot: Boolean = openAllSnapshots
    )
}


class ProfiledResult<R>(
    val result: R,
    val snapshot: MFile?
)