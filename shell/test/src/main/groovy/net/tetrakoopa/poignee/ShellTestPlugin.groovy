package net.tetrakoopa.poignee

import net.tetrakoopa.poignee.AbstractShellProjectPlugin
import net.tetrakoopa.poignee.test.AllShellTestsTask
import net.tetrakoopa.poignee.test.ShellTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

class ShellTestPlugin extends AbstractShellProjectPlugin implements Plugin<Project> {

	public static final String ALL_TESTS_RESULT_TASK_NAME = "shell-test"

	private File toolResourcesDir

	private void preprareEnvironment(Project project) {
		project.shellTest.environmentVariables['POIGNEE_TESTUNIT_SHUNIT2_EXEC'] = project.shellTest.testSuite.executable
		project.shellTest.environmentVariables['POIGNEE_TESTUNIT_PROJECT_DIRECTORY'] = project.projectDir
		//project.shellTest.environmentVariables['POIGNEE_TESTUNIT_SOURCE_DIRECTORY'] = project.shellTest.testSuite.executable
		//project.shellTest.environmentVariables['POIGNEE_TESTUNIT_TEST_SOURCE_DIRECTORY'] = project.shellTest.testSuite.executable
		//project.shellTest.environmentVariables['POIGNEE_TESTUNIT_TEST_RESOURCE_DIRECTORY'] = project.shellTest.testSuite.executable
	}

	void apply(Project project) {
		File toolResourcesDir = prepareResources(project, "net.tetrakoopa.poignee.shell-test", "tool")

		project.extensions.create("shellTest", ShellTestPluginExtension)

		project.ext.ShellTestTask = ShellTestTask
		project.ext.AllShellTestsTask = AllShellTestsTask

		project.shellTest.testSuite.shunit2Home = new File(toolResourcesDir, "shunit2-2.0.3")
		project.shellTest.testSuite.executable = new File(project.shellTest.testSuite.shunit2Home, "shunit2")

		preprareEnvironment(project)

		project.task(ALL_TESTS_RESULT_TASK_NAME, type:AllShellTestsTask)
	}
}

class ShellTestPluginExtension {
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
	final Result result = new Result()
}

