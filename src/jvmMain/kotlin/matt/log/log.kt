@file:JvmName("LogJvmKt")

package matt.log

import matt.lang.NOT_IMPLEMENTED
import matt.prim.str.joinWithCommas
import java.io.ByteArrayOutputStream
import java.io.Flushable
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.charset.StandardCharsets


fun <T> logInvokation(vararg withstuff: Any, f: ()->T): T {
  val withstr = if (withstuff.isEmpty()) "" else " with $withstuff"
  println("running $f $withstr")
  val rrr = f()
  println("finished running $f")
  return rrr
}


class Printer(private val pw: PrintWriter): Prints {
  override fun println(a: Any) = pw.println(a)
  override fun print(a: Any) = pw.print(a)
}


fun Exception.printStackTraceToString(): String {
  val baos = ByteArrayOutputStream()
  val utf8: String = StandardCharsets.UTF_8.name()
  printStackTrace(PrintStream(baos, true, utf8))
  val data = baos.toString(utf8)
  return data
}


open class HasLogger(val log: Logger) {
  inline fun <R> decorate(vararg params: Any?, debugStack: Boolean = false, op: ()->R): R = decorateGlobal(
	log,
	*params,
	depth = 2,
	debugStack = debugStack,
	op = op
  )
}


open class AppendLogger(
  private val logfile: Appendable? = null,
): Logger {

  var includeTimeInfo: Boolean = true

  override var startTime: Long? = null


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
	printNoNewline(s + "\n")
  }

  open fun postLog() = Unit
}

val SystemOutLogger by lazy { AppendLogger(System.out) }
val SystemErrLogger by lazy { AppendLogger(System.err) }
val NOPLogger by lazy { AppendLogger(null) }
val NONE by lazy { NOPLogger }

class MultiLogger(private vararg val loggers: Logger): Logger {
  override var startTime: Long?
	get() = NOT_IMPLEMENTED
	set(value) {
	  loggers.forEach { it.startTime = value }
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
  op: ()->R
): R {
  val t = Thread.currentThread()
  val stack = t.stackTrace
  if (debugStack) {
	println("DEBUG STACK")
	stack.toList().take(10).forEach {
	  tab(it)
	}
  }
  //  println("getting mine")
  val maybeThisFarBack = stack[depth]
  //  println("got mine")
  val m = maybeThisFarBack.methodName
  //  println("m=${m}")
  log += "starting $m(${params.joinWithCommas()})"
  //  println("starting $m(${params.joinWithCommas()})")
  val r = op()
  log += "finished running $m, result=$r"
  //  println("finished running $m, result=$r")
  return r
}
