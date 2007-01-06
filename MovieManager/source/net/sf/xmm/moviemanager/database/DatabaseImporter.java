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

import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMovies;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SwingWorker;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.ShowGUI;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

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
            
		    Importer importer = null;
            
		    try {
            
			importer =  new Importer(importSettings);
		    }
		    catch (Exception e) {
			log.warn("Exception:" + e.getMessage());   
		    }
		    return importer;
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
		    
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	ArrayList movielist = null;
    
	int extraInfoFieldsCount;
    
	Importer(ModelImportSettings importSettings) {
	    
	    multiAddSelectOption = importSettings.multiAddSelectOption;
	    importMode = importSettings.importMode;
	    overwriteWithImdbInfo = importSettings.overwriteWithImdbInfo;
	    excelColumn = importSettings.excelColumn;
	    addToThisList = importSettings.addToThisList;
	    filePath = importSettings.filePath;
	    originalLanguage = importSettings.extremeOriginalLanguage;
	    coverPath = importSettings.coverPath;
	    	    
	    /* Setting the priority of the thread to 4 to give the GUI room to update more often */
	    Thread.currentThread().setPriority(4);
	    
	    if (filePath != null) {
		
		try {
		    movielist = getMovieList();
		    
		    if (movielist == null)
			throw new Exception("An error occured while reading file");

		} catch (Exception e) {
		    
		    JDialog alert = new DialogAlert(parent, "Error", e.getMessage());
		    ShowGUI.showAndWait(alert, true);
		    return;
		}
		
		lengthOfTask = movielist.size();
		transferred = new String[lengthOfTask + 1];

		String title;
			    
		/* Extreme cover path */
		//File tempFile = new File(filePath);
		
		extraInfoFieldsCount = MovieManager.getIt().getDatabase().getExtraInfoFieldNames().size();
		
		for (int i = 0; i < movielist.size(); i++) {
		    
		    cancel = false;
		    
		    if (importMode == 2)
		    	title = ((ModelExtremeMovie) movielist.get(i)).title;
		    else if (importMode == 3) {
		    
		    	if (movielist.get(i) instanceof ModelMovie)
			    title = ((ModelMovie) movielist.get(i)).getTitle();
		    	else if (movielist.get(i) instanceof ModelSeries) {
			    title = ((ModelSeries) movielist.get(i)).getMovie().getTitle();
		    	}
		    	else
			    continue;
		    }
		    else
		    	title = (String) movielist.get(i);
		
		    if (!title.equals("")) {
			
			/* First resetting the info already present */
		    	
		    	modelMovieInfo.clearModel();
			
			if (multiAddSelectOption != -1) {
			    
			    executeCommandGetIMDBInfoMultiMovies(title, title, multiAddSelectOption);
			    
			    /* If the user cancels the imdb */
			    if (dropImdbInfo)
			    	modelMovieInfo.clearModel();
			    
			    dropImdbInfo = false;
			}
			
			if (cancelAll || canceled)
			    break;
			
			if (!cancel) {
			
			    /* Extreme movie manager */
			    if (importMode == 2) {
			        addExtremeMovie(i);
			    }
			    else if (importMode == 3) {
                	
				Object tmp = movielist.get(i);
                	
				if (tmp instanceof ModelMovie) {
				    modelMovieInfo.setModel((ModelMovie) tmp);
				    modelMovieInfo.saveToDatabase(null);
                			
				} else if (tmp instanceof ModelSeries) {
                    	
				    ModelSeries seriesTmp = (ModelSeries) tmp;
				    ModelEpisode episodeTmp;
                    	
				    modelMovieInfo.setModel(seriesTmp.getMovie());
				    modelMovieInfo.saveToDatabase(null);
                    	
				    int movieKey = modelMovieInfo.model.getKey();
                    	
				    title = "";
				    transferred[++current] = modelMovieInfo.model.getTitle();
                    	
				    for (int u = 0; u < seriesTmp.episodes.size(); u++) {
					episodeTmp = (ModelEpisode) seriesTmp.episodes.get(u);
					episodeTmp.setMovieKey(movieKey);
                    		
					modelMovieInfo.setModel(episodeTmp);
					modelMovieInfo.saveToDatabase(null);
                    		
					transferred[++current] = episodeTmp.getTitle();
				    }
				}
			    }
			    else
			    	modelMovieInfo.model.setTitle(title);
						    
			    int ret = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
			     
			    if (ret == -1) {
				
				if (MovieManager.getIt().getDatabase().getErrorMessage().equals("Data truncation cover")) {
				    modelMovieInfo.setCover("", null);
				    ret = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
				}
			    }
			    
			    if (ret == -1)
			    	transferred[++current] = "Failed to import: " + title;
			    else if (!title.equals(""))
			    	transferred[++current] = title;
			    
			}
		    }
		    else
		    	transferred[++current] = "Empty entry";
		}
		done = true;
	    }
	}
	
	void addExtremeMovie(int i) {
	    /* Title */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getTitle().equals("")) && !((ModelExtremeMovie) movielist.get(i)).title.equals(""))
	    	modelMovieInfo.model.setTitle(((ModelExtremeMovie) movielist.get(i)).title);
        
	    /* Date */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getDate().equals("")) && !((ModelExtremeMovie) movielist.get(i)).date.equals(""))
	    	modelMovieInfo.model.setDate(((ModelExtremeMovie) movielist.get(i)).date);
        
	    /* Colour */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getColour().equals("")) && !((ModelExtremeMovie) movielist.get(i)).colour.equals(""))
	    	modelMovieInfo.model.setColour(((ModelExtremeMovie) movielist.get(i)).colour);
        
	    /* Directed by */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getDirectedBy().equals("")) && !((ModelExtremeMovie) movielist.get(i)).directed_by.equals(""))
	    	modelMovieInfo.model.setDirectedBy(((ModelExtremeMovie) movielist.get(i)).directed_by);
        
	    /* Written by */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getWrittenBy().equals("")) && !((ModelExtremeMovie) movielist.get(i)).written_by.equals(""))
	    	modelMovieInfo.model.setWrittenBy(((ModelExtremeMovie) movielist.get(i)).written_by);
        
	    /* Genre */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getGenre().equals("")) && !((ModelExtremeMovie) movielist.get(i)).genre.equals(""))
	    	modelMovieInfo.model.setGenre(((ModelExtremeMovie) movielist.get(i)).genre);
        
	    /* Rating */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getRating().equals("")) && !((ModelExtremeMovie) movielist.get(i)).rating.equals(""))
	    	modelMovieInfo.model.setRating(((ModelExtremeMovie) movielist.get(i)).rating);
        
	    /* Country */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getCountry().equals("")) && !((ModelExtremeMovie) movielist.get(i)).country.equals(""))
	    	modelMovieInfo.model.setCountry(((ModelExtremeMovie) movielist.get(i)).country);
        
	    /* Seen */
	    modelMovieInfo.model.setSeen(((ModelExtremeMovie) movielist.get(i)).seen);
        
	    /* Language */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getLanguage().equals("")) && !((ModelExtremeMovie) movielist.get(i)).language.equals("")) {
		if (originalLanguage)
		    modelMovieInfo.model.setLanguage(((ModelExtremeMovie) movielist.get(i)).originalLanguage);
		else
		    modelMovieInfo.model.setLanguage(((ModelExtremeMovie) movielist.get(i)).language);
	    }
        
	    /* Plot */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getPlot().equals("")) && !((ModelExtremeMovie) movielist.get(i)).plot.equals(""))
	    	modelMovieInfo.model.setPlot(((ModelExtremeMovie) movielist.get(i)).plot);
        
	    /* Cast */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getCast().equals("")) && !((ModelExtremeMovie) movielist.get(i)).cast.equals(""))
	    	modelMovieInfo.model.setCast(((ModelExtremeMovie) movielist.get(i)).cast);
        
	    /* Notes */
	    modelMovieInfo.model.setNotes(((ModelExtremeMovie) movielist.get(i)).notes);
        
	    /* Runntime */
	    modelMovieInfo.model.setWebRuntime(((ModelExtremeMovie) movielist.get(i)).length);
        
	    /* If Aka doesn't equal the title */
	    if (!((ModelExtremeMovie) movielist.get(i)).aka.equalsIgnoreCase(((ModelExtremeMovie) movielist.get(i)).title))
	    	modelMovieInfo.model.setAka(((ModelExtremeMovie) movielist.get(i)).aka);

	    /* MPAA */
	    modelMovieInfo.model.setMpaa(((ModelExtremeMovie) movielist.get(i)).mpaa);
        
        
	    modelMovieInfo._fieldValues = new ArrayList();
        
	    /* Subtitles */
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).subtitles);
        
	    String duration;
	    try {
		int length;
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
        
	    modelMovieInfo._fieldValues.add(duration); /* Duration */
	    modelMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).filesize, 0));
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).cds); /* CDs */ 
	    modelMovieInfo._fieldValues.add("1"); /* CD cases */
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).resolution);
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).codec);
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).videoRate);
	    modelMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).bitrate, 0));
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).audioCodec);
	    modelMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).sampleRate, 0));
	    modelMovieInfo._fieldValues.add(cleanInt(((ModelExtremeMovie) movielist.get(i)).audioBitrate, 0));
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).channels);
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).filePath); /* Location */
	    modelMovieInfo._fieldValues.add(String.valueOf(((ModelExtremeMovie) movielist.get(i)).fileCount));
	    modelMovieInfo._fieldValues.add(""); /* Container */
	    modelMovieInfo._fieldValues.add(((ModelExtremeMovie) movielist.get(i)).media);
        
	    /* Adding empty values for the extra info fields */
	    for (int u = 0; u < extraInfoFieldsCount; u++)
	    	modelMovieInfo._fieldValues.add("");
        
	    if (((ModelExtremeMovie) movielist.get(i)).scriptUsed.indexOf("IMDB.COM") != -1)
	    	modelMovieInfo.model.setUrlKey(((ModelExtremeMovie) movielist.get(i)).imdb);
        
	    if (!((ModelExtremeMovie) movielist.get(i)).cover.equals("none.jpg")) {
		File tempFile = new File(coverPath + ((ModelExtremeMovie) movielist.get(i)).cover);
        
		if (tempFile.isFile()) {
            
		    try {
			/* Reading cover to memory */
			InputStream inputStream = new FileInputStream(tempFile);
			byte [] coverData = new byte[inputStream.available()];
			inputStream.read(coverData);
			inputStream.close();
            
			/* Setting cover */
			modelMovieInfo.setCover(((ModelExtremeMovie) movielist.get(i)).cover, coverData);
		    }
		    catch (Exception e) {
			log.error("", e);
		    }
		}
		else
		    log.warn(tempFile.getAbsolutePath() + " does not exist");
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
        
       
	void addFromXML(int i) {
        
	    System.err.println("addFromXML");
    	
	    Object tmp = movielist.get(i);
        
	    ModelMovie movie = null;
        
	    if (tmp instanceof ModelMovie)
		movie = (ModelMovie) tmp;
	    else if (tmp instanceof ModelSeries) {
        	System.err.println("ModelSeries  -  !");
		movie = ((ModelSeries) tmp).getMovie();
	    }
	    else return;
        
	    System.err.println("SKROT 1");
        
	    /* Title */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getTitle().equals("")) && !movie.getTitle().equals(""))
		modelMovieInfo.model.setTitle(movie.getTitle());
        
	    /* Date */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getDate().equals("")) && !movie.getDate().equals(""))
		modelMovieInfo.model.setDate(movie.getDate());
        
	    /* Colour */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getColour().equals("")) && !movie.getColour().equals(""))
		modelMovieInfo.model.setColour(movie.getColour());
        
	    /* Directed by */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getDirectedBy().equals("")) && !movie.getDirectedBy().equals(""))
		modelMovieInfo.model.setDirectedBy(movie.getDirectedBy());
        
	    /* Written by */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getWrittenBy().equals("")) && !movie.getWrittenBy().equals(""))
		modelMovieInfo.model.setWrittenBy(movie.getWrittenBy());
        
	    /* Genre */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getGenre().equals("")) && !movie.getGenre().equals(""))
		modelMovieInfo.model.setGenre(movie.getGenre());
        
	    /* Rating */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getRating().equals("")) && !movie.getRating().equals(""))
		modelMovieInfo.model.setRating(movie.getRating());
        
	    /* Country */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getCountry().equals("")) && !movie.getCountry().equals(""))
		modelMovieInfo.model.setCountry(movie.getCountry());
        
	    /* Seen */
	    modelMovieInfo.model.setSeen(movie.getSeen());
        
	    /* Language */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getLanguage().equals("")) && !movie.getLanguage().equals("")) {
		modelMovieInfo.model.setLanguage(movie.getLanguage());
	    }
        
	    /* Plot */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getPlot().equals("")) && !movie.getPlot().equals(""))
		modelMovieInfo.model.setPlot(movie.getPlot());
        
	    /* Cast */
	    if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getCast().equals("")) && !movie.getCast().equals(""))
		modelMovieInfo.model.setCast(movie.getCast());
        
	    /* Notes */
	    modelMovieInfo.model.setNotes(movie.getNotes());
        
	    /* Runntime */
	    modelMovieInfo.model.setWebRuntime(movie.getWebRuntime());
        
	    /* Aka */
	    modelMovieInfo.model.setAka(movie.getAka());

	    /* MPAA */
	    modelMovieInfo.model.setMpaa(movie.getMpaa());
                
	    ModelAdditionalInfo additionalInfo = movie.getAdditionalInfo();
        
	    modelMovieInfo._fieldValues = new ArrayList();
        
	    System.err.println("SKROT 5");
        
	    if (additionalInfo != null) {
        	
	        /* Subtitles */
	        modelMovieInfo._fieldValues.add(additionalInfo.getSubtitles());
	        modelMovieInfo._fieldValues.add(String.valueOf(additionalInfo.getDuration()));
	        modelMovieInfo._fieldValues.add(String.valueOf(additionalInfo.getFileSize()));
	        modelMovieInfo._fieldValues.add(String.valueOf(additionalInfo.getCDs())); /* CDs */ 
	        modelMovieInfo._fieldValues.add(String.valueOf(additionalInfo.getCDCases())); /* CD cases */
	        modelMovieInfo._fieldValues.add(additionalInfo.getResolution());
	        modelMovieInfo._fieldValues.add(additionalInfo.getVideoCodec());
	        modelMovieInfo._fieldValues.add(additionalInfo.getVideoRate());
	        modelMovieInfo._fieldValues.add(additionalInfo.getVideoBitrate());
	        modelMovieInfo._fieldValues.add(additionalInfo.getAudioCodec());
	        modelMovieInfo._fieldValues.add(additionalInfo.getAudioRate());
	        modelMovieInfo._fieldValues.add(additionalInfo.getAudioBitrate());
	        modelMovieInfo._fieldValues.add(additionalInfo.getAudioChannels());
	        modelMovieInfo._fieldValues.add(additionalInfo.getFileLocation()); /* Location */
	        modelMovieInfo._fieldValues.add(String.valueOf(additionalInfo.getFileCount()));
	        modelMovieInfo._fieldValues.add(additionalInfo.getContainer());
	        modelMovieInfo._fieldValues.add(additionalInfo.getMediaType());
	        
	        /* Adding empty values for the extra info fields */
	        for (int u = 0; u < extraInfoFieldsCount; u++)
		    modelMovieInfo._fieldValues.add("");
	                
	        
	        File tempFile = new File(coverPath + movie.getCover());
	        
	        if (tempFile.isFile()) {
	            
	            try {
			/* Reading cover to memory */
			InputStream inputStream = new FileInputStream(tempFile);
			byte [] coverData = new byte[inputStream.available()];
			inputStream.read(coverData);
			inputStream.close();
	            
			/* Setting cover */
			modelMovieInfo.setCover(movie.getCover(), coverData);
	            }
	            catch (Exception e) {
			log.error("", e);
	            }
	        }
	        else
	            log.warn(tempFile.getAbsolutePath() + " does not exist");
	    }
               
        }
        
    	
    
    
	protected ArrayList getMovieList() throws Exception {
	    
	    System.err.println("getMovieList()");
        
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
        
        
		/* XML */
	    } else if (importMode == 3) {
        
	        final ArrayList list = movieList;
           
    		Mapping mapping = new Mapping();
    		mapping.loadMapping(FileUtil.getFileURL("mapping.xml"));
             
    		System.err.println("loadMapping");
             
    		Unmarshaller unmarshaller = new Unmarshaller(ModelExportXML.class);
    		//unmarshaller.setDebug(true);
             
    		System.err.println("setMapping");
             
    		unmarshaller.setMapping(mapping);
             
    		System.err.println("new Unmarshaller(mapping)");
             
    		log.debug("mapping set --------------- !!!!!!!");
    		
        	File xmlFile = new File("output.xml");
                 
    		FileReader reader = null;
    		Object tmp = null;
             
    		if (xmlFile.isFile()) {
    		    System.err.println("xmlFile.isFile()");
    		    reader = new FileReader(xmlFile);
    		}
    		else {
    		    log.error("XML file not found");
    		    return null;
    		}
                    
    		tmp = unmarshaller.unmarshal(reader);
            
    		if (tmp instanceof ModelSeries) {
    		    System.err.println("ModelSerie");
    		}
    		else if (tmp instanceof ModelMovie) {
    		    System.err.println("ModelMovie");
    		}
    		else if (tmp instanceof ModelExportXML) {
    		    System.err.println("ModelExportXML");
    		
    		    System.err.println("get combined list");
    		    
            
		    movieList = ((ModelExportXML) tmp).getCombindedList();
            
    		
    		
		    ModelMovie movieinfo = (ModelMovie) movieList.get(1);
    		
		    System.err.println("movieinfo:" + movieinfo);
            
		    ModelAdditionalInfo addInfo = movieinfo.getAdditionalInfo();
            
		    System.err.println("addInfo:" + addInfo);
            
		}
               
    		System.err.println("ost");
    		
    		System.err.println("movieList size:" + movieList.size());
            
    	    }
    	    return movieList;
    	}
	
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public void executeCommandGetIMDBInfoMultiMovies(String searchString, String filename, int multiAddSelectOption) {
	
	    /* Checks the movie title... */
	    log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
	    if (!searchString.equals("")) { //$NON-NLS-1$
		DialogIMDB dialogIMDB = new DialogIMDB(modelMovieInfo, searchString, filename, this, multiAddSelectOption);
	    } else {
		DialogAlert alert = new DialogAlert(MovieManager.getIt(), Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
		ShowGUI.showAndWait(alert, true);
	    }
	}
    }
}

