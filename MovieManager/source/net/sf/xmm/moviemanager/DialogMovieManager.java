/**
 * @(#)DialogMovieManager.java 1.0 10.10.06 (dd.mm.yy)
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

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import org.apache.log4j.*;
import org.dotuseful.ui.tree.*;
import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.swing.extentions.*;
import net.sf.xmm.moviemanager.util.*;


public class DialogMovieManager extends JFrame implements ComponentListener {
    
    public static Logger log = Logger.getRootLogger();
        
    public static MovieManagerConfig config = MovieManager.getConfig();
    
    public JScrollPane movieListScrollPane;
    public JPanel filterPanel;
    public JPanel moviesList;
    public ExtendedToolBar toolBar;
            
    /*Number of entries in the list*/
    private int entries;
    
    public int fontSize = 12;
    
    private int movieListWidth = 0;
    
    private JLabel showEntries;

	private JTextPane textAreaMiscellaenous;
	private JTextArea textAreaPlot;
	private JTextArea textAreaCast;
    
    public static JApplet applet = null;

    public DialogMovieManager() {}
    
    public DialogMovieManager(Object applet) {
        DialogMovieManager.applet = (JApplet) applet;
    }
    
    public static boolean isApplet() {
        return applet != null ? true : false;
    }
    
    public static void destroy() {
        
        if (applet != null)
            applet.destroy();
    }
    
    public static void setDefaultLookAndFeelDecorated(boolean enable) {
    	 JFrame.setDefaultLookAndFeelDecorated(enable);
         JDialog.setDefaultLookAndFeelDecorated(enable);
	}

    /**
     * Setup the main MovieManager object.
     **/
    protected void setUp() {
        
        try {
            if (!MovieManager.isApplet()) {
                
                
                /* Gets the working dir... */
                String directory = FileUtil.getUserDir();
                File laf = new File(directory + "LookAndFeels" + File.separator + "lookAndFeels.ini");
                
                if (!laf.exists() && !MovieManager.isMacAppBundle()) {
                    new File(directory + "LookAndFeels").mkdirs();
                    
                    String text = "Here you can add new Look and Feels." + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "Make sure the 'look and Feel' jar file is placed in the 'LookAndFeels' directory" + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "and that the correct classname is given below." + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "Both the name and classname must be enclosed in quotes." + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "The names may be set to whatever fit your needs." + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "Example:" + FileUtil.getLineSeparator()+ FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "\"Metal look and feel\"       \"javax.swing.plaf.metal.MetalLookAndFeel\"" + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "\"Windows look and feel\"     \"com.sun.java.swing.plaf.windows.WindowsLookAndFeel\"" + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    FileUtil.getLineSeparator()+ "The metal and windows look and feels are preinstalled." + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "Define the look and feels below:" + FileUtil.getLineSeparator()+ //$NON-NLS-1$
                    "#" + FileUtil.getLineSeparator(); //$NON-NLS-1$
                    
                    
                    /* Creating the texfile */
                    PrintWriter pwriter = new PrintWriter(new FileWriter(laf), true); //$NON-NLS-1$ //$NON-NLS-2$
                    
                    /* Writes the lookAndFeels.ini textfile. */
                    for (int i=0; i < text.length(); i++) {
                        pwriter.write(text.charAt(i));
                    }
                    pwriter.close();
                }
            }
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
        }
        
        /* Starts other inits. */
        log.debug("Start setting up the MovieManager."); //$NON-NLS-1$
        
        LookAndFeelManager.setLookAndFeel();
        
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        
        if (!MovieManager.isApplet())
            System.setProperty("sun.awt.noerasebackground", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        
        setTitle(" MeD's Movie Manager v" + MovieManager.getVersion()); //$NON-NLS-1$
        
        setIconImage(FileUtil.getImage("/images/film.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH)); //$NON-NLS-1$
        
        setJMenuBar(createMenuBar());
        getContentPane().add(createWorkingArea(),BorderLayout.CENTER);
        
        setResizable(true);
        
        /* Hides database related components. */
        setDatabaseComponentsEnable(false);
        
        updateJTreeIcons();
        
        addComponentListener(this);
        
        /* All done, pack. */
        pack();
        toolBar.updateToolButtonBorder();
        
        setSize(MovieManager.getConfig().mainSize);
        
        if (config.getMainMaximized())
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = config.getScreenLocation();
        
        if (location != null && location.getX() < screenSize.getWidth() && location.getY() < screenSize.getHeight())
            setLocation(location);
        else {
            setLocation((int)(screenSize.getWidth() - getSize().getWidth())/2,
                    (int)(screenSize.getHeight() - getSize().getHeight())/2 - 12);
        }
        
        
        /* Setting Additional Info / Notes slider position */
        if (config.additionalInfoNotesSliderPosition == -1) {
            getAdditionalInfoNotesSplitPane().setDividerLocation(0.5);
            getAdditionalInfoNotesSplitPane().setLastDividerLocation(getAdditionalInfoNotesSplitPane().getDividerLocation());
        }
        else {
            getAdditionalInfoNotesSplitPane().setDividerLocation(config.additionalInfoNotesSliderPosition);
            
            if (config.additionalInfoNotesLastSliderPosition != -1)
                getAdditionalInfoNotesSplitPane().setLastDividerLocation(config.additionalInfoNotesLastSliderPosition);
        }
        
        
        /* Setting Movie Info slider position */
        if (config.movieInfoSliderPosition == -1) {
            getMovieInfoSplitPane().setDividerLocation(0.6);
            getMovieInfoSplitPane().setLastDividerLocation(getMovieInfoSplitPane().getDividerLocation());
        }
        else {
            getMovieInfoSplitPane().setDividerLocation(config.movieInfoSliderPosition);
            
            if (config.movieInfoLastSliderPosition != -1)
                getMovieInfoSplitPane().setLastDividerLocation(config.movieInfoLastSliderPosition);
        }
        
        setVisible(true);
        log.debug("MovieManager SetUp done!"); //$NON-NLS-1$
    }
    
   
    
    public DefaultTreeModel createTreeModel(DefaultListModel movieList, ArrayList episodes) {
        
        Object[] movies = movieList.toArray();
        
        ExtendedTreeNode root = new ExtendedTreeNode(new ModelMovie(-1, null, null, null, "Loading Database", null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null)); //$NON-NLS-1$
        
        DefaultTreeModel model = new AutomatedTreeModel(root, false);
        
        ExtendedTreeNode temp, temp2;
        int tempKey = 0;
        
        for (int i = 0; i < movies.length; i++) {
            
            temp = new ExtendedTreeNode((ModelEntry) movies[i]);
            tempKey = ((ModelEntry) movies[i]).getKey();
            
            /* Adding episodes */
            for (int u = 0; u < episodes.size(); u++) {
                
                if (tempKey == ((ModelEpisode) episodes.get(u)).getMovieKey()) {
                    
                    temp2 = new ExtendedTreeNode((ModelEntry) episodes.get(u));
                    temp.add(temp2);
                    
                    episodes.remove(u);
                    u--;
                }
            }
            
            root.add(temp);
        }
        return model;
    }
    
    
    
    
    public void setListTitle(String title) {
        
        JPanel moviesList = getPanelMovieList();
        moviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " "+ Localizer.getString("moviemanager.listpanel-title") + " - " + title , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(moviesList.getFont().getName(),Font.BOLD, fontSize)),
                BorderFactory.createEmptyBorder(0,5,5,5)));
    }
    
    
    
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    
    public void componentResized(ComponentEvent e) {
    	
    	movieListWidth = (int) getMoviesList().getSize().getWidth();
        
        /* Maximized */
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            config.setMainMaximized(true);
        }
        else {
            config.setMainSize(getMainSize());
            config.setMainMaximized(false);
        }
    }
    
    public void componentMoved(ComponentEvent e) {
        config.setScreenLocation(getLocationOnScreen());
    }
    
    
    /**
     * Sets enabled/disabled the related database components.
     **/
    public void setDatabaseComponentsEnable(boolean enable) {
        /* Close database MenuItem. */
        getJMenuBar().getMenu(0).getItem(3).setEnabled(enable);
        /* Import MenuItem. */
        getJMenuBar().getMenu(0).getItem(5).setEnabled(enable);
        /* Export MenuItem. */
        getJMenuBar().getMenu(0).getItem(7).setEnabled(enable);
        /* Database Menu. */
        getJMenuBar().getMenu(1).setEnabled(enable);
        /* Queries MenuItem. */
        getJMenuBar().getMenu(1).getItem(0).setEnabled(enable);
        /* Folders MenuItem. */
        getJMenuBar().getMenu(1).getItem(2).setEnabled(enable);
        /* Additional Info Fields MenuItem. */
        getJMenuBar().getMenu(1).getItem(3).setEnabled(enable);
        /* Convert Database MenuItem*/
        getJMenuBar().getMenu(1).getItem(4).setEnabled(enable);
        /* Add multiple movies MenuItem*/
        getJMenuBar().getMenu(2).getItem(2).setEnabled(enable);
        /* Update IMDb info */
        getJMenuBar().getMenu(2).getItem(3).setEnabled(enable);
        /* Report generator */
        getJMenuBar().getMenu(2).getItem(5).setEnabled(enable);
        /* Lists*/
        getJMenuBar().getMenu(3).setEnabled(enable);
                
        toolBar.setEnableButtons(enable);
        
        /* The JTree. */
        getMoviesList().setEnabled(enable);
        
        /* Filter textField. */
        getFilter().setEnabled(enable);
        
        /* Makes the list selected. */
        getMoviesList().requestFocus(true);
    }
    
    
    /**
     * Finalizes this object (closes the out streams and disposes).
     **/
    public void finalize() {
        
        /* Disposes. */
        dispose();
    }
    
    public void updateLookAndFeelValues() {
        toolBar.updateToolButtonBorder();
        updateJTreeIcons();
    }
    
    public void updateJTreeIcons() {
        getMoviesList().setRowHeight(config.getMovieListRowHeight() + 2);
        
        /* Show handles in cover mode or no icon mode, otherwise it's hard to recognize series. */
        getMoviesList().setShowsRootHandles(config.getUseJTreeCovers() || !config.getUseJTreeIcons());
    }
    
    
    
    
    
    /* mode = 0 (invert), 1 (all to seen), 2(all to unseen). */
    public void updateSeen(int mode) {
        
        JTree movieList = MovieManager.getDialog().getMoviesList();
        
        if (movieList.getLastSelectedPathComponent() == null)
            return;
        
        TreePath [] selectionPaths = movieList.getSelectionPaths();
        
        /* The currently visible entry */
        ModelEntry selected = (ModelEntry) ((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject();
        
        if (selected.getKey() == -1)
            return;
        
        /* Should only be one entry when inverting (Pusing the seen label/image)*/
        if (mode == 0)
            selectionPaths = new TreePath[] {movieList.getLeadSelectionPath()};
        
        Database db = MovieManager.getIt().getDatabase();
        boolean seen;
        int key;
        ModelEntry model;
        
        for (int i = 0; i < selectionPaths.length; i++) {
            
            model = (ModelEntry) ((DefaultMutableTreeNode) selectionPaths[i].getLastPathComponent()).getUserObject();
            
            key = model.getKey();
            seen = model.getSeen();
            
            if (mode == 0 || (seen && mode == 2) || !seen && mode == 1) {
                
                if (model instanceof ModelMovie)
                    db.setSeen(key, !seen);
                else
                    db.setSeenEpisode(key, !seen);
                
                model.setSeen(!seen);
                
                getSeen().setSelected(!seen);
            }
        }
    }
    
    public void loadMenuLists(Database database) {
        
        if (database != null) {
            
            String currentList = config.getCurrentList();
            
            ArrayList listColumns = database.getListsColumnNames();
            JRadioButtonMenuItem menuItem;
            
            JMenu menuLists = getListMenu();
            menuLists.removeAll();
            
            ButtonGroup group = new ButtonGroup();
            int indexCounter = 0;
            
            while (!listColumns.isEmpty()) {
                
                menuItem = new JRadioButtonMenuItem((String) listColumns.get(0));
                menuItem.setActionCommand((String) listColumns.get(0));
                menuItem.addActionListener(new MovieManagerCommandLoadList());
                group.add(menuItem);
                menuLists.add(menuItem, indexCounter);
                
                if (currentList.equals(listColumns.get(0)))
                    menuItem.setSelected(true);
                
                listColumns.remove(0);
                indexCounter++;
            }
            
            /* Adds 'Show all' in the list */
            menuItem = new JRadioButtonMenuItem("Show All", true); //$NON-NLS-1$
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            menuItem.setActionCommand("Show All"); //$NON-NLS-1$
            menuItem.addActionListener(new MovieManagerCommandLoadList());
            group.add(menuItem);
            
            menuLists.add(menuItem, indexCounter);
        }
    }
    
        
    /* Returns number of entries currently shown in the list */
    int getEntries() {
        return entries;
    }
    
    public void setAndShowEntries() {
        setAndShowEntries(MovieManager.getDialog().getMoviesList().getModel().getChildCount(MovieManager.getDialog().getMoviesList().getModel().getRoot()));
    }
    
    /**
     * Sets the entries variable and uppdates the showEntries Label with the new number
     **/
    public void setAndShowEntries(int entries) {
        
        this.entries = entries;
        
        String value;;
        
        if (entries < 10)
            value = "    "; //$NON-NLS-1$
        else if (entries < 100)
            value = "  "; //$NON-NLS-1$
        else
            value = " "; //$NON-NLS-1$
        
        if (entries != -1) {
            value += String.valueOf(entries);
        }
        
        showEntries.setText(value);
        showEntries.updateUI();
    }
        
    public int getFontSize() {
        return fontSize;
    }
    
    void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    public int getMovieListWidth() {
        return movieListWidth;
    }
       
    public Dimension getMainSize() {
        return this.getSize();
    }
    
    
    
    
    
    
    
    
    // Below is GUI creation code
    
    
    
    
    
    /**
     * Creates the menuBar.
     *
     * @return The menubar.
     **/
    protected JMenuBar createMenuBar() {
        log.debug("Start creation of the MenuBar."); //$NON-NLS-1$
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(2,0,8,0));
        /* Creation of the file menu. */
        menuBar.add(createMenuFile());
        /* Creation of the database menu. */
        menuBar.add(createMenuDatabase());
        /* Creation of the options menu. */
        menuBar.add(createMenuTools());
        
        /* Creation of the lists. */
        menuBar.add(createMenuLists());
        
        /* Creation of the help menu. */
        menuBar.add(createMenuHelp());
        log.debug("Creation of the MenuBar done."); //$NON-NLS-1$
        return menuBar;
    }
    
    /**
     * Creates the file menu.
     *
     * @return The file menu.
     **/
    protected JMenu createMenuFile() {
        log.debug("Start creation of the File menu."); //$NON-NLS-1$
        JMenu menuFile = new JMenu(Localizer.getString("moviemanager.menu.file")); //$NON-NLS-1$
        menuFile.setMnemonic('F');
        
        /* MenuItem New. */
        JMenuItem menuItemNew = new JMenuItem(Localizer.getString("moviemanager.menu.file.newdb"),'N'); //$NON-NLS-1$
        menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemNew.setActionCommand("New"); //$NON-NLS-1$
        menuItemNew.addActionListener(new MovieManagerCommandNew());
        menuFile.add(menuItemNew);
        
        /* MenuItem Open. */
        JMenuItem menuItemOpen = new JMenuItem(Localizer.getString("moviemanager.menu.file.opendb"),'O'); //$NON-NLS-1$
        menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemOpen.setActionCommand("Open"); //$NON-NLS-1$
        menuItemOpen.addActionListener(new MovieManagerCommandOpen());
        menuFile.add(menuItemOpen);
        /* A separator. */
        menuFile.addSeparator();
        
        /* MenuItem Close. */
        JMenuItem menuItemClose = new JMenuItem(Localizer.getString("moviemanager.menu.file.closedb"),'C'); //$NON-NLS-1$
        menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        menuItemClose.setActionCommand("Open"); //$NON-NLS-1$
        menuItemClose.addActionListener(new MovieManagerCommandCloseDatabase());
        menuFile.add(menuItemClose);
        /* A separator. */
        menuFile.addSeparator();
        
        /* The Import menuItem. */
        JMenuItem menuImport = new JMenuItem(Localizer.getString("moviemanager.menu.file.import"),'I'); //$NON-NLS-1$
        menuImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        menuImport.addActionListener(new MovieManagerCommandImport());
        /* Adds MenuItem Import. */
        menuFile.add(menuImport);
        /* A separator. */
        menuFile.addSeparator();
        
        /* The Export menuItem. */
        JMenuItem menuExport = new JMenuItem(Localizer.getString("moviemanager.menu.file.export"),'E'); //$NON-NLS-1$
        menuExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuExport.addActionListener(new MovieManagerCommandExport());
        /* Adds menuItem Export. */
        menuFile.add(menuExport);
        /* A separator. */
        menuFile.addSeparator();
        
        /* MenuItem Exit. */
        JMenuItem menuItemExit = new JMenuItem(Localizer.getString("moviemanager.menu.file.exit"),'X'); //$NON-NLS-1$
        menuItemExit.setActionCommand("Exit"); //$NON-NLS-1$
        menuItemExit.addActionListener(new MovieManagerCommandExit());
        menuFile.add(menuItemExit);
        /* All done. */
        log.debug("Creation of the File menu done."); //$NON-NLS-1$
        return menuFile;
    }
    
    /**
     * Creates the database menu.
     *
     * @return The database menu.
     **/
    protected JMenu createMenuDatabase() {
        log.debug("Start creation of the Database menu."); //$NON-NLS-1$
        JMenu menuDatabase = new JMenu(Localizer.getString("moviemanager.menu.database")); //$NON-NLS-1$
        menuDatabase.setMnemonic('D');
        
        /* MenuItem Queries. */
        JMenuItem menuItemQueries = new JMenuItem(Localizer.getString("moviemanager.menu.database.queries"),'Q'); //$NON-NLS-1$
        menuItemQueries.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        menuItemQueries.setActionCommand("Queries"); //$NON-NLS-1$
        menuItemQueries.addActionListener(new MovieManagerCommandQueries());
        menuDatabase.add(menuItemQueries);
        
        /* A separator. */
        menuDatabase.addSeparator();
        
        /* MenuItem Folders. */
        JMenuItem menuItemFolders = new JMenuItem(Localizer.getString("moviemanager.menu.database.folders"),'F'); //$NON-NLS-1$
        menuItemFolders.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemFolders.setActionCommand("Folders"); //$NON-NLS-1$
        menuItemFolders.addActionListener(new MovieManagerCommandFolders());
        menuDatabase.add(menuItemFolders);
        
        /* MenuItem AddField. */
        JMenuItem menuItemAddField = new JMenuItem(Localizer.getString("moviemanager.menu.database.additionalinfofields"),'I'); //$NON-NLS-1$
        menuItemAddField.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, (java.awt.event.InputEvent.ALT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        menuItemAddField.setActionCommand("AdditionalInfoFields"); //$NON-NLS-1$
        menuItemAddField.addActionListener(new MovieManagerCommandAdditionalInfoFields());
        menuDatabase.add(menuItemAddField);
        
        /* MenuItem AddList. */
        JMenuItem menuItemAddList = new JMenuItem(Localizer.getString("moviemanager.menu.database.lists"),'L'); //$NON-NLS-1$
        menuItemAddList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemAddList.setActionCommand("setLists"); //$NON-NLS-1$
        menuItemAddList.addActionListener(new MovieManagerCommandLists(this));
        menuDatabase.add(menuItemAddList);
        
        /* MenuItem Convert Database. */
        JMenuItem convertDatabase = new JMenuItem(Localizer.getString("moviemanager.menu.database.covertdb")); //$NON-NLS-1$
        convertDatabase.setActionCommand("Convert Database"); //$NON-NLS-1$
        convertDatabase.addActionListener(new MovieManagerCommandConvertDatabase());
        menuDatabase.add(convertDatabase);
        
        /* MenuItem Save changed notes. */
        JMenuItem saveNotes = new JMenuItem(Localizer.getString("moviemanager.menu.database.savechanhednotes"),'S'); //$NON-NLS-1$
        saveNotes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveNotes.setActionCommand("Save changed notes"); //$NON-NLS-1$
        saveNotes.addActionListener(new MovieManagerCommandSaveChangedNotes());
        menuDatabase.add(saveNotes);
        
        /* All done. */
        log.debug("Creation of the Database menu done."); //$NON-NLS-1$
        return menuDatabase;
    }
    
    /**
     * Creates the tools menu.
     *
     * @return The tools menu.
     **/
    protected JMenu createMenuTools() {
        log.debug("Start creation of the Tools menu."); //$NON-NLS-1$
        JMenu menuTools = new JMenu(Localizer.getString("moviemanager.menu.tools")); //$NON-NLS-1$
        menuTools.setMnemonic('T');
        
        /* MenuItem Preferences.
         For some reason, addMovie KeyEvent.VK_A doesn't work when focused
         on the selected movie or the filter*/
        
        JMenuItem menuItemPrefs = new JMenuItem(Localizer.getString("moviemanager.menu.tools.preferences"),'P'); //$NON-NLS-1$
        menuItemPrefs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemPrefs.setActionCommand("Preferences"); //$NON-NLS-1$
        menuItemPrefs.addActionListener(new MovieManagerCommandPrefs());
        menuTools.add(menuItemPrefs);
        
        menuTools.addSeparator();
        JMenuItem addMultipleMovies = new JMenuItem(Localizer.getString("moviemanager.menu.tools.addmultiplemovies"),'M'); //$NON-NLS-1$
        addMultipleMovies.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addMultipleMovies.setActionCommand("Add Multiple Movies"); //$NON-NLS-1$
        addMultipleMovies.addActionListener(new MovieManagerCommandAddMultipleMoviesByFile());
        menuTools.add(addMultipleMovies);
        
        JMenuItem updateIMDbInfo = new JMenuItem(Localizer.getString("moviemanager.menu.tools.updateIMDbInfo"),'U'); //$NON-NLS-1$
        updateIMDbInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        updateIMDbInfo.setActionCommand("Update IMDb Info"); //$NON-NLS-1$
        updateIMDbInfo.addActionListener(new MovieManagerCommandUpdateIMDBInfo());
        menuTools.add(updateIMDbInfo);
        
        menuTools.addSeparator();
        
        JMenuItem reportGenerator = new JMenuItem(Localizer.getString("moviemanager.menu.tools.reportgenerator"),'R'); //$NON-NLS-1$
        reportGenerator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        reportGenerator.setActionCommand("Report Generator"); //$NON-NLS-1$
        reportGenerator.addActionListener(new MovieManagerCommandReportGenerator());
        menuTools.add(reportGenerator);
        
        /* All done. */
        log.debug("Creation of the Tools menu done."); //$NON-NLS-1$
        return menuTools;
    }
    
    /**
     * Creates the tools menu.
     *
     * @return The tools menu.
     **/
    protected JMenu createMenuLists() {
        log.debug("Start creation of the Lists menu."); //$NON-NLS-1$
        JMenu menuLists = new JMenu(Localizer.getString("moviemanager.menu.lists")); //$NON-NLS-1$
        menuLists.setMnemonic('L');
        
        log.debug("Creation of the Lists menu done."); //$NON-NLS-1$
        return menuLists;
    }
    
    /**
     * Creates the help menu.
     *
     * @return The help menu.
     **/
    protected JMenu createMenuHelp() {
        log.debug("Start creation of the Help menu."); //$NON-NLS-1$
        JMenu menuHelp = new JMenu(Localizer.getString("moviemanager.menu.help")); //$NON-NLS-1$
        menuHelp.setMnemonic('H');
        /* MenuItem Help. */
        JMenuItem menuItemHelp = new JMenuItem(Localizer.getString("moviemanager.menu.help.help"),'H'); //$NON-NLS-1$
        menuItemHelp.setAccelerator(KeyStroke.getKeyStroke("F1")); //$NON-NLS-1$
        menuItemHelp.setActionCommand("Help"); //$NON-NLS-1$
        menuItemHelp.addActionListener(new MovieManagerCommandHelp());
        menuHelp.add(menuItemHelp);
        /* MenuItem Online Help. */
        JMenuItem menuItemOnlineHelp = new JMenuItem(Localizer.getString("moviemanager.menu.help.onlinehelp"),'O'); //$NON-NLS-1$
        menuItemOnlineHelp.setActionCommand("OpenPage (Online Help)"); //$NON-NLS-1$
        menuItemOnlineHelp.addActionListener(new MovieManagerCommandOpenPage("http://xmm.sourceforge.net/help.html")); //$NON-NLS-1$
        menuHelp.add(menuItemOnlineHelp);
        /* A Separator. */
        menuHelp.addSeparator();
        /* MenuItem HomePage. */
        JMenuItem menuItemHomePage = new JMenuItem(Localizer.getString("moviemanager.menu.help.homepage"),'P'); //$NON-NLS-1$
        menuItemHomePage.setActionCommand("OpenPage (Home Page)"); //$NON-NLS-1$
        menuItemHomePage.addActionListener(new MovieManagerCommandOpenPage("http://xmm.sourceforge.net/")); //$NON-NLS-1$
        menuHelp.add(menuItemHomePage);
        /* A Separator. */
        menuHelp.addSeparator();
        /* MenuItem SourceForge. */
        JMenuItem menuItemSourceForge = new JMenuItem(Localizer.getString("moviemanager.menu.help.sourceforgepage"),'S'); //$NON-NLS-1$
        menuItemSourceForge.setActionCommand("OpenPage (SourceForge.net)"); //$NON-NLS-1$
        menuItemSourceForge.addActionListener(new MovieManagerCommandOpenPage("http://sourceforge.net/projects/xmm/")); //$NON-NLS-1$
        menuHelp.add(menuItemSourceForge);
        /* A Separator. */
        menuHelp.addSeparator();
        /* MenuItem About. */
        JMenuItem menuItemAbout = new JMenuItem(Localizer.getString("moviemanager.menu.help.about")); //$NON-NLS-1$
        menuItemAbout.setActionCommand("About"); //$NON-NLS-1$
        menuItemAbout.addActionListener(new MovieManagerCommandAbout());
        menuHelp.add(menuItemAbout);
        /* All done. */
        log.debug("Creation of the Help menu done."); //$NON-NLS-1$
        return menuHelp;
    }
    
    /**
     * Creates the working area.
     *
     * @return JPanel with working area.
     **/
    protected JPanel createWorkingArea() {
        log.debug("Start creation of the WorkingArea."); //$NON-NLS-1$
        JPanel workingArea = new JPanel();
        
        /* The minimum size of the main window is honoured only when using L&F border (title bar decorated) 
         Makes sure the minimum size is small so that it's not fixed at a too big default value.*/
        workingArea.setMinimumSize(new Dimension(100, 100));
        
        workingArea.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
       
        double size[][] = {{0.33, info.clearthought.layout.TableLayout.FILL}, {info.clearthought.layout.TableLayout.FILL}};
        
        workingArea.setLayout(new info.clearthought.layout.TableLayout(size));
        
        /* Creates the Movies List Panel. */
        workingArea.add(createMoviesList(), "0, 0"); //$NON-NLS-1$
        
        /* Creates the Movie Info Panel.*/
        workingArea.add(createMovieInfo(), "1, 0"); //$NON-NLS-1$
        
        /* All done. */
        log.debug("Creation of the WorkingArea done."); //$NON-NLS-1$
        return workingArea;
    }
    
    /**
     * Creates the Movies List Panel.
     *
     * @return The Movies List Panel.
     **/
    protected JPanel createMoviesList() {
        
        if (getContentPane().getFont() == null) {
            getContentPane().setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
        }
        log.debug("Start creation of the Movies List panel."); //$NON-NLS-1$
        
        moviesList = new JPanel(new GridBagLayout());
        moviesList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " "+ Localizer.getString("moviemanager.listpanel-title") + config.getCurrentList() + " ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(moviesList.getFont().getName(),Font.BOLD, fontSize)),
                BorderFactory.createEmptyBorder(0,5,5,5)));
        
        
        GridBagConstraints constraints;
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 2;
        constraints.weighty = 0;
         constraints.insets = new Insets(2,0,4,0);
        
        toolBar = createToolBar();
        
        /* Adds the toolbar.*/
        moviesList.add(toolBar, constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 3;
        constraints.weightx = 0;
        constraints.weighty = 1;
        constraints.insets = new Insets(0,0,0,0);
        constraints.fill = GridBagConstraints.BOTH;
        
        movieListScrollPane = createList();
        
        /* Adds the list. */
        moviesList.add(movieListScrollPane, constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.insets = new Insets(0,0,0,0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        filterPanel = createFilter();
        
        /* Adds the filter. */
        moviesList.add(filterPanel, constraints);
        
        /* All done. */
        log.debug("Creation of the Movies List panel done."); //$NON-NLS-1$
        return moviesList;
    }
    
  
    
    /**
     * Creates the toolbar.
     *
     * @return The toolbar.
     **/
    protected ExtendedToolBar createToolBar() {
        log.debug("Start creation of the ToolBar."); //$NON-NLS-1$
        
        ExtendedToolBar toolBar = new ExtendedToolBar(SwingConstants.HORIZONTAL);
        
        showEntries = toolBar.showEntries;
        
        /* All done. */
        log.debug("Creation of the ToolBar done."); //$NON-NLS-1$
        
        return toolBar;
    }
    
    /**
     * Creates the list of movies.
     *
     * @return The listofmovies.
     **/
    protected JScrollPane createList() {
        log.debug("Start creation of the List."); //$NON-NLS-1$
        
        ExtendedJTree tree = new ExtendedJTree();
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(new ModelMovie(-1, null, null, null, "", null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null)))); //$NON-NLS-1$
        
        tree.setRootVisible(false);
        tree.setDragEnabled(false);
        
        tree.setFont(new Font(tree.getFont().getName(),Font.PLAIN,fontSize));
        
        MovieManagerCommandSelect listener = new MovieManagerCommandSelect();
        
        /* Adding listeners to the movie list */
        tree.addTreeSelectionListener(listener);
        tree.addMouseListener(listener);
        tree.addKeyListener(listener);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tree);
        tree.setCellRenderer(new ExtendedTreeCellRenderer(MovieManager.getDialog(), scrollPane));
        
        /* All done. */
        log.debug("Creation of the List done."); //$NON-NLS-1$
        return scrollPane;
    }
    
    /**
     * Creates a filter to act over the list of movies.
     *
     * @return The filter.
     **/
    
    protected JPanel createFilter() {
        
        log.debug("Start creation of the Filter."); //$NON-NLS-1$
        JPanel filter = new JPanel(new BorderLayout());
        filter.setBorder(BorderFactory.createEmptyBorder(10,4,4,4));
        
        /* Adds the Label. */
        JLabel label = new JLabel(Localizer.getString("moviemanager.listpanel-filter")); //$NON-NLS-1$
        label.setFont(new Font(label.getFont().getName(),Font.PLAIN,fontSize));
        filter.add(label, BorderLayout.WEST);
        
        /* Adds the TextField. */
        JTextField textField = new JTextField();
        textField.setFont(new Font("", Font.PLAIN, 12)); //$NON-NLS-1$
        textField.setActionCommand("Filter"); //$NON-NLS-1$
        textField.addActionListener(new MovieManagerCommandFilter("", null, true, true)); //$NON-NLS-1$
        filter.add(textField, BorderLayout.CENTER);
        
        /* All done. */
        log.debug("Creation of the Filter done."); //$NON-NLS-1$
        
        filter.setSize(255, 100);
        return filter;
    }
    
    /**
     * Creates the Movie Info Panel.
     *
     * @return The Movie Info Panel.
     **/
    protected JPanel createMovieInfo() {
        log.debug("Start creation of the Movie Info panel."); //$NON-NLS-1$
        JPanel movieInfo = new JPanel();
        movieInfo.addComponentListener(this);
        
        JPanel generalInfoPanel = createGeneralInfo();
        
        generalInfoPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        
        double size[][] = {{info.clearthought.layout.TableLayout.FILL}, {generalInfoPanel.getPreferredSize().getHeight() + 20, info.clearthought.layout.TableLayout.FILL}};
        
        movieInfo.setLayout(new info.clearthought.layout.TableLayout(size));
        movieInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("moviemanager.movieinfopanel.title"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(movieInfo.getFont().getName(),Font.BOLD, fontSize)),
                BorderFactory.createEmptyBorder(0,5,5,5)));
        
        /* Adds the general info. */
        GridBagConstraints constraints;
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.insets = new Insets(0,0,0,0);
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        movieInfo.add(generalInfoPanel, "0, 0"); //$NON-NLS-1$
        
        JPanel miscellaneous = createMiscellaneous();
        
        JPanel plotAndCast = new JPanel();
        
        plotAndCast.setLayout(new GridLayout(2,1));
        
        plotAndCast.add(createPlot());
        plotAndCast.add(createCast());
        
        JTabbedPane all = new JTabbedPane();
        all.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        all.add(Localizer.getString("moviemanager.movie-info-panel.plot_and_cast"), plotAndCast); //$NON-NLS-1$
        all.add(Localizer.getString("moviemanager.movie-info-panel.miscellaneous"), miscellaneous); //$NON-NLS-1$
        
        JPanel tabbedPanel = new JPanel(new BorderLayout());
        tabbedPanel.add(all, BorderLayout.CENTER);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(0,0,0,0);
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.fill = GridBagConstraints.BOTH;
        
        /* Adds the additional info and notes. */
        
        /* All done. */
        log.debug("Creation of the Movie Info panel done."); //$NON-NLS-1$
        
        /* Removing the border of the splitpane */
        UIManager.put("SplitPane.border", new javax.swing.plaf.BorderUIResource(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0))); //$NON-NLS-1$
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tabbedPanel, createAdditionalInfoAndNotes());
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(12);
        splitPane.setResizeWeight(0.5);
        
        movieInfo.add(splitPane, "0, 1"); //$NON-NLS-1$
        
        return movieInfo;
    }
    
    /**
     * Creates a JPanel for display the general info.
     *
     * @return The JPanel.
     **/
    
    protected JPanel createGeneralInfo() {
        log.debug("Start creation of the General Info panel."); //$NON-NLS-1$
        
        JPanel panelGeneralInfo = new JPanel();
        panelGeneralInfo.setLayout(new GridBagLayout());
        
        GridBagConstraints constraints;
        
        JPanel panelColour = new JPanel();
        panelColour.setLayout(new BoxLayout(panelColour, BoxLayout.X_AXIS));
        
        JLabel colourID = new JLabel(""); //$NON-NLS-1$
        colourID.setFont(new Font(colourID.getFont().getName(), Font.BOLD, fontSize));
        panelColour.add(colourID);
        
        JLabel colour = new JLabel(" "); //$NON-NLS-1$
        colour.setFont(new Font(colour.getFont().getName(), Font.PLAIN, fontSize));
        panelColour.add(colour);
        
         
        /* Adds the subInfo JPanel. */
        JPanel panelDateAndTitle = new JPanel();
        
        panelDateAndTitle.setLayout(new BorderLayout());
        
        JTextField date = new JTextField();
        date.setFont(new Font(date.getFont().getName(), Font.BOLD, fontSize +3));
        date.setBorder(null);
        date.setOpaque(false);
        date.setEditable(false);
        
        panelDateAndTitle.add(date, BorderLayout.WEST);
        
        
        JTextField title = new JTextField();
        title.setFont(new Font("Dialog", Font.BOLD, fontSize +3)); //$NON-NLS-1$
        title.setBorder(null);
        title.setOpaque(false);
        title.setEditable(false);
        
        panelDateAndTitle.add(title, BorderLayout.CENTER);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.insets = new Insets(0,0,10,0);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        panelGeneralInfo.add(panelDateAndTitle, constraints);
        
        
        JPanel panelDirected = new JPanel();
        panelDirected.setLayout(new BoxLayout(panelDirected, BoxLayout.X_AXIS));
        
        JLabel directedID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.directedby") + ": "); //$NON-NLS-1$
        directedID.setFont(new Font(directedID.getFont().getName(), Font.BOLD, fontSize));
        panelDirected.add(directedID);
        
        JTextField directed = new JTextField();
        directed.setFont(new Font(directed.getFont().getName(), Font.PLAIN, fontSize));
        directed.setBorder(null);
        directed.setOpaque(false);
        directed.setEditable(false);
        
        panelDirected.add(directed);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.insets = new Insets(0,0,0,5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        panelGeneralInfo.add(panelDirected, constraints);
        
        JPanel panelWritten = new JPanel();
        panelWritten.setLayout(new BoxLayout(panelWritten, BoxLayout.X_AXIS));
        
        JLabel writtenID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.writtenby") + ": "); //$NON-NLS-1$
        writtenID.setFont(new Font(writtenID.getFont().getName(), Font.BOLD, fontSize));
        panelWritten.add(writtenID);
        
        JTextField written = new JTextField();
        written.setFont(new Font(written.getFont().getName(), Font.PLAIN, fontSize));
        written.setBorder(null);
        written.setOpaque(false);
        written.setEditable(false);
        
        panelWritten.add(written);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 0;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.insets = new Insets(0,0,4,0);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        panelGeneralInfo.add(panelWritten, constraints);
        
        JPanel panelGenre = new JPanel();
        panelGenre.setLayout(new BoxLayout(panelGenre, BoxLayout.X_AXIS));
        
        JLabel genreID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.genre") + ": "); //$NON-NLS-1$
        genreID.setFont(new Font(genreID.getFont().getName(), Font.BOLD, fontSize));
        panelGenre.add(genreID);
        
        JTextField genre = new JTextField();
        genre.setFont(new Font(genre.getFont().getName(), Font.PLAIN, fontSize));
        genre.setBorder(null);
        genre.setOpaque(false);
        genre.setEditable(false);
        
        panelGenre.add(genre);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        constraints.insets = new Insets(4,0,2,0);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        panelGeneralInfo.add(panelGenre, constraints);
        
        JPanel panelRating = new JPanel();
        panelRating.setLayout(new BoxLayout(panelRating, BoxLayout.X_AXIS));
        
        
        
        JLabel ratingID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.raing") + ": "); //$NON-NLS-1$
        ratingID.setFont(new Font(ratingID.getFont().getName(), Font.BOLD, fontSize));
        panelRating.add(ratingID);
        
        JLabel rating = new JLabel();
        rating.setFont(new Font(rating.getFont().getName(), Font.PLAIN, fontSize));
        panelRating.add(rating);
        
        panelRating.add(rating);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(2,0,2,0);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        panelGeneralInfo.add(panelRating, constraints);
        
        JPanel panelCountry = new JPanel();
        panelCountry.setLayout(new BoxLayout(panelCountry, BoxLayout.X_AXIS));
        
        JLabel countryID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.country") + ": "); //$NON-NLS-1$
        countryID.setFont(new Font(countryID.getFont().getName(), Font.BOLD, fontSize));
        
        countryID.setMinimumSize(countryID.getPreferredSize());
        
        panelCountry.add(countryID);
        
        JTextField country = new JTextField();
        country.setFont(new Font(country.getFont().getName(), Font.PLAIN, fontSize));
        country.setBorder(null);
        country.setOpaque(false);
        country.setEditable(false);
        
        panelCountry.add(country);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 5;
        constraints.weightx = 6;
        constraints.weighty = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2,0,2,0);
        constraints.anchor = GridBagConstraints.WEST;
        
        panelGeneralInfo.add(panelCountry, constraints);
        
        JPanel panelSeen = new JPanel();
        panelSeen.setLayout(new BoxLayout(panelSeen, BoxLayout.X_AXIS));
        
        JLabel seenID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.seen") + ": "); //$NON-NLS-1$
        seenID.setFont(new Font(seenID.getFont().getName(), Font.BOLD, fontSize));
        panelSeen.add(seenID);
        
        /* Will only change value if seen option is set to editable */
        JCheckBox seenBox = new JCheckBox() {
            protected void processMouseEvent(MouseEvent event) {
                
                if (event.getID() == MouseEvent.MOUSE_CLICKED) {
                    if (config.getSeenEditable())
                        updateSeen(0);
                }
            }
        };
        
        if (config.getUseRegularSeenIcon()) {
            seenBox.setIcon(new ImageIcon(FileUtil.getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
            seenBox.setSelectedIcon(new ImageIcon(FileUtil.getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
        }
        
        seenBox.setPreferredSize(new Dimension(21, 21));
        seenBox.setMinimumSize(new Dimension(21, 21));
        
        panelSeen.add(seenBox);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(2,0,2,4);
        constraints.anchor = GridBagConstraints.WEST;
        
        panelGeneralInfo.add(panelSeen, constraints);
        
        /* Adds the language. */
        JPanel panelLanguage = new JPanel();
        panelLanguage.setLayout(new BoxLayout(panelLanguage, BoxLayout.X_AXIS));
        
        JLabel languageID = new JLabel(Localizer.getString("moviemanager.movie-info-panel.language") + ": "); //$NON-NLS-1$
        languageID.setFont(new Font(languageID.getFont().getName(), Font.BOLD, fontSize));
        languageID.setMinimumSize(languageID.getPreferredSize());
        
        panelLanguage.add(languageID);
        
        JTextField language = new JTextField();
        language.setFont(new Font(language.getFont().getName(), Font.PLAIN, fontSize));
        language.setBorder(null);
        language.setOpaque(false);
        language.setEditable(false);
        
        panelLanguage.add(language);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.weightx = 6;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2,0,2,0);
        constraints.anchor = GridBagConstraints.WEST;
        
        panelGeneralInfo.add(panelLanguage, constraints);
        
        /* Adds the cover. */
        JPanel panelCover = new JPanel();
        
        JLabel cover = new JLabel(new ImageIcon(FileUtil.getImage("/images/" + config.getNoCover()).getScaledInstance(97,97,Image.SCALE_SMOOTH))); //$NON-NLS-1$
        cover.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), BorderFactory.createEtchedBorder()));
        cover.setPreferredSize(new Dimension(97,145));
        cover.setMinimumSize(new Dimension(97,145));
        
        panelCover.add(cover);
        
        
        constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridheight = 6;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        
        
          
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new GridBagLayout());
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0,0,0,0);
        constraints.anchor = GridBagConstraints.CENTER;
        
        panelInfo.add(panelGeneralInfo, constraints);
        
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(0,0,0,0);
        constraints.anchor = GridBagConstraints.NORTHEAST;
        
        panelInfo.add(panelColour, constraints);
        
        
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets = new Insets(0,0,0,0);
        constraints.anchor = GridBagConstraints.EAST;
        
        panelInfo.add(cover, constraints);
        
        /* All done. */
        log.debug("Creation of the General Info panel done."); //$NON-NLS-1$
        
        return panelInfo;
    }
    
    /**
     * Creates a JPanel for display the plot.
     *
     * @return The JPanel.
     **/
    protected JPanel createPlot() {
        log.debug("Start creation of the Plot panel."); //$NON-NLS-1$
        JPanel plot = new JPanel();
        
        plot.setLayout(new BorderLayout());
        
        plot.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder() /*BorderFactory.createEmptyBorder()*/,
                Localizer.getString("moviemanager.movie-info-panel.plot"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(plot.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(2,5,3,5))));
        
        this.textAreaPlot = new JTextArea(""); //$NON-NLS-1$
        textAreaPlot.setEditable(false);
        textAreaPlot.setFocusable(true);
        textAreaPlot.setLineWrap(true);
        textAreaPlot.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textAreaPlot);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        plot.add(scrollPane, BorderLayout.CENTER);
        /* All done. */
        log.debug("Creation of the Plot panel done."); //$NON-NLS-1$
        return plot;
    }
    
    /**
     * Creates a JPanel for display the cast.
     *
     * @return The JPanel.
     **/
    protected JPanel createCast() {
        log.debug("Start creation of the Cast panel."); //$NON-NLS-1$
        JPanel cast = new JPanel();
        
        cast.setLayout(new BorderLayout());
        
        cast.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("moviemanager.movie-info-panel.cast"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(cast.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(2,5,3,5))));
        this.textAreaCast = new JTextArea();
        textAreaCast.setEditable(false);
        textAreaCast.setFocusable(true);
        textAreaCast.setLineWrap(true);
        textAreaCast.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textAreaCast);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        cast.add(scrollPane, BorderLayout.CENTER);
        
        /* All done. */
        log.debug("Creation of the Cast done."); //$NON-NLS-1$
        return cast;
    }
    
    
    /**
     * Creates a JPanel for display the cast.
     *
     * @return The JPanel.
     **/
    protected JPanel createMiscellaneous() {
        
        JPanel miscellaenous = new JPanel();
        
        miscellaenous.setLayout(new BorderLayout());
        
        miscellaenous.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " Miscellaneous ", //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(miscellaenous.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(2,5,3,5))));
        
        
        this.textAreaMiscellaenous = new JTextPane();
        textAreaMiscellaenous.setContentType("text/html"); //$NON-NLS-1$
        textAreaMiscellaenous.setBackground((Color) UIManager.get("TextArea.background")); //$NON-NLS-1$
        textAreaMiscellaenous.setEditable(false);
        textAreaMiscellaenous.setFocusable(true);
        
        JScrollPane scrollPane = new JScrollPane(textAreaMiscellaenous);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        miscellaenous.add(scrollPane, BorderLayout.CENTER);
        
        /* All done. */
        log.debug("Creation of the Miscellaenous done."); //$NON-NLS-1$
        return miscellaenous;
    }
    
    
    /**
     * Creates a JPanel for display the additional info and the notes.
     *
     * @return The JPanel.
     **/
    protected JPanel createAdditionalInfoAndNotes() {
        
        log.debug("Start creation of the Additional Info and Notes panel."); //$NON-NLS-1$
        JPanel additionalInfoAndNotes = new JPanel();
        additionalInfoAndNotes.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
        
        additionalInfoAndNotes.setLayout(new BorderLayout());
        
        /* The additional info panel. */
        JPanel additionalInfo = new JPanel();
        additionalInfo.setLayout(new BorderLayout());
        
        additionalInfo.addComponentListener(this);
        additionalInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("moviemanager.movie-info-panel.additionalinfo"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(additionalInfo.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(1,5,3,5))));
        JTextArea textAreaAdditionalInfo = new JTextArea(""); //$NON-NLS-1$
        textAreaAdditionalInfo.setEditable(false);
        textAreaAdditionalInfo.setFocusable(true);
        textAreaAdditionalInfo.setLineWrap(false);
        
        JScrollPane scrollPaneAdditionalInfo = new JScrollPane(textAreaAdditionalInfo);
        scrollPaneAdditionalInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        additionalInfo.add(scrollPaneAdditionalInfo, BorderLayout.CENTER);
        
        /* The notes panel. */
        JPanel notes = new JPanel();
        notes.setLayout(new BorderLayout());
        notes.addComponentListener(this);
        
        notes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("moviemanager.movie-info-panel.notes"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(notes.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(1,5,3,5))));
        
        JTextArea textAreaNotes = new JTextArea(""); //$NON-NLS-1$
        textAreaNotes.setEditable(true);
        textAreaNotes.setFocusable(true);
        textAreaNotes.setLineWrap(true);
        textAreaNotes.setWrapStyleWord(true);
        JScrollPane scrollPaneNotes = new JScrollPane(textAreaNotes);
        scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        notes.add(scrollPaneNotes, BorderLayout.CENTER);
        
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, additionalInfo, notes);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(12);
        splitPane.setResizeWeight(0.5);
        
        additionalInfoAndNotes.add(splitPane, BorderLayout.CENTER);
        
        /* All done. */
        log.debug("Creation of the Additional Info and Notes done."); //$NON-NLS-1$
        return additionalInfoAndNotes;
    }
    
  
    
    
    JPanel getPanelMovieList() {
        return ((JPanel)
                ((JPanel)
                        getContentPane().getComponent(0)).getComponent(0));
    }
    
    JPanel getPanelMovieInfo() {
        return ((JPanel)
                ((JPanel)
                        getContentPane().getComponent(0)).getComponent(1));
    }
    
    public JPanel getPanelGeneralInfo() {
        return
        (JPanel)
        ((JPanel)
                getPanelMovieInfo().getComponent(0)).getComponent(0);
    }
    
    JScrollPane getPlotScrollPane() {
        return
        ((JScrollPane)
                ((JPanel)
                        ((JPanel)
                                ((JTabbedPane)
                                        getPanelMovieInfo().getComponent(1)).getComponent(0)).getComponent(0)).getComponent(0));
    }
    
    JScrollPane getCastScrollPane() {
        return
        ((JScrollPane)
                ((JPanel)
                        ((JPanel)
                                ((JTabbedPane)
                                        getPanelMovieInfo().getComponent(1)).getComponent(0)).getComponent(1)).getComponent(0));
    }
    
    JScrollPane getMiscellaneousScrollPane() {
        return
        ((JScrollPane)
                (getMiscellaneousPanel()).getComponent(0));
    }
    
    JPanel getPlotPanel() {
        return
        ((JPanel)
                ((JPanel)
                        getTabbedPlotCastMiscellaneous().getComponent(0)).getComponent(0));
    }
    
    JPanel getCastPanel() {
        return
        ((JPanel)
                ((JPanel)
                        getTabbedPlotCastMiscellaneous().getComponent(0)).getComponent(1));
    }
    
    JPanel getMiscellaneousPanel() {
        return
        ((JPanel)
                (getTabbedPlotCastMiscellaneous()).getComponent(1));
    }
    
    JTabbedPane getTabbedPlotCastMiscellaneous() {
        return
        ((JTabbedPane)
                ((JPanel)
                        getMovieInfoSplitPane().getComponent(0)).getComponent(0));
    }
    
    public JSplitPane getMovieInfoSplitPane() {
        return ((JSplitPane)
                (getPanelMovieInfo()).getComponent(1));
    }
    
    
    public JSplitPane getAdditionalInfoNotesSplitPane() {
        return ((JSplitPane)
                ((JPanel)
                        (getMovieInfoSplitPane()).getComponent(1)).getComponent(0));
    }
    
    public JTextArea getPlot() {
        return this.textAreaPlot;
      /*  ((JTextArea)
                ((JScrollPane)
                        getPlotPanel().getComponent(0)).getViewport().getComponent(0)); */
    }
    
    public JTextArea getCast() {
        return this.textAreaCast;
/*        ((JTextArea)
                ((JScrollPane)
                        getCastPanel().getComponent(0)).getViewport().getComponent(0));
    */}
    
    public JTextPane getMiscellaneous() {
        return this.textAreaMiscellaenous;
      /*  ((JTextPane)
                ((JScrollPane)
                        getMiscellaneousPanel().getComponent(0)).getViewport().getComponent(0));
    */}
    
    /**
     * Gets the AdditionalInfo JTextArea.
     **/
    public JTextArea getAdditionalInfo() {
        return
        ((JTextArea)
                getAdditionalInfoScrollPane().getViewport().getComponent(0));
    }
    
    /**
     * Gets the AdditionalInfo JScrollPane.
     **/
    public JScrollPane getAdditionalInfoScrollPane() {
        return
        ((JScrollPane)
                ((JPanel)
                        getAdditionalInfoNotesSplitPane().getComponent(0)).getComponent(0));
    }
    
    /**
     * Gets the notes JTextArea.
     **/
    public JTextArea getNotes() {
        return
        ((JTextArea)
                ((JScrollPane)
                        ((JPanel)
                                getAdditionalInfoNotesSplitPane().getComponent(1)).getComponent(0)).getViewport().getComponent(0));
    }
    
    JPanel getPanelAdditionalInfo() {
        return ((JPanel)
                (getAdditionalInfoNotesSplitPane()).getComponent(0));
    }
    
    JPanel panelNotes() {
        return ((JPanel)
                (getAdditionalInfoNotesSplitPane()).getComponent(1));
    }
    
    
    JMenu getListMenu() {
        return (JMenu) (getJMenuBar()).getComponent(3);
    }
    
    JToolBar getToolBar() {
        return toolBar;
    }
   
    
    JButton getAddButton() {
        return ((ExtendedToolBar) getToolBar()).getAddButton();
    }
    
    JButton getRemoveButton() {
        return ((ExtendedToolBar) getToolBar()).getRemoveButton();
    }
    
    JButton getEditButton() {
        return ((ExtendedToolBar) getToolBar()).getEditButton();
    }
    
    JButton getSearchButton() {
        return ((ExtendedToolBar) getToolBar()).getSearchButton();
    }
    
    JButton getPlayButton() {
        return ((ExtendedToolBar) getToolBar()).getPlayButton();
    }
    
    JButton getPrintButton() {
        return ((ExtendedToolBar) getToolBar()).getPrintButton();
    }
    
    JPanel getEntriesPanel() {
        return
        ((JPanel)
                ((JPanel)
                        ((JPanel)
                                ((JPanel)
                                        getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1));
    }
    
    /**
     * Gets the Movie List.
     *
     * @return JList that displays the MovieList.
     **/
    public JTree getMoviesList() {
        return
        (JTree)
        
        getMoviesListScrollPane().getViewport().getComponent(0);
    }
    
    
    /**
     * Gets the Movie List.
     *
     * @return JList that displays the MovieList.
     **/
    public JScrollPane getMoviesListScrollPane() {
        return movieListScrollPane;
    }
    
    /**
     * Gets the Filter JTextField.
     *
     * @return JTextField.
     **/
    public JTextField getFilter() {
        return
        (JTextField)
        getFilterPanel().getComponent(1);
    }
    
    protected JPanel getFilterPanel() {
        return filterPanel;
    }
    
    
    public JLabel getCover() {
        return
        (JLabel)
        ((JPanel)
                getPanelMovieInfo().getComponent(0)).getComponent(2);
    }
    
        
    /**
     * Gets the Colour JLabel.
     **/
    public JLabel getColourField() {
        return
        (JLabel)
        ((JPanel)
                ((JPanel)
                        getPanelMovieInfo().getComponent(0)).getComponent(1)).getComponent(1);
    }
    
    /**
     * Gets the ColourLabel JLabel.
     **/
    public JLabel getColourLabel() {
        return
        (JLabel)
        ((JPanel)
                ((JPanel)
                        getPanelMovieInfo().getComponent(0)).getComponent(1)).getComponent(0);
    }
    
    
    /**
     * Gets the Date JLabel.
     **/
    public JTextField getDateField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(0)).getComponent(0);
    }
    
    
    /**
     * Gets the Movie Title JLabel.
     **/
    public JTextField getTitleField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(0)).getComponent(1);
    }
    
    
    /**
     * Gets the Directed by JLabel.
     **/
    public JTextField getDirectedByField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(1)).getComponent(1);
    }
    
    /**
     * Gets the Written by JLabel.
     **/
    public JTextField getWrittenByField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(2)).getComponent(1);
    }
    
    /**
     * Gets the Genre JLabel.
     **/
    public JTextField getGenreField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(3)).getComponent(1);
    }
    
    /**
     * Gets the Rating JLabel.
     **/
    public JLabel getRatingField() {
        return
        (JLabel)
        ((JPanel)
                getPanelGeneralInfo().getComponent(4)).getComponent(1);
    }
    
    
    /**
     * Gets the country JTextField.
     **/
    public JTextField getCountryTextField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(5)).getComponent(1);
    }
    
    /**
     * Gets the countryLabel JLabel.
     **/
    public JLabel getCountryLabel() {
        return
        (JLabel)
        ((JPanel)
                getPanelGeneralInfo().getComponent(5)).getComponent(0);
    }
    
    
    /**
     * Gets the Seen JLabel.
     **/
    public JCheckBox getSeen() {
        return
        (JCheckBox)
        ((JPanel)
                getPanelGeneralInfo().getComponent(6)).getComponent(1);
    }
    
    /**
     * Gets the language JTextField.
     **/
    public JTextField getLanguageTextField() {
        return
        (JTextField)
        ((JPanel)
                getPanelGeneralInfo().getComponent(7)).getComponent(1);
    }
    
    /**
     * Gets the language JTextField.
     **/
    public JLabel getLanguageLabel() {
        return
        (JLabel)
        ((JPanel)
                getPanelGeneralInfo().getComponent(7)).getComponent(0);
    }
        
     
  
}


