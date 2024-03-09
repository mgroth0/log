package matt.log.warn.common

import matt.lang.function.Consume
import matt.log.mem.LogMemory
import matt.log.warn.common.AlertType.Error
import matt.log.warn.common.AlertType.Warning
import matt.log.warn.dumpStack

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

enum class AlertType {
    Warning, Error
}

fun alert(s: String, alertType: AlertType) {
    when (alertType) {
        Warning    -> warn(s)
        Error -> error(s)
    }
}

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

fun LogMemory.opOnce(s: String, op: Consume<String>) {
    if (s in opOnceMemory) return
    else {
        op(s)
        opOnceMemory += s
        if (opOnceMemory.size > 100) {
            throw RuntimeException("too many opOnces")
        }
    }
}


