package matt.log.profile.mem

import matt.lang.RUNTIME
import matt.model.byte.ByteSize
import matt.model.byte.megabytes
import matt.model.errreport.Report
import matt.time.dur.sleep
import kotlin.time.Duration.Companion.seconds

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

private val THROTTLE_THRESHOLD = 100.megabytes
private val THROTTLE_INTERVAL = 1.seconds
fun throttle(label: String) {
  val free = ByteSize(RUNTIME.freeMemory())
  if (free < THROTTLE_THRESHOLD) {
	println("throttling $label because there is < $THROTTLE_THRESHOLD free...")
	sleep(THROTTLE_INTERVAL)
  }
}
