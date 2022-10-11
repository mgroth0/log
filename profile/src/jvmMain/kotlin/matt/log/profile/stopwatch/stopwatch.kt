package matt.log.profile.stopwatch

import matt.async.EveryFirst.OP
import matt.async.everyDaemon
import matt.collect.dmap.withStoringDefault
import matt.lang.preciseTime
import matt.lang.sync
import matt.log.reporter.Reporter
import matt.prim.str.addSpacesUntilLengthIs
import java.io.PrintWriter
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.MILLISECONDS

fun <R> stopwatch(s: String, enabled: Boolean = true, op: Stopwatch.()->R): R {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  val t = tic(s, enabled = enabled)
  val r = t.op()
  t.toc("stop")
  return r
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

class Stopwatch(
  startRelative: Duration,
  var enabled: Boolean = true,
  val printWriter: PrintWriter? = null,
  val prefix: String? = null,
  val silent: Boolean = false
): Reporter {

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

  infix fun toc(a: Any?): Duration? {
	if (enabled) {
	  val stop = preciseTime()
	  val dur = stop - startRelative
	  record += stop to a.toString()
	  if (!silent) {

		printFun("${dur.toString(MILLISECONDS, decimals = 3).addSpacesUntilLengthIs(10)}\t$prefixS$a")
	  }
	  return dur
	}
	return null
  }
}

private val ticSyncer = object {}

private var globalsw: Stopwatch? = null

val keysForNestedStuffUsedRecently by lazy {
  mutableMapOf<String, Int>().apply {
	everyDaemon(2.seconds, first = OP) {
	  ticSyncer.sync { clear() }
	}
  }
}


private val prefixSampleIs = mutableMapOf<String?, Int>().withStoringDefault { 0 }