/**
 * @(#)DialogIMDB.java
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandPlay;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager.KeyMapping;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

/* This class is a total mess */
public class DialogIMDbMultiAdd extends DialogIMDB {
    
	private static final long serialVersionUID = 9074815790929713958L;

	static Logger log = Logger.getLogger(DialogIMDbMultiAdd.class);
	
	JTextField searchStringField;
	JButton playMediaFiles;
	JButton abortButton;
	JTextArea fileLocation;
	JButton buttonSearch;
	JButton addWithoutIMDBInfoButton;
	
    boolean multiAddByFile = false;
    
    boolean addInfoToExistingMovie = false;
    
    ImdbImportOption multiAddSelectOption = ImdbImportOption.displayList;
    
    enum ShowListOption {show, no_show};
    
    Files multiAddFile = null;
    
    String filename = null;
    
    String imdbId = null;
    
    boolean getUrlKeyOnly = false;

    String addToThisList = null; 
    
    boolean switchBetweenIMDBAndDatabase = false;
    boolean addWithoutIMDBInfo = false;
        
    long time;
            
    private boolean aborted = false;
    private boolean dropImdbInfoSet = false;
    
    DialogIMDbMultiAdd thisDialog = this;
    
    public boolean getAborted() {
    	return aborted;
    }
        
    public boolean getDropIMDbInfo() {
    	return dropImdbInfoSet;
    }
    
    public void resetFeedbackValues() {
    	setCanceled(false);
    	aborted = false;
        dropImdbInfoSet = false;
    }
    
    String getFilename() {
        return filename;
    }
   
    public DialogIMDbMultiAdd(ModelEntry modelEntry, boolean getUrlKeyOnly, String alternateTitle) {
    	super(modelEntry, alternateTitle, false);
    	
    	if (alternateTitle != null)
    		setTitle(alternateTitle);

    	this.getUrlKeyOnly = getUrlKeyOnly;
    	addWithoutIMDBInfo = true;

    	createUpdateIMDbComponents();

    	searchStringField.setText(alternateTitle);
    	
    	executeSearchMultipleMovies();
    }
       
    public DialogIMDbMultiAdd(ModelEntry modelEntry, String searchString, 
    		ImdbImportOption multiAddSelectOption, String addToThisList) {
    	this(modelEntry, searchString, multiAddSelectOption, addToThisList, true);
    }
    
    /**
     * Constructor - Used when importing.
     **/
    private DialogIMDbMultiAdd(ModelEntry modelEntry, String searchString, 
    		ImdbImportOption multiAddSelectOption, String addToThisList, boolean performSearch) {
    	    	
    	super(modelEntry, searchString, false);
    	    	
    	createMultiAddComponents();
    	
    	/* Dialog creation...*/
    	 if (searchString != null)
           	setTitle(searchString);
          	        
    	 addWithoutIMDBInfo = true;
        
        this.addToThisList = addToThisList;
        
        /* Sets parent... */
        this.multiAddSelectOption = multiAddSelectOption;
        
        switchBetweenIMDBAndDatabase = true;
        
        searchStringField.setText(searchString);
        
        if (performSearch)
        	performSearch(searchString, null, null);
    }
    
    /**
     * Constructor - When adding multiple movies by file.
     **/
    public DialogIMDbMultiAdd(String _imdbId, ModelEntry modelEntry, String searchString, String year,
    		String filename, Files multiAddFile, ImdbImportOption multiAddSelectOption, String addToThisList, 
    		ArrayList<ModelIMDbSearchHit> hits, boolean performSearch) {
    
    	/* Dialog creation...*/
    	this(modelEntry, searchString, multiAddSelectOption, addToThisList, false);
    	
    	
        imdbId = (_imdbId == null || _imdbId.equals("")) ? null : null; //$NON-NLS-1$
        this.multiAddFile = multiAddFile;
        this.filename = filename;
        this.multiAddByFile = true;
        
        setTitle(filename);
          
        setFileLocationContent();
        
        if (performSearch)
        	performSearch(searchString, year, hits);
    }

   
    
    public void dispose() {
    	MovieManager.getConfig().setMultiAddIMDbDialogWindowSize(getSize());
    	super.dispose();
    }


