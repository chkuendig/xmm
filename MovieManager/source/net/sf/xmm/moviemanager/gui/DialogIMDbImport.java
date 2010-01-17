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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandPlay;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
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
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

public class DialogIMDbImport extends DialogIMDbUpdate {
    
	private static final long serialVersionUID = 9074815790929713958L;

	static Logger log = Logger.getLogger(DialogIMDbImport.class);
	
	protected JButton addWithoutIMDBInfoButton;
    
    ImdbImportOption multiAddSelectOption = ImdbImportOption.displayList;
        
    boolean getUrlKeyOnly = false;

    String addToThisList = null; 
    
    public boolean dropImdbInfoSet = false;
    
    DialogIMDbImport thisDialog = this;
    
    public JPanel multipleMovieButtons;
    
    public boolean getDropIMDbInfo() {
    	return dropImdbInfoSet;
    }
    
    public void resetFeedbackValues() {
    	super.resetFeedbackValues();
    	dropImdbInfoSet = false;
    }
        
    
    public DialogIMDbImport(ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits) {
    	super(modelEntry, alternateTitle, false);
    	    	
    	if (alternateTitle != null)
    		setTitle(alternateTitle);

    	createDialogImportComponents();

    	getSearchField().setText(alternateTitle);
    
    	if (hits != null)
    		handleSearchResults(hits);
    	else
    		super.executeSearch();
    }
       
    // May not want to execute method, therefore override since it'll be called by the DialogIMDb constructor, 
    public void callSearch() {
    	
    }
    
    // Override parent create component method
    public void createDialogUpdateComponents() {}
    
    
    void createDialogImportComponents() {
    	
    	addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});    	    	
    	
    	JPanel multipleMovieButtons = new JPanel();
    	multipleMovieButtons.setLayout(new FlowLayout());

    	abortButton = createAbortButton();
    	addWithoutIMDBInfoButton = createAddWithoutIMDBInfoButton();
    	multipleMovieButtons.add(addWithoutIMDBInfoButton);
    	multipleMovieButtons.add(abortButton);
    	    	    	
    	subclassButtons.add(multipleMovieButtons, BorderLayout.CENTER);
    	
    	getButtonCancel().setText(Localizer.get("DialogIMDB.button.cancel.text.skip-movie")); //$NON-NLS-1$
        
    	pack();
    	
    	Dimension dim = MovieManager.getConfig().getMultiAddIMDbDialogWindowSize();
    	
    	if (dim != null && dim.height > 0 && dim.width > 0) {
    		setSize(dim);
    	}

    	setHotkeyModifiers();
    }


    JButton createAddWithoutIMDBInfoButton() {

    	JButton addWithoutIMDBInfo = new JButton(Localizer.get("DialogIMDbMultiAdd.button.add-without-web-info.text")); //$NON-NLS-1$
    	addWithoutIMDBInfo.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-without-web-info.tooltip")); //$NON-NLS-1$
    	addWithoutIMDBInfo.setActionCommand("GetIMDBInfo - addWithoutIMDBInfo"); //$NON-NLS-1$

    	addWithoutIMDBInfo.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {

    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    			if (getSearchField().getText().trim().equals("")) { //$NON-NLS-1$
    				DialogAlert alert = new DialogAlert(thisDialog, Localizer.get("DialogIMDbMultiAdd.alert.no-title-specified.title"), "<html>" + Localizer.get("DialogIMDbMultiAdd.alert.no-title-specified.message") + "</html>", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    				GUIUtil.show(alert, true);
    				return;
    			}

    			modelEntry.setTitle(getSearchField().getText());

    			dropImdbInfoSet = true;
    			dispose();
    			return;
    		}});

    	return addWithoutIMDBInfo;
    }
    
           
    
    /**
     * Overrides super class method
     **/
    /*
    void executeCommandSelect() {
    	    	
    	int index = getMoviesList().getSelectedIndex();
    	
    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));
    	    	
    	if (model.getUrlID() == null)
    		return;

    	if (getUrlKeyOnly) {
    		modelEntry.setUrlKey(model.getUrlID());
    		dispose();
    		return;
    	}

    	getIMDbInfo(modelEntry, model.getUrlID());
    	ModelMovieInfo.executeTitleModification(modelEntry);

    	dispose();
    }
*/    
    
    private void setHotkeyModifiers() {
    	    	    	
    	try {
			// ALT+W for add without IMDb info
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Add without IMDb info", new AbstractAction() {
						public void actionPerformed(ActionEvent ae) {
							addWithoutIMDBInfoButton.doClick();
						}
					}, addWithoutIMDBInfoButton);
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
    }
}
