package net.tetrakoopa.poignee

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class UtilPlugin implements Plugin<Project> {

	public static String ENVIRONMENT_PLACEHOLDER = "{environment}"

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
			project.applyEnvironmentScript(null)
		}
		project.ext.applyEnvironmentScript = { template ->
			String envScriptTemplate
			if ( template != null ) {
				if (!template.contains(ENVIRONMENT_PLACEHOLDER)) throw new GradleException("Template filename must contains '${ENVIRONMENT_PLACEHOLDER}'.")
				envScriptTemplate = template
			} else {
				envScriptTemplate = "gradle/{environment}-env.gradle"
			}
			def env = 'dev'
			if ( project.hasProperty('environment') ) {
				env = project.environment
				project.logger.info("No environment defined : assuming development environment ('${env}')")
			}
			def envScriptName = envScriptTemplate.replace(ENVIRONMENT_PLACEHOLDER,"${env}")
			def envScript = project.file(envScriptName)
			if (!envScript.exists()) {
				throw new GradleException("Environment config for '${env}' not found. Searched in '${envScript}'")
			}
			project.apply from: envScript
		}

	}
}

