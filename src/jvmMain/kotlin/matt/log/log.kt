@file:JvmName("LogJvmKt")

package matt.log

import matt.lang.NOT_IMPLEMENTED
import matt.lang.atomic.AtomicInt
import matt.lang.charset.DEFAULT_CHARSET
import matt.log.level.MattLogLevel.INFO
import matt.log.level.MattLogLevel.PROFILE
import matt.log.logger.Logger
import matt.log.logger.LoggerImpl
import matt.model.ctx.ShowContext
import matt.model.op.prints.Prints
import matt.prim.str.joinWithCommas
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.PrintWriter

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

context(ShowContext)
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
    override fun local(prefix: String): Prints {
        return PrefixPrinter(prefix = this.prefix + prefix, pw = pw)
    }

    override fun println(a: Any) {
        pw.println(prefix + a)
    }

    override fun print(a: Any) {
        pw.print(prefix + a)
    }

}

class Printer(private val pw: PrintWriter) : Prints {
    override fun local(prefix: String): Prints {
        return PrefixPrinter(pw = pw, prefix = prefix)
    }

    override fun println(a: Any) = pw.println(a)
    override fun print(a: Any) = pw.print(a)
}


fun Exception.printStackTraceToString(): String {
    val baos = ByteArrayOutputStream()
    val utf8: String = DEFAULT_CHARSET.name()
    printStackTrace(PrintStream(baos, true, utf8))
    val data = baos.toString(utf8)
    return data
}


open class HasLogger(val log: Logger) {
    inline fun <R> decorate(
        vararg params: Any?,
        debugStack: Boolean = false,
        op: () -> R
    ): R = decorateGlobal(
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

    override fun printNoNewline(a: Any) {
        loggers.forEach { it.printNoNewline(a) }
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
