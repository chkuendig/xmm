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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;

import org.apache.log4j.Logger;

public class MovieManagerCommandImportText extends MovieManagerCommandImportHandler {

	static Logger log = Logger.getRootLogger();

	ArrayList movieList = null;
	
	MovieManagerCommandImportText(ModelImportExportSettings settings) {
		super(settings);
	}
	
	public void handleMovie(int i) {
		
	}
	
	public void execute() {

		log.error("This function does nothing!!!!!");
		
		//DialogTableImport importTable = new DialogTableImport(MovieManager.getDialog(), importSettings.file, importSettings);
		//GUIUtil.showAndWait(importTable, true);
	}
	
	
	public int getMovieListSize() {
		
		if (movieList == null)
			return -1;
		
		return movieList.size();
	}
	
	
	public String getTitle(int i) {
		modelMovieInfo.clearModel();
		
		return ((String) movieList.get(i));
	}
	
	public int addMovie(int i) {
     	
     	int key = 1;
     	String title = (String) movieList.get(i);
     	
     	modelMovieInfo.setTitle(title);
     	
     	 try {
     		 key = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
     		 modelMovieInfo.saveCoverToFile();
          } catch (Exception e) {
              log.error("Saving to database failed.", e);
              key = -1; 
          }
          return key;
     }
	 
	
	public void retrieveMovieList() throws Exception {

		File textFile = new File(settings.filePath);

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

	}
}
