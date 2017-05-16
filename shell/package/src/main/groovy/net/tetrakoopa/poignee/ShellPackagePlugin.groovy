package net.tetrakoopa.poignee

import net.tetrakoopa.poignee.packaage.PathOrContentLocation
import net.tetrakoopa.poignee.packaage.ShellPackagePluginExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

import static net.tetrakoopa.poignee.packaage.ShellPackagePluginExtension.SHELL_PACKAGE_EXTENSION_NAME

class ShellPackagePlugin extends AbstractShellProjectPlugin implements Plugin<Project> {

	public static final String ID = "net.tetrakoopa.poignee.shell-package"


	private void insertProperty(File destination, String propertyName, String propertyValue) {
		if (propertyValue==null || propertyValue.equals(""))
			destination.append("${propertyName}=\n")
		else
			destination.append("${propertyName}=\"${propertyValue}\"\n")
	}
	private boolean insertFileDeclaredAsProperty(File destination, Project project, File file, String propertyName, String fileCopiedName) {
		if (file!=null) {
			project.copy {
				from file
				into "${project.shell_package.output.distributionDir}/installer"
				rename { name -> "${fileCopiedName}" }
			}
			insertProperty(destination, propertyName, "./${fileCopiedName}")
		} else {
			insertProperty(destination, propertyName, null)
		}
	}
	private boolean insertFileOrContentAndDeclareAsProperty(File destination, Project project, PathOrContentLocation pathOrLocation, String propertyName, String fileCopiedName) {
		if (pathOrLocation.defined()) {
			if (pathOrLocation.path != null) {
				insertFileDeclaredAsProperty(destination, project, pathOrLocation.path, propertyName, fileCopiedName)
			} else {
				insertProperty(destination, propertyName, "./content/${pathOrLocation.location}")
			}
			return true
		} else {
			destination.append("${propertyName}=\n")
			return false
		}
	}

	private void setShellPackageDefaultsConfiguration(Project project) {
		ShellPackagePluginExtension shell_package = (ShellPackagePluginExtension) project.getExtensions().findByName(SHELL_PACKAGE_EXTENSION_NAME)
		shell_package.ready = false
		shell_package.distributionName = null
		shell_package.installer.licence.location = shell_package.installer.licence.path = null
		shell_package.installer.readme.location = shell_package.installer.readme.path = null
		shell_package.installer.userScript.script.location = shell_package.installer.userScript.script.path = null
		shell_package.installer.userScript.question = null
		shell_package.output.distributionDir = null
		shell_package.output.documentationDir = null
	}
	private void makeShellPackageConfiguration(Project project) {
		ShellPackagePluginExtension shell_package = (ShellPackagePluginExtension) project.getExtensions().findByName(SHELL_PACKAGE_EXTENSION_NAME)
		if (shell_package.ready) return
		shell_package.ready = true
		if (shell_package.source == null) throw new GradleException("No source file(s) defined")
		if (shell_package.distributionName == null) shell_package.distributionName = "${project.name}-${project.version}"
		if (shell_package.output.distributionDir == null) shell_package.output.distributionDir = "${project.buildDir}/distribution"
		if (shell_package.output.documentationDir == null) shell_package.output.documentationDir = "${project.buildDir}/documentation"
	}

	void apply(Project project) {

		File toolResourcesDir = prepareResources(project, "${ID}", "tool")

		project.extensions.create(SHELL_PACKAGE_EXTENSION_NAME, ShellPackagePluginExtension, project)
		setShellPackageDefaultsConfiguration(project)

		project.task('documentation') {

			ext.inputFiles = project.shell_package.source

			doLast {

				makeShellPackageConfiguration(project)

				def shellScripts = ext.inputFiles

				File docDir = project.file(project.shell_package.output.documentationDir)

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

					baseName = project.shell_package.distributionName

					destinationDir = project.file("${project.shell_package.output.distributionDir}/final")

					from project.shell_package.source
				}.execute()
			}
		}

		project.task('installer', dependsOn: 'packageZip') {

			doLast {

				makeShellPackageConfiguration(project)

				def installerFile = project.file("${project.shell_package.output.distributionDir}/${project.shell_package.distributionName}.shar")

				project.copy {
					from project.zipTree(project.file("${project.shell_package.output.distributionDir}/final/${project.shell_package.distributionName}.zip"))
					into "${project.shell_package.output.distributionDir}/installer/content"
				}
				def installsh = project.file("${project.shell_package.output.distributionDir}/installer/install.sh")
				if (installsh.exists()) installsh.delete()
				installsh.append(new File("${toolResourcesDir}/install/template/install/install-pre.sh").text)

				insertFileOrContentAndDeclareAsProperty(installsh, project, project.shell_package.installer.readme, "readme_to_show", "README")

				insertFileOrContentAndDeclareAsProperty(installsh, project, project.shell_package.installer.licence, "licence_to_show", "LICENCE")

				insertFileOrContentAndDeclareAsProperty(installsh, project, project.shell_package.installer.userScript.script, "user_script_to_execute", "user_post_install")
				insertProperty(installsh, "user_script_question", project.shell_package.installer.userScript.question)

				installsh.append(new File("${toolResourcesDir}/install/template/install/install-post.sh").text)

				project.exec {
					workingDir "${project.shell_package.output.distributionDir}"
					// --submitter=who@where
					// --archive-name=${project.shell_package.distributionName}
					commandLine 'shar', '-q', 'installer'
					standardOutput = new FileOutputStream(installerFile)
				}

				def shar = project.file("${project.shell_package.output.distributionDir}/final/${project.shell_package.distributionName}.run")
				if (shar.exists()) shar.delete()

				shar.append(new File("${toolResourcesDir}/install/template/extract-pre.sh").text)

				shar.append("# *** Application variables *** \n")
				insertProperty(shar, "MDU_INSTALL_APPLICATION_NAME", "${project.name}")
				insertProperty(shar, "MDU_INSTALL_APPLICATION_LABEL", "${project.name}")
				insertProperty(shar, "MDU_INSTALL_APPLICATION_VERSION", "${project.version}")
				shar.append("\n")

				shar.append(new File("${project.shell_package.output.distributionDir}/${project.shell_package.distributionName}.shar").text.replaceAll('\nexit[ ]+0[\n ]*$', "\n"))
				shar.append(new File("${toolResourcesDir}/install/template/extract-post.sh").text)

			}

		}
		project.task('packages', dependsOn: ['documentation','packageZip','installer']) { }
	}
}

