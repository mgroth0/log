package matt.log.fourj

import matt.lang.sysprop.MY_CUSTOM_LOG4J_CONFIGURATION_FACT_NAME
import matt.test.Tests
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals


class FourJTests : Tests() {
    @Test
    fun logStuff() {
        val slf4jLogger: Logger = LoggerFactory.getLogger("test-logger")
        slf4jLogger.info("This is an INFO level message.")
        slf4jLogger.debug("This is a DEBUG level message (won't be printed).")
        slf4jLogger.info("This is a test: $EXCLUDE")
        slf4jLogger.debug("This is a test: $EXCLUDE")
    }


    @Test
    fun namesAreInSync() {
        val cls = MyCustomConfigurationFactory::class
        assertEquals(
            cls.java.annotations.filterIsInstance<Plugin>().first().name,
            cls.simpleName,
            "in the tutorial these are the same, though I am not sure they have to be. Better to be safe though at least for now."
        )
        assertEquals(
            MY_CUSTOM_LOG4J_CONFIGURATION_FACT_NAME,
            cls.qualifiedName,
            "these definitely have to be the same, since the class is loaded directly from this string"
        )
    }

    @Test
    fun initVals() {
        EXCLUDE
    }

    @Test
    fun instantiateClasses() {
        MyCustomConfigurationFactory()
    }
}
