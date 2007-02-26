/**
 * @(#)MovieManagerCommandSaveChangedNotes.java 1.0 26.09.05 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.*;
import java.util.*;

public class MovieManagerCommandSaveChangedNotes implements ActionListener {
    
    /**
     * Executes the command.
     **/
    public static void execute() {
	
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
	ModelEntry model;
	DefaultMutableTreeNode node;
	
	Enumeration enumeration = root.children();
	
	while (enumeration.hasMoreElements()) {
	    
	    node = ((DefaultMutableTreeNode) enumeration.nextElement());
	    model = (ModelEntry) node.getUserObject();
	    
	    if (model.hasChangedNotes) {
		MovieManager.getIt().getDatabase().setGeneralInfo((ModelMovie) model);
	    }
	    
	    /* Has children */
	    if (!node.isLeaf()) {
		Enumeration episodeEnumeration = node.children();
		
		while (episodeEnumeration.hasMoreElements()) {
		    model = (ModelEntry) ((DefaultMutableTreeNode) episodeEnumeration.nextElement()).getUserObject();
		    
		    if (model.hasChangedNotes) {
			MovieManager.getIt().getDatabase().setGeneralInfoEpisode((ModelEpisode) model);
		    }
		}
	    }
	}
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	MovieManager.log.debug("ActionPerformed: " + event.getActionCommand());
	execute();
    }
}
