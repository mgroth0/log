package matt.log.profile.profiler

import matt.log.profile.profiler.ProfileRecursionType.ALL
import matt.log.profile.profiler.ProfileRecursionType.DEEPEST_ONLY
import matt.log.profile.profiler.ProfileRecursionType.NOT_ALLOWED
import matt.log.profile.profiler.ProfileRecursionType.TOP_ONLY
import matt.log.profile.stopwatch.Stopwatch
import matt.log.profile.stopwatch.tic
import matt.log.report
import matt.math.reduce.median
import matt.prim.str.build.t
import matt.prim.str.joinWithNewLines
import kotlin.time.Duration


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

@Synchronized
fun profile(name: String = "insert profile name here", op: ()->Unit) {
  ProfiledBlock.clearInstanceMap()
  recursionChecker = object {}
  val myRecursionChecker = recursionChecker
  op()
  require(myRecursionChecker == recursionChecker)
  ProfiledBlock.reportAll(profileName = name)
}

private var recursionChecker: Any? = null