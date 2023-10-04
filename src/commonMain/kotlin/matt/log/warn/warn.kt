package matt.log.warn

import matt.log.mem.LogMemory


fun LogMemory.warnIf(
    b: Boolean,
    w: () -> String
) {
    if (b) warn(w())
}

fun warnIf(
    b: Boolean,
    w: () -> String
) {
    if (b) warn(w())
}

fun LogMemory.warnIfNot(
    b: Boolean,
    w: () -> String
) = warnIf(!b, w)

fun warnIfNot(
    b: Boolean,
    w: () -> String
) = warnIf(!b, w)

fun LogMemory.warn(vararg s: Any) {
    s.forEach {
        warned += it
        println("Warning: $it")
    }
}

fun warn(vararg s: Any) {
    s.forEach {
        println("Warning: $it")
    }
}

expect fun dumpStack()

fun LogMemory.warnAndDumpStack(vararg s: Any) {
    warn(*s)
    dumpStack()
}

fun warnAndDumpStack(vararg s: Any) {
    warn(*s)
    dumpStack()
}

fun LogMemory.warnOnce(s: Any) {
    if (s in warned) return
    else {
        warn(s)
        warned += s
        if (warned.size > 100) {
            throw RuntimeException("too many warnings")
        }
    }
}


fun LogMemory.printlnOnce(s: String) {
    if (s in printlnOnceMemory) return
    else {
        println(s)
        printlnOnceMemory += s
        if (printlnOnceMemory.size > 100) {
            throw RuntimeException("too many printlnOnces")
        }
    }
}