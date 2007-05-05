/**
 * @(#)DialogAddMultipleMovies.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;


public class DialogAddMultipleMovies extends JDialog implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    private JTextField excludeString;
    private JTextField moviesPath;
    
    private JButton browseDirectories;
    private JButton buttonOk;
    private JButton buttonCancel;
    private JButton buttonAddMovies;
    private JButton buttonAddList;
    
    private JRadioButton askButton;
    private JRadioButton selectIfOnlyOneHitButton;
    private JRadioButton selectFirstHitButton;
    
    public JCheckBox enableExludeParantheses;
    public JCheckBox enableExludeCDNotation;
    public JCheckBox enableExludeIntegers;
    public JCheckBox enableExludeCodecInfo;
    public JCheckBox enableSearchInSubdirectories;
	public JCheckBox titleOption;
    
    private JCheckBox enableExludeString;
    
    public JCheckBox enableAddMoviesToList;
    public JComboBox listChooser;
    
    JPanel all;
    
    /* The int stores the ints 0,1 or 2.
       0 for ask awlays, 1 for automatically select first hit, 
       and 2 to automatically select if only one hit.
    */
    private int multiAddSelectOption; /*From 0-2*/
        
    public DialogAddMultipleMovies() {
	/* Dialog creation...*/
	super(MovieManager.getDialog());
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    moviesPath.setText(""); //$NON-NLS-1$
		    dispose();
		}
	    });
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e) {
		    moviesPath.setText(""); //$NON-NLS-1$
		    dispose();
		}
	    };
	
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
	getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
	
	setTitle(Localizer.getString("DialogAddMultipleMovies.title")); //$NON-NLS-1$
	setModal(true);
	setResizable(false);
	
	multiAddSelectOption = MovieManager.getConfig().getMultiAddSelectOption();
	
	/*Radio buttons, choses if the list of hits should apear or not*/
	askButton = new JRadioButton(Localizer.getString("DialogAddMultipleMovies.panel-hits.option-display-list-of-hits.text")); //$NON-NLS-1$
	askButton.setActionCommand("Display list of hits"); //$NON-NLS-1$
	askButton.addActionListener(this);
	
	selectFirstHitButton = new JRadioButton(Localizer.getString("DialogAddMultipleMovies.panel-hits.option-select-first-hit.text")); //$NON-NLS-1$
	selectFirstHitButton.setActionCommand("Select First Hit"); //$NON-NLS-1$
	selectFirstHitButton.addActionListener(this);
	
	selectIfOnlyOneHitButton = new JRadioButton(Localizer.getString("DialogAddMultipleMovies.panel-hits.option-select-if-onlt-one-hit.text")); //$NON-NLS-1$
	selectIfOnlyOneHitButton.setActionCommand("Select If Only One Hit"); //$NON-NLS-1$
	selectIfOnlyOneHitButton.addActionListener(this);
	
	ButtonGroup radioButtonGroup = new ButtonGroup();
	radioButtonGroup.add(askButton);
	radioButtonGroup.add(selectFirstHitButton);
	radioButtonGroup.add(selectIfOnlyOneHitButton);
	
	switch (multiAddSelectOption) {
	case 0 : askButton.setSelected(true); break;
	case 1 : selectFirstHitButton.setSelected(true); break;
	case 2 : selectIfOnlyOneHitButton.setSelected(true); break;
	}
	
	JPanel radioButtonPanel = new JPanel(new GridLayout(0, 1));
	radioButtonPanel.add(askButton);
	radioButtonPanel.add(selectFirstHitButton);
	radioButtonPanel.add(selectIfOnlyOneHitButton);
	
	radioButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-hits.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
	
	/*Predefined remove values. 
	  The actual removal of the strings goes on in MovieManagerCommandAddMultipleMovies.java
	*/
	enableExludeParantheses = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-parantheses.text")); //$NON-NLS-1$
	enableExludeParantheses.setActionCommand("enableExludeParantheses"); //$NON-NLS-1$
	enableExludeParantheses.addActionListener(this);
	enableExludeParantheses.setSelected(MovieManager.getIt().getConfig().getMultiAddEnableExludeParantheses());
	
	enableExludeCDNotation = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-cd-notation.text")); //$NON-NLS-1$
	enableExludeCDNotation.setActionCommand("enableExludeCDNotation"); //$NON-NLS-1$
	enableExludeCDNotation.addActionListener(this);
	enableExludeCDNotation.setSelected(MovieManager.getIt().getConfig().getMultiAddEnableExludeCDNotation());
	
	enableExludeIntegers = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-integers.text")); //$NON-NLS-1$
	enableExludeIntegers.setActionCommand("enableExludeInteger"); //$NON-NLS-1$
	enableExludeIntegers.addActionListener(this);
	enableExludeIntegers.setSelected(MovieManager.getIt().getConfig().getMultiAddEnableExludeIntegers());
	
	enableExludeCodecInfo = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-predefined-codec-info.text")); //$NON-NLS-1$
	enableExludeCodecInfo.setActionCommand("enableExludeCodecInfo"); //$NON-NLS-1$
	enableExludeCodecInfo.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-predefined-codec-info-tooltip")); //$NON-NLS-1$
	enableExludeCodecInfo.addActionListener(this);
	enableExludeCodecInfo.setSelected(MovieManager.getIt().getConfig().getMultiAddEnableExludeCodecInfo());
	
	JPanel removeCheckBoxPanel = new JPanel(new GridLayout(0, 1));
	removeCheckBoxPanel.add(enableExludeParantheses);
	removeCheckBoxPanel.add(enableExludeCDNotation);
	removeCheckBoxPanel.add(enableExludeIntegers);
	removeCheckBoxPanel.add(enableExludeCodecInfo);
	
	removeCheckBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-clean-string.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
	
	enableSearchInSubdirectories = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-subdirs.search-through-subdirs.text")); //$NON-NLS-1$
	enableSearchInSubdirectories.setActionCommand("enableSearchInSubdirectories"); //$NON-NLS-1$
	enableSearchInSubdirectories.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-subdirs.search-through-subdirs.tooltip")); //$NON-NLS-1$
	enableSearchInSubdirectories.addActionListener(this);
	enableSearchInSubdirectories.setSelected(MovieManager.getIt().getConfig().getMultiAddEnableSearchInSubdirectories());
	
	JPanel searchInSubdirectoriesCheckBoxPanel = new JPanel(new GridLayout(0, 1));
	searchInSubdirectoriesCheckBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-subdirs.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
	
	searchInSubdirectoriesCheckBoxPanel.add(enableSearchInSubdirectories);

	
											

