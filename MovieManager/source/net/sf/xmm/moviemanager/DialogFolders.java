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
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.GUIUtil;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;

import org.apache.log4j.Logger;


public class DialogFolders extends JDialog implements ItemListener, DocumentListener {
    
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
    
    private Color invalidPath = new Color(233, 180, 180);
    private boolean validCoversDirectory = false;
    private boolean validQueriesDirectory = false;
    
    /**
     * The Constructor.
     **/
    public DialogFolders() {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
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
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        /* Dialog properties...*/
        setTitle(Localizer.getString("DialogFolders.title")); //$NON-NLS-1$
        setModal(true);
        setResizable(false);
        
        boolean disableCoverAndQueries = !MovieManager.getConfig().getStoreCoversLocally() && (MovieManager.getIt().getDatabase() instanceof DatabaseMySQL);
        
        /* Folders panel...*/
        JPanel panelFolders = new JPanel();
        panelFolders.setBorder(BorderFactory.createEmptyBorder(5,-3,0,-3));
        panelFolders.setLayout(new GridBagLayout());
        GridBagConstraints constraints;
        
        /*Covers*/
        JLabel labelCovers = new JLabel(Localizer.getString("DialogFolders.label-covers")); //$NON-NLS-1$
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
        textFieldCovers.getDocument().addDocumentListener(this);
        
		// When using MySQL, no need to change this unless storecoverslocally is enabled
		if (disableCoverAndQueries)	 
			textFieldCovers.setEditable(false);

        JButton buttonCovers = new JButton(Localizer.getString("DialogFolders.browse-covers")); //$NON-NLS-1$
        buttonCovers.setToolTipText(Localizer.getString("DialogFolders.browse-covers-tooltip")); //$NON-NLS-1$
        buttonCovers.setActionCommand("Folders - Browse Covers"); //$NON-NLS-1$
        buttonCovers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandBrowse(Localizer.getString("DialogFolders.selectCoversDir")); //$NON-NLS-1$
            }});
        
        if (disableCoverAndQueries)
        	buttonCovers.setEnabled(false);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.insets = new Insets(5,5,5,5);
        panelFolders.add(buttonCovers,constraints); 
        
        /*Queries*/
        JLabel labelQueries = new JLabel(Localizer.getString("DialogFolders.label-queries")); //$NON-NLS-1$
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
        textFieldQueries.getDocument().addDocumentListener(this);
        
        if (disableCoverAndQueries)
			textFieldQueries.setEditable(false);

        JButton buttonQueries = new JButton(Localizer.getString("DialogFolders.browse-queries")); //$NON-NLS-1$
        buttonQueries.setToolTipText(Localizer.getString("DialogFolders.browse-queries-tooltip")); //$NON-NLS-1$
        buttonQueries.setActionCommand("Folders - Browse Queries"); //$NON-NLS-1$
        buttonQueries.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandBrowse(Localizer.getString("DialogFolders.selectQueriesDir")); //$NON-NLS-1$
            }});
        
        if (disableCoverAndQueries)
        	buttonQueries.setEnabled(false);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 1;
        constraints.insets = new Insets(5,5,5,5);   
        panelFolders.add(buttonQueries,constraints);
        
        /* Database */
        JLabel labelDatabase = new JLabel(Localizer.getString("DialogFolders.current-database")); //$NON-NLS-1$
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
            labelDatabaseType.setText("  MS Access Database"); //$NON-NLS-1$
        else if (MovieManager.getIt().getDatabase() instanceof DatabaseHSQL)
            labelDatabaseType.setText("     HSQL Database"); //$NON-NLS-1$
        else if (MovieManager.getIt().getDatabase() instanceof DatabaseMySQL)
            labelDatabaseType.setText("     MySQL Database"); //$NON-NLS-1$
        else
            labelDatabaseType.setText(Localizer.getString("DialogFolders.database-label")); //$NON-NLS-1$
        
        labelDatabaseType.setFont(new Font(labelDatabase.getFont().getName(),Font.BOLD,labelDatabase.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelDatabaseType,constraints);
        
        
        
        JLabel labelLoadDatabase = new JLabel(Localizer.getString("DialogFolders.load-database")); //$NON-NLS-1$
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
        
        setPermanentDatabase = new JCheckBox(Localizer.getString("DialogFolders.set-permanent")); //$NON-NLS-1$
        setPermanentDatabase.setToolTipText(Localizer.getString("DialogFolders.set-permanent-tooltip")); //$NON-NLS-1$
        
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
        
        JLabel optionTitle = new JLabel(Localizer.getString("DialogFolders.save-paths-relative-to")); //$NON-NLS-1$
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        // constraints.gridwidth = 3;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionTitle, constraints);
        
        optionCovers = new JLabel(Localizer.getString("DialogFolders.covers")); //$NON-NLS-1$
        optionCovers.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionCovers, constraints);
        
        optionQueries = new JLabel(Localizer.getString("DialogFolders.queries")); //$NON-NLS-1$
        optionQueries.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionQueries, constraints);
        
        optionDatabase = new JLabel(Localizer.getString("DialogFolders.database")); //$NON-NLS-1$
        optionDatabase.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionDatabase, constraints);
        
        JLabel optionProgramLocation = new JLabel(Localizer.getString("DialogFolders.program-location")); //$NON-NLS-1$
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
        
        JLabel optionDatabaseLocation = new JLabel(Localizer.getString("DialogFolders.database-location")); //$NON-NLS-1$
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
        
        
        JLabel relativeEnable = new JLabel(Localizer.getString("DialogFolders.enable")); //$NON-NLS-1$
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
        JButton buttonSave = new JButton(Localizer.getString("DialogFolders.save")); //$NON-NLS-1$
        buttonSave.setToolTipText(Localizer.getString("DialogFolders.save-changes")); //$NON-NLS-1$
        buttonSave.setActionCommand("Folders - Save"); //$NON-NLS-1$
        buttonSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandSave();
            }});
        panelButtons.add(buttonSave);
        JButton buttonCancel = new JButton(Localizer.getString("DialogFolders.cancel")); //$NON-NLS-1$
        buttonCancel.setToolTipText(Localizer.getString("DialogFolders.cancel-tooltip")); //$NON-NLS-1$
        buttonCancel.setActionCommand("Folders - Cancel"); //$NON-NLS-1$
        buttonCancel.addActionListener(new CommandDialogDispose(this));
        panelButtons.add(buttonCancel);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        bottomPanel.add(panelOptions, BorderLayout.WEST);
        bottomPanel.add(panelButtons, BorderLayout.PAGE_END);
        
        /* Adds all and buttonsPanel... */    
        getContentPane().add(all, BorderLayout.NORTH);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        processPathValidation("Covers&Queries");
        
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
        
    	//validQueriesDirectory
    	
        File coversFolder;
        File queriesFolder;
        String coversPath = getCoversPath();
        String queriesPath = getQueriesPath();
        
        /* Relative covers path enabled */
        if (relativeCoversEnabled.isSelected()) {
            
        	// Program location 
            if (relativeCoversProgram.isSelected()) {
                
                if (coversPath.indexOf(FileUtil.getUserDir()) != -1)
                    coversPath = coversPath.substring(FileUtil.getUserDir().length()+1, coversPath.length());
                
                MovieManager.getConfig().setUseRelativeCoversPath(2);
            }
            // Database
            else {
                String dbPath = MovieManager.getConfig().getDatabasePath(true);
                dbPath = dbPath.substring(0, dbPath.lastIndexOf(FileUtil.getDirSeparator()));
               
                if (coversPath.indexOf(dbPath) != -1)
                    coversPath = coversPath.substring(dbPath.length()+1, coversPath.length());
                
                MovieManager.getConfig().setUseRelativeCoversPath(1);
            }
        }
        else {
            MovieManager.getConfig().setUseRelativeCoversPath(0);
        }
        
        
        
        if (relativeQueriesEnabled.isSelected()) {
            
            if (relativeQueriesProgram.isSelected()) {
                
            	if (queriesPath.indexOf(FileUtil.getUserDir()) != -1)
                    queriesPath = queriesPath.substring(FileUtil.getUserDir().length()+1, queriesPath.length());
                
                MovieManager.getConfig().setUseRelativeQueriesPath(2);
            }
            else {
                String dbPath = MovieManager.getConfig().getDatabasePath(true);
                dbPath = dbPath.substring(0, dbPath.lastIndexOf(FileUtil.getDirSeparator()));
               
                if (queriesPath.indexOf(dbPath) != -1)
                    queriesPath = queriesPath.substring(dbPath.length()+1, queriesPath.length());
                
                MovieManager.getConfig().setUseRelativeQueriesPath(1);
            }
        }
        else {
        	MovieManager.getConfig().setUseRelativeQueriesPath(0);
        }
        
        if (relativeDatabaseEnabled.isSelected()) {
            
            String databasePath = MovieManager.getConfig().getDatabasePath(true);
            
            if (databasePath.indexOf(FileUtil.getUserDir()) == -1) {
                DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogFolders.alert.title"), Localizer.getString("DialogFolders.alert.database-relative-install.message")); 
                GUIUtil.showAndWait(alert, true);
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
        
        // Sets cover and queries directory
        MovieManager.getConfig().setCoverAndQueriesPaths(coversPath, queriesPath);
        
        MovieManagerCommandSelect.execute();
        dispose();
    }
    
   
    
    public void itemStateChanged(ItemEvent event) {
        
        Object source = event.getItemSelectable();
        
        if (source.equals(setPermanentDatabase)) {
            
            String databaseType = MovieManager.getIt().getDatabase().getDatabaseType();
            textFieldLoadDatabase.setText(databaseType+ ">" +textFieldDatabase.getText()); //$NON-NLS-1$
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
            
            processPathValidation("Queries");
        }
        
        if (source.equals(relativeCoversEnabled)) {
            
            if (relativeCoversEnabled.isSelected())
                value = true;
            else
                value = false;
            
            relativeCoversProgram.setEnabled(value);
            relativeCoversDatabase.setEnabled(value);
            optionCovers.setEnabled(value);
            
            processPathValidation("Covers");
        }
        
        if (source.equals(relativeDatabaseEnabled)) {
            
            if (relativeDatabaseEnabled.isSelected())
                value = true;
            else
                value = false;
            
            relativeDatabaseProgram.setEnabled(value);
            optionDatabase.setEnabled(value);
        }
        
        
        if (source.equals(relativeCoversProgram)) {
            
            if (relativeCoversProgram.isSelected())
            	processPathValidation("Covers");
        }
        
        if (source.equals(relativeCoversDatabase)) {
            
            if (relativeCoversDatabase.isSelected())
            	processPathValidation("Covers");
        }
        
        if (source.equals(relativeQueriesProgram)) {
            
            if (relativeQueriesProgram.isSelected())
            	processPathValidation("Queries");
        }
        
        if (source.equals(relativeCoversDatabase)) {
            
            if (relativeQueriesDatabase.isSelected())
            	processPathValidation("Queries");
        }
        
    }

    
    //textFieldQueries.setBackground(new Color(233, 180, 180));
    
   // textFieldQueries.getDocument().addDocumentListener(myListener);
    
    //Gives notification that an attribute or set of attributes changed.
    public void changedUpdate(DocumentEvent e) {
	}

    // Gives notification that there was an insert into the document. The range given by the DocumentEvent bounds the freshly inserted region.
    public void insertUpdate(DocumentEvent e) {

    	if (e.getDocument().equals(textFieldCovers.getDocument()))
    		processPathValidation("Covers");
    	else
    		processPathValidation("Queries");
    }
    	

//  Gives notification that a portion of the document has been removed. The range is given in terms of what the view last saw (that is, before updating sticky positions).
    public void removeUpdate(DocumentEvent e){

    	if (e.getDocument().equals(textFieldCovers.getDocument()))
    		processPathValidation("Covers");
    	else
    		processPathValidation("Queries");
	}

    
    public void processPathValidation(String option) {
    	
    	File coversFolder;
    	File queriesFolder;
    	String coversPath = getCoversPath();
    	String queriesPath = getQueriesPath();

    	if (option.indexOf("Covers") != -1) {

    		/* Relative covers path enabled */
    		if (relativeCoversEnabled.isSelected()) {

    			// Relative to program location
    			if (relativeCoversProgram.isSelected()) {

    				if (new File(coversPath).isDirectory() && (coversPath.indexOf(FileUtil.getUserDir()) == -1)) {
    					//DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogFolders.alert.title"), Localizer.getString("DialogFolders.alert.covers-inside-install.message")); 
    					textFieldCovers.setBackground(invalidPath);
    					textFieldCovers.setToolTipText(Localizer.getString("DialogFolders.alert.covers-inside-install.message"));
    					validCoversDirectory = false;
    					return;
    				}

    				if (!(coversFolder = new File(coversPath)).isDirectory() && !(coversFolder = new File(FileUtil.getUserDir(), coversPath)).isDirectory()) { //$NON-NLS-1$
    					//DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogFolders.alert.title"),Localizer.getString("DialogFolders.alert-covers-exist")); 
    					textFieldCovers.setBackground(invalidPath);
    					textFieldCovers.setToolTipText(Localizer.getString("DialogFolders.alert-covers-exist"));
    					validCoversDirectory = false;
    					return;
    				}

    				textFieldCovers.setBackground(Color.WHITE);
    				textFieldCovers.setToolTipText(null);
    				validCoversDirectory = true;
    			}
    			// Relative to database
    			else {
    				String dbPath = MovieManager.getConfig().getDatabasePath(true);
    				dbPath = dbPath.substring(0, dbPath.lastIndexOf(FileUtil.getDirSeparator()));

    				if ((coversFolder = new File(coversPath)).isDirectory()) {

    					if (coversPath.indexOf(dbPath) == -1) {
    						textFieldCovers.setBackground(invalidPath);
    						textFieldCovers.setToolTipText(Localizer.getString("DialogFolders.alert-covers-relative-database"));
    						validCoversDirectory = false;
    						return;
    					}
    				}
    				else if (!(coversFolder = new File(dbPath, coversPath)).isDirectory()) {
    					textFieldCovers.setBackground(invalidPath);
    					textFieldCovers.setToolTipText(Localizer.getString("DialogFolders.alert.covers-doesnt-exist.message"));
    					validCoversDirectory = false;
    					return;
    				}

    				textFieldCovers.setBackground(Color.WHITE);
    				textFieldCovers.setToolTipText(null);
    				validCoversDirectory = true;
    			}
    		}
    		else {
    			coversFolder = new File(coversPath);

    			if (!coversFolder.isDirectory()) {
    				textFieldCovers.setBackground(invalidPath);
    				textFieldCovers.setToolTipText(Localizer.getString("DialogFolders.alert.covers-doesnt-exist.message"));
    				validCoversDirectory = false;
    				return;
    			}
    			
    			textFieldCovers.setBackground(Color.WHITE);
    			textFieldCovers.setToolTipText(null);
    			validCoversDirectory = true;
    		}
    	}
    	
    	// Queries
    	if (option.indexOf("Queries") != -1) {

    		/* Relative covers path enabled */
    		if (relativeQueriesEnabled.isSelected()) {

    			// Relative to program location
    			if (relativeQueriesProgram.isSelected()) {

    				if (new File(queriesPath).isDirectory() && (queriesPath.indexOf(FileUtil.getUserDir()) == -1)) {
    					//DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogFolders.alert.title"), Localizer.getString("DialogFolders.alert.covers-inside-install.message")); 
    					textFieldQueries.setBackground(invalidPath);
    					textFieldQueries.setToolTipText(Localizer.getString("DialogFolders.alert.queries-inside-install.message"));
    					validQueriesDirectory = false;
    					return;
    				}

    				if (!(queriesFolder = new File(queriesPath)).isDirectory() && !(queriesFolder = new File(FileUtil.getUserDir(), queriesPath)).isDirectory()) { //$NON-NLS-1$
    					//DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogFolders.alert.title"),Localizer.getString("DialogFolders.alert-covers-exist")); 
    					textFieldQueries.setBackground(invalidPath);
    					textFieldQueries.setToolTipText(Localizer.getString("DialogFolders.alert-queries-exist"));
    					validQueriesDirectory = false;
    					return;
    				}

    				textFieldQueries.setBackground(Color.WHITE);
    				textFieldQueries.setToolTipText(null);
    				validQueriesDirectory = true;
    			}
    			// Relative to database
    			else {
    				String dbPath = MovieManager.getConfig().getDatabasePath(true);
    				dbPath = dbPath.substring(0, dbPath.lastIndexOf(FileUtil.getDirSeparator()));

    				if ((queriesFolder = new File(queriesPath)).isDirectory()) {

    					if (queriesPath.indexOf(dbPath) == -1) {
    						textFieldQueries.setBackground(invalidPath);
    						textFieldQueries.setToolTipText(Localizer.getString("DialogFolders.alert-queries-relative-database"));
    						validQueriesDirectory = false;
    						return;
    					}
    				}
    				else if (!(queriesFolder = new File(dbPath, queriesPath)).isDirectory()) {
    					textFieldQueries.setBackground(invalidPath);
    					textFieldQueries.setToolTipText(Localizer.getString("DialogFolders.alert.queries-doesnt-exist.message"));
    					validQueriesDirectory = false;
    					return;
    				}

    				textFieldQueries.setBackground(Color.WHITE);
    				textFieldQueries.setToolTipText(null);
    				validQueriesDirectory = true;
    			}
    		}
    		else {
    			queriesFolder = new File(queriesPath);

    			if (!queriesFolder.isDirectory()) {
    				textFieldQueries.setBackground(invalidPath);
    				textFieldQueries.setToolTipText(Localizer.getString("DialogFolders.alert.queries-doesnt-exist.message"));
    				validQueriesDirectory = false;
    				return;
    			}
    			
    			textFieldQueries.setBackground(Color.WHITE);
    			textFieldQueries.setToolTipText(null);
    			validQueriesDirectory = true;
    		}
    	}
    }
    
    
    /**
     * Gets a folder and updates the textfield...
     **/
    private void executeCommandBrowse(String title) {
    	JTextField textField;
    	/* Gets the right JTextField. */
    	if (title.equals(Localizer.getString("DialogFolders.selectCoversDir"))) {
    		textField = getCovers();
    	} else {
    		textField = getQueries();
    	}

    	/*The Oyoaha theme wouldn't set the file name in the name texField so a contructor accepting current dir and selection mode takes care of that*/
    	ExtendedFileChooser fileChooser;

    	if (title.equals(Localizer.getString("DialogFolders.selectCoversDir")))
    		fileChooser = new ExtendedFileChooser(MovieManager.getConfig().getCoversFolder(), ExtendedFileChooser.DIRECTORIES_ONLY);
    	else
    		fileChooser = new ExtendedFileChooser(MovieManager.getConfig().getQueriesFolder(), ExtendedFileChooser.DIRECTORIES_ONLY);

    	fileChooser.setDialogTitle(title);
    	fileChooser.setApproveButtonText(Localizer.getString("DialogFolders.fileChooser.approve.text"));
    	fileChooser.setApproveButtonToolTipText(Localizer.getString("DialogFolders.filechooser.approve.tooltip"));
    	fileChooser.setAcceptAllFileFilterUsed(false);

    	int returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());
    	if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {

    		/* Verifies that it's a directory... */
    		if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().isDirectory()) {
    			/* Sets the new dir... */
    			textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
    		}
    	}
    }
}
