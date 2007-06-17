/**
 * @(#)MovieManagerConfig.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.models.AdditionalInfoFieldDefaultValues;
import net.sf.xmm.moviemanager.util.*;

import org.apache.log4j.Logger;

import java.awt.Dimension;
import java.awt.Point;
import java.io.*;
import java.net.URL;
import java.util.*;

public class MovieManagerConfig implements NewDatabaseLoadedEventListener {

	static Logger log = Logger.getRootLogger();

	/**
	 * Keeps track of the last directory open...(Moviefiles)
	 **/
	private static File lastFileDir;
	private static File lastDVDDir;

	private static File lastExportAndImportDir;

	private static File lastCoversDir;

	/**
	 * Keeps track of the last directory open...(Databasefiles)
	 **/
	private static File lastDatabaseDir;

	/**
	 * Keeps track of the last directory open...(Misc)
	 **/
	private static File lastMiscDir;

	/**
	 * Database file path
	 **/
	private String databasePath;

	private String coversFolder = "";

	private String queriesFolder = "";

	private boolean displayPlayButton = false;
	private boolean displayPrintButton = false;

	private boolean regularToolBarButtons = true;

	private boolean useRegularSeenIcon = true;

	private boolean useJTreeIcons = true;

	private boolean useJTreeCovers = false;

	private int movieListRowHeight = 22;


	final private int defaultFrameHeight = 635;
	final private int defaultFrameWidth = 808;


	/* Main window size */
	public Dimension mainSize = new Dimension(defaultFrameWidth, defaultFrameHeight);

	private Point screenLocation;

	private boolean mainMaximized = false;

	// private int movieListWidth = 0;

	private boolean movieListHighlightEntireRow = true;

	private boolean enableCtrlMouseRightClick = false;

	/* 0 == No, 1 == Preserve all, 2 == Preserve episode covers only */
	private int preserveCoverAspectRatioSetting = 2;

	private String playerPath = "";
	private String browserPath = "";

	private boolean useDefaultWindowsPlayer = true;

	private String systemWebBrowser = MovieManager.getDefaultPlatformBrowser();

	private int lastPreferencesTabIndex = 0;

	/* 0 = no, 1 = yes if no java parser avaliable, 2 = yes */
	private int useMediaInfoDLL = 1;

	/* Used only with MySQL database */
	private boolean storeCoversLocally = false;

	/**
	 * Desides what kind of filter the movie search will use.
	 **/
	private String filterCategory = "Movie Title";

	/* Only applies if filterCategory == "Movie Title" */
	private boolean includeAkaTitlesInFilter = true;

	/**
	 * Desides what kind of filter the movie search will use.
	 **/
	private String sortOption = "Title";

	/**
       Tells if the filter should filter out seen/unseen movies.
       0 means the seen is disabled (and seenButton selected),
       1 means the seen is disabled (and unseenButton selected),
       2 means show only seen,
       3 means show only unseen
	 **/
	private int filterSeen = 0;

	/**
       0 means the rating is disabled (and ratingAboveButton selected),
       1 means the rating is disabled (and ratingBelowButton selected),
       2 means show only above the ratingValue,
       3 means show only below the ratingValue.
       ratingValue == value from JComboBox
	 **/
	private int ratingOption = 0;

	/*from 1-10, the actual movie rating value*/
	private double ratingValue = 5;

	/**
       0 means the date is disabled (and dateAboveButton selected),
       1 means the date is disabled (and dateBelowButton selected),
       2 means show only above the dateValue,
       3 means show only below the dateValue.
       dateOption == value from JComboBox
	 **/
	private int dateOption = 0;

	/*Any string, for instance 1990*/
	private String dateValue = "";

	private HashMap searchAlias = new HashMap();

	private boolean loadDatabaseOnStartup = true;

	private boolean loadLastUsedListAtStartup = false;
	private boolean seenEditableInMainWindow = false;

	private boolean autoMoveThe = false;
	private boolean autoMoveAnAndA = false;

	private String titleLanguageCode = "";

	private boolean storeAllAkaTitles = false;
	private boolean includeAkaLanguageCodes = false;
	private boolean useLanguageSpecificTitle = false;

	private String multiAddDirectoryPath = "";
	private String multiAddExcludeString = "";
	private boolean multiAddExcludeStringEnabled = false;
	private int multiAddSelectOption = 0;
	private boolean multiAddEnableExludeParantheses;
	private boolean multiAddEnableExludeCDNotation;
	private boolean multiAddEnableExludeIntegers;
	private boolean multiAddEnableExludeCodecInfo;
	private boolean multiAddEnableSearchInSubdirectories;
	private boolean multiAddTitleOption;
	
		
	/* Import */
	private int lastDialogImportType = 0;
	
	private String importTextfilePath = "";
	private String importExcelfilePath = "";
	private String importXMLfilePath = "";
	private String importCSVfilePath = "";
	private String importCSVseparator = ",";
	private String importExtremefilePath = "";
	
	private String lastFileFilterMovieInfoUsed = "";

	/*Proxy settings*/
	private String proxyType = "HTTP";
	private String proxyHost = "";
	private String proxyPort = "8080";
	private String proxyUser = "";
	private String proxyPassword = "";
	private boolean proxyEnabled = false;
	private boolean authenticationEnabled = false;

	/*Export*/
	private String exportType = "simple";

	/*icons*/
	private String nocover = "nocover_puma.png";

	/* Current list */
	protected String currentList = "Show All";

	protected boolean multiAddListEnabled = false;

	private int useRelativeDatabasePath = 0;
	private int useRelativeCoversPath = 1;  // 0 == absolute, 1 == database, 2 == program location 
	private int useRelativeQueriesPath = 1; // 0 == absolute, 1 == database, 2 == program location 

	private String coversPath;
	private String queriesPath;

	private boolean databasePathPermanent = false;

	private boolean useDisplayQueriesInTree = true;

	private String multipleAddList = "";

	/* Movie Info SplitPane */
	public int movieInfoSliderPosition = -1;
	public int movieInfoLastSliderPosition = -1;

	/* Additional Info / Notes SplitPane */
	public int additionalInfoNotesSliderPosition = -1;
	public int additionalInfoNotesLastSliderPosition = -1;

	/* Stores default values for additional info fields, key == fieldName */
	private HashMap additionalInfoDefaultValues = new HashMap();


	private String lookAndFeel = "";

	private String skinlfThemePack = "";

	private String oyoahaThemePack = "";

	private String skinlfThemePackDir;

	private String oyoahaThemePackDir;

	private int lookAndFeelType; /*0 = custom, 1 = skinlf, 2 = oyoaha*/

	public int numberOfLookAndFeels;

	private boolean defaultLookAndFeelDecorated = false;

	MovieManagerConfig() {
		MovieManager.newDbHandler.addNewDatabaseLoadedEventListener(this);
	}

	public void newDatabaseLoadedEvent(NewDatabaseLoadedEvent evt) {
		resetCoverAndQueries();
	}

	public boolean isWritable(File dir) {

		if (dir == null)
			return false;

		File testDir = new File(dir.getAbsolutePath() + File.separator +"MedsMovieManagerTestDir"+ Math.ceil(Math.random()));
		if (testDir.mkdir()) {
			testDir.delete();
			return true;
		}
		return false;
	}




	public String getLastFileFilterUsed() {
		return lastFileFilterMovieInfoUsed;
	}

	public void setLastFileFilterUsed(String lastFileFilterMovieInfoUsed) {
		this.lastFileFilterMovieInfoUsed = lastFileFilterMovieInfoUsed;
	}

	/**
	 * Returns the last fileChooser directory. Since the File.exists() method
	 * produces a requst to insert disc into cd-rom (at least on Win2k) a
	 * test to see if the directory is writable is done to avoid the error.
	 *
	 * @return The last fileChooser directory.
	 **/
	public File getLastFileDir() {

		if (lastFileDir == null)
			return null;

		try {

			if (FileUtil.isWindows()) {
				String tmp = lastFileDir.getAbsolutePath();

				if (tmp.indexOf(":") != -1) {
					String drive = tmp.substring(0, tmp.indexOf(":") + 1);

					if (drive.length() != 0) {
						DriveInfo d = new DriveInfo(drive);
						
						if (d.isInitialized() && d.isValid() && lastFileDir.exists() ) {
							return lastFileDir;
						}
					}
				}
			}
			else if (lastFileDir.exists()) {
				return lastFileDir;
			}

		} catch (UnsatisfiedLinkError e) {
			log.warn("Exception:" + e.getMessage());
		}
		return new File("");
	}


	/**
	 * Sets the current directory.
	 *
	 * @param A directory.
	 **/
	public void setLastFileDir(File directory) {
		lastFileDir = directory;
	}

	/* If the display name is empty, it's probably an empty removable device */
	public File getLastDVDDir() {

		String displayName = FileUtil.getDriveDisplayName(lastDVDDir);

		displayName = MovieManagerCommandAddMultipleMoviesByFile.performExcludeParantheses(displayName, false);

		if (displayName != null) {

			if (!displayName.equals(""))
				return lastDVDDir;
			else
				new File("");
		}
		return lastDVDDir;
	}


	public void setLastDVDDir(File directory) {
		lastDVDDir = directory;
	}

	public File getLastExportAndImportDir() {
		return lastExportAndImportDir;
	}

	public void setLastExportAndImportDir(File directory) {
		lastExportAndImportDir = directory;
	}

	public File getLastCoversDir() {
		return lastCoversDir;
	}

	public void setLastCoversDir(File directory) {
		lastCoversDir = directory;
	}


	public File getLastDatabaseDir() {
		return lastDatabaseDir;
	}

	public void setLastDatabaseDir(File directory) {
		lastDatabaseDir = directory;
	}

	public File getLastMiscDir() {
		return lastMiscDir;
	}

	public void setLastMiscDir(File directory) {
		lastMiscDir = directory;
	}


	public void setCoverAndQueriesPaths(String coversPath, String queriesPath) {

		this.queriesFolder = "";
		this.coversFolder = "";

		this.coversPath = coversPath;
		this.queriesPath = queriesPath;
		MovieManager.getIt().getDatabase().setFolders(coversPath, queriesPath);

	}

	public void updateCoverAndQueriesPaths(String coversPath, String queriesPath) {
		this.coversPath = coversPath;
		this.queriesPath = queriesPath;
	}


	public void resetCoverAndQueries() {
		coversPath = "";
		queriesPath = "";
		coversFolder = "";
		queriesFolder = "";
	}

	public String getCoversFolder() {
		return getCoversFolder(null);
	}


	/* Returns the value stored in the database */
	public String getCoversFolder(Database database) {

		if (database != null) {
			return database.getCoversFolder();
		}

		database = MovieManager.getIt().getDatabase();

		if (this.coversFolder.equals("") && database != null) {
			this.coversFolder = database.getCoversFolder();
		}
		return this.coversFolder;
	}

	public String getCoversPath() {
		return getCoversPath(null);
	}


	/* Returns the absolute cover path */
	public String getCoversPath(Database database) {

		/* Get covers folder from database*/
		String coversFolder = getCoversFolder(database);
		String coversPath = "";

		/* Relative to user dir */
		if (getUseRelativeCoversPath() == 2) {
			coversPath = FileUtil.getUserDir() + File.separator;
		}
		/* Relative to database location */
		else if (getUseRelativeCoversPath() == 1) {
			String dbPath;
			
			if (database != null)
				dbPath = database.getPath();
			else
				dbPath = getDatabasePath(true);
			dbPath = dbPath.substring(0, dbPath.lastIndexOf(FileUtil.getDirSeparator()));
			coversPath = dbPath + File.separator;
		}


		return new File(coversPath + coversFolder).getAbsolutePath();
	}

	public String getQueriesFolder() {
		return getQueriesFolder(null);
	}

	public String getQueriesFolder(Database database) {

		if (database != null) {
			return database.getQueriesFolder();
		}

		database = MovieManager.getIt().getDatabase();

		if (this.queriesFolder.equals("") && database != null) {
			this.queriesFolder = database.getQueriesFolder();
		}
		return this.queriesFolder;
	}

	public String getQueriesPath() {
		return getQueriesPath(null);
	}

	/* Returns the relative queries path */
	public String getQueriesPath(Database database) {

		String queriesFolder = getQueriesFolder(database);

		/* Get queries folder from database*/
		String queriesPath = "";

		/* If relative path is used checks if directory exist after the user dir is added to the beginning */
		if (getUseRelativeQueriesPath() == 2) {
			queriesPath = FileUtil.getUserDir() + File.separator;
		}
		else if (getUseRelativeQueriesPath() == 1) {
			String dbPath;
			
			if (database != null)
				dbPath = database.getPath();
			else
				dbPath = getDatabasePath(true);
				
			dbPath = dbPath.substring(0, dbPath.lastIndexOf(FileUtil.getDirSeparator()));
			queriesPath = dbPath + File.separator;
		}

		return new File(queriesPath + queriesFolder).getAbsolutePath();
	}


	public String getDatabasePath(boolean getPathFromDatabase) {

		/* When loading the database from the config file the path is stored in this.databasePath */
		if (!getPathFromDatabase || MovieManager.getIt().getDatabase() == null) {
			return this.databasePath;
		}
		else {
			return MovieManager.getIt().getDatabase().getPath();
		}
	}

	public void setDatabasePath(String dbPath) {
		databasePath = dbPath;
	}


	public HashMap getSearchAlias() {
		return searchAlias;
	}

	// public void setSearchAlias(HashMap searchAlias) {
