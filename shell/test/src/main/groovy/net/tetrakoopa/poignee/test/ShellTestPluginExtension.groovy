package net.tetrakoopa.poignee.test

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.util.ConfigureUtil


class ShellTestPluginExtension {

	public final static String SHELL_TEST_EXTENSION_NAME = "shell_test"

	private Project project

	ShellTestPluginExtension(Project project) {
		this.project = project
	}

	class Result {
		int executedTestsCount
		def failedTests = []
	}

	class TestSuite {
		String shunit2Home
		String executable
	}

	def environmentVariables = [:]

	final TestSuite testSuite = new TestSuite()
	ConfigurableFileCollection testScripts
	File workingDir
	final Result result = new Result()

	ConfigurableFileCollection from(Object... paths) {
		if (testScripts == null)
			testScripts = project.files(paths)
		else
			testScripts.from(paths)
		return testScripts
	}

	ShellTestPluginExtension workingDir(Object dir) {
		workingDir = project.file(dir)
		return this
	}

	def testSuite(Closure closure) { ConfigureUtil.configure(closure, testSuite) }
	def result(Closure closure) { ConfigureUtil.configure(closure, result) }
}

