package net.tetrakoopa.gradle.plugin.common.file.exception;

public class AbstractFileException extends RuntimeException {

	public AbstractFileException(String message, Throwable cause) {
		super(message, cause);
	}
	public AbstractFileException(String message) {
		super(message);
	}
	public AbstractFileException(Throwable cause) {
		super(cause);
	}

}
