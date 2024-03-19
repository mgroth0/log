@file:OptIn(ExperimentalContracts::class)

package matt.log.profile.stopwatch

import matt.collect.map.dmap.inter.withStoringDefault
import matt.lang.anno.Duplicated
import matt.lang.common.NOT_IMPLEMENTED
import matt.lang.sync.common.ReferenceMonitor
import matt.lang.sync.inSync
import matt.log.reporter.TracksTime
import matt.model.op.prints.Prints
import matt.prim.str.addSpacesUntilLengthIs
import matt.time.largestFullUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit.NANOSECONDS
import kotlin.time.TimeSource.Monotonic
import kotlin.io.println as kotlinPrintln


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
    val sw =
        Stopwatch(
            start,
            enabled = enabled,
            printWriter = printWriter,
            prefix = prefix,
            silent = silent,
            threshold = threshold
        )
    return sw
}


fun globalTic(enabled: Boolean = true) {
    globalSw = tic(enabled = enabled)
}

fun globalToc(s: String) {
    if (globalSw == null) {
        kotlinPrintln("gotta use globaltic first:$s")
    } else {
        globalSw!!.toc(s)
    }
}


@Duplicated(4958763)
class Stopwatch(
    start: ComparableTimeMark = Monotonic.markNow(),
    var enabled: Boolean = true,
    val printWriter: Prints? = null,
    val prefix: String? = null,
    val silent: Boolean = false,
    private val threshold: Duration? = null
) : TracksTime, Prints, ReferenceMonitor {

    override fun local(prefix: String): Stopwatch = tic(prefix)

    var start: ComparableTimeMark = start
        private set

    override fun reset() {
        start = Monotonic.markNow()
    }

    companion object {
        val globalInstances = mutableMapOf<String, Stopwatch>()
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

    fun increments() =
        record.mapIndexed { index, (l, s) ->
            if (index == 0) 0L.milliseconds to s
            else (l - record[index - 1].first) to s
        }


    var storePrints = false
    var storedPrints = ""
    @Suppress("MemberVisibilityCanBePrivate")
    fun printFun(s: String) {
        if (storePrints) {
            storedPrints += s + "\n"
        } else if (printWriter == null) kotlinPrintln(s)
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
            kotlinPrintln(a)
        }
    }

    override fun print(a: Any) = NOT_IMPLEMENTED
}

private var globalSw: Stopwatch? = null


private val prefixSampleIs = mutableMapOf<String?, Int>().withStoringDefault { 0 }
