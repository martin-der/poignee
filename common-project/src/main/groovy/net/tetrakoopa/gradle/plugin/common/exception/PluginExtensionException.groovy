package net.tetrakoopa.gradle.plugin.common.exception

import org.gradle.api.GradleException

class PluginExtensionException extends PluginException {
	PluginExtensionException(String pluginId, String extensionName, String message) { super(pluginId, "Configuring '${extensionName}' : "+message) }
}
