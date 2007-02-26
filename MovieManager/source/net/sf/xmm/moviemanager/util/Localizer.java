package net.sf.xmm.moviemanager.util;

import net.sf.xmm.moviemanager.MovieManager;

//import org.xnap.commons.i18n.I18n;

import java.io.File;
import java.io.InputStream;
import java.util.MissingResourceException;


public class Localizer {

   // static public I18n i18n;
    
    final static TMXResourceBundle res_en;
    final static TMXResourceBundle res_no = null;
    
    static String temp;
    
    static {
        
        
        /* try {
         
         
         InputStream inputStream = url.openStream();
         InputStreamReader reader = new InputStreamReader(inputStream);
         
         
         } catch(IOException ioex) {
         ioex.printStackTrace();
         }
         */
                
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
        	res_en = new TMXResourceBundle("MovieManager.tmx", "en-EN");
                  
            
            //res_it = new TMXResourceBundle("tmx/sample_tmx.xml", "it", "src/com/tecnick/tmxjavabridge/test/test_tmx_it.obj");
            
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
