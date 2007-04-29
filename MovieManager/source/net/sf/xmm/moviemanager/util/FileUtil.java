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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;

import javax.swing.JApplet;
import javax.swing.filechooser.FileSystemView;

public class FileUtil {
    
    static Logger log = Logger.getRootLogger();
    
    
    public static String getLineSeparator() {
        return System.getProperty("line.separator"); //$NON-NLS-1$
    }
    
    
    public static String getDirSeparator() {
        return File.separator;
    }
    
    /* Mainly used for debugging */
    public static void writeToFile(String fileName, StringBuffer data) {
        try {
            FileOutputStream fileStream = new FileOutputStream(new File(fileName));
            for (int u = 0; u < data.length(); u++)
                fileStream.write(data.charAt(u));
            fileStream.close();
            
        } catch (Exception e) {
            log.error("Exception:"+ e.getMessage());
        }
    }
    
    
    public static InputStream getResourceAsStream(String name) {
    	return getResourceAsStream(name, null);
    }
    /**
     * Returns a resource as a Stream or null if not found.
     *
     * @param name A resource name.
     **/
    public static InputStream getResourceAsStream(String name, JApplet applet) {
        
        try {
            if (applet != null) {
                if (!name.startsWith("/"))
                    name = "/" + name;
                    
                return applet.getClass().getResourceAsStream(name);
            }
            else
                return FileUtil.class.getResourceAsStream(name);
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }
        return null;
    }
    
    
    public static File getFile(String fileName) {
        try {
            return new File(new URI(null, getFileURL(fileName).toString(), null));
        } catch (URISyntaxException e) {
            log.error(e);
        }
       return null;
    }
    

    public static URL getFileURL(String fileName) {
    	return getFileURL(fileName, null);
    }
    

    public static URL getFileURL(String fileName, JApplet applet) {
        
        URL url = null;
		
        try {
	    //URL p = FileUtil.class.getResource(fileName);
	    //String path = URLDecoder.decode(fileName, "UTF-8");
	   
	    if (applet == null) {
		
		File f;
            	
		if (fileName.startsWith("/")) {
		    f = new File(fileName);
		    System.err.println("url1:" + f + " (" + new File(fileName).exists() + ")");
		} else {
		    f = new File(getUserDir() + fileName);
		    System.err.println("url2:" + f + " (" + new File(getUserDir() + fileName).exists() + ")");
		}
		
		url = f.toURL();
		
		/*
		// If it exists inside the jar 
		if (!f.exists()) {
		    url = FileUtil.class.getResource("/" + fileName);
		    
		    System.err.println("ur3:" + url + "  ("+ new File(url.toString()).isFile() +")" );
		    
		    //url = new File("/MovieManager.tmx").toURL();
		    
		    
		    //f = new File("/" + fileName);
		    //System.err.println("url3:" + f + " (" + new File("/" + fileName).exists() + ")");
		    //System.err.println("url3:"+ url);
		}
		*/
		
            }
            else {
                
                fileName = fileName.replaceAll("\\\\", "/");
                
                if (fileName.startsWith("/"))
                    fileName = fileName.replaceFirst("/", "");
                
                //log.debug("fileName:" + fileName);
                //log.debug("codebase:"+ _movieManager.applet.getCodeBase());
                
                url = new URL(applet.getCodeBase(), fileName);
                
		//log.debug("URL:"+ url.toString());
                //log.debug("url.getFile():" + url.getFile());
                //log.debug("getPath():" + url.getPath());
                
                //log.debug("encode:"+URLEncoder.encode(url.toString() , "UTF-8"));
            }
            //return new File((java.net.URI) new java.net.URI(URLEncoder.encode(url.toString() , "UTF-8")));
            
        } catch(Exception e) {
            log.error("Exception:" + e.getMessage(), e);
        }
        return url;
    }
    
    
    
