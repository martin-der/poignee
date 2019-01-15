package net.tetrakoopa.poignee

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class UtilPlugin implements Plugin<Project> {

	public static String ENVIRONMENT_PLACEHOLDER = "{environment}"

	public static String DEFAULT_ENVIRONMENT_FILENAME_TEMPLATE = "gradle/"+ENVIRONMENT_PLACEHOLDER+"-env.gradle"

	private static String getGitOutput(Project project, String... parameters) {
		def stdout = new ByteArrayOutputStream()
		def stderr = new ByteArrayOutputStream()
		def allParameters = ['git']
		allParameters.addAll(parameters)
		try {
			project.exec {
				workingDir = project.projectDir
				commandLine = allParameters as List
				standardOutput = stdout
				errorOutput = stderr
			}
		} catch (org.gradle.process.internal.ExecException ee) {
			println(stderr.toString())
			throw ee
		}
		return stdout.toString()
	}

	private static String getGitVersionName(Project project) {
		return getGitOutput(project, 'describe', '--tags', '--always').trim()
	}
	private static String getGitUserName(Project project) {
		return getGitOutput(project, 'config', 'user.name').trim()
	}
	private static String getGitUserEmail(Project project) {
		return getGitOutput(project, 'config', 'user.email').trim()
	}

	private static String getGitLastTag(Project project) {
		return getGitOutput(project, 'rev-list', '--tags', '--skip=0', '--max-count=1').trim()
	}

	/**
	 * @param format Format as in git <code>--pretty=format:</code>
	 * @param newerRevision This revision is excluded from the list
	 */
	private static String[] getGitCommitMessages(Project project, String format, String olderRevision, String newerRevision) {
		final String[] commits = getGitOutput(project, 'log', "--pretty=format:${format}", '--ancestry-path', "${olderRevision}..${newerRevision}").split('\n')
		return commits
	}

	void apply(Project project) {
		project.ext.gitVersionName = { ->
			return UtilPlugin.getGitVersionName(project)
		}
		project.ext.gitUserName = { ->
			return UtilPlugin.getGitUserName(project)
		}
		project.ext.gitUserEmail = { ->
			return UtilPlugin.getGitUserEmail(project)
		}

		project.ext.gitLastTag = { ->
			return UtilPlugin.getGitLastTag(project)
		}
		project.ext.gitCommitMessages = { format, olderRevision, newerRevision ->
			return UtilPlugin.getGitCommitMessages(project, format, olderRevision, newerRevision)
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

