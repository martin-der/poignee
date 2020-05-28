package net.tetrakoopa.gradle.plugin.common.exception

import org.gradle.api.GradleException

class PluginException extends GradleException {
	protected PluginException(String pluginId, String message) { super("[${pluginId}] : "+message) }
	protected PluginException(String pluginId, String message, Throwable cause) { super("[${pluginId}] : "+message, cause) }
}
