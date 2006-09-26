/**
 * @(#)ExtendedTreeCellRenderer.java 1.0 26.09.06 (dd.mm.yy)
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

import java.io.*;
import java.util.*;
import java.lang.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import javax.swing.event.*;

import java.awt.event.*;
import java.beans.*;


import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.*;

public class ExtendedTreeCellRenderer extends JLabel implements TreeCellRenderer, ComponentListener {
    
    
    private HashMap coverCache = new HashMap();
    private Icon defaultIconMovie;
    private Icon defaultIconSerie;
    private String folder;
    private JScrollPane scrollPane;
    private MovieManager mm;
    private MovieManagerConfig config;
    private Color foreground = UIManager.getColor("Tree.foreground");
    private Color background = UIManager.getColor("Tree.background");
    private Color selectionForeground = UIManager.getColor("Tree.selectionForeground");
    private Color selectionBackground = UIManager.getColor("Tree.selectionBackground");
    private int lastRowHeight = -1;
    private boolean lastUseCovers = false;
    private Image movieImage;
    private Image serieImage;
    
    private static int maxWidth = 0;
    private static int viewPortWidth = 0;
    
    /**
     * ExtendedTreeCellRenderer constructor
     *
     * @param mm MovieManager
     * @param scrollPane - JScrollPane containing JTree
     */
    public ExtendedTreeCellRenderer(MovieManager mm, JScrollPane scrollPane) {
        this(mm, scrollPane, MovieManager.getConfig());
    }
    
    
    /**
     * ExtendedTreeCellRenderer constrictor
     *
     * @param mm MovieManager
     * @param scrollPane - JScrollPane containing JTree
     * @param config MovieManagerConfig
     */
    public ExtendedTreeCellRenderer(MovieManager mm, JScrollPane scrollPane, MovieManagerConfig config) {
        this.mm = mm;
        this.scrollPane = scrollPane;
        this.config = config;

        // load and scale default images
        movieImage = mm.getImage("/images/movie.png");
        serieImage = mm.getImage("/images/serie.png");
        setOpaque(true);
	
	scrollPane.addComponentListener(this);
    }
    
    public void	componentHidden(ComponentEvent e) {}
    public void	componentMoved(ComponentEvent e) {}
    public void	componentShown(ComponentEvent e){}
    
    /* Size of viewPort changed */
    public void componentResized(ComponentEvent e) {
	
	if (!config.getMovieListHighlightEntireRow())
	    return;
	
	int newViewPortWidth = scrollPane.getViewport().getExtentSize().width;
	
	if (viewPortWidth != newViewPortWidth) {
	    viewPortWidth = newViewPortWidth;
	    
	    TreePath[] selPaths = mm.getMoviesList().getSelectionPaths();
	    
	    mm.getMoviesList().clearSelection();
	    mm.getMoviesList().addSelectionPaths(selPaths);
	}
    }
    
    /**
     * Returns specialized JLabel for JTree node display
     *
     * @param tree JTree
     * @param value Object
     * @param selected boolean
     * @param expanded boolean
     * @param leaf boolean
     * @param row int
     * @param hasFocus boolean
     * @return specialized JLabel
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        boolean useCovers = config.getUseJTreeCovers();
        boolean useIcons = config.getUseJTreeIcons();

        Object o = ( (DefaultMutableTreeNode) value).getUserObject();
        if (o instanceof ModelEntry) {
            ModelEntry entry = (ModelEntry) o;

            // icon
            int h = config.getMovieListRowHeight();
            if (!useCovers)
                h += 8;
	    
            if ((h != lastRowHeight) || (useCovers != lastUseCovers)) {
		/* Use height as width to obtain square default images when not showing covers. */
                int w = useCovers ? h * 32 / 44 : h; 
                defaultIconMovie = new ImageIcon(movieImage.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                defaultIconSerie = new ImageIcon(serieImage.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                lastRowHeight = h;
                lastUseCovers = useCovers;
                clearCoverCache();
            }
            
	    Icon icon = null;
            
	    if (useCovers) {
                if (entry.getCover() != null && entry.getCover().length() > 0) {
                    icon = (Icon) coverCache.get(entry.getCover());
                    if (icon == null) {
                        icon = loadCover(entry);
                        if (icon != null) {
                            coverCache.put(entry.getCover(), icon);
                        }
                    }
                }
                if (icon == null) {
                    icon = leaf ? defaultIconMovie : defaultIconSerie;
                }
            }
            else if (useIcons) {
                icon = leaf ? defaultIconMovie : defaultIconSerie;
            }
            setIcon(icon);

            /* text and colors */
            setForeground(selected ? selectionForeground : foreground);
            setBackground(selected ? selectionBackground : background);
	    
            if (useCovers) {
                Color c = selected ? selectionForeground : foreground;
                String foreground = " color='#" + Integer.toHexString(c.getRed() * 256 * 256 + c.getGreen() * 256 + c.getBlue()) + "'";
                int fontSize = 3 + h / 40;
                setText("<html><font size='" + fontSize + "'" + foreground + "><b>" + entry.getTitle() + "</b></font><br><font size='" + (fontSize - 1) + "'" + foreground + ">" + entry.getDate() + "</font></html>");
            }
            else {
                setText(entry.getTitle());
            }
	}
        else {
            setText(o.toString());
        }

        return this;
    }
    
    
    public Dimension getPreferredSize() {
	  
	Dimension dim = super.getPreferredSize();
	int w = getIconTextGap() + SwingUtilities.computeStringWidth(this.getFontMetrics(this.getFont()), this.getText());
	
	if (getIcon() != null)
	    w += + getIcon().getIconWidth() + 4;
	
	if (w > maxWidth)
	    maxWidth = w;
	
	/* If highlightEntireRow == true, using the stored maxwidth on all the cells */
	if (config.getMovieListHighlightEntireRow())
	    w = maxWidth > viewPortWidth ? maxWidth : viewPortWidth;
	
	dim.width = w;
	dim.height = lastRowHeight;
	
	return dim;
    }
    
    
    /**
     * loadCover load cover from disk or database
     *
     * @param entry ModelEntry - movie to load cover for
     * @return Icon - loaded cover
     */
    private Icon loadCover(ModelEntry entry) {
        if (folder == null) {
            folder = config.getCoversFolder();
            String dirSep = MovieManager.getDirSeparator();
            if (!folder.endsWith(dirSep)) {
                folder += dirSep;
            }
        }

        int h = config.getMovieListRowHeight();
        int w = h * 32 / 44; // hardcoded aspect ratio

        if (mm.getDatabase() instanceof DatabaseMySQL) {
            if (config.getStoreCoversLocally() && new File(folder + entry.getCover()).exists()) {
                return new ImageIcon(mm.getImage(folder + entry.getCover()).getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
            else {
                byte[] coverData = entry.getCoverData();

                if (coverData != null) {
                    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(coverData).getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
                else {
                    return null;
                }
            }
        }
        else if ( (new File(folder + entry.getCover()).exists())) {
            /* Loads the image... */
            return new ImageIcon(mm.getImage(folder + entry.getCover()).getScaledInstance(w, h, Image.SCALE_SMOOTH));
        }
        else {
            return null;
        }
    }
    
    
    /**
     * clear cached covers
     */
    public void clearCoverCache() {
        coverCache.clear();
    }
    
    
    /**
     * removeCoverFromCache - remove specified cover from cache
     *
     * @param cover - as represented in Model- or MovieEntry cover field
     */
    public void removeCoverFromCache(String cover) {
        coverCache.remove(cover);
    }
}