/***************** Added ********************/

       titleOption = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming.text")); //$NON-NLS-1$
       titleOption.setActionCommand("enableFolderTitle"); //$NON-NLS-1$
       titleOption.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming-tooltip")); //$NON-NLS-1$
       titleOption.addActionListener(this);
       titleOption.setSelected(MovieManager.getIt().getConfig().getMultiAddTitleOption());
       
       JPanel titleOptionPanel = new JPanel(new GridLayout(0, 1));
       titleOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Title Options"), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

       titleOptionPanel.add(titleOption);

/********************************************/	

       

	/*Exclude String, a checkbox to enable and the TextField
	  DocumentRegExp makes sure illigal characters can't be entered.
	 */
	excludeString = new JTextField(27);
	excludeString.setActionCommand("Exclude String:"); //$NON-NLS-1$
	excludeString.setDocument(new DocumentRegExp("[^(){}.,=+$\\x5B\\x5D]*", 200)); //$NON-NLS-1$
	excludeString.setText(""); //$NON-NLS-1$
	
	enableExludeString = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-exclude-string.enable.text")); //$NON-NLS-1$
	enableExludeString.setActionCommand("enableExludeString"); //$NON-NLS-1$
	enableExludeString.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-exclude-string.enable.tooltip")); //$NON-NLS-1$
	enableExludeString.addActionListener(this);
	
	if (MovieManager.getConfig().getMultiAddExcludeStringEnabled() && !MovieManager.getConfig().getMultiAddExcludeString().equals("")) { //$NON-NLS-1$
	    enableExludeString.setSelected(true);
	    excludeString.setEnabled(true);
	    excludeString.setText(MovieManager.getConfig().getMultiAddExcludeString());
	}
	else {
	    enableExludeString.setSelected(false);
	    excludeString.setEnabled(false);
	    excludeString.setText(MovieManager.getConfig().getMultiAddExcludeString());
	}
	
	JPanel excludeStringPanel = new JPanel();
	excludeStringPanel.setLayout(new FlowLayout());
	excludeStringPanel.add(enableExludeString);
	excludeStringPanel.add(excludeString);
	
	excludeStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-exclude-string.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
	
	
	/* Add to list */
	
	JPanel listPanel = makeListPanel();
	
	/* Multi-add directory */
	moviesPath = new JTextField(27);
	moviesPath.setActionCommand("MultiAdd directory"); //$NON-NLS-1$
	moviesPath.setText(MovieManager.getConfig().getMultiAddDirectoryPath());
	
	browseDirectories = new JButton(Localizer.getString("DialogAddMultipleMovies.panel-directory.button-browse.text")); //$NON-NLS-1$
	browseDirectories.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-directory.button-browse.tooltip")); //$NON-NLS-1$
	browseDirectories.setActionCommand("DialogAddMultipleMovies - Browse"); //$NON-NLS-1$
	browseDirectories.addActionListener(this);
	
	JPanel moviesPathPanel = new JPanel();
	moviesPathPanel.setLayout(new FlowLayout());
	moviesPathPanel.add(moviesPath);
	moviesPathPanel.add(browseDirectories);
	
	moviesPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-directory.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
	
	buttonOk = new JButton(Localizer.getString("DialogAddMultipleMovies.button-ok.text")); //$NON-NLS-1$
	buttonOk.setToolTipText(Localizer.getString("DialogAddMultipleMovies.button-ok.tooltip")); //$NON-NLS-1$
	buttonOk.setActionCommand("DialogAddMultipleMovies - OK"); //$NON-NLS-1$
	buttonOk.addActionListener(this);
	
	buttonCancel = new JButton(Localizer.getString("DialogAddMultipleMovies.button-cancel.text")); //$NON-NLS-1$
	buttonCancel.setToolTipText(Localizer.getString("DialogAddMultipleMovies.button-cancel.tooltip")); //$NON-NLS-1$
	buttonCancel.setActionCommand("DialogAddMultipleMovies - Cancel"); //$NON-NLS-1$
	buttonCancel.addActionListener(this);
	
	buttonAddMovies = new JButton(Localizer.getString("DialogAddMultipleMovies.button-add-movies.text")); //$NON-NLS-1$
	buttonAddMovies.setToolTipText(Localizer.getString("DialogAddMultipleMovies.button-add-movies.tooltip")); //$NON-NLS-1$
	buttonAddMovies.setActionCommand("DialogAddMultipleMovies - Add Movies"); //$NON-NLS-1$
	buttonAddMovies.addActionListener(this);
	
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.add(buttonOk);
	buttonPanel.add(buttonAddMovies);
	buttonPanel.add(buttonCancel);
	
	all = new JPanel();
	all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
	all.add(radioButtonPanel);
	all.add(removeCheckBoxPanel);
	all.add(titleOptionPanel);
	all.add(searchInSubdirectoriesCheckBoxPanel);
	all.add(excludeStringPanel);
	all.add(listPanel);
	all.add(moviesPathPanel);
	all.add(buttonPanel);
	
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
	getContentPane().add(all,BorderLayout.NORTH);
	/* Packs and sets location... */
	pack();
	
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
                (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	
	}
    
    JPanel makeListPanel() {
	
	JPanel listPanel = new JPanel();
	listPanel.setLayout(new BorderLayout());
	listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
	
	ArrayList columnListNames = MovieManager.getIt().getDatabase().getListsColumnNames();
	Object [] listNames = columnListNames.toArray();
	
	if (listNames.length == 0) {
	    
	    JLabel label = new JLabel(Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.label.create-list.text")); //$NON-NLS-1$
	    listPanel.add(label, BorderLayout.WEST);
	    
	    buttonAddList = new JButton(Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.button.add-list.text")); //$NON-NLS-1$
	    buttonAddList.addActionListener(this);
	    listPanel.add(buttonAddList, BorderLayout.CENTER);
	    
	}
	else {
	    buttonAddList = new JButton(Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.button.add-list.text")); //$NON-NLS-1$
	    buttonAddList.addActionListener(this);
	    listPanel.add(buttonAddList, BorderLayout.WEST);
	    
	    String list = MovieManager.getConfig().getMultiAddList();
	    	    
	    listChooser = new JComboBox(listNames);
	    listChooser.setSelectedItem(list);
	    
	    enableAddMoviesToList = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.button.enable-add-movies-to-list.text")); //$NON-NLS-1$
	    enableAddMoviesToList.setActionCommand("enableAddMoviesToList"); //$NON-NLS-1$
	    enableAddMoviesToList.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.button.enable-add-movies-to-list.tooltip")); //$NON-NLS-1$
	    enableAddMoviesToList.addActionListener(this);
	    
	    if (MovieManager.getConfig().getMultiAddListEnabled()) {
		enableAddMoviesToList.setSelected(true);
		listChooser.setEnabled(true);
	    }
	    else {
		enableAddMoviesToList.setSelected(false);
		listChooser.setEnabled(false);
	    }
	    
	    if (listChooser.getSelectedIndex() == -1)
		listChooser.setSelectedIndex(0);
	    
	    JPanel listChooserPanel = new JPanel(); 
	    listChooserPanel.add(enableAddMoviesToList);
	    listChooserPanel.add(listChooser);
	    
	    listPanel.add(listChooserPanel, BorderLayout.EAST);
	}
	return listPanel;
    }
    
    /*Opens a filechooser and returns the absolute path to the selected file*/
    private String executeCommandGetMovieDirectory() {
	
	/* Opens the Open dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	try {
	    fileChooser.setFileSelectionMode(ExtendedFileChooser.DIRECTORIES_ONLY);
	    File path;
	    if (!moviesPath.getText().equals("") && ((path = new File(moviesPath.getText())).exists())) //$NON-NLS-1$
		fileChooser.setCurrentDirectory(path);
	    
	    else if (MovieManager.getConfig().getLastFileDir() != null)
		fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastFileDir());
	    
	    fileChooser.setDialogTitle(Localizer.getString("DialogAddMultipleMovies.filechooser.select-movie-directory.title")); //$NON-NLS-1$
	    fileChooser.setApproveButtonText(Localizer.getString("DialogAddMultipleMovies.filechooser.select-movie-directory.approve.text")); //$NON-NLS-1$
	    fileChooser.setApproveButtonToolTipText(Localizer.getString("DialogAddMultipleMovies.filechooser.select-movie-directory.approve.tooltip")); //$NON-NLS-1$
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    
	    int returnVal = fileChooser.showOpenDialog(this);
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		/* Gets the path... */
		String filepath = fileChooser.getSelectedFile().getAbsolutePath();
		
		if (!(new File(filepath).exists())) {
		    throw new Exception("Movie catalogue not found."); //$NON-NLS-1$
		}
		return filepath;
	    }
	}
	catch (Exception e) {
	    log.error("", e); //$NON-NLS-1$
	}
	/* Sets the last path... */
	MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
	
	return moviesPath.getText();
    }
    
    /*Saves the options to the MovieManager object*/
    void executeSave() {
	MovieManager.getConfig().setMultiAddExcludeStringEnabled(enableExludeString.isSelected());
	MovieManager.getConfig().setMultiAddExcludeString(excludeString.getText());
	MovieManager.getConfig().setMultiAddDirectoryPath(moviesPath.getText());
	
	if (listChooser != null) {
	    MovieManager.getConfig().setMultiAddList((String) listChooser.getSelectedItem());
	    
	    if (enableAddMoviesToList.isSelected())
		MovieManager.getConfig().setMultiAddListEnabled(true);
	    else
		MovieManager.getConfig().setMultiAddListEnabled(false);
	}
	
	MovieManager.getConfig().setMultiAddSelectOption(multiAddSelectOption);
	MovieManager.getConfig().setMultiAddEnableExludeParantheses(enableExludeParantheses.isSelected());
	MovieManager.getConfig().setMultiAddEnableExludeCDNotation(enableExludeCDNotation.isSelected());
	MovieManager.getConfig().setMultiAddEnableExludeIntegers(enableExludeIntegers.isSelected());
	MovieManager.getConfig().setMultiAddEnableExludeCodecInfo(enableExludeCodecInfo.isSelected());
	MovieManager.getConfig().setMultiAddEnableSearchInSubdirectories(enableSearchInSubdirectories.isSelected());
	MovieManager.getConfig().setMultiAddTitleOption(titleOption.isSelected());
    }
	
	
    /*Returns the string in the path textfield*/
    public String getPath() {
	return moviesPath.getText();
    }
    
    /*Returns the user defined exlude string*/
    public String getMultiAddExcludeString() {
	return excludeString.getText();
    }
    
    public boolean getMultiAddExcludeStringEnabled() {
	 return enableExludeString.isSelected();
    }
    
    /*returns the value of the multiAddSelectOption variable.( values 0-2)*/
    public int getMultiAddSelectOption() {
	return multiAddSelectOption;
    }
  
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	
	if (event.getSource().equals(browseDirectories)) {
	    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	    moviesPath.setText(executeCommandGetMovieDirectory());
	}

	if (event.getSource().equals(buttonOk)) {
	    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	    executeSave();
	    moviesPath.setText(""); //$NON-NLS-1$
	    dispose();
	}
	
	if (event.getSource().equals(buttonCancel)) {
	    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	    moviesPath.setText(""); //$NON-NLS-1$
	    dispose();
	}
	
	if (event.getSource().equals(buttonAddMovies)) {
	    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	    
	    if (moviesPath.getText().equals("")) { //$NON-NLS-1$
		DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogAddMultipleMovies.alert.title.alert"), Localizer.getString("DialogAddMultipleMovies.alert.message.specify-directory-path")); //$NON-NLS-1$ //$NON-NLS-2$
		GUIUtil.showAndWait(this, true);
	    }
	    else if (!new File(moviesPath.getText()).exists()) {
		DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogAddMultipleMovies.alert.title.alert"), Localizer.getString("DialogAddMultipleMovies.alert.message.specified-directory-does-not-exist")); //$NON-NLS-1$ //$NON-NLS-2$
		GUIUtil.showAndWait(this, true);
	    }
	    else {
		executeSave();
		dispose();
	    }
	}
	
	if (event.getSource().equals(buttonAddList)) {
	
	    MovieManagerCommandLists.execute(this);
	    
	    all.remove(5);
	    all.add(makeListPanel(), 5);
	    pack();
	    GUIUtil.show(this, true);
	}
	
	if (event.getSource().equals(enableExludeString)) {
	    if (enableExludeString.isSelected())
		excludeString.setEnabled(true);
	    else
		excludeString.setEnabled(false);
	}
	
	
	if (event.getSource().equals(enableAddMoviesToList)) {
	    
	    if (enableAddMoviesToList.isSelected())
		listChooser.setEnabled(true);
	    else
		listChooser.setEnabled(false);
	}
	
	if (event.getSource().equals(askButton))
	    multiAddSelectOption = 0;
	
	if (event.getSource().equals(selectFirstHitButton))
	    multiAddSelectOption = 1;
	
	if (event.getSource().equals(selectIfOnlyOneHitButton))
	    multiAddSelectOption = 2;
    }
}
