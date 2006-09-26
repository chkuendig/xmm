/**
 * @(#)MovieManagerCommandExportToSimpleHTML.java 1.1 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.DialogQuestion;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.util.*;

import java.io.File;
import java.io.FileWriter;

import javax.swing.DefaultListModel;

import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.models.ModelMovie;

import org.apache.log4j.Logger;

public class MovieManagerCommandExportToSimpleHTML {
    
    static Logger log = Logger.getRootLogger();
    
    static String title;
    static String sortBy;
    static DefaultListModel listModel;
    
    public MovieManagerCommandExportToSimpleHTML(String _title, DefaultListModel _listModel) {
	title = _title;
	listModel = _listModel;
    }
    
    /**
     * Exports the content of the database to html (just title and imdb link)...
     **/
    
    protected static void export(File htmlFile) {
	try {
	    /* Creates the movielist file... */
	    
	    FileWriter writer = new FileWriter(htmlFile);
	    /* The html header... */      
	    writer.write(
			 "<html>\n"+
			 "\n"+
			 "<head>\n"+
			 "  <title>Movies List (Simple View) - Generated by MeD's Movie Manager</title>\n"+
			 "</head>\n"+
			 "\n"+
			 "<body>\n"+
			 "\n"+
			 "<font face=\"arial\" size=\"2\">\n"+
			 "\n"+
			 "<br><CENTER><font size=\"+3\">"+ title +"  "+ "</font></CENTER><br><br><br><br>\n"+
			 "<!-- START Movies Description... -->\n"+
			 "\n"+
			 "<ol>\n");
	    /* Used vars... */
	    String imdb = "imdb";
	    String title = "title";
	    /* For each movie.... */
	    
	    for (int i = 0; i < listModel.getSize(); i++) {
		ModelMovie mode = (ModelMovie) listModel.elementAt(i);
		imdb = mode.getUrlKey();
		title = mode.getTitle() + " ("+ mode.getDate() +")";
		
		if (!imdb.equals("")) {
		    writer.write(
				 "  <li><a href=\"http://www.imdb.com/Title?"+imdb+"\" target=\"_new\" title=\"Jump to IMDB ("+imdb+")\">"+title+"</a></li>\n");
		} else {
		    writer.write(
				 "  <li>"+title+"</li>\n");
		}
	    }
	    /* The html ending...*/
	    writer.write(
			 "</ol>\n"+
			 "\n"+
			 "<!-- END Movies Description... -->\n"+
			 "\n"+
			 "</font>\n"+
			 "\n"+
			 "</body>\n"+
			 "\n"+
			 "</html>\n");
	    writer.close();
	} catch (Exception e) {
	    log.error("", e);
	}
    }
    
    /**
     * Executes the command.
     **/
    public void execute() {
	/* Opens the Export to HTML dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	fileChooser.setFileFilter(new CustomFileFilter(new String[]{"html", "htm"},new String("HTML Files (*.html, *.htm)")));
    
	if (MovieManager.getConfig().getLastMiscDir()!=null) {
	    fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastMiscDir());
	}
	fileChooser.setDialogTitle("Export to HTML - Simple");
	fileChooser.setApproveButtonToolTipText("Export to file");
	fileChooser.setAcceptAllFileFilterUsed(false);
	int returnVal = fileChooser.showDialog(MovieManager.getIt(), "Export");
    
	while (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
	    /* Gets the path... */
	    String path = fileChooser.getSelectedFile().getAbsolutePath().replaceAll(fileChooser.getSelectedFile().getName(),"");
	    String fileName = fileChooser.getSelectedFile().getName();
	    if (!fileName.endsWith(".html")) {
		fileName = fileName + ".html";
	    }
	
	    /* Creates the movielist file... */
	    File htmlFile = new File(path+fileName);
	
	    if (htmlFile.exists()) {
		DialogQuestion question = new DialogQuestion("File already exists", "A file with the chosen filename already exists. Would you like to overwrite the old file?");
		//question.setVisible(true);
		ShowGUI.showAndWait(question, true);
	    
		if (question.getAnswer()) {
		    htmlFile.delete();
		    export(htmlFile);
		    break;
		}
	    
		else {
		    returnVal = fileChooser.showOpenDialog(MovieManager.getIt());
		}
	    }
	    else {
		export(htmlFile);
		break;
	    }
	}
	
	/* Sets the last path... */
	MovieManager.getConfig().setLastMiscDir(fileChooser.getCurrentDirectory());
    }
}
