/**
 * @(#)ModelMovie.java 26.01.06 (dd.mm.yy)
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

public class ModelMovie extends ModelEntry {

	/* default public constructor for XML export */
	public ModelMovie() {
		setAdditionalInfo(new ModelAdditionalInfo());
	}

	public ModelMovie(ModelMovie model) {
		copyData(model);
	}

	/**
	 * The constructor.
	 **/
	public ModelMovie(int key, String urlKey, String cover, String date, String title, String directedBy, String writtenBy, String genre, String rating, String plot, String cast, String notes, boolean seen, String aka, String country, String language, String colour, String certification, String mpaa, String webSoundMix, String webRuntime, String awards) {

		setKey(key);
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
		setMpaa(mpaa);
		setWebSoundMix(webSoundMix);
		setWebRuntime(webRuntime);
		setAwards(awards);

		hasGeneralInfoData = true;

		setAdditionalInfo(new ModelAdditionalInfo());
	}

	public ModelMovie(int key, String title) {
		setKey(key);
		setTitle(title);
	}

	public ModelMovie(int key, String title, String urlKey, String cover, String date) {
		setKey(key);
		setTitle(title);
		setUrlKey(urlKey);
		setCover(cover);
		setDate(date);
	}

	public void copyData(ModelEntry model) {
		setKey(model.getKey());
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

		setCoverData(model.getCoverData());
		
		hasGeneralInfoData = model.getHasGeneralInfoData();
		hasAdditionalInfoData = model.getHasAdditionalInfoData();
		hasChangedNotes = model.hasChangedNotes;
		hasAdditionalInfoData = model.getHasAdditionalInfoData();

		setAdditionalInfo(model.getAdditionalInfo());
	}

	public void updateGeneralInfoData() {

		if (getKey() != -1) {

			ModelEntry model = null;
			model = MovieManager.getIt().getDatabase().getMovie(getKey(), true);

			if (model != null) {
				copyData(model);
			}
		}
	}

	public void updateCoverData() {

		if (getKey() != -1) {
			if (MovieManager.getIt().getDatabase().getDatabaseType().equals("MySQL"))
				setCoverData(((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataMovie(getKey()));
		}
	}

	public void updateAdditionalInfoData() {

		if (getKey() != -1) {

			ModelAdditionalInfo tmp = MovieManager.getIt().getDatabase().getAdditionalInfo(getKey(), false);

			if (tmp != null) {
				setAdditionalInfo(tmp);
				hasAdditionalInfoData = true;
			}
		}
	}
}
