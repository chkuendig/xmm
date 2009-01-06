/**
 * @(#)MovieManagerCommandSaveChangedNotes.java 1.0 26.09.05 (dd.mm.yy)
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelExportXML;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelSeries;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;




public class MovieManagerCommandImportXMLDatabase extends MovieManagerCommandImportExportHandler {

	
	static Logger log = Logger.getRootLogger();
	
	boolean cancelled = false;
	
	ModelImportExportSettings importSettings;
	
	ArrayList movieList = null;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	MovieManagerCommandImportXMLDatabase(ModelImportExportSettings importSettings) {
		this.importSettings = importSettings;
	}
	
	public void execute() {
		log.debug("Starting XML Datbase import.");
	}
	
	public void done() throws Exception {
		log.debug("Import XML Database finished.");
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	
	public int getMovieListSize() {
		
		if (movieList == null)
			return -1;
		
		return movieList.size();
	}
	
	
	public String getNextMovie(int i) {

		String title = null;
		
    	if (movieList.get(i) instanceof ModelMovie)
    		title = ((ModelMovie) movieList.get(i)).getTitle();
    	else if (movieList.get(i) instanceof ModelSeries) {
    		title = ((ModelSeries) movieList.get(i)).getMovie().getTitle();
    	}
    	
    	return title;
	}
	

	public int addMovie(int i) {

		int key = -1;
		Object model = movieList.get(i);

		if (model instanceof ModelMovie) {

			while(((ModelMovie) model).getAdditionalInfo().getExtraInfoFieldValues().size() < MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false).size())
				((ModelMovie) model).getAdditionalInfo().getExtraInfoFieldValues().add(0, "");

			modelMovieInfo.setModel((ModelMovie) model, false, false);

			try {
				key = (modelMovieInfo.saveToDatabase(importSettings.addToThisList)).getKey();
			} catch (Exception e) {
				log.error("Saving to database failed.", e);
			}
		}
		else if (model instanceof ModelSeries) {

			ModelSeries seriesTmp = (ModelSeries) model;
			ModelEpisode episodeTmp;

			while(((ModelMovie) seriesTmp.getMovie()).getAdditionalInfo().getExtraInfoFieldValues().size() < MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false).size())
				((ModelMovie) seriesTmp.getMovie()).getAdditionalInfo().getExtraInfoFieldValues().add(0, "");

			modelMovieInfo.setModel(seriesTmp.getMovie(), false, false);

			try {
				key = (modelMovieInfo.saveToDatabase(importSettings.addToThisList)).getKey();
			} catch (Exception e) {
				log.error("Saving to database failed.", e);
			}

			int movieKey = modelMovieInfo.model.getKey();

			for (int u = 0; u < seriesTmp.episodes.size(); u++) {
				episodeTmp = (ModelEpisode) seriesTmp.episodes.get(u);
				episodeTmp.setMovieKey(movieKey);

				while(episodeTmp.getAdditionalInfo().getExtraInfoFieldValues().size() < MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false).size())
					episodeTmp.getAdditionalInfo().getExtraInfoFieldValues().add(0, "");

				modelMovieInfo.setModel(episodeTmp, false, false);

				try {
					(modelMovieInfo.saveToDatabase(null)).getKey();
				} catch (Exception e) {
					log.error("Saving to database failed.", e);
				}
			}
		}

		return key;
	}

	public void retrieveMovieList() throws Exception {


		File xmlFile = new File(importSettings.filePath);

		if (!xmlFile.isFile()) {
			log.error("XML file not found:" + xmlFile.getAbsolutePath());
			throw new Exception("");
		}

		Mapping mapping = new Mapping();
		mapping.loadMapping(FileUtil.getFileURL("config/mapping.xml"));

		String encoding = "UTF-8";

		Unmarshaller unmarshaller = new Unmarshaller(ModelExportXML.class);
		unmarshaller.setWhitespacePreserve(true);
		unmarshaller.setMapping(mapping);

		Reader reader = new InputStreamReader(new FileInputStream(xmlFile), encoding);

		Object tmp = unmarshaller.unmarshal(reader);

		if (tmp instanceof ModelExportXML) {
			movieList = ((ModelExportXML) tmp).getCombindedList();

		}
	}
}
