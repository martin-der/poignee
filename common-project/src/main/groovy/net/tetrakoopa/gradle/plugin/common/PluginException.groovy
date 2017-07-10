package net.tetrakoopa.gradle.plugin.common

import org.gradle.api.GradleException

class PluginException extends GradleException {
	PluginException(String message) { super(message) }
}
