@file:OptIn(ExperimentalContracts::class, DelicateCoroutinesApi::class)

package matt.log.profile

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import matt.collect.dmap.withStoringDefault
import matt.lang.RUNTIME
import matt.lang.async.EveryFirst.OP
import matt.lang.async.everyDaemon
import matt.lang.preciseTime
import matt.lang.sync
import matt.lang.unixTime
import matt.log.tab
import matt.math.median
import matt.model.byte.ByteSize
import matt.prim.str.addSpacesUntilLengthIs
import java.io.PrintWriter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.MILLISECONDS


fun println_withtime(s: String) {
  println(unixTime().toString() + ":" + s)
}

@OptIn(ExperimentalContracts::class)
fun <R> stopwatch(s: String, enabled: Boolean = true, op: ()->R): R {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  val start = if (enabled) run {
	println("timing ${s}...")
	preciseTime()
  } else null

  val r = op()
  if (enabled) {
	requireNotNull(start)
	val stop = preciseTime()
	println("$s took ${stop - start}")
  }
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


	runBlocking {

	}

	everyDaemon(2.seconds, first = OP) {
	  ticSyncer.sync { clear() }
	}
  }
}


fun tic(
  enabled: Boolean = true,
  printWriter: PrintWriter? = null,
  keyForNestedStuff: String? = null,
  nestLevel: Int = 1,
  prefix: String? = null,
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
	println() *//*to visually space this stopwatch print statements*//*
  }*/



  return sw
}

private var globalsw: Stopwatch? = null
fun globaltic(enabled: Boolean = true) {
  globalsw = tic(enabled = enabled)
}

fun globaltoc(s: String) {
  if (globalsw == null) {
	println("gotta use globaltic first:${s}")
  } else {
	globalsw!!.toc(s)
  }
}

inline fun <R> withStopwatch(s: String, op: (Stopwatch)->R): R {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  val t = tic()
  t.toc("starting stopwatch: $s")
  val r = op(t)
  t.toc("finished stopwatch: $s")
  return r
}


class ProfiledBlock(val key: String, val onlyDeepest: Boolean = true, val allowRecursion: Boolean = false) {
  companion object {
	val instances = mutableMapOf<String, ProfiledBlock>().withStoringDefault { ProfiledBlock(key = it) }
	operator fun get(s: String) = instances[s]
	fun reportAll() {
	  instances.forEach {
		it.value.report()
	  }
	}
  }

  val times = mutableListOf<Duration>()
  var lastTic: Stopwatch? = null
  inline fun <R> with(op: ()->R): R {
	val t = tic(silent = true)
	lastTic = t
	val r = op()
	require(allowRecursion || lastTic == t) {
	  "recursion is not allowed in this profiled block"
	}
	if (!onlyDeepest || t == lastTic) {
	  times += t.toc("")!!
	}
	return r
  }

  fun report() {
	println("${ProfiledBlock::class.simpleName} $key Report")
	tab("count\t${times.count()}")
	val mn = times.withIndex().minBy { it.value.inWholeMilliseconds }
	tab("min(idx=${mn.index})\t${mn.value}")
	tab("mean\t${times.map { it.inWholeMilliseconds }.toLongArray().average()}")
	tab("median\t${times.map { it.inWholeMilliseconds }.median()}")
	val mx = times.withIndex().maxBy { it.value.inWholeMilliseconds }
	tab("max(idx=${mx.index})\t${mx.value}")
	tab("sum\t${times.sumOf { it.inWholeMilliseconds }}")
  }
}

class MemReport {
  val total = ByteSize(RUNTIME.totalMemory())
  val max = ByteSize(RUNTIME.maxMemory())
  val free = ByteSize(RUNTIME.freeMemory())
  override fun toString(): String {
	var s = ""
	s += "heapsize:${total}\n"
	s += "heapmaxsize:${max}\n"
	s += "heapFreesize:${free}"
	return s
  }
}
