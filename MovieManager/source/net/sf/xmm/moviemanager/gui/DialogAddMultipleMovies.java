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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.MultiAddProgressBar;
import net.sf.xmm.moviemanager.SimpleProgressBar;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileTreeEvent;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileTreeEventListener;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileNode;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileTree;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.ProgressBean;
import net.sf.xmm.moviemanager.util.ProgressBeanImpl;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.StringUtil.FilenameCloseness;

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
	public JCheckBox enableExludeUserdefinedInfo;
	public JCheckBox enableExludeAllAfterMatchOnUserDefinedInfo;
	public JCheckBox enableSearchNfoForImdb;
	public JCheckBox enableSkipHiddenDirectories;
	public JCheckBox enableUseFolderName;
	public JCheckBox enableSelectFirstHitMark;
	
	public JCheckBox enableAutomaticCombine;
	public JCheckBox enableUseParentFolderIfCD;

	private JTextField userDefinedInfo;
	private JTextArea customExtension;
	
	private JCheckBox anyExtension;
	private JCheckBox aviExtension;
	private JCheckBox divxExtension;
	private JCheckBox mpegExtension;
	private JCheckBox ogmExtension;
	private JCheckBox mkvExtension;
	private JCheckBox mp4Extension;
	
	JCheckBox enabledRegEx;
	JCheckBox regexNegate;
	JCheckBox regexCaseSensitive;
	
	JCheckBox filterOutDuplicates;
	JCheckBox filterOutDuplicatesEntireFilePath;
	
	public JCheckBox enableAddMoviesToList;
	public JComboBox listChooser;

	JList mediaFileList;
	JList filesToAddList;
	
	JPanel listPanel;
	JPanel regularOptionsPanel;
	
	FileTree fileTree;
	
	JPanel imdbOptionsPanel;
	JPanel optionsPanel;
	JPanel all;
	
	private HashMap<String, Files> nodesInFileLists = new HashMap<String, Files>();
		
	boolean mediaFilesInDatabaseAdded = false;
	
	private boolean addListMustContainValidItemsAlert = false; // denotes if the list contains an alert
	private boolean noFilesMatchListAlert = false;
	
	DialogAddMultipleMovies thisDialog = this;
	MultiAddProgressBar progressBar;
	    
	int fileTreeThreadsWorking = 0;
	
	private ImdbImportOption multiAddSelectOption;

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

		enableSelectFirstHitMark = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-hits.option-select-first-hit-mark.text")); //$NON-NLS-1$
		enableSelectFirstHitMark.setActionCommand("enableSelectFirstHitMark"); //$NON-NLS-1$
		enableSelectFirstHitMark.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-hits.option-select-first-hit-mark-tooltip")); //$NON-NLS-1$
		enableSelectFirstHitMark.addActionListener(this);
		
		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(askButton);
		radioButtonGroup.add(selectFirstHitButton);
		radioButtonGroup.add(selectIfOnlyOneHitButton);

		
		JPanel imdbRadioButtonPanel = new JPanel(new GridLayout(0, 1));
		imdbRadioButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-hits.title"))), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		imdbRadioButtonPanel.add(askButton);
		imdbRadioButtonPanel.add(selectFirstHitButton);
		imdbRadioButtonPanel.add(selectIfOnlyOneHitButton);
		imdbRadioButtonPanel.add(enableSelectFirstHitMark);

		
		/*
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
		
		enableExludeUserdefinedInfo = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-user-defined-info.text")); //$NON-NLS-1$
		enableExludeUserdefinedInfo.setActionCommand("enableExludeUserdefinedInfo"); //$NON-NLS-1$
		enableExludeUserdefinedInfo.addItemListener(new ItemListener() {
		
			public void itemStateChanged(ItemEvent e) {
				boolean selected = enableExludeUserdefinedInfo.isSelected();
				enableExludeAllAfterMatchOnUserDefinedInfo.setEnabled(selected);
				userDefinedInfo.setEnabled(selected);
				if (!selected)
					enableExludeAllAfterMatchOnUserDefinedInfo.setSelected(false);
			}
		});
		
		// yes that is a ridiculously long variable name
		enableExludeAllAfterMatchOnUserDefinedInfo = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-clean-string.remove-everything-after-match-on-user-defined-info.text")); //$NON-NLS-1$
		enableExludeAllAfterMatchOnUserDefinedInfo.setToolTipText("Remove"); //$NON-NLS-1$
		enableExludeAllAfterMatchOnUserDefinedInfo.setEnabled(false);
		
		// JTextField is not as nice displayed as JTextArea but it has a fixed width and a larger string is scrolled within instead of growing in width with JTextArea.
		userDefinedInfo = new JTextField();
		userDefinedInfo.setBorder(BorderFactory.createEtchedBorder());
		userDefinedInfo.setEnabled(false);
		
		JPanel cleanStringPanel = new JPanel(new GridLayout(0, 1));
		cleanStringPanel.add(enableExludeParantheses);
		cleanStringPanel.add(enableExludeCDNotation);
		cleanStringPanel.add(enableExludeIntegers);
		cleanStringPanel.add(enableExludeUserdefinedInfo);
		cleanStringPanel.add(enableExludeAllAfterMatchOnUserDefinedInfo);
		cleanStringPanel.add(userDefinedInfo);
				
		cleanStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogAddMultipleMovies.panel-clean-string.title"))), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		
		enableUseFolderName = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming.text")); //$NON-NLS-1$
		enableUseFolderName.setActionCommand("enableFolderTitle"); //$NON-NLS-1$
		enableUseFolderName.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming-tooltip")); //$NON-NLS-1$
		enableUseFolderName.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent e) {
				boolean selected = enableUseFolderName.isSelected();
				enableUseParentFolderIfCD.setEnabled(selected);
				if (!selected)
					enableUseParentFolderIfCD.setSelected(false);
			}
		});
	
		enableUseParentFolderIfCD = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming-nocd.text")); //$NON-NLS-1$
		enableUseParentFolderIfCD.setActionCommand("enableTitleOptionNoCd"); //$NON-NLS-1$
		enableUseParentFolderIfCD.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Naming-nocd-tooltip")); //$NON-NLS-1$
		enableUseParentFolderIfCD.setEnabled(false);
		
		JPanel titleOptionPanel = new JPanel(new GridLayout(0, 1));
		titleOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Title options")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		titleOptionPanel.add(enableUseFolderName);
		titleOptionPanel.add(enableUseParentFolderIfCD);
		
		JPanel miscOptionsPanel = new JPanel(new GridLayout(0, 1));
		miscOptionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Miscellaneous options")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
				
		enableSearchNfoForImdb = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Nfo-scanning.text")); //$NON-NLS-1$
		enableSearchNfoForImdb.setActionCommand("enableSearchNfoForImdb"); //$NON-NLS-1$
		enableSearchNfoForImdb.setToolTipText(Localizer.getString("DialogAddMultipleMovies.panel-options.enable-Folder-Nfo-scanning-tooltip")); //$NON-NLS-1$
		
		miscOptionsPanel.add(enableSearchNfoForImdb);
			
		enableSkipHiddenDirectories = new JCheckBox("Skip hidden directories");
		enableSkipHiddenDirectories.setActionCommand("enableSkipHiddenDirectories"); //$NON-NLS-1$
		enableSkipHiddenDirectories.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.setSkipHiddenDirectories(enableSkipHiddenDirectories.isSelected());
			}
		});		
		
		miscOptionsPanel.add(enableSkipHiddenDirectories);
				
		anyExtension = new JCheckBox("*(any)");
		aviExtension = new JCheckBox("avi");
		divxExtension = new JCheckBox("divx");
		mpegExtension = new JCheckBox("mpeg");
		ogmExtension = new JCheckBox("ogm");
		mkvExtension = new JCheckBox("mkv");
		mp4Extension = new JCheckBox("mp4");
				
		ActionListener extListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateExtensionoOnTree();
			}
		};
		
		anyExtension.addActionListener(extListener);
		aviExtension.addActionListener(extListener);
		divxExtension.addActionListener(extListener);
		mpegExtension.addActionListener(extListener);
		ogmExtension.addActionListener(extListener);
		mkvExtension.addActionListener(extListener);
		mp4Extension.addActionListener(extListener);
		
		JPanel validExtension = new JPanel();
		validExtension.setLayout(new GridLayout(2, 4));
		validExtension.add(anyExtension);
		validExtension.add(aviExtension);
		validExtension.add(mpegExtension);
		validExtension.add(divxExtension);
		validExtension.add(ogmExtension);
		validExtension.add(mkvExtension);
		validExtension.add(mp4Extension);
		
		customExtension = new JTextArea();
		customExtension.setBorder(BorderFactory.createEtchedBorder());

		customExtension.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateExtensionoOnTree();
			}
		});
				
		Font font = customExtension.getFont();
		customExtension.setFont(new Font(font.getFontName(), font.getStyle(), 15));
		customExtension.setToolTipText("Specify additional extensions separated by comma");
		
		JPanel extensionPanel = new JPanel(new BorderLayout());
		extensionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "File extensions")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
		extensionPanel.add(validExtension, BorderLayout.NORTH);
		extensionPanel.add(customExtension, BorderLayout.CENTER);
		
		//extensionPanel
		
		/* Add to list */

		listPanel = makeListPanel();
		
		buttonUpdateFileList = new JButton("Find matching files"); //$NON-NLS-1$
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

		JTabbedPane tabbedPane = new JTabbedPane();
		
		imdbOptionsPanel = new JPanel();
		imdbOptionsPanel.setLayout(new BoxLayout(imdbOptionsPanel, BoxLayout.Y_AXIS));
		imdbOptionsPanel.add(imdbRadioButtonPanel);
		imdbOptionsPanel.add(cleanStringPanel);
		
		regularOptionsPanel = new JPanel();
		regularOptionsPanel.setLayout(new BoxLayout(regularOptionsPanel, BoxLayout.Y_AXIS));
		regularOptionsPanel.add(titleOptionPanel);
		regularOptionsPanel.add(miscOptionsPanel);
		regularOptionsPanel.add(extensionPanel);
		regularOptionsPanel.add(listPanel);
		
		tabbedPane.add("IMDb settings", imdbOptionsPanel);
		tabbedPane.add("Options", regularOptionsPanel);
		
		optionsPanel = new JPanel();
		optionsPanel.setLayout(new BorderLayout());
		optionsPanel.add(tabbedPane, BorderLayout.CENTER);
		optionsPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		fileTree = new FileTree();
		
		fileTree.eventHandler.addFileTreeEventListener(new FileTreeEventListener() {
			
			/**
			 * Removes any directories in the list, and calls addFilesToAddToList
			 */
			public void addSelectedFilesEventOccurred(FileTreeEvent evt) {
				ArrayList<FileNode> files =  fileTree.getSelectedFiles();
				
				// Remove directories
				for (int i = 0; i < files.size(); i++) {
					if (files.get(i).isDirectory()) {
						files.remove(files.get(i));
						i--;
					}
				}
				addFilesToAddToList(files);
			}

			public void rootDeviceAddedEventOccurred(FileTreeEvent evt) {
				File f = evt.getFile();
				MovieManager.getConfig().addMultiAddRootDevice(f.getAbsolutePath());
			}

			public void rootDeviceRemovedEventOccurred(FileTreeEvent evt) {
				File f = evt.getFile();
				MovieManager.getConfig().removeMultiAddRootDevice(f.getAbsolutePath());
			}

			public void fileTreeIsWorkingEvent(FileTreeEvent evt) {

				fileTreeThreadsWorking++;
				
				fileTree.setCursor(new Cursor(Cursor.WAIT_CURSOR));

				if (evt.isSearching()) {

					// Shows the progress bar after some time has passed
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							if (!fileTree.isReady())
								GUIUtil.show(progressBar, true);
						}					
					});
				}
			}

			public void fileTreeIsReadyEvent(FileTreeEvent evt) {
				
				fileTreeThreadsWorking--;
								
				if (fileTreeThreadsWorking == 0)
					fileTree.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				
				if (evt.isSearching()) {
					GUIUtil.show(progressBar, false);
				}
			}
		});
		
		progressBar = new MultiAddProgressBar(MovieManager.getDialog(), "Searching...", true, fileTree);
		
		// Adding previously defined root devices
		String [] rootDevices = MovieManager.getConfig().getMultiAddRootDevices();
		for (String device : rootDevices) {
			fileTree.addRootDevice(new File(device));		
		}
		
		
	
		// Regular expression
				
		enabledRegEx = new JCheckBox("Enable:");
		enabledRegEx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				regexTextField.setEnabled(enabledRegEx.isSelected());
				regexNegate.setEnabled(enabledRegEx.isSelected());
				regexCaseSensitive.setEnabled(enabledRegEx.isSelected());
				updateRegexPattern();
			}
		});
		
		regexNegate = new JCheckBox("Negate");
		regexNegate.setToolTipText("Invert the results");
		regexNegate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.setRegexNegate(regexNegate.isSelected());
			}
		});		
		
		regexCaseSensitive = new JCheckBox("Case sensitive");
		regexCaseSensitive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateRegexPattern();
			}
		});		
		
		regexTextField = new JTextField(" ");
		regexTextField.setToolTipText("Regular expressions is supported");
		regexTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void	keyTyped(KeyEvent e) {}
		
			public void keyReleased(KeyEvent e) {
				updateRegexPattern();
			}
		});
			
						
		double size[][] =
         {{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED},
          {0.5, 0.5}};

