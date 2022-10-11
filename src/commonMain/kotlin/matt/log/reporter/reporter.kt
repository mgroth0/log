package matt.log.reporter

import kotlin.time.Duration

interface Reporter

interface TracksTime: Reporter {
  fun toc(a: Any?): Duration?
}