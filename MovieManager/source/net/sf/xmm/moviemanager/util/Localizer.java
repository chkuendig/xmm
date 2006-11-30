package net.sf.xmm.moviemanager.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Localizer {

    final static TMXResourceBundle res_en;
    final static TMXResourceBundle res_no = null;
        
    static String temp;
    
    static {
     
        //res_no = new TMXResourceBundle("no_NO.tmx", "no-NO");
        res_en = new TMXResourceBundle("MovieManager.tmx", "");
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
