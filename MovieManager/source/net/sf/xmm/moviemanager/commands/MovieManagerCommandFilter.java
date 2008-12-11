/**
 * @(#)MovieManagerCommandFilter.java 1.0 21.12.07 (dd.mm.yy)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.swing.extentions.events.ComboCheckBoxKeyEvent;

import org.apache.log4j.Logger;

public class MovieManagerCommandFilter implements ActionListener, net.sf.xmm.moviemanager.swing.extentions.events.ComboCheckBoxKeyEventListener {

	static Logger log = Logger.getRootLogger();

	private static long filterStart;
	private static boolean mainFilter; 
	private static javax.swing.JComponent movieList;
	private static boolean addEmptyEntry = false;
	private static String noHitsMessage = "Empty Database";
	
	public MovieManagerCommandFilter(String _filterString, javax.swing.JComponent _movieList, boolean _mainFilter, boolean _addEmptyEntry) {
		movieList = _movieList;
		mainFilter = _mainFilter;
		addEmptyEntry = _addEmptyEntry;
	}

	MovieManagerCommandFilter(String _filterString, boolean _mainFilter, boolean _addEmptyEntry, String _filterCategory) {
		mainFilter = _mainFilter;
		addEmptyEntry = _addEmptyEntry;
	}


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
		noHitsMessage = "Empty Database";
		addEmptyEntry = false;
		
		if (mainFilter) {
			ModelDatabaseSearch options = MovieManager.getIt().getFilterOptions();
			listModel = database.getMoviesList(options);
			movieList = MovieManager.getDialog().getMoviesList();
		}
		else {
			ModelDatabaseSearch options = MovieManager.getIt().getFilterOptions();

			if (options.getListName() != null && !"".equals(options.getListName()))
				listModel = database.getMoviesList("Title", options.getListName());
			else
				listModel = database.getMoviesList("Title");
		}

		if (listModel.isEmpty()) {
			addEmptyEntry = true;
			
			if (database.getDatabaseSize() > 0) {
				noHitsMessage = "No matches found";

				if (! database.getErrorMessage().equals("")) {
					noHitsMessage = database.getErrorMessage();
					database.resetError();
				}
			}
		}

		if (listModel.size() == 0 && addEmptyEntry) {

			listModel.addElement(new ModelMovie(-1, "", "", "", noHitsMessage, "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", "", ""));

			if (mainFilter) 
				MovieManager.getDialog().setAndShowEntries(0);
		}

		else if (mainFilter) {
			/*Uppdates the entries*/
			MovieManager.getDialog().setAndShowEntries(listModel.size());
		}

		/* Replaces the old model... */
		if (mainFilter) {
			ArrayList episodeListModel = database.getEpisodeList("movieID");
			DefaultTreeModel treeModel = MovieManager.getDialog().createTreeModel(listModel, episodeListModel);
			
			MovieManager.getDialog().setTreeModel(treeModel, listModel, episodeListModel);
			((ExtendedJTree) movieList).setSelectionInterval(0, 0);

			MovieManagerCommandSelect.execute();
			
		}	
		else {
			((JList) movieList).setModel(listModel);
			((JList) movieList).setSelectedIndex(0);
		}

		log.debug("It took:" + (System.currentTimeMillis() - filterStart)+" ms to process the filter.");
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {

		log.debug("ActionPerformed: " + event.getActionCommand());

		/* The same object is used by the main filter every time, therefore these variables needs to be set back to default */
		if (event.getSource().equals(MovieManager.getDialog().getFilter())) {
			mainFilter = true;
		}
			
		execute();
	}
	
	public void comboCheckBoxKeyActionPerformed(ComboCheckBoxKeyEvent evt) {
		mainFilter = true;
		execute();
	}
}
