/**
 * @(#)MovieManagerCommandAddMultipleMoviesByFile.java 1.0 24.01.06 (dd.mm.yy)
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
 * 7Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class MovieManagerCommandAddMultipleMoviesByFile extends MovieManagerCommandAddMultipleMovies implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    String[] stringFiles;
    
    ArrayList fileList = null;
    
    ModelMovieInfo movieInfoModel = null;
    
    String excludeString;
    boolean enableExludeString = false;
    int multiAddSelectOption;
    boolean enableExludeParantheses = false;
    boolean enableExludeCDNotations = false;
    boolean enableExludeIntegers = false;
    boolean enableExludeCodecInfo = false;
    boolean searchInSubdirectories = false;
    boolean addMovieToList = false;
    String addToThisList = null;
    
    /**
     * Executes the command.
     * Checks all the options before starting to process the list of movies
     * found in the directory
     **/
    protected void execute() {
	
//    	 If any notes have been changed, they will be saved before changing list
    	MovieManagerCommandSaveChangedNotes.execute();
    	
        movieInfoModel = new ModelMovieInfo(false, true);
        
	cancelAll = false;
	
	DialogAddMultipleMovies damm = new DialogAddMultipleMovies();
	GUIUtil.showAndWait(damm, true);
	
	multiAddSelectOption = damm.getMultiAddSelectOption();
	enableExludeString = damm.getMultiAddExcludeStringEnabled();
	excludeString = damm.getMultiAddExcludeString();
	
	String path = damm.getPath();
	
	if (path.equals(""))
	    return;
	
	addFilesToArrayList(new File(path));
	
	if (fileList == null)
	    return;
	
	if (damm.enableExludeParantheses.isSelected())
	    enableExludeParantheses = true;
	
	if (damm.enableExludeCDNotation.isSelected())
	    enableExludeCDNotations = true;
	
	if (damm.enableExludeIntegers.isSelected())
	    enableExludeIntegers = true;
	
	if (damm.enableExludeCodecInfo.isSelected())
	    enableExludeCodecInfo = true;
	
	if (damm.enableSearchInSubdirectories.isSelected())
	    searchInSubdirectories = true;
	
	if (damm.enableAddMoviesToList != null && damm.enableAddMoviesToList.isSelected()) {
	    addMovieToList = true;
	    addToThisList = (String) damm.listChooser.getSelectedItem();
	}
	
	createMovies();
    }
    
    
    protected void createMovies() {
	
	//String [] extensions = {".avi", ".mpeg", ".mpg", ".divx", ".ogm"};
	
	ArrayList extensionList = new ArrayList(5);
	extensionList.add(".avi");
	extensionList.add(".mpeg");
	extensionList.add(".mpg");
	extensionList.add(".divx");
	extensionList.add(".ogm");
	
	File [] tempFile = new File[1];
	
	while (!fileList.isEmpty()) {
		
	    tempFile[0] = (File) fileList.get(0);
	    String fileName = tempFile[0].getName();
	    
	    if (fileName.lastIndexOf(".") != -1 && extensionList.contains(fileName.substring(fileName.lastIndexOf("."), fileName.length()).toLowerCase())) {
		
		log.debug("Processing:" + fileName);
		
		movieInfoModel.setAdditionalInfoFieldsEmpty();
		
		/*Getting the fileinfo from file*/
		movieInfoModel.getFileInfo(tempFile);
		
		/*Used if editing existing movie*/
		movieInfoModel.setMultiAddFile(tempFile[0]);
		
		/*Used to set the title of the DialogIMDB window*/
		String searchString = fileName;/*Used to search on imdb*/
		
		if (enableExludeString)
 		    searchString = performExcludeString(searchString);
		
		if (enableExludeParantheses)
		    searchString = performExcludeParantheses(searchString, true);
		
		if (enableExludeCodecInfo)
		    searchString = performExcludeCodecInfo(searchString);
		
		if (enableExludeCDNotations)
		    searchString = performExcludeCDNotations(searchString);
		
		if (enableExludeIntegers)
		    searchString = performExcludeIntegers(searchString);
		
		/*removes dots, double spaces, underscore...*/
		searchString = removeVarious(searchString);
		
		executeCommandGetIMDBInfoMultiMovies(searchString, fileName, multiAddSelectOption, addToThisList);
		
		if (dropImdbInfo)
			movieInfoModel.setGeneralInfoFieldsEmpty();
		
		dropImdbInfo = false;
		
		if (cancelAll) {
			movieInfoModel.model.setTitle("");
		    fileList.clear();
		    return;
		}
		else if (cancel) {
			movieInfoModel.model.setTitle("");
		    
		    /* Empties the additional fields in the DialogMovieInfo object */
			movieInfoModel.setAdditionalInfoFieldsEmpty();
			cancel = false;
		}
		else {
            try {
            	ModelEntry model = movieInfoModel.saveToDatabase(addToThisList);
                MovieManagerCommandSelect.executeAndReload(model, false, false, false);
            } catch (Exception e) {
                log.error("Saving to database failed.", e);
            }
			
		    movieInfoModel.model.setTitle("");
		}
	    }
	    else  if (searchInSubdirectories){
		if (tempFile[0].isDirectory())
		    addFilesToArrayList(tempFile[0]);
	    }
	    fileList.remove(0);
	}
	
	MovieManagerCommandSelect.executeAndReload(0);
	
    }
    
    private String performExcludeString(String searchString) {
	
	String [] excludeStrings = new String[50];
	searchString = searchString.toLowerCase();
	
	StringTokenizer tokenizer = new StringTokenizer(excludeString," ");
	for ( int i = 0; tokenizer.hasMoreTokens() && i < 49; i++) {
	    excludeStrings[i] = tokenizer.nextToken();
	    excludeStrings[i+1] = null;
	}
	
	for (int u = 0; excludeStrings[u] != null; u++) {
	    
	    Pattern excludeFilter = Pattern.compile("(.*"+excludeStrings[u].toLowerCase()+".*)+");
	    if (excludeFilter.matcher(searchString).matches()) {
		searchString = searchString.replaceAll(((excludeStrings[u]).toLowerCase()), " ");
	    }
	}
	return searchString;
    }
    
    /*Removes the predefined codec info*/
    private String performExcludeCodecInfo(String searchString) {
	
	String [] excludeStrings = new String[] {"divx", "dvdivx", "xvidvd", "xvid", "dvdrip", "ac3", "bivx", "mp3"};
	searchString = searchString.toLowerCase();
	
	for (int u = 0; u < excludeStrings.length; u++) {
	    Pattern excludeFilter = Pattern.compile("(.*"+excludeStrings[u].toLowerCase()+".*)+");
	    
	    if (excludeFilter.matcher(searchString).matches()) {
		searchString = searchString.replaceAll(((excludeStrings[u]).toLowerCase()), " ");
	    }
	}
	return searchString;
    }
    
    /* Removes parantheses and it's content */
    public static String performExcludeParantheses(String searchString, boolean toLowerCase) {
	
	if (toLowerCase)
	    searchString = searchString.toLowerCase();
	
	int index1;
	int index2;
	
	char [] paranthese1 = new char[] {'(', '[', '{'};
	char [] paranthese2 = new char[] {')', ']', '}'};
	
	for (int i = 0; i < 3; i++) {
	    
	    while (true) {
		
		index1 = searchString.indexOf(paranthese1[i]);
		index2 = searchString.indexOf(paranthese2[i]);
		
		if (index1 != -1 && index2 != -1) {
		    
		    if (index1 > index2) {
			searchString = removeCharAt(searchString, index2);
		    }
		    
		    else {
			String substring = searchString.substring(index1, index2+1);
			
			while ((substring.indexOf(paranthese1[i]) != -1) || (substring.indexOf(paranthese2[i]) != -1)) {
			    if (substring.indexOf(paranthese1[i]) != -1)
				substring = removeCharAt(substring, substring.indexOf(paranthese1[i]));
			    else
				substring = removeCharAt(substring, substring.indexOf(paranthese2[i]));
			}
			searchString = searchString.replaceFirst(substring, " ");
			searchString = removeCharAt(searchString, index1);
			searchString = replaceCharAt(searchString, index1, ' ');
		    }
		}
		else if (index1 != -1)
		    searchString = replaceCharAt(searchString, index1, ' ');
		else if (index2 != -1) 
		    searchString = replaceCharAt(searchString, index2, ' ');
		else
		    break;
	    }
	}
	return searchString;
    }
    
    /*Removes cd notations from the search string*/
    private String performExcludeCDNotations(String searchString) {
	
	searchString = searchString.toLowerCase();
	
	int index = 0;
	
	while ((index = searchString.indexOf("cd", index)) != -1) {
	    
	    /*if character after cd is a digit, the notation is removed*/
	    if (Character.isDigit(searchString.charAt(index+2))) {
		searchString = removeCharAt(searchString, index);
		searchString = removeCharAt(searchString, index);
		searchString = replaceCharAt(searchString, index, ' ');
		
		if (Character.isDigit(searchString.charAt(index)))
		    searchString = removeCharAt(searchString, index);
	    }
	    index++;
	}
	index = 0;
	while ((index = searchString.indexOf("of", index)) != -1) {
	    if (index > 0) {
		if (Character.isDigit(searchString.charAt(index+2)) && Character.isDigit(searchString.charAt(index-1))) {
		    searchString = removeCharAt(searchString, index-1);
		    searchString = removeCharAt(searchString, index-1);
		    searchString = removeCharAt(searchString, index-1);
		    searchString = replaceCharAt(searchString, index-1, ' ');
		}
	    }
	    index++;
	}
	return searchString;
    }
    
    /*Removes all integers from the search string*/
    private String performExcludeIntegers(String searchString) {
	
	int length = searchString.length()-4;/* "-4" - Don't need to check the extension*/
	int index = 0;
	
	while (index < length) {
	    	    
	    if (Character.isDigit(searchString.charAt(index))) {
		searchString = replaceCharAt(searchString, index, ' ');
		length--;
	    }
	    else
		index++;
	}
	return searchString;
    }
    
    
    String removeVarious(String searchString) {
	
	/*Removes extension*/
	searchString = searchString.substring(0, searchString.lastIndexOf('.'));
	
	/*Removes various*/
	searchString = searchString.replace('.', ' ');
	searchString = searchString.replace(',', ' ');
	searchString = searchString.replace('=', ' ');
	
	int index;
	
	/*Removes all double spaces*/
	while ((index = searchString.indexOf("  ")) != -1) {
	    searchString = removeCharAt(searchString, index);
	}
	return searchString;
    }
    
    static String removeCharAt(String s, int pos) {
    	return s.substring(0,pos)+s.substring(pos+1);
    }
    
    static String replaceCharAt(String s, int pos, char c) {
	return s.substring(0,pos) + c + s.substring(pos+1);
    }
    
    public void setCancelAll(boolean value) {
	cancelAll = value;
    }
    
    public void setCancel(boolean value) {
	cancel = value;
    }
    
    protected void addFilesToArrayList(File directory) {

	if (directory.exists()) {
	    
	    if (fileList == null)
		fileList = new ArrayList(directory.list().length);
	    
	    File [] allTheFiles = directory.listFiles();
	    
	    if (allTheFiles != null) {
		
		for (int i = 0; i < allTheFiles.length; i++)
		    fileList.add(allTheFiles[i]);
	    }
	}
    }
    
    /**
     * Gets the IMDB info for movies (multiAdd)
     **/
    public void executeCommandGetIMDBInfoMultiMovies(String searchString, String filename, int multiAddSelectOption, String addToThisList) {
	
	/* Checks the movie title... */
	log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
	if (!searchString.equals("")) { //$NON-NLS-1$
	    DialogIMDB dialogIMDB = new DialogIMDB(movieInfoModel, searchString, filename, this, multiAddSelectOption, addToThisList);
	    cancel = dialogIMDB.cancelSet;
	    cancelAll = dialogIMDB.cancelAllSet;
	    dropImdbInfo = dialogIMDB.dropImdbInfoSet;
	    
	} else {
	    DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
	    GUIUtil.showAndWait(alert, true);
	}
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: "+ event.getActionCommand());
	execute();
    }
}
  
