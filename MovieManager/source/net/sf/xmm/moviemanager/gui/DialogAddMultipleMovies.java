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

package net.sf.xmm.moviemanager.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.swing.extentions.filetree.AddSelectedFilesEvent;
import net.sf.xmm.moviemanager.swing.extentions.filetree.AddSelectedFilesEventListener;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileNode;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileTree;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;


public class DialogAddMultipleMovies extends JDialog implements ActionListener  {

	Logger log = Logger.getLogger(getClass());

	private JTextField excludeStringTextField;
	private JTextField regexTextField;
	
	private JButton buttonUpdateFileList;
	private JButton buttonCancel;
	public JButton buttonAddMovies;
	private JButton buttonAddList;

	private JRadioButton askButton;
	private JRadioButton selectIfOnlyOneHitButton;
	private JRadioButton selectFirstHitButton;

	public JCheckBox enableExludeParantheses;
	public JCheckBox enableExludeCDNotation;
	public JCheckBox enableExludeIntegers;
	public JCheckBox enableExludeCodecInfo;
	public JCheckBox titleOption;

	private JTextArea customExtension;
	private JCheckBox aviExtension;
	private JCheckBox divxExtension;
	private JCheckBox mpegExtension;
	private JCheckBox ogmExtension;
	private JCheckBox mkvExtension;
	
	private JCheckBox enableIncludeOrExludeString;
	JCheckBox enabledRegEx;
	
	JCheckBox regularStringNegate;
	JCheckBox regexNegate;
	
	JCheckBox filterOutDuplicates;
	
	public JCheckBox enableAddMoviesToList;
	public JComboBox listChooser;

	JList mediaFileList;
	JList filesToAddList;
	
	JPanel listPanel;
	
	FileTree fileTree;
	
	JPanel optionsPanel;
	JPanel all;
	
	HashMap<String, ModelEntry> existingMediaFiles = new HashMap<String, ModelEntry>();
	
	private HashMap<FileNode, FileNode> nodesInFileLists = new HashMap<FileNode, FileNode>();
		
