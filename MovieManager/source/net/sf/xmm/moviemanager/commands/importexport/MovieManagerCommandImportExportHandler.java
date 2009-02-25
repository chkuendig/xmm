package net.sf.xmm.moviemanager.commands.importexport;

public interface MovieManagerCommandImportExportHandler {

	public void setCancelled(boolean cancel);
		
	public void setAborted(boolean abort);
		
	public boolean isCancelled();
	
	public boolean isAborted();
	
	/**
	 * Override if needed.
	 * Will be called before the import session is started.
	 */
	public void execute() throws Exception;	
	
	/**
	 * Override if needed.
	 * Will be called when all the entries have been processed.
	 */
	public void done() throws Exception;
	
	/**
	 * Will be called for each entry.
	 * @argument int i - the index in the arraylist movieList.
	 * @Return -1 if failure, else not -1
	 */
	public int addMovie(int i) throws Exception;
	
	/**
	 * Will be called at the beginning of the import sesion.
	 */
	public void retrieveMovieList() throws Exception;
	
	/**
	 * Return the size of the movie list.
	 */
	public int getMovieListSize() throws Exception;
	
	/**
	 * Return the title of the entry at index i.
	 * @argument int i - the index of the entry in the movieList.
	 */
	public String getTitle(int i) throws Exception;
		
}
