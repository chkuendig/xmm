package net.sf.xmm.moviemanager.util;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;

//import org.xnap.commons.i18n.I18n;

import java.io.File;
import java.io.InputStream;
import java.util.MissingResourceException;


public class Localizer {

	static Logger log = Logger.getRootLogger();
	
	// static public I18n i18n;

	final static TMXResourceBundle res_en;
	final static TMXResourceBundle res_no = null;

	static String temp;

	static {

		if (MovieManager.isApplet()) {

			InputStream inpuStream = null;

			try {
				inpuStream = FileUtil.getResourceAsStream("/MovieManager.tmx");
				// inpuStream = DialogMovieManager.applet.getClass().getResourceAsStream("/MovieManager.tmx");
			} catch (Exception e) {

			}

			if (inpuStream != null)
				res_en = new TMXResourceBundle(null, inpuStream, "en-EN", "");
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

			
			File f = FileUtil.getFile("MovieManager.tmx");
			
			if (f == null || !f.isFile()) {
				 f = new File(FileUtil.getFileURL(System.getProperty("user.dir") + "/MovieManager.tmx").getPath());
			}

			if (f == null || !f.isFile()) {
				InputStream inpuStream = FileUtil.getResourceAsStream("/MovieManager.tmx");

				if (inpuStream != null) {
					System.err.println("tmx ResourceAsStream");
					res_en = new TMXResourceBundle(null, inpuStream, "en-EN", "");
				}
				else {
					log.error("TMX lanuguage file not accessible");
					res_en = null;
				}
			}
			else {
				res_en = new TMXResourceBundle(f.getAbsolutePath(), "en-EN");
				System.err.println("tmx getFile !");
			}
	
			//i18n = new org.xnap.commons.i18n.I18n(res_en);
		}
	}

	private Localizer() {
	}


	public static String getString(String key) {

		try {

			temp = ""; //res_no.getString(key, "no-NO");

			if ("".equals(temp)) {
				temp = res_en.getString(key, "");
			}
			return temp;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
