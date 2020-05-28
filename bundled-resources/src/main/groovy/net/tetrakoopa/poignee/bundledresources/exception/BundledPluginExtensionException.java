package net.tetrakoopa.poignee.bundledresources.exception;

import net.tetrakoopa.gradle.plugin.common.exception.PluginExtensionException;
import net.tetrakoopa.poignee.bundledresources.BundledResourcesPlugin;

public class BundledPluginExtensionException extends PluginExtensionException {
	BundledPluginExtensionException(String extensionName, String message) { super(BundledResourcesPlugin.ID, extensionName, message); }
	BundledPluginExtensionException(String extensionName, String message, Throwable cause) { super(BundledResourcesPlugin.ID, extensionName, message, cause); }
}
