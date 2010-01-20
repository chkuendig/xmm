package net.sf.xmm.moviemanager.imdblib;

public class IMDbLib {
	
	final static String version = "17.01.2010";
	final static String name = "IMDb lib";
	
	final static int release = 1;
	
	public static int getRelease() {
		return release;
	}
	
	public String getName() {
		return name;
	}
	
	public static String getVersion() {
		return version;
	}
	
	public static String getVersionString() {
		return name + " " + version;
	}
	
}
