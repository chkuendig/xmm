/**
 * @(#)ExtendedTreeCellRenderer.java 1.0 01.07.06 (dd.mm.yy)
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
 * Contact: Bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.extentions;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ExtendedTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private int maxWidth = 0;
    private int rowHeight = 20;
    private boolean highlightEntireRow = false;
    
    JScrollPane scrollPane;
    
    public ExtendedTreeCellRenderer() {}
    
    public ExtendedTreeCellRenderer(JScrollPane scrollPane, boolean mode) {
	highlightEntireRow = mode;
	this.scrollPane = scrollPane;
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	return this;
    }
    
    public void setHighlightMode(boolean mode) {
	highlightEntireRow = mode;
    }
    
    public void setMinimumWidth(int width) {
	maxWidth = width;
    }
    
    public void setRowHeight(int rowHeight) {
	this.rowHeight = rowHeight;
    }
    
    public Dimension getPreferredSize() {
	  
	Dimension dim = super.getPreferredSize();
	int w = 0;
	
	if (this.getText() != null)
	    w = getIconTextGap() + SwingUtilities.computeStringWidth(this.getFontMetrics(this.getFont()), this.getText());
	
	if (maxWidth == 0) {
	    maxWidth = scrollPane.getViewport().getViewSize().width;
	}
	
	if (this.getIcon() != null)
	    w += + this.getIcon().getIconWidth()+4;
	else if(this.getLeafIcon() != null)
	    w += + this.getLeafIcon().getIconWidth()+4;
	else if(this.getOpenIcon() != null)
	    w += + this.getOpenIcon().getIconWidth()+4;
	else if(this.getClosedIcon() != null)
	    w += + this.getClosedIcon().getIconWidth()+4;
	
	if (w > maxWidth)
	    maxWidth = w;
	
	/* If highlightEntireRow == true, using the stored maxWidth on all the cells */
	if (highlightEntireRow)
	    w = maxWidth;
	
	if (dim.width < w)
	    dim.width = w;
	
	dim.height = rowHeight;
	
	return dim;
    }
}
