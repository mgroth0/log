package matt.log.level

import matt.log.level.MattLogLevel.DEBUG
import matt.log.level.MattLogLevel.ERROR
import matt.log.level.MattLogLevel.INFO
import matt.log.level.MattLogLevel.PROFILE
import matt.log.level.MattLogLevel.SILENT
import matt.log.level.MattLogLevel.TRACE
import matt.log.level.MattLogLevel.WARN
import java.util.logging.Level
import java.util.logging.Level.ALL
import java.util.logging.Level.CONFIG
import java.util.logging.Level.FINE
import java.util.logging.Level.OFF
import java.util.logging.Level.SEVERE
import java.util.logging.Level.WARNING


fun MattLogLevel.toJavaLogLevel(): Level = when (this) {
    SILENT  -> OFF
    ERROR   -> SEVERE
    WARN    -> WARNING
    INFO    -> CONFIG /*this could also be INFO, but CONFIG is more inclusive*/
    DEBUG   -> FINE
    TRACE   -> ALL /*skipping FINER and FINEST*/
    PROFILE -> ALL
}
