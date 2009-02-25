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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.MovieManagerConfig.InternalConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandFilter;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandLoadList;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandPlay;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.gui.menubar.DefaultMenuBar;
import net.sf.xmm.moviemanager.gui.menubar.MovieManagerMenuBar;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedToolBar;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeNode;
import net.sf.xmm.moviemanager.swing.extentions.JComboCheckBox;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerPlayHandler;

import org.apache.log4j.Logger;
import org.dotuseful.ui.tree.AutomatedTreeModel;
import org.dotuseful.ui.tree.AutomatedTreeNode;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.w3c.dom.Document;



public class DialogMovieManager extends JFrame implements ComponentListener {
    
	Logger log = Logger.getLogger(getClass());
        
    public static MovieManagerConfig config = MovieManager.getConfig();
    
    public ExtendedToolBar toolBar;
            
    InternalConfig internalConfig = MovieManager.getConfig().getInternalConfig();
    
    /*Number of entries in the list*/
    private int entries;

    public int fontSize = 12;

    private int movieListWidth = 0;

    MovieManagerMenuBar menuBar = null;
    
   public MovieManagerMenuBar getAppMenuBar() {
	   return menuBar;
   }
    
    
    JPanel filterPanel;
    JPanel movieListPanel;
    
    JPanel movieInfoPanel;
    JPanel generalInfoPanel;
    JPanel plotPanel;
    JPanel castPanel;
    JPanel miscellaneousPanel;
    JPanel additionalInfoPanel;
    JPanel notesPanel;
    JPanel panelMovieInfo;

    JScrollPane movieListScrollPane;
    JScrollPane plotScrollPane;
    JScrollPane castScrollPane;
    JScrollPane miscellaneousScrollPane;
    JScrollPane additionalInfoScrollPane;

    JTabbedPane tabbedPlotCastMiscellaneous;

    JSplitPane mainWindowSplitPane;
    JSplitPane movieInfoSplitPane;
    JSplitPane additionalInfoNotesSplitPane;

    JTextArea additionalInfoTextArea;
    JTextArea notesTextArea;

    private JTextPane textAreaMiscellaenous;
    private JTextArea textAreaPlot;
    private JTextArea textAreaCast;
 

    ExtendedJTree moviesList;
    ExtendedTreeCellRenderer treeCellRenderer;
    
   public JComboCheckBox comboBoxFilter;
    JLabel coverLabel;
    JLabel colourField;
    JLabel colourLabel;
    JTextField dateField;
    JTextField titleField;
    JTextField directedByField;
    JTextField writtenByField;
    JTextField genreField;
    JLabel ratingField;
    JTextField countryTextField;
    JLabel countryLabel;
    JCheckBox seenCheckBox;
    JTextField languageTextField;
    JLabel languageLabel;

    JTabbedPane movieInfo;
    
//  The movies that are currently displayed in the movie list
    DefaultListModel currentMovieList;
    ArrayList currentEpisodeList;
    
    
    ExtendedTreeCellRenderer extendedTreeCellRenderer;
    
    public HtmlPanel htmlPanel;
    
    public static JApplet applet = null;

    public DialogMovieManager() { }
    
    public DialogMovieManager(Object applet) {
        DialogMovieManager.applet = (JApplet) applet;
    }
      
    
    public void setCurrentLists(DefaultListModel currentMovieList, ArrayList currentEpisodeList) {
    	this.currentMovieList = currentMovieList;
    	this.currentEpisodeList = currentEpisodeList;
    }
    
    public DefaultListModel getCurrentMoviesList() {
    	return currentMovieList;
    }
    
    public ArrayList getCurrentEpisodesList() {
    	return currentEpisodeList;
    }
    
    public ExtendedTreeCellRenderer getTreeCellRenderer() {
    	return treeCellRenderer;
    }
    
    public static boolean isApplet() {
        return applet != null ? true : false;
    }
    
    public static JApplet getApplet() {
        return applet;
    }
    
    public static void destroy() {
        
        if (applet != null)
            applet.destroy();
    }
    
    /**
     * Finalizes this object (closes the out streams and disposes).
     **/
    public void finalize() {
        
        /* Disposes. */
        dispose();
    }
    
  
    
