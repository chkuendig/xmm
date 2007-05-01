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

	public static boolean notesHaveBeenChanged = false;
	
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
		additionalInfo = new ModelAdditionalInfo();
	}

	/**
	 * The constructor.
	 **/
	public ModelEpisode(int key, int movieKey, int episodeNumber, String urlKey, String cover, String date, String title, String directedBy, String writtenBy, String genre, String rating, String plot, String cast, String notes, boolean seen, String aka, String country, String language, String colour, String certification, String webSoundMix, String webRuntime, String awards) {

		setKey(key);
		this.movieKey = movieKey;
		this.episodeNumber = episodeNumber;
		setUrlKey(urlKey);
		setCover(cover);
		setDate(date);
		setTitle(title);
		setDirectedBy(directedBy);
		setWrittenBy(writtenBy);
		setGenre(genre);
		setRating(rating);
		setPlot(plot);
		setCast(cast);
		setNotes(notes);
		setSeen(seen);
		setAka(aka);
		setCountry(country); 
		setLanguage(language);
		setColour(colour);
		setCertification(certification);
		//setMpaa(mpaa); Not yet implemented in Episodes (database field is missing)
		setWebSoundMix(webSoundMix);
		setWebRuntime(webRuntime);
		setAwards(awards);
		
		additionalInfo = new ModelAdditionalInfo();
	}

	public ModelEpisode(int key, int movieKey, int episodeNumber, String title) {

		setKey(key);
		this.movieKey = movieKey;
		this.episodeNumber = episodeNumber;
		setTitle(title);
	}

	public boolean isEpisode() {
		return true;
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

		setKey(model.getKey());
		this.movieKey = ((ModelEpisode) model).getMovieKey();
		this.episodeNumber = ((ModelEpisode) model).getEpisodeNumber();
		setUrlKey(model.getUrlKey());
		setCover(model.getCover());
		setDate(model.getDate());
		setTitle(model.getTitle());
		setDirectedBy(model.getDirectedBy());
		setWrittenBy(model.getWrittenBy());
		setGenre(model.getGenre());
		setRating(model.getRating());
		setPlot(model.getPlot());
		setCast(model.getCast());
		setNotes(model.getNotes());
		setSeen(model.getSeen());
		setAka(model.getAka());
		setCountry(model.getCountry()); 
		setLanguage(model.getLanguage());
		setColour(model.getColour());
		setCertification(model.getCertification());
		setWebSoundMix(model.getWebSoundMix());
		setWebRuntime(model.getWebRuntime());
		setAwards(model.getAwards());

		//setMpaa(model.getMpaa());
		setCoverData(model.getCoverData());

		hasGeneralInfoData = model.getHasGeneralInfoData();
		
		if (model.getHasAdditionalInfoData())
			setAdditionalInfo(model.getAdditionalInfo());
		
		hasChangedNotes = model.hasChangedNotes;
		
	}

	public void updateGeneralInfoData() {

		if (getKey() != -1) {

			ModelEntry model = null;
			model = MovieManager.getIt().getDatabase().getEpisode(getKey(), true);

			if (model != null) {
				copyData(model);
			}
		}
	}

	public void updateCoverData() {

		if (MovieManager.getIt().getDatabase().getDatabaseType().equals("MySQL"))
			setCoverData(((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataEpisode(getKey()));
	}

	public void updateAdditionalInfoData() {

		if (additionalInfo.hasOldExtraInfoFieldNames())
			additionalInfo.updateExtraInfoFieldNames();
			
		if (getKey() != -1) {
			
			ModelAdditionalInfo tmp = MovieManager.getIt().getDatabase().getAdditionalInfo(getKey(), true);

			if (tmp != null) {
				setAdditionalInfo(tmp);
				hasAdditionalInfoData = true;
			}
		}
		
		if (additionalInfo == null)
			additionalInfo = new ModelAdditionalInfo();
	}
}
