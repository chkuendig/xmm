/**
 * @(#)DatabaseImporterExporter.java 1.0 26.09.06 (dd.mm.yy)
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

import java.awt.Dialog;
import java.util.ArrayList;

import javax.swing.JDialog;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogMovieInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.log4j.Logger;



public class DatabaseImporterExporter {

	Logger log = Logger.getLogger(getClass());

	private int lengthOfTask = 0;
	private int current = -1;
	private boolean done = false;
	private boolean canceled = false;
	private ArrayList transferred;
	private ModelImportExportSettings importSettings;
	private Dialog parent;

	public MovieManagerCommandImportExportHandler handler;

	public DatabaseImporterExporter(Dialog parent, MovieManagerCommandImportExportHandler handler, ModelImportExportSettings importSettings) {
		this.importSettings = importSettings;
		this.parent = parent;
		this.handler = handler;
	}

	public void go() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				current = -1;
				done = false;
				canceled = false;

				Importer importer = null;

				try {
					importer =  new Importer(importSettings);
				}
				catch (Exception e) {
					log.warn("Exception:" + e.getMessage(), e);   
				}
				return importer;
			}
		};
		worker.start();
	}

	public int getLengthOfTask() {
		return lengthOfTask;
	}


	public int getCurrent() {
		return current;
	}

	/* Stops the importing process */
	public void stop() {
		canceled = true;
	}

	public boolean isDone() {
		return done;
	}

	/* Returns the array transferred which contains all the finished database entries */
	public ArrayList getTransferred() {
		return transferred;
	}



	/**
	 * The actual database import task.
	 * This runs in a SwingWorker thread.
	 */
	class Importer {

		ArrayList movielist = null;

		int extraInfoFieldsCount;

		Importer(ModelImportExportSettings importSettings) {

			try {

				/* Setting the priority of the thread to 4 to give the GUI room to update more often */
				Thread.currentThread().setPriority(3);

				try {
					handler.retrieveMovieList();
					lengthOfTask = handler.getMovieListSize();

				} catch (Exception e) {
					log.error("Exception:" + e.getMessage(), e);
					JDialog alert = new DialogAlert(parent, "Error", e.getMessage());
					GUIUtil.showAndWait(alert, true);
					return;
				}

				transferred = new ArrayList(lengthOfTask);

				String title = "";

				for (int i = 0; i < lengthOfTask; i++) {

					title = handler.getTitle(i);
					
					if (title != null && !title.equals("")) {
					
						if (handler.isAborted() || canceled)
							break;

						if (!handler.isCancelled()) {

							int ret = -1; 

							ret = handler.addMovie(i);

							if (ret == -1)
								transferred.add("Failed to import: " + title);
							else if (!title.equals(""))
								transferred.add(title);

							current++;
						}
						else {// Reset settings
							handler.setCancelled(false);
						}
					}
					else {
						transferred.add("Empty entry");
						current++;
					}
				}
				done = true;
				handler.done();
				
			} catch(Exception e) {
				log.error("Exception:" + e.getMessage(), e);
			} 
		}
	}
}

