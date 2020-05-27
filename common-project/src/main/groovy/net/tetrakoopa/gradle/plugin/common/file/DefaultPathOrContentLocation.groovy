package net.tetrakoopa.gradle.plugin.common.file

import net.tetrakoopa.gradle.plugin.common.file.exception.PathAndLocationDefined
import org.gradle.util.ConfigureUtil

class DefaultPathOrContentLocation implements PathOrContentLocation {

	def __configure(Closure closure, String forWhat) {
		ConfigureUtil.configure(closure, this)
		checkOnlyOneDefinition(forWhat)
	}

	File path

	String location

	boolean defined() { return location != null || path != null }

	private void checkOnlyOneDefinition(String forWhat) { if (location != null && path != null) throw new PathAndLocationDefined(forWhat) }

	@Override
	void path(File path) {
		setPath(path)
	}

	@Override
	void location(String location) {
		setLocation(location)
	}
}