	private boolean addListMustContainValidItemsAlert = false; // denotes if the list contains an alert
	
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
				executeSave();
				dispose();
			}
		});

		/* Enables dispose when pushing escape */
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				executeSave();
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
		getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

		setTitle(Localizer.getString("DialogAddMultipleMovies.title")); //$NON-NLS-1$
		setModal(true);
		setResizable(true);

		
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

		
		JPanel radioButtonPanel = new JPanel(new GridLayout(0, 1));
		radioButtonPanel.add(askButton);
		radioButtonPanel.add(selectFirstHitButton);
		radioButtonPanel.add(selectIfOnlyOneHitButton);

		radioButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-hits.title"))), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		/*
		 * Predefined remove values. 
	  	 * The actual removal of the strings goes on in MovieManagerCommandAddMultipleMovies.java
		 */
		enableExludeParantheses = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-parantheses.text")); //$NON-NLS-1$
		enableExludeParantheses.setActionCommand("enableExludeParantheses"); //$NON-NLS-1$
		enableExludeParantheses.addActionListener(this);
		
		enableExludeCDNotation = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-cd-notation.text")); //$NON-NLS-1$
		enableExludeCDNotation.setActionCommand("enableExludeCDNotation"); //$NON-NLS-1$
		enableExludeCDNotation.addActionListener(this);
		
		enableExludeIntegers = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-integers.text")); //$NON-NLS-1$
		enableExludeIntegers.setActionCommand("enableExludeInteger"); //$NON-NLS-1$
		enableExludeIntegers.addActionListener(this);
		
		enableExludeCodecInfo = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-predefined-codec-info.text")); //$NON-NLS-1$
		enableExludeCodecInfo.setActionCommand("enableExludeCodecInfo"); //$NON-NLS-1$
		enableExludeCodecInfo.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-predefined-codec-info-tooltip")); //$NON-NLS-1$
		enableExludeCodecInfo.addActionListener(this);
		
		JPanel removeCheckBoxPanel = new JPanel(new GridLayout(0, 1));
		removeCheckBoxPanel.add(enableExludeParantheses);
		removeCheckBoxPanel.add(enableExludeCDNotation);
		removeCheckBoxPanel.add(enableExludeIntegers);
		removeCheckBoxPanel.add(enableExludeCodecInfo);

		removeCheckBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-clean-string.title"))), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		
		titleOption = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming.text")); //$NON-NLS-1$
		titleOption.setActionCommand("enableFolderTitle"); //$NON-NLS-1$
		titleOption.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming-tooltip")); //$NON-NLS-1$
		titleOption.addActionListener(this);
	
		JPanel titleOptionPanel = new JPanel(new GridLayout(0, 1));
		titleOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Title Options")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		titleOptionPanel.add(titleOption);
			
		
		aviExtension = new JCheckBox("avi");
		divxExtension = new JCheckBox("divx");
		mpegExtension = new JCheckBox("mpeg");
		ogmExtension = new JCheckBox("ogm");
		mkvExtension = new JCheckBox("mkv");
		
		ActionListener extListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateExtensionoOnTree();
			}
		};
		
		aviExtension.addActionListener(extListener);
		divxExtension.addActionListener(extListener);
		mpegExtension.addActionListener(extListener);
		ogmExtension.addActionListener(extListener);
		mkvExtension.addActionListener(extListener);
		
		JPanel validExtension = new JPanel();
		validExtension.add(aviExtension);
		validExtension.add(mpegExtension);
		validExtension.add(divxExtension);
		validExtension.add(ogmExtension);
		validExtension.add(mkvExtension);
		
		customExtension = new JTextArea();
		customExtension.setBorder(BorderFactory.createEtchedBorder()); 

		customExtension.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateExtensionoOnTree();
			}
		});
		
		
		
		Font font = customExtension.getFont();
		customExtension.setFont(new Font(font.getFontName(), font.getStyle(), 15));
		
		JPanel extensionPanel = new JPanel(new BorderLayout());
		extensionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Valid extensions")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		
		extensionPanel.add(validExtension, BorderLayout.NORTH);
		extensionPanel.add(customExtension, BorderLayout.CENTER);
		
		//extensionPanel
		
		/* Add to list */

		listPanel = makeListPanel();
		
		buttonUpdateFileList = new JButton("Update file list"); //$NON-NLS-1$
		//buttonUpdateFileList.setToolTipText(Localizer.getString("DialogAddMultipleMovies.button-ok.tooltip")); //$NON-NLS-1$
		buttonUpdateFileList.setActionCommand("DialogAddMultipleMovies - Update file list"); //$NON-NLS-1$
		buttonUpdateFileList.addActionListener(this);

		buttonCancel = new JButton(Localizer.getString("DialogAddMultipleMovies.button-close.text")); //$NON-NLS-1$
		buttonCancel.setToolTipText(Localizer.getString("DialogAddMultipleMovies.button-close.tooltip")); //$NON-NLS-1$
		buttonCancel.setActionCommand("DialogAddMultipleMovies - Cancel"); //$NON-NLS-1$
		buttonCancel.addActionListener(this);

		buttonAddMovies = new JButton(Localizer.getString("DialogAddMultipleMovies.button-add-movies.text")); //$NON-NLS-1$
		buttonAddMovies.setToolTipText(Localizer.getString("DialogAddMultipleMovies.button-add-movies.tooltip")); //$NON-NLS-1$
		buttonAddMovies.setActionCommand("DialogAddMultipleMovies - Add Movies"); //$NON-NLS-1$
		buttonAddMovies.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(buttonUpdateFileList);
		buttonPanel.add(buttonAddMovies);
		buttonPanel.add(buttonCancel);

		optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.add(radioButtonPanel);
		optionsPanel.add(removeCheckBoxPanel);
		optionsPanel.add(titleOptionPanel);
		
		optionsPanel.add(extensionPanel);
		optionsPanel.add(listPanel);
		optionsPanel.add(buttonPanel);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		fileTree = new FileTree(existingMediaFiles);
		fileTree.setFilterOutDuplicates(true);
		
		fileTree.eventHandler.addAddSelectedFilesEventListener(new AddSelectedFilesEventListener() {
			public void addSelectedFilesEventOccurred(AddSelectedFilesEvent evt) {
				ArrayList<FileNode> files =  fileTree.getSelectedFiles();
				addFilesToAddToList(files);
			}
		});
			
		
