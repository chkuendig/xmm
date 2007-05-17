/**
 * @(#)MovieManagerCommandExit.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class MovieManagerCommandExit implements ActionListener {

    /**
     * Executes the command.
     **/
	public static void execute() {

		// If any notes have been changed, they will be saved before exiting
		MovieManagerCommandSaveChangedNotes.execute();
			
		MovieManager.log.debug("Shutting down...");

		/* Finalizes the main frame... */
		MovieManager.getDialog().finalize();

		// Saving config file
		MovieManager.getConfig().saveConfig();
		
		Database db = MovieManager.getIt().getDatabase();
		String type = "";

		long time = System.currentTimeMillis();
		
		if (db != null) {
			/* Finalizing database... */
			db.finalizeDatabase();

			type = db.getDatabaseType();
		}

		MovieManager.log.debug("Finalized " + type + " database in " + (System.currentTimeMillis() - time) + " ms.");

		/* Writes the date. */
		MovieManager.log.debug("Log End: "+new Date(System.currentTimeMillis()));

		MovieManager.exit();
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		MovieManager.log.debug("ActionPerformed: " + event.getActionCommand());
		execute();
	}
}
