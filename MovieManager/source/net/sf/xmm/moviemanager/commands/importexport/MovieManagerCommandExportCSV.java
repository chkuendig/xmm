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
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogTableExport;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

import com.Ostermiller.util.CSVPrinter;




public class MovieManagerCommandExportCSV extends MovieManagerCommandImportExportHandler {

	
	static Logger log = Logger.getRootLogger();

	
	File output;
	
	Object [][] data = null;
	
	ModelImportExportSettings settings;
	DialogTableExport dialogExportTable = null;
	
	CSVPrinter csvp;
	StringWriter writer = new StringWriter(100);
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	Object [][] tableData = null;
	
	int titleColumnIndex = -1;	
	
	MovieManagerCommandExportCSV(ModelImportExportSettings exportSettings) {
		settings = exportSettings;
		csvp = new CSVPrinter(writer);
	}
	
	
	public void execute() {
		
		data = getDatabaseData();
		dialogExportTable = new DialogTableExport(MovieManager.getDialog(), data, settings);
		GUIUtil.showAndWait(dialogExportTable, true);

		if (dialogExportTable.cancelled)
			setCancelled(true);
		
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	

	public void retrieveMovieList() throws Exception {
		
		tableData = dialogExportTable.retrieveValuesFromTable();
		//movieList = dialogExportTable.retrieveMovieListFromTable();
		
		titleColumnIndex = dialogExportTable.titleColumnIndex;
	}

	public String getNextMovie(int i) {
				
		try {

			if (tableData == null)
				retrieveMovieList();

			if (tableData == null) {
				return null;
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
				
		if (titleColumnIndex == -1) {
			String ret = "";
			
			for (int u = 0; u < tableData[0].length; u++) {
				ret += (String) tableData[i][u] + ",  ";
			}
			return ret;
		}
		else
			return (String) tableData[i][titleColumnIndex];
	}
	
	public int getMovieListSize() {
				
		try {
			if (tableData == null)
				retrieveMovieList();

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return tableData.length;
	}
	
	public void done() throws Exception {
		String output = writer.toString();
		FileUtil.writeToFile(settings.getFile().getAbsolutePath(), new StringBuffer(output), settings.textEncoding);
	}
		
	public int addMovie(int i) {
		
		try {
			String [] data = new String[tableData[0].length];
						
			for (int u = 0; u < data.length; u++) {
				data[u] = (String) tableData[i][u];
			}
			
//			 Write to the printer
			csvp.writeln(data);

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			return -1;
		}
		return 0;
	}
			
	
	public Object [][] getDatabaseData() {

		ArrayList generalInfoFieldNames = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
		ArrayList additionalInfoFieldNames = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
		ArrayList extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();

		int columnCount = generalInfoFieldNames.size() + additionalInfoFieldNames.size() + extraInfoFieldNames.size();

		DefaultListModel movieList = MovieManager.getDialog().getCurrentMoviesList();

		Object [] movies = movieList.toArray();
		Object [][] data = new Object[movies.length][columnCount];

		for (int i = 0; i < movies.length; i++) {

			int tableIndex = 0;
						
			for (int o = 0; o < generalInfoFieldNames.size(); o++) {
				data[i][tableIndex] = ((ModelEntry) movies[i]).getValue((String) generalInfoFieldNames.get(o), "General Info");
				tableIndex++;
			}

			for (int o = 0; o < additionalInfoFieldNames.size(); o++) {
				data[i][tableIndex] = ((ModelEntry) movies[i]).getValue((String) additionalInfoFieldNames.get(o), "Additional Info");
				tableIndex++;
			}
			
			for (int o = 0; o < extraInfoFieldNames.size(); o++) {
				data[i][tableIndex] = ((ModelEntry) movies[i]).getValue((String) extraInfoFieldNames.get(o), "Extra Info");
				tableIndex++;
			}
		}
		return data;
	}

}
