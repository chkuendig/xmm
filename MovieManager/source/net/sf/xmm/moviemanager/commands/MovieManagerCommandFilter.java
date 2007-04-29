/**
 * @(#)MovieManagerCommandFilter.java 1.0 29.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelMovie;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTree;

public class MovieManagerCommandFilter implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    private static long filterStart;
    private static String filterString;
    private static boolean mainFilter; 
    private static javax.swing.JComponent movieList;
    private static boolean addEmptyEntry = false;
    private static String databaseEmptyMessage = "Empty Database";
    private static String filterCategory;
    
    public MovieManagerCommandFilter(String _filterString, javax.swing.JComponent _movieList, boolean _mainFilter, boolean _addEmptyEntry) {
	filterString = _filterString;
	movieList = _movieList;
	mainFilter = _mainFilter;
	addEmptyEntry = _addEmptyEntry;
    }
    
    MovieManagerCommandFilter(String _filterString, boolean _mainFilter, boolean _addEmptyEntry, String _filterCategory) {
	filterString = _filterString;
	filterCategory = _filterCategory;
	mainFilter = _mainFilter;
	addEmptyEntry = _addEmptyEntry;
    }
    
    
    /* Not used any more as the search functions are done solely by the database */
    
    /*
    protected static void applyFilter(DefaultListModel listModel) {
	
	int filterNr = 0;
	
	int seen = MovieManager.getConfig().getFilterSeen();
	int ratingOption = MovieManager.getConfig().getRatingOption();
	double ratingValue = MovieManager.getConfig().getRatingValue();
	int dateOption = MovieManager.getConfig().getDateOption();
	int dateValue;
	
	try {
	    dateValue = Integer.parseInt(MovieManager.getConfig().getDateValue());
	}
	
	catch (NumberFormatException s){
	    dateValue = -1;
	}
	
	// If the filter is not empty removes the unwanted items... 
	
	if (filterString.length() > 0) {
	    
	    filterString = filterString.toLowerCase();
	    
	    if (filterCategory.equals("Movie Title"))
		filterNr = 1;
	    
	    else if (filterCategory.equals("Directed By"))
		filterNr = 2;
	    
	    else if (filterCategory.equals("Written By"))
		filterNr = 3;
	    
	    else if (filterCategory.equals("Genre"))
		filterNr = 4;
	    
	    else if (filterCategory.equals("Cast"))
		filterNr = 5;
	    
	    
	    // Actual removal... 
	    switch(filterNr) {
		
		//Movie Title
	    case 1 : {
		String [] result = filterString.split(" ");
		for (int i=0; i<listModel.getSize(); i++) {
		    for (int u=0; u<result.length; u++) {
			Pattern movieTitleFilter = Pattern.compile("(.*"+result[u].toLowerCase()+".*)+");
			if (!movieTitleFilter.matcher(listModel.get(i).toString().toLowerCase()).matches()) {
			    listModel.remove(i);
			    i--;
			    break;
			}
		    }
		}
	    }
		break;
	    
		//Directed by
	    case 2 : {
		String [] result = filterString.split(" ");
		for (int i=0; i<listModel.getSize(); i++) {
		    for (int u=0; u<result.length; u++) {
			Pattern directedByFilter = Pattern.compile("(.*"+result[u].toLowerCase()+".*)+");
			
			if (!directedByFilter.matcher((((ModelMovie)listModel.elementAt(i)).getDirectedBy()).toLowerCase()).matches()) {
			    listModel.remove(i);
			    i--;
			    break;
			}
		    }
		}
	    }
		break;
	    
		//Written by
	    case 3 : {
		String [] result = filterString.split(" ");
		for (int i=0; i<listModel.getSize(); i++) {
		    for (int u=0; u<result.length; u++) {
			Pattern writtenByFilter = Pattern.compile("(.*"+result[u].toLowerCase()+".*)+");
			if (!writtenByFilter.matcher((((ModelMovie)listModel.elementAt(i)).getWrittenBy()).toLowerCase()).matches()) {
			    listModel.remove(i);
			    i--;
			    break;
			}
		    }
		}
	    }
		break;
	    
		//Genre
	    case 4 : {
		String [] result = filterString.split(" ");
		
		for (int i=0; i<listModel.getSize(); i++) {
		    for (int u=0; u<result.length; u++) {
			Pattern genreFilter = Pattern.compile("(.*"+result[u].toLowerCase()+".*)+");
			
			if (!genreFilter.matcher((((ModelMovie)listModel.elementAt(i)).getGenre()).toLowerCase()).matches()) {
			    listModel.remove(i);
			    i--;
			    break;
			}
		    }
		}
	    }
		break;
	    
		//Cast
	    case 5 : {
		String [] result = filterString.split(" ");
		
		for (int i=0; i<listModel.getSize(); i++) {
		    for (int u=0; u<result.length; u++) {
			Pattern castFilter = Pattern.compile("(.*"+result[u].toLowerCase()+".*)+");
			
			if (!castFilter.matcher((((ModelMovie)listModel.elementAt(i)).getCast()).toLowerCase().replace(',', ' ')).matches()) {
			    listModel.remove(i);
			    i--;
			    break;
			}
		    }
		}
	    }
		break;
	    }
	}
	
	
	// removes all movies according to seen, rating or dating options
	
	if (mainFilter) {
	    
	    if (ratingOption == 2 || ratingOption == 3 || dateOption == 2 || dateOption == 3 || seen == 2 || seen == 3) {
		for (int i=0; i<listModel.getSize(); i++) {
		    
		    String movieRating = ((ModelMovie)listModel.elementAt(i)).getRating();
		    int movieDate;
		    
		    //Some movies does not have a date
		    try {
			movieDate = Integer.parseInt(((ModelMovie)listModel.elementAt(i)).getDate());
		    }
		    
		    catch (NumberFormatException e){
			movieDate = -1;
		    }
		
		    //If seen is either 2 or 3 means the seen option is enabled, 
		     // and it checks if the movie should be removed from the listmodel
		    if ((seen == 2 && !((ModelMovie)listModel.elementAt(i)).getSeen()) || (seen == 3 && ((ModelMovie)listModel.elementAt(i)).getSeen())) {
			listModel.remove(i);
			i--;
		    }
		    
		    //If the movie wasn't removed by the seen check, it checks if the ratingOption is enabled (2 or 3),
		     // and removes the movie if both tests are positive
		    
		    else if ((ratingOption == 2) && (!movieRating.equals("")) && (((Double.parseDouble(movieRating))) < ratingValue)) {
			listModel.remove(i);
			i--;
		    }
		    
		    else if ((ratingOption == 3) &&  (!movieRating.equals("")) && (((Double.parseDouble(movieRating))) > ratingValue)) {
			listModel.remove(i);
			i--;
		    }
		    
		    else if ((dateOption == 2) && (movieDate != -1) && (movieDate < dateValue)) {
			listModel.remove(i);
			i--;
		    }
		    
		    else if ((dateOption == 3) && (movieDate != -1) && (movieDate > dateValue)) {
			listModel.remove(i);
			i--;
		    }
		}
	    }
	}
    }
*/    

    /**
     * Executes the command.
     **/
    public void execute() {
	
    	// If any notes have been changed, they will be saved before seaching
    	MovieManagerCommandSaveChangedNotes.execute();
    	
	DefaultListModel listModel;
	Database database = MovieManager.getIt().getDatabase();
	
	if (database == null)
	    return;
	
	filterStart = System.currentTimeMillis();
	databaseEmptyMessage = "Empty Database";
	
	System.out.println("mainFilter:" + mainFilter);
	
	if (mainFilter) {
		
	    ModelDatabaseSearch options = MovieManager.getIt().getFilterOptions();
		
	    listModel = database.getMoviesList(options);
	    
	    filterCategory = MovieManager.getConfig().getFilterCategory();
	    filterString = MovieManager.getDialog().getFilter().getText();
	    movieList = MovieManager.getDialog().getMoviesList();
	}
	else {
		ModelDatabaseSearch options = MovieManager.getIt().getFilterOptions();
		
		if (options.getListName() != null && !"".equals(options.getListName()))
			listModel = database.getMoviesList("Title", options.getListName());
		else
			listModel = database.getMoviesList("Title");
		
	    filterCategory = "Movie Title";
	}
	
	if (listModel.isEmpty()) {
	    if (database.getDatabaseSize() > 0) {
		databaseEmptyMessage = "No matches found";
		
		//if (MovieManager.getIt().getDatabase().getErrorMessage().indexOf("Syntax error") != -1) {
		if (! database.getErrorMessage().equals("")) {
		    databaseEmptyMessage = database.getErrorMessage();
		    database.resetError();
		}
	    }
	}

	//applyFilter(listModel); 
	
	if (listModel.size() == 0 && addEmptyEntry) {
	    
	    listModel.addElement(new ModelMovie(-1, "", "", "", databaseEmptyMessage, "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", "", ""));
		
	    if (mainFilter) 
	    	MovieManager.getDialog().setAndShowEntries(0);
	}
	
	else if (mainFilter) {
	    /*Uppdates the entries*/
	    MovieManager.getDialog().setAndShowEntries(listModel.size());
	}
	
	/* Replaces the old model... */
	if (mainFilter) {
	    ((JTree) movieList).setModel(MovieManager.getDialog().createTreeModel(listModel, database.getEpisodeList("movieID")));
	    ((JTree) movieList).setSelectionInterval(0, 0);
	    
	    MovieManagerCommandSelect.execute();
	}	
	else {
	    ((JList) movieList).setModel(listModel);
	    ((JList) movieList).setSelectedIndex(0);
	}
	
	log.info("It took:" + (System.currentTimeMillis() - filterStart)+" ms to process the filter.");
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	
	log.debug("ActionPerformed: " + event.getActionCommand());
	
	/* The same object is used by the main filter every time, therefore these variables needs to be set back to default */
	if (event.getSource().equals(MovieManager.getDialog().getFilter())) {
	    mainFilter = true;
	    addEmptyEntry = true;
	}
	execute();
    }
}
