/**
 * @(#)FileUtil.java 1.0 23.03.05 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;

import java.io.*;

public class FileUtil {
    
    public static void writeToFile(String fileName, StringBuffer data) {
	try {
	    FileOutputStream fileStream = new FileOutputStream(new File(fileName));
	    for (int u = 0; u < data.length(); u++)
		fileStream.write(data.charAt(u));
	    fileStream.close();
	    
	} catch (Exception e) {
	    MovieManager.log.error("Exception:"+ e.getMessage());
	}
    }
} 
