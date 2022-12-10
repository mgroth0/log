package matt.log.print

import matt.lang.function.DSL
import matt.log.textart.TEXT_BAR

/*cant use the name "print" since thats a built in kotlin function which also takes a lambda*/
fun echo(op: DSL<PrintDSL>) = PrintDSL.apply(op)

object PrintDSL {
  val bar get() = +TEXT_BAR
  val emptyLine get() = println()
  val empty get() = emptyLine
  operator fun Any?.unaryPlus() = println(this)
}