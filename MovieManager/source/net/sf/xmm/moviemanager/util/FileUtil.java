/**
 * @(#)FileUtil.java 1.0 23.03.05 (dd.mm.yy)
 *
 * Copyright (2003) Bro3
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.util;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;

import java.net.*;
import java.io.*;

public class FileUtil {
    
    static Logger log = Logger.getRootLogger();
    
    public static void writeToFile(String fileName, StringBuffer data) {
	try {
	    FileOutputStream fileStream = new FileOutputStream(new File(fileName));
	    for (int u = 0; u < data.length(); u++)
		fileStream.write(data.charAt(u));
	    fileStream.close();
	    
	} catch (Exception e) {
	    MovieManager.log.error("Exception:"+ e.getMessage());
	}
    }
    
    
    /**
     * Returns a resource as a Stream or null if not found.
     *
     * @param name A resource name.
     **/
    public static InputStream getResourceAsStream(String name) {

	try {
	    return FileUtil.class.getResourceAsStream(name);

	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	}
	return null;
    }
    
    
     public static URL getFile(String fileName) {

    	URL url = null;

	try {
	    //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8");

	    if (!MovieManager.isApplet()) {
		url = new File(getUserDir() + fileName).toURL();
	    }
	    else {

		fileName = fileName.replaceAll("\\\\", "/");

		if (fileName.startsWith("/"))
		    fileName = fileName.replaceFirst("/", "");

		//log.debug("fileName:" + fileName);
		//log.debug("codebase:"+ _movieManager.applet.getCodeBase());

		url = new URL(MovieManager.applet.getCodeBase(), fileName);

		//log.debug("URL:"+ url.toString());
		//log.debug("url.getFile():" + url.getFile());
		//log.debug("getPath():" + url.getPath());

		//log.debug("encode:"+URLEncoder.encode(url.toString() , "UTF-8"));
	    }
	    //return new File((java.net.URI) new java.net.URI(URLEncoder.encode(url.toString() , "UTF-8")));

    	} catch(Exception e) {
	    log.error("Exception:" + e.getMessage());
	}
	return url;
     }
    
     /**
     * Getting the 'root directory' of the app.
     **/
    public static String getUserDir() {

	String path = "";

	try {
	    java.net.URL url = MovieManager.class.getProtectionDomain().getCodeSource().getLocation();
	    File file = new File(java.net.URLDecoder.decode(url.getPath(), "UTF-8"));

	    /* If running in a jar file the parent is the root dir */
	    if (file.isFile())
	    	path = file.getParentFile().getAbsolutePath();
	    else
	    	path = file.getAbsolutePath();
	}
	catch (UnsupportedEncodingException e) {
	    path = System.getProperty("user.dir");
	}

	if (!path.endsWith(getDirSeparator()))
	    path += getDirSeparator();

	return path;
    }
    
      public static String getLineSeparator() {
	return System.getProperty("line.separator");
    }


    public static String getDirSeparator() {
	return File.separator;
    }
} 
