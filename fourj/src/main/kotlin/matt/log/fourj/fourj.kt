package matt.log.fourj


import matt.lang.anno.SeeURL
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target.SYSTEM_OUT
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.ConfigurationFactory
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Order
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
import org.apache.logging.log4j.core.config.plugins.Plugin
import java.net.URI


private val LEVEL: Level = Level.INFO

internal const val EXCLUDE = "Autoreload is disabled because the development mode is off"

/*
import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus.NEUTRAL
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.spi.FilterReply.ACCEPT
import ch.qos.logback.core.spi.FilterReply.DENY
import matt.lang.anno.SeeURL

private val NORMAL_LEVEL = Level.INFO
private val CURRENT_LEVEL: Level = System.getenv("LOGBACK_LEVEL")?.let {
    Level.toLevel(it)
} ?: NORMAL_LEVEL


@SeeURL("https://logback.qos.ch/manual/filters.html")
class SampleFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return if (EXCLUDE in event.message) {
            DENY
        } else {
            ACCEPT
        }
    }
}

@SeeURL("https://logback.qos.ch/manual/configuration.html")
class MyLogBackConfigurator : BasicConfigurator() {

    override fun configure(lc: LoggerContext): ExecutionStatus {
        lc.loggerList.forEach { aLogger ->
            aLogger.level = CURRENT_LEVEL
            aLogger.iteratorForAppenders().forEach {
                it.addFilter(SampleFilter())
            }
        }
        return NEUTRAL
    }
}*/


@Plugin(name = "MyCustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
@SeeURL("https://logging.apache.org/log4j/2.x/manual/customconfig.html")
class MyCustomConfigurationFactory : ConfigurationFactory() {

    companion object {


        private fun createConfiguration(
            name: String,
            builder: ConfigurationBuilder<BuiltConfiguration>
        ): Configuration {
            builder.setConfigurationName(name)
            builder.setStatusLevel(LEVEL)
            val stdoutAppenderRef = "Stdout"
            val appenderBuilder = builder.newAppender(stdoutAppenderRef, "CONSOLE").addAttribute("target", SYSTEM_OUT)
            appenderBuilder.add(
                builder.newLayout("PatternLayout")
                    .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable")
            )
            appenderBuilder.add(
                builder.newFilter(
                    "MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL
                ).addAttribute("marker", EXCLUDE)
            )
            builder.add(appenderBuilder)
            builder.add(builder.newRootLogger(LEVEL).add(builder.newAppenderRef(stdoutAppenderRef)))
            return builder.build()
        }
    }

    override fun getSupportedTypes(): Array<String> = arrayOf("*")

    override fun getConfiguration(
        loggerContext: LoggerContext?,
        name: String,
        configLocation: URI?
    ): Configuration {
        val builder: ConfigurationBuilder<BuiltConfiguration> = ConfigurationBuilderFactory.newConfigurationBuilder()
        return createConfiguration(name, builder)
    }

    override fun getConfiguration(
        loggerContext: LoggerContext?,
        source: ConfigurationSource?
    ): Configuration = getConfiguration(loggerContext, source.toString(), null)
}


