Poignee
=======

Collection of gradle plugins

[![](https://jitpack.io/v/net.tetrakoopa/poignee.svg)](https://jitpack.io/#net.tetrakoopa/poignee)

# Bundled Resources plugin


~~~groovy
apply plugin: 'net.tetrakoopa.bundled-resources'
~~~

Declare resource folders to be added to the plugin

~~~groovy
bundled {
	pluginId 'my.plugin.id'

	bundle {
		name = 'tool'
		from file('toolz')
	}

	// as many bundles as needed...
	bundle {
		name = 'misc'
		from file('/public/lost+found')
	}
}
~~~

Add this code to 'my.plugin.id' Plugin implementation :

~~~groovy
project.afterEvaluate {
   	BundledResourcesPlugin.unpackBundledResources(project, 'my.plugin.id', 'tool')
}
~~~

Then any project using 'my.plugin.id' will have a `my.plugin.id/tool` folder in their build directory.


# Util plugin

~~~groovy
apply plugin: 'net.tetrakoopa.util'
~~~

## Version

A function `gitVersionName` is added to project.

It uses the most recent tag or SHA1 if none exists.


## Version Mapper

A `VERSION_MAPPER` class is added to project.

This class exposes public functions to modify the version.

* `DROP_LEADING_V` remove the leading 'v' in tag

