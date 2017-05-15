package net.tetrakoopa.poignee

import org.gradle.api.GradleException

class ShellPluginException extends GradleException {
	ShellPluginException(String message) { super(message) }
}
