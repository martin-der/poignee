package net.tetrakoopa.poignee.bundledresources

import net.tetrakoopa.gradle.plugin.common.AbstractProjectPlugin
import net.tetrakoopa.gradle.plugin.common.StringUtil
import net.tetrakoopa.gradle.plugin.common.Util
import net.tetrakoopa.gradle.plugin.common.exception.PluginExtensionException
import net.tetrakoopa.mdu4j.util.IOUtil
import net.tetrakoopa.poignee.bundledresources.exception.BundledPluginExtensionException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.bundling.Zip

class BundledResourcesPlugin extends AbstractProjectPlugin implements Plugin<Project>, DependencyResolutionListener {

	public final static String ID = "net.tetrakoopa.bundled-resources"

	public final static String PLUGIN_ID_EXCLUDED_CHARACTERS = " /\\:<>\"?*|"

	private final static String TASK_NAME_CREATE_RESOURCES_ARCHIVE = "mdu.poignee.createResourcesArchive"
	private final static String TASK_NAME_ADD_RESOURCES_ARCHIVE_TO_RESOURCES = "mdu.poignee.addResourcesArchiveToPluginResources"
	private final static String TASK_NAME_COPY_RESOURCES_TO_INTERMEDIATE_DIRECTORY = "mdu.poignee.taskNameCopyResourcesToIntermediateDirectory"

	private static final String RESOURCE_NAME_PROJECTS_REFERENCES = "net.tetrakoopa.bundled-resources.projects-references.properties";

	private Project project

	private void addDependencies(Project project) {
		final Properties projectsReferences = new Properties()
		projectsReferences.load(SDLPlugin.class.getClassLoader().getResourceAsStream(RESOURCE_NAME_PROJECTS_REFERENCES))

		String selfProjectReference = projectsReferences.getProperty("bundled-resources")

		def compileOnlyDependencies = project.getConfigurations().getByName("compileOnly").getDependencies()

		compileOnlyDependencies.add(project.getDependencies().create(selfProjectReference, { artifact { name = "bundled-resources"; type = 'jar' }}))
	}

	protected void addProjectExtensions(Project project) {
		super.addProjectExtensions(project)
		def bundleExtension = project.extensions.create(BundledResourcesPluginExtension.EXTENSION_NAME, BundledResourcesPluginExtension, project)
		/*bundleExtension.with {
			returnCode {
				executionError = 23
				assertionFailure = 24
			}
		}*/
	}

	@Override
	void apply(Project project) {

		this.project = project

		addProjectExtensions(project)

		project.afterEvaluate {
			BundledResourcesPluginExtension extension = project.extensions.getByName(BundledResourcesPluginExtension.EXTENSION_NAME)

			if (! extension.pluginId?.trim()) {
				throw new BundledPluginExtensionException(BundledResourcesPluginExtension.EXTENSION_NAME, "Plugin Identifier 'pluginId' is missing")
			}

			if (StringUtil.containsOneOf(extension.pluginId.toCharArray(),PLUGIN_ID_EXCLUDED_CHARACTERS.toCharArray())) {
				throw new BundledPluginExtensionException(BundledResourcesPluginExtension.EXTENSION_NAME, "Plugin Identifier 'pluginId' must not contain any of '${PLUGIN_ID_EXCLUDED_CHARACTERS}'");
			}

			int bundleIndex = 0
			extension.getBundles().each { bundle ->
				if (! bundle.name?.trim()) {
					throw new BundledPluginExtensionException(BundledResourcesPluginExtension.EXTENSION_NAME, "bundle[${bundleIndex}].name is missing")
				}
				addCreateResourcesBundleTasks(project, extension.pluginId, bundle.source, bundle.destination, bundle.name)
				bundleIndex++
			}
		}
	}

