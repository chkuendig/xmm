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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import org.apache.log4j.Logger;

/* This class is a total mess */
public class DialogIMDB extends JDialog {
    
	static Logger log = Logger.getLogger(DialogIMDB.class);
	

    JButton buttonSelect;
	JButton buttonCancel;
	JPanel panelButtons;

    JPanel panelMoviesList;

    JList listMovies;

    ModelEntry modelEntry = null;
    IMDB imdb = null;
    String addToThisList = null; 
        
	int hitCount;
	long time;
            
    private boolean canceled = false;    
    
   
    
    /**
     * Constructor used by UpdateIMDBInfo
     **/
    public DialogIMDB(ModelEntry modelEntry, String alternateTitle, boolean executeSearch) {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        this.modelEntry = modelEntry;
        
        if (alternateTitle == null)
        	setTitle(Localizer.getString("DialogIMDB.title")); //$NON-NLS-1$
        else
        	setTitle(alternateTitle);
       	         
        /* Close dialog... */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        /*Enables dispose when pushing escape*/
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        createListDialog(); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (executeSearch)
        	executeSearch();
    }
    
    
   
    /**
     * Constructor - When adding multiple movies by file.
     **/
    /*
    public DialogIMDB(String _imdbId, ModelEntry modelEntry, String searchString, String filename, File multiAddFile, ImdbImportOption multiAddSelectOption, String addToThisList) {
    	    	
        super(MovieManager.getDialog());
        setTitle(Localizer.getString("DialogIMDB.title")); //$NON-NLS-1$
        
        imdbId = _imdbId;
        this.modelEntry = modelEntry;
        this.multiAddFile = multiAddFile;
        this.addToThisList = addToThisList;
                        
        time = System.currentTimeMillis();
                
        createListDialog(searchString, filename);
        
        // Insert prefix in Title to show that these movies maybe got wrong imdb infos
		if (MovieManager.getConfig().getMultiAddSelectFirstHitMark() && hitCount > 1 && multiAddSelectOption == ImdbImportOption.selectFirst && (_imdbId == null || _imdbId.equals("")))
			modelEntry.setTitle("_verify_" + modelEntry.getTitle());
    }
*/

