/**
 * @(#)MovieManagerCommandLoadList.java 1.0 26.09.05 (dd.mm.yy)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import net.sf.xmm.moviemanager.MovieManager;

public class MovieManagerCommandLoadList implements ActionListener {
    
    void execute(String column) {
	
    	// If any notes have been changed, they will be saved before loading list
    	MovieManagerCommandSaveChangedNotes.execute();
    	
	MovieManager.getConfig().setCurrentList(column);
	MovieManager.getDialog().setListTitle(column);
	
	new MovieManagerCommandFilter("", null, true, true).execute();
    //MovieManagerCommandFilter.execute();
    }
    
    /**
     * Invoked when an action occurs.
     **/
    
    public void actionPerformed(ActionEvent event) {
	MovieManager.log.debug("ActionPerformed: " + event.getActionCommand());
	
	if (event.getSource() instanceof JMenuItem) {
	    ((JMenuItem) event.getSource()).setArmed(true);
	}
	execute(event.getActionCommand());
    }
}
