/**
 * @(#)ExtendedTreeCellRenderer.java 1.0 24.11.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.swing.extentions;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.swing.extentions.events.NewDatabaseLoadedEvent;
import net.sf.xmm.moviemanager.swing.extentions.events.NewDatabaseLoadedEventListener;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

public class ExtendedTreeCellRenderer extends JLabel implements TreeCellRenderer, NewDatabaseLoadedEventListener {

	Logger log = Logger.getLogger(getClass());
	
	private HashMap<String, Icon> coverCache = new HashMap<String, Icon>();
	private Icon defaultIconMovie;
	private Icon defaultIconSerie;
	private String folder;
	private MovieManager mm = MovieManager.getIt();
	private MovieManagerConfig config;
	private int lastRowHeight = -1;
	private boolean lastUseCovers = false;
	private Image movieImage;
	private Image serieImage;

	int coverWidth;
	
	StringBuffer coverTitleBuf = new StringBuffer();
		
	private static Color background;
	private static Color selectionBackground;
		
	JTree tree;
	
	Map<ModelEntry, Object> views = new HashMap<ModelEntry, Object>();
	
	public void removeNode(DefaultMutableTreeNode node) {
		
		if (views.remove(node.getUserObject()) == null) {
			log.warn("No mapping existed for entry:" + node.getUserObject());
		}
	}
	
	public void removeEntry(ModelEntry entry) {
		views.remove(entry);
	}
	
	/**
	 * ExtendedTreeCellRenderer constructor
	 *
	 * @param mm MovieManager
	 * @param scrollPane - JScrollPane containing JTree
	 */
	public ExtendedTreeCellRenderer(JTree tree, JScrollPane scrollPane) {
		this(tree, scrollPane, MovieManager.getConfig());
		this.tree = tree;
	}


	/**
	 * ExtendedTreeCellRenderer constructor
	 *
	 * @param dmm DialogMovieManager
	 * @param scrollPane - JScrollPane containing JTree
	 * @param config MovieManagerConfig
	 */
	public ExtendedTreeCellRenderer(JTree tree, JScrollPane scrollPane, MovieManagerConfig config) {
		
		this.config = config;
		this.tree = tree;
		
		if (tree != null && tree.getModel() != null) {
			final JTree finalTree = tree;
			scrollPane.getViewport().addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					TreeNode node = (DefaultMutableTreeNode) finalTree.getLastSelectedPathComponent();
					
					if (finalTree.getModel() != null)
						((DefaultTreeModel) finalTree.getModel()).nodeChanged(node); 
				}
			});
		}
		
		// load and scale default images
		movieImage = FileUtil.getImage("/images/movie.png");
		serieImage = FileUtil.getImage("/images/serie.png");
		setOpaque(true);

		setDefaultColors();	
	}


	public static int minViewWidth = -1;
	
	int treeMinWidth = -1;
	int treePrefWidth = -1;
	
	// @Override
	/*	 
	 
	  	 public java.awt.Dimension getPreferredSize1() {
	 
		 java.awt.Dimension size = super.getPreferredSize();
		// System.err.println("\nsize:" + size);
		 
		 //size.width = Short.MAX_VALUE;
		 	
		 if (tree instanceof ExtendedJTree) {
	
			 if (((ExtendedJTree) tree).viewPortPrefWidth != -1) {
				 //size.width = ((ExtendedJTree) tree).viewPortPrefWidth;
		
				 int viewPortPrefWidth = ((ExtendedJTree) tree).viewPortPrefWidth;
				 int treeWidth = ((ExtendedJTree) tree).treeWidth;
				 treePrefWidth = ((ExtendedJTree) tree).treePrefWidth;
				 int treeMaxWidth = ((ExtendedJTree) tree).treeMaxWidth;
				 treeMinWidth = ((ExtendedJTree) tree).treeMinWidth;
				
				 
				 //size.width += 20 + 17;
				 //size.width += 17 + 20;
				 
				 System.err.println("size.width:" + size.width);
				 
				 //System.err.println("size.width("+size.width+") < minViewWidth("+minViewWidth+"):" + (size.width < minViewWidth));
				 
				 if (size.width < (minViewWidth- (30))) {
					 System.err.println("size.width("+size.width+") < minViewWidth("+minViewWidth+")");
					 //	 size.width = minViewWidth - 17;
					 //size.width = minViewWidth - (17 + 20);
					 size.width = minViewWidth - (19 + 20);
				 }
				 else {
					 System.err.println("not less ----------------------------------------");
				 }
				 
				 //if (size.width < treePrefWidth)
				//	 size.width = treePrefWidth - 20;
					 
				 System.err.println("getIconTextGap:" + getIconTextGap());
				 
				 if (getIcon() != null)
					 System.err.println("getIconWidth:" + getIcon().getIconWidth());
				 
				// else {
				//	 size.width += 17;
				 //}
				 //System.err.println("size.width:" + size.width);
				 
				//System.err.println("Title:" + getText());
				 
				 
//				    System.err.println();
//				 System.err.println("viewPortPrefWidth:" + viewPortPrefWidth);
//				 System.err.println("tree      Width:" + treeWidth);
//				 System.err.println("tree pref Width:" + treePrefWidth);
//				 System.err.println("tree max  Width:" + treeMaxWidth);
//				 System.err.println("tree min  Width:" + treeMinWidth);
//				 				 
//				 System.err.println("Label Preferred:" + size.width);
//				 							 				
//				 System.err.println("getIconTextGap:" + getIconTextGap());
//				 
//				
//				 
//				 if (getIcon() != null)
//					 System.err.println("getIconWidth:" + getIcon().getIconWidth());
//				 
				  
								 
				 //int calculatedwidth = javax.swing.SwingUtilities.computeStringWidth(getFontMetrics(getFont()), getText());
				 //System.err.println("computeStringWidth:" + calculatedwidth);

//				 System.err.println("getIconTextGap:" + getIconTextGap());
//
//				 if (getIcon() != null) {
//					 System.err.println("getIconWidth:" + getIcon().getIconWidth());
//					 //size.width -= getIcon().getIconWidth();
//					// size.width -= 17;
//					 }
	
				 
				 setBackground(Color.red);
			 }
			// System.err.println("getPrefe + rredSize" + ((ExtendedJTree) tree).viewPortPrefWidth);
		 }
		 
		 return size;
	 }

	
	 
	// @Override
	 public void setBounds1(final int x, final int y, int width, final int height) {
		 //System.err.println("tree.getWidth():" + tree.getWidth());
		 //System.err.println("x:" + x);
		 
		 width += 20 + 17;
		 
		 if (width < minViewWidth) {
			 //System.err.println("size.width("+size.width+") < minViewWidth("+minViewWidth+")");
			 //	 size.width = minViewWidth - 17;
			 //size.width = minViewWidth - (17 + 20);
			 System.err.println("setBounds set from "+width+ " to width:" + minViewWidth);
			width = minViewWidth + 17 +20;
		 }
		 
		 System.err.println("width:" + width);
		 System.err.println("treePrefWidth:" + treePrefWidth);
		 
		 if (width < treePrefWidth)
			 width = treePrefWidth + 20 + 170;
		 
		 System.err.println("width:" + width); 
		 
		// else
		//	 width += 17;
		 
		 super.setBounds(x, y, width, height);
		 //super.setBounds(x, y, Math.min(tree.getWidth() - x, width), height);
	 }
	*/
	 
	public void newDatabaseLoaded(NewDatabaseLoadedEvent evt) {
		folder = null;
		clearCoverCache();
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

		try {

			boolean useCovers = config.getUseJTreeCovers();
			boolean useIcons = config.getUseJTreeIcons();

			Object o = ((DefaultMutableTreeNode) value).getUserObject();

			setBackground(selected ? selectionBackground : background);

			if (o instanceof ModelEntry) {
				ModelEntry entry = (ModelEntry) o;

				// icon
				int h = config.getMovieListRowHeight();

				if (!useCovers)
					h += 8;

				if ((h != lastRowHeight) || (useCovers != lastUseCovers)) {
					/* Use height as width to obtain square default images when not showing covers. */
					int w = useCovers ? h * 32 / 44 : h; 

					coverWidth = w;
					defaultIconMovie = new ImageIcon(movieImage.getScaledInstance(w, h, Image.SCALE_SMOOTH));
					defaultIconSerie = new ImageIcon(serieImage.getScaledInstance(w, h, Image.SCALE_SMOOTH));
					lastRowHeight = h;
					lastUseCovers = useCovers;
					clearCoverCache();
				}

				Icon icon = null;

				if (useCovers) {

					if (entry.getKey() != -1) {

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
				}
				else if (useIcons) {
					icon = leaf ? defaultIconMovie : defaultIconSerie;
				}

				setIcon(icon);

				if (useCovers) {

					Object view = views.get(entry);
					
					if (view != null) {
						putClientProperty("html", view);
					}
					else {

						int fontSize = 3 + h / 40;

						//coverTitleBuf.setLength(0);
						coverTitleBuf = new StringBuffer();
						coverTitleBuf.append("<html><font size='");
						coverTitleBuf.append(fontSize);
						coverTitleBuf.append("'><b>");
						coverTitleBuf.append(entry.isEpisode() ? ((ModelEpisode) entry).getEpisodeTitle() : entry.getTitle());
						coverTitleBuf.append("</b></font><br><font size='");
						coverTitleBuf.append((fontSize - 1));
						coverTitleBuf.append("'>");
						coverTitleBuf.append(entry.getDate());
						coverTitleBuf.append("</font></html>");
						
						setText(coverTitleBuf.toString());
						views.put(entry, getClientProperty("html"));
					}
				}
				else {
					setText(entry.isEpisode() ? ((ModelEpisode) entry).getEpisodeTitle() : entry.getTitle());
				}

			}
			else {
				setText(o.toString());
			}

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		javax.swing.JPanel p = new javax.swing.JPanel();
		//p.setPreferredSize(new java.awt.Dimension(tree.getWidth(), tree.getRowHeight()));
		//System.err.println("size:" + new java.awt.Dimension(tree.getWidth(), tree.getRowHeight()));
		p.setLayout(new java.awt.BorderLayout());
		p.add(this, BorderLayout.CENTER);
		
		return p;
	}

		
	
	public static void setDefaultColors() {
			
		if (UIManager.getColor("ScrollPane.background") != null)
			background = UIManager.getColor("ScrollPane.background");
		
		if (background == null) {
			background = UIManager.getColor("TaskPane.background");
		}
				
		if (UIManager.getColor("Tree.selectionBackground") != null)
			selectionBackground = UIManager.getColor("Tree.selectionBackground");
				
	}
	
	
	/**
	 * loadCover load cover from disk or database
	 *
	 * @param entry ModelEntry - movie to load cover for
	 * @return Icon - loaded cover
	 */
	private Icon loadCover(ModelEntry entry) {

		if (folder == null) {
			folder = config.getCoversPath();
		}

		int h = config.getMovieListRowHeight();
		coverWidth = h * 32 / 44; // hardcoded aspect ratio

		if (mm.getDatabase().isMySQL()) {
			if (config.getStoreCoversLocally() && new File(folder, entry.getCover()).exists()) {
				return new ImageIcon(FileUtil.getImage(folder + File.separator + entry.getCover()).getScaledInstance(coverWidth, h, Image.SCALE_SMOOTH));
			}
			else {
				byte[] coverData = entry.getCoverData();

				if (coverData != null) {
					return new ImageIcon(Toolkit.getDefaultToolkit().createImage(coverData).getScaledInstance(coverWidth, h, Image.SCALE_SMOOTH));
				}
				else {
					return null;
				}
			}
		}
		else if ( (new File(folder, entry.getCover()).exists())) {
			/* Loads the image... */
			return new ImageIcon(FileUtil.getImage(folder + File.separator + entry.getCover()).getScaledInstance(coverWidth, h, Image.SCALE_SMOOTH));
		}
		else {
			return null;
		}
	}


	/**
	 * Clear cached covers and the html view cache
	 */
	public void clearCoverCache() {
		views.clear();
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
