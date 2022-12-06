package matt.log.profile.err

import matt.async.thread.daemon
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.profile.err.ExceptionResponse.IGNORE
import matt.log.profile.err.ExceptionResponse.THROW
import matt.log.profile.mem.MemReport
import matt.model.code.errreport.Report
import matt.model.code.errreport.ThrowReport
import matt.model.code.successorfail.Fail
import matt.model.code.successorfail.Success
import matt.model.code.successorfail.SuccessOrFail
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.system.exitProcess

fun reportButIgnore(vararg clses: KClass<out java.lang.Exception>): ExceptionHandler = { e, r ->
  r.print()
  val response = when {
	clses.any { e::class.isSubclassOf(it) } -> IGNORE
	else                                    -> THROW
  }
  response
}

enum class ExceptionResponse { EXIT, IGNORE, THROW }

typealias ExceptionHandler = (Throwable, Report)->ExceptionResponse


fun ExceptionHandler.with(vararg ignore: KClass<out java.lang.Exception>, op: ()->Unit) = withResult(*ignore) {
  op()
  Success
}

fun ExceptionHandler.withResult(vararg ignore: KClass<out java.lang.Exception>, op: ()->SuccessOrFail): SuccessOrFail {
  return try {
	op()
  } catch (e: Exception) {
	when (this(e, BugReport(Thread.currentThread(), e))) {
	  EXIT   -> when {
		ignore.any { e::class.isSubclassOf(it) } -> Fail("${e::class.simpleName}")
		else                                     -> {
		  daemon {
			/*needs to be in thread to avoid *circular blockage of threads waiting for other threads to end in shutdown process*/
			exitProcess(1)
		  }
		  Fail("${e::class.simpleName}")
		}
	  }

	  IGNORE -> Fail("${e::class.simpleName}")
	  THROW  -> throw e
	}
  }
}

val defaultExceptionHandler: ExceptionHandler = { _, r ->
  r.print()
  EXIT
}
val silentInterruptsExceptionHandler: ExceptionHandler = { t, r ->
  if (t !is InterruptedException) {
	r.print()
  }
  EXIT
}

abstract class StructuredExceptionHandler: UncaughtExceptionHandler {
  abstract fun handleException(t: Thread, e: Throwable, report: Report): ExceptionResponse
  private var gotOne = object: ThreadLocal<Boolean>() {
	override fun initialValue(): Boolean {
	  return false
	}
  }

  final override fun uncaughtException(t: Thread, e: Throwable) {
	val report = BugReport(t, e)
	if (gotOne.get()) {
	  println("wow, got an error in the error handler: ")
	  report.print()
	  exitProcess(1)
	}
	gotOne.set(true)
	when (handleException(t, e, report)) {
	  EXIT   -> {
		println("ok really exiting")
		daemon {
		  /*needs to be in thread to avoid *circular blockage of threads waiting for other threads to end in shutdown process*/
		  exitProcess(1)
		}

	  }

	  IGNORE -> {
		println("ignoring that exception")
	  }

	  THROW  -> throw e
	}
  }
}

class BugReport(t: Thread?, e: Throwable?): Report() {
  private val memReport = MemReport()
  private val throwReport = ThrowReport(t, e)
  override val text by lazy {
	"""
	  RAM REPORT
	  $memReport
	  
	  THROW REPORT
	  $throwReport
	""".trimIndent()
  }

}
