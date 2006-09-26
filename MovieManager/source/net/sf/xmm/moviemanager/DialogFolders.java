/**
 * @(#)DialogFolders.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.ShowGUI;

import java.awt.*;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;

import org.apache.log4j.Logger;


public class DialogFolders extends JDialog implements ItemListener {
    
    static Logger log = Logger.getRootLogger();
    
    private JCheckBox setPermanentDatabase;
   
    private JRadioButton relativeQueriesProgram;
    private JRadioButton relativeCoversProgram;
    private JRadioButton relativeDatabaseProgram;
    private JRadioButton relativeQueriesDatabase;
    private JRadioButton relativeCoversDatabase;
    
    private JCheckBox relativeQueriesEnabled;
    private JCheckBox relativeCoversEnabled;
    private JCheckBox relativeDatabaseEnabled;
    
    private JLabel optionQueries;
    private JLabel optionCovers;
    private JLabel optionDatabase;

    private JTextField textFieldCovers;
    private JTextField textFieldQueries;
    private JTextField textFieldLoadDatabase;
    private JTextField textFieldDatabase;
    
    
    /**
     * The Constructor.
     **/
    public DialogFolders() {
	/* Dialog creation...*/
	super(MovieManager.getIt());
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
    
	/* Dialog properties...*/
	setTitle("Folders");
	setModal(true);
	setResizable(true);
	
	/* Folders panel...*/
	JPanel panelFolders = new JPanel();
	panelFolders.setBorder(BorderFactory.createEmptyBorder(5,-3,0,-3));
	panelFolders.setLayout(new GridBagLayout());
	GridBagConstraints constraints;
        
	/*Covers*/
	JLabel labelCovers = new JLabel("Covers Folder");
	labelCovers.setFont(new Font(labelCovers.getFont().getName(),Font.PLAIN,labelCovers.getFont().getSize()));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelFolders.add(labelCovers,constraints);
    
	textFieldCovers = new JTextField(MovieManager.getConfig().getCoversFolder(),30);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	panelFolders.add(textFieldCovers,constraints);
	
	JButton buttonCovers = new JButton("Browse");
	buttonCovers.setToolTipText("Browse to get the desired Covers folder");
	buttonCovers.setActionCommand("Folders - Browse Covers");
	buttonCovers.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: " + event.getActionCommand());
		    executeCommandBrowse("Select Covers Directory");
		}});
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	panelFolders.add(buttonCovers,constraints); 
    
	/*Queries*/
	JLabel labelQueries = new JLabel("Queries Folder");
	labelQueries.setFont(new Font(labelQueries.getFont().getName(),Font.PLAIN,labelQueries.getFont().getSize()));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
        
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelFolders.add(labelQueries,constraints);
    
	textFieldQueries = new JTextField(MovieManager.getConfig().getQueriesFolder(),30);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.insets = new Insets(5,5,0,5);
	panelFolders.add(textFieldQueries,constraints);
	
	
	
	    
	JButton buttonQueries = new JButton("Browse");
	buttonQueries.setToolTipText("Browse to get the desired Queries folder");
	buttonQueries.setActionCommand("Folders - Browse Queries");
	buttonQueries.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: " + event.getActionCommand());
		    executeCommandBrowse("Select Queries Directory");
		}});
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 1;
       
	constraints.insets = new Insets(5,5,5,5);   
     	panelFolders.add(buttonQueries,constraints);
        
	/* Database */
	JLabel labelDatabase = new JLabel("Current Database");
	labelDatabase.setFont(new Font(labelDatabase.getFont().getName(),Font.PLAIN,labelDatabase.getFont().getSize()));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelFolders.add(labelDatabase,constraints);
    
	textFieldDatabase = new JTextField(MovieManager.getConfig().getDatabasePath(true), 30);
	textFieldDatabase.setEditable(false);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.insets = new Insets(5,5,5,5);
	panelFolders.add(textFieldDatabase,constraints);
    
	

	JLabel labelDatabaseType = new JLabel();
	if (MovieManager.getIt().getDatabase() instanceof DatabaseAccess)
	    labelDatabaseType.setText("  MS Access Database");
	else if (MovieManager.getIt().getDatabase() instanceof DatabaseHSQL)
	    labelDatabaseType.setText("     HSQL Database");
	else if (MovieManager.getIt().getDatabase() instanceof DatabaseMySQL)
	    labelDatabaseType.setText("     MySQL Database");
	else
	    labelDatabaseType.setText("     Database");

	labelDatabaseType.setFont(new Font(labelDatabase.getFont().getName(),Font.BOLD,labelDatabase.getFont().getSize()));
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 3;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelFolders.add(labelDatabaseType,constraints);
    
	

	JLabel labelLoadDatabase = new JLabel("Load Database:");
	labelLoadDatabase.setFont(new Font(labelLoadDatabase.getFont().getName(),Font.PLAIN, labelLoadDatabase.getFont().getSize()));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelFolders.add(labelLoadDatabase, constraints);
	
	textFieldLoadDatabase = new JTextField(MovieManager.getConfig().getDatabasePath(!MovieManager.getConfig().getDatabasePathPermanent()), 30);
	
	textFieldLoadDatabase.setEditable(false);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 4;
	constraints.insets = new Insets(5,5,5,5);
	panelFolders.add(textFieldLoadDatabase, constraints);
	
	setPermanentDatabase = new JCheckBox("Set Permanent");
	setPermanentDatabase.setToolTipText("Any databases opened after this is selected will not be saved to the config file");
	
	if (MovieManager.getConfig().getDatabasePathPermanent())
	    setPermanentDatabase.setSelected(true);
	
	setPermanentDatabase.addItemListener(this);
	
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 4;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelFolders.add(setPermanentDatabase, constraints);
	
	
	/* All stuff together... */
	JPanel all = new JPanel();
	all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
	all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
	all.add(panelFolders);
	
	
	JPanel panelOptions = new JPanel(new GridBagLayout());
	panelOptions.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
	
	JLabel optionTitle = new JLabel("Save paths relative to:");
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	// constraints.gridwidth = 3;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(optionTitle, constraints);
	
	optionCovers = new JLabel("Covers");
	optionCovers.setEnabled(false);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(optionCovers, constraints);
	
	optionQueries = new JLabel("Queries");
	optionQueries.setEnabled(false);
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 0;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(optionQueries, constraints);

	optionDatabase = new JLabel("Database");
	optionDatabase.setEnabled(false);
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 0;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(optionDatabase, constraints);
	
	JLabel optionProgramLocation = new JLabel("Program Location");
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(optionProgramLocation, constraints);
	
	relativeCoversProgram = new JRadioButton();
	relativeCoversProgram.setEnabled(false);
	relativeCoversProgram.setSelected(true);
	relativeCoversProgram.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeCoversProgram, constraints);
	
	relativeQueriesProgram = new JRadioButton();
	relativeQueriesProgram.setEnabled(false);
	relativeQueriesProgram.setSelected(true);
	relativeQueriesProgram.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 1;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeQueriesProgram, constraints);
	
	relativeDatabaseProgram = new JRadioButton();
	relativeDatabaseProgram.setEnabled(false);
	relativeDatabaseProgram.setSelected(true);
	relativeDatabaseProgram.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 1;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeDatabaseProgram, constraints);
	
	JLabel optionDatabaseLocation = new JLabel("Database Location");
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(optionDatabaseLocation, constraints);
	
	relativeCoversDatabase = new JRadioButton();
	relativeCoversDatabase.setEnabled(false);
	relativeCoversDatabase.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeCoversDatabase, constraints);
	
	relativeQueriesDatabase = new JRadioButton();
	relativeQueriesDatabase.setEnabled(false);
	relativeQueriesDatabase.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 2;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeQueriesDatabase, constraints);
	
	
	JLabel relativeEnable = new JLabel("Enable");
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelOptions.add(relativeEnable, constraints);
	
	relativeCoversEnabled = new JCheckBox();
	relativeCoversEnabled.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeCoversEnabled, constraints);
	
	relativeQueriesEnabled = new JCheckBox();
	relativeQueriesEnabled.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 3;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeQueriesEnabled, constraints);
	
	relativeDatabaseEnabled = new JCheckBox();
	relativeDatabaseEnabled.addItemListener(this);
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 3;
	constraints.insets = new Insets(2,3,2,3);
	constraints.anchor = GridBagConstraints.CENTER;
	panelOptions.add(relativeDatabaseEnabled, constraints);
	
	if ((MovieManager.getIt().getDatabase() instanceof DatabaseMySQL)) {
	    relativeQueriesEnabled.setEnabled(false);
	    relativeCoversEnabled.setEnabled(false);
	    relativeDatabaseEnabled.setEnabled(false);
	}
	
	/* Setting up groups */
	
	ButtonGroup coverGroup = new ButtonGroup();
	coverGroup.add(relativeCoversProgram);
	coverGroup.add(relativeCoversDatabase);
	
	ButtonGroup queriesGroup = new ButtonGroup();
	queriesGroup.add(relativeQueriesProgram);
	queriesGroup.add(relativeQueriesDatabase);
	
	ButtonGroup databaseGroup = new ButtonGroup();
	databaseGroup.add(relativeDatabaseProgram);
	
	int coverOption = MovieManager.getConfig().getUseRelativeCoversPath();
	
	if (coverOption != 0) {
	    relativeCoversEnabled.setSelected(true);
	    
	    if (coverOption == 1)
		relativeCoversDatabase.setSelected(true);
	    else
		relativeCoversProgram.setSelected(true);
	}
	
	int queriesOption = MovieManager.getConfig().getUseRelativeQueriesPath();
	
	if (queriesOption != 0) {
	    relativeQueriesEnabled.setSelected(true);
	    
	    if (queriesOption == 1)
		relativeQueriesDatabase.setSelected(true);
	    else
		relativeQueriesProgram.setSelected(true);
	}
	
	int databaseOption = MovieManager.getConfig().getUseRelativeDatabasePath();
	
	if (databaseOption != 0) {
	    relativeQueriesEnabled.setSelected(true);
	    relativeDatabaseProgram.setSelected(true);
	}
	
	
	/* Buttons panel...*/
	JPanel panelButtons = new JPanel();
	panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
	JButton buttonSave = new JButton("Save");
	buttonSave.setToolTipText("Save changes");
	buttonSave.setActionCommand("Folders - Save");
	buttonSave.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: " + event.getActionCommand());
		    executeCommandSave();
		}});
	panelButtons.add(buttonSave);
	JButton buttonCancel = new JButton("Cancel");
	buttonCancel.setToolTipText("Discard changes");
	buttonCancel.setActionCommand("Folders - Cancel");
	buttonCancel.addActionListener(new CommandDialogDispose(this));
	panelButtons.add(buttonCancel);
	
	JPanel bottomPanel = new JPanel(new BorderLayout());
	
	bottomPanel.add(panelOptions, BorderLayout.WEST);
	bottomPanel.add(panelButtons, BorderLayout.PAGE_END);
	
	/* Adds all and buttonsPanel... */    
	getContentPane().add(all, BorderLayout.NORTH);
	getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	
	/* Packs and sets location... */
	pack();
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	
    }

    /**
     * Returns the text in the covers textfield.
     **/
    protected String getCoversPath() {
	return textFieldCovers.getText();
    }

    /**
     * Returns the text in the queries textfield.
     **/
    protected String getQueriesPath() {
	return textFieldQueries.getText();
    }

    /**
     * Returns the covers textfield.
     **/
    protected JTextField getCovers() {
	return textFieldCovers;
    }

    /**
     * Returns the queries textfield.
     **/
    protected JTextField getQueries() {
	return textFieldQueries;
    }
    
    /**
     * Saves and exits...
     **/
    private void executeCommandSave() {
	/* Checks if the specified paths exist and if so sets the new folders... */
      
	File coversFolder;
	File queriesFolder;
	String coversPath = getCoversPath();
	String queriesPath = getQueriesPath();
	
	/* Relative covers path enabled */
	if (relativeCoversEnabled.isSelected()) {
	    
	    if (relativeCoversProgram.isSelected()) {
		
		if ((coversPath.indexOf(MovieManager.getUserDir()) == -1) && (!new File(MovieManager.getUserDir()+ coversPath).isDirectory())) {
		    DialogAlert alert = new DialogAlert(this, "Alert", "Current options demand that the covers directory must be located inside the install directory when using relative path");
		    //alert.setVisible(true);
		    ShowGUI.showAndWait(alert, true);
		    return;
		}
		
		if (coversPath.equals("") || !(coversFolder = new File(coversPath)).isDirectory()) {
		    if (coversPath.equals("") || !(coversFolder = new File(MovieManager.getUserDir()+ coversPath)).isDirectory()) {
			DialogAlert alert = new DialogAlert(this, "Alert","Covers directory does not exist");
			//alert.setVisible(true);
			ShowGUI.showAndWait(alert, true);
			return;
		    }
		}
		
		if (coversPath.indexOf(MovieManager.getUserDir()) != -1)
		    coversPath = coversPath.substring(MovieManager.getUserDir().length()+1, coversPath.length());
		
		MovieManager.getConfig().setUseRelativeCoversPath(2);
	    }
	    else {
		String dbPath = MovieManager.getConfig().getDatabasePath(true);
		dbPath = dbPath.substring(0, dbPath.lastIndexOf(MovieManager.getDirSeparator()));
		
		if ((coversPath.indexOf(dbPath) == -1) && (!new File(dbPath + MovieManager.getDirSeparator() + coversPath).isDirectory())) {
		    DialogAlert alert = new DialogAlert(this, "Alert", "Current options demand that the covers directory must be located relative to the database");
		    //alert.setVisible(true);
		    ShowGUI.showAndWait(alert, true);
		    return;
		}
		
		if (coversPath.indexOf(dbPath) != -1)
		    coversPath = coversPath.substring(dbPath.length()+1, coversPath.length());
		
		MovieManager.getConfig().setUseRelativeCoversPath(1);
	    }
	}
	else {
	    coversFolder = new File(coversPath);
	    
	    if(!coversFolder.isDirectory()) {
		DialogAlert alert = new DialogAlert(this, "Alert","Covers directory does not exist");
		//alert.setVisible(true);
		ShowGUI.showAndWait(alert, true);
		return;
	    }
	    coversPath = coversFolder.getAbsolutePath();
	    
	    MovieManager.getConfig().setUseRelativeCoversPath(0);
	}
	
	
	
	if (relativeQueriesEnabled.isSelected()) {
	    
	    if (relativeQueriesProgram.isSelected()) {
		
		if (queriesPath.indexOf(MovieManager.getUserDir()) == -1 && !(new File(MovieManager.getUserDir(), queriesPath)).isDirectory()) {
		    
		    DialogAlert alert = new DialogAlert(this, "Alert", "Queries directory must be located inside the install directory when using relative path");
		    //alert.setVisible(true);
		    ShowGUI.showAndWait(alert, true);
		    return;
		}
		
		if (queriesPath.equals("") || !(queriesFolder = new File(queriesPath)).isDirectory()) {
		    if (queriesPath.equals("") || !(queriesFolder = new File(MovieManager.getUserDir(), queriesPath)).isDirectory()) {
			DialogAlert alert = new DialogAlert(this, "Alert", "Queries directory does not exist");
			//alert.setVisible(true);
			ShowGUI.showAndWait(alert, true);
			return;
		    }
		}
		
		if (queriesPath.indexOf(MovieManager.getUserDir()) != -1)
		    queriesPath = queriesPath.substring(MovieManager.getUserDir().length()+1, queriesPath.length());
		
		MovieManager.getConfig().setUseRelativeQueriesPath(2);
	    }
	    else {
		String dbPath = MovieManager.getConfig().getDatabasePath(true);
		dbPath = dbPath.substring(0, dbPath.lastIndexOf(MovieManager.getDirSeparator()));
		
		if ((queriesPath.indexOf(dbPath) == -1) && (!new File(dbPath, queriesPath).isDirectory())) {
		    DialogAlert alert = new DialogAlert(this, "Alert", "Current options demand that the queries directory must be located relative to the database");
		    //alert.setVisible(true);
		    ShowGUI.showAndWait(alert, true);
		    return;
		}
		
		if (queriesPath.indexOf(dbPath) != -1)
		    queriesPath = queriesPath.substring(dbPath.length()+1, queriesPath.length());

		MovieManager.getConfig().setUseRelativeQueriesPath(1);
	    }
	}
	else {
	    queriesFolder = new File(queriesPath);
	    
	    if(!queriesFolder.isDirectory()) {
		DialogAlert alert = new DialogAlert(this, "Alert","Queries directory does not exist");
		//alert.setVisible(true);
		ShowGUI.showAndWait(alert, true);
		return;
	    }
	    queriesPath = queriesFolder.getAbsolutePath();
	    MovieManager.getConfig().setUseRelativeQueriesPath(0);
	}
	
	if (relativeDatabaseEnabled.isSelected()) {
	    
	    String databasePath = MovieManager.getConfig().getDatabasePath(true);
	    
	    if (databasePath.indexOf(MovieManager.getUserDir()) == -1) {
		DialogAlert alert = new DialogAlert(this, "Alert", "Database must be located inside the install directory when using relative path");
		//alert.setVisible(true);
		ShowGUI.showAndWait(alert, true);
		return;
	    }
	    else {
		MovieManager.getConfig().setUseRelativeDatabasePath(2);
	    }
	}
	else {
	    MovieManager.getConfig().setUseRelativeDatabasePath(0);
	}
	
	if (setPermanentDatabase.isSelected()) {
	    MovieManager.getConfig().setDatabasePathPermanent(true);
	    MovieManager.getConfig().setDatabasePath(textFieldLoadDatabase.getText());
	}
	else
	    MovieManager.getConfig().setDatabasePathPermanent(false);
	
	/*Sets cover and queries directory*/
	MovieManager.getConfig().setCoverAndQueriesPaths(coversPath, queriesPath);
	
	MovieManagerCommandSelect.execute();
	dispose();
    }
    
    
    public void itemStateChanged(ItemEvent event) {
	
	Object source = event.getItemSelectable();
	
	
	if (source.equals(setPermanentDatabase)) {
	    
	    String databaseType = MovieManager.getIt().getDatabase().getDatabaseType();
	    textFieldLoadDatabase.setText(databaseType+ ">" +textFieldDatabase.getText());
	}
	
	boolean value = false;

	/* Enable/Disable relative paths  */
	
	if (source.equals(relativeQueriesEnabled)) {
	    
	    if (relativeQueriesEnabled.isSelected())
		value = true;
	    else
		value = false;
	    
	    relativeQueriesProgram.setEnabled(value);
	    relativeQueriesDatabase.setEnabled(value);
	    optionQueries.setEnabled(value);
	}
	
	if (source.equals(relativeCoversEnabled)) {
	    
	    if (relativeCoversEnabled.isSelected())
		value = true;
	    else
		value = false;
	    
	    relativeCoversProgram.setEnabled(value);
	    relativeCoversDatabase.setEnabled(value);
	    optionCovers.setEnabled(value);
	}
	
	if (source.equals(relativeDatabaseEnabled)) {
	    
	    if (relativeDatabaseEnabled.isSelected())
		value = true;
	    else
		value = false;
	    
	    relativeDatabaseProgram.setEnabled(value);
	    optionDatabase.setEnabled(value);
	}
    }
    
    /**
     * Gets a folder and updates the textfield...
     **/
    private void executeCommandBrowse(String title) {
	JTextField textField;
	/* Gets the right JTextField. */
	if (title.equals("Select Covers Directory")) {
	    textField = getCovers();
	} else {
	    textField = getQueries();
	}
	
	/*The Oyoaha theme wouldn't set the file name in the name texField so a contructor accepting current dir and selection mode takes care of that*/
	ExtendedFileChooser fileChooser;
	
	if (title.equals("Select Covers Directory"))
	    fileChooser = new ExtendedFileChooser(MovieManager.getConfig().getCoversFolder(), ExtendedFileChooser.DIRECTORIES_ONLY);
	else
	    fileChooser = new ExtendedFileChooser(MovieManager.getConfig().getQueriesFolder(), ExtendedFileChooser.DIRECTORIES_ONLY);
	
	fileChooser.setDialogTitle(title);
	fileChooser.setApproveButtonText("Select");
	fileChooser.setApproveButtonToolTipText("Select this folder");
	fileChooser.setAcceptAllFileFilterUsed(false);
	
	int returnVal = fileChooser.showOpenDialog(MovieManager.getIt());
	if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
	    
	    /* Verifies that is a directory... */
	    if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().isDirectory()) {
		/* Sets the new dir... */
		textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
	    }
	    /* Sets the last path... */
	    MovieManager.getConfig().setLastMiscDir(fileChooser.getCurrentDirectory());
	}
    }
}
