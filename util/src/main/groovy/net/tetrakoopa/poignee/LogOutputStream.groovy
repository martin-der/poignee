package net.tetrakoopa.poignee

import org.gradle.api.logging.LogLevel
import org.slf4j.Logger

class LogOutputStream extends ByteArrayOutputStream {

	private final Logger logger;
	private final LogLevel level;

	LogOutputStream(Logger logger, LogLevel level) {
		this.logger = logger
		this.level = level
	}

	Logger getLogger() {
		return logger
	}

	LogLevel getLevel() {
		return level
	}

	@Override
	void flush() {
		logger.log(level, toString());
		reset()
	}
}