    void createListDialog() {
    	/* Dialog properties...*/

    	setModal(true);
    	setResizable(true);
    	/* Movies List panel...*/
    	panelMoviesList = new JPanel();
    	panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogIMDB.panel-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

    	listMovies = new JList() {

    		public String getToolTipText(MouseEvent e) {
    			
    			if (getCellBounds(0,0) == null)
    				return null;
    			
    			String retVal = null;
    			
    			int row = (int) e.getPoint().getY() / (int) getCellBounds(0,0).getHeight();

				if (row >= 0 && row < getModel().getSize() && getMoviesList().getModel().getElementAt(row) instanceof ModelIMDbSearchHit) {
    				retVal = ((ModelIMDbSearchHit) getMoviesList().getModel().getElementAt(row)).getAka();
    				
    				if (retVal != null && retVal.trim().equals(""))
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
    					BrowserOpener opener = new BrowserOpener(hit.getCompleteUrl());
    					opener.executeOpenBrowser(MovieManager.getConfig().getSystemWebBrowser(), MovieManager.getConfig().getBrowserPath());
    				}
    			}
    			else if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() >= 2) {
    				buttonSelect.doClick();
    			}
    		}
    	});

    	KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0, true);
    	ActionListener listKeyBoardActionListener = new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			buttonSelect.doClick();
    		}
    	};
    	listMovies.registerKeyboardAction(listKeyBoardActionListener, enterKeyStroke, JComponent.WHEN_FOCUSED);
        	
    	JScrollPane scrollPaneMovies = new JScrollPane(listMovies);
    	scrollPaneMovies.setAutoscrolls(true);
    	//scrollPaneMovies.registerKeyboardAction(listKeyBoardActionListener,enterKeyStroke, JComponent.WHEN_FOCUSED);
    	
    	
    	panelMoviesList.setLayout(new BorderLayout());
    	panelMoviesList.add(scrollPaneMovies, BorderLayout.CENTER);

    	/* To add outside border... */
    	JPanel all = new JPanel();

    	all.setLayout(new BorderLayout());
    	all.add(panelMoviesList, BorderLayout.CENTER);
    	all.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,0,8), null));

    	panelButtons = new JPanel();
    	panelButtons.setLayout(new BorderLayout());
    	
    	/* regular Buttons panel...*/
    	JPanel panelRegularButtons = new JPanel();
    	panelRegularButtons.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
    	panelRegularButtons.setLayout(new FlowLayout());
    	
    	buttonSelect = new JButton(Localizer.getString("DialogIMDB.button.select.text")); //$NON-NLS-1$
    	buttonSelect.setToolTipText(Localizer.getString("DialogIMDB.button.select.tooltip")); //$NON-NLS-1$
    	buttonSelect.setActionCommand("GetIMDBInfo - Select"); //$NON-NLS-1$
    	buttonSelect.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    			if (!getMoviesList().isSelectionEmpty())
    				executeCommandSelect();
    		}});

    	panelRegularButtons.add(buttonSelect);

    	buttonCancel = new JButton(Localizer.getString("DialogIMDB.button.cancel.text.cancel")); //$NON-NLS-1$
    	buttonCancel.setToolTipText(Localizer.getString("DialogIMDB.button.cancel.tooltip.cancel")); //$NON-NLS-1$
    	
    	buttonCancel.setActionCommand("GetIMDBInfo - Cancel"); //$NON-NLS-1$

    	buttonCancel.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
    			canceled = true;
    			dispose();
    		}});

    	panelRegularButtons.add(buttonCancel);
    	panelButtons.add(panelRegularButtons, BorderLayout.SOUTH);
    	
    	getContentPane().add(all, BorderLayout.CENTER);
    	getContentPane().add(panelButtons,BorderLayout.SOUTH);
    	    	
    	getMoviesList().ensureIndexIsVisible(0);
    	
    	setPreferredSize(new Dimension(420, 440));
    	setMinimumSize(new Dimension(420, 440));
    	
    	pack();
    	    	
    	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
    			(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

    }
    
    void executeSearch() {
    	    	
    	DefaultListModel model = new DefaultListModel();
    	model.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogIMDB.list-element.messsage.search-in-progress"), null)); //$NON-NLS-1$
    	listMovies.setModel(model);
    	
    	SwingWorker worker = new SwingWorker() {
    		public Object construct() {
    			try {

    				ArrayList<ModelIMDbSearchHit> hits = new IMDB(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(modelEntry.getTitle());
    				final DefaultListModel list = new DefaultListModel();

    				if (hits.size() == 0) {
    					list.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
    				}
    				else {
    					for (ModelIMDbSearchHit hit : hits)
    						list.addElement(hit);
    				}

    				// make changes on EDT
    				SwingUtilities.invokeLater(new Runnable() {
    					public void run() {
    						listMovies.setModel(list);
    						listMovies.setSelectedIndex(0);
    						getButtonSelect().setEnabled(true);
    					}
    				});
    			}
    			catch (Exception e) {
    				log.error(e.getMessage(), e);
    				executeErrorMessage(e);
    				dispose();
    			}
    			return ""; //$NON-NLS-1$
    		}
    	};
    	worker.start();
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
    
    
    /**
     * Returns the JButton select.
     **/
    protected JButton getButtonSelect() {
        return buttonSelect;
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
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.authentication-required"), Localizer.getString("DialogIMDB.alert.message.proxy-authentication-required")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("Connection timed out")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-timed-out"), Localizer.getString("DialogIMDB.alert.message.connection-timed-out")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("Connection reset")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-reset"), Localizer.getString("DialogIMDB.alert.message.connection-reset")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("Server redirected too many  times")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.access-denied"), Localizer.getString("DialogIMDB.alert.message.username-of-password-invalid")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (message.startsWith("The host did not accept the connection within timeout of")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-timed-out"), message); //$NON-NLS-1$
            GUIUtil.showAndWait(alert, true);
        }
        
    }
    
    /**
     * Gets info...
     **/
    private void executeCommandSelect() {
    	
    	int index = getMoviesList().getSelectedIndex();
    	
    	/*
    	 * When adding the file info the an existing movie, a new ModelMovieInfo object is created. 
    	 * When done, the old ModelMovieInfo object created in the 
    	 * MovieManagerCommandAddMultipleMovies object needs not to save the file as a new movie,
    	 * therefore setCancel method with true is called at the end of the if scoop.
    	 */	

    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));

    	if (model.getUrlID() == null)
    		return;

    	time = System.currentTimeMillis();

    	getIMDbInfo(modelEntry, model.getUrlID());

    	ModelMovieInfo.executeTitleModification(modelEntry);

    	dispose();
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
}
