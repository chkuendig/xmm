/**
 * @(#)MovieManager.java 1.0 10.10.06 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import org.apache.log4j.*;
import org.dotuseful.ui.tree.*;
import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.extentions.*;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.*;


public class MovieManager extends JFrame implements ComponentListener {

    public static Logger log;

    /**
     * Reference to the only instance of MovieManagerConfig.
     **/
    public static MovieManagerConfig config;

    public static JApplet applet = null;
    
    public NewDatabaseLoadedHandler newDbHandler = new NewDatabaseLoadedHandler();

    /**
     * Reference to the only instance of MovieManager.
     **/
    public static MovieManager _movieManager;

    /**
     * The current version of the program.
     **/
    private String _version = "2.41"; //$NON-NLS-1$

    /**
     * The current database object.
     **/
    private Database _database;

    /*Number of entries in the list*/
    private int entries;

    private int fontSize = 12;

    /* Stores the active additional fields */
    private int [] activeAdditionalInfoFields;


    /* While multi-deleting, this is set to true */
    private boolean deleting = false;

    private int movieListWidth = 0;

    private JLabel showEntries;

    /**
     * Constructor.
     **/
    private MovieManager() {

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    MovieManagerCommandExit.execute();
		}
	    });
    }

    /* Applet feature is not finished */
    MovieManager(JApplet applet) {

	_movieManager = this;
	MovieManager.applet = applet;

	System.err.println("Codebase:" + MovieManager.applet.getCodeBase()); //$NON-NLS-1$
	
	EventQueue.invokeLater(new Runnable() {
		public final void run() {
		    
		     /* Disable HTTPClient logging output */
		    //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		    
		    URL configFile = getFile("log4j.properties"); //$NON-NLS-1$

		    System.err.println("loading:" + configFile.toString()); //$NON-NLS-1$
		    
		    PropertyConfigurator.configure(configFile);
		    
		    log = Logger.getRootLogger();
		    
		    /* Writes the date. */
		    log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
		    
		    config = new MovieManagerConfig();

		     /* Loads the config */
		    config.loadConfig();

		    /* Must be executed before the JFrame (MovieManager) object is created. */
		    if (config.getDefaultLookAndFeelDecorated()) {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		    }

		  
// 		    /* Installs the Look&Feels*/
// 		    LookAndFeelManager.instalLAFs();

		    /* Starts the MovieManager. */
		    MovieManager.getIt().setUp();
		    
		    /* Loads the database. */
		    MovieManager.getIt().loadDatabase();
		}
	    });


// 	//this.applet = applet;
//     	_movieManager.applet = applet;

// 	_movieManager.loadConfig();

// 	/* Installs the Look&Feels*/
// 	//instalLAFs();

// 	/* Starts the MovieManager. */
// 	_movieManager.setUp();

// 	/* Loads the database. */
// 	_movieManager.loadDatabase();

    }

    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of MovieManager.
     **/
    public static MovieManager getIt() {
	return _movieManager;
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
    public String getVersion() {
	return _version;
    }

    /**
     * Returns the current database.
     *
     * @return The current database.
     **/
    synchronized public Database getDatabase() {
	return _database;
    }







    /**
     * Sets the current database.
     *
     * @param The current database.
     **/
    public boolean setDatabase(Database database, boolean cancelRelativePaths) {
        
	if (database != null) {

	    boolean databaseUpdateAllowed = false;

	    if (Thread.currentThread().isInterrupted()) {
		return false;
	    }

	    /* Check if script file needs update (v2.1) */
	    if (database instanceof DatabaseHSQL) {
		if (((DatabaseHSQL) database).isScriptOutOfDate()) {

		    if (!allowDatabaseUpdate(database.getPath())) {
			return false;
		    }

		    if (database.makeDatabaseBackup() != 1) {
			showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
			return false;
		    }

		    /* updates the script if audio channel type is INTEGER (HSQLDB)*/
		    if (!((DatabaseHSQL) database).updateScriptFile()) {
			showDatabaseUpdateMessage("Script update error"); //$NON-NLS-1$
			return false;
		    }
		    databaseUpdateAllowed = true;
		}
	    }

	    if (Thread.currentThread().isInterrupted()) {
		return false;
	    }

	    if (!database.isSetUp()) {
		if (!database.setUp()) {
		    DialogDatabase.showDatabaseMessage(this, database, null);
		    getMoviesList().setModel(null);
		    return false;
		}
	    }

	    if (Thread.currentThread().isInterrupted()) {
		return false;
	    }

	    /* If it went ok. */
	    if (database.isInitialized()) {

		/* If database is old, it's updated */
		if (database.isDatabaseOld()) {

		    if (!databaseUpdateAllowed) {
			if (!allowDatabaseUpdate(database.getPath())) {
			    return false;
			}
			if (database.makeDatabaseBackup() != 1) {
			    showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
			    return false;
			}
		    }

		    if (database.makeDatabaseUpToDate() == 1)
			showDatabaseUpdateMessage("Success"); //$NON-NLS-1$
		    else {
			String message = database.getErrorMessage();

			showDatabaseUpdateMessage(message);
			return false;
		    }
		}

		if (Thread.currentThread().isInterrupted()) {
		    return false;
		}

		//long time = System.currentTimeMillis();

		/* Resetting the covers and queries paths */
		//config.resetCoverAndQueries();
		
		newDbHandler.newDatabaseLoaded(this);

		setActiveAdditionalInfoFields(database.getActiveAdditionalInfoFields());

		/* Error occured */
		if (database.getFatalError()) {

		    if (!database.getErrorMessage().equals("")) { //$NON-NLS-1$
			DialogDatabase.showDatabaseMessage(this, database, null);
			getMoviesList().setModel(null);
		    }

		    return false;
		}
		
		/* Resets the additonal info fields names stored in ModelAdditionalInfo */
		ModelAdditionalInfo.setHasOldExtraInfoFieldNames(true);

		log.info("Loads the movies list"); //$NON-NLS-1$

		ModelDatabaseSearch options = new ModelDatabaseSearch();

		if (config.getLoadLastUsedListAtStartup() && !config.getCurrentList().equals("Show All") && database.listColumnExist(config.getCurrentList())) { //$NON-NLS-1$
		    options.setListOption(1);
		    options.setListName(config.getCurrentList());
		    setListTitle(config.getCurrentList());
		}
		else {
		    options.setListOption(0);
		    setListTitle("Show All"); //$NON-NLS-1$
		    config.setCurrentList("Show All"); //$NON-NLS-1$
		}

		options.setFilterString(""); //$NON-NLS-1$
		options.setOrderCategory("Title"); //$NON-NLS-1$
		options.setSeen(0);
		options.setRatingOption(0);
		options.setDateOption(0);

		if (Thread.currentThread().isInterrupted()) {
		    return false;
		}

		DefaultListModel moviesList = database.getMoviesList(options);
		ArrayList episodesList = database.getEpisodeList("movieID"); //$NON-NLS-1$
		DefaultTreeModel treeModel = createTreeModel(moviesList, episodesList);
		
		getMoviesList().setRootVisible(false);
		//getMoviesList().setLargeModel(true);
		
		/* Makes database components visible. */
		setDatabaseComponentsEnable(true);
		
		if (cancelRelativePaths && !isApplet()) {
		    
		    if (!new File(config.getCoversPath(database)).isDirectory() && new File(config.getCoversFolder(database)).isDirectory()) {
			config.setUseRelativeCoversPath(0);
		    }
		    
		    if (!new File(config.getQueriesPath(database)).isDirectory() && new File(config.getQueriesFolder(database)).isDirectory()) {
			config.setUseRelativeQueriesPath(0);
		    }
		    
		    if (database.getPath().indexOf(getUserDir()) == -1) {
			config.setUseRelativeDatabasePath(0);
		    }
		}
		
		
		/* Must be set here and not earlier. 
		   If the database is set at the top and the  method returns because of an error after the database is set, 
		   a faulty database will then be stored and used */
		_database = database;
		
		/* Loads the movies list. */
		getMoviesList().setModel(treeModel);
		
		/* Updates the entries Label */
		setAndShowEntries();
		loadMenuLists(database);
		
	    } else {
		/* Makes database components invisible. */
		setDatabaseComponentsEnable(false);
		DialogDatabase.showDatabaseMessage(this, database, null);
	    }
	}
	else
	    _database = null;
	
	
	if (_database != null) {
	    
	    /* Selects the first movie in the list and loads its info. */
	    if (getMoviesList().getModel().getChildCount(getMoviesList().getModel().getRoot()) > 0) {
		
		Runnable showProgress = new Runnable() {
			public void run() {
			    getMoviesList().setSelectionRow(0);
			    MovieManagerCommandSelect.execute();
			}};
		SwingUtilities.invokeLater(showProgress);
	    }
	}
	
	return _database != null;
    }

    public DefaultTreeModel createTreeModel(DefaultListModel movieList, ArrayList episodes) {

	Object[] movies = movieList.toArray();

	ExtendedTreeNode root = new ExtendedTreeNode(new ModelMovie(-1, null, null, null, "Loading Database", null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null)); //$NON-NLS-1$

	DefaultTreeModel model = new AutomatedTreeModel(root, false);

	ExtendedTreeNode temp, temp2;
	int tempKey = 0;

	for (int i = 0; i < movies.length; i++) {

	    temp = new ExtendedTreeNode((ModelEntry) movies[i]);
	    tempKey = ((ModelEntry) movies[i]).getKey();

	    /* Adding episodes */
	    for (int u = 0; u < episodes.size(); u++) {

		if (tempKey == ((ModelEpisode) episodes.get(u)).getMovieKey()) {

		    temp2 = new ExtendedTreeNode((ModelEntry) episodes.get(u));
		    temp.add(temp2);

		    episodes.remove(u);
		    u --;
		}
	    }

	    root.add(temp);
	}

	return model;
    }

    protected boolean allowDatabaseUpdate(String databasePath) {
	DialogQuestion question = new DialogQuestion("Old Database", "<html>This version of MeD's Movie Manager requires your old database:<br> ("+databasePath+") to be updated.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						     "Perform update now? (A backup will be made)</html>"); //$NON-NLS-1$
	ShowGUI.showAndWait(question, true);
	
	if (question.getAnswer()) {
	    return true;
	}
	else {
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.update-necessary"), Localizer.getString("moviemanager.update-necessary-message")); //$NON-NLS-1$ //$NON-NLS-2$
	    ShowGUI.showAndWait(alert, true);
	}
	return false;
    }

    protected void showDatabaseUpdateMessage(String result) {
	if (result.equals("Success")) { //$NON-NLS-1$
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.operation-successfull"), Localizer.getString("moviemanager.operation-successfullMessage")); //$NON-NLS-1$ //$NON-NLS-2$
	    ShowGUI.showAndWait(alert, true);
	}
	else if (result.equals("Database update error")) { //$NON-NLS-1$
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.database-update-failed"), Localizer.getString("moviemanager.database-update-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
	    ShowGUI.showAndWait(alert, true);
	}
	else if (result.equals("Script update error")) { //$NON-NLS-1$
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.script-update-failed"), Localizer.getString("moviemanager.script-update-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
	    ShowGUI.showAndWait(alert, true);
	}
	else if (result.equals("Backup failed")) { //$NON-NLS-1$
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.backup-failed"), Localizer.getString("moviemanager.backup-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
	    ShowGUI.showAndWait(alert, true);
	}
	else {
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.update-failed"), result); //$NON-NLS-1$
	    ShowGUI.showAndWait(alert, true);
	}
    }

    public void processDatabaseError() {

	String error = _database.getErrorMessage();

	if (error.equals("Connection reset")) { //$NON-NLS-1$

	    setDatabaseComponentsEnable(false);

	    DialogQuestion question = new DialogQuestion(Localizer.getString("moviemanager.connection-reset"), "<html>The connection to the MySQL server has been reset.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$
							 "Reconnect now?</html>"); //$NON-NLS-1$
	   ShowGUI.showAndWait(question, true);

	    if (question.getAnswer()) {
		setDatabase(new DatabaseMySQL(_database.getPath()), false);
	    }
	}

	if (error.equals("MySQL server is out of space")) { //$NON-NLS-1$
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("moviemanager.mysql-out-of-space"), Localizer.getString("moviemanager.mysql-out-of-space-message")); //$NON-NLS-1$ //$NON-NLS-2$
	    ShowGUI.showAndWait(alert, true);
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
		inputStream = inputStream = getClass().getResourceAsStream(name);
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
     * Setup the main MovieManager object.
     **/
    protected void setUp() {

	try {
	    if (!isApplet()) {
    		
            
         /* Gets the working dir... */
            String directory = MovieManager.getUserDir();
            File laf = new File(directory + "LookAndFeels" + File.separator + "lookAndFeels.ini");
           
	    if (!laf.exists() && !isMacAppBundle()) {
		new File(directory + "LookAndFeels").mkdirs();
		
		String text = "Here you can add new Look and Feels." + getLineSeparator()+ //$NON-NLS-1$
		    "Make sure the 'look and Feel' jar file is placed in the 'LookAndFeels' directory" + getLineSeparator()+ //$NON-NLS-1$
		    "and that the correct classname is given below." + getLineSeparator()+ //$NON-NLS-1$
		    "Both the name and classname must be enclosed in quotes." + getLineSeparator()+ //$NON-NLS-1$
			"The names may be set to whatever fit your needs." + getLineSeparator()+ //$NON-NLS-1$
		    "Example:" + getLineSeparator()+ getLineSeparator()+ //$NON-NLS-1$
		    "\"Metal look and feel\"       \"javax.swing.plaf.metal.MetalLookAndFeel\"" + getLineSeparator()+ //$NON-NLS-1$
		    "\"Windows look and feel\"     \"com.sun.java.swing.plaf.windows.WindowsLookAndFeel\"" + getLineSeparator()+ //$NON-NLS-1$
		    getLineSeparator()+ "The metal and windows look and feels are preinstalled." + getLineSeparator()+ //$NON-NLS-1$
		    "Define the look and feels below:" + getLineSeparator()+ //$NON-NLS-1$
		    "#" + getLineSeparator(); //$NON-NLS-1$
		
		
		/* Creating the texfile */
		PrintWriter pwriter = new PrintWriter(new FileWriter(laf), true); //$NON-NLS-1$ //$NON-NLS-2$
		
		/* Writes the lookAndFeels.ini textfile. */
		for (int i=0; i < text.length(); i++) {
		    pwriter.write(text.charAt(i));
		}
		pwriter.close();
	    }
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
	}
	/* Starts other inits. */
	log.debug("Start setting up the MovieManager."); //$NON-NLS-1$

	LookAndFeelManager.setLookAndFeel();

	Toolkit.getDefaultToolkit().setDynamicLayout(true);

	if (!MovieManager.isApplet())
	    System.setProperty("sun.awt.noerasebackground", "true"); //$NON-NLS-1$ //$NON-NLS-2$

	_movieManager.setTitle(" MeD's Movie Manager v" + _version); //$NON-NLS-1$

	_movieManager.setIconImage(_movieManager.getImage("/images/film.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH)); //$NON-NLS-1$

	_movieManager.setJMenuBar(createMenuBar());
	_movieManager.getContentPane().add(createWorkingArea(),BorderLayout.CENTER);

	_movieManager.setResizable(true);

	/* Hides database related components. */
	setDatabaseComponentsEnable(false);

	updateJTreeIcons();

	addComponentListener(this);

	/* All done, pack. */
	pack();
	updateToolButtonBorder();

	_movieManager.setSize(MovieManager.getConfig().mainSize);

	if (config.getMainMaximized())
	    _movieManager.setExtendedState(JFrame.MAXIMIZED_BOTH);


	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Point location = config.getScreenLocation();

	if (location != null && location.getX() < screenSize.getWidth() && location.getY() < screenSize.getHeight())
	    _movieManager.setLocation(location);
	else {
	    _movieManager.setLocation((int)(screenSize.getWidth() - getSize().getWidth())/2,
				      (int)(screenSize.getHeight() - getSize().getHeight())/2 - 12);
	}


	/* Setting Additional Info / Notes slider position */
	if (config.additionalInfoNotesSliderPosition == -1) {
	    getAdditionalInfoNotesSplitPane().setDividerLocation(0.5);
	    getAdditionalInfoNotesSplitPane().setLastDividerLocation(getAdditionalInfoNotesSplitPane().getDividerLocation());
	}
	else {
	    getAdditionalInfoNotesSplitPane().setDividerLocation(config.additionalInfoNotesSliderPosition);

        if (config.additionalInfoNotesLastSliderPosition != -1)
            getAdditionalInfoNotesSplitPane().setLastDividerLocation(config.additionalInfoNotesLastSliderPosition);
	}


	/* Setting Movie Info slider position */
	if (config.movieInfoSliderPosition == -1) {
	    getMovieInfoSplitPane().setDividerLocation(0.6);
	    getMovieInfoSplitPane().setLastDividerLocation(getMovieInfoSplitPane().getDividerLocation());
	 }
	else {
	    getMovieInfoSplitPane().setDividerLocation(config.movieInfoSliderPosition);

        if (config.movieInfoLastSliderPosition != -1)
            getMovieInfoSplitPane().setLastDividerLocation(config.movieInfoLastSliderPosition);
	}

	_movieManager.setVisible(true);
	log.debug("MovieManager SetUp done!"); //$NON-NLS-1$
    }


    /**
     * Creates the menuBar.
     *
     * @return The menubar.
     **/
    protected JMenuBar createMenuBar() {
	log.debug("Start creation of the MenuBar."); //$NON-NLS-1$
	JMenuBar menuBar = new JMenuBar();
	menuBar.setBorder(BorderFactory.createEmptyBorder(2,0,8,0));
	/* Creation of the file menu. */
	menuBar.add(createMenuFile());
	/* Creation of the database menu. */
	menuBar.add(createMenuDatabase());
	/* Creation of the options menu. */
	menuBar.add(createMenuTools());

	/* Creation of the lists. */
	menuBar.add(createMenuLists());

	/* Creation of the help menu. */
	menuBar.add(createMenuHelp());
	log.debug("Creation of the MenuBar done."); //$NON-NLS-1$
	return menuBar;
    }

    /**
     * Creates the file menu.
     *
     * @return The file menu.
     **/
    protected JMenu createMenuFile() {
	log.debug("Start creation of the File menu."); //$NON-NLS-1$
	JMenu menuFile = new JMenu(Localizer.getString("moviemanager.menu.file")); //$NON-NLS-1$
	menuFile.setMnemonic('F');

	/* MenuItem New. */
	JMenuItem menuItemNew = new JMenuItem(Localizer.getString("moviemanager.menu.file.newdb"),'N'); //$NON-NLS-1$
	menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
	menuItemNew.setActionCommand("New"); //$NON-NLS-1$
	menuItemNew.addActionListener(new MovieManagerCommandNew());
	menuFile.add(menuItemNew);

	/* MenuItem Open. */
	JMenuItem menuItemOpen = new JMenuItem(Localizer.getString("moviemanager.menu.file.opendb"),'O'); //$NON-NLS-1$
	menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
	menuItemOpen.setActionCommand("Open"); //$NON-NLS-1$
	menuItemOpen.addActionListener(new MovieManagerCommandOpen());
	menuFile.add(menuItemOpen);
	/* A separator. */
	menuFile.addSeparator();

	/* MenuItem Close. */
	JMenuItem menuItemClose = new JMenuItem(Localizer.getString("moviemanager.menu.file.closedb"),'C'); //$NON-NLS-1$
	menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
	menuItemClose.setActionCommand("Open"); //$NON-NLS-1$
	menuItemClose.addActionListener(new MovieManagerCommandCloseDatabase());
	menuFile.add(menuItemClose);
	/* A separator. */
	menuFile.addSeparator();

	/* The Import menuItem. */
	JMenuItem menuImport = new JMenuItem(Localizer.getString("moviemanager.menu.file.import"),'I'); //$NON-NLS-1$
	menuImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
	menuImport.addActionListener(new MovieManagerCommandImport());
	/* Adds MenuItem Import. */
	menuFile.add(menuImport);
	/* A separator. */
	menuFile.addSeparator();

	/* The Export menuItem. */
	JMenuItem menuExport = new JMenuItem(Localizer.getString("moviemanager.menu.file.export"),'E'); //$NON-NLS-1$
	menuExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK));
	menuExport.addActionListener(new MovieManagerCommandExport());
	/* Adds menuItem Export. */
	menuFile.add(menuExport);
	/* A separator. */
	menuFile.addSeparator();

	/* MenuItem Exit. */
	JMenuItem menuItemExit = new JMenuItem(Localizer.getString("moviemanager.menu.file.exit"),'X'); //$NON-NLS-1$
	menuItemExit.setActionCommand("Exit"); //$NON-NLS-1$
	menuItemExit.addActionListener(new MovieManagerCommandExit());
	menuFile.add(menuItemExit);
	/* All done. */
	log.debug("Creation of the File menu done."); //$NON-NLS-1$
	return menuFile;
    }

    /**
     * Creates the database menu.
     *
     * @return The database menu.
     **/
    protected JMenu createMenuDatabase() {
	log.debug("Start creation of the Database menu."); //$NON-NLS-1$
	JMenu menuDatabase = new JMenu(Localizer.getString("moviemanager.menu.database")); //$NON-NLS-1$
	menuDatabase.setMnemonic('D');

	/* MenuItem Queries. */
	JMenuItem menuItemQueries = new JMenuItem(Localizer.getString("moviemanager.menu.database.queries"),'Q'); //$NON-NLS-1$
	menuItemQueries.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.CTRL_MASK));
	menuItemQueries.setActionCommand("Queries"); //$NON-NLS-1$
	menuItemQueries.addActionListener(new MovieManagerCommandQueries());
	menuDatabase.add(menuItemQueries);

	/* A separator. */
	menuDatabase.addSeparator();

	/* MenuItem Folders. */
	JMenuItem menuItemFolders = new JMenuItem(Localizer.getString("moviemanager.menu.database.folders"),'F'); //$NON-NLS-1$
	menuItemFolders.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,ActionEvent.CTRL_MASK));
	menuItemFolders.setActionCommand("Folders"); //$NON-NLS-1$
	menuItemFolders.addActionListener(new MovieManagerCommandFolders());
	menuDatabase.add(menuItemFolders);

	/* MenuItem AddField. */
	JMenuItem menuItemAddField = new JMenuItem(Localizer.getString("moviemanager.menu.database.additionalinfofields"),'I'); //$NON-NLS-1$
	menuItemAddField.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
	menuItemAddField.setActionCommand("AdditionalInfoFields"); //$NON-NLS-1$
	menuItemAddField.addActionListener(new MovieManagerCommandAdditionalInfoFields());
	menuDatabase.add(menuItemAddField);

	/* MenuItem AddList. */
	JMenuItem menuItemAddList = new JMenuItem(Localizer.getString("moviemanager.menu.database.lists"),'L'); //$NON-NLS-1$
	menuItemAddList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.CTRL_MASK));
	menuItemAddList.setActionCommand("setLists"); //$NON-NLS-1$
	menuItemAddList.addActionListener(new MovieManagerCommandLists());
	menuDatabase.add(menuItemAddList);

	/* MenuItem Convert Database. */
	JMenuItem convertDatabase = new JMenuItem(Localizer.getString("moviemanager.menu.database.covertdb"),'C'); //$NON-NLS-1$
	convertDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
	convertDatabase.setActionCommand("Convert Database"); //$NON-NLS-1$
	convertDatabase.addActionListener(new MovieManagerCommandConvertDatabase());
	menuDatabase.add(convertDatabase);

	/* MenuItem Convert Database. */
	JMenuItem saveNotes = new JMenuItem(Localizer.getString("moviemanager.menu.database.savechanhednotes"),'Z'); //$NON-NLS-1$
	saveNotes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,ActionEvent.CTRL_MASK));
	saveNotes.setActionCommand("Save changed notes"); //$NON-NLS-1$
	saveNotes.addActionListener(new MovieManagerCommandSaveChangedNotes());
	menuDatabase.add(saveNotes);

	/* All done. */
	log.debug("Creation of the Database menu done."); //$NON-NLS-1$
	return menuDatabase;
    }

    /**
     * Creates the tools menu.
     *
     * @return The tools menu.
     **/
    protected JMenu createMenuTools() {
	log.debug("Start creation of the Tools menu."); //$NON-NLS-1$
	JMenu menuTools = new JMenu(Localizer.getString("moviemanager.menu.tools")); //$NON-NLS-1$
	menuTools.setMnemonic('T');
	/* MenuItem Preferences.
	   For some reason, addMovie KeyEvent.VK_A doesn't work when focused
	   on the selected movie or the filter*/

	JMenuItem menuItemPrefs = new JMenuItem(Localizer.getString("moviemanager.menu.tools.preferences"),'P'); //$NON-NLS-1$
	menuItemPrefs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.CTRL_MASK));
	menuItemPrefs.setActionCommand("Preferences"); //$NON-NLS-1$
	menuItemPrefs.addActionListener(new MovieManagerCommandPrefs());
	menuTools.add(menuItemPrefs);

	menuTools.addSeparator();
	JMenuItem addMultipleMovies = new JMenuItem(Localizer.getString("moviemanager.menu.tools.addmultiplemovies"),'M'); //$NON-NLS-1$
	addMultipleMovies.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,ActionEvent.CTRL_MASK));
	addMultipleMovies.setActionCommand("Add Multiple Movies"); //$NON-NLS-1$
	addMultipleMovies.addActionListener(new MovieManagerCommandAddMultipleMoviesByFile());
	menuTools.add(addMultipleMovies);

	JMenuItem updateIMDbInfo = new JMenuItem(Localizer.getString("moviemanager.menu.tools.updateIMDbInfo"),'U'); //$NON-NLS-1$
	updateIMDbInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,ActionEvent.CTRL_MASK));
	updateIMDbInfo.setActionCommand("Update IMDb Info"); //$NON-NLS-1$
	updateIMDbInfo.addActionListener(new MovieManagerCommandUpdateIMDBInfo());
	menuTools.add(updateIMDbInfo);

	 menuTools.addSeparator();

        JMenuItem reportGenerator = new JMenuItem(Localizer.getString("moviemanager.menu.tools.reportgenerator"),'R'); //$NON-NLS-1$
        reportGenerator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
        reportGenerator.setActionCommand("Report Generator"); //$NON-NLS-1$
        reportGenerator.addActionListener(new MovieManagerCommandReportGenerator());
        menuTools.add(reportGenerator);

	/* All done. */
	log.debug("Creation of the Tools menu done."); //$NON-NLS-1$
	return menuTools;
    }

    /**
     * Creates the tools menu.
     *
     * @return The tools menu.
     **/
    protected JMenu createMenuLists() {
	log.debug("Start creation of the Lists menu."); //$NON-NLS-1$
	JMenu menuLists = new JMenu(Localizer.getString("moviemanager.menu.lists")); //$NON-NLS-1$
	menuLists.setMnemonic('L');

	log.debug("Creation of the Lists menu done."); //$NON-NLS-1$
	return menuLists;
    }

    /**
     * Creates the help menu.
     *
     * @return The help menu.
     **/
    protected JMenu createMenuHelp() {
	log.debug("Start creation of the Help menu."); //$NON-NLS-1$
	JMenu menuHelp = new JMenu(Localizer.getString("moviemanager.menu.help")); //$NON-NLS-1$
	menuHelp.setMnemonic('H');
	/* MenuItem Help. */
	JMenuItem menuItemHelp = new JMenuItem(Localizer.getString("moviemanager.menu.help.help"),'H'); //$NON-NLS-1$
	menuItemHelp.setAccelerator(KeyStroke.getKeyStroke("F1")); //$NON-NLS-1$
	menuItemHelp.setActionCommand("Help"); //$NON-NLS-1$
	menuItemHelp.addActionListener(new MovieManagerCommandHelp());
	menuHelp.add(menuItemHelp);
	/* MenuItem Online Help. */
	JMenuItem menuItemOnlineHelp = new JMenuItem(Localizer.getString("moviemanager.menu.help.onlinehelp"),'O'); //$NON-NLS-1$
	menuItemOnlineHelp.setActionCommand("OpenPage (Online Help)"); //$NON-NLS-1$
	menuItemOnlineHelp.addActionListener(new MovieManagerCommandOpenPage("http://xmm.sourceforge.net/help.html")); //$NON-NLS-1$
	menuHelp.add(menuItemOnlineHelp);
	/* A Separator. */
	menuHelp.addSeparator();
	/* MenuItem HomePage. */
	JMenuItem menuItemHomePage = new JMenuItem(Localizer.getString("moviemanager.menu.help.homepage"),'P'); //$NON-NLS-1$
	menuItemHomePage.setActionCommand("OpenPage (Home Page)"); //$NON-NLS-1$
	menuItemHomePage.addActionListener(new MovieManagerCommandOpenPage("http://xmm.sourceforge.net/")); //$NON-NLS-1$
	menuHelp.add(menuItemHomePage);
	/* A Separator. */
	menuHelp.addSeparator();
	/* MenuItem SourceForge. */
	JMenuItem menuItemSourceForge = new JMenuItem(Localizer.getString("moviemanager.menu.help.sourceforgepage"),'S'); //$NON-NLS-1$
	menuItemSourceForge.setActionCommand("OpenPage (SourceForge.net)"); //$NON-NLS-1$
	menuItemSourceForge.addActionListener(new MovieManagerCommandOpenPage("http://sourceforge.net/projects/xmm/")); //$NON-NLS-1$
	menuHelp.add(menuItemSourceForge);
	/* A Separator. */
	menuHelp.addSeparator();
	/* MenuItem About. */
	JMenuItem menuItemAbout = new JMenuItem(Localizer.getString("moviemanager.menu.help.about")); //$NON-NLS-1$
	menuItemAbout.setActionCommand("About"); //$NON-NLS-1$
	menuItemAbout.addActionListener(new MovieManagerCommandAbout());
	menuHelp.add(menuItemAbout);
	/* All done. */
	log.debug("Creation of the Help menu done."); //$NON-NLS-1$
	return menuHelp;
    }

    /**
     * Creates the working area.
     *
     * @return JPanel with working area.
     **/
    protected JPanel createWorkingArea() {
	log.debug("Start creation of the WorkingArea."); //$NON-NLS-1$
	JPanel workingArea = new JPanel();

	workingArea.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

	//double border = 10;
	double size[][] = {{0.33, info.clearthought.layout.TableLayout.FILL}, {info.clearthought.layout.TableLayout.FILL}};

	workingArea.setLayout(new info.clearthought.layout.TableLayout(size));

	/* Creates the Movies List Panel. */
	workingArea.add(createMoviesList(), "0, 0"); //$NON-NLS-1$

	/* Creates the Movie Info Panel.*/
	workingArea.add(createMovieInfo(), "1, 0"); //$NON-NLS-1$

	/* All done. */
	log.debug("Creation of the WorkingArea done."); //$NON-NLS-1$
	return workingArea;
    }

    /**
     * Creates the Movies List Panel.
     *
     * @return The Movies List Panel.
     **/
    protected JPanel createMoviesList() {

	if (getContentPane().getFont() == null) {
	    getContentPane().setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
	}
	log.debug("Start creation of the Movies List panel."); //$NON-NLS-1$

	JPanel moviesList = new JPanel(new GridBagLayout());

	moviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												 " "+ Localizer.getString("moviemanager.listpanel-title") + config.getCurrentList() + " ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												 TitledBorder.DEFAULT_JUSTIFICATION,
												 TitledBorder.DEFAULT_POSITION,
												 new Font(moviesList.getFont().getName(),Font.BOLD, fontSize)),
								BorderFactory.createEmptyBorder(0,5,5,5)));


	GridBagConstraints constraints;


	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	 constraints.weightx = 1;
 	constraints.weighty = 0;
	constraints.insets = new Insets(0,0,0,0);

	/* Adds the toolbar.*/
	moviesList.add(createToolBar(), constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.insets = new Insets(0,0,0,0);
	constraints.fill = GridBagConstraints.BOTH;

	/* Adds the list. */
	moviesList.add(createList(), constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.insets = new Insets(0,0,0,0);
	constraints.fill = GridBagConstraints.HORIZONTAL;

	/* Adds the filter. */
	moviesList.add(createFilter(), constraints);

	/* All done. */
	log.debug("Creation of the Movies List panel done."); //$NON-NLS-1$
	return moviesList;
    }


    public void setListTitle(String title) {

	JPanel moviesList = getPanelMovieList();
	moviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												 " "+ Localizer.getString("moviemanager.listpanel-title") + title + " ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												 TitledBorder.DEFAULT_JUSTIFICATION,
												 TitledBorder.DEFAULT_POSITION,
												 new Font(moviesList.getFont().getName(),Font.BOLD, fontSize)),
								BorderFactory.createEmptyBorder(0,5,5,5)));
    }

    /**
     * Creates the toolbar.
     *
     * @return The toolbar.
     **/
    //protected JToolBar createToolBar() {
    protected JToolBar createToolBar() {
	log.debug("Start creation of the ToolBar."); //$NON-NLS-1$

	boolean useDynamicToolbar = false;

	//JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
	JToolBar toolBar = null;

	if (useDynamicToolbar)
	    ;
	//toolBar = new WrapAroundToolBar4(SwingConstants.HORIZONTAL);
	else
	    toolBar = new JToolBar(SwingConstants.HORIZONTAL);
	
	toolBar.setRollover(true);
	
	//	toolBar.setLayout(new ModifiedFlowLayout(SwingConstants.HORIZONTAL, 4, 4));

	//toolBar.putClientProperty(new String("JToolBar.isRollover"), Boolean.TRUE);

	//toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,40,10), BorderFactory.createEmptyBorder(10,10,30,10)));

	//toolBar.setMinimumSize(new Dimension(254, 50));
	//toolBar.setPreferredSize(new Dimension(254, 60));

	toolBar.setFloatable(false);

	Dimension dim = new Dimension(3, 0);
	
	/* The Add button. */
	JButton buttonAdd = new JButton(new ImageIcon(_movieManager.getImage("/images/add.png").getScaledInstance(24, 24, Image.SCALE_SMOOTH))); //$NON-NLS-1$

	buttonAdd.setPreferredSize(new Dimension(39, 39));
	buttonAdd.setMaximumSize(new Dimension(45, 45));
	//buttonAdd.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,100,0,100), buttonAdd.getBorder()));
	buttonAdd.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

	buttonAdd.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-add")); //$NON-NLS-1$
	buttonAdd.setActionCommand("Add"); //$NON-NLS-1$
	buttonAdd.setMnemonic('A');
	buttonAdd.addActionListener(new MovieManagerCommandAdd());

	toolBar.add(buttonAdd);
	
	/* A separator. */
	toolBar.addSeparator(dim);

	/* The Remove button. */
	JButton buttonRemove = new JButton(new ImageIcon(_movieManager.getImage("/images/remove.png").getScaledInstance(26,26,Image.SCALE_SMOOTH))); //$NON-NLS-1$

	buttonRemove.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), buttonRemove.getBorder()));

	buttonRemove.setPreferredSize(new Dimension(39, 39));
	buttonRemove.setMaximumSize(new Dimension(44, 45));
	buttonRemove.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-remove")); //$NON-NLS-1$
	buttonRemove.setActionCommand("Remove"); //$NON-NLS-1$
	buttonRemove.setMnemonic('R');
	buttonRemove.addActionListener(new MovieManagerCommandRemove());
	toolBar.add(buttonRemove);

	/* A separator. */
	toolBar.addSeparator(dim);

	/* The Edit button. */
	JButton buttonEdit = new JButton(new ImageIcon(_movieManager.getImage("/images/edit.png").getScaledInstance(23,23,Image.SCALE_SMOOTH))); //$NON-NLS-1$

	//buttonEdit.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), buttonEdit.getBorder()));
	buttonEdit.setMargin(new Insets(0,10,0,10));
    
	buttonEdit.setPreferredSize(new Dimension(39, 39));
	buttonEdit.setMaximumSize(new Dimension(44, 45));
	buttonEdit.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-edit")); //$NON-NLS-1$
	buttonEdit.setActionCommand("Edit"); //$NON-NLS-1$
	buttonEdit.setMnemonic('E');
	buttonEdit.addActionListener(new MovieManagerCommandEdit());
	toolBar.add(buttonEdit);

	/* A separator. */
	toolBar.addSeparator(dim);

	/* The Search button. */
	JButton buttonSearch = new JButton(new ImageIcon(_movieManager.getImage("/images/search.png").getScaledInstance(25,25,Image.SCALE_SMOOTH))); //$NON-NLS-1$

	buttonSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), buttonSearch.getBorder()));

	buttonSearch.setPreferredSize(new Dimension(39, 39));
	buttonSearch.setMaximumSize(new Dimension(45, 45));
	buttonSearch.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-search")); //$NON-NLS-1$
	buttonSearch.setActionCommand("Search"); //$NON-NLS-1$
	buttonSearch.setMnemonic('S');
	buttonSearch.addActionListener(new MovieManagerCommandSearch());
	toolBar.add(buttonSearch);



	/* A separator. */
	toolBar.add(new JToolBar.Separator(new Dimension(8, 3)));

	JPanel panelEntries = new JPanel();
	panelEntries.setLayout(new BoxLayout(panelEntries, BoxLayout.X_AXIS));

	panelEntries.setBorder(new CompoundBorder(new EmptyBorder(0,0,0,0), new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(6,4,5,5))));


	panelEntries.setMaximumSize(new Dimension(46, 33));
	panelEntries.setPreferredSize(new Dimension(46, 33));
	panelEntries.setSize(new Dimension(46, 33));

	showEntries = new JLabel("    "); //$NON-NLS-1$
	showEntries.setFont(new Font(showEntries.getFont().getName(), Font.PLAIN,fontSize + 1));

	panelEntries.add(showEntries);

	toolBar.add(panelEntries);

	toolBar.setPreferredSize(new Dimension(toolBar.getMaximumSize()));
	toolBar.setMinimumSize(toolBar.getPreferredSize());
	
	/* All done. */
	log.debug("Creation of the ToolBar done."); //$NON-NLS-1$
	return toolBar;
    }

    /**
     * Creates the list of movies.
     *
     * @return The listofmovies.
     **/
    protected JScrollPane createList() {
	log.debug("Start creation of the List."); //$NON-NLS-1$

	ExtendedJTree tree = new ExtendedJTree();
	tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(new ModelMovie(-1, null, null, null, "", null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null)))); //$NON-NLS-1$

	tree.setRootVisible(false);
	tree.setDragEnabled(false);

	tree.setFont(new Font(tree.getFont().getName(),Font.PLAIN,fontSize));

	MovieManagerCommandSelect listener = new MovieManagerCommandSelect();

	/* Adding listeners to the movie list */
	tree.addTreeSelectionListener(listener);
	tree.addMouseListener(listener);
	tree.addKeyListener(listener);

	JScrollPane scrollPane = new JScrollPane();
	scrollPane.setViewportView(tree);
        tree.setCellRenderer(new ExtendedTreeCellRenderer(this, scrollPane));

	/* All done. */
	log.debug("Creation of the List done."); //$NON-NLS-1$
	return scrollPane;
    }

    /**
     * Creates a filter to act over the list of movies.
     *
     * @return The filter.
     **/

    protected JPanel createFilter() {

	log.debug("Start creation of the Filter."); //$NON-NLS-1$
	JPanel filter = new JPanel(new BorderLayout());
	filter.setBorder(BorderFactory.createEmptyBorder(10,4,4,4));

	/* Adds the Label. */
	JLabel label = new JLabel(Localizer.getString("moviemanager.listpanel-filter")); //$NON-NLS-1$
	label.setFont(new Font(label.getFont().getName(),Font.PLAIN,fontSize));
	filter.add(label, BorderLayout.WEST);

	/* Adds the TextField. */
	JTextField textField = new JTextField();
	textField.setFont(new Font("", Font.PLAIN, 12)); //$NON-NLS-1$
	textField.setActionCommand("Filter"); //$NON-NLS-1$
	textField.addActionListener(new MovieManagerCommandFilter("", null, true, true)); //$NON-NLS-1$
	filter.add(textField, BorderLayout.CENTER);

	/* All done. */
	log.debug("Creation of the Filter done."); //$NON-NLS-1$

	filter.setSize(255, 100);
	return filter;
    }

    /**
     * Creates the Movie Info Panel.
     *
     * @return The Movie Info Panel.
     **/
    protected JPanel createMovieInfo() {
	log.debug("Start creation of the Movie Info panel."); //$NON-NLS-1$
	JPanel movieInfo = new JPanel();
	movieInfo.addComponentListener(this);

	JPanel generalInfoPanel = createGeneralInfo();

	generalInfoPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

	double size[][] = {{info.clearthought.layout.TableLayout.FILL}, {generalInfoPanel.getPreferredSize().getHeight() + 20, info.clearthought.layout.TableLayout.FILL}};

	movieInfo.setLayout(new info.clearthought.layout.TableLayout(size));
	movieInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												Localizer.getString("moviemanager.movieinfopanel.title"), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(movieInfo.getFont().getName(),Font.BOLD, fontSize)),
							       BorderFactory.createEmptyBorder(0,5,5,5)));

	/* Adds the general info. */
	GridBagConstraints constraints;

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.weightx = 1;
	constraints.weighty = 0;
	constraints.insets = new Insets(0,0,0,0);
	constraints.anchor = GridBagConstraints.NORTH;
	constraints.fill = GridBagConstraints.HORIZONTAL;

	movieInfo.add(generalInfoPanel, "0, 0"); //$NON-NLS-1$

	JPanel miscellaneous = createMiscellaneous();

	JPanel plotAndCast = new JPanel();
	plotAndCast.setLayout(new GridLayout(2,1));

	plotAndCast.add(createPlot());
	plotAndCast.add(createCast());

	JTabbedPane all = new JTabbedPane();
	all.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
	all.add(Localizer.getString("moviemanager.movie-info-panel.plot_and_cast"), plotAndCast); //$NON-NLS-1$
	all.add(Localizer.getString("moviemanager.movie-info-panel.miscellaneous"), miscellaneous); //$NON-NLS-1$

	JPanel tabbedPanel = new JPanel(new BorderLayout());
	tabbedPanel.add(all, BorderLayout.CENTER);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.insets = new Insets(0,0,0,0);
	constraints.anchor = GridBagConstraints.SOUTH;
	constraints.fill = GridBagConstraints.BOTH;

	/* Adds the additional info and notes. */

	/* All done. */
	log.debug("Creation of the Movie Info panel done."); //$NON-NLS-1$

	/* Removing the border of the splitpane */
	//UIManager.put("SplitPane.contentBorderInsets", new Insets(0,0,0,0));
	UIManager.put("SplitPane.border", new javax.swing.plaf.BorderUIResource(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0))); //$NON-NLS-1$

	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tabbedPanel, createAdditionalInfoAndNotes());
	splitPane.setOneTouchExpandable(true);
	splitPane.setContinuousLayout(true);
	splitPane.setDividerSize(12);
	splitPane.setResizeWeight(0.5);

	movieInfo.add(splitPane, "0, 1"); //$NON-NLS-1$

	return movieInfo;
    }

    /**
     * Creates a JPanel for display the general info.
     *
     * @return The JPanel.
     **/

    protected JPanel createGeneralInfo() {
	log.debug("Start creation of the General Info panel."); //$NON-NLS-1$

	JPanel panelGeneralInfo = new JPanel();
	panelGeneralInfo.setLayout(new GridBagLayout());

	GridBagConstraints constraints;

	JPanel panelColour = new JPanel();
	panelColour.setLayout(new BoxLayout(panelColour, BoxLayout.X_AXIS));

	JLabel colourID = new JLabel(""); //$NON-NLS-1$
	colourID.setFont(new Font(colourID.getFont().getName(), Font.BOLD, fontSize));
	panelColour.add(colourID);

	JLabel colour = new JLabel(" "); //$NON-NLS-1$
	colour.setFont(new Font(colour.getFont().getName(), Font.PLAIN, fontSize));
	panelColour.add(colour);

	/* Movie Info (Excluding color and cover)  */

	/* Adds the subInfo JPanel. */
	JPanel panelDateAndTitle = new JPanel();

	panelDateAndTitle.setLayout(new BorderLayout());

	JTextField date = new JTextField();
	date.setFont(new Font(date.getFont().getName(), Font.BOLD, fontSize +3));
	date.setBorder(null);
	date.setOpaque(false);
	date.setEditable(false);

   	panelDateAndTitle.add(date, BorderLayout.WEST);


	JTextField title = new JTextField();
	title.setFont(new Font("Dialog", Font.BOLD, fontSize +3)); //$NON-NLS-1$
	title.setBorder(null);
	title.setOpaque(false);
	title.setEditable(false);

	panelDateAndTitle.add(title, BorderLayout.CENTER);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridwidth = 4;
	constraints.insets = new Insets(0,0,10,0);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.fill = GridBagConstraints.HORIZONTAL;

	panelGeneralInfo.add(panelDateAndTitle, constraints);


	JPanel panelDirected = new JPanel();
	panelDirected.setLayout(new BoxLayout(panelDirected, BoxLayout.X_AXIS));

	JLabel directedID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.directedby") + ": "); //$NON-NLS-1$
	directedID.setFont(new Font(directedID.getFont().getName(), Font.BOLD, fontSize));
	panelDirected.add(directedID);

	JTextField directed = new JTextField();
	directed.setFont(new Font(directed.getFont().getName(), Font.PLAIN, fontSize));
	directed.setBorder(null);
	directed.setOpaque(false);
	directed.setEditable(false);

	panelDirected.add(directed);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridwidth = 4;
	constraints.insets = new Insets(0,0,0,5);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.fill = GridBagConstraints.HORIZONTAL;

	panelGeneralInfo.add(panelDirected, constraints);

	JPanel panelWritten = new JPanel();
	panelWritten.setLayout(new BoxLayout(panelWritten, BoxLayout.X_AXIS));

	JLabel writtenID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.writtenby") + ": "); //$NON-NLS-1$
	writtenID.setFont(new Font(writtenID.getFont().getName(), Font.BOLD, fontSize));
	panelWritten.add(writtenID);

	JTextField written = new JTextField();
	written.setFont(new Font(written.getFont().getName(), Font.PLAIN, fontSize));
	written.setBorder(null);
	written.setOpaque(false);
	written.setEditable(false);

	panelWritten.add(written);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.weightx = 0;
	constraints.weighty = 1;
	constraints.gridwidth = 4;
	constraints.insets = new Insets(0,0,4,0);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.fill = GridBagConstraints.HORIZONTAL;

	panelGeneralInfo.add(panelWritten, constraints);

	JPanel panelGenre = new JPanel();
	panelGenre.setLayout(new BoxLayout(panelGenre, BoxLayout.X_AXIS));

	JLabel genreID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.genre") + ": "); //$NON-NLS-1$
	genreID.setFont(new Font(genreID.getFont().getName(), Font.BOLD, fontSize));
	panelGenre.add(genreID);

	JTextField genre = new JTextField();
	genre.setFont(new Font(genre.getFont().getName(), Font.PLAIN, fontSize));
	genre.setBorder(null);
	genre.setOpaque(false);
	genre.setEditable(false);

	panelGenre.add(genre);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridwidth = 4;
	constraints.insets = new Insets(4,0,2,0);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.fill = GridBagConstraints.HORIZONTAL;

	panelGeneralInfo.add(panelGenre, constraints);

	JPanel panelRating = new JPanel();
	panelRating.setLayout(new BoxLayout(panelRating, BoxLayout.X_AXIS));



	JLabel ratingID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.raing") + ": "); //$NON-NLS-1$
	ratingID.setFont(new Font(ratingID.getFont().getName(), Font.BOLD, fontSize));
	panelRating.add(ratingID);

	JLabel rating = new JLabel();
	rating.setFont(new Font(rating.getFont().getName(), Font.PLAIN, fontSize));
	panelRating.add(rating);

	panelRating.add(rating);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(2,0,2,0);
	constraints.anchor = GridBagConstraints.WEST;
	constraints.fill = GridBagConstraints.HORIZONTAL;

	panelGeneralInfo.add(panelRating, constraints);

	JPanel panelCountry = new JPanel();
	panelCountry.setLayout(new BoxLayout(panelCountry, BoxLayout.X_AXIS));

	JLabel countryID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.country") + ": "); //$NON-NLS-1$
	countryID.setFont(new Font(countryID.getFont().getName(), Font.BOLD, fontSize));

	countryID.setMinimumSize(countryID.getPreferredSize());

	panelCountry.add(countryID);

	JTextField country = new JTextField();
	country.setFont(new Font(country.getFont().getName(), Font.PLAIN, fontSize));
	country.setBorder(null);
	country.setOpaque(false);
	country.setEditable(false);

	panelCountry.add(country);

	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 5;
	constraints.weightx = 6;
	constraints.weighty = 0;
	constraints.gridwidth = 2;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.insets = new Insets(2,0,2,0);
	constraints.anchor = GridBagConstraints.WEST;

	panelGeneralInfo.add(panelCountry, constraints);

	JPanel panelSeen = new JPanel();
	panelSeen.setLayout(new BoxLayout(panelSeen, BoxLayout.X_AXIS));

	JLabel seenID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.seen") + ": "); //$NON-NLS-1$
	seenID.setFont(new Font(seenID.getFont().getName(), Font.BOLD, fontSize));
	panelSeen.add(seenID);

	/* Will only change value if seen option is set to editable */
	JCheckBox seenBox = new JCheckBox() {
		protected void processMouseEvent(MouseEvent event) {

		    if (event.getID() == MouseEvent.MOUSE_CLICKED) {
			if (config.getSeenEditable())
			    updateSeen(0);
		    }
		}
	    };

	if (config.getUseRegularSeenIcon()) {
	    seenBox.setIcon(new ImageIcon(MovieManager.getIt().getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	    seenBox.setSelectedIcon(new ImageIcon(MovieManager.getIt().getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	}

	seenBox.setPreferredSize(new Dimension(21, 21));
	seenBox.setMinimumSize(new Dimension(21, 21));

	panelSeen.add(seenBox);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(2,0,2,4);
	constraints.anchor = GridBagConstraints.WEST;

	panelGeneralInfo.add(panelSeen, constraints);

	/* Adds the language. */
	JPanel panelLanguage = new JPanel();
	panelLanguage.setLayout(new BoxLayout(panelLanguage, BoxLayout.X_AXIS));

	JLabel languageID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.language") + ": "); //$NON-NLS-1$
	languageID.setFont(new Font(languageID.getFont().getName(), Font.BOLD, fontSize));
	//languageID.setPreferredSize(new Dimension((int) new JLabel("Language: ").getPreferredSize().getWidth(), (int) languageID.getPreferredSize().getHeight()));
	languageID.setMinimumSize(languageID.getPreferredSize());

	panelLanguage.add(languageID);

	JTextField language = new JTextField();
	language.setFont(new Font(language.getFont().getName(), Font.PLAIN, fontSize));
	language.setBorder(null);
	language.setOpaque(false);
	language.setEditable(false);

	panelLanguage.add(language);

	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 6;
	constraints.gridwidth = 2;
	constraints.weightx = 6;
	constraints.weighty = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.insets = new Insets(2,0,2,0);
	constraints.anchor = GridBagConstraints.WEST;

	panelGeneralInfo.add(panelLanguage, constraints);

	/* Adds the cover. */
	JPanel panelCover = new JPanel();

	JLabel cover = new JLabel(new ImageIcon(_movieManager.getImage("/images/" + config.getNoCover()).getScaledInstance(97,97,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	cover.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), BorderFactory.createEtchedBorder()));
	cover.setPreferredSize(new Dimension(97,145));
	cover.setMinimumSize(new Dimension(97,145));

	panelCover.add(cover);


	constraints = new GridBagConstraints();
	constraints.gridx = 4;
	constraints.gridy = 1;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.gridheight = 6;
	constraints.anchor = GridBagConstraints.NORTHEAST;


	/* Testing */

	JPanel panelInfo = new JPanel();
	panelInfo.setLayout(new GridBagLayout());

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 2;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.insets = new Insets(0,0,0,0);
	constraints.anchor = GridBagConstraints.CENTER;

	panelInfo.add(panelGeneralInfo, constraints);


	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(0,0,0,0);
	constraints.anchor = GridBagConstraints.NORTHEAST;

	panelInfo.add(panelColour, constraints);


	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.insets = new Insets(0,0,0,0);
	constraints.anchor = GridBagConstraints.EAST;

	panelInfo.add(cover, constraints);

	/* All done. */
	log.debug("Creation of the General Info panel done."); //$NON-NLS-1$

	return panelInfo;
    }

    /**
     * Creates a JPanel for display the plot.
     *
     * @return The JPanel.
     **/
    protected JPanel createPlot() {
	log.debug("Start creation of the Plot panel."); //$NON-NLS-1$
	JPanel plot = new JPanel();

	plot.setLayout(new BorderLayout());

	plot.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder() /*BorderFactory.createEmptyBorder()*/,
																					Localizer.getString("moviemanager.movie-info-panel.plot"), //$NON-NLS-1$
																					TitledBorder.DEFAULT_JUSTIFICATION,
																					TitledBorder.DEFAULT_POSITION,
																					new Font(plot.getFont().getName(),Font.PLAIN, fontSize)),
																       BorderFactory.createEmptyBorder(2,5,3,5))));

	JTextArea textAreaPlot = new JTextArea(""); //$NON-NLS-1$
	textAreaPlot.setEditable(false);
	textAreaPlot.setFocusable(true);
	textAreaPlot.setLineWrap(true);
	textAreaPlot.setWrapStyleWord(true);
	JScrollPane scrollPane = new JScrollPane(textAreaPlot);
	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	plot.add(scrollPane, BorderLayout.CENTER);
	/* All done. */
	log.debug("Creation of the Plot panel done."); //$NON-NLS-1$
	return plot;
    }

    /**
     * Creates a JPanel for display the cast.
     *
     * @return The JPanel.
     **/
    protected JPanel createCast() {
	log.debug("Start creation of the Cast panel."); //$NON-NLS-1$
	JPanel cast = new JPanel();

	cast.setLayout(new BorderLayout());

	cast.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																					Localizer.getString("moviemanager.movie-info-panel.cast"), //$NON-NLS-1$
																					TitledBorder.DEFAULT_JUSTIFICATION,
																					TitledBorder.DEFAULT_POSITION,
																					new Font(cast.getFont().getName(),Font.PLAIN, fontSize)),
																       BorderFactory.createEmptyBorder(2,5,3,5))));
	JTextArea textAreaCast = new JTextArea();
	textAreaCast.setEditable(false);
	textAreaCast.setFocusable(true);
	textAreaCast.setLineWrap(true);
	textAreaCast.setWrapStyleWord(true);
	JScrollPane scrollPane = new JScrollPane(textAreaCast);
	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	cast.add(scrollPane, BorderLayout.CENTER);

	/* All done. */
	log.debug("Creation of the Cast done."); //$NON-NLS-1$
	return cast;
    }


    /**
     * Creates a JPanel for display the cast.
     *
     * @return The JPanel.
     **/
    protected JPanel createMiscellaneous() {
	log.debug(Localizer.getString("moviemanager.0")); //$NON-NLS-1$
	JPanel miscellaenous = new JPanel();

	miscellaenous.setLayout(new BorderLayout());

	miscellaenous.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																						 " Miscellaneous ", //$NON-NLS-1$
																						 TitledBorder.DEFAULT_JUSTIFICATION,
																						 TitledBorder.DEFAULT_POSITION,
																						 new Font(miscellaenous.getFont().getName(),Font.PLAIN, fontSize)),
																		BorderFactory.createEmptyBorder(2,5,3,5))));


	JTextPane textAreaMiscellaenous = new JTextPane();
	textAreaMiscellaenous.setContentType("text/html"); //$NON-NLS-1$
	textAreaMiscellaenous.setBackground((Color) UIManager.get("TextArea.background")); //$NON-NLS-1$
	textAreaMiscellaenous.setEditable(false);
	textAreaMiscellaenous.setFocusable(false);

	JScrollPane scrollPane = new JScrollPane(textAreaMiscellaenous);
	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	miscellaenous.add(scrollPane, BorderLayout.CENTER);

	/* All done. */
	log.debug("Creation of the Miscellaenous done."); //$NON-NLS-1$
	return miscellaenous;
    }


    /**
     * Creates a JPanel for display the additional info and the notes.
     *
     * @return The JPanel.
     **/
    protected JPanel createAdditionalInfoAndNotes() {

	log.debug("Start creation of the Additional Info and Notes panel."); //$NON-NLS-1$
	JPanel additionalInfoAndNotes = new JPanel();
	additionalInfoAndNotes.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));

	additionalInfoAndNotes.setLayout(new BorderLayout());

	/* The additional info panel. */
	JPanel additionalInfo = new JPanel();
	additionalInfo.setLayout(new BorderLayout());

	additionalInfo.addComponentListener(this);
	additionalInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																						  Localizer.getString("moviemanager.movie-info-panel.additionalinfo"), //$NON-NLS-1$
																						  TitledBorder.DEFAULT_JUSTIFICATION,
																						  TitledBorder.DEFAULT_POSITION,
																						  new Font(additionalInfo.getFont().getName(),Font.PLAIN, fontSize)),
																		 BorderFactory.createEmptyBorder(1,5,3,5))));
	JTextArea textAreaAdditionalInfo = new JTextArea(""); //$NON-NLS-1$
	textAreaAdditionalInfo.setEditable(false);
	textAreaAdditionalInfo.setFocusable(true);
	textAreaAdditionalInfo.setLineWrap(false);

	JScrollPane scrollPaneAdditionalInfo = new JScrollPane(textAreaAdditionalInfo);
	scrollPaneAdditionalInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	additionalInfo.add(scrollPaneAdditionalInfo, BorderLayout.CENTER);

	/* The notes panel. */
	JPanel notes = new JPanel();
	notes.setLayout(new BorderLayout());
	notes.addComponentListener(this);

	notes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																					 Localizer.getString("moviemanager.movie-info-panel.notes"), //$NON-NLS-1$
																					 TitledBorder.DEFAULT_JUSTIFICATION,
																					 TitledBorder.DEFAULT_POSITION,
																					 new Font(notes.getFont().getName(),Font.PLAIN, fontSize)),
																	BorderFactory.createEmptyBorder(1,5,3,5))));

	JTextArea textAreaNotes = new JTextArea(""); //$NON-NLS-1$
	textAreaNotes.setEditable(true);
	textAreaNotes.setFocusable(true);
	textAreaNotes.setLineWrap(true);
	textAreaNotes.setWrapStyleWord(true);
	JScrollPane scrollPaneNotes = new JScrollPane(textAreaNotes);
	scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	notes.add(scrollPaneNotes, BorderLayout.CENTER);


	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, additionalInfo, notes);
	splitPane.setOneTouchExpandable(true);
	splitPane.setContinuousLayout(true);
	splitPane.setDividerSize(12);
	splitPane.setResizeWeight(0.5);

	additionalInfoAndNotes.add(splitPane, BorderLayout.CENTER);

	/* All done. */
	log.debug("Creation of the Additional Info and Notes done."); //$NON-NLS-1$
	return additionalInfoAndNotes;
    }


    public void	componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}

    public void componentResized(ComponentEvent e) {
	movieListWidth = (int) getMoviesList().getSize().getWidth();

	/* Maximized */
	if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
	    config.setMainMaximized(true);
	}
	else {
	    config.setMainSize(getMainSize());
	    config.setMainMaximized(false);
	}
    }

    public void componentMoved(ComponentEvent e) {
	config.setScreenLocation(getLocationOnScreen());
    }


     /**
     * Sets enabled/disabled the related database components.
     **/
    public void setDatabaseComponentsEnable(boolean enable) {
	/* Close database MenuItem. */
    	_movieManager.getJMenuBar().getMenu(0).getItem(3).setEnabled(enable);
	/* Import MenuItem. */
    	_movieManager.getJMenuBar().getMenu(0).getItem(5).setEnabled(enable);
	/* Export MenuItem. */
    	_movieManager.getJMenuBar().getMenu(0).getItem(7).setEnabled(enable);
	/* Database Menu. */
    	_movieManager.getJMenuBar().getMenu(1).setEnabled(enable);
	/* Queries MenuItem. */
    	_movieManager.getJMenuBar().getMenu(1).getItem(0).setEnabled(enable);
	/* Folders MenuItem. */
    	_movieManager.getJMenuBar().getMenu(1).getItem(2).setEnabled(enable);
	/* Additional Info Fields MenuItem. */
    	_movieManager.getJMenuBar().getMenu(1).getItem(3).setEnabled(enable);
	/* Convert Database MenuItem*/
    	_movieManager.getJMenuBar().getMenu(1).getItem(4).setEnabled(enable);
	/* Add multiple movies MenuItem*/
    	_movieManager.getJMenuBar().getMenu(2).getItem(2).setEnabled(enable);
	/* Update IMDb info */
	_movieManager.getJMenuBar().getMenu(2).getItem(3).setEnabled(enable);
	/* Report generator */
	_movieManager.getJMenuBar().getMenu(2).getItem(5).setEnabled(enable);
	/* Lists*/
    	_movieManager.getJMenuBar().getMenu(3).setEnabled(enable);


	/* Add Button. */
	getAddButton().setEnabled(enable);
	/* Remove Button. */
	getRemoveButton().setEnabled(enable);
	/* Edit Button. */
	getEditButton().setEnabled(enable);
	/* Search Button. */
	getSearchButton().setEnabled(enable);

	/* The JTree. */
	getMoviesList().setEnabled(enable);

	/* Filter textField. */
	getFilter().setEnabled(enable);

	/* Makes the list selected. */
	getMoviesList().requestFocus(true);
    }


    /**
     * Finalizes this object (closes the out streams and disposes).
     **/
    public void finalize() {
	
	/* Disposes. */
	dispose();
    }

     public void updateLookAndFeelValues() {
	updateToolButtonBorder();
	updateJTreeIcons();
	
	//getMoviesList().updateUI();
    }

    public void updateJTreeIcons() {
	getMoviesList().setRowHeight(config.getMovieListRowHeight() + 2);
	
	/* Show handles in cover mode or no icon mode, otherwise it's hard to recognize series. */
	getMoviesList().setShowsRootHandles(config.getUseJTreeCovers() || !config.getUseJTreeIcons());
    }

    public void updateToolButtonBorder() {
	if (config.isRegularToolButtonsUsed())
	    updateToolButtonBorderToRegular();
	else
	    updateToolButtonBorderToCurrentLaf();
    }


    void updateToolButtonBorderToCurrentLaf() {

	//getToolBar().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEmptyBorder(0,0,0,0)));

	// getAddButton().setMargin(new Insets(15,15,15,45));
// 	getRemoveButton().setMargin(new Insets(15,45,15,15));

	getAddButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,15,3,15), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
	getRemoveButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,15,3,15), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
	getEditButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,13,3,13), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
	getSearchButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,13,3,13), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));

	// getAddButton().setBorder(BorderFactory.createCompoundBorder(new JButton().getBorder(), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
