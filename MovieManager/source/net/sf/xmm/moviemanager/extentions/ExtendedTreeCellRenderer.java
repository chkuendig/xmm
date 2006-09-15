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
    private static final int COVER_WIDTH = 32;
    private static final int COVER_HEIGHT = 44;
    private static final int SMALL_WIDTH = 20;
    private static final int SMALL_HEIGHT = 20;

    private HashMap coverCache = new HashMap();
    private JLabel label = new JLabel();
    private Icon defaultIconMovie, defaultIconMovieSmall;
    private Icon defaultIconSerie, defaultIconSerieSmall;
    private String folder;
    private boolean highlightEntireRow;
    private JScrollPane scrollPane;
    private MovieManager mm;
    private Color foreground = UIManager.getColor("Tree.foreground");
    private Color background = UIManager.getColor("Tree.background");
    private Color selectionForeground = UIManager.getColor("Tree.selectionForeground");
    private Color selectionBackground = UIManager.getColor("Tree.selectionBackground");

    /**
     * ExtendedTreeCellRenderer constructor
     *
     * @param mm MovieManager
     * @param scrollPane JScrollPane containing JTree
     * @param mode boolean - initial highlightEntireRow setting
     */
    public ExtendedTreeCellRenderer(MovieManager mm, JScrollPane scrollPane, boolean mode) {
        this.mm = mm;
        this.scrollPane = scrollPane;
        this.highlightEntireRow = mode;

        // load and scale default images
        Image movieImage = mm.getImage("/images/movie.png");
        Image serieImage = mm.getImage("/images/serie.png");
        this.defaultIconMovie = new ImageIcon(movieImage.getScaledInstance(COVER_WIDTH, COVER_WIDTH, Image.SCALE_SMOOTH)); // use width as height to obtain square default images
        this.defaultIconSerie = new ImageIcon(serieImage.getScaledInstance(COVER_WIDTH, COVER_WIDTH, Image.SCALE_SMOOTH));
        this.defaultIconMovieSmall = new ImageIcon(movieImage.getScaledInstance(SMALL_WIDTH, SMALL_HEIGHT, Image.SCALE_SMOOTH));
        this.defaultIconSerieSmall = new ImageIcon(serieImage.getScaledInstance(SMALL_WIDTH, SMALL_HEIGHT, Image.SCALE_SMOOTH));

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
        boolean useCovers = mm.getConfig().getUseJTreeCovers();
        boolean useIcons = mm.getConfig().getUseJTreeIcons();

        Object o = ( (DefaultMutableTreeNode) value).getUserObject();
        if (o instanceof ModelEntry) {
            ModelEntry entry = (ModelEntry) o;

            // icon
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
                icon = leaf ? defaultIconMovieSmall : defaultIconSerieSmall;
            }
            label.setIcon(icon);

            // text and colors
            label.setForeground(selected ? selectionForeground : foreground);
            label.setBackground(selected ? selectionBackground : background);
            if (useCovers) {
                Color c = selected ? selectionForeground : foreground;
                String foreground = " color='#" + Integer.toHexString(c.getRed() * 256 * 256 + c.getGreen() * 256 + c.getBlue()) + "'";
                label.setText("<html><font size='4'" + foreground + "><b>" + entry.getTitle() + "</b></font><br><font" + foreground + ">" + entry.getDate() + "</font></html>");
            }
            else {
                label.setText(entry.getTitle());
            }

            // special row highlight
            if (highlightEntireRow) {
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
            folder = MovieManager.getConfig().getCoversFolder();
            String dirSep = mm.getDirSeparator();
            if (!folder.endsWith(dirSep)) {
                folder += dirSep;
            }
        }

        if (mm.getDatabase() instanceof DatabaseMySQL) {
            if (MovieManager.getConfig().getStoreCoversLocally() && new File(folder + entry.getCover()).exists()) {
                return new ImageIcon(mm.getImage(folder + entry.getCover()).getScaledInstance(COVER_WIDTH, COVER_HEIGHT, Image.SCALE_SMOOTH));
            }
            else {
                byte[] coverData = entry.getCoverData();

                if (coverData != null) {
                    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(coverData).getScaledInstance(COVER_WIDTH, COVER_HEIGHT, Image.SCALE_SMOOTH));
                }
                else {
                    return null;
                }
            }
        }
        else if ( (new File(folder + entry.getCover()).exists())) {
            /* Loads the image... */
            return new ImageIcon(mm.getImage(folder + entry.getCover()).getScaledInstance(COVER_WIDTH, COVER_HEIGHT, Image.SCALE_SMOOTH));
        }
        else {
            return null;
        }
    }

    /**
     * setHighlightMode - choose between ordinary treelabel selection and entire
     * row
     *
     * @param mode boolean - true for entire row
     */
    public void setHighlightMode(boolean mode) {
        highlightEntireRow = mode;
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
