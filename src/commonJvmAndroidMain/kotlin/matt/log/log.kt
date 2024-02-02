@file:JvmName("LogJvmAndroidKt")

package matt.log

import matt.log.logger.LoggerImpl
import matt.model.op.prints.Prints
import java.io.Flushable


open class AppendLogger(
    private val logfile: Appendable? = null,
) : LoggerImpl() {

    /*  fun copy(): AppendLogger {
        return AppendLogger(logfile = logfile).also {
          it.includeTimeInfo = includeTimeInfo
        }
      }*/

    var includeTimeInfo: Boolean = true

    final override var startTime: Long? = null

    final override fun local(prefix: String): Prints = PrefixAppendLogger(prefix = prefix, appendLogger = this)


    final override fun print(a: Any) {
        if (includeTimeInfo) {
            val now = System.currentTimeMillis()
            val dur = startTime?.let { now - it }
            val line = "[$now][$dur] $a"
            logfile?.append(line)
        } else {
            logfile?.append(a.toString())
        }

        (logfile as? Flushable)?.flush()
        postLog()
    }


    final override fun printLog(s: String) {
        print(s)
    }

    open fun postLog() = Unit
}


class PrefixAppendLogger(
    private val appendLogger: AppendLogger,
    private val prefix: String
) : Prints {
    override fun local(prefix: String): Prints {
        TODO()
    }

    override fun println(a: Any) {
        appendLogger.println(prefix + a)
    }

    override fun print(a: Any) {
        appendLogger.print(prefix + a)
    }

}


val SystemErrLogger by lazy { AppendLogger(System.err) }
val NOPLogger by lazy { AppendLogger(null) }
val NONE by lazy { NOPLogger }

val DEFAULT_SESSION_LOGGER by lazy {
    NOPLogger
}
