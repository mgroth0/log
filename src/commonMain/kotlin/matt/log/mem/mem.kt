package matt.log.mem


class LogMemory {
    val warned = mutableSetOf<Any>()
    val printlnOnceMemory = mutableSetOf<String>()
    val todos = mutableSetOf<String>()
    val opOnceMemory = mutableSetOf<String>()
}
