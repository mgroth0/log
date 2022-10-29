@file:JvmName("WarnKvmKt")

package matt.log.warn

actual fun dumpStack() = Thread.dumpStack()