    /**
     * Setup the main MovieManager object.
     **/
    public void setUp() {
                
        /* Starts other inits. */
        log.debug("Start setting up the MovieManager."); //$NON-NLS-1$
          
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        // Tooltip delay
        ToolTipManager.sharedInstance().setDismissDelay(100000);
        
        if (!MovieManager.isApplet())
            System.setProperty("sun.awt.noerasebackground", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        
        setTitle(MovieManager.getConfig().sysSettings.getAppTitle()); //$NON-NLS-1$
        //setIconImage(FileUtil.getImageFromJar("/images/film.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH)); //$NON-NLS-1$
        setIconImage(FileUtil.getImage("/images/film.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH)); //$NON-NLS-1$
        
        setJMenuBar(createMenuBar());
        
        getContentPane().add(createWorkingArea(),BorderLayout.CENTER);
        
        setResizable(true);
        
        /* Hides database related components. */
        menuBar.setDatabaseComponentsEnable(false);
        
        updateJTreeIcons();
        
        addComponentListener(this);
        
        /* All done, pack. */
        pack();
        updateToolButtonBorder();
        
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
                
      
        /* Setting Main Window slider position */
        if (config.mainWindowSliderPosition == -1) {
        	getMainWindowSplitPane().setDividerLocation(0.537);
        	getMainWindowSplitPane().setLastDividerLocation(getMainWindowSplitPane().getDividerLocation());
        }
        else {
        	getMainWindowSplitPane().setDividerLocation(config.mainWindowSliderPosition);
            if (config.mainWindowLastSliderPosition != -1)
            	getMainWindowSplitPane().setLastDividerLocation(config.mainWindowLastSliderPosition);
        }
                
        /* Setting Movie Info slider position */
        if (config.movieInfoSliderPosition == -1) {
        	getMovieInfoSplitPane().setDividerLocation(0.5);
        	getMovieInfoSplitPane().setLastDividerLocation(getMovieInfoSplitPane().getDividerLocation());
        }
        else if (getMovieInfoSplitPane() != null) {
        	getMovieInfoSplitPane().setDividerLocation(config.movieInfoSliderPosition);

        	if (config.movieInfoLastSliderPosition != -1)
        		getMovieInfoSplitPane().setLastDividerLocation(config.movieInfoLastSliderPosition);
        }
       

        if (getAdditionalInfoNotesSplitPane() != null) {

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
    
    public void resetTreeModel() {
    	setTreeModel(null, null, null);
    }
    
    public void setTreeModel(DefaultTreeModel model, DefaultListModel movieList, ArrayList episodeList) {
    	moviesList.setModel(model);
    	setCurrentLists(movieList, episodeList);
    	MovieManager.newMovieListLoadedHandler.newMovieListLoaded(this);
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
    	
    	if (isShowing())
    		config.setScreenLocation(getLocationOnScreen());
    }
    
        
    public static void setDefaultLookAndFeelDecorated(boolean enable) {
    	JFrame.setDefaultLookAndFeelDecorated(enable);
        JDialog.setDefaultLookAndFeelDecorated(enable);
	}

    
    public void updateLookAndFeelValues() {
    	updateToolButtonBorder();
        updateJTreeIcons();
        
        ExtendedTreeCellRenderer.setDefaultColors();
        moviesList.updateUI();
                
        SwingUtilities.updateComponentTreeUI(filterPanel);
    }
    
    public void updateJTreeIcons() {
        getMoviesList().setRowHeight(config.getMovieListRowHeight() + 2);
        
        /* Show handles in cover mode or no icon mode, otherwise it's hard to recognize series. */
        getMoviesList().setShowsRootHandles(config.getUseJTreeCovers() || !config.getUseJTreeIcons());
    }
    
    
    public void updateToolButtonBorder() {
    	if (MovieManager.getConfig().isRegularToolButtonsUsed())
    		toolBar.updateToolButtonBorderToRegular();
    	else
    		toolBar.updateToolButtonBorderToCurrentLaf();
    }

    
    
    /* mode = 0 (invert), 1 (all to seen), 2(all to unseen). */
    public void updateSeen(int mode) {
        
    	ExtendedJTree movieList = MovieManager.getDialog().getMoviesList();
        
        if (movieList.getLastSelectedPathComponent() == null)
            return;
        
        TreePath [] selectionPaths = movieList.getSelectionPaths();
        
        /* The currently visible entry */
        ModelEntry selected = (ModelEntry) ((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject();
        
        if (selected.getKey() == -1)
            return;
        
        /* Should only be one entry when inverting (Using the seen label/image)*/
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
           
                // If the currently selected movie was changed, it is reloaded.
                if (model == selected) {
                	MovieManagerCommandSelect.reloadCurrentModel();
                }
            }
        }
    }
    
    public void loadMenuLists(Database database) {
           	
    	JMenu menuLists = menuBar.getMenuLists();
    	
        if (database != null && menuLists != null) {
            
            String currentList = config.getCurrentList();
            
            ArrayList listColumns = database.getListsColumnNames();
            JRadioButtonMenuItem menuItem;
            
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
    
    public void setAndShowEntries(int entries) {
    	toolBar.setAndShowEntries(entries);
    	this.entries = entries;
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
    
    
    
   public void newVersionAvailable(String newVersion, String info) {
	   menuBar.newVersionAvailable(newVersion, info);
   }
    
   protected JMenuBar createMenuBar() {
	   
	   boolean defaultMenu = true;
	   
	 ///String className = "net.sf.xmm.moviemanager.gui.menubar.PlanetaryMenuBar";
	   
	   InternalConfig internalConfig = MovieManager.getConfig().getInternalConfig();
	   String className =  internalConfig.getPlugin("menuBar");
	   	   
	   if (className != null)
		   defaultMenu = false;
	   
	   if (!defaultMenu) {

		   try {
			   Class menuBarClass = Class.forName(className);
			   menuBar = (MovieManagerMenuBar) menuBarClass.newInstance();
			   return menuBar.getNewInstance(internalConfig, config);

		   } catch (ClassNotFoundException e) {
			   log.error("ClassNotFoundException. Failed to load class " + className);
			   defaultMenu = true;
		   } catch (IllegalAccessException e) {
			   log.error("IllegalAccessException. Failed to load class " + className);
			   defaultMenu = true;
		   } catch (InstantiationException e) {
			   log.error("InstantiationException. Failed to load class " + className);
			   defaultMenu = true;
		   }
	   }
	   
	   
	   //menuBar = new DefaultMenuBar();
	   //return menuBar.getNewInstance(internalConfig, config);
	   
	   menuBar = new DefaultMenuBar(internalConfig, config);
	   return (JMenuBar) menuBar;
   }

   
   // Below is GUI creation code 
   
   public int getCurrentMainTabIndex() {
	   return movieInfo.getSelectedIndex();
   }
   
   public void setTabbedMovieInfoTitle(int i, String tabName) {
	   movieInfo.setTitleAt(i, tabName);
   }
    
   
    public void setTabbedMovieInfoTitle() {
    	String tabName;
    	ModelHTMLTemplate template = MovieManager.getConfig().getHTMLTemplate();
    	
    	if (template == null) {
    		movieInfo.setTitleAt(1, "No Templates available");
    		return;
    	}
    
    	if (template.hasStyles()) {
						
			String styleName = MovieManager.getConfig().getHTMLTemplateStyleName();
			tabName = template.getName() + " > " + template.getStyle(styleName);
		}
		else
			tabName = template.getName();
		
		// Setting the style name as title of tab bar.
		movieInfo.setTitleAt(1, tabName);
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
       
        mainWindowSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,createMoviesList(), createMovieInfo());
        mainWindowSplitPane.setOneTouchExpandable(true);
        mainWindowSplitPane.setContinuousLayout(true);
        mainWindowSplitPane.setDividerSize(7);
        mainWindowSplitPane.setResizeWeight(0);
        mainWindowSplitPane.setDividerLocation(0.12);
         
//      Makes sure the cellrenderer width is updated when clicking the main splitter
        (((BasicSplitPaneUI) mainWindowSplitPane.getUI()).getDivider()).addMouseListener(new MouseListener() {
        	public void mouseClicked(MouseEvent e) {}
        	public void mouseEntered(MouseEvent e) {}
        	public void mouseExited(MouseEvent e) {
        		TreeNode node = (AutomatedTreeNode) getMoviesList().getLastSelectedPathComponent();
        		
        		if (getMoviesList().getModel() != null)
        			((DefaultTreeModel) getMoviesList().getModel()).nodeChanged(node); 
        	}
        	public void mousePressed(MouseEvent e) {}
        	public void mouseReleased(MouseEvent e) {
        		TreeNode node = (AutomatedTreeNode) getMoviesList().getLastSelectedPathComponent();
        		if (getMoviesList().getModel() != null)
        			((DefaultTreeModel) getMoviesList().getModel()).nodeChanged(node); 
        	}
        });
       
        workingArea.setLayout(new BorderLayout());
        workingArea.add(mainWindowSplitPane, BorderLayout.CENTER); //$NON-NLS-1$
        
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
        
        movieListPanel = new JPanel();
        movieListPanel.setLayout(new GridBagLayout());
        
        movieListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " "+ Localizer.getString("moviemanager.listpanel-title") + " - " + config.getCurrentList() + " ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(movieListPanel.getFont().getName(),Font.BOLD, fontSize)),
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
        movieListPanel.add(toolBar, constraints);
        
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
        movieListPanel.add(movieListScrollPane, constraints);
        
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
        movieListPanel.add(filterPanel, constraints);
        
        /* All done. */
        log.debug("Creation of the Movies List panel done."); //$NON-NLS-1$
        return movieListPanel;
    }
    
  
    
    /**
     * Creates the toolbar.
     *
     * @return The toolbar.
     **/
    protected ExtendedToolBar createToolBar() {
    	log.debug("Start creation of the ToolBar."); //$NON-NLS-1$

    	ExtendedToolBar toolBar = null;

    	InternalConfig internalConfig = MovieManager.getConfig().getInternalConfig();
    	String className =  internalConfig.getPlugin("toolBar");

    	if (className != null) {

    		try {
    			Class menuBarClass = Class.forName(className);
    			toolBar = (ExtendedToolBar) menuBarClass.newInstance();
    		} catch (ClassNotFoundException e) {
    			log.error("ClassNotFoundException. Failed to load class " + className);

    		} catch (IllegalAccessException e) {
    			log.error("IllegalAccessException. Failed to load class " + className);

    		} catch (InstantiationException e) {
    			log.error("InstantiationException. Failed to load class " + className);

    		}
    	}

    	if (toolBar == null)
    		toolBar = new ExtendedToolBar();
        
        toolBar.setPlayButtonLegal(!internalConfig.isPlayMovieDisabled());
        toolBar.setPrintButtonLegal(!internalConfig.isPrintFunctionDisabled());
        toolBar.setAddButtonLegal(!internalConfig.isAddMovieDisabled());
        toolBar.setEditButtonLegal(!internalConfig.isEditMovieDisabled());
        toolBar.setRemoveButtonLegal(!internalConfig.isRemoveMovieDisabled());
        toolBar.setSearchButtonLegal(!internalConfig.isSearchMenuDisabled());
        toolBar.setShowEntriesCount(!internalConfig.isEntriesCountDisabled()); 
        
        toolBar.showPlayButton(MovieManager.getConfig().getDisplayPlayButton());
        toolBar.showPrintButton(MovieManager.getConfig().getDisplayPrintButton());
        toolBar.setEnableButtonPopup(!internalConfig.isToolBarPopupDisabled());
                
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

    	moviesList = new ExtendedJTree() {

    		// This makes sure the entire width of the tree rows is painted
    		protected void paintComponent(Graphics g) {
    			int[] rows = getSelectionRows();

    			if (rows != null && rows.length > 0) {
    				for (int i = 0; i < rows.length; i++) {
    					Rectangle b = getRowBounds(rows[i]);
    					g.setColor(UIManager.getColor("Tree.selectionBackground"));
    					g.fillRect(0, b.y, getWidth(), b.height);
    				}
    			}
    			super.paintComponent(g);
    		}
    	};
        
        ExtendedTreeCellRenderer.setDefaultColors();
         
        moviesList.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(new ModelMovie(false)))); //$NON-NLS-1$
        
        moviesList.setRootVisible(false);
        moviesList.setDragEnabled(false);
        moviesList.setLargeModel(true);
        
     
        
        // Gives error on some versions of substance L&F.
        moviesList.setFont(new Font(moviesList.getFont().getName(),Font.PLAIN,fontSize));
        
        MovieManagerCommandSelect listener = new MovieManagerCommandSelect();
        
        /* Adding listeners to the movie list */
        moviesList.addTreeSelectionListener(listener);
        moviesList.addMouseListener(listener);
        moviesList.addKeyListener(listener);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(moviesList);
        
        moviesList.setOpaque(false);
        
        //Avoids NullPointer on Synthetica L&F.
        scrollPane.getViewport().setBackground(UIManager.getColor("ScrollPane.background"));
        
        treeCellRenderer = new ExtendedTreeCellRenderer(MovieManager.getDialog().getMoviesList(), scrollPane);
        extendedTreeCellRenderer = treeCellRenderer;
        
        moviesList.setCellRenderer(treeCellRenderer);
        MovieManager.newDbHandler.addNewDatabaseLoadedEventListener(treeCellRenderer);
         
        /* All done. */
        log.debug("Creation of the List done."); //$NON-NLS-1$
        return scrollPane;
    }
    
  
     

    protected JPanel createFilter() {

    	log.debug("Start creation of the Filter."); //$NON-NLS-1$
    	JPanel filter = new JPanel(new BorderLayout());
    	filter.setBorder(BorderFactory.createEmptyBorder(10,4,4,4));
    	filter.setLayout(new BorderLayout());
    	
    	/* Adds the Label. */
    	JLabel label = new JLabel(Localizer.getString("moviemanager.listpanel-filter")); //$NON-NLS-1$
    	label.setFont(new Font(label.getFont().getName(),Font.PLAIN,fontSize));
    	
    	filter.add(label, BorderLayout.WEST);
    	
    	ArrayList filterValues = config.getMainFilterSearchValues();
    	JCheckBox [] items = new JCheckBox[filterValues.size()];
    	    	
    	for (int i = 0; i < items.length; i++)
    		items[i] = new JCheckBox((String) filterValues.get(i));
    	    	
    	
    	comboBoxFilter = new JComboCheckBox(items); //$NON-NLS-1$ //$NON-NLS-2$
    	comboBoxFilter.setActionCommand("Filter"); //$NON-NLS-1$
    	
    	((JComboCheckBox.JComboCheckBoxEditor) comboBoxFilter.getEditor()).addComboCheckBoxKeyEventListener(new MovieManagerCommandFilter("", null, true, true));
    	comboBoxFilter.setFont(new Font(comboBoxFilter.getFont().getName(), Font.PLAIN,	fontSize));

    	/*
    	 * If using oyoaha laf the border needs to be set to make the
    	 * combobox smaller
    	 */
    	if (MovieManager.getConfig().getLookAndFeelType() == 2)
    		comboBoxFilter.setBorder(BorderFactory.createLoweredBevelBorder());
    
    	filter.add(comboBoxFilter, BorderLayout.CENTER);
        
    	/* All done. */
    	log.debug("Creation of the Filter done."); //$NON-NLS-1$

    	Dimension filterDim = new Dimension(255, 37);
    		
    	filter.setPreferredSize(filterDim);
    	filter.setMaximumSize(filterDim);
    	filter.setMinimumSize(filterDim);
    	filter.setSize(filterDim);
    	
    	return filter;
    }

    
   

    protected JTabbedPane createMovieInfo() {

    	movieInfo = new JTabbedPane();
    	movieInfo.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
    	movieInfo.add(MovieManager.getConfig().sysSettings.getLookAndFeelTitle(), createStandardMovieInfo()); //$NON-NLS-1$

    	// HTML panel isn't supported on Java 1.4
    	if (!SysUtil.isCurrentJRES14() && !MovieManager.isApplet() && !MovieManager.getConfig().getInternalConfig().getDisableHTMLView()) {
    		htmlPanel = new HtmlPanel();
    		movieInfo.add(htmlPanel); //$NON-NLS-1$
    		setTabbedMovieInfoTitle();
    		movieInfo.setSelectedIndex(config.getLastMovieInfoTabIndex());
    		final JTabbedPane finalMovieInfo = movieInfo;

    		finalMovieInfo.addMouseListener(new MouseListener() {

    			public	void mousePressed(MouseEvent e) {
    				config.setLastMovieInfoTabIndex(finalMovieInfo.getSelectedIndex());
    				MovieManagerCommandSelect.execute();
    			}

    			public	void mouseClicked(MouseEvent e) {}
    			public void mouseEntered(MouseEvent e) {}
    			public void mouseExited(MouseEvent e) {}
    			public void mouseReleased(MouseEvent e) {}
    		});
    	}
    	return movieInfo;
    }


    public void setHTMLData(Document document, SimpleHtmlRendererContext rcontext) {
    	htmlPanel.setDocument(document, rcontext);
    }
    
    
    protected JPanel createStandardMovieInfo() {
    	log.debug("Start creation of the Movie Info panel."); //$NON-NLS-1$
    	panelMovieInfo = new JPanel();
    	panelMovieInfo.addComponentListener(this);

    	JPanel generalInfoPanel = createGeneralInfo();

    	double size[][] = {{info.clearthought.layout.TableLayout.FILL}, {generalInfoPanel.getPreferredSize().getHeight() + 20, info.clearthought.layout.TableLayout.FILL}};

    	panelMovieInfo.setLayout(new info.clearthought.layout.TableLayout(size));
    	panelMovieInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,4,3,3), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
    			Localizer.getString("moviemanager.movieinfopanel.title"), //$NON-NLS-1$
    			TitledBorder.DEFAULT_JUSTIFICATION,
    			TitledBorder.DEFAULT_POSITION,
    			new Font(panelMovieInfo.getFont().getName(),Font.BOLD, fontSize)),
    			BorderFactory.createEmptyBorder(0,5,5,5))));


    	/* Adds the general info. */
    	GridBagConstraints constraints;

    	panelMovieInfo.add(generalInfoPanel, "0, 0"); //$NON-NLS-1$

    	JPanel miscellaneous = createMiscellaneous();

    	JPanel plotAndCast = new JPanel();


    	size = new double [][] {{info.clearthought.layout.TableLayout.FILL}, {0.55, info.clearthought.layout.TableLayout.FILL}};

    	plotAndCast.setLayout(new info.clearthought.layout.TableLayout(size));

    	plotAndCast.add(createPlot(), "0, 0");
    	plotAndCast.add(createCast(), "0, 1");

    	tabbedPlotCastMiscellaneous = new JTabbedPane();
    	tabbedPlotCastMiscellaneous.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
    	tabbedPlotCastMiscellaneous.add(Localizer.getString("moviemanager.movie-info-panel.plot_and_cast"), plotAndCast); //$NON-NLS-1$
    	tabbedPlotCastMiscellaneous.add(Localizer.getString("moviemanager.movie-info-panel.miscellaneous"), miscellaneous); //$NON-NLS-1$


    	JPanel tabbedPanel = new JPanel(new BorderLayout());
    	tabbedPanel.add(tabbedPlotCastMiscellaneous, BorderLayout.CENTER);

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

    	JPanel additionalInfo = null;

    	if (!internalConfig.isAdditionalInfoAndNotesReplacedByHTMLAdd()) {
    		additionalInfo = createAdditionalInfoAndNotes();
    	}

    	movieInfoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tabbedPanel, additionalInfo);
    	movieInfoSplitPane.setContinuousLayout(true);
    	movieInfoSplitPane.setDividerSize(7);
    	movieInfoSplitPane.setResizeWeight(0.5);
    	movieInfoSplitPane.setDividerLocation(0.4);

