/**
 * @(#)MovieManager.java 1.0 21.12.07 (dd.mm.yy)
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
import java.io.FileWriter;
import java.net.URL;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
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
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
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
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MovieManager {

    public static Logger log;
    
    public static NewDatabaseLoadedHandler newDbHandler = new NewDatabaseLoadedHandler();
    public static NewMovieListLoadedHandler newMovieListLoadedHandler = new NewMovieListLoadedHandler();
    
    /**
     * Reference to the only instance of MovieManagerConfig.
     **/
    public static MovieManagerConfig config = new MovieManagerConfig();
    
    /**
     * Reference to the only instance of MovieManager.
     **/
    public static MovieManager movieManager;
    
    /**
     * Reference to the only instance of DialogMovieManager.
     **/
    public static DialogMovieManager dialogMovieManager;
    
   
    
    /**
     * The current database object.
     **/
    private Database database;
      
    
    /* Stores the active additional fields */
    private int [] activeAdditionalInfoFields;
    
    /* While multi-deleting, this is set to true */
    private boolean deleting = false;
    
    private boolean sandbox = false;
    
    public boolean isSandbox() {
    	return sandbox;
    }
    
    public HashMap htmlTemplates = new HashMap();
    
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
                movieManager.loadDatabase();
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
    
    
    /**
     * Returns the current database.
     *
     * @return The current database.
     **/
    synchronized public Database getDatabase() {
        return database;
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
    
 

    public void setMovieListHighlightEntireRow(boolean movieListHighlightEntireRow) {
    	config.setMovieListHighlightEntireRow(movieListHighlightEntireRow);
    }


    public ModelDatabaseSearch getFilterOptions() {

    	ModelDatabaseSearch options = new ModelDatabaseSearch();

    	options.setFilterCategory(config.getFilterCategory());

    	if ("Movie Title".equals(config.getFilterCategory()) && config.getIncludeAkaTitlesInFilter()) //$NON-NLS-1$
    		options.setIncludeAkaTitlesInFilter(true);
    	else
    		options.setIncludeAkaTitlesInFilter(false);


    	//options.setFilterString(((String) dialogMovieManager.getFilter().getSelectedItem()));
    	options.setFilterString(dialogMovieManager.getFilterString());
    	options.setOrderCategory(config.getSortOption());
    	options.setSeen(config.getFilterSeen());
    	options.setListName(config.getCurrentList());

    	if (!options.getListName().equals("Show All")) //$NON-NLS-1$
    		options.setListOption(1);

    	options.setRatingOption(config.getRatingOption());
    	options.setRating(config.getRatingValue());
    	options.setDateOption(config.getDateOption());
    	options.setDate(config.getDateValue());

    	options.setSearchAlias(config.getSearchAlias());

    	if (database.getDatabaseType().equals("MySQL"))
    		options.getFullGeneralInfo = false;

    	return options;
    }


    public int [] getActiveAdditionalInfoFields() {
    	return activeAdditionalInfoFields;
    }

    public void setActiveAdditionalInfoFields(int [] activeAdditionalInfoFields) {
    	this.activeAdditionalInfoFields = activeAdditionalInfoFields;
    }



    public void setDeleting(boolean deleting) {
    	this.deleting = deleting;
    }

    public boolean isDeleting() {
    	return deleting;
    }


    public static boolean isMacAppBundle() {
    	return SysUtil.isMac() & (MovieManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().indexOf(".app/Contents/Resources") > -1);
    }



    public boolean setDatabase(Database _database, boolean cancelRelativePaths) {
    	return setDatabase(_database, null, cancelRelativePaths);
    }

    /**
     * * Sets the current database.
     *
     * @param The current database.
     **/
    public boolean setDatabase(Database _database, ProgressBean progressBean, boolean cancelRelativePaths) {

    	if (_database != null) {

    		if (_database.isMySQL())
    			cancelRelativePaths = true;

    		boolean databaseUpdateAllowed = false;

    		//  If database loading aborted by user
    		if (progressBean != null && progressBean.getCancelled()) {
    			return false;
    		}

    		/* Check if script file needs update (v2.1) */
    		if (_database instanceof DatabaseHSQL) {
    			if (((DatabaseHSQL) _database).isScriptOutOfDate()) {

    				if (!allowDatabaseUpdate(_database.getPath())) {
    					return false;
    				}

    				if (_database.makeDatabaseBackup() != 1) {
    					showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
    					return false;
    				}

    				/* updates the script if audio channel type is INTEGER (HSQLDB)*/
    				if (!((DatabaseHSQL) _database).updateScriptFile()) {
    					showDatabaseUpdateMessage("Script update error"); //$NON-NLS-1$
    					return false;
    				}
    				databaseUpdateAllowed = true;
    			}
    		}

    		//  If database loading aborted by user
    		if (progressBean != null && progressBean.getCancelled()) {
    			return false;
    		}

    		if (!_database.isSetUp()) {
    			if (!_database.setUp()) {
    				DialogDatabase.showDatabaseMessage(dialogMovieManager, _database, null);
    				dialogMovieManager.resetTreeModel();
    				return false;
    			}
    		}

    		//  If database loading aborted by user
    		if (progressBean != null && progressBean.getCancelled()) {
    			return false;
    		}

    		/* If it went ok. */
    		if (_database.isInitialized()) {
    			
    			/* If database is old, it's updated */
    			if (_database.isDatabaseOld()) {

    				if (!databaseUpdateAllowed) {
    					if (!allowDatabaseUpdate(_database.getPath())) {
    						return false;
    					}
    					if (_database.makeDatabaseBackup() != 1) {
    						showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
    						return false;
    					}
    				}

    				if (_database.makeDatabaseUpToDate() == 1)
    					showDatabaseUpdateMessage("Success"); //$NON-NLS-1$
    				else {
    					String message = _database.getErrorMessage();

    					showDatabaseUpdateMessage(message);
    					return false;
    				}
    			}
    			
    			//  If database loading aborted by user
    			if (progressBean != null && progressBean.getCancelled()) {
    				return false;
    			}

    			newDbHandler.newDatabaseLoaded(this);

    			setActiveAdditionalInfoFields(_database.getActiveAdditionalInfoFields());

    			/* Error occured */
    			if (_database.getFatalError()) {

    				if (!_database.getErrorMessage().equals("")) { //$NON-NLS-1$
    					DialogDatabase.showDatabaseMessage(dialogMovieManager, _database, null);
    					dialogMovieManager.resetTreeModel();
    				}
    				return false;
    			}
    		
    			log.info("Loads the movies list"); //$NON-NLS-1$

    			ModelDatabaseSearch options = new ModelDatabaseSearch();

    			if (config.getLoadLastUsedListAtStartup() && !config.getCurrentList().equals("Show All") && _database.listColumnExist(config.getCurrentList())) { //$NON-NLS-1$
    				options.setListOption(1);
    				options.setListName(config.getCurrentList());
    				dialogMovieManager.setListTitle(config.getCurrentList());
    			}
    			else {
    				options.setListOption(0);
    				dialogMovieManager.setListTitle("Show All"); //$NON-NLS-1$
    				config.setCurrentList("Show All"); //$NON-NLS-1$
    			}
    			
    			if (_database.getDatabaseType().equals("MySQL"))
    				options.getFullGeneralInfo = false;

    			options.setFilterString(""); //$NON-NLS-1$
    			options.setOrderCategory("Title"); //$NON-NLS-1$
    			options.setSeen(0);
    			options.setRatingOption(0);
    			options.setDateOption(0);
    			options.setSearchAlias(config.getSearchAlias());

    			//  If database loading aborted by user  
    			if (progressBean != null && progressBean.getCancelled()) {
    				return false;
    			}

    			DefaultListModel moviesList = _database.getMoviesList(options);
    			ArrayList episodesList = _database.getEpisodeList("movieID"); //$NON-NLS-1$
    			DefaultTreeModel treeModel = dialogMovieManager.createTreeModel(moviesList, episodesList);
    			
    			if (cancelRelativePaths && !isApplet()) {

    				if (_database.isMySQL()) {
    					if (config.getUseRelativeCoversPath() == 1)
    						config.setUseRelativeCoversPath(0);

    					if (config.getUseRelativeQueriesPath() == 1)
    						config.setUseRelativeQueriesPath(0);

    					config.setUseRelativeDatabasePath(0);
    				}
    				else {
    					if (!new File(config.getCoversPath(_database)).isDirectory() && new File(config.getCoversFolder(_database)).isDirectory()) {
    						config.setUseRelativeCoversPath(0);
    					}

    					if (!new File(config.getQueriesPath(_database)).isDirectory() && new File(config.getQueriesFolder(_database)).isDirectory()) {
    						config.setUseRelativeQueriesPath(0);
    					}

    					if (_database.getPath().indexOf(SysUtil.getUserDir()) == -1) {
    						config.setUseRelativeDatabasePath(0);
    					}
    				}
    			}
    			
    			//  If database loading aborted by user
    			if (progressBean != null && progressBean.getCancelled()) {
    				return false;
    			}

    			/* Must be set here and not earlier. 
                 If the database is set at the top and the  method returns because of an error after the database is set, 
                 a faulty database will then be stored and used */
    			database = _database;
    			
    			/* Loads the movies list. */
    			dialogMovieManager.setTreeModel(treeModel, moviesList, episodesList);
    			
    			/* Updates the entries Label */
    			dialogMovieManager.setAndShowEntries();
    			
    			dialogMovieManager.loadMenuLists(database);
    			
    			/* Makes database components visible. */
    			dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(true);
    			
    		} else {
    			/* Makes database components invisible. */
    			dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(false);
    			DialogDatabase.showDatabaseMessage(dialogMovieManager, _database, null);
    		}
    	}
    	else
    		database = null;

    	if (_database != null) {

    		Runnable selectMovie = new Runnable() {
    			public void run() {

    				/* Selects the first movie in the list and loads its info. */
    				if (dialogMovieManager.getMoviesList().getModel().getChildCount(dialogMovieManager.getMoviesList().getModel().getRoot()) > 0)
    					dialogMovieManager.getMoviesList().setSelectionRow(0); 

    				MovieManagerCommandSelect.execute();
    			}};
    			GUIUtil.invokeLater(selectMovie);
    	}
    	
    	return _database != null;
    }

    protected boolean allowDatabaseUpdate(String databasePath) {
        DialogQuestion question = new DialogQuestion("Old Database", "<html>This version of MeD's Movie Manager requires your old database:<br> ("+databasePath+") to be updated.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "Perform update now? (A backup will be made)</html>"); //$NON-NLS-1$
        GUIUtil.showAndWait(question, true);
        
        if (question.getAnswer()) {
            return true;
        }
        else {
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.update-necessary"), Localizer.getString("moviemanager.update-necessary-message")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        return false;
    }
    
    protected void showDatabaseUpdateMessage(String result) {
        if (result.equals("Success")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.operation-successfull"), Localizer.getString("moviemanager.operation-successfullMessage")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        else if (result.equals("Database update error")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.database-update-failed"), Localizer.getString("moviemanager.database-update-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        else if (result.equals("Script update error")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.script-update-failed"), Localizer.getString("moviemanager.script-update-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        else if (result.equals("Backup failed")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.backup-failed"), Localizer.getString("moviemanager.backup-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        else {
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.update-failed"), result); //$NON-NLS-1$
            GUIUtil.showAndWait(alert, true);
        }
    }
    
    
    
    public void processDatabaseError(Database db) {
         
    	String error = db.getErrorMessage();
    	
    	log.error("Database error:" + error);
    	
        if (error.equals("Server shutdown in progress")) { //$NON-NLS-1$
            
            dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(false);
            
            DialogAlert alert = new DialogAlert(getDialog(), "Server shutdown in progress", "<html>MySQL server is shutting down."); //$NON-NLS-1$
            GUIUtil.showAndWait(alert, true);
            
            database = null;
        }
        else if (error.equals("Connection reset")) { //$NON-NLS-1$
        	
            dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(false);
            
            if (database != null) {
            	DialogQuestion question = new DialogQuestion(Localizer.getString("moviemanager.connection-reset"), "<html>The connection to the MySQL server has been reset.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$
            	"Reconnect now?</html>"); //$NON-NLS-1$
            	GUIUtil.showAndWait(question, true);

            	if (question.getAnswer()) {
            		setDatabase(new DatabaseMySQL(database.getPath()), false);
            	}
            }
        }
        else if (error.equals("Connection closed")) { //$NON-NLS-1$
        	
            dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(false);
            
            if (database != null) {
            	DialogQuestion question = new DialogQuestion("Connection closed", "<html>The connection to the MySQL server has closed.<br>" +
            	"Reconnect now?</html>");
            	GUIUtil.showAndWait(question, true);

            	if (question.getAnswer()) {
            		setDatabase(new DatabaseMySQL(database.getPath()), false);
            	}
            }
        }
        else if (error.equals("Socket Write Error")) {
        	
        	dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(false);
            
        	if (database != null) {
        		DialogQuestion question = new DialogQuestion("Socket Write Error", "<html>Software caused connection abort due to a Socket Write Error.<br>" +
        		"Reconnect to server now?</html>");
        		GUIUtil.showAndWait(question, true);

        		if (question.getAnswer()) {
        			setDatabase(new DatabaseMySQL(database.getPath()), false);
        		}
        	}
        }                  
        else if (error.equals("UnknownHostException")) {
        	DialogAlert alert = new DialogAlert(dialogMovieManager, "Unknown Host", "<html>The host could not be found.</html>"); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }   
        
        else if (error.equals("Communications link failure")) {
        	
        	dialogMovieManager.getAppMenuBar().setDatabaseComponentsEnable(false);

        	if (database != null) {
        		DialogQuestion question = new DialogQuestion("Communications link failure", "<html>Client failed to connect to MySQL server.<br>" +
        		"Reconnect to server now?</html>");
        		GUIUtil.showAndWait(question, true);

        		if (question.getAnswer()) {
        			setDatabase(new DatabaseMySQL(database.getPath()), false);
        		}
        	}
        }        	
        else if (error.equals("MySQL server is out of space")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.mysql-out-of-space"), Localizer.getString("moviemanager.mysql-out-of-space-message")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        else
        	DialogDatabase.showDatabaseMessage(MovieManager.getDialog(), db, "");
    }
    
   
       
    protected void loadDatabase() {
    	loadDatabase(false);
    }
    
    /**
     * Loads the database with the path read from config.ini file
     **/
    protected void loadDatabase(final boolean perfromBackup) {
        
        if (!config.getLoadDatabaseOnStartup())
        	return;
        
       final ProgressBean worker = new ProgressBeanImpl() {

        	public void run() {

        		try {

        			//Thread.currentThread().setPriority(8);

        			// Gets the database path read from the config file.
        			String databasePath = config.getDatabasePath(false);
        			String type = ""; //$NON-NLS-1$

        			if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
        				log.debug("databasePath:"+ databasePath); //$NON-NLS-1$

        			if (databasePath == null || databasePath.equals("null")) { //$NON-NLS-1$
        				if (listener != null)
        					listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
        				return;
        			}
        			
        			// Checking if there is a login feature enabled
        			MovieManagerLoginHandler loginHandler = MovieManager.getConfig().getLoginHandler();
        			        			
        			if (loginHandler != null) {

        				listener.propertyChange(new PropertyChangeEvent(this, "value", null, "Verifying username and password"));
        				databasePath = loginHandler.handleLogin(databasePath);
        				
        				if (databasePath == null) {
        					log.warn("Login failed. Database loading aborted");
        					
        					if (listener != null)
            					listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
        					return;
        				}

        			}
        			
        			log.debug("Start loading database."); //$NON-NLS-1$

        			if (!databasePath.equals("")) { //$NON-NLS-1$

        				/* If not, no database type specified */
        				if (databasePath.indexOf(">") != -1) { //$NON-NLS-1$
        					type = databasePath.substring(0, databasePath.indexOf(">")); //$NON-NLS-1$
        					databasePath = databasePath.substring(databasePath.indexOf(">")+1, databasePath.length()); //$NON-NLS-1$
        				}
        				else {
        					if (databasePath.endsWith(".mdb") || databasePath.endsWith(".accdb")) //$NON-NLS-1$
        						type = "MSAccess"; //$NON-NLS-1$
        					else if (new File(databasePath+".properties").exists() && new File(databasePath+".script").exists()) //$NON-NLS-1$ //$NON-NLS-2$
        						type = "HSQL"; //$NON-NLS-1$
        				}
        				//config.setDatabasePath(databasePath);
        			}
        			else {
        				log.debug("database path is empty"); //$NON-NLS-1$
        				if (listener != null)
        					listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
        				return;
        			}

        			if (type.equals("MySQL")) { //$NON-NLS-1$
        				if (listener != null)
        					this.listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.getString("moviemanager.progress.connecting-to-database")));
        			}
        			else {
        				if (listener != null)
        					listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.getString("moviemanager.progress.creating-connection")));
        			}

        			if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
        				log.info("Loading " +type+ ":" + databasePath); //$NON-NLS-1$ //$NON-NLS-2$

        			/* Database path relative to program location */
        			if (config.getUseRelativeDatabasePath() == 2)
        				databasePath = SysUtil.getUserDir() + databasePath;

        			Database db = null;

//      			If database loading aborted by user
        			if (getCancelled()) {
        				return;
        			}
        			
        			
        			try {

        				if (type.equals("MSAccess")) { //$NON-NLS-1$
        					if (new File(databasePath).exists()) {
        						log.debug("Loading Access database"); //$NON-NLS-1$
        						db = new DatabaseAccess(databasePath);
        					}
        					else
        						log.debug("Access database does not exist"); //$NON-NLS-1$
        				}
        				else if (type.equals("HSQL")) { //$NON-NLS-1$
        					if (new File(databasePath+".properties").exists() && new File(databasePath+".script").exists()) { //$NON-NLS-1$ //$NON-NLS-2$
        						log.debug("Loading HSQL database"); //$NON-NLS-1$
        						db = new DatabaseHSQL(databasePath);
        					}
        					else
        						log.debug("HSQL database does not exist"); //$NON-NLS-1$
        				}
        				else if (type.equals("MySQL")) { //$NON-NLS-1$
        					log.debug("Loading MySQL database"); //$NON-NLS-1$
        					db = new DatabaseMySQL(databasePath);
        				}

        			} catch (Exception e) {
        				log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
        			}

        			// If database loading aborted by user
        			if (getCancelled()) {
        				return;
        			}

        			if (db != null) {


        				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        				long time = System.currentTimeMillis();

        				if (listener != null)
        					listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.getString("moviemanager.progress.retrieving-movie-list")));

        				// If database loading aborted by user
        				if (getCancelled()) {
        					return;
        				}

        				if (setDatabase(db, this, false))
        					log.debug("Database loaded in:" + (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        				else
        					log.debug("Failed to load database"); //$NON-NLS-1$
        			}

        			if (listener != null)
        				listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));

        			
        			// Perform database backup
        			
        			if (perfromBackup) {

        				if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isSetUp()) {

        					if (config.handleBackupSettings())
        						movieManager.makeDatabaseBackup();

        					log.debug("Backup and new version check finished.");
        				}
        			}
        			
        		} catch (Exception e) {
        			log.error("Exception:" + e.getMessage(), e);
        		}
        	}
        };
        
        SimpleProgressBar progressBar = new SimpleProgressBar(MovieManager.getDialog(), "Loading Database", true, worker);
    	GUIUtil.show(progressBar, true);        
        
        final SwingWorker swingWorker = new SwingWorker() {
			public Object construct() {
				worker.start();
				return worker;
			}
		};
		swingWorker.start();
    }
    
    
    
    
    static void handleVersionUpdate() {
	
    	if (!config.getCheckForProgramUpdates())
    		return;

    	Thread t = new Thread() {

    		public void run() { 
	
    			try {
    				String buf = new HttpUtil(getConfig().getHttpSettings()).readDataToStringBuffer(new URL("http://xmm.sourceforge.net/LatestVersion.txt")).toString();

    				String [] lines = buf.split("\n|\r\n?");

    				if (lines == null || lines.length == 0)
    					return;

    				if (lines[1].length() > 0 && !lines[1].trim().equals(config.sysSettings.getVersion())) {

    					String currentVersion = config.sysSettings.getVersion().replaceAll("\\.", "");
    					String newVersion = lines[1].replaceAll("\\.", "");

    					int currentLength = currentVersion.length();
    					int newLength = newVersion.length();
	
    					if (currentLength > newLength) {
    						for (int i = (currentLength - newLength) ; i < currentLength; i++)
    							newVersion += "0";
    					}
    					else if (currentLength < newLength) {
    						for (int i = (newLength - currentLength) ; i < newLength-1; i++)
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
    		}
    	};
    	t.start();
    }

    
    /*
     * Creating backup of the datdabase.
     */
    void makeDatabaseBackup() {
     	
    	try {
    		String backupFolder = config.getDatabaseBackupDirectory(); 
    		
    		if (backupFolder.equals("")) {
    			String tmp = config.getDatabasePath(true);

    			if (tmp != null && !tmp.equals("")) {
    				File f = new File(tmp);
    				backupFolder = f.getAbsolutePath();
    				f = f.getParentFile();
    				
    				if (f.isDirectory()) {
    					f = new File(f, "backup");
    					f.mkdir();
    					config.setDatabaseBackupDirectory(f.getAbsolutePath());
    					backupFolder = f.getAbsolutePath();
    				}
    			}
    			else
    				return;
    		}
    		
    		if (!new File(backupFolder).isDirectory()) {
    			throw new Exception("Invalid backup directory:" + backupFolder);
    		}
    		    		
    		int sizeLimit = Integer.parseInt(config.getDatabaseBackupDeleteOldest());
    		
    		if (sizeLimit > 0) {
    		
    			sizeLimit = sizeLimit * 1000 * 1000;
    			
    			// Getting the size of the database backup files
    			int sizeOfBackupDir = 0;
    			File [] f3 = new File(backupFolder).listFiles();
    			
    			for (int i = 0; i < f3.length; i++) {
    				if (f3[i].isDirectory()) {
    					File [] f4 = f3[i].listFiles();

    					for (int u = 0; u < f4.length; u++) {
    						if (f4[u].isDirectory()) {
    							File [] f5 = f4[u].listFiles();

    							for (int y = 0; y < f5.length; y++) {
    								if (f5[y].isFile()) {
    									sizeOfBackupDir += f5[y].length();
    								}
    							}
    						}
    					}
    				}
    			}
    				
    			// Delete oldest backup(s)
    			if (sizeOfBackupDir > sizeLimit) {
    				
    				String[] files = new File(backupFolder).list();
    				Arrays.sort(files);
    				
    				for (int u = 0; u < files.length; u++) {
    					
    					File f1 = new File(backupFolder + "/" + files[u]);
    					    					
    					if (!f1.isDirectory() || !Pattern.matches("\\d+\\.\\d+\\.\\d+", files[u]))
    						continue;
    					
    					String [] files2 = f1.list();
        				Arrays.sort(files2);
    					int i;
    					
    					for (i = 0; i < files2.length && sizeOfBackupDir > sizeLimit; i++) {
    						File f2 = new File(backupFolder + "/" + files[u] + "/" + files2[i]);
    						
    						long s = FileUtil.getDirectorySize(f2, null);
    						
    						FileUtil.deleteDirectoryStructure(f2);
    						
    						if (f2.getParentFile().list().length == 0)
    							f2.getParentFile().delete();
    						
    						sizeOfBackupDir -= s;
    					}
    					
    					if (i == files2.length-1) {
    						f1.delete();
    					}
    				}
    			}
    		}
    		
//    		creating the new backup
    		String dbPath = config.getDatabasePath(true);
	
    		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    		SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm.ss");
    		
    		Calendar c = Calendar.getInstance();

    		String date = dateFormat.format(c.getTime());
    		String time = timeFormat.format(c.getTime());
    		
    		backupFolder += "/" + date + "/" + time;

    		File dbBackup = new File(backupFolder);
    		dbBackup.mkdirs();

    		if (database.isHSQL()) {
    			File tmp = new File(dbPath + ".script");

    			if (!tmp.isFile())
    				throw new Exception("HSQLDB script file does not exist.");
    			else
    				FileUtil.copyToDir(tmp, dbBackup);

    			tmp = new File(dbPath + ".properties");
    			
    			if (!tmp.isFile())
    				throw new Exception("HSQLDB properties file does not exist.");
    			else
    				FileUtil.copyToDir(tmp, dbBackup);
    		}
    		else if (database.isMSAccess()) {
    			File tmp;

    			if ((tmp = new File(dbPath)).isFile()) {
    				FileUtil.copyToDir(tmp, dbBackup);
    			}
    			else
    				throw new Exception("MS Access database file does not exist:" + dbPath);
    		}
    	} catch (Exception e) {
    		log.warn("Error occured in backup procedure:" + e.getMessage());
    	}
    }

    public HashMap getHTMLTemplates() {
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

    						ArrayList lines = FileUtil.readFileToArrayList(template);
    						
    						if (lines == null) {
    							log.error("Failed to read file "  + template);
    							throw new Exception();
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

    
    public static boolean isRestrictedSandbox() {
    	
    	SecurityManager securityManager = System.getSecurityManager();
    	
    	if (securityManager == null) {
    		return false;
    	}
    	
    	try {
    		securityManager.checkPropertiesAccess();
    	} catch (Exception e) {
    		log.debug("Exception:" + e.getMessage());
    		return true;
    	} 
    	
    	return false;
    }
    
    public static String getUserHome() {
    	String userHome = (String) AccessController.doPrivileged(
    			new PrivilegedAction() {
    				public Object run() {
    					return System.getProperty("user.home");
    				}
    			}
    	);
    	return userHome;
    }
    
    public static void main(String args[]) {
     	    	
    	boolean sandbox = isRestrictedSandbox();
    	
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
		
		/* Writes the date. */
		log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
		log.debug("MeD's Movie Manager v" + config.sysSettings.getVersion()); //$NON-NLS-1$
				
		
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
			/* Must be called before the GUI is created */
			if (SysUtil.isMac()) { 
				SysUtil.includeJarFilesInClasspath(System.getProperty("user.dir") + "/lib/LookAndFeels");
				LookAndFeelManager.setupOSXLaF(); 
			}
			else /* Includes the avallable jar files*/
				SysUtil.includeJarFilesInClasspath("lib/LookAndFeels");

			SysUtil.includeJarFilesInClasspath("lib/drivers");
		}
		
		movieManager = new MovieManager();
		movieManager.sandbox = sandbox;
				
		log.debug("MovieManager object initialized.");
				
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
            		MovieManager.getIt().loadDatabase(true);

            		log.debug("Database loaded.");
            		
            		handleVersionUpdate();
            		            		
            	} catch (Exception e) {
            		log.error("Exception occured while intializing MeD's Movie Manager", e);
            	}
            }
        });
    }
}