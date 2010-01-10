package net.sf.xmm.moviemanager.commands.importexport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDbMultiAdd;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelFileImportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;

public class MovieManagerCommandImportMoviesByFile extends MovieManagerCommandImportHandler {

	ModelFileImportSettings fileSettings;
	
	public MovieManagerCommandImportMoviesByFile(ModelImportExportSettings settings) {
		super(settings);
		fileSettings = (ModelFileImportSettings) settings;
	}
	
	Logger log = Logger.getLogger(getClass());

	String[] stringFiles;
	
	ModelMovieInfo movieInfoModel = null;

	ArrayList<String> moviesToAdd;

	ArrayList <DialogAddMultipleMovies.Files> fileList = null;
	
	
	public int getMovieListSize() throws Exception {
		return fileList.size();
	}
	
	
	
	public void retrieveMovieList() throws Exception {
				
		fileList = fileSettings.fileList;
		
		System.err.println("retrieveMovieList");
		System.err.println("fileList.size:" + fileList.size());
		
		
		movieInfoModel = new ModelMovieInfo(false, true);
		movieInfoModel.setSaveCover(true);
		
		
	}
	
	
	public String getTitle(int i) throws Exception {

		// Reset cancelled status
		resetStatus();
		
		DialogAddMultipleMovies.Files fileNode;
		File [] tempFile = new File[1];

		String searchString = null; // Used to search on imdb 
		String searchTitle = null; // Title of the IMDb dialog
		String path = null; // Path of the file 

		String imdbId = null;

		fileNode = (DialogAddMultipleMovies.Files) fileList.get(i);

		//System.err.println("File to add:" + fileNode.getName());

		ArrayList<Files> files = fileNode.getFiles();

		tempFile = new File[files.size()];

		for (int u = 0; u < tempFile.length; u++) {
			tempFile[u] = files.get(u).getFile();
		}				

		searchString = tempFile[0].getName();
		//searchTitle = tempFile[0].getAbsolutePath();
		searchTitle = searchString;

		log.debug("Processing:" + searchTitle);

		movieInfoModel.clearModel();
		movieInfoModel.setAdditionalInfoFieldsEmpty();

		// Getting the fileinfo from file 
		try {
			movieInfoModel.getFileInfo(tempFile);
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}

		// Used if editing existing movie
		movieInfoModel.setMultiAddFile(tempFile[0]);

		// Used to set the title of the DialogIMDB window
		//searchString = searchTitle;/*Used to search on imdb

		//System.err.println("searchString:" + searchTitle);
		//System.err.println("tempFile[0].getPath():" + tempFile[0].getPath());



		// Sets up search string as Folder name or file name 
		if (fileSettings.enableUseFolderName) {
			//System.err.println("enableUseFolderName");

			path = tempFile[0].getPath();
			int slash = path.lastIndexOf(File.separator);

			if (slash == -1) {
				searchString = path;
				//System.err.println("setting searchString1:" + searchString);
			}
			else {
				path = path.substring(0, slash);
				slash = path.lastIndexOf(File.separator);

				if (slash == -1) {
					searchString = path;
					//System.err.println("setting searchString2:" + searchString);
				}
				else {
					String temp = path.substring(slash+1);
					if (fileSettings.enableUseParentFolderIfCD && temp.toLowerCase().startsWith("cd")) {
						// IF last directory is CD* than the name is in the directory above.
						path = path.substring(0, slash);
						slash = path.lastIndexOf(File.separator);
					}
					searchString = path.substring(slash+1);
					//System.err.println("setting searchString3:" + searchString);
				}
			}
			//searchTitle = searchString;
			//System.err.println("setting searchTitle:" + searchTitle);
		}

		//System.err.println("searchTitle:" + searchTitle);
		//System.err.println("searchString:" + searchString);

		if (fileSettings.enableExludeParantheses) {
			searchString = StringUtil.performExcludeParantheses(searchString, true);
			//System.err.println("setting searchString4:" + searchString);
		}

		if (fileSettings.enableExludeAllAfterMatchOnUserDefinedInfo) {
			String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();

			if (!info.equals("")) {
				Pattern p = Pattern.compile("[,]");
				String[] excludeStrings = p.split(info);
				searchString = StringUtil.performExcludeUserdefinedInfo(searchString, excludeStrings);
				//System.err.println("setting searchString5:" + searchString);
			}
		}
		else if (fileSettings.enableExludeUserdefinedInfo) {
			String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();

			if (!info.equals("")) {
				Pattern p = Pattern.compile("[,]");
				String[] excludeStrings = p.split(info);
				searchString = StringUtil.performExcludeCodecInfo(searchString, excludeStrings);
				//System.err.println("setting searchString6:" + searchString);
			}
		}

		if (fileSettings.enableExludeCDNotations)
			searchString = StringUtil.performExcludeCDNotations(searchString);

		if (fileSettings.enableExludeIntegers)
			searchString = StringUtil.performExcludeIntegers(searchString);

		if (fileSettings.enableSearchNfoForImdb)
			imdbId = searchNfoForImdb(path);

		//removes dots, double spaces, underscore...
		searchString = StringUtil.removeVarious(searchString);

		//System.err.println("searchString:" + searchString);
		//System.err.println("searchTitle:" + searchTitle);

		ImportExportReturn ret = executeCommandGetIMDBInfoMultiMovies(imdbId, searchString, searchTitle, fileSettings.multiAddSelectOption, fileSettings.addToThisList);

		System.err.println("Imdb done - canceled:" + isCancelled());

		/*
				if (dropImdbInfo)
					movieInfoModel.setGeneralInfoFieldsEmpty();

				dropImdbInfo = false;

				if (cancelAll) {
					movieInfoModel.model.setTitle("");
					return;
				}
				else if (cancel) {
					movieInfoModel.model.setTitle("");

					// Empties the additional fields in the DialogMovieInfo object 
					movieInfoModel.setAdditionalInfoFieldsEmpty();
					cancel = false;
				}
		 */

		return fileList.get(i).getName();
	}

