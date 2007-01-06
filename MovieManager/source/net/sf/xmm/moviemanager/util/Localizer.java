package net.sf.xmm.moviemanager.util;

import java.util.MissingResourceException;


public class Localizer {

    final static TMXResourceBundle res_en;
    final static TMXResourceBundle res_no = null;
    // test cache system
    //final static TMXResourceBundle res_it = new TMXResourceBundle("tmx/sample_tmx.xml", "it", "src/com/tecnick/tmxjavabridge/test/test_tmx_it.obj");
    
    static String temp;
    
    static {
     
        //res_no = new TMXResourceBundle("no_NO.tmx", "no-NO");
        res_en = new TMXResourceBundle("MovieManager.tmx", "en-EN");
        //res_it = new TMXResourceBundle("tmx/sample_tmx.xml", "it", "src/com/tecnick/tmxjavabridge/test/test_tmx_it.obj");
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