    	if (internalConfig.isAdditionalInfoAndNotesReplacedByHTMLAdd()) {
    		movieInfoSplitPane.setEnabled(false);
    	}
    	else
    		movieInfoSplitPane.setOneTouchExpandable(true);

    	panelMovieInfo.add(movieInfoSplitPane, "0, 1"); //$NON-NLS-1$

    	
    	
    	return panelMovieInfo;
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
        
        colourLabel = new JLabel(""); //$NON-NLS-1$
        colourLabel.setFont(new Font(colourLabel.getFont().getName(), Font.BOLD, fontSize));
        panelColour.add(colourLabel);
        
        colourField = new JLabel(" "); //$NON-NLS-1$
        colourField.setFont(new Font(colourField.getFont().getName(), Font.PLAIN, fontSize));
        panelColour.add(colourField);
               
        
        
        /* Adds the subInfo JPanel. */
        JPanel panelDateAndTitle = new JPanel();
        
        panelDateAndTitle.setLayout(new BorderLayout());
        
        dateField = new JTextField();
        dateField.setFont(new Font(dateField.getFont().getName(), Font.BOLD, fontSize +3));
        dateField.setBorder(null);
        dateField.setOpaque(false);
        dateField.setEditable(false);
        
        panelDateAndTitle.add(dateField, BorderLayout.WEST);
        
        
        titleField = new JTextField();
        titleField.setFont(new Font("Dialog", Font.BOLD, fontSize +3)); //$NON-NLS-1$
        titleField.setBorder(null);
        titleField.setOpaque(false);
        titleField.setEditable(false);
        
