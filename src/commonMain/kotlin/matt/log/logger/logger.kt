package matt.log.logger

import matt.lang.anno.Open
import matt.lang.anno.SeeURL
import matt.log.level.MattLogLevel
import matt.log.level.MattLogLevel.DEBUG
import matt.log.level.MattLogLevel.ERROR
import matt.log.level.MattLogLevel.INFO
import matt.log.level.MattLogLevel.PROFILE
import matt.log.level.MattLogLevel.TRACE
import matt.log.level.MattLogLevel.WARN
import matt.model.code.report.Reporter
import matt.model.op.prints.Prints
import kotlin.io.print as kotlinPrint
import kotlin.io.println as kotlinPrintln


class SimplePrefixLogger(private val prefix: String): LoggerImpl() {
    override fun printLog(s: String) {
        println(prefix + s)
    }

    override var startTime: Long?
        get() = TODO("Not yet implemented")
        set(value) {
            TODO("Not yet implemented")
        }

    override fun local(prefix: String): Prints = SimplePrefixLogger(this.prefix + prefix)

    override fun print(a: Any) {
        print(prefix + a.toString())
    }
}

class CalculatedPrefixLogger(private val prefix: () -> String): LoggerImpl() {
    override fun printLog(s: String) {
        kotlinPrintln(prefix() + s)
    }

    override var startTime: Long?
        get() = TODO("Not yet implemented")
        set(value) {
            TODO("Not yet implemented")
        }

    override fun local(prefix: String): Prints {
        TODO("Not yet implemented")
    }

    override fun print(a: Any) {
        kotlinPrint(prefix() + a.toString())
    }
}



@SeeURL("https://www.wikiwand.com/en/Cross-cutting_concern")
interface CrossCuttingConcern

interface Logger : Reporter, Prints, CrossCuttingConcern {
    @Open
    fun log(a: Any) = printLog(a)
    fun printLog(s: String)
    @Open
    fun printLog(a: Any?) = printLog(a.toString())

    var startTime: Long?

    var level: MattLogLevel

    /*DO NOT CALL THIS JUST "error" OR I WILL MIX IT UP WITH THE BUILT IN FUNCTION THAT THROWS EXCEPTIONS. Which would be very bad. It happened once, and it was bad. */
    fun logError(s: Any?)
    fun warn(s: Any?)
    fun info(a: Any?)
    fun debug(s: Any?)
    fun trace(s: Any?)
    fun profile(s: Any?)
}


abstract class LoggerImpl() : Logger {

    final override var level = WARN

    final override fun logError(s: Any?) {
        if (level >= ERROR) printLog(s)
    }

    final override fun warn(s: Any?) {
        if (level >= WARN) printLog(s)
    }

    final override fun info(a: Any?) {
        if (level >= INFO) printLog(a)
    }

    final override fun debug(s: Any?) {
        if (level >= DEBUG) printLog(s)
    }

    final override fun trace(s: Any?) {
        if (level >= TRACE) printLog(s)
    }

    final override fun profile(s: Any?) {
        if (level >= PROFILE) printLog(s)
    }
}

class LazyString(op: () -> String) : CharSequence {
    private val s by lazy { op() }
    override val length: Int
        get() = s.length

    override fun get(index: Int): Char = s[index]

    override fun subSequence(
        startIndex: Int,
        endIndex: Int
    ): CharSequence = s.subSequence(startIndex, endIndex)

    override fun toString(): String = s
}
