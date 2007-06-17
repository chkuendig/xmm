/**
 * @(#)DialogImport.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.networktools.proportionlayout.ProportionLayout;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandImport;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.*;
import net.sf.xmm.moviemanager.models.*;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;


public class DialogImport extends JDialog implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    // Stores in array for easy access through indexes.
    final String [] importTypes = new String[ModelImportSettings.IMPORT_COUNT];
      
    void setImportTypeValues() {
    	importTypes[ModelImportSettings.IMPORT_TEXT] = "Text File";
        importTypes[ModelImportSettings.IMPORT_EXCEL] = "Excel Spreadsheet";
        importTypes[ModelImportSettings.IMPORT_XML] = "XML File";
        importTypes[ModelImportSettings.IMPORT_CSV] = "CSV File";
        importTypes[ModelImportSettings.IMPORT_EXTREME] = "Extreme Movie Manager database";
    } 
    
     
    private JTextField textFilePath;
    private JTextField excelFilePath;
    private JTextField extremeFilePath;
    private JTextField xmlFilePath;
    private JTextField csvFilePath;
    public JTextField csvSeparator;
    
    private JButton browseForTextFile;
    private JButton browseForExcelFile;
    private JButton browseForExtremeFile;
    private JButton browseForXMLFile;
    private JButton browseForCSVFile;
    
    private JButton buttonOk;
    private JButton buttonCancel;
    private JButton buttonAddMovies;
    private JButton buttonAddList;
        
    private JRadioButton askButton;
    private JRadioButton selectIfOnlyOneHitButton;
    private JRadioButton selectFirstHitButton;
    
    protected JCheckBox enableSearchForImdbInfo;
    public JCheckBox enableOverwriteImportedInfoWithImdbInfo;
    
    private JRadioButton useOriginalLanguage;
    public JRadioButton useMediaLanguage;
    
    public JCheckBox enableAddMoviesToList;
    public JComboBox listChooser;
    
    protected JTabbedPane tabbedPane;
    
    private MovieManagerCommandImport parent;
    protected JPanel all;
    
    /* The int stores the ints 0,1 or 2.
       0 for ask awlays, 1 for automatically select first hit, 
       and 2 to automatically select if only one hit.
    */
    private int multiAddSelectOption; /*From 0-2*/
        
    public DialogImport(final MovieManagerCommandImport parent) {
	/* Dialog creation...*/
    	super(MovieManager.getDialog());
    	this.parent = parent;
		
    	setImportTypeValues();
    	
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    parent.setCancelAll(true);
		    dispose();
		}
	    });
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e) {
		    parent.setCancelAll(true);
		    dispose();
		}
	    };
	
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	setTitle("Import Movies");
	setModal(true);
	setResizable(false);
	
	
	/*Radio buttons, choses if the list of hits should apear or not*/
	askButton = new JRadioButton("Display list of hits");
	askButton.setActionCommand("Display list of hits");
	askButton.addActionListener(this);
	askButton.setSelected(true);
	
	selectFirstHitButton = new JRadioButton("Select First Hit");
	selectFirstHitButton.setActionCommand("Select First Hit");
	selectFirstHitButton.addActionListener(this);
	
	selectIfOnlyOneHitButton = new JRadioButton("Select If Only One Hit, else display list of hits");
	selectIfOnlyOneHitButton.setActionCommand("Select If Only One Hit");
	selectIfOnlyOneHitButton.addActionListener(this);
	
	askButton.setEnabled(false);
	selectIfOnlyOneHitButton.setEnabled(false);
	selectFirstHitButton.setEnabled(false);
	
	ButtonGroup radioButtonGroup = new ButtonGroup();
	radioButtonGroup.add(askButton);
	radioButtonGroup.add(selectFirstHitButton);
	radioButtonGroup.add(selectIfOnlyOneHitButton);
	
	
	JPanel radioButtonPanel = new JPanel(new GridLayout(0, 1));
	
	radioButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	radioButtonPanel.add(askButton);
	radioButtonPanel.add(selectFirstHitButton);
	radioButtonPanel.add(selectIfOnlyOneHitButton);
	
	
	enableSearchForImdbInfo = new JCheckBox("Get IMDb info");
	enableSearchForImdbInfo.addActionListener(this);
	
	JPanel imdbPanel = new JPanel();
	
	imdbPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," IMDb Dialog "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	imdbPanel.add(enableSearchForImdbInfo);
	imdbPanel.add(radioButtonPanel);
	
	/* Textfile */
	
	JLabel textlabel = new JLabel("Import movies from a textfile containing movie titles only");
	JPanel textLabelPanel = new JPanel();
    textLabelPanel.add(textlabel);
	
	/* textfile path */
	textFilePath = new JTextField(27);
	textFilePath.setText(MovieManager.getConfig().getImportTextfilePath());
	
	browseForTextFile = new JButton("Browse");
	browseForTextFile.setToolTipText("Browse for a text file");
	browseForTextFile.setActionCommand("Browse text File");
	browseForTextFile.addActionListener(this);
	
	JPanel textPathPanel = new JPanel();
	textPathPanel.setLayout(new FlowLayout());
	textPathPanel.add(textFilePath);
	textPathPanel.add(browseForTextFile);
	
	textPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," File to Import "), BorderFactory.createEmptyBorder(0,0,0,0))));
	
	JPanel textFilePanel = new JPanel(new BorderLayout());
	textFilePanel.add(textLabelPanel, BorderLayout.NORTH);
	textFilePanel.add(textPathPanel, BorderLayout.SOUTH);
	
	/* Excel spreadsheet */
	JLabel excelLabel = new JLabel("Import movies from an excel spreadsheet");
	JPanel excelLabelPanel = new JPanel();
	excelLabelPanel.add(excelLabel);
	 
	JPanel excelOptionPanel = new JPanel();
    excelOptionPanel.setLayout(new BoxLayout(excelOptionPanel, BoxLayout.Y_AXIS));
    
    ProportionLayout propLayout = new ProportionLayout();
    
    
    propLayout.appendColumn(10);                                // column 0
          // Column 0 will be an empty space of width 10
    propLayout.appendColumn(0, ProportionLayout.NO_PROPORTION); // column 1
          // Column 1 will always be the greatest preferred width of all it's components
      
    propLayout.appendColumn(10); // column 1
    
          // Then add all the rows to the ProportionLayout
    propLayout.appendRow(10);                                // row 0
          // Row 0 will be an empty space of width 10
    propLayout.appendRow(0, ProportionLayout.NO_PROPORTION); // row 1
          // Row 1 will always be the greatest preferred height of all it's components
          // Row 1 will never get any of the additional height
    propLayout.appendRow(10);                                // row 2
    
        
    excelOptionPanel.setLayout(propLayout);
               
    
	/* Excel file path */
	excelFilePath = new JTextField(27);
	excelFilePath.setText(MovieManager.getConfig().getImportExcelfilePath());
	
	browseForExcelFile = new JButton("Browse");
	browseForExcelFile.setToolTipText("Browse for a excel file");
	browseForExcelFile.setActionCommand("Browse excel File");
	browseForExcelFile.addActionListener(this);
	
	JPanel excelPathPanel = new JPanel();
	excelPathPanel.setLayout(new FlowLayout());
	excelPathPanel.add(excelFilePath);
	excelPathPanel.add(browseForExcelFile);
	
	excelPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," File to Import "), BorderFactory.createEmptyBorder(0,5,0,5))));
	
	JPanel excelFilePanel = new JPanel(new BorderLayout());
	excelFilePanel.add(excelLabelPanel, BorderLayout.NORTH);
	excelFilePanel.add(excelOptionPanel, BorderLayout.CENTER);
	excelFilePanel.add(excelPathPanel, BorderLayout.SOUTH);
	
	
	/* Extreme movie manager database */
	JLabel extremeLabel = new JLabel("Import movies from an extreme Movie Manager database");
	JPanel extremeLabelPanel = new JPanel();
	extremeLabelPanel.add(extremeLabel);
	
	enableOverwriteImportedInfoWithImdbInfo = new JCheckBox("Overwrite imported info with imdb info");
	enableOverwriteImportedInfoWithImdbInfo.setEnabled(false);
	
	useOriginalLanguage = new JRadioButton("Import Original Language");
	useOriginalLanguage.addActionListener(this);
	useOriginalLanguage.setSelected(true);
	
	useMediaLanguage = new JRadioButton("Import Media Language");
	useMediaLanguage.addActionListener(this);
	
	ButtonGroup languageButtonGroup = new ButtonGroup();
	languageButtonGroup.add(useOriginalLanguage);
	languageButtonGroup.add(useMediaLanguage);
	
	
	/* Extreme option panel */
	JPanel extremeOptionPanel1 = new JPanel();
	extremeOptionPanel1.setLayout(new BoxLayout(extremeOptionPanel1, BoxLayout.Y_AXIS));
	extremeOptionPanel1.add(enableOverwriteImportedInfoWithImdbInfo);
	extremeOptionPanel1.add(useOriginalLanguage);
	extremeOptionPanel1.add(useMediaLanguage);
	
	JPanel extremeOptionPanel2 = new JPanel();
	extremeOptionPanel2.add(extremeOptionPanel1);
	
	
	extremeFilePath = new JTextField(27);
	extremeFilePath.setText(MovieManager.getConfig().getImportExtremefilePath());
	
	browseForExtremeFile = new JButton("Browse");
	browseForExtremeFile.setToolTipText("Browse for a extreme file");
	browseForExtremeFile.setActionCommand("Browse extreme File");
	browseForExtremeFile.addActionListener(this);
	
	JPanel extremePathPanel = new JPanel();
	extremePathPanel.setLayout(new FlowLayout());
	extremePathPanel.add(extremeFilePath);
	extremePathPanel.add(browseForExtremeFile);
	
	extremePathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Import "), BorderFactory.createEmptyBorder(0,5,0,5))));
	
	JPanel extremePanel = new JPanel(new BorderLayout());
	extremePanel.add(extremeLabelPanel, BorderLayout.NORTH);
	extremePanel.add(extremeOptionPanel2, BorderLayout.CENTER);
	extremePanel.add(extremePathPanel, BorderLayout.SOUTH);
	
    
    /* XML file path */
    xmlFilePath = new JTextField(27);
    xmlFilePath.setText(MovieManager.getConfig().getImportXMLfilePath());
    
    browseForXMLFile = new JButton("Browse");
    browseForXMLFile.setToolTipText("Browse for an XML file");
    browseForXMLFile.setActionCommand("Browse XML File");
    browseForXMLFile.addActionListener(this);
    
    JPanel xmlPathPanel = new JPanel();
    xmlPathPanel.setLayout(new FlowLayout());
    xmlPathPanel.add(xmlFilePath);
    xmlPathPanel.add(browseForXMLFile);
    
    xmlPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," File to Import "), BorderFactory.createEmptyBorder(0,0,0,0))));
    
    JLabel xmlLabel = new JLabel("Import movies from a XML file (Must be exported by MeD's Movie Manager)");
    JPanel xmlLabelPanel = new JPanel();
    xmlLabelPanel.add(xmlLabel);
    
    JPanel xmlFilePanel = new JPanel(new BorderLayout());
    xmlFilePanel.add(xmlLabelPanel, BorderLayout.NORTH);
    xmlFilePanel.add(xmlPathPanel, BorderLayout.SOUTH);

    
    /* CSV TEST */
    JLabel csvLabel = new JLabel("Import movies from a CSV file");
	JPanel csvLabelPanel = new JPanel();
	csvLabelPanel.add(csvLabel);
	
    JLabel csvSeparatorLabel = new JLabel("Separator:");
    csvSeparator = new JTextField(5);
    csvSeparator.setText(MovieManager.getConfig().getImportCSVseparator());
    JPanel csvOpt = new JPanel();
    csvOpt.add(csvSeparatorLabel);
    csvOpt.add(csvSeparator);
   
	/* CSV option panel */
	JPanel csvOptionPanel1 = new JPanel();
	csvOptionPanel1.setLayout(new BoxLayout(csvOptionPanel1, BoxLayout.Y_AXIS));
	csvOptionPanel1.add(csvOpt);
	
	csvFilePath = new JTextField(27);
	csvFilePath.setText(MovieManager.getConfig().getImportCSVfilePath());
	
	browseForCSVFile = new JButton("Browse");
	browseForCSVFile.setToolTipText("Browse for a CSV file");
	browseForCSVFile.setActionCommand("Browse CSV File");
	browseForCSVFile.addActionListener(this);
	
	JPanel csvPathPanel = new JPanel();
	csvPathPanel.setLayout(new FlowLayout());
	csvPathPanel.add(csvFilePath);
	csvPathPanel.add(browseForCSVFile);
	
	csvPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Import "), BorderFactory.createEmptyBorder(0,5,0,5))));
	
	JPanel csvFilePanel = new JPanel(new BorderLayout());
	csvFilePanel.add(csvLabelPanel, BorderLayout.NORTH);
	csvFilePanel.add(csvOptionPanel1, BorderLayout.CENTER);
	csvFilePanel.add(csvPathPanel, BorderLayout.SOUTH);
	
	
	/* Tabbed pane */
	tabbedPane = new JTabbedPane();
	tabbedPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	tabbedPane.add(textFilePanel, importTypes[ModelImportSettings.IMPORT_TEXT]);
	tabbedPane.add(excelFilePanel, importTypes[ModelImportSettings.IMPORT_EXCEL]);
	tabbedPane.add(xmlFilePanel, importTypes[ModelImportSettings.IMPORT_XML]);
	tabbedPane.add(csvFilePanel, importTypes[ModelImportSettings.IMPORT_CSV]);
	
	// Extreme movie manager uses MS Access, so it's only supported on Windows.
	if (FileUtil.isWindows())
	    tabbedPane.add(extremePanel, importTypes[ModelImportSettings.IMPORT_EXTREME]);
	
	tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(importTypes[MovieManager.getConfig().getLastDialogImportType()]));
		
	/* Add to list */
	JPanel listPanel = makeListPanel();
	
	/* Buttons */
	buttonOk = new JButton("Close");
	buttonOk.setToolTipText("Save and Close");
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
	all.add(imdbPanel);
	all.add(tabbedPane);
	all.add(listPanel);
	all.add(buttonPanel);
	
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
	getContentPane().add(all,BorderLayout.NORTH);
	/* Packs and sets location... */
	pack();
	
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	
	GUIUtil.showAndWait(this, true);
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
	    
	    enableAddMoviesToList = new JCheckBox("Enable");
	    enableAddMoviesToList.setActionCommand("enableAddMoviesToList");
	    enableAddMoviesToList.setToolTipText("Enable applying added movies to a list.");
	    enableAddMoviesToList.addActionListener(this);
	    
	    listChooser.setSelectedItem(list);
	    
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
    private String executeCommandGetFile(int importMode) {
	
	
	/* Opens the Open dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	try {
	    fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);
	    
	    String path = getPath();
	     
	    if (!path.equals("") && new File(path).isFile() && !new File(path).isDirectory()) {
	    	path = new File(path).getParent();
	    }
	    	    
        fileChooser.setCurrentDirectory(new File(path));
        
	    String title = "";
	    
	    if (importMode == ModelImportSettings.IMPORT_TEXT) {
		title = "Select text file";
		fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, new String("All Files (*.*)")));
		fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"txt"},new String("Textfile (*.txt)")));
	    } 
	    else if (importMode == ModelImportSettings.IMPORT_EXCEL) {
		title = "Select excel file";
		fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, new String("All Files (*.*)")));
		fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xls"},new String("Excel spreadsheet (*.xls)")));
	    }
	    else if (importMode == ModelImportSettings.IMPORT_XML) {
            title = "Select XML file";
            fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xml"},new String("Exported XML file (*.xml)")));
            }
	    else if (importMode == ModelImportSettings.IMPORT_CSV) {
        	title = "Select CSV file";
        	fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[] {
                "csv"
            }, new String("Exported CSV file (*.csv)")));
        }
	    else if (importMode == ModelImportSettings.IMPORT_EXTREME) {
			title = "Select eXtreme movie manager database";
			fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"mdb", "accdb"},new String("MS Access Database File (*.mdb, *.accdb)")));
		 }
	    
	    fileChooser.setDialogTitle(title);
	    fileChooser.setApproveButtonText("Select");
	    fileChooser.setApproveButtonToolTipText("Select file");
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    
	    int returnVal = fileChooser.showOpenDialog(this);
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		/* Gets the path... */
		String filepath = fileChooser.getSelectedFile().getAbsolutePath();
		
		if (!(new File(filepath).exists())) {
		    throw new Exception("File not found!");
		}
        
        MovieManager.getConfig().setLastMiscDir(new File(filepath));
        
		return filepath;
	    }
	}
	catch (Exception e) {
	    log.error("", e);
	}
	
	return "";
    }
    
    /*Saves the options to the MovieManager object*/
    void executeSave() {

    	MovieManager.getConfig().setLastDialogImportType(getImportMode());
    	
    	MovieManager.getConfig().setImportTextfilePath(textFilePath.getText());
    	MovieManager.getConfig().setImportExcelfilePath(excelFilePath.getText());
    	MovieManager.getConfig().setImportXMLfilePath(xmlFilePath.getText());
    	MovieManager.getConfig().setImportCSVfilePath(csvFilePath.getText());
    	MovieManager.getConfig().setImportCSVseparator(csvSeparator.getText());
    	MovieManager.getConfig().setImportExtremefilePath(extremeFilePath.getText());
   	
    	// Save CSV separator
    	//csvSeparator
    	
    	if (listChooser != null) {
    		MovieManager.getConfig().setMultiAddList((String) listChooser.getSelectedItem());

    		if (enableAddMoviesToList.isSelected())
    			MovieManager.getConfig().setMultiAddListEnabled(true);
    		else
    			MovieManager.getConfig().setMultiAddListEnabled(false);
    	}

    	MovieManager.getConfig().setMultiAddSelectOption(multiAddSelectOption);
    }

    public int getImportMode() {
    	
    	String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
    	
    	if (title.equals(importTypes[ModelImportSettings.IMPORT_TEXT]))
    		return ModelImportSettings.IMPORT_TEXT;
    	else if (title.equals(importTypes[ModelImportSettings.IMPORT_EXCEL]))
    		return ModelImportSettings.IMPORT_EXCEL;
    	else if (title.equals(importTypes[ModelImportSettings.IMPORT_XML]))
    		return ModelImportSettings.IMPORT_XML;
    	else if (title.equals(importTypes[ModelImportSettings.IMPORT_CSV]))
    		return ModelImportSettings.IMPORT_CSV;
    	else if (title.equals(importTypes[ModelImportSettings.IMPORT_EXTREME]))
    		return ModelImportSettings.IMPORT_EXTREME;
    	
    	return -1;
    }

    /*Returns the string in the path textfield*/
    public String getPath() {

    	switch (getImportMode()) {
    	case ModelImportSettings.IMPORT_TEXT : return textFilePath.getText();
    	case ModelImportSettings.IMPORT_EXCEL : return excelFilePath.getText();
    	case ModelImportSettings.IMPORT_XML : return xmlFilePath.getText();
    	case ModelImportSettings.IMPORT_CSV : return csvFilePath.getText();
    	case ModelImportSettings.IMPORT_EXTREME : return extremeFilePath.getText();
    	}
    	return "";
    }

    
    /*returns the value of the multiAddSelectOption variable.( values 0-2)*/
    public int getMultiAddSelectOption() {

    	if (!enableSearchForImdbInfo.isSelected())
    		return -1;

    	return multiAddSelectOption;
    }
    
    public void actionPerformed(ActionEvent event) {
    	log.debug("ActionPerformed: "+ event.getActionCommand());

    	if (event.getSource().equals(browseForTextFile)) {
    		String ret = executeCommandGetFile(ModelImportSettings.IMPORT_TEXT);
    		if (!ret.equals(""))
    			textFilePath.setText(ret);
    	}

    	if (event.getSource().equals(browseForExcelFile)) {
    		String ret = executeCommandGetFile(ModelImportSettings.IMPORT_EXCEL);
    		if (!ret.equals(""))
    			excelFilePath.setText(ret);
    	}

    	if (event.getSource().equals(browseForXMLFile)) {
    		String ret = executeCommandGetFile(ModelImportSettings.IMPORT_XML);
    		if (!ret.equals(""))
    			xmlFilePath.setText(ret);
    	}

    	if(event.getSource().equals(browseForCSVFile))
    	{
    		String s2 = executeCommandGetFile(ModelImportSettings.IMPORT_CSV);
    		if(!s2.equals(""))
    			csvFilePath.setText(s2);
    	}

    	if (event.getSource().equals(browseForExtremeFile)) {
    		String ret = executeCommandGetFile(ModelImportSettings.IMPORT_EXTREME);
    		if (!ret.equals(""))
    			extremeFilePath.setText(ret);
    	}

    	if (event.getSource().equals(buttonOk)) {

    		executeSave();
    		parent.setCancelAll(true);
    		dispose();
    	}

    	if (event.getSource().equals(buttonCancel)) {
    		log.debug("ActionPerformed: " + event.getActionCommand());
    		parent.setCancelAll(true);
    		dispose();
    	}

    	if (event.getSource().equals(buttonAddMovies)) {
    		log.debug("ActionPerformed: " + event.getActionCommand());

    		if (getPath().equals("")) {
    			DialogAlert alert = new DialogAlert(this, "Alert","Please specify a file path.");
    			GUIUtil.showAndWait(alert, true);
    		}
    		else if (!new File(getPath()).exists()) {
    			DialogAlert alert = new DialogAlert(this, "Alert","The specified file does not exist.");
    			GUIUtil.showAndWait(alert, true);
    		}
    		else {
    			executeSave();
    			dispose();
    		}
    	}


    	if (event.getSource().equals(buttonAddList)) {

    		MovieManagerCommandLists.execute(this);

    		all.remove(2);
    		all.add(makeListPanel(), 2);
    		pack();

    		GUIUtil.show(this, true);
    	}

    	if (event.getSource().equals(enableSearchForImdbInfo)) {

    		if (enableSearchForImdbInfo.isSelected()) {
    			askButton.setEnabled(true);
    			selectIfOnlyOneHitButton.setEnabled(true);
    			selectFirstHitButton.setEnabled(true);
    			enableOverwriteImportedInfoWithImdbInfo.setEnabled(true);

    		}
    		else {
    			askButton.setEnabled(false);
    			selectIfOnlyOneHitButton.setEnabled(false);
    			selectFirstHitButton.setEnabled(false);
    			enableOverwriteImportedInfoWithImdbInfo.setEnabled(false);
    		}
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
