package matt.log.remind

val reminded = mutableSetOf<Any>()
fun remindIf(b: Boolean, w: ()->String) {
  if (b) remind(w())
}

fun remindIfNot(b: Boolean, w: ()->String) = remindIf(!b, w)

fun remind(vararg s: Any) {
  s.forEach {
	reminded += it
	println("Reminder: ${it.toString()}")
  }
}

fun remindOnce(s: Any) {
  if (s in reminded) return
  else {
	remind(s)
	reminded += s
	if (reminded.size > 100) {
	  throw RuntimeException("too many reminders")
	}
  }
}