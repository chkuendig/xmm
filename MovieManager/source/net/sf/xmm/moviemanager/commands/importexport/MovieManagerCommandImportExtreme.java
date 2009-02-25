/**
 * @(#)MovieManagerCommandImportExtreme.java 1.0 26.09.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.DatabaseExtreme;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelExtremeMovie;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandImportExtreme extends MovieManagerCommandImport {

	static Logger log = Logger.getLogger(MovieManagerCommandImportExtreme.class);
	
	File output;
	boolean cancelled = false;
	
	ArrayList movielist = null;
	
	ModelImportExportSettings importSettings;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	int extraInfoFieldsCount = -1;
	
	MovieManagerCommandImportExtreme(ModelImportExportSettings importSettings) {
		this.importSettings = importSettings;
	}
	

	public void handleMovie(int i) {
		
	}
	
	public void execute() {
		
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	

	public void retrieveMovieList() throws Exception {
		
	    /* extreme movie manager */
    
        DatabaseExtreme extremeDb = new DatabaseExtreme(importSettings.filePath);
        extremeDb.setUp();
        
        movielist = extremeDb.getMovies();
        	
		
		ArrayList extraInfoFieldDatabase = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
        extraInfoFieldsCount = extraInfoFieldDatabase.size();
        
		
	}

	public String getTitle(int i) {
		return ((ModelMovie) movielist.get(i)).getTitle();
	}
	
	public int getMovieListSize() {
		
		if (movielist == null)
			return -1;
		
		return movielist.size();
	}
	
	
	  
    public int addMovie(int i) {
        
        int ret = -1;
        
        /* Title */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getTitle().equals("")) && !((ModelExtremeMovie) movielist.get(i)).title.equals("")) {
            modelMovieInfo.model.setTitle(((ModelExtremeMovie) movielist.get(i)).title);
        }
        
        /* Date */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getDate().equals("")) && !((ModelExtremeMovie) movielist.get(i)).date.equals(""))
            modelMovieInfo.model.setDate(((ModelExtremeMovie) movielist.get(i)).date);
        
        /* Colour */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getColour().equals("")) && !((ModelExtremeMovie) movielist.get(i)).colour.equals(""))
            modelMovieInfo.model.setColour(((ModelExtremeMovie) movielist.get(i)).colour);
        
        /* Directed by */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getDirectedBy().equals("")) && !((ModelExtremeMovie) movielist.get(i)).directed_by.equals(""))
            modelMovieInfo.model.setDirectedBy(((ModelExtremeMovie) movielist.get(i)).directed_by);
        
        /* Written by */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getWrittenBy().equals("")) && !((ModelExtremeMovie) movielist.get(i)).written_by.equals(""))
            modelMovieInfo.model.setWrittenBy(((ModelExtremeMovie) movielist.get(i)).written_by);
        
        /* Genre */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getGenre().equals("")) && !((ModelExtremeMovie) movielist.get(i)).genre.equals(""))
            modelMovieInfo.model.setGenre(((ModelExtremeMovie) movielist.get(i)).genre);
        
        /* Rating */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getRating().equals("")) && !((ModelExtremeMovie) movielist.get(i)).rating.equals(""))
            modelMovieInfo.model.setRating(((ModelExtremeMovie) movielist.get(i)).rating);
        
        /* Country */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getCountry().equals("")) && !((ModelExtremeMovie) movielist.get(i)).country.equals(""))
            modelMovieInfo.model.setCountry(((ModelExtremeMovie) movielist.get(i)).country);
        
        /* Seen */
        modelMovieInfo.model.setSeen(((ModelExtremeMovie) movielist.get(i)).seen);
        
        /* Language */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getLanguage().equals("")) && !((ModelExtremeMovie) movielist.get(i)).language.equals("")) {
            if (importSettings.extremeOriginalLanguage)
                modelMovieInfo.model.setLanguage(((ModelExtremeMovie) movielist.get(i)).originalLanguage);
            else
                modelMovieInfo.model.setLanguage(((ModelExtremeMovie) movielist.get(i)).language);
        }
        
        /* Plot */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getPlot().equals("")) && !((ModelExtremeMovie) movielist.get(i)).plot.equals(""))
            modelMovieInfo.model.setPlot(((ModelExtremeMovie) movielist.get(i)).plot);
        
        /* Cast */
        if (!(importSettings.overwriteWithImdbInfo && !modelMovieInfo.model.getCast().equals("")) && !((ModelExtremeMovie) movielist.get(i)).cast.equals(""))
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
        
        modelMovieInfo.setFieldValues(new ArrayList());
        
        /* Subtitles */
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).subtitles);
        
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
        
        modelMovieInfo.getFieldValues().add(duration); /* Duration */
        modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).filesize, 0));
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).cds); /* CDs */ 
        modelMovieInfo.getFieldValues().add("1"); /* CD cases */
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).resolution);
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).codec);
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).videoRate);
        modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).bitrate, 0));
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).audioCodec);
        modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).sampleRate, 0));
        modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).audioBitrate, 0));
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).channels);
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).filePath); /* Location */
        modelMovieInfo.getFieldValues().add(String.valueOf(((ModelExtremeMovie) movielist.get(i)).fileCount));
        modelMovieInfo.getFieldValues().add(""); /* Container */
        modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).media);
        
        /* Adding empty values for the extra info fields */
        for (int u = 0; u < extraInfoFieldsCount; u++)
            modelMovieInfo.getFieldValues().add("");
        
        if (((ModelExtremeMovie) movielist.get(i)).scriptUsed.indexOf("IMDB.COM") != -1)
            modelMovieInfo.model.setUrlKey(((ModelExtremeMovie) movielist.get(i)).imdb);
        
        if (!((ModelExtremeMovie) movielist.get(i)).cover.equals("none.jpg")) {
            File tempFile = new File(importSettings.coverPath + ((ModelExtremeMovie) movielist.get(i)).cover);
            
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
     
        try {
            ret = (modelMovieInfo.saveToDatabase(importSettings.addToThisList)).getKey();
        } catch (Exception e) {
            log.error("Saving to database failed.", e);
        }
        
        return ret;
    }

}
