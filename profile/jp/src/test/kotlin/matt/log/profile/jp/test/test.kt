package matt.log.profile.jp.test


import matt.log.profile.jp.JProfiler
import matt.test.Tests
import kotlin.test.Test

class JpTests() : Tests() {
    @Test
    fun instantiateClasses() {
        JProfiler.DEFAULT
    }
}
