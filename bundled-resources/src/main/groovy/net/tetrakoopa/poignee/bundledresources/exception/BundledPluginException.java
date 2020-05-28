package net.tetrakoopa.poignee.bundledresources.exception;

import net.tetrakoopa.gradle.plugin.common.exception.PluginException;
import net.tetrakoopa.poignee.bundledresources.BundledResourcesPlugin;

public class BundledPluginException extends PluginException {
	BundledPluginException(String message) { super(BundledResourcesPlugin.ID,message); }
	BundledPluginException(String message, Throwable cause) { super(BundledResourcesPlugin.ID,message,cause); }
}
