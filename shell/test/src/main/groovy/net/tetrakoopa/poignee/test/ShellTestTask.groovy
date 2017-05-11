package net.tetrakoopa.poignee.test

import net.tetrakoopa.poignee.ShellTestPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger


class ShellTestTask extends DefaultTask {

	//@Input
	File script

	String workingDir = "."

	final OutputRedirect outputRedirect = new OutputRedirect()

	@TaskAction
	def testScript() {

		Project project = getProject()
		def logger = project.logger
		ShellTestPluginExtension shellTest = project.shellTest
		logger.info("Testing '${script.name}'")
		Project topProject = project.getTopProject()
		File toolsResourcesDir = topProject.file("${topProject.buildDir}/net.tetrakoopa.poignee.shell-test/tool")

		shellTest.result.executedTestsCount++

		if (workingDir==null) workingDir = project.file(".").absolutePath

		def execResult = project.exec() {
			it.workingDir = workingDir
			it.environment project.shellTest.environmentVariables
			commandLine 'bash', "${script.path}"
			ignoreExitValue true
			standardOutput new LogOutputStream(logger, outputRedirect.standard)
			errorOutput    new LogOutputStream(logger, outputRedirect.error)
		}
		logger.info("  Tested '${script.path}' : $execResult")
		if(execResult.exitValue != 0) {
			shellTest.result.failedTests << "${script.path}"
		}
	}

	class OutputRedirect {
		LogLevel standard = LogLevel.INFO
		LogLevel error = LogLevel.ERROR
	}
}


// -----------------------------------------------

/*
def homeutilProjectDir = "${project.projectDir}/.."

def trimStart = {
	def start = "${project.projectDir}"
	it.startsWith(start) ? it.substring(start.size()) : it
}

def sourceDir = 'script'

def testsCount = 0
def failedTests = []


def sourceDirectory = file("$sourceDir")
sourceDirectory.eachFileRecurse(groovy.io.FileType.FILES) {
	script ->
		def isTest = script.name.endsWith('.sh') && !script.name.equals("common.sh") && !script.name.equals("runner.sh")
		if(isTest) {

			testsCount++

			def trimmedStart = "${project.projectDir}/script/"
			def filename = "$script".startsWith(trimmedStart) ? "$script".substring(trimmedStart.size()) : "$file"

			task "performTest_$filename" (type: Exec) {
				workingDir homeutilProjectDir
				commandLine 'bash', "$script"
				ignoreExitValue true
				standardOutput new LogOutputStream(logger, LogLevel.INFO)
				errorOutput    new LogOutputStream(logger, LogLevel.ERROR)
				doLast {
					logger.info("[Tested '$filename' :  $execResult]")
					if(execResult.exitValue != 0) {
						failedTests << "$filename"
					}
				}
			}

			allTests.dependsOn("performTest_$filename")
		}
}*/

class LogOutputStream extends ByteArrayOutputStream {

	private final Logger logger;
	private final LogLevel level;

	LogOutputStream(Logger logger, LogLevel level) {
		this.logger = logger
		this.level = level
	}

	Logger getLogger() {
		return logger
	}

	LogLevel getLevel() {
		return level
	}

	@Override
	void flush() {
		logger.log(level, toString());
		reset()
	}
}
