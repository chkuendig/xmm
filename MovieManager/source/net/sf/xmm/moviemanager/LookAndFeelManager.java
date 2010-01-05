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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.xmm.moviemanager.MovieManagerConfig.LookAndFeelType;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

public class LookAndFeelManager {
    
    static Logger log = Logger.getRootLogger();
    
    public static void setLookAndFeel() {
        
        MovieManagerConfig config = MovieManager.getConfig();
        
        final UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        
        if (config.getCustomLookAndFeel().equals("")) {
            LookAndFeel currentLAF = UIManager.getLookAndFeel();
            if (currentLAF != null) {
                config.setCustomLookAndFeel(currentLAF.getName());
            } else {
                for (int i = 0;i<installedLookAndFeels.length;i++) {
                    if(installedLookAndFeels[i].getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
                        config.setCustomLookAndFeel(installedLookAndFeels[i].getName());
                    }
                }
            }
        }
        
        try {
            config.numberOfLookAndFeels = installedLookAndFeels.length;
            
            boolean laFSet = false;
            
            if (config.getLookAndFeelType() == LookAndFeelType.CustomLaF && 
            		!config.getCustomLookAndFeel().equals(UIManager.getLookAndFeel().getName())) {
            	
            	for (int i = 0; i < installedLookAndFeels.length; i++) {
            		if (installedLookAndFeels[i].getName().equals(config.getCustomLookAndFeel())) {
            			setLookAndFeel(installedLookAndFeels[i].getClassName());
            			laFSet = true;
            			break;
            		}
            	}
            }        

            if (config.getLookAndFeelType() == LookAndFeelType.SkinlfLaF &&
            		getSkinlfThemepackList() != null) {
                
            	File theme = new File(config.getSkinlfThemePackDir(), config.getSkinlfThemePack());
            	
            	// All the theme file names were changed (prettyfied). Handle if old theme name is loaded from config 
            	if (!theme.isFile()) {
            		 String newName = theme.getName();
                     newName = newName.replace("themepack", "");
                     
                     char[] newNameArray = newName.toCharArray();
                     newNameArray[0] = Character.toUpperCase(newNameArray[0]);
                     theme = new File(theme.getParentFile(), new String(newNameArray));
            	}
            	
            	if (theme.isFile()) {
            		/* Sets the themepack and then sets the skinlf look and feel */
            		Skin skin = SkinLookAndFeel.loadThemePack(theme.getAbsolutePath());
            		SkinLookAndFeel.setSkin(skin);
            		LookAndFeel laf = new SkinLookAndFeel();
            		setLookAndFeel(laf);
            		laFSet = true;
            	}
            	else {
            		log.debug("SkinLf theme file not found:" + theme);
            		log.debug("Default Look & Feel is used.");
            		
            		config.setLookAndFeelType(LookAndFeelType.CustomLaF);
            		config.setCustomLookAndFeel("Metal");
            	}
            }


            ExtendedTreeCellRenderer.setDefaultColors();

            if (laFSet) {
            	SwingUtilities.invokeLater(new Runnable() {
            		public void run() {
            			SwingUtilities.updateComponentTreeUI(MovieManager.getDialog());
            		}
            	});
            }

        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            e.printStackTrace();
            DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Look and Feel error", "Look and feel may not be properly installed.", e.getMessage());
            GUIUtil.showAndWait(alert, true);
        }
    }
    
    
    public static String [] getSkinlfThemepackList() {
        
        try {
            File dir;
            String dirSep = SysUtil.getDirSeparator();
            MovieManagerConfig config = MovieManager.getConfig();
            
            if (!MovieManager.isApplet()) {
                 
                config.setSkinlfThemePackDir(SysUtil.getUserDir() + dirSep + "lib/LookAndFeels" + dirSep + "Skinlf_Theme_Packs" + dirSep);
                dir = new File(config.getSkinlfThemePackDir());
                
                if (!dir.exists()) {
                	return null;
                }
               // File [] listFiles = dir.listFiles();
                String [] list = dir.list();
                ArrayList<String> themePackList = new ArrayList<String>();
                
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].endsWith(".zip")) {
                            themePackList.add(list[i]);
                        }
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
    
    
    
    protected static void instalLAFs() {
        
        try {
        	           
        	log.debug("Installing Look & Feels.");
        	
        	File lookAndFeel;
        	
        	if (!SysUtil.isMacAppBundle()) {
        		lookAndFeel =  FileUtil.getFile("lib/LookAndFeels/lookAndFeels.ini");
        	} else {
        		File f = new File(SysUtil.getJarLocation()).getParentFile();
        		lookAndFeel = new File(f, "lib/LookAndFeels/lookAndFeels.ini");
        	}
        	
        	if (!lookAndFeel.isFile()) {
        		log.warn("lookAndFeels.ini was not found:" + lookAndFeel.getAbsolutePath());
        		return;
        	}
        	
        	BufferedReader reader = new BufferedReader(new FileReader(lookAndFeel));
        	Pattern p = Pattern.compile("\"(.+?)\"\\s+?\"(.+?)\"\\s+?\"(.+?)\"\\s*(?:\"(.+?)\")?.*");
        	
        	double javaVersion = Double.parseDouble(System.getProperty("java.version").substring(0, 3));
        	
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
            	
            	line = line.trim();
            	
            	if (line.equals(""))
            		continue;
            	
            	Matcher m = p.matcher(line);
            	
            	if (!m.matches() || m.groupCount() < 3)
            		continue;
            	            	
            	try {
            		
            		double javaVersionSupported =  Double.parseDouble(m.group(3));
            		
            		// Check java version
            		if (javaVersionSupported > javaVersion)
            			continue;
            		
            		// Check platform
            		if (m.groupCount() == 4) {
            			String os = m.group(4);
            			
            			if (os != null) {

            				// Currently there is only the office and jgoodies L&F which is unique for Windows and Quaque for Mac.

            				if (os.indexOf("Windows") != -1) {
            					if (!SysUtil.isWindows())	            				
            						continue;
            				}
            				else if (os.indexOf("Mac") != -1) {
            					if (!SysUtil.isMac())	            				
            						continue;
            				}
            				else if (os.indexOf("Linux") != -1) {
            					if (!SysUtil.isLinux())	            				
            						continue;
            				}
            			}
            		}
           		
            		try {
						Class.forName(m.group(2));
						UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(m.group(1), m.group(2)));
					} catch (ClassNotFoundException e) {
						log.warn("Failed to locate L&F class " + m.group(2));
					} catch(Exception e) {
						log.warn("Exception:" + e .getMessage());
					}										
                }
                catch (SecurityException s) {
                	log.warn("SecurityException: "+ s);
                } catch(java.lang.NoClassDefFoundError e) {
                	log.warn("NoClassDefFoundError:" + e.getMessage());
                }	
            }
        }
        catch (Exception e) {
            log.warn("Failed to install Look & Feels.", e);
        }
    }
    
    
    public static void setupOSXLaF() {
        
        if (SysUtil.isMac()) {
         	
        	log.debug("Setting up OS X L&F");
        	
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MeD's Movie Manager");
            
            try {
                Class<?> quaquaClass = ClassLoader.getSystemClassLoader().loadClass("ch.randelshofer.quaqua.QuaquaLookAndFeel");
                LookAndFeel quaquqLAF = (LookAndFeel) quaquaClass.newInstance();
                UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(quaquqLAF.getName(), "ch.randelshofer.quaqua.QuaquaLookAndFeel"));
                // Override system look and feel
                setLookAndFeel(quaquqLAF);
                System.load(FileUtil.getFile("lib/mac/libquaqua.jnilib").getPath());
                
                System.setProperty("Quaqua.JNI.isPreloaded","true");
                log.debug("Quaqua installed");
            } catch (Exception e) {
                log.error("Quaqua Look and Feel not installed: ", e);
            }
            catch(UnsatisfiedLinkError e) {
                log.error("Quaqua installed, but without the native parts: ", e);
            }
        }
    }
    
    private static void setLookAndFeel(final String className) {
    	SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			try {
    				UIManager.setLookAndFeel(className);
				} catch (UnsupportedLookAndFeelException e) {
					log.warn("Exception:" + e.getMessage(), e);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	});
    }
    
    private static void setLookAndFeel(final LookAndFeel lookAndFeel) {
    	SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			try {
    				UIManager.setLookAndFeel(lookAndFeel);
				} catch (UnsupportedLookAndFeelException e) {
					log.warn("Exception:" + e.getMessage(), e);
				}
    		}
    	});
    }
    
    
    public static void macOSXRegistration() {
    	
        if (SysUtil.isMac()) {
        
        	log.debug("Registering OS X application menu");
        	
        	try {
                Class<?> osxAdapter = ClassLoader.getSystemClassLoader().loadClass("net.sf.xmm.moviemanager.util.mac.OSXAdapter");
                Class<?>[] defArgs = {DialogMovieManager.class};
                
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                 
                if (registerMethod != null) {
                    Object[] args = { MovieManager.getDialog() };
                    registerMethod.invoke(osxAdapter, args);
                }
             
                defArgs[0] = boolean.class;
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
               
                if (prefsEnableMethod != null) {
                    Object args[] = {Boolean.TRUE};
                    prefsEnableMethod.invoke(osxAdapter, args);
                }
                
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                log.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")", e);
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the 
                // above NoClassDefFoundError first.
                log.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")", e);
            } catch (Exception e) {
                log.error("Exception while loading the OSXAdapter:", e);
            } catch (Error e) {
            	log.error("Exception while loading the OSXAdapter:", e);
            }
        }
    }
}
