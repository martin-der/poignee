package net.tetrakoopa.poignee

import net.tetrakoopa.poignee.packaage.ShellPackageException
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.file.ConfigurableFileCollection

class ShellPackagePlugin extends AbstractShellProjectPlugin implements Plugin<Project> {

	public static final String ID = "net.tetrakoopa.poignee.shell-package"

	private ConfigurableFileCollection makeFileCollectionRelativeToProject(Project project, ConfigurableFileCollection collection) {
		def projectAbsolutePathWithSlash = project.projectDir.absolutePath+"/"
		ConfigurableFileCollection relativeCollection
		relativeCollection = project.files()
		collection.each { file ->
			def filenameTrimmed = "${file.path}".startsWith(projectAbsolutePathWithSlash) ? "${file.path}".substring(projectAbsolutePathWithSlash.size()) : file.path
			relativeCollection.from project.file(filenameTrimmed)
		}
		return collection
	}


	private void setShellPackageDefaultsConfiguration(Project project) {
		ShellPackagePluginExtension packaging = (ShellPackagePluginExtension) project.getExtensions().findByName("packaging")
		packaging.ready = false
		packaging.source.directory = "src"
		packaging.distributionName = null
		packaging.output.distributionDir = null
		packaging.output.documentationDir = null
	}
	private void makeShellPackageConfiguration(Project project) {
		ShellPackagePluginExtension packaging = (ShellPackagePluginExtension) project.getExtensions().findByName("packaging")
		if (packaging.ready) return
		packaging.ready = true
		if (packaging.source.fileCollection == null) throw new GradleException("No source file(s) defined")
		packaging.source.fileCollection = makeFileCollectionRelativeToProject(project, packaging.source.fileCollection)
		if (packaging.distributionName == null) packaging.distributionName = "${project.name}-${project.version}"
		if (packaging.output.distributionDir == null) packaging.output.distributionDir = "${project.buildDir}/distribution"
		if (packaging.output.documentationDir == null) packaging.output.documentationDir = "${project.buildDir}/documentation"

		packaging.source.fileCollection.each { file ->
			println "File in source : '${file.path}'"
		}
	}

