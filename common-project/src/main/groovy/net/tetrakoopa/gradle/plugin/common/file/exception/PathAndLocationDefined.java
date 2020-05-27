package net.tetrakoopa.gradle.plugin.common.file.exception;

public class PathAndLocationDefined extends AbstractFileException {

	public PathAndLocationDefined() {
		this(null);
	}
	public PathAndLocationDefined(String forWhat) {
		super("Both a part and a location have been provided"+(forWhat!=null?(" for "+forWhat):""));
	}
}
