package net.tetrakoopa.poignee.util

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import net.tetrakoopa.mdu4j.util.IOUtil

class ShellPackagePlugin implements Plugin<Project> {

	private File toolRootDir

	private getTopProject(Project project) {
		while (project.getParent() != null) project = project.getParent()
		return project
	}

	private void setShellPackageDefaults(Project project) {
		ShellPackagePluginExtension packaging = (ShellPackagePluginExtension) project.getExtensions().findByName("packaging")
		packaging.sourceDir = "src"
		packaging.distributionName = "${project.name}-${project.version}"
		packaging.output.distributionDir = "${project.buildDir}/distribution"
		packaging.output.documentationDir = "${project.buildDir}/documentation"
	}

	private void prepareTools(Project project) {
		//File.createTempDir

		Project topProject = getTopProject(project);
		toolRootDir = topProject.file("${topProject.buildDir}/net.tetrakoopa.poignee.shell-package/tool")
		toolRootDir.mkdirs()
		InputStream toolInput = getClass().getClassLoader().getResourceAsStream("net.tetrakoopa.poignee.shell-package.tool.zip")
		IOUtil.copy((InputStream)toolInput, new FileOutputStream(topProject.file("${toolRootDir}/tools.zip")))

		topProject.copy {
			from topProject.zipTree(topProject.file("${toolRootDir}/tools.zip"))
			into "${toolRootDir}"
		}
	}

	void apply(Project project) {

		prepareTools(project)

		project.extensions.create("packaging", ShellPackagePluginExtension)
		setShellPackageDefaults(project)

		project.task('documentation') {

			ext.inputFiles = project.fileTree(project.packaging.sourceDir).include('*.sh')

			doLast {
				def shellScripts = ext.inputFiles

				File docDir = project.file(project.packaging.output.documentationDir)

				if (!docDir.exists()) docDir.mkdirs()

				shellScripts.each { File file ->
					def document = new File(docDir, "${file.name}.md")
					project.exec {
						commandLine "${toolRootDir}/doc/shdoc/shdoc_io_w.sh", "${file.path}", "${document.path}"
					}
					// Remove the documentation if it's empty
					if(!(document.length()>0) || document.text.matches("[\n \t]*") ) document.delete()
				}
			}
		}
		project.task('packageZip', type: Zip, dependsOn: 'documentation') {

			baseName = project.packaging.distributionName

			destinationDir = project.file("${project.packaging.output.distributionDir}/final")

			from { ->
				project.fileTree(project.packaging.sourceDir).include('*.sh', '*.py', 'README.md')
			}
			from { ->
				project.fileTree(project.packaging.output.documentationDir).include('./**')
			}
			from  { ->
				project.file('README.md')
			}
		}

		project.task('installer', dependsOn: 'packageZip') {

			ext.outputFiles = project.file("${project.packaging.output.distributionDir}/${project.packaging.distributionName}.shar")

			doLast {

				def installerFile = ext.outputFiles

				project.copy {
					from project.zipTree(project.file("${project.packaging.output.distributionDir}/final/${project.packaging.distributionName}.zip"))
					into "${project.packaging.output.distributionDir}/installer/content"
				}
				project.copy {
					from new File("${toolRootDir}/install/template/install.sh")
					into "${project.packaging.output.distributionDir}/installer"
				}

				project.exec {
					workingDir "${project.packaging.output.distributionDir}"
					// --submitter=who@where
					// --archive-name=${project.packaging.distributionName}
					commandLine 'shar', '-q', 'installer'
					standardOutput = new FileOutputStream(installerFile)
				}

				def shar = project.file("${project.packaging.output.distributionDir}/final/${project.packaging.distributionName}.run")
				if (shar.exists()) shar.delete()

				shar.append(new File("${toolRootDir}/install/template/install-pre.sh").text)

				shar.append("# *** Application variables *** \n")
				shar.append("MDU_INSTALL_APPLICATION_NAME=${project.name}\n")
				shar.append("MDU_INSTALL_APPLICATION_LABEL=${project.name}\n")
				shar.append("MDU_INSTALL_APPLICATION_VERSION=${project.version}\n")
				shar.append("\n")

				shar.append(new File("${project.packaging.output.distributionDir}/${project.packaging.distributionName}.shar").text.replaceAll('\nexit[ ]+0[\n ]*$', "\n"))
				shar.append(new File("${toolRootDir}/install/template/install-post.sh").text)

			}

		}
		project.task('packages', dependsOn: ['documentation','packageZip','installer']) { }
	}
}

class ShellPackagePluginExtension {
	class Output {
		String distributionDir
		String documentationDir
	}
	String sourceDir
	String distributionName
	Output output = new Output()
}
