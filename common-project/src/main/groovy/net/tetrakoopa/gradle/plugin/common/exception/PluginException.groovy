package net.tetrakoopa.gradle.plugin.common.exception

import org.gradle.api.GradleException

class PluginException extends GradleException {
	PluginException(String pluginId, String message) { super("[${pluginId}] : "+message) }
}
