/**
 * @(#)DialogMovieInfo.java 1.0 26.09.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMovies;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.extentions.CoverTransferHandler;
import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.extentions.SteppedComboBox;
import net.sf.xmm.moviemanager.fileproperties.FilePropertiesMovie;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.ShowGUI;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import net.sf.xmm.moviemanager.extentions.ExtendedTreeCellRenderer;

import org.apache.log4j.Logger;

public class DialogMovieInfo extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    private boolean _edit = false;
    
    public boolean _seen = false;
    
    private int _index = -1;
    
    private String _cover = ""; //$NON-NLS-1$
    
    private byte[] _coverData;
    
    private boolean _saveCover = false;
    
    private String _IMDB = ""; //$NON-NLS-1$
    
    private int episodeNumber = -1;
    
    private boolean _hasReadProperties = false;
    
    private int fontSize = 12;

    /*Used when multiadding movies, if adding fileinfo
      to existing movie*/
    private File multiAddFile; 
    
    private int valueComboBoxWidth = -1;
    
    private int valueComboBoxHeight = -1;
    
    /* Additional Info Stuff... */
    private String mediaType = ""; //$NON-NLS-1$
    
    private boolean isEpisode = false;
    
    private int _lastFieldIndex = -1;
    
    private List _saveLastFieldValue = new ArrayList();
    
    private List _fieldNames = new ArrayList();
    
    public List _fieldValues = new ArrayList();
    
    private List _fieldUnits = new ArrayList();
    
    private List _fieldDocuments = new ArrayList();
    
    private int EXTRA_START = 17;
    
    private ModelEntry model;
    
    public static final DataFlavor[] flavors = {DataFlavor.javaFileListFlavor};
    
    /**
     * The Constructor - Add Movie.
     **/
    public DialogMovieInfo(String dialogTitle) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
	setUp(dialogTitle);
	loadEmptyAdditionalFields();
    }

    /**
     * Add Episode.
     **/
    public DialogMovieInfo(ModelMovie model, String dialogTitle) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
	
	isEpisode = true;
	setUp(dialogTitle);
	_index = model.getKey();
	
	loadEmptyAdditionalFields();
	
	/* Sets the General Info Title */
	getMovieTitle().setText(model.getTitle());
	getMovieTitle().setCaretPosition(0);
    }

    /**
     * Edit Movie/Episode
     * This constructor initializes the fields with the info of the movie with
     * index 'index' in the database.
     **/
    public DialogMovieInfo(String dialogTitle, ModelEntry model) {
	/* Dialog creation...*/
	super(MovieManager.getIt());
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
	
	this.model = model;
	
	if (model instanceof ModelEpisode)
	    this.isEpisode = true;
	
	_edit = true;
	
	/* Saves the index... */
	_index = model.getKey();
	
	if (isEpisode)
	    dialogTitle = Localizer.getString("DialogMovieInfo.title.edit-episode"); //$NON-NLS-1$
	
	setUp(dialogTitle);
	
	/* Loads the movie info... */
	loadMovieInfo();
    }
    
    
    /**
     * Sets up the dialog...
     **/
    private void setUp(final String dialogTitle) {
	
	/* Dialog properties...*/
	setTitle(dialogTitle);
	setModal(true);
	setResizable(true);
	
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
	getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
	
	fontSize = MovieManager.getIt().getFontSize();
	JPanel panelMovieInfo = new JPanel();
    
	panelMovieInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,7,0,7), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.getString("DialogMovieInfo.panel-movie-info.title."), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panelMovieInfo.getFont().getName(),Font.BOLD, fontSize))), BorderFactory.createEmptyBorder(0,5,0,5))); //$NON-NLS-1$
    
	panelMovieInfo.setLayout(new GridBagLayout());
	/* Creates the general info... */
	JPanel panelGeneralInfo = new JPanel();
	panelGeneralInfo.setLayout(new GridBagLayout());
	GridBagConstraints constraints;
	
	JLabel dateID = new JLabel(Localizer.getString("DialogMovieInfo.field.date") + ": "); //$NON-NLS-1$
	dateID.setFont((new Font(dateID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(dateID,constraints);
	
	//YYYYMMDD, or YYYY/MM/DD or even YYYY-MM-DD, YYYY.MM.DD

	JTextField date = new JTextField(7);
	
	//date.setDocument(new DocumentRegExp("(\\d)*",4));
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;  
	panelGeneralInfo.add(date,constraints);
    
	JLabel colourID = new JLabel(Localizer.getString("DialogMovieInfo.field.colour") + ": "); //$NON-NLS-1$
	colourID.setFont((new Font(colourID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(colourID,constraints);
    
	JTextField colour = new JTextField(15);
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 0;
	constraints.gridwidth = 3;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(colour,constraints);
    
	JLabel movieTitleID = new JLabel(""); //$NON-NLS-1$
	movieTitleID.setFont((new Font(movieTitleID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(movieTitleID,constraints);
	final JTextField movieTitle = new JTextField(28);
    
	/*This makes sure the focus will be on the movieTitle by default*/
	addWindowListener(new WindowAdapter() {
		public void windowOpened( WindowEvent e ){
		    movieTitle.requestFocus();
		}
	    });
	
	movieTitle.addKeyListener(new KeyAdapter() {
		
		public void keyPressed(KeyEvent e) {
		    
		    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			
			if (getMovieTitle().getText().equals("")) { //$NON-NLS-1$
			    File [] file = executeGetFile();
			    if (file != null)
				executeCommandGetFileInfo(file);
			}
			else {
			    if (isEpisode)
				executeCommandGetTVDOTCOMInfo();
			    else
				executeCommandGetIMDBInfo();
			}
		    }
		}});
	
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.gridwidth = 6;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(movieTitle,constraints);
    
	JLabel directedID = new JLabel(Localizer.getString("DialogMovieInfo.field.directed-by") + ": "); //$NON-NLS-1$
	directedID.setFont((new Font(directedID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(directedID,constraints);
    
	JTextField directed = new JTextField(28);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 6;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(directed,constraints);
    
	JLabel writtenID = new JLabel(Localizer.getString("DialogMovieInfo.field.written-by") + ": "); //$NON-NLS-1$
	writtenID.setFont((new Font(writtenID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(writtenID,constraints);
	JTextField written = new JTextField(28);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.gridwidth = 6;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(written,constraints);
    
    
	JLabel genreID = new JLabel(Localizer.getString("DialogMovieInfo.field.genre") + ": "); //$NON-NLS-1$
	genreID.setFont((new Font(genreID.getFont().getName(), 1, fontSize)));
    
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(genreID,constraints);
    
	JTextField genre = new JTextField(28);
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 4;
	constraints.gridwidth = 6;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(genre,constraints);
    
	JLabel ratingID = new JLabel(Localizer.getString("DialogMovieInfo.field.rating") + ": "); //$NON-NLS-1$
	ratingID.setFont((new Font(ratingID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(ratingID,constraints);
    
	JTextField rating = new JTextField(3);
	rating.setDocument(new DocumentRegExp("(\\d)*(\\.)?(\\d)*",4)); //$NON-NLS-1$
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(rating,constraints);
    
    
	JLabel countryID = new JLabel(Localizer.getString("DialogMovieInfo.field.country") + ": "); //$NON-NLS-1$
	countryID.setFont((new Font(countryID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(countryID,constraints);
    
	JTextField country = new JTextField(15);
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 5;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(country,constraints);
    
   
    
	JLabel seenID = new JLabel(Localizer.getString("DialogMovieInfo.field.seen") + ": "); //$NON-NLS-1$
	seenID.setFont((new Font(seenID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(seenID,constraints);
	JLabel seen = new JLabel(new ImageIcon(MovieManager.getIt().getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	seen.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent event) {
		    log.debug("actionPerformed: MovieInfo - Seen"); //$NON-NLS-1$
		    executeCommandSeen();
		}});
    
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 6;
	constraints.insets = new Insets(1,5,1,50);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(seen,constraints);
    
	JLabel languageID = new JLabel(Localizer.getString("DialogMovieInfo.field.language") + ": "); //$NON-NLS-1$
	languageID.setFont((new Font(languageID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 6;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(languageID,constraints);
    
	JTextField language = new JTextField(15);
	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 6;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelGeneralInfo.add(language,constraints);
    
	
	JLabel cover = new JLabel(new ImageIcon(MovieManager.getIt().getImage("/images/" + MovieManager.getConfig().getNoCover()).getScaledInstance(97,97,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	cover.setBorder(BorderFactory.createEtchedBorder());
	cover.setPreferredSize(new Dimension(97, 145));
	cover.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent event) {
		    log.debug("actionPerformed: MovieInfo - Cover"); //$NON-NLS-1$
		    executeCommandCover();
		}});
    cover.setTransferHandler(new CoverTransferHandler(this));
	constraints = new GridBagConstraints();
	constraints.gridx = 7;
	constraints.gridy = 0;
	constraints.gridheight = 7;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.CENTER;
	panelGeneralInfo.add(cover,constraints);
    
	constraints = new GridBagConstraints();    
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(0,-1,5,-1);
	constraints.anchor = GridBagConstraints.CENTER;
	panelMovieInfo.add(panelGeneralInfo,constraints);
	
	
	/* Creates the plot... */
	JPanel panelPlot = new JPanel();
	panelPlot.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												Localizer.getString("DialogMovieInfo.panel-plot.title."), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(panelPlot.getFont().getName(),Font.PLAIN,fontSize)),
							       BorderFactory.createEmptyBorder(0,5,5,5)));
	JTextArea textAreaPlot = new JTextArea("",4,43); //$NON-NLS-1$
	textAreaPlot.setLineWrap(true);
	textAreaPlot.setWrapStyleWord(true);
	JScrollPane scrollPanePlot = new JScrollPane(textAreaPlot);
	scrollPanePlot.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	panelPlot.add(scrollPanePlot);
	


	/* Creates the cast... */
	JPanel panelCast = new JPanel();
	panelCast.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												Localizer.getString("DialogMovieInfo.panel-cast.title."), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(panelCast.getFont().getName(),Font.PLAIN,fontSize)),
							       BorderFactory.createEmptyBorder(0,5,5,5)));
    
	JTextArea textAreaCast = new JTextArea("", 4, 43); //$NON-NLS-1$
	textAreaCast.setLineWrap(true);
	textAreaCast.setWrapStyleWord(true);
	JScrollPane scrollPaneCast = new JScrollPane(textAreaCast);
	scrollPaneCast.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	panelCast.add(scrollPaneCast);
	
	
	
	JPanel panelPlotAndCast = new JPanel();
	panelPlotAndCast.setLayout(new BoxLayout(panelPlotAndCast, BoxLayout.Y_AXIS));
	panelPlotAndCast.setBorder(BorderFactory.createEmptyBorder(5,3,2,3));
	panelPlotAndCast.add(panelPlot);
	panelPlotAndCast.add(panelCast);
	
	
	/* Miscellaneous */
	
	JPanel panelMisc = new JPanel();
	panelMisc.setLayout(new GridBagLayout());
    panelMisc.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
    
    JLabel webRuntimeID = new JLabel(Localizer.getString("DialogMovieInfo.field.web-runtime") + ": "); //$NON-NLS-1$
	webRuntimeID.setFont((new Font(webRuntimeID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	//constraints.insets = new Insets(1,5,1,5);
	//constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	panelMisc.add(webRuntimeID, constraints);
	
	JTextField webRuntime = new JTextField();
	webRuntime.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	webRuntime.setMinimumSize(webRuntime.getPreferredSize());
	
	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.gridwidth = 3;
    constraints.weightx = 1.0;
	constraints.insets = new Insets(1,0,1,0);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.EAST;
	panelMisc.add(webRuntime, constraints);
	
	JLabel soundMixID = new JLabel(Localizer.getString("DialogMovieInfo.field.sound-mix") + ": "); //$NON-NLS-1$
	soundMixID.setFont((new Font(soundMixID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	//constraints.insets = new Insets(1,5,1,5);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	panelMisc.add(soundMixID, constraints);
	
	JTextField soundMix = new JTextField();
	soundMix.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	soundMix.setMinimumSize(soundMix.getPreferredSize());

	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.gridwidth = 3;
    constraints.weightx = 1.0;
	constraints.insets = new Insets(1,0,1,0);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.EAST;
	panelMisc.add(soundMix, constraints);
	
	
	JLabel awardsID = new JLabel(Localizer.getString("DialogMovieInfo.field.awards") + ": "); //$NON-NLS-1$
	awardsID.setFont((new Font(awardsID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	//constraints.insets = new Insets(1,5,1,5);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	panelMisc.add(awardsID, constraints);
	
	JTextField awards = new JTextField();
	awards.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	awards.setMinimumSize(awards.getPreferredSize());

	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 3;
    constraints.weightx = 1.0;
	constraints.insets = new Insets(1,0,1,0);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.EAST;
	panelMisc.add(awards, constraints);
	
	
	JLabel mpaaID = new JLabel(Localizer.getString("DialogMovieInfo.field.MPAA") + ": "); //$NON-NLS-1$
	mpaaID.setFont((new Font(mpaaID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	//constraints.insets = new Insets(1,5,1,5);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	panelMisc.add(mpaaID, constraints);
	
	JTextField mpaa = new JTextField();
	mpaa.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	mpaa.setMinimumSize(mpaa.getPreferredSize());

	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.gridwidth = 3;
    constraints.weightx = 1.0;
	constraints.insets = new Insets(1,0,1,0);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.EAST;
	panelMisc.add(mpaa, constraints);
	
	
	JLabel akaID = new JLabel(Localizer.getString("DialogMovieInfo.field.also-known-as") + ": "); //$NON-NLS-1$
	akaID.setFont((new Font(akaID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(0,0,0,4);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	panelMisc.add(akaID, constraints);
	
	JTextArea textAreaAka = new JTextArea("", 4, 10);
	//textAreaAka.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	textAreaAka.setLineWrap(true);
	textAreaAka.setWrapStyleWord(true);
	JScrollPane scrollPaneAka = new JScrollPane(textAreaAka);
	scrollPaneAka.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
    scrollPaneAka.setMinimumSize(scrollPaneAka.getPreferredSize());
    
	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 4;
	constraints.gridwidth = 3;
    constraints.gridheight = 4;
    constraints.weightx = 1.0;
	constraints.insets = new Insets(1,0,1,0);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.EAST;
	panelMisc.add(scrollPaneAka, constraints);
	
	
	JLabel certificationID = new JLabel(Localizer.getString("DialogMovieInfo.field.certification") + ": "); //$NON-NLS-1$
	certificationID.setFont((new Font(certificationID.getFont().getName(), 1, fontSize)));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.gridwidth = 1;
	//constraints.insets = new Insets(1,5,1,5);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	panelMisc.add(certificationID, constraints);
	
	JTextArea textAreaCertification = new JTextArea("", 4, 30);
	//textAreaCertification.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	textAreaCertification.setLineWrap(true);
	textAreaCertification.setWrapStyleWord(true);

	JScrollPane scrollPaneCertification = new JScrollPane(textAreaCertification);
	scrollPaneCertification.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPaneCertification.setMinimumSize(scrollPaneCertification.getPreferredSize());
    
	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 8;
	constraints.gridwidth = 3;
    constraints.gridheight = 3;
    constraints.weightx = 1.0;
	constraints.insets = new Insets(1,0,1,0);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.EAST;
	panelMisc.add(scrollPaneCertification, constraints);
	
	
	JTabbedPane allTabbedInfo = new JTabbedPane();
	allTabbedInfo.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	allTabbedInfo.add(Localizer.getString("DialogMovieInfo.tab.plot-and-cast"), panelPlotAndCast); //$NON-NLS-1$
	allTabbedInfo.add(Localizer.getString("DialogMovieInfo.tab.miscellaneous"), panelMisc); //$NON-NLS-1$
	
	constraints = new GridBagConstraints();    
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(0,-1,5,-1);
	constraints.anchor = GridBagConstraints.CENTER;
	panelMovieInfo.add(allTabbedInfo, constraints);
	
	/* Creates the Additional Info... */
	JPanel panelAdditionalInfo = new JPanel();
	
	panelAdditionalInfo.setTransferHandler(new FileTransferHandler());
	
	panelAdditionalInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
													  Localizer.getString("DialogMovieInfo.panel-additional-info.title"), //$NON-NLS-1$
													  TitledBorder.DEFAULT_JUSTIFICATION,
													  TitledBorder.DEFAULT_POSITION,
													  new Font(panelAdditionalInfo.getFont().getName(),Font.PLAIN,fontSize)),
									 BorderFactory.createEmptyBorder(0,5,5,5)));
	panelAdditionalInfo.setLayout(new GridBagLayout());
	
	JLabel fieldsID = new JLabel(Localizer.getString("DialogMovieInfo.panel-additional-info.field") + ": "); //$NON-NLS-1$
	fieldsID.setFont(new Font(fieldsID.getFont().getName(),Font.PLAIN,fontSize));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.insets = new Insets(1,0,7,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelAdditionalInfo.add(fieldsID,constraints);
	
	JComboBox fields = new JComboBox(new String[]{"",""}); //$NON-NLS-1$ //$NON-NLS-2$
	
	fields.setFont(new Font(fields.getFont().getName(), Font.PLAIN, fontSize));
	fields.setEditable(false);
	fields.setMaximumRowCount(6);
	fields.setActionCommand("MovieInfo - Additional Info"); //$NON-NLS-1$
	fields.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    executeCommandAdditionalInfo();
		}});
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,7,0);
	constraints.anchor = GridBagConstraints.WEST;
	panelAdditionalInfo.add(fields, constraints);
	
	JLabel valueID = new JLabel(Localizer.getString("DialogMovieInfo.panel-additional-info.value") + ": "); //$NON-NLS-1$
	valueID.setFont(new Font(valueID.getFont().getName(),Font.PLAIN,fontSize));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.insets = new Insets(7,0,1,3);
	constraints.anchor = GridBagConstraints.WEST;
	panelAdditionalInfo.add(valueID,constraints);
	
	JPanel valuePanel = new JPanel();
	valuePanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	valuePanel.setLayout(new FlowLayout(0,0,0));
	
	
	JTextField value = new JTextField(14);
	
	Font font = value.getFont();
	
	font = new Font(font.getName(), font.getStyle(), 11);
	
	value.setFont(font);
	
	valuePanel.add(value);
	
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	//constraints.gridwidth = 2;
	constraints.insets = new Insets(7,5,1,0);
	constraints.anchor = GridBagConstraints.WEST;
	panelAdditionalInfo.add(valuePanel, constraints);
	
	JLabel unit = new JLabel(" units",JLabel.RIGHT); //$NON-NLS-1$
	unit.setFont(new Font(unit.getFont().getName(),Font.PLAIN,fontSize));
	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 1;
	//constraints.gridwidth = 1;
	constraints.insets = new Insets(7,0,1,0);
	constraints.anchor = GridBagConstraints.EAST;
	panelAdditionalInfo.add(unit, constraints);
	
	constraints = new GridBagConstraints();    
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.insets = new Insets(0,-1,5,-1);
	constraints.anchor = GridBagConstraints.WEST;
	panelMovieInfo.add(panelAdditionalInfo, constraints);
	
	/* Creates the notes... */
	JPanel panelNotes = new JPanel();
	panelNotes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												 Localizer.getString("DialogMovieInfo.panel-notes.title"), //$NON-NLS-1$
												 TitledBorder.DEFAULT_JUSTIFICATION,
												 TitledBorder.DEFAULT_POSITION,
												 new Font(panelNotes.getFont().getName(),Font.PLAIN,fontSize)),
								BorderFactory.createEmptyBorder(0,5,5,5)));
	JTextArea textAreaNotes = new JTextArea("",4,20); //$NON-NLS-1$
	textAreaNotes.setLineWrap(true);
	textAreaNotes.setWrapStyleWord(true);
	JScrollPane scrollPaneNotes = new JScrollPane(textAreaNotes);
	scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	panelNotes.add(scrollPaneNotes);
	constraints = new GridBagConstraints();    
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.insets = new Insets(0,-1,5,-1);
	constraints.anchor = GridBagConstraints.EAST;
	panelMovieInfo.add(panelNotes,constraints);
    
	/* Buttons panel...*/
	JPanel panelButtons = new JPanel();
	panelButtons.setBorder(BorderFactory.createEmptyBorder(15,5,5,5));
	panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
	
	JButton buttonSave;
	
	if (isEpisode && !_edit)
	    buttonSave = new JButton(Localizer.getString("DialogMovieInfo.button-save.text.add-episode")); //$NON-NLS-1$
	else
	    buttonSave = new JButton(Localizer.getString("DialogMovieInfo.button-save.text.save")); //$NON-NLS-1$
	
	buttonSave.setToolTipText(Localizer.getString("DialogMovieInfo.button-save.tooltip")); //$NON-NLS-1$
	buttonSave.setActionCommand("MovieInfo - Save"); //$NON-NLS-1$
	buttonSave.setMnemonic(KeyEvent.VK_S);
	buttonSave.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    executeAndReloadMovieList(executeCommandSave(MovieManager.getConfig().getCurrentList()));
		}});
	panelButtons.add(buttonSave);

	JButton buttonGetDVDInfo = new JButton(Localizer.getString("DialogMovieInfo.button-get-DVD-info.text")); //$NON-NLS-1$
	buttonGetDVDInfo.setMnemonic(KeyEvent.VK_D);
	buttonGetDVDInfo.setToolTipText(Localizer.getString("DialogMovieInfo.button-get-DVD-info.tooltip")); //$NON-NLS-1$
	buttonGetDVDInfo.setActionCommand("MovieInfo - GetDVDInfo"); //$NON-NLS-1$
	buttonGetDVDInfo.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    executeGetDVDInfo();
		}});
	panelButtons.add(buttonGetDVDInfo);
    
	JButton buttonGetFileInfo = new JButton(Localizer.getString("DialogMovieInfo.button-get-file-info.text")); //$NON-NLS-1$
	buttonGetFileInfo.setMnemonic(KeyEvent.VK_F);
	buttonGetFileInfo.setToolTipText(Localizer.getString("DialogMovieInfo.button-get-file-info.tooltip")); //$NON-NLS-1$
	buttonGetFileInfo.setActionCommand("MovieInfo - GetFileInfo"); //$NON-NLS-1$
	buttonGetFileInfo.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    File [] file = executeGetFile();
		    if (file != null)
			executeCommandGetFileInfo(file);
		}});
	
	panelButtons.add(buttonGetFileInfo);
	
	JButton buttonGetIMDBInfo;
	
	if (isEpisode)
	    buttonGetIMDBInfo = new JButton(Localizer.getString("DialogMovieInfo.button-get-web-info.text.get-tvdotcom-info")); //$NON-NLS-1$
	else
	    buttonGetIMDBInfo = new JButton(Localizer.getString("DialogMovieInfo.button-get-web-info.text.get-imdb-info")); //$NON-NLS-1$
	
	buttonGetIMDBInfo.setMnemonic(KeyEvent.VK_M);
	buttonGetIMDBInfo.setToolTipText(Localizer.getString("DialogMovieInfo.button-get-web-info.tooltip.get-imdb-info")); //$NON-NLS-1$
	buttonGetIMDBInfo.setActionCommand("MovieInfo - GetIMDBInfo"); //$NON-NLS-1$
	buttonGetIMDBInfo.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    if (isEpisode)
			executeCommandGetTVDOTCOMInfo();
		    else
			executeCommandGetIMDBInfo();
		}});
	panelButtons.add(buttonGetIMDBInfo);
	JButton buttonCancel = new JButton(Localizer.getString("DialogMovieInfo.button-cancel.text")); //$NON-NLS-1$
	buttonCancel.setMnemonic(KeyEvent.VK_C);
	buttonCancel.setToolTipText(Localizer.getString("DialogMovieInfo.button-cancel.tooltip")); //$NON-NLS-1$
	buttonCancel.setActionCommand("MovieInfo - Cancel"); //$NON-NLS-1$
	buttonCancel.addActionListener(new CommandDialogDispose(this));
	panelButtons.add(buttonCancel);
    
	/* Adds all and buttonsPanel... */    
	getContentPane().add(panelMovieInfo,BorderLayout.NORTH);
	getContentPane().add(panelButtons,BorderLayout.SOUTH);
    
	fields.setPreferredSize(new Dimension((int) fields.getPreferredSize().getWidth()+60, (int) fields.getPreferredSize().getHeight()+15));
	/* Packs and sets location... */
	pack();
	
	
	webRuntime.setPreferredSize(new Dimension(panelMovieInfo.getWidth()- 150, 22));
 	soundMix.setPreferredSize(new Dimension(panelMovieInfo.getWidth()- 150, 22));
 	awards.setPreferredSize(new Dimension(panelMovieInfo.getWidth()- 150, 22));
	mpaa.setPreferredSize(new Dimension(panelMovieInfo.getWidth()- 150, 22));
	scrollPaneAka.setPreferredSize(new Dimension(panelMovieInfo.getWidth()- 150, 64));
	scrollPaneCertification.setPreferredSize(new Dimension(panelMovieInfo.getWidth()- 150, 52));
	
	allTabbedInfo.setPreferredSize(new Dimension(panelGeneralInfo.getWidth(),((int) allTabbedInfo.getPreferredSize().getHeight())));
	
	
	scrollPanePlot.setPreferredSize(new Dimension(panelMovieInfo.getWidth()-76,72));
	scrollPaneCast.setPreferredSize(new Dimension(panelMovieInfo.getWidth()-76,72));
	
	panelAdditionalInfo.setPreferredSize(new Dimension(panelMovieInfo.getWidth()/2-20, (int) panelAdditionalInfo.getPreferredSize().getHeight()+10));
	
	scrollPaneNotes.setPreferredSize(new Dimension((panelMovieInfo.getWidth()-114)/2,76));
	panelNotes.setPreferredSize(new Dimension((int) panelNotes.getPreferredSize().getWidth(), (int) panelAdditionalInfo.getPreferredSize().getHeight()+1));

	value.setPreferredSize(value.getSize());
	unit.setPreferredSize(unit.getSize());
	
	/* Setting preferred size of the additional info dropdown menu */
	
	fields.setPreferredSize(new Dimension((int) (panelAdditionalInfo.getPreferredSize().getWidth() - fieldsID.getPreferredSize().getWidth())-50, (int) fields.getPreferredSize().getHeight()));
	
	valueComboBoxWidth = (int) fields.getPreferredSize().getWidth()-33;
	valueComboBoxHeight = (int) value.getPreferredSize().getHeight();
	
	int x = (int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2;
	int y = (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2;
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension size = getSize();
	
	if (x + size.width > screenSize.width)
	    x = screenSize.width - size.width;
	
	if (y + size.height > screenSize.height)
	    y = screenSize.height - size.height - 20;
	
	setLocation(x, y);
    }
    
    
    /**
     * Gets the date JTextField...
     **/
    public JTextField getDate() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(1);
    }
  
    /**
     * Gets the colour JTextField...
     **/
    public JTextField getColour() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(3);
    }
    
    /**
     * Gets the movie title JTextField...
     **/
    public JTextField getMovieTitle() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(5);
    }

    /**
     * Gets the directed by JTextField...
     **/
    public JTextField getDirectedBy() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(7);
    }

    /**
     * Gets the written by JTextField...
     **/
    public JTextField getWrittenBy() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(9);
    }

    /**
     * Gets the genre JTextField...
     **/
    public JTextField getGenre() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(11);
    }

    /**
     * Gets the rating JTextField...
     **/
    public JTextField getRating() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(13);
    }
    
    /**
     * Gets the country JTextField...
     **/
    public JTextField getCountry() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(15);
    }
    
    
    /**
     * Gets the seen JLabel...
     **/
    protected JLabel getSeen() {
	return    
	    (JLabel)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(17);
    }
    
    /**
     * Gets the language JTextField...
     **/
    public JTextField getLanguage() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(19);
    }
    
    /**
     * Gets the cover JLabel...
     **/
    public JLabel getCover() {
	return    
	    (JLabel)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(0)).getComponent(20);
    }

    /**
     * Gets the plot JTextArea...
     **/
    public JTextArea getPlot() {
	return    
	    (JTextArea)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       ((JTabbedPane)
		((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
    }

    /**
     * Gets the cast JTextArea...
     **/
    public JTextArea getCast() {
	return    
	    (JTextArea)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       ((JTabbedPane)
		((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(0)).getComponent(1)).getComponent(0)).getViewport().getComponent(0);
    }
    
    
    /**
     * Gets the web-runtime JTextArea...
     **/
    public JTextField getWebRuntime() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JTabbedPane)
	      ((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(1)).getComponent(1);
    }
    
    /**
     * Gets the web-sound mix JTextArea...
     **/
    protected JTextField getWebSoundMix() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JTabbedPane)
	      ((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(1)).getComponent(3);
    }
    
    /**
     * Gets the awards JTextArea...
     **/
    protected JTextField getAwards() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JTabbedPane)
	      ((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(1)).getComponent(5);
    }
    
    /**
     * Gets the MPAA JTextArea...
     **/
    public JTextField getMpaa() {
	return    
	    (JTextField)
	    ((JPanel)
	     ((JTabbedPane)
	      ((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(1)).getComponent(7);
    }
    
    /**
     * Gets the Also known as JTextArea...
     **/
    public JTextArea getAka() {
	return    
	    (JTextArea)
	    ((JScrollPane)
	     ((JPanel)
	      ((JTabbedPane)
	       ((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(1)).getComponent(9)).getViewport().getComponent(0);
    }
    
    /**
     * Gets the Also known as JTextArea...
     **/
    protected JTextArea getCertification() {
	return    
	    (JTextArea)
	    ((JScrollPane)
	     ((JPanel)
	      ((JTabbedPane)
	       ((JPanel)getContentPane().getComponent(0)).getComponent(1)).getComponent(1)).getComponent(11)).getViewport().getComponent(0);
    }
    
    /**
     * Gets the additional info fields JComboBox...
     **/
    protected JPanel getAdditionalInfoPanel() {
	return    
	    (JPanel)
	    ((JPanel)getContentPane().getComponent(0)).getComponent(2);
    }
    
    /**
     * Gets the additional info fields JComboBox...
     **/
    protected JComboBox getAdditionalInfoFields() {
	return    
	    (JComboBox)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(2)).getComponent(1);
    }

    /**
     * Gets the additional info value JPanel...
     **/
    protected JPanel getAdditionalInfoValuePanel() {
	return    
	    (JPanel)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(2)).getComponent(3);
    }

    /**
     * Gets the additional info units JLabel...
     **/
    protected JLabel getAdditionalInfoUnits() {
	return    
	    (JLabel)
	    ((JPanel)
	     ((JPanel)getContentPane().getComponent(0)).getComponent(2)).getComponent(4);
    }

    /**
     * Gets the notes JTextArea...
     **/
    public JTextArea getNotes() {
	return    
	    (JTextArea)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)getContentPane().getComponent(0)).getComponent(3)).getComponent(0)).getViewport().getComponent(0);
    }

    /**
     * Removes the cover.
     **/
    public void removeCover() {
	
	_cover = ""; //$NON-NLS-1$
	_coverData = null;
	
	/* Loads the nocover cover... */
	getCover().setIcon(new ImageIcon(MovieManager.getIt().getImage("/images/" + MovieManager.getConfig().getNoCover()).getScaledInstance(97,97,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	
	setSaveCover(false);
    }

    /**
     * Sets _cover and _coverData (used by MySQL).
     **/
    public void setCover(String cover, byte[] coverData) {
	
	_cover = cover;
	_coverData = coverData;
	
	/* Loads the new cover... */
	getCover().setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(_coverData).getScaledInstance(97,145,Image.SCALE_FAST)));
	setSaveCover(true);
    }
    
    /**
     * Sets _saveCover.
     **/
    public void setSaveCover(boolean saveCover) {
	
	if (!((MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) && !MovieManager.getConfig().getStoreCoversLocally()))
	    _saveCover = saveCover;
	else
	    _saveCover = false;
    }
    
    /**
     * Sets _IMDB.
     **/
    public void setIMDB(String IMDB) {
	_IMDB = IMDB;
    }
    
    /**
     * Sets _IMDB.
     **/
    protected void setEpisodeNumber(int episodeNumber) {
	this.episodeNumber = episodeNumber;
    }
    
    
    /**
     * Loads the info from the model
     **/
    private void loadMovieInfo() {
	
	if (isEpisode) {
	    setEpisodeNumber(((ModelEpisode) model).getEpisodeNumber());
	}	
	
	/* Gets the general info... */
	_IMDB = model.getUrlKey();
	getDate().setText(model.getDate());
	getDate().setCaretPosition(0);
	getMovieTitle().setText(model.getTitle());
	getMovieTitle().setCaretPosition(0);
	getDirectedBy().setText(model.getDirectedBy());
	getDirectedBy().setCaretPosition(0);
	getWrittenBy().setText(model.getWrittenBy());
	getWrittenBy().setCaretPosition(0);
	getGenre().setText(model.getGenre());
	getGenre().setCaretPosition(0);
	getRating().setText(model.getRating());
	getRating().setCaretPosition(0);
	getCountry().setText(model.getCountry());
	getCountry().setCaretPosition(0);
	getLanguage().setText(model.getLanguage());
	getLanguage().setCaretPosition(0);
	getColour().setText(model.getColour());
	getColour().setCaretPosition(0);
	
	getAka().setText(model.getAka());
	getAka().setCaretPosition(0);
	getCertification().setText(model.getCertification());
	getCertification().setCaretPosition(0);
	getWebSoundMix().setText(model.getWebSoundMix());
	getWebSoundMix().setCaretPosition(0);
	getWebRuntime().setText(model.getWebRuntime());
	getWebRuntime().setCaretPosition(0);
	getAwards().setText(model.getAwards());
	getAwards().setCaretPosition(0);
	
	if (model.getSeen()) {
	    getSeen().setIcon(new ImageIcon(MovieManager.getIt().getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	    _seen = true;
	}
	_cover = model.getCover();
	_coverData = model.getCoverData();
	
	try {
	    if (!_cover.equals("")) { //$NON-NLS-1$
		/* Gets the full path for the cover... */
		
		File cover = new File(MovieManager.getConfig().getCoversPath(), _cover);
		
		if (MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) {
		    
		    if (MovieManager.getConfig().getStoreCoversLocally() && cover.exists()) {
			/* Loads the image...*/
			getCover().setIcon(new ImageIcon(MovieManager.getIt().getImage(cover.getAbsolutePath()).getScaledInstance(97,145,Image.SCALE_FAST)));
		    }
		    else {
			byte [] coverData = model.getCoverData();
			
			if (coverData != null) {
			    getCover().setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(coverData).getScaledInstance(97, 145,Image.SCALE_SMOOTH)));
			    _coverData = coverData;
			}
			else {
			    
			    if (isEpisode)
				_coverData = ((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataEpisode(_index);
			    else
				_coverData = ((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataMovie(_index);
			    
			    if (_coverData == null)
				log.warn("Cover data not available."); //$NON-NLS-1$
			}
		    }
		}
		else if ((cover.exists())) {
		    /* Loads the image...*/
		    getCover().setIcon(new ImageIcon(MovieManager.getIt().getImage(cover.getAbsolutePath()).getScaledInstance(97,145,Image.SCALE_FAST)));
		}
		else 
		    log.warn("Cover file not found."); //$NON-NLS-1$
	    }
	} catch (Exception e) {
	    log.error("", e); //$NON-NLS-1$
	}
	/* Gets the plot... */
	getPlot().setText(model.getPlot());
	getPlot().setCaretPosition(0);
	
	/* Gets the cast... */
	getCast().setText(model.getCast());
	getCast().setCaretPosition(0);
	
	/* Gets the notes... */
	getNotes().setText(model.getNotes());
	getNotes().setCaretPosition(0);
	
	/* Gets the additional info... */
	ModelAdditionalInfo additionalInfo;
	
	additionalInfo = MovieManager.getIt().getDatabase().getAdditionalInfo(_index, isEpisode);
    
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Subtitles"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getSubtitles());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Duration"); //$NON-NLS-1$
	int duration = additionalInfo.getDuration();
	if(duration > 0) {
	    _fieldValues.add(String.valueOf(duration));
	} else {
	    _fieldValues.add(""); //$NON-NLS-1$
	}
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*",2)); //$NON-NLS-1$
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("File Size"); //$NON-NLS-1$
	int fileSize = additionalInfo.getFileSize();
	if (fileSize > 0) {
	    _fieldValues.add(String.valueOf(fileSize));
	} else {
	    _fieldValues.add(""); //$NON-NLS-1$
	}
	_fieldUnits.add("MiB"); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*",9)); //$NON-NLS-1$

	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("CDs"); //$NON-NLS-1$
	int cds = additionalInfo.getCDs();
		
	if (cds > 0) {
	    _fieldValues.add(String.valueOf(cds));
	} else {
	    _fieldValues.add(""); //$NON-NLS-1$
	}
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*",9)); //$NON-NLS-1$
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("CD Cases"); //$NON-NLS-1$
	double cdCases = additionalInfo.getCDCases();
	
	if (cdCases > 0) {
	    _fieldValues.add(String.valueOf(cdCases));
	} else {
	    _fieldValues.add(""); //$NON-NLS-1$
	}
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*(\\.)?(\\d)*")); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Resolution"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getResolution());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*(x)?(\\d)*")); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Video Codec"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getVideoCodec());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());

	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Video Rate"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getVideoRate());
	_fieldUnits.add("fps"); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*(\\.)?(\\d)*")); //$NON-NLS-1$

	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Video Bit Rate"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getVideoBitrate());
	_fieldUnits.add("kbps");         //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*")); //$NON-NLS-1$
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Codec"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getAudioCodec());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Rate"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getAudioRate());
	_fieldUnits.add("Hz"); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*")); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Bit Rate"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getAudioBitrate());
	_fieldUnits.add("kbps"); //$NON-NLS-1$
	//_fieldDocuments.add(new DocumentRegExp("(\\d)*"));
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Channels"); //$NON-NLS-1$
	String audioChannels = additionalInfo.getAudioChannels();
	_fieldValues.add(audioChannels);
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Location"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getFileLocation());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("File Count"); //$NON-NLS-1$
	int fileCount = additionalInfo.getFileCount();
	if (fileCount > 0) {
	    _fieldValues.add(String.valueOf(fileCount));
	} else {
	    _fieldValues.add(""); //$NON-NLS-1$
	}
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
			
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Container"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getContainer());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
			
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Media Type"); //$NON-NLS-1$
	_fieldValues.add(additionalInfo.getMediaType());
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_fieldNames.addAll(MovieManager.getIt().getDatabase().getExtraInfoFieldNames());
	
	String name;
	for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
	    _saveLastFieldValue.add(new Boolean(true));
	    
	    if (isEpisode)
		_fieldValues.add(MovieManager.getIt().getDatabase().getExtraInfoEpisodeField(_index,(String)_fieldNames.get(i)));
	    else
		_fieldValues.add(MovieManager.getIt().getDatabase().getExtraInfoMovieField(_index,(String)_fieldNames.get(i)));
	    
	    _fieldUnits.add(""); //$NON-NLS-1$
	    _fieldDocuments.add(new PlainDocument());
	}
	
	/* Adds all the active fields to the Dropdown box */
	DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
	int [] activeAdditionalInfoFields;
	
	activeAdditionalInfoFields = MovieManager.getIt().getActiveAdditionalInfoFields();
	
	for (int i = 0; i < activeAdditionalInfoFields.length; i++) {
	    
	    switch (activeAdditionalInfoFields[i]) {
	    
	    case 0: comboBoxModel.addElement("Subtitles"); break; //$NON-NLS-1$
	    case 1: comboBoxModel.addElement("Duration"); break; //$NON-NLS-1$
	    case 2: comboBoxModel.addElement("File Size"); break; //$NON-NLS-1$
	    case 3: comboBoxModel.addElement("CDs"); break; //$NON-NLS-1$
	    case 4: comboBoxModel.addElement("CD Cases"); break; //$NON-NLS-1$
	    case 5: comboBoxModel.addElement("Resolution"); break; //$NON-NLS-1$
	    case 6: comboBoxModel.addElement("Video Codec"); break; //$NON-NLS-1$
	    case 7: comboBoxModel.addElement("Video Rate"); break; //$NON-NLS-1$
	    case 8: comboBoxModel.addElement("Video Bit Rate"); break; //$NON-NLS-1$
	    case 9: comboBoxModel.addElement("Audio Codec"); break; //$NON-NLS-1$
	    case 10: comboBoxModel.addElement("Audio Rate"); break; //$NON-NLS-1$
	    case 11: comboBoxModel.addElement("Audio Bit Rate"); break; //$NON-NLS-1$
	    case 12: comboBoxModel.addElement("Audio Channels"); break; //$NON-NLS-1$
	    case 13: comboBoxModel.addElement("Location"); break; //$NON-NLS-1$
	    case 14: comboBoxModel.addElement("File Count"); break; //$NON-NLS-1$
	    case 15: comboBoxModel.addElement("Container"); break; //$NON-NLS-1$
	    case 16: comboBoxModel.addElement("Media Type"); break; //$NON-NLS-1$
		
		/* Getting name of extra info field */
	    default : { 
		name = (String)_fieldNames.get(activeAdditionalInfoFields[i]);
		comboBoxModel.addElement(name);
	    }
	    }
	}
	getAdditionalInfoFields().setModel(comboBoxModel);
	executeCommandAdditionalInfo();
    }
    
    
    /**
     * Loads an empty additional fields model...
     **/
    protected void loadEmptyAdditionalFields() {
	/* loads the additional info... */
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Subtitles"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Duration"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*",2)); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("File Size"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add("MiB"); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*",9)); //$NON-NLS-1$
	
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("CDs"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*",9)); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("CD Cases"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*(\\.)?(\\d)*")); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Resolution"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*(x)?(\\d)*")); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Video Codec"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Video Rate"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add("fps"); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*(\\.)?(\\d)*")); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Video Bit Rate"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add("kbps"); //$NON-NLS-1$
	_fieldDocuments.add(new DocumentRegExp("(\\d)*")); //$NON-NLS-1$
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Codec"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Rate"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add("Hz"); //$NON-NLS-1$
	//_fieldDocuments.add(new DocumentRegExp("(\\d)*((,\\s)?\\d*)*"));
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Bit Rate"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add("kbps"); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Audio Channels"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Location"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("File Count"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Container"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
		
	_saveLastFieldValue.add(new Boolean(true));
	_fieldNames.add("Media Type"); //$NON-NLS-1$
	_fieldValues.add(""); //$NON-NLS-1$
	_fieldUnits.add(""); //$NON-NLS-1$
	_fieldDocuments.add(new PlainDocument());
	//_fieldDocuments.add(new DocumentRegExp("(\\d)*",9));
	
	_fieldNames.addAll(MovieManager.getIt().getDatabase().getExtraInfoFieldNames());
	
	String name;
	for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
	    _saveLastFieldValue.add(new Boolean(true));
	    _fieldValues.add(""); //$NON-NLS-1$
	    _fieldUnits.add(""); //$NON-NLS-1$
	    _fieldDocuments.add(new PlainDocument());
	}
	
	DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
	int [] activeAdditionalInfoFields;
	
	activeAdditionalInfoFields = MovieManager.getIt().getActiveAdditionalInfoFields();
	
	for (int i = 0; i < activeAdditionalInfoFields.length; i++) {
	    
	    switch (activeAdditionalInfoFields[i]) {
		
	    case 0: comboBoxModel.addElement("Subtitles"); break; //$NON-NLS-1$
	    case 1: comboBoxModel.addElement("Duration"); break; //$NON-NLS-1$
	    case 2: comboBoxModel.addElement("File Size"); break; //$NON-NLS-1$
	    case 3: comboBoxModel.addElement("CDs"); break; //$NON-NLS-1$
	    case 4: comboBoxModel.addElement("CD Cases"); break; //$NON-NLS-1$
	    case 5: comboBoxModel.addElement("Resolution"); break; //$NON-NLS-1$
	    case 6: comboBoxModel.addElement("Video Codec"); break; //$NON-NLS-1$
	    case 7: comboBoxModel.addElement("Video Rate"); break; //$NON-NLS-1$
	    case 8: comboBoxModel.addElement("Video Bit Rate"); break; //$NON-NLS-1$
	    case 9: comboBoxModel.addElement("Audio Codec"); break; //$NON-NLS-1$
	    case 10: comboBoxModel.addElement("Audio Rate"); break; //$NON-NLS-1$
	    case 11: comboBoxModel.addElement("Audio Bit Rate"); break; //$NON-NLS-1$
	    case 12: comboBoxModel.addElement("Audio Channels"); break; //$NON-NLS-1$
	    case 13: comboBoxModel.addElement("Location"); break; //$NON-NLS-1$
	    case 14: comboBoxModel.addElement("File Count"); break; //$NON-NLS-1$
	    case 15: comboBoxModel.addElement("Container"); break; //$NON-NLS-1$
	    case 16: comboBoxModel.addElement("Media Type"); break; //$NON-NLS-1$
		
	    default : { 
		
		name = (String)_fieldNames.get(activeAdditionalInfoFields[i]);
		comboBoxModel.addElement(name);
	    }
	    }
	}
	
	getAdditionalInfoFields().setModel(comboBoxModel);
	
	executeCommandAdditionalInfo();
    }
  
    /**
     * Changes the seen status...
     **/
    private void executeCommandSeen() {
	
	getSeen();
      
	if(!_seen) {
	    getSeen().setIcon(new ImageIcon(MovieManager.getIt().getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	    _seen = true;
	} else {
	    getSeen().setIcon(new ImageIcon(MovieManager.getIt().getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
	    _seen = false;
	}
    }
    
    /**
     * Changes the cover...
     **/
    private void executeCommandCover() {
	try {
	    /* Opens the Open dialog... */
	    ExtendedFileChooser fileChooser = new ExtendedFileChooser();
      
	    fileChooser.setFileFilter(new CustomFileFilter(new String[]{"gif","png","jpg"},new String("Image Files (*.gif, *.png, *.jpg)"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      
	    if (MovieManager.getConfig().getLastDatabaseDir()!=null) {
		fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastDatabaseDir());
	    }
	    fileChooser.setDialogTitle(Localizer.getString("DialogMovieInfo.filechooser.select-cover.text")); //$NON-NLS-1$
	    fileChooser.setApproveButtonText(Localizer.getString("DialogMovieInfo.filechooser.select-cover.tooltip")); //$NON-NLS-1$
	    fileChooser.setApproveButtonToolTipText("Select cover"); //$NON-NLS-1$
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    int returnVal = fileChooser.showOpenDialog(this);
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		/* Gets the path... */
		String coverFolder = fileChooser.getSelectedFile().getAbsolutePath().replaceAll(fileChooser.getSelectedFile().getName(),""); //$NON-NLS-1$
		String cover = fileChooser.getSelectedFile().getName();
		/* Verifies extension... */
		String extension = cover.substring(cover.lastIndexOf('.')+1);
		if (extension.compareToIgnoreCase("gif")!=0 && extension.compareToIgnoreCase("png")!=0 && extension.compareToIgnoreCase("jpg")!=0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    throw new Exception("Image extension not supported."); //$NON-NLS-1$
		}
	
		if (!(new File(coverFolder+cover).exists())) {
		    throw new Exception("Image file not found."); //$NON-NLS-1$
		}
		/* Saves info... */
		InputStream inputStream = new FileInputStream(coverFolder+cover);
		_coverData = new byte[inputStream.available()];
		inputStream.read(_coverData);
		inputStream.close();
		_cover = cover;
		/* Loads the image...*/
		getCover().setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(_coverData).getScaledInstance(97,145,Image.SCALE_FAST)));
		setSaveCover(true);
		/* Sets the last path... */
		MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
	    }
	} catch (Exception e) {
	    log.error("", e); //$NON-NLS-1$
	}
    }

    /**
     * Changes the value JPanel and the JLabel unit and the...
     **/
    private void executeCommandAdditionalInfo() {
	
	int [] activeAdditionalInfoFields;
	
	activeAdditionalInfoFields = MovieManager.getIt().getActiveAdditionalInfoFields();
	
	if (_lastFieldIndex != -1 && _saveLastFieldValue.get(_lastFieldIndex).toString().equals(new Boolean(true).toString())) {
	    
	    /* Saves the info in the _fieldsValues... */
	    if (activeAdditionalInfoFields[_lastFieldIndex] == 1) {
		
		/* Saves the duration... */
		String hours = ((JTextField)getAdditionalInfoValuePanel().getComponent(0)).getText();
		String mints = ((JTextField)getAdditionalInfoValuePanel().getComponent(2)).getText();
		String secds = ((JTextField)getAdditionalInfoValuePanel().getComponent(4)).getText();
	    
		if (!hours.equals("") || !mints.equals("") || !secds.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    int time = 0;
		    if (!hours.equals("")) //$NON-NLS-1$
			time += Integer.parseInt(hours) * 3600;
		    if (!mints.equals("")) //$NON-NLS-1$
			time += Integer.parseInt(mints) * 60;
		    if (!secds.equals("")) //$NON-NLS-1$
			time += Integer.parseInt(secds);
		
		    _fieldValues.set(activeAdditionalInfoFields[_lastFieldIndex], String.valueOf(time));
		}
	    
		/* Recreates the JPanel... */
		getAdditionalInfoValuePanel().removeAll();
		JTextField textfield = new JTextField(14);
		Font font = textfield.getFont();
		font = new Font(font.getName(), font.getStyle(), 11);
		textfield.setFont(font);
		getAdditionalInfoValuePanel().add(textfield);
	    
	    } /* Subtitle, media info, and all the extra info fields */
	    else if (activeAdditionalInfoFields[_lastFieldIndex] == 0 || activeAdditionalInfoFields[_lastFieldIndex] >= 16) {
		/* Current value in combobox */
		String value = (String) ((JComboBox) getAdditionalInfoValuePanel().getComponent(0)).getSelectedItem();
		
		/* Old value */
		String oldValue = (String) _fieldValues.get(activeAdditionalInfoFields[_lastFieldIndex]);
		
		_fieldValues.set(activeAdditionalInfoFields[_lastFieldIndex], value);
		
		AdditionalInfoFieldDefaultValues valuesObj = (AdditionalInfoFieldDefaultValues) MovieManager.getConfig().getAdditionalInfoDefaultValues().get(_fieldNames.get(_lastFieldIndex));
		
		/* Creating a new entry in the values hashmap */
		if (valuesObj == null) {
		    valuesObj = new AdditionalInfoFieldDefaultValues((String) _fieldNames.get(_lastFieldIndex));
		    valuesObj.addValue(oldValue);
		    MovieManager.getConfig().getAdditionalInfoDefaultValues().put(_fieldNames.get(_lastFieldIndex), valuesObj);
		}
		
		valuesObj.insertValue(value);
		
		/* Recreates the JPanel... */
		getAdditionalInfoValuePanel().removeAll();
		JTextField textfield = new JTextField(14);
		Font font = textfield.getFont();
		font = new Font(font.getName(), font.getStyle(), 11);
		textfield.setFont(font);
		getAdditionalInfoValuePanel().add(textfield);
	    }        
	    else {
		/* Saves the field... */
		_fieldValues.set(activeAdditionalInfoFields[_lastFieldIndex],((JTextField)getAdditionalInfoValuePanel().getComponent(0)).getText());
	    }
	}
	
	int currentFieldIndex = getAdditionalInfoFields().getSelectedIndex();
	String currentFieldIndexValue = ""; //$NON-NLS-1$
	
	if (currentFieldIndex != -1) {
	    currentFieldIndexValue = (String)_fieldValues.get(activeAdditionalInfoFields[currentFieldIndex]);
	    
	    /* Displays the new info... */
	    if (activeAdditionalInfoFields[currentFieldIndex] == 1) {
		/* Recreates the panel... */
		getAdditionalInfoValuePanel().removeAll();
		JTextField hoursText = new JTextField(3);
		
		/* Get the correct fontsize */
		Font font = hoursText.getFont();
		font = new Font(font.getName(), font.getStyle(), 11);
		hoursText.setFont(font);
	    
		hoursText.setDocument(new DocumentRegExp("(\\d)*",2)); //$NON-NLS-1$
		getAdditionalInfoValuePanel().add(hoursText);
		JLabel separatorOne = new JLabel(" : "); //$NON-NLS-1$
		separatorOne.setFont(new Font(separatorOne.getFont().getName(),Font.PLAIN,separatorOne.getFont().getSize()));
		getAdditionalInfoValuePanel().add(separatorOne);
		JTextField mintsText = new JTextField(3);
		mintsText.setFont(font);
		mintsText.setDocument(new DocumentRegExp("(\\d)*",2)); //$NON-NLS-1$
		getAdditionalInfoValuePanel().add(mintsText);
		JLabel separatorTwo = new JLabel(" . "); //$NON-NLS-1$
		separatorTwo.setFont(new Font(separatorTwo.getFont().getName(),Font.PLAIN,separatorTwo.getFont().getSize()));
		getAdditionalInfoValuePanel().add(separatorTwo);
		JTextField secdsText = new JTextField(3);
		secdsText.setFont(font);
		secdsText.setDocument(new DocumentRegExp("(\\d)*",2)); //$NON-NLS-1$
		getAdditionalInfoValuePanel().add(secdsText);
		
		JLabel separatorThree = new JLabel("  "); //$NON-NLS-1$
		separatorThree.setFont(new Font(separatorThree.getFont().getName(),Font.PLAIN,separatorThree.getFont().getSize()));
		getAdditionalInfoValuePanel().add(separatorThree);
		
		/* Displays... */
		if(!_fieldValues.get(activeAdditionalInfoFields[currentFieldIndex]).equals("")) { //$NON-NLS-1$
		
		    int time = Integer.parseInt((String)_fieldValues.get(activeAdditionalInfoFields[currentFieldIndex]));
		    int hours = time / 3600;
		    int mints = time / 60 - hours * 60;
		    int secds = time - hours * 3600 - mints *60;
		    hoursText.setText(String.valueOf(hours));
		    mintsText.setText(String.valueOf(mints));
		    secdsText.setText(String.valueOf(secds));
		}
	    } 
	    else if (activeAdditionalInfoFields[currentFieldIndex] == 0 || activeAdditionalInfoFields[currentFieldIndex] >= 16) {
		
		getAdditionalInfoValuePanel().removeAll();
		
		SteppedComboBox fields;
		
		/* Temporary till the office LnFs supports the SteppedComboBox */
		if (MovieManager.getConfig().getLookAndFeelType() == 0 && MovieManager.getConfig().getLookAndFeelString().startsWith("Office")) { //$NON-NLS-1$
		    fields = new SteppedComboBox(new String[]{"",""}, 0); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		    fields = new SteppedComboBox(new String[]{"",""}); //$NON-NLS-1$ //$NON-NLS-2$
		
		fields.setFont(new Font(fields.getFont().getName(), Font.PLAIN, fontSize));
		
		/* If using oyoaha laf the border needs to be set to make the combobox smaller */
		if (MovieManager.getConfig().getLookAndFeelType() == 2)
		    fields.setBorder(BorderFactory.createLoweredBevelBorder());
		
		fields.setEditable(true);
		
		fields.setPreferredSize(new Dimension(valueComboBoxWidth, valueComboBoxHeight));
		
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		
		/* Getting the stored additional info values */
		AdditionalInfoFieldDefaultValues valuesObj = (AdditionalInfoFieldDefaultValues) MovieManager.getConfig().getAdditionalInfoDefaultValues().get(_fieldNames.get(currentFieldIndex));
		
		comboBoxModel.addElement(currentFieldIndexValue);
		
		FontMetrics fontmetrics = getFontMetrics(getFont());
		int line_widths;
		int maxPopupWidth = fontmetrics.stringWidth(currentFieldIndexValue);;
		
		if (valuesObj != null) {
		    
		    ArrayList values = valuesObj.getDefaultValues();
		    String temp;
		    
		    while (!values.isEmpty()) {
			temp = (String) values.remove(0);
			if (!temp.equals(currentFieldIndexValue)) {
			    comboBoxModel.addElement(temp);
			    
			    line_widths = fontmetrics.stringWidth(temp);
			    
			    if (line_widths > maxPopupWidth)
				maxPopupWidth = line_widths;
			}
		    }
		}
		
		Dimension d = fields.getPreferredSize();
		
		maxPopupWidth += 20;
		
		/* Minimum size should be width of combobox */
		if (maxPopupWidth < d.width)
		    maxPopupWidth = d.width;
		
		/* Setting width of the popup menu */
		
		fields.setPopupWidth(maxPopupWidth);
		
		fields.setModel(comboBoxModel);
		
		/* Setting the caret position of the combobox */
		((JTextField) fields.getEditor().getEditorComponent()).setCaretPosition(0);
		
		getAdditionalInfoValuePanel().add(fields);
		
		getAdditionalInfoPanel().validate();
	    }
	    else {
		/* Adds document... */
		((JTextField)getAdditionalInfoValuePanel().getComponent(0)).setDocument((Document)_fieldDocuments.get(activeAdditionalInfoFields[currentFieldIndex]));
		((JTextField)getAdditionalInfoValuePanel().getComponent(0)).setText(currentFieldIndexValue);
		((JTextField)getAdditionalInfoValuePanel().getComponent(0)).setCaretPosition(0);
	    }
	    
	    /* Changes units... */
	    getAdditionalInfoUnits().setText((String)_fieldUnits.get(activeAdditionalInfoFields[currentFieldIndex]));
	    /* Revalidates... */
	    getAdditionalInfoUnits().revalidate();
	}
	
	/* updates index... */
	_lastFieldIndex = currentFieldIndex;
    }
    
    
    /**
     * Saves and exits...
     * If column is not null, the movie should be added to the list named column
     **/
    public ModelEntry executeCommandSave(String listName) {
	
	Database database = MovieManager.getIt().getDatabase();
	ModelEntry entry = null;
	
	_hasReadProperties = false;
	
	/* Checks the movie title... */
	if (!getMovieTitle().getText().equals("")) { //$NON-NLS-1$
	    /* Saves the current field... */
	    executeCommandAdditionalInfo();
	    
	    /* Saves the cover... */
	    if (_saveCover) {
		
		String coversFolder = MovieManager.getConfig().getCoversPath();
		File coverFile = new File(coversFolder, _cover);
		
		try {
		    
		    if (coverFile.exists()) {
			if (!coverFile.delete() && !coverFile.createNewFile()) {
			    throw new Exception("Cannot delete old cover file and create a new one."); //$NON-NLS-1$
			}
		    } else {
			if (!coverFile.createNewFile()) {
			    throw new Exception("Cannot create cover file:" + coverFile.getAbsolutePath()); //$NON-NLS-1$
			}
		    }
		    
		    /* Copies the cover to the covers folder... */
		    OutputStream outputStream = new FileOutputStream(coverFile);
		    outputStream.write(_coverData);
		    outputStream.close();
		    
		} catch (Exception e) {
		    log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
		    log.warn("Access denied when trying to save cover:" + coverFile.getAbsolutePath()); //$NON-NLS-1$
		    
		    DialogAlert alert;
		    
		    if (isDisplayable())
			alert = new DialogAlert(this, Localizer.getString("DialogMovieInfo.alert.title.access-denied"), Localizer.getString("DialogMovieInfo.alert.message.error-when-saving-cover"), e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		    else
			alert = new DialogAlert(MovieManager.getIt(), Localizer.getString("DialogMovieInfo.alert.title.access-denied"), Localizer.getString("DialogMovieInfo.alert.message.error-when-saving-cover"), e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		    
		    ShowGUI.showAndWait(alert, true);

		    _cover = ""; //$NON-NLS-1$
		}
	    }
	    
	    
	    /* Sets the additional info... */
	    int duration = -1;
	    if (!((String)_fieldValues.get(1)).equals("")) { //$NON-NLS-1$
		duration = Integer.parseInt((String)_fieldValues.get(1));
	    }
	    int fileSize = -1;
	    if (!((String)_fieldValues.get(2)).equals("")) { //$NON-NLS-1$
		fileSize = Integer.parseInt((String)_fieldValues.get(2));
	    }
	    int CDs = -1;
	    if (!((String)_fieldValues.get(3)).equals("")) { //$NON-NLS-1$
		CDs = Integer.parseInt((String)_fieldValues.get(3));
	    }
	    double cdCases = -1;
	    if (!((String)_fieldValues.get(4)).equals("")) { //$NON-NLS-1$
		cdCases = Double.parseDouble((String)_fieldValues.get(4));
	    }
	    int fileCount = -1;
	    if (!((String)_fieldValues.get(14)).equals("")) { //$NON-NLS-1$
		fileCount = Integer.parseInt((String)_fieldValues.get(14));
	    }
	    
	    
	    /* 
	     * Updates the additional info on the already existing movie
	     */
	    
	    if (isEpisode) {
		
		/* If editing */
		if (_edit) {
		    
		    model.setCover(_cover);
		    model.setDate(getDate().getText());
		    model.setTitle(getMovieTitle().getText());
		    model.setDirectedBy(getDirectedBy().getText());
		    model.setWrittenBy(getWrittenBy().getText());
		    model.setGenre(getGenre().getText());
		    model.setRating(getRating().getText());
		    model.setPlot(getPlot().getText());
		    model.setCast(getCast().getText());
		    model.setNotes(getNotes().getText());
		    model.setSeen(_seen);
		    model.setAka(getAka().getText());
		    model.setCountry(getCountry().getText());
		    model.setLanguage(getLanguage().getText());
		    model.setColour(getColour().getText());
		    model.setCertification(getCertification().getText());
		    model.setWebSoundMix(getWebSoundMix().getText());
		    model.setWebRuntime(getWebRuntime().getText());
		    model.setAwards(getAwards().getText());
		    
		    model.setCoverData(_coverData);
		    setEpisodeNumber(episodeNumber);
		    entry = model;

		    /* Saves info in the database... */
		    
		    /* Sets General Info... */
		    database.setGeneralInfoEpisode(_index, (ModelEpisode) model);
		    
		    ModelAdditionalInfo additionalInfo = new ModelAdditionalInfo((String)_fieldValues.get(0), duration, fileSize,CDs, cdCases,(String)_fieldValues.get(5),
										 (String)_fieldValues.get(6), (String)_fieldValues.get(7), (String)_fieldValues.get(8), 
										 (String)_fieldValues.get(9), (String)_fieldValues.get(10), (String)_fieldValues.get(11), 
										 (String)_fieldValues.get(12), (String)_fieldValues.get(13), fileCount, (String)_fieldValues.get(15), 
										 (String)_fieldValues.get(16));
		    
		    
		    
		    /* Sets the additional info... */
		    database.setAdditionalInfoEpisode(_index, additionalInfo);
		    
		    /* Adds the extra info... */
		    ArrayList extraFieldNamesList = new ArrayList();
		    ArrayList extraFieldValuesList = new ArrayList();
		    
		    for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
			extraFieldNamesList.add(_fieldNames.get(i));
			extraFieldValuesList.add(_fieldValues.get(i));
		    }
		    
		    database.setExtraInfoEpisode(_index, extraFieldNamesList, extraFieldValuesList);
		    
		    /* Updates model */
		    additionalInfo.setExtraInfoFieldValues(extraFieldValuesList);
		    entry.setAdditionalInfo(additionalInfo);
		    
		}
		/* If adding new episode */
		else {
		    
		    entry = new ModelEpisode(-1, _index, episodeNumber, _IMDB, _cover,
					 getDate().getText(), getMovieTitle().getText(), getDirectedBy().getText(),
					 getWrittenBy().getText(), getGenre().getText(),
					 getRating().getText(), getPlot().getText(), getCast().getText(), getNotes().getText(),
					 _seen, getAka().getText(), getCountry().getText(), getLanguage().getText(), 
					 getColour().getText(), getCertification().getText(), getWebSoundMix().getText(), 
					 getWebRuntime().getText(), getAwards().getText());
		    
		    entry.setCoverData(_coverData);
		    
		    
		    int episodeindex = database.addGeneralInfoEpisode((ModelEpisode) entry);
		    
		    if (episodeindex != -1) {
			
			ModelAdditionalInfo additionalInfo = new ModelAdditionalInfo((String)_fieldValues.get(0),duration, fileSize,CDs,cdCases,
										     (String)_fieldValues.get(5),(String)_fieldValues.get(6),(String)_fieldValues.get(7),
										     (String)_fieldValues.get(8),(String)_fieldValues.get(9),(String)_fieldValues.get(10),
										     (String)_fieldValues.get(11), (String)_fieldValues.get(12), (String)_fieldValues.get(13),
										     fileCount, (String)_fieldValues.get(15), (String)_fieldValues.get(16));
			
			
			
			/* Adds the additional info... */
			database.addAdditionalInfoEpisode(episodeindex, additionalInfo);
			
			
			/* Adds the extra info... */
			ArrayList extraFieldNamesList = new ArrayList();
			ArrayList extraFieldValuesList = new ArrayList();
			
			for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
			    extraFieldNamesList.add(_fieldNames.get(i));
			    extraFieldValuesList.add(_fieldValues.get(i));
			}
			if (extraFieldNamesList.size() > 0)
			    database.addExtraInfoEpisode(episodeindex, extraFieldNamesList, extraFieldValuesList);
			
			entry.setKey(episodeindex);
			
			/* Updates model */
			additionalInfo.setExtraInfoFieldValues(extraFieldValuesList);
			entry.setAdditionalInfo(additionalInfo);
		    }
		}
		/* If movie */
	    } else {
		
		entry = new ModelMovie(_index, _IMDB, _cover, getDate().getText(), getMovieTitle().getText(), 
				       getDirectedBy().getText(), getWrittenBy().getText(), getGenre().getText(),
				       getRating().getText(), getPlot().getText(), getCast().getText(), getNotes().getText(),
				       _seen, getAka().getText(), getCountry().getText(), getLanguage().getText(), 
				       getColour().getText(), getCertification().getText(), getMpaa().getText(), 
				       getWebSoundMix().getText(), getWebRuntime().getText(), getAwards().getText());
		
		entry.setCoverData(_coverData);
		
		if (_edit) {
		    
		    /* Saves info in the database... */
		    
		    /* Sets General Info... */
		    database.setGeneralInfo((ModelMovie) entry);
		    
		    ModelAdditionalInfo additionalInfo = new ModelAdditionalInfo((String)_fieldValues.get(0),duration, fileSize,CDs,cdCases,
										 (String)_fieldValues.get(5),(String)_fieldValues.get(6),(String)_fieldValues.get(7),
										 (String)_fieldValues.get(8),(String)_fieldValues.get(9),(String)_fieldValues.get(10),
										 (String)_fieldValues.get(11), (String)_fieldValues.get(12), (String)_fieldValues.get(13),
										 fileCount, (String)_fieldValues.get(15), (String)_fieldValues.get(16));
		    
		    database.setAdditionalInfo(_index, additionalInfo);
		    
		    ArrayList extraFieldNamesList = new ArrayList();
		    ArrayList extraFieldValuesList = new ArrayList();
		    
		    for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
			extraFieldNamesList.add(_fieldNames.get(i));
			extraFieldValuesList.add(_fieldValues.get(i));
		    }
		    
		    database.setExtraInfoMovie(_index, extraFieldNamesList, extraFieldValuesList);
		    
		    /* Updates model */
		    additionalInfo.setExtraInfoFieldValues(extraFieldValuesList);
		    entry.setAdditionalInfo(additionalInfo);
		    
		}
		
		/* If adding new movie */
		else {
		    
		    entry = new ModelMovie(-1, _IMDB, _cover, getDate().getText(), getMovieTitle().getText(), 
				       getDirectedBy().getText(), getWrittenBy().getText(), getGenre().getText(),
				       getRating().getText(), getPlot().getText(), getCast().getText(), getNotes().getText(),
				       _seen, getAka().getText(), getCountry().getText(), getLanguage().getText(), 
				       getColour().getText(), getCertification().getText(), getMpaa().getText(), 
				       getWebSoundMix().getText(), getWebRuntime().getText(), getAwards().getText());
		    
		    entry.setCoverData(_coverData);
		    
		    /* Adds General Info... */
		    _index = MovieManager.getIt().getDatabase().addGeneralInfo((ModelMovie) entry);
		    
		    if (_index != -1) {
			
			entry.setKey(_index);
			
			ModelAdditionalInfo additionalInfo = new ModelAdditionalInfo((String)_fieldValues.get(0),duration, fileSize,CDs,cdCases,
										     (String)_fieldValues.get(5),(String)_fieldValues.get(6),(String)_fieldValues.get(7),
										     (String)_fieldValues.get(8),(String)_fieldValues.get(9),(String)_fieldValues.get(10),
										     (String)_fieldValues.get(11), (String)_fieldValues.get(12), (String)_fieldValues.get(13),
										     fileCount, (String)_fieldValues.get(15), (String)_fieldValues.get(16));
			
			MovieManager.getIt().getDatabase().addAdditionalInfo(_index, additionalInfo);
			
			
			/* Adds the extra info... */
			 ArrayList extraFieldNamesList = new ArrayList();
			 ArrayList extraFieldValuesList = new ArrayList();
			 
			 for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
			     extraFieldNamesList.add(_fieldNames.get(i));
			     extraFieldValuesList.add(_fieldValues.get(i));
			 }
			 
			 database.addExtraInfoMovie(_index, extraFieldNamesList, extraFieldValuesList);
			 
			 /* Updates model */
			 additionalInfo.setExtraInfoFieldValues(extraFieldValuesList);
			 entry.setAdditionalInfo(additionalInfo);
			 
			 /* Add new row in Lists table with default values */
			extraFieldNamesList = database.getListsColumnNames();
			extraFieldValuesList = new ArrayList();
			
			String applyToThisList = ""; //$NON-NLS-1$
			
			if (listName != null)
			    applyToThisList = listName;
			
			for (int i = 0; i < extraFieldNamesList.size(); i++) {
			    
			    if (extraFieldNamesList.get(i).equals(applyToThisList))
				extraFieldValuesList.add(new Boolean(true));
			    else
				extraFieldValuesList.add(new Boolean(false));
			}
			database.addLists(_index, extraFieldNamesList, extraFieldValuesList);
		    }
		}
	    }
	    
	} else {
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
	    //alert.setVisible(true);
	    ShowGUI.showAndWait(alert, true);
	}
	
	/* Remove old cover possible cached by JTree cellrenderer */
        ((ExtendedTreeCellRenderer) MovieManager.getIt().getMoviesList().getCellRenderer()).removeCoverFromCache(entry.getCover());
	
	return entry;
    }
    
    public void executeAndReloadMovieList(ModelEntry reloadEntry) {
	
	if  (reloadEntry != null && reloadEntry.getKey() != -1) {
	    
	    /* Reloads... */
	    
	    //long time = System.currentTimeMillis();
	    MovieManagerCommandSelect.executeAndReload(reloadEntry, _edit, isEpisode, true);
	    MovieManager.getIt().getMoviesList().requestFocus(true);
	    
	    /*Updates the entries value shown in the right side of the toolbars.*/
	    MovieManager.getIt().setAndShowEntries();
	    
	    /* Exits... */
	    dispose();
	}
    }
    
    private File [] executeGetFile() {
	
	File [] file = null;
	
	try {
	    /* Opens the Open dialog... */
	    ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	    //JFileChooser fileChooser = new JFileChooser();
	    
		fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);
		
	    String [] filterChoices = new String[] {"All Files (*.*)",  //$NON-NLS-1$
					      "All Files (*.*) Parse media files with MediaInfo library", //$NON-NLS-1$
					      "Media files (*.avi, *.ogm, *.mpeg, *.divx, *.ifo)" }; //$NON-NLS-1$
		
	    fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, filterChoices[0])); //$NON-NLS-1$
	    
	    if (MovieManager.isWindows())
		fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, filterChoices[1])); //$NON-NLS-1$
	    
		
	    
	    fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"avi","mpg","mpeg","ogm","ogg","ifo","divx"}, filterChoices[2])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	    
	    
	    javax.swing.filechooser.FileFilter[] filters = fileChooser.getChoosableFileFilters();
	    
	    for (int i = 0; i < filters.length; i++) {
		if (filters[i].getDescription().equals(MovieManager.getConfig().getLastFileFilterUsed()))
		    fileChooser.setFileFilter(filters[i]);
	    }
	    
	    // fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, new String("All Files (*.*)")));
