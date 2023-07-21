package matt.log.context

import matt.log.logger.Logger

interface LogContext {
    val logger: Logger
}