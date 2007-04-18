/**
 * @(#)DialogTVDOTCOM.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.http.TVDOTCOM;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SwingWorker;
import net.sf.xmm.moviemanager.util.ModelUpdatedEvent.IllegalEventTypeException;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

class DialogTVDOTCOM extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    //private DialogMovieInfo _parent;
    
    ModelMovieInfo modelInfo;
    
    private JList listMovies;
    
    private JPanel panelMoviesList;
    
    private StringBuffer [] streams;
    
    private JButton buttonSelectAll;
    
    /* The current mode (Season/episode */
    private static int mode = 0;
    
    public boolean multipleEpisodesAdded = false;
    
    /**
     * The Constructor.
     **/
    protected DialogTVDOTCOM(ModelMovieInfo modelInfo) {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        
        this.modelInfo = modelInfo;
        
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
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        
        createListDialog(null);
    }
    
    void createListDialog(DefaultListModel list) {
        /* Dialog properties...*/
        setTitle(Localizer.getString("DialogTVDOTCOM.title")); //$NON-NLS-1$
        setModal(true);
        setResizable(false);
        mode = 0;
        
        /* Movies List panel...*/
        panelMoviesList = new JPanel();
        panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," TV.COM "), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
        
        listMovies = new JList();
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
        
        /* regular Buttons panel...*/
        JPanel panelRegularButtons = new JPanel();
        panelRegularButtons.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        panelRegularButtons.setLayout(new FlowLayout());
        
        buttonSelectAll = new JButton(Localizer.getString("DialogTVDOTCOM.button.select-all.text")); //$NON-NLS-1$
        buttonSelectAll.setActionCommand("Select All"); //$NON-NLS-1$
        buttonSelectAll.setEnabled(false);
        buttonSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                
                if (getMoviesList().getModel().getSize() > 0)
                    getMoviesList().setSelectionInterval(0, getMoviesList().getModel().getSize()-1);
                
            }});
        panelRegularButtons.add(buttonSelectAll);
        
        JButton buttonSelect = new JButton(Localizer.getString("DialogTVDOTCOM.button.select.text")); //$NON-NLS-1$
        buttonSelect.setToolTipText(Localizer.getString("DialogTVDOTCOM.button.select.tooltip")); //$NON-NLS-1$
        buttonSelect.setEnabled(false);
        buttonSelect.setActionCommand("GetTVDOTCOMInfo - Select"); //$NON-NLS-1$
        buttonSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandSelect();
            }});
        
        panelRegularButtons.add(buttonSelect);
        
        JButton buttonOk;
        buttonOk = new JButton(Localizer.getString("DialogTVDOTCOM.button.cancel.text")); //$NON-NLS-1$
        buttonOk.setToolTipText(Localizer.getString("DialogTVDOTCOM.button.cancel.tooltip")); //$NON-NLS-1$
        
        buttonOk.setActionCommand("GetIMDBInfo - Cancel"); //$NON-NLS-1$
        
        buttonOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
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
        setLocation((int)MovieManager.getDialog().getLocation().getX()+(MovieManager.getDialog().getWidth()-getWidth())/2,
                (int)MovieManager.getDialog().getLocation().getY()+(MovieManager.getDialog().getHeight()-getHeight())/2);
        
        DefaultListModel model = new DefaultListModel();
        model.addElement(new ModelMovie(-1, Localizer.getString("DialogTVDOTCOM.list-item.message.search-in-progress"))); //$NON-NLS-1$
        listMovies.setModel(model);
        
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                try {
                    DefaultListModel list = TVDOTCOM.getSeriesMatches(modelInfo.model.getTitle());
                    
                    if (list == null) {
                        final DefaultListModel model = new DefaultListModel();
                        model.addElement(new ModelMovie(-1, Localizer.getString("DialogTVDOTCOM.list-item.message.no-matches-found"))); //$NON-NLS-1$
                        
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
                    return ""; //$NON-NLS-1$
                }
                return ""; //$NON-NLS-1$
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
    
    
    /* Alerts the user of different error messages from proxy servers*/
    void executeErrorMessage() {
        
        String exception = TVDOTCOM.getException();
        
        if (exception == null)
            return;
        
        if (exception.startsWith("Server returned HTTP response code: 407")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.authentication-requeried"), Localizer.getString("DialogTVDOTCOM.alert.message.proxy-server-requires-authentication")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection timed out")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.connection-timed.out"), Localizer.getString("DialogTVDOTCOM.alert.message.connection-timed.out")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection reset")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.connection-reset"), Localizer.getString("DialogTVDOTCOM.alert.message.connection-reset-by-server")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Server redirected too many  times")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.access-denied"), Localizer.getString("DialogTVDOTCOM.alert.message.username-or-password-invalid")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
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
            ModelEntry tmpEntry = null;
            
            String coverFileName = ((ModelSearchHit) selectedValues[0]).getUrlTitle() +((ModelSearchHit) selectedValues[0]).getCoverExtension();
            
            if (selectedValues.length > 1)
                multipleEpisodesAdded = true;
            
            for (int i = 0; i < selectedValues.length; i++) {
                
                //long time = System.currentTimeMillis();
                
                ModelEpisode episode = TVDOTCOM.getEpisodeInfo((ModelSearchHit) selectedValues[i], streams);
                 
				modelInfo.setModel(episode, true, false);
                
                // The cover... 
                if (cover != null) {
                    modelInfo.setCover(coverFileName, cover);
                    modelInfo.setSaveCover(true);
                } else {
                    modelInfo.setCover("", null);
                    modelInfo.setSaveCover(false);
                }
                
                if (multipleEpisodesAdded) {
                    
                    try {
                        tmpEntry = modelInfo.saveToDatabase(null);
                    } catch (Exception e) {
                        log.error("Saving to database failed.", e);
                    }
                    
                    if (i+1 == selectedValues.length)
                        execute = true;
                    
                    /* Adding each entry to the movie list */
                    MovieManagerCommandSelect.executeAndReload(tmpEntry, false, true, execute);
                }
            }
            
            try {
            	modelInfo.saveCoverToFile();
            } catch (Exception e) {
                log.error("Saving cover file failed.", e);
            }
            dispose();
            
            try {
            	modelInfo.modelChanged(this, "GeneralInfo");
            } catch (IllegalEventTypeException e) {
            	log.error("IllegalEventTypeException:" + e.getMessage());
            }
            
            }
        mode++;
    }
}

