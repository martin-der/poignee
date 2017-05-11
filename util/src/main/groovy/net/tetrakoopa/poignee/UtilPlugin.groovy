package net.tetrakoopa.poignee

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

class UtilPlugin implements Plugin<Project> {

	void apply(Project project) {
		project.ext.getGitVersionName = { ->
			def stdout = new ByteArrayOutputStream()
			project.exec {
				commandLine 'git', 'describe', '--tags', '--always'
				standardOutput = stdout
			}
			return stdout.toString().trim()
		}

	}
}

