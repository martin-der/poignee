package net.tetrakoopa.gradle.plugin.common.file;

import net.tetrakoopa.gradle.plugin.common.file.exception.NotARelativeFileException;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import groovy.lang.Closure;

public class DefaultRelativeConfigurableFileCollection implements RelativeConfigurableFileCollection {

	private File root;

	private final ConfigurableFileCollection configurableFileCollection;


	public DefaultRelativeConfigurableFileCollection(ConfigurableFileCollection configurableFileCollection) {
		this.configurableFileCollection = configurableFileCollection;
	}


	protected static void checkRootAgainstFiles(File root, Set<File> files) {
		Helper.checkRootAgainstFiles(root, files);
	}
	protected void checkRootAgainstFiles() {
		checkRootAgainstFiles(root, configurableFileCollection.getFiles());
	}

	@Override
	public void setRootDir(File root) {
		this.root = root;
		checkRootAgainstFiles();
	}

	@Override
	public File getRootDir() {
		return root;
	}

	@Override
	public void rootDir(File root) {
		setRootDir(root);
	}

	@Override
	public Set<String> getRelativePaths() {
		if (root==null) throw new IllegalStateException("Root directory is not defined");
		return Helper.getRelativePaths(root.getAbsolutePath(), getFiles());
	}

	static class Helper {

		public static Set<String> getRelativePaths(String rootDirAbsolutePath, Set<File> files) {
			final Set<String> relativePaths = new HashSet<>();
			files.forEach(file -> {
				final String fileAbsolutePath = file.getAbsolutePath();
				if (! fileAbsolutePath.startsWith(rootDirAbsolutePath)) {
					throw new NotARelativeFileException(rootDirAbsolutePath, fileAbsolutePath);
				}
			});
			return relativePaths;
		}
		public static void checkRootAgainstFiles(File root, Set<File> files) {
			if (root != null) {
				String rootAbsolutePath = root.getAbsolutePath();
				files.forEach(file ->  {
					String fileAbsolutePath = file.getAbsolutePath();
					if (! fileAbsolutePath.startsWith(rootAbsolutePath)) {
						throw new NotARelativeFileException(rootAbsolutePath, fileAbsolutePath);
					}
				});
			}
		}
	}

	@Override
	public Set<Object> getFrom() {
		return configurableFileCollection.getFrom();
	}

	@Override
	public void setFrom(Iterable<?> iterable) {
		configurableFileCollection.setFrom(iterable);
		checkRootAgainstFiles();
	}

	@Override
	public void setFrom(Object... objects) {
		configurableFileCollection.setFrom(objects);
		checkRootAgainstFiles();
	}

	@Override
	public ConfigurableFileCollection from(Object... objects) {
		ConfigurableFileCollection collection = configurableFileCollection.from(objects);
		checkRootAgainstFiles();
		return collection;
	}

	@Override
	public Set<Object> getBuiltBy() {
		return configurableFileCollection.getBuiltBy();
	}

	@Override
	public ConfigurableFileCollection setBuiltBy(Iterable<?> iterable) {
		return configurableFileCollection.setBuiltBy(iterable);
	}

	@Override
	public ConfigurableFileCollection builtBy(Object... objects) {
		return configurableFileCollection.builtBy(objects);
	}

	@Override
	public File getSingleFile() throws IllegalStateException {
		return configurableFileCollection.getSingleFile();
	}

	@Override
	public Set<File> getFiles() {
		return configurableFileCollection.getFiles();
	}

	@Override
	public boolean contains(File file) {
		return configurableFileCollection.contains(file);
	}

	@Override
	public String getAsPath() {
		return configurableFileCollection.getAsPath();
	}

	@Override
	public FileCollection plus(FileCollection fileCollection) {
		return configurableFileCollection.plus(fileCollection);
	}

	@Override
	public FileCollection minus(FileCollection fileCollection) {
		return configurableFileCollection.minus(fileCollection);
	}

	@Override
	public FileCollection filter(Closure closure) {
		return configurableFileCollection.filter(closure);
	}

	@Override
	public FileCollection filter(Spec<? super File> spec) {
		return configurableFileCollection.filter(spec);
	}

	@Override
	public boolean isEmpty() {
		return configurableFileCollection.isEmpty();
	}

	@Override
	public FileTree getAsFileTree() {
		return configurableFileCollection.getAsFileTree();
	}

	@Override
	public void addToAntBuilder(Object o, String s, AntType antType) {
		configurableFileCollection.addToAntBuilder(o,s,antType);
	}

	@Override
	public Object addToAntBuilder(Object o, String s) {
		return configurableFileCollection.addToAntBuilder(o, s);
	}

	@Override
	public Iterator<File> iterator() {
		return configurableFileCollection.iterator();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return configurableFileCollection.getBuildDependencies();
	}
}
