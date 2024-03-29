package matt.log

import matt.lang.anno.SeeURL
import matt.lang.unixTime
import matt.log.textart.TEXT_BAR
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.time.TimeMark


@Suppress("unused")
fun log(s: String?) = println(s)


fun tab(a: Any?) {
    println("\t$a")
}


fun printImportant(a: Any?) {
    println("\n\n")
    println(TEXT_BAR)
    println(a)
    println(TEXT_BAR)
    println("\n\n")
}

fun report(
    name: String,
    report: String
) {
    printImportant(name)
    print(report)
    println("\n\n")
    println(TEXT_BAR)
    println("\n\n")
}


fun taball(
    s: String,
    itr: Collection<*>?
) {
    println("$s(len=${itr?.size}):")
    itr?.forEach {
        println("\t$it")
    }
}

fun printObjectInfo(
    name: String,
    vararg props: Pair<String, Any>
) {
    println(TEXT_BAR)
    println("$name:")
    props.forEach {
        println("\t${it.first}\t${it.second}")
    }
    println(TEXT_BAR)
}

fun taball(
    s: String,
    itr: DoubleArray?
) {
    println("$s(len=${itr?.size}):")
    itr?.forEach {
        println("\t$it")
    }
}

fun taball(
    s: String,
    itr: Array<*>?
) {
    println("$s(len=${itr?.size}):")
    itr?.forEach {
        println("\t$it")
    }
}

fun taball(
    s: String,
    itr: Iterable<*>?
) {
    println("$s:")
    itr?.forEach {
        println("\t$it")
    }
}

fun taball(
    s: String,
    itr: Map<*, *>?
) {
    taball(s, itr?.entries)
}


@OptIn(ExperimentalContracts::class)
@SeeURL("https://youtrack.jetbrains.com/issue/KT-65158/K2-Contracts-False-positive-WRONGINVOCATIONKIND-with-unrelated-higher-order-function-call")
@Suppress("WRONG_INVOCATION_KIND")
inline fun <T> T.takeUnlessPrintln(
    msg: String,
    predicate: (T) -> Boolean
): T? {
    contract {
        callsInPlace(predicate, EXACTLY_ONCE)
    }
    return if (!predicate(this)) this else run {
        println(msg)
        null
    }
}

fun <T> analyzeExceptions(op: () -> T): T {
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


infix fun TimeMark.printElapsedNow(label: String) = println("$label\t${elapsedNow()}")


