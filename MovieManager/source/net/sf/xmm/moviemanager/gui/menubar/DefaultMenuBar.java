package net.sf.xmm.moviemanager.gui.menubar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.MovieManagerConfig.InternalConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandCloseDatabase;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandConvertDatabase;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandExit;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandOpenPage;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandAbout;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandAdditionalInfoFields;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandFolders;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandHelp;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandNew;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandOpen;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandPrefs;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandQueries;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandReportGenerator;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandUpdateIMDBInfo;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandExport;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImport;
import net.sf.xmm.moviemanager.gui.DialogNewVersionInfo;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.swing.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;



public class DefaultMenuBar extends JMenuBar implements MovieManagerMenuBar {

	Logger log = Logger.getLogger(getClass());

	JMenu menuFile = null;
	JMenu menuDatabase = null;
	JMenu menuTools = null;
	JMenu menuLists = null;
	JMenu menuView = null;
	JMenu menuHelp = null;
	JMenu menuUpdate = null;

	JMenuItem menuItemNew = null;
	JMenuItem menuItemOpen = null;
	JMenuItem menuItemClose = null;
	JMenuItem menuImport = null;
	JMenuItem menuExport = null;
	JMenuItem menuItemExit = null;

	JMenuItem menuItemQueries = null;
	JMenuItem menuItemFolders = null;
	JMenuItem menuItemAddField = null;
	JMenuItem menuItemAddList = null;
	JMenuItem menuItemConvertDatabase = null;

	JMenuItem menuItemPrefs = null;
	JMenuItem menuItemAddMultipleMovies = null;
	JMenuItem menuItemUpdateIMDbInfo = null;
	JMenuItem menuItemReportGenerator = null;

	JMenuItem menuItemAbout = null;
	
	
	InternalConfig internalConfig;
	MovieManagerConfig config;
	
	
	public JMenu getMenuFile() {
        return menuFile;
    }
    
	public JMenu getMenuDatabase() {
        return menuDatabase;
    }
    
	public JMenu getMenuTools() {
        return menuTools;
    }
    
	public JMenu getMenuLists() {
        return menuLists;
    }
    
	public JMenu getMenuView() {
        return menuView;
    }
    
	public JMenu getMenuHelp() {
        return menuHelp;
    }
    
	public JMenu getMenuUpdate() {
        return menuUpdate;
    }
	
	public JMenuBar getNewInstance(InternalConfig internalConfig, MovieManagerConfig config) {
		this.internalConfig = internalConfig;
		this.config = config;
		return createMenuBar();
	} 
	
	public DefaultMenuBar(InternalConfig internalConfig, MovieManagerConfig config) {
		this.internalConfig = internalConfig;
		this.config = config;
		createMenuBar();
	} 
		
	
	/**
	 * Creates the menuBar.
	 *
	 * @return The menubar.
	 **/
	protected JMenuBar createMenuBar() {
		log.debug("Start creation of the MenuBar."); //$NON-NLS-1$
		//JMenuBar menuBar = new JMenuBar();
		JMenuBar menuBar = this;
		
		menuBar.setBorder(BorderFactory.createEmptyBorder(2,0,8,0));
		/* Creation of the file menu. */
		menuBar.add(createMenuFile());

		menuBar.add(createMenuDatabase());
		/* Creation of the options menu. */
		menuBar.add(createMenuTools());
		menuBar.add(createMenuLists());

		if (!SysUtil.isCurrentJRES14() && !MovieManager.isApplet())
			menuBar.add(createMenuView());

		/* Creation of the help menu. */
		menuBar.add(createMenuHelp());

		log.debug("Creation of the MenuBar done."); //$NON-NLS-1$
		return menuBar;
	}

