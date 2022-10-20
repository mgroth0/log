package matt.log.profile.err

import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.profile.err.ExceptionResponse.IGNORE
import matt.log.profile.mem.MemReport
import matt.model.errreport.Report
import matt.model.errreport.ThrowReport
import matt.model.successorfail.Fail
import matt.model.successorfail.Success
import matt.model.successorfail.SuccessOrFail
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.system.exitProcess

fun reportButIgnore(vararg clses: KClass<out java.lang.Exception>): ExceptionHandler = { e, r ->
  r.print()
  val response = when {
	clses.any { e::class.isSubclassOf(it) } -> IGNORE
	else                                    -> EXIT
  }
  response
}

enum class ExceptionResponse { EXIT, IGNORE }

typealias ExceptionHandler = (Throwable, Report)->ExceptionResponse


fun ExceptionHandler.with(op: ()->Unit) = withResult {
  op()
  Success
}

fun ExceptionHandler.withResult(op: ()->SuccessOrFail): SuccessOrFail {
  return try {
	op()
  } catch (e: Exception) {
	when (this(e, BugReport(Thread.currentThread(), e))) {
	  EXIT   -> exitProcess(1)
	  IGNORE -> Fail("${e::class.simpleName}")
	}
  }
}

val defaultExceptionHandler: ExceptionHandler = { _, r ->
  r.print()
  EXIT
}

abstract class StructuredExceptionHandler: UncaughtExceptionHandler {
  abstract fun handleException(t: Thread, e: Throwable, report: Report): ExceptionResponse
  private var gotOne = false
  final override fun uncaughtException(t: Thread, e: Throwable) {
	val report = BugReport(t, e)
	if (gotOne) {
	  println("wow, got an error in the error handler: ")
	  report.print()
	  exitProcess(1)
	}
	gotOne = true
	when (handleException(t, e, report)) {
	  EXIT   -> {
		println("ok really exiting")
		exitProcess(1)
	  }

	  IGNORE -> {
		println("ignoring that exception")
	  }
	}
  }
}

class BugReport(private val t: Thread?, private val e: Throwable?): Report() {
  val memReport = MemReport()
  val throwReport = ThrowReport(t, e)
  override val text by lazy {
	"""
	  RAM REPORT
	  $memReport
	  
	  THROW REPORT
	  $throwReport
	""".trimIndent()
  }

}
