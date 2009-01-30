package net.sf.xmm.moviemanager.util.mac;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.simplericity.macify.eawt.DefaultApplication;

import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.gui.menubar.MovieManagerMenuBar;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Pictures;

import com.apple.eawt.*;
//import com.apple.mrj.MRJApplicationUtils;

public class OSXAdapter extends ApplicationAdapter /*implements com.apple.mrj.MRJAboutHandler */ {


	static Logger log = Logger.getRootLogger();

	// pseudo-singleton model; no point in making multiple instances
	// of the EAWT application or our adapter
	private static OSXAdapter						theAdapter;
	private static com.apple.eawt.Application		theApplication;

	private static DefaultApplication defaultApplication;
	
	// reference to the app where the existing quit, about, prefs code is
	private DialogMovieManager						mainApp;
	
	MovieManagerMenuBar menuBar = null;
	
	/*
	private JMenuItem aboutButton;
	private JMenuItem prefsButton;
	private JMenuItem quitButton;
*/
	
	private OSXAdapter (DialogMovieManager inApp) {
		mainApp = inApp;

		System.err.println("menu bar:" + mainApp.getJMenuBar());

		menuBar = (MovieManagerMenuBar) mainApp.getJMenuBar();

		menuBar.setDatabaseComponentsEnable(false);

		
		
		/*
		JMenu helpMenu = mainApp.getJMenuBar().getMenu(4);
		this.aboutButton = helpMenu.getItem(7);
		aboutButton.setVisible(false);
		helpMenu.getItem(0).setVisible(false);
		helpMenu.remove(4);
		helpMenu.add(new JPanel(), 4);
		helpMenu.remove(6);
		helpMenu.add(new JPanel(), 6);

		JMenu toolsMenu = mainApp.getJMenuBar().getMenu(2);
		this.prefsButton = toolsMenu.getItem(0);
		prefsButton.setVisible(false);
		toolsMenu.remove(1);
		toolsMenu.add(new JPanel(), 1);

		JMenu fileMenu = mainApp.getJMenuBar().getMenu(0);
		this.quitButton = fileMenu.getItem(9);
		quitButton.setVisible(false);
		fileMenu.remove(8);
		fileMenu.add(new JPanel(), 8);
		 */
	}

	// The main entry-point for this functionality.  This is the only method
	// that needs to be called at runtime, and it can easily be done using
	// reflection (see MyApp.java) 
	public static void registerMacOSXApplication(DialogMovieManager inApp) {

		System.err.println("registerMacOSXApplication");
		System.err.println("theApplication:" + theApplication);

		//System.setProperty("apple.laf.useScreenMenuBar", "true");  
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MyApplication");
		
		if (theApplication == null) {
			theApplication = new com.apple.eawt.Application();
			defaultApplication = new DefaultApplication();
		}      

		if (theAdapter == null) {
			theAdapter = new OSXAdapter(inApp);
		}
		
		System.err.println("theAdapter:" + theAdapter);
		
		theApplication.addApplicationListener(theAdapter);
		
		//inApp.setApplication(defaultApplication);  
		
		Image image = FileUtil.getImage("/images/film.png");
		
		BufferedImage bufferedImage = Pictures.toBufferedImage(image);
		
		defaultApplication.setApplicationIconImage(bufferedImage);
		
		//MRJApplicationUtils.registerAboutHandler(theAdapter);
		
		//BufferedImage newIcon = new BufferedImage(originalIcon.getWidth(), originalIcon.getHeight(), BufferedImage.TYPE_INT_ARGB);
		//theApplication.setApplicationIconImage(image);
	}

	// implemented handler methods.  These are basically hooks into existing 
	// functionality from the main app, as if it came over from another platform.
	public void handleAbout(ApplicationEvent ae) {

		System.err.println("handleAbout");

		if (mainApp != null) {
			menuBar.getAboutButton().doClick();
			ae.setHandled(true);			
		} else {
			throw new IllegalStateException("handleAbout: instance detached from listener");
		}
	}
	
	public void handleAbout() {
		System.err.println("handleAbout");
	}
		
	public void handlePreferences(ApplicationEvent ae) {

		if (mainApp != null) {
			menuBar.getPreferencesButton().doClick();
			ae.setHandled(true);
		} else {
			throw new IllegalStateException("handlePreferences: instance detached from listener");
		}
	}

	// Another static entry point for EAWT functionality.  Enables the 
	// "Preferences..." menu item in the application menu. 
	public static void enablePrefs(boolean enabled) {

		if (theApplication == null) {
			theApplication = new com.apple.eawt.Application();
		}
		theApplication.setEnabledPreferencesMenu(enabled);
	}

	public void handleQuit(ApplicationEvent ae) {

		if (mainApp != null) {
			/*	
			/	You MUST setHandled(false) if you want to delay or cancel the quit.
			/	This is important for cross-platform development -- have a universal quit
			/	routine that chooses whether or not to quit, so the functionality is identical
			/	on all platforms.  This example simply cancels the AppleEvent-based quit and
			/	defers to that universal method.
			 */
			ae.setHandled(false);
			
			menuBar.getExitButton().doClick();
		} else {
			throw new IllegalStateException("handleQuit: instance detached from listener");
		}
	}
}
