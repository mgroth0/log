package matt.log.logger

import matt.log.level.MattLogLevel
import matt.log.level.MattLogLevel.DEBUG
import matt.log.level.MattLogLevel.ERROR
import matt.log.level.MattLogLevel.INFO
import matt.log.level.MattLogLevel.PROFILE
import matt.log.level.MattLogLevel.TRACE
import matt.log.level.MattLogLevel.WARN
import matt.model.code.report.Reporter
import matt.model.op.prints.Prints

interface Logger : Reporter, Prints {
    fun printNoNewline(a: Any)
    fun log(a: Any) = printLog(a)
    fun printLog(s: String)
    fun printLog(a: Any?) = printLog(a.toString())
    override fun print(a: Any) = printNoNewline(a)
    override fun println(a: Any) = printNoNewline(a.toString() + "\n")
    fun printWithNewline(a: Any?) = printNoNewline(a.toString() + "\n")
    fun tab(s: Any?) = printLog("\t$s")
    operator fun plusAssign(s: Any) = println(s.toString())
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

    override var level = WARN

    override fun logError(s: Any?) {
        if (level >= ERROR) printWithNewline(s)
    }

    override fun warn(s: Any?) {
        if (level >= WARN) printWithNewline(s)
    }

    override fun info(a: Any?) {
        if (level >= INFO) printWithNewline(a)
    }

    override fun debug(s: Any?) {
        if (level >= DEBUG) printWithNewline(s)
    }

    override fun trace(s: Any?) {
        if (level >= TRACE) printWithNewline(s)
    }

    override fun profile(s: Any?) {
        if (level >= PROFILE) printWithNewline(s)
    }

}

class LazyString(op: () -> String) : CharSequence {
    private val s by lazy { op() }
    override val length: Int
        get() = s.length

    override fun get(index: Int): Char {
        return s[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return s.subSequence(startIndex, endIndex)
    }

    override fun toString(): String {
        return s
    }
}