//		 Include Or Exclude Strings
		JPanel includeOrExcludeStringsAndRegex = new JPanel();
		includeOrExcludeStringsAndRegex.setLayout(new TableLayout(size));
		
		includeOrExcludeStringsAndRegex.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "String matching on filename"), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
				
		includeOrExcludeStringsAndRegex.add(enabledRegEx,   "0, 0");
		includeOrExcludeStringsAndRegex.add(regexTextField, "1, 0");
		includeOrExcludeStringsAndRegex.add(regexNegate,    "0, 1");
		includeOrExcludeStringsAndRegex.add(regexCaseSensitive,    "1, 1");

		filterOutDuplicates = new JCheckBox("Ignore media files already in database"); 
		filterOutDuplicates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFilterOutDuplicates(filterOutDuplicates.isSelected());
			}
		});

		
		filterOutDuplicatesEntireFilePath = new JCheckBox("Use entire file path");
		filterOutDuplicatesEntireFilePath.setEnabled(false);
		filterOutDuplicatesEntireFilePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.setFilterOutDuplicatesByEntireFilePath(filterOutDuplicatesEntireFilePath.isSelected());
			}
		});
		
		JPanel duplicatesPanel = new JPanel();
		duplicatesPanel.add(filterOutDuplicates);
		duplicatesPanel.add(filterOutDuplicatesEntireFilePath);
		
		JPanel filterPanel = new JPanel();
		filterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4,4,4,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Filter files"), BorderFactory.createEmptyBorder(4,4,4,4)))); //$NON-NLS-1$
				
		filterPanel.setLayout(new BorderLayout());
		filterPanel.add(duplicatesPanel, BorderLayout.NORTH);
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
		
		all.add(addMultipleMoviesSplitPane, BorderLayout.CENTER);
		
		
		getContentPane().add(all, BorderLayout.CENTER);
		/* Packs and sets location... */
		pack();

		// Reduce height by 100
		Dimension dim = getSize();
		dim.height = dim.height - 150;
		setSize(dim);
		
		addMultipleMoviesSplitPane.setDividerLocation(0.60);
		
		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

		loadConfigSettings();

	}

	void updateRegexPattern() {
		String expression = regexTextField.getText().trim();
		
		if (!expression.equals("") && enabledRegEx.isSelected()) {
			fileTree.setRegexPattern(expression, regexCaseSensitive.isSelected());
		}
		else
			fileTree.setRegexPattern(null, false);
	}
	
	void setFilterOutDuplicates(boolean value) {
					
		filterOutDuplicatesEntireFilePath.setEnabled(filterOutDuplicates.isSelected());
		
		fileTree.setFilterOutDuplicates(value);
		
		// Adding all the file paths of each media file from the database
		if (filterOutDuplicates.isSelected() && !mediaFilesInDatabaseAdded) {

			mediaFilesInDatabaseAdded = true;
			
			ArrayList<ModelMovie> list = MovieManager.getIt().getDatabase().getMoviesList("Title");
			DefaultListModel movies = GUIUtil.toDefaultListModel(list);
			
			for (int i = 0; i < movies.size(); i++) {

				ModelEntry model = (ModelEntry) movies.get(i);
				
				if (!model.getHasAdditionalInfoData())
					model.updateAdditionalInfoData();
				
				String fileLocation = model.getAdditionalInfo().getFileLocation();

				if (fileLocation.trim().equals(""))
					continue;
				
				String [] files = fileLocation.split("\\*");
				
				for (int u = 0; u < files.length; u++) {
					fileTree.addExistingMediaFileInDatabase(files[u], model);
				}
			}
		}
	}

	
	JPanel makeListPanel() {

		JPanel listPanel = new JPanel();
		listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.getString("DialogAddMultipleMovies.panel-add-to-list.title"))), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

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
		case displayList : askButton.setSelected(true); break;
		case selectFirst : selectFirstHitButton.setSelected(true); break;
		case selectFirstOrAddToSkippedList : selectFirstHitButton.setSelected(true); break;
		case selectIfOnlyOneHit : selectIfOnlyOneHitButton.setSelected(true); break;
		case selectIfOnlyOneHitOrAddToSkippedList : selectIfOnlyOneHitButton.setSelected(true); break;
		default: askButton.setSelected(true);
		}


		enableUseFolderName.setSelected(config.getMultiAddTitleOption());
		enableExludeAllAfterMatchOnUserDefinedInfo.setSelected(config.getMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo());
		enableExludeIntegers.setSelected(config.getMultiAddEnableExludeIntegers());
		enableExludeCDNotation.setSelected(config.getMultiAddEnableExludeCDNotation());
		enableExludeParantheses.setSelected(config.getMultiAddEnableExludeParantheses());
		enableUseParentFolderIfCD.setSelected(config.getMultiAddTitleOptionNoCd());
		enableSearchNfoForImdb.setSelected(config.getMultiAddSearchNfoForImdb());
		
		
		enableSelectFirstHitMark.setSelected(config.getMultiAddSelectFirstHitMark());
		enableExludeUserdefinedInfo.setSelected(config.getMultiAddEnableExludeUserdefinedInfo());
		enableAutomaticCombine.setSelected(config.getMultiAddEnableAutomaticCombine());

		// Simulate click so that actionlistener is triggered.
		if (MovieManager.getConfig().getMultiAddSkipHiddenDirectories())
			enableSkipHiddenDirectories.doClick();
		
		// Regex string
		enabledRegEx.setSelected(config.getMultiAddRegexStringEnabled());
		regexTextField.setText(config.getMultiAddRegexString());
		regexNegate.setSelected(config.getMultiAddRegexStringNegated());
		regexCaseSensitive.setSelected(config.getMultiAddRegexCaseSensitive());
		
		regexTextField.setEnabled(config.getMultiAddRegexStringEnabled());
		regexNegate.setEnabled(config.getMultiAddRegexStringEnabled());
		regexCaseSensitive.setEnabled(config.getMultiAddRegexStringEnabled());
	
		fileTree.setRegexNegate(config.getMultiAddRegexStringNegated());
	
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
		if (ext.contains("mp4"))
			mp4Extension.setSelected(true);
				
		customExtension.setText(MovieManager.getConfig().getMultiAddCustomExtensions());
		
		// Avoid textfield being resized after text is set...
		userDefinedInfo.setPreferredSize(userDefinedInfo.getSize());
		
		// Custom exclude string
		if (MovieManager.getConfig().getMultiAddExcludeUserDefinedString().equals(""))
			userDefinedInfo.setText("divx,xvid,dvdrip,ac3,mp3,uncut,recut,dl,bluray,x264,720p,1080p");
		else
			userDefinedInfo.setText(MovieManager.getConfig().getMultiAddExcludeUserDefinedString());
		
		userDefinedInfo.setCaretPosition(0);
	
		
		updateRegexPattern();
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
		
		if (mp4Extension.isSelected())
			validExtensions.add("mp4");
			
		
		String [] ext = customExtension.getText().trim().split("\\s*,\\s*");
		
		for (int i = 0; i < ext.length; i++) {
			
			if (ext[i].length() > 0)
				validExtensions.add(ext[i].startsWith(".") ? ext[i].substring(1, ext[i].length()) : ext[i]);
		}
		
		if (anyExtension.isSelected()) {
			fileTree.setAllowAnyExtension(true);
		}
		else
			fileTree.setAllowAnyExtension(false);		
	
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
		config.setMultiAddTitleOption(enableUseFolderName.isSelected());
		config.setMultiAddTitleOptionNoCd(enableUseParentFolderIfCD.isSelected());
		config.setMultiAddEnableAutomaticCombine(enableAutomaticCombine.isSelected());
		config.setMultiAddSearchNfoForImdb(enableSearchNfoForImdb.isSelected());
		config.setMultiAddSkipHiddenDirectories(enableSkipHiddenDirectories.isSelected());
		config.setMultiAddSelectFirstHitMark(enableSelectFirstHitMark.isSelected());
		config.setMultiAddEnableExludeUserdefinedInfo(enableExludeUserdefinedInfo.isSelected());
		config.setMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo(enableExludeAllAfterMatchOnUserDefinedInfo.isSelected());
		
		config.setMultiAddExcludeUserDefinedString(userDefinedInfo.getText());
		
		// Regex string
		config.setMultiAddRegexStringEnabled(enabledRegEx.isSelected());
		config.setMultiAddRegexString(regexTextField.getText());
		config.setMultiAddRegexStringNegated(regexNegate.isSelected());
		config.setMultiAddRegexCaseSensitive(regexCaseSensitive.isSelected());
			
		
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
		if (mp4Extension.isSelected())
			ext.add("mp4");
					
		MovieManager.getConfig().setMultiAddValidExtension(ext);
		MovieManager.getConfig().setMultiAddCustomExtensions(customExtension.getText());
	}
	
	

	void addFilesToAddToList(ArrayList<FileNode> files) {

		DefaultListModel model = (DefaultListModel) filesToAddList.getModel();

		for (int i = 0; i < files.size(); i++) {

			if (!nodesInFileLists.containsKey(files.get(i).getFile().getAbsolutePath())) {
				Files newFile = new Files(files.get(i).getFile());
				nodesInFileLists.put(files.get(i).getFile().getAbsolutePath(), newFile);
				model.addElement(newFile);
			}
		}
	}
	
	/**
	 * Adds the files in the arraylist to the list of matching media files
	 * @param files
	 */
	void addFilesToFileList(ArrayList<FileNode> files) {
		
		DefaultListModel model = (DefaultListModel) mediaFileList.getModel();
		
		model.clear();
		
		 for (int i = 0; i < files.size(); i++) {
			 File f = ((FileNode) files.get(i)).getFile();
			 model.addElement(new Files(f));
		 }
	}		
	
	void addSelectedMediaFilesToAddList() {
		
		DefaultListModel mediaFilelistModel = (DefaultListModel) mediaFileList.getModel();
		DefaultListModel fileToAddListModel = (DefaultListModel) filesToAddList.getModel();
				
		// No valid entries in the list
		if (noFilesMatchListAlert) {
			return;
		}
		
		Object[] selectedValues = mediaFileList.getSelectedValues(); 
		
		 for (int i = 0; i < selectedValues.length; i++) {
			 mediaFilelistModel.removeElement(selectedValues[i]);
		 
			 if (!nodesInFileLists.containsKey(((Files) selectedValues[i]).getFile().getAbsolutePath())) {
				 fileToAddListModel.addElement(selectedValues[i]);
				 nodesInFileLists.put(((Files) selectedValues[i]).getFile().getAbsolutePath(), (Files) selectedValues[i]);
			 }
		 }
		 
		 calculateSimilarity(fileToAddListModel);
	}
	
	/** 
	 * Returns the user defined exclude string
	 */
	public String getMultiAddExcludeString() {
		return excludeStringTextField.getText();
	}

	public boolean getEnableExludeUserdefinedInfo() {
		return enableExludeUserdefinedInfo.isSelected();
	}

		
	/** 
	 * Returns the user defined regex 
	 */
	public String getMultiAddRegexString() {
		return regexTextField.getText();
	}

	public boolean getMultiAddRegexEnabled() {
		return enabledRegEx.isSelected();
	}
	
	public boolean getMultiAddRegexNegated() {
		return regexNegate.isSelected();
	}
	

	/**
	 * Returns the value of the multiAddSelectOption variable.
	 */
	public ImdbImportOption getMultiAddSelectOption() {
		return multiAddSelectOption;
	}

	public boolean validateAddList() {
		
		DefaultListModel listModel = (DefaultListModel) filesToAddList.getModel();
		
		if (listModel.getSize() == 0 || addListMustContainValidItemsAlert) {
			
			// Does not contain the alert, it is added
			if (!addListMustContainValidItemsAlert) {
				listModel.addElement("List must contain movies to add");
				addListMustContainValidItemsAlert = true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Updates the list of matching files in the list of matches
	 */
	void handleUpdateFileList() {
		executeSave();
		
		Thread t = new Thread(new Runnable() {

			public void run() {
				
				ArrayList<FileNode> files =  fileTree.getFilesFromDirectoryTree(true);
				
				if (files == null || files.size() == 0) {
					
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								DialogAlert alert;
								if (fileTree.getValidExtension().size() == 0) {
									alert = new DialogAlert(thisDialog, "No extensions selected", "<html>You must specify valid file extensions in the options menu.</html>", true);
									GUIUtil.showAndWait(alert, true);
								}
								else if (fileTree.getChosenFoldersCount() == 0) {
									alert = new DialogAlert(thisDialog, "Search option missing", "<html>Right click the file tree and select an option<br> for the directories to be searched through.</html>", true);
									GUIUtil.showAndWait(alert, true);
								}
								else {
									
									DefaultListModel listModel = (DefaultListModel) mediaFileList.getModel();
									
									// Add notice to list that no files were found
									if (!noFilesMatchListAlert) {
										listModel.clear();
										listModel.addElement("No matching files found");
										noFilesMatchListAlert = true;
									}
								}
							}
						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					noFilesMatchListAlert = false;
					addFilesToFileList(files);
				}
			}
		});
		t.start();
		
	}

	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

		if (event.getSource().equals(buttonUpdateFileList)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
			handleUpdateFileList();
		}

		if (event.getSource().equals(buttonCancel)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
			executeSave();
			dispose();
		}

		if (event.getSource().equals(buttonAddList)) {
			
			MovieManagerCommandLists.execute(this);
			SwingUtilities.invokeLater(new Runnable() {
				/* Must do changed on EDT to be safe */
				public void run() {
					regularOptionsPanel.remove(listPanel);
					listPanel = makeListPanel();
					regularOptionsPanel.add(listPanel);
				}
			});
		}
			
		if (event.getSource().equals(enableAddMoviesToList)) {

			if (enableAddMoviesToList.isSelected())
				listChooser.setEnabled(true);
			else
				listChooser.setEnabled(false);
		}

		if (event.getSource().equals(askButton))
			multiAddSelectOption = ImdbImportOption.displayList;

		if (event.getSource().equals(selectFirstHitButton))
			multiAddSelectOption = ImdbImportOption.selectFirst;

		if (event.getSource().equals(selectIfOnlyOneHitButton))
			multiAddSelectOption = ImdbImportOption.selectIfOnlyOneHit;
			
	}
	
	
	/**
	 * Used in the list of media files to add. 
	 * Files can contain multiple filenames when two or more files are combined in the list
	 */
	public class Files {
		
		File file = null;
		
		ArrayList<Files> addedFiles = new ArrayList<Files>();
		
		public Color similarityColor = null;
		
		Files(File f) {
			file = f;
		}
		
		public String getName() {
			if (addedFiles.size() == 0 )
				return file.getName();

			return null;
		}
		
		public void addFile(Files f) {
			addedFiles.add(f);
			
			Files [] files = f.getAddedFiles();
			
			for (int i = 0; i < files.length; i++) {
				addedFiles.add(files[i]);
			}
			
			f.clearAddedFiles();
		}
		
		public File getFile() {
			return file;
		}
		
		public Files [] getAddedFiles() {
			return addedFiles.toArray(new Files[addedFiles.size()]);
		}
		
		public void clearAddedFiles() {
			addedFiles.clear();
		}
		
		public String toString() {
			
			String str = file.getName();
			
			for (Files f : addedFiles)
				str += " " + f.getName();
			
			return str; 
		}
		
		public String toToolTipText() {
			
			String str = "<html> Files:<br>" + file.getName();
			
			for (Files f : addedFiles) {
				str += "<br>" + f.getName();
			}
			str += "</html>";
			return str; 
		}
	}
	
	int colorIndex = 0;
	
	void calculateSimilarity(DefaultListModel fileToAddListModel) {
		
		ArrayList<Files> list = new ArrayList<Files>();
		
		for (int i = 0; i <fileToAddListModel.getSize(); i++) {
			list.add((Files) fileToAddListModel.get(i));
		}

		for (int i = 0; i < list.size(); i++) {
			
			Files f1 = list.get(i);
			
			if (f1.similarityColor != null)
				continue;
			
			String f1Name = f1.getName();	
			
			if (f1Name == null)
				continue;
			
			ArrayList<Files> toRemove = new ArrayList<Files>();
			for (int j = i+1; j < list.size(); j++) {
				
				Files f2 = list.get(j);
				String f2Name = f2.getName();
								
				if (f2Name == null)
					continue;
				
				if (f2.similarityColor != null)
					continue;
				
				FilenameCloseness closeness =  StringUtil.compareFileNames(f1Name, f2Name);

				if (closeness == FilenameCloseness.much) {
					// Much equal files are colored blue/red in change
					f1.similarityColor = Color.red;
					f2.similarityColor = Color.red;
				}
				else if (closeness == FilenameCloseness.almostidentical) {
					// Almost identical files are colored green
					f1.similarityColor = Color.green;
					f2.similarityColor = Color.green;
					
					if (MovieManager.getConfig().getMultiAddEnableAutomaticCombine()) {
						// Automatically combine files only if they are almost identical
						f1.addFile(f2);
						toRemove.add(list.get(j));
					}
				}
			}
			
			if (MovieManager.getConfig().getMultiAddEnableAutomaticCombine()) {
				// Remove automatically combined files from imdb searching
				for (Files f : toRemove)
					fileToAddListModel.removeElement(f);
			}
		}
	}
	
	public ArrayList<Files> getMoviesToAdd() {
		
		ArrayList<Files> list = new ArrayList<Files>();
		
		DefaultListModel listModel = (DefaultListModel) filesToAddList.getModel();
		
		Enumeration<Files> enumeration = (Enumeration<Files>) listModel.elements();
		
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
	
		mediaFilesListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Matching media files", //$NON-NLS-1$
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
				//Object [] elems = model.toArray();
				model.removeAllElements();
			}
		});

		clearToAddFileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel model = (DefaultListModel) filesToAddList.getModel();
				Object [] elems = model.toArray();
				model.removeAllElements();
				
				for (int i = 0; i < elems.length; i++)
					nodesInFileLists.remove(((Files)elems[i]).getFile().getAbsolutePath());
			}
		});


		JButton addSelectedMediaFilesToAddList = new JButton("Add selected");

		addSelectedMediaFilesToAddList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MovieManager.getConfig().setMultiAddEnableAutomaticCombine(enableAutomaticCombine.isSelected());
				
				if (addListMustContainValidItemsAlert) {
					DefaultListModel listModel = (DefaultListModel) filesToAddList.getModel();
					listModel.clear();
					addListMustContainValidItemsAlert = false;
				}
				addSelectedMediaFilesToAddList();
			}
		});

		enableAutomaticCombine = new JCheckBox(Localizer.getString("DialogAddMultipleMovies.files-options.enable-automatic-combine.text")); //$NON-NLS-1$
		enableAutomaticCombine.setActionCommand("enableAutomaticCombine"); //$NON-NLS-1$
		enableAutomaticCombine.setToolTipText(Localizer.getString("DialogAddMultipleMovies.files-options.enable-automatic-combine-tooltip")); //$NON-NLS-1$
		enableAutomaticCombine.addActionListener(this);

		mediaFileListButtonPanel.add(clearFileList);
		mediaFileListButtonPanel.add(clearToAddFileList);
		mediaFileListButtonPanel.add(addSelectedMediaFilesToAddList);
		mediaFileListButtonPanel.add(enableAutomaticCombine);

		mediaFilesListPanel.add(mediaFileListScrollPane, BorderLayout.CENTER);
		mediaFilesListPanel.add(mediaFileListButtonPanel, BorderLayout.EAST);


		/* The files to add panel. */
		JPanel filesToAddListPanel = new JPanel();
		filesToAddListPanel.setLayout(new BorderLayout());
		filesToAddListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,5), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Media files to be added to database", //$NON-NLS-1$
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font(filesToAddListPanel.getFont().getName(),Font.PLAIN, 12)),
				BorderFactory.createEmptyBorder(1,5,3,5))));

		filesToAddList = new JList() {
			
			public String getToolTipText(MouseEvent evt) {
				int index = locationToIndex(evt.getPoint());
				
				if (index == -1)
					return null;
				
				Object item = getModel().getElementAt(index);

				if (item instanceof Files) {
					return ((Files) item).toToolTipText();
				}
				
				// Return the tool tip text
				return null;
			}
		};
        
		filesToAddList.setCellRenderer(new DefaultListCellRenderer() {

			public Component getListCellRendererComponent(JList list, 
					Object value, 
					int index, 
					boolean isSelected, 
					boolean cellHasFocus) 
			{ 
				
				Component c = super.getListCellRendererComponent( 
						list,value,index,isSelected,cellHasFocus); 
				
				if (value instanceof Files) {
					
					Files n = (Files) value;
										
					if (n.similarityColor != null)
						c.setForeground(n.similarityColor); 
				}
				
				//c.setForeground(index==0 ? Color.red : Color:black); 
				return c;
			} 
		});
	
		filesToAddList.addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {}	
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

			public void mousePressed(MouseEvent e) {
				handleFilesToBeAddedPopup(e);				
			}		
		});
		
		filesToAddList.setModel(new DefaultListModel()); 

		JScrollPane scrollPaneNotes = new JScrollPane(filesToAddList);
		scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel filesToAddButtonPanel = new JPanel();
		
		/*
		JButton combineSimilarFiles = new JButton("Combine similar files");
		combineSimilarFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		
		filesToAddButtonPanel.add(combineSimilarFiles);
		*/
		
		filesToAddListPanel.add(scrollPaneNotes, BorderLayout.CENTER);
		filesToAddListPanel.add(filesToAddButtonPanel, BorderLayout.EAST);
		
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

	
	public void handleFilesToBeAddedPopup(MouseEvent event) {

		/* Button 2 */
		if (SwingUtilities.isRightMouseButton(event)) {

			int rowForLocation = filesToAddList.locationToIndex(event.getPoint());

			int[] selectedIndexes = filesToAddList.getSelectedIndices();

			if (selectedIndexes.length == 0)
				return;

			boolean isSelected = false;

			for (int i = 0; i < selectedIndexes.length; i++) {
				if (selectedIndexes[i] == rowForLocation)
					isSelected = true;
			}

			if (!isSelected)
				return;
			
			makeFilesToBeAddedPopupMenu(event.getX(), event.getY(), event);
		}
	}
	
	public void makeFilesToBeAddedPopupMenu(int x, int y, MouseEvent event) {

		final int[] selectedIndexes = filesToAddList.getSelectedIndices();
		
		JMenuItem combineSelectedEntries = new JMenuItem("Combine selected Entries");
		JMenuItem expandSelectedEntries = new JMenuItem("Expand selected Entry");
		JMenuItem removeSelectedEntries = new JMenuItem("Remove selected entries");
		
		JPopupMenu popupMenu = new JPopupMenu();
		
		
		if (selectedIndexes.length == 1) {
			DefaultListModel model = (DefaultListModel) filesToAddList.getModel();
			Files f1 = (Files) model.getElementAt(selectedIndexes[0]);
			
			// If only one file, no need to show popup
			if (f1.getAddedFiles().length > 0)
				popupMenu.add(expandSelectedEntries);
		}
		else
			popupMenu.add(combineSelectedEntries);
		
		
		popupMenu.add(removeSelectedEntries);
		
		combineSelectedEntries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				DefaultListModel model = (DefaultListModel) filesToAddList.getModel();
				Files f1 = (Files) model.getElementAt(selectedIndexes[0]);
				
				ArrayList<Files> toRemove = new ArrayList<Files>();
				
				for (int i = 1; i < selectedIndexes.length; i++) {
					Files f2 = (Files) model.getElementAt(selectedIndexes[i]);
					f1.addFile(f2);
					toRemove.add((Files) model.getElementAt(selectedIndexes[i]));
				}
			
				for (Files f : toRemove) {
					model.removeElement(f);
				}				
			}
		});

		
		expandSelectedEntries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				DefaultListModel model = (DefaultListModel) filesToAddList.getModel();
				Files f1 = (Files) model.getElementAt(selectedIndexes[0]);
				
				Files [] files = f1.getAddedFiles();
				
				for (int i = 0; i < files.length; i++) {
					model.add(selectedIndexes[0]+ 1 + i, files[i]);
				}
				
				f1.clearAddedFiles();
			}
		});
		
		
		removeSelectedEntries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				Object[] selectedValues = filesToAddList.getSelectedValues();
				
				DefaultListModel model = (DefaultListModel) filesToAddList.getModel();
				
				for (int i = 0; i < selectedValues.length; i++) {
					model.removeElement(selectedValues[i]);
				}
			}
		});
		
		//popupMenu.setInvoker(movieList);
		popupMenu.setLocation(x, y);

		popupMenu.show(filesToAddList, x, y);
	}
}
