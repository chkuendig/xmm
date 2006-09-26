/**
 * @(#)DatabaseImporter.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.database;

import net.sf.xmm.moviemanager.DialogAlert;
import net.sf.xmm.moviemanager.DialogMovieInfo;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMovies;
import net.sf.xmm.moviemanager.util.SwingWorker;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.ShowGUI;

import org.apache.log4j.Logger;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;


public class DatabaseImporter {
    
    static Logger log = Logger.getRootLogger();
    
    private int lengthOfTask = 0;
    private int current = -1;
    private boolean done = false;
    private boolean canceled = false;
    private String [] transferred = null;
    private ModelImportSettings importSettings;
    private Dialog parent;

    public DatabaseImporter(Dialog parent, ModelImportSettings importSettings) {
	this.importSettings = importSettings;
	this.parent = parent;
    }
    
    public void go() {
        final SwingWorker worker = new SwingWorker() {
		public Object construct() {
		    current = -1;
		    done = false;
		    canceled = false;
		    return new Importer(importSettings);
		}
	    };
	worker.start();
    }
    
    public int getLengthOfTask() {
        return lengthOfTask;
    }
    
    /*Returns the current position in the array*/
    public int getCurrent() {
        return current;
    }
    
    /*Stops the importing process*/
    public void stop() {
	canceled = true;
    }
    
    public boolean isDone() {
        return done;
    }
    
    /* Returns the array transferred which contains all the finished database entries */
    public String[] getTransferred() {
	return transferred;
    }
    
    /**
     * The actual database import task.
     * This runs in a SwingWorker thread.
     */
    class Importer extends MovieManagerCommandAddMultipleMovies {
	
	int multiAddSelectOption;
	int importMode;
	boolean overwriteWithImdbInfo;
	int excelColumn;
	String addToThisList;
	String filePath;
	boolean originalLanguage = true;
	String coverPath;
	
	DialogMovieInfo dialogMovieInfo;
	    
        Importer(ModelImportSettings importSettings) {
	    
	    multiAddSelectOption = importSettings.multiAddSelectOption;
	    importMode = importSettings.importMode;
	    overwriteWithImdbInfo = importSettings.overwriteWithImdbInfo;
	    excelColumn = importSettings.excelColumn;
	    addToThisList = importSettings.addToThisList;
	    filePath = importSettings.filePath;
	    originalLanguage = importSettings.extremeOriginalLanguage;
	    coverPath = importSettings.coverPath;
	    
	    dialogMovieInfo = new DialogMovieInfo("Add Movie");
	    
	    /* Setting the priority of the thread to 4 to give the GUI room to update more often */
	    Thread.currentThread().setPriority(4);
	    
	    if (filePath != null) {
		
		ArrayList movielist = null;
		
		try {
		    movielist = getMovieList();
		    
		    if (movielist == null)
			throw new Exception("An error occured while reading file");

		} catch (Exception e) {
		    
		    JDialog alert = new DialogAlert(parent, "Error", e.getMessage());
		    //alert.setVisible(true);
		    ShowGUI.showAndWait(alert, true);
		    return;
		}
		
		lengthOfTask = movielist.size();
		transferred = new String[lengthOfTask + 1];

		String title;
		int length;
	    
		/* Extreme cover path */
		File tempFile = new File(filePath);
		
		int extraInfoFieldsCount = MovieManager.getIt().getDatabase().getExtraInfoFieldNames().size();
		
		for (int i = 0; i < movielist.size(); i++) {
		    
		    cancel = false;
		
		    if (importMode == 2)
			title = ((ModelExtremeMovie) movielist.get(i)).title;
		    else
			title = (String) movielist.get(i);
		
		    if (!title.equals("")) {
			
			/* First resetting the info already present */
			dialogMovieInfo.executeCommandSetGeneralInfoFieldsEmpty();
			
			if (multiAddSelectOption != -1) {
			    
			    dialogMovieInfo.executeCommandGetIMDBInfoMultiMovies(this, title, title, multiAddSelectOption);
			    
			    /* If the user cancels the imdb */
			    if (dropImdbInfo)
				dialogMovieInfo.executeCommandSetGeneralInfoFieldsEmpty();
			    dropImdbInfo = false;
			}
			
			if (cancelAll || canceled)
			    break;
			
			if (!cancel) {
			
			    /* Extreme movie manager */
			    if (importMode == 2) {
				
				/* Title */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getMovieTitle().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).title.equals(""))
				    dialogMovieInfo.getMovieTitle().setText(((ModelExtremeMovie) movielist.get(i)).title);
				
				/* Date */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getDate().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).date.equals(""))
				    dialogMovieInfo.getDate().setText(((ModelExtremeMovie) movielist.get(i)).date);
				
				/* Colour */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getColour().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).colour.equals(""))
				    dialogMovieInfo.getColour().setText(((ModelExtremeMovie) movielist.get(i)).colour);
			    
				/* Directed by */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getDirectedBy().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).directed_by.equals(""))
				    dialogMovieInfo.getDirectedBy().setText(((ModelExtremeMovie) movielist.get(i)).directed_by);
			    
				/* Written by */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getWrittenBy().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).written_by.equals(""))
				    dialogMovieInfo.getWrittenBy().setText(((ModelExtremeMovie) movielist.get(i)).written_by);
			    
				/* Genre */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getGenre().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).genre.equals(""))
				    dialogMovieInfo.getGenre().setText(((ModelExtremeMovie) movielist.get(i)).genre);
			    
				/* Rating */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getRating().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).rating.equals(""))
				    dialogMovieInfo.getRating().setText(((ModelExtremeMovie) movielist.get(i)).rating);
			    
				/* Country */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getCountry().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).country.equals(""))
				    dialogMovieInfo.getCountry().setText(((ModelExtremeMovie) movielist.get(i)).country);
			    
				/* Seen */
				dialogMovieInfo._seen = ((ModelExtremeMovie) movielist.get(i)).seen;
			    
				/* Language */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getLanguage().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).language.equals("")) {
				    if (originalLanguage)
					dialogMovieInfo.getLanguage().setText(((ModelExtremeMovie) movielist.get(i)).originalLanguage);
				    else
					dialogMovieInfo.getLanguage().setText(((ModelExtremeMovie) movielist.get(i)).language);
				}
				
				/* Plot */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getPlot().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).plot.equals(""))
				    dialogMovieInfo.getPlot().setText(((ModelExtremeMovie) movielist.get(i)).plot);
			    
				/* Cast */
				if (!(overwriteWithImdbInfo && !dialogMovieInfo.getCast().getText().equals("")) && !((ModelExtremeMovie) movielist.get(i)).cast.equals(""))
				    dialogMovieInfo.getCast().setText(((ModelExtremeMovie) movielist.get(i)).cast);
			    
				/* Notes */
				dialogMovieInfo.getNotes().setText(((ModelExtremeMovie) movielist.get(i)).notes);
				
				/* Runntime */
				dialogMovieInfo.getWebRuntime().setText(((ModelExtremeMovie) movielist.get(i)).length);
				
				/* If Aka doesn't equal the title */
				if (!((ModelExtremeMovie) movielist.get(i)).aka.equalsIgnoreCase(((ModelExtremeMovie) movielist.get(i)).title))
				    dialogMovieInfo.getAka().setText(((ModelExtremeMovie) movielist.get(i)).aka);

				/* MPAA */
				dialogMovieInfo.getMpaa().setText(((ModelExtremeMovie) movielist.get(i)).mpaa);
				
				
				dialogMovieInfo._fieldValues = new ArrayList();
				
				/* Subtitles */
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).subtitles);
				
				String duration;
				try {
				    duration = IMDB.extractTime(((ModelExtremeMovie) movielist.get(i)).length);
				
				    if (!duration.equals("")) {
					length = Integer.parseInt(duration);
					length = length*60;
					duration = String.valueOf(length);
				    }
				}
				catch (Exception e) {
				    duration = "";
				}
			    
				dialogMovieInfo._fieldValues.add(duration); /* Duration */
				dialogMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).filesize, 0));
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).cds); /* CDs */ 
				dialogMovieInfo._fieldValues.add("1"); /* CD cases */
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).resolution);
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).codec);
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).videoRate);
				dialogMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).bitrate, 0));
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).audioCodec);
				dialogMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).sampleRate, 0));
				dialogMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).audioBitrate, 0));
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).channels);
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).filePath); /* Location */
				dialogMovieInfo._fieldValues.add(String.valueOf(((ModelExtremeMovie) movielist.get(i)).fileCount));
				dialogMovieInfo._fieldValues.add(""); /* Container */
				dialogMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).media);
				
				/* Adding empty values for the extra info fields */
				for (int u = 0; u < extraInfoFieldsCount; u++)
				    dialogMovieInfo._fieldValues.add("");
				
				if (((ModelExtremeMovie) movielist.get(i)).scriptUsed.indexOf("IMDB.COM") != -1)
				    dialogMovieInfo.setIMDB(((ModelExtremeMovie) movielist.get(i)).imdb);
			    
				if (!((ModelExtremeMovie) movielist.get(i)).cover.equals("none.jpg")) {
				    tempFile = new File(coverPath + ((ModelExtremeMovie) movielist.get(i)).cover);
				
				    if (tempFile.isFile()) {
				    
					try {
					    /* Reading cover to memory */
					    InputStream inputStream = new FileInputStream(tempFile);
					    byte [] coverData = new byte[inputStream.available()];
					    inputStream.read(coverData);
					    inputStream.close();
					
					    /* Setting cover */
					    dialogMovieInfo.setCover(((ModelExtremeMovie) movielist.get(i)).cover, coverData);
					}
					catch (Exception e) {
					    log.error("", e);
					}
				    }
				    else
					log.warn(tempFile.getAbsolutePath() + " does not exist");
				}
			    }
			    else
				dialogMovieInfo.getMovieTitle().setText(title);
			
			    int ret = (dialogMovieInfo.executeCommandSave(addToThisList)).getKey();
			    
			    if (ret == -1) {
				
				if (MovieManager.getIt().getDatabase().getErrorMessage().equals("Data truncation cover")) {
				    dialogMovieInfo.removeCover();
				    ret = (dialogMovieInfo.executeCommandSave(addToThisList)).getKey();
				}
			    }
			    
			    if (ret == -1)
				transferred[++current] = "Failed to import: " + title;
			    else
				transferred[++current] = title;
			    
			}
		    }
		    else
			transferred[++current] = "Empty entry";
		}
		done = true;
	    }
	}
	
	private String cleanInt(String toBeCleaned, int mode) {
	    
	    boolean start = false;
	    String result = "";
	
	    if (mode == 1)
		log.debug("toBeCleaned:" + toBeCleaned);
	
	    for (int i = 0; i < toBeCleaned.length(); i++) {
	    
		if (Character.isDigit(toBeCleaned.charAt(i)))
		    start = true;
	    
		if (start) {
		
		    if (mode == 1 && toBeCleaned.charAt(i) == ',')
			log.debug("NumValComma:"+ Character.getNumericValue(toBeCleaned.charAt(i)));
		    
		    if (Character.getNumericValue(toBeCleaned.charAt(i)) != -1 || toBeCleaned.charAt(i) == ',' || toBeCleaned.charAt(i) == '.') {
		    
			if (Character.isDigit(toBeCleaned.charAt(i))) {
			    result += toBeCleaned.charAt(i);
			}
			else {
			    break;
			}
		    }
		}
	    }
	
	    if (mode == 1)
		log.debug("result:" + result);
	    
	    return result;
	}
    
	protected ArrayList getMovieList() throws Exception {
	    
	    ArrayList movieList = null;
	    
	    /* Textfile */
	    if (importMode == 0) {
		
		File textFile = new File(filePath);
		
		if (!textFile.isFile()) {
		    throw new Exception("Text file does not exist.");
		}
		
		movieList = new ArrayList(10);
		
		try {
		    
		    FileReader reader = new FileReader(textFile);
		    BufferedReader stream = new BufferedReader(reader);
		    
		    String line;
		    while ((line = stream.readLine()) != null) {
			movieList.add(line.trim());
		    }
		}
		catch (Exception e) {
		    log.error("", e);
		}
	    
		/* Excel spreadsheet */
	    } else if (importMode == 1) {
		
		try {
		    
		    Workbook workbook = Workbook.getWorkbook(new File(filePath));
		    Sheet sheet = workbook.getSheet(0);
		    Cell tempCell;
		    String tempString; 
		    movieList = new ArrayList();
		    
		    for (int i = 0; i < sheet.getRows(); i++) {
		    
			tempCell = sheet.getCell(excelColumn, i); 
			tempString = tempCell.getContents();
			movieList.add(tempString.trim());
		    }
		}
		catch (Exception e) {
		    log.error("", e);
		}
	    
		/* extreme movie manager */
	    } else if (importMode == 2) {
		DatabaseExtreme extremeDb = new DatabaseExtreme(filePath);
		extremeDb.setUp();
		
		movieList = extremeDb.getMovies();
	    }
	
	    return movieList;
	}
    }
}
