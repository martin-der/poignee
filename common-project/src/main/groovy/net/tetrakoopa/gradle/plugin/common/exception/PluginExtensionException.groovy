package net.tetrakoopa.gradle.plugin.common.exception

import org.gradle.api.GradleException

class PluginExtensionException extends PluginException {
	protected PluginExtensionException(String pluginId, String extensionName, String message) { super(pluginId, "Configuring '${extensionName}' : "+message) }
	protected PluginExtensionException(String pluginId, String extensionName, String message, Throwable cause) { super(pluginId, "Configuring '${extensionName}' : "+message, cause) }
}
