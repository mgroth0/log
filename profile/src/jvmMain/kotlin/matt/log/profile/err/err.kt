package matt.log.profile.err

import matt.async.thread.namedThread
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.profile.err.ExceptionResponse.IGNORE
import matt.log.profile.err.ExceptionResponse.THROW
import matt.log.report.BugReport
import matt.model.code.errreport.Report
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess


enum class ExceptionResponse { EXIT, IGNORE, THROW }

typealias ExceptionHandler = (Throwable, Report) -> ExceptionResponse


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

abstract class StructuredExceptionHandler : UncaughtExceptionHandler {
    abstract fun handleException(
        t: Thread,
        e: Throwable,
        report: Report
    ): ExceptionResponse

    private var gotOne = object : ThreadLocal<Boolean>() {
        override fun initialValue(): Boolean {
            return false
        }
    }

    final override fun uncaughtException(
        t: Thread,
        e: Throwable
    ) {
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
                namedThread(isDaemon = true, name = "uncaughtException Thread") {
                    /*needs to be in thread to avoid *circular blockage of threads waiting for other threads to end in shutdown process*/
                    exitProcess(1)
                }

            }

            IGNORE -> {
                println("ignoring that exception")
            }

            THROW  -> {
                throw e
            }
        }
    }
}



