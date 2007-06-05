package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandExit;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeNode;
import net.sf.xmm.moviemanager.util.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dotuseful.ui.tree.AutomatedTreeModel;

import spin.Spin;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultTreeModel;

public class MovieManager {

    public static Logger log;
    
    public static NewDatabaseLoadedHandler newDbHandler = new NewDatabaseLoadedHandler();
    
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
     * The current version of the program.
     **/
    private static String _version = "2.5.3.3"; //$NON-NLS-1$
    
    /**
     * The current database object.
     **/
    private Database database;
    
  
    /* Stores the active additional fields */
    private int [] activeAdditionalInfoFields;
    
    /* While multi-deleting, this is set to true */
    private boolean deleting = false;
    
    
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
        
        movieManager = this;
        dialogMovieManager = new DialogMovieManager(applet);
        
        EventQueue.invokeLater(new Runnable() {
            public final void run() {
                
                /* Disable HTTPClient logging output */
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
               
                System.setProperty("apache.commons.logging.simplelog.defaultlog", "OFF");
               
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
                
                System.setProperty("log4j.logger.org.apache.commons.httpclient", "OFF");
                
                
                URL configFile = FileUtil.getFileURL("log4j.properties"); //$NON-NLS-1$
                
                PropertyConfigurator.configure(configFile);
                
                log = Logger.getRootLogger();
                
                /* Writes the date. */
                log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
                
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
     * Returns the version.
     *
     * @return Program Version.
     **/
    public static String getVersion() {
        return _version;
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
        
        options.setFilterString(dialogMovieManager.getFilter().getText());
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
     return FileUtil.isMac() & (MovieManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().indexOf(".app/Contents/Resources") > -1);
 }
 
 public static String getDefaultPlatformBrowser() {
     String browser = "";
     
     if (FileUtil.isWindows())
         browser = "Default";
     else if (FileUtil.isMac())
         browser = "Safari";
     else
         browser = "Firefox";
     
     return browser;
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
            
            boolean databaseUpdateAllowed = false;
            
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
             
            if (progressBean != null && progressBean.getCancelled()) {
            	 return false;
            }
            
            if (!_database.isSetUp()) {
                if (!_database.setUp()) {
                    DialogDatabase.showDatabaseMessage(dialogMovieManager, _database, null);
                    dialogMovieManager.getMoviesList().setModel(null);
                    return false;
                }
            }
            
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
                
                if (progressBean != null && progressBean.getCancelled()) {
                	return false;
                }
                 
                newDbHandler.newDatabaseLoaded(this);
                
                setActiveAdditionalInfoFields(_database.getActiveAdditionalInfoFields());
                
                /* Error occured */
                if (_database.getFatalError()) {
                    
                    if (!_database.getErrorMessage().equals("")) { //$NON-NLS-1$
                        DialogDatabase.showDatabaseMessage(dialogMovieManager, _database, null);
                        dialogMovieManager.getMoviesList().setModel(null);
                    }
                    
                    return false;
                }
                
                /* Resets the additonal info fields names stored in ModelAdditionalInfo */
                ModelAdditionalInfo.setExtraInfoFieldNamesChanged();
                
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
                
                if (progressBean != null && progressBean.getCancelled()) {
                	return false;
                }
                
                DefaultListModel moviesList = _database.getMoviesList(options);
                
                ArrayList episodesList = _database.getEpisodeList("movieID"); //$NON-NLS-1$
                 
                DefaultTreeModel treeModel = dialogMovieManager.createTreeModel(moviesList, episodesList);
                
                dialogMovieManager.getMoviesList().setRootVisible(false);
                //getMoviesList().setLargeModel(true);
                
                /* Makes database components visible. */
                dialogMovieManager.setDatabaseComponentsEnable(true);
                
                if (cancelRelativePaths && !isApplet()) {
                    
                    if (!new File(config.getCoversPath(_database)).isDirectory() && new File(config.getCoversFolder(_database)).isDirectory()) {
                        config.setUseRelativeCoversPath(0);
                    }
                    
                    if (!new File(config.getQueriesPath(_database)).isDirectory() && new File(config.getQueriesFolder(_database)).isDirectory()) {
                        config.setUseRelativeQueriesPath(0);
                    }
                    
                    if (_database.getPath().indexOf(FileUtil.getUserDir()) == -1) {
                        config.setUseRelativeDatabasePath(0);
                    }
                }
                
                
                /* Must be set here and not earlier. 
                 If the database is set at the top and the  method returns because of an error after the database is set, 
                 a faulty database will then be stored and used */
                database = _database;
                 
                /* Loads the movies list. */
                dialogMovieManager.getMoviesList().setModel(treeModel);
                
                /* Updates the entries Label */
                dialogMovieManager.setAndShowEntries();
                dialogMovieManager.loadMenuLists(database);
                
            } else {
                /* Makes database components invisible. */
                dialogMovieManager.setDatabaseComponentsEnable(false);
                DialogDatabase.showDatabaseMessage(dialogMovieManager, _database, null);
            }
        }
                
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
    
    
    
    public void processDatabaseError() {
        
    	String error = database.getErrorMessage();
        
    	System.err.println("processDatabaseError:" + error);
    	
        if (error.equals("Connection reset")) { //$NON-NLS-1$
            
            dialogMovieManager.setDatabaseComponentsEnable(false);
            
            DialogQuestion question = new DialogQuestion(Localizer.getString("moviemanager.connection-reset"), "<html>The connection to the MySQL server has been reset.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$
            "Reconnect now?</html>"); //$NON-NLS-1$
            GUIUtil.showAndWait(question, true);
            
            if (question.getAnswer()) {
                setDatabase(new DatabaseMySQL(database.getPath()), false);
            }
        }
        else if (error.equals("Connection closed")) { //$NON-NLS-1$
            
            dialogMovieManager.setDatabaseComponentsEnable(false);
            
            DialogQuestion question = new DialogQuestion("Connection closed", "<html>The connection to the MySQL server has closed.<br>" +
            "Reconnect now?</html>");
            GUIUtil.showAndWait(question, true);
            
            if (question.getAnswer()) {
                setDatabase(new DatabaseMySQL(database.getPath()), false);
            }
        }
        else if (error.equals("Socket Write Error")) {
        	
        	dialogMovieManager.setDatabaseComponentsEnable(false);
            
            DialogQuestion question = new DialogQuestion("Socket Write Error", "<html>Software caused connection abort due to a Socket Write Error.<br>" +
            "Reconnect to server now?</html>");
            GUIUtil.showAndWait(question, true);
            
            if (question.getAnswer()) {
                setDatabase(new DatabaseMySQL(database.getPath()), false);
            }
        }
        if (error.equals("MySQL server is out of space")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(dialogMovieManager, Localizer.getString("moviemanager.mysql-out-of-space"), Localizer.getString("moviemanager.mysql-out-of-space-message")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
    }
    
    /**
     * Returns a resource in a byte[] or null if not found.
     *
     * @param name A resource name.
     **/
    public byte[] getResourceAsByteArray(String name) {
        
        try {
            InputStream inputStream;
            
            if (new File(name).exists()) {
                inputStream = new FileInputStream(new File(name));
            }
            else {
                inputStream = getClass().getResourceAsStream(name);
            }
            
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bufferedInputStream.available());
            
            int buffer;
            while ((buffer = bufferedInputStream.read()) != -1)
                byteStream.write(buffer);
            
            bufferedInputStream.close();
            
            return byteStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
        }
        return null;
    }
    
    /**
     * Returns a resource as a Stream or null if not found.
     *
     * @param name A resource name.
     **/
    public InputStream getResourceAsStream(String name) {
        
        try {
            return getClass().getResourceAsStream(name);
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
        }
        return null;
    }
    
  
    
    
    /**
     * Loads the database with the the path read from config.ini file
     **/
    protected void loadDatabase() {
        
        if (!config.getLoadDatabaseOnStartup())
            return;
        
        
        ProgressBean worker = new ProgressBeanImpl() {
        
            public void start() {
                
            	//Thread.currentThread().setPriority(8);
            		
                String databasePath = config.getDatabasePath(true);
                String type = ""; //$NON-NLS-1$
                
                if (databasePath == null || databasePath.equals("null")) { //$NON-NLS-1$
                	listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
                    return;
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
                }
                else {
                    log.debug("database path is empty"); //$NON-NLS-1$
                    listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
                    return;
                }
                
                if (type.equals("MySQL")) //$NON-NLS-1$
                	this.listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.getString("moviemanager.progress.connecting-to-database")));
                else
                	listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.getString("moviemanager.progress.creating-connection")));
                
                
                log.info("Loading " +type+ ":" + databasePath); //$NON-NLS-1$ //$NON-NLS-2$
                
                /* Database path relative to program location */
                if (config.getUseRelativeDatabasePath() == 2)
                    databasePath = FileUtil.getUserDir() + databasePath;
                
                Database db = null;
                
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
                
                if (getCancelled()) {
                		return;
                }
                
                if (db != null) {
                    
                    try {
                        Runnable setModel = new Runnable() {
                            public void run() {
                                ExtendedTreeNode root = new ExtendedTreeNode(new ModelMovie(-1, null, null, null, Localizer.getString("moviemanager.progress.loading-database"), null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null)); //$NON-NLS-1$
                                
                                DefaultTreeModel model = new AutomatedTreeModel(root, false);
                                
                                dialogMovieManager.getMoviesList().setModel(model);
                                dialogMovieManager.getMoviesList().setRootVisible(true);
                            }
                        };
                        GUIUtil.invokeLater(setModel);
                        
                    } catch (Exception e) {
                        log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
                    }
                    
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    
                    long time = System.currentTimeMillis();
                    
                    //DialogDatabase.updateProgress(progressBar, Localizer.getString("moviemanager.progress.retrieving-movie-list")); //$NON-NLS-1$
                    
                    listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.getString("moviemanager.progress.retrieving-movie-list")));
                     
                    if (setDatabase(db, false))
                        log.debug("Database loaded in:" + (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
                    else
                        log.debug("Failed to load database"); //$NON-NLS-1$
                }
                
                listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
                //return;
            }
        };
        worker = (ProgressBean) Spin.off(worker);
        
