package net.sf.xmm.moviemanager.commands.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.gui.DialogIMDbMultiAdd;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
import net.sf.xmm.moviemanager.imdblib.IMDb;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelFileImportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandImportMoviesByFile extends MovieManagerCommandImportHandler {

	ModelFileImportSettings fileSettings;
	
	HashMap<String, ModelEntry> existingMediaFiles;
	HashMap<String, ModelEntry> existingMediaFileNames;
		
	public MovieManagerCommandImportMoviesByFile(ModelImportExportSettings settings) {
		super(settings);
		fileSettings = (ModelFileImportSettings) settings;
		
		existingMediaFileNames = fileSettings.existingMediaFileNames;
		existingMediaFiles = fileSettings.existingMediaFiles;
	}
	
	Logger log = Logger.getLogger(getClass());

	String[] stringFiles;
	
	ModelMovieInfo movieInfoModel = null;

	ArrayList<String> moviesToAdd;
	IMDb imdb;
	
	//ArrayList<DialogAddMultipleMovies.Files> fileList = null;
	
	ArrayList<MultiAddMovie> workingList = null;
	
	class MultiAddMovie {
		DialogAddMultipleMovies.Files files;
		ModelMovie model = new ModelMovie();
		ArrayList<ModelIMDbSearchHit> hits;
		int chosenIMDbHit = -1;
		boolean hasIMDbInfo = false;
		String searchString;
	}
	
	
	MovieManagerCommandImportMoviesByFile importProcess = this;
	
	public int getMovieListSize() throws Exception {
		return workingList.size();
	}
		
	public void retrieveMovieList() throws Exception {
		
		//fileList = fileSettings.fileList;
		movieInfoModel = new ModelMovieInfo(false, true);
		movieInfoModel.setSaveCover(true);
		workingList = new ArrayList<MultiAddMovie>();
		
		for (int i = 0; i < fileSettings.fileList.size(); i++) {
			MultiAddMovie tmp = new MultiAddMovie();
			tmp.files = fileSettings.fileList.get(i);
			workingList.add(tmp);
		}
		
		// Initiate IMDb search
		startIMDbSearch();
	}
	
	void startIMDbSearch() {
		Thread t = new Thread() {
			public void run() {
				for (int i = 0; i < workingList.size(); i++) {
					if (isAborted()) {
						break;
					}
					String searchString = performTitleModifications(workingList.get(i));
					performIMDbSearch(workingList.get(i), searchString);
				}
			}			
		};
		t.start();
	}
		
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public ImportExportReturn performIMDbSearch(MultiAddMovie multiAddMovie, String searchString) {
	
		ImportExportReturn ret = ImportExportReturn.success;
				
		/* Checks the movie title... */
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$

		try {

			int hitCount = -1;

			// If "Select FirstHit" is selected and IMDB Id was found in an nfo/txt file
			if (fileSettings.multiAddSelectOption == ImdbImportOption.selectFirst && multiAddMovie.model.getUrlKey() != null) {
				DialogIMDB.getIMDbInfo(multiAddMovie.model, multiAddMovie.model.getUrlKey());
				multiAddMovie.hasIMDbInfo = true;
			}
			else {
				if (imdb == null)
					imdb = IMDbLib.newIMDb(MovieManager.getConfig().getHttpSettings());
				
				final ArrayList<ModelIMDbSearchHit> hits = imdb.getSimpleMatches(searchString);
				multiAddMovie.hits = hits;
				
				/*Number of movie hits*/
				hitCount = hits.size();

				if ((hitCount > 0 && fileSettings.multiAddSelectOption == ImdbImportOption.selectFirst) || 
						hitCount == 1 && fileSettings.multiAddSelectOption == ImdbImportOption.selectIfOnlyOneHit) {

					DialogIMDB.getIMDbInfo(multiAddMovie.model, hits.get(0).getUrlID());
					multiAddMovie.hasIMDbInfo = true;
					
					// Insert prefix in Title to show that these movies maybe got wrong imdb infos
					if (MovieManager.getConfig().getMultiAddPrefixMovieTitle() && hitCount > 1 && 
							fileSettings.multiAddSelectOption == ImdbImportOption.selectFirst && (multiAddMovie.model.getUrlKey() == null))
						multiAddMovie.model.setTitle("_verify_ " + multiAddMovie.model.getTitle()); //$NON-NLS-1$
				}				
			}
		} catch (Exception e) {
			log.debug("Exception:" + e.getMessage(), e);
		}
		return ret;
	}
	
	public String performTitleModifications(MultiAddMovie model) {

		if (isAlreadyInDatabase(model.files)) {
			
			ArrayList<Files> f = model.files.getFiles();
			String [] fString = new String[f.size()];
			
			for (int y = 0; y < f.size(); y++)
				fString[y] = f.get(y).getFile().getAbsolutePath();
			
			return getTitleWithMediaFiles("One or more files are already in the database:", fString);
		}
		
		File [] tempFile = new File[1];

		String searchString = null; // Used to search on imdb 
		String searchTitle = null; // Title of the IMDb dialog
		String path = null; // Path of the file 
		String imdbId = null;

		DialogAddMultipleMovies.Files fileNode = (DialogAddMultipleMovies.Files) model.files;
		ArrayList<Files> files = fileNode.getFiles();

		tempFile = new File[files.size()];

		for (int u = 0; u < tempFile.length; u++) {
			tempFile[u] = files.get(u).getFile();
		}				

		searchString = tempFile[0].getName();
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

		
		// Sets up search string as Folder name or file name 
		if (fileSettings.enableUseFolderName) {

			path = tempFile[0].getPath();
			int slash = path.lastIndexOf(File.separator);

			if (slash == -1) {
				searchString = path;
			}
			else {
				path = path.substring(0, slash);
				slash = path.lastIndexOf(File.separator);

				if (slash == -1) {
					searchString = path;
				}
				else {
					String temp = path.substring(slash+1);
					if (fileSettings.enableUseParentFolderIfCD && temp.toLowerCase().startsWith("cd")) {
						// IF last directory is CD* than the name is in the directory above.
						path = path.substring(0, slash);
						slash = path.lastIndexOf(File.separator);
					}
					searchString = path.substring(slash+1);
				}
			}
		}

		String year = null;
				
		String [] extensions = new String[] {"avi", "mkv", "mpg", "mpeg", "mpe", "divx", "mp4", "ogm", "ogv", "ogg", "flv", "rm", "swf", "vob", "wmv", "asf"};
				
		// Remove file extension
		searchString = StringUtil.removeExtension(searchString, extensions);
		
		if (fileSettings.enableExludeYear) {
			String [] year2 = new String[1];
			searchString = StringUtil.removeYearAndAllAfter(searchString, year2);
			year = year2[0];
		}
		else {
			// only find the year
			String [] year2 = new String[1];
			StringUtil.removeYearAndAllAfter(searchString, year2);
			year = year2[0];
		}
		
		if (fileSettings.enableExludeParantheses) {
			searchString = StringUtil.performExcludeParantheses(searchString, false);
		}

		if (fileSettings.enableExludeAllAfterMatchOnUserDefinedInfo) {
			String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();

			if (!info.equals("")) {
				Pattern p = Pattern.compile("[,]");
				String[] excludeStrings = p.split(info);
				searchString = StringUtil.performExcludeUserdefinedInfo(searchString, excludeStrings);
			}
		}
		else if (fileSettings.enableExludeUserdefinedInfo) {
			String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();

			if (!info.equals("")) {
				Pattern p = Pattern.compile("[,]");
				String[] excludeStrings = p.split(info);
				searchString = StringUtil.performExcludeStrings(searchString, excludeStrings);
			}
		}

		if (fileSettings.enableExludeCDNotations) {
			searchString = StringUtil.performExcludeCDNotations(searchString);
		}
		
		if (fileSettings.enableExludeIntegers) {
			searchString = StringUtil.performExcludeIntegers(searchString);
		}
		
		if (fileSettings.enableSearchNfoForImdb) {
			imdbId = searchNfoForImdb(path);
			model.model.setUrlKey(imdbId);
		}
				
		//removes dots, double spaces, underscore...
		searchString = StringUtil.removeVarious(searchString);
		model.searchString = searchString;
		model.model.setDate(year);
		model.model.setTitle(searchTitle);
		return searchString;
	}
			
	
	boolean isAlreadyInDatabase(Files files) {

		// Check if file is in database
		if (MovieManager.getConfig().getMultiAddFilterOutDuplicates() || 
				MovieManager.getConfig().getMultiAddFilterOutDuplicatesByAbsolutePath()) {
			ArrayList<Files> fileList = files.getFiles();

			for (int u = 0; u < fileList.size(); u++) {

				File file = fileList.get(u).getFile();
				
				if (MovieManager.getConfig().getMultiAddFilterOutDuplicatesByAbsolutePath()) {
					if (existingMediaFiles.containsKey(file.getAbsolutePath())) {
						return true;
					}
				}
				else if (MovieManager.getConfig().getMultiAddFilterOutDuplicates()) {
					if (existingMediaFileNames.containsKey(file.getName())) {
						return true;
					}
				}
			}		
		}
		
		return false;
	}
	
	
	public String getTitle(final int i) throws Exception {

		
		// Reset canceled status
		resetStatus();
		
		MultiAddMovie model = workingList.get(i);

		System.err.println("getTitle("+i+"):" + model.files);
		System.err.println("getIMDbInfo:" + model.searchString);
		System.err.println("hasIMDbInfo:" + model.hasIMDbInfo);
		
		// Already has IMDb info
		if (model.hasIMDbInfo)
			return model.files.getName();
			
		
		// Search not yet performed
		while (model.hits == null) {
			System.err.println("No search hits, waiting");
			Thread.sleep(500);
		}
				
		ImportExportReturn ret = getIMDbInfo(model, model.searchString, workingList.get(i).files, 
																	  fileSettings.addToThisList);
		System.err.println("ret:" + ret);
		
		if (ret == ImportExportReturn.success) {
			String title = movieInfoModel.model.getTitle();
			String [] fileLoc = movieInfoModel.model.getAdditionalInfo().getFileLocationAsArray();
			System.err.println("fileLoc:" + fileLoc);
			return getTitleWithMediaFiles(title, fileLoc);
		}
			
		return workingList.get(i).files.getName();
	}
	
	
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	
	public ImportExportReturn getIMDbInfo(final MultiAddMovie multiAddModel, final String searchString,	final Files files, 
			final String addToThisList) {
		
		ImportExportReturn ret = ImportExportReturn.success;
		
		// Checks the movie title... 
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
		
		try {
			if (multiAddModel.searchString == null || multiAddModel.searchString.equals("")) { //$NON-NLS-1$
				System.err.println("EMPTY SEARCHSTRING:" + files.toString());
				GUIUtil.invokeAndWait(new Runnable() {
					public void run() {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieInfo.alert.title.alert"), Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
				});
				return ret;
			}
			
			int hitCount = -1;
			final ArrayList<ModelIMDbSearchHit> hits = multiAddModel.hits;

			GUIUtil.invokeAndWait(new Runnable() {
				public void run() {
					DialogIMDbMultiAdd dialogIMDB = new DialogIMDbMultiAdd(fileSettings.getParent(), 
							multiAddModel.model, multiAddModel.searchString, 
							multiAddModel.model.getDate(), multiAddModel.model.getTitle(), 
							files, multiAddModel.model.getUrlKey(), multiAddModel.hits, imdb);
					GUIUtil.showAndWait(dialogIMDB, true);

					if (dialogIMDB.getCanceled()) {
						setCancelled(true);
					}

					if (dialogIMDB.getAborted()) {
						setAborted(true);
					}
				}
			});
		} catch (Exception e) {
			log.debug("Exception:" + e.getMessage(), e);
		}
		return ret;
	}
	
	
	String getTitleWithMediaFiles(String title, String [] files) {
		for (int u = 0; u < files.length; u++) {
			title += SysUtil.getLineSeparator() + "      " +  files[u];
		}
		return title;
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
			
			final ModelEntry model = movieInfoModel.saveToDatabase(list);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MovieManagerCommandSelect.executeAndReload(model, false, false, true);
				}
			});
			
			String [] fileLoc = model.getAdditionalInfo().getFileLocationAsArray();
			
			for (int y = 0; y < fileLoc.length; y++) {
				existingMediaFiles.put(fileLoc[y], model);
				existingMediaFileNames.put(new File(fileLoc[y]).getName(), model);
			}
			
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
			ret = ImportExportReturn.error;
		}

		movieInfoModel.model.setTitle("");
				
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
}
