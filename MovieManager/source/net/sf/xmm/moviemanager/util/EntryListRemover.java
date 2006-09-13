/**
 * @(#)EntryListRemover.java 1.0 03.11.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.util;

import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.models.*;

import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.Database;

import org.apache.log4j.Logger;


public class EntryListRemover {
    
    static Logger log = Logger.getRootLogger();
    
    DefaultTreeModel listModel;
    ArrayList entries;
    TreePath[] selectedPaths;
    
    Database database;
    JTree moviesList;
    int selectedIndex = -1;
    
    public EntryListRemover(Database database, JTree moviesList) {
	this.database = database;
	this.moviesList = moviesList;
	
	listModel = (DefaultTreeModel) moviesList.getModel();
			
	selectedPaths = moviesList.getSelectionPaths();
	entries = new ArrayList(selectedPaths.length);
	
	for (int i = 0; i < selectedPaths.length; i++)
	    entries.add(selectedPaths[i].getLastPathComponent());
	
	selectedIndex = moviesList.getMinSelectionRow();
    }
    
    public void go() {
	final SwingWorker worker = new SwingWorker() {
		public Object construct() {
		    return new Deleter();
		}
	    };
	worker.start();
    }
    
    void updateList(final DefaultMutableTreeNode parent, final DefaultMutableTreeNode child) {
	Runnable updateListModel = new Runnable() {
		public void run() {
		    //listModel.removeElement(element);
		    parent.remove(child);
		    listModel.nodeStructureChanged(parent);
		    MovieManager.getIt().setAndShowEntries();
		}
	    };
	SwingUtilities.invokeLater(updateListModel);
    }
    
    /**
     * The actual removal task.
     * This runs in a SwingWorker thread.
     */
    class Deleter {
        Deleter() {
	    
	    boolean fastremove = false;
	    
	    if (entries.size() > 10)
		fastremove = true;
	    
	    /* Setting the priority of the thread to 4 */
	    Thread.currentThread().setPriority(3);

	    try {
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) MovieManager.getIt().getMoviesList().getModel().getRoot();
		
		while (!entries.isEmpty()) {
		    /* Removes the movie from database... */
		    
		    if (((DefaultMutableTreeNode) entries.get(0)).getUserObject() instanceof ModelEpisode) {
			
			if (database.removeEpisode(((ModelEpisode)((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey()) != 0) {
			    /* Removes from the list... */
			    if (!fastremove)
				updateList((DefaultMutableTreeNode) ((DefaultMutableTreeNode) entries.get(0)).getParent(), (DefaultMutableTreeNode) entries.get(0));
			    
			}
			else
			    log.warn("Error deleting entry:"+ ((ModelEpisode) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey() +" "+ ((ModelEpisode) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).toString());
			
		    }
		    else {
			if (!((DefaultMutableTreeNode) entries.get(0)).isLeaf()) {
			    
			    DefaultMutableTreeNode node = (DefaultMutableTreeNode) entries.get(0);
			    DefaultMutableTreeNode [] child = new DefaultMutableTreeNode[node.getChildCount()]; 
			    
			    for (int o = 0; o < node.getChildCount(); o++)
				child[o] = (DefaultMutableTreeNode) node.getChildAt(o);
			    
			    for (int u = 0; u < child.length; u++) {
				
				if ((MovieManager.getIt().getDatabase()).removeEpisode(((ModelEpisode) child[u].getUserObject()).getKey()) != 0) {
				    if (!fastremove)
					node.remove(child[u]);
				    
				}
				else
				    log.warn("error deleting episode with key:" + ((ModelEpisode) child[u].getUserObject()).getKey());
			    }
			}
			
			if (database.removeMovie(((ModelMovie)((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey()) != 0) {
			    /* Removes from the list... */
			    if (!fastremove)
				updateList(root, (DefaultMutableTreeNode) entries.get(0));
			}
			else
			    log.warn("Error deleting entry:"+ ((ModelMovie) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey() +" "+ ((ModelMovie) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).toString());
		    }
		    entries.remove(0);
		}
		
	    } catch (Exception e) {
		log.error(e.getMessage());
	    }
	    
	    MovieManager.getIt().setDeleting(false);
	    
	    if (fastremove)
		MovieManagerCommandSelect.executeAndReload(selectedIndex);
	    else {
		if (selectedIndex == 0)
		    moviesList.setSelectionRow(selectedIndex);
		else
		    moviesList.setSelectionRow(selectedIndex-1);
		MovieManagerCommandSelect.execute();
	    }
	    MovieManager.getIt().setAndShowEntries();
	}
    }
}
