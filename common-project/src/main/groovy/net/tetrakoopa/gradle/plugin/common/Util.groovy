package net.tetrakoopa.gradle.plugin.common

import org.gradle.api.Project

class Util {

	static Project getTopProject(Project project) {
		while (project.getParent() != null) project = project.getParent()
		return project
	}
}
