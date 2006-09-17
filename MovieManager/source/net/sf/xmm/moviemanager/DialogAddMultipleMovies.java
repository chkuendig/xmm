/**
 * @(#)DialogAddMultipleMovies.java 1.0 14.09.05 (dd.mm.yy)
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
import net.sf.xmm.moviemanager.commands.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;

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
	super(MovieManager.getIt());
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    moviesPath.setText("");
		    dispose();
		}
	    });
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e) {
		    moviesPath.setText("");
		    dispose();
		}
	    };
	
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	setTitle("Add Multiple Movies");
	setModal(true);
	setResizable(false);
	
	multiAddSelectOption = MovieManager.getConfig().getMultiAddSelectOption();
	
	/*Radio buttons, choses if the list of hits should apear or not*/
	askButton = new JRadioButton("Display list of hits");
	askButton.setActionCommand("Display list of hits");
	askButton.addActionListener(this);
	
	selectFirstHitButton = new JRadioButton("Select First Hit");
	selectFirstHitButton.setActionCommand("Select First Hit");
	selectFirstHitButton.addActionListener(this);
	
	selectIfOnlyOneHitButton = new JRadioButton("Select If Only One Hit, else display list of hits");
	selectIfOnlyOneHitButton.setActionCommand("Select If Only One Hit");
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
	
	radioButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," IMDb Dialog "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	/*Predefined remove values. 
	  The actual removal of the strings goes on in MovieManagerCommandAddMultipleMovies.java
	*/
	enableExludeParantheses = new JCheckBox("Remove parantheses and it's content ( \"( )\" , \"[ ]\" , \"{ }\" )");
	enableExludeParantheses.setActionCommand("enableExludeParantheses");
	enableExludeParantheses.addActionListener(this);
	
	enableExludeCDNotation = new JCheckBox("Remove cd notations e.g. cd1 and 1of2");
	enableExludeCDNotation.setActionCommand("enableExludeCDNotation");
	enableExludeCDNotation.addActionListener(this);
	
	enableExludeIntegers = new JCheckBox("Remove integers");
	enableExludeIntegers.setActionCommand("enableExludeInteger");
	enableExludeIntegers.addActionListener(this);
	
	enableExludeCodecInfo = new JCheckBox("Remove predefined codec info");
	enableExludeCodecInfo.setActionCommand("enableExludeCodecInfo");
	enableExludeCodecInfo.setToolTipText("Removes: divx, dvdivx, xvidvd, xvid, dvdrip, ac3, bivx, mp3");
	enableExludeCodecInfo.addActionListener(this);
	
	JPanel removeCheckBoxPanel = new JPanel(new GridLayout(0, 1));
	removeCheckBoxPanel.add(enableExludeParantheses);
	removeCheckBoxPanel.add(enableExludeCDNotation);
	removeCheckBoxPanel.add(enableExludeIntegers);
	removeCheckBoxPanel.add(enableExludeCodecInfo);
	
	removeCheckBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Clean Search String "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	enableSearchInSubdirectories = new JCheckBox("Search through subdirectories");
	enableSearchInSubdirectories.setActionCommand("enableSearchInSubdirectories");
	enableSearchInSubdirectories.setToolTipText("Add all movies found in subdirectories");
	enableSearchInSubdirectories.addActionListener(this);
	
	JPanel searchInSubdirectoriesCheckBoxPanel = new JPanel(new GridLayout(0, 1));
	searchInSubdirectoriesCheckBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Subdirectories "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	searchInSubdirectoriesCheckBoxPanel.add(enableSearchInSubdirectories);
	
	/*Exlude String, a checkbox to enable and the TextField
	  DocumentRegExp makes sure illigal characters can't be entered.
	 */
	excludeString = new JTextField(27);
	excludeString.setActionCommand("Exclude String:");
	excludeString.setDocument(new DocumentRegExp("[^(){}.,=+$\\x5B\\x5D]*", 200));
	excludeString.setText("");
	
	enableExludeString = new JCheckBox("Enable");
	enableExludeString.setActionCommand("enableExludeString");
	enableExludeString.setToolTipText("Excludes the additional string(s).");
	enableExludeString.addActionListener(this);
	
	if (MovieManager.getConfig().getMultiAddExcludeStringEnabled() && !MovieManager.getConfig().getMultiAddExcludeString().equals("")) {
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
	
	excludeStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Exclude String "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	
	/* Add to list */
	
	JPanel listPanel = makeListPanel();
	
	/* Multi-add directory */
	moviesPath = new JTextField(27);
	moviesPath.setActionCommand("MultiAdd directory");
	moviesPath.setText(MovieManager.getConfig().getMultiAddDirectoryPath());
	
	browseDirectories = new JButton("Browse");
	browseDirectories.setToolTipText("Browse to a directory containing movies");
	browseDirectories.setActionCommand("DialogAddMultipleMovies - Browse");
	browseDirectories.addActionListener(this);
	
	JPanel moviesPathPanel = new JPanel();
	moviesPathPanel.setLayout(new FlowLayout());
	moviesPathPanel.add(moviesPath);
	moviesPathPanel.add(browseDirectories);
	
	moviesPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Directory "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	buttonOk = new JButton("OK");
	buttonOk.setToolTipText("Save info");
	buttonOk.setActionCommand("DialogAddMultipleMovies - OK");
	buttonOk.addActionListener(this);
	
	buttonCancel = new JButton("Cancel");
	buttonCancel.setToolTipText("Leave without saving");
	buttonCancel.setActionCommand("DialogAddMultipleMovies - Cancel");
	buttonCancel.addActionListener(this);
	
	buttonAddMovies = new JButton("Add Movies");
	buttonAddMovies.setToolTipText("Add movies in the selected directory");
	buttonAddMovies.setActionCommand("DialogAddMultipleMovies - Add Movies");
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
	
	setVisible(true);
    }
    
    JPanel makeListPanel() {
	
	JPanel listPanel = new JPanel();
	listPanel.setLayout(new BorderLayout());
	listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Add to list "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	ArrayList columnListNames = MovieManager.getIt().getDatabase().getListsColumnNames();
	Object [] listNames = columnListNames.toArray();
	
	if (listNames.length == 0) {
	    
	    JLabel label = new JLabel("To add the movies to a list you need need to create a list");
	    listPanel.add(label, BorderLayout.WEST);
	    
	    buttonAddList = new JButton("Add list");
	    buttonAddList.addActionListener(this);
	    listPanel.add(buttonAddList, BorderLayout.CENTER);
	    
	}
	else {
	    buttonAddList = new JButton("Add list");
	    buttonAddList.addActionListener(this);
	    listPanel.add(buttonAddList, BorderLayout.WEST);
	    
	    String list = MovieManager.getConfig().getMultiAddList();
	    	    
	    listChooser = new JComboBox(listNames);
	    listChooser.setSelectedItem(list);
	    
	    enableAddMoviesToList = new JCheckBox("Enable");
	    enableAddMoviesToList.setActionCommand("enableAddMoviesToList");
	    enableAddMoviesToList.setToolTipText("Enable applying added movies to a list.");
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
	    if (!moviesPath.getText().equals("") && ((path = new File(moviesPath.getText())).exists()))
		fileChooser.setCurrentDirectory(path);
	    
	    else if (MovieManager.getConfig().getLastFileDir() != null)
		fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastFileDir());
	    
	    fileChooser.setDialogTitle("Select Movies Directory");
	    fileChooser.setApproveButtonText("Select");
	    fileChooser.setApproveButtonToolTipText("Select Movies Directory");
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    
	    int returnVal = fileChooser.showOpenDialog(this);
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		/* Gets the path... */
		String filepath = fileChooser.getSelectedFile().getAbsolutePath();
		
		if (!(new File(filepath).exists())) {
		    throw new Exception("Movie catalogue not found.");
		}
		return filepath;
	    }
	}
	catch (Exception e) {
	    log.error("", e);
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
	log.debug("ActionPerformed: " + event.getActionCommand());
	
	if (event.getSource().equals(browseDirectories)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    moviesPath.setText(executeCommandGetMovieDirectory());
	}

	if (event.getSource().equals(buttonOk)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    executeSave();
	    moviesPath.setText("");
	    dispose();
	}
	
	if (event.getSource().equals(buttonCancel)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    moviesPath.setText("");
	    dispose();
	}
	
	if (event.getSource().equals(buttonAddMovies)) {
	    log.debug("ActionPerformed: " + event.getActionCommand());
	    
	    if (moviesPath.getText().equals("")) {
		DialogAlert alert = new DialogAlert("Alert","Please specify a directory path.");
		alert.setVisible(true);
	    }
	    else if (!new File(moviesPath.getText()).exists()) {
		DialogAlert alert = new DialogAlert("Alert","The specified directory does not exist.");
		alert.setVisible(true);
	    }
	    else {
		executeSave();
		dispose();
	    }
	}
	
	if (event.getSource().equals(buttonAddList)) {
	
	    MovieManagerCommandLists.execute();
	    
	    all.remove(4);
	    all.add(makeListPanel(), 4);
	    pack();
	    setVisible(true);
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
