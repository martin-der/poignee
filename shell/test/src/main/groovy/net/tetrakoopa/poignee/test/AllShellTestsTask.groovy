package net.tetrakoopa.poignee.test

import net.tetrakoopa.poignee.test.ShellTestException
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class AllShellTestsTask extends DefaultTask {

	@TaskAction
	def showResults() {
		Project project = getProject()
		def logger = project.logger
		ShellTestPluginExtension shell_test = project.shell_test
		def testsCount = shell_test.result.executedTestsCount
		def failedTestsCount = shell_test.result.failedTests.size()
		if (failedTestsCount>0) {
			logger.error("Some test(s) failed :")
			shell_test.each { logger.error("  - $name") }
			throw new ShellTestException("$failedTestsCount / $testsCount failed")
		} else
			logger.info("All tests succeeded")
	}
}
