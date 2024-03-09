package matt.log.reporter

import matt.model.code.report.Reporter
import kotlin.time.Duration


interface TracksTime: Reporter {
    override fun local(prefix: String): TracksTime
    fun reset()
    fun toc(a: Any?): Duration?
}
