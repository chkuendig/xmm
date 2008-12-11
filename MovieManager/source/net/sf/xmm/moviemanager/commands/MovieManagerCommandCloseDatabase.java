/**
 * @(#)MovieManagerCommandCloseDatabase.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;

public class MovieManagerCommandCloseDatabase implements ActionListener {
    
    
    public static void execute() {
	
    	// If any notes have been changed, they will be saved before seaching
    	MovieManagerCommandSaveChangedNotes.execute();
    	
    	Database database = MovieManager.getIt().getDatabase();
	
	if (database != null) {
	    
	    /* Closes the open database... */
	    database.finalizeDatabase();
	    
	    MovieManager.getDialog().resetTreeModel();
	    MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);
	    MovieManager.getDialog().setAndShowEntries(-1);
	    MovieManager.getIt().setDatabase(null, false);
	    
	    MovieManagerCommandSelect.execute();
	    
	    if (!MovieManager.getConfig().getDatabasePathPermanent())
		MovieManager.getConfig().setDatabasePath("");
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
