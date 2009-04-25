/**
 * @(#)MovieManagerCommandImportHandler.java 1.0 26.09.05 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public abstract class MovieManagerCommandImportHandler implements MovieManagerCommandImportExportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandImportHandler.class);
	
	boolean cancelled = false;
	boolean aborted = false;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	ModelMovie movie = null;
	
	ModelImportExportSettings settings;
	
	ArrayList<String> addToThisList = new ArrayList<String>();
	
	MovieManagerCommandImportHandler(ModelImportExportSettings settings) {
		this.settings = settings;
		addToThisList.add(settings.addToThisList);
		
		if (settings.isIMDbEnabled() &&
				(!MovieManager.getIt().getDatabase().isMySQL() || 
				(MovieManager.getIt().getDatabase().isMySQL() && MovieManager.getConfig().getStoreCoversLocally())))
			modelMovieInfo.setSaveCover(true);
		else
			modelMovieInfo.setSaveCover(false);
	}
	
	
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
		modelMovieInfo.clearModel();
	}
		
	public void setAborted(boolean abort) {
		aborted = abort;
	}
		
	public boolean isCancelled() {
		return cancelled;
	}
	
	public boolean isAborted() {
		return aborted;
	}
	
	public void execute() throws Exception {};
	
	public void done() throws Exception {};
		
	public abstract int addMovie(int i) throws Exception;
	
	public abstract void retrieveMovieList() throws Exception;
	
	public abstract int getMovieListSize() throws Exception;
	
	public abstract String getTitle(int i) throws Exception;
	
		
	/**
     * Gets the IMDB info for movies (multiAdd)
     **/
    public void executeCommandGetIMDBInfoMultiMovies(String searchString, String filename, ModelImportExportSettings settings, ModelMovie model) {
      	
        /* Checks the movie title... */
        log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
        
        if (!searchString.equals("")) { //$NON-NLS-1$
            DialogIMDB dialogIMDB = new DialogIMDB(model, searchString, filename, null, settings.multiAddIMDbSelectOption, null);
             
            if (dialogIMDB.cancelSet)
            	setCancelled(true);
            
            if (dialogIMDB.cancelAllSet)
            	setAborted(true);
            
            addToThisList.clear();
            
            if (dialogIMDB.dropImdbInfoSet)
            	addToThisList.add(settings.skippedListName);
            else
            	addToThisList.add(settings.addToThisList);
                        
        } else {
            DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"), Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
    }
}