	public ImportExportReturn addMovie(int i) throws Exception {

		ImportExportReturn ret = ImportExportReturn.success;

		try {
			boolean status = movieInfoModel.saveCoverToFile();

			if (!status)
				log.debug("Cover for title " + movieInfoModel.model.getTitle() + " not saved to file.");

		} catch (Exception e) {
			log.warn("Exception: " + e.getMessage()); //$NON-NLS-1$
		}

		try {
			ArrayList<String> list = new ArrayList<String>();
			list.add(fileSettings.addToThisList);
			ModelEntry model = movieInfoModel.saveToDatabase(list);
			MovieManagerCommandSelect.executeAndReload(model, false, false, true);
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
			ret = ImportExportReturn.error;
		}

		movieInfoModel.model.setTitle("");

		MovieManagerCommandSelect.executeAndReload(0);

		return ret;
	}

	
	public String searchNfoForImdb(String _path) {
		try {
			if (_path != null && !_path.equals("")) {
				String tmp;
				BufferedReader br;
				File path = new File(_path);
				File files[] = path.listFiles();
				
				for (int i = 0; i < files.length; i++) {
					// Cycle through all entries in the directory
					String filename = files[i].getName().toLowerCase();
					if (files[i].isFile() && files[i].length() < 40000 && (filename.endsWith(".txt") || filename.endsWith(".nfo") || filename.endsWith(".url"))) {
						// Only process files < 40000 Bytes with with .txt or .nfo suffix and no directories
												
						br = new BufferedReader(new FileReader(files[i]));
						tmp = br.readLine();
						while (tmp != null) {
							if (tmp.contains("imdb.com/title/tt") || tmp.contains("imdb.de/title/tt")) {
								// If File contains an imdb url than get it out
								if (tmp.contains("imdb.com/title/tt"))
									tmp = tmp.substring(tmp.indexOf("imdb.com/title/tt") + 17);
								else
									tmp = tmp.substring(tmp.indexOf("imdb.de/title/tt") + 16);

								// Search for a 6 to 8 digits long number (normally 7 digits is used in the url)
								Pattern p = Pattern.compile("[\\d]{6,8}");
								Matcher m = p.matcher(tmp);

								if (m.find()) {
									br.close();
									return m.group();
								}
							}
							tmp = br.readLine();
						}
						br.close();
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			log.debug("No nfo/txt file found for parsing");
		}
		catch (IOException e) {
			log.debug("I/O error while processing nfo/txt files");
		}

		return null;
	}
	
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public ImportExportReturn executeCommandGetIMDBInfoMultiMovies(String imdbId, String searchString, String filename, ImdbImportOption multiAddSelectOption, String addToThisList) {

		System.err.println("fileimporthandler execute IMDb");
		
		ImportExportReturn ret = ImportExportReturn.success;
		
		/* Checks the movie title... */
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
		if (!searchString.equals("")) { //$NON-NLS-1$
			System.err.println("IMDb - " + searchString);
			System.err.println("filename - " + filename);
			
			DialogIMDbMultiAdd dialogIMDB = new DialogIMDbMultiAdd(imdbId, movieInfoModel.model, searchString, filename, movieInfoModel.getMultiAddFile(), multiAddSelectOption, addToThisList, true);
			//cancel = dialogIMDB.getCanceled();
			//cancelAll = dialogIMDB.getAborted();
			if (dialogIMDB.getDropIMDbInfo())
				ret = ImportExportReturn.skipIMDbInfo;

			System.err.println("canceled:" + dialogIMDB.getCanceled());
			System.err.println("aborted:" + dialogIMDB.getAborted());
			
			if (dialogIMDB.getCanceled()) {
				setCancelled(true);
				ret = ImportExportReturn.cancelled;
			}

			if (dialogIMDB.getAborted()) {
				setAborted(true);
				ret = ImportExportReturn.aborted;
			}

			//addToThisList.clear();

			//if (dialogIMDB.getDropIMDbInfo())
			//	addToThisList.add(settings.skippedListName);
			//else
			//addToThisList.add(settings.addToThisList);

		} else {
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieInfo.alert.title.alert"), Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtil.showAndWait(alert, true);
		}
		
		return ret;
	}
	
	/**
     * Gets the IMDB info for movies (multiAdd)
     **/
    public ImportExportReturn executeCommandGetIMDBInfoMultiMovies(String searchString, ModelImportExportSettings settings, ModelMovie model) {
      	
    	ImportExportReturn ret = ImportExportReturn.success;
    	
    	System.err.println("fileimporthandler wong");
    	
        /* Checks the movie title... */
        log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
        
        if (!searchString.equals("")) { //$NON-NLS-1$
        	DialogIMDbMultiAdd dialogIMDB = new DialogIMDbMultiAdd(model, searchString, settings.multiAddIMDbSelectOption, null);
             
            if (dialogIMDB.getCanceled()) {
            	setCancelled(true);
            	ret = ImportExportReturn.cancelled;
            }
            
            if (dialogIMDB.getAborted()) {
            	setAborted(true);
            	ret = ImportExportReturn.aborted;
            }
            
            addToThisList.clear();
            
            if (dialogIMDB.getDropIMDbInfo())
            	addToThisList.add(settings.skippedListName);
            else
            	addToThisList.add(settings.addToThisList);
                        
        } else {
            DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieInfo.alert.title.alert"), Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        return ret;
    }
	
	/**
	 * Invoked when an action occurs.
	 **/
	/*
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: "+ event.getActionCommand());
		execute();
	}
*/
}
