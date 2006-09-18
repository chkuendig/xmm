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

import java.io.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.*;

public class ExtendedTreeCellRenderer
    implements TreeCellRenderer {
    private HashMap coverCache = new HashMap();
    private JLabel label = new JLabel();
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
        label.setOpaque(true);
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
            if(!useCovers)
                h += 8;
            if(h != lastRowHeight || useCovers != lastUseCovers) {
                int w = useCovers ? h * 32 / 44 : h;  // use height as width to obtain square default images when not showing covers
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
            label.setIcon(icon);

            // text and colors
            label.setForeground(selected ? selectionForeground : foreground);
            label.setBackground(selected ? selectionBackground : background);
            if (useCovers) {
                Color c = selected ? selectionForeground : foreground;
                String foreground = " color='#" + Integer.toHexString(c.getRed() * 256 * 256 + c.getGreen() * 256 + c.getBlue()) + "'";
                int fontSize = 3 + h / 40;
                label.setText("<html><font size='" + fontSize + "'" + foreground + "><b>" + entry.getTitle() + "</b></font><br><font size='" + (fontSize - 1) + "'" + foreground + ">" + entry.getDate() + "</font></html>");
            }
            else {
                label.setText(entry.getTitle());
            }

            // special row highlight
            if (config.getMovieListHighlightEntireRow()) {
                Dimension d = label.getUI().getPreferredSize(label); // hmmm... seems to return right size
                int w = scrollPane.getVisibleRect().width;
                if (d.width < w) {
                    d.width = w;
                }
                label.setPreferredSize(d);
            }
        }
        else {
            label.setText(o.toString());
        }

        return label;
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
            String dirSep = mm.getDirSeparator();
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
