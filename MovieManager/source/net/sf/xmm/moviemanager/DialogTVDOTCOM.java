/**
 * @(#)DialogTVDOTCOM.java 1.0 13.11.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.http.TVDOTCOM;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.*;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import javax.swing.SwingUtilities;

class DialogTVDOTCOM extends JDialog {
   
    static Logger log = Logger.getRootLogger();
    
    private DialogMovieInfo _parent;
    private JList listMovies;
    private int multiAddSelectOption;
    
    private JPanel panelMoviesList;
    
    private StringBuffer [] streams;
    
    private JButton buttonSelectAll;
    
    /* The current mode (Season/episode */
    private static int mode = 0;
    
    /**
     * The Constructor.
     **/
    protected DialogTVDOTCOM(DialogMovieInfo parent) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
		    
	/* Sets parent... */
	_parent = parent;
	
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    mode = 0;
		    dispose();
		}
	    });
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    mode = 0;
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	createListDialog(null);
    }
    
    void createListDialog(DefaultListModel list) {
	/* Dialog properties...*/
	setTitle("Select Movie Title");
	setModal(true);
	setResizable(false);
	mode = 0;
	
	/* Movies List panel...*/
	panelMoviesList = new JPanel();
	panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," TV.COM "), BorderFactory.createEmptyBorder(5,5,5,5)));
	
	listMovies = new JList();
	listMovies.setFont(new Font(listMovies.getFont().getName(),Font.PLAIN,listMovies.getFont().getSize()));
	listMovies.setLayoutOrientation(JList.VERTICAL);
	listMovies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	

	
	JScrollPane scrollPaneMovies = new JScrollPane(listMovies);
	scrollPaneMovies.setPreferredSize(new Dimension(300,255));
	panelMoviesList.add(scrollPaneMovies);
	
	/* To add outside border... */
	JPanel all = new JPanel();
	all.setLayout(new BorderLayout());
	all.add(panelMoviesList, BorderLayout.NORTH);
	all.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,0,8), null));
	
	/* regular Buttons panel...*/
	JPanel panelRegularButtons = new JPanel();
	panelRegularButtons.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
	panelRegularButtons.setLayout(new FlowLayout());
	
	buttonSelectAll = new JButton("Select All");
	buttonSelectAll.setActionCommand("Select All");
	buttonSelectAll.setEnabled(false);
	buttonSelectAll.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+ event.getActionCommand());
		    
		    if (getMoviesList().getModel().getSize() > 0)
			getMoviesList().setSelectionInterval(0, getMoviesList().getModel().getSize()-1);
		    
		}});
	panelRegularButtons.add(buttonSelectAll);
	
	JButton buttonSelect = new JButton("Select");
	buttonSelect.setToolTipText("Get the info for the selected title(s)");
	buttonSelect.setEnabled(false);
	buttonSelect.setActionCommand("GetTVDOTCOMInfo - Select");
	buttonSelect.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+ event.getActionCommand());
		    executeCommandSelect();
		}});
	
	panelRegularButtons.add(buttonSelect);
	
	JButton buttonOk;
	buttonOk = new JButton("Cancel");
	buttonOk.setToolTipText("Close the TV.COM Info dialog");
	
	buttonOk.setActionCommand("GetIMDBInfo - Cancel");
	
	buttonOk.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+ event.getActionCommand());
		    mode = 0;
		    dispose();
		}});
	
	panelRegularButtons.add(buttonOk);
	/* Adds all and buttonsPanel... */
	getContentPane().add(all,BorderLayout.NORTH);
	getContentPane().add(panelRegularButtons,BorderLayout.SOUTH);
	/* Packs and sets location... */
	pack();
	
	getMoviesList().ensureIndexIsVisible(0);
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	
	DefaultListModel model = new DefaultListModel();
	model.addElement(new ModelMovie(-1, "Search in progress..."));
	listMovies.setModel(model);
	
	SwingWorker worker = new SwingWorker() {
		public Object construct() {
		    try {
			DefaultListModel list = TVDOTCOM.getSeriesMatches(_parent.getMovieTitle().getText());
			
			if (list == null) {
			    final DefaultListModel model = new DefaultListModel();
			    model.addElement(new ModelMovie(-1, "No matches found"));
			    
			    Runnable updateProgres = new Runnable() {
				    public void run() {
					try {
					    getMoviesList().setModel(model);  
					} catch (Exception e) {
					    log.error(e.getMessage());
					}
		    
				    }};
	
			    SwingUtilities.invokeLater(updateProgres);
			    return this;
			}
			
			if (list.getSize() == 0)
			    executeErrorMessage();
			else 
			    listMovies.setModel(list);
	
			listMovies.setSelectedIndex(0);

			    
			//getButtonMore().setEnabled(true);
			getButtonSelect().setEnabled(true);
		    }
		    catch (Exception e) {
			return "";
			}
			return "";
		}
	    };
	worker.start();
    }
    
    /**
     * Returns the JList listMovies.
     **/
    protected JList getMoviesList() {
	return
	    (JList)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
    }
    
    /**
     * Returns the JButton More.
     **/
    protected JButton getButtonMore() {
	return
	    (JButton)
	    ((JPanel)
	     getContentPane().getComponent(1)).getComponent(0);
    }
    
     /**
     * Returns the JButton Select.
     **/
    protected JButton getButtonSelect() {
	return
	    (JButton)
	    ((JPanel)
	     getContentPane().getComponent(1)).getComponent(1);
    }
    
    private int executeCommandMultipleMoviesSelectCheck(int listSize) {
	
	/*here is some code to check the property settings entered in the multi add movie preferences*/
	
	if (multiAddSelectOption != 0) {
	    
	    /*Select first hit no matter*/
	    if ((multiAddSelectOption == 1) && (listSize > 0)) {
		executeCommandSelect();
		return 0;
	    }
	    
	    /*Select first if only one hit*/
	    if (multiAddSelectOption == 2) {
		if (listSize == 1) {
		    getMoviesList().setSelectedIndex(0);
		    executeCommandSelect();
		    return 0;
		}
	    }
	}
	return 1;
    }
    
    /* Alerts the user of different error messages from proxy servers*/
    void executeErrorMessage() {
	
	String exception = TVDOTCOM.getException();
	
	if (exception == null)
	    return;
	
	if (exception.startsWith("Server returned HTTP response code: 407")) {
	    DialogAlert alert = new DialogAlert("Authentication required", "Proxy server requires authentication");
	    alert.setVisible(true);
	}
	
	if (exception.startsWith("Connection timed out")) {
	    DialogAlert alert = new DialogAlert("Connection timed out", "Server did not respond");
	    alert.setVisible(true);
	}
	
	if (exception.startsWith("Connection reset")) {
	    DialogAlert alert = new DialogAlert("Connection reset", "Connection reset by server");
	    alert.setVisible(true);
	}
	
	if (exception.startsWith("Server redirected too many  times")) {
	    DialogAlert alert = new DialogAlert("Access denied", "Username or password is invalid");
	    alert.setVisible(true);
	}
    }
    
    /**
     * Gets more or less info...
     **/
    private void executeCommandSelect() {
	int index = getMoviesList().getSelectedIndex();
	
	DefaultListModel listModel = (DefaultListModel)getMoviesList().getModel();
	
	/* Get seasons */
	if (mode == 0) {
	    
	    getMoviesList().setModel(TVDOTCOM.getSeasons((ModelSearchHit) listModel.getElementAt(index)));
	    getMoviesList().setSelectedIndex(0);
	}
	
	/* Get episodes */
	else if (mode == 1) {
	    
	    ModelSearchHit selected = (ModelSearchHit) listModel.getElementAt(index);
	    
	    streams = TVDOTCOM.getEpisodesStream(selected);
	    
	    getMoviesList().setModel(TVDOTCOM.getEpisodes(selected, streams));
	    getMoviesList().setSelectedIndex(0);
	    getMoviesList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    
	    buttonSelectAll.setEnabled(true);
	}
	
	/* Get episode info */
	else if (mode == 2) {
	    
	    Object [] selectedValues = getMoviesList().getSelectedValues();
	    
	    byte [] cover = TVDOTCOM.getSeriesCover((ModelSearchHit) selectedValues[0]);
	    boolean execute = false;
	    String coverFileName = ((ModelSearchHit) selectedValues[0]).getUrlTitle() +((ModelSearchHit) selectedValues[0]).getCoverExtension();

	    if (selectedValues.length != 1)
	        _parent.dispose();
	    
	    for (int i = 0; i < selectedValues.length; i++) {
		
		//long time = System.currentTimeMillis();
		
		ModelEpisode episode = TVDOTCOM.getEpisodeInfo((ModelSearchHit) selectedValues[i], streams);
		
		_parent.getMovieTitle().setText(episode.getTitle());
		_parent.getMovieTitle().setCaretPosition(0);
		_parent.getDate().setText(episode.getDate());
		_parent.getDate().setCaretPosition(0);
		_parent.getDirectedBy().setText(episode.getDirectedBy());
		_parent.getDirectedBy().setCaretPosition(0);
		_parent.getWrittenBy().setText(episode.getWrittenBy());
		_parent.getWrittenBy().setCaretPosition(0);
		_parent.getRating().setText(episode.getRating());
		_parent.getRating().setCaretPosition(0);
		_parent.getPlot().setText(episode.getPlot());
		_parent.getPlot().setCaretPosition(0);
		_parent.getCast().setText(episode.getCast());
		_parent.getCast().setCaretPosition(0);
		_parent.setIMDB(episode.getUrlKey());
		_parent.setEpisodeNumber(episode.getEpisodeNumber());
		
		/* The cover... */
		if (cover != null) {
		    _parent.setCover(coverFileName, cover);
		} else {
		    _parent.removeCover();
		}
		
		/* The imdb id... */
		if (selectedValues.length != 1) {
		    ModelEntry entry = _parent.executeCommandSave(null);
		    
		    if (i+1 == selectedValues.length)
			execute = true;
		    
		    MovieManagerCommandSelect.executeAndReload(entry, false, true, execute);
		}
	    }
	    dispose();
	}
	
	mode++;
    }
}
    
