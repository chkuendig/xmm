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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelIMDB;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SwingWorker;
import net.sf.xmm.moviemanager.util.ShowGUI;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

public class DialogIMDB extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    ModelMovieInfo modelInfo;
    
    JList listMovies;
    JTextField searchStringField;
    boolean multiAdd = false;
    boolean addInfoToExistingMovie = false;
    int multiAddSelectOption = 0;
    File [] multiAddFile;
    JPanel panelMoviesList;
    
    boolean getUrlKeyOnly = false;
    
    long time;
    
    public boolean isMultiAdd() {
        return multiAdd;
    }
    
    /*Used only to tell the MovieManagerCommandAddMultipleMovies object
     to abort the multiple add movies method*/
    MovieManagerCommandAddMultipleMovies commandAddMovies = null;
    
    
    /**
     * The Constructor.
     **/
    public DialogIMDB(ModelMovieInfo modelInfo, boolean getUrlKeyOnly) {
        /* Dialog creation...*/
        super(MovieManager.getIt());
        multiAddFile = new File[1];
        this.getUrlKeyOnly = getUrlKeyOnly;
        
        /* Sets parent... */
        this.modelInfo = modelInfo;
        
        /* Close dialog... */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        /*Enables dispose when pushing escape*/
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        createListDialog("not used", "not used"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    /**
     * Constructor - When adding multiple movies by file.
     **/
    public DialogIMDB(ModelMovieInfo modelInfo, String searchString, String filename, final MovieManagerCommandAddMultipleMovies commandAddMovies, int multiAddSelectOption) {
        /* Dialog creation...*/
        super(MovieManager.getIt());
        multiAddFile = new File[1];
        
        /* Sets parent... */
        this.modelInfo = modelInfo;
        this.multiAdd = true;
        this.commandAddMovies = commandAddMovies;
        this.multiAddSelectOption = multiAddSelectOption;
        
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
                commandAddMovies.setCancel(true);
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        createListDialog(searchString, filename);
    }
    
    
    void createListDialog(final String searchString, String filename) {
        /* Dialog properties...*/
        setTitle(Localizer.getString("DialogIMDB.title")); //$NON-NLS-1$
        setModal(true);
        setResizable(false);
        /* Movies List panel...*/
        panelMoviesList = new JPanel();
        panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogIMDB.panel-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
        
        listMovies = new JList() {
            
            public String getToolTipText(MouseEvent e) {
                
                if (getCellBounds(0,0) == null)
                    return null;
                
                String retVal = null;
                int row = (int) e.getPoint().getY() / (int) getCellBounds(0,0).getHeight();
                
                if (row < getModel().getSize())
                    retVal = ((ModelIMDB) getMoviesList().getModel().getElementAt(row)).getAka();
                
                return retVal;
            }
            
            public JToolTip createToolTip() {
                
                JMultiLineToolTip tooltip = new JMultiLineToolTip();
                tooltip.setComponent(this);
                return tooltip;
            }
        };
        
        listMovies.setFixedCellHeight(18);
        
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
        
        /*If running the multiadd feature, here are all the extra buttons added. */
        if (isMultiAdd()) {
            
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
            all.add(searchStringPanel, BorderLayout.CENTER);
            
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
            
            if (commandAddMovies instanceof MovieManagerCommandAddMultipleMoviesByFile) {
                
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
                        
                        multiAddFile[0] = modelInfo.getMultiAddFile();
                        
                    }});
                
                multipleMovieButtons.add(chooseBetweenImdbAndLocalDatabase);
            }
            
            JButton addWithoutIMDBInfo = new JButton(Localizer.getString("DialogIMDB.button.add-without-web-info.text")); //$NON-NLS-1$
            addWithoutIMDBInfo.setToolTipText(Localizer.getString("DialogIMDB.button.add-without-web-info.tooltip")); //$NON-NLS-1$
            addWithoutIMDBInfo.setActionCommand("GetIMDBInfo - addWithoutIMDBInfo"); //$NON-NLS-1$
            
            addWithoutIMDBInfo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
                    
                    modelInfo.model.setTitle(searchStringField.getText());
                    commandAddMovies.setDropImdbInfo(true);
                    dispose();
                    return;
                }});
            multipleMovieButtons.add(addWithoutIMDBInfo);
            
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
                    commandAddMovies.setCancelAll(true);
                    dispose();
                    return;
                }});
            multipleMovieButtons.add(cancelAll);
            all.add(multipleMovieButtons, BorderLayout.SOUTH);
        }
        
        /* regular Buttons panel...*/
        JPanel panelRegularButtons = new JPanel();
        panelRegularButtons.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        panelRegularButtons.setLayout(new FlowLayout());
        
        JButton buttonMore = new JButton(Localizer.getString("DialogIMDB.button.more-titles.text")); //$NON-NLS-1$
        buttonMore.setToolTipText(Localizer.getString("DialogIMDB.button.more-titles.tooltip")); //$NON-NLS-1$
        buttonMore.setActionCommand("GetIMDBInfo - More"); //$NON-NLS-1$
        buttonMore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
                
                getButtonMore().setEnabled(false);
                getButtonSelect().setEnabled(false);
                executeCommandMore();
                getMoviesList().setSelectedIndex(0);
            }});
        
        panelRegularButtons.add(buttonMore);
        JButton buttonSelect = new JButton(Localizer.getString("DialogIMDB.button.select.text")); //$NON-NLS-1$
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
            buttonMore.setEnabled(false);
        }
        
        JButton buttonCancel;
        
        if (isMultiAdd()) {
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
                
                if (isMultiAdd())
                    commandAddMovies.setCancel(true);
                dispose();
            }});
        
        panelRegularButtons.add(buttonCancel);
        /* Adds all and buttonsPanel... */
        getContentPane().add(all,BorderLayout.NORTH);
        getContentPane().add(panelRegularButtons,BorderLayout.SOUTH);
        /* Packs and sets location... */
        pack();
        
        getMoviesList().ensureIndexIsVisible(0);
        setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
                (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
        
        
        
        
        //listModel.addElement(new ModelIMDB(key, movieTitle, aka));
        
        DefaultListModel model = new DefaultListModel();
        model.addElement(new ModelIMDB(null, Localizer.getString("DialogIMDB.list-element.messsage.search-in-progress"), null)); //$NON-NLS-1$
        listMovies.setModel(model);
        
        
        if (isMultiAdd()) { 
            try {
                DefaultListModel list = IMDB.getSimpleMatches(searchString);
                /*Number of movie hits*/
                int listSize = list.getSize();
                
                if (list.getSize() == 0) {
                    list.addElement(new ModelIMDB(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
                }
                
                listMovies.setModel(list);
                listMovies.setSelectedIndex(0);
                
                if (executeCommandMultipleMoviesSelectCheck(listSize) == 1) {
                    ShowGUI.showAndWait(this, true);
                }
                
            } catch (Exception e) {
                executeErrorMessage(e.getMessage());
                listMovies.setModel(null);
            }
        } else {
            SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    try {
                        DefaultListModel list = IMDB.getSimpleMatches(modelInfo.model.getTitle());
                        
                        if (list.getSize() == 0) {
                            list.addElement(new ModelIMDB(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
                        }
                        
                        listMovies.setModel(list);
                        listMovies.setSelectedIndex(0);
                        
                        getButtonMore().setEnabled(true);
                        getButtonSelect().setEnabled(true);
                    }
                    catch (Exception e) {
                        executeErrorMessage(e.getMessage());
                        listMovies.setModel(null);
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
        MovieManagerCommandFilter mmcf = new MovieManagerCommandFilter(searchString, getMoviesList(), false, true);
        MovieManagerCommandFilter.execute();
    }
    
    /*Checks if the movie list should be retrived from IMDB or the local movie Databse
     */
    void executeSearchMultipleMovies() {
        
        if (addInfoToExistingMovie)
            executeEditExistingMovie(searchStringField.getText());
        
        else {
            DefaultListModel listModel = null;
            
            try {
                
                if (!getButtonMore().getText().equals(Localizer.getString("DialogIMDB.button.more-titles.text"))) //$NON-NLS-1$
                    listModel = IMDB.getExtendedMatches(searchStringField.getText());
                else
                    listModel = IMDB.getSimpleMatches(searchStringField.getText());
                
            } catch (Exception e) {
                
                executeErrorMessage(e.getMessage());
                dispose();
            }
            
            getMoviesList().setModel(listModel);
            getMoviesList().setSelectedIndex(0);
        }
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
     * Returns the JButton More.
     **/
    protected JButton getButtonSelect() {
        return
        (JButton)
        ((JPanel)
                getContentPane().getComponent(1)).getComponent(1);
    }
    
    /**
     * Gets more or less info...
     **/
    void executeCommandMore() {
        
        DefaultListModel model = new DefaultListModel();
        model.addElement(new ModelMovie(-1, Localizer.getString("DialogIMDB.list-element.messsage.search-in-progress"))); //$NON-NLS-1$
        listMovies.setModel(model);
        
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                
                DefaultListModel listModel = null;
                
                try {
                    
                    if (getButtonMore().getText().equals(Localizer.getString("DialogIMDB.button.more-titles.text"))) { //$NON-NLS-1$
                        
                        if (isMultiAdd())
                            listModel = IMDB.getExtendedMatches(searchStringField.getText());
                        else
                            listModel = IMDB.getExtendedMatches(modelInfo.model.getTitle());
                        
                        
                        if (listModel.getSize() == 0) {
                            listModel.addElement(new ModelIMDB(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
                        }
                        
                        getMoviesList().setModel(listModel);
                        getMoviesList().setSelectedIndex(0);
                        
                        getButtonMore().setToolTipText(Localizer.getString("DialogIMDB.button.less-titles.tooltip")); //$NON-NLS-1$
                        getButtonMore().setText(Localizer.getString("DialogIMDB.button.less-titles.text")); //$NON-NLS-1$
                    } else {
                        
                        if (isMultiAdd())
                            listModel = IMDB.getSimpleMatches(searchStringField.getText());
                        else
                            listModel = IMDB.getSimpleMatches(modelInfo.model.getTitle());
                        
                        if (listModel.getSize() == 0) {
                            listModel.addElement(new ModelIMDB(null, Localizer.getString("DialogIMDB.list-element.messsage.no-hits-found"), null)); //$NON-NLS-1$
                        }
                        
                        getMoviesList().setModel(listModel);
                        getMoviesList().setSelectedIndex(0);
                        
                        getButtonMore().setToolTipText(Localizer.getString("DialogIMDB.button.more-titles.tooltip")); //$NON-NLS-1$
                        getButtonMore().setText(Localizer.getString("DialogIMDB.button.more-titles.text")); //$NON-NLS-1$
                    }
                    getButtonMore().setEnabled(true);
                    getButtonSelect().setEnabled(true);
                    
                } catch (Exception e) {
                    executeErrorMessage(e.getMessage());
                    dispose();
                }
                
                return ""; //$NON-NLS-1$
            }
        };
        worker.start();
    }
    
    private int executeCommandMultipleMoviesSelectCheck(int listSize) {
        
        /* checks the property settings entered in the multi add movie preferences*/
        
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
    
    /*ALerts the user of different error messages from proxy servers*/
    void executeErrorMessage(String exception) {
        
        if (exception == null)
            return;
        
        if (exception.startsWith("Server returned HTTP response code: 407")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.authentication-required"), Localizer.getString("DialogIMDB.alert.message.proxy-authentication-required")); //$NON-NLS-1$ //$NON-NLS-2$
            ShowGUI.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection timed out")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-timed-out"), Localizer.getString("DialogIMDB.alert.message.connection-timed-out")); //$NON-NLS-1$ //$NON-NLS-2$
            ShowGUI.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection reset")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-reset"), Localizer.getString("DialogIMDB.alert.message.connection-reset")); //$NON-NLS-1$ //$NON-NLS-2$
            ShowGUI.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Server redirected too many  times")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.access-denied"), Localizer.getString("DialogIMDB.alert.message.username-of-password-invalid")); //$NON-NLS-1$ //$NON-NLS-2$
            ShowGUI.showAndWait(alert, true);
        }
        
        if (exception.startsWith("The host did not accept the connection within timeout of")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogIMDB.alert.title.connection-timed-out"), exception); //$NON-NLS-1$
            ShowGUI.showAndWait(alert, true);
        }
    }
    
    /**
     * Gets more or less info...
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
            
            ModelMovieInfo modelInfoTmp = new ModelMovieInfo(false, true);
            
            /* Need to set the hasReadProperties variable because when normally 
             calling the getfileinfo the first time it replaces the old additional values with the new ones
             Then the second time it plusses the time and size to match.
             When multiadding the next file info should be directly added to the old, not replace it
             */
            modelInfoTmp._hasReadProperties = true;
            modelInfoTmp.getFileInfo(multiAddFile);
            
            try {
                modelInfoTmp.saveToDatabase(null);
            } catch (Exception e) {
                log.error("Saving to database failed.", e);
            }
                
            MovieManagerCommandSelect.executeAndReload(modelInfoTmp.model, modelInfoTmp._edit, modelInfoTmp.isEpisode, true);
            
            commandAddMovies.setCancel(true);
            dispose();
        }
        else {
            ModelIMDB model = ((ModelIMDB) listModel.getElementAt(index));
            
            if (model.getKey() == null)
                return;
                        
            if (getUrlKeyOnly) {
                modelInfo.model.setUrlKey(model.getKey());
                dispose();
                return;
            }
            else {
                time = System.currentTimeMillis();
                
                IMDB imdb;
                
                try {
                    imdb = new IMDB(model.getKey());
                } catch (Exception e) {
                    log.error(e.getMessage()); //$NON-NLS-1$
                    return;
                }
                
                modelInfo.model.setTitle(imdb.getTitle());
                modelInfo.model.setDate(imdb.getDate());
                modelInfo.model.setColour(imdb.getColour());
                modelInfo.model.setDirectedBy(imdb.getDirectedBy());
                modelInfo.model.setWrittenBy(imdb.getWrittenBy());
                modelInfo.model.setGenre(imdb.getGenre());
                modelInfo.model.setRating(imdb.getRating());
                modelInfo.model.setCountry(imdb.getCountry());
                modelInfo.model.setLanguage(imdb.getLanguage());
                modelInfo.model.setPlot(imdb.getPlot());
                modelInfo.model.setCast(imdb.getCast());
                
                modelInfo.model.setWebRuntime(imdb.getRuntime());
                modelInfo.model.setWebSoundMix(imdb.getSoundMix());
                modelInfo.model.setAwards(imdb.getAwards());
                modelInfo.model.setMpaa(imdb.getMpaa());
                modelInfo.model.setAka(imdb.getAka());
                modelInfo.model.setCertification(imdb.getCertification());
                
                modelInfo.model.setUrlKey(imdb.getKey());
                
                /* The cover... */
                byte[] coverData = imdb.getCover();
                
                if (imdb.getCoverOK()) {
                    modelInfo.setCover(imdb.getCoverName(), coverData);
                    modelInfo.setSaveCover(true);
                } else {
                    modelInfo.setCover("", null);
                    modelInfo.setSaveCover(false);
                }
                
                modelInfo.executeTitleModification(imdb.getTitle());
                
                dispose();
            }
        }
    }
}
