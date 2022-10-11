package matt.log.profile.mem

import matt.lang.RUNTIME
import matt.model.byte.ByteSize
import matt.model.errreport.Report

class MemReport: Report() {
  val total = ByteSize(RUNTIME.totalMemory())
  val max = ByteSize(RUNTIME.maxMemory())
  val free = ByteSize(RUNTIME.freeMemory())

  override val text by lazy {
	var s = ""
	s += "heapsize:${total}\n"
	s += "heapmaxsize:${max}\n"
	s += "heapFreesize:${free}"
	s
  }
}