        panelDateAndTitle.add(titleField, BorderLayout.CENTER);
        
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
        
        directedByField = new JTextField();
        directedByField.setFont(new Font(directedByField.getFont().getName(), Font.PLAIN, fontSize));
        directedByField.setBorder(null);
        directedByField.setOpaque(false);
        directedByField.setEditable(false);
        
        
        directedByField.setBackground(new JPanel().getBackground());
        
        panelDirected.add(directedByField);
        
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
        
        writtenByField = new JTextField();
        writtenByField.setFont(new Font(writtenByField.getFont().getName(), Font.PLAIN, fontSize));
        writtenByField.setBorder(null);
        writtenByField.setOpaque(false);
        writtenByField.setEditable(false);
        
        panelWritten.add(writtenByField);
        
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
        
        genreField = new JTextField();
        genreField.setFont(new Font(genreField.getFont().getName(), Font.PLAIN, fontSize));
        genreField.setBorder(null);
        genreField.setOpaque(false);
        genreField.setEditable(false);
        
        panelGenre.add(genreField);
        
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
        
        ratingField = new JLabel();
        ratingField.setFont(new Font(ratingField.getFont().getName(), Font.PLAIN, fontSize));
        panelRating.add(ratingField);
        
        panelRating.add(ratingField);
        
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
        
