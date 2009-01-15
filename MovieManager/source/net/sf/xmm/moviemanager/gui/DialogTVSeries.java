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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.extentions.events.ModelUpdatedEvent.IllegalEventTypeException;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.log4j.Logger;

public class DialogTVSeries extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    private JList listMovies;
    private JPanel panelMoviesList;
    public JButton buttonSelectAll;
    public JButton buttonSelect;
    public JButton buttonOk;
    
    
    /**
     * The Constructor.
     **/
    public DialogTVSeries(String searchTitle, JDialog parent) {
        /* Dialog creation...*/
        super(parent);
                	
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
        
        createListDialog(null);
    }
    
    void createListDialog(DefaultListModel list) {
        /* Dialog properties...*/
        setTitle(Localizer.getString("DialogTVDOTCOM.title")); //$NON-NLS-1$
        setModal(true);
        setResizable(false);
        
        /* Movies List panel...*/
        panelMoviesList = new JPanel(new BorderLayout());
        panelMoviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Episode Search "), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
        
        listMovies = new JList();
        listMovies.setFixedCellHeight(18);
        listMovies.setFont(new Font(listMovies.getFont().getName(),Font.PLAIN,listMovies.getFont().getSize()));
        listMovies.setLayoutOrientation(JList.VERTICAL);
        listMovies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listMovies.setCellRenderer(new TVSeriesListCellRenderer());
        
        JScrollPane scrollPaneMovies = new JScrollPane(listMovies);
        scrollPaneMovies.setPreferredSize(new Dimension(300,255));
        panelMoviesList.add(scrollPaneMovies, BorderLayout.CENTER);
        
        /* To add outside border... */
        JPanel all = new JPanel();
        all.setLayout(new BorderLayout());
        all.add(panelMoviesList, BorderLayout.CENTER);
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
        
        buttonSelect = new JButton(Localizer.getString("DialogTVDOTCOM.button.select.text")); //$NON-NLS-1$
        buttonSelect.setToolTipText(Localizer.getString("DialogTVDOTCOM.button.select.tooltip")); //$NON-NLS-1$
        buttonSelect.setEnabled(false);
        buttonSelect.setActionCommand("GetTVDOTCOMInfo - Select"); //$NON-NLS-1$
               
        panelRegularButtons.add(buttonSelect);
        
        
        buttonOk = new JButton(Localizer.getString("DialogTVDOTCOM.button.cancel.text")); //$NON-NLS-1$
        buttonOk.setToolTipText(Localizer.getString("DialogTVDOTCOM.button.cancel.tooltip")); //$NON-NLS-1$
        
        buttonOk.setActionCommand("GetIMDBInfo - Cancel"); //$NON-NLS-1$
        
       
        
        panelRegularButtons.add(buttonOk);
        
        all.add(panelRegularButtons,BorderLayout.SOUTH);
        
        /* Adds all and buttonsPanel... */
        getContentPane().add(all);
        
        /* Packs and sets location... */
        setPreferredSize(new Dimension(400, 400));
        
        pack();
        
        getMoviesList().ensureIndexIsVisible(0);
        setLocation((int)MovieManager.getDialog().getLocation().getX()+(MovieManager.getDialog().getWidth()-getWidth())/2,
                (int)MovieManager.getDialog().getLocation().getY()+(MovieManager.getDialog().getHeight()-getHeight())/2);
        
        DefaultListModel model = new DefaultListModel();
        model.addElement(new ModelMovie(-1, Localizer.getString("DialogTVDOTCOM.list-item.message.search-in-progress"))); //$NON-NLS-1$
        listMovies.setModel(model);
    }
    
    /**
     * Returns the JList listMovies.
     **/
    public JList getMoviesList() {
        return listMovies;
    }
    
    
    /**
     * Returns the JButton Select.
     **/
    public JButton getButtonSelect() {
        return buttonSelect;
    }
 
    
    public class TVSeriesListCellRenderer extends DefaultListCellRenderer {
	  
	  public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean hasFocus) {
	    super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
	  
	   if (value instanceof ModelIMDbSearchHit) {
		  
		   if (((ModelIMDbSearchHit) value).processed) {
			   setBackground(new Color(125, 203, 138));
		   }
	   }
	   return this;
	  }
	}
}

