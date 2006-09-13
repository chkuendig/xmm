/**
 * @(#)MovieManagerCommandSearch.java 1.0 24.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.commands.MovieManagerCommandFilter;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.*;

import org.apache.log4j.Logger;

import java.util.*;

import java.awt.Container;
import java.awt.Dimension;
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

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class DialogSearch extends JDialog implements ActionListener, ItemListener, Runnable {
    
    static Logger log = Logger.getRootLogger();
    
    String movieTitleFilterString = "Movie Title";
    String directorFilterString = "Directed By";
    String writerFilterString = "Written By";
    String genreFilterString = "Genre";
    String castFilterString = "Cast";
    String finalFilter; /*used to set the filterOption variable in MovieManager*/
    
    String sortByString = "Sort by ";
    String dateSortString = "Date";
    String movieTitleSortString = "Title"; /*Need only Title in that string because it's used 
					     directly in the database when sorting*/
    String directorSortString = "Directed By";
    String ratingSortString = "Rating";
    String durationSortString = "Duration";
    
    String finalSort; /*used to set the sortOption variable in MovieManager*/
    
    String seenString = "Seen";
    String unseenString = "Unseen";
    String enableSeenString = "Enable";
    int seen = MovieManager.getConfig().getFilterSeen();   /*used to set the seen variable in MovieManager
							 0 means the seen is disabled (and seenButton selected), 
							 1 means the seen is disabled (and unseenButton selected),
							 2 means show only seen, 
							 3 means show only unseen*/
    
    String ratingAboveString = "Above";
    String ratingBelowString = "Below";
    String enableRatingString = "Enable";
    int ratingValue;
    int ratingOption = MovieManager.getConfig().getRatingOption();  /*used to set the ratingOption variable in MovieManager
								  0 means the seen is disabled (and ratingAboveButton selected), 
								  1 means the seen is disabled (and ratingBelowButton selected), 
								  2 means show only above the ratingValue,
								  3 means show only below the ratingValue.
								  ratingValue == value from JComboBox*/
    
    String dateAboveString = "Above";
    String dateBelowString = "Below";
    String enableDateString = "Enable";
    int dateValue;
    int dateOption = MovieManager.getConfig().getDateOption();  /*used to set the dateOption variable in MovieManager
								  0 means the date is disabled (and dateAboveButton selected), 
								  1 means the date is disabled (and dateBelowButton selected), 
								  2 means show only above the dateValue,
								  3 means show only below the dateValue.
								  dateValue == value from JTextField*/
    
    
    JButton buttonSave;
    JButton buttonApply;
    
    JRadioButton dateSortButton;
    JRadioButton movieTitleSortButton;
    JRadioButton directorSortButton;
    JRadioButton ratingSortButton;
    JRadioButton durationSortButton;
    
    JRadioButton movieTitleFilterButton;
    JRadioButton directorFilterButton;
    JRadioButton writerFilterButton;
    JRadioButton genreFilterButton;
    JRadioButton castFilterButton;
    
    JCheckBox includeAkaTitles;
    
    JRadioButton seenButton;
    JRadioButton unseenButton;
    JCheckBox enableSeenButton;
    
    JCheckBox enableRatingButton;
    JRadioButton ratingAboveButton;
    JRadioButton ratingBelowButton;
    JComboBox rateList;
    
    JCheckBox enableDateButton;
    JRadioButton dateAboveButton;
    JRadioButton dateBelowButton;
    JTextField dateTextField;
    
    JPanel generalAliasPanel;
    JPanel additionalAliasPanel;
    
    int generalInfoFieldsCount = 0;
    int additionalInfoFieldsCount = 0;
    
    
    JTabbedPane allTabbedPanes;
    
    public DialogSearch() {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    MovieManager.getIt().setDialogSearch(null);
		    dispose();
		}
	    });
	
	MovieManager.getIt().setDialogSearch(this);
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    MovieManager.getIt().setDialogSearch(null);
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	setTitle("Advanced search");
	setResizable(false);
	

	/*Filter panel*********************/
	
	movieTitleFilterButton = new JRadioButton(movieTitleFilterString +"  ");
	movieTitleFilterButton.setActionCommand(movieTitleFilterString);
	
	movieTitleFilterButton.addItemListener(this);

	includeAkaTitles = new JCheckBox("Include \"Also known as\" titles");
	//includeAkaTitles = new JCheckBox("Include aka");
	
	JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	titlePanel.add(movieTitleFilterButton);
	titlePanel.add(includeAkaTitles);
	
	directorFilterButton = new JRadioButton(directorFilterString);
	directorFilterButton.setActionCommand(directorFilterString);
		
	writerFilterButton = new JRadioButton(writerFilterString);
	writerFilterButton.setActionCommand(writerFilterString);
		
	genreFilterButton = new JRadioButton(genreFilterString);
	genreFilterButton.setActionCommand(genreFilterString);
		
	castFilterButton = new JRadioButton(castFilterString);
	castFilterButton.setActionCommand(castFilterString);
		
	/*Retrieves the current filterOption and sets the appropriate button selected*/
	finalFilter = MovieManager.getConfig().getFilterCategory();
	
	includeAkaTitles.setEnabled(false);
	
	if (finalFilter.equals("Movie Title")) {
	    movieTitleFilterButton.setSelected(true);
	    includeAkaTitles.setEnabled(true);
	}
	else if(finalFilter.equals("Directed By"))
	    directorFilterButton.setSelected(true);
	
	else if(finalFilter.equals("Written By"))
	    writerFilterButton.setSelected(true);

	else if(finalFilter.equals("Genre"))
	    genreFilterButton.setSelected(true);

	else if(finalFilter.equals("Cast"))
	    castFilterButton.setSelected(true);
	
	
	includeAkaTitles.setSelected(MovieManager.getConfig().getIncludeAkaTitlesInFilter());
	
	/*Group the radio buttons.*/
	ButtonGroup filterGroup = new ButtonGroup();
	filterGroup.add(movieTitleFilterButton);
	filterGroup.add(directorFilterButton);
	filterGroup.add(writerFilterButton);
	filterGroup.add(genreFilterButton);
	filterGroup.add(castFilterButton);
     	
	/*Register a listener for the radio buttons.*/
	movieTitleFilterButton.addActionListener(this);
	directorFilterButton.addActionListener(this);
	writerFilterButton.addActionListener(this);
	genreFilterButton.addActionListener(this);
	castFilterButton.addActionListener(this);
	
	/*Put the radio buttons in a column in a panel.*/
        JPanel radioFilterPanel = new JPanel(new GridLayout(0, 1));
	
	radioFilterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												       " Filter Category ",
												       TitledBorder.DEFAULT_JUSTIFICATION,
												       TitledBorder.DEFAULT_POSITION,
											    new Font(radioFilterPanel.getFont().getName(),Font.BOLD, radioFilterPanel.getFont().getSize())),
                                                           BorderFactory.createEmptyBorder(5,30,5,5)));
	
	radioFilterPanel.add(titlePanel);
	radioFilterPanel.add(directorFilterButton);
	radioFilterPanel.add(writerFilterButton);
	radioFilterPanel.add(genreFilterButton);
	radioFilterPanel.add(castFilterButton);
	
	
	/*Sort panel*/
	dateSortButton = new JRadioButton(dateSortString);
	dateSortButton.setActionCommand(dateSortString);
	
	movieTitleSortButton = new JRadioButton("Movie "+movieTitleSortString);
	movieTitleSortButton.setActionCommand(movieTitleSortString);
	
	directorSortButton = new JRadioButton(directorSortString);
	directorSortButton.setActionCommand(directorSortString);
	
	ratingSortButton = new JRadioButton(ratingSortString);
	ratingSortButton.setActionCommand(ratingSortString);
	
	durationSortButton = new JRadioButton(durationSortString);
	durationSortButton.setActionCommand(durationSortString);
	
	/*Retrieves the current sortOption and sets the appropriate button selected*/
	finalSort = MovieManager.getConfig().getSortOption();
	
	if("Date".equals(finalSort))
	    dateSortButton.setSelected(true);
	
	else if ("Title".equals(finalSort))
	    movieTitleSortButton.setSelected(true);
	
	else if("Directed By".equals(finalSort))
	    directorSortButton.setSelected(true);
	
	else if("Rating".equals(finalSort))
	    ratingSortButton.setSelected(true);
	
	else if("Duration".equals(finalSort))
	    durationSortButton.setSelected(true);
	
	/*Group the radio buttons.*/
	ButtonGroup sortGroup = new ButtonGroup();
	sortGroup.add(movieTitleSortButton);
	sortGroup.add(directorSortButton);
	sortGroup.add(ratingSortButton);
	sortGroup.add(dateSortButton);
     	sortGroup.add(durationSortButton);
	
	/*Register a listener for the radio buttons.*/
	dateSortButton.addActionListener(this);
	movieTitleSortButton.addActionListener(this);
	directorSortButton.addActionListener(this);
	ratingSortButton.addActionListener(this);
	durationSortButton.addActionListener(this);
	includeAkaTitles.addActionListener(this);
	
	/*Put the radio buttons in a column in a panel.*/
        JPanel radioSortPanel = new JPanel(new GridLayout(0, 1));
	/*All the variables referes to sort, but I think Order is more....better in some way... ,-)*/
		
	
	radioSortPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                                            " Order Category ",
                                                                                            TitledBorder.DEFAULT_JUSTIFICATION,
                                                                                            TitledBorder.DEFAULT_POSITION,
											    new Font(radioSortPanel.getFont().getName(),Font.BOLD, radioSortPanel.getFont().getSize())),
                                                           BorderFactory.createEmptyBorder(5,30,5,5)));
	
	radioSortPanel.add(movieTitleSortButton);
	radioSortPanel.add(directorSortButton);
	radioSortPanel.add(ratingSortButton);
	radioSortPanel.add(dateSortButton);
	radioSortPanel.add(durationSortButton);
	
	
	

	/*Seen panel*********************/

	JPanel radioSeenPanel = new JPanel(new GridLayout(1, 0));
		
	radioSeenPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												       " Show only ",
												       TitledBorder.DEFAULT_JUSTIFICATION,
												       TitledBorder.DEFAULT_POSITION,
											    new Font(radioSeenPanel.getFont().getName(),Font.BOLD, radioSeenPanel.getFont().getSize())),
                                                           BorderFactory.createEmptyBorder(5,5,5,5)));
	
	enableSeenButton = new JCheckBox(enableSeenString);
	enableSeenButton.setActionCommand(enableSeenString);
		
	seenButton = new JRadioButton(seenString);
	seenButton.setActionCommand(seenString);
		
	unseenButton = new JRadioButton(unseenString);
	unseenButton.setActionCommand(unseenString);
	unseenButton.setEnabled(false);
		
	int filterSeen = MovieManager.getConfig().getFilterSeen();
	
	/*seen is disabled but seen button is selected*/
	if (filterSeen == 0) {
	    enableSeenButton.setSelected(false);
	    seenButton.setEnabled(false);
	    seenButton.setSelected(true);
	    unseenButton.setEnabled(false);
	}
	
	/*seen is disabled but unseen button is selected*/
	if (filterSeen == 1) {
	    enableSeenButton.setSelected(false);
	    seenButton.setEnabled(false);
	    unseenButton.setEnabled(false);
	    unseenButton.setSelected(true);
	}
	
	/*seen is enabled and seen button is selected*/
	if (filterSeen == 2) {
	    enableSeenButton.setSelected(true);
	    seenButton.setEnabled(true);
	    seenButton.setSelected(true);
	    unseenButton.setEnabled(true);
	}
	
	/*seen is enabled and unseen button is selected*/
	if (filterSeen == 3) {
	    enableSeenButton.setSelected(true);
	    seenButton.setEnabled(true);
	    unseenButton.setEnabled(true);
	    unseenButton.setSelected(true);
	}
	
	ButtonGroup seenGroup = new ButtonGroup();
        seenGroup.add(seenButton);
	seenGroup.add(unseenButton);
	
	radioSeenPanel.add(enableSeenButton);
	radioSeenPanel.add(seenButton);
	radioSeenPanel.add(unseenButton);
	
	seenButton.addActionListener(this);
	unseenButton.addActionListener(this);
	enableSeenButton.addItemListener(this);
	
	/*Rating panel*/
	JPanel radioRatingPanel = new JPanel(new GridLayout(1, 0));
	
	radioRatingPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												       " Show only movies with chosen rating and ",
												       TitledBorder.DEFAULT_JUSTIFICATION,
												       TitledBorder.DEFAULT_POSITION,
											    new Font(radioRatingPanel.getFont().getName(),Font.BOLD, radioRatingPanel.getFont().getSize())),
                                                           BorderFactory.createEmptyBorder(5,5,5,5)));
	
	enableRatingButton = new JCheckBox(enableRatingString);
	enableRatingButton.setActionCommand(enableRatingString);
		
	ratingAboveButton = new JRadioButton(ratingAboveString);
	ratingAboveButton.setActionCommand(ratingAboveString);
		
	ratingBelowButton = new JRadioButton(ratingBelowString);
	ratingBelowButton.setActionCommand(ratingBelowString);
	
	/* rateList.setSelectedIndex is to pick the index as to the former 
	  rating value that was picked. -10*-1 is because the values in 
	  rateValues is placed opposite of the way the index is numbered.
	  so string 10 is on index 0 and value 6 is on index 4, so to get 
	  the correct index this is necessary. I placed the values like 
	  this and not the other way around because it looks better on 
	  the dropdown menu.. ;-)
	*/ 
	
	String[] rateValues = { "10", "9", "8", "7", "6", "5", "4", "3", "2", "1" };
	rateList = new JComboBox(rateValues);
	
	rateList.setPreferredSize(new Dimension((int) ratingBelowButton.getPreferredSize().getWidth()+10, (int) rateList.getPreferredSize().getHeight()));
	
	/* Rating is disabled but ratingAbove button is selected */
	if (MovieManager.getConfig().getRatingOption() == 0) {
	    enableRatingButton.setSelected(false);
	    ratingAboveButton.setEnabled(false);
	    ratingAboveButton.setSelected(true);
	    ratingBelowButton.setEnabled(false);
	    rateList.setEnabled(false);
	    rateList.setSelectedIndex((int)(MovieManager.getConfig().getRatingValue()-10)*-1);
	}
	
	/* Rating is disabled but ratingBelow button is selected */
	if (MovieManager.getConfig().getRatingOption() == 1) {
	    enableRatingButton.setSelected(false);
	    ratingAboveButton.setEnabled(false);
	    ratingBelowButton.setEnabled(false);
	    ratingBelowButton.setSelected(true);
	    rateList.setEnabled(false);
	    rateList.setSelectedIndex((int)(MovieManager.getConfig().getRatingValue()-10)*-1);
	}
	
	/* Rating is enabled and ratingAbove button is selected */
	if (MovieManager.getConfig().getRatingOption() == 2) {
	    enableRatingButton.setSelected(true);
	    ratingAboveButton.setEnabled(true);
	    ratingAboveButton.setSelected(true);
	    ratingBelowButton.setEnabled(true);
	    rateList.setEnabled(true);
	    rateList.setSelectedIndex((int)(MovieManager.getConfig().getRatingValue()-10)*-1);
	}
	
	/* Rating is enabled and ratingBeow button is selected */
	if (MovieManager.getConfig().getRatingOption() == 3) {
	    enableRatingButton.setSelected(true);
	    ratingAboveButton.setEnabled(true);
	    ratingBelowButton.setEnabled(true);
	    ratingBelowButton.setSelected(true);
	    rateList.setEnabled(true);
	    rateList.setSelectedIndex((int)(MovieManager.getConfig().getRatingValue()-10)*-1);
	}
	
  	ButtonGroup ratingGroup = new ButtonGroup();
        ratingGroup.add(ratingAboveButton);
	ratingGroup.add(ratingBelowButton);
	
	radioRatingPanel.add(enableRatingButton);
	radioRatingPanel.add(ratingAboveButton);
	radioRatingPanel.add(ratingBelowButton);
	radioRatingPanel.add(rateList);
	
	ratingAboveButton.addActionListener(this);
	ratingBelowButton.addActionListener(this);
	enableRatingButton.addItemListener(this);
	
	/* Date panel */
	JPanel radioDatePanel = new JPanel(new GridLayout(1, 0));
	
	radioDatePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												       " Show only movies from chosen date and ",
												       TitledBorder.DEFAULT_JUSTIFICATION,
												       TitledBorder.DEFAULT_POSITION,
											    new Font(radioDatePanel.getFont().getName(),Font.BOLD, radioDatePanel.getFont().getSize())),
                                                           BorderFactory.createEmptyBorder(5,5,5,5)));
	
	enableDateButton = new JCheckBox(enableDateString);
	enableDateButton.setActionCommand(enableDateString);
		
	dateAboveButton = new JRadioButton(dateAboveString);
	dateAboveButton.setActionCommand(dateAboveString);
		
	dateBelowButton = new JRadioButton(dateBelowString);
	dateBelowButton.setActionCommand(dateBelowString);
		
	enableDateButton.addActionListener(this);
	dateAboveButton.addActionListener(this);
	dateBelowButton.addItemListener(this);
	
	dateTextField = new JTextField(4);
	dateTextField.setEditable(true);
	dateTextField.setDocument(new DocumentRegExp("(\\d)*",4));
		
	int dateOption = MovieManager.getConfig().getDateOption();
	
	/*Date is disabled but dateAbove button is selected*/
	if (dateOption == 0) {
	    enableDateButton.setSelected(false);
	    dateAboveButton.setEnabled(false);
	    dateAboveButton.setSelected(true);
	    dateBelowButton.setEnabled(false);
	    dateTextField.setEnabled(false);
	    dateTextField.setText((MovieManager.getConfig().getDateValue()));
	}
	
	/*Date is disabled but dateBelow button is selected*/
	if (dateOption == 1) {
	    enableDateButton.setSelected(false);
	    dateAboveButton.setEnabled(false);
	    dateBelowButton.setEnabled(false);
	    dateBelowButton.setSelected(true);
	    dateTextField.setEnabled(false);
	    dateTextField.setText((MovieManager.getConfig().getDateValue()));
	}
	/*Date is enabled and dateAbove button is selected*/
	if (dateOption == 2) {
	    enableDateButton.setSelected(true);
	    dateAboveButton.setEnabled(true);
	    dateAboveButton.setSelected(true);
	    dateBelowButton.setEnabled(true);
	    dateTextField.setEnabled(true);
	    dateTextField.setText((MovieManager.getConfig().getDateValue()));
	}
	
	/*Date is enabled and dateBelow button is selected*/
	if (dateOption == 3) {
	    enableDateButton.setSelected(true);
	    dateAboveButton.setEnabled(true);
	    dateBelowButton.setEnabled(true);
	    dateBelowButton.setSelected(true);
	    dateTextField.setEnabled(true);
	    dateTextField.setText((MovieManager.getConfig().getDateValue()));
	}
	
	ButtonGroup dateGroup = new ButtonGroup();
        dateGroup.add(dateAboveButton);
	dateGroup.add(dateBelowButton);
	
	radioDatePanel.add(enableDateButton);
	radioDatePanel.add(dateAboveButton);
	radioDatePanel.add(dateBelowButton);
	radioDatePanel.add(dateTextField);
	
	dateAboveButton.addActionListener(this);
	dateBelowButton.addActionListener(this);
	enableDateButton.addItemListener(this);
	
	
	
	/*Button panel*/
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));
	
	buttonApply = new JButton("Apply");
	buttonApply.setToolTipText("Apply selected settings");
	
	buttonApply.addActionListener(this);
	buttonPanel.add(buttonApply);
	
	buttonSave = new JButton("Save");
	buttonSave.setToolTipText("Save and Close");
	
	buttonSave.addActionListener(this);
	buttonPanel.add(buttonSave);
		
	JPanel settings = new JPanel();
	settings.setBorder(BorderFactory.createEmptyBorder(8,8,5,8));
	settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
	settings.add(radioFilterPanel);
	settings.add(radioSortPanel);
	settings.add(radioSeenPanel);
	settings.add(radioRatingPanel);
	settings.add(radioDatePanel);
	
	allTabbedPanes = new JTabbedPane();
	allTabbedPanes.setBorder(BorderFactory.createEmptyBorder(8,8,5,8));
	allTabbedPanes.add("Settings", settings);
	
	allTabbedPanes.add("Loading Alias list..", null);
	allTabbedPanes.add("loading Alias list..", null);
	
	allTabbedPanes.setEnabledAt(1, false);
	allTabbedPanes.setEnabledAt(2, false);
	
	Container contentPane = getContentPane();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	
	contentPane.add(allTabbedPanes);
	contentPane.add(buttonPanel);
	
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
	/*Display the window.*/
	pack();
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	
	
	try {
	    SwingUtilities.invokeLater(this);
	} catch (Exception e) {
	    log.error("Exception;" + e.getMessage());
	}
    }
    
    

    /* Setting the search aliases */
	
    public void run() {
	
	SwingWorker worker = new SwingWorker() {
		    public Object construct() {
			try {
			    Thread.currentThread().setPriority(4);
			    
			    ArrayList tableNames = MovieManager.getIt().getDatabase().getTableNames();
			    
			    ArrayList generalInfoFields = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
			    generalAliasPanel = new JPanel(new GridLayout(generalInfoFields.size()+1, 3));
	
			    JLabel tableName;
			    JLabel columnName;
			    JTextField alias;
	
			    String tmpKey;
	
			    generalAliasPanel.add(new JLabel("Table Name"));   
			    generalAliasPanel.add(new JLabel("Column Name"));
			    generalAliasPanel.add(new JLabel("Alias"));
	
			    HashMap searchAlias = MovieManager.getConfig().getSearchAlias();
	
			    for (int i = 0; i < generalInfoFields.size(); i++) {
	    
				tableName = new JLabel((String) tableNames.get(5));
				columnName = new JLabel((String) generalInfoFields.get(i));
				alias = new JTextField(10);
	    
				tmpKey = (((String) tableNames.get(5)).toLowerCase() +"."+ (String) generalInfoFields.get(i)).replaceAll("_", " ");
	    
				if (searchAlias.containsKey(tmpKey)) {
				    alias.setText((String) searchAlias.remove(tmpKey));
				}
				
				generalAliasPanel.add(tableName);   
				generalAliasPanel.add(columnName);
				generalAliasPanel.add(alias);

				generalInfoFieldsCount++;
			    }
			    
			    ArrayList additionalInfoFields = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
			    ArrayList extraInfoFields = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();
	
			    additionalAliasPanel = new JPanel(new GridLayout(additionalInfoFields.size()+ extraInfoFields.size() +1, 3));
	
			    additionalAliasPanel.add(new JLabel("Table Name"));   
			    additionalAliasPanel.add(new JLabel("Column Name"));
			    additionalAliasPanel.add(new JLabel("Alias"));
	
			    String tmpColumn;
			    String table = (String) tableNames.get(0);

			    for (int i = 0;i < additionalInfoFields.size(); i++) {
	    
				tmpColumn = (String) additionalInfoFields.get(i);
	    
				tableName = new JLabel(table);
				columnName = new JLabel(tmpColumn);
				alias = new JTextField(10);
	    
				if (tmpColumn.equals("SubTitles"))
				    tmpColumn = tmpColumn.replaceFirst("SubTitles", "Subtitles");
	    
				tmpKey = (table.toLowerCase() +"."+ tmpColumn).replaceAll("_", " ");
	    
				if (searchAlias.containsKey(tmpKey)) {
				    alias.setText((String) searchAlias.remove(tmpKey));
				}
				
				additionalAliasPanel.add(tableName);   
				additionalAliasPanel.add(columnName);
				additionalAliasPanel.add(alias);
	    
				additionalInfoFieldsCount++;
			    }
	
			    for (int i = 0;i < extraInfoFields.size(); i++) {
	    
				tableName = new JLabel((String) tableNames.get(2));
				columnName = new JLabel((String) extraInfoFields.get(i));
				alias = new JTextField(10);
	    
				tmpKey = (((String) tableNames.get(2)).toLowerCase() +"."+ (String) extraInfoFields.get(i)).replaceAll("_", " ");
	     
				if (searchAlias.containsKey(tmpKey)) {
				    alias.setText((String) searchAlias.remove(tmpKey));
				}
				
				additionalAliasPanel.add(tableName);   
				additionalAliasPanel.add(columnName);
				additionalAliasPanel.add(alias);
	    
				additionalInfoFieldsCount++;
			    }
			    
			    allTabbedPanes.remove(1);
			    allTabbedPanes.remove(1);
			    
			    allTabbedPanes.add("General Info Alias", generalAliasPanel);
			    allTabbedPanes.add("Additional Info Alias", additionalAliasPanel);
			    
			} catch (Exception e) {
			    return "";
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    pack();
				    setVisible(true);
				}});
			
			return "";
		    }
	    };
	worker.start();
    }
    
    
    
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: "+ event.getActionCommand());
	
	/*Saving settings and closing window*/
	if (event.getSource().equals(buttonSave)) {
	    saveOptions(false);
	    return;
	}
	
	/* Saving and applying settings without closing window */
	if (event.getSource().equals(buttonApply)) {
	    saveOptions(true);
	    return;
	}
	
	/*Filter*/
	if (event.getSource().equals(movieTitleFilterButton)) {
	    finalFilter = movieTitleFilterString;
	    MovieManager.getConfig().setFilterCategory(finalFilter);
	    includeAkaTitles.setEnabled(true);
	    return;
	}
	
	if (event.getSource().equals(directorFilterButton)) {
	    finalFilter = directorFilterString;
	    return;
	}
	
	if (event.getSource().equals(writerFilterButton)) {
	    finalFilter = writerFilterString;
	    return;
	}
	
	if (event.getSource().equals(genreFilterButton)) {
	    finalFilter = genreFilterString;
	    return;
	}
	
	if (event.getSource().equals(castFilterButton)) {
	    finalFilter = castFilterString;
	    return;
	}
	
	
	/*Sort*/
	if (event.getSource().equals(movieTitleSortButton)) {
	    finalSort = movieTitleSortString;
	    return;
	}
	
	if (event.getSource().equals(directorSortButton)) {
	    finalSort = directorSortString;
	    return;
	}
	
	if (event.getSource().equals(ratingSortButton)) {
	    finalSort = ratingSortString;
	    return;
	}
	
	if (event.getSource().equals(dateSortButton)) {
	    finalSort = dateSortString;
	    return;
	}
	
	if (event.getSource().equals(durationSortButton)) {
	    finalSort = durationSortString;
	    return;
	}
	
	/*Seen*/
	if (event.getSource().equals(seenButton)) {
	    seen = 2;
	    return;
  	}
	
  	if (event.getSource().equals(unseenButton)) {
  	    seen = 3;
	    return;
  	}
	
	/*Rating*/
	if (event.getSource().equals(ratingAboveButton)) {
  	    ratingOption = 2;
	    return;
  	}
	
	if (event.getSource().equals(ratingBelowButton)) {
  	    ratingOption = 3;
	    return;
	}
	
	if (event.getSource().equals(ratingBelowButton)) {
  	    ratingOption = 3;
	    return;
	}
	
	/*Date*/
	if (event.getSource().equals(dateAboveButton)) {
  	    dateOption = 2;
	    return;
  	}
	
	if (event.getSource().equals(dateBelowButton)) {
  	    dateOption = 3;
	    return;
	}
	
	MovieManager.getIt().getMoviesList().requestFocus(true);
      }
    
    /*This is for the three enable buttons, seen, rating, date.*/
    public void itemStateChanged(ItemEvent e) {
        
        Object source = e.getItemSelectable();
	
        if (source.equals(enableSeenButton)) {
	    
	    if (enableSeenButton.isSelected()) {
		seenButton.setEnabled(true);
		unseenButton.setEnabled(true);
		
		if (seenButton.isSelected())
		    seen = 2;
		
		if (unseenButton.isSelected())
		    seen = 3;
	    }
	    else {
		seenButton.setEnabled(false);
		unseenButton.setEnabled(false);
		
		if (seenButton.isSelected())
		    seen = 0;
		
		if (unseenButton.isSelected())
		    seen = 1;
	    }
	}
	
	if (source.equals(enableRatingButton)) {
	    
	    if (enableRatingButton.isSelected()) {
		ratingAboveButton.setEnabled(true);
		ratingBelowButton.setEnabled(true);
		rateList.setEnabled(true);
		
		if (ratingAboveButton.isSelected())
		    ratingOption = 2;
		
		if (ratingBelowButton.isSelected())
		    ratingOption = 3;
	    }
	    else {
		ratingAboveButton.setEnabled(false);
		ratingBelowButton.setEnabled(false);
		rateList.setEnabled(false);
		
		if (ratingAboveButton.isSelected())
		    ratingOption = 0;
		
		if (ratingBelowButton.isSelected())
		    ratingOption = 1;
	    }
	}
	
	if (source.equals(enableDateButton)) {
	    
	    if (enableDateButton.isSelected()) {
		dateAboveButton.setEnabled(true);
		dateBelowButton.setEnabled(true);
		dateTextField.setEnabled(true);
		
		if (dateAboveButton.isSelected())
		    dateOption = 2;
		
		if (dateBelowButton.isSelected())
		    dateOption = 3;
	    }
	    else {
		dateAboveButton.setEnabled(false);
		dateBelowButton.setEnabled(false);
		dateTextField.setEnabled(false);
		
		if (dateAboveButton.isSelected())
		    dateOption = 0;
		
		if (dateBelowButton.isSelected())
		    dateOption = 1;
	    }
	}
	
	if (source.equals(movieTitleFilterButton)) {
	    
	    if (!movieTitleFilterButton.isSelected())
		includeAkaTitles.setEnabled(false);
	}
    }
    
    public void saveOptions(boolean apply) {
	
	if (enableDateButton.isSelected()) {
	    if (dateTextField.getText().length() != 4) {
		DialogAlert alert = new DialogAlert("Alert", "The date must be 4 integers, no more...no less!");
		alert.setVisible(true);
		return;
	    }
	}
	    
	/* Saving the settings */
	MovieManager.getConfig().setFilterCategory(finalFilter);
	MovieManager.getConfig().setSortOption(finalSort);
	MovieManager.getConfig().setFilterSeen(seen);
	MovieManager.getConfig().setRatingOption(ratingOption);
	MovieManager.getConfig().setRatingValue(Integer.parseInt((String) rateList.getSelectedItem()));
	MovieManager.getConfig().setDateOption(dateOption);
	MovieManager.getConfig().setDateValue(dateTextField.getText());
	    
	MovieManager.getConfig().setIncludeAkaTitlesInFilter(includeAkaTitles.isSelected());
	    
	if ("Title".equals(finalSort))
	    ModelEntry.sort = 1;
	else if ("directed".equals(finalSort))
	    ModelEntry.sort = 2;
	else if ("Rating".equals(finalSort))
	    ModelEntry.sort = 3;
	else if ("Date".equals(finalSort))
	    ModelEntry.sort = 4;
	else if ("Duration".equals(finalSort))
	    ModelEntry.sort = 5;
	    
	HashMap searchAlias = MovieManager.getConfig().getSearchAlias();
	    
	String tmp;
	int index = 5;
	    
	for (int i = 0; i < generalInfoFieldsCount; i++) {
		
	    tmp = ((JTextField) generalAliasPanel.getComponent(index)).getText();
	    
	    if (!tmp.equals("")) {
		searchAlias.put((((JLabel) generalAliasPanel.getComponent(index -2)).getText().replaceAll("_", " ").toLowerCase() +"."+((JLabel) generalAliasPanel.getComponent(index -1)).getText().replaceAll("_", " ")), tmp);
	    }
	    index += 3;
	}
	    	    
	index = 5;
	    
	for (int i = 0; i < additionalInfoFieldsCount; i++) {

	    tmp = ((JTextField) additionalAliasPanel.getComponent(index)).getText();
		
	    if (!tmp.equals("")) {
		searchAlias.put((((JLabel) additionalAliasPanel.getComponent(index -2)).getText().replaceAll("_", " ").toLowerCase() +"."+((JLabel) additionalAliasPanel.getComponent(index -1)).getText().replaceAll("_", " ").replaceFirst("SubTitles", "Subtitles")), tmp);
	    }
	    index += 3;
	}
	    
	if (apply) {
	    /*Applying the new setting to the movielist*/
	    new MovieManagerCommandFilter("", null, true, true).execute();
	}
	else {
	    MovieManager.getIt().setDialogSearch(null);
	    dispose();
	}
    }
}


    
    
