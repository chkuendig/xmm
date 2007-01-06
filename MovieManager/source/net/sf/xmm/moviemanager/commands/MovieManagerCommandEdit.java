/**
 * @(#)MovieManagerCommandEdit.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.DialogMovieInfo;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.ShowGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.tree.DefaultMutableTreeNode;

public class MovieManagerCommandEdit implements ActionListener {
    
    /**
     * Executes the command.
     **/
    protected static void execute() {
	/* Makes sure a movie is selected... */
	int listIndex = -1;
	
	if (MovieManager.getIt().getMoviesList().getModel().getChildCount(MovieManager.getIt().getMoviesList().getModel().getRoot()) > 0) {
	    
	    listIndex = MovieManager.getIt().getMoviesList().getLeadSelectionRow();
	    
	    if (listIndex == -1)
		listIndex = MovieManager.getIt().getMoviesList().getMaxSelectionRow();

	    if (MovieManager.getIt().getMoviesList().getSelectionCount() > 1) {
		MovieManager.getIt().getMoviesList().setSelectionRow(listIndex);
	    }
	}
	
	if (listIndex != -1) {
	    
	    ModelEntry selected = ((ModelEntry) ((DefaultMutableTreeNode) MovieManager.getIt().getMoviesList().getLastSelectedPathComponent()).getUserObject());
	    
	    if (selected.getKey() != -1) {
		DialogMovieInfo dialogMovieInfo = new DialogMovieInfo(selected);
		ShowGUI.show(dialogMovieInfo, true);
	    }
	}
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	MovieManager.log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	execute();
    }
}
