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

interface Logger: Reporter, Prints {
  fun printNoNewline(a: Any)
  fun log(a: Any) = printLog(a)
  fun printLog(s: String)
  fun printLog(a: Any?) = printLog(a.toString())
  override fun print(a: Any) = printLog(a)
  override fun println(a: Any) = printLog(a.toString() + "\n")
  fun tab(s: Any?) = printLog("\t$s")
  operator fun plusAssign(s: Any) = println(s.toString())
  var startTime: Long?

  var level: MattLogLevel

  fun error(s: Any?)
  fun warn(s: Any?)
  fun info(a: Any?)
  fun debug(s: Any?)
  fun trace(s: Any?)
  fun profile(s: Any?)
}

abstract class LoggerImpl(): Logger {

  override var level = WARN

  override fun error(s: Any?) {
	if (level >= ERROR) printLog(s)
  }

  override fun warn(s: Any?) {
	if (level >= WARN) printLog(s)
  }

  override fun info(a: Any?) {
	if (level >= INFO) printLog(a)
  }

  override fun debug(s: Any?) {
	if (level >= DEBUG) printLog(s)
  }

  override fun trace(s: Any?) {
	if (level >= TRACE) printLog(s)
  }

  override fun profile(s: Any?) {
	if (level >= PROFILE) printLog(s)
  }

}