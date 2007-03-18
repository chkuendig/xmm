/**
 * @(#)DriveInfo.java 1.0 16.03.07 (dd.mm.yy)
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
import stec.sfc.Win32.*;
import java.io.IOException;
import java.lang.System;

public class DriveInfo {

	static Logger log = Logger.getRootLogger();
	
	public static boolean initialized = true;
	boolean valid = true;
	boolean removable = false;
	
	static {
		
		try {
			LibPathHacker.addDir(FileUtil.getFile("lib/driveinfo").getAbsolutePath());
			System.load(FileUtil.getFile("lib/driveinfo/sfc_w2k.dll").getAbsolutePath());
			System.load(FileUtil.getFile("lib/driveinfo/sfc_w9x.dll").getAbsolutePath());
			System.load(FileUtil.getFile("lib/driveinfo/sfc_wnt.dll").getAbsolutePath());
		
		} catch(IOException e) {
			initialized = false;
		}
	}
		
	public boolean isValid() {
		return valid;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public boolean isRemovable() {
		return removable;
	}
	
	public DriveInfo(String filePath) {
	
		if (!initialized)
			return;
		
		try {

			Drive drive = new Drive(filePath, false);

			int type = drive.getType();

			if (type == DriveType.REMOVABLE || type == DriveType.CDROM || type == DriveType.NO_ROOT_DIR)
				removable = true;
			
		} catch(Win32Exception ex) { 
			valid = false;
			log.error("Drive is not available: " + ex.getErrorCode());
			log.error(ex.getMessage(), ex);
			return;
		}
	}
}
