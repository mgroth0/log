package matt.log.textart

import matt.prim.str.mybuild.api.LineDelimitedStringDsl

const val TEXT_BAR = "|||||||||||||||||||||||||||||||||||||||||||||||"



fun LineDelimitedStringDsl<Any?>.bar() = append(TEXT_BAR)