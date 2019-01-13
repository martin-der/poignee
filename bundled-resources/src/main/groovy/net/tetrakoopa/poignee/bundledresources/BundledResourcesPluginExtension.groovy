package net.tetrakoopa.poignee.bundledresources

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.util.ConfigureUtil

class BundledResourcesPluginExtension {

	public final static String EXTENSION_NAME = "bundled"

	private Project project

	BundledResourcesPluginExtension(Project project) {
		this.project = project
	}

	String pluginId

	static class Bundle {
		private Project project

		String name
		private ConfigurableFileCollection source
		private String destination

		Bundle(Project project) {
			this.project = project
		}

		String into(String dirs) {
			destination = dirs
			return destination
		}
		ConfigurableFileCollection from(Object... paths) {
			if (source == null)
				source = project.files(paths)
			else
				source.from(paths)
			return source
		}
	}

	private List<Bundle> bundles = []

	void bundle(Closure closure) {
		Bundle bundle = new Bundle(project)
		ConfigureUtil.configure(closure, bundle)
		//project.configure(bundle, closure)
		bundles.add(bundle)
	}

	List<Bundle> getBundles() { return bundles }

}
