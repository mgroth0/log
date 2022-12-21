package matt.log.profile.yk

import com.yourkit.api.controller.Controller
import com.yourkit.api.controller.CpuProfilingSettings
import matt.file.commons.YOUR_KIT_APP_FOLDER
import matt.kjlib.shell.shell
import matt.lang.function.Op


object YourKit {


  private val controller by lazy {
	println("building YourKit Controller...")
	Controller.newBuilder().self().build()
  }

  fun recordCPU(enable: Boolean = true, op: Op) {
	if (enable) {
	  println("clearing CPU data...")
	  controller.clearCpuData()
	  println("starting CPU recording...")
	  controller.startSampling(CpuProfilingSettings())
	  println("running op in CPU recording...")
	}
	op()
	if (enable) {
	  println("stopping CPU recording...")
	  controller.stopCpuProfiling()
	  println("stopped CPU recording")
	}
  }

  fun captureAndOpenMemorySnapshot() {
	println("capturing memory snapshot...")
	val snapshotFilePath = controller.captureMemorySnapshot()
	println("Own memory snapshot captured: $snapshotFilePath")
	println("opening snapshot")
	/*https://www.yourkit.com/forum/viewtopic.php?t=43490*/
	shell("open", "-a", YOUR_KIT_APP_FOLDER.abspath, "-open", snapshotFilePath)
  }
}