/**
 * @(#)DialogIMDB.java 1.0 26.09.06 (dd.mm.yy)
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.log4j.Logger;

/* This class is a total mess */
public class DialogIMDB extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    IMDB imdb = null;
    
    JList listMovies;
    JButton buttonSelect;
    
    JTextField searchStringField;
    boolean multiAdd = false;
    boolean addInfoToExistingMovie = false;
    int multiAddSelectOption = 0;
    File multiAddFile;
    JPanel panelMoviesList;

    String filename = null;
    
    boolean getUrlKeyOnly = false;

    String addToThisList = null; 
    
    boolean switchBetweenIMDBAndDatabase = false;
    boolean addWithoutIMDBInfo = false;
    
    boolean addMultiplMovieByFile;
    
    long time;
    
    public boolean isMultiAdd() {
        return multiAdd;
    }
    
    ModelEntry modelEntry = null;
        
    public boolean cancelSet = false;
    public boolean cancelAllSet = false;
    public boolean dropImdbInfoSet = false;
    
    
    public void resetFeedbackValues() {
    	cancelSet = false;
        cancelAllSet = false;
        dropImdbInfoSet = false;
    }
    
   
    /**
     * Constructor used by UpdateIMDBInfo
     **/
    public DialogIMDB(ModelEntry modelEntry, boolean getUrlKeyOnly, String alternateTitle) {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        this.modelEntry= modelEntry;
        
        if (alternateTitle == null)
        	setTitle(Localizer.getString("DialogIMDB.title")); //$NON-NLS-1$
        else
        	setTitle(alternateTitle);
       	
        
        this.getUrlKeyOnly = getUrlKeyOnly;
         
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
        
        if (getUrlKeyOnly)
        	createListDialog(alternateTitle, alternateTitle); //$NON-NLS-1$ //$NON-NLS-2$
        else
        	createListDialog("not used", "not used"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
   
    /**
     * Constructor - When adding multiple movies by file.
     **/
    public DialogIMDB(ModelEntry modelEntry, String searchString, String filename, 
    		File multiAddFile, 
    		int multiAddSelectOption, String addToThisList) {
    	    	
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        setTitle(Localizer.getString("DialogIMDB.title")); //$NON-NLS-1$
        
        this.modelEntry = modelEntry;
        this.multiAddFile = multiAddFile;
        this.addToThisList = addToThisList;
        this.addMultiplMovieByFile = addMultiplMovieByFile;
        
        /* Sets parent... */
        this.multiAdd = true;
        this.multiAddSelectOption = multiAddSelectOption;

        this.filename = filename;
        
        switchBetweenIMDBAndDatabase = true;
        addWithoutIMDBInfo = true;
        
        
        /* Close dialog... */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        time = System.currentTimeMillis();
        
        /*Enables dispose when pushing escape*/
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancelSet = true;
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        createListDialog(searchString, filename);
    }


    void createListDialog(final String searchString, String filename) {
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

    	// Unfortunately this affect ALL tooltips
    	ToolTipManager ttm = ToolTipManager.sharedInstance();
    	ttm.registerComponent(listMovies);
    	ttm.setInitialDelay(0);
    	ttm.setReshowDelay(0);
    	
    	listMovies.setFixedCellHeight(18);

    	listMovies.setFont(new Font(listMovies.getFont().getName(),Font.PLAIN,listMovies.getFont().getSize()));
    	listMovies.setLayoutOrientation(JList.VERTICAL);
    	listMovies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    	listMovies.addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent event) {
    			if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() >= 2) {
    				buttonSelect.doClick();
    			}
    		}
    	});

    	KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,true);
    	ActionListener listKeyBoardActionListener = new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			buttonSelect.doClick();
    		}
    	};
    	listMovies.registerKeyboardAction(listKeyBoardActionListener,enterKeyStroke, JComponent.WHEN_FOCUSED);
    	    	
    	JScrollPane scrollPaneMovies = new JScrollPane(listMovies);
    	scrollPaneMovies.setPreferredSize(new Dimension(300,255));
    	scrollPaneMovies.setAutoscrolls(true);
    	
    	panelMoviesList.setLayout(new BorderLayout());
    	panelMoviesList.add(scrollPaneMovies, BorderLayout.CENTER);

    	/* To add outside border... */
    	JPanel all = new JPanel();

    	all.setLayout(new BorderLayout());
    	all.add(panelMoviesList, BorderLayout.CENTER);
    	all.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,0,8), null));

    	JPanel panelButtons = new JPanel();
    	panelButtons.setLayout(new BorderLayout());
    	
    	/*If running the multiadd feature, here are all the extra buttons added. */
    	if (isMultiAdd() || getUrlKeyOnly) {

    		setTitle(filename);

    		JPanel searchStringPanel = new JPanel();
    		searchStringPanel.setLayout(new BorderLayout());
    		searchStringPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogIMDB.panel-search-string.title")), BorderFactory.createEmptyBorder(4,4,4,4))); //$NON-NLS-1$

    		searchStringField = new JTextField(27);
    		searchStringField.setActionCommand("Search String:"); //$NON-NLS-1$
    		searchStringField.setText(searchString);
    		searchStringField.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent event) {
    				log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
    				executeSearchMultipleMovies();
    			}
    		});

    		searchStringPanel.add(searchStringField, BorderLayout.NORTH);
    		panelButtons.add(searchStringPanel, BorderLayout.CENTER);

    		JPanel multipleMovieButtons = new JPanel();
    		multipleMovieButtons.setLayout(new FlowLayout());


    		/*This button is used to search for on IMDB and for movies in the Database
             Where to search is desided in the executeSearchMultipleMovies method
    		 */
    		JButton buttonSearch = new JButton(Localizer.getString("DialogIMDB.button.search.text")); //$NON-NLS-1$
    		buttonSearch.setToolTipText(Localizer.getString("DialogIMDB.button.search.tooltip")); //$NON-NLS-1$
    		buttonSearch.setActionCommand("GetIMDBInfo - Search again"); //$NON-NLS-1$


    		buttonSearch.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent event) {
    				log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
    				executeSearchMultipleMovies();
    			}});
    		multipleMovieButtons.add(buttonSearch);
	
    		if (switchBetweenIMDBAndDatabase) {

    			if (multiAddFile != null) {

    				/*This button choses between IMDB and local movie database*/
    				final JButton chooseBetweenImdbAndLocalDatabase  = new JButton(Localizer.getString("DialogIMDB.button.add-to-existing-movie.text")); //$NON-NLS-1$
    				chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.getString("DialogIMDB.button.add-to-existing-movie.tooltip")); //$NON-NLS-1$
    				chooseBetweenImdbAndLocalDatabase.setPreferredSize(new Dimension(155, 26));
    				chooseBetweenImdbAndLocalDatabase.setActionCommand("GetIMDBInfo - chooseBetweenImdbAndLocalDatabase"); //$NON-NLS-1$
    				chooseBetweenImdbAndLocalDatabase.addActionListener(new ActionListener() {
    					public void actionPerformed(ActionEvent event) {
    						log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

    						if (addInfoToExistingMovie) {
    							panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogIMDB.panel-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
    							chooseBetweenImdbAndLocalDatabase.setText(Localizer.getString("DialogIMDB.button.add-to-existing-movie.text")); //$NON-NLS-1$
    							chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.getString("DialogIMDB.button.add-to-existing-movie.tooltip")); //$NON-NLS-1$
    							addInfoToExistingMovie = false;
    							executeSearchMultipleMovies();
    						}

    						else {
    							executeEditExistingMovie(""); //$NON-NLS-1$
    							chooseBetweenImdbAndLocalDatabase.setText(Localizer.getString("DialogIMDB.button.search-on-IMDb.text")); //$NON-NLS-1$
    							chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.getString("DialogIMDB.button.search-on-IMDb.tooltip")); //$NON-NLS-1$
    							addInfoToExistingMovie = true;

    							panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogIMDB.panel-your-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

    						}
    					}});

    				multipleMovieButtons.add(chooseBetweenImdbAndLocalDatabase);
    			}
    		}

    		if (addWithoutIMDBInfo) {

    			JButton addWithoutIMDBInfo = new JButton(Localizer.getString("DialogIMDB.button.add-without-web-info.text")); //$NON-NLS-1$
    			addWithoutIMDBInfo.setToolTipText(Localizer.getString("DialogIMDB.button.add-without-web-info.tooltip")); //$NON-NLS-1$
    			addWithoutIMDBInfo.setActionCommand("GetIMDBInfo - addWithoutIMDBInfo"); //$NON-NLS-1$

    			addWithoutIMDBInfo.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent event) {
					String fn = getFilename();
    					log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    					modelEntry.setTitle( fn.substring( 0, fn.lastIndexOf('.')) );
    					dropImdbInfoSet = true;
    					dispose();
    					return;
    				}});
    			multipleMovieButtons.add(addWithoutIMDBInfo);
    		}
	
    		JButton cancelAll = new JButton(Localizer.getString("DialogIMDB.button.abort.text")); //$NON-NLS-1$
    		cancelAll.setToolTipText(Localizer.getString("DialogIMDB.button.abort.tooltip")); //$NON-NLS-1$
    		cancelAll.setActionCommand("GetIMDBInfo - cancelAll"); //$NON-NLS-1$

    		cancelAll.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent event) {
    				log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    				/*Sets the cancelAll variable in multipleMovies object to true
                     This variable is checked in the multipleMovies object before saving,
                     and the appropriate action is performed.
    				 */
    				cancelAllSet = true;
    				dispose();
    			}});
    		multipleMovieButtons.add(cancelAll);
    		panelButtons.add(multipleMovieButtons, BorderLayout.NORTH);
    	}

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

    	if (!isMultiAdd()) {
    		buttonSelect.setEnabled(false);    		
    	}

    	JButton buttonCancel;

    	if (isMultiAdd() || getUrlKeyOnly) {
    		buttonCancel = new JButton(Localizer.getString("DialogIMDB.button.cancel.text.skip-movie")); //$NON-NLS-1$
    		buttonCancel.setToolTipText(Localizer.getString("DialogIMDB.button.cancel.ooltip.cancel")); //$NON-NLS-1$
    	}
    	else {
    		buttonCancel = new JButton(Localizer.getString("DialogIMDB.button.cancel.text.cancel")); //$NON-NLS-1$
    		buttonCancel.setToolTipText(Localizer.getString("DialogIMDB.button.cancel.tooltip.cancel")); //$NON-NLS-1$
    	}

    	buttonCancel.setActionCommand("GetIMDBInfo - Cancel"); //$NON-NLS-1$

    	buttonCancel.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
    			cancelSet = true;
    			dispose();
    		}});

    	panelRegularButtons.add(buttonCancel);
    	panelButtons.add(panelRegularButtons, BorderLayout.SOUTH);
    	
    	getContentPane().add(all,BorderLayout.CENTER);
    	getContentPane().add(panelButtons,BorderLayout.SOUTH);
    	
    	pack();
    	getMoviesList().ensureIndexIsVisible(0);
    	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
    			(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);


    	DefaultListModel model = new DefaultListModel();
    	model.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogIMDB.list-element.messsage.search-in-progress"), null)); //$NON-NLS-1$
    	listMovies.setModel(model);
	
    	
    	
    	if (isMultiAdd() && !getUrlKeyOnly) { 
    		
    		try {
    			DefaultListModel list = new IMDB(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(searchString);
    			/*Number of movie hits*/
    			int listSize = list.getSize();

    			if (list.getSize() == 0) {
    				list.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
    			}

    			listMovies.setModel(list);
    			listMovies.setSelectedIndex(0);

    			if (executeCommandMultipleMoviesSelectCheck(listSize) == 1) {
    				GUIUtil.showAndWait(this, true);
    			}

    		} catch (Exception e) {
    			executeErrorMessage(e.getMessage());
    			listMovies.setModel(null);
    		}
    	} else {
    		SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {
    					DefaultListModel list = new IMDB(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(modelEntry.getTitle());

    					if (list.getSize() == 0) {
    						list.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
    					}

    					listMovies.setModel(list);
    					listMovies.setSelectedIndex(0);

    					getButtonSelect().setEnabled(true);
    				}
    				catch (Exception e) {
    					log.error(e.getMessage(), e);
    					executeErrorMessage(e.getMessage());
    				}
    				return ""; //$NON-NLS-1$
    			}
    		};
    		worker.start();
    	}
    }


    /*The MovieManagerCommandFilter gets the movielist from the database ordered by movie title
     Then uses the searchstring to remove unwanted hits
     The last boolean argument states if the filter is called from the main search or the IMDB search.
     If called from the main search, it will take in consideration all the advanced search options.
     */

    void executeEditExistingMovie(String searchString) {

    	DefaultListModel listModel;

    	if (addToThisList != null)
    		listModel = MovieManager.getIt().getDatabase().getMoviesList("Title", addToThisList);
    	else
    		listModel = MovieManager.getIt().getDatabase().getMoviesList("Title");

    	listMovies.setModel(listModel);
    }

    /*Checks if the movie list should be retrived from IMDB or the local movie Database
     */
    void executeSearchMultipleMovies() {

    	if (addInfoToExistingMovie)
    		executeEditExistingMovie(searchStringField.getText());

    	else {
    		DefaultListModel listModel = new DefaultListModel();

    		try {
    			listModel = new IMDB(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(searchStringField.getText());
    		} catch (Exception e) {
    			executeErrorMessage(e.getMessage());

    			e.printStackTrace();
    			dispose();
    		}

    		if (listModel.getSize() == 0) {
    			listModel.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
    		}
    		
    		getMoviesList().setModel(listModel);
    		getMoviesList().setSelectedIndex(0);
    	}
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
    
    String getFilename() {
        return filename;
    }
    
   
    
    private int executeCommandMultipleMoviesSelectCheck(int listSize) {
        
        /* checks the property settings entered in the multi add movie preferences*/
      	
        if (multiAddSelectOption > 0) {
            
        	// No hits on IMDb
        	if (listSize == 0) {
        		
        		// Show search dialog 
        		if (multiAddSelectOption == 1 || multiAddSelectOption == 3)
        			return 1;
        		// Add to skipped-list
        		else {
        			dropImdbInfoSet = true;
        			return 0;
        		}
        	}
        	else
        	// If only one hit option
        	if (listSize == 1 && multiAddSelectOption == 3 || multiAddSelectOption == 4) {
        		executeCommandSelect();
        		return 0;
        	}
        	else
        	// Option 'select first hit' and there are more than 0 hits
        	if (multiAddSelectOption == 2 || multiAddSelectOption == 3) {
        		executeCommandSelect();
        		return 0;
        	}            
        }
        return 1;
    }
    
    /*ALerts the user of different error messages from proxy servers*/
    void executeErrorMessage(String exception) {
        
        if (exception == null)
            return;
        
        if (exception.startsWith("Server returned HTTP response code: 407")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.authentication-required"), Localizer.getString("DialogIMDB.alert.message.proxy-authentication-required")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection timed out")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-timed-out"), Localizer.getString("DialogIMDB.alert.message.connection-timed-out")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection reset")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-reset"), Localizer.getString("DialogIMDB.alert.message.connection-reset")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Server redirected too many  times")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.access-denied"), Localizer.getString("DialogIMDB.alert.message.username-of-password-invalid")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("The host did not accept the connection within timeout of")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-timed-out"), exception); //$NON-NLS-1$
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
    	 * therefore setCAncel method with true is called at the end of the if scoop.
    	 */	

    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	if (addInfoToExistingMovie) {

    		ModelMovie model = ((ModelMovie) listModel.getElementAt(index));

    		if (model.getKey() == -1)
    			return;

    		if (!model.getHasAdditionalInfoData()) {
    			model.updateAdditionalInfoData();
    		}

    		ModelMovieInfo modelInfoTmp = new ModelMovieInfo(model, false);

    		/* Need to set the hasReadProperties variable because when normally 
             calling the getfileinfo the first time it replaces the old additional values with the new ones
             Then the second time it plusses the time and size to match.
             When multiadding the next file info should be directly added to the old, not replace it
    		 */
    		modelInfoTmp._edit = true;
    		modelInfoTmp._hasReadProperties = true;
    		modelInfoTmp.getFileInfo(new File[] {multiAddFile});

    		try {
    			modelInfoTmp.saveToDatabase(null);
    		} catch (Exception e) {
    			log.error("Saving to database failed.", e);
    		}

    		cancelSet = true;
    		dispose();
    	}
    	else {
    		ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));
	
    		if (model.getUrlID() == null)
    			return;

    		if (getUrlKeyOnly) {
    			modelEntry.setUrlKey(model.getUrlID());
    			dispose();
    			return;
    		}
    		else {
    			time = System.currentTimeMillis();

    			getIMDbInfo(modelEntry, model.getUrlID());
    			ModelMovieInfo.executeTitleModification(modelEntry);
    			
    			dispose();
    		}
    	}
    }

    public static boolean getIMDbInfo(ModelEntry modelEntry, String key) {
    	IMDB imdb;

    	try {
    		imdb = new IMDB(key, MovieManager.getConfig().getHttpSettings());
    	} catch (Exception e) {
    		log.error(e.getMessage(), e); //$NON-NLS-1$
    		return false;
    	}

    	if (key.equals(imdb.getUrlID())) {

    		modelEntry.setTitle(imdb.getCorrectedTitle(imdb.getIMDbTitle()));
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
    	}
    	return true;
    }
}