// 	    Moviemanager.getConfig().setLastFilterUsed(fileChooser.getFileFilter().getDescription());
	    
	    if (MovieManager.getConfig().getLastFileDir() != null)
		fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastFileDir());
	    
	    fileChooser.setDialogTitle(Localizer.getString("DialogMovieInfo.filechooser.title.select-movie-file")); //$NON-NLS-1$
	    fileChooser.setApproveButtonText(Localizer.getString("DialogMovieInfo.filechooser.approve-button.text")); //$NON-NLS-1$
	    fileChooser.setApproveButtonToolTipText(Localizer.getString("DialogMovieInfo.filechooser.approve-button.tooltip")); //$NON-NLS-1$
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    fileChooser.setMultiSelectionEnabled(true);
	    
	    int returnVal = fileChooser.showOpenDialog(this);
	    if ((returnVal == JFileChooser.APPROVE_OPTION)) {
		
		file = fileChooser.getSelectedFiles();
		
		String lastFileFilter = fileChooser.getFileFilter().getDescription();
		MovieManager.getConfig().setLastFileFilterUsed(lastFileFilter);
		
		/* Use media info library */
		if (lastFileFilter.equals(filterChoices[1]))
		    MovieManager.getConfig().setUseMediaInfoDLL(2);
		else if (MovieManager.isWindows()) /* Use media info library only if java parser not available */
		    MovieManager.getConfig().setUseMediaInfoDLL(1);
		else
		    MovieManager.getConfig().setUseMediaInfoDLL(0);
		
		/* Sets the last path... */
		MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
	    }
	    
	    
	} catch (Exception e) {
	    log.error("" + e); //$NON-NLS-1$
	}
	return file;
    }
    
    
    private void executeGetDVDInfo() {
	
	/* Opens the Open dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	//JFileChooser fileChooser = new JFileChooser();
	
	try {
	    fileChooser.setFileSelectionMode(ExtendedFileChooser.DIRECTORIES_ONLY);
	    
	    fileChooser.setFileFilter(new CustomFileFilter(CustomFileFilter.DIRECTORIES_ONLY, Localizer.getString("DialogMovieInfo.filechooser.filter.dvd-drive"))); //$NON-NLS-1$

	    fileChooser.setDialogTitle(Localizer.getString("DialogMovieInfo.filechooser.title.select-dvd-drive")); //$NON-NLS-1$
	    fileChooser.setApproveButtonText(Localizer.getString("DialogMovieInfo.filechooser.approve-button.text")); //$NON-NLS-1$
	    fileChooser.setApproveButtonToolTipText(Localizer.getString("DialogMovieInfo.filechooser.approve-button.select-dvd-drive.tooltip")); //$NON-NLS-1$
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    
	    int returnVal = fileChooser.showOpenDialog(this);
	    
	    if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
		/* Gets the path... */
		File selectedFile = fileChooser.getSelectedFile();
		
		if (selectedFile.getName().equals("AUDIO_TS")) { //$NON-NLS-1$
		    selectedFile = selectedFile.getParentFile();
		}
		
		if (!selectedFile.getName().equals("VIDEO_TS")) { //$NON-NLS-1$
		    File tmp = new File(selectedFile.getAbsolutePath(), "VIDEO_TS"); //$NON-NLS-1$
		    
		    if (tmp.isDirectory())
			selectedFile = tmp;
		}
		
		if (!selectedFile.getName().equals("VIDEO_TS")) { //$NON-NLS-1$
		    throw new Exception("DVD drive not found:" + fileChooser.getSelectedFile()); //$NON-NLS-1$
		}
		    

		/* Get the ifo files */
		File [] list = selectedFile.listFiles();
		
		ArrayList ifoList = new ArrayList(4);
		
		for (int i = 0; i < list.length; i++) {
		    if (list[i].getName().regionMatches(true, list[i].getName().lastIndexOf("."), ".ifo", 0, 4)) //$NON-NLS-1$ //$NON-NLS-2$
			ifoList.add(list[i]);
		}
		
		File [] ifo = (File[]) ifoList.toArray(new File[0]);
		
		if (ifo == null || ifo.length == 0) {
		    DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogMovieInfo.alert.title.alert"), Localizer.getString("DialogMovieInfo.alert.message.failed-to-locate-the-drive")); //$NON-NLS-1$ //$NON-NLS-2$
		    //alert.setVisible(true);
		    ShowGUI.showAndWait(alert, true);
		}
		else {
		    
		    int biggestSize = 0;
		    int biggestSizeIndex = -1;
		    int longestDuration = 0;
		    int longestDurationIndex = -1;
		    
		    int mainIfoIndex = -1;
		    
		    FilePropertiesMovie [] fileProperties = new FilePropertiesMovie[ifo.length]; 
		    
		    for (int i = 0; i < ifo.length; i++) {
			try {
			    fileProperties[i] = new FilePropertiesMovie(ifo[i].getAbsolutePath(), MovieManager.getConfig().getUseMediaInfoDLL());
			    
			    if (ifo[i].length() > biggestSize) {
				biggestSize = (int) ifo[i].length();
				biggestSizeIndex = i;
			    }
			    
			    if (fileProperties[i].getDuration() > longestDuration) {
				longestDuration = fileProperties[i].getDuration();
				longestDurationIndex = i;
			    }
			    
			} catch (Exception e) {
			    log.warn("Error when parsing file:" + ifo[i]); //$NON-NLS-1$
			}
		    }
		    
		    /* If duration less than 30 minutes, will check the other ifo files */
		    if (fileProperties[biggestSizeIndex].getDuration() < 1800) {
			
			if (longestDurationIndex != biggestSizeIndex && longestDuration > 1800)
			    mainIfoIndex = longestDurationIndex;
		    }
		    else
			mainIfoIndex = biggestSizeIndex;
		    
		    
		    mediaType = "DVD"; //$NON-NLS-1$
		    
		    executeCommandGetFileInfo(fileProperties[mainIfoIndex]);
		    
		    /* Calculating the size */
		    
		    long size = 0;
		    
		    for (int i = 0; i < list.length; i++) {
			size += list[i].length();
		    }
		    
		    size = size / (1024 * 1024);
		    
		    _fieldValues.set(2, String.valueOf((int) size));
		    _saveLastFieldValue.set(2, new Boolean(true));
		}
	    }
	}
	catch (Exception e) {
	    log.error("" + e); //$NON-NLS-1$
	}
	/* Sets the last path... */
	MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
    }
	
    
    
  
    
    
    
    
    /**
     * Gets the file properties...
     **/
    public void executeCommandGetFileInfo(File [] file) {
	
	for (int i = 0; i < file.length; i++) {
	    
	    if (!file[i].isFile())
		continue;
	    
	    try {
		/* Reads the info... */
		FilePropertiesMovie properties = new FilePropertiesMovie(file[i].getAbsolutePath(), MovieManager.getConfig().getUseMediaInfoDLL());
		
		executeCommandGetFileInfo(properties);
		
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
	    }
	}
    }    
    
    
    /**
     * Gets the file properties...
     **/
    public void executeCommandGetFileInfo(FilePropertiesMovie properties) {
	
	try {
		
	    //executeCommandAdditionalInfo();
		
	    if (!properties.getSubtitles().equals("")) //$NON-NLS-1$
		_fieldValues.set(0, properties.getSubtitles());
		
	    /* Saves info... */
	    int duration = properties.getDuration();
	    if (_hasReadProperties && duration != -1 && !((String)_fieldValues.get(1)).equals("")) { //$NON-NLS-1$
		duration += Integer.parseInt((String)_fieldValues.get(1));
	    }
	    if(duration != -1) {
		_fieldValues.set(1, String.valueOf(duration));
		_saveLastFieldValue.set(1,new Boolean(false));
	    } else {
		_fieldValues.set(1, ""); //$NON-NLS-1$
	    }
		
	    int fileSize = properties.getFileSize();
	    if (_hasReadProperties && fileSize != -1 && !((String)_fieldValues.get(2)).equals("")) { //$NON-NLS-1$
		fileSize += Integer.parseInt((String)_fieldValues.get(2));
	    }
	    if (fileSize != -1) {
		_fieldValues.set(2,String.valueOf(fileSize));
		_saveLastFieldValue.set(2,new Boolean(false));
	    } else {
		_fieldValues.set(2,""); //$NON-NLS-1$
	    }
	    if (fileSize != -1) {
		int cds = (int)Math.ceil(fileSize / 702.0);
		_fieldValues.set(3,String.valueOf(cds));
		_saveLastFieldValue.set(3,new Boolean(false));
	    }
		
	    if (((String)_fieldValues.get(4)).equals("")) { //$NON-NLS-1$
		_fieldValues.set(4,String.valueOf(1));
		_saveLastFieldValue.set(4,new Boolean(false));
	    }
		
	    _fieldValues.set(5,properties.getVideoResolution());
	    _fieldValues.set(6,properties.getVideoCodec());
	    _fieldValues.set(7,properties.getVideoRate());
	    _fieldValues.set(8,properties.getVideoBitrate());
	    _fieldValues.set(9,properties.getAudioCodec());
	    _fieldValues.set(10,properties.getAudioRate());
	    _fieldValues.set(11,properties.getAudioBitrate());
	  
	    String audioChannels = properties.getAudioChannels();
	    _fieldValues.set(12, audioChannels);
		
	    String location = properties.getLocation();
		
	    /* Adds location only if file exist on writable media */
	    if (MovieManager.getConfig().isWritable(new File(location).getParentFile())) {
		    
		String currentValue = (String) _fieldValues.get(13);
		    
		if (currentValue.equals("")) { //$NON-NLS-1$
		    _fieldValues.set(13, location);
		}
		else {
		    if (_hasReadProperties) {
			    
			StringTokenizer tokenizer = new StringTokenizer(currentValue, "*"); //$NON-NLS-1$
			boolean fileAlreadyAdded = false;
			    
			while (tokenizer.hasMoreTokens()) {
			    if (tokenizer.nextToken().equals(location))
				fileAlreadyAdded = true;
			}
			    
			if (fileAlreadyAdded)
			    log.warn("file already added"); //$NON-NLS-1$
			else {
			    currentValue += "*" + location; //$NON-NLS-1$
			    _fieldValues.set(13, currentValue);
			}
		    }
		    else 
			_fieldValues.set(13, location);
		}
	    }
		
	    int fileCount = 1;
	    if (_hasReadProperties && !((String)_fieldValues.get(14)).equals("")) { //$NON-NLS-1$
		fileCount += Integer.parseInt((String)_fieldValues.get(14));
	    }
		
	    _fieldValues.set(14, String.valueOf(fileCount));
		
	    _fieldValues.set(15, properties.getContainer());
		
	    _fieldValues.set(16, mediaType);
		
	    /* Sets title if title field is empty... */
	    if (getMovieTitle().getText().equals("")) { //$NON-NLS-1$
		    
		if (!properties.getMetaDataTagInfo("INAM").equals("")) //$NON-NLS-1$ //$NON-NLS-2$
		    getMovieTitle().setText(properties.getMetaDataTagInfo("INAM")); //$NON-NLS-1$
		    
		else {
		    String title = properties.getFileName();
			
		    if (title.lastIndexOf(".") != -1) //$NON-NLS-1$
			title = title.substring(0, title.lastIndexOf('.'));
			
		    getMovieTitle().setText(title);
		}
	    }
		
	    /* The value currently in the selected index will not be saved, but be replaced by the new info */
	    _saveLastFieldValue.set(_lastFieldIndex, new Boolean(false).toString());
	    executeCommandAdditionalInfo();
	    for (int u = 0; u < _saveLastFieldValue.size(); u++) {
		_saveLastFieldValue.set(u, new Boolean(true));
	    }
		
	    _hasReadProperties = true;
		
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
	}
    }
    
    
    
