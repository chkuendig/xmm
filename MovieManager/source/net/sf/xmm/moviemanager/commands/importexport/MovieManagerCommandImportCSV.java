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

import java.io.FileInputStream;
import java.io.InputStreamReader;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogTableImport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.util.GUIUtil;

import com.Ostermiller.util.CSVParse;
import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;




public class MovieManagerCommandImportCSV extends MovieManagerCommandImportExportHandler {



	ModelMovie movie = null;

	Object [][] data;
	int len = -1;

	ModelImportExportSettings settings;
	DialogTableImport dialogImportTable = null;
	
	MovieManagerCommandImportCSV(ModelImportExportSettings settings) {
		this.settings = settings;
	}



	public void execute() {

		Object [][] data = readData();

		dialogImportTable = new DialogTableImport(MovieManager.getDialog(), data, settings);
		GUIUtil.showAndWait(dialogImportTable, true);
		
		if (dialogImportTable.cancelled)
			setCancelled(true);
	}

	public boolean isCancelled() {
		return cancelled;
	}


	public int getMovieListSize() {

		if (movieList == null)
			movieList = dialogImportTable.retrieveMovieListFromTable();
		
		return movieList.size();
	}

	
	public String getNextMovie(int i) {
		modelMovieInfo.clearModel();

		try {

			if (movieList == null)
				retrieveMovieList();

			if (movieList == null) {
				return null;
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		String title = ((ModelMovie) movieList.get(i)).getTitle();

		if (title != null && !title.equals("")) {

			/* First resetting the info already present */

			if (settings.multiAddIMDbSelectOption != -1) {

				executeCommandGetIMDBInfoMultiMovies(title, title, settings.multiAddIMDbSelectOption, (ModelMovie) movieList.get(i));
			}
			return (String) ((ModelMovie) movieList.get(i)).getTitle();
		}

		return null;
	}



	public int addMovie(int i) {

		int ret = -1;
		Object tmp = movieList.get(i);

		modelMovieInfo.setModel((ModelMovie) tmp, false, false);

		try {
			ret = modelMovieInfo.saveToDatabase(settings.addToThisList).getKey();
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
		}

		return ret;
	}
	
	public void done() throws Exception {
		log.debug("CSV import completetd");
	}

	//  Eetrieved the data from the table and stores it in movieList in super class.
	public void retrieveMovieList() {
		movieList = dialogImportTable.retrieveMovieListFromTable();
	}



	// Returns the data read from CSV file
	public Object [][] readData() {

		try {
			CSVParse cvsParser;

			InputStreamReader reader;
			
			if (settings.getTextEncoding() == null)
				reader = new InputStreamReader(new FileInputStream(settings.getFile()));
			else
				reader = new InputStreamReader(new FileInputStream(settings.getFile()), settings.getTextEncoding());
			
			if (settings.csvSeparator.length() == 0)
				cvsParser = new CSVParser(reader);
			else
				cvsParser = new CSVParser(reader, settings.csvSeparator.charAt(0));
			//ExcelCSVParser cvsParser = new ExcelCSVParser(new FileReader(file));

			cvsParser = new LabeledCSVParser(cvsParser);

			data = cvsParser.getAllValues();
			len = data[0].length;

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return data;
	}


	/*
	public void getTableDataFromCSV() {

	try {

		movieList = new ArrayList(10);

		JTable table = settings.table;

		TableModel tableModel = table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		int columnCount = table.getModel().getColumnCount();

		TableColumn tmpColumn;
		FieldModel fieldModel;
		String tableValue;
		ModelMovie tmpMovie;
		boolean valueStored = false;

		for (int row = 0; row < tableModel.getRowCount(); row++) {

			tmpMovie = new ModelMovie();
			valueStored = false;

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {

				tmpColumn = columnModel.getColumn(columnIndex);
				Object val = tmpColumn.getHeaderValue();

				if (!(val instanceof FieldModel)) {
					continue;
				}

				fieldModel = (FieldModel) val;

				// column has been assigned an info field 
				if (!fieldModel.toString().trim().equals("")) {

					tableValue = (String) table.getModel().getValueAt(row, columnIndex);
					fieldModel.setValue(tableValue);

					fieldModel.validateValue();

					if (tmpMovie.setValue(fieldModel)) {
						valueStored = true;
					}
				}
			}

			if (valueStored && tmpMovie.getTitle() != null && !tmpMovie.equals("")) {
				movieList.add(tmpMovie);
			}
		}
	}
	catch (Exception e) {
		log.error("", e);
	}	
	 */





/*
	public static void execute(String outputFile) {

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
		ModelEntry model;
		DefaultMutableTreeNode node;

		Enumeration enumeration = root.children();

		StringBuffer strBuf = new StringBuffer();

		try {

			File file = new File(outputFile);

			if (file.isFile()) {
				DialogTableExport importTable = new DialogTableExport(this, file, importSettings);
				GUIUtil.showAndWait(importTable, true);

				if (importTable.canceled)
					setCanceled(true);

				importSettings.table = importTable.getSettings().table;
				dispose();

			}

			//strBuf

//			 Create the printer
			CSVPrinter csvp = new CSVPrinter(
			    System.out
			);

//			 Write to the printer
			csvp.writeln(
			    new String[]{
			        "hello","world"
			    }
			);

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			return;
		}
	}
 */
}


