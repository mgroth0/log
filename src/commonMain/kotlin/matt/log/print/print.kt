package matt.log.print

import matt.lang.function.Dsl
import matt.log.textart.TEXT_BAR

/*cant use the name "print" since thats a built in kotlin function which also takes a lambda*/
fun echo(op: Dsl<PrintDSL>) = PrintDSL.apply(op)

object PrintDSL {
    val bar get() = println(TEXT_BAR)
    val emptyLine get() = println()
    val empty get() = emptyLine
}
