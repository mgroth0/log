package matt.log.test


import matt.log.taball
import kotlin.test.Test

class LogTests {
    @Test
    fun testTabAll() {
        taball("numbers", listOf(1, 2, 3))
    }
}
