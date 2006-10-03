/**
 * @(#)MovieManagerCommandUpdateIMDBInfo.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.DialogUpdateIMDbInfo;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelImportSettings;
import net.sf.xmm.moviemanager.util.ShowGUI;

import java.awt.event.*;

import javax.swing.*;


public class MovieManagerCommandUpdateIMDBInfo extends JPanel implements ActionListener{
    
    boolean canceled = true;
    boolean done = false;
    JDialog dbImporter;
        
    boolean cancelAll = false;
   
    ModelImportSettings importSettings;
    
    void createAndShowGUI() {
	
	/* Owner, title, modal=true */
	dbImporter = new JDialog(MovieManager.getIt(), "IMDb Info Updater", true);
	dbImporter.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
	final JComponent newContentPane = new DialogUpdateIMDbInfo(this, dbImporter);
        newContentPane.setOpaque(true);
        dbImporter.setContentPane(newContentPane);
	dbImporter.pack();
	dbImporter.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dbImporter.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		    
		    if (canceled || done) {
			dbImporter.dispose();
			MovieManagerCommandSelect.execute();
		    }
		}
	    });
	
	/*Dispose on escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    
		    if (canceled || done) {
			dbImporter.dispose();
			MovieManagerCommandSelect.execute();
		    }
		}
	    };
	
	dbImporter.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	dbImporter.getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	MovieManager mm = MovieManager.getIt();
	
	setLocation((int) mm.getLocation().getX()+(mm.getWidth()-getWidth())/2,
		    (int) mm.getLocation().getY()+(mm.getHeight()-getHeight())/2);
	
	dbImporter.setLocation((int)mm.getLocation().getX()+(mm.getWidth()- dbImporter.getWidth())/2,
			  (int)mm.getLocation().getY()+(mm.getHeight()- dbImporter.getHeight())/2);
	//dbImporter.setVisible(true);
	ShowGUI.show(dbImporter, true);
    }
    
    public void setCanceled(boolean canceled) {
	this.canceled = canceled;
    }
    
    public void setDone(boolean done) {
	this.done = done;
    }
    
    public void dispose() {
	//dbImporter.setVisible(false);
	ShowGUI.show(dbImporter, false);
    }
    
    public void setCancelAll(boolean value) {
	cancelAll = value;
    }
    
    protected void execute() {
	
	cancelAll = false;
	
	DialogUpdateIMDbInfo importMovie = new DialogUpdateIMDbInfo(this, dbImporter);
	
	if (cancelAll)
	    return;
		
	createAndShowGUI();
    }
    
        /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	MovieManager.log.debug("ActionPerformed: " + event.getActionCommand());
	execute();
    }
}

    
    
    
