package net.tetrakoopa.gradle.plugin.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import net.tetrakoopa.mdu4j.util.IOUtil

import net.tetrakoopa.mdu4j.util.SystemUtil

abstract class AbstractProjectPlugin implements Plugin<Project> {

	protected void addProjectExtensions(Project project) {
		project.ext.getTopProject = { return getTopProject(project) }
	}

	protected getTopProject(Project project) {
		while (project.getParent() != null) project = project.getParent()
		return project
	}

	protected File prepareResources(Project project, String pluginId, String name) {

		Project topProject = getTopProject(project)
		File resourcesDir = topProject.file("${topProject.buildDir}/${pluginId}/${name}")
		File resourcesZip = topProject.file("${topProject.buildDir}/${pluginId}/${name}.zip")

		File okFile = new File(resourcesDir, ".unpack-ok");
		if (okFile.exists()) {
			return resourcesDir
		}

		resourcesDir.mkdirs()
		String pluginBundledResource = "${pluginId}.${name}.zip"
		InputStream toolInput = getClass().getClassLoader().getResourceAsStream(pluginBundledResource)
		if (toolInput == null) throw new NullPointerException("No such resource '${pluginBundledResource}'")
		IOUtil.copy((InputStream)toolInput, new FileOutputStream(resourcesZip))

		topProject.copy {
			from topProject.zipTree(resourcesZip)
			into "${resourcesDir}"
		}
		resourcesZip.delete()

		okFile.append(new Date().toString().bytes)

		return resourcesDir
	}

	protected boolean existsInPath(String executable) {
		return SystemUtil.findExecutableInPath(executable) != null
	}

}