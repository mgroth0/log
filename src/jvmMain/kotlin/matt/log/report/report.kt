package matt.log.report

import matt.lang.RUNTIME
import matt.lang.arch
import matt.lang.myPid
import matt.lang.os
import matt.model.code.errreport.Report
import matt.model.code.errreport.ThrowReport
import matt.model.data.byte.ByteSize
import matt.prim.str.mybuild.string

class BugReport(t: Thread?, e: Throwable?): Report() {
  private val memReport = MemReport()
  private val throwReport = ThrowReport(t, e)
  private val sysReport = SystemReport()
  override val text by lazy {
	string {
	  lineDelimited {
		+"PID: $myPid"
		blankLine()
		+"SYSTEM REPORT"
		+sysReport
		blankLine()
		+"RAM REPORT"
		+memReport
		blankLine()
		+"THROW REPORT"
		+throwReport
	  }
	}
  }
}


class SystemReport {
  override fun toString(): String {
	return text
  }

  val text by lazy {
	"""
	OS: $os
	ARCH: $arch
	CPUS: ${Runtime.getRuntime().availableProcessors()}
	""".trimIndent()
  }
}


class MemReport: Report() {
  val total = ByteSize(RUNTIME.totalMemory())
  val max = ByteSize(RUNTIME.maxMemory())
  val free = ByteSize(RUNTIME.freeMemory())

  val used by lazy {
	total - free
  }
  val spaceToGrow by lazy {
	max - used
  }

  override val text by lazy {
	var s = ""
	s += "heapsize:${total}\n"
	s += "heapmaxsize:${max}\n"
	s += "heapFreesize:${free}"
	s
  }
}