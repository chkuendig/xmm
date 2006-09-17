/**
 * @(#)LookAndFeelManager.java 1.0 20.08.06 (dd.mm.yy)
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

import org.apache.log4j.Logger;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class LookAndFeelManager {
    
    static Logger log = Logger.getRootLogger();
    
    public static void setLookAndFeel() {
	
	MovieManagerConfig config = MovieManager.getConfig();

	try {
	    UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
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
	    
	    SwingUtilities.updateComponentTreeUI(MovieManager.getIt());
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    DialogAlert alert = new DialogAlert("Look and Feel error", "Look and feel may not be properly installed.", e.getMessage());
	    alert.setVisible(true);
	}
    }
    
    
    public static String [] getSkinlfThemepackList() {
	
	try {
	    File dir;
	    String dirSep = MovieManager.getDirSeparator();
	    MovieManagerConfig config = MovieManager.getConfig();
	    
	    if (!MovieManager.isApplet()) {
	    
		config.setSkinlfThemePackDir(MovieManager.getUserDir() + dirSep + "LookAndFeels" + dirSep + "Skinlf Theme Packs" + dirSep);
		
	    	dir = new File(config.getSkinlfThemePackDir());
		
		if (!dir.exists()) {
		    dir.mkdir();
		
		    String text = "Here you can add new Skinlf themes."+ MovieManager.getLineSeparator()+
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
	    
	    	for (int i = 0; i < list.length; i++) {
		    if (list[i].endsWith(".zip"))
			themePackList.add(list[i]);
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
	    String dirSep = MovieManager.getDirSeparator();
	    
	    if (!MovieManager.isApplet()) {
		config.setOyoahaThemePackDir(MovieManager.getUserDir() + dirSep + "LookAndFeels" + dirSep + "Oyoaha Theme Packs" + dirSep);
		File dir = new File(config.getOyoahaThemePackDir());
		
		ArrayList themePackList = new ArrayList();
	    	themePackList.add("Default Theme");
		
	    	if (!dir.exists()) {
		    dir.mkdir();
		
		    String text = "Here you can add new Oyoaha themes."+ MovieManager.getLineSeparator()+
	    		"Simply put the .zotm files into the 'Oyoaha Theme Packs' directory.";
		
		    File oyoaha = null;
		
		    oyoaha = new File(config.getOyoahaThemePackDir() + "Oyoaha.txt");
		
		
		    if (oyoaha.createNewFile()) {
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
	    URL url = MovieManager.getFile("LookAndFeels/lookAndFeels.ini");
	    BufferedInputStream stream = new BufferedInputStream(url.openStream());
				
	    int buffer;
	    StringBuffer lookAndFeel = new StringBuffer();
	    
	    /* Reads the lookAndFeels.ini textfile. */
	    while ((buffer = stream.read()) != -1) {
	    	lookAndFeel.append((char)buffer);
	    }
	    stream.close();
	    
	    String lineSeparator = MovieManager.getLineSeparator();
	    int start = lookAndFeel.indexOf("#")+2;
	    
	    if (start == 1)
		start = lookAndFeel.indexOf("\"", 0);
	    
	    int end = start;
	    String name = "";
	    String className = "";
	    String line;	    
	    int fileEnd = lookAndFeel.length();
	    
	    while (true) {
		end = lookAndFeel.indexOf(lineSeparator, start);
		
		/*If the last line has no lineSeparator at the end*/
		if (end == -1)
		    end = fileEnd;
		
		if (start >= end)
		    break;
		
		line = lookAndFeel.substring(start, end);
		if (line.startsWith("\"")) {
		    
		    end = lookAndFeel.indexOf("\"", start+1);
		    if (end == -1)
			break;
		    
		    name = lookAndFeel.substring(start+1, end);
		    start = lookAndFeel.indexOf("\"", end+1);
		    end = lookAndFeel.indexOf("\"", start+1);
		    className = lookAndFeel.substring(start+1, end);
		    
		    try {
			UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(name, className));
		    }
		    catch (SecurityException s) {
			log.error("SecurityException: "+ s);
		    }
		}
		start = lookAndFeel.indexOf("\"", end+1);
		
		if (start == -1) {
		    break;
		}
	    }
	}
	catch (Exception e) {
	    log.warn("Failed to open lookAndFeels.ini file.");
	}
    }
}
