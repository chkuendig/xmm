/**
 * @(#)DialogIMDB.java
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.*;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager.KeyMapping;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

public class DialogIMDB extends JDialog {
    
	static Logger log = Logger.getLogger(DialogIMDB.class);
	
	private JTextField searchStringField;
	
    private JButton buttonChoose;
	private JButton buttonCancel;
	private JButton buttonSearch;
	
    private JPanel panelMoviesList;

    JPanel subclassButtons;
    
    private JList listMovies;

    ModelEntry modelEntry = null;
    IMDB imdb = null;
    String addToThisList = null; 
        
	int hitCount;
	long time;
            
    private boolean canceled = false;    
    
    KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
    
    /**
     * Constructor used by UpdateIMDBInfo
     **/
    public DialogIMDB(ModelEntry modelEntry, String alternateTitle, boolean executeSearch) {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        this.modelEntry = modelEntry;
                
        if (alternateTitle == null)
        	setTitle(Localizer.get("DialogIMDB.title")); //$NON-NLS-1$
        else
        	setTitle(alternateTitle);
       	         
        GUIUtil.enableDisposeOnEscapeKey(shortcutManager, new AbstractAction() {
    		public void actionPerformed(ActionEvent arg0) {
				setCanceled(true);
			}
		}, "Close window (and discard)");
        
        createListDialog();
        
        setHotkeyModifiers();
        
        searchStringField.setText(modelEntry.getTitle());
        
        //if (executeSearch)
        callSearch();
    }
    
    JPanel createMoviehitsList()  {
    	/* Movies List panel...*/
    	JPanel panelMoviesList = new JPanel();
    	panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

    	listMovies = new JList() {

    		public String getToolTipText(MouseEvent e) {
    			
    			if (getCellBounds(0,0) == null)
    				return null;
    			
    			String retVal = null;
    			
    			int row = (int) e.getPoint().getY() / (int) getCellBounds(0,0).getHeight();

    			if (row >= 0 && row < getModel().getSize() && getMoviesList().getModel().getElementAt(row) instanceof ModelIMDbSearchHit) {
    				retVal = ((ModelIMDbSearchHit) getMoviesList().getModel().getElementAt(row)).getAka();
    				
    				if (retVal != null && retVal.trim().equals("")) //$NON-NLS-1$
    					retVal = null;
				}
				
    			return retVal;
    		}

    		public JToolTip createToolTip() {
    			JMultiLineToolTip tooltip = new JMultiLineToolTip();
    			tooltip.setComponent(this);
    			return tooltip;
    		}
    	};

    	// Unfortunately setting tooltip timeout affects ALL tooltips
    	ToolTipManager ttm = ToolTipManager.sharedInstance();
    	ttm.registerComponent(listMovies);
    	ttm.setInitialDelay(0);
    	ttm.setReshowDelay(0);
    	
    	listMovies.setFixedCellHeight(18);

    	listMovies.setFont(new Font(listMovies.getFont().getName(),Font.PLAIN,listMovies.getFont().getSize()));
    	listMovies.setLayoutOrientation(JList.VERTICAL);
    	listMovies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	listMovies.setCellRenderer(new MovieHitListCellRenderer());
    	
    	listMovies.addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent event) {
    			
    			// Open we page
    			if (SwingUtilities.isRightMouseButton(event)) {
    				
    				int	index = listMovies.locationToIndex(event.getPoint());
    				
    				if (index >= 0) {
    					ModelIMDbSearchHit hit = (ModelIMDbSearchHit) listMovies.getModel().getElementAt(index);
    					
    					if (hit.getUrlID() != null && !hit.getUrlID().equals("")) {
    						BrowserOpener opener = new BrowserOpener(hit.getCompleteUrl());
    						opener.executeOpenBrowser(MovieManager.getConfig().getSystemWebBrowser(), MovieManager.getConfig().getBrowserPath());
    					}
    				}
    			}
    			else if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() >= 2) {
    				buttonChoose.doClick();
    			}
    		}
    	});

    	KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0, true);
    	ActionListener listKeyBoardActionListener = new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {    			
    			log.debug("ActionPerformed: " + "Movielist - ENTER pressed."); //$NON-NLS-1$
    			buttonChoose.doClick();
    		}
    	};
    	listMovies.registerKeyboardAction(listKeyBoardActionListener, enterKeyStroke, JComponent.WHEN_FOCUSED);
        	
    	JScrollPane scrollPaneMovies = new JScrollPane(listMovies);
    	scrollPaneMovies.setAutoscrolls(true);
    	//scrollPaneMovies.registerKeyboardAction(listKeyBoardActionListener,enterKeyStroke, JComponent.WHEN_FOCUSED);
    	    	
    	panelMoviesList.setLayout(new BorderLayout());
    	panelMoviesList.add(scrollPaneMovies, BorderLayout.CENTER);
    	
    	return panelMoviesList;
    }
    
    private void createListDialog() {
    	/* Dialog properties...*/

    	setModal(true);
    	setResizable(true);
    	
    	panelMoviesList = createMoviehitsList();
    	JPanel searchPanel = createSearchStringPanel();
    	JPanel panelButtons = createButtonsPanel();
    	
    	JPanel panelSearchAndButtons = new JPanel();
    	panelSearchAndButtons.setLayout(new BorderLayout());
    	panelSearchAndButtons.add(searchPanel, BorderLayout.NORTH);
    	panelSearchAndButtons.add(panelButtons, BorderLayout.SOUTH);
    	
    	subclassButtons = new JPanel();
    	
    	JPanel sharedPanel = new JPanel();
    	sharedPanel.setLayout(new BorderLayout());
    	sharedPanel.add(panelSearchAndButtons, BorderLayout.NORTH);
    	sharedPanel.add(subclassButtons, BorderLayout.SOUTH);
    	
    	/* To add outside border... */
    	JPanel all = new JPanel();
    	all.setLayout(new BorderLayout());
    	all.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,0,5), null));
    	all.add(panelMoviesList, BorderLayout.CENTER);
    	all.add(sharedPanel, BorderLayout.SOUTH);
    	    	
    	getContentPane().add(all, BorderLayout.CENTER);
    	//getContentPane().add(sharedPanel,BorderLayout.SOUTH);
    	    	
    	getMoviesList().ensureIndexIsVisible(0);
    	
    	setPreferredSize(new Dimension(500, 440));
    	setMinimumSize(new Dimension(500, 440));
    	
    	pack();
    	    	
    	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
    			(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

    }
    
    private JPanel createButtonsPanel() {
    	
    	JPanel panelButtons = new JPanel();
    	panelButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,0,5), null));
    	panelButtons.setLayout(new BorderLayout());
    	
    	/* regular Buttons panel...*/
    	JPanel panelRegularButtons = new JPanel();
    	panelRegularButtons.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
    	panelRegularButtons.setLayout(new FlowLayout());
    	
    	buttonChoose = new JButton(Localizer.get("DialogIMDB.button.choose.text")); //$NON-NLS-1$
    	buttonChoose.setToolTipText(Localizer.get("DialogIMDB.button.choose.tooltip")); //$NON-NLS-1$
    	buttonChoose.setActionCommand("GetIMDBInfo - Select"); //$NON-NLS-1$
    	buttonChoose.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    			if (!getMoviesList().isSelectionEmpty())
    				executeCommandSelect();
    		}});

    	panelRegularButtons.add(buttonChoose);

    	// Search button
    	/*This button is used to search for on IMDB and for movies in the Database
        Where to search is decided in the executeSearchMultipleMovies method
    	 */
    	JButton buttonSearch = new JButton(Localizer.get("DialogIMDbMultiAdd.button.search.text")); //$NON-NLS-1$
    	buttonSearch.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.search.tooltip")); //$NON-NLS-1$
    	buttonSearch.setActionCommand("GetIMDBInfo - Search again"); //$NON-NLS-1$
    	buttonSearch.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
    			executeSearch();
    		}
    	});
    	
    	panelRegularButtons.add(buttonSearch);
    	
    	// cancel button
    	buttonCancel = new JButton(Localizer.get("DialogIMDB.button.cancel.text.cancel")); //$NON-NLS-1$
    	buttonCancel.setToolTipText(Localizer.get("DialogIMDB.button.cancel.tooltip.cancel")); //$NON-NLS-1$
    	
    	buttonCancel.setActionCommand("GetIMDBInfo - Cancel"); //$NON-NLS-1$

    	buttonCancel.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
    			canceled = true;
    			dispose();
    		}});

    	panelRegularButtons.add(buttonCancel);
    	panelButtons.add(panelRegularButtons, BorderLayout.SOUTH);
  
    	return panelButtons;
    }
    
   
    
    /**
     * Creates a panel containing a text field used to search
     * @return
     */
    private JPanel createSearchStringPanel() {
    	
    	JPanel searchStringPanel = new JPanel();
    	searchStringPanel.setLayout(new BorderLayout());
    	searchStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-search-string.title")), BorderFactory.createEmptyBorder(4,4,4,4))); //$NON-NLS-1$
    	
    	searchStringField = new JTextField(27);
    	searchStringField.setActionCommand("Search String:"); //$NON-NLS-1$
    	searchStringField.setCaretPosition(0);
    	searchStringField.addKeyListener(new KeyAdapter() {
    		public void keyPressed(KeyEvent e) {
    			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
    				executeSearch();
    			}
    		}
    	});
    	
    	searchStringPanel.add(searchStringField, BorderLayout.NORTH);
    	
    	return searchStringPanel;
    }
    
    /**
     * This method can be overridden by subclass to avoid executeSearch 
     * being called by the constructor
     */
    void callSearch() {
    	executeSearch();
    }
    
    void executeSearch() {
    	SwingWorker worker = new SwingWorker() {
    		public Object construct() {
    			performSearch();
    			return null;
    		}
    	};
    	worker.start();
    }
    
    ArrayList<ModelIMDbSearchHit> performSearch() {
    	    	
    	DefaultListModel model = new DefaultListModel();
    	model.addElement(new ModelIMDbSearchHit(Localizer.get("DialogIMDB.list-element.messsage.search-in-progress"))); //$NON-NLS-1$
    	listMovies.setModel(model);

    	ArrayList<ModelIMDbSearchHit> hits = null;
    	
    	try {
    		hits = new IMDB(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(searchStringField.getText());
    		handleSearchResults(hits);
    	}
    	catch (Exception e) {
    		log.error(e.getMessage(), e);
    		executeErrorMessage(e);
    		dispose();
    	}
    	return hits;
    }
    
    void handleSearchResults(ArrayList<ModelIMDbSearchHit> hits) {

		final DefaultListModel list = new DefaultListModel();

		boolean noHits = false;
		
		// Error
		if (hits == null) {
			HTTPResult res = imdb.getLastHTTPResult();

			if (res.getStatusCode() == HttpStatus.SC_REQUEST_TIMEOUT) {
				list.addElement(new ModelIMDbSearchHit("Connection timed out...")); //$NON-NLS-1$
				noHits = true;
			}
		}
		else if (hits.size() == 0) {
			list.addElement(new ModelIMDbSearchHit(Localizer.get("DialogIMDB.list-element.messsage.no-hits-found"))); //$NON-NLS-1$
			noHits = true;
		}
		else {
			for (ModelIMDbSearchHit hit : hits)
				list.addElement(hit);
		}

		final boolean setButtonChooseEnabled = !noHits;
		
		// make changes on EDT
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setListModel(list);
				listMovies.setSelectedIndex(0);
				getButtonChoose().setEnabled(setButtonChooseEnabled);
			}
		});
    }
    
    /**
     * Takes the current selected element, retrieves the IMDb info and disposes.
     **/
    private void executeCommandSelect() {
    	
    	int index = getMoviesList().getSelectedIndex();
    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));

    	if (model.getUrlID() == null)
    		return;

    	getIMDbInfo(modelEntry, model.getUrlID());

    	ModelMovieInfo.executeTitleModification(modelEntry);

    	dispose();
    }
    

    public void setListModel(DefaultListModel list) {
    	listMovies.setModel(list);
    	listMovies.requestFocusInWindow();
    }
    
    
    public boolean getCanceled() {
    	return canceled;
    }
    
    protected void setCanceled(boolean cancel) {
    	canceled = cancel;
    }
    
    public DialogIMDB(ModelEntry modelEntry, String alternateTitle) {
    	this(modelEntry, alternateTitle, true);
    }
    
    /**
     * Returns the JList listMovies.
     **/
    protected JList getMoviesList() {
        return listMovies;
    }
    
    protected JPanel getPanelMoviesList() {
        return panelMoviesList;
    }
    
    public JTextField getSearchField() {
    	return searchStringField;
    }
    
    /**
     * Returns the JButton select.
     **/
    protected JButton getButtonChoose() {
        return buttonChoose;
    }
   
    
    JButton getButtonSearch() {
    	return buttonSearch;
    }
    
    JButton getButtonCancel() {
    	return buttonCancel;
    }
    
    
    /*ALerts the user of different error messages from proxy servers*/
    void executeErrorMessage(Exception e) {
        
    	String message = e.getMessage();
    	
    	if (e instanceof UnknownHostException) {
    		 DialogAlert alert = new DialogAlert(this, "Unkown host", "Failed to connect to " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
             GUIUtil.showAndWait(alert, true);
    	}
    	
        if (message == null)
            return;
        
        if (message.startsWith("Server returned HTTP response code: 407")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.get("DialogIMDB.alert.title.authentication-required"), Localizer.get("DialogIMDB.alert.message.proxy-authentication-required")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("Connection timed out")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.get("DialogIMDB.alert.title.connection-timed-out"), Localizer.get("DialogIMDB.alert.message.connection-timed-out")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("Connection reset")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.get("DialogIMDB.alert.title.connection-reset"), Localizer.get("DialogIMDB.alert.message.connection-reset")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("Server redirected too many  times")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.get("DialogIMDB.alert.title.access-denied"), Localizer.get("DialogIMDB.alert.message.username-of-password-invalid")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("The host did not accept the connection within timeout of")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.get("DialogIMDB.alert.title.connection-timed-out"), message); //$NON-NLS-1$
            GUIUtil.showAndWait(alert, true);
        }
        
    }
    
  
    
    
    public static boolean getIMDbInfo(ModelEntry modelEntry, String key) {
    	IMDB imdb;

    	try {
    		//net.sf.xmm.moviemanager.http.IMDB_if i = SysUtil.getIMDBInstance();
    		
    		//imdb = i.getIMDB(key, MovieManager.getConfig().getHttpSettings());
    		
    		//net.sf.xmm.moviemanager.http.IMDB_if i = SysUtil.getIMDBInstance();
    		
    		imdb = new IMDB(key, MovieManager.getConfig().getHttpSettings());
    	} catch (Exception e) {
    		log.error(e.getMessage(), e); //$NON-NLS-1$
    		return false;
    	}
    	
    	ModelIMDbEntry dataModel = imdb.getLastDataModel();
    	   

    	if (key.equals(imdb.getUrlID())) {

    		modelEntry.setTitle(imdb.getTitle());
    		modelEntry.setDate(imdb.getDate());
    		modelEntry.setColour(imdb.getColour());
    		modelEntry.setDirectedBy(imdb.getDirectedBy());
    		modelEntry.setWrittenBy(imdb.getWrittenBy());
    		modelEntry.setGenre(imdb.getGenre());
    		modelEntry.setRating(imdb.getRating());
    		modelEntry.setCountry(imdb.getCountry());
    		modelEntry.setLanguage(imdb.getLanguage());
    		modelEntry.setPlot(imdb.getPlot());
    		modelEntry.setCast(imdb.getCast());

    		modelEntry.setWebRuntime(imdb.getRuntime());
    		modelEntry.setWebSoundMix(imdb.getSoundMix());
    		modelEntry.setAwards(imdb.getAwards());
    		modelEntry.setMpaa(imdb.getMpaa());
    		modelEntry.setAka(imdb.getAka());
    		modelEntry.setCertification(imdb.getCertification());

    		modelEntry.setUrlKey(imdb.getUrlID());

    		/* The cover... */
    		byte[] coverData = imdb.getCover();

    		if (imdb.getCoverOK()) {

    			modelEntry.setCover(imdb.getCoverName());
    			modelEntry.setCoverData(coverData);
    		} else {
    			modelEntry.setCover(null);
    			modelEntry.setCoverData(null);
    		}
    		
    		// Big cover available
    		if (imdb.retrieveBiggerCover(dataModel)) {
    			modelEntry.setCoverData(dataModel.getBigCoverData());
    		}
    	}
    	return true;
    }
    
    public class MovieHitListCellRenderer extends DefaultListCellRenderer {

    	public Component getListCellRendererComponent(JList list, Object value,
    			int index, boolean isSelected, boolean hasFocus) {
    		super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);

    		if (value instanceof ModelIMDbSearchHit) {

    			//String category = ((ModelIMDbSearchHit) value).getHitCategory();
    			
    			//"Popular Titles", "Titles (Exact Matches)", "Titles (Partial Matches)", "Titles (Approx Matches)
    				
    			/*
    			if (category == null)
    				setBackground(null);    			    			
    			else if (category.equals("Popular Titles"))
    				setBackground(new Color(162, 179, 243));
    			else if (category.equals("Titles (Exact Matches)"))
    				setBackground(new Color(240, 119, 119));
    			else if (category.equals("Titles (Partial Matches)"))
    				setBackground(new Color(236, 240, 119));
    			else if (category.equals("Titles (Approx Matches)"))
    				setBackground(new Color(119, 240, 124));
    			*/
    			
    		}
    		return this;
    	}
    }
    
	private void setHotkeyModifiers() {
		
		try {
			// ALT+C for Select
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Choose selected title", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonChoose.doClick();
				}
			}, buttonChoose);
				
			// ALT+D for skip (Discard)
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Discard this movie", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonCancel.doClick();
				}
			}, buttonCancel);
			
			// ALT+S for search field focus
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Give search field focus or perform search", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
									
					if (!searchStringField.hasFocus()) {
						searchStringField.requestFocusInWindow();
					}
					else {
						buttonSearch.doClick();
					}
				}
			}, buttonSearch);
			
			shortcutManager.setKeysToolTipComponent(panelMoviesList);
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
}
