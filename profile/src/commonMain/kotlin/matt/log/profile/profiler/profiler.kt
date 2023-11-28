package matt.log.profile.profiler

import matt.lang.assertions.require.requireEquals
import matt.lang.assertions.require.requireNotIn
import matt.lang.sync.SimpleReferenceMonitor
import matt.lang.sync.inSync
import matt.log.profile.profiler.ProfileRecursionType.ALL
import matt.log.profile.profiler.ProfileRecursionType.DEEPEST_ONLY
import matt.log.profile.profiler.ProfileRecursionType.NOT_ALLOWED
import matt.log.profile.profiler.ProfileRecursionType.TOP_ONLY
import matt.log.profile.stopwatch.Stopwatch
import matt.log.profile.stopwatch.tic
import matt.log.report
import matt.prim.str.build.t
import matt.prim.str.joinWithNewLines
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.time.Duration

private val profileMonitor = SimpleReferenceMonitor()

fun profile(
    name: String = "insert profile name here",
    enabled: Boolean = true,
    op: () -> Unit
) {
    inSync(profileMonitor) {
        if (enabled) {
            ProfiledBlock.clearInstanceMap()
            recursionChecker = object {}
            val myRecursionChecker = recursionChecker
            op()
            requireEquals(myRecursionChecker, recursionChecker)
            ProfiledBlock.reportAll(profileName = name)
        } else {
            op()
        }
    }
}

enum class ProfileRecursionType {
    NOT_ALLOWED,
    DEEPEST_ONLY,
    TOP_ONLY,
    ALL
}


interface ProfileDSL {
    fun <R> subBlock(
        key: String,
        op: ProfileDSL.() -> R
    ): R
}


class ProfiledBlock(
    key: String,
    uniqueSuffix: Boolean = false,
    val recursionType: ProfileRecursionType = NOT_ALLOWED
) : ProfileDSL {
    private val realKey = if (uniqueSuffix) "$key${uniqueSuffixes.next()}" else key

    companion object {
        private val instances = mutableMapOf<String, ProfiledBlock>()
        operator fun get(
            s: String,
            recursionType: ProfileRecursionType = NOT_ALLOWED
        ): ProfiledBlock {

            return instances[s]?.also {
                requireEquals(it.recursionType, recursionType)
            } ?: ProfiledBlock(
                key = s, recursionType = recursionType
            )
        }

        fun reportAll(profileName: String? = "insert profile name here") {
            report("Profile: $profileName", instances.values.joinWithNewLines { it.reportString() })
        }

        fun clearInstanceMap() {
            instances.clear()
        }

        val uniqueSuffixes by lazy {
            (1..Int.MAX_VALUE).asSequence().map {
                "-$it"
            }.iterator()
        }

    }

    init {
        requireNotIn(this.realKey, instances)
        instances[this.realKey] = this
    }

    val times = mutableListOf<Duration>()
    fun clear() = times.clear()
    private var lastTic: Stopwatch? = null


    inner class BlockExecution(
        private val startInfo: StartInfo
    ) {
        fun stop() {
            val didRecurse = lastTic == null
            val didNotRecurse = !didRecurse
            lastTic = null
            require(recursionType != NOT_ALLOWED || didNotRecurse) {
                "recursion is not allowed in this profiled block (perpetrator = ${realKey})"
            }
            when (recursionType) {
                NOT_ALLOWED  -> times += startInfo.t.toc("")!!
                DEEPEST_ONLY -> if (didNotRecurse) times += startInfo.t.toc("")!!
                TOP_ONLY     -> if (!startInfo.isInRecursion) times += startInfo.t.toc("")!!
                ALL          -> times += startInfo.t.toc("")!!
            }
        }

    }

    class StartInfo internal constructor(
        val t: Stopwatch,
        val isInRecursion: Boolean
    )

    fun start(): BlockExecution {
        val t = tic(silent = true)
        val isInRecursion = lastTic != null
        lastTic = t
        return BlockExecution(StartInfo(t = t, isInRecursion = isInRecursion))
    }


    inline fun <R> with(

        enabled: Boolean = true,

        op: ProfileDSL.() -> R

    ): R {
        contract {
            callsInPlace(op, EXACTLY_ONCE)
        }
        if (enabled) {
            val blockExecution = start()
            val r = this.op()
            blockExecution.stop()
            return r
        } else return this.op()
    }


    fun report() {


        println(reportString())


    }

    private val STATS_SIZE = 100
    fun reportString(): String = buildString {
        appendLine("${ProfiledBlock::class.simpleName} $realKey Report (sample of ${STATS_SIZE})")
        t.appendLine("r-type\t${recursionType}")
        val reportTimes = times.toList()

        val reportTimesForStats = if (times.size <= STATS_SIZE) reportTimes else reportTimes.shuffled().take(STATS_SIZE)
        t.appendLine("count\t${reportTimes.count()}")
        if (reportTimes.isEmpty()) {
            t.appendLine("\tEMPTY")
        } else {
            if (recursionType != ALL) {
                val mn = reportTimesForStats.min()/*  reportTimes.withIndex().minBy { it.value }*/
                /*(idx=${mn.index})*/
                t.appendLine("min\t${mn/*.value*/}")
                val sum = reportTimesForStats.reduce { a, b -> a + b }
                t.appendLine("mean\t${sum / reportTimesForStats.size}")
                /*t.appendLine("median\t${reportTimesForStats.map { it*//*.toMDuration()*//* }.median()}")*/
                val mx = reportTimes/*.withIndex()*//*.maxBy { it.value }*/.max()
                /*(idx=${mx.index})*/
                t.appendLine("max\t${mx/*.value*/}")
                t.appendLine("sum\t${reportTimes.reduce { a, b -> a + b }}")
            }
        }

        if (subBlocks.isNotEmpty()) {
            appendLine("Sub Blocks:")
            subBlocks.values.forEach {
                appendLine(it.reportString())
            }
        }
    }


    private val subBlocks = mutableMapOf<String, ProfiledBlock>()

    override fun <R> subBlock(
        key: String,
        op: ProfileDSL.() -> R
    ): R {
        val localRealKey = "$realKey - $key"
        val sub = subBlocks[localRealKey] ?: ProfiledBlock(localRealKey).also { subBlocks[localRealKey] = it }
        return sub.with(op = op)
    }

    fun subBlock(key: String): ProfiledBlock {
        val localRealKey = "$realKey - $key"
        val sub = subBlocks[localRealKey] ?: ProfiledBlock(localRealKey).also { subBlocks[localRealKey] = it }
        return sub
    }

}


private var recursionChecker: Any? = null