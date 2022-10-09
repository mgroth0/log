package matt.log

import matt.log.textart.TEXT_BAR
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

var DEBUG = false
var PROFILE = false

@Suppress("unused")
fun log(s: String?) = println(s)

interface Prints {
  fun println(a: Any)
  fun print(a: Any)
}

interface Logger {
  fun printNoNewline(a: Any)
  fun log(a: Any) = printLog(a)
  fun printLog(s: String)
  fun printLog(a: Any) = printLog(a.toString())
  fun tab(s: Any) = printLog("\t$s")
  operator fun plusAssign(s: Any) = printLog(s.toString())
  var startTime: Long?
}


fun debug(s: Any) {
  if (DEBUG) {
	println(s.toString())
  }
}

//fun profile(s: Any) {
//  if (PROFILE) {
//	println(s.toString())
//  }
//}

val warned = mutableSetOf<Any>()
fun warnIf(b: Boolean, w: ()->String) {
  if (b) warn(w())
}

fun warnIfNot(b: Boolean, w: ()->String) = warnIf(!b, w)

fun warn(vararg s: Any) {
  s.forEach {
	warned += it
	println("WARNING:${it.toString().uppercase()}")
  }
}

fun warnOnce(s: Any) {
  if (s in warned) return
  else {
	warn(s)
	warned += s
	if (warned.size > 100) {
	  throw RuntimeException("too many warnings")
	}
  }
}


fun tab(a: Any?) {
  println("\t${a}")
}


fun printImportant(a: Any?) {
  println("\n\n")
  println(TEXT_BAR)
  println(a)
  println(TEXT_BAR)
  println("\n\n")
}

fun report(name: String, report: String) {
  printImportant(name)
  print(report)
  println("\n\n")
  println(TEXT_BAR)
  println("\n\n")
}


//fun taball(itr: DoubleArray) {
//  itr.forEach {
//	println("\t${it}")
//  }
//}

//fun taball(itr: Array<*>) {
//  itr.forEach {
//	println("\t${it}")
//  }
//}
//
//fun taball(itr: Iterable<*>) {
//  itr.forEach {
//	println("\t${it}")
//  }
//}


fun taball(s: String, itr: Collection<*>?) {
  println("$s(len=${itr?.size}):")
  itr?.forEach {
	println("\t${it}")
  }
}

fun taball(s: String, itr: DoubleArray?) {
  println("$s(len=${itr?.size}):")
  itr?.forEach {
	println("\t${it}")
  }
}

fun taball(s: String, itr: Array<*>?) {
  println("$s(len=${itr?.size}):")
  itr?.forEach {
	println("\t${it}")
  }
}

fun taball(s: String, itr: Iterable<*>?) {
  println("$s:")
  itr?.forEach {
	println("\t${it}")
  }
}

fun taball(s: String, itr: Map<*, *>?) {
  taball(s, itr?.entries)
}


val todos = mutableSetOf<String>()
fun todo(vararg s: String) {
  s.forEach {
	todos += it
	println("todo: $it")
  }
}

fun todoOnce(s: String) {
  if (s in todos) return
  else todo(s)
}


inline fun <T> T.takeUnlessPrintln(msg: String, predicate: (T)->Boolean): T? {
  contract {
	callsInPlace(predicate, EXACTLY_ONCE)
  }
  return if (!predicate(this)) this else run {
	println(msg)
	null
  }
}

fun <T> analyzeExceptions(op: ()->T): T {
  try {
	return op()
  } catch (e: Throwable) {
	println("${e::class.simpleName} message: ${e.message}")
	e.printStackTrace()
	throw e
  }
}