        SimpleProgressBar progressBar = new SimpleProgressBar(MovieManager.getDialog(), "Loading Database", true, worker);
        GUIUtil.show(progressBar, true);        
        
        //worker.addPropertyChangeListener(progressBar);
        
        worker.start();
    }
    
    
    /* Adds all the files ending in .jar to the classpath */
    public static void includeJarFilesInClasspath(String path) {
        
        URL url = FileUtil.getFileURL(path);
        
        if (url.toExternalForm().startsWith("http://")) //$NON-NLS-1$
            return;
        
        File dir = new File(url.getPath());
        
        try {
            File [] jarList = dir.listFiles();
            
            if (jarList != null) {
                
                String absolutePath = ""; //$NON-NLS-1$
                for (int i = 0; i < jarList.length; i++) {
                    
                    absolutePath = jarList[i].getAbsolutePath();
                     
                    if (absolutePath.endsWith(".jar")) { //$NON-NLS-1$
                    	net.sf.xmm.moviemanager.util.ClassPathHacker.addFile(absolutePath);
                        log.debug(absolutePath+ " added to classpath"); //$NON-NLS-1$
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
    }
    
    public static void exit() {
        
        if (isApplet())
            DialogMovieManager.destroy();
        else
            System.exit(0);
    }
    
    public static void main(String args[]) {
        
        EventQueue.invokeLater(new Runnable() {
            public final void run() {
                
                /* Disable HTTPClient logging output */
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
                
                
                File log4jConfigFile = FileUtil.getFile("log4j.properties"); //$NON-NLS-1$
                
                if (log4jConfigFile.isFile()) {
                    PropertyConfigurator.configure(log4jConfigFile.getAbsolutePath());
                } else {
                    BasicConfigurator.configure();
                }
                
                log = Logger.getRootLogger();
                
                /* Writes the date. */
                log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
                log.debug("MeD's Movie Mnager v" + _version); //$NON-NLS-1$
                
                /* Loads the config */
                config.loadConfig();
                        
                /* Must be executed before the JFrame (DialogMovieManager) object is created. */
                if (config.getDefaultLookAndFeelDecorated()) {
                	DialogMovieManager.setDefaultLookAndFeelDecorated(true);
                }
                
                /* Must be called before the GUI is created */
                if (FileUtil.isMac()) { 
                	includeJarFilesInClasspath(System.getProperty("user.dir") + "/LookAndFeels");
                    LookAndFeelManager.setupOSXLaF(); 
                }
                
				/* Includes the avallable jar files*/
		        includeJarFilesInClasspath("LookAndFeels");
                
                movieManager = new MovieManager();
                
                /* Installs the Look&Feels */
                LookAndFeelManager.instalLAFs();
                
                /* Starts the MovieManager. */
                MovieManager.getDialog().setUp();
                
                /* SetUp the Application Menu */
                if (FileUtil.isMac()) {
                    LookAndFeelManager.macOSXRegistration();
                }   
                
                
                /* Loads the database. */
                MovieManager.getIt().loadDatabase();
            }
        });
    }
}