    /**
     * Getting the 'root directory' of the app.
     **/
    public static String getUserDir() {
        
        String path = ""; //$NON-NLS-1$
        
        try {
            //java.net.URL url = MovieManager.class.getProtectionDomain().getCodeSource().getLocation();
            java.net.URL url = FileUtil.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(java.net.URLDecoder.decode(url.getPath(), "UTF-8")); //$NON-NLS-1$
            //File file = new File(java.net.URLDecoder.decode(System.getProperty("user.dir"), "UTF-8"));
            
	    // If running in a jar file the parent is the root dir 
            if (file.isFile())
                path = file.getParentFile().getAbsolutePath();
            else
                path = file.getAbsolutePath();
            
            
            /* If running in a mac application bundle, we can't write in the application-directory, so we use the home of the user */
            if (FileUtil.isMac() /* && path.indexOf(".app/Contents") > -1 */) {
                path = System.getProperty("user.home") + "/Library/Application Support/MovieManager/";
                File dir = new File(path);
                
                if (!dir.exists()) {
                    if(!dir.mkdir()) {
                        log.error("Could not create settings folder.");
                    }
                }
            }
        }
        catch (UnsupportedEncodingException e) {
            path = System.getProperty("user.dir"); //$NON-NLS-1$
        }
        
        if (!path.endsWith(getDirSeparator()))
            path += getDirSeparator();
        
        return path;
    }
    
    public static Image getImage(String imageName) {
    	return getImage(imageName, null);
    }
    
    public static Image getImage(String imageName, JApplet applet) {
        Image image = null;
        
        try {
            
            if (applet != null) {
                //URL url = MovieManager.getIt().getClass().getResource(imageName);
            	URL url = FileUtil.class.getResource(imageName);
                image = applet.getImage(url);
            }
            else {
                String path = "";
                
                if (!new File(imageName).exists()){
                    path = System.getProperty("user.dir");
                }
                if (new File(path + imageName).exists()) {
                    image = Toolkit.getDefaultToolkit().getImage(path + imageName);
                }
                else {
                    
                    try {
                        //URL url = MovieManager.class.getResource(imageName);
                        URL url = FileUtil.class.getResource(imageName);
                        image = Toolkit.getDefaultToolkit().getImage(url);
                    }
                    catch (Exception e) {
                        log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
        return image;
    }
    
    
    public static String getPath(String fileName) {
        String path = ""; //$NON-NLS-1$
        try {
            //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8"); //$NON-NLS-1$
            path = URLDecoder.decode(FileUtil.class.getResource(fileName).getPath(), "UTF-8"); //$NON-NLS-1$
             }
        catch (Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
        return path;
    }
    
    
    public static File getAppletFile(String fileName, JApplet applet) {
        
        try {
            //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8");
            
            fileName = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
            
            if (fileName.startsWith("/")) //$NON-NLS-1$
                fileName = fileName.replaceFirst("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
            
            //log.debug("fileName:" + fileName);
            //log.debug("codebase:"+ _movieManager.applet.getCodeBase());
            
            URL url = new URL(applet.getCodeBase(), fileName);
            
            //log.debug("URL"+ url.toString());
            //log.debug("url.getFile():" + url.getFile());
            
            //log.debug("encode:"+URLEncoder.encode(url.toString() , "UTF-8"));
            
            
            return new File(url.toString());
            
            //return new File((java.net.URI) new java.net.URI(URLEncoder.encode(url.toString() , "UTF-8")));
            
        } catch(Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
        return null;
    }
    
    
    public static String getDriveDisplayName(File path) {
        
        FileSystemView fsv = new javax.swing.JFileChooser().getFileSystemView();
        
        if (fsv != null) {
            
            File tmp = path;
            
            while (tmp.getParentFile() != null)
                tmp = tmp.getParentFile();
            
            String displayName = fsv.getSystemDisplayName(tmp);
                        
            if (!displayName.trim().equals(""))
                return displayName;
            
            return "";
        }
            
        return null;
    }
    
    public static boolean isMac() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return os != null && os.toLowerCase().startsWith("mac") ? true : false; //$NON-NLS-1$
    }
    
    public static boolean isLinux() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return os != null && os.toLowerCase().startsWith("linux") ? true : false; //$NON-NLS-1$
    }
    
    public static boolean isSolaris() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return os != null && (os.toLowerCase().startsWith("sunos") || os.toLowerCase().startsWith("solaris")) ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static boolean isWindows() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return os != null && os.toLowerCase().startsWith("windows") ? true : false; //$NON-NLS-1$
    }
} 
