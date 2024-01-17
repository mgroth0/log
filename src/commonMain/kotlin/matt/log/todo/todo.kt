package matt.log.todo

import matt.log.mem.LogMemory


@Deprecated("this is no longer acceptable. Put it in Brainstorm")
fun LogMemory.todo(vararg s: String) {
    s.forEach {
        todos += it
        @Suppress("NoTodoInStringLiteral")
        println("todo: $it")
    }
}

@Deprecated("this is no longer acceptable. Put it in Brainstorm")
fun LogMemory.todoOnce(s: String) {
    @Suppress("DEPRECATION")
    if (s in todos) return
    else todo(s)
}

