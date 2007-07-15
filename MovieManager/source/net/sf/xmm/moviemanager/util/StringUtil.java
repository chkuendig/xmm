/**
 * @(#)StringUtil.java 1.0 05.06.07 (dd.mm.yy)
 *
 * Copyright (2003) Bro3
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.util;

import org.apache.log4j.Logger;

public class StringUtil {
    
    static Logger log = Logger.getRootLogger();
    
  
	public static String cleanInt(String toBeCleaned, int debugMode) {
            
		boolean start = false;
		String result = "";
            
		if (debugMode == 1)
			log.debug("toBeCleaned:" + toBeCleaned);
            
		for (int i = 0; i < toBeCleaned.length(); i++) {
                
			if (Character.isDigit(toBeCleaned.charAt(i)))
				start = true;
                
			if (start) {
                    
				if (debugMode == 1 && toBeCleaned.charAt(i) == ',')
					log.debug("NumValComma:"+ Character.getNumericValue(toBeCleaned.charAt(i)));
                    
				if (Character.getNumericValue(toBeCleaned.charAt(i)) != -1 || toBeCleaned.charAt(i) == ',' || toBeCleaned.charAt(i) == '.') {
                        
					if (Character.isDigit(toBeCleaned.charAt(i))) {
						result += toBeCleaned.charAt(i);
					}
					else {
						break;
					}
				}
			}
		}
            
		if (debugMode == 1)
			log.debug("result:" + result);
            
		return result;
	}

	public static String removeDoubleSpace(String str) {

		/* Remove all double white space */
		StringBuffer stringBuff = new StringBuffer();
		for(int x = 0; x < str.length(); x++)
			if (str.charAt(x) != ' ' || (str.length() != x+1 && str.charAt(x+1) != ' '))
				stringBuff = stringBuff.append(str.charAt(x));
		
		return stringBuff.toString();
	}
} 
