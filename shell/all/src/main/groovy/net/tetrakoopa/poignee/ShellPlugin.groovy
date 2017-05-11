package net.tetrakoopa.poignee

import org.gradle.api.Plugin
import org.gradle.api.Project

class ShellPlugin implements Plugin<Project> {

	void apply(Project project) {
		project.plugins.with {
			apply 'net.tetrakoopa.poignee.shell-package'
			apply 'net.tetrakoopa.poignee.shell-test'
		}
	}
}
