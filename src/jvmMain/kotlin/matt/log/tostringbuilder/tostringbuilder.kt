package matt.log.tostringbuilder

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.jvm.isAccessible

fun Any.toStringBuilder(
  vararg props: KProperty<*>
): String {
  return toStringBuilder(props.associate {
	it.name to it.apply { isAccessible = true }.getter.call()
  })
}

fun Any.toStringBuilder(
  vararg kvPairs: Pair<String, Any?>
) = toStringBuilder(mapOf(*kvPairs))


fun Any.toStringBuilder(
  map: Map<String, Any?>
): String {
  val realMap = map.toMutableMap()
  if (realMap.isEmpty()) {
	realMap["@"] = hashCode()
  }
  return this::class.firstSimpleName() + map.entries.joinToString(prefix = "[", postfix = "]") {
	"${it.key}=${it.value}"
  }
}


//fun Any.toStringBuilder(vararg values: Pair<String, Any?>): String {
//  val suffix = if (values.isEmpty()) "@" + this.hashCode() else values.joinToString(" ") {
//	it.first + "=" + it.second
//  }
//  return "${this::class.simpleName} [$suffix]"
//}


fun KClass<*>.firstSimpleName() = this.simpleName ?: this.allSuperclasses.first().simpleName