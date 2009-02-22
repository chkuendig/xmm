package net.sf.xmm.moviemanager.commands.importexport;

import net.sf.xmm.moviemanager.models.ModelMovie;

public interface MovieManagerCommandImportExportHandler {

	public void setCancelled(boolean cancel);
		
	public void setAborted(boolean abort);
		
	public boolean isCancelled();
	
	public boolean isAborted();
	
	public void execute() throws Exception;	
	public void done() throws Exception;
		
	public int addMovie(int i) throws Exception;
	
	public void retrieveMovieList() throws Exception;
	
	public int getMovieListSize() throws Exception;
	
	public String getNextMovie(int i) throws Exception;
	
	public void setNextModel(ModelMovie model);
}
