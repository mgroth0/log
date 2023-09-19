package matt.log.profile.yk

import com.yourkit.api.controller.Controller
import com.yourkit.api.controller.CpuProfilingSettings
import matt.lang.model.file.FsFile
import matt.file.commons.YOUR_KIT_APP_FOLDER
import matt.file.macJioFile
import matt.lang.shutdown.preaper.ProcessReaper
import matt.log.profile.real.ProfilerEngine
import matt.shell.shell


object YourKit : ProfilerEngine {

    context(ProcessReaper)
    override fun openSnapshot(file: FsFile) {
        println("opening snapshot $file")
        /*https://www.yourkit.com/forum/viewtopic.php?t=43490*/
        shell("open", "-a", YOUR_KIT_APP_FOLDER.abspath, "-open", file.abspath)
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


    override fun saveCpuSnapshot(): FsFile {
        return macJioFile(controller.capturePerformanceSnapshot())
    }

    override fun stopCpuRecording() {
        controller.stopCpuProfiling()
    }


    override fun captureMemorySnapshot(): FsFile {
        return macJioFile(controller.captureMemorySnapshot())
    }

}