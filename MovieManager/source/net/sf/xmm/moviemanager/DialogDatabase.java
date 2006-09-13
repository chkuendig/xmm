/**
 * @(#)DialogDatabase.java 1.0 29.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;


public class DialogDatabase extends JDialog implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    static private JTextField hsqlFilePath;
    static private JTextField accessFilePath;
    
    private JButton browseForHSQLFile;
    private JButton browseForAccessFile;
    
    private JButton buttonConfirm;
    private JButton buttonCancel;
    
    static private boolean newDatabase = false;
    

    /* MySQL */
    
    static private JTextField databaseNameField;
    static private JTextField hostTextField;
    static private JTextField portTextField;
    static private JTextField userNameTextField;
    static private JTextField passwordTextField;
    
    
    static protected JTabbedPane tabbedPane;
    
    protected JPanel all;
    
    static SimpleProgressBar progressBar;
    
    
    static private DialogDatabase dialogDatabase;
    
    public DialogDatabase(boolean mode) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	
	dialogDatabase = this;
	
	newDatabase = mode;
	
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e) {
		    dispose();
		}
	    };
	
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	if (newDatabase)
	    setTitle("New Database");
	else
	    setTitle("Open Database");
	
	setModal(true);
	setResizable(false);
	
	JList recentDatabases = new JList();
	
	JScrollPane dbList = new JScrollPane(recentDatabases);
	
	JPanel recentDBPanel = new JPanel();
	
	recentDBPanel.add(dbList);
	
	
	/* HSQL database */

	/* Label */
	JLabel hsqlLabel;
	
	if (newDatabase)
	    hsqlLabel = new JLabel("Create a new HSQL Database");
	else
	    hsqlLabel = new JLabel("Open an existing HSQL Database");
	
	JPanel hsqlLabelPanel = new JPanel();
	hsqlLabelPanel.add(hsqlLabel);
	
	/* HSQL file path */
	hsqlFilePath = new JTextField(27);
	hsqlFilePath.setText("");
	
	browseForHSQLFile = new JButton("Browse");
	browseForHSQLFile.setToolTipText("Browse for a HSQL Database");
	browseForHSQLFile.setActionCommand("Browse HSQL");
	browseForHSQLFile.addActionListener(this);
	
	JPanel hsqlPathPanel = new JPanel();
	hsqlPathPanel.setLayout(new FlowLayout());
	hsqlPathPanel.add(hsqlFilePath);
	hsqlPathPanel.add(browseForHSQLFile);
	
	hsqlPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," HSQL Database Path "), BorderFactory.createEmptyBorder(0,5,0,5))));
	
	JPanel hsqlPanel = new JPanel(new BorderLayout());
	hsqlPanel.add(hsqlLabelPanel, BorderLayout.NORTH);
	hsqlPanel.add(hsqlPathPanel, BorderLayout.SOUTH);
	
	
	/* MySQL Database */
	JLabel mysqlLabel;
	
	if (newDatabase)
	    mysqlLabel = new JLabel("Create a new MySQL Database");
	else
	    mysqlLabel = new JLabel("Open an existing MySQL Database");
	
	JPanel mysqlLabelPanel = new JPanel();
	mysqlLabelPanel.add(mysqlLabel);
	
	
	JLabel databaseNameLabel = new JLabel("Database name:");
	databaseNameField = new JTextField(10);
	databaseNameField.setText("");
	
	JPanel databaseNamePanel = new JPanel();
	databaseNamePanel.add(databaseNameLabel);
	databaseNamePanel.add(databaseNameField);
	
	JLabel hostLabel = new JLabel("Host address:");
	hostTextField = new JTextField(15);
	hostTextField.setText("");
	
	JPanel hostPanel = new JPanel();
	hostPanel.add(hostLabel);
	hostPanel.add(hostTextField);
	
	JLabel portLabel = new JLabel("Port:");
	portTextField = new JTextField(4);
	
	portTextField.setDocument(new DocumentRegExp("(\\d)*",8));
	portTextField.setText("3306");

	JPanel portPanel = new JPanel();
	portPanel.add(portLabel);
	portPanel.add(portTextField);
	
	JPanel mysqlServerPanel = new JPanel(new GridBagLayout());
	
	GridBagConstraints constraints;
	
	/* database name */
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets(1,5,1,5);
	mysqlServerPanel.add(databaseNameLabel,constraints);
	
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets(1,5,1,5);
	mysqlServerPanel.add(databaseNameField,constraints);
	
	
	/* host */
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets(1,5,1,5);
	mysqlServerPanel.add(hostLabel,constraints);
	
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets(1,5,1,5);
	mysqlServerPanel.add(hostTextField,constraints);
	
	/* port */
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,1,1,1);
	constraints.anchor = GridBagConstraints.EAST;
	mysqlServerPanel.add(portPanel,constraints);

	JLabel userNameLabel = new JLabel("Username:");
	userNameTextField = new JTextField(7);
	userNameTextField.setText("");
	
	JPanel userNamePanel = new JPanel();
	userNamePanel.add(userNameLabel);
	userNamePanel.add(userNameTextField);
	
	JLabel passwordLabel = new JLabel("Password:");
	passwordTextField = new JTextField(7);
	passwordTextField.setText("");
	
	JPanel passwordPanel = new JPanel() ;
	passwordPanel.add(passwordLabel);
	passwordPanel.add(passwordTextField);
	
	JPanel authenticationPanel = new JPanel(new GridBagLayout());
	authenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Authentication "), BorderFactory.createEmptyBorder(0,5,5,5)));
	
	
	/* username */
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.LAST_LINE_START;
	authenticationPanel.add(userNamePanel, constraints);
	
	/* password */
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.LAST_LINE_END;
	authenticationPanel.add(passwordPanel,constraints);
	
	
	/* MySQL end */
	
	JPanel mysqlOptionPanel = new JPanel(new GridLayout(0, 1));
	mysqlOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," MySQL Settings "), BorderFactory.createEmptyBorder(0,5,5,5)));
	
	mysqlOptionPanel.add(mysqlServerPanel);
	mysqlOptionPanel.add(authenticationPanel);
	
	JPanel mysqlPanel = new JPanel(new BorderLayout());
	mysqlPanel.add(mysqlLabelPanel, BorderLayout.NORTH);
	mysqlPanel.add(mysqlOptionPanel, BorderLayout.CENTER);
	
	
	/* MS Access Database */
	
	JLabel accessLabel;
	
	if (newDatabase)
	    accessLabel = new JLabel("Create a new MS Access database");
	else
	    accessLabel = new JLabel("Open an existing MS Access database");
	
	JPanel accessLabelPanel = new JPanel();
	accessLabelPanel.add(accessLabel);
	
	/* ms Access database */
	accessFilePath = new JTextField(27);
	accessFilePath.setText("");
	
	browseForAccessFile = new JButton("Browse");
	browseForAccessFile.setToolTipText("Browse for a MS Access database");
	browseForAccessFile.setActionCommand("Browse MS Access");
	browseForAccessFile.addActionListener(this);
	
	JPanel accessPathPanel = new JPanel();
	accessPathPanel.setLayout(new FlowLayout());
	accessPathPanel.add(accessFilePath);
	accessPathPanel.add(browseForAccessFile);
	
	accessPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," MS Access Database Path "), BorderFactory.createEmptyBorder(0,0,0,0))));
	
	JPanel accessPanel = new JPanel(new BorderLayout());
	accessPanel.add(accessLabelPanel, BorderLayout.NORTH);
	accessPanel.add(accessPathPanel, BorderLayout.SOUTH);
	
	
	/* Tabbed pane */
	tabbedPane = new JTabbedPane();
	
	tabbedPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	tabbedPane.add("HSQL Database", hsqlPanel);
	
	if (MovieManager.isWindows())
	    tabbedPane.add("MS Access Database", accessPanel);
	
	tabbedPane.add("MySQL Database", mysqlPanel);
	
	/* Buttons */
	if (newDatabase)
	    buttonConfirm = new JButton("Create Database");
	else
	    buttonConfirm = new JButton("Open Database");
	
	buttonConfirm.setToolTipText("Save info");
	buttonConfirm.setActionCommand("DialogAddMultipleMovies - OK");
	buttonConfirm.addActionListener(this);
	
	buttonCancel = new JButton("Cancel");
	buttonCancel.setToolTipText("Leave without saving");
	buttonCancel.setActionCommand("DialogAddMultipleMovies - Cancel");
	buttonCancel.addActionListener(this);
	
	
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.add(buttonConfirm);
	buttonPanel.add(buttonCancel);
	
	all = new JPanel();
	all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
	
	all.add(tabbedPane);
	all.add(buttonPanel);
	
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
	getContentPane().add(all,BorderLayout.NORTH);
	/* Packs and sets location... */
	pack();
	
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	}
    
    
    /*Opens a filechooser and returns the absolute path to the selected file*/
    private String executeCommandGetFile(int databaseMode, File currentDir) {
	
	/* Opens the Open dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser(currentDir);
	try {
	    fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);
	    
	    if (databaseMode == 0) {
		fileChooser.setFileFilter(new CustomFileFilter(new String[]{"properties", "script", "lck"},new String("HSQL Database Files (*.properties, *.script, *.lck)")));
	    } 
	    else if (databaseMode == 1) {
		fileChooser.setFileFilter(new CustomFileFilter(new String[]{"mdb"},new String("MS Access Database Files (*.mdb)")));
	    }
	    
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    fileChooser.requestFocusInWindow();
	    fileChooser.setFocusTraversalKeysEnabled(false);
	    
	    int returnVal;
	    
	    if (newDatabase) {
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileAlreadyExistWarningMessage("Database ");
		
		fileChooser.setApproveButtonText("Create Database");
		fileChooser.setApproveButtonToolTipText("Create new Database");
		fileChooser.setDialogTitle("Create new Database");
		
		returnVal = fileChooser.showSaveDialog(MovieManager.getIt());
	    }
	    else {
		fileChooser.setApproveButtonText("Open Database");
		fileChooser.setApproveButtonToolTipText("Select database file");
		fileChooser.setDialogTitle("Open existing Database");
		
		returnVal = fileChooser.showOpenDialog(MovieManager.getIt());
	    }
	    
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		
		/* Gets the path... */
		String filepath = fileChooser.getSelectedFile().getAbsolutePath();
		
		if (!newDatabase && !(new File(filepath).exists())) {
		    return "";
		}
		
		/* MS Access */
		if (databaseMode == 1) {
		    if (!filepath.endsWith(".mdb"))
			filepath += ".mdb";
		}
		    
		return filepath;
	    }
	}
	catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	}
	
	return "";
    }
    
    /*Saves the options to the MovieManager object*/
    void executeSave() {
	;
    }
    
    protected int getImportMode() {
	return tabbedPane.getSelectedIndex();
    }
    
    
    /* Returns the path for each database type */
    static protected String getPath() {
	
	String type = getType();
	
	if (type.equals("MSAccess"))
	    return accessFilePath.getText();
	else if (type.equals("HSQL"))
	    return hsqlFilePath.getText();
	else if (type.equals("MySQL"))
	    return createMySQLPath("");
	
	return "";
    }
    
    
    /*Returns the string in the path textfield*/
    static protected String getType() {
	
	String type = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
	
	if (type.equals("MS Access Database"))
	    return "MSAccess";
	else if (type.equals("HSQL Database"))
	    return "HSQL";
	else if (type.equals("MySQL Database"))
	    return "MySQL";
	
	return ""; 
    }
    
    
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: " + event.getActionCommand());
	
	if (event.getSource().equals(browseForHSQLFile)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    
	    String ret = executeCommandGetFile(0, MovieManager.getConfig().getLastDatabaseDir());
	    if (!ret.equals("")) {
		hsqlFilePath.setText(ret);
		executeConfirm();
	    }
	}
	
	if (event.getSource().equals(browseForAccessFile)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    String ret = executeCommandGetFile(1, MovieManager.getConfig().getLastDatabaseDir());
	    
	    if (!ret.equals("")) {
		accessFilePath.setText(ret);
		executeConfirm();
	    }
	}

	if (event.getSource().equals(buttonConfirm)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    
	    executeConfirm();
	}
	
	if (event.getSource().equals(buttonCancel)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    dispose();
	}
    }
    
    protected void executeConfirm() {
	
	String databaseType = getType();
	    
	if (databaseType.equals("MySQL")) {
		
	    if (databaseNameField.getText().equals("")) {
		DialogAlert alert = new DialogAlert("Database Name", "A database name need to be specified");
		alert.setVisible(true);
		return;
	    }
	    if (hostTextField.getText().equals("")) {
		DialogAlert alert = new DialogAlert("Host address", "Host address need to be specified");
		alert.setVisible(true);
		return;
	    }
		
	    if (portTextField.getText().equals("")) {
		DialogAlert alert = new DialogAlert("Port", "A port need to be specified");
		alert.setVisible(true);
		return;
	    }
	}
	else if (databaseType.equals("HSQL")) {
	    
	    if (hsqlFilePath.getText().equals("")) {
		DialogAlert alert = new DialogAlert("Database Path", "The database path cannot be empty!");
		alert.setVisible(true);
		return;
	    }
	}
	else {
	    if (accessFilePath.getText().equals("")) {
		DialogAlert alert = new DialogAlert("Database Path", "The database path cannot be empty!");
		alert.setVisible(true);
		return;
	    }
	}
	
	executeSave();
	    
	 final SwingWorker worker = new SwingWorker() {
		 public Object construct() {
		     
		    try {
			Thread.currentThread().setPriority(5);
			Database database = connectToDatabase(null);
			
			if (database != null) {
			
			    if (!database.setUp()) {
				progressBar.close();
				showDatabaseMessage(dialogDatabase, database, null);
			    }
			    
			    if (database != null && database.isInitialized()) {
				
				MovieManager.getConfig().setCurrentList("Show All");
				
				/* Loads the database... */
				updateProgress(progressBar, "Retrieving movie list...");
				MovieManager.getIt().setDatabase(database, true);
				
				if (database.isSetUp()) {
				    progressBar.dispose();
				    dispose();
				}
			    }
			    
			    /* Sets the last path... */
			    if ((!MovieManager.isApplet()) && new File(getPath()).exists())
				MovieManager.getConfig().setLastDatabaseDir(new File(getPath()).getParentFile());
			}
			progressBar.close();
		    }
		    catch (Exception e) {
			;
		    }
		    return this;
		}
	    };
	
	progressBar = new SimpleProgressBar(this, true, worker);
	worker.start();
	
	progressBar.setVisible(true);
    }
    
    
    static void updateProgress(final SimpleProgressBar progressBar, final String str) {
	
	Runnable updateProgress = new Runnable() {
		public void run() {
		    try {
			progressBar.setString(str);
			
		    } catch (Exception e) {
			log.error(e.getMessage());
		    }
		}};
	SwingUtilities.invokeLater(updateProgress);
    }
    
    
    static protected Database connectToDatabase(String path) {
	
	if (getType().equals("MySQL"))
	    updateProgress(progressBar, "Connecting to database");
	else
	    updateProgress(progressBar, "Creating connection");
	    
	String databaseType = getType();
	Database database = null;
	
	if (path == null || "".equals(path))
	    path = getPath();
	
	/* New Database */
	if (newDatabase) {
	    database = createNewDatabase(databaseType);
	}
	else {
	    /* open Database */
	    if (databaseType.equals("MySQL")) {
		database = new DatabaseMySQL(path);
	    }
	    else if (!new File(path).exists()) {
		DialogAlert alert = new DialogAlert("Error", "File does not exist");
		alert.setVisible(true);
		return null;
	    }
	    else if (databaseType.equals("MSAccess")) {
		if (!path.endsWith(".mdb"))
		    path += ".mdb";
		    
		database = new DatabaseAccess(path);
	    }
	    else if (databaseType.equals("HSQL")) {
		    
		if (path.endsWith(".properties") || path.endsWith(".script") || path.endsWith(".lck"))
		    path = path.substring(0, path.lastIndexOf("."));
		    
		try {
		    database = new DatabaseHSQL(path);
			
		} catch (Exception e) {
		    log.error("Exception:"+ e);
		    return null;
		}
	    }
	}
	
	if (database != null) {
	    database.setUp();
	    
	    if (database.isSetUp()) {
		updateProgress(progressBar, "Successfully connected to database");
	    }
	}
	return database;
    }
    
    /**
     * Creates a new database and loads it...
     **/
    static protected Database createNewDatabase(String databaseType) {
	
	Database database = null;
	
	try {
	    
	    String path = getPath();
	    
	    String parentPath = path.substring(0, path.lastIndexOf(File.separator) +1);
	    
	    if (!databaseType.equals("MySQL")) {
		/* Creates the covers folder... */
		File coversDir = new File(parentPath + "Covers");
		if (!coversDir.exists() && !coversDir.mkdir()) {
		    throw new Exception("Cannot create the covers directory.");
		}
		/* Creates the queries folder... */
		File queriesDir = new File(parentPath + "Queries");
		if (!queriesDir.exists() && !queriesDir.mkdir()) {
		    throw new Exception("Cannot create the queries directory.");
		}
	    }
	    
	    /* Creates a new HSQL database... */
	    if (databaseType.equals("HSQL")) {
		database = new DatabaseHSQL(path);
		database.setUp();
		
		if (database.isSetUp()) {
		    updateProgress(progressBar, "Creating database...");
		    ((DatabaseHSQL)database).createDatabaseTables();
		}	
	    }
	    
	    /* Creates a new MySQL database... */
	    else if (databaseType.equals("MySQL")) {
		
		boolean success = true;
		
		database = new DatabaseMySQL(path);
		database.setUp();
		
		/* Could not connect to the database */
		if (!database.isSetUp()) {
		    
		    String message = database.getErrorMessage();
		    
		    /* If the database doesn't already exists, a connection must be made through a default database,
		       tries "mysql" and "information_schema" */
		    if (message.indexOf("denied") != -1 || (message.indexOf("Unknown") != -1)) {
			success = false;
			
			/* Default mysql database */
			database = new DatabaseMySQL(createMySQLPath("mysql"));
			database.setUp();
		    
			if (!database.isSetUp()) {
			    /* Default mysql database */
			    database = new DatabaseMySQL(createMySQLPath("information_schema"));
			    database.setUp();
			}
		    
			if (!database.isSetUp()) {
			    progressBar.close();
			    showDatabaseMessage(dialogDatabase, database, "Unknown database");
			}
			else
			    success = true;
		    }
		    
		    /* If a connection is successfully established - creating database*/
		    
		    if (success && database.isSetUp()) {
			
			/* Creating the database */
			if (((DatabaseMySQL) database).createDatabase(databaseNameField.getText()) == 1) {
			    database.finalize();
			    database = new DatabaseMySQL(getPath());
			    database.setUp();
			}
			else {
			    success = false;
			    
			    progressBar.close();
			    showDatabaseMessage(dialogDatabase, database, "create database denied");
			}
		    }
		}
		
		/* A connection to the given database is now established */
		if (success) {
		    if (((DatabaseMySQL) database).createDatabaseTables() == -1) {
		        success = false;
			
			progressBar.close();
			showDatabaseMessage(dialogDatabase, database, null);
		    }
		}
		
		if (!success) {
		    return null;
		}
		
		updateProgress(progressBar, "Successfully created new database");
	    }
	    else {
		/* Creates the MS Access database file... */
		File databaseFile = new File(path);
		if (!databaseFile.createNewFile()) {
		    throw new Exception("Cannot create database file.");
		}
		
		/* Copies the empty database file in the package to the new file... */
		byte[] data;
		InputStream inputStream;
		OutputStream outputStream;
		
		inputStream = new FileInputStream("Temp.mdb");
		outputStream = new FileOutputStream(databaseFile);
		data = new byte[inputStream.available()];
		while (inputStream.read(data)!=-1) {
		    outputStream.write(data);
		}
		outputStream.close();
		inputStream.close();
		
		database = new DatabaseAccess(path);
		database.setUp();
	    }	
	    
	    /* Sets the folders path in the database... */
	    if (database.setFolders(parentPath + "Covers", parentPath + "Queries") != 1) {
		throw new Exception("Could not set the covers and queries paths in the database.");
	    }
	    
	    /* Closes the open database if any... */
	    if (MovieManager.getIt().getDatabase() != null) {
		MovieManager.getIt().getDatabase().finalize();
	    }
	} catch (Exception e) {
	    log.error("Exception: "+ e);
	    DialogAlert alert = new DialogAlert("Database creation failed!",e.getMessage());
	    alert.setVisible(true);
	}
	
	return database;
    }
    
    static String createMySQLPath(String optionalDatabase) {
	
	String mysqlPath = hostTextField.getText();
	
	if (!portTextField.getText().equals(""))
	    mysqlPath += ":" + portTextField.getText();
	
	if (!optionalDatabase.equals(""))
	    mysqlPath += "/" + optionalDatabase + "?";
	else
	    mysqlPath += "/" + databaseNameField.getText() + "?";
	
	if (!userNameTextField.getText().equals("")) {
	    mysqlPath += "user=" + userNameTextField.getText();
	    
	    if (!passwordTextField.getText().equals(""))
		mysqlPath += "&password=" + passwordTextField.getText();
	}
	
	return mysqlPath;
    }
    

    public static void showDatabaseMessage(Window parent, Database _database, String msg) {
	
	String title = "";
	String message = "";;
	
	message = _database.getErrorMessage();
	_database.resetError();
	
	if (msg != null && !msg.equals(""))
	    message = msg;
	    
	if (message.indexOf("Connection refused") != -1 && _database instanceof DatabaseMySQL) {
	    message = "Could not connect to database (Connection refused).";
	    title = "Connection alert";
	}
	else if (message.indexOf("access denied (java.net.SocketPermission") != -1 && _database instanceof DatabaseMySQL && MovieManager.isApplet()) {
	    message = "The program has executed code that need the jar file to be signed (Access denied).";
	    title = "Code execution error";
	}
	else if (message.indexOf("java.net.UnknownHostException") != -1 && _database instanceof DatabaseMySQL) {
	    message = "The host is unknown.";
	    title = "Connection alert";
	}
	else if (message.indexOf("Connection timed out") != -1 && _database instanceof DatabaseMySQL) {
	    message = "<html> Connection timed out...<br>Make sure you have network access and that the IP and port is correct.</html>";
	    title = "Connection alert";
	}
	else if (message.indexOf("CREATE command denied") != -1) {
	    String tmp = "You do not have permission to create database tables.";
	    message = "<html>"+ message + "<br>"+ tmp +"</html>";
	    title = "Database creation failed";
	}
	else if (message.indexOf("Access denied") != -1 && _database instanceof DatabaseMySQL) {
	    message = "<html>"+ message + "<br>Access was denied, permission not sufficient.</html>";
	    title = "Access denied";
	}
	else if (message.indexOf("Authentication failed") != -1 && _database instanceof DatabaseMySQL) {
	    message = "<html>"+ message + "<br>The authentication process failed.</html>";
	    title = "Authentication failed";
	}
	else if (message.indexOf("Unknown database") != -1 && _database instanceof DatabaseMySQL) {
	    message = "<html> " + message + "...<br>To be able to connect to the MySQL server<br>the database must already exist.</html>";
	    title = "Connection attempt failed";
	}
	else if (message.equals("org.hsqldb.jdbcDriver")) {
	    title = "Failed to load the HSQL database driver";
	    message = "<html> The HSQL database driver should be placed in the \"lib/drivers\" directory <br> or in any other directory included in the classpath</html>";
	}
    else if (message.equals("The database is already in use by another process")) {
        message = "<html>The database is already in use by another process.</html>";
        title = "Connection attempt failed";
    }
	else if (message.indexOf("settings' doesn't exist") != -1) {
	    title = "Database error";
	    message = "<html>" + message + " <br> The database does not contain the necessary tables for MeD's Movie Manager to function properly." ;
	}
	else if (message.indexOf("already exists") != -1) {
	    title = "Database error";
	    message = "<html> Table creation was denied. <br>" + message;
	}
	
	if (message.equals("Connection reset")) {
	    
	    MovieManager.getIt().setDatabaseComponentsEnable(false);
	    
	    DialogQuestion question = new DialogQuestion("Connection reset", "<html>The connection to the MySQL server has been reset.<br>"+
							 "Reconnect now?</html>");
	    question.setVisible(true);
		
	    if (question.getAnswer()) {
		Database db = connectToDatabase(_database.getPath());
		    
		if (db != null && _database.isSetUp()) {
		    updateProgress(progressBar, "Retrieving movie list...");
		    MovieManager.getIt().setDatabase(_database, true);
		    return;
		}
		else {
		    //swingWorker.interrupt();
		    progressBar.close();
		    showDatabaseMessage(parent, _database, null);
		    //dialogDatabase.setVisible(true);
		    return;
		}
	    }
	}
	    
	if (message.equals("MySQL server is out of space")) {
	    title = "Server Out of space";
	    message = "The MySQL server is out of space";
	}
	    
	// else if (message.indexOf("Access denied for user") != -1)
	// 		alertMessage = message;
	    
	if (!message.equals("")) {
		
	    Dialog alert;
            
            if (parent instanceof Frame)
                alert = new DialogAlert((Frame) parent, title, message);
            else
                alert = new DialogAlert((Dialog) parent, title, message);
                
		alert.setVisible(true);
	}
    }
}
