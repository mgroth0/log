package matt.log.profile.data

import kotlinx.serialization.Serializable
import matt.log.profile.mem.MemReport
import matt.time.UnixTime

@Serializable
class TestSession(
  val tests: MutableList<TestResults> = mutableListOf()
)

@Serializable
class TestResults(
  val name: String,
  val loadMillis: Long
)


fun ramSample(): RamSample {
  val report = MemReport()
  return RamSample(
	timestampMillis = UnixTime().duration.inWholeMilliseconds,
	usedBytes = report.used.bytes,
	maxBytes = report.max.bytes,
	totalBytes = report.total.bytes
  )
}

@Serializable class RamSample(
  val timestampMillis: Long,
  val usedBytes: Long,
  val maxBytes: Long,
  val totalBytes: Long
)