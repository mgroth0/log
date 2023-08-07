package matt.log.test


import matt.log.taball
import matt.test.JupiterTestAssertions.assertRunsInOneMinute
import kotlin.test.Test

class LogTests {
    @Test
    fun testTabAll() = assertRunsInOneMinute {
        taball("numbers", listOf(1, 2, 3))
    }
}