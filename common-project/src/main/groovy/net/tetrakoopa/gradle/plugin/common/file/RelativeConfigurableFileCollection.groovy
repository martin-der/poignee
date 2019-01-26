package net.tetrakoopa.gradle.plugin.common.file


import org.gradle.api.file.ConfigurableFileCollection

interface RelativeConfigurableFileCollection extends ConfigurableFileCollection {

	void setRootDir(File root);
	File getRootDir();

	void rootDir(File root);

	Set<String> getRelativePaths()

}