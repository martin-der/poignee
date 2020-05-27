package net.tetrakoopa.gradle.plugin.common.file

interface PathOrContentLocation {

	void setPath(File path)
	File getPath()
	void path(File path)

	void setLocation(String location)
	/** A relative location */
	String getLocation()
	void location(String location)

}