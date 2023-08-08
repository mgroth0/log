package matt.log.profile.real

import matt.file.MFile
import matt.file.construct.mFile

class Profiler(
    val enableAll: Boolean = true,
    val engine: ProfilerEngine,
    val onSaveSnapshot: (MFile) -> Unit = {},
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
    ): MFile? {
        if (enable) {
            println("capturing memory snapshot...")
            val snapshotFilePath = engine.captureMemorySnapshot()
            onSaveSnapshot(snapshotFilePath)
            return snapshotFilePath
        }
        return null
    }
}


interface ProfilerEngine {
    fun clearCpuDataAndStartCPURecording()
    fun saveCpuSnapshot(): MFile
    fun stopCpuRecording()
    fun captureMemorySnapshot(): MFile
    fun openSnapshot(file: MFile)
}

class ProfiledResult<R>(
    val result: R,
    val snapshot: MFile?
)