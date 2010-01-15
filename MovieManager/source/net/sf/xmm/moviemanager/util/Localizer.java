package net.sf.xmm.moviemanager.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.MissingResourceException;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;


public class Localizer {

	static Logger log = Logger.getLogger(Localizer.class);
	
	final static TMXResourceBundle resource;

	static String temp;

	static {

		if (MovieManager.isApplet() || MovieManager.getIt().isSandbox()) {

			InputStream inpuStream = null;

			try {
				inpuStream = FileUtil.getResourceAsStream("/config/MovieManager.tmx");
				// inpuStream = DialogMovieManager.applet.getClass().getResourceAsStream("/MovieManager.tmx");
			} catch (Exception e) {

			}

			if (inpuStream != null)
				resource = new TMXResourceBundle(null, inpuStream, "");
			else
				resource = null;

		}
		else {
			          
			// TMXResourceBundle searches the file in half a dozen places anyway, so it's 
			// probably better to not use a path here... (actually, it fails on mac if we don't
			// do so)

			// First try to get the file from the current dir
			File f = FileUtil.getFile("config/MovieManager.tmx");
			
			
			if (f == null || !f.isFile()) {
				 f = new File(FileUtil.getFileURL(System.getProperty("user.dir") + "/config/MovieManager.tmx").getPath());
			}

			// If no success the MovieManager.tmx is grabbed from the MovieManager.jar file.
			if (!f.isFile()) {
				InputStream inpuStream = FileUtil.getResourceAsStream("/config/MovieManager.tmx");

				if (inpuStream != null) {
					log.debug("tmx ResourceAsStream");
					resource = new TMXResourceBundle(null, inpuStream, null);
				}
				else {
					log.error("TMX lanuguage file not accessible");
					resource = null;
				}
			}
			else {
				resource = new TMXResourceBundle(f.getAbsolutePath());
			}
	
			java.util.HashMap<String, String> langs = resource.getLanuages();
			
			int counter = 0;
		
			log.debug("Loaded languages:");
			for (String key : langs.keySet()) {
				log.debug(counter++ + ":" + key);
			}
		}
		
		try {
			
			String locale = MovieManager.getConfig().getLocale();			
			
			resource.load();
			
			if (!resource.setDefaultLangauge("en-US"))
				log.warn("Failed to set default language");
			
			// If failed to load language, load default en-US
			if (!resource.setLangauge(locale))
				resource.setLangauge("en-US");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {

		try {
			temp = resource.getString(key);
			
			return temp;
		} catch (MissingResourceException e) {
			log.warn("Invalid key:" + key);
			return '!' + key + '!';
		}
	}
	
	public static String [] getAvailableLanguages() {
		HashMap<String, String> langauges = resource.getLanuages();
		String [] langs = new String[langauges.size()];
		
		int index = 0;
		for (String key : langauges.keySet()) {
			langs[index] = key;
			index++;
		}
		return langs;
	}
	
}
