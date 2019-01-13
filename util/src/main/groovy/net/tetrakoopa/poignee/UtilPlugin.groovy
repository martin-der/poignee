package net.tetrakoopa.poignee

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class UtilPlugin implements Plugin<Project> {

	public static String ENVIRONMENT_PLACEHOLDER = "{environment}"

	public static String DEFAULT_ENVIRONMENT_FILENAME_TEMPLATE = "gradle/"+ENVIRONMENT_PLACEHOLDER+"-env.gradle"

	private static String getGitOutput(Project project, String... parameters) {
		def stdout = new ByteArrayOutputStream()
		def allParameters = ['git'] + parameters
		project.exec {
			workingDir = project.projectDir
			commandLine = allParameters as List
			standardOutput = stdout
		}
		return stdout.toString()
	}

	private static String getGitVersionName(Project project) {
		return getGitOutput(project, 'git', 'describe', '--tags', '--always').trim()
	}
	private static String getGitUserName(Project project) {
		return getGitOutput(project, 'config', 'user.name').trim()
	}
	private static String getGitUserEmail(Project project) {
		return getGitOutput(project, 'config', 'user.email').trim()
	}

	void apply(Project project) {
		project.ext.gitVersionName = { ->
			return UtilPlugin.getGitVersionName(project)
		}
		project.ext.getGitUserName = { ->
			return UtilPlugin.getGitUserName(project)
		}
		project.ext.getGitUserEmail = { ->
			return UtilPlugin.getGitUserEmail(project)
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
				envScriptTemplate = DEFAULT_ENVIRONMENT_FILENAME_TEMPLATE
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

