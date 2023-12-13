package matt.log.profile.yk

import com.yourkit.api.controller.Controller
import com.yourkit.api.controller.CpuProfilingSettings
import matt.file.commons.YOUR_KIT_APP_FOLDER
import matt.file.context.ProcessContext
import matt.file.macJioFile
import matt.lang.model.file.FsFile
import matt.lang.myPid
import matt.lang.profiling.IsProfilingWithYourKit
import matt.lang.shutdown.preaper.ProcessReaper
import matt.lang.unsafeErr
import matt.log.profile.real.JpEnableAttachMode
import matt.log.profile.real.ProfilerEngine
import matt.shell.ShellVerbosity
import matt.shell.shell


object YourKit : ProfilerEngine {

    private var attachedYourKitProgrammaticallyAtRuntime = false

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

    override fun wasAttachedAtStartup(): Boolean {
        return IsProfilingWithYourKit
    }

    override fun wasAttachedProgrammaticallyAtRuntime(): Boolean {
        return attachedYourKitProgrammaticallyAtRuntime
    }

    context(ProcessContext, ProcessReaper)
    override fun attach(
        mode: JpEnableAttachMode
    ): String {
        unsafeErr("TODO: Create yourkit attach version of JpEnableAttachMode. Extract common interface and make things generic or whatever")
        val r = shell(
            files.yourKitAttachScript.path,
            myPid,
            verbosity = ShellVerbosity.STREAM
        )
        attachedYourKitProgrammaticallyAtRuntime = true
        return r
    }

}