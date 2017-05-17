package net.tetrakoopa.poignee

import net.tetrakoopa.poignee.AbstractShellProjectPlugin
import net.tetrakoopa.poignee.test.ShellTestPluginExtension
import net.tetrakoopa.poignee.test.AllShellTestsTask
import net.tetrakoopa.poignee.test.ShellTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

import static net.tetrakoopa.poignee.test.ShellTestPluginExtension.SHELL_TEST_EXTENSION_NAME

class ShellTestPlugin extends AbstractShellProjectPlugin implements Plugin<Project> {

	public static final String ALL_TESTS_RESULT_TASK_NAME = "shell-test"

	private void preprareEnvironment(Project project) {
		project.shell_test.environmentVariables['POIGNEE_TESTUNIT_SHUNIT2_EXEC'] = project.shell_test.testSuite.executable
		project.shell_test.environmentVariables['POIGNEE_TESTUNIT_PROJECT_DIRECTORY'] = project.projectDir
		//project.shell_test.environmentVariables['POIGNEE_TESTUNIT_SOURCE_DIRECTORY'] = project.shell_test.testSuite.executable
		//project.shell_test.environmentVariables['POIGNEE_TESTUNIT_TEST_SOURCE_DIRECTORY'] = project.shell_test.testSuite.executable
		//project.shell_test.environmentVariables['POIGNEE_TESTUNIT_TEST_RESOURCE_DIRECTORY'] = project.shell_test.testSuite.executable
	}

	void apply(Project project) {
		File toolResourcesDir = prepareResources(project, "net.tetrakoopa.poignee.shell-test", "tool")

		project.extensions.create(SHELL_TEST_EXTENSION_NAME, ShellTestPluginExtension, project)

		project.ext.ShellTestTask = ShellTestTask
		project.ext.AllShellTestsTask = AllShellTestsTask

		project.shell_test.testSuite.shunit2Home = new File(toolResourcesDir, "shunit2-2.0.3")
		project.shell_test.testSuite.executable = new File(project.shell_test.testSuite.shunit2Home, "shunit2")

		preprareEnvironment(project)

		project.afterEvaluate {
			project.shell_test.testScripts.each() { file ->
				println "Create test for ${file.name}"
				project.task("__shell_test_${file.name}", type:ShellTestTask) {
					script = file
				}
			}
		}

		project.task(ALL_TESTS_RESULT_TASK_NAME, type:AllShellTestsTask)
	}
}

