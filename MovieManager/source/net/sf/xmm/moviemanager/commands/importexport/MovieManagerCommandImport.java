/**
 * @(#)MovieManagerCommandImport.java 1.0 26.09.06 (dd.mm.yy)
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
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.commands.importexport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSaveChangedNotes;
import net.sf.xmm.moviemanager.gui.DialogDatabaseImporterExporter;
import net.sf.xmm.moviemanager.gui.DialogImport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class MovieManagerCommandImport implements ActionListener{

	static Logger log = Logger.getLogger(MovieManagerCommandImport.class);

	boolean done = false;

	boolean cancelAll = false;

	ModelImportExportSettings importSettings;


	protected void execute() {

		try {

			// If any notes have been changed, they will be saved before changing list
			MovieManagerCommandSaveChangedNotes.execute();


			DialogImport importMovie = new DialogImport();
			GUIUtil.showAndWait(importMovie, true);
			
			importSettings = importMovie.getSettings();
	
			if (importMovie.cancelAll)
				return;

			MovieManagerCommandImportExportHandler importer = null;

			
			
			if (importSettings.mode == ModelImportExportSettings.IMPORT_MODE_TEXT) {
				importer = new MovieManagerCommandImportText(importSettings);
				importer.execute();
			}
			else if (importSettings.mode == ModelImportExportSettings.IMPORT_MODE_CSV) {
				importer = new MovieManagerCommandImportCSV(importSettings);
				importer.execute();
			}
			else if (importSettings.mode == ModelImportExportSettings.IMPORT_MODE_EXCEL) {
				importer = new MovieManagerCommandImportExcel(importSettings);
				importer.execute();
			}
			else if (importSettings.mode == ModelImportExportSettings.IMPORT_MODE_XML_DATABASE) {
				importer = new MovieManagerCommandImportXMLDatabase(importSettings);
				importer.execute();
			}
			else if (importSettings.mode == ModelImportExportSettings.IMPORT_MODE_EXTREME) {

			/*	if (importMovie.enableOverwriteImportedInfoWithImdbInfo.isSelected())
					importSettings.overwriteWithImdbInfo = true;

				if (importMovie.useMediaLanguage.isSelected())
					importSettings.extremeOriginalLanguage = false;*/

				File tempFile = new File(importSettings.filePath);

				importSettings.coverPath = tempFile.getParentFile().getParent()+ File.separator +"Covers" + File.separator; //$NON-NLS-1$
				if (!new File(importSettings.coverPath).isDirectory()) {

					String path = ""; //$NON-NLS-1$
					if (!(path = getCoverDirectory(importSettings.filePath)).equals("")) //$NON-NLS-1$
						importSettings.coverPath = path + File.separator;
				}
			}

			if (importer != null && !importer.isCancelled()) {
				final JDialog dialogExorter = new DialogDatabaseImporterExporter(importer, importSettings);
				GUIUtil.show(dialogExorter, true);
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}



	/*Opens a filechooser and returns the absolute path to the selected file*/
	private String getCoverDirectory(String databaseFilePath) {

		/* Opens the Open dialog... */
		ExtendedFileChooser fileChooser = new ExtendedFileChooser();
		try {
			fileChooser.setFileSelectionMode(ExtendedFileChooser.DIRECTORIES_ONLY);
			File path;

			if (!databaseFilePath.equals("") && (path = new File(databaseFilePath)).exists()) //$NON-NLS-1$
				fileChooser.setCurrentDirectory(path);
			else if (MovieManager.getConfig().getLastFileDir() != null)
				fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastFileDir());

			fileChooser.setDialogTitle(Localizer.getString("MovieManagerCommandImport.dialog-importer.filechooser.title")); //$NON-NLS-1$
			fileChooser.setApproveButtonText(Localizer.getString("MovieManagerCommandImport.dialog-importer.filechooser.approve-button.text")); //$NON-NLS-1$
			fileChooser.setApproveButtonToolTipText(Localizer.getString("MovieManagerCommandImport.dialog-importer.filechooser.approve-button.tooltip")); //$NON-NLS-1$
			fileChooser.setAcceptAllFileFilterUsed(false);

			int returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());
			if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
				/* Gets the path... */
				String filepath = fileChooser.getSelectedFile().getAbsolutePath();

				if (!(new File(filepath).exists())) {
					throw new Exception("Covers directory not found."); //$NON-NLS-1$
				}
				/* Sets the last path... */
				MovieManager.getConfig().setLastFileDir(new File(filepath));
				return filepath;
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		/* Sets the last path... */
		MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
		return ""; //$NON-NLS-1$
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$'
		
		Thread t = new Thread() {
			public void run() {
				execute();
			}
		};
		t.start();
	}
	
	
}




