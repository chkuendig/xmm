package net.sf.xmm.moviemanager.commands.importexport;

import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class MovieManagerCommandImportExportHandler {

	static Logger log = Logger.getRootLogger();
	boolean cancelled = false;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	ModelMovie movie = null;
	public ArrayList movieList = null;
	
	public void execute() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method execute!");
	}
	
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
		modelMovieInfo.clearModel();
	}
		
	public void done() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method done!");
	}
	
	public boolean isCancelled() throws Exception {
		throw new Exception("MovieManagerCommandImportExportHandler does not implement method isCancelled!");
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
    public void executeCommandGetIMDBInfoMultiMovies(String searchString, String filename, int multiAddSelectOption) {
        
        /* Checks the movie title... */
        log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
        if (!searchString.equals("")) { //$NON-NLS-1$
            DialogIMDB dialogIMDB = new DialogIMDB(modelMovieInfo.model, searchString, filename, null, multiAddSelectOption, null);
            
            if (dialogIMDB.cancelSet || dialogIMDB.dropImdbInfoSet)
            	setCancelled(true);
            
        } else {
            DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"), Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
    }
    
}
