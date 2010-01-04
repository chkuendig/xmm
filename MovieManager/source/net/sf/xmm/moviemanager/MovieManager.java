/**
 * @(#)MovieManager.java
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

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandExit;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogDatabase;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.http.HttpUtil;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.events.NewDatabaseLoadedHandler;
import net.sf.xmm.moviemanager.swing.extentions.events.NewMovieListLoadedHandler;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.ProgressBean;
import net.sf.xmm.moviemanager.util.ProgressBeanImpl;
import net.sf.xmm.moviemanager.util.SwingWorker;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerLoginHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStartupHandler;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;

public class MovieManager {

	static Logger log = Logger.getLogger(MovieManager.class);
    
	
	static DatabaseHandler databaseHandler = new DatabaseHandler();
    /**
     * Reference to the only instance of MovieManagerConfig.
     **/
    public static MovieManagerConfig config = new MovieManagerConfig();
    
    /**
     * Reference to the only instance of MovieManager.
     **/
    static MovieManager movieManager;
    
    /**
     * Reference to the only instance of DialogMovieManager.
     **/
    static DialogMovieManager dialogMovieManager;
      
    
    
    
    
   
    
    /* While multi-deleting, this is set to true */
    private boolean deleting = false;
    
    private boolean sandbox = false;
    
    public boolean isSandbox() {
    	return sandbox;
    }
    
    public HashMap<String, ModelHTMLTemplate> htmlTemplates = new HashMap<String, ModelHTMLTemplate>();
    
    public ModelHTMLTemplate getTemplate(String name) {
    	return (ModelHTMLTemplate) htmlTemplates.get(name);
    }
  
    
    /**
     * Constructor.
     **/
    private MovieManager() {
        
        dialogMovieManager = new DialogMovieManager();
        
        dialogMovieManager.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                MovieManagerCommandExit.execute();
            }
        });
    }
    
    /* Applet feature is not finished */
    MovieManager(Object applet) {
        
    	
    	getAppMode();
    		
        movieManager = this;
        dialogMovieManager = new DialogMovieManager(applet);
        
        EventQueue.invokeLater(new Runnable() {
            public final void run() {
            	
                /* Disable HTTPClient logging output */
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
               
                //System.setProperty("apache.commons.logging.simplelog.defaultlog", "OFF");
               
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
                
               // System.setProperty("log4j.logger.org.apache.commons.httpclient", "OFF");
                
                //URL configFile = FileUtil.getFileURL("log4j.properties"); //$NON-NLS-1$
                
                //PropertyConfigurator.configure(configFile);
                
                log = Logger.getRootLogger();
                
                /* Writes the date. */
                log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
                log.debug("Starting applet"); //$NON-NLS-1$
                
                /* Loads the config */
                config.loadConfig();
                
                /* Must be executed before the JFrame (MovieManager) object is created. */
                if (config.getDefaultLookAndFeelDecorated()) {
                	DialogMovieManager.setDefaultLookAndFeelDecorated(true);
                }
                
                /* Starts the MovieManager. */
                MovieManager.getDialog().setUp();
                
                /* Loads the database. */
                databaseHandler.loadDatabase();
            }
        });
    }
    
    
    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of MovieManager.
     **/
    public static MovieManager getIt() {
        return movieManager;
    }
    
    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of the DialogMovieManager.
     **/
    public static DialogMovieManager getDialog() {
        return dialogMovieManager;
    }
    
      
    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of MovieManager.
     **/
    public static MovieManagerConfig getConfig() {
        return config;
    }
    
    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }
    
    /**
     * Returns the current database.
     *
     * @return The current database.
     **/
    synchronized public Database getDatabase() {
        return databaseHandler.getDatabase();
    }
    
   
      
    public int getHeight() {
        return dialogMovieManager.getHeight();
    }
    
    public int getWidth() {
        return dialogMovieManager.getWidth();
    }
    
    public Point getLocation() {
        return dialogMovieManager.getLocation();
    }
    
    public int getFontSize() {
        return dialogMovieManager.getFontSize();
    }
    
    
    public static boolean isApplet() {
    	return DialogMovieManager.isApplet();
    }
    
 
    public ModelDatabaseSearch getFilterOptions() {
    	return getFilterOptions(getDatabase());
    }
    
    
    public ModelDatabaseSearch getFilterOptions(Database db) {

    	ModelDatabaseSearch options = new ModelDatabaseSearch();

    	options.setFilterCategory(config.getFilterCategory());

    	if ("Movie Title".equals(config.getFilterCategory()) && config.getIncludeAkaTitlesInFilter()) //$NON-NLS-1$
    		options.setIncludeAkaTitlesInFilter(true);
    	else
    		options.setIncludeAkaTitlesInFilter(false);

    	options.setFilterString(dialogMovieManager.getFilterString());
    	options.setOrderCategory(config.getSortOption());
    	options.setSeen(config.getFilterSeen());
    	
    	if (config.getCurrentLists() == null)
    		options.setCurrentListNames(new ArrayList<String>());
    	else
    		options.setCurrentListNames(new ArrayList<String>(config.getCurrentLists()));
    	
    	options.setShowUnlistedEntries(config.getShowUnlistedEntries());
    	options.setListOption(0);
    	
    	
    	if (db != null) {
	
    		// If there are no lists, or if all the lists are shown in addition to the unlisted ones, no point in enabling lists
    		if ((options.getCurrentListNames().size() > 0 || options.getShowUnlistedEntries()) &&
    				!(options.getShowUnlistedEntries() && options.getCurrentListNames().size() == db.getListsColumnNames().size())) {
    			options.setListOption(1);
    		}

    		ArrayList <String> currentLists = options.getCurrentListNames();
    		ArrayList <String> dbLists = db.getListsColumnNames();
    		    		
    		// Verify all lists. When changing database, this might be a problem
    		for (int i = 0; i < currentLists.size(); i++) {
    			String list = currentLists.get(i);
    			
    			if (!dbLists.contains(list)) {
    				log.warn("Found list " + list + " in currentLists which does not exist in database.");
    				MovieManager.getConfig().getCurrentLists().remove(list);
    				currentLists.remove(i);
    				i--; // Just removed the entry, must use same index again
    			}
    		}
    		
    		options.getFullGeneralInfo = !db.isMySQL();
    	}
    	
    	options.setRatingOption(config.getRatingOption());
    	options.setRating(config.getRatingValue());
    	options.setDateOption(config.getDateOption());
    	options.setDate(config.getDateValue());
    	options.setSearchAlias(config.getSearchAlias());
    	
    	return options;
    }

    
  public void addDatabaseList(String listName) {
    	
		log.info("Ceating list " + listName);
		
		databaseHandler.getDatabase().addListsColumn(listName);
		MovieManager.getConfig().addToCurrentLists(listName);
		
		getDialog().loadMenuLists();
    }


    public void setDeleting(boolean deleting) {
    	this.deleting = deleting;
    }

    public boolean isDeleting() {
    	return deleting;
    }

    
    static void handleVersionUpdate() {
	
    	if (!config.getCheckForProgramUpdates())
    		return;

    	Thread t = new Thread() {

    		public void run() { 
	
    			Thread.currentThread().setPriority(MIN_PRIORITY);
    			
    			try {
    				HttpUtil httpUtil = new HttpUtil(getConfig().getHttpSettings());
    				
    				String buf = httpUtil.readData(new URL("http://xmm.sourceforge.net/LatestVersion.txt")).getData().toString();

    				String [] lines = buf.split("\n|\r\n?");

    				if (lines == null || lines.length == 0)
    					return;

    				if (lines[1].length() > 0 && !lines[1].trim().equals(config.sysSettings.getVersion())) {

    					String currentVersion = config.sysSettings.getVersion().replaceAll("\\.", "").trim();
    					String newVersion = lines[1].replaceAll("\\.", "").trim();

    					// check if only digits. If not, aborted (won't notice about betas)
    					for (int i = 0; i < newVersion.length(); i++) {
    						if (!Character.isDigit(newVersion.charAt(i)))
    							log.debug("Aborting version check. New version contains non-digits:" + newVersion);
    					}
    					
    					// Cut string at first non-digit character
    					for (int i = 0; i < currentVersion.length(); i++) {
    						if (!Character.isDigit(currentVersion.charAt(i))) {
    							currentVersion = currentVersion.substring(0, i);
    							break;
    						}
    					}
    					    					
    					int currentLength = currentVersion.length();
    					int newLength = newVersion.length();
		
    					if (currentLength > newLength) {
    						while (newVersion.length() < currentLength)
    							newVersion += "0";
    					}
    					else if (currentLength < newLength) {
    						while (currentVersion.length() < newLength)
    							currentVersion += "0";
    					}
    				
    					// Checks if the version on the home page is newer than the current version
    					if (Double.parseDouble(newVersion) > Double.parseDouble(currentVersion)) {
    						log.debug("New version available:" + lines[1]);
    						dialogMovieManager.newVersionAvailable(lines[1], buf);
    					}
    				}
    			} catch (Exception e) {
    				log.warn("CheckForProgramUpdates aborted:" + e.getMessage());
    			}
    			
    			log.debug("handleVersionUpdate finished.");
    		}
    	};
    	t.start();
    }

    
  

    public HashMap<String, ModelHTMLTemplate> getHTMLTemplates() {
    	return htmlTemplates;
    }

    void loadHTMLTemplates() {
    	
    	if (MovieManager.getConfig().getInternalConfig().getDisableHTMLView())
    		return;
    		
    	try {

        	File f = FileUtil.getFile(MovieManager.getConfig().HTMLTemplateRootDir);
	
    		if (f != null && f.isDirectory()) {

    			File [] templateFiles = f.listFiles();
	
    			for (int i = 0; i < templateFiles.length; i++) {

    				try {
    					// For each template directory
    					if (templateFiles[i].isDirectory()) {
	
    						// Finding template.txt
    						File template = new File(templateFiles[i], "template.txt");

    						if (!template.isFile()) {
    							log.debug("No template.txt file found in the directory of template " + templateFiles[i] + 
    							"\n Template not added.");
    							continue;
    						}

    						ArrayList<String> lines = FileUtil.readFileToArrayList(template);
    						
    						if (lines == null) {
    							log.error("Failed to read file "  + template);
    							throw new Exception("Failed to read file "  + template);
    						}
    						
    						ModelHTMLTemplate newTemplate = new ModelHTMLTemplate(templateFiles[i].getName(), lines);
    						
    						if (htmlTemplates.containsKey(newTemplate.getName())) {
    							log.warn("A template named " + newTemplate.getName() + " already exists! \r\n" + 
    									templateFiles[i] + " is not added.");
    							continue;
    						}
    						htmlTemplates.put(newTemplate.getName(), newTemplate);
    					
    						// Getting the styles
    						File styles = new File(templateFiles[i], "Styles");

    						if (!styles.isDirectory()) {
    							log.debug("No styles found for HTML template " + templateFiles[i] + 
    							"\n No template styles added.");
    						} else {

    							File [] styleFiles = styles.listFiles();

    							for (int u = 0; u < styleFiles.length; u++) {

    								// Style files end with .style.txt
    								if (!styleFiles[u].getName().endsWith(".style.txt"))
    									continue;

    								// Getting all all available styles for this template
    								lines = FileUtil.readFileToArrayList(styleFiles[u]);
    								ModelHTMLTemplateStyle style = new ModelHTMLTemplateStyle(newTemplate, lines);

    								newTemplate.addStyle(style);
    							}
    						}
    					}
    				} catch (Exception e) {
    					log.warn(e.getMessage()+ "\n Failed to import template " + templateFiles[i], e);
    				}
    			}
    		}
    	} catch (Exception e) {
    		log.error("Failed to read HTML temlplate files.", e);
    	}

    	log.debug("Done loading HTML templates."); //$NON-NLS-1$
    }


    
    public static void exit() {
        
        if (isApplet())
            DialogMovieManager.destroy();
        else
            System.exit(0);
    }
        
    // 0 = Normal application, 1 = Applet, 2 = Java Web Start
    public static int getAppMode() {

    	int mode = -1;

    	SecurityManager securityManager = System.getSecurityManager();

    	if (securityManager == null) {
    		mode = 0;
		}
    	else {
    		String securityManagerString = securityManager.getClass().getName();

    		if ("com.sun.javaws.security.JavaWebStartSecurity".equals(securityManagerString))
    			mode = 2;
    		else if ("sun.applet.AppletSecurity".equals(securityManagerString))
    			mode = 1;
    	}
    	return mode;
    }

  
    
    
    public static void main(String args[]) {
     
    	boolean sandbox = SysUtil.isRestrictedSandbox();
    	
    	// Uses this to check if the app is running in a sandbox with limited privileges
    	try {
    		/* Disable HTTPClient logging output */
    		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
    				
    	} catch (java.security.AccessControlException s) {    
    		s.printStackTrace();
    		sandbox = true;
    	}
    		
    	if (!sandbox) {
    		// Disables logging for cobra html renderer
    		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.OFF); 

    		File log4jConfigFile = FileUtil.getFile("config/log4j.properties"); //$NON-NLS-1$
		
    		if (log4jConfigFile.isFile()) {
    			PropertyConfigurator.configure(log4jConfigFile.getAbsolutePath());
    		} else {
    			BasicConfigurator.configure();
    		}
    	}
    	else
    		BasicConfigurator.configure();
		
    	log = Logger.getRootLogger();
    	
    	// Places the Log file in the user directory (the program location)
    	RollingFileAppender appndr = (RollingFileAppender) log.getAppender("FileAppender");
    	
    	String logFile = null;
    	
    	try {
			if (SysUtil.isMac() || SysUtil.isWindowsVista() || SysUtil.isWindows7())
				logFile = new File(SysUtil.getConfigDir(), "Log.txt").getAbsolutePath();
    	} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			
			if (logFile == null)
				logFile = new File(SysUtil.getUserDir(), "Log.txt").getAbsolutePath();
		}
    		
		if (appndr != null && appndr.getFile() == null) {
			appndr.setFile(logFile);
			appndr.activateOptions();
		}
				
		/* Writes the date. */
		log.debug("================================================================================"); //$NON-NLS-1$
		log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
		log.debug("MeD's Movie Manager v" + config.sysSettings.getVersion()); //$NON-NLS-1$
		log.debug(SysUtil.getSystemInfo(SysUtil.getLineSeparator())); //$NON-NLS-1$
						
		/* Loads the config */
		if (!sandbox)
			config.loadConfig();
		
		// Calls the plugin startup method 
		MovieManagerStartupHandler startupHandler = MovieManager.getConfig().getStartupHandler();
		
		if (startupHandler != null) {
			startupHandler.startUp();
		}
				
		/* Must be executed before the JFrame (DialogMovieManager) object is created. */
		if (config.getDefaultLookAndFeelDecorated()) {
			DialogMovieManager.setDefaultLookAndFeelDecorated(true);
		}

		if (!sandbox) {
							
			SysUtil.includeJarFilesInClasspath("lib/LookAndFeels");
			SysUtil.includeJarFilesInClasspath("lib/drivers");
		
			/* Must be called before the GUI is created */
			if (SysUtil.isMac()) { 
				SysUtil.includeJarFilesInClasspath("lib/mac");
				LookAndFeelManager.setupOSXLaF(); 
			}
			
		}
		
		movieManager = new MovieManager();
		movieManager.sandbox = sandbox;
						
		/* Installs the Look&Feels */
		LookAndFeelManager.instalLAFs();

		if (!MovieManager.isApplet())
        	LookAndFeelManager.setLookAndFeel();
		
		log.debug("Look & Feels installed.");
    	
//		 Loads the HTML templates
		MovieManager.getIt().loadHTMLTemplates();
		
		
        EventQueue.invokeLater(new Runnable() {
            public final void run() {

            	try {
            		
            		/* Starts the MovieManager. */
            		MovieManager.getDialog().setUp();

            		log.debug("GUI - setup.");
            		
            		/* SetUp the Application Menu */
            		if (SysUtil.isMac()) {
            			LookAndFeelManager.macOSXRegistration();
            		}   
            		
            		// Calls the plugin startup method 
            		MovieManagerLoginHandler loginHandler = MovieManager.getConfig().getLoginHandler();
            		
					if (loginHandler != null) {
            			loginHandler.loginStartUp();
        			}
            		
            		
            		/* Loads the database. */
            		databaseHandler.loadDatabase(true);

            		log.debug("Database loaded.");
            		
            		handleVersionUpdate();
            		            		
            	} catch (Exception e) {
            		log.error("Exception occured while intializing MeD's Movie Manager", e);
            	}
            }
        });
    }
}