/**
 * @(#)LookAndFeelManager.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class LookAndFeelManager {
    
    static Logger log = Logger.getRootLogger();
    
    public static void setLookAndFeel() {
        
        MovieManagerConfig config = MovieManager.getConfig();
        
        UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        
        if(config.getLookAndFeelString().equals("")) {
            LookAndFeel currentLAF = UIManager.getLookAndFeel();
            if (currentLAF != null) {
                config.setLookAndFeelString(currentLAF.getName());
            } else {
                for(int i = 0;i<installedLookAndFeels.length;i++) {
                    if(installedLookAndFeels[i].getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
                        config.setLookAndFeelString(installedLookAndFeels[i].getName());
                    }
                }
            }
        }
        
        try {
            config.numberOfLookAndFeels = installedLookAndFeels.length;
            
            if (getSkinlfThemepackList() != null && config.getLookAndFeelType() == 1) {
                
                /* Sets the themepack and then sets the skinlf look and feel */
                Skin skin = SkinLookAndFeel.loadThemePack(config.getSkinlfThemePackDir()+ config.getSkinlfThemePack());
                SkinLookAndFeel.setSkin(skin);
                LookAndFeel laf = new SkinLookAndFeel();
                UIManager.setLookAndFeel(laf);
                UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
            }
            
            if (getOyoahaThemepackList() != null && config.getLookAndFeelType() == 2 ) {
                
                File theme = new File(config.getOyoahaThemePackDir()+ config.getOyoahaThemePack());
                OyoahaLookAndFeel lnf = new OyoahaLookAndFeel();
                
                if(theme.isFile())
                    lnf.setOyoahaTheme(theme);
                
                UIManager.setLookAndFeel(lnf);
            }
            
            /*Any lookAndFeel but skinlf and oyoaha*/
            if (config.getLookAndFeelType() != 1 && config.getLookAndFeelType() != 2) {
                for (int i = 0; i < installedLookAndFeels.length; i++) {
                    if (installedLookAndFeels[i].getName().equals(config.getLookAndFeelString())) {
                        UIManager.setLookAndFeel(installedLookAndFeels[i].getClassName());
                        break;
                    }
                }
            }
            
            SwingUtilities.updateComponentTreeUI(MovieManager.getDialog());
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Look and Feel error", "Look and feel may not be properly installed.", e.getMessage());
            GUIUtil.showAndWait(alert, true);
        }
    }
    
    
    public static String [] getSkinlfThemepackList() {
        
        try {
            File dir;
            String dirSep = FileUtil.getDirSeparator();
            MovieManagerConfig config = MovieManager.getConfig();
            
            if (!MovieManager.isApplet()) {
                
                config.setSkinlfThemePackDir(FileUtil.getUserDir() + dirSep + "LookAndFeels" + dirSep + "Skinlf Theme Packs" + dirSep);
                
                dir = new File(config.getSkinlfThemePackDir());
                
                if (!dir.exists() && !MovieManager.isMacAppBundle()) {
                    dir.mkdirs();
                    
                    String text = "Here you can add new Skinlf themes."+ FileUtil.getLineSeparator()+
                    "Simply put the .zip files into the 'Skinlf Theme Packs' directory.";
                    
                    File skinlf = null;
                    
                    skinlf = new File(config.getSkinlfThemePackDir() + "Skinlf.txt");
                    
                    if (skinlf.createNewFile()) {
                        
                        /* Writes the skinlf textfile. */
                        FileOutputStream stream = new FileOutputStream(skinlf);
                        for (int i = 0; i < text.length(); i++) {
                            stream.write(text.charAt(i));
                        }
                        stream.close();
                    }
                    return null;
                }
                
                String [] list = dir.list();
                ArrayList themePackList = new ArrayList();
                if(list != null) {
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].endsWith(".zip"))
                            themePackList.add(list[i]);
                    }
                }
                if (themePackList.size() == 0)
                    return null;
                
                String [] tempList = new String[themePackList.toArray().length];
                tempList = (String[]) themePackList.toArray(tempList);
                return tempList;
            }
            
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
    
    
    
    public static String [] getOyoahaThemepackList() {
        
        try {
            MovieManagerConfig config = MovieManager.getConfig();
            String dirSep = FileUtil.getDirSeparator();
            
            if (!MovieManager.isApplet()) {
                config.setOyoahaThemePackDir(FileUtil.getUserDir() + dirSep + "LookAndFeels" + dirSep + "Oyoaha Theme Packs" + dirSep);
                File dir = new File(config.getOyoahaThemePackDir());
                
                ArrayList themePackList = new ArrayList();
                themePackList.add("Default Theme");
                
                if (!dir.exists()) {
                    dir.mkdir();
                    
                    String text = "Here you can add new Oyoaha themes."+ FileUtil.getLineSeparator()+
                    "Simply put the .zotm files into the 'Oyoaha Theme Packs' directory.";
                    
                    File oyoaha = null;
                    
                    oyoaha = new File(config.getOyoahaThemePackDir() + "Oyoaha.txt");
                    
                    if (oyoaha.mkdirs() && oyoaha.createNewFile()) {
                        /* Writes the oyoaha textfile. */
                        FileOutputStream stream = new FileOutputStream(oyoaha);
                        for (int i=0; i < text.length(); i++) {
                            stream.write(text.charAt(i));
                        }
                        stream.close();
                    }
                }
                else {
                    String [] list = dir.list();
                    
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].endsWith(".zotm"))
                            themePackList.add(list[i]);
                    }
                }
                
                String [] tempList = new String[themePackList.toArray().length];
                tempList = (String[]) themePackList.toArray(tempList);
                
                return tempList;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
    
    
    protected static void instalLAFs() {
        
        try {
        	   	
        	File lookAndFeel;
        	
        	if (!MovieManager.isMacAppBundle()) {
        		lookAndFeel =  FileUtil.getFile("LookAndFeels/lookAndFeels.ini");
        	} else {
                // Search in the absolute Path of the Application Bundle (Search as if we weren't in a Application Bundle)
            	lookAndFeel =  FileUtil.getFile(System.getProperty("user.dir") + "/LookAndFeels/lookAndFeels.ini");
            }
        	
        	if (!lookAndFeel.isFile()) {
        		log.warn("lookAndFeels.ini was not found");
        		return;
        	}
        	
        	BufferedReader reader = new BufferedReader(new FileReader(lookAndFeel));
        	Pattern p = Pattern.compile("\"(.+?)\"\\s+?\"(.+?)\".*");
        	
        	String line;
        	boolean start = false;
        	
            while (true) {
             	
            	line = reader.readLine();
            	
            	if (line == null)
            		break;
            	
            	if (!start) {
            		if (line.indexOf("#") != -1)
            			start = true;
            		continue;
            	}
            	
            	if (line.trim().equals(""))
            		continue;
            	
            	Matcher m = p.matcher(line);
            	            	
            	if (!m.matches() || m.groupCount() != 2)
            		continue;
            		
            	try {
					UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(m.group(1), m.group(2)));
                }
                catch (SecurityException s) {
                    log.error("SecurityException: "+ s);
                }
            }
        }
        catch (Exception e) {
            log.warn("Failed to install Look & Feels.", e);
        }
    }
    
    
    public static void setupOSXLaF() {
        
        if (FileUtil.isMac()) {
            
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MeD's Movie Manager");
            
            try {
                Class quaquaClass = ClassLoader.getSystemClassLoader().loadClass("ch.randelshofer.quaqua.QuaquaLookAndFeel");
                LookAndFeel quaquqLAF = (LookAndFeel) quaquaClass.newInstance();
                UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(quaquqLAF.getName(), "ch.randelshofer.quaqua.QuaquaLookAndFeel"));
                // Override system look and feel
                UIManager.setLookAndFeel(quaquqLAF);
                System.load(FileUtil.getFile(System.getProperty("user.dir") + "/lib/mac/libquaqua.jnilib").getPath());
                System.setProperty("Quaqua.JNI.isPreloaded","true");
                log.debug("Quaqua installed");
            } catch (Exception e) {
                log.error("Quaqua Look and Feel not installed: " + e);
            }
            catch(UnsatisfiedLinkError e) {
                log.error("Quaqua installed, but without the native parts: " + e);
            }
        }
    }
    
    public static void macOSXRegistration() {
        if (FileUtil.isMac()) {
            try {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("net.sf.xmm.moviemanager.util.mac.OSXAdapter");
                
                Class[] defArgs = {DialogMovieManager.class};
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                if (registerMethod != null) {
                    Object[] args = { MovieManager.getDialog() };
                    registerMethod.invoke(osxAdapter, args);
                }
                // This is slightly gross.  to reflectively access methods with boolean args, 
                // use "boolean.class", then pass a Boolean object in as the arg, which apparently
                // gets converted for you by the reflection system.
                defArgs[0] = boolean.class;
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
                if (prefsEnableMethod != null) {
                    Object args[] = {Boolean.TRUE};
                    prefsEnableMethod.invoke(osxAdapter, args);
                }
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                log.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the 
                // above NoClassDefFoundError first.
                log.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (Exception e) {
                log.error("Exception while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }
}