// -------Include/Exclude regular expression/string------------
		 	
		
		// Regular string
		enableIncludeOrExludeString = new JCheckBox("Regular String:"); //$NON-NLS-1$
		enableIncludeOrExludeString.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-exclude-string.enable.tooltip")); //$NON-NLS-1$
		enableIncludeOrExludeString.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (enableIncludeOrExludeString.isSelected()) {
					excludeStringTextField.setEnabled(true);
					
					if (!"".equals(excludeStringTextField.getText().trim()))
						fileTree.setStringPattern(excludeStringTextField.getText());
					else
						fileTree.setStringPattern(null);
				}
				else {
					excludeStringTextField.setEnabled(false);
					fileTree.setStringPattern(null);
				}
			}
		});
			
		excludeStringTextField = new JTextField(27);
		// DocumentRegExp makes sure illigal characters can't be entered.
		excludeStringTextField.setDocument(new DocumentRegExp("[^(){}.,=+$\\x5B\\x5D]*", 200)); //$NON-NLS-1$
		
		excludeStringTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void	keyTyped(KeyEvent e) {}

			public void keyReleased(KeyEvent e) {
				
				String expression = excludeStringTextField.getText().trim();
				
				if (!expression.equals("") && enableIncludeOrExludeString.isSelected())
					fileTree.setStringPattern(expression);
				else
					fileTree.setStringPattern(null);
			}

		});

		
		regularStringNegate = new JCheckBox("Negate");
		regularStringNegate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.setStringNegate(regularStringNegate.isSelected());
			}
		});
				
	
		// Regular expression
				
		enabledRegEx = new JCheckBox("Regular expression:");
		enabledRegEx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (enabledRegEx.isSelected()) {
					regexTextField.setEnabled(true);
										
					if (!"".equals(regexTextField.getText().trim()))
						fileTree.setRegexPattern(regexTextField.getText());
					else
						fileTree.setRegexPattern(null);
					
				}
				else {
					regexTextField.setEnabled(false);
					fileTree.setRegexPattern(null);
				}
			}
		});
		
		
		
		
		regexNegate = new JCheckBox("Negate");
		regexNegate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.setRegexNegate(regexNegate.isSelected());
			}
		});		
		
		regexTextField = new JTextField(" ");
		regexTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void	keyTyped(KeyEvent e) {}
		
			public void keyReleased(KeyEvent e) {

				String expression = regexTextField.getText().trim();
				
				if (!expression.equals("") && enabledRegEx.isSelected()) {
					fileTree.setRegexPattern(expression);
				}
				else
					fileTree.setRegexPattern(null);
			}

		});
			
						
		double size[][] =
         {{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED},
          {0.5, 0.5}};

