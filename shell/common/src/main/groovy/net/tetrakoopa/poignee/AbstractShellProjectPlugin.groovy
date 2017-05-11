package net.tetrakoopa.poignee

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import net.tetrakoopa.mdu4j.util.IOUtil

abstract class AbstractShellProjectPlugin implements Plugin<Project> {

	protected getTopProject(Project project) {
		while (project.getParent() != null) project = project.getParent()
		return project
	}

	protected File prepareResources(Project project, String pluginId, String name) {

		project.ext.getTopProject = { return getTopProject(project) }

		Project topProject = getTopProject(project)
		File resourcesDir = topProject.file("${topProject.buildDir}/${pluginId}/${name}")
		File resourcesZip = topProject.file("${topProject.buildDir}/${pluginId}/${name}.zip")
		resourcesDir.mkdirs()
		InputStream toolInput = getClass().getClassLoader().getResourceAsStream("${pluginId}.${name}.zip")
		IOUtil.copy((InputStream)toolInput, new FileOutputStream(resourcesZip))

		topProject.copy {
			from topProject.zipTree(resourcesZip)
			into "${resourcesDir}"
		}

		return resourcesDir
	}

}