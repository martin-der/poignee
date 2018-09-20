package net.tetrakoopa.gradle.plugin.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import net.tetrakoopa.mdu4j.util.IOUtil

import net.tetrakoopa.mdu4j.util.SystemUtil
import org.gradle.api.tasks.bundling.Zip

abstract class AbstractProjectPlugin implements Plugin<Project> {

	public final static String ID = "net.tetrakoopa.poignee.common-project"

	protected void addProjectExtensions(Project project) {
		project.ext.getTopProject = { return getTopProject(project) }
	}

	protected Project getTopProject(Project project) {
		return Util.getTopProject(project)
	}

	protected boolean existsInPath(String executable) {
		return SystemUtil.findExecutableInPath(executable) != null
	}

}