	public static File unpackBundledResources(Project project, String pluginId, String name) {
		return unpackBundledResourcesUsingThisZip ( project, pluginId, name, null );
	}
	public static File unpackBundledResourcesUsingThisZip(Project project, String pluginId, String name, File zipSource) {

		Project topProject = Util.getTopProject(project)
		File resourcesDir = topProject.file("${topProject.buildDir}/${pluginId}/${name}")
		File resourcesZip = topProject.file("${topProject.buildDir}/${pluginId}/${name}.zip")

		File okFile = new File(resourcesDir, ".unpack-ok");
		if (okFile.exists()) {
			return resourcesDir
		}

		resourcesDir.mkdirs()
		String pluginBundledResource = "${ID}/${pluginId}.${name}.zip"
		//InputStream toolInput = project.getClass().getClassLoader().getResourceAsStream(pluginBundledResource)
		InputStream toolInput;
		if (zipSource != null) {
			// If zipSource is provided (we are is most certainly in a test) we use it as content to bundle
			if (!zipSource.exists()) throw new RuntimeException("Resources bundle zip '${zipSource.absolutePath}' does not exists");
			toolInput = new FileInputStream(zipSource)
			if (toolInput == null) throw new RuntimeException("Cannot read resources bundle zip '${zipSource.absolutePath}'")
		} else {
			// This is the regular case : the resource zip is inside the plugin jar
			toolInput = BundledResourcesPlugin.class.getClassLoader().getResourceAsStream(pluginBundledResource)
			if (toolInput == null) throw new MissingResourceException("No such resources bundle '${pluginBundledResource}' for project ${project.name}", project.class.name, pluginBundledResource)
		}
		IOUtil.copy((InputStream)toolInput, new FileOutputStream(resourcesZip))

		topProject.copy {
			from topProject.zipTree(resourcesZip)
			into "${resourcesDir}"
		}
		resourcesZip.delete()

		okFile.append(new Date().toString().bytes)

		return resourcesDir
	}

	protected void addCreateResourcesBundleTasks(Project project, String pluginId, ConfigurableFileCollection source, String destination, String name) {
		project.logger.info("Adding resource bundle ${name} ")
		Task addCreateResourcesBundleTask = createAddCreateResourcesBundleTasks ( project, pluginId, source, destination, name )
		project.tasks['jar'].dependsOn addCreateResourcesBundleTask
	}
	protected Task createAddCreateResourcesBundleTasks(Project project, String pluginId, ConfigurableFileCollection source, String destination, String name) {
		def zipName = "${pluginId}.${name}.zip"

		final String taskNameCreateResourcesArchive = pluginId+"__"+TASK_NAME_CREATE_RESOURCES_ARCHIVE+"__"+name
		final String taskNameAddResourcesArchiveToResources = pluginId+"__"+TASK_NAME_ADD_RESOURCES_ARCHIVE_TO_RESOURCES+"__"+name
		final String taskNameCopyResourcesToIntermediateDirectory = pluginId+"__"+TASK_NAME_COPY_RESOURCES_TO_INTERMEDIATE_DIRECTORY+"__"+name

		def copyResourcesToIntermediateDirectoryTask = null
		File intermediateDirectory = null

		if (destination != null) {
			intermediateDirectory = project.file("${project.buildDir}/${ID}/intermediate/${name}")
			copyResourcesToIntermediateDirectoryTask = project.task(taskNameCopyResourcesToIntermediateDirectory) {
				doLast {
					project.copy {
						from source
						into new File(intermediateDirectory, destination)
					}
				}
			}
		}

		def createArchiveTask = project.task(taskNameCreateResourcesArchive, type: Zip) {

			archiveName = zipName

			destinationDir = project.file("${project.buildDir}/${ID}")

			from copyResourcesToIntermediateDirectoryTask == null ? source : intermediateDirectory
		}
		if (copyResourcesToIntermediateDirectoryTask != null)
			createArchiveTask.dependsOn copyResourcesToIntermediateDirectoryTask

		def addArchiveToResourcesTask = project.task(taskNameAddResourcesArchiveToResources, dependsOn: taskNameCreateResourcesArchive) {
			doLast {
				project.copy {
					from project.file("${project.buildDir}/${ID}/${zipName}")
					into "${project.buildDir}/resources/main/${ID}"
				}
			}
		}

		return addArchiveToResourcesTask
	}

	@Override
	void beforeResolve(ResolvableDependencies resolvableDependencies) {
		addDependencies(project)
		project.gradle.removeListener(this)
	}

	@Override
	void afterResolve(ResolvableDependencies resolvableDependencies) {

	}
}