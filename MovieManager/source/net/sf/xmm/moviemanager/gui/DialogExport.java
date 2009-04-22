/**
 * @(#)DialogExport.java 1.0 28.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class DialogExport extends JDialog implements ActionListener {
    
	static Logger log = Logger.getLogger(DialogExport.class);
         
    JRadioButton simpleExport;
    JRadioButton fullExport;
    
    JCheckBox enableAlphabeticSplit;
    
    JTextField titleTextField;
    
    JButton closeButton;
    JButton exportButton;
    
    JTextField csvFilePath;
    JTextField csvSeparator;
    JComboBox csvEncoding;
    JButton browseForCSVFile;
    
    JTextField xmlFilePath;
    JComboBox xmlEncoding;
    JButton browseForXMLFile;
    
    JTextField excelFilePath;
    JButton browseForEXCELFile;
    
 
    
    boolean cancelled = false;
        
    ModelImportExportSettings settings = new ModelImportExportSettings();
        
    JTabbedPane tabs = null;
    
    public DialogExport() {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        /* Close dialog... */
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	cancelled = true;
                dispose();
            }
        });
        
        /*Enables dispose when pushing escape*/
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	cancelled = true;
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
        setTitle(Localizer.getString("DialogExport.title")); //$NON-NLS-1$
        setResizable(false);
        setModal(true);
      
        
        /*Export options*/
        simpleExport = new JRadioButton(Localizer.getString("DialogExport.panel-export-options.button.simple-export")); //$NON-NLS-1$
        simpleExport.setActionCommand("Simple Export"); //$NON-NLS-1$
        
        fullExport = new JRadioButton(Localizer.getString("DialogExport.panel-export-options.button.full-export")); //$NON-NLS-1$
        fullExport.setActionCommand("Full Export"); //$NON-NLS-1$
        
        if (MovieManager.getConfig().getExportType().equals("full")) //$NON-NLS-1$
            fullExport.setSelected(true);
        else
            simpleExport.setSelected(true);
        
        /*Group the radio buttons.*/
        ButtonGroup exportGroup = new ButtonGroup();
        exportGroup.add(simpleExport);
        exportGroup.add(fullExport);
        
        /*Register a listener for the radio buttons.*/
        simpleExport.addActionListener(this);
        fullExport.addActionListener(this);
        
        enableAlphabeticSplit = new JCheckBox(Localizer.getString("DialogExport.panel-export-options.button.divide-alphabetically")); //$NON-NLS-1$
        enableAlphabeticSplit.setActionCommand("Divide alphabetically"); //$NON-NLS-1$
        enableAlphabeticSplit.setEnabled(false);
        
        /*Put the radio buttons in a column in a panel.*/
        JPanel exportOptionPanel = new JPanel(new GridLayout(2, 1));
        
        exportOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,5,0) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("DialogExport.panel-export-options.title"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(exportOptionPanel.getFont().getName(),Font.BOLD, exportOptionPanel.getFont().getSize())),
                BorderFactory.createEmptyBorder(5,5,5,5))));
        
        
        exportOptionPanel.add(simpleExport);
        exportOptionPanel.add(fullExport);
        exportOptionPanel.add(enableAlphabeticSplit);
        
        JPanel htmlExportPanel = new JPanel();
        htmlExportPanel.setLayout(new BoxLayout(htmlExportPanel, BoxLayout.PAGE_AXIS));
        
        htmlExportPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(), Localizer.getString("DialogExport.panel-html-export.title") , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(htmlExportPanel.getFont().getName(),Font.BOLD, htmlExportPanel.getFont().getSize()) //$NON-NLS-1$
        )), BorderFactory.createEmptyBorder(0,2,2,2)));
        
        
        
        JPanel titlePanel = new JPanel();
        
        titleTextField = new JTextField(16);
        titleTextField.setEditable(true);
        
        JLabel titleLabel = new JLabel(Localizer.getString("DialogExport.title-text-field") + ": "); //$NON-NLS-1$
        titleLabel.setLabelFor(titleTextField);
        
        titlePanel.add(titleLabel);
        titlePanel.add(titleTextField);
        
        htmlExportPanel.add(new JLabel("The movies currently displayed in the movie list will be exported."));
        htmlExportPanel.add(exportOptionPanel);
        htmlExportPanel.add(titlePanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        exportButton = new JButton(Localizer.getString("DialogExport.button.export.text")); //$NON-NLS-1$
        exportButton.setActionCommand("Export"); //$NON-NLS-1$
        exportButton.addActionListener(this);
        
        closeButton = new JButton(Localizer.getString("DialogExport.button.close.text")); //$NON-NLS-1$
        closeButton.setActionCommand("Close"); //$NON-NLS-1$
        closeButton.addActionListener(this);
        
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        
        //|Localizer.getString("DialogExport.panel-xml-export.title")
        
        
    
               
        
        // CSV panel
                 
        JLabel csvLabel = new JLabel("Export movies to CSV");
    	JPanel csvLabelPanel = new JPanel();
    	csvLabelPanel.add(csvLabel);
    	
        JLabel csvSeparatorLabel = new JLabel("Separator:");
        csvSeparator = new JTextField(5);
        csvSeparator.setText(MovieManager.getConfig().getExportCSVseparator());
        
        JLabel csvEncodingLabel = new JLabel("File encoding:");
        csvEncoding = new JComboBox(new DefaultComboBoxModel(ModelImportExportSettings.encodings));
                
        JPanel csvOpt = new JPanel();
        csvOpt.add(csvSeparatorLabel);
        csvOpt.add(csvSeparator);
        csvOpt.add(csvEncodingLabel);
        csvOpt.add(csvEncoding);
                
        
        /* CSV option panel */
    	JPanel csvOptionPanel = new JPanel();
    	csvOptionPanel.setLayout(new BoxLayout(csvOptionPanel, BoxLayout.Y_AXIS));
    	csvOptionPanel.add(csvOpt);
    	
    	csvFilePath = new JTextField(27);
    	csvFilePath.setText(MovieManager.getConfig().getExportCSVFilePath());
    	
    	browseForCSVFile = new JButton("Browse");
    	browseForCSVFile.setToolTipText("Browse for a CSV file");
    	browseForCSVFile.setActionCommand("Browse CSV File");
    	browseForCSVFile.addActionListener(this);
    	
    	JPanel csvPathPanel = new JPanel();
    	csvPathPanel.setLayout(new FlowLayout());
    	csvPathPanel.add(csvFilePath);
    	csvPathPanel.add(browseForCSVFile);
    	
    	csvPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));
    	
    	JPanel csvFilePanel = new JPanel(new BorderLayout());
    	csvFilePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " CSV ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(csvFilePanel.getFont().getName(),Font.BOLD, csvFilePanel.getFont().getSize()) //$NON-NLS-1$
        )), BorderFactory.createEmptyBorder(0,2,2,2)));
    	
    	csvFilePanel.add(csvLabelPanel, BorderLayout.NORTH);
    	csvFilePanel.add(csvOptionPanel, BorderLayout.CENTER);
    	csvFilePanel.add(csvPathPanel, BorderLayout.SOUTH);
    	
    	
       // XML database panel
        
        JPanel xmlDatabasePanel = new JPanel();
        xmlDatabasePanel.setLayout(new BoxLayout(xmlDatabasePanel, BoxLayout.PAGE_AXIS));
        
        xmlDatabasePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " XML Database " , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(xmlDatabasePanel.getFont().getName(),Font.BOLD, xmlDatabasePanel.getFont().getSize()) //$NON-NLS-1$
        )), BorderFactory.createEmptyBorder(0,2,2,2)));
        
        JLabel labelInfo = new JLabel("Export current movie list to XML Database");
        xmlDatabasePanel.add(labelInfo);
    	
    	
        // XML panel
        JLabel xmlLabel = new JLabel("Export current movie list to XML ");
    	JPanel xmlLabelPanel = new JPanel();
    	xmlLabelPanel.add(xmlLabel);
    	
    	
        JLabel xmlEncodingLabel = new JLabel("File encoding:");
        xmlEncoding = new JComboBox(new DefaultComboBoxModel(ModelImportExportSettings.encodings));
        xmlEncoding.setSelectedItem("UTF-8");
        
        JPanel xmlOpt = new JPanel();
        xmlOpt.add(xmlEncodingLabel);
        xmlOpt.add(xmlEncoding);
                
        
        // XML option panel 
    	JPanel xmlOptionPanel = new JPanel();
    	xmlOptionPanel.setLayout(new BoxLayout(xmlOptionPanel, BoxLayout.Y_AXIS));
    	xmlOptionPanel.add(xmlOpt);
    	
    	xmlFilePath = new JTextField(27);
    	xmlFilePath.setText(MovieManager.getConfig().getExportXMLFilePath());
    	
    	browseForXMLFile = new JButton("Browse");
    	browseForXMLFile.setToolTipText("Browse for a XML file");
    	browseForXMLFile.setActionCommand("Browse XML File");
    	browseForXMLFile.addActionListener(this);
    	
    	JPanel xmlPathPanel = new JPanel();
    	xmlPathPanel.setLayout(new FlowLayout());
    	xmlPathPanel.add(xmlFilePath);
    	xmlPathPanel.add(browseForXMLFile);
    	
    	xmlPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));
    	
    	JPanel xmlPanel = new JPanel(new BorderLayout());
    	xmlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "XML", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(xmlPathPanel.getFont().getName(),Font.BOLD, xmlPathPanel.getFont().getSize()) //$NON-NLS-1$
        )), BorderFactory.createEmptyBorder(0,2,2,2)));
    	
    	xmlPanel.add(xmlLabelPanel, BorderLayout.NORTH);
    	xmlPanel.add(xmlOptionPanel, BorderLayout.CENTER);
    	xmlPanel.add(xmlPathPanel, BorderLayout.SOUTH);
    	  	
    	
    	
 // Excel panel
         
        
        JLabel excelLabel = new JLabel("Export movies to excel");
    	JPanel excelLabelPanel = new JPanel();
    	excelLabelPanel.add(excelLabel);
    	
    	excelFilePath = new JTextField(27);
    	excelFilePath.setText(MovieManager.getConfig().getExportExcelFilePath());
    	
    	browseForEXCELFile = new JButton("Browse");
    	browseForEXCELFile.setToolTipText("Browse for a excel file");
    	browseForEXCELFile.setActionCommand("Browse excel File");
    	browseForEXCELFile.addActionListener(this);
    	
    	JPanel excelPathPanel = new JPanel();
    	excelPathPanel.setLayout(new FlowLayout());
    	excelPathPanel.add(excelFilePath);
    	excelPathPanel.add(browseForEXCELFile);
    	
    	excelPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));
    	
    	JPanel excelFilePanel = new JPanel(new BorderLayout());
    	excelFilePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Excel ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(excelPathPanel.getFont().getName(),Font.BOLD, excelPathPanel.getFont().getSize()) //$NON-NLS-1$
        )), BorderFactory.createEmptyBorder(0,2,2,2)));
    	
    	excelFilePanel.add(excelLabelPanel, BorderLayout.NORTH);
    	excelFilePanel.add(excelPathPanel, BorderLayout.SOUTH);
    	
    	
        
    	
        tabs = new JTabbedPane();
        tabs.add(csvFilePanel, ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_CSV]);
        tabs.add(excelFilePanel, ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_EXCEL]);
        tabs.add(xmlPanel, ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_XML_DATABASE]);
       // tabs.add(xmlDatabasePanel, "XML Database");
        tabs.add(htmlExportPanel, ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_HTML]);
      
        if (MovieManager.getConfig().getLastDialogImportType() < ModelImportExportSettings.EXPORT_MODE_COUNT) {
        	
        	int index = 0;
        	
        	if (MovieManager.getConfig().getLastDialogExportType() < ModelImportExportSettings.exportTypes.length)
        		index = tabs.indexOfTab(ModelImportExportSettings.exportTypes[MovieManager.getConfig().getLastDialogExportType()]);
        	
        	if (index >= 0)
        		tabs.setSelectedIndex(index);
        }
        
        Container container = getContentPane();
        container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
        container.add(tabs);
        container.add(buttonPanel);
        
        pack();
        setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
                (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
    }

    public boolean isCancelled() {
    	return cancelled;
    }
    
    /* Returns the string in the path textfield */
	public String getPath() {

		switch (getExportMode()) {
		case ModelImportExportSettings.EXPORT_MODE_EXCEL : return excelFilePath.getText();
		case ModelImportExportSettings.EXPORT_MODE_XML_DATABASE : return xmlFilePath.getText();
		case ModelImportExportSettings.EXPORT_MODE_CSV : return csvFilePath.getText();
		}
		return "";
	}

    
    public int getExportMode() {

    	String title = tabs.getTitleAt(tabs.getSelectedIndex());
    	
    	if (title.equals(ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_CSV]))
    		return ModelImportExportSettings.EXPORT_MODE_CSV;
    	else if (title.equals(ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_EXCEL]))
    		return ModelImportExportSettings.EXPORT_MODE_EXCEL;
    	//else if (title.equals(ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_XML]))
    	//	return ModelImportExportSettings.EXPORT_MODE_XML;
    	else if (title.equals(ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_XML_DATABASE]))
    		return ModelImportExportSettings.EXPORT_MODE_XML_DATABASE;
    	else if (title.equals(ModelImportExportSettings.exportTypes[ModelImportExportSettings.EXPORT_MODE_HTML]))
    		return ModelImportExportSettings.EXPORT_MODE_HTML;
    	    		
    	return -1;
    }

    public ModelImportExportSettings getSettings() {
    	executeSave();
 	   return settings;
    }
    
    /*Saves the options to the MovieManager object*/
    void executeSave() {

    	MovieManager.getConfig().setLastDialogExportType(getExportMode());
    	
    	MovieManager.getConfig().setExportCSVFilePath(csvFilePath.getText());
    	MovieManager.getConfig().setExportExcelFilePath(excelFilePath.getText());
    	MovieManager.getConfig().setExportXMLFilePath(xmlFilePath.getText());
    	
    	// Save CSV separator
    	if (csvSeparator.getText().trim().length() > 0)
    		settings.csvSeparator = csvSeparator.getText().trim().charAt(0);
    	    	
    	settings.mode = getExportMode();
    	    	
    	settings.htmlTitle = titleTextField.getText();
    	settings.htmlAlphabeticSplit = enableAlphabeticSplit.isSelected();
    	settings.htmlSimpleMode = simpleExport.isSelected();
    	
    	switch (settings.mode) {
    	case ModelImportExportSettings.EXPORT_MODE_CSV: {
    		settings.filePath = csvFilePath.getText();
    		settings.textEncoding = (String) csvEncoding.getSelectedItem();
    		break;
    	}
    	case ModelImportExportSettings.EXPORT_MODE_EXCEL: {
    		settings.filePath = excelFilePath.getText();
    		break;
    	}
    	//case ModelImportExportSettings.EXPORT_MODE_XML: {
    	//	settings.textEncoding = (String) xmlEncoding.getSelectedItem();
    	//	break;
    //	}
    	case ModelImportExportSettings.EXPORT_MODE_XML_DATABASE: {
    		settings.filePath = xmlFilePath.getText();
    		settings.textEncoding = (String) xmlEncoding.getSelectedItem();
    		break;
    	}
    	case ModelImportExportSettings.EXPORT_MODE_HTML: {}
    	}
    }
    
    
    public void actionPerformed(ActionEvent event) {
        
        log.debug("ActionPerformed: "+event.getActionCommand()); //$NON-NLS-1$
        
        if (event.getSource().equals(closeButton)) {
        	cancelled = true;
        	executeSave();
        	dispose();
            return;
        }
        
        if (event.getSource().equals(exportButton)) {
            
        	cancelled = false;
        	
            /* HTMl export */
            if (tabs.getSelectedIndex() == ModelImportExportSettings.EXPORT_MODE_HTML) {
            	executeSave();
            }
            // CSV or Excel
            else if (tabs.getSelectedIndex() == ModelImportExportSettings.EXPORT_MODE_CSV || 
            		tabs.getSelectedIndex() == ModelImportExportSettings.EXPORT_MODE_EXCEL ||
            		tabs.getSelectedIndex() == ModelImportExportSettings.EXPORT_MODE_XML_DATABASE) {
            
            	boolean execute = true;
            	
            	String filePath = getPath();
            	
            	if (filePath.equals("")) {
        			DialogAlert alert = new DialogAlert(this, "Alert","Please specify a file path.");
        			GUIUtil.showAndWait(alert, true);
        			execute = false;
        		}
        		else if (new File(filePath).exists()) {
        			 DialogQuestion question = new DialogQuestion("File exists", "<html>The specified file already exists.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        			 "Overwrite file?</html>"); //$NON-NLS-1$
        			 GUIUtil.showAndWait(question, true);
        			         			
        			 if (!question.getAnswer())
        				 execute = false;
        		}
        		else if (new File(filePath).getParentFile() == null || !new File(filePath).getParentFile().isDirectory()) {
        			DialogAlert alert = new DialogAlert(this, "Alert","The parent directory does not exist.");
        			GUIUtil.show(alert, true);
        			execute = false;
        		}
            	
        		if (execute) {
        			executeSave();
        		}
        		else
        			cancelled = true;
            }
                        
            if (!cancelled)
            	dispose();
            
            return;
        }
    
        if (event.getSource().equals(simpleExport)) {
        	enableAlphabeticSplit.setEnabled(false);
            return;
        }
        
        if (event.getSource().equals(fullExport)) {
        	enableAlphabeticSplit.setEnabled(true );
            return;
        }
        

        int saveExportFile = -1;
        String extension = null;
        
        if (event.getSource().equals(browseForCSVFile)) {
        	saveExportFile = ModelImportExportSettings.EXPORT_MODE_CSV;
        	extension = ".csv";
        }
        else if (event.getSource().equals(browseForEXCELFile)) {
        	saveExportFile = ModelImportExportSettings.EXPORT_MODE_EXCEL;
        	extension = ".xls";
        }
        else if (event.getSource().equals(browseForXMLFile)) {
        	saveExportFile = ModelImportExportSettings.EXPORT_MODE_XML_DATABASE;
        	extension = ".xml";
        }
        
        if (saveExportFile != -1) {

        	JFileChooser chooser = new JFileChooser();
        	String path = getPath();
        	chooser.setCurrentDirectory(new File(path));
        	
        	int returnVal = chooser.showDialog(null, "Save output file");

        	if (returnVal != JFileChooser.APPROVE_OPTION) {
        		log.warn("Failed to retrieve output file");
        	}
        	else {
        		try {
        			String outputFile = chooser.getSelectedFile().getCanonicalPath();

        			if (!outputFile.toLowerCase().endsWith(extension))
        				outputFile += extension;

        			settings.filePath = outputFile;
        			
        			if (saveExportFile == ModelImportExportSettings.EXPORT_MODE_CSV)
        				csvFilePath.setText(outputFile);
        			else if (saveExportFile == ModelImportExportSettings.EXPORT_MODE_EXCEL)
        				excelFilePath.setText(outputFile);
        			else if (saveExportFile == ModelImportExportSettings.EXPORT_MODE_XML_DATABASE)
        				xmlFilePath.setText(outputFile);

        			return;
        		} catch (IOException e) {
        			log.warn("Failed to retrieve output file");
        		}
        	}
        }

        MovieManager.getDialog().getMoviesList().requestFocus(true);
    }
    
}
