package matt.log.profile.test


import matt.log.profile.stopwatch.tic
import matt.test.Tests
import kotlin.test.Test

class ProfileTests : Tests() {
    @Test
    fun ticToc() {
        val t = tic()
        t.toc(1)
    }
}
