/**
 * @(#)DialogDatabaseImporter.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.ModelImportSettings;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;


public class DialogDatabaseImporter extends JPanel implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    private JProgressBar progressBar;
    
    public final static int milliseconds = 1;
    private Timer timer;
    private JButton startButton;
    private JButton cancelButton;
    private JButton closeButton;
    
    private DatabaseImporter databaseImporter;
    private JTextArea taskOutput;
    private String newline = "\n"; //$NON-NLS-1$
    
    int movieCounter = 0;
    int counter = 0;
    int lengthOfTask = 0;
    long conversionStart = 0;
    boolean canceled;
    String [] transferred;
    MovieManagerCommandImport parent;
    ModelImportSettings importSettings;
    
    public DialogDatabaseImporter(final MovieManagerCommandImport parent, ModelImportSettings importSettings) {
        super(new BorderLayout());
        this.parent = parent;
	this.importSettings = importSettings;
	
	databaseImporter = new DatabaseImporter(parent, importSettings);
	
	startButton = new JButton(Localizer.getString("DialogDatabaseImporter.button.start.text")); //$NON-NLS-1$
        startButton.setActionCommand("Start"); //$NON-NLS-1$
        startButton.addActionListener(this);
	
	cancelButton = new JButton(Localizer.getString("DialogDatabaseImporter.button.cancel.text")); //$NON-NLS-1$
        cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
	cancelButton.setEnabled(false);
        cancelButton.addActionListener(this);
	
	closeButton = new JButton(Localizer.getString("DialogDatabaseImporter.button.close.text")); //$NON-NLS-1$
        closeButton.setActionCommand("Close"); //$NON-NLS-1$
	closeButton.setEnabled(false);
        closeButton.addActionListener(this);

        progressBar = new JProgressBar();
        progressBar.setValue(0);
	progressBar.setString("                                 "); //$NON-NLS-1$
        progressBar.setStringPainted(true);
	progressBar.setPreferredSize(new Dimension(200, 25));
	
	taskOutput = new JTextArea(20, 50);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setCursor(null); 
	
        JPanel panel = new JPanel();
	panel.add(startButton);
	panel.add(cancelButton);
	panel.add(closeButton);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	
	/*Create a timer*/
        timer = new Timer(milliseconds, new TimerListener());
    }
    
    class TimerListener implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
	    
	    try {
		/*First run the array of processed movies may be null*/
		while (transferred == null) {
		    Thread.sleep(2);
		    transferred = databaseImporter.getTransferred();
		}
		
	    } catch (Exception e) {
		log.warn("Exception:"+ e); //$NON-NLS-1$
	    }
	    
	    if (lengthOfTask == 0) {
		lengthOfTask = databaseImporter.getLengthOfTask();
		
		if (lengthOfTask != 0) {
		    progressBar.setMinimum(0);
		    progressBar.setMaximum(lengthOfTask);
		}
	    }
	    
	    while (transferred != null && counter < lengthOfTask && transferred[counter] != null) {
		
		movieCounter++;
		int percent = ((counter+1) * 100)/lengthOfTask;
		
		String msg = percent+ "%  (" + (counter+1) + Localizer.getString("DialogDatabaseImporter.message.out-of") + lengthOfTask+")     "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		progressBar.setValue(counter+1);
		progressBar.setString(msg);
		taskOutput.append(movieCounter + " - " + transferred[counter] + newline); //$NON-NLS-1$
		taskOutput.setCaretPosition(taskOutput.getDocument().getLength());
		counter++;
	    }
	    
	    if (databaseImporter.isDone() || canceled) {
		timer.stop();
		
		if (!canceled) {
		    
		    taskOutput.append(movieCounter + Localizer.getString("DialogDatabaseImporter.message.entries-process-in") + (millisecondsToString(System.currentTimeMillis() - conversionStart)) + newline); //$NON-NLS-1$
		    closeButton.setEnabled(true);
		    cancelButton.setEnabled(false);
		    parent.setDone(true);
		}
		else {
		    taskOutput.append(newline + Localizer.getString("DialogDatabaseImporter.message.import-canceled") + newline); //$NON-NLS-1$
		    parent.setCanceled(true);
		    
		    databaseImporter = new DatabaseImporter(parent, importSettings);
		    timer = new Timer(milliseconds, new TimerListener());
		    
		    counter = 0;
		    movieCounter = 0;
		    transferred = null;
		}
	    }
	}
    }
    
    
    /**
     * Called when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
	
	log.debug("ActionPerformed: "+ evt.getActionCommand()); //$NON-NLS-1$

	if (evt.getActionCommand().equals("Start")) { //$NON-NLS-1$
	    
	    /*If the conversion was canceled it removes the listed movies to start fresh*/
	    if (!taskOutput.getText().equals("")) //$NON-NLS-1$
		taskOutput.setText(""); //$NON-NLS-1$
	    
	    startButton.setEnabled(false);
	    cancelButton.setEnabled(true);
	    closeButton.setEnabled(false);
	    
	    canceled = false;
	    parent.setCanceled(false);
	    parent.setDone(false);
	    databaseImporter.go();
	    
	    timer.start();
	    conversionStart = System.currentTimeMillis();
	    taskOutput.append(Localizer.getString("DialogDatabaseImporter.message.processing-import-list")); //$NON-NLS-1$
	}
	
	if (evt.getActionCommand().equals("Cancel")) { //$NON-NLS-1$
	    parent.setCanceled(true);
	    canceled = true;
	    cancelButton.setEnabled(false);
	    startButton.setEnabled(true);
	    closeButton.setEnabled(true);
	    databaseImporter.stop();
	}
	
	if (evt.getActionCommand().equals("Close")) { //$NON-NLS-1$
	    parent.dispose();
	    MovieManagerCommandSelect.executeAndReload(-1);
	}
    }
    
    public static String millisecondsToString(long time) {
	
	int milliseconds = (int)(time % 1000);
	int seconds = (int)((time/1000) % 60);
	int minutes = (int)((time/60000) % 60);
	//int hours = (int)((time/3600000) % 24);
	String millisecondsStr = (milliseconds<10 ? "00" : (milliseconds<100 ? "0" : ""))+milliseconds; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	String secondsStr = (seconds<10 ? "0" : "")+seconds; //$NON-NLS-1$ //$NON-NLS-2$
	String minutesStr = (minutes<10 ? "0" : "")+minutes; //$NON-NLS-1$ //$NON-NLS-2$
	//String hoursStr = (hours<10 ? "0" : "")+hours;
	
	String finalString = ""; //$NON-NLS-1$
	
	if (!minutesStr.equals("00")) //$NON-NLS-1$
	    finalString += minutesStr+Localizer.getString("DialogDatabaseImporter.message.minutes"); //$NON-NLS-1$
	finalString += secondsStr+"."+millisecondsStr + Localizer.getString("DialogDatabaseImporter.message.seconds"); //$NON-NLS-1$ //$NON-NLS-2$
	
	return new String(finalString);
    }
}


