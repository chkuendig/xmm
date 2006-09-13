/**
 * @(#)DialogDatabaseConverter.java 1.0 04.11.05 (dd.mm.yy)
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
import net.sf.xmm.moviemanager.commands.MovieManagerCommandConvertDatabase;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.Timer;

import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseConverter;

import org.apache.log4j.Logger;


public class DialogDatabaseConverter extends JPanel implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    private JProgressBar progressBar;
    
    public final static int milliseconds = 1;
    private Timer timer;
    private JButton startButton;
    private JButton cancelButton;
    private JButton openDbButton;
    
    private DatabaseConverter databaseConverter;
    private JTextArea taskOutput;
    private String newline = "\n";
    Database newDatabase;
    ListModel movieListModel;
    ArrayList episodeList;
    int movieCounter = 0;
    int counter = 0;
    int lengthOfTask = 0;
    long conversionStart = 0;
    boolean canceled;
    String[] transferred;
    public MovieManagerCommandConvertDatabase parent;
    
    public DialogDatabaseConverter(final Database newDatabase, ListModel movieListModel, ArrayList episodeList, final MovieManagerCommandConvertDatabase parent) {
        super(new BorderLayout());
        this.parent = parent;
	this.newDatabase = newDatabase;
	this.movieListModel = movieListModel;
	this.episodeList = episodeList;
	
	databaseConverter = new DatabaseConverter(movieListModel.getSize()+ episodeList.size());
	
	startButton = new JButton("Start");
        startButton.setActionCommand("Start");
        startButton.addActionListener(this);
	
	cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
	cancelButton.setEnabled(false);
        cancelButton.addActionListener(this);
	
	openDbButton = new JButton("Open new database");
        openDbButton.setActionCommand("Open");
	openDbButton.setEnabled(false);
        openDbButton.addActionListener(this);

        progressBar = new JProgressBar(0, databaseConverter.getLengthOfTask());
        progressBar.setValue(0);
	progressBar.setString("                                                                                     ");
        progressBar.setStringPainted(true);
	
	taskOutput = new JTextArea(20, 50);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setCursor(null); 
	
        JPanel panel = new JPanel();
	panel.add(startButton);
	panel.add(cancelButton);
	panel.add(openDbButton);
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
		    transferred = databaseConverter.getTransferred();
		    if (transferred == null)
			Thread.sleep(2);
		}
	    } catch (Exception e) {
		log.warn("Exception:" + e);
	    }
	    
	    if (lengthOfTask == 0)
		lengthOfTask = databaseConverter.getLengthOfTask();
	    
	    while (counter < lengthOfTask && transferred[counter] != null) {
		
		movieCounter++;
		int percent = ((counter+1) * 100)/lengthOfTask;
		
		String msg = percent+ "%  (" + (counter+1) + " out of " + lengthOfTask+")     ";
		progressBar.setValue(counter+1);
		progressBar.setString(msg);
		taskOutput.append(movieCounter + " - " + transferred[counter] + newline);
		taskOutput.setCaretPosition(taskOutput.getDocument().getLength());
		counter++;
	    }
	    
	    if (databaseConverter.isDone() || canceled) {
		timer.stop();
		Toolkit.getDefaultToolkit().beep();
		
		if (!canceled) {
		    
		    String oldDB = "HSQL";
		    String newDB = "HSQL";
		    
		    if (MovieManager.getIt().getDatabase() instanceof DatabaseAccess)
			oldDB = "MS Access";
		    
		    if (newDatabase instanceof DatabaseAccess)
			newDB = "MS Access";
		    
		    taskOutput.append(newline + oldDB+ " database successfully transferred to a new "+ newDB +" database." + newline);
		    
		    taskOutput.append(movieCounter + " entries processed in " + (millisecondsToString(System.currentTimeMillis() - conversionStart)) + newline);
		    openDbButton.setEnabled(true);
		    cancelButton.setEnabled(false);
		    parent.setDone(true);
		}
		else {
		    taskOutput.append(newline + "Conversion canceled!" + newline);
		    parent.setCanceled(true);
		    
		    databaseConverter = new DatabaseConverter(movieListModel.getSize()+ episodeList.size());
		    timer = new Timer(milliseconds, new TimerListener());
		    
		    /*If the new database is HSQL a few exceptions may occur, but it still works*/
		    parent.deleteNewDatabase();
		    
		    newDatabase = parent.createNewDatabase();
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
	
	log.debug("ActionPerformed: "+ evt.getActionCommand());

	if (evt.getActionCommand().equals("Start")) {
	    
	    /*If the conversion was canceled it removes the listed movies to start fresh*/
	    if (!taskOutput.getText().equals(""))
		taskOutput.setText("");
	    
	    startButton.setEnabled(false);
	    cancelButton.setEnabled(true);
	    canceled = false;
	    parent.setCanceled(false);
	    parent.setDone(false);
	    databaseConverter.go(newDatabase, movieListModel, episodeList);
	    
	    timer.start();
	    conversionStart = System.currentTimeMillis();
	}
	
	if (evt.getActionCommand().equals("Cancel")) {
	    parent.setCanceled(true);
	    canceled = true;
	    cancelButton.setEnabled(false);
	    startButton.setEnabled(true);
	    databaseConverter.stop();
	}
	
	if (evt.getActionCommand().equals("Open")) {
	    openDbButton.setEnabled(false);
	    parent.setDbOpened(true);
	    parent.loadDatabase();
	    parent.dispose();
	}
    }
    
    public static String millisecondsToString(long time) {
	
	int milliseconds = (int)(time % 1000);
	int seconds = (int)((time/1000) % 60);
	int minutes = (int)((time/60000) % 60);
	//int hours = (int)((time/3600000) % 24);
	String millisecondsStr = (milliseconds<10 ? "00" : (milliseconds<100 ? "0" : ""))+milliseconds;
	String secondsStr = (seconds<10 ? "0" : "")+seconds;
	String minutesStr = (minutes<10 ? "0" : "")+minutes;
	//String hoursStr = (hours<10 ? "0" : "")+hours;
	
	String finalString = "";
	
	if (!minutesStr.equals("00"))
	    finalString += minutesStr+" min ";
	finalString += secondsStr+"."+millisecondsStr + " seconds.";
	
	return new String(finalString);
    }
}


