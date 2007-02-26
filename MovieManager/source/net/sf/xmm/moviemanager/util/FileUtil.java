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

import net.sf.xmm.moviemanager.DialogMovieManager;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;

import java.net.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;

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
            if (MovieManager.isApplet()) {
                if (!name.startsWith("/"))
                    name = "/" + name;
                    
                return DialogMovieManager.applet.getClass().getResourceAsStream(name);
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
        
        URL url = null;
        
        try {
            //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8");
           
            if (!MovieManager.isApplet()) {
            	if(!fileName.startsWith("/")) {
            		url = new File(getUserDir() + fileName).toURL();
            	} else {
            		url = new File(fileName).toURL();
            	}
            }
            else {
                
                fileName = fileName.replaceAll("\\\\", "/");
                
                if (fileName.startsWith("/"))
                    fileName = fileName.replaceFirst("/", "");
                
                //log.debug("fileName:" + fileName);
                //log.debug("codebase:"+ _movieManager.applet.getCodeBase());
                
                url = new URL(DialogMovieManager.applet.getCodeBase(), fileName);
                
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
        
        String path = ""; //$NON-NLS-1$
        
        try {
            java.net.URL url = MovieManager.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(java.net.URLDecoder.decode(url.getPath(), "UTF-8")); //$NON-NLS-1$
            //File file = new File(java.net.URLDecoder.decode(System.getProperty("user.dir"), "UTF-8"));
            
	    // If running in a jar file the parent is the root dir 
            if (file.isFile())
                path = file.getParentFile().getAbsolutePath();
            else
                path = file.getAbsolutePath();
            
            
            /* If running in a mac application bundle, we can't write in the application-directory, so we use the home of the user */
            if (MovieManager.isMac() && path.indexOf(".app/Contents") > -1) {
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
        Image image = null;
        
        try {
            
            if (MovieManager.isApplet()) {
                URL url = MovieManager.getIt().getClass().getResource(imageName);
		image = DialogMovieManager.applet.getImage(url);
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
                        URL url = MovieManager.class.getResource(imageName);
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
            path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
        return path;
    }
    
    
    public static File getAppletFile(String fileName) {
        
        try {
            //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8");
            
            fileName = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
            
            if (fileName.startsWith("/")) //$NON-NLS-1$
                fileName = fileName.replaceFirst("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
            
            //log.debug("fileName:" + fileName);
            //log.debug("codebase:"+ _movieManager.applet.getCodeBase());
            
            URL url = new URL(DialogMovieManager.applet.getCodeBase(), fileName);
            
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
            displayName = MovieManagerCommandAddMultipleMoviesByFile.performExcludeParantheses(displayName, false);
            
            if (!displayName.trim().equals(""))
                return displayName;
            
            return "";
        }
            
        return null;
    }
    
} 
