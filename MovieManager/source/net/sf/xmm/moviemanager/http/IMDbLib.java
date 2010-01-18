package net.sf.xmm.moviemanager.http;

public class IMDbLib {
	
	final static String version = "1.0";
	final static String versionString = "IMDb lib " + version;
	
	final static int numric_version = 1;
	
	public static int getNumericVersion() {
		return numric_version;
	}
	
	public static String getVersion() {
		return version;
	}
	
	public static String getVersionString() {
		return versionString;
	}
	
}
