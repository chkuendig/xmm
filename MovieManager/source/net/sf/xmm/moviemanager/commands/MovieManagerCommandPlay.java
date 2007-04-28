/**
 * @(#)MovieManagerCommandPlay 1.0 10.10.06 (dd.mm.yy)
 *
 * Copyright (2003) 
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
 * Contact: 
 **/

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.DialogMovieManager;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.FileUtil;

public class MovieManagerCommandPlay implements ActionListener{

	static Logger log = Logger.getRootLogger();
	
    public void actionPerformed(ActionEvent e) {

	MovieManager.log.debug("ActionPerformed: " + e.getActionCommand());
	try {
	    execute();
	} catch (IOException e1) {
	} catch (InterruptedException e1) {
	    e1.printStackTrace();
	}
    }

    protected static void execute() throws IOException, InterruptedException {
	// check if there is a file selected
	int listIndex = -1;
    DialogMovieManager movieManagerInstance = MovieManager.getDialog();
	TreeModel moviesListTreeModel = movieManagerInstance.getMoviesList().getModel();

	// check whether movies list has entries
	if (moviesListTreeModel.
	    getChildCount(moviesListTreeModel.getRoot()) > 0) {

	    listIndex = movieManagerInstance.getMoviesList().getLeadSelectionRow();

	    if (listIndex == -1)
		listIndex = movieManagerInstance.getMoviesList().getMaxSelectionRow();

	    if (movieManagerInstance.getMoviesList().getSelectionCount() > 1) {
		movieManagerInstance.getMoviesList().setSelectionRow(listIndex);
	    }
	}
	
	if (listIndex != -1) {
	    
	    ModelEntry selected = ((ModelEntry) ((DefaultMutableTreeNode) movieManagerInstance.getMoviesList().
	            getLastSelectedPathComponent()).getUserObject());
	    
	    if (selected.getKey() != -1) {
	        
            MovieManagerConfig mmc = MovieManager.getConfig();
            
	        String fileLocation = selected.getAdditionalInfo().getFileLocation();
	        String cmd;
            
            if (FileUtil.isWindows() && mmc.getUseDefaultWindowsPlayer()) {
                cmd = "rundll32 url.dll,FileProtocolHandler ";
             
                if (fileLocation.indexOf("*") != -1)
                    fileLocation = fileLocation.substring(0, fileLocation.indexOf("*"));
                
                fileLocation = "file://" + fileLocation;
                
            }
            else {
                // Splitting on *, each file is enclosed by quotes.	
                String [] files = fileLocation.split("\\*");
                fileLocation = "";
                
                for (int i = 0; i < files.length; i++)
                	fileLocation += "\"" + files[i] + "\" ";
                                
                cmd = mmc.getMediaPlayerPath();
                
                System.err.println("cmd:" + cmd);
                
                if (!new File(cmd).isFile() && 1 == 0){
                    
                    JFileChooser chooser = new JFileChooser();
                    int returnVal = chooser.showDialog(null, "Launch");
                    if(returnVal != JFileChooser.APPROVE_OPTION)
                        return;
                    
                    cmd = chooser.getSelectedFile().getCanonicalPath();
                    mmc.setMediaPlayerPath(cmd);
                }
                cmd += " ";
            }
	    
            //fileLocation = "\"" + fileLocation + "\"";
            
            //fileLocation = "/uio/aristoteles/s06/bendiko/movieManager/media\\ files/inputSmall2.avi";
            
            String [] args = new String[] {cmd, fileLocation};
            
            System.err.println("CMD1:" + cmd);
            
            cmd = cmd + fileLocation;
            
            log.debug("CMD2:" + cmd);
	    Process p = Runtime.getRuntime().exec(cmd);
	    }
	}
    }
}
