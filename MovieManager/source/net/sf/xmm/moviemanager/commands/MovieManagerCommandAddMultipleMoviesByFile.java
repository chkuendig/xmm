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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.FileUtil;
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
	ImdbImportOption multiAddSelectOption;
	boolean enableExludeParantheses = false;
	boolean enableExludeCDNotations = false;
	boolean enableExludeIntegers = false;
	boolean enableExludeAllAfterMatchOnUserDefinedInfo = false;
	boolean enableSearchNfoForImdb = false;
	boolean searchInSubdirectories = false;
	boolean addMovieToList = false;
	boolean enableUseFolderName = false;
	boolean enableExludeUserdefinedInfo = false;
	boolean enableUseParentFolderIfCD = false;

	String addToThisList = null;

	ArrayList<String> moviesToAdd;

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

		ArrayList <DialogAddMultipleMovies.Files> fileList = damm.getMoviesToAdd();

		if (fileList == null) {
			return;
		}

		enableExludeParantheses = damm.enableExludeParantheses.isSelected();
		enableExludeCDNotations = damm.enableExludeCDNotation.isSelected();
		enableExludeIntegers = damm.enableExludeIntegers.isSelected();
		enableUseFolderName = damm.enableUseFolderName.isSelected();
		enableSearchNfoForImdb = damm.enableSearchNfoForImdb.isSelected();
		enableExludeUserdefinedInfo = damm.enableExludeUserdefinedInfo.isSelected();
		enableExludeAllAfterMatchOnUserDefinedInfo = damm.enableExludeAllAfterMatchOnUserDefinedInfo.isSelected();
		enableUseParentFolderIfCD = damm.enableUseParentFolderIfCD.isSelected();
			
		if (damm.enableAddMoviesToList != null && damm.enableAddMoviesToList.isSelected()) {
			addMovieToList = true;
			addToThisList = (String) damm.listChooser.getSelectedItem();
		}
		else
			addMovieToList = false;
		
		createMovies(fileList);
	}


	protected void createMovies(ArrayList<DialogAddMultipleMovies.Files> fileList) {

		DialogAddMultipleMovies.Files fileNode;
		File [] tempFile = new File[1];

		String searchString = null; /* Used to search on imdb */
		String path = null; /* Path of the file */

		while (!fileList.isEmpty()) {
			String imdbId = null;

			fileNode = (DialogAddMultipleMovies.Files) fileList.remove(0);
			
			if (fileNode.getAddedFiles().length == 0)
				tempFile = new File[]{fileNode.getFile()};
			else {
				tempFile = new File[fileNode.getAddedFiles().length + 1];
				tempFile[0] = fileNode.getFile();
				
				DialogAddMultipleMovies.Files [] addedFiles = fileNode.getAddedFiles();
				
				for (int i = 0; i < addedFiles.length; i++) {
					tempFile[i+1] = addedFiles[i].getFile();
				}				
			}
						
			String searchTitle = tempFile[0].getName();

			log.debug("Processing:" + searchTitle);

			movieInfoModel.setAdditionalInfoFieldsEmpty();

			/* Getting the fileinfo from file */
			try {
				movieInfoModel.getFileInfo(tempFile);
			} catch (Exception e) {
				log.warn("Exception:" + e.getMessage(), e);
			}

			/*Used if editing existing movie*/
			movieInfoModel.setMultiAddFile(tempFile[0]);

			/*Used to set the title of the DialogIMDB window*/
			searchString = searchTitle;/*Used to search on imdb*/

			/* Sets up search string as Folder name or file name */
			if (enableUseFolderName) {
				path = tempFile[0].getPath();
				int slash = path.lastIndexOf(File.separator);

				if (slash == -1) {
					searchString = path;
				}
				else {
					path = path.substring(0, slash);
					slash = path.lastIndexOf(File.separator);

					if (slash == -1) {
						searchString = path;
					}
					else {
						String temp = path.substring(slash+1);
						if (enableUseParentFolderIfCD && temp.toLowerCase().startsWith("cd")) {
							// IF last directory is CD* than the name is in the directory above.
							path = path.substring(0, slash);
							slash = path.lastIndexOf(File.separator);
						}
						searchString = path.substring(slash+1);
					}
				}
				searchTitle = searchString;
			}

			if (enableExludeString)
				searchString = StringUtil.performExcludeString(searchString, excludeString);

			if (enableExludeParantheses)
				searchString = StringUtil.performExcludeParantheses(searchString, true);

			if (enableExludeAllAfterMatchOnUserDefinedInfo) {
				String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();
				
				if (!info.equals("")) {
					Pattern p = Pattern.compile("[,]");
					String[] excludeStrings = p.split(info);
					searchString = StringUtil.performExcludeUserdefinedInfo(searchString, excludeStrings);
				}
			}
			else if (enableExludeUserdefinedInfo) {
				String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();
				
				if (!info.equals("")) {
					Pattern p = Pattern.compile("[,]");
					String[] excludeStrings = p.split(info);
					searchString = StringUtil.performExcludeCodecInfo(searchString, excludeStrings);
				}
			}
			
			if (enableExludeCDNotations)
				searchString = StringUtil.performExcludeCDNotations(searchString);

			if (enableExludeIntegers)
				searchString = StringUtil.performExcludeIntegers(searchString);

			if (enableSearchNfoForImdb)
				imdbId = searchNfoForImdb(path);
			
			/*removes dots, double spaces, underscore...*/
			searchString = StringUtil.removeVarious(searchString);
	
			executeCommandGetIMDBInfoMultiMovies(imdbId, searchString, searchTitle, multiAddSelectOption, addToThisList);

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
				
					if (!status)
						log.debug("Cover for title " + movieInfoModel.model.getTitle() + " not saved to file.");
					
				} catch (Exception e) {
					log.warn("Exception: " + e.getMessage()); //$NON-NLS-1$
				}

				try {
					ArrayList<String> list = new ArrayList<String>();
					list.add(addToThisList);
					ModelEntry model = movieInfoModel.saveToDatabase(list);
					MovieManagerCommandSelect.executeAndReload(model, false, false, true);
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

	public String searchNfoForImdb(String _path) {
		try {
			if (_path != null && !_path.equals("")) {
				String tmp;
				BufferedReader br;
				File path = new File(_path);
				File files[] = path.listFiles();
				
				for (int i = 0; i < files.length; i++) {
					// Cycle through all entries in the directory
					String filename = files[i].getName().toLowerCase();
					if (files[i].isFile() && files[i].length() < 40000 && (filename.endsWith(".txt") || filename.endsWith(".nfo") || filename.endsWith(".url"))) {
						// Only process files < 40000 Bytes with with .txt or .nfo suffix and no directories
												
						br = new BufferedReader(new FileReader(files[i]));
						tmp = br.readLine();
						while (tmp != null) {
							if (tmp.contains("imdb.com/title/tt") || tmp.contains("imdb.de/title/tt")) {
								// If File contains an imdb url than get it out
								if (tmp.contains("imdb.com/title/tt"))
									tmp = tmp.substring(tmp.indexOf("imdb.com/title/tt") + 17);
								else
									tmp = tmp.substring(tmp.indexOf("imdb.de/title/tt") + 16);

								// Search for a 6 to 8 digits long number (normally 7 digits is used in the url)
								Pattern p = Pattern.compile("[\\d]{6,8}");
								Matcher m = p.matcher(tmp);

								if (m.find()) {
									br.close();
									return m.group();
								}
							}
							tmp = br.readLine();
						}
						br.close();
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			log.debug("No nfo/txt file found for parsing");
		}
		catch (IOException e) {
			log.debug("I/O error while processing nfo/txt files");
		}

		return null;
	}
	
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public void executeCommandGetIMDBInfoMultiMovies(String imdbId, String searchString, String filename, ImdbImportOption multiAddSelectOption, String addToThisList) {

		/* Checks the movie title... */
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
		if (!searchString.equals("")) { //$NON-NLS-1$
			DialogIMDB dialogIMDB = new DialogIMDB(imdbId, movieInfoModel.model, searchString, filename, movieInfoModel.getMultiAddFile(), multiAddSelectOption, addToThisList);
			cancel = dialogIMDB.cancelSet;
			cancelAll = dialogIMDB.cancelAllSet;
			dropImdbInfo = dialogIMDB.dropImdbInfoSet;

		} else {
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"), Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
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

