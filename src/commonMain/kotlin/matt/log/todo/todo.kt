package matt.log.todo

import matt.log.mem.LogMemory


fun LogMemory.todo(vararg s: String) {
  s.forEach {
	todos += it
	println("todo: $it")
  }
}

fun LogMemory.todoOnce(s: String) {
  if (s in todos) return
  else todo(s)
}

