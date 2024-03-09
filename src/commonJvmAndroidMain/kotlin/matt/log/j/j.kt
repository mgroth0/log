
package matt.log.j

import matt.lang.atomic.AtomicInt
import matt.lang.common.NOT_IMPLEMENTED
import matt.log.level.MattLogLevel.INFO
import matt.log.level.MattLogLevel.PROFILE
import matt.log.logger.Logger
import matt.log.logger.LoggerImpl
import matt.log.tab
import matt.model.op.prints.Prints
import matt.model.op.prints.plusAssign
import matt.prim.str.joinWithCommas
import java.io.Flushable
import java.io.PrintWriter


open class AppendLogger(
    private val logfile: Appendable? = null
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


class CountPrinter(
    private val printEvery: Int? = null,
    private val print: (Int) -> String
) {
    private val count = AtomicInt()
    fun click(): Int {


        val i = count.incrementAndGet()
        if (printEvery == null || i % printEvery == 0) {
            println(print(i))
        }
        return i
    }
}

fun <T> logInvocation(
    vararg withStuff: Any,
    f: () -> T
): T {
    val withStr = if (withStuff.isEmpty()) "" else " with $withStuff"
    println("running $f $withStr")
    val rrr = f()
    println("finished running $f")
    return rrr
}


class PrefixPrinter(
    private val prefix: String,
    private val pw: PrintWriter
) : Prints {
    override fun local(prefix: String): Prints = PrefixPrinter(prefix = this.prefix + prefix, pw = pw)

    override fun println(a: Any) {
        pw.println(prefix + a)
    }

    override fun print(a: Any) {
        pw.print(prefix + a)
    }
}


class Printer(private val pw: PrintWriter) : Prints {
    override fun local(prefix: String): Prints = PrefixPrinter(pw = pw, prefix = prefix)

    override fun println(a: Any) = pw.println(a)
    override fun print(a: Any) = pw.print(a)
}



open class HasLogger(val log: Logger) {
    inline fun <R> decorate(
        vararg params: Any?,
        debugStack: Boolean = false,
        op: () -> R
    ): R =
        decorateGlobal(
            log,
            *params,
            depth = 2,
            debugStack = debugStack,
            op = op
        )
}


val SystemOutLogger by lazy {
    AppendLogger(System.out).also {
        if (System.getenv("VERBOSE")?.toBooleanStrict() == true) {
            it.level = PROFILE
        }
    }
}



val DefaultLogger by lazy {
    SystemOutLogger.apply {
        includeTimeInfo = false
    }
}

val InfoLogger by lazy {
    AppendLogger(System.out).apply {
        level = INFO
    }
}


class MultiLogger(private vararg val loggers: Logger) : LoggerImpl() {
    override var startTime: Long?
        get() = NOT_IMPLEMENTED
        set(value) {
            loggers.forEach { it.startTime = value }
        }

    override fun local(prefix: String): Prints {
        TODO()
    }

    override fun print(a: Any) {
        loggers.forEach { it.print(a) }
    }

    override fun printLog(s: String) {
        loggers.forEach { it.printLog(s) }
    }
}

/*inline might matter here. might change the place in the stack where I should look*/
inline fun <R> decorateGlobal(
    log: Logger,
    vararg params: Any?,
    depth: Int = 1,
    debugStack: Boolean = false,
    op: () -> R
): R {
    val t = Thread.currentThread()
    val stack = t.stackTrace
    if (debugStack) {
        println("DEBUG STACK")
        stack.toList().take(10).forEach {
            tab(it)
        }
    }
    val maybeThisFarBack = stack[depth]
    val m = maybeThisFarBack.methodName
    log += "starting $m(${params.joinWithCommas()})"
    val r = op()
    val resultString = if (r == Unit) "" else ", result=$r"
    log += "finished running $m$resultString"
    return r
}

context(matt.model.code.ctx.ShowContext)
class CountStatusEmitter(
    private val printEvery: Int? = null,
    private val print: (Int) -> String
) {
    private val count = AtomicInt()
    fun click(): Int {


        val i = count.incrementAndGet()
        if (printEvery == null || i % printEvery == 0) {
            showStatus(print(i))
        }
        return i
    }
}



