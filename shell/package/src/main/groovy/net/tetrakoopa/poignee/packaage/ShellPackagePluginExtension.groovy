package net.tetrakoopa.poignee.packaage

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.util.ConfigureUtil

class ShellPackagePluginExtension {

	public final static String SHELL_PACKAGE_EXTENSION_NAME = "shell_package"

	private final Project project
	boolean ready

	ShellPackagePluginExtension(Project project) {
		this.project = project
	}
	class Output {
		String distributionDir
		String documentationDir
	}
	class Documentation {
		boolean tableOfContent
	}
	class Installer {
		class UserScript {
			final PathOrContentLocation script = new PathOrContentLocation()
			String question
			def script(Closure closure) { script.__configure(closure, "installer userScript script")}
		}
		final PathOrContentLocation readme = new PathOrContentLocation()
		final PathOrContentLocation licence = new PathOrContentLocation()
		final UserScript userScript = new UserScript()

		def userScript(Closure closure) { ConfigureUtil.configure(closure, userScript) }
		def readme(Closure closure) { readme.__configure(closure, "installer readme")}
		def licence(Closure closure) { licence.__configure(closure, "installer licence")}
	}

	ConfigurableFileCollection source
	String distributionName
	final Output output = new Output()
	final Documentation documentation = new Documentation()
	final Installer installer = new Installer()

	ConfigurableFileCollection from(Object... paths) {
		if (source == null)
			source = project.files(paths)
		else
			source.from(paths)
		return source
	}

	def output(Closure closure) { ConfigureUtil.configure(closure, output) }
	def documentation(Closure closure) { ConfigureUtil.configure(closure, documentation) }
	def installer(Closure closure) { ConfigureUtil.configure(closure, installer) }
}

class PathOrContentLocation {

	def __configure(Closure closure, String forWhat) {
		ConfigureUtil.configure(closure, this)
		checkOnlyOneDefinition(forWhat)
	}

	File path
	/** Relative to install content './' */
	String location
	boolean defined() { return location != null || path != null }
	void checkOnlyOneDefinition(String forWhat) { if (location != null && path != null) throw new ShellPackageException("Cannot define both for 'path' and 'location' for '$forWhat'") }
}

