/**
 * @(#)MovieManagerCommandAddMultipleMoviesByFile.java 1.0 24.01.06 (dd.mm.yy)
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
 * 7Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.swing.extentions.filetree.FileNode;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandAddMultipleMoviesByFile extends MovieManagerCommandAddMultipleMovies implements ActionListener {

	Logger log = Logger.getLogger(getClass());

	String[] stringFiles;
	
	ModelMovieInfo movieInfoModel = null;

	String excludeString;
	boolean enableExludeString = false;
	int multiAddSelectOption;
	boolean enableExludeParantheses = false;
	boolean enableExludeCDNotations = false;
	boolean enableExludeIntegers = false;
	boolean enableExludeCodecInfo = false;
	boolean searchInSubdirectories = false;
	boolean addMovieToList = false;
	boolean titleOption = false;
	String addToThisList = null;

	ArrayList moviesToAdd;

	DialogAddMultipleMovies damm;

	/**
	 * Executes the command.
	 * Checks all the options before starting to process the list of movies
	 * found in the directory
	 **/
	protected void execute() {

//		If any notes have been changed, they will be saved before changing list
		MovieManagerCommandSaveChangedNotes.execute();

		movieInfoModel = new ModelMovieInfo(false, true);
		movieInfoModel.setSaveCover(true);
		
		cancelAll = false;

		damm = new DialogAddMultipleMovies();

		damm.buttonAddMovies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				damm.executeSave();
				if (damm.validateAddList())
					startAddMovies();
			}
		});

		GUIUtil.showAndWait(damm, true);
	}

	void startAddMovies() {

		multiAddSelectOption = damm.getMultiAddSelectOption();
		enableExludeString = damm.getMultiAddExcludeStringEnabled();
		excludeString = damm.getMultiAddExcludeString();

		ArrayList fileList = damm.getMoviesToAdd();

		if (fileList == null) {
			return;
		}

		enableExludeParantheses = damm.enableExludeParantheses.isSelected();
		enableExludeCDNotations = damm.enableExludeCDNotation.isSelected();
		enableExludeIntegers = damm.enableExludeIntegers.isSelected();
		enableExludeCodecInfo = damm.enableExludeCodecInfo.isSelected();
		titleOption = damm.titleOption.isSelected();
			
		if (damm.enableAddMoviesToList != null && damm.enableAddMoviesToList.isSelected()) {
			addMovieToList = true;
			addToThisList = (String) damm.listChooser.getSelectedItem();
		}
		else
			addMovieToList = false;
		
		createMovies(fileList);
	}


	protected void createMovies(ArrayList fileList) {

		FileNode fileNode;
		File [] tempFile = new File[1];

		String searchString = null; /*Used to search on imdb*/
		String path = null; /* Path of the file */

		while (!fileList.isEmpty()) {

			fileNode = (FileNode) fileList.remove(0);
			tempFile[0] =  fileNode.getFile();
			String searchTitle = tempFile[0].getName();

			log.debug("Processing:" + searchTitle);

			movieInfoModel.setAdditionalInfoFieldsEmpty();

			/*Getting the fileinfo from file*/
			movieInfoModel.getFileInfo(tempFile);

			/*Used if editing existing movie*/
			movieInfoModel.setMultiAddFile(tempFile[0]);

			/*Used to set the title of the DialogIMDB window*/
			searchString = searchTitle;/*Used to search on imdb*/
			/* Sets up search string as Folder name or file name */
			if (titleOption) {


				path = tempFile[0].getPath();
				int slash = path.lastIndexOf('\\');

				if (slash == -1) {
					searchString = path;
				}
				else {
					path = path.substring(0, slash);
					slash = path.lastIndexOf('\\');

					if (slash == -1) {
						searchString = path;
					}
					else {
						searchString = path.substring(slash+1);
					}
				}
				searchTitle = searchString;
			}

			if (enableExludeString)
				searchString = StringUtil.performExcludeString(searchString, excludeString);

			if (enableExludeParantheses)
				searchString = StringUtil.performExcludeParantheses(searchString, true);

			if (enableExludeCodecInfo) {
				String [] excludeStrings = new String[] {"divx", "dvdivx", "xvidvd", "xvid", "dvdrip", "ac3", "bivx", "mp3"};
				searchString = StringUtil.performExcludeCodecInfo(searchString, excludeStrings);
			}
			if (enableExludeCDNotations)
				searchString = StringUtil.performExcludeCDNotations(searchString);

			if (enableExludeIntegers)
				searchString = StringUtil.performExcludeIntegers(searchString);

			/*removes dots, double spaces, underscore...*/
			searchString = StringUtil.removeVarious(searchString);

			executeCommandGetIMDBInfoMultiMovies(searchString, searchTitle, multiAddSelectOption, addToThisList);

			if (dropImdbInfo)
				movieInfoModel.setGeneralInfoFieldsEmpty();

			dropImdbInfo = false;

			if (cancelAll) {
				movieInfoModel.model.setTitle("");
				return;
			}
			else if (cancel) {
				movieInfoModel.model.setTitle("");

				/* Empties the additional fields in the DialogMovieInfo object */
				movieInfoModel.setAdditionalInfoFieldsEmpty();
				cancel = false;
			}
			else {
				try {
					boolean status = movieInfoModel.saveCoverToFile();
					
				} catch (Exception e) {
					log.warn("Exception: " + e.getMessage()); //$NON-NLS-1$
				}

				try {
					ModelEntry model = movieInfoModel.saveToDatabase(addToThisList);
					MovieManagerCommandSelect.executeAndReload(model, false, false, false);
				} catch (Exception e) {
					log.error("Saving to database failed.", e);
				}

				movieInfoModel.model.setTitle("");
			}
		}
		MovieManagerCommandSelect.executeAndReload(0);
	}

	

	public void setCancelAll(boolean value) {
		cancelAll = value;
	}

	public void setCancel(boolean value) {
		cancel = value;
	}


	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public void executeCommandGetIMDBInfoMultiMovies(String searchString, String filename, int multiAddSelectOption, String addToThisList) {

		/* Checks the movie title... */
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
		if (!searchString.equals("")) { //$NON-NLS-1$
			DialogIMDB dialogIMDB = new DialogIMDB(movieInfoModel.model, searchString, filename, movieInfoModel.getMultiAddFile(), multiAddSelectOption, addToThisList);
			cancel = dialogIMDB.cancelSet;
			cancelAll = dialogIMDB.cancelAllSet;
			dropImdbInfo = dialogIMDB.dropImdbInfoSet;

		} else {
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtil.showAndWait(alert, true);
		}
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: "+ event.getActionCommand());
		execute();
	}
}