/**
     * Empties all the general info fields values
     **/
    public void executeCommandSetGeneralInfoFieldsEmpty() {
	
	try {
	   
	    getDate().setText(""); //$NON-NLS-1$
	    getColour().setText(""); //$NON-NLS-1$
	    getDirectedBy().setText(""); //$NON-NLS-1$
	    getWrittenBy().setText(""); //$NON-NLS-1$
	    getGenre().setText(""); //$NON-NLS-1$
	    getRating().setText(""); //$NON-NLS-1$
	    getCountry().setText(""); //$NON-NLS-1$
	    _seen = false;
	    getLanguage().setText(""); //$NON-NLS-1$
	    getPlot().setText(""); //$NON-NLS-1$
	    getCast().setText(""); //$NON-NLS-1$
	    
	    getAka().setText(""); //$NON-NLS-1$
	    getCertification().setText(""); //$NON-NLS-1$
	    getWebSoundMix().setText(""); //$NON-NLS-1$
	    getWebRuntime().setText(""); //$NON-NLS-1$
	    getAwards().setText(""); //$NON-NLS-1$
	    
	    _cover = ""; //$NON-NLS-1$
	    _coverData = null;
	    setSaveCover(false);
	    setIMDB(""); //$NON-NLS-1$
	    
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
	    e.printStackTrace();
	}
    }
    
    
    /**
     * Empties all the additional fields values stored in the _fieldValues arrayList
     **/
    public void executeCommandSetAdditionalInfoFieldsEmpty() {
	
	try {
	    
	    for (int i = 0; i < _fieldValues.size(); i++)
		_fieldValues.set(i,""); //$NON-NLS-1$
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
	    e.printStackTrace();
	    _hasReadProperties = false;
	}
    }
    

    /**
     * Gets the IMDB info for this movie...
     **/
    protected void executeCommandGetIMDBInfo() {
	/* Checks the movie title... */
	if(!getMovieTitle().getText().equals("")) { //$NON-NLS-1$
	    DialogIMDB dialogIMDB = new DialogIMDB(this);
	    //dialogIMDB.setVisible(true);
	    ShowGUI.show(dialogIMDB, true);
	} else {
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
	    //alert.setVisible(true);
	    ShowGUI.showAndWait(alert, true);
	}
    }
    
    /**
     * Gets the IMDB info for movies (multiAdd)
     **/
    public void executeCommandGetIMDBInfoMultiMovies(MovieManagerCommandAddMultipleMovies multipleMovies, String searchString, String filename, int multiAddSelectOption) {
	
	/* Checks the movie title... */
	log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
	if(!searchString.equals("")) { //$NON-NLS-1$
	    DialogIMDB dialogIMDB = new DialogIMDB(this, searchString, filename, multipleMovies, multiAddSelectOption);
	} else {
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
	    //    alert.setVisible(true);
	    ShowGUI.showAndWait(alert, true);
	}
    }
    
    /**
     * Gets the tv.com info for this episode...
     **/
    protected void executeCommandGetTVDOTCOMInfo() {
	/* Checks the movie title... */
	if(!getMovieTitle().getText().equals("")) { //$NON-NLS-1$
	    DialogTVDOTCOM dialogIMDB = new DialogTVDOTCOM(this);
	    //dialogIMDB.setVisible(true);
	    ShowGUI.show(dialogIMDB, true);
	} else {
	    DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogMovieInfo.alert.title.alert"),Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
	    //alert.setVisible(true);
	    ShowGUI.showAndWait(alert, true);
	}
    }
    
    /*Used when multiadding*/
    protected File getMultiAddFile() {
	return multiAddFile;
    }
    
    /*Used when multiadding*/
    public void setMultiAddFile(File multiAddFile) {
	this.multiAddFile = multiAddFile;
    }
    
    
    protected void setHasReadProperties(boolean hasReadProperties) {
	_hasReadProperties = hasReadProperties;
    }
    

    class FileTransferHandler extends TransferHandler {
	
    
    
	public int getSourceActions(JComponent c) {
	    return TransferHandler.COPY;
	}
    
	public boolean canImport(JComponent comp, DataFlavor flavor[]) {
	    if (!(comp instanceof JPanel)) {
		return false;
	    }
	    for (int i = 0, n = flavor.length; i < n; i++) {
		for (int j = 0, m = flavors.length; j < m; j++) {
		    if (flavor[i].equals(flavors[j])) {
			return true;
		    }
		}
	    }
	    return false;
	}
    
	public boolean importData(JComponent comp, Transferable t) {
	    
	    if (comp instanceof JPanel) {
		
		if (t.isDataFlavorSupported(flavors[0])) {
		    try {
			java.util.List list = (java.util.List)t.getTransferData(flavors[0]);
			
			executeCommandGetFileInfo((File []) list.toArray());
			
		    } catch (UnsupportedFlavorException ignored) {
			
		    } catch (java.io.IOException ignored) {
		    
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	    return false;
	}
    }
}

