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

    override var startTime: Long? = null

    override fun local(prefix: String): Prints {
        return PrefixAppendLogger(prefix = prefix, appendLogger = this)
    }


    override fun printNoNewline(a: Any) {
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


    override fun printLog(s: String) {
        printNoNewline(s)
    }

    open fun postLog() = Unit
}


class PrefixAppendLogger(private val appendLogger: AppendLogger, private val prefix: String) : Prints {
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