//	this.searchAlias = searchAlias;;
//	}


	/* Used by the filter to determine which conditions to filter out movies. */
	/* filterCategory to determine which category to filter by */
	public void setFilterCategory(String filterString) {
		filterCategory = filterString;
	}

	public String getFilterCategory() {
		return filterCategory;
	}

	public void setIncludeAkaTitlesInFilter(boolean includeAkaTitlesInFilter) {
		this.includeAkaTitlesInFilter = includeAkaTitlesInFilter;
	}

	public boolean getIncludeAkaTitlesInFilter() {
		return includeAkaTitlesInFilter;
	}


	/*Used to determine how to sort.*/
	/*sortOption to determine which category to sort by*/
	public void setSortOption(String sortOption) {
		this.sortOption = sortOption;
	}

	public String getSortOption() {
		return sortOption;
	}



	/* filterSeen is to determine if all movies, only seen, or only unseen movies should show up*/
	public void setFilterSeen(int filterSeen) {
		this.filterSeen = filterSeen;
	}

	public int getFilterSeen() {
		return filterSeen;
	}

	public void setRatingOption(int ratingOption) {
		this.ratingOption = ratingOption;
	}

	public int getRatingOption() {
		return ratingOption;
	}

	public void setRatingValue(double ratingValue) {
		this.ratingValue = ratingValue;
	}

	public double getRatingValue() {
		return ratingValue;
	}

	public void setDateOption(int dateOption) {
		this.dateOption = dateOption;
	}

	public int getDateOption() {
		return dateOption;
	}

	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	public String getDateValue() {
		return dateValue;
	}


	public int getUseMediaInfoDLL() {
		return useMediaInfoDLL;
	}

	public void setUseMediaInfoDLL(int useMediaInfoDLL) {
		this.useMediaInfoDLL = useMediaInfoDLL;
	}

	public boolean getStoreCoversLocally() {
		return storeCoversLocally;
	}

	public void setStoreCoversLocally(boolean storeCoversLocally) {
		this.storeCoversLocally = storeCoversLocally;
	}

	public boolean getLoadDatabaseOnStartup() {
		return loadDatabaseOnStartup;
	}

	public void setLoadDatabaseOnStartup(boolean loadDatabaseOnStartup) {
		this.loadDatabaseOnStartup = loadDatabaseOnStartup;
	}

	public boolean getAutoMoveThe() {
		return autoMoveThe;
	}

	public void setAutoMoveThe(boolean autoMoveThe) {
		this.autoMoveThe = autoMoveThe;
	}

	public boolean getAutoMoveAnAndA() {
		return autoMoveAnAndA;
	}

	public void setAutoMoveAnAndA(boolean autoMoveAnAndA) {
		this.autoMoveAnAndA = autoMoveAnAndA;
	}




	public boolean getStoreAllAkaTitles() {
		return storeAllAkaTitles;
	}

	public void setStoreAllAkaTitles(boolean storeAllAkaTitles) {
		this.storeAllAkaTitles = storeAllAkaTitles;
	}

	public boolean getIncludeAkaLanguageCodes() {
		return includeAkaLanguageCodes;
	}

	public void setIncludeAkaLanguageCodes(boolean includeAkaLanguageCodes) {
		this.includeAkaLanguageCodes = includeAkaLanguageCodes;
	}

	public String getTitleLanguageCode() {
		return titleLanguageCode;
	}

	public void setTitleLanguageCode(String titleLanguageCode) {
		this.titleLanguageCode = titleLanguageCode;
	}

	public boolean getUseLanguageSpecificTitle() {
		return useLanguageSpecificTitle;
	}

	public void setUseLanguageSpecificTitle(boolean useLanguageSpecificTitle) {
		this.useLanguageSpecificTitle = useLanguageSpecificTitle;
	}



	public boolean getSeenEditable() {
		return seenEditableInMainWindow;
	}

	public void setSeenEditable(boolean seenEditableInMainWindow) {
		this.seenEditableInMainWindow = seenEditableInMainWindow;
	}

	public int getMovieListRowHeight() {
		return movieListRowHeight;
	}

	public void setMovieListRowHeight(int movieListRowHeight) {
		this.movieListRowHeight = movieListRowHeight;
	}

	public boolean getMovieListHighlightEntireRow() {
		return movieListHighlightEntireRow;
	}

	public void setMovieListHighlightEntireRow(boolean movieListHighlightEntireRow) {
		this.movieListHighlightEntireRow = movieListHighlightEntireRow;
	}

	public boolean getEnableCtrlMouseRightClick() {
		return enableCtrlMouseRightClick;
	}

	public void setEnableCtrlMouseRightClick(boolean enableCtrlMouseRightClick) {
		this.enableCtrlMouseRightClick = enableCtrlMouseRightClick;
	}

	public int getLastDialogImportType() {
		return lastDialogImportType;
	}

	public void setLastDialogImportType(int lastDialogImportType) {
		this.lastDialogImportType = lastDialogImportType;
	}
		
	public String getImportTextfilePath() {
		return importTextfilePath;
	}

	public void setImportTextfilePath(String importTextfilePath) {
		this.importTextfilePath = importTextfilePath;
	}

	public String getImportExcelfilePath() {
		return importExcelfilePath;
	}

	public void setImportExcelfilePath(String importExcelfilePath) {
		this.importExcelfilePath = importExcelfilePath;
	}

	public String getImportXMLfilePath() {
		return importXMLfilePath;
	}

	public void setImportXMLfilePath(String importXMLfilePath) {
		this.importXMLfilePath = importXMLfilePath;
	}

	public String getImportCSVfilePath() {
		return importCSVfilePath;
	}

	public void setImportCSVfilePath(String importCSVfilePath) {
		this.importCSVfilePath = importCSVfilePath;
	}

	public String getImportCSVseparator() {
		return importCSVseparator;
	}

	public void setImportCSVseparator(String importCSVseparator) {
		this.importCSVseparator = importCSVseparator;
	}

	
	public String getImportExtremefilePath() {
		return importExtremefilePath;
	}

	public void setImportExtremefilePath(String importExtremefilePath) {
		this.importExtremefilePath = importExtremefilePath;
	}

	public boolean getMultiAddListEnabled() {
		return multiAddListEnabled;
	}

	public void setMultiAddListEnabled(boolean multiAddListEnabled) {
		this.multiAddListEnabled = multiAddListEnabled;
	}

	public String getMultiAddList() {
		return multipleAddList;
	}

	public void setMultiAddList(String multipleAddList) {
		this.multipleAddList = multipleAddList;
	}

	public boolean getMultiAddExcludeStringEnabled() {
		return multiAddExcludeStringEnabled;
	}

	public void setMultiAddExcludeStringEnabled(boolean b) {
		multiAddExcludeStringEnabled = b;
	}

	public String getMultiAddDirectoryPath() {
		return multiAddDirectoryPath;
	}

	public void setMultiAddDirectoryPath(String p) {
		multiAddDirectoryPath = p;
	}

	public String getMultiAddExcludeString() {
		return multiAddExcludeString;
	}

	public void setMultiAddExcludeString(String e) {
		multiAddExcludeString = e;
	}

	public int getMultiAddSelectOption() {
		return multiAddSelectOption;
	}

	public void setMultiAddSelectOption(int o) {
		multiAddSelectOption = o;
	}

	
	public boolean getMultiAddEnableExludeParantheses() {
		return multiAddEnableExludeParantheses;
	}

	public void setMultiAddEnableExludeParantheses(boolean val) {
		multiAddEnableExludeParantheses = val;
	}
	
	
	public boolean getMultiAddEnableExludeCDNotation() {
		return multiAddEnableExludeCDNotation;
	}

	public void setMultiAddEnableExludeCDNotation(boolean val) {
		multiAddEnableExludeCDNotation = val;
	}
	
	
	public boolean getMultiAddEnableExludeIntegers() {
		return multiAddEnableExludeIntegers;
	}

	public void setMultiAddEnableExludeIntegers(boolean val) {
		multiAddEnableExludeIntegers = val;
	}
	
	
	public boolean getMultiAddEnableExludeCodecInfo() {
		return multiAddEnableExludeCodecInfo;
	}

	public void setMultiAddEnableExludeCodecInfo(boolean val) {
		multiAddEnableExludeCodecInfo = val;
	}
	
	
	public boolean getMultiAddEnableSearchInSubdirectories() {
		return multiAddEnableSearchInSubdirectories;
	}

	public void setMultiAddEnableSearchInSubdirectories(boolean val) {
		multiAddEnableSearchInSubdirectories = val;
	}
	
	
	
	public boolean getMultiAddTitleOption() {
		return multiAddTitleOption;
	}

	public void setMultiAddTitleOption(boolean val) {
		multiAddTitleOption = val;
	}
	
	
	
	
	public boolean getDefaultLookAndFeelDecorated() {
		return defaultLookAndFeelDecorated;
	}

	public void setDefaultLookAndFeelDecorated(boolean defaultLookAndFeelDecorated) {
		this.defaultLookAndFeelDecorated = defaultLookAndFeelDecorated;
	}

	public int getNumberOfLookAndFeels() {
		return numberOfLookAndFeels;
	}

	public String getLookAndFeelString() {
		return lookAndFeel;
	}

	public void setLookAndFeelString(String lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	public String getSkinlfThemePack() {
		return skinlfThemePack;
	}

	public void setSkinlfThemePack(String skinlfThemePack) {
		this.skinlfThemePack = skinlfThemePack;
	}

	public String getSkinlfThemePackDir() {
		return skinlfThemePackDir;
	}

	public void setSkinlfThemePackDir(String skinlfThemePackDir) {
		this.skinlfThemePackDir = skinlfThemePackDir;
	}

	public int getLookAndFeelType() {
		return lookAndFeelType;
	}

	public void setLookAndFeelType(int lafType) {
		this.lookAndFeelType = lafType;
	}

	public String getOyoahaThemePack() {
		return oyoahaThemePack;
	}

	public void setOyoahaThemePack(String oyoahaThemePack) {
		this.oyoahaThemePack = oyoahaThemePack;
	}

	public String getOyoahaThemePackDir() {
		return oyoahaThemePackDir;
	}

	public void setOyoahaThemePackDir(String oyoahaThemePackDir) {
		this.oyoahaThemePackDir = oyoahaThemePackDir;
	}

	public void setDisplayPlayButton(boolean displayPlayButton) {
		this.displayPlayButton = displayPlayButton;
	}

	public boolean getDisplayPlayButton() {
		return displayPlayButton;
	}

	public void setDisplayPrintButton(boolean displayPrintButton) {
		this.displayPrintButton = displayPrintButton;
	}

	public boolean getDisplayPrintButton() {
		return displayPrintButton;
	}


	public void setRegularToolButtonsUsed(boolean regularToolBarButtons) {
		this.regularToolBarButtons = regularToolBarButtons;
	}

	public boolean isRegularToolButtonsUsed() {
		return regularToolBarButtons;
	}

	public void setUseRegularSeenIcon(boolean useRegularSeenIcon) {
		this.useRegularSeenIcon = useRegularSeenIcon;
	}

	public boolean getUseRegularSeenIcon() {
		return useRegularSeenIcon;
	}

	public Point getScreenLocation() {
		return screenLocation;
	}

	public void setScreenLocation(Point screenLocation) {
		this.screenLocation = screenLocation;
	}

	public void setMainSize(Dimension mainSize) {
		this.mainSize = mainSize;
	}

	public boolean getMainMaximized() {
		return mainMaximized;
	}

	public void setMainMaximized(boolean mainMaximized) {
		this.mainMaximized = mainMaximized;
	}

	public String getProxyType() {
		return proxyType;
	}

	public void setProxyType(String proxyType) {
		this.proxyType = proxyType;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public boolean getProxyEnabled() {
		return proxyEnabled;
	}

	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}

	public boolean getAuthenticationEnabled() {
		return authenticationEnabled;
	}

	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}

	public void setUseRelativeDatabasePath(int useRelativeDatabasePath) {
		this.useRelativeDatabasePath = useRelativeDatabasePath;
	}

	public int getUseRelativeDatabasePath() {
		return useRelativeDatabasePath;
	}

	public void setUseRelativeCoversPath(int useRelativeCoversPath) {
		this.useRelativeCoversPath = useRelativeCoversPath;
	}

	public int getUseRelativeCoversPath() {
		return useRelativeCoversPath;
	}

	public void setUseRelativeQueriesPath(int useRelativeQueriesPath) {
		this.useRelativeQueriesPath = useRelativeQueriesPath;
	}

	public int getUseRelativeQueriesPath() {
		return useRelativeQueriesPath;
	}

	public void setDatabasePathPermanent(boolean databasePathPermanent) {
		this.databasePathPermanent = databasePathPermanent;
	}

	public boolean getDatabasePathPermanent() {
		return databasePathPermanent;
	}

	public void setUseDisplayQueriesInTree(boolean useDisplayQueriesInTree) {
		this.useDisplayQueriesInTree = useDisplayQueriesInTree;
	}

	public boolean getDisplayQueriesInTree() {
		return useDisplayQueriesInTree;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public String getNoCover() {
		return nocover;
	}

	public void setNoCover(String nocover) {
		this.nocover = nocover;
	}

	public HashMap getAdditionalInfoDefaultValues() {
		return additionalInfoDefaultValues;
	}

	public String getCurrentList() {
		return currentList;
	}

	public void setCurrentList(String currentList) {
		this.currentList = currentList;
	}

	public boolean getLoadLastUsedListAtStartup() {
		return loadLastUsedListAtStartup;
	}

	public void setLoadLastUsedListAtStartup(boolean loadLastUsedListAtStartup) {
		this.loadLastUsedListAtStartup = loadLastUsedListAtStartup;
	}

	public boolean getUseJTreeIcons() {
		return useJTreeIcons;
	}

	public void setUseJTreeIcons(boolean useJTreeIcons) {
		this.useJTreeIcons = useJTreeIcons;
	}

	public boolean getUseJTreeCovers() {
		return useJTreeCovers;
	}

	public void setUseJTreeCovers(boolean useJTreeCovers) {
		this.useJTreeCovers = useJTreeCovers;
	}

	public int getPreserveCoverAspectRatio() {
		return preserveCoverAspectRatioSetting;
	}

	public void setPreserveCoverAspectRatio(int preserveCoverAspectRatioSetting) {
		this.preserveCoverAspectRatioSetting = preserveCoverAspectRatioSetting;
	}

	public int getLastPreferencesTabIndex() {
		return lastPreferencesTabIndex;
	}

	public void setLastPreferencesTabIndex(int lastPreferencesTabIndex) {
		this.lastPreferencesTabIndex = lastPreferencesTabIndex;
	}

	public String getMediaPlayerPath() {
		return this.playerPath;
	}

	public void setMediaPlayerPath(String playerPath){
		this.playerPath = playerPath;
	}

	public String getBrowserPath() {
		return browserPath;
	}

	public void setBrowserPath(String browserPath){
		this.browserPath = browserPath;
	}



	public boolean getUseDefaultWindowsPlayer() {
		return useDefaultWindowsPlayer;
	}

	public void setUseDefaultWindowsPlayer(boolean useDefaultWindowsPlayer) {
		this.useDefaultWindowsPlayer = useDefaultWindowsPlayer;
	}

	public String getSystemWebBrowser() {
		return systemWebBrowser;
	}

	public void setSystemWebBrowser(String systemWebBrowser) {
		this.systemWebBrowser = systemWebBrowser;
	}


	/**
	 * Loads info from the config file...
	 **/
	 protected void loadConfig() {

		try {

			URL url;

			if (MovieManager.isApplet())
				url = FileUtil.getFileURL("Config_Applet.ini");
			else
				url = FileUtil.getFileURL("Config.ini");


			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			// search Alias And Additional Info Default Values
			ArrayList searchAliasList = new ArrayList();
			ArrayList additionalFieldDefaults = new ArrayList();
			HashMap config = new HashMap();
						
			String tmp, key, value;

			while ((tmp = reader.readLine()) != null) {

				try {
					key = tmp.substring(0, tmp.indexOf(":") + 1);
					value = tmp.substring(tmp.indexOf(":") + 1, tmp.length());

					if ("AdditionalInfoDefaultValues:".equals(key))
						additionalFieldDefaults.add(value);
					else if ("Search Alias:".equals(key))
						searchAliasList.add(value);
					else
						config.put(key, value);

				} catch (Exception e) {
					log.warn("Error in Config file:" + e.getMessage());
				}
			}


			value = (String) config.get("Database:");

			if (value != null) {
				setDatabasePath(value);
			}

			
			value = (String) config.get("useRelativeDatabasePath:");

			if (value != null) {

				if (value.equals("true"))
					setUseRelativeDatabasePath(2);
				else if (value.equals("false"))
					setUseRelativeDatabasePath(0);
				else
					setUseRelativeDatabasePath(Integer.parseInt(value));
			}

			
			value = (String) config.get("useRelativeCoverPath:");

			if (value != null) {
				
				if (value.equals("true"))
					setUseRelativeCoversPath(2);
				else if (value.equals("false"))
					setUseRelativeCoversPath(0);
				else
					setUseRelativeCoversPath(Integer.parseInt(value));
			}


			value = (String) config.get("useRelativeQueriesPath:");

			if (value != null) {

				if (value.equals("true"))
					setUseRelativeQueriesPath(2);
				else if (value.equals("false"))
					setUseRelativeQueriesPath(0);
				else
					setUseRelativeQueriesPath(Integer.parseInt(value));
			}

			
			value = (String) config.get("storeCoversLocally:");

			if (value != null) {
				setStoreCoversLocally(new Boolean(value).booleanValue());
			}
			
			
			value = (String) config.get("loadDatabaseOnStartup:");

			if (value != null) {
				setLoadDatabaseOnStartup(new Boolean(value).booleanValue());
			}


			value = (String) config.get("databasePathPermanent:");

			if (value != null) {
				setDatabasePathPermanent(new Boolean(value).booleanValue());
			}


			value = (String) config.get("useDisplayQueriesInTree:");

			if (value != null) {
				setUseDisplayQueriesInTree(new Boolean(value).booleanValue());
			}


			value = (String) config.get("lookAndFeel:");

			if (value != null) {
				setLookAndFeelString(value);
			}


			value = (String) config.get("skinlfTheme:");

			if (value != null) {
				setSkinlfThemePack(value);
			}


			value = (String) config.get("oyoahaTheme:");

			if (value != null) {
				setOyoahaThemePack(value);
			}


			value = (String) config.get("lookAndFeelType:");

			if (value != null) {
				setLookAndFeelType(Integer.parseInt(value));
			}


			value = (String) config.get("regularToolButtonsUsed:");

			if (value != null) {
				setRegularToolButtonsUsed(new Boolean(value).booleanValue());
			}


			value = (String) config.get("useRegularSeenIcon:");

			if (value != null) {
				setUseRegularSeenIcon(new Boolean(value).booleanValue());
			}


			value = (String) config.get("defaultLookAndFeelDecorated:");

			if (value != null) {
				setDefaultLookAndFeelDecorated(new Boolean(value).booleanValue());
			}


			value = (String) config.get("filterOption:");

			if (value != null) {
				setFilterCategory(value);
			}


			value = (String) config.get("sortOption:");

			if (value != null) {
				setSortOption(value);
			}


			value = (String) config.get("filterSeen:");

			if (value != null) {
				setFilterSeen(Integer.parseInt(value));
			}


			value = (String) config.get("ratingOption:");

			if (value != null) {
				setRatingOption(Integer.parseInt(value));
			}


			value = (String) config.get("ratingValue:");

			if (value != null) {
				setRatingValue(Double.parseDouble(value));
			}


			value = (String) config.get("dateOption:");

			if (value != null) {
				setDateOption(Integer.parseInt(value));
			}


			value = (String) config.get("dateValue:");

			if (value != null) {
				setDateValue(value);
			}


			value = (String) config.get("seenEditableInMainWindow:");

			if (value != null) {
				setSeenEditable(new Boolean(value).booleanValue());
			}


			value = (String) config.get("multiAddDirectoryPath:");

			if (value != null) {
				if (!value.equals("null"))
					setMultiAddDirectoryPath(value);
			}


			value = (String) config.get("multiAddExcludeString:");

			if (value != null) {
				if (!value.equals("null"))
					setMultiAddExcludeString(value);
			}


			value = (String) config.get("multiAddExcludeStringEnabled:");

			if (value != null) {
				setMultiAddExcludeStringEnabled(new Boolean(value).booleanValue());
			}


			value = (String) config.get("multiAddSelectOption:");

			if (value != null) {
				setMultiAddSelectOption(Integer.parseInt(value));
			}


			value = (String) config.get("multiAddEnableExludeParantheses:");

			if (value != null) {
				setMultiAddEnableExludeParantheses(new Boolean(value).booleanValue());
			}
			
			
			value = (String) config.get("multiAddEnableExludeCDNotation:");

			if (value != null) {
				setMultiAddEnableExludeCDNotation(new Boolean(value).booleanValue());
			}
			
			
			value = (String) config.get("multiAddEnableExludeIntegers:");

			if (value != null) {
				setMultiAddEnableExludeIntegers(new Boolean(value).booleanValue());
			}
			
			
			value = (String) config.get("multiAddEnableExludeCodecInfo:");

			if (value != null) {
				setMultiAddEnableExludeCodecInfo(new Boolean(value).booleanValue());
			}
			
			
			value = (String) config.get("multiAddEnableSearchInSubdirectories:");

			if (value != null) {
				setMultiAddEnableSearchInSubdirectories(new Boolean(value).booleanValue());
			}
			
			
			value = (String) config.get("multiAddTitleOption:");

			if (value != null) {
				setMultiAddTitleOption(new Boolean(value).booleanValue());
			}
								
			
			value = (String) config.get("proxyEnabled:");

			if (value != null) {
				setProxyEnabled(new Boolean(value).booleanValue());
			}


			value = (String) config.get("proxyType:");

			if (value != null) {
				setProxyType(value);
			}


			value = (String) config.get("authenticationEnabled:");

			if (value != null) {
				setAuthenticationEnabled(new Boolean(value).booleanValue());
			}


			value = (String) config.get("proxyHost:");

			if (value != null) {
				setProxyHost(value);
			}


			value = (String) config.get("proxyPort:");

			if (value != null) {
				setProxyPort(value);
			}


			value = (String) config.get("proxyUser:");

			if (value != null) {
				setProxyUser(value);
			}


			value = (String) config.get("proxyPassword:");

			if (value != null) {
				setProxyPassword(value);
			}


			value = (String) config.get("lastFileDir:");

			if (value != null) {
				File lastFileDirctory = new File(value);
				setLastFileDir(lastFileDirctory);
			}


			value = (String) config.get("lastDVDDir:");

			if (value != null) {
				File lastDVDDirctory = new File(value);
				setLastDVDDir(lastDVDDirctory);
			}


			value = (String) config.get("lastDatabaseDir:");

			if (value != null) {
				File lastDatabaseDirctory = new File(value);
				if (lastDatabaseDirctory.exists())
					setLastDatabaseDir(lastDatabaseDirctory);
			}


			value = (String) config.get("lastMiscDir:");

			if (value != null) {
				File lastMiscDirctory = new File(value);
				if (lastMiscDirctory.exists())
					setLastMiscDir(lastMiscDirctory);
			}


			value = (String) config.get("lastFileFilterMovieInfoUsed:");

			if (value != null) {
				setLastFileFilterUsed(value);
			}


			value = (String) config.get("exportType:");

			if (value != null) {
				setExportType(value);
			}


			value = (String) config.get("movieInfoSliderPosition:");

			if (value != null) {
				movieInfoSliderPosition = Integer.parseInt(value);
			}


			value = (String) config.get("movieInfoLastSliderPosition:");

			if (value != null) {
				movieInfoLastSliderPosition = Integer.parseInt(value);
			}


			value = (String) config.get("addionalInfoNotesSliderPosition:");

			if (value != null) {
				additionalInfoNotesSliderPosition = Integer.parseInt(value);
			}


			value = (String) config.get("addionalInfoNotesLastSliderPosition:");

			if (value != null) {
				additionalInfoNotesLastSliderPosition = Integer.parseInt(value);
			}


			value = (String) config.get("mainWidth:");
			int mainWidth = 0;

			if (value != null) {
				mainWidth = Integer.parseInt(value);
			}


			value = (String) config.get("mainHeight:");
			int mainHeight = 0;

			if (value != null) {
				mainHeight = Integer.parseInt(value);
			}

			mainSize = new Dimension(mainWidth, mainHeight);


			value = (String) config.get("screenLocationX:");
			int screenLocationX = 0;

			if (value != null) {
				screenLocationX = Integer.parseInt(value);
			}


			value = (String) config.get("screenLocationY:");
			int screenLocationY = 0;

			if (value != null) {
				screenLocationY = Integer.parseInt(value);
			}

			if (screenLocationX > 0 && screenLocationY > 0)
				screenLocation = new Point(screenLocationX, screenLocationY);


			value = (String) config.get("mainMaximized:");

			if (value != null) {
				setMainMaximized(new Boolean(value).booleanValue());
			}


			value = (String) config.get("currentList:");

			if (value != null) {
				setCurrentList(value);
			}


			value = (String) config.get("loadCurrentListAtStartup:");

			if (value != null) {
				setLoadLastUsedListAtStartup(new Boolean(value).booleanValue());
			}


			value = (String) config.get("multiAddList:");

			if (value != null) {
				setMultiAddList(value);
			}


			value = (String) config.get("multiAddListEnabled:");

			if (value != null) {
				setMultiAddListEnabled(new Boolean(value).booleanValue());
			}

			
			value = (String) config.get("lastDialogImportType:");

			if (value != null) {
				setLastDialogImportType(Integer.parseInt(value));
			}

			
			value = (String) config.get("importTextfilePath:");

			if (value != null) {
				setImportTextfilePath(value);
			}


			value = (String) config.get("importExcelfilePath:");

			if (value != null) {
				setImportExcelfilePath(value);
			}

			value = (String) config.get("importXMLfilePath:");

			if (value != null) {
				setImportXMLfilePath(value);
			}

			
			value = (String) config.get("importCSVfilePath:");

			if (value != null) {
				setImportCSVfilePath(value);
			}

			value = (String) config.get("importCSVseparator:");

			if (value != null) {
				setImportCSVseparator(value);
			}

			
			value = (String) config.get("importExtremefilePath:");

			if (value != null) {
				setImportExtremefilePath(value);
			}


			value = (String) config.get("useJTreeIcons:");

			if (value != null) {
				setUseJTreeIcons(new Boolean(value).booleanValue());
			}


			value = (String) config.get("useJTreeCovers:");

			if (value != null) {
				setUseJTreeCovers(new Boolean(value).booleanValue());
			}


			value = (String) config.get("lastPreferencesTabIndex:");

			if (value != null) {
				if (Character.isDigit(value.charAt(0)))
					setLastPreferencesTabIndex(Character.digit(value.charAt(0), 10));
			}


			value = (String) config.get("movieListRowHeight:");

			if (value != null) {
				setMovieListRowHeight(Integer.parseInt(value));
			}


			value = (String) config.get("movieListHighlightEntireRow:");

			if (value != null) {
				movieListHighlightEntireRow = new Boolean(value).booleanValue();
			}

			value = (String) config.get("enableCtrlMouseRightClick:");

			if (value != null) {
				enableCtrlMouseRightClick = new Boolean(value).booleanValue();
			}


			value = (String) config.get("preserveCoverAspectRatioSetting:");

			if (value != null) {
				
				if (value.equals("0"))
					preserveCoverAspectRatioSetting = 0;
				else if (value.equals("1"))
					preserveCoverAspectRatioSetting = 1;
				else if (value.equals("2"))
					preserveCoverAspectRatioSetting = 2;
			}


			value = (String) config.get("noCover:");

			if (value != null) {
				
				if (value.equals("nocover_puma.png"))
					setNoCover(value);
				else if (value.equals("nocover_jaguar.png"))
					setNoCover(value);
			}


			value = (String) config.get("autoMoveThe:");

			if (value != null) {
				setAutoMoveThe(new Boolean(value).booleanValue());
			}


			value = (String) config.get("autoMoveAnAndA:");

			if (value != null) {
				setAutoMoveAnAndA(new Boolean(value).booleanValue());
			}


			value = (String) config.get("storeAllAkaTitles:");

			if (value != null) {
				setStoreAllAkaTitles(new Boolean(value).booleanValue());
			}

			
			value = (String) config.get("includeAkaLanguageCodes:");

			if (value != null) {
				setIncludeAkaLanguageCodes(new Boolean(value).booleanValue());
			}


			value = (String) config.get("useLanguageSpecificTitle:");

			if (value != null) {
				setUseLanguageSpecificTitle(new Boolean(value).booleanValue());
			}


			value = (String) config.get("titleLanguageCode:");

			if (value != null) {
				setTitleLanguageCode(value);
			}


			value = (String) config.get("useMediaInfoDLL:");

			if (value != null) {
				if (!value.equals("")) {
					setUseMediaInfoDLL(Integer.parseInt(value));
				}
			}


			value = (String) config.get("playerPath:");

			if (value != null) {
				setMediaPlayerPath(value);
			}


			value = (String) config.get("browserPath:");

			if (value != null) {
				setBrowserPath(value);
			}


			value = (String) config.get("useDefaultWindowsPlayer:");

			if (value != null) {
				setUseDefaultWindowsPlayer(new Boolean(value).booleanValue());
			}


			value = (String) config.get("systemWebBrowser:");

			if (value != null) {
				setSystemWebBrowser(value);
			}


			value = (String) config.get("displayPlayButton:");

			if (value != null) {
				setDisplayPlayButton(new Boolean(value).booleanValue());
			}


			value = (String) config.get("displayPrintButton:");

			if (value != null) {
				setDisplayPrintButton(new Boolean(value).booleanValue());
			}

						AdditionalInfoFieldDefaultValues defaultValue;
			StringTokenizer tokenizer;
			String name = "";
			String token;

			for (int i = 0; i < additionalFieldDefaults.size(); i++) {
			
				tmp = (String) additionalFieldDefaults.get(i);
				name = tmp.substring(0, tmp.indexOf(":"));
				
				defaultValue = new AdditionalInfoFieldDefaultValues(name);

				tokenizer = new StringTokenizer(tmp.substring(tmp.indexOf(":") + 1, tmp.length()), "|");

				while (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					defaultValue.addValue(token);					
				}
				additionalInfoDefaultValues.put(name, defaultValue);

			}

			String tableAndColumn;
			String alias;
			
			for (int i = 0; i < searchAliasList.size(); i++) {

				tmp = (String) searchAliasList.get(i);
				tableAndColumn = tmp.substring(0, tmp.indexOf("="));
				alias = tmp.substring(tmp.indexOf("=") + 1, tmp.length());
				
				if (!alias.equals("") && !tableAndColumn.equals(""))
					searchAlias.put(tableAndColumn, alias);
			}
				
		} catch (Exception e) {
			log.warn("Cannot find config file." + e.getMessage(), e);
		}
	 }


	 public void saveConfig() {

		 StringBuffer settings = new StringBuffer(1500);
		 String lineSeparator = FileUtil.getLineSeparator();
		 Database database = MovieManager.getIt().getDatabase();

		 /* Absort if Applet */
		 if (MovieManager.isApplet())
			 return;

		 String dbPath = "";

		 /* Verifies if the database is initialized... */
		 if (database == null || getDatabasePathPermanent()) {
			 dbPath = getDatabasePath(false);
		 }
		 else if (database != null) {

			 String databaseType = database.getDatabaseType();
			 dbPath = getDatabasePath(true);

			 if (getUseRelativeDatabasePath() == 2) {
				 if (dbPath.indexOf(FileUtil.getUserDir()) != -1)
					 dbPath = databaseType + ">" + dbPath.substring(FileUtil.getUserDir().length(), dbPath.length());

			 }
			 else
				 dbPath = databaseType + ">" + dbPath;
		 }

		 if (dbPath != null && !dbPath.equals("")) {
			 settings.append("Database:" + dbPath);

		 }

		 settings.append(lineSeparator);
		 settings.append("useRelativeDatabasePath:" + getUseRelativeDatabasePath());

		 settings.append(lineSeparator);
		 settings.append("storeCoversLocally:" + getStoreCoversLocally());
	 
		 settings.append(lineSeparator);
		 settings.append("loadDatabaseOnStartup:" + getLoadDatabaseOnStartup());

		 settings.append(lineSeparator);
		 settings.append("useRelativeCoverPath:" + getUseRelativeCoversPath());

		 settings.append(lineSeparator);
		 settings.append("useRelativeQueriesPath:" + getUseRelativeQueriesPath());

		 settings.append(lineSeparator);
		 settings.append("useDisplayQueriesInTree:" + getDisplayQueriesInTree());

		 settings.append(lineSeparator);
		 settings.append("databasePathPermanent:" + getDatabasePathPermanent());

		 settings.append(lineSeparator);
		 settings.append("lookAndFeel:" + getLookAndFeelString());

		 settings.append(lineSeparator);
		 settings.append("skinlfTheme:" + getSkinlfThemePack());

		 settings.append(lineSeparator);
		 settings.append("oyoahaTheme:" + getOyoahaThemePack());

		 settings.append(lineSeparator);
		 settings.append("lookAndFeelType:" + getLookAndFeelType());

		 settings.append(lineSeparator);
		 settings.append("regularToolButtonsUsed:" + isRegularToolButtonsUsed());

		 settings.append(lineSeparator);
		 settings.append("useRegularSeenIcon:" + getUseRegularSeenIcon());

		 settings.append(lineSeparator);
		 settings.append("defaultLookAndFeelDecorated:" + getDefaultLookAndFeelDecorated());

		 settings.append(lineSeparator);
		 settings.append("filterOption:" + getFilterCategory());

		 settings.append(lineSeparator);
		 settings.append("sortOption:" + getSortOption());

		 settings.append(lineSeparator);
		 settings.append("filterSeen:" + getFilterSeen());

		 settings.append(lineSeparator);
		 settings.append("ratingOption:" + getRatingOption());

		 settings.append(lineSeparator);
		 settings.append("ratingValue:" + Double.toString(getRatingValue()));

		 settings.append(lineSeparator);
		 settings.append("dateOption:" + Integer.toString(getDateOption()));

		 settings.append(lineSeparator);
		 settings.append("dateValue:" + getDateValue());

		 settings.append(lineSeparator);
		 settings.append("seenEditableInMainWindow:" + getSeenEditable());

		 settings.append(lineSeparator);
		 settings.append("multiAddDirectoryPath:" + getMultiAddDirectoryPath());

		 settings.append(lineSeparator);
		 settings.append("multiAddExcludeString:" + getMultiAddExcludeString());

		 settings.append(lineSeparator);
		 settings.append("multiAddExcludeStringEnabled:" + getMultiAddExcludeStringEnabled());

		 settings.append(lineSeparator);
		 settings.append("multiAddSelectOption:" + getMultiAddSelectOption());

		 settings.append(lineSeparator);
		 settings.append("multiAddEnableExludeParantheses:" + getMultiAddEnableExludeParantheses());
		 
		 settings.append(lineSeparator);
		 settings.append("multiAddEnableExludeCDNotation:" + getMultiAddEnableExludeCDNotation());
		 
		 settings.append(lineSeparator);
		 settings.append("multiAddEnableExludeIntegers:" + getMultiAddEnableExludeIntegers());
		 
		 settings.append(lineSeparator);
		 settings.append("multiAddEnableExludeCodecInfo:" + getMultiAddEnableExludeCodecInfo());
		 
		 settings.append(lineSeparator);
		 settings.append("multiAddEnableSearchInSubdirectories:" + getMultiAddEnableSearchInSubdirectories());
		 
		 settings.append(lineSeparator);
		 settings.append("multiAddTitleOption:" + getMultiAddEnableSearchInSubdirectories());
		 
				 
		 settings.append(lineSeparator);
		 settings.append("proxyEnabled:" + getProxyEnabled());

		 settings.append(lineSeparator);
		 settings.append("proxyType:"+getProxyType());

		 settings.append(lineSeparator);
		 settings.append("authenticationEnabled:" + getAuthenticationEnabled());

		 settings.append(lineSeparator);
		 settings.append("proxyHost:" + getProxyHost());

		 settings.append(lineSeparator);
		 settings.append("proxyPort:" + getProxyPort());

		 settings.append(lineSeparator);
		 settings.append("proxyUser:" + getProxyUser());

		 settings.append(lineSeparator);
		 settings.append("proxyPassword:" + getProxyPassword());

		 settings.append(lineSeparator);
		 settings.append("lastFileDir:");
		 if (getLastFileDir() != null)
			 settings.append(getLastFileDir().getPath());

		 settings.append(lineSeparator);
		 settings.append("lastDVDDir:");
		 if (getLastFileDir() != null)
			 settings.append(getLastFileDir().getPath());


		 settings.append(lineSeparator);
		 settings.append("lastDatabaseDir:");
		 if (getLastDatabaseDir() != null)
			 settings.append(getLastDatabaseDir().getPath());

		 settings.append(lineSeparator);
		 settings.append("lastMiscDir:");
		 if (getLastMiscDir() != null)
			 settings.append(getLastMiscDir().getPath());


		 settings.append(lineSeparator);
		 settings.append("lastFileFilterMovieInfoUsed:" + getLastFileFilterUsed());

		 settings.append(lineSeparator);
		 settings.append("exportType:" + getExportType());


		 settings.append(lineSeparator);
		 settings.append("movieInfoSliderPosition:" + MovieManager.getDialog().getMovieInfoSplitPane().getDividerLocation());

		 settings.append(lineSeparator);
		 settings.append("movieInfoLastSliderPosition:" + MovieManager.getDialog().getMovieInfoSplitPane().getLastDividerLocation());

		 settings.append(lineSeparator);
		 settings.append("addionalInfoNotesSliderPosition:" + MovieManager.getDialog().getAdditionalInfoNotesSplitPane().getDividerLocation());

		 settings.append(lineSeparator);
		 settings.append("addionalInfoNotesLastSliderPosition:" + MovieManager.getDialog().getAdditionalInfoNotesSplitPane().getLastDividerLocation());

		 settings.append(lineSeparator);
		 settings.append("mainWidth:" + ((int) mainSize.getWidth()));

		 settings.append(lineSeparator);
		 settings.append("mainHeight:" + ((int) mainSize.getHeight()));

		 settings.append(lineSeparator);
		 settings.append("screenLocationX:" + ((int) getScreenLocation().getX()));

		 settings.append(lineSeparator);
		 settings.append("screenLocationY:" + ((int) getScreenLocation().getY()));

		 settings.append(lineSeparator);
		 settings.append("mainMaximized:" + getMainMaximized());

		 settings.append(lineSeparator);
		 settings.append("currentList:"+ getCurrentList());

		 settings.append(lineSeparator);
		 settings.append("loadCurrentListAtStartup:" + getLoadLastUsedListAtStartup());

		 settings.append(lineSeparator);
		 settings.append("multiAddList:" + getMultiAddList());

		 settings.append(lineSeparator);
		 settings.append("multiAddListEnabled:"+ getMultiAddListEnabled());

		 settings.append(lineSeparator);
		 settings.append("lastDialogImportType:" + getLastDialogImportType());
		 
		 settings.append(lineSeparator);
		 settings.append("importTextfilePath:" + getImportTextfilePath());

		 settings.append(lineSeparator);
		 settings.append("importExcelfilePath:" + getImportExcelfilePath());

		 settings.append(lineSeparator);
		 settings.append("importXMLfilePath:" + getImportXMLfilePath());

		 settings.append(lineSeparator);
		 settings.append("importCSVfilePath:" + getImportCSVfilePath());
		 settings.append(lineSeparator);
		 settings.append("importCSVseparator:" + getImportCSVseparator());

		 settings.append(lineSeparator);
		 settings.append("importExtremefilePath:" + getImportExtremefilePath());

		 settings.append(lineSeparator);
		 settings.append("useJTreeIcons:" + getUseJTreeIcons());

		 settings.append(lineSeparator);
		 settings.append("useJTreeCovers:" + getUseJTreeCovers());

		 settings.append(lineSeparator);
		 settings.append("lastPreferencesTabIndex:"+ Integer.toString(getLastPreferencesTabIndex()));

		 settings.append(lineSeparator);
		 settings.append("movieListRowHeight:"+ Integer.toString(getMovieListRowHeight()));

		 settings.append(lineSeparator);
		 settings.append("movieListHighlightEntireRow:" + getMovieListHighlightEntireRow());

		 settings.append(lineSeparator);
		 settings.append("enableCtrlMouseRightClick:" + getEnableCtrlMouseRightClick());

		 settings.append(lineSeparator);
		 settings.append("preserveCoverAspectRatioSetting:"+ getPreserveCoverAspectRatio());

		 settings.append(lineSeparator);
		 settings.append("noCover:"+ getNoCover());

		 settings.append(lineSeparator);
		 settings.append("autoMoveThe:"+ getAutoMoveThe());

		 settings.append(lineSeparator);
		 settings.append("autoMoveAnAndA:"+ getAutoMoveAnAndA());

		 settings.append(lineSeparator);
		 settings.append("storeAllAkaTitles:"+ getStoreAllAkaTitles());

		 settings.append(lineSeparator);
		 settings.append("includeAkaLanguageCodes:"+ getIncludeAkaLanguageCodes());

		 settings.append(lineSeparator);
		 settings.append("titleLanguageCode:"+ getTitleLanguageCode());

		 settings.append(lineSeparator);
		 settings.append("useLanguageSpecificTitle:"+ getUseLanguageSpecificTitle());

		 settings.append(lineSeparator);
		 settings.append("useMediaInfoDLL:"+ getUseMediaInfoDLL());

		 settings.append(lineSeparator);
		 settings.append("playerPath:"+ getMediaPlayerPath());

		 settings.append(lineSeparator);
		 settings.append("browserPath:"+ getBrowserPath());

		 settings.append(lineSeparator);
		 settings.append("useDefaultWindowsPlayer:"+ getUseDefaultWindowsPlayer());

		 settings.append(lineSeparator);
		 settings.append("systemWebBrowser:"+ getSystemWebBrowser());

		 settings.append(lineSeparator);
		 settings.append("displayPlayButton:"+ getDisplayPlayButton());

		 settings.append(lineSeparator);
		 settings.append("displayPrintButton:"+ getDisplayPrintButton());





		 HashMap defaultValues = getAdditionalInfoDefaultValues();
		 AdditionalInfoFieldDefaultValues value;

		 Object[] keys = defaultValues.keySet().toArray();

		 for (int i = 0; i < keys.length; i++) {
			 value = (AdditionalInfoFieldDefaultValues) defaultValues.get(keys[i]);

			 if (value != null) {
				 settings.append(lineSeparator);
				 settings.append("AdditionalInfoDefaultValues:"+value.getFieldName()+":");
				 settings.append(value.getDefaultValuesString("|"));
			 }
		 }

		 settings.append(lineSeparator);

		 HashMap searchAlias = getSearchAlias();
		 String key;
		 String val;

		 for (Iterator i = searchAlias.keySet().iterator(); i.hasNext();) {
			 key = (String) i.next();
			 val = (String) searchAlias.get(key);

			 settings.append("Search Alias:" + key + "=" + val);
			 settings.append(lineSeparator);
		 }

		 try {
			 /* Gets the working dir... */
			 String directory = FileUtil.getUserDir();
  
			 /* Gets the File ini... */
			 File ini = new File(directory + "Config.ini");
 
			 /* If it exists deletes... */
			 if (ini.exists() && !ini.delete()) {
				 throw new Exception("Cannot delete config file.");
			 }
			  
			 /* Recreates... */
			 if (!ini.createNewFile()) {
				 throw new Exception("Cannot create config file.");
			 }
			 
			 FileWriter stream = new FileWriter(ini);
			 stream.write(settings.toString());
			 stream.close();
				 
		 } catch (Exception e) {
			 log.error("", e);
		 }
	 }
}
