@file:JvmName("WarnJvmAndroidKt")

package matt.log.warn

actual fun dumpStack() = Thread.dumpStack()