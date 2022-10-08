package matt.log.profile

import matt.async.EveryFirst.OP
import matt.async.everyDaemon
import matt.collect.dmap.withStoringDefault
import matt.lang.RUNTIME
import matt.lang.preciseTime
import matt.lang.sync
import matt.lang.unixTime
import matt.log.profile.ExceptionResponse.EXIT
import matt.log.profile.ExceptionResponse.IGNORE
import matt.log.profile.ProfileRecursionType.ALL
import matt.log.profile.ProfileRecursionType.DEEPEST_ONLY
import matt.log.profile.ProfileRecursionType.NOT_ALLOWED
import matt.log.profile.ProfileRecursionType.TOP_ONLY
import matt.log.profile.ProfiledBlock.Companion
import matt.log.report
import matt.math.reduce.median
import matt.model.byte.ByteSize
import matt.model.errreport.Report
import matt.model.errreport.ThrowReport
import matt.model.successorfail.SuccessOrFail
import matt.model.successorfail.SuccessOrFail.FAIL
import matt.model.successorfail.SuccessOrFail.SUCCESS
import matt.prim.str.addSpacesUntilLengthIs
import matt.prim.str.build.t
import matt.prim.str.joinWithNewLines
import java.io.PrintWriter
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.MILLISECONDS

enum class ExceptionResponse { EXIT, IGNORE }

typealias ExceptionHandler = (Throwable, Report)->ExceptionResponse


/*null means there was an exception (but EXIT wasn't returned)*/
fun ExceptionHandler.with(op: ()->Unit): SuccessOrFail {
  return try {
	op()
	SUCCESS
  } catch (e: Exception) {
	when (this(e, BugReport(Thread.currentThread(), e))) {
	  EXIT   -> exitProcess(1)
	  IGNORE -> FAIL
	}
  }
}

val defaultExceptionHandler: ExceptionHandler = { _, r ->
  r.print()
  ExceptionResponse.EXIT
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
	  ExceptionResponse.EXIT   -> {
		println("ok really exiting")
		exitProcess(1)
	  }

	  ExceptionResponse.IGNORE -> {
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


fun printlnWithTime(s: String) {
  println(unixTime().toString() + ":" + s)
}

fun <R> stopwatch(s: String, enabled: Boolean = true, op: Stopwatch.()->R): R {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  val t = tic(s, enabled = enabled)
  val r = t.op()
  t.toc("stop")
  return r
}

val prefixSampleIs = mutableMapOf<String?, Int>().withStoringDefault { 0 }

class Stopwatch(
  startRelative: Duration,
  var enabled: Boolean = true,
  val printWriter: PrintWriter? = null,
  val prefix: String? = null,
  val silent: Boolean = false
) {

  var startRelative: Duration = startRelative
	private set

  fun reset() {
	startRelative = preciseTime()
  }

  companion object {
	val globalInstances = mutableMapOf<String, Stopwatch>()
  }

  var i = 0

  fun <R> sampleEvery(period: Int, op: Stopwatch.()->R): R {
	i++
	enabled = i == period
	val r = this.op()
	if (enabled) {
	  i = 0
	}
	return r
  }

  fun <R> sampleEveryByPrefix(period: Int, onlyIf: Boolean = true, op: Stopwatch.()->R): R {
	if (onlyIf) {
	  prefixSampleIs[prefix]++
	  enabled = prefixSampleIs[prefix] == period
	}
	val r = this.op()
	if (onlyIf) {
	  if (enabled) {
		prefixSampleIs[prefix] = 0
	  }
	}
	return r
  }

  private val prefixS = if (prefix != null) "$prefix\t" else ""

  val record = mutableListOf<Pair<Duration, String>>()

  fun increments() = record.mapIndexed { index, (l, s) ->
	if (index == 0) 0L.milliseconds to s
	else (l - record[index - 1].first) to s
  }

  //	record.entries.runningFold(0L to "START") { acc, it ->
  //	((it.key - acc.first) - startRelativeNanos) to it.value
  //  }

  var storePrints = false
  var storedPrints = ""
  fun printFun(s: String) {
	if (storePrints) {
	  storedPrints += s + "\n"
	} else if (printWriter == null) println(s)
	else printWriter.println(s)
  }

  infix fun toc(s: Any): Duration? {
	if (enabled) {
	  val stop = preciseTime()
	  val dur = stop - startRelative
	  record += stop to s.toString()
	  if (!silent) {

		printFun("${dur.toString(MILLISECONDS, decimals = 3).addSpacesUntilLengthIs(10)}\t$prefixS$s")
	  }
	  return dur
	}
	return null
  }
}

private val ticSyncer = object {}


val keysForNestedStuffUsedRecently by lazy {
  mutableMapOf<String, Int>().apply {
	everyDaemon(2.seconds, first = OP) {
	  ticSyncer.sync { clear() }
	}
  }
}


fun tic(
  prefix: String? = null,
  enabled: Boolean = true,
  printWriter: PrintWriter? = null,
  keyForNestedStuff: String? = null,
  nestLevel: Int = 1,
  silent: Boolean = false
): Stopwatch {
  var realEnabled = enabled
  if (enabled) {
	ticSyncer.sync {
	  if (keyForNestedStuff in keysForNestedStuffUsedRecently && nestLevel == keysForNestedStuffUsedRecently[keyForNestedStuff]) {
		realEnabled = false
	  } else if (keyForNestedStuff != null) {
		if (keyForNestedStuff in keysForNestedStuffUsedRecently) {
		  keysForNestedStuffUsedRecently[keyForNestedStuff] = keysForNestedStuffUsedRecently[keyForNestedStuff]!! + 1
		} else {
		  keysForNestedStuffUsedRecently[keyForNestedStuff] = 1
		}
	  }
	}
  }
  val start = preciseTime()
  val sw = Stopwatch(start, enabled = realEnabled, printWriter = printWriter, prefix = prefix, silent = silent)
  /*if (realEnabled && !simplePrinting) {
	println() *//*to visually space this matt.log.profile.stopwatch print statements*//*
  }*/



  return sw
}

private var globalsw: Stopwatch? = null
fun globaltic(enabled: Boolean = true) {
  globalsw = tic(enabled = enabled)
}

fun globaltoc(s: String) {
  if (globalsw == null) {
	println("gotta use matt.log.profile.globaltic first:${s}")
  } else {
	globalsw!!.toc(s)
  }
}

inline fun <R> withStopwatch(s: String, op: (Stopwatch)->R): R {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  val t = tic()
  t.toc("starting matt.log.profile.stopwatch: $s")
  val r = op(t)
  t.toc("finished matt.log.profile.stopwatch: $s")
  return r
}

private var recursionChecker: Any? = null

@Synchronized
fun profile(name: String = "insert matt.log.profile.profile name here", op: ()->Unit) {
  Companion.clearInstanceMap()
  recursionChecker = object {}
  val myRecursionChecker = recursionChecker
  op()
  require(myRecursionChecker == recursionChecker)
  Companion.reportAll(profileName = name)
}

enum class ProfileRecursionType {
  NOT_ALLOWED,
  DEEPEST_ONLY,
  TOP_ONLY,
  ALL
}

class ProfiledBlock(
  val key: String,
  val recursionType: ProfileRecursionType = NOT_ALLOWED
) {
  companion object {
	private val instances = mutableMapOf<String, ProfiledBlock>()
	operator fun get(s: String, recursionType: ProfileRecursionType = NOT_ALLOWED) =
	  instances[s]?.also { require(it.recursionType == recursionType) } ?: ProfiledBlock(
		key = s, recursionType = recursionType
	  )

	fun reportAll(profileName: String? = "insert matt.log.profile.profile name here") {
	  report("Profile: $profileName", instances.values.joinWithNewLines { it.reportString() })
	}

	fun clearInstanceMap() {
	  instances.clear()
	}


  }

  init {
	require(key !in instances)
	instances[key] = this
  }

  val times = mutableListOf<Duration>()
  fun clear() = times.clear()
  var lastTic: Stopwatch? = null
  inline fun <R> with(op: ()->R): R {
	val t = tic(silent = true)
	val isInRecursion = lastTic != null
	lastTic = t
	val r = op()
	val didRecurse = lastTic == null
	val didNotRecurse = !didRecurse
	lastTic = null
	require(recursionType != NOT_ALLOWED || didNotRecurse) {
	  "recursion is not allowed in this profiled block"
	}
	when (recursionType) {
	  NOT_ALLOWED  -> times += t.toc("")!!
	  DEEPEST_ONLY -> if (didNotRecurse) times += t.toc("")!!
	  TOP_ONLY     -> if (!isInRecursion) times += t.toc("")!!
	  ALL          -> times += t.toc("")!!
	}
	return r
  }

  fun report() {
	println(reportString())
  }

  fun reportString() = buildString {
	appendLine("${ProfiledBlock::class.simpleName} $key Report")
	t.appendLine("r-type\t${recursionType}")
	t.appendLine("count\t${times.count()}")
	if (recursionType != ALL) {
	  val mn = times.withIndex().minBy { it.value }
	  t.appendLine("min(idx=${mn.index})\t${mn.value}")
	  val sum = times.reduce { a, b -> a + b }
	  t.appendLine("mean\t${sum/times.size}")
	  t.appendLine("median\t${times.map { it/*.toMDuration()*/ }.median()}")
	  val mx = times.withIndex().maxBy { it.value }
	  t.appendLine("max(idx=${mx.index})\t${mx.value}")
	  t.appendLine("sum\t${sum}")
	}
  }
}

class MemReport: Report() {
  val total = ByteSize(RUNTIME.totalMemory())
  val max = ByteSize(RUNTIME.maxMemory())
  val free = ByteSize(RUNTIME.freeMemory())

  override val text by lazy {
	var s = ""
	s += "heapsize:${total}\n"
	s += "heapmaxsize:${max}\n"
	s += "heapFreesize:${free}"
	s
  }
}
