package matt.log.profile.mem

import matt.log.report.desktop.MemReport
import matt.model.data.byte.mebibytes
import matt.time.dur.sleep
import kotlin.time.Duration.Companion.seconds


private val THROTTLE_THRESHOLD = 100.mebibytes
private val THROTTLE_INTERVAL = 1.seconds
fun throttle(label: String) {
    if (MemReport().spaceToGrow < THROTTLE_THRESHOLD) {
        println("throttling $label because there is < $THROTTLE_THRESHOLD space to grow...")
        sleep(THROTTLE_INTERVAL)
    }
}
