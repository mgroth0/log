package matt.log.todo

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

