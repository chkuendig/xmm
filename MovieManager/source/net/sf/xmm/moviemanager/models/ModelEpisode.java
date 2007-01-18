/**
 * @(#)ModelEpisode.java 29.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.models;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;

public class ModelEpisode extends ModelEntry {

    /*The key to the entry this episode is linked to.*/
    private int movieKey = -1;
    
    /*The database key for this episode.*/
    private int episodeNumber;
    
    /* default public constructor for XML export */
    public ModelEpisode() {
        additionalInfo = new ModelAdditionalInfo();
    }
    
    public ModelEpisode(int movieKey) {
        this.movieKey = movieKey;
        additionalInfo = new ModelAdditionalInfo();
    }
    
    public ModelEpisode(ModelEpisode model) {
    	copyData(model);
    }
    
    /**
     * The constructor.
     **/
    public ModelEpisode(int key, int movieKey, int episodeNumber, String urlKey, String cover, String date, String title, String directedBy, String writtenBy, String genre, String rating, String plot, String cast, String notes, boolean seen, String aka, String country, String language, String colour, String certification, String webSoundMix, String webRuntime, String awards) {
	
	this.key = key;
	this.movieKey = movieKey;
	this.episodeNumber = episodeNumber;
	this.urlKey = urlKey;
	this.cover = cover;
	this.date = date;
	this.title = title;
	this.directedBy = directedBy;
	this.writtenBy = writtenBy;
	this.genre = genre;
 	this.rating = rating;
	this.plot = plot;
	this.cast = cast;
	this.notes = notes;
	this.seen = seen;
	this.aka = aka;
	this.country = country; 
	this.language = language;
	this.colour = colour;
	this.certification = certification;
	this.webSoundMix = webSoundMix;
	this.webRuntime = webRuntime;
	this.awards = awards;
	
	hasGeneralInfoData = true;
    
    additionalInfo = new ModelAdditionalInfo();
    }
    
    public ModelEpisode(int key, int movieKey, int episodeNumber, String title) {
	
	this.key = key;
	this.movieKey = movieKey;
	this.episodeNumber = episodeNumber;
	this.title = title;
	
	hasGeneralInfoData = false;
    }
    
    public int getMovieKey() {
	return movieKey; 
    }
    
    public void setMovieKey(int movieKey) {
    	this.movieKey = movieKey; 
    }
    
    public int getEpisodeNumber() {
    	return episodeNumber; 
    }
    
    public void setEpisodeNumber(int episodeNumber) {
	this.episodeNumber = episodeNumber; 
    }

    public void copyData(ModelEntry model) {
	
	this.key = model.getKey();
	this.movieKey = ((ModelEpisode) model).getMovieKey();
	this.episodeNumber = ((ModelEpisode) model).getEpisodeNumber();
	this.urlKey = model.getUrlKey();
	this.cover = model.getCover();
	this.coverData = model.getCoverData();
	this.date = model.getDate();
	this.title = model.getTitle();
	this.directedBy = model.getDirectedBy();
	this.writtenBy = model.getWrittenBy();
	this.genre = model.getGenre();
 	this.rating = model.getRating();
	this.plot = model.getPlot();
	this.cast = model.getCast();
	this.notes = model.getNotes();
	this.seen = model.getSeen();
	this.aka = model.getAka();
	this.country = model.getCountry(); 
	this.language = model.getLanguage();
	this.colour = model.getColour();
	this.certification = model.getCertification();
	this.webSoundMix = model.getWebSoundMix();
	this.webRuntime = model.getWebRuntime();
	this.awards = model.getAwards();
	
	coverData =  model.getCoverData();
	
	hasGeneralInfoData = model.getHasGeneralInfoData();
	hasAdditionalInfoData = model.getHasAdditionalInfoData();
	hasChangedNotes = model.hasChangedNotes;
	hasAdditionalInfoData = model.getHasAdditionalInfoData();
	
	additionalInfo = model.getAdditionalInfo();
	 }
    
     public void updateGeneralInfoData() {
	ModelEntry model = null;
	
	model = MovieManager.getIt().getDatabase().getEpisode(getKey(), true);
	
	if (model != null) {
	    copyData(model);
	}
    }
    
     public void updateCoverData() {
         
         if (MovieManager.getIt().getDatabase().getDatabaseType().equals("MySQL"))
             coverData = ((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataEpisode(getKey());
     }
     
    public void updateAdditionalInfoData() {
	
	ModelAdditionalInfo tmp = MovieManager.getIt().getDatabase().getAdditionalInfo(getKey(), true);
	
	if (tmp != null) {
	    additionalInfo = tmp;
	    hasAdditionalInfoData = true;
	}
    }
}
