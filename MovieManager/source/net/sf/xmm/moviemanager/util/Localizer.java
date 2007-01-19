package net.sf.xmm.moviemanager.util;

import net.sf.xmm.moviemanager.MovieManager;

//import org.xnap.commons.i18n.I18n;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;


public class Localizer {

   // static public I18n i18n;
    
    final static TMXResourceBundle res_en;
    final static TMXResourceBundle res_no = null;
    // test cache system
    //final static TMXResourceBundle res_it = new TMXResourceBundle("tmx/sample_tmx.xml", "it", "src/com/tecnick/tmxjavabridge/test/test_tmx_it.obj");
    
    static String temp;
    
    static {
     
        
       /* try {
            
            
            InputStream inputStream = url.openStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            
            
        } catch(IOException ioex) {
            ioex.printStackTrace();
        }
        */
        File file = new File("MovieManager.tmx");
        
        if (MovieManager.isApplet()) {
        
            InputStream inpuStream = null;
            
        try {
            inpuStream = MovieManager.getIt().applet.getClass().getResourceAsStream("/laban/MovieManager.tmx");
        } catch (Exception e) {
            
        }
        
        if (inpuStream != null)
            res_en = new TMXResourceBundle(null, inpuStream, "en-EN", "");
        else
            res_en = null;
        
        }
        else {
        //res_no = new TMXResourceBundle("no_NO.tmx", "no-NO");
        //res_en = new TMXResourceBundle(FileUtil.getFile("MovieManager.tmx").getAbsolutePath(), "en-EN");
        res_en = new TMXResourceBundle(file.toString(), "en-EN");
        
        
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
