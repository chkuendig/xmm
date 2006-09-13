/**
 * @(#)DialogIMDB.java 1.0 28.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMovies;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandFilter;
import net.sf.xmm.moviemanager.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelIMDB;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

class DialogIMDB extends JDialog {
   
    static Logger log = Logger.getRootLogger();
    
    DialogMovieInfo _parent;
    JList listMovies;
    JTextField searchStringField;
    boolean multiAdd = false;
    boolean addInfoToExistingMovie = false;
    int multiAddSelectOption = 0;
    File [] multiAddFile;
    JPanel panelMoviesList;
    
    long time;
    
    public boolean isMultiAdd() {
	return multiAdd;
    }
    
    /*Used only to tell the MovieManagerCommandAddMultipleMovies object
      to abort the multiple add movies method*/
    MovieManagerCommandAddMultipleMovies commandAddMovies = null;
    
    
    /**
     * The Constructor.
     **/
    protected DialogIMDB(DialogMovieInfo parent) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	multiAddFile = new File[1];
	
	/* Sets parent... */
	_parent = parent;
	
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
		public void actionPerformed(ActionEvent e)
		{
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	createListDialog("searchString", "filename");
    }
    
    
    /**
     * The Constructor.
     **/
    protected DialogIMDB(DialogMovieInfo parent, String searchString, String filename, final MovieManagerCommandAddMultipleMovies commandAddMovies, int multiAddSelectOption) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	multiAddFile = new File[1];
	    
	/* Sets parent... */
	_parent = parent;
	this.multiAdd = true;
	this.commandAddMovies = commandAddMovies;
	this.multiAddSelectOption = multiAddSelectOption;
	
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
	
	time = System.currentTimeMillis();
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    commandAddMovies.setCancel(true);
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	createListDialog(searchString, filename);
    }

    
    void createListDialog(final String searchString, String filename) {
	/* Dialog properties...*/
	setTitle("Select Movie Title");
	setModal(true);
	setResizable(false);
	/* Movies List panel...*/
	panelMoviesList = new JPanel();
	panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," IMDB Movies List "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	listMovies = new JList() {
		
		public String getToolTipText(MouseEvent e) {
		    
		    if (getCellBounds(0,0) == null)
			return null;
			
		    String retVal = null;
		    int row = (int) e.getPoint().getY() / (int) getCellBounds(0,0).getHeight();
		    
		    if (row < getModel().getSize())
			retVal = ((ModelIMDB) getMoviesList().getModel().getElementAt(row)).getAka();
		    
		    return retVal;
		}

		public JToolTip createToolTip() {
		    
		    JMultiLineToolTip tooltip = new JMultiLineToolTip();
		    tooltip.setComponent(this);
		    return tooltip;
		}
	    };
	
	listMovies.setFont(new Font(listMovies.getFont().getName(),Font.PLAIN,listMovies.getFont().getSize()));
	listMovies.setLayoutOrientation(JList.VERTICAL);
	listMovies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
	
	
	JScrollPane scrollPaneMovies = new JScrollPane(listMovies);
	scrollPaneMovies.setPreferredSize(new Dimension(300,255));
	panelMoviesList.add(scrollPaneMovies);
	
	/* To add outside border... */
	JPanel all = new JPanel();
	
	all.setLayout(new BorderLayout());
	all.add(panelMoviesList, BorderLayout.NORTH);
	all.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,0,8), null));
	
	/*If running the multiadd feature, here are all the extra buttons added. */
	if (isMultiAdd()) {
	    
	    setTitle(filename);
	    JPanel searchStringPanel = new JPanel();
	    searchStringPanel.setLayout(new BorderLayout());
	    searchStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Search string "), BorderFactory.createEmptyBorder(4,4,4,4)));
	    
	    searchStringField = new JTextField(27);
	    searchStringField.setActionCommand("Search String:");
	    searchStringField.setText(searchString);
	    searchStringField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
			log.debug("ActionPerformed: "+event.getActionCommand());
			executeSearchMultipleMovies();
			
		    }
		});
	   
	    searchStringPanel.add(searchStringField, BorderLayout.NORTH);
	    all.add(searchStringPanel, BorderLayout.CENTER);
	    
	    JPanel multipleMovieButtons = new JPanel();
	    multipleMovieButtons.setLayout(new FlowLayout());
	    
	    
	    /*This button is used to search for on IMDB and for movies in the Database
	      Where to search is desided in the executeSearchMultipleMovies method
	    */
	    JButton buttonSearch = new JButton("Search");
	    buttonSearch.setToolTipText("Do a search with the search string");
	    buttonSearch.setActionCommand("GetIMDBInfo - Search again");
	    
	    
	    buttonSearch.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
			log.debug("ActionPerformed: "+event.getActionCommand());
			executeSearchMultipleMovies();
		    }});
	    multipleMovieButtons.add(buttonSearch);
	    
	    if (commandAddMovies instanceof MovieManagerCommandAddMultipleMoviesByFile) {
		
		/*This button choses between IMDB and local movie database*/
		final JButton chooseBetweenImdbAndLocalDatabase  = new JButton("Add to existing movie");
		chooseBetweenImdbAndLocalDatabase.setToolTipText("Add file info to an existing movie instead of adding a new movie to the movie list");
		chooseBetweenImdbAndLocalDatabase.setPreferredSize(new Dimension(155, 26));
		chooseBetweenImdbAndLocalDatabase.setActionCommand("GetIMDBInfo - chooseBetweenImdbAndLocalDatabase");
		chooseBetweenImdbAndLocalDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
			    log.debug("ActionPerformed: "+event.getActionCommand());
			
			    if (addInfoToExistingMovie) {
				panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," IMDB Movies List "), BorderFactory.createEmptyBorder(5,5,5,5)));
				chooseBetweenImdbAndLocalDatabase.setText("Add to existing movie");
				chooseBetweenImdbAndLocalDatabase.setToolTipText("Add file info to an existing movie instead of adding a new movie to the movie list");
				addInfoToExistingMovie = false;
				executeSearchMultipleMovies();
			    }
			
			    else {
				executeEditExistingMovie("");
				chooseBetweenImdbAndLocalDatabase.setText("Search on IMDB");
				chooseBetweenImdbAndLocalDatabase.setToolTipText("Search on IMDB");
				addInfoToExistingMovie = true;
			    
				panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Your Movies List "), BorderFactory.createEmptyBorder(5,5,5,5)));
			    
			    }
			
			    multiAddFile[0] = _parent.getMultiAddFile();
			
			}});
		
		multipleMovieButtons.add(chooseBetweenImdbAndLocalDatabase);
	    }
	    
	    JButton addWithoutIMDBInfo = new JButton("Add without IMDB info");
	    addWithoutIMDBInfo.setToolTipText("Add movie to database without retrieving movie info from IMDB");
	    addWithoutIMDBInfo.setActionCommand("GetIMDBInfo - addWithoutIMDBInfo");
	    
	    addWithoutIMDBInfo.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
			log.debug("ActionPerformed: "+ event.getActionCommand());
			
			_parent.getMovieTitle().setText(searchStringField.getText());
			_parent.getMovieTitle().setCaretPosition(0);
			commandAddMovies.setDropImdbInfo(true);
			dispose();
			return;
		    }});
	    multipleMovieButtons.add(addWithoutIMDBInfo);
	    
	    JButton cancelAll = new JButton("Abort");
	    cancelAll.setToolTipText("Abort will cancel every movie left on the add-list");
	    cancelAll.setActionCommand("GetIMDBInfo - cancelAll");
	    
	    cancelAll.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
			log.debug("ActionPerformed: "+ event.getActionCommand());
			
			/*Sets the cancelAll variable in multipleMovies object to true
			  This variable is checked in the multipleMovies object before saving,
			  and the appropriate action is performed.
			*/
			commandAddMovies.setCancelAll(true);
			dispose();
			return;
		    }});
	    multipleMovieButtons.add(cancelAll);
	    all.add(multipleMovieButtons, BorderLayout.SOUTH);
	}
	
	/* regular Buttons panel...*/
	JPanel panelRegularButtons = new JPanel();
	panelRegularButtons.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
	panelRegularButtons.setLayout(new FlowLayout());
	
	JButton buttonMore = new JButton("More Titles");
	buttonMore.setToolTipText("Get more related titles from IMDB");
	buttonMore.setActionCommand("GetIMDBInfo - More");
	buttonMore.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+ event.getActionCommand());
		    
		    getButtonMore().setEnabled(false);
		    getButtonSelect().setEnabled(false);
		    executeCommandMore();
		    getMoviesList().setSelectedIndex(0);
		}});
	
	panelRegularButtons.add(buttonMore);
	JButton buttonSelect = new JButton("Select");
	buttonSelect.setToolTipText("Get the info for the selected title");
	buttonSelect.setActionCommand("GetIMDBInfo - Select");
	buttonSelect.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+ event.getActionCommand());
		    
		    if (!getMoviesList().isSelectionEmpty())
			executeCommandSelect();
		}});
	
	panelRegularButtons.add(buttonSelect);
	
	if (!isMultiAdd()) {
	    buttonSelect.setEnabled(false);
	    buttonMore.setEnabled(false);
	}
	
	JButton buttonCancel;
	
	if (isMultiAdd()) {
	    buttonCancel = new JButton("Skip movie");
	    buttonCancel.setToolTipText("Skip this movie");
	}
	else {
	    buttonCancel = new JButton("Cancel");
	    buttonCancel.setToolTipText("Close the Get IMDB Info dialog");
	}
	
	buttonCancel.setActionCommand("GetIMDBInfo - Cancel");
	
	buttonCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+ event.getActionCommand());
		    
		    if (isMultiAdd())
			commandAddMovies.setCancel(true);
		    dispose();
		}});
	
	panelRegularButtons.add(buttonCancel);
	/* Adds all and buttonsPanel... */
	getContentPane().add(all,BorderLayout.NORTH);
	getContentPane().add(panelRegularButtons,BorderLayout.SOUTH);
	/* Packs and sets location... */
	pack();
	
	getMoviesList().ensureIndexIsVisible(0);
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	
	
	
	
	//listModel.addElement(new ModelIMDB(key, movieTitle, aka));
	
	DefaultListModel model = new DefaultListModel();
	model.addElement(new ModelIMDB(null, "Search in progress...", null));
	listMovies.setModel(model);
	
	
	if (isMultiAdd()) { 
	    try {
		DefaultListModel list = IMDB.getSimpleMatches(searchString);
		/*Number of movie hits*/
		int listSize = list.getSize();
						
		if (list.getSize() == 0) {
		    list.addElement(new ModelIMDB(null, "No hits found", null));
		}
						
		listMovies.setModel(list);
		listMovies.setSelectedIndex(0);
						
		if (executeCommandMultipleMoviesSelectCheck(listSize) == 1)
		    setVisible(true);
						
	    } catch (Exception e) {
		executeErrorMessage(e.getMessage());
		listMovies.setModel(null);
	    }
	} else {
	    SwingWorker worker = new SwingWorker() {
		    public Object construct() {
			try {
			    DefaultListModel list = IMDB.getSimpleMatches(_parent.getMovieTitle().getText());
			    
			    if (list.getSize() == 0) {
				list.addElement(new ModelIMDB(null, "No hits found", null));
			    }
						
			    listMovies.setModel(list);
			    listMovies.setSelectedIndex(0);
						
			    getButtonMore().setEnabled(true);
			    getButtonSelect().setEnabled(true);
			}
			catch (Exception e) {
			    executeErrorMessage(e.getMessage());
			    listMovies.setModel(null);
			}
			return "";
		    }
		};
	    worker.start();
	}
    }
    
    
    /*The MovieManagerCommandFilter gets the movielist from the database ordered by movie title
      Then uses the searchstring to remove unwanted hits
      The last boolean argument states if the filter is called from the main search or the IMDB search.
      If called from the main search, it will take in consideration all the advanced search options.
    */
    
    void executeEditExistingMovie(String searchString) {
	MovieManagerCommandFilter mmcf = new MovieManagerCommandFilter(searchString, getMoviesList(), false, true);
	mmcf.execute();
    }
    
    /*Checks if the movie list should be retrived from IMDB or the local movie Databse
     */
    void executeSearchMultipleMovies() {
		
	if (addInfoToExistingMovie)
	    executeEditExistingMovie(searchStringField.getText());
		
	else {
	    DefaultListModel listModel = null;
			
	    try {
				
		if (!getButtonMore().getText().equals("More Titles"))
		    listModel = IMDB.getExtendedMatches(searchStringField.getText());
		else
		    listModel = IMDB.getSimpleMatches(searchStringField.getText());
				
	    } catch (Exception e) {
				
		executeErrorMessage(e.getMessage());
		dispose();
	    }
			
	    getMoviesList().setModel(listModel);
	    getMoviesList().setSelectedIndex(0);
	}
    }
    
    /**
     * Returns the JList listMovies.
     **/
    protected JList getMoviesList() {
	return
	    (JList)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
    }
    
    /**
     * Returns the JButton More.
     **/
    protected JButton getButtonMore() {
	return
	    (JButton)
	    ((JPanel)
	     getContentPane().getComponent(1)).getComponent(0);
    }

    /**
     * Returns the JButton More.
     **/
    protected JButton getButtonSelect() {
	return
	    (JButton)
	    ((JPanel)
	     getContentPane().getComponent(1)).getComponent(1);
    }
    
    /**
     * Gets more or less info...
     **/
    void executeCommandMore() {
	
	DefaultListModel model = new DefaultListModel();
	model.addElement(new ModelMovie(-1, "Search in progress..."));
	listMovies.setModel(model);
	
	SwingWorker worker = new SwingWorker() {
		public Object construct() {
				
		    DefaultListModel listModel = null;
				
		    try {
					
			if (getButtonMore().getText().equals("More Titles")) {
					
			    if (isMultiAdd())
				listModel = IMDB.getExtendedMatches(searchStringField.getText());
			    else
				listModel = IMDB.getExtendedMatches(_parent.getMovieTitle().getText());
					
						
			    if (listModel.getSize() == 0) {
				listModel.addElement(new ModelIMDB(null, "No hits found", null));
			    }
						
			    getMoviesList().setModel(listModel);
			    getMoviesList().setSelectedIndex(0);
						
			    getButtonMore().setToolTipText("Show less related titles");
			    getButtonMore().setText("Less Titles");
			} else {
						
			    if (isMultiAdd())
				listModel = IMDB.getSimpleMatches(searchStringField.getText());
			    else
				listModel = IMDB.getSimpleMatches(_parent.getMovieTitle().getText());
						
			    if (listModel.getSize() == 0) {
				listModel.addElement(new ModelIMDB(null, "No hits found", null));
			    }
						
			    getMoviesList().setModel(listModel);
			    getMoviesList().setSelectedIndex(0);
						
			    getButtonMore().setToolTipText("Get more related titles from IMDB");
			    getButtonMore().setText("More Titles");
			}
			getButtonMore().setEnabled(true);
			getButtonSelect().setEnabled(true);
		    
		    } catch (Exception e) {
			executeErrorMessage(e.getMessage());
			dispose();
		    }
				
		    return "";
		}
	    };
	worker.start();
    }
    
    private int executeCommandMultipleMoviesSelectCheck(int listSize) {
	
	/* checks the property settings entered in the multi add movie preferences*/
	
	if (multiAddSelectOption != 0) {
	    
	    /*Select first hit no matter*/
	    if ((multiAddSelectOption == 1) && (listSize > 0)) {
		executeCommandSelect();
		return 0;
	    }
	    
	    /*Select first if only one hit*/
	    if (multiAddSelectOption == 2) {
		if (listSize == 1) {
		    getMoviesList().setSelectedIndex(0);
		    executeCommandSelect();
		    return 0;
		}
	    }
	}
	return 1;
    }
    
    /*ALerts the user of different error messages from proxy servers*/
    void executeErrorMessage(String exception) {
	
	if (exception == null)
	    return;
		
	if (exception.startsWith("Server returned HTTP response code: 407")) {
	    DialogAlert alert = new DialogAlert("Authentication required", "Proxy server requires authentication");
	    alert.setVisible(true);
	}
		
	if (exception.startsWith("Connection timed out")) {
	    DialogAlert alert = new DialogAlert("Connection timed out", "Server did not respond");
	    alert.setVisible(true);
	}
		
	if (exception.startsWith("Connection reset")) {
	    DialogAlert alert = new DialogAlert("Connection reset", "Connection reset by server");
	    alert.setVisible(true);
	}
		
	if (exception.startsWith("Server redirected too many  times")) {
	    DialogAlert alert = new DialogAlert("Access denied", "Username or password is invalid");
	    alert.setVisible(true);
	}
		
	if (exception.startsWith("The host did not accept the connection within timeout of")) {
	    DialogAlert alert = new DialogAlert("Connection timeout", exception);
	    alert.setVisible(true);
	}
    }
    
    /**
     * Gets more or less info...
     **/
    private void executeCommandSelect() {
	int index = getMoviesList().getSelectedIndex();
	
	/*When adding the file info the an existing movie, a new DialogMovieInfo object is created
	  with title "Edit Movie". When done the old DialogMovieInfo object created in the 
	  MovieManagerCommandAddMultipleMovies object needs not to save the file as a new movie,
	  therefore setCAncel method with true is called at the end of the if scoop.
	*/
	
	if (index == -1)
	    return;
	
	DefaultListModel listModel = (DefaultListModel)getMoviesList().getModel();
	
	if (addInfoToExistingMovie) {
		
	    ModelMovie model = ((ModelMovie) listModel.getElementAt(index));
		
	    if (model.getKey() == -1)
		return;
		
	    DialogMovieInfo dialogMovieInfo = new DialogMovieInfo("Edit Movie", model);
	    
	    /* Need to set the hasReadProperties variable because when normally 
	       calling the getfileinfo the first time it replaces the old additional values with the new ones
	       Then the second time it plusses the time and size to match.
	       When multiadding the next file info should be directly added to the old, not replace it
	    */
	    dialogMovieInfo.setHasReadProperties(true);
	    dialogMovieInfo.executeCommandGetFileInfo(multiAddFile);
	    dialogMovieInfo.executeAndReloadMovieList(dialogMovieInfo.executeCommandSave(null));
	    
	    commandAddMovies.setCancel(true);
	    dispose();
	}
	else {
		
	    ModelIMDB model = ((ModelIMDB) listModel.getElementAt(index));
		
	    if (model.getKey() == null)
		return;
		
	    if (index != -1 && index <= listModel.size()) {
		time = System.currentTimeMillis();
			
		IMDB imdb;
			
		try {
		    imdb = new IMDB(model.getKey());
		} catch (Exception e) {
		    log.error("Exception: "+ e);
		    return;
		}
		
		String title = imdb.getTitle();
		
		_parent.getMovieTitle().setText(title);
		_parent.getMovieTitle().setCaretPosition(0);
		
		_parent.getDate().setText(imdb.getDate());
		_parent.getDate().setCaretPosition(0);
		_parent.getColour().setText(imdb.getColour());
		_parent.getColour().setCaretPosition(0);
		_parent.getDirectedBy().setText(imdb.getDirectedBy());
		_parent.getDirectedBy().setCaretPosition(0);
		_parent.getWrittenBy().setText(imdb.getWrittenBy());
		_parent.getWrittenBy().setCaretPosition(0);
		_parent.getGenre().setText(imdb.getGenre());
		_parent.getGenre().setCaretPosition(0);
		_parent.getRating().setText(imdb.getRating());
		_parent.getRating().setCaretPosition(0);
		_parent.getCountry().setText(imdb.getCountry());
		_parent.getCountry().setCaretPosition(0);
		_parent.getLanguage().setText(imdb.getLanguage());
		_parent.getLanguage().setCaretPosition(0);
		_parent.getPlot().setText(imdb.getPlot());
		_parent.getPlot().setCaretPosition(0);
		_parent.getCast().setText(imdb.getCast());
		_parent.getCast().setCaretPosition(0);
		
		
		_parent.getWebRuntime().setText(imdb.getRuntime());
		_parent.getWebRuntime().setCaretPosition(0);
		
		_parent.getWebSoundMix().setText(imdb.getSoundMix());
		_parent.getWebSoundMix().setCaretPosition(0);

		_parent.getAwards().setText(imdb.getAwards());
		_parent.getAwards().setCaretPosition(0);
		
		_parent.getMpaa().setText(imdb.getMpaa());
		_parent.getMpaa().setCaretPosition(0);

		_parent.getAka().setText(imdb.getAka());
		_parent.getAka().setCaretPosition(0);
		
		_parent.getCertification().append(imdb.getCertification());
		_parent.getCertification().setCaretPosition(0);
		
		/* The cover... */
		byte[] coverData = imdb.getCover();
		
		if (imdb.getCoverOK()) {
		    _parent.setCover(imdb.getKey()+imdb.getCoverURL().substring(imdb.getCoverURL().lastIndexOf('.')), coverData);
		} else {
		    _parent.removeCover();
		}
		
		/* The imdb id... */
		_parent.setIMDB(imdb.getKey());
		dispose();
	    }
	}
    }
}
