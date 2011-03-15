/**
 * @(#)MovieManagerCommandExportToSimpleXHTML.java 1.1 26.09.06 (dd.mm.yy)
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
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.HTMLEntities;

import org.apache.log4j.Logger;

public class MovieManagerCommandExportToSimpleXHTML extends MovieManagerCommandExportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandExportToSimpleXHTML.class);

	String title;
	String sortBy;

	File outputFile = null;
	FileWriter writer = null;
	
	ArrayList<ModelMovie> movieList = null;
	  
	public MovieManagerCommandExportToSimpleXHTML(String _title) {
		title = _title;
	}

	public int getMovieListSize() throws Exception {
		return movieList.size();
	}

	public String getTitle(int i) throws Exception {
		ModelMovie model = movieList.get(i);
		return model.getTitle();
	}

	public void retrieveMovieList() throws Exception {
		
		log.debug(this.getClass() + ".retrieveMovieList()");
		
		if (outputFile == null) {
			aborted = true;
			return;
		}
		
		movieList = MovieManager.getDialog().getCurrentMoviesList();
				
		writer = new FileWriter(outputFile);
	
		/* The html header... */      
		writer.write(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"+
				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n"+
				"<head>\n"+
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />\n"+
				"<title>Movies List (Simple View) - Generated by MeD's Movie Manager</title>\n"+
				"</head>\n"+
				"<body>\n"+
				"<br /><CENTER><font size=\"+3\">"+ title +"  "+ "</font></CENTER><br /><br /><br /><br />\n"+
				"<!-- START Movies Description... -->\n"+
				"\n"+
		"<ol>\n");
		
	}
	
	public void done() throws Exception {

		/* The html ending...*/
		writer.write(
				"</ol>\n"+
				"\n"+
				"</body>" +
				"<!-- END Movies Description... -->\n"+
		"</html>\n");
		
		writer.close();
	};

	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandExportHandler#addMovie(int)
	 */
	public ImportExportReturn addMovie(int i) throws Exception {
		
		try {
			
			/* Used vars... */
			String imdb = "imdb";
			String title = "title";

			ModelMovie model = movieList.get(i);

			imdb = model.getUrlKey();
			title = HTMLEntities.htmlentities(model.getTitle()) + " ("+ model.getDate() +")";

			if (!imdb.equals("")) {
				writer.write(
						"  <li><a href=\"http://www.imdb.com/Title?"+imdb+"\" target=\"_blank\" title=\"Jump to IMDB ("+imdb+")\">"+title+"</a></li>\n");
			} else {
				writer.write(
						"  <li>"+title+"</li>\n");
			}
			
		} catch (Exception e) {
			log.error("", e);
			return ImportExportReturn.error;
		}
		         
		return ImportExportReturn.success;
	}

	/**
	 * Executes the command.
	 **/
	public void execute() {
		
		log.debug(this.getClass() + ".execute()");
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if (!handleGetOutputFile()) {
						cancelled = true;
					}
				}
			});
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
		
		
	public boolean handleGetOutputFile() {
		boolean ret = false;
		
		log.debug(this.getClass() + ".handleGetOutputFile()");
		
		try {

			/* Opens the Export to HTML dialog... */
			ExtendedFileChooser fileChooser = new ExtendedFileChooser();
			fileChooser.setFileFilter(new CustomFileFilter(new String[]{"xhtml"},new String("XHTML Files (*.xhtml)")));

			if (MovieManager.getConfig().getLastMiscDir() != null) {
				fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastMiscDir());
			}

			fileChooser.setDialogTitle("Export to XHTML - Simple");
			fileChooser.setApproveButtonToolTipText("Export to file");
			fileChooser.setAcceptAllFileFilterUsed(false);
			
			int returnVal = fileChooser.showDialog(MovieManager.getDialog(), "Export");
	
			while (returnVal == JFileChooser.APPROVE_OPTION) {
	
				/* Gets the path... */
				File selected = fileChooser.getSelectedFile();
				String fileName = selected.getName();

				if (!fileName.endsWith(".xhtml")) {
					fileName = fileName + ".xhtml";
				}

				/* Creates the movielist file... */
				File xhtmlFile = new File(selected.getParent(), fileName);

				if (xhtmlFile.exists()) {
					DialogQuestion question = new DialogQuestion("File already exists", "A file with the chosen filename already exists. Would you like to overwrite the old file?");
					GUIUtil.showAndWait(question, true);
					
					if (question.getAnswer()) {
						xhtmlFile.delete();
						outputFile = xhtmlFile;
						//export(xhtmlFile);
						ret = true;
						break;
					}
					else {
						returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());
					}
				}
				else {
					outputFile = xhtmlFile;
					ret = true;
					break;
				}
			}

			/* Sets the last path... */
			MovieManager.getConfig().setLastMiscDir(fileChooser.getCurrentDirectory());
			return ret;
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		return false;
	}
}
