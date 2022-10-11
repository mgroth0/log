package matt.log

import matt.lang.unixTime
import matt.log.textart.TEXT_BAR
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract


@Suppress("unused")
fun log(s: String?) = println(s)



interface Prints {
  fun println(a: Any)
  fun print(a: Any)
}


fun <T> tab(a: Any?) {
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


fun printlnWithTime(s: String) {
  println(unixTime().toString() + ":" + s)
}


