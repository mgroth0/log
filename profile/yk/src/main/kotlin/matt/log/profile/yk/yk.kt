package matt.log.profile.yk

import com.yourkit.api.controller.Controller
import com.yourkit.api.controller.CpuProfilingSettings
import matt.file.MFile
import matt.file.commons.YOUR_KIT_APP_FOLDER
import matt.file.construct.mFile
import matt.log.profile.real.ProfilerEngine
import matt.shell.shell


object YourKit : ProfilerEngine {

    fun openSnapshot(mFile: MFile) {
        println("opening snapshot $mFile")
        /*https://www.yourkit.com/forum/viewtopic.php?t=43490*/
        shell("open", "-a", YOUR_KIT_APP_FOLDER.abspath, "-open", mFile.abspath)
    }

    private val controller by lazy {
        println("building YourKit Controller...")
        Controller.newBuilder().self().build()
    }

    override fun clearCpuDataAndStartCPURecording() {
        println("clearing CPU data...")
        controller.clearCpuData()
        println("starting CPU recording...")
        controller.startSampling(CpuProfilingSettings())
        println("running op in CPU recording...")
    }


    override fun saveCpuSnapshot(): MFile {
        return mFile(controller.capturePerformanceSnapshot())
    }

    override fun stopCpuRecording() {
        controller.stopCpuProfiling()
    }


    override fun captureMemorySnapshot(): MFile {
        return mFile(controller.captureMemorySnapshot())
    }

}