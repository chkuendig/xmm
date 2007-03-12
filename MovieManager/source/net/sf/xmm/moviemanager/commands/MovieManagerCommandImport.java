/**
 * @(#)MovieManagerCommandImport.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.*;
import net.sf.xmm.moviemanager.models.ModelImportSettings;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

import java.awt.event.*;
import java.io.File;

import javax.swing.*;

public class MovieManagerCommandImport extends JDialog implements ActionListener{
    
    static Logger log = Logger.getRootLogger();
    
    boolean canceled = true;
    boolean done = false;
        

    
    boolean cancelAll = false;
   
    ModelImportSettings importSettings;
    
    public MovieManagerCommandImport() {
	super(MovieManager.getDialog(), Localizer.getString("MovieManagerCommandImport.dialog-importer.title"), true); //$NON-NLS-1$
    }
    
    void createAndShowGUI() {
	
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
	final JComponent newContentPane = new DialogDatabaseImporter(this, importSettings);
        newContentPane.setOpaque(true);
	setContentPane(newContentPane);
	pack();
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		    
		    if (canceled || done) {
			dispose();
			MovieManagerCommandSelect.executeAndReload(-1);
		    }
		}
	    });
	
	/*Dispose on escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    
		    if (canceled || done) {
			dispose();
			MovieManagerCommandSelect.executeAndReload(-1);
		    }
		}
	    };
	
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
	getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
	
	MovieManager mm = MovieManager.getIt();
	
	setLocation((int) mm.getLocation().getX()+(mm.getWidth()-getWidth())/2,
		    (int) mm.getLocation().getY()+(mm.getHeight()-getHeight())/2);
	
	setLocation((int)mm.getLocation().getX()+(mm.getWidth()- getWidth())/2,
		    (int)mm.getLocation().getY()+(mm.getHeight()- getHeight())/2);
	
	GUIUtil.show(this, true);
    }
    
    public void setCanceled(boolean canceled) {
	this.canceled = canceled;
    }
    
    public void setDone(boolean done) {
	this.done = done;
    }
    
    public void dispose() {
	GUIUtil.show(this, false);
    }
    
    public void setCancelAll(boolean value) {
	cancelAll = value;
    }
    
    protected void execute() {
	
	cancelAll = false;
	
	DialogImport importMovie = new DialogImport(this);
	importSettings = new ModelImportSettings();
	
	if (cancelAll)
	    return;
	
    
    
	importSettings.multiAddSelectOption = importMovie.getMultiAddSelectOption();
       	
	importSettings.importMode = importMovie.getImportMode();
	
	importSettings.filePath = importMovie.getPath();
	
	if (importMovie.enableAddMoviesToList != null && importMovie.enableAddMoviesToList.isSelected()) {
	    importSettings.addToThisList = (String) importMovie.listChooser.getSelectedItem();
	}
	
	/* Excel spreadsheet */
	if (importSettings.importMode == 1) {
        
        File file = new File(importMovie.getPath());
            
        if (file.isFile()) {
            DialogImportTable importTable = new DialogImportTable(this, file);
            GUIUtil.showAndWait(importTable, true);
            
            importSettings.table = importTable.getSettings().table;
            	
            dispose();
        }
	}
	
    /* extreme movie manager */
	else if (importSettings.importMode == 2) {
	    if (importMovie.enableOverwriteImportedInfoWithImdbInfo.isSelected())
		importSettings.overwriteWithImdbInfo = true;
	    
	    if (importMovie.useMediaLanguage.isSelected())
		importSettings.extremeOriginalLanguage = false;
	    
	    File tempFile = new File(importSettings.filePath);
	    
	    importSettings.coverPath = tempFile.getParentFile().getParent()+ File.separator +"Covers" + File.separator; //$NON-NLS-1$
	    if (!new File(importSettings.coverPath).isDirectory()) {
		
		String path = ""; //$NON-NLS-1$
		if (!(path = getCoverDirectory(importSettings.filePath)).equals("")) //$NON-NLS-1$
		    importSettings.coverPath = path + File.separator;
	    }
	}
	
	createAndShowGUI();
    }
    
     /*Opens a filechooser and returns the absolute path to the selected file*/
    private String getCoverDirectory(String databaseFilePath) {
	
	/* Opens the Open dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	try {
	    fileChooser.setFileSelectionMode(ExtendedFileChooser.DIRECTORIES_ONLY);
	    File path;
	    
	    if (!databaseFilePath.equals("") && (path = new File(databaseFilePath)).exists()) //$NON-NLS-1$
		fileChooser.setCurrentDirectory(path);
	    else if (MovieManager.getConfig().getLastFileDir() != null)
		fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastFileDir());
	    
	    fileChooser.setDialogTitle(Localizer.getString("MovieManagerCommandImport.dialog-importer.filechooser.title")); //$NON-NLS-1$
	    fileChooser.setApproveButtonText(Localizer.getString("MovieManagerCommandImport.dialog-importer.filechooser.approve-button.text")); //$NON-NLS-1$
	    fileChooser.setApproveButtonToolTipText(Localizer.getString("MovieManagerCommandImport.dialog-importer.filechooser.approve-button.tooltip")); //$NON-NLS-1$
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    
	    int returnVal = fileChooser.showOpenDialog(this);
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		/* Gets the path... */
		String filepath = fileChooser.getSelectedFile().getAbsolutePath();
		
		if (!(new File(filepath).exists())) {
		    throw new Exception("Covers directory not found."); //$NON-NLS-1$
		}
		/* Sets the last path... */
		MovieManager.getConfig().setLastFileDir(new File(filepath));
		return filepath;
	    }
	}
	catch (Exception e) {
	    log.error("", e); //$NON-NLS-1$
	}
	/* Sets the last path... */
	MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
	return ""; //$NON-NLS-1$
    }

    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	execute();
    }
}

    
    
    
