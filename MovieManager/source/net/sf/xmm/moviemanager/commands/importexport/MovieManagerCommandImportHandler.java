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

public class MovieManagerCommandImportHandler implements MovieManagerCommandImportExportHandler {

	static Logger log = Logger.getRootLogger();
	boolean cancelled = false;
	boolean aborted = false;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	ModelMovie movie = null;
	
	public ArrayList movieList = null;
	
	ModelImportExportSettings settings;
	
	String addToThisList = null;
	
	MovieManagerCommandImportHandler(ModelImportExportSettings settings) {
		this.settings = settings;
		addToThisList = settings.addToThisList;
		
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
	
	public void execute() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method execute!");
	}
	
	public void done() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method done!");
	}
		
	public int addMovie(int i) throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method addMovie!");
	}
	
	public void retrieveMovieList() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method retrieveMovieList!");
	}
	
	public int getMovieListSize() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method getMovieListSize!");
	}
	
	public String getNextMovie(int i) throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method getTitle!");
	}
	
	public void setNextModel(ModelMovie model) {
		modelMovieInfo.setModel(model, true, false);
	}
	
	
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
            
            if (dialogIMDB.dropImdbInfoSet)
            	addToThisList = settings.skippedListName;
            else
            	addToThisList = settings.addToThisList;
                        
        } else {
            DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"), Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
    }
}
