/**
 * @(#)MovieManagerCommandConvertDatabase.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.WindowConstants;

import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;

import org.apache.log4j.Logger;


public class MovieManagerCommandConvertDatabase extends JPanel implements ActionListener{
    
    static Logger log = Logger.getRootLogger();
    
    Database newDatabase;
    ListModel movieListModel;
    ArrayList episodeList;
    boolean canceled = true;
    boolean done = false;
    boolean dbOpened = false;
    String filePath;
    JDialog dbConverter;
    int newDatabaseType = 1; /* 1 == Access, 2 == hsql*/
    
    public Database createNewDatabase() {
	
	try {
	    	    
	    if (newDatabaseType == 2) {
		/* Creates a new HSQL database... */
		newDatabase = new DatabaseHSQL(filePath);
		newDatabase.setUp();
		
		((DatabaseHSQL)newDatabase).createDatabaseTables();
		
		/* Adds extra info field names */
		ArrayList columnNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();
		for (int i = 0; i < columnNames.size(); i++)
		    newDatabase.addExtraInfoFieldName((String) columnNames.get(i));
	    
		/* Adds lists columns */
		columnNames = MovieManager.getIt().getDatabase().getListsColumnNames();
		for (int i = 0; i < columnNames.size(); i++)
		    newDatabase.addListsColumn((String) columnNames.get(i));
		
	    }
	    else {
		
		/* Creates a new MS Access database file... */
		File dataBaseFile = new File(filePath);
		
		if (!dataBaseFile.createNewFile()) {
		    throw new Exception("Cannot create database file.");
		}
		
		/* Copies the empty database file in the package to the new file... */
		byte[] data;
		InputStream inputStream;
		OutputStream outputStream;
		
		inputStream = new FileInputStream("Temp.mdb");
		outputStream = new FileOutputStream(dataBaseFile);
		data = new byte[inputStream.available()];
		
		while (inputStream.read(data) != -1)
		    outputStream.write(data);
		
		outputStream.close();
		inputStream.close();
		
		newDatabase = new DatabaseAccess(filePath);
		newDatabase.setUp();
		
		/* Adds extra info field names */
		ArrayList columnNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();
		for (int i = 0; i < columnNames.size(); i++)
		    newDatabase.addExtraInfoFieldName((String) columnNames.get(i));
	    
		/* Adds lists columns */
		columnNames = MovieManager.getIt().getDatabase().getListsColumnNames();
		for (int i = 0; i < columnNames.size(); i++)
		    newDatabase.addListsColumn((String) columnNames.get(i));
	    }
	    
	} catch (Exception e) {
	    log.error("", e);
	}
	
	return newDatabase;
    }
    
    
    void createAndShowGUI() {
	
	/* Owner, title, modal=true */
	dbConverter = new JDialog(MovieManager.getIt(), "Database converter", true);
	dbConverter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	final JComponent newContentPane = new DialogDatabaseConverter(newDatabase, movieListModel, episodeList, this);
        newContentPane.setOpaque(true);
        dbConverter.setContentPane(newContentPane);
	dbConverter.pack();
	dbConverter.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dbConverter.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		    
		    if (canceled) {
			deleteNewDatabase();
			dbConverter.dispose();
		    }		    
		    else if (done){
			if (!dbOpened)
			    finalizeDatabase();
			dbConverter.dispose();
		    }
		}
	    });
	
	/*Dispose on escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (canceled) {
			deleteNewDatabase();
			dbConverter.dispose();
		    }
		    else if (done){
			if (!dbOpened)
			    finalizeDatabase();
			dbConverter.dispose();
		    }
		}
	    };
	
	dbConverter.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	dbConverter.getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	MovieManager mm = MovieManager.getIt();
	
	setLocation((int) mm.getLocation().getX()+(mm.getWidth()-getWidth())/2,
		    (int) mm.getLocation().getY()+(mm.getHeight()-getHeight())/2);
	
	dbConverter.setLocation((int)mm.getLocation().getX()+(mm.getWidth()- dbConverter.getWidth())/2,
				(int)mm.getLocation().getY()+(mm.getHeight()- dbConverter.getHeight())/2);
	//dbConverter.setVisible(true);
	ShowGUI.show(dbConverter, true);
    }
    
    void finalizeDatabase() {
	if (newDatabase instanceof DatabaseHSQL)
	    ((DatabaseHSQL) newDatabase).shutDownDatabase("SHUTDOWN COMPACT;");
	newDatabase.finalizeDatabase();
    }
    
    public void deleteNewDatabase() {
	newDatabase.deleteDatabase();
    }
    
    public void loadDatabase() {
	MovieManager.getIt().setDatabase(newDatabase, true);
    }
    
    public void setCanceled(boolean canceled) {
	this.canceled = canceled;
    }
    
    public void setDone(boolean done) {
	this.done = done;
    }
    
    public void setDbOpened(boolean dbOpened) {
	this.dbOpened = dbOpened;
    }
    
    public void dispose() {
	//dbConverter.setVisible(false);
	ShowGUI.show(dbConverter, false);
    }
    
    protected String getFilePath() {
	
	/* Opens the Open dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	newDatabaseType = 1;
	
	if (MovieManager.getIt().getDatabase() instanceof DatabaseAccess) {
	    fileChooser.setFileFilter(new CustomFileFilter(new String[]{"mdb"},new String("MS Access Database File (*.mdb)")));
	    fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"properties", "script"},new String("HSQL Database Files (*.properties, *.script)")));
	}	
	else {
	    fileChooser.setFileFilter(new CustomFileFilter(new String[]{"properties", "script"},new String("HSQL Database Files (*.properties, *.script)")));
	    fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"mdb"},new String("MS Access Database File (*.mdb)")));
	}
	
	if (MovieManager.getConfig().getLastDatabaseDir() != null)
	    fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastDatabaseDir());
	
	fileChooser.setDialogTitle("Save new database as");
	fileChooser.setApproveButtonText("Save database");
	fileChooser.setApproveButtonToolTipText("Save new database");
	fileChooser.setDialogType(1);
	fileChooser.setAcceptAllFileFilterUsed(false);
	
	int returnVal = fileChooser.showDialog(MovieManager.getIt(), "Save new database");
	
	if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
	    /* Gets the path... */
	    String absolutePath = fileChooser.getSelectedFile().getAbsolutePath();
	    
	    if (fileChooser.getFileFilter().getDescription().equals("HSQL Database Files (*.properties, *.script)"))
		newDatabaseType = 2;
	    
	    if (newDatabaseType == 1) {
		if (!absolutePath.endsWith(".mdb"))
		    absolutePath += ".mdb";
	    }
	    else {
		if (absolutePath.endsWith(".properties"))
		    absolutePath = absolutePath.substring(0, absolutePath.length()-11);
		else if (absolutePath.endsWith(".script"))
		    absolutePath = absolutePath.substring(0, absolutePath.length()-7);
	    }
	    return absolutePath;
	}
	return null;
    }
    
    
    protected void execute() {
	
	
	if (!MovieManager.isWindows()) {
	    DialogAlert alert = new DialogAlert(MovieManager.getIt(), "Windows Only", "This function is available on windows only");
	    //alert.setVisible(true);
	    ShowGUI.show(alert, true);
	}
	else {
	    movieListModel = MovieManager.getIt().getDatabase().getMoviesList("Title");
	    episodeList = MovieManager.getIt().getDatabase().getEpisodeList("movieID");
	    
	    int listModelSize = movieListModel.getSize();
	    
	    if (listModelSize == 0) {
		DialogAlert alert = new DialogAlert(MovieManager.getIt(), "Empty Database", "The database is empty! ");
		//alert.setVisible(true);
		ShowGUI.show(alert, true);
	    }
	    else {
		filePath = getFilePath();
		
		if (filePath != null) {
		    createNewDatabase();
		    createAndShowGUI();
		}
	    }
	}
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: " + event.getActionCommand());
	execute();
    }
}
    
    
    
