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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;

import org.apache.log4j.Logger;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

public class DialogExport extends JDialog implements ActionListener, ItemListener {
                                     
    static Logger log = Logger.getRootLogger();
    
    JRadioButton orderByMovieTitle;
    JRadioButton orderByDirectedBy;
    JRadioButton orderByRating;
    JRadioButton orderByDate;
    
    JCheckBox applyCurrentAdvancedSearchSettings;
    JCheckBox exportCurrentList;

    JRadioButton simpleExport;
    JRadioButton fullExport;
    
    JCheckBox enableXhtml;
    JCheckBox enableAlphabeticSplit;
    
    JTextField titleTextField;
    
    JButton closeButton;
    JButton exportButton;
    
    public DialogExport() {
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
	Action escapeAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	setTitle("Export Options");
	setResizable(false);
	
	
	/* apply current search-filter values to list  */
	exportCurrentList = new JCheckBox("Export only current selected list");
	applyCurrentAdvancedSearchSettings = new JCheckBox("Use current advanced search settings");
	
	JPanel moviesPanel = new JPanel(new GridLayout(2, 1));
	moviesPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,0) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																					       "",
																					       TitledBorder.DEFAULT_JUSTIFICATION,
																					       TitledBorder.DEFAULT_POSITION,
																					       new Font(moviesPanel.getFont().getName(),Font.BOLD, moviesPanel.getFont().getSize())),
																	      BorderFactory.createEmptyBorder(5,10,5,5))));
	
	moviesPanel.add(exportCurrentList);
	moviesPanel.add(applyCurrentAdvancedSearchSettings);
	
	/*Order panel*/
	orderByMovieTitle = new JRadioButton("Movie Title");
	orderByDirectedBy = new JRadioButton("Directed By");
	orderByRating = new JRadioButton("Rating");
	orderByDate = new JRadioButton("Date");
	
	orderByMovieTitle.addItemListener(this);
	orderByDirectedBy.addItemListener(this);
	orderByRating.addItemListener(this);
	orderByDate.addItemListener(this);

	ButtonGroup orderGroup = new ButtonGroup();
	orderGroup.add(orderByMovieTitle);
	orderGroup.add(orderByDirectedBy);
	orderGroup.add(orderByRating);
	orderGroup.add(orderByDate);
	
	orderByMovieTitle.setSelected(true);
	
	JPanel orderByPanel = new JPanel(new GridLayout(2, 1));
	orderByPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,5,0) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																						" Order By",
																						TitledBorder.DEFAULT_JUSTIFICATION,
																						TitledBorder.DEFAULT_POSITION,
																						new Font(orderByPanel.getFont().getName(),Font.BOLD, orderByPanel.getFont().getSize())),
																	       BorderFactory.createEmptyBorder(5,5,5,5))));
	
	
	orderByPanel.add(orderByMovieTitle);
	orderByPanel.add(orderByDirectedBy);
	orderByPanel.add(orderByRating);
	orderByPanel.add(orderByDate);
	
	
	/*Export options*/
	simpleExport = new JRadioButton("Simple Export");
	simpleExport.setActionCommand("Simple Export");
	
	fullExport = new JRadioButton("Full Export");
	fullExport.setActionCommand("Full Export");
	
	if (MovieManager.getConfig().getExportType().equals("full"))
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
	
	enableXhtml = new JCheckBox("Xhtml");
	enableXhtml.setActionCommand("Xhtml");
	
	enableAlphabeticSplit = new JCheckBox("Divide alphabetically");
	enableAlphabeticSplit.setActionCommand("Divide alphabetically");
	enableAlphabeticSplit.setEnabled(false);
	enableAlphabeticSplit.addItemListener(this);
	
	/*Put the radio buttons in a column in a panel.*/
        JPanel exportOptionPanel = new JPanel(new GridLayout(2, 1));
	
	exportOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,5,0) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																						     " Export Options",
																						     TitledBorder.DEFAULT_JUSTIFICATION,
																						     TitledBorder.DEFAULT_POSITION,
																						     new Font(exportOptionPanel.getFont().getName(),Font.BOLD, exportOptionPanel.getFont().getSize())),
																		    BorderFactory.createEmptyBorder(5,5,5,5))));
	
	
	exportOptionPanel.add(simpleExport);
	exportOptionPanel.add(fullExport);
	exportOptionPanel.add(enableXhtml);
	exportOptionPanel.add(enableAlphabeticSplit);
	
	JPanel exportPanel = new JPanel();
	exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.PAGE_AXIS));
	
	exportPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
																					       BorderFactory.createEtchedBorder(), " HTML Export ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(exportPanel.getFont().getName(),Font.BOLD, exportPanel.getFont().getSize())
																					       )), BorderFactory.createEmptyBorder(0,2,2,2)));
	
	
	
	JPanel titlePanel = new JPanel();
	
	titleTextField = new JTextField(16);
	titleTextField.setEditable(true);
	
	JLabel titleLabel = new JLabel("Title:");
	titleLabel.setLabelFor(titleTextField);
	
	titlePanel.add(titleLabel);
	titlePanel.add(titleTextField);
	
	exportPanel.add(moviesPanel);
	exportPanel.add(orderByPanel);
	exportPanel.add(exportOptionPanel);
	exportPanel.add(titlePanel);
	
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	
	exportButton = new JButton("Export");
	exportButton.setActionCommand("Export");
	exportButton.addActionListener(this);
	
	closeButton = new JButton("Close");
	closeButton.setActionCommand("Close");
	closeButton.addActionListener(this);
	
	buttonPanel.add(exportButton);
	buttonPanel.add(closeButton);
	
	Container lerret = getContentPane();
	lerret.setLayout(new BoxLayout(lerret,BoxLayout.Y_AXIS));
	
	lerret.add(exportPanel);
	lerret.add(buttonPanel);
	
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
	/*Display the window.*/
	pack();
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
    }
    
    public void actionPerformed(ActionEvent event) {
	
	log.debug("ActionPerformed: "+event.getActionCommand());
	
	if (event.getSource().equals(closeButton)) {
	    dispose();
	    return;
	}
	
	if (event.getSource().equals(exportButton)) {
	    
	    dispose();
	    
	    String title = titleTextField.getText();
	    
	    String orderBy = "Title";
	    
	    if (orderByDirectedBy.isSelected())
		orderBy = "Directed By";
	    else if (orderByRating.isSelected())
		orderBy = "Rating";
	    else if (orderByDate.isSelected())
		orderBy = "Date";
	    
	    DefaultListModel listModel;
	    
	    String currentList = "Show All";
	    
	    if (exportCurrentList.isSelected())
		currentList = MovieManager.getConfig().getCurrentList();
	    
	    if (exportCurrentList.isSelected() && !currentList.equals("Show All"))
		listModel = MovieManager.getIt().getDatabase().getMoviesList(orderBy, currentList);
	    else
		listModel = MovieManager.getIt().getDatabase().getMoviesList(orderBy);
	    
	    if (applyCurrentAdvancedSearchSettings.isSelected()) {
		ModelDatabaseSearch options = MovieManager.getIt().getFilterOptions();
		options.setListName(currentList);
		
		if (currentList.equals("Show All"))
		    options.setListOption(0);
		else
		    options.setListOption(1);
		
		listModel = MovieManager.getIt().getDatabase().getMoviesList(options);
	    }
	    
	    if (simpleExport.isSelected()) {
		if (enableXhtml.isSelected())
		    new MovieManagerCommandExportToSimpleXHTML(title, listModel).execute();
		else
		    new MovieManagerCommandExportToSimpleHTML(title, listModel).execute();
	    }
	    else {
		if (enableAlphabeticSplit.isSelected())
		    new MovieManagerCommandExportToFullHTML(true, title, listModel).execute();
		else
		    new MovieManagerCommandExportToFullHTML(false, title, listModel).execute();
	    }
	    return;
	}
	
	if (event.getSource().equals(simpleExport)) {
	    
	    enableXhtml.setEnabled(true);
	    enableAlphabeticSplit.setEnabled(false);
	    return;
	}
	
	if (event.getSource().equals(fullExport)) {
	    
	    enableXhtml.setEnabled(false);
	    enableAlphabeticSplit.setEnabled(true );
	    return;
	}
	
	MovieManager.getIt().getMoviesList().requestFocus(true);
    }
    
    public void itemStateChanged(ItemEvent e) {
	
	if (e.getSource().equals(enableAlphabeticSplit)) {
	    
	    if (enableAlphabeticSplit.isSelected()) {
		orderByMovieTitle.setSelected(true);
		enableAlphabeticSplit.setSelected(true);
	    }
	}
	
	if (e.getSource().equals(orderByDirectedBy) || e.getSource().equals(orderByRating)|| e.getSource().equals(orderByDate)) {
	    enableAlphabeticSplit.setSelected(false);
	}
    }
}
