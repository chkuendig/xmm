/**
 * @(#)MovieManagerCommandSelect.java 1.0 26.09.06 (dd.mm.yy)
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
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeNode;
import net.sf.xmm.moviemanager.util.DriveInfo;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;
import org.dotuseful.ui.tree.AutomatedTreeNode;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;


public class MovieManagerCommandSelect extends KeyAdapter implements TreeSelectionListener, MouseListener, ActionListener {

	private JMenuItem change, setAllToSeen, setAllToUnseen, addEpisode;
	private JMenu menuApplyToLists, menuRemoveFromLists;

	public static Logger log = Logger.getRootLogger();

	private static boolean ignoreValueChanged = false;

	private static ModelEntry lastSelectedEntry = null;
	
	/**
	 * Executes the command, and reloads the list with the selectedIndex select. 
	 **/
	public static void executeAndReload(int selectedIndex) {

		DefaultListModel list;

		if (MovieManager.getConfig().getCurrentList().equals("Show All")) //$NON-NLS-1$
			list = MovieManager.getIt().getDatabase().getMoviesList(MovieManager.getConfig().getSortOption());
		else
			list = MovieManager.getIt().getDatabase().getMoviesList(MovieManager.getConfig().getSortOption(), MovieManager.getConfig().getCurrentList());

		MovieManager.getDialog().getMoviesList().setModel(MovieManager.getDialog().createTreeModel(list, MovieManager.getIt().getDatabase().getEpisodeList("movieID"))); //$NON-NLS-1$

		if (selectedIndex < 0 || selectedIndex > list.getSize())
			selectedIndex = 0;

		final int index = selectedIndex;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					ignoreValueChanged = true;
					MovieManager.getDialog().getMoviesList().setSelectionRow(index);
					MovieManager.getDialog().setAndShowEntries();
					execute();

					ignoreValueChanged = false;
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		});
	}



	/**
	 * Executes the command, and reloads the list (mantaining the current selected
	 * index visible).
	 **/
	public static void executeAndReload(ModelEntry reloadEntry, boolean edit, boolean isEpisode, boolean execute) {

		//long time = System.currentTimeMillis();

		JTree movieList = MovieManager.getDialog().getMoviesList();

		/* If Adding */
		if (!edit) {

			/* If movie */
			if (!isEpisode) {

				ModelEntry.sortCategory = MovieManager.getConfig().getSortOption();

				if ("Title".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 1;
				else if ("directed".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 2;
				else if ("Rating".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 3;
				else if ("Date".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 4;
				else if ("Duration".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 5;

				ExtendedTreeNode root = ((ExtendedTreeNode) movieList.getModel().getRoot());

				if (root.getChildCount() == 1 && ((ModelEntry) ((ExtendedTreeNode) root.getFirstChild()).getUserObject()).getKey() == -1)
					root.removeAllChildren();

				ExtendedTreeNode newNode = new ExtendedTreeNode(new ModelMovie((ModelMovie) reloadEntry));
				root.addNode(newNode);

				movieList.setSelectionPath(new TreePath(newNode.getPath()));
			}
			else {
				/* If adding episode */
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) movieList.getLastSelectedPathComponent();

				if (node != null && node.getParent() != null) {

					if (!((DefaultMutableTreeNode) node.getParent()).isRoot())
						node = (DefaultMutableTreeNode) node.getParent();
					
					ExtendedTreeNode child = new ExtendedTreeNode(new ModelEpisode((ModelEpisode) reloadEntry));

					/* Adds the new child to the node */
					node.add(child);

					/* expands the node and selects the new child */
					if (execute) 
						movieList.setSelectionPath(new TreePath(new Object[] {node.getParent(), node, child}));
				}
			}
		}
		/* If editing */
		else {

			AutomatedTreeNode node = (AutomatedTreeNode) movieList.getLastSelectedPathComponent();

			node.setUserObject(reloadEntry);
			((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).nodeChanged(node.getParent());

			movieList.setSelectionPath(new TreePath(node.getPath()));
		}

		MovieManager.getDialog().setAndShowEntries();

		/* Saves time not to execute when multiadding episodes */
		if (execute)
			execute();

		MovieManager.getDialog().getMoviesList().requestFocus(true);

		/*Updates the entries value shown in the right side of the toolbars.*/
		MovieManager.getDialog().setAndShowEntries();
	}


	protected static ArrayList createNodeList(TreeModel model) {

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) model).getRoot();
		int childCount = root.getChildCount();
		ArrayList list = new ArrayList(childCount);

		for (int i = 0; i < childCount; i++) {
			if (!((DefaultMutableTreeNode) root.getChildAt(i)).isLeaf())
				list.add(root.getChildAt(i));
		}

		return list;
	}


	/**
	 * Executes the command.
	 **/
	public static void execute() {

		String date = ""; //$NON-NLS-1$
		String title = ""; //$NON-NLS-1$
		String colour = ""; //$NON-NLS-1$
		String cover = ""; //$NON-NLS-1$
		String urlKey = ""; //$NON-NLS-1$
		String directedBy = ""; //$NON-NLS-1$
		String writtenBy = ""; //$NON-NLS-1$
		String genre = ""; //$NON-NLS-1$
		String rating = ""; //$NON-NLS-1$
		String country = ""; //$NON-NLS-1$
		boolean seen = false;
		String aka = ""; //$NON-NLS-1$
		String language = ""; //$NON-NLS-1$
		String plot = ""; //$NON-NLS-1$
		String cast = ""; //$NON-NLS-1$
		String additionalInfoString = ""; //$NON-NLS-1$
		String notes = ""; //$NON-NLS-1$

		String mpaa = ""; //$NON-NLS-1$
		String certification = ""; //$NON-NLS-1$
		String soundMix = ""; //$NON-NLS-1$
		String webRuntime = ""; //$NON-NLS-1$
		String awards = ""; //$NON-NLS-1$

		/* If the cover is retrieved from mysql database (byte form)*/
		boolean byteCover = false;
		byte [] coverData = null;

		/* Use the no cover image */
		boolean noCover = true;

		ModelEntry model = null;
		JTree movieList = MovieManager.getDialog().getMoviesList();

		//long time = System.currentTimeMillis();

		/* Makes sure the list is not empty and an object is selected... */
		if (movieList.getModel() != null && movieList.getModel().getChildCount(movieList.getModel().getRoot()) > 0 && movieList.getMaxSelectionRow() != -1) {

			int selectedIndex = movieList.getMaxSelectionRow();

			if (selectedIndex >= movieList.getModel().getChildCount(movieList.getModel().getRoot())) {
				selectedIndex = movieList.getModel().getChildCount(movieList.getModel().getRoot()) -1;
			}

			model = (ModelEntry) ((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject();

			if (model == null)
				return;

			if (model.getKey() != -1) {

				File coverFile = new File(MovieManager.getConfig().getCoversPath(), model.getCover());
				boolean getCoverFromDisc = true;
				
				/* If MySQL */
				if ((MovieManager.getIt().getDatabase() instanceof DatabaseMySQL)) {

					boolean storeLocally = MovieManager.getConfig().getStoreCoversLocally();
					boolean getCoverFromDatabase = !storeLocally ? true : false;
										
					if (model.getCoverData() == null && !coverFile.isFile()) {
						getCoverFromDatabase = true;
					}
					
					if (!model.getHasGeneralInfoData()) {
						model.updateGeneralInfoData(getCoverFromDatabase);
					}// has general info
					else if (getCoverFromDatabase) {
						model.updateCoverData();
					}

					byteCover = model.getCoverData() != null;

					if (byteCover) {
						coverData = model.getCoverData();
						/* Saving the cover to covers directory */
						if  (storeLocally && !coverFile.isFile()) {
							try {
								if (coverFile.createNewFile()) {
									/* Copies the cover to the covers folder... */
									OutputStream outputStream = new FileOutputStream(coverFile);
									outputStream.write(coverData);
									outputStream.close();
								}
							} catch (Exception e) {
								log.warn("Failed to save cover:" + e.getMessage()); //$NON-NLS-1$
							}
						}
					}
				
					if (byteCover)
						getCoverFromDisc = false;
				
				} else if (!coverFile.isFile()) {
					getCoverFromDisc = false;
				}
				
				if (getCoverFromDisc) {
					byte [] byteBuffer = new byte[(int) coverFile.length()];

					try {
						DataInputStream dis = new DataInputStream(new FileInputStream(coverFile));
						dis.readFully(byteBuffer);
						dis.close();
						model.setCoverData(byteBuffer);
						coverData = byteBuffer;
						byteCover = true;
					} catch(IOException e) {
						log.warn(e.getMessage());
					}
				}
			
				if (!model.getHasAdditionalInfoData())
					model.updateAdditionalInfoData();


				date = model.getDate();
				title = model.getTitle();
				colour = model.getColour();
				urlKey = model.getUrlKey();
				directedBy = model.getDirectedBy();
				writtenBy = model.getWrittenBy();
				genre = model.getGenre();
				rating = model.getRating();
				aka = model.getAka();
				country = model.getCountry();
				seen = model.getSeen();
				language = model.getLanguage();
				plot = model.getPlot();
				cast = model.getCast();
				notes = model.getNotes();
				mpaa = model.getMpaa();
				certification = model.getCertification();
				soundMix = model.getWebSoundMix();
				webRuntime = model.getWebRuntime();
				awards = model.getAwards();

				ModelAdditionalInfo additionalInfo = model.getAdditionalInfo();
				
				/* Experimental - needs more work */
				StringTokenizer tokenizer = new StringTokenizer(additionalInfo.getFileLocation(), "*");
				boolean enable = false;
				String tmp;

				while (tokenizer.hasMoreElements()) {

					tmp = tokenizer.nextToken();

					if (FileUtil.isWindows()) {

						String drive = tmp.substring(0, tmp.indexOf(":") + 1);

						if (drive.length() == 0)
							continue;
						
						try {
							DriveInfo d = new DriveInfo(drive);
							
							if (d.isInitialized() && d.isValid() && !d.isRemovable()) {
								enable = true;
								break;
							}
						} catch(Exception e) {
							log.warn("Exception:", e);
						} 
					}
					else if (new File(tmp).exists()) {
						enable = true;
						break;
					}
				}

				MovieManager.getDialog().toolBar.setEnablePlayButton(enable);

				additionalInfoString = ModelAdditionalInfo.getAdditionalInfoString(additionalInfo);
				
				try {
					if (byteCover) {
						/* Loads the image...*/
						int height = 145;

						if (MovieManager.getConfig().getPreserveCoverAspectRatio() != 0) {

							if ((MovieManager.getConfig().getPreserveCoverAspectRatio() == 1) || model instanceof ModelEpisode) {
								InputStream in = new ByteArrayInputStream(coverData);
								BufferedImage image = javax.imageio.ImageIO.read(in);
								height = ((97*image.getHeight())/image.getWidth());
							}
						}
							
						if (byteCover) {
							
							if (height > 145)
								height = 145;
							
							MovieManager.getDialog().getCover().setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(coverData).getScaledInstance(97, height,Image.SCALE_SMOOTH)));
							noCover = false;
						}
					}
					else if (!byteCover && (!MovieManager.isApplet())) {
						log.debug("Cover not found:" + coverFile.getAbsolutePath()); //$NON-NLS-1$
					}
				} catch (Exception e) {
					log.error("Exception: "+e.getMessage()); //$NON-NLS-1$
				} 
			}
		}

		// Getting the no cover image
		if (noCover) {
			MovieManager.getDialog().getCover().setIcon(new ImageIcon(FileUtil.getImage("/images/" + MovieManager.getConfig().getNoCover()).getScaledInstance(97,97, Image.SCALE_SMOOTH))); //$NON-NLS-1$
		}

		/* Removes mouse listeners */
		for (int i = 0; i < MovieManager.getDialog().getCover().getMouseListeners().length; i++) {
			MovieManager.getDialog().getCover().removeMouseListener(MovieManager.getDialog().getCover().getMouseListeners()[i]);
			i--;
		}

		if (model != null && !urlKey.equals("")) { //$NON-NLS-1$
			if (model instanceof ModelMovie)
				MovieManager.getDialog().getCover().addMouseListener(new MovieManagerCommandOpenPage("http://imdb.com/title/tt"+urlKey+"/")); //$NON-NLS-1$ //$NON-NLS-2$
			else
				MovieManager.getDialog().getCover().addMouseListener(new MovieManagerCommandOpenPage("http://www.tv.com"+urlKey+"summary.html")); //$NON-NLS-1$ //$NON-NLS-2$

			MovieManager.getDialog().getCover().setToolTipText(Localizer.getString("MovieManagerCommandSelect.show-cover.tooltip.open-in-browser")); //$NON-NLS-1$
		} else {
			MovieManager.getDialog().getCover().setToolTipText(null);
		}

		if (date.equals("")) //$NON-NLS-1$
			MovieManager.getDialog().getDateField().setText(""); //$NON-NLS-1$
		else {
			MovieManager.getDialog().getDateField().setText("("+date+") "); //$NON-NLS-1$ //$NON-NLS-2$
			MovieManager.getDialog().getDateField().setCaretPosition(0);
		}

		MovieManager.getDialog().getTitleField().setText(title);
		MovieManager.getDialog().getTitleField().setCaretPosition(0);

		MovieManager.getDialog().getDirectedByField().setText(directedBy);
		MovieManager.getDialog().getDirectedByField().setCaretPosition(0);

		MovieManager.getDialog().getWrittenByField().setText(writtenBy);
		MovieManager.getDialog().getWrittenByField().setCaretPosition(0);

		MovieManager.getDialog().getGenreField().setText(genre);
		MovieManager.getDialog().getGenreField().setCaretPosition(0);

		MovieManager.getDialog().getRatingField().setText(rating);

		StringBuffer misc = new StringBuffer();

		Object font = UIManager.get("TextField.font"); //$NON-NLS-1$
		String fontname = "Dialog.plain"; //$NON-NLS-1$

		if (font != null)
			fontname = ((Font) font).getFontName();

		misc.append("<html><FONT  SIZE=3 FACE=\""+ fontname +"\">"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!soundMix.equals("")) //$NON-NLS-1$
			misc.append("<b>" + Localizer.getString("MovieManagerCommandSelect.miscellaneous-panel.field.sound-mix.title") + "</b><br>" + soundMix + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!webRuntime.equals("")) //$NON-NLS-1$
			misc.append("<b>" + Localizer.getString("MovieManagerCommandSelect.miscellaneous-panel.field.runtime.title") + ":</b><br>" + webRuntime + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!awards.equals("")) //$NON-NLS-1$
			misc.append("<b>" + Localizer.getString("MovieManagerCommandSelect.miscellaneous-panel.field.awards.title") +":</b><br>" + awards + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!mpaa.equals("")) //$NON-NLS-1$
			misc.append("<b>" + Localizer.getString("MovieManagerCommandSelect.miscellaneous-panel.field.mpaa.title") + ":</b><br>"  + mpaa + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!certification.equals("")) //$NON-NLS-1$
		misc.append("<b>" + Localizer.getString("MovieManagerCommandSelect.miscellaneous-panel.field.certification.title") + ":</b><br>" + certification + "<br><br>"); //$NON-NLS-1$

		if (!aka.equals("")) //$NON-NLS-1$
			misc.append("<b>" + Localizer.getString("MovieManagerCommandSelect.miscellaneous-panel.field.also-known-as.title") + ":</b><br>"  + aka.replaceAll("\r\n", "<br>") ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		misc.append("</FONT></html>"); //$NON-NLS-1$

		MovieManager.getDialog().getMiscellaneous().setText(misc.toString());
		MovieManager.getDialog().getMiscellaneous().setCaretPosition(0);

		if (country.equals("")) { //$NON-NLS-1$
			MovieManager.getDialog().getCountryLabel().setText(""); //$NON-NLS-1$
			MovieManager.getDialog().getCountryTextField().setText(""); //$NON-NLS-1$
		}
		else {
			MovieManager.getDialog().getCountryLabel().setText(Localizer.getString("moviemanager.movie-info-panel.country")+ ": "); //$NON-NLS-1$
			MovieManager.getDialog().getCountryTextField().setText(country);
			MovieManager.getDialog().getCountryTextField().setCaretPosition(0);
		}

		if (language.equals("")) { //$NON-NLS-1$
			MovieManager.getDialog().getLanguageLabel().setText(""); //$NON-NLS-1$
			MovieManager.getDialog().getLanguageTextField().setText(""); //$NON-NLS-1$
		}
		else {
			MovieManager.getDialog().getLanguageLabel().setText(Localizer.getString("moviemanager.movie-info-panel.language")+ ": "); //$NON-NLS-1$
			MovieManager.getDialog().getLanguageTextField().setText(language);
			MovieManager.getDialog().getLanguageTextField().setCaretPosition(0);
		}

		/* Must be a spaces to avoid the top row from collapsing */
		if (colour.equals("")) { //$NON-NLS-1$
			MovieManager.getDialog().getColourLabel().setText(" "); //$NON-NLS-1$
			MovieManager.getDialog().getColourField().setText(" "); //$NON-NLS-1$
		}
		else {
			MovieManager.getDialog().getColourLabel().setText(""); //$NON-NLS-1$
			MovieManager.getDialog().getColourField().setText(colour+ " "); //$NON-NLS-1$
		}

		MovieManager.getDialog().getSeen().setSelected(seen);



		MovieManager.getDialog().getPlot().setText(plot);
		MovieManager.getDialog().getPlot().setCaretPosition(0);

		MovieManager.getDialog().getCast().setText(cast);
		MovieManager.getDialog().getCast().setCaretPosition(0);

		/* Stores the additional info scollBar position */
		final int verticalPosition = MovieManager.getDialog().getAdditionalInfoScrollPane().getVerticalScrollBar().getValue();
		MovieManager.getDialog().getAdditionalInfo().setText(additionalInfoString);

		Runnable restoreScrollBarPosition = new Runnable() {
			public void run() {
				MovieManager.getDialog().getAdditionalInfoScrollPane().getVerticalScrollBar().setValue(verticalPosition);
			}
		};

		/* Restores the additional info scollBar position */
		SwingUtilities.invokeLater(restoreScrollBarPosition);

		MovieManager.getDialog().getNotes().setText(notes);
		MovieManager.getDialog().getNotes().setCaretPosition(0);

		TreeNode selected = ((TreeNode) movieList.getLastSelectedPathComponent());
		int horizontalPosition = MovieManager.getDialog().getMoviesListScrollPane().getHorizontalScrollBar().getValue();

		if (selected != null) {
			/* Notifies the JTree model of the updates - 
	       1 - Keeps the entire width of the selected cell(s) highlighted after resize.
	       2 - no longer seems necessary: (prevents the titles in the list from being truncated) */
			((DefaultTreeModel) movieList.getModel()).nodeChanged(selected);



			if (movieList.getLastSelectedPathComponent() != null && 
					((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject() instanceof ModelEpisode && movieList.getSelectionCount() == 1) {
				((ExtendedJTree) movieList).scrollPathToVisible2(movieList.getSelectionPath(), horizontalPosition);
			}
			else
				((ExtendedJTree) movieList).scrollPathToVisible2(movieList.getSelectionPath(), horizontalPosition);
		}
	}

	/**
	 * Checks the value of the seenEditable(in MainWindow) variable in MovieManager 
	 * and performes the appropriate action. A popup menu window is shown at 
	 * the selected index when clicking right mouse button.
	 **/
	public void mousePressed(MouseEvent event) {

		JTree movieList = MovieManager.getDialog().getMoviesList();
		int rowForLocation = movieList.getRowForLocation(event.getX(), event.getY());

		/* Button 2 */
		if (SwingUtilities.isRightMouseButton(event)) {

			if (rowForLocation != -1) {

				int selectionCount = movieList.getSelectionCount();
				ModelEntry leadSelectionRow = (ModelEntry) ((DefaultMutableTreeNode) (movieList.getPathForRow(rowForLocation)).getLastPathComponent()).getUserObject();

				if (!movieList.isRowSelected(rowForLocation)) {
					movieList.setSelectionRow(rowForLocation);
					selectionCount = 1;
				}

				makeMovieListPopupMenu(event.getX(), event.getY(), event, leadSelectionRow, selectionCount);
			}
		}

		/* Button 1 */
		if (SwingUtilities.isLeftMouseButton(event)) {

			if (MovieManager.getConfig().getEnableCtrlMouseRightClick() && isCtrlPressed(event)) {

				if (rowForLocation != -1) {

					int selectionCount = movieList.getSelectionCount();
					ModelEntry leadSelectionRow = (ModelEntry) ((DefaultMutableTreeNode) (movieList.getPathForRow(rowForLocation)).getLastPathComponent()).getUserObject();

					/* After the node is deselected, the node is selected again automatically */
					if (movieList.isRowSelected(rowForLocation))
						movieList.removeSelectionRow(rowForLocation);

					makeMovieListPopupMenu(event.getX(), event.getY(), event, leadSelectionRow, selectionCount);
				}      
			}
			else if (rowForLocation == -1) {

				rowForLocation = -1;

				/* Finds the row */
				for (int i = 0; rowForLocation == -1 && i < 100; i++)
					rowForLocation = movieList.getRowForLocation(i, event.getY());

				if (rowForLocation != -1) {
					if (isShiftPressed(event))
						movieList.setSelectionInterval(rowForLocation, movieList.getMaxSelectionRow());
					else if (isCtrlPressed(event)) 
						movieList.addSelectionRow(rowForLocation);
					else {
						movieList.setSelectionRow(rowForLocation);
					}
				}
			}
		}
	}

	public void actionPerformed(ActionEvent event) {

		if (event.getSource().equals(change))
			MovieManager.getDialog().updateSeen(0);

		else if (event.getSource().equals(setAllToSeen))
			MovieManager.getDialog().updateSeen(1);

		else if (event.getSource().equals(setAllToUnseen))
			MovieManager.getDialog().updateSeen(2);

		else if (menuApplyToLists != null) {

			Component[] applyComponents = menuApplyToLists.getMenuComponents();
			Component[] removeComponents = menuRemoveFromLists.getMenuComponents();

			String columnName = event.getActionCommand();
			/* = false; */
			int mode = -1;

			for (int i = 0; i < applyComponents.length; i++) {
				if (applyComponents[i].equals(event.getSource())) {
					mode = 1;
					break;
				}
				else if (removeComponents[i].equals(event.getSource())) {
					mode = 0;
					break;
				}
			}

			boolean apply = false;

			if (mode == -1)
				return;
			else if (mode == 1)
				apply = true;

			TreePath[] selectedPaths = MovieManager.getDialog().getMoviesList().getSelectionPaths();
			ModelEntry temp;

			for (int i = 0; i < selectedPaths.length; i++) {
				temp = (ModelEntry) ((DefaultMutableTreeNode) selectedPaths[i].getLastPathComponent()).getUserObject();

				if (temp instanceof ModelMovie) {
					MovieManager.getIt().getDatabase().setLists(temp.getKey(), columnName, new Boolean(apply));
				}
			}
		}
	}

	public void keyPressed(KeyEvent e) {

		/* 127 == delete key */
		if (KeyStroke.getKeyStrokeForEvent(e).getKeyCode() == 127)
			MovieManagerCommandRemove.execute();
	}

	public void mouseReleased(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseClicked(MouseEvent event) {

		/* If Button1 and more than 1 click the node is expanded/collapsed */
		if (SwingUtilities.isLeftMouseButton(event)) {
			if (event.getClickCount() >= 2) {
				JTree movieList = MovieManager.getDialog().getMoviesList();
				int rowForLocation = movieList.getRowForLocation(event.getX(), event.getY());

				// Open edit dialog on double click, if clicked entry has no children
				if (((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).isLeaf())
					MovieManagerCommandEdit.execute();

				// otherwise, expand/collapse row (also works without Ctrl pressed)
				else if (isCtrlPressed(event)) {
					if (movieList.isCollapsed(rowForLocation))
						movieList.expandRow(rowForLocation);
					else
						movieList.collapseRow(rowForLocation);
				}
			}
		}
	}

	public void makeMovieListPopupMenu(int x, int y, MouseEvent event, ModelEntry selected, int selectionCount) {

		JTree movieList = MovieManager.getDialog().getMoviesList();
		boolean isSeenEditable = MovieManager.getConfig().getSeenEditable();
		JPopupMenu popupMenu = null;

		if (selectionCount <= 1) {

			popupMenu = new JPopupMenu();

			int key = selected.getKey();

			/* Not a valid movie/episode entry */
			if (key == -1)
				return;

			boolean seen = selected.getSeen();

			if (selected instanceof ModelMovie) {
				popupMenu.add(addEpisode = new JMenuItem(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.add-episodes"))); //$NON-NLS-1$
				addEpisode.addActionListener(new MovieManagerCommandAddEpisode());
			}

			if (isSeenEditable) {

				if (popupMenu.getSubElements().length > 0)
					popupMenu.add(new JPopupMenu.Separator());

				if (seen)
					popupMenu.add(change = new JMenuItem(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.change-to-seen"))); //$NON-NLS-1$
				else 
					popupMenu.add(change = new JMenuItem(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.change-to-unseen"))); //$NON-NLS-1$

				change.addActionListener(this);
			}
		}

		else {
			popupMenu = new JPopupMenu();

			if (isSeenEditable) {
				popupMenu.add(setAllToSeen = new JMenuItem(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.set-selected-to-seen"))); //$NON-NLS-1$
				popupMenu.add(setAllToUnseen = new JMenuItem(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.set-selected-to-unseen"))); //$NON-NLS-1$
				setAllToSeen.addActionListener(this);
				setAllToUnseen.addActionListener(this);
			}
		}

		if (popupMenu != null) {
			if (popupMenu.getSubElements().length > 0)
				popupMenu.add(new JPopupMenu.Separator());

			ArrayList listcolumns = MovieManager.getIt().getDatabase().getListsColumnNames();

			if (listcolumns.size() > 0 && !(movieList.getSelectionCount() == 1 && selected instanceof ModelEpisode)) {

				menuApplyToLists = new JMenu(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.apply-to-list")); //$NON-NLS-1$
				menuRemoveFromLists = new JMenu(Localizer.getString("MovieManagerCommandSelect.movie-list-popup.remove-from-list")); //$NON-NLS-1$
				JMenuItem temp, temp2;

				while (!listcolumns.isEmpty()) {
					temp = new JMenuItem((String) listcolumns.get(0));
					temp2 = new JMenuItem((String) listcolumns.get(0));
					listcolumns.remove(0);
					temp.addActionListener(this);
					temp2.addActionListener(this);
					menuApplyToLists.add(temp);
					menuRemoveFromLists.add(temp2);
				}
				popupMenu.add(menuApplyToLists);
				popupMenu.add(menuRemoveFromLists);
			}

			popupMenu.setInvoker(movieList);
			popupMenu.setLocation(x, y);

			popupMenu.show(movieList, x, y);
		}
	}

	private boolean isCtrlPressed(InputEvent event) {
		return ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK);
	}

	private boolean isShiftPressed(InputEvent event) {
		return ((event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK);
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void valueChanged(TreeSelectionEvent event) {

		if (ignoreValueChanged)
			return;

		TreePath [] paths = event.getPaths();

		/* If less than 2 changed no need to call execute */
		if (paths != null && paths.length <= 1) {
			return;
		}

		/* Saving the node's changed notes value */
		if (lastSelectedEntry != null) {
			
			if (!lastSelectedEntry.getNotes().equals(MovieManager.getDialog().getNotes().getText())) {
				lastSelectedEntry.setNotes(MovieManager.getDialog().getNotes().getText());
				lastSelectedEntry.hasChangedNotes = true;
				
				if (lastSelectedEntry.isEpisode())
					ModelEpisode.notesHaveBeenChanged = true;
				else
					ModelMovie.notesHaveBeenChanged = true;
			}
		}
		
		TreePath path = (TreePath) MovieManager.getDialog().getMoviesList().getSelectionPath();
		
		if (path != null) {
			ModelEntry entry = (ModelEntry) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
			lastSelectedEntry = entry;
		}
			
		if (!MovieManager.getIt().isDeleting()) {
			execute();
		}
	}
}