	void apply(Project project) {

		File toolResourcesDir = prepareResources(project, "${ID}", "tool")

		project.extensions.create("packaging", ShellPackagePluginExtension)
		setShellPackageDefaultsConfiguration(project)
		((ShellPackagePluginExtension) project.getExtensions().findByName("packaging")).project = project

		project.task('documentation') {

			ext.inputFiles = project.fileTree(project.packaging.source.directory).include('*.sh')

			doLast {

				makeShellPackageConfiguration(project)

				def shellScripts = ext.inputFiles

				File docDir = project.file(project.packaging.output.documentationDir)

				if (!docDir.exists()) docDir.mkdirs()

				shellScripts.each { File file ->
					def document = new File(docDir, "${file.name}.md")
					project.exec {
						commandLine "${toolResourcesDir}/doc/shdoc/shdoc_io_w.sh", "${file.path}", "${document.path}"
					}
					// Remove the documentation if it's empty
					if(!(document.length()>0) || document.text.matches("[\n \t]*") ) document.delete()
				}
			}
		}
		project.task('packageZip', dependsOn: 'documentation') {

			doLast {

				makeShellPackageConfiguration(project)

				project.task('packageZip.doIt', type: Zip, dependsOn: 'documentation') {

					baseName = project.packaging.distributionName

					destinationDir = project.file("${project.packaging.output.distributionDir}/final")

					//from project.fileTree(project.packaging.source.directory).include('*.sh', '*.py', 'README.md')
					//from project.fileTree(project.packaging.output.documentationDir).include('./**')
					//from project.file('README.md')
					from project.packaging.source.fileCollection
				}.execute()
			}
		}

		project.task('installer', dependsOn: 'packageZip') {

			doLast {

				makeShellPackageConfiguration(project)

				def installerFile = project.file("${project.packaging.output.distributionDir}/${project.packaging.distributionName}.shar")

				project.copy {
					from project.zipTree(project.file("${project.packaging.output.distributionDir}/final/${project.packaging.distributionName}.zip"))
					into "${project.packaging.output.distributionDir}/installer/content"
				}
				def installsh = project.file("${project.packaging.output.distributionDir}/installer/install.sh")
				if (installsh.exists()) installsh.delete()
				installsh.append(new File("${toolResourcesDir}/install/template/install/install-pre.sh").text)

				if (project.packaging.installer.readme.defined()) {
					project.packaging.installer.readme.checkOnlyOneDefinition("packaging.installer.readme")
					installsh.append("readme_to_show=\"content/${project.packaging.installer.readme.location}\"\n")
				}
				else
					installsh.append("readme_to_show=\n")

				if (project.packaging.installer.licence.defined()) {
					project.packaging.installer.licence.checkOnlyOneDefinition("packaging.installer.licence")
					installsh.append("licence_to_show=\"content/${project.packaging.installer.licence.location}\"\n")
				}
				else
					installsh.append("licence_to_show=\n")

				if (project.packaging.installer.userScript.path != null) {
					project.copy {
						from project.packaging.installer.userScript.path
						into "${project.packaging.output.distributionDir}/installer/userscript.sh"
					}
					installsh.append("user_script_to_execute='./userscript.sh'\n")
					installsh.append("user_script_question=\"${project.packaging.installer.userScript.question}\"\n")
				} else {
					installsh.append("user_script_to_execute=\n")
				}
				installsh.append(new File("${toolResourcesDir}/install/template/install/install-post.sh").text)

				project.exec {
					workingDir "${project.packaging.output.distributionDir}"
					// --submitter=who@where
					// --archive-name=${project.packaging.distributionName}
					commandLine 'shar', '-q', 'installer'
					standardOutput = new FileOutputStream(installerFile)
				}

				def shar = project.file("${project.packaging.output.distributionDir}/final/${project.packaging.distributionName}.run")
				if (shar.exists()) shar.delete()

				shar.append(new File("${toolResourcesDir}/install/template/extract-pre.sh").text)

				shar.append("# *** Application variables *** \n")
				shar.append("MDU_INSTALL_APPLICATION_NAME=${project.name}\n")
				shar.append("MDU_INSTALL_APPLICATION_LABEL=${project.name}\n")
				shar.append("MDU_INSTALL_APPLICATION_VERSION=${project.version}\n")
				shar.append("\n")

				shar.append(new File("${project.packaging.output.distributionDir}/${project.packaging.distributionName}.shar").text.replaceAll('\nexit[ ]+0[\n ]*$', "\n"))
				shar.append(new File("${toolResourcesDir}/install/template/extract-post.sh").text)

			}

		}
		project.task('packages', dependsOn: ['documentation','packageZip','installer']) { }
	}
}

class PathOrContentLocation {
	File path
	/** Relative to install content './' */
	String location
	boolean defined() { return location != null || path != null }
	void checkOnlyOneDefinition(String forWhat) { if (location != null && path != null) throw new ShellPackageException("Cannot define both for path and loaction for '$forWhat'") }
}

class ShellPackagePluginExtension {
	boolean ready
	Project project
	class Output {
		String distributionDir
		String documentationDir
	}
	class Source {
		private ConfigurableFileCollection fileCollection
		String directory
	}
	class Installer {
		class UserScript {
			File path
			String question
		}
		final PathOrContentLocation readme = new PathOrContentLocation()
		final PathOrContentLocation licence = new PathOrContentLocation()
		final UserScript userScript = new UserScript()
	}

	final Source source = new Source()
	String distributionName
	final Output output = new Output()
	final Installer installer = new Installer()

	ConfigurableFileCollection sourceFrom(Object... paths) {
		if (source.fileCollection == null)
			source.fileCollection = project.files(paths)
		else
			source.fileCollection.from(paths)
	}
}