    void createMultiAddComponents() {

    	addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			dispose();
    		}
    	});


    	JPanel multipleMovieButtons = new JPanel();
    	multipleMovieButtons.setLayout(new FlowLayout());

    	buttonSearch = createButtonSearch();

    	multipleMovieButtons.add(buttonSearch);

    	if (switchBetweenIMDBAndDatabase) {

    		if (multiAddFile != null) {
    			JButton chooseBetweenImdbAndLocalDatabase = createChooseBetweenImdbAndLocalDatabaseButton();
    			multipleMovieButtons.add(chooseBetweenImdbAndLocalDatabase);
    		}
    	}

    	addWithoutIMDBInfoButton = createAddWithoutIMDBInfoButton();
    	multipleMovieButtons.add(addWithoutIMDBInfoButton);
    	
    	System.err.println("CREATED addWithoutIMDBInfoButton:" + addWithoutIMDBInfoButton);
    	
    	playMediaFiles = createPlayButton();

    	multipleMovieButtons.add(playMediaFiles);

    	abortButton = createAbortButton();
    	multipleMovieButtons.add(abortButton);

    	JPanel multiAddButtonsPanel = new JPanel();
    	multiAddButtonsPanel.setLayout(new BorderLayout());
    	multiAddButtonsPanel.add(createSearchStringPanel(), BorderLayout.NORTH);
    	multiAddButtonsPanel.add(multipleMovieButtons, BorderLayout.CENTER);
    	multiAddButtonsPanel.add(createFileLocationPanel(), BorderLayout.SOUTH);

    	panelButtons.add(multiAddButtonsPanel, BorderLayout.CENTER);

    	buttonChoose.setEnabled(false);
    	buttonCancel.setText(Localizer.get("DialogIMDB.button.cancel.text.skip-movie")); //$NON-NLS-1$

    	pack();

    	Dimension dim = MovieManager.getConfig().getMultiAddIMDbDialogWindowSize();

    	if (dim != null && dim.height > 0 && dim.width > 0) {
    		setSize(dim);
    	}

    	setHotkeyModifiersMultiAdd();
    }
    
    void createUpdateIMDbComponents() {
    	
    	addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});    	    	
    	
    	JPanel multipleMovieButtons = new JPanel();
    	multipleMovieButtons.setLayout(new FlowLayout());

    	buttonSearch = createButtonSearch();
    	multipleMovieButtons.add(buttonSearch);
    	    
    	abortButton = createAbortButton();
    	multipleMovieButtons.add(abortButton);
    	    	
    	JPanel multiAddButtonsPanel = new JPanel();
    	multiAddButtonsPanel.setLayout(new BorderLayout());
    	multiAddButtonsPanel.add(createSearchStringPanel(), BorderLayout.NORTH);
    	multiAddButtonsPanel.add(multipleMovieButtons, BorderLayout.CENTER);
    	    	
    	panelButtons.add(multiAddButtonsPanel, BorderLayout.CENTER);
    	
    	buttonCancel.setText(Localizer.get("DialogIMDB.button.cancel.text.skip-movie")); //$NON-NLS-1$
        
    	pack();
    	
    	Dimension dim = MovieManager.getConfig().getMultiAddIMDbDialogWindowSize();
    	
    	if (dim != null && dim.height > 0 && dim.width > 0) {
    		setSize(dim);
    	}
    	
        setHotkeyModifiersMultiAdd();
     }
	
    /**
     * Creates a panel containing a text field used to search
     * @return
     */
    JPanel createSearchStringPanel() {
    	
    	JPanel searchStringPanel = new JPanel();
    	searchStringPanel.setLayout(new BorderLayout());
    	searchStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-search-string.title")), BorderFactory.createEmptyBorder(4,4,4,4))); //$NON-NLS-1$
    	
    	searchStringField = new JTextField(27);
    	searchStringField.setActionCommand("Search String:"); //$NON-NLS-1$
    	searchStringField.setCaretPosition(0);
    	searchStringField.addKeyListener(new KeyAdapter() {
    		public void keyPressed(KeyEvent e) {
			    			
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					executeSearchMultipleMovies();
				}
			}
    	});
    	
    	searchStringPanel.add(searchStringField, BorderLayout.NORTH);
    	
    	return searchStringPanel;
    }
    
    /**
     * Creates the button for searching IMDb
     * @return
     */
    JButton createButtonSearch() {
    	/*This button is used to search for on IMDB and for movies in the Database
        Where to search is decided in the executeSearchMultipleMovies method
    	 */
    	JButton buttonSearch = new JButton(Localizer.get("DialogIMDbMultiAdd.button.search.text")); //$NON-NLS-1$
    	buttonSearch.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.search.tooltip")); //$NON-NLS-1$
    	buttonSearch.setActionCommand("GetIMDBInfo - Search again"); //$NON-NLS-1$


    	buttonSearch.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
    			executeSearchMultipleMovies();
    		}});

    	return buttonSearch;
    }
    
    
    JButton createChooseBetweenImdbAndLocalDatabaseButton() {
    	
    	/*This button choses between IMDB and local movie database*/
		final JButton chooseBetweenImdbAndLocalDatabase  = new JButton(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.text")); //$NON-NLS-1$
		chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.tooltip")); //$NON-NLS-1$
		chooseBetweenImdbAndLocalDatabase.setActionCommand("GetIMDBInfo - chooseBetweenImdbAndLocalDatabase"); //$NON-NLS-1$
		chooseBetweenImdbAndLocalDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

				if (addInfoToExistingMovie) {
					panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setText(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.text")); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.tooltip")); //$NON-NLS-1$
					addInfoToExistingMovie = false;
					executeSearchMultipleMovies();
				}

				else {
					executeEditExistingMovie(""); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setText(Localizer.get("DialogIMDbMultiAdd.button.search-on-IMDb.text")); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.search-on-IMDb.tooltip")); //$NON-NLS-1$
					addInfoToExistingMovie = true;

					panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-your-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
				}
			}});
		
		return chooseBetweenImdbAndLocalDatabase;
    }
    
    
    JButton createAddWithoutIMDBInfoButton() {

    	JButton addWithoutIMDBInfo = new JButton(Localizer.get("DialogIMDbMultiAdd.button.add-without-web-info.text")); //$NON-NLS-1$
    	addWithoutIMDBInfo.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-without-web-info.tooltip")); //$NON-NLS-1$
    	addWithoutIMDBInfo.setActionCommand("GetIMDBInfo - addWithoutIMDBInfo"); //$NON-NLS-1$

    	addWithoutIMDBInfo.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {

    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    			if (multiAddByFile) {
    				String fileName = getFilename();

    				if (fileName.lastIndexOf('.') != -1)
    					modelEntry.setTitle(fileName.substring(0, fileName.lastIndexOf('.')));
    				else
    					modelEntry.setTitle(fileName);
    			}
    			else {
    				if (searchStringField.getText().trim().equals("")) { //$NON-NLS-1$
    					DialogAlert alert = new DialogAlert(thisDialog, Localizer.get("DialogIMDbMultiAdd.alert.no-title-specified.title"), "<html>" + Localizer.get("DialogIMDbMultiAdd.alert.no-title-specified.message") + "</html>", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    					GUIUtil.show(alert, true);
    					return;
    				}

    				modelEntry.setTitle(searchStringField.getText());
    			}

    			dropImdbInfoSet = true;
    			dispose();
    			return;
    		}});

    	return addWithoutIMDBInfo;
    }
    
    JButton createPlayButton() {
    	
    	JButton playMediaFiles = new JButton("Play");
    	playMediaFiles.setActionCommand("DialogIMDbMultiAdd - Play"); //$NON-NLS-1$

    	playMediaFiles.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
    			
    			if (multiAddFile != null) {
    				
    				String [] files = multiAddFile.toStringArray();
    				
    				try {
    					MovieManagerCommandPlay.executePlay(files);
    				} catch (IOException e) {
    					log.warn("Exception:" + e.getMessage(), e);
    				} catch (InterruptedException e) {
    					log.warn("Exception:" + e.getMessage(), e);
    				}
    			}
    		}
    	});
    	
    	return playMediaFiles;
    }
    
    
    JButton createAbortButton() {
    	JButton abortButton = new JButton(Localizer.get("DialogIMDbMultiAdd.button.abort.text")); //$NON-NLS-1$
    	abortButton.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.abort.tooltip")); //$NON-NLS-1$
    	abortButton.setActionCommand("GetIMDBInfo - abort"); //$NON-NLS-1$

    	abortButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    			/*Sets the cancelAll variable in multipleMovies object to true
                     This variable is checked in the multipleMovies object before saving,
                     and the appropriate action is performed.
    			 */
    			aborted = true;
    			dispose();
    		}});

    	return abortButton;
    }
    
    JPanel createFileLocationPanel() {
    	// Panel file location
    	JPanel fileLocationPanel = new JPanel();
    	fileLocationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Movie parts "), BorderFactory.createEmptyBorder(0,3,3,3)));
    	fileLocationPanel.setLayout(new BorderLayout());
    	fileLocation = new JTextArea();
    	fileLocation.setEditable(false);
    	
    	JScrollPane fileLocaScroll = new JScrollPane(fileLocation);
    	
    	fileLocationPanel.add(fileLocaScroll, BorderLayout.CENTER);
    	fileLocation.addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent e) {
    			//handleFileLocationPopup(e);
    		}
    	});
    	
    	return fileLocationPanel;
    }
    
    /**
     * Not yet implemented
     * @param e
     */
    void handleFileLocationPopup(MouseEvent e) {
    	
    	if (!GUIUtil.isRightMouseButton(e))
    		return;
    	
    	JPopupMenu fileLocationPopup = new JPopupMenu();
    	JMenuItem fileLocationItem = new JMenuItem("Open content folder");
    	fileLocationPopup.add(fileLocationItem);
    	
    	fileLocationPopup.show(fileLocation, e.getX(), e.getY());
    }
    
    
    
    
    void performSearch(String searchString, String year, ArrayList<ModelIMDbSearchHit> hits) {
		    	
    	try {
    		int hitCount = -1;
    		
    		int pos = -1;
    		DefaultListModel list = new DefaultListModel();

    		if (multiAddSelectOption == ImdbImportOption.selectFirst && imdbId != null) {
    			ModelIMDbSearchHit hit = new ModelIMDbSearchHit(imdbId, searchString, null, "Titles (Approx Matches)"); //$NON-NLS-1$

    			list.addElement(hit);
    			hitCount = 1;
    		}
    		else {
    			// Only pull list from imdb if not "Select FirstHit" is selected and no IMDB Id was found in an nfo/txt file
    			
    			if (hits == null)
    				hits = new IMDB(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(searchString);

    			/*Number of movie hits*/
    			hitCount = hits.size();
    			
    			if (hitCount == 0)
    				list.addElement(new ModelIMDbSearchHit(Localizer.get("DialogIMDB.list-element.messsage.no-hits-found"))); //$NON-NLS-1$
    			else {
    				for (int i = 0; i < hitCount; i++) {
    					ModelIMDbSearchHit hit = hits.get(i);
    					list.addElement(hit);
    					
    					if (pos == -1 && year != null && year.equals(hit.getDate())) {
    						pos = i;
    					}
    					
    					if (imdbId != null && hit.getUrlID().equals(imdbId))
    						// If current movie equals the one in the nfo than preselect this in the list
    						pos = i;
    				}
    			}
    		}
    		listMovies.setModel(list);
    		
    		if (pos == -1)
    			pos = 0;
    		    		
    		listMovies.setSelectedIndex(pos);
    		getButtonChoose().setEnabled(true);
    		
    		if (executeCommandMultipleMoviesSelectCheck(hitCount) == ShowListOption.show) {
    			GUIUtil.showAndWait(this, true);
    		}
    		
    	    // Insert prefix in Title to show that these movies maybe got wrong imdb infos
    		if (MovieManager.getConfig().getMultiAddPrefixMovieTitle() && hitCount > 1 && 
    				multiAddSelectOption == ImdbImportOption.selectFirst && (imdbId == null))
    			modelEntry.setTitle("_verify_ " + modelEntry.getTitle()); //$NON-NLS-1$
    		
    	} catch (Exception e) {
    		executeErrorMessage(e);
    		listMovies.setModel(null);
    	}
    }


    /**
     * The MovieManagerCommandFilter gets the movielist from the database ordered by movie title
     * Then uses the searchstring to remove unwanted hits
     * The last boolean argument states if the filter is called from the main search or the IMDB search.
     * If called from the main search, it will take in consideration all the advanced search options.
     */

    void executeEditExistingMovie(String searchString) {

    	DefaultListModel listModel;

    	ArrayList<String> lists = new ArrayList<String>();
    	
    	if (addToThisList != null)
    		lists.add(addToThisList);
    	    	
    	ArrayList<ModelMovie> list = MovieManager.getIt().getDatabase().getMoviesList("Title", lists,  //$NON-NLS-1$
    			MovieManager.getConfig().getShowUnlistedEntries());
    	listModel = GUIUtil.toDefaultListModel(list);
    			
    	listMovies.setModel(listModel);
    }

    /**
     * Checks if the movie list should be retrived from IMDB or the local movie Database
     */
    void executeSearchMultipleMovies() {
	
    	if (addInfoToExistingMovie)
    		executeEditExistingMovie(searchStringField.getText());

    	else {
    		final DefaultListModel listModel = new DefaultListModel();
	
    		int setSelectedIndex = 0;
    		
    		try {
    			    			
    			IMDB imdb = new IMDB(MovieManager.getConfig().getHttpSettings());
    			ArrayList<ModelIMDbSearchHit> hits = imdb.getSimpleMatches(searchStringField.getText());
    			        		
    			// Error
    			if (hits == null) {
    				HTTPResult res = imdb.getLastHTTPResult();
    				
    				if (res.getStatusCode() == HttpStatus.SC_REQUEST_TIMEOUT) {
    					listModel.addElement(new ModelIMDbSearchHit("Connection timed out...")); //$NON-NLS-1$
    				}
    			}
    			
    			for (int i = 0; i < hits.size(); i++) {
    				listModel.addElement(hits.get(i));
    				
    				//if (hits.get(i).getDate())
    			}

    		} catch (Exception e) {
    			executeErrorMessage(e);

    			e.printStackTrace();
    			dispose();
    		}
    		    		
    		if (listModel.getSize() == 0)
    			listModel.addElement(new ModelIMDbSearchHit(Localizer.get("DialogIMDB.list-element.messsage.no-hits-found"))); //$NON-NLS-1$
    		    		
    		    		
    		getMoviesList().setModel(listModel);
    		getMoviesList().setSelectedIndex(setSelectedIndex);
    		    		
    		// This delays the execution of requestFocusInWindow.
    		// The reason is to avoid that the actionlistener for the choose button 
    		// is invoked, which is would be if invokelater isn't used. (Experienced on Ubuntu).
    		SwingUtilities.invokeLater(new Runnable() {
    			public void run() {
    				getMoviesList().requestFocusInWindow();
    			}
    		});
    	}
    }

    
    private ShowListOption executeCommandMultipleMoviesSelectCheck(int listSize) {
        
        /* checks the property settings entered in the multi add movie preferences*/
    	
        if (multiAddSelectOption != ImdbImportOption.off) {
       	
        	// Always show list
        	if (multiAddSelectOption == ImdbImportOption.displayList)
        		return ShowListOption.show;
        	
        	// No hits on IMDb
        	if (listSize == 0) {
        		
        		// Show search dialog 
        		if (multiAddSelectOption == ImdbImportOption.selectFirst ||
        				multiAddSelectOption == ImdbImportOption.selectIfOnlyOneHit) {
        			 return ShowListOption.show;
        		}
        		
        		// Only options left are 'selectFirstOrAddToSkippedList' and 'selectIfOnlyOneHitOrAddToSkippedList'
        		// Add to skipped-list
    			dropImdbInfoSet = true;
    			return ShowListOption.no_show;
    		}
        	// If only one hit
        	else if (listSize == 1 && 
        			(multiAddSelectOption == ImdbImportOption.selectIfOnlyOneHit || 
        					multiAddSelectOption == ImdbImportOption.selectIfOnlyOneHitOrAddToSkippedList)) {
        		executeCommandSelect();
        		return ShowListOption.no_show;
        	}
        	// more than 1 hits
        	else if (multiAddSelectOption == ImdbImportOption.selectFirst || 
        			multiAddSelectOption == ImdbImportOption.selectFirstOrAddToSkippedList) {
        		executeCommandSelect();
        		return ShowListOption.no_show;
        	}            
        }
        return ShowListOption.show;
    }
    
   
    
    /**
     * Gets info...
     **/
    private void executeCommandSelect() {
    	    	
    	int index = getMoviesList().getSelectedIndex();
    	
    	/*
    	 * When adding the file info the an existing movie, a new ModelMovieInfo object is created. 
    	 * When done, the old ModelMovieInfo object created in the 
    	 * MovieManagerCommandAddMultipleMovies object needs not to save the file as a new movie,
    	 * therefore setCAncel method with true is called at the end of the if scoop.
    	 */	

    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	if (addInfoToExistingMovie) {

    		ModelMovie model = ((ModelMovie) listModel.getElementAt(index));

    		if (model.getKey() == -1)
    			return;

    		if (!model.getHasAdditionalInfoData()) {
    			model.updateAdditionalInfoData();
    		}

    		ModelMovieInfo modelInfoTmp = new ModelMovieInfo(model, false);

    		/* Need to set the hasReadProperties variable because when normally 
             calling the getfileinfo the first time it replaces the old additional values with the new ones
             Then the second time it plusses the time and size to match.
             When multiadding the next file info should be directly added to the old, not replace it
    		 */
    		
    		modelInfoTmp._hasReadProperties = true;
    		try {
				modelInfoTmp.getFileInfo(new File[] {multiAddFile.getFile()});
			} catch (Exception e) {
				log.error("Error occured while retrieving file info.", e); //$NON-NLS-1$
			}

    		try {
    			modelInfoTmp.saveToDatabase(null);
    		} catch (Exception e) {
    			log.error("Saving to database failed.", e); //$NON-NLS-1$
    		}

    		setCanceled(true);
    		dispose();
    	}
    	else {
    		ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));
	    		
    		if (model.getUrlID() == null)
    			return;

    		if (getUrlKeyOnly) {
    			modelEntry.setUrlKey(model.getUrlID());
    			dispose();
    			return;
    		}

			time = System.currentTimeMillis();

			if (multiAddSelectOption == ImdbImportOption.selectFirst && imdbId != null && !imdbId.equals("")) //$NON-NLS-1$
				// Use previously fetched imdb id
				getIMDbInfo(modelEntry, imdbId);
			else
				getIMDbInfo(modelEntry, model.getUrlID());

			ModelMovieInfo.executeTitleModification(modelEntry);
			
			dispose();
    	}
    }

    /**
     * Takes all the media files in a Files object and generated a string 
     * listing all the files categorized by parent directory.
     */
    void setFileLocationContent() {
        
      	if (multiAddFile != null && multiAddFile.getFile() != null) {
      		
      		int height = 0;
      		
      		ArrayList<Files> files = multiAddFile.getFiles();
      		String str = "";
      		
      		while (files.size() > 0) {
      			      			
      			String parent = files.get(0).getParent();
      			
      			if (str.length() > 0)
      				str += SysUtil.getLineSeparator();
      				
      			// Show directory of the file(s)
      			str += parent + SysUtil.getDirSeparator();
      			height++;
      			
      			int fileNumber = 1;
      			
      			for (int i = 0; i < files.size(); i++) {
          			      				
      				// Find all files in the current parent
      				if (files.get(i).getParent().equals(parent)) {
      					height++;
      					str += SysUtil.getLineSeparator() + String.format("  %-3d - %s", fileNumber++, files.get(i).getName());
      					files.remove(i);
      					i--;
      				}
      			}
      			
      		}
      		      		
      		fileLocation.setText(str);
      		fileLocation.setRows(height);
      	}
    }
    
    void setHotkeyModifiersMultiAdd() {
    	    	
    	// ALT+P for Play
    	if (playMediaFiles != null) {
    		shortcutManager.registerKeyboardShortcut(
    				KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyboardShortcutManager.getToolbarShortcutMask()),
    				"Play file", new AbstractAction() {
    					public void actionPerformed(ActionEvent ae) {
    						playMediaFiles.doClick();
    					}
    				}, playMediaFiles);
    	}

		// ALT+A for abort
		shortcutManager.registerKeyboardShortcut(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyboardShortcutManager.getToolbarShortcutMask()),
				"Abort import", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				abortButton.doClick();
			}
		}, abortButton);
		
		System.err.println("addWithoutIMDBInfoButton:" + addWithoutIMDBInfoButton);
		
		if (addWithoutIMDBInfoButton != null) {
			// ALT+W for add without IMDb info
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Add without IMDb info", new AbstractAction() {
						public void actionPerformed(ActionEvent ae) {
							addWithoutIMDBInfoButton.doClick();
						}
					}, addWithoutIMDBInfoButton);
		}
		
		// ALT+L for list focus
		shortcutManager.registerKeyboardShortcut(
				KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyboardShortcutManager.getToolbarShortcutMask()),
				"List Focus", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				listMovies.requestFocusInWindow();
			}
		});
		
		// ALT+S for search field focus
		shortcutManager.registerKeyboardShortcut(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyboardShortcutManager.getToolbarShortcutMask()),
				"Give search field focus or perform search", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
								
				if (!searchStringField.hasFocus()) {
					searchStringField.requestFocusInWindow();
				}
				else {
					buttonSearch.doClick();
				}
			}
		});
    }
}