//		 Include Or Exclude Strings
		JPanel includeOrExcludeStringsAndRegex = new JPanel();
		includeOrExcludeStringsAndRegex.setLayout(new TableLayout(size));
		
		includeOrExcludeStringsAndRegex.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "String Matching"), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
		
		includeOrExcludeStringsAndRegex.add(enableIncludeOrExludeString, "0, 0");
		includeOrExcludeStringsAndRegex.add(excludeStringTextField, 		"1, 0");
		includeOrExcludeStringsAndRegex.add(regularStringNegate,"2, 0");
		
		includeOrExcludeStringsAndRegex.add(enabledRegEx, "0, 1");
		includeOrExcludeStringsAndRegex.add(regexTextField,   "1, 1");
		includeOrExcludeStringsAndRegex.add(regexNegate,  "2, 1");

		filterOutDuplicates = new JCheckBox("Filter out media files already in database"); 
		filterOutDuplicates.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				fileTree.setFilterOutDuplicates(filterOutDuplicates.isSelected());
				
				// Adding all the file paths of each media file from the database
				if (filterOutDuplicates.isSelected() && !existingMediaFiles.isEmpty()) {

					DefaultListModel movies = MovieManager.getIt().getDatabase().getMoviesList("Title");

					for (int i = 0; i < movies.size(); i++) {

						ModelEntry model = (ModelEntry) movies.get(i);
						String fileLocation = model.getAdditionalInfo().getFileLocation();

						String [] files = fileLocation.split("*");

						for (int u = 0; u < files.length; u++) {
							existingMediaFiles.put(files[u], model);
						}
					}
				}
			}
		});

		
		JPanel filterPanel = new JPanel();
		filterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Filter files"), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
				
		filterPanel.setLayout(new BorderLayout());
		filterPanel.add(filterOutDuplicates, BorderLayout.NORTH);
		filterPanel.add(includeOrExcludeStringsAndRegex, BorderLayout.SOUTH);
		
		JPanel filetreeAndRegex = new JPanel();
		filetreeAndRegex.setLayout(new BorderLayout());
		
		filetreeAndRegex.add(fileTree, BorderLayout.CENTER);
		filetreeAndRegex.add(filterPanel, BorderLayout.SOUTH);
				
		
		JPanel menuAndFileTreePanel = new JPanel();
		menuAndFileTreePanel.setLayout(new BorderLayout());
		
		menuAndFileTreePanel.add(optionsPanel, BorderLayout.WEST);
		menuAndFileTreePanel.add(filetreeAndRegex, BorderLayout.CENTER);
			
		
		
		JPanel mainTop  = new JPanel();
		mainTop.setLayout(new BorderLayout());
		mainTop.add(menuAndFileTreePanel, BorderLayout.CENTER);
		
		JPanel mediaFilesPanel = new JPanel();
		JScrollPane mediaFileListScrollPane = new JScrollPane();
		JTextArea mediaFileListTextArea = new JTextArea(3, 30);
		mediaFileListScrollPane.add(mediaFileListTextArea);
		mediaFilesPanel.add(mediaFileListScrollPane);
		
		JPanel mediaFilesToAddPanel = new JPanel();
		JScrollPane mediaFilesToAddListScrollPane = new JScrollPane();
		JTextArea mediaFilesToAddListTextArea = new JTextArea(3, 30);
		mediaFilesToAddListScrollPane.add(mediaFilesToAddListTextArea);
		mediaFilesToAddPanel.add(mediaFilesToAddListScrollPane);
		
		mediaFilesPanel.add(new JLabel("media files list"));
		mediaFilesToAddPanel.add(new JLabel("media files to add list"));
		
		JSplitPane addMultipleMoviesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, mainTop, createMediaFilesListPanel());
		
		all = new JPanel();
		all.setLayout(new BorderLayout());
		
		//all.add(optionsPanel, BorderLayout.WEST);
		all.add(addMultipleMoviesSplitPane, BorderLayout.CENTER);
		
		
		getContentPane().add(all, BorderLayout.CENTER);
		/* Packs and sets location... */
		pack();

		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

		loadConfigSettings();
	}

	

	
	JPanel makeListPanel() {

		JPanel listPanel = new JPanel();
		//listPanel.setLayout(new BorderLayout());
		listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		ArrayList<String> columnListNames = MovieManager.getIt().getDatabase().getListsColumnNames();
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

	
	
	
	void loadConfigSettings() {
		
		MovieManagerConfig config = MovieManager.getConfig();
		
		multiAddSelectOption = MovieManager.getConfig().getMultiAddSelectOption();

		switch (multiAddSelectOption) {
		case 0 : askButton.setSelected(true); break;
		case 1 : selectFirstHitButton.setSelected(true); break;
		case 2 : selectIfOnlyOneHitButton.setSelected(true); break;
		}


		titleOption.setSelected(config.getMultiAddTitleOption());
		enableExludeCodecInfo.setSelected(config.getMultiAddEnableExludeCodecInfo());
		enableExludeIntegers.setSelected(config.getMultiAddEnableExludeIntegers());
		enableExludeCDNotation.setSelected(config.getMultiAddEnableExludeCDNotation());
		enableExludeParantheses.setSelected(config.getMultiAddEnableExludeParantheses());

		// Regex string
		enabledRegEx.setSelected(config.getMultiAddRegexStringEnabled());
		regexNegate.setSelected(config.getMultiAddRegexStringNegated());
		regexTextField.setEnabled(config.getMultiAddRegexStringEnabled());
		regexTextField.setText(config.getMultiAddRegexString());
		
		// Include/Exclude string
		enableIncludeOrExludeString.setSelected(config.getMultiAddExcludeStringEnabled());
		regularStringNegate.setSelected(config.getMultiAddExcludeStringNegated());
		excludeStringTextField.setEnabled(config.getMultiAddExcludeStringEnabled());
		excludeStringTextField.setText(config.getMultiAddExcludeString());
	
		if (config.getMultiAddRegexStringEnabled() && !"".equals(config.getMultiAddRegexString()))
			fileTree.setRegexPattern(config.getMultiAddRegexString());
					
		fileTree.setRegexNegate(config.getMultiAddRegexStringNegated());
		
		if (config.getMultiAddExcludeStringEnabled() && !"".equals(config.getMultiAddExcludeString()))
			fileTree.setStringPattern(config.getMultiAddExcludeString());
					
		fileTree.setStringNegate(config.getMultiAddExcludeStringNegated());
		
		
		// Adding valid extension
		ArrayList<String> ext = MovieManager.getConfig().getMultiAddValidExtensions();
				
		if (ext.contains("avi"))
			aviExtension.setSelected(true);
		if (ext.contains("divx"))
			divxExtension.setSelected(true);
		if (ext.contains("mpeg"))
			mpegExtension.setSelected(true);
		if (ext.contains("ogm"))
			ogmExtension.setSelected(true);
		if (ext.contains("mkv"))
			mkvExtension.setSelected(true);
		
		customExtension.setText(MovieManager.getConfig().getMultiAddCustomExtensions());
		
		updateExtensionoOnTree();
	}
	
	
	void updateExtensionoOnTree() {
				
		ArrayList<String> validExtensions = new ArrayList<String>();
		
		if (aviExtension.isSelected())
			validExtensions.add("avi");
		
		if (divxExtension.isSelected())
			validExtensions.add("divx");
		
		if (mpegExtension.isSelected()) {
			validExtensions.add("mpeg");
			validExtensions.add("mpg");
		}
		
		if (ogmExtension.isSelected())
			validExtensions.add("ogm");
		
		if (mkvExtension.isSelected())
			validExtensions.add("mkv");
		
		
		String [] ext = customExtension.getText().split("\\s|,");
		
		for (int i = 0; i < ext.length; i++) {
			
			if (ext[i].length() > 0)
				validExtensions.add(ext[i].startsWith(".") ? ext[i].substring(1, ext[i].length()) : ext[i]);
		}
				
		fileTree.setValidExtension(validExtensions);
	}
	

	/*Saves the options to the MovieManager object*/
	public void executeSave() {
		
		MovieManagerConfig config = MovieManager.getConfig();
		
		if (listChooser != null) {
			config.setMultiAddList((String) listChooser.getSelectedItem());

			if (enableAddMoviesToList.isSelected())
				config.setMultiAddListEnabled(true);
			else
				config.setMultiAddListEnabled(false);
		}

		config.setMultiAddSelectOption(multiAddSelectOption);
		config.setMultiAddEnableExludeParantheses(enableExludeParantheses.isSelected());
		config.setMultiAddEnableExludeCDNotation(enableExludeCDNotation.isSelected());
		config.setMultiAddEnableExludeIntegers(enableExludeIntegers.isSelected());
		config.setMultiAddEnableExludeCodecInfo(enableExludeCodecInfo.isSelected());
		config.setMultiAddTitleOption(titleOption.isSelected());
	
		// Regex string
		config.setMultiAddRegexStringEnabled(enabledRegEx.isSelected());
		config.setMultiAddRegexString(regexTextField.getText());
		config.setMultiAddRegexStringNegated(regexNegate.isSelected());
		
		// Regular string
		config.setMultiAddExcludeStringEnabled(enableIncludeOrExludeString.isSelected());
		config.setMultiAddExcludeString(excludeStringTextField.getText());
		config.setMultiAddExcludeStringNegated(regularStringNegate.isSelected());
		
		
		
		ArrayList<String> ext = new ArrayList<String>();
		
		if (aviExtension.isSelected())
			ext.add("avi");
		if (divxExtension.isSelected())
			ext.add("divx");
		if (mpegExtension.isSelected())
			ext.add("mpeg");
		if (ogmExtension.isSelected())
			ext.add("ogm");
		if (mkvExtension.isSelected())
			ext.add("mkv");
					
		MovieManager.getConfig().setMultiAddValidExtension(ext);
		MovieManager.getConfig().setMultiAddCustomExtensions(customExtension.getText());
	}
	
	

	void addFilesToAddToList(ArrayList<FileNode> files) {

		DefaultListModel model = (DefaultListModel) filesToAddList.getModel();

		for (int i = 0; i < files.size(); i++) {

			if (!nodesInFileLists.containsKey(files.get(i))) {
				nodesInFileLists.put(files.get(i), files.get(i));
				model.addElement(files.get(i));
			}
		}
	}
	
	
	void addFilesToFileList(ArrayList<FileNode> files) {
		
		DefaultListModel model = (DefaultListModel) mediaFileList.getModel();
		
		 for (int i = 0; i < files.size(); i++) {
			 
			 if (!nodesInFileLists.containsKey(files.get(i))) {
				 nodesInFileLists.put(files.get(i), files.get(i));
				 model.addElement(files.get(i));
			 }
		 }
	}		
	
	
	/*Returns the user defined exlude string*/
	public String getMultiAddExcludeString() {
		return excludeStringTextField.getText();
	}

	public boolean getMultiAddExcludeStringEnabled() {
		return enableIncludeOrExludeString.isSelected();
	}

	public boolean getMultiAddExcludeOrIncludeNegated() {
		return regularStringNegate.isSelected();
	}
	
	/*Returns the user defined exlude string*/
	public String getMultiAddRegexString() {
		return regexTextField.getText();
	}

	public boolean getMultiAddRegexEnabled() {
		return enableIncludeOrExludeString.isSelected();
	}
	
	public boolean getMultiAddRegexNegated() {
		return regexNegate.isSelected();
	}
	
	
	/*returns the value of the multiAddSelectOption variable.( values 0-2)*/
	public int getMultiAddSelectOption() {
		return multiAddSelectOption;
	}

	public boolean validateAddList() {
				
		log.debug("validateAddList");
		
		DefaultListModel listModel = (DefaultListModel) filesToAddList.getModel();
		
		System.err.println("listModel.getSize():" + listModel.getSize());
		System.err.println("addListMustContainValidItemsAlert:" + addListMustContainValidItemsAlert);
		
		if (listModel.getSize() == 0 || addListMustContainValidItemsAlert) {
			
			// Does not contain the alert, it is added
			if (!addListMustContainValidItemsAlert) {
				listModel.addElement("List must contain movies to add");
				addListMustContainValidItemsAlert = true;
			}
			System.err.println("return false");
			return false;
		}
		
		return true;
	}
	
	

	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

		if (event.getSource().equals(buttonUpdateFileList)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
			executeSave();
									
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			ArrayList<FileNode> files =  fileTree.getFilesFromDirectoryTree(true);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			
			if (files == null || files.size() == 0) {
				
				DialogAlert alert;
				if (fileTree.getValidExtension().size() == 0) {
					alert = new DialogAlert(this, "No extensions selected", "<html>You must specify valid file extensions in the options menu.</html>", true);
				}
				else
					alert = new DialogAlert(this, "No files marked", "<html>Right click the file tree and select an option<br> for the directories to be searched through.</html>", true);
				
				GUIUtil.showAndWait(alert, true);
			}
			else
				addFilesToFileList(files);
		}

		if (event.getSource().equals(buttonCancel)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
			executeSave();
			dispose();
		}

		if (event.getSource().equals(buttonAddMovies)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		//	executeSave();
			// Another listener on this button handles the rest
		}

		if (event.getSource().equals(buttonAddList)) {
			
			MovieManagerCommandLists.execute(this);

			//optionsPanel.remove(5);
			
			optionsPanel.remove(listPanel);
			
			listPanel = makeListPanel();
			
			optionsPanel.add(listPanel, 4);
			pack();
			GUIUtil.show(this, true);
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
	
	
	void addSelectedMediaFilesToAddList() {
		
		DefaultListModel mediaFilelistModel = (DefaultListModel) mediaFileList.getModel();
		DefaultListModel fileToAddListModel = (DefaultListModel) filesToAddList.getModel();
				
		Object[] selectedValues = mediaFileList.getSelectedValues(); 
		
		 for (int i = 0; i < selectedValues.length; i++) {
			 fileToAddListModel.addElement(selectedValues[i]);
			 mediaFilelistModel.removeElement(selectedValues[i]);
		 }
	}
	
	public ArrayList<FileNode> getMoviesToAdd() {
		
		ArrayList<FileNode> list = new ArrayList<FileNode>();
		
		DefaultListModel listModel = (DefaultListModel) filesToAddList.getModel();
		
		Enumeration<FileNode> enumeration = (Enumeration<FileNode>) listModel.elements();
		
		while (enumeration.hasMoreElements()) {
			list.add(enumeration.nextElement());
		}
				
		return list;
	}

	protected JPanel createMediaFilesListPanel() {

		log.debug("Start creation of the Files List panel."); //$NON-NLS-1$
		JPanel filesListPanel = new JPanel();
		filesListPanel.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
		filesListPanel.setLayout(new BorderLayout());

		/* The media files panel. */
		JPanel mediaFilesListPanel = new JPanel();
		mediaFilesListPanel.setLayout(new BorderLayout());
	
		//mediaFileList.addComponentListener(this);
		mediaFilesListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Media files", //$NON-NLS-1$
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font(mediaFilesListPanel.getFont().getName(),Font.PLAIN, 12)),
				BorderFactory.createEmptyBorder(1,5,3,5))));
		mediaFileList = new JList(); //$NON-NLS-1$
		mediaFileList.setModel(new DefaultListModel()); 

		JScrollPane mediaFileListScrollPane = new JScrollPane(mediaFileList);
		mediaFileListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Button panel

		JPanel mediaFileListButtonPanel = new JPanel();
		mediaFileListButtonPanel.setLayout(new BoxLayout(mediaFileListButtonPanel, BoxLayout.Y_AXIS));

		JButton clearFileList = new JButton("Clear left");
		JButton clearToAddFileList = new JButton("Clear right");

		clearFileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel model = (DefaultListModel) mediaFileList.getModel();
				Object [] elems = model.toArray();
				model.removeAllElements();
				
				for (int i = 0; i < elems.length; i++)
					nodesInFileLists.remove(elems[i]);
			}
		});

		clearToAddFileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel model = (DefaultListModel) filesToAddList.getModel();
				Object [] elems = model.toArray();
				model.removeAllElements();
				
				for (int i = 0; i < elems.length; i++)
					nodesInFileLists.remove(elems[i]);
			}
		});


		JButton addSelectedMediaFilesToAddList = new JButton("Add selected");

		addSelectedMediaFilesToAddList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (addListMustContainValidItemsAlert) {
					DefaultListModel listModel = (DefaultListModel) filesToAddList.getModel();
					listModel.clear();
					addListMustContainValidItemsAlert = false;
				}
				addSelectedMediaFilesToAddList();
			}
		});

		mediaFileListButtonPanel.add(clearFileList);
		mediaFileListButtonPanel.add(clearToAddFileList);
		mediaFileListButtonPanel.add(addSelectedMediaFilesToAddList);

		mediaFilesListPanel.add(mediaFileListScrollPane, BorderLayout.CENTER);
		mediaFilesListPanel.add(mediaFileListButtonPanel, BorderLayout.EAST);


		/* The files to add panel. */
		JPanel filesToAddListPanel = new JPanel();
		filesToAddListPanel.setLayout(new BorderLayout());
		//filesToAddListPanel.addComponentListener(this);

		filesToAddListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,5), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Media files to be added", //$NON-NLS-1$
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font(filesToAddListPanel.getFont().getName(),Font.PLAIN, 12)),
				BorderFactory.createEmptyBorder(1,5,3,5))));

		filesToAddList = new JList(); //$NON-NLS-1$
		filesToAddList.setModel(new DefaultListModel()); 

		JScrollPane scrollPaneNotes = new JScrollPane(filesToAddList);
		scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		filesToAddListPanel.add(scrollPaneNotes, BorderLayout.CENTER);

		JSplitPane fileListsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mediaFilesListPanel, filesToAddListPanel);
		fileListsSplitPane.setOneTouchExpandable(true);
		fileListsSplitPane.setContinuousLayout(true);
		fileListsSplitPane.setDividerSize(7);
		fileListsSplitPane.setResizeWeight(0.5);

		filesListPanel.add(fileListsSplitPane, BorderLayout.CENTER);


		/* All done. */
		log.debug("Creation of the media file list panel done."); //$NON-NLS-1$
		return filesListPanel;
	}

	

}
