package net.sf.xmm.moviemanager.util;

import java.io.File;
import java.io.InputStream;
import java.util.MissingResourceException;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;


public class Localizer {

	static Logger log = Logger.getLogger(Localizer.class);
	
	// static public I18n i18n;

	final static TMXResourceBundle res_en;
	final static TMXResourceBundle res_no = null;

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
				res_en = new TMXResourceBundle(null, inpuStream, "");
			else
				res_en = null;

		}
		else {
			/*
            File file = FileUtil.getFile("MovieManager.tmx");
            //res_no = new TMXResourceBundle("no_NO.tmx", "no-NO");
            //res_en = new TMXResourceBundle(FileUtil.getFile("MovieManager.tmx").getAbsolutePath(), "en-EN");
            res_en = new TMXResourceBundle(file.toString(), "en-EN");
			 */

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
					res_en = new TMXResourceBundle(null, inpuStream, null);
				}
				else {
					log.error("TMX lanuguage file not accessible");
					res_en = null;
				}
			}
			else {
				res_en = new TMXResourceBundle(f.getAbsolutePath());
			}
	
			java.util.HashMap<String,String> langs = res_en.getLanuages();
			
			int counter = 0;
			
			System.err.println("loaded lanuage files:" + langs.keySet().size());
			
			for (String key : langs.keySet()) {
				System.err.println("Language("+counter+"):" + key);
			}
			
			//i18n = new org.xnap.commons.i18n.I18n(res_en);
		}
		
		try {
			
			String locale = MovieManager.getConfig().getLocale();
			System.err.println("locale:" + locale);
			
			// If failed to load language, load defulat en-US
			//if (!res_en.loadLanguage(locale))
			//	res_en.loadLanguage("en-US");		
			res_en.load();
			
			if (!res_en.setDefaultLangauge("en-US"))
				log.warn("Failed to set default language");
			
			if (!res_en.setLangauge(locale))
				res_en.setLangauge("en-US");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {

		try {
			temp = ""; //res_no.getString(key, "no-NO");
			temp = res_en.getString(key, "");
			
			return temp;
		} catch (MissingResourceException e) {
			log.warn("Invalid key:" + key, e);
			return '!' + key + '!';
		}
	}
}
