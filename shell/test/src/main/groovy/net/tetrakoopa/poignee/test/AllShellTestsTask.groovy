package net.tetrakoopa.poignee.test

import net.tetrakoopa.poignee.ShellTestPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class AllShellTestsTask extends DefaultTask {

	@TaskAction
	def showResults() {
		Project project = getProject()
		def logger = project.logger
		ShellTestPluginExtension shellTest = project.shellTest
		logger.info("Executed ${shellTest.result.executedTestsCount} test(s).")
//		def failedTestsCount = failedTests.size()
//		if (failedTestsCount>0) {
//			logger.error("Some test(s) failed :")
//			failedTests.each { logger.error("  - $name") }
//			//throw new ShellPluginException("$failedTestsCount / $testsCount failed")
//		} else
//			logger.info("All tests succeeded")
	}
}
