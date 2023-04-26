package matt.log.warn

val warned = mutableSetOf<Any>()
fun warnIf(b: Boolean, w: () -> String) {
    if (b) warn(w())
}

fun warnIfNot(b: Boolean, w: () -> String) = warnIf(!b, w)

fun warn(vararg s: Any, upper: Boolean = true) {
    s.forEach {
        warned += it
        println("WARNING:${it.toString().let { if (upper) it.uppercase() else it }}")
    }
}

expect fun dumpStack()

fun warnAndDumpStack(vararg s: Any) {
    warn(*s)
    dumpStack()
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


val printlnOnceMemory = mutableSetOf<String>()
fun printlnOnce(s: String) {
    if (s in printlnOnceMemory) return
    else {
        println(s)
        printlnOnceMemory += s
        if (printlnOnceMemory.size > 100) {
            throw RuntimeException("too many printlnOnces")
        }
    }
}