// 	getRemoveButton().setBorder(BorderFactory.createCompoundBorder(new JButton().getBorder(), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
// 	getEditButton().setBorder(BorderFactory.createCompoundBorder(new JButton().getBorder(), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
// 	getSearchButton().setBorder(BorderFactory.createCompoundBorder(new JButton().getBorder(), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));

	getAddButton().setMaximumSize(new Dimension(45, 45));
	getRemoveButton().setMaximumSize(new Dimension(44, 45));
	getEditButton().setMaximumSize(new Dimension(44, 45));
	getSearchButton().setMaximumSize(new Dimension(45, 45));

	getAddButton().setPreferredSize(new Dimension(45, 45));
	getRemoveButton().setPreferredSize(new Dimension(44, 45));
	getEditButton().setPreferredSize(new Dimension(44, 45));
	getSearchButton().setPreferredSize(new Dimension(45, 45));
    }

    void updateToolButtonBorderToRegular() {

	getToolBar().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,0), BorderFactory.createEmptyBorder(0,0,0,0)));

	getAddButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
	getRemoveButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
	getEditButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
	getSearchButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));

	Dimension dim = new Dimension(40, 40);

	getAddButton().setPreferredSize(dim);
	getRemoveButton().setPreferredSize(dim);
	getEditButton().setPreferredSize(dim);
	getSearchButton().setPreferredSize(dim);

	getAddButton().setMaximumSize(dim);
	getRemoveButton().setMaximumSize(dim);
	getEditButton().setMaximumSize(dim);
	getSearchButton().setMaximumSize(dim);
    }


    JPanel getPanelMovieList() {
	return ((JPanel)
		((JPanel)
		 _movieManager.getContentPane().getComponent(0)).getComponent(0));
    }

    JPanel getPanelMovieInfo() {
	return ((JPanel)
		((JPanel)
		 _movieManager.getContentPane().getComponent(0)).getComponent(1));
    }

    public JPanel getPanelGeneralInfo() {
	return
	    (JPanel)
	    ((JPanel)
	    getPanelMovieInfo().getComponent(0)).getComponent(0);
    }

    JScrollPane getPlotScrollPane() {
	return
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       ((JTabbedPane)
		getPanelMovieInfo().getComponent(1)).getComponent(0)).getComponent(0)).getComponent(0));
    }

    JScrollPane getCastScrollPane() {
	return
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       ((JTabbedPane)
		getPanelMovieInfo().getComponent(1)).getComponent(0)).getComponent(1)).getComponent(0));
    }

    JScrollPane getMiscellaneousScrollPane() {
	return
	    ((JScrollPane)
	     (getMiscellaneousPanel()).getComponent(0));
    }

    JPanel getPlotPanel() {
	return
	    ((JPanel)
	     ((JPanel)
	      getTabbedPlotCastMiscellaneous().getComponent(0)).getComponent(0));
    }

    JPanel getCastPanel() {
	return
	    ((JPanel)
	     ((JPanel)
	      getTabbedPlotCastMiscellaneous().getComponent(0)).getComponent(1));
    }

    JPanel getMiscellaneousPanel() {
	return
	    ((JPanel)
	     (getTabbedPlotCastMiscellaneous()).getComponent(1));
    }

    JTabbedPane getTabbedPlotCastMiscellaneous() {
	return
	    ((JTabbedPane)
	     ((JPanel)
	      getMovieInfoSplitPane().getComponent(0)).getComponent(0));
    }

    public JSplitPane getMovieInfoSplitPane() {
	return ((JSplitPane)
		(getPanelMovieInfo()).getComponent(1));
    }


    public JSplitPane getAdditionalInfoNotesSplitPane() {
	return ((JSplitPane)
		((JPanel)
		 (getMovieInfoSplitPane()).getComponent(1)).getComponent(0));
    }

     public JTextArea getPlot() {
	 return
	    ((JTextArea)
	     ((JScrollPane)
	      getPlotPanel().getComponent(0)).getViewport().getComponent(0));
    }

    public JTextArea getCast() {
	return
	    ((JTextArea)
	     ((JScrollPane)
	      getCastPanel().getComponent(0)).getViewport().getComponent(0));
    }

    public JTextPane getMiscellaneous() {
	return
	    ((JTextPane)
	     ((JScrollPane)
	      getMiscellaneousPanel().getComponent(0)).getViewport().getComponent(0));
    }

    /**
     * Gets the AdditionalInfo JTextArea.
     **/
    public JTextArea getAdditionalInfo() {
	return
	    ((JTextArea)
	     getAdditionalInfoScrollPane().getViewport().getComponent(0));
    }

    /**
     * Gets the AdditionalInfo JScrollPane.
     **/
    public JScrollPane getAdditionalInfoScrollPane() {
	return
	    ((JScrollPane)
	     ((JPanel)
	      getAdditionalInfoNotesSplitPane().getComponent(0)).getComponent(0));
    }

    /**
     * Gets the notes JTextArea.
     **/
    public JTextArea getNotes() {
	return
	    ((JTextArea)
	     ((JScrollPane)
	      ((JPanel)
	       getAdditionalInfoNotesSplitPane().getComponent(1)).getComponent(0)).getViewport().getComponent(0));
    }

    JPanel getPanelAdditionalInfo() {
	return ((JPanel)
		(getAdditionalInfoNotesSplitPane()).getComponent(0));
    }

    JPanel panelNotes() {
	return ((JPanel)
		(getAdditionalInfoNotesSplitPane()).getComponent(1));
    }


    JMenu getListMenu() {
	return (JMenu) (_movieManager.getJMenuBar()).getComponent(3);
    }

    JToolBar getToolBar() {
	return
	    ((JToolBar)
	      ((JPanel)
	       ((JPanel)
		_movieManager.getContentPane().getComponent(0)).getComponent(0)).getComponent(0))/*.getComponent(0))*/;
    }

    JButton getAddButton() {
	return (JButton) getToolBar().getComponent(0);
    }

    JButton getRemoveButton() {
	return (JButton) getToolBar().getComponent(2);
    }

    JButton getEditButton() {
	return (JButton) getToolBar().getComponent(4);
    }

    JButton getSearchButton() {
	return (JButton) getToolBar().getComponent(6);
    }

    JPanel getEntriesPanel() {
	return
	    ((JPanel)
	     ((JPanel)
	      ((JPanel)
	       ((JPanel)
		_movieManager.getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1));
    }

    /**
     * Gets the Movie List.
     *
     * @return JList that displays the MovieList.
     **/
    public JTree getMoviesList() {
	return
	    (JTree)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       _movieManager.getContentPane().getComponent(0)).getComponent(0)).getComponent(1)).getViewport().getComponent(0);
    }



    /**
     * Gets the Movie List.
     *
     * @return JList that displays the MovieList.
     **/
    public JScrollPane getMoviesListScrollPane() {
	return
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       _movieManager.getContentPane().getComponent(0)).getComponent(0)).getComponent(1));
    }

    /**
     * Gets the Filter JTextField.
     *
     * @return JTextField.
     **/
    public JTextField getFilter() {
	return
	    (JTextField)
	    ((JPanel)
	     ((JPanel)
	      ((JPanel)
	       _movieManager.getContentPane().getComponent(0)).getComponent(0)).getComponent(2)).getComponent(1);
    }

    protected JPanel getFilterPanel() {
	return
	    ((JPanel)
	     ((JPanel)
	      ((JPanel)
	       _movieManager.getContentPane().getComponent(0)).getComponent(0)).getComponent(2));
    }


    public JLabel getCover() {
	return
	    (JLabel)
	    ((JPanel)
	     getPanelMovieInfo().getComponent(0)).getComponent(2);
    }

    public JLabel getCover4() {
	return
	    (JLabel)
	    getPanelGeneralInfo().getComponent(9);
    }

    public JLabel getCover1() {
	return
	    (JLabel)
	    ((JPanel)
	     ((JPanel)
	      ((JPanel)
	       ((JPanel)
		((JPanel)
                 _movieManager.getContentPane().getComponent(0)).getComponent(1)).getComponent(0)).getComponent(0)).getComponent(9)).getComponent(0);
    }

    /**
     * Gets the cover JLabel.
     **/
    public JLabel getCover2() {
	return
	    (JLabel)
	    ((JPanel)
	    ((JPanel)
	     ((JPanel)
	      ((JPanel)
	       _movieManager.getContentPane().getComponent(0)).getComponent(1)).getComponent(0)).getComponent(1)).getComponent(0);
    }



    /**
     * Gets the Colour JLabel.
     **/
    public JLabel getColourField() {
	return
	    (JLabel)
	    ((JPanel)
	     ((JPanel)
	      getPanelMovieInfo().getComponent(0)).getComponent(1)).getComponent(1);
    }

    /**
     * Gets the ColourLabel JLabel.
     **/
    public JLabel getColourLabel() {
	return
	    (JLabel)
	    ((JPanel)
	     ((JPanel)
	      getPanelMovieInfo().getComponent(0)).getComponent(1)).getComponent(0);
    }


    /**
     * Gets the Date JLabel.
     **/
    public JTextField getDateField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(0)).getComponent(0);
    }


    /**
     * Gets the Movie Title JLabel.
     **/
    public JTextField getTitleField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(0)).getComponent(1);
    }


    /**
     * Gets the Directed by JLabel.
     **/
    public JTextField getDirectedByField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(1)).getComponent(1);
    }

    /**
     * Gets the Written by JLabel.
     **/
    public JTextField getWrittenByField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(2)).getComponent(1);
    }

    /**
     * Gets the Genre JLabel.
     **/
    public JTextField getGenreField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(3)).getComponent(1);
    }

    /**
     * Gets the Rating JLabel.
     **/
    public JLabel getRatingField() {
	return
	    (JLabel)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(4)).getComponent(1);
    }


    /**
     * Gets the country JTextField.
     **/
    public JTextField getCountryTextField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(5)).getComponent(1);
    }

    /**
     * Gets the countryLabel JLabel.
     **/
    public JLabel getCountryLabel() {
	return
	    (JLabel)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(5)).getComponent(0);
    }


    /**
     * Gets the Seen JLabel.
     **/
    public JCheckBox getSeen() {
	return
	    (JCheckBox)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(6)).getComponent(1);
    }

    /**
     * Gets the language JTextField.
     **/
    public JTextField getLanguageTextField() {
	return
	    (JTextField)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(7)).getComponent(1);
    }

    /**
     * Gets the language JTextField.
     **/
    public JLabel getLanguageLabel() {
	return
	    (JLabel)
	    ((JPanel)
	     getPanelGeneralInfo().getComponent(7)).getComponent(0);
    }



    /**
     * Loads the database with the the path read from config.ini file
     **/
    protected void loadDatabase() {

	if (!config.getLoadDatabaseOnStartup())
	    return;

	SwingWorker worker = new SwingWorker() {
		SimpleProgressBar progressBar;

		public Object construct() {

		    String databasePath = config.getDatabasePath(true);
		    String type = ""; //$NON-NLS-1$

		    if (databasePath == null || databasePath.equals("null")) { //$NON-NLS-1$
			return null;
		    }
		    
		    log.debug("Start loading database."); //$NON-NLS-1$
		    
		    if (!databasePath.equals("")) { //$NON-NLS-1$

			/* If not, no database type specified */
			if (databasePath.indexOf(">") != -1) { //$NON-NLS-1$
			    type = databasePath.substring(0, databasePath.indexOf(">")); //$NON-NLS-1$
			    databasePath = databasePath.substring(databasePath.indexOf(">")+1, databasePath.length()); //$NON-NLS-1$
			}
			else {
			    if (databasePath.endsWith(".mdb")) //$NON-NLS-1$
				type = "MSAccess"; //$NON-NLS-1$
			    else if (new File(databasePath+".properties").exists() && new File(databasePath+".script").exists()) //$NON-NLS-1$ //$NON-NLS-2$
				type = "HSQL"; //$NON-NLS-1$
			}
		    }
		    else {
			log.debug("database path is empty"); //$NON-NLS-1$
			return null;
		    }
		    
		    progressBar = new SimpleProgressBar(MovieManager.getIt(), true, this);
		    
		    ShowGUI.show(progressBar, true);
		    
		    if (type.equals("MySQL")) //$NON-NLS-1$
			DialogDatabase.updateProgress(progressBar, Localizer.getString("moviemanager.progress.connecting-to-database")); //$NON-NLS-1$
		    else
			DialogDatabase.updateProgress(progressBar, Localizer.getString("moviemanager.progress.creating-connection")); //$NON-NLS-1$


		    log.info("Loading " +type+ ":" + databasePath); //$NON-NLS-1$ //$NON-NLS-2$

		    /* Database path relative to program location */
		    if (config.getUseRelativeDatabasePath() == 2)
			databasePath = getUserDir() + databasePath;

		    Database db = null;

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


		    if (db != null) {

			try {
			    Runnable setModel = new Runnable() {
				    public void run() {
					ExtendedTreeNode root = new ExtendedTreeNode(new ModelMovie(-1, null, null, null, Localizer.getString("moviemanager.progress.loading-database"), null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null)); //$NON-NLS-1$

					DefaultTreeModel model = new AutomatedTreeModel(root, false);

					getMoviesList().setModel(model);
					getMoviesList().setRootVisible(true);
				    }
				};
			    SwingUtilities.invokeLater(setModel);

			} catch (Exception e) {
			    log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
			}

			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

			long time = System.currentTimeMillis();

			DialogDatabase.updateProgress(progressBar, Localizer.getString("moviemanager.progress.retrieving-movie-list")); //$NON-NLS-1$

			if (setDatabase(db, false))
			    log.debug("Database loaded in:" + (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
			else
			    log.debug("Failed to load database"); //$NON-NLS-1$
		    }

		    progressBar.dispose();
		    return null;
		}
	    };
	worker.start();
    }



    /* mode = 0 (invert), 1 (all to seen), 2(all to unseen). */
    public void updateSeen(int mode) {

	JTree movieList = MovieManager.getIt().getMoviesList();

	if (movieList.getLastSelectedPathComponent() == null)
	    return;

	TreePath [] selectionPaths = movieList.getSelectionPaths();

	/* The currently visible entry */
	ModelEntry selected = (ModelEntry) ((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject();

	if (selected.getKey() == -1)
	    return;

	/* Should only be one entry when inverting (Pusing the seen label/image)*/
	if (mode == 0)
	    selectionPaths = new TreePath[] {movieList.getLeadSelectionPath()};

	Database db = MovieManager.getIt().getDatabase();
	boolean seen;
	int key;
	ModelEntry model;

	for (int i = 0; i < selectionPaths.length; i++) {

	    model = (ModelEntry) ((DefaultMutableTreeNode) selectionPaths[i].getLastPathComponent()).getUserObject();

	    key = model.getKey();
	    seen = model.getSeen();

	    if (mode == 0 || (seen && mode == 2) || !seen && mode == 1) {

		if (model instanceof ModelMovie)
		    db.setSeen(key, !seen);
		else
		    db.setSeenEpisode(key, !seen);

		model.setSeen(!seen);

		getSeen().setSelected(!seen);
	    }
	}
    }

    public void loadMenuLists(Database database) {

	if (database != null) {

	    String currentList = config.getCurrentList();

	    ArrayList listColumns = database.getListsColumnNames();
	    JRadioButtonMenuItem menuItem;

	    JMenu menuLists = getListMenu();
	    menuLists.removeAll();

	    ButtonGroup group = new ButtonGroup();
	    int indexCounter = 0;

	    while (!listColumns.isEmpty()) {

		menuItem = new JRadioButtonMenuItem((String) listColumns.get(0));
		menuItem.setActionCommand((String) listColumns.get(0));
		menuItem.addActionListener(new MovieManagerCommandLoadList());
		group.add(menuItem);
		menuLists.add(menuItem, indexCounter);

		if (currentList.equals(listColumns.get(0)))
		    menuItem.setSelected(true);

		listColumns.remove(0);
		indexCounter++;
	    }

	    /* Adds 'Show all' in the list */
	    menuItem = new JRadioButtonMenuItem("Show All", true); //$NON-NLS-1$
	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
	    menuItem.setActionCommand("Show All"); //$NON-NLS-1$
	    menuItem.addActionListener(new MovieManagerCommandLoadList());
	    group.add(menuItem);

	    menuLists.add(menuItem, indexCounter);
	}
    }

    File getAppletFile(String fileName) {

	try {
	    //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8");

	    fileName = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$

	    if (fileName.startsWith("/")) //$NON-NLS-1$
		fileName = fileName.replaceFirst("/", ""); //$NON-NLS-1$ //$NON-NLS-2$

	    //log.debug("fileName:" + fileName);
	    //log.debug("codebase:"+ _movieManager.applet.getCodeBase());

	    URL url = new URL(MovieManager.applet.getCodeBase(), fileName);

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

    public static URL getFile(String fileName) {

    	URL url = null;

	try {
	    //path = URLDecoder.decode(MovieManager.class.getResource(fileName).getPath(), "UTF-8");

	    if (!MovieManager.isApplet()) {
	    	/* handle paths with leading slash as absolute paths */
	    	if (fileName.startsWith("/")) {
		    url = new File(fileName).toURL();
	    	} else {
		    url = new File(getUserDir() + fileName).toURL();
	    	}
	    }
	    else {
		fileName = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$

		if (fileName.startsWith("/")) //$NON-NLS-1$
		    fileName = fileName.replaceFirst("/", ""); //$NON-NLS-1$ //$NON-NLS-2$

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
	    log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
	}
	return url;
    }



    public static boolean isApplet() {
	return applet != null ? true : false;
    }


     public void setMovieListHighlightEntireRow(boolean movieListHighlightEntireRow) {
	config.setMovieListHighlightEntireRow(movieListHighlightEntireRow);
	}


    /* Returns number of entries currently shown in the list */
    int getEntries() {
	return entries;
    }

    public void setAndShowEntries() {
	setAndShowEntries(MovieManager.getIt().getMoviesList().getModel().getChildCount(MovieManager.getIt().getMoviesList().getModel().getRoot()));
    }

    /**
     * Sets the entries variable and uppdates the showEntries Label with the new number
     **/
    public void setAndShowEntries(int entries) {

	this.entries = entries;

	String value;;

	if (entries < 10)
	    value = "    "; //$NON-NLS-1$
	else if (entries < 100)
	    value = "  "; //$NON-NLS-1$
	else
	    value = " "; //$NON-NLS-1$

	if (entries != -1) {
	    value += String.valueOf(entries);
	}

	showEntries.setText(value);
	showEntries.updateUI();
    }


    public ModelDatabaseSearch getFilterOptions() {

	ModelDatabaseSearch options = new ModelDatabaseSearch();

	options.setFilterCategory(config.getFilterCategory());

	if ("Movie Title".equals(config.getFilterCategory()) && config.getIncludeAkaTitlesInFilter()) //$NON-NLS-1$
	    options.setIncludeAkaTitlesInFilter(true);
	else
	    options.setIncludeAkaTitlesInFilter(false);

	options.setFilterString(getFilter().getText());
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

	return options;
    }


    public int getFontSize() {
	return fontSize;
    }

    void setFontSize(int fontSize) {
	this.fontSize = fontSize;
    }

    public int getMovieListWidth() {
	return movieListWidth;
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


    public static String getLineSeparator() {
	return System.getProperty("line.separator"); //$NON-NLS-1$
    }


    public static String getDirSeparator() {
	return File.separator;
    }

    public static boolean isMacAppBundle() {
    	return isMac() & (MovieManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().indexOf(".app/Contents/Resources") > -1);
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

    public Dimension getMainSize() {
	return MovieManager.getIt().getSize();
    }

    public Image getImage(String imageName) {
    	Image image = null;

	try {

	    if (MovieManager.isApplet()) {
		URL url = MovieManager.getIt().getClass().getResource(imageName);
    		image = MovieManager.applet.getImage(url);
	    }
	    else {
	    	String path = new String();
	    	if(!new File(imageName).exists()){
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


    /* Adds all the files ending in .jar to the classpath */
    public static void includeJarFilesInClasspath(String path) {

	URL url = MovieManager.getFile(path);

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
			ClassPathHacker.addFile(absolutePath);
		    log.debug(absolutePath+ " added to classpath"); //$NON-NLS-1$
		    }
		}
	    }
	}
	catch (Exception e) {
	    log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
	}
    }


    public static void main(String args[]) {

	EventQueue.invokeLater(new Runnable() {
		public final void run() {
		    
		    /* Disable HTTPClient logging output */
		    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
		    
		    File configFile = new File(getUserDir() + "/log4j.properties"); //$NON-NLS-1$
		                
		    if (configFile.isFile()) {
		    } else {
		        BasicConfigurator.configure();
		    }
		   
		    log = Logger.getRootLogger();
		    
		    /* Writes the date. */
		    log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
		    
		    _movieManager = new MovieManager();
		    
		    config = new MovieManagerConfig();
		    
		     /* Loads the config */
		    config.loadConfig();

		    /* Must be executed before the JFrame (MovieManager) object is created. */
		    if (config.getDefaultLookAndFeelDecorated()) {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		    }
		    
		    /* Includes the avallable jar files*/
		    includeJarFilesInClasspath("LookAndFeels"); //$NON-NLS-1$

		    if(!MovieManager.isMacAppBundle()) {
		    	includeJarFilesInClasspath("LookAndFeels");
		    } else {
			includeJarFilesInClasspath(System.getProperty("user.dir") + "/LookAndFeels");
		    }
		    
		    /* Installs the Look&Feels*/
		    if(isMac()) { 
                LookAndFeelManager.setupOSXLaF(); 
		    }
		    LookAndFeelManager.instalLAFs();
		    
		    /* Starts the MovieManager. */
		    MovieManager.getIt().setUp();
		    
		    if (MovieManager.isMac()) {
                LookAndFeelManager.macOSXRegistration();
		    }   
		    
		    /* Loads the database. */
		    MovieManager.getIt().loadDatabase();

		    getUserDir();
		}
	    });
    }


  }


