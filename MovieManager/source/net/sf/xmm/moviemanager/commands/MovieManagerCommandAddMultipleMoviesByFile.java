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
import java.util.ArrayList;

import javax.swing.JDialog;

import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportMoviesByFile;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogDatabaseImporterExporter;
import net.sf.xmm.moviemanager.models.ModelFileImportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandAddMultipleMoviesByFile extends MovieManagerCommandAddMultipleMovies implements ActionListener {

	Logger log = Logger.getLogger(getClass());
	
		
	DialogAddMultipleMovies damm;

	/**
	 * Executes the command.
	 * Checks all the options before starting to process the list of movies
	 * found in the directory
	 **/
	protected void execute() {

//		If any notes have been changed, they will be saved before changing list
		MovieManagerCommandSaveChangedNotes.execute();
		
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
		
		damm.initializeTree();
	}

	void startAddMovies() {
		ModelImportExportSettings importSettings = getSettings();
		MovieManagerCommandImportMoviesByFile importByFile = new MovieManagerCommandImportMoviesByFile(importSettings);

		final JDialog dialog = new DialogDatabaseImporterExporter(damm, importByFile, importSettings, true);
		importSettings.setParent(dialog);
		GUIUtil.show(dialog, true);
	}
	

	ModelImportExportSettings getSettings() {

		ModelFileImportSettings importSettings = new ModelFileImportSettings();
				
		ArrayList <DialogAddMultipleMovies.Files> fileList = damm.getMoviesToAdd();

		if (fileList == null) {
			return null;
		}
		
		importSettings.fileList = fileList;
		importSettings.multiAddSelectOption = damm.getMultiAddSelectOption();
		importSettings.enableExludeParantheses = damm.enableExludeParantheses.isSelected();
		importSettings.enableExludeCDNotations = damm.enableExludeCDNotation.isSelected();
		importSettings.enableExludeIntegers = damm.enableExludeIntegers.isSelected();
		importSettings.enableExludeYear = damm.enableExludeYear.isSelected();
		importSettings.enableUseFolderName = damm.enableUseFolderName.isSelected();
		importSettings.enableSearchNfoForImdb = damm.enableSearchNfoForImdb.isSelected();
		importSettings.enableExludeUserdefinedInfo = damm.getEnableExludeUserdefinedInfo();
		importSettings.enableExludeAllAfterMatchOnUserDefinedInfo = damm.enableExludeAllAfterMatchOnUserDefinedInfo.isSelected();
		importSettings.enableUseParentFolderIfCD = damm.enableUseParentFolderIfCD.isSelected();
			
		if (damm.enableAddMoviesToList != null && damm.enableAddMoviesToList.isSelected()) {
			importSettings.addMovieToList = true;
			importSettings.addToThisList = (String) damm.listChooser.getSelectedItem();
		}
		else
			importSettings.addMovieToList = false;
		
		
		importSettings.existingMediaFiles = damm.getExistingMediaFiles();
		importSettings.existingMediaFileNames = damm.getExistingMediaFileNames();
		
		return importSettings;
	}
	
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: "+ event.getActionCommand());
		execute();
	}
}