        countryLabel = new JLabel(Localizer.getString("moviemanager.movie-info-panel.country") + ": "); //$NON-NLS-1$
        countryLabel.setFont(new Font(countryLabel.getFont().getName(), Font.BOLD, fontSize));
        
        countryLabel.setMinimumSize(countryLabel.getPreferredSize());
        
        panelCountry.add(countryLabel);
        
        countryTextField = new JTextField();
        countryTextField.setFont(new Font(countryTextField.getFont().getName(), Font.PLAIN, fontSize));
        countryTextField.setBorder(null);
        countryTextField.setOpaque(false);
        countryTextField.setEditable(false);
        
        panelCountry.add(countryTextField);
        
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
        seenCheckBox = new JCheckBox() {
            protected void processMouseEvent(MouseEvent event) {
                
                if (event.getID() == MouseEvent.MOUSE_CLICKED) {
                	
                	if (internalConfig.movieSeenReplaceWithPlay()) {
                		                		
                	}
                	else if (config.getSeenEditable())
                        updateSeen(0);
                }
            }
        };
    
        
        if (internalConfig.movieSeenReplaceWithPlay()) {
        	JButton play = new JButton(new ImageIcon(FileUtil.getImage("/images/play.png").getScaledInstance(20,20,Image.SCALE_SMOOTH)));
        	play.setPreferredSize(new Dimension(27, 27));
        	
        	// Uses plugin playhandler is it exists
        	MovieManagerPlayHandler playHandler = MovieManager.getConfig().getPlayHandler();
        	play.setActionCommand("Play"); //$NON-NLS-1$
        	
			if (playHandler != null)
    			play.addActionListener(playHandler); 
			else
				play.addActionListener(new MovieManagerCommandPlay());
        	
			seenID.setText("Play  ");
        	panelSeen.add(play);
        }
        else { 
        	
        	if (config.getUseRegularSeenIcon()) {
        		seenCheckBox.setIcon(new ImageIcon(FileUtil.getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
        		seenCheckBox.setSelectedIcon(new ImageIcon(FileUtil.getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
        	}
        
        	seenCheckBox.setPreferredSize(new Dimension(21, 21));
        	seenCheckBox.setMinimumSize(new Dimension(21, 21));
        	panelSeen.add(seenCheckBox);
        }
        
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
        
        languageLabel = new JLabel(Localizer.getString("moviemanager.movie-info-panel.language") + ": "); //$NON-NLS-1$
        languageLabel.setFont(new Font(languageLabel.getFont().getName(), Font.BOLD, fontSize));
        languageLabel.setMinimumSize(languageLabel.getPreferredSize());
        
        panelLanguage.add(languageLabel);
        
        languageTextField = new JTextField();
        languageTextField.setFont(new Font(languageTextField.getFont().getName(), Font.PLAIN, fontSize));
        languageTextField.setBorder(null);
        languageTextField.setOpaque(false);
        languageTextField.setEditable(false);
        
        panelLanguage.add(languageTextField);
        
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
        
        coverLabel = new JLabel(new ImageIcon(FileUtil.getImage("/images/" + config.getNoCover()).getScaledInstance(97,97,Image.SCALE_SMOOTH))); //$NON-NLS-1$
        coverLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), BorderFactory.createEtchedBorder()));
        coverLabel.setPreferredSize(new Dimension(97,145));
        coverLabel.setMinimumSize(new Dimension(97,145));
        
        panelCover.add(coverLabel);
        
        
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
        
        panelInfo.add(coverLabel, constraints);
        
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
        
        plot.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder() /*BorderFactory.createEmptyBorder()*/,
                Localizer.getString("moviemanager.movie-info-panel.plot"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(plot.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(0,5,3,5))));
        
        this.textAreaPlot = new JTextArea(""); //$NON-NLS-1$
        textAreaPlot.setEditable(false);
        textAreaPlot.setFocusable(true);
        textAreaPlot.setLineWrap(true);
        textAreaPlot.setWrapStyleWord(true);
 
        plotScrollPane = new JScrollPane(textAreaPlot);
        plotScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        plot.add(plotScrollPane, BorderLayout.CENTER);
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
        
        cast.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("moviemanager.movie-info-panel.cast"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(cast.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(0,5,2,5))));
        textAreaCast = new JTextArea();
        textAreaCast.setEditable(false);
        textAreaCast.setFocusable(true);
        textAreaCast.setLineWrap(true);
        textAreaCast.setWrapStyleWord(true);
        castScrollPane = new JScrollPane(textAreaCast);
        castScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        cast.add(castScrollPane, BorderLayout.CENTER);
        
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
        
        miscellaenous.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,4,2,4), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " Miscellaneous ", //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(miscellaenous.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(0,5,2,5))));
        
        
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
        additionalInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.getString("moviemanager.movie-info-panel.additionalinfo"), //$NON-NLS-1$
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(additionalInfo.getFont().getName(),Font.PLAIN, fontSize)),
                BorderFactory.createEmptyBorder(1,5,3,3))));
        additionalInfoTextArea = new JTextArea(""); //$NON-NLS-1$
        additionalInfoTextArea.setEditable(false);
        additionalInfoTextArea.setFocusable(true);
        additionalInfoTextArea.setLineWrap(false);
        
        additionalInfoScrollPane = new JScrollPane(additionalInfoTextArea);
        additionalInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        additionalInfo.add(additionalInfoScrollPane, BorderLayout.CENTER);
        
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
        
        notesTextArea = new JTextArea(""); //$NON-NLS-1$
        // Only editable when not in applet mode
        notesTextArea.setEditable(!isApplet());
        notesTextArea.setFocusable(true);
        notesTextArea.setLineWrap(true);
        notesTextArea.setWrapStyleWord(true);
        
        JScrollPane scrollPaneNotes = new JScrollPane(notesTextArea);
        scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        notes.add(scrollPaneNotes, BorderLayout.CENTER);
                
        additionalInfoNotesSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, additionalInfo, notes);
        additionalInfoNotesSplitPane.setOneTouchExpandable(true);
        additionalInfoNotesSplitPane.setContinuousLayout(true);
        additionalInfoNotesSplitPane.setDividerSize(7);
        additionalInfoNotesSplitPane.setResizeWeight(0.5);
        
        additionalInfoAndNotes.add(additionalInfoNotesSplitPane, BorderLayout.CENTER);
        
        /* All done. */
        log.debug("Creation of the Additional Info and Notes done."); //$NON-NLS-1$
        return additionalInfoAndNotes;
    }

    
    
    
    JPanel getPanelMovieList() {
        return movieListPanel;
    }
     
     
    JTabbedPane getTabbedPlotCastMiscellaneous() {
        return tabbedPlotCastMiscellaneous;
    }
    
    public JSplitPane getMainWindowSplitPane() {
        return mainWindowSplitPane;
    }
    
    public JSplitPane getMovieInfoSplitPane() {
        return movieInfoSplitPane;
    }
        
    public JSplitPane getAdditionalInfoNotesSplitPane() {
        return additionalInfoNotesSplitPane;
    }
    
    public JTextArea getPlot() {
        return this.textAreaPlot;
	}
    
    public JTextArea getCast() {
        return this.textAreaCast;
	}
    
    public JTextPane getMiscellaneous() {
        return this.textAreaMiscellaenous;
    }
    
    /**
     * Gets the AdditionalInfo JTextArea.
     **/
    public JTextArea getAdditionalInfo() {
        return additionalInfoTextArea;
    }
    
    /**
     * Gets the AdditionalInfo JScrollPane.
     **/
    public JScrollPane getAdditionalInfoScrollPane() {
        return additionalInfoScrollPane;
    }
    
    /**
     * Gets the notes JTextArea.
     **/
    public JTextArea getNotes() {
        return notesTextArea;
    }
    
    JPanel getPanelAdditionalInfo() {
        return additionalInfoPanel;
    }
    
    public ExtendedToolBar getToolBar() {
        return toolBar;
    }
    
       
 /*   JMenu getMenuFile1() {
        return menuBar.getMenuFile();
    }
    
    JMenu getMenuDatabase1() {
        return menuBar.getMenuDatabase();
    }
    
    JMenu getMenuTools1() {
        return menuBar.getMenuTools();
    }
    
    JMenu getMenuLists1() {
        return menuBar.getMenuLists();
    }
    
    JMenu getMenuView1() {
        return menuBar.getMenuView();
    }
    
    JMenu getMenuHelp1() {
        return menuBar.getMenuHelp();
    }
    
    JMenu getMenuUpdate1() {
        return menuBar.getMenuUpdate();
    }
        
   
       
    JButton getAddButton1() {
        return toolBar.getAddButton();
    }
    
    JButton getRemoveButton1() {
        return toolBar.getRemoveButton();
    }
    
    JButton getEditButton1() {
        return toolBar.getEditButton();
    }
    
    JButton getSearchButton1() {
        return toolBar.getSearchButton();
    }
    
    JButton getPlayButton1() {
        return toolBar.getPlayButton();
    }
    
    JButton getPrintButton1() {
        return toolBar.getPrintButton();
    }*/
    
    
    public boolean getPLayButtonVisible() {
    	return getToolBar().getPLayButtonVisible();
    }
    
    public boolean getPrintButtonVisible() {
    	return getToolBar().getPrintButtonVisible();
    }
   
        
    /**
     * Gets the Movie List.
     *
     * @return JList that displays the MovieList.
     **/
    public ExtendedJTree getMoviesList() {
        return moviesList;
    }
    
    
    /**
     * Gets the Movie List.
     *
     * @return JList that displays the MovieList.
     **/
    public JScrollPane getMoviesListScrollPane() {
        return movieListScrollPane;
    }
    
    
    public JComboCheckBox getFilter() {
        return comboBoxFilter;
    }
    
    
    public String getFilterString() {
    	return getFilter().getText();
    }
    
    
    public JLabel getCover() {
        return coverLabel;
    }
    
        
    /**
     * Gets the Colour JLabel.
     **/
    public JLabel getColourField() {
        return colourField;
    }
    
    /**
     * Gets the ColourLabel JLabel.
     **/
    public JLabel getColourLabel() {
        return colourLabel;
    }
    
    
    /**
     * Gets the Date JLabel.
     **/
    public JTextField getDateField() {
        return dateField;
    }
    
    
    /**
     * Gets the Movie Title JLabel.
     **/
    public JTextField getTitleField() {
        return titleField;
    }
    
    
    /**
     * Gets the Directed by JLabel.
     **/
    public JTextField getDirectedByField() {
        return directedByField;
    }
  
    /**
     * Gets the Written by JLabel.
     **/
    public JTextField getWrittenByField() {
        return writtenByField;
    }
    
    /**
     * Gets the Genre JLabel.
     **/
    public JTextField getGenreField() {
        return genreField;
    }
    
    /**
     * Gets the Rating JLabel.
     **/
    public JLabel getRatingField() {
        return ratingField;
    }
    
    
    /**
     * Gets the country JTextField.
     **/
    public JTextField getCountryTextField() {
        return countryTextField;
    }
    
    /**
     * Gets the countryLabel JLabel.
     **/
    public JLabel getCountryLabel() {
        return countryLabel;
    }
    
    
    /**
     * Gets the Seen JLabel.
     **/
    public JCheckBox getSeen() {
        return seenCheckBox;
    }
    
    /**
     * Gets the language JTextField.
     **/
    public JTextField getLanguageTextField() {
        return languageTextField;
    }
    
    /**
     * Gets the language JTextField.
     **/
    public JLabel getLanguageLabel() {
        return languageLabel;
    }
}


