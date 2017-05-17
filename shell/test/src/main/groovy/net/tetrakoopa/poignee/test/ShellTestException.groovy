package net.tetrakoopa.poignee.test

import org.gradle.api.GradleException

class ShellTestException extends GradleException {
	ShellTestException(String message) { super(message) }
}
