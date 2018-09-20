# Bundled Resources

A groovy plugin to bundle resources into a plugin

## HowTo

### Add plugin

```groovy
buildscript {
	dependencies {
		classpath "net.tetrakoopa.poignee:bundled-resources:123456"
	}
}

apply plugin: 'net.tetrakoopa.poignee.bundled-resources'
```

### Configure

example
```groovy
bundled {
	pluginId = 'my.plugin'
	bundle {
		name = 'extra.images'
		from fileTree('../some/other/place/image')
		into 'sub/directory'
	}
}
```

### Result

When 'my.plugin' is applied to a gradle project, then a folder will be created in the build directory (usually `build`) containing the bundled resources.
Using the example from the configure section, a folder `build/my.plugin/extra.images/sub/directory` will contain the resources.
