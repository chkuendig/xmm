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

import javax.swing.JDialog;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandSaveChangedNotes;
import net.sf.xmm.moviemanager.gui.DialogDatabaseImporterExporter;
import net.sf.xmm.moviemanager.gui.DialogExport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandExport implements ActionListener{

	static Logger log = Logger.getRootLogger();

	ModelImportExportSettings exportSettings;

	protected void execute() {

		try {

			// If any notes have been changed, they will be saved before changing list
			MovieManagerCommandSaveChangedNotes.execute();

			DialogExport dialogExport = new DialogExport();
			GUIUtil.showAndWait(dialogExport, true);

			exportSettings = dialogExport.getSettings();

			if (dialogExport.isCancelled())
				return;

			MovieManagerCommandImportExportHandler exporter = null;

			//CSV  or  Excel spreadsheet 
			if (exportSettings.mode == ModelImportExportSettings.EXPORT_MODE_CSV)
				exporter = new MovieManagerCommandExportCSV(exportSettings);
			else if (exportSettings.mode == ModelImportExportSettings.EXPORT_MODE_EXCEL)
				exporter = new MovieManagerCommandExportExcel(exportSettings);
				
			if (exporter != null) {
				try {
					exporter.execute();
				} catch (Exception e) {
					log.error("Exception:" + e.getMessage(), e);
				}

				if (!exporter.isCancelled()) {
					final JDialog dialogExorter = new DialogDatabaseImporterExporter(exporter, exportSettings);
					GUIUtil.show(dialogExorter, true);
				}
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}

	
	
	
	/* 
	
	 //Opens a filechooser and returns the absolute path to the selected file
	private String getCoverDirectory1(String databaseFilePath) {

		// Opens the Open dialog... 
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

			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
				// Gets the path... 
				String filepath = fileChooser.getSelectedFile().getAbsolutePath();

				if (!(new File(filepath).exists())) {
					throw new Exception("Covers directory not found."); //$NON-NLS-1$
				}
				// Sets the last path... 
				MovieManager.getConfig().setLastFileDir(new File(filepath));
				return filepath;
			}
		}
		catch (Exception e) {
			log.error("", e); //$NON-NLS-1$
		}
		// Sets the last path... 
		MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
		return ""; //$NON-NLS-1$
	}

/*

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		
		Thread t = new Thread() {
			public void run() {
				execute();
			}
		};
		t.start();
	}
}

