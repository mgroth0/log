package matt.log.profile.stopwatch

import matt.collect.map.dmap.withStoringDefault
import matt.lang.NOT_IMPLEMENTED
import matt.lang.anno.Duplicated
import matt.lang.sync.ReferenceMonitor
import matt.lang.sync.inSync
import matt.log.reporter.TracksTime
import matt.model.op.prints.Prints
import matt.prim.str.addSpacesUntilLengthIs
import matt.time.largestFullUnit
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.NANOSECONDS
import kotlin.time.TimeSource.Monotonic


fun <R> stopwatch(
    s: String,
    enabled: Boolean = true,
    op: Stopwatch.() -> R
): R {
    contract {
        callsInPlace(op, EXACTLY_ONCE)
    }
    val t = tic(s, enabled = enabled)
    val r = t.op()
    t.toc("stop")
    return r
}

inline fun <R> withStopwatch(
    s: String,
    op: (Stopwatch) -> R
): R {
    contract {
        callsInPlace(op, EXACTLY_ONCE)
    }
    val t = tic()
    t.toc("starting stopwatch: $s")
    val r = op(t)
    t.toc("finished stopwatch: $s")
    return r
}


fun tic(
    prefix: String? = null,
    enabled: Boolean = true,
    printWriter: Prints? = null,
    silent: Boolean = false,
    threshold: Duration? = null
): Stopwatch {
    val start = Monotonic.markNow()
    val sw = Stopwatch(
        start,
        enabled = enabled,
        printWriter = printWriter,
        prefix = prefix,
        silent = silent,
        threshold = threshold
    )/*if (realEnabled && !simplePrinting) {
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


@Duplicated(4958763)
class Stopwatch(
    start: ComparableTimeMark = Monotonic.markNow(),
    var enabled: Boolean = true,
    val printWriter: Prints? = null,
    val prefix: String? = null,
    val silent: Boolean = false, //  val resetOnTic: Boolean = false
    private val threshold: Duration? = null
) : TracksTime, Prints, ReferenceMonitor {

    override fun local(prefix: String): Stopwatch = tic(prefix)

    override fun tic(prefix: String): Stopwatch =
        matt.log.profile.stopwatch.tic(prefix = prefix) /*full qualified or else*/

    var start: ComparableTimeMark = start
        private set

    override fun reset() {
        start = Monotonic.markNow()
    }

    companion object {
        val globalInstances = mutableMapOf<String, Stopwatch>()
        private val ONE_SEC = 1.seconds
    }

    var i = 0

    fun <R> sampleEvery(
        period: Int,
        op: Stopwatch.() -> R
    ): R {
        i++
        enabled = i == period
        val r = this.op()
        if (enabled) {
            i = 0
        }
        return r
    }

    fun <R> sampleEveryByPrefix(
        period: Int,
        onlyIf: Boolean = true,
        op: Stopwatch.() -> R
    ): R {
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

    val record = mutableListOf<Pair<ComparableTimeMark, String>>()

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
        } else if (printWriter == null) kotlin.io.println(s) /* must use fully qualified name here or else...*/
        else printWriter.println(s)
    }

    private fun Duration.formatDur() =
        if (this == Duration.ZERO) "0" else toString(largestFullUnit ?: NANOSECONDS, decimals = 3)


    override infix fun toc(a: Any?): Duration? {
        inSync(this) {
            if (enabled) {
                val stop = Monotonic.markNow()
                val dur = (stop - start)
                val durSinceLast = record.lastOrNull()?.first?.let { stop - it } ?: dur
                record += stop to a.toString()
                if (!silent && (threshold == null || durSinceLast >= threshold)) {
                    val absTime = dur.formatDur().addSpacesUntilLengthIs(10)
                    val relTime = durSinceLast.formatDur().addSpacesUntilLengthIs(10)
                    printFun(
                        "$absTime\t$relTime\t$prefixS$a"
                    )
                }
                return dur
            }
            return null
        }
    }

    override fun println(a: Any) {
        if (enabled) {
            toc(a)
        } else {
            kotlin.io.println(a)
        }

    }

    override fun print(a: Any) = NOT_IMPLEMENTED
}

private val ticSyncer = object {}

private var globalsw: Stopwatch? = null


private val prefixSampleIs = mutableMapOf<String?, Int>().withStoringDefault { 0 }