	/**
	 * Creates the update menu.
	 *
	 * @return The update menu.
	 **/
	protected JMenu createMenuUpdate(final String newVersion, final String buf) {
		log.debug("Start creation of the Update menu."); //$NON-NLS-1$

		JMenu menuUpdate = new JMenu("Update Available"); //$NON-NLS-1$

		menuUpdate.setBackground(Color.BLACK);	
		menuUpdate.setForeground(Color.WHITE);


		class MyMenuItemUI extends javax.swing.plaf.basic.BasicMenuItemUI {

			public MyMenuItemUI() {
				super();
				selectionBackground = Color.BLACK;
				selectionForeground = Color.WHITE;
			}

			public Dimension getMaximumSize(JComponent c) {
				return new Dimension(110, MovieManager.getDialog().getSize().height);
			}
		}

		menuUpdate.setUI(new MyMenuItemUI());


		/* MenuItem VersionInfo. */
		JMenuItem menuItemVersionInfo = new JMenuItem("Version Info"); //$NON-NLS-1$
		menuUpdate.add(menuItemVersionInfo);

		menuItemVersionInfo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				DialogNewVersionInfo info = new DialogNewVersionInfo(newVersion, buf);
				GUIUtil.showAndWait(info, true);
			}
		});

		/* MenuItem topCheck. */
		JMenuItem menuItemStopCheck = new JMenuItem("Do not check for updates"); //$NON-NLS-1$
		menuUpdate.add(menuItemStopCheck);

		menuItemStopCheck.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				config.setCheckForProgramUpdates(false);  		
			}
		});

		/* All done. */
		log.debug("Creation of the Help menu done."); //$NON-NLS-1$
		return menuUpdate;
	}


	 public void newVersionAvailable(String newVersion, String info) {
		   JMenuBar menuBar = MovieManager.getDialog().getJMenuBar();
		   menuBar.add(createMenuUpdate(newVersion, info));
		   MovieManager.getDialog().setJMenuBar(menuBar);
	 }
	

	/**
	 * Creates the file menu.
	 *
	 * @return The file menu.
	 **/
	protected JMenu createMenuFile() {
		log.debug("Start creation of the File menu."); //$NON-NLS-1$
		menuFile = new JMenu(Localizer.getString("moviemanager.menu.file")); //$NON-NLS-1$
		menuFile.setMnemonic('F');


		/* MenuItem New. */
		JMenuItem menuItemNew = new JMenuItem(Localizer.getString("moviemanager.menu.file.newdb"),'N'); //$NON-NLS-1$
		menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemNew.setActionCommand("New"); //$NON-NLS-1$
		menuItemNew.addActionListener(new MovieManagerCommandNew());

		menuFile.addSeparator();
		menuFile.add(menuItemNew);
		this.menuItemNew = menuItemNew;
		
		/* MenuItem Open. */
		JMenuItem menuItemOpen = new JMenuItem(Localizer.getString("moviemanager.menu.file.opendb"),'O'); //$NON-NLS-1$
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemOpen.setActionCommand("Open"); //$NON-NLS-1$
		menuItemOpen.addActionListener(new MovieManagerCommandOpen());

		menuFile.addSeparator();
		menuFile.add(menuItemOpen);
		this.menuItemOpen = menuItemOpen;
		
		/* MenuItem Close. */
		JMenuItem menuItemClose = new JMenuItem(Localizer.getString("moviemanager.menu.file.closedb"),'C'); //$NON-NLS-1$
		menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuItemClose.setActionCommand("Open"); //$NON-NLS-1$
		menuItemClose.addActionListener(new MovieManagerCommandCloseDatabase());

		menuFile.addSeparator();
		menuFile.add(menuItemClose);
		this.menuItemClose = menuItemClose;
		
		/* The Import menuItem. */
		JMenuItem menuImport = new JMenuItem(Localizer.getString("moviemanager.menu.file.import"),'I'); //$NON-NLS-1$
		menuImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuImport.addActionListener(new MovieManagerCommandImport());

		menuFile.addSeparator();
		menuFile.add(menuImport);
		this.menuImport = menuImport;
	
		/* The Export menuItem. */
		JMenuItem menuExport = new JMenuItem(Localizer.getString("moviemanager.menu.file.export"),'E'); //$NON-NLS-1$
		menuExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuExport.addActionListener(new MovieManagerCommandExport());

		menuFile.addSeparator();
		menuFile.add(menuExport);
		this.menuExport = menuExport;
		
		/* MenuItem Exit. */
		JMenuItem menuItemExit = new JMenuItem(Localizer.getString("moviemanager.menu.file.exit"),'X'); //$NON-NLS-1$
		menuItemExit.setActionCommand("Exit"); //$NON-NLS-1$
		menuItemExit.addActionListener(new MovieManagerCommandExit());

		menuFile.addSeparator();
		menuFile.add(menuItemExit);
		this.menuItemNew = menuItemNew;
		
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
		menuDatabase = new JMenu(Localizer.getString("moviemanager.menu.database")); //$NON-NLS-1$
		menuDatabase.setMnemonic('D');

		/* MenuItem Queries. */
		menuItemQueries = new JMenuItem(Localizer.getString("moviemanager.menu.database.queries"),'Q'); //$NON-NLS-1$
		menuItemQueries.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuItemQueries.setActionCommand("Queries"); //$NON-NLS-1$
		menuItemQueries.addActionListener(new MovieManagerCommandQueries());
		menuDatabase.add(menuItemQueries);

		/* A separator. */
		menuDatabase.addSeparator();

		/* MenuItem Folders. */
		menuItemFolders = new JMenuItem(Localizer.getString("moviemanager.menu.database.folders"),'F'); //$NON-NLS-1$
		menuItemFolders.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemFolders.setActionCommand("Folders"); //$NON-NLS-1$
		menuItemFolders.addActionListener(new MovieManagerCommandFolders());
		menuDatabase.add(menuItemFolders);

		/* MenuItem AddField. */
		menuItemAddField = new JMenuItem(Localizer.getString("moviemanager.menu.database.additionalinfofields"),'I'); //$NON-NLS-1$
		menuItemAddField.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, (java.awt.event.InputEvent.ALT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuItemAddField.setActionCommand("AdditionalInfoFields"); //$NON-NLS-1$
		menuItemAddField.addActionListener(new MovieManagerCommandAdditionalInfoFields());
		menuDatabase.add(menuItemAddField);

		/* MenuItem AddList. */
		menuItemAddList = new JMenuItem(Localizer.getString("moviemanager.menu.database.lists"),'L'); //$NON-NLS-1$
		menuItemAddList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemAddList.setActionCommand("setLists"); //$NON-NLS-1$
		menuItemAddList.addActionListener(new MovieManagerCommandLists(MovieManager.getDialog()));
		menuDatabase.add(menuItemAddList);

		/* MenuItem Convert Database. */
		menuItemConvertDatabase = new JMenuItem(Localizer.getString("moviemanager.menu.database.covertdb")); //$NON-NLS-1$
		menuItemConvertDatabase.setActionCommand("Convert Database"); //$NON-NLS-1$
		menuItemConvertDatabase.addActionListener(new MovieManagerCommandConvertDatabase());
		menuDatabase.add(menuItemConvertDatabase);

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
		menuTools = new JMenu(Localizer.getString("moviemanager.menu.tools")); //$NON-NLS-1$
		menuTools.setMnemonic('T');
	
		/* MenuItem Preferences.
	         For some reason, addMovie KeyEvent.VK_A doesn't work when focused
	         on the selected movie or the filter*/

		menuItemPrefs = new JMenuItem(Localizer.getString("moviemanager.menu.tools.preferences"),'P'); //$NON-NLS-1$
		menuItemPrefs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemPrefs.setActionCommand("Preferences"); //$NON-NLS-1$
		menuItemPrefs.addActionListener(new MovieManagerCommandPrefs());
	
		menuTools.add(menuItemPrefs);
		
		JMenuItem menuItemAddMultipleMovies = new JMenuItem(Localizer.getString("moviemanager.menu.tools.addmultiplemovies"),'M'); //$NON-NLS-1$
		menuItemAddMultipleMovies.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemAddMultipleMovies.setActionCommand("Add Multiple Movies"); //$NON-NLS-1$
		menuItemAddMultipleMovies.addActionListener(new MovieManagerCommandAddMultipleMoviesByFile());

		menuTools.addSeparator();
		menuTools.add(menuItemAddMultipleMovies);
		this.menuItemAddMultipleMovies = menuItemAddMultipleMovies;
	
		JMenuItem menuItemUpdateIMDbInfo = new JMenuItem(Localizer.getString("moviemanager.menu.tools.updateIMDbInfo"),'U'); //$NON-NLS-1$
		menuItemUpdateIMDbInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemUpdateIMDbInfo.setActionCommand("Update IMDb Info"); //$NON-NLS-1$
		menuItemUpdateIMDbInfo.addActionListener(new MovieManagerCommandUpdateIMDBInfo());

		menuTools.add(menuItemUpdateIMDbInfo);
		this.menuItemUpdateIMDbInfo = menuItemUpdateIMDbInfo;
		
		JMenuItem menuItemReportGenerator = new JMenuItem(Localizer.getString("moviemanager.menu.tools.reportgenerator"),'R'); //$NON-NLS-1$
		menuItemReportGenerator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemReportGenerator.setActionCommand("Report Generator"); //$NON-NLS-1$
		menuItemReportGenerator.addActionListener(new MovieManagerCommandReportGenerator());

		menuTools.addSeparator();
		menuTools.add(menuItemReportGenerator);
		this.menuItemReportGenerator = menuItemReportGenerator;
		
		/* All done. */
		log.debug("Creation of the Tools menu done."); //$NON-NLS-1$
		return menuTools;
	}

	/**
	 * Creates the lists menu.
	 *
	 * @return The lists menu.
	 **/
	protected JMenu createMenuLists() {
		log.debug("Start creation of the Lists menu."); //$NON-NLS-1$
		menuLists = new JMenu(Localizer.getString("moviemanager.menu.lists")); //$NON-NLS-1$
		menuLists.setMnemonic('L');

		log.debug("Creation of the Lists menu done."); //$NON-NLS-1$
		return menuLists;
	}


	/**
	 * Creates the views menu.
	 *
	 * @return The views menu.
	 **/
	protected JMenu createMenuView() {

		log.debug("Start creation of the View menu."); //$NON-NLS-1$
		menuView = new JMenu("View"); //$NON-NLS-1$
		menuView.setMnemonic('V');

		final HashMap templates = MovieManager.getIt().getHTMLTemplates();
		String currentTemplateName = MovieManager.getConfig().getHTMLTemplateName();
		String currentTemplateStyleName = MovieManager.getConfig().getHTMLTemplateStyleName();

		try {
			ButtonGroup buttonGroup = new ButtonGroup();

			Set keys = templates.keySet();
			Iterator keysIt = keys.iterator();

			while (keysIt.hasNext()) {
				String templateName = (String) keysIt.next();
				ModelHTMLTemplate template = (ModelHTMLTemplate) templates.get(templateName);
				ArrayList styles = template.getStyles();

				JMenu templateMenu = new JMenu(template.getName()) {
					public JToolTip createToolTip() {
						JMultiLineToolTip tooltip = new JMultiLineToolTip();
						tooltip.setComponent(this);
						return tooltip;
					}
				};
				templateMenu.setToolTipText(template.getInfo());

				boolean itemAdded = false;

				for (int u = 0; u < styles.size(); u++) {
					ModelHTMLTemplateStyle style = (ModelHTMLTemplateStyle) styles.get(u);

					boolean selected = style.getName().equals(currentTemplateStyleName);
					final JMenuItem m = new JRadioButtonMenuItem(style.getName(), selected)  {
						public JToolTip createToolTip() {
							JMultiLineToolTip tooltip = new JMultiLineToolTip();
							tooltip.setComponent(this);
							return tooltip;
						}
					};
					m.setToolTipText(style.getInfo());

					m.setActionCommand(template.getName());
					buttonGroup.add(m);
					m.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String styleName = ((JRadioButtonMenuItem) e.getSource()).getText();
							String command = m.getActionCommand();
							ModelHTMLTemplate t = (ModelHTMLTemplate) templates.get(command);

							MovieManager.getConfig().setHTMLTemplate(t, t.getStyle(styleName));
							MovieManager.getDialog().setTabbedMovieInfoTitle();

							MovieManagerCommandSelect.execute();
						}
					});

					templateMenu.add(m);	
					itemAdded = true;
				}

				if (itemAdded) 
					menuView.add(templateMenu);
				else {

					boolean selected = template.getName().equals(currentTemplateName);
					JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(template.getName(), selected) {
						public JToolTip createToolTip() {
							JMultiLineToolTip tooltip = new JMultiLineToolTip();
							tooltip.setComponent(this);
							return tooltip;
						}
					};
					menuItem.setToolTipText(template.getInfo());

					buttonGroup.add(menuItem);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String styleName = ((JRadioButtonMenuItem) e.getSource()).getText();
							String command = e.getActionCommand();
							ModelHTMLTemplate t = (ModelHTMLTemplate) templates.get(command);

							MovieManager.getConfig().setHTMLTemplate(t, t.getStyle(styleName));

							String tabName;

							if (MovieManager.getConfig().getHTMLTemplate().hasStyles())
								tabName = MovieManager.getConfig().getHTMLTemplateStyleName();
							else
								tabName = MovieManager.getConfig().getHTMLTemplate().getName();

							// Setting the style name as title of tab bar.
							MovieManager.getDialog().setTabbedMovieInfoTitle(1, tabName);
							MovieManagerCommandSelect.execute();
						}
					});
					menuView.add(menuItem);
				}
			}

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e); //$NON-NLS-1$
		}

		log.debug("Creation of the View done."); //$NON-NLS-1$
		return menuView;
	}


	/**
	 * Creates the help menu.
	 *
	 * @return The help menu.
	 **/
	protected JMenu createMenuHelp() {
		log.debug("Start creation of the Help menu."); //$NON-NLS-1$
		menuHelp = new JMenu(Localizer.getString("moviemanager.menu.help")); //$NON-NLS-1$
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
		/* MenuItem About. */
		menuItemAbout = new JMenuItem(Localizer.getString("moviemanager.menu.help.about")); //$NON-NLS-1$
		menuItemAbout.setActionCommand("About"); //$NON-NLS-1$
		menuItemAbout.addActionListener(new MovieManagerCommandAbout());
		menuHelp.add(menuItemAbout);
		/* All done. */
		log.debug("Creation of the Help menu done."); //$NON-NLS-1$
		return menuHelp;
	}



	/**
	 * Sets enabled/disabled the related database components.
	 **/
	public void setDatabaseComponentsEnable(boolean enable) {
	
		if (menuItemClose != null)
			menuItemClose.setEnabled(enable);

		if (menuImport != null)
			menuImport.setEnabled(enable);

		if (menuExport != null)
			menuExport.setEnabled(enable);

		
		if (menuDatabase != null) {

			if (menuItemQueries != null)
				menuItemQueries.setEnabled(enable);

			if (menuItemFolders != null)
				menuItemFolders.setEnabled(enable);

			if (menuItemAddField != null)
				menuItemAddField.setEnabled(enable);

			if (menuItemAddList != null)
				menuItemAddList.setEnabled(enable);

			if (menuItemConvertDatabase != null)
				menuItemConvertDatabase.setEnabled(enable);
			
		}

		if (menuItemPrefs != null)
			menuItemPrefs.setEnabled(enable);

		if (menuItemAddMultipleMovies != null)
			menuItemAddMultipleMovies.setEnabled(enable);

		if (menuItemUpdateIMDbInfo != null)
			menuItemUpdateIMDbInfo.setEnabled(enable);

		if (menuItemReportGenerator != null)
			menuItemReportGenerator.setEnabled(enable);

		MovieManager.getDialog().getToolBar().setEnableButtons(enable, MovieManager.isApplet());

		/* The JTree. */
		MovieManager.getDialog().getMoviesList().setEnabled(enable);

		/* Filter textField. */
		MovieManager.getDialog().getFilter().setEnabled(enable);

		/* Makes the list selected. */
		MovieManager.getDialog().getMoviesList().requestFocus(true);
	}

	public JMenuItem getAboutButton() {
		return menuItemAbout;
	}

	public JMenuItem getPreferencesButton() {
		return menuItemPrefs;
	}

	public JMenuItem getExitButton() {
		return menuItemExit;
	}
}
