/**
 * @(#)MovieManagerCommandExportHandler.java 1.0 26.09.05 (dd.mm.yy)
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

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

/**
 * The Class MovieManagerCommandExportHandler.
 */
public abstract class MovieManagerCommandExportHandler implements MovieManagerCommandImportExportHandler {

	/** The log. */
	static Logger log = Logger.getLogger(MovieManagerCommandExportHandler.class);
	
	/** The cancelled. */
	boolean cancelled = false;
	
	/** The aborted. */
	boolean aborted = false;
	
	/** The model movie info. */
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	/** The movie. */
	ModelMovie movie = null;
	
	/** The list to add movie to. */
	public String listToAddMovieTo = null;
	
	/** The movie list. */
	public ArrayList movieList = null;
	
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#setCancelled(boolean)
	 */
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
		modelMovieInfo.clearModel();
	}
		
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#setAborted(boolean)
	 */
	public void setAborted(boolean abort) {
		aborted = abort;
	}
		
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#isCancelled()
	 */
	public boolean isCancelled() {
		return cancelled;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#isAborted()
	 */
	public boolean isAborted() {
		return aborted;
	}
	
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#execute()
	 */
	public void execute() throws Exception {};
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#retrieveMovieList()
	 */
	public abstract void retrieveMovieList() throws Exception;
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#getMovieListSize()
	 */
	public abstract int getMovieListSize() throws Exception;
		
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#getTitle(int)
	 */
	public abstract String getTitle(int i) throws Exception;
	
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#addMovie(int)
	 */
	public abstract int addMovie(int i) throws Exception;
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#done()
	 */
	public void done() throws Exception {};
	
	
	
	
    /**
	 * Gets the database data.
	 * 
	 * @return the database data
	 */
	public Object [][] getDatabaseData() {

		ArrayList generalInfoFieldNames = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
		ArrayList additionalInfoFieldNames = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
		ArrayList extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);

		int columnCount = generalInfoFieldNames.size() + additionalInfoFieldNames.size() + extraInfoFieldNames.size();

		DefaultListModel movieList = MovieManager.getDialog().getCurrentMoviesList();

		Object [] movies = movieList.toArray();
		Object [][] data = new Object[movies.length][columnCount];

		for (int i = 0; i < movies.length; i++) {

			int tableIndex = 0;
						
			if (!((ModelEntry) movies[i]).getHasAdditionalInfoData())
				((ModelEntry) movies[i]).updateAdditionalInfoData();
				
			
			for (int o = 0; o < generalInfoFieldNames.size(); o++) {
				data[i][tableIndex] = ((ModelEntry) movies[i]).getValue((String) generalInfoFieldNames.get(o), "General Info");
				tableIndex++;
			}

			for (int o = 0; o < additionalInfoFieldNames.size(); o++) {
				data[i][tableIndex] = ((ModelEntry) movies[i]).getValue((String) additionalInfoFieldNames.get(o), "Additional Info");
				tableIndex++;
			}
			
			for (int o = 0; o < extraInfoFieldNames.size(); o++) {
				data[i][tableIndex] = ((ModelEntry) movies[i]).getValue((String) extraInfoFieldNames.get(o), "Extra Info");
				tableIndex++;
			}
		}
			
		return data;
	}
}
