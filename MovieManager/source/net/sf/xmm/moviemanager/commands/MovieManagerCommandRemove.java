/**
 * @(#)MovieManagerCommandRemove.java 1.0 26.09.06 (dd.mm.yy)
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
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.ShowGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.util.EntryListRemover;
import net.sf.xmm.moviemanager.models.ModelEpisode;

import org.apache.log4j.Logger;


public class MovieManagerCommandRemove implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    /**
     * Executes the command.
     **/
    protected static void execute() {
	/* Makes sure a movie is selected... */
	JTree movieList = MovieManager.getIt().getMoviesList();
	
	DefaultMutableTreeNode selected = (DefaultMutableTreeNode) movieList.getLastSelectedPathComponent();
	
	int ret = 0;

	if (selected != null && ((ModelEntry) selected.getUserObject()).getKey() != -1) {
	    /* Asks for removal... */
	    boolean multipleRemove = false;
	    DialogQuestion question;
	    Object [] entries = null;
	    
	    if (movieList.getSelectionCount() > 1)
		multipleRemove = true;
	    
	    DefaultMutableTreeNode root = (DefaultMutableTreeNode) movieList.getModel().getRoot();
	    
	    if (multipleRemove) {
		
		TreePath[] selectedPaths = movieList.getSelectionPaths();
		entries = new Object[selectedPaths.length];
		
		for (int i = 0; i < selectedPaths.length; i++) {
		    entries[i] = ((DefaultMutableTreeNode) selectedPaths[i].getLastPathComponent()).getUserObject();
		}
		question = new DialogQuestion("Remove Movie", "Are you sure you want to remove the following "+entries.length+ " entries", entries);
	    }
	    else {
		question = new DialogQuestion("Remove Movie", "Are you sure you want to remove '"+ selected.getUserObject() +"'?");
	    }
	    
	    //question.setVisible(true);
	    ShowGUI.show(question, true);
	    
	    if (question.getAnswer()) {
		
		if (multipleRemove) {
		    
		    MovieManager.getIt().setDeleting(true);
		    
		    EntryListRemover deleter = new EntryListRemover(MovieManager.getIt().getDatabase(), movieList);
		    movieList.clearSelection();
		    deleter.go();
		}
		else {
		    
		    ModelEntry entry =  (ModelEntry) selected.getUserObject();
		    
		    /* If episode */
		    if (entry instanceof ModelEpisode) {
			
			/* Removes episode from database */
			if ((ret = MovieManager.getIt().getDatabase().removeEpisode(entry.getKey())) == 0) {
			    
			    /* Ensures that a new node will be selected */
			    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selected.getParent();
			    
			    DefaultMutableTreeNode newSelectedEntry;
			    TreePath newSelectionPath;
			    
			    if ((newSelectedEntry = selected.getNextSibling()) != null)
				newSelectionPath = new TreePath(new Object[]{root, parent, newSelectedEntry});
			    else if ((newSelectedEntry = selected.getPreviousSibling()) != null)
				newSelectionPath = new TreePath(new Object[]{root, parent, newSelectedEntry});
			    else
				newSelectionPath = new TreePath(new Object[]{root, parent});
			    
			    /* Removes from the list... */
			    parent.remove(selected);
			    
			    ((DefaultTreeModel) MovieManager.getIt().getMoviesList().getModel()).reload(parent);
			    
			    /* Sets the new selected node (path) */
			    movieList.setSelectionPath(newSelectionPath);
			}
			else {
			    log.warn("Error deleting entry:"+ entry.getKey() +" "+ entry.toString());
			}
		    }
		    else {
			
			/* If it contains children (episodes) */
			if (!selected.isLeaf()) {
			    
			    DefaultMutableTreeNode [] child = new DefaultMutableTreeNode[selected.getChildCount()]; 
			    
			    for (int o = 0; o < selected.getChildCount(); o++)
				child[o] = (DefaultMutableTreeNode) selected.getChildAt(o);
			    
			    for (int u = 0; u < child.length; u++) {
				
				if ((ret = MovieManager.getIt().getDatabase().removeEpisode(((ModelEpisode) child[u].getUserObject()).getKey())) == 0)
				    selected.remove(child[u]);
				else
				    log.warn("Error deleting episode with key:"+ ((ModelEpisode) child[u].getUserObject()).getKey());
			    }
			}
			/* Removes the movie from the database... */
			if ((ret = MovieManager.getIt().getDatabase().removeMovie(entry.getKey())) == 0) {
			    
			    /* Ensures that a new node will be selected */
			    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selected.getParent();
			    DefaultMutableTreeNode newSelectedEntry;
			    TreePath newSelectionPath;
			    
			    if ((newSelectedEntry = selected.getNextSibling()) != null) {
				newSelectionPath = new TreePath(new Object[]{parent, newSelectedEntry});
			    }
			    else if ((newSelectedEntry = selected.getPreviousSibling()) != null) {
				newSelectionPath = new TreePath(new Object[]{parent, newSelectedEntry});
			    }
			    else {
				newSelectionPath = new TreePath(new Object[]{parent});
			    }
			    /* Removes from the list... */
			    parent.remove(selected);
			    
			    //((DefaultTreeModel) MovieManager.getIt().getMoviesList().getModel()).reload(parent);
			    ((DefaultTreeModel) movieList.getModel()).nodeStructureChanged(root);
			    
			    /* Sets the new selected node (path) */
			    movieList.setSelectionPath(newSelectionPath);
			}
			else {
			    log.warn("Error deleting entry:"+ entry.getKey() +" "+ entry.toString());
			}
		    }
		    MovieManager.getIt().setAndShowEntries();
		    MovieManagerCommandSelect.execute();
		}
	    }
	}
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: " + event.getActionCommand());
	execute();
	MovieManager.getIt().getMoviesList().requestFocus(true);
    }
}
