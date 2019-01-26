package net.tetrakoopa.gradle.plugin.common.file.exception;

import java.io.File;

public class NotARelativeFileException extends RuntimeException {
	public NotARelativeFileException(File root, File file) {
		this(root.getAbsolutePath(), file.getAbsolutePath());
	}
	public NotARelativeFileException(String rootAbsolutePath, String fileAbsolutePath) {
		super("File '${fileAbsolutePath}' is not it root directory '${rootAbsolutePath}'");
	}
}
