package net.tetrakoopa.poignee

import org.gradle.api.GradleException
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

		project.ext.applyEnvironmentScript = { ->
			def env = 'dev'
			if ( project.hasProperty('environment') ) {
				env = project.environment
				project.logger.info("No environment defined : assuming development environment")
			}
			def envScript = project.file("gradle/${env}-env.gradle")
			if (!envScript.exists()) {
				throw new GradleException("Unknown environment: '${env}'")
			}
			project.apply from: envScript
		}

	}
}

