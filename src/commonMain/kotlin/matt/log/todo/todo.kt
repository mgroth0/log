package matt.log.todo

val todos = mutableSetOf<String>()
fun todo(vararg s: String) {
  s.forEach {
	todos += it
	println("matt.log.todo.todo: $it")
  }
}

fun todoOnce(s: String) {
  if (s in todos) return
  else todo(s)
}

