/**
 * @(#)DialogUpdateIMDbInfo.java 1.0 26.09.06 (dd.mm.yy)
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
import net.sf.xmm.moviemanager.commands.MovieManagerCommandUpdateIMDBInfo;
import net.sf.xmm.moviemanager.extentions.ButtonGroupNoSelection;
import net.sf.xmm.moviemanager.util.ShowGUI;

import org.apache.log4j.Logger;

import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class DialogUpdateIMDbInfo extends JPanel implements ActionListener, ItemListener {
    
    static Logger log = Logger.getRootLogger();
    
    private JProgressBar progressBar;
    
    public final static int milliseconds = 1;
    private Timer timer;
    private JButton startButton;
    private JButton cancelButton;
    private JButton closeButton;
    
    private IMDbInfoUpdater imdbInfoUpdater;
    private JTextArea taskOutput;
    private String newline = "\n";
    
    int movieCounter = 0;
    int counter = 0;
    int lengthOfTask = 0;
    long conversionStart = 0;
    boolean canceled;
    ArrayList transferred;
    MovieManagerCommandUpdateIMDBInfo parent;
    
    
    /* update settings buttons */
    JCheckBox titleUpdate;
    JCheckBox titleUpdateIfEmpty;
    JCheckBox coverUpdate;
    JCheckBox coverUpdateIfEmpty;
    JCheckBox dateUpdate;
    JCheckBox dateUpdateIfEmpty;
    JCheckBox colourUpdate;
    JCheckBox colourUpdateIfEmpty;
    JCheckBox directedByUpdate;
    JCheckBox directedByUpdateIfEmpty;
    JCheckBox writtenByUpdate;
    JCheckBox writtenByUpdateIfEmpty;
    JCheckBox genreUpdate;
    JCheckBox genreUpdateIfEmpty;
    JCheckBox ratingUpdate;
    JCheckBox ratingUpdateIfEmpty;
    JCheckBox countryUpdate;
    JCheckBox countryUpdateIfEmpty;
    JCheckBox languageUpdate;
    JCheckBox languageUpdateIfEmpty;
    JCheckBox plotUpdate;
    JCheckBox plotUpdateIfEmpty;
    JCheckBox castUpdate;
    JCheckBox castUpdateIfEmpty;
    JCheckBox akaUpdate;
    JCheckBox akaUpdateIfEmpty;
    JCheckBox soundMixUpdate;
    JCheckBox soundMixUpdateIfEmpty;
    JCheckBox runtimeUpdate;
    JCheckBox runtimeUpdateIfEmpty;
    JCheckBox awardsUpdate;
    JCheckBox awardsUpdateIfEmpty;
    JCheckBox mpaaUpdate;
    JCheckBox mpaaUpdateIfEmpty;
    JCheckBox certificationUpdate;
    JCheckBox certificationUpdateIfEmpty;
    
    JCheckBox markAll;
    JCheckBox markAllIfEmpty;
    
    JDialog dialog;
    
    public DialogUpdateIMDbInfo(final MovieManagerCommandUpdateIMDBInfo parent, JDialog dialog) {
        super(new BorderLayout());
        this.parent = parent;
	this.dialog = dialog;

	imdbInfoUpdater = new IMDbInfoUpdater();
	
	startButton = new JButton("Start");
        startButton.setActionCommand("Start");
        startButton.addActionListener(this);
	
	cancelButton = new JButton("Abort");
        cancelButton.setActionCommand("Cancel");
	cancelButton.setEnabled(false);
        cancelButton.addActionListener(this);
	
	closeButton = new JButton("Close");
        closeButton.setActionCommand("Close");
	closeButton.setEnabled(true);
        closeButton.addActionListener(this);

        progressBar = new JProgressBar();
        progressBar.setValue(0);
	progressBar.setString("                                 ");
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

	
	/* Creating update options */
	
	JPanel updateSettingsPanel = new JPanel();
	updateSettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,5,2,5), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
																					" Update settings ",
																					TitledBorder.DEFAULT_JUSTIFICATION,
																					TitledBorder.DEFAULT_POSITION,
																					new Font(updateSettingsPanel.getFont().getName(),Font.PLAIN, updateSettingsPanel.getFont().getSize())),
																		      BorderFactory.createEmptyBorder(2,5,3,5))));
	
	
	double size[][] = {{TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED}, 
			   {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED}};
	
	updateSettingsPanel.setLayout(new TableLayout(size));
	
	/* Header */
	
	JLabel headerUpdateInfo = new JLabel("Update");
	updateSettingsPanel.add(headerUpdateInfo, "0, 0, CENTER, CENTER");
	
	JLabel headerIfEmpty = new JLabel("If empty");
	updateSettingsPanel.add(headerIfEmpty, "2, 0, CENTER, CENTER");

	JLabel headerFiled = new JLabel("Field");
	updateSettingsPanel.add(headerFiled, "4, 0, CENTER, CENTER");
	
	
	/* Title */
	ButtonGroupNoSelection titleButtonGroup = new ButtonGroupNoSelection();
	
	titleUpdate = new JCheckBox();
	updateSettingsPanel.add(titleUpdate, "0, 1, CENTER, CENTER");
	titleButtonGroup.add(titleUpdate);
	
	titleUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(titleUpdateIfEmpty, "2, 1, CENTER, CENTER");
	titleButtonGroup.add(titleUpdateIfEmpty);
	
	JLabel titleLabel = new JLabel("Title");
	updateSettingsPanel.add(titleLabel, "4, 1");
	
	
	/* Cover */
	ButtonGroupNoSelection coverButtonGroup = new ButtonGroupNoSelection();
	
	coverUpdate = new JCheckBox();
	updateSettingsPanel.add(coverUpdate, "0, 2, CENTER, CENTER");
	coverButtonGroup.add(coverUpdate);

	coverUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(coverUpdateIfEmpty, "2, 2, CENTER, CENTER");
	coverButtonGroup.add(coverUpdateIfEmpty);

	JLabel coverLabel = new JLabel("Cover");
	updateSettingsPanel.add(coverLabel, "4, 2");
	
	
	/* date */
	ButtonGroupNoSelection dateButtonGroup = new ButtonGroupNoSelection();
	
	dateUpdate = new JCheckBox();
	updateSettingsPanel.add(dateUpdate, "0, 3, CENTER, CENTER");
	dateButtonGroup.add(dateUpdate);

	dateUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(dateUpdateIfEmpty, "2, 3, CENTER, CENTER");
	dateButtonGroup.add(dateUpdateIfEmpty);

	JLabel dateLabel = new JLabel("Date");
	updateSettingsPanel.add(dateLabel, "4, 3");
	
	
	/* colour */
	ButtonGroupNoSelection colourButtonGroup = new ButtonGroupNoSelection();
	
	colourUpdate = new JCheckBox();
	updateSettingsPanel.add(colourUpdate, "0, 4, CENTER, CENTER");
	colourButtonGroup.add(colourUpdate);

	colourUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(colourUpdateIfEmpty, "2, 4, CENTER, CENTER");
	colourButtonGroup.add(colourUpdateIfEmpty);

	JLabel colourLabel = new JLabel("Colour");
	updateSettingsPanel.add(colourLabel, "4, 4");

	
	/* Directed By */
	ButtonGroupNoSelection directedByButtonGroup = new ButtonGroupNoSelection();
	
	directedByUpdate = new JCheckBox();
	updateSettingsPanel.add(directedByUpdate, "0, 5, CENTER, CENTER");
	directedByButtonGroup.add(directedByUpdate);

	directedByUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(directedByUpdateIfEmpty, "2, 5, CENTER, CENTER");
	directedByButtonGroup.add(directedByUpdateIfEmpty);

	JLabel directedByLabel = new JLabel("Directed By");
	updateSettingsPanel.add(directedByLabel, "4, 5");

	
	/* writtenBy */
	ButtonGroupNoSelection writtenByButtonGroup = new ButtonGroupNoSelection();
	
	writtenByUpdate = new JCheckBox();
	updateSettingsPanel.add(writtenByUpdate, "0, 6, CENTER, CENTER");
	writtenByButtonGroup.add(writtenByUpdate);

	writtenByUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(writtenByUpdateIfEmpty, "2, 6, CENTER, CENTER");
	writtenByButtonGroup.add(writtenByUpdateIfEmpty);

	JLabel writtenByLabel = new JLabel("Written By");
	updateSettingsPanel.add(writtenByLabel, "4, 6");
	
	
	/* genre */
	ButtonGroupNoSelection genreButtonGroup = new ButtonGroupNoSelection();
	
	genreUpdate = new JCheckBox();
	updateSettingsPanel.add(genreUpdate, "0, 7, CENTER, CENTER");
	genreButtonGroup.add(genreUpdate);

	genreUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(genreUpdateIfEmpty, "2, 7, CENTER, CENTER");
	genreButtonGroup.add(genreUpdateIfEmpty);

	JLabel genreLabel = new JLabel("Genre");
	updateSettingsPanel.add(genreLabel, "4, 7");


	/* rating */
	ButtonGroupNoSelection ratingButtonGroup = new ButtonGroupNoSelection();
	
	ratingUpdate = new JCheckBox();
	updateSettingsPanel.add(ratingUpdate, "0, 8, CENTER, CENTER");
	ratingButtonGroup.add(ratingUpdate);

	ratingUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(ratingUpdateIfEmpty, "2, 8, CENTER, CENTER");
	ratingButtonGroup.add(ratingUpdateIfEmpty);

	JLabel ratingLabel = new JLabel("Rating");
	updateSettingsPanel.add(ratingLabel, "4, 8");
	
	/* country */
	ButtonGroupNoSelection countryButtonGroup = new ButtonGroupNoSelection();
	
	countryUpdate = new JCheckBox();
	updateSettingsPanel.add(countryUpdate, "0, 9, CENTER, CENTER");
	countryButtonGroup.add(countryUpdate);

	countryUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(countryUpdateIfEmpty, "2, 9, CENTER, CENTER");
	countryButtonGroup.add(countryUpdateIfEmpty);

	JLabel countryLabel = new JLabel("Country");
	updateSettingsPanel.add(countryLabel, "4, 9");
	
	/* language */
	ButtonGroupNoSelection languageButtonGroup = new ButtonGroupNoSelection();
	
	languageUpdate = new JCheckBox();
	updateSettingsPanel.add(languageUpdate, "0, 10, CENTER, CENTER");
	languageButtonGroup.add(languageUpdate);

	languageUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(languageUpdateIfEmpty, "2, 10, CENTER, CENTER");
	languageButtonGroup.add(languageUpdateIfEmpty);

	JLabel languageLabel = new JLabel("Language");
	updateSettingsPanel.add(languageLabel, "4, 10");
	
	
	/* plot */
	ButtonGroupNoSelection plotButtonGroup = new ButtonGroupNoSelection();
	
	plotUpdate = new JCheckBox();
	updateSettingsPanel.add(plotUpdate, "0, 11, CENTER, CENTER");
	plotButtonGroup.add(plotUpdate);

	plotUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(plotUpdateIfEmpty, "2, 11, CENTER, CENTER");
	plotButtonGroup.add(plotUpdateIfEmpty);

	JLabel plotLabel = new JLabel("Plot");
	updateSettingsPanel.add(plotLabel, "4, 11");
	
	
	/* cast */
	ButtonGroupNoSelection castButtonGroup = new ButtonGroupNoSelection();
	
	castUpdate = new JCheckBox();
	updateSettingsPanel.add(castUpdate, "0, 12, CENTER, CENTER");
	castButtonGroup.add(castUpdate);

	castUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(castUpdateIfEmpty, "2, 12, CENTER, CENTER");
	castButtonGroup.add(castUpdateIfEmpty);

	JLabel castLabel = new JLabel("Cast");
	updateSettingsPanel.add(castLabel, "4, 12");
	
	
	/* aka */
	ButtonGroupNoSelection akaButtonGroup = new ButtonGroupNoSelection();
	
	akaUpdate = new JCheckBox();
	updateSettingsPanel.add(akaUpdate, "0, 13, CENTER, CENTER");
	akaButtonGroup.add(akaUpdate);

	akaUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(akaUpdateIfEmpty, "2, 13, CENTER, CENTER");
	akaButtonGroup.add(akaUpdateIfEmpty);

	JLabel akaLabel = new JLabel("Also Know As");
	updateSettingsPanel.add(akaLabel, "4, 13");
	
	
	/* soundMix */
	ButtonGroupNoSelection soundMixButtonGroup = new ButtonGroupNoSelection();
	
	soundMixUpdate = new JCheckBox();
	updateSettingsPanel.add(soundMixUpdate, "0, 14, CENTER, CENTER");
	soundMixButtonGroup.add(soundMixUpdate);

	soundMixUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(soundMixUpdateIfEmpty, "2, 14, CENTER, CENTER");
	soundMixButtonGroup.add(soundMixUpdateIfEmpty);

	JLabel soundMixLabel = new JLabel("Sound Mix");
	updateSettingsPanel.add(soundMixLabel, "4, 14");
	
	
	/* runtime */
	ButtonGroupNoSelection runtimeButtonGroup = new ButtonGroupNoSelection();
	
	runtimeUpdate = new JCheckBox();
	updateSettingsPanel.add(runtimeUpdate, "0, 15, CENTER, CENTER");
	runtimeButtonGroup.add(runtimeUpdate);

	runtimeUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(runtimeUpdateIfEmpty, "2, 15, CENTER, CENTER");
	runtimeButtonGroup.add(runtimeUpdateIfEmpty);

	JLabel runtimeLabel = new JLabel("Runtime");
	updateSettingsPanel.add(runtimeLabel, "4, 15");
	
	
	/* awards */
	ButtonGroupNoSelection awardsButtonGroup = new ButtonGroupNoSelection();
	
	awardsUpdate = new JCheckBox();
	updateSettingsPanel.add(awardsUpdate, "0, 16, CENTER, CENTER");
	awardsButtonGroup.add(awardsUpdate);

	awardsUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(awardsUpdateIfEmpty, "2, 16, CENTER, CENTER");
	awardsButtonGroup.add(awardsUpdateIfEmpty);

	JLabel awardsLabel = new JLabel("Awards");
	updateSettingsPanel.add(awardsLabel, "4, 16");
	
	

	/* mpaa */
	ButtonGroupNoSelection mpaaButtonGroup = new ButtonGroupNoSelection();
	
	mpaaUpdate = new JCheckBox();
	updateSettingsPanel.add(mpaaUpdate, "0, 17, CENTER, CENTER");
	mpaaButtonGroup.add(mpaaUpdate);

	mpaaUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(mpaaUpdateIfEmpty, "2, 17, CENTER, CENTER");
	mpaaButtonGroup.add(mpaaUpdateIfEmpty);

	JLabel mpaaLabel = new JLabel("MPAA");
	updateSettingsPanel.add(mpaaLabel, "4, 17");
	
	
	/* certification */
	ButtonGroupNoSelection certificationButtonGroup = new ButtonGroupNoSelection();
	
	certificationUpdate = new JCheckBox();
	updateSettingsPanel.add(certificationUpdate, "0, 18, CENTER, CENTER");
	certificationButtonGroup.add(certificationUpdate);

	certificationUpdateIfEmpty = new JCheckBox();
	updateSettingsPanel.add(certificationUpdateIfEmpty, "2, 18, CENTER, CENTER");
	certificationButtonGroup.add(certificationUpdateIfEmpty);

	JLabel certificationLabel = new JLabel("Certification");
	updateSettingsPanel.add(certificationLabel, "4, 18");

	
	/* mark all */
	ButtonGroupNoSelection markAllButtonGroup = new ButtonGroupNoSelection();
	
	markAll = new JCheckBox();
	updateSettingsPanel.add(markAll, "0, 20, CENTER, CENTER");
	markAllButtonGroup.add(markAll);
	markAll.addItemListener(this);
	
	markAllIfEmpty = new JCheckBox();
	updateSettingsPanel.add(markAllIfEmpty, "2, 20, CENTER, CENTER");
	markAllButtonGroup.add(markAllIfEmpty);
	markAllIfEmpty.addItemListener(this);
	
	JLabel markAllLabel = new JLabel("(de)select All");
	updateSettingsPanel.add(markAllLabel, "4, 20");
	
	
	
	JPanel updatePanel = new JPanel(new BorderLayout());
	
	updatePanel.add(panel, BorderLayout.PAGE_START);
        updatePanel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
	
	add(updatePanel, BorderLayout.WEST);
	add(updateSettingsPanel, BorderLayout.EAST);
	
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	
	/*Create a timer*/
        timer = new Timer(milliseconds, new TimerListener());
    }
    
    
    class TimerListener implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
	    
	    if (transferred == null) {
		transferred = imdbInfoUpdater.getTransferred();
	    }
	    
	    if (lengthOfTask == 0) {
		lengthOfTask = imdbInfoUpdater.getLengthOfTask();
		
		if (lengthOfTask != 0) {
		    progressBar.setMinimum(0);
		    progressBar.setMaximum(lengthOfTask);
		}
	    }
	    
	    while (transferred != null && counter < lengthOfTask && transferred.size() > 0) {
		
		movieCounter++;
		int percent = ((counter+1) * 100)/lengthOfTask;
		
		String msg = percent+ "%  (" + (counter+1) + " out of " + lengthOfTask+")     ";
		progressBar.setValue(counter+1);
		progressBar.setString(msg);
		taskOutput.append(movieCounter + " - " + ((String) transferred.remove(0)) + newline);
		taskOutput.setCaretPosition(taskOutput.getDocument().getLength());
		counter++;
	    }
	    
	    if (imdbInfoUpdater.isDone() || canceled) {
		timer.stop();
		
		if (!canceled) {
		    
		    taskOutput.append(newline + "Update process finished" + newline);
 
		    taskOutput.append(movieCounter + " entries processed in " + (millisecondsToString(System.currentTimeMillis() - conversionStart)) + newline);
		    closeButton.setEnabled(true);
		    cancelButton.setEnabled(false);
		    parent.setDone(true);
		}
		else {
		    taskOutput.append(newline + "Import aborted!" + newline);
		    parent.setCanceled(true);
		    
		    imdbInfoUpdater = new IMDbInfoUpdater();
		    timer = new Timer(milliseconds, new TimerListener());
		    
		    counter = 0;
		    movieCounter = 0;
		    transferred = null;
		}
	    }
	    }
	}
    
    
    public void itemStateChanged(ItemEvent e) {
	
	if (e.getSource().equals(markAll)) {
	    
	    boolean value = markAll.isSelected();
	    
	    titleUpdate.setSelected(value);
	    coverUpdate.setSelected(value);
	    dateUpdate.setSelected(value);
	    colourUpdate.setSelected(value);
	    directedByUpdate.setSelected(value);
	    writtenByUpdate.setSelected(value);
	    genreUpdate.setSelected(value);
	    ratingUpdate.setSelected(value);
	    countryUpdate.setSelected(value);
	    languageUpdate.setSelected(value);
	    plotUpdate.setSelected(value);
	    castUpdate.setSelected(value);
	    akaUpdate.setSelected(value);
	    soundMixUpdate.setSelected(value);
	    runtimeUpdate.setSelected(value);
	    awardsUpdate.setSelected(value);
	    mpaaUpdate.setSelected(value);
	    certificationUpdate.setSelected(value);
	}
	
	if (e.getSource().equals(markAllIfEmpty)) {
	    
	    boolean value = markAllIfEmpty.isSelected();
	    
	    titleUpdateIfEmpty.setSelected(value);
	    coverUpdateIfEmpty.setSelected(value);
	    dateUpdateIfEmpty.setSelected(value);
	    colourUpdateIfEmpty.setSelected(value);
	    directedByUpdateIfEmpty.setSelected(value);
	    writtenByUpdateIfEmpty.setSelected(value);
	    genreUpdateIfEmpty.setSelected(value);
	    ratingUpdateIfEmpty.setSelected(value);
	    countryUpdateIfEmpty.setSelected(value);
	    languageUpdateIfEmpty.setSelected(value);
	    plotUpdateIfEmpty.setSelected(value);
	    castUpdateIfEmpty.setSelected(value);
	    akaUpdateIfEmpty.setSelected(value);
	    soundMixUpdateIfEmpty.setSelected(value);
	    runtimeUpdateIfEmpty.setSelected(value);
	    awardsUpdateIfEmpty.setSelected(value);
	    mpaaUpdateIfEmpty.setSelected(value);
	    certificationUpdateIfEmpty.setSelected(value);
	}
    }
    
    
   

  
    
    /**
     * Called when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
	
	log.debug("ActionPerformed: "+ evt.getActionCommand());
	
	if (evt.getActionCommand().equals("Start")) {
	    
	    boolean anySelectedButton = false;
	    
	    /*If the conversion was canceled it removes the listed movies to start fresh*/
	    if (!taskOutput.getText().equals(""))
		taskOutput.setText("");
	    
	    startButton.setEnabled(false);
	    cancelButton.setEnabled(true);
	    closeButton.setEnabled(false);
	    
	    canceled = false;
	    parent.setCanceled(false);
	    parent.setDone(false);
	    
	    
	    if (titleUpdate.isSelected()) {
		imdbInfoUpdater.title = 1;
		anySelectedButton = true;
	    }
	    else if (titleUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.title = 2;
		anySelectedButton = true;
	    }
	    
	    
	    if (coverUpdate.isSelected()) {
		imdbInfoUpdater.cover = 1;
		anySelectedButton = true;
	    }
	    else if (coverUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.cover = 2;
		anySelectedButton = true;
	    }

	    if (dateUpdate.isSelected()) {
		imdbInfoUpdater.date = 1;
		anySelectedButton = true;
	    }
	    else if (dateUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.date = 2;
		anySelectedButton = true;
	    }
	    
	    if (colourUpdate.isSelected()) {
		imdbInfoUpdater.colour = 1;
		anySelectedButton = true;
	    }
	    else if (colourUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.colour = 2;
		anySelectedButton = true;
	    }
	    if (directedByUpdate.isSelected()) {
		imdbInfoUpdater.directedBy = 1;
		anySelectedButton = true;
	    }
	    else if (directedByUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.directedBy = 2;
		anySelectedButton = true;
	    }
	    
	    if (writtenByUpdate.isSelected()) {
		imdbInfoUpdater.writtenBy = 1;
		anySelectedButton = true;
	    }
	    else if (writtenByUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.writtenBy = 2;
		anySelectedButton = true;
	    }
	    
	    if (genreUpdate.isSelected()) {
		imdbInfoUpdater.genre = 1;
		anySelectedButton = true;
	    }
	    else if (genreUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.genre = 2;
		anySelectedButton = true;
	    }
	    
	    if (ratingUpdate.isSelected()) {
		imdbInfoUpdater.rating = 1;
		anySelectedButton = true;
	    }
	    else if (ratingUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.rating = 2;
		anySelectedButton = true;
	    }
	    
	    if (countryUpdate.isSelected()) {
		imdbInfoUpdater.country = 1;
		anySelectedButton = true;
	    }
	    else if (countryUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.country = 2;
		anySelectedButton = true;
	    }
	   
	    if (languageUpdate.isSelected()) {
		imdbInfoUpdater.language = 1;
		anySelectedButton = true;
	    }
	    else if (languageUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.language = 2;
		anySelectedButton = true;
	    }
	   
	    if (plotUpdate.isSelected()) {
		imdbInfoUpdater.plot = 1;
		anySelectedButton = true;
	    }
	    else if (plotUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.plot = 2;
		anySelectedButton = true;
	    }
	    
	    if (castUpdate.isSelected()) {
		imdbInfoUpdater.cast = 1;
		anySelectedButton = true;
	    }
	    else if (castUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.cast = 2;
		anySelectedButton = true;
	    }
	    
	    if (akaUpdate.isSelected()) {
		imdbInfoUpdater.aka = 1;
		anySelectedButton = true;
	    }
	    else if (akaUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.aka = 2;
		anySelectedButton = true;
	    }
	    
	    if (soundMixUpdate.isSelected()) {
		imdbInfoUpdater.soundMix = 1;
		anySelectedButton = true;
	    }
	    else if (soundMixUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.soundMix = 2;
		anySelectedButton = true;
	    }
	    
	    if (runtimeUpdate.isSelected()) {
		imdbInfoUpdater.runtime = 1;
		anySelectedButton = true;
	    }
	    else if (runtimeUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.runtime = 2;
		anySelectedButton = true;
	    }
	    
	    if (awardsUpdate.isSelected()) {
		imdbInfoUpdater.awards = 1;
		anySelectedButton = true;
	    }
	    else if (awardsUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.awards = 2;
		anySelectedButton = true;
	    }
	    
	    if (mpaaUpdate.isSelected()) {
		imdbInfoUpdater.mpaa = 1;
		anySelectedButton = true;
	    }
	    else if (mpaaUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.mpaa = 2;
		anySelectedButton = true;
	    }
	    
	    if (certificationUpdate.isSelected()) {
		imdbInfoUpdater.certification = 1;
		anySelectedButton = true;
	    }
	    else if (certificationUpdateIfEmpty.isSelected()) {
		imdbInfoUpdater.certification = 2;
		anySelectedButton = true;
	    }
	    
	    
	    if (anySelectedButton) {
		imdbInfoUpdater.go();
	       
		timer.start();
		conversionStart = System.currentTimeMillis();
		taskOutput.append("Processing import list...\n");
	    }
	    else {
		DialogAlert alert = new DialogAlert(dialog, "Alert", "At least one of the fields should be checked.");
		//alert.setVisible(true);
		 ShowGUI.showAndWait(alert, true);

		 startButton.setEnabled(true);
		 cancelButton.setEnabled(false);
		 closeButton.setEnabled(true);
		 
		 imdbInfoUpdater = new IMDbInfoUpdater();
	    }
	}
	
	
	if (evt.getActionCommand().equals("Cancel")) {
	    
	    parent.setCanceled(true);
	    canceled = true;
	    cancelButton.setEnabled(false);
	    startButton.setEnabled(true);
	    closeButton.setEnabled(true);
	    imdbInfoUpdater.stop();
	}
	
	if (evt.getActionCommand().equals("Close")) {
	    parent.dispose();
	    MovieManagerCommandSelect.execute();
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


