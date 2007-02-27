/**
 * @(#)ExtendedTreeNode.java 1.0 04.06.06 (dd.mm.yy)
 *
 * Copyright (2003) Bro
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

package net.sf.xmm.moviemanager.swing.extentions;

import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;

import org.dotuseful.ui.tree.AutomatedTreeNode;

import java.util.Collections;

import javax.swing.tree.MutableTreeNode;

//public class ExtendedTreeNode extends DefaultMutableTreeNode implements Comparable  {

public class ExtendedTreeNode extends AutomatedTreeNode implements Comparable  {
// public class ExtendedTreeNode extends SortedTreeNode implements Comparable  {
     
    //private ModelEntry m;
    
    int modelType = 0; /* 0 = ModelMovie, 1 == ModelEpisode */
    
    public ExtendedTreeNode(ModelEntry m) {
	super(m);
	
	if (m instanceof ModelEpisode)
	    modelType = 1;
	    
	//this.m = m;
    }
    
    // public String toString() {
// 	return getUserObject().toString();
//     }
    
    
    public void setUserObject(ModelEntry userObject) {
	
	super.setUserObject(userObject);
    }
    
    public int compareTo(Object o) { 
	ExtendedTreeNode n = (ExtendedTreeNode) o;
	
	if (modelType == 0) {
	    
	    switch (ModelEntry.sort) {
		
	    case 1: {
		return ((ModelEntry) getUserObject()).getTitle().compareToIgnoreCase(((ModelEntry) n.getUserObject()).getTitle());
	    }
	    case 2: {
		return ((ModelEntry) getUserObject()).getDirectedBy().compareToIgnoreCase(((ModelEntry) n.getUserObject()).getDirectedBy());
	    }
	    case 3: {
		return (int) (Double.parseDouble(((ModelEntry) getUserObject()).getSortRating()) - Double.parseDouble(((ModelEntry) n.getUserObject()).getSortRating()));
	    }
	    case 4: {
		return ((ModelEntry) getUserObject()).getDate().compareToIgnoreCase(((ModelEntry) n.getUserObject()).getDate());
	    }
	    case 5: {
		
		return 0;
	    }
	    }
	}
	else {
	    return ((ModelEpisode) getUserObject()).getEpisodeNumber() - ((ModelEpisode) n.getUserObject()).getEpisodeNumber();
	}
	
	return 0;
	
    }
    
   //  public void nodeChanged() {
// 	super.nodeChanged();
//     }
    
    // public void resortChildren() {
// 	sortChildren(children.toArray());
//     }
    
    public int addNode(final MutableTreeNode newChild) {
	
	if (newChild != null && newChild.getParent() == this) {
	    remove(newChild);
	}
	
	int index;
	
	if (children == null) {
	    index = 0;
	} else {
	    index = Collections.binarySearch(children, newChild, null /*comparator*/ );
	}
	if (index < 0) {
	    index = -index - 1;
	}
	insert(newChild, index);
	
	return index;
    }
    
    
    public void addEpisode(MutableTreeNode newChild) {
	if(newChild != null && newChild.getParent() == this)
	    insert(newChild, getChildCount() - 1);
	else
	    insert(newChild, getChildCount());
    }
}
