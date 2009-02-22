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

public class MovieManagerCommandExportHandler implements MovieManagerCommandImportExportHandler {

	static Logger log = Logger.getRootLogger();
	boolean cancelled = false;
	boolean aborted = false;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	ModelMovie movie = null;
	
	public String listToAddMovieTo = null;
	
	public ArrayList movieList = null;
	
	
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
