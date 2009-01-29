package net.sf.xmm.moviemanager.gui.menubar;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.MovieManagerConfig.InternalConfig;

public interface MovieManagerMenuBar {

	/**
	 * Sets enabled/disabled the related database components.
	 **/
	public void setDatabaseComponentsEnable(boolean enable);
	public JMenuBar getNewInstance(InternalConfig internalConfig, MovieManagerConfig config);
	public void newVersionAvailable(String newVersion, String info);

	JMenu getMenuFile();
	JMenu getMenuDatabase();
	JMenu getMenuTools();
	JMenu getMenuLists(); 
	JMenu getMenuView(); 
	JMenu getMenuHelp(); 
	JMenu getMenuUpdate();
	
	JMenuItem getAboutButton();
	JMenuItem getPreferencesButton();
	JMenuItem getExitButton();
}
