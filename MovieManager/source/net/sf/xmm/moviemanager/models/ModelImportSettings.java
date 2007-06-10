/**
 * @(#)IMDbInfoUpdater.java 1.0 26.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.models;

import javax.swing.JTable;


public class ModelImportSettings {

	
	public final static int IMPORT_COUNT = 5;
	
	public final static int IMPORT_TEXT = 0;
	public final static int IMPORT_EXCEL = 1;
	public final static int IMPORT_XML = 2;
    public final static int IMPORT_CSV = 3;
    public final static int IMPORT_EXTREME = 4;
	
    public JTable table;
    
    public ModelImportSettings() {}
    
    public ModelImportSettings(JTable table) {
        this.table = table;
    }
        
    public int multiAddSelectOption = 0;
    public int importMode = IMPORT_TEXT;
    public boolean overwriteWithImdbInfo = false;
    public String addToThisList = null;
    public String filePath = "";
    public boolean extremeOriginalLanguage = true;
    public String coverPath = "";
    
    public String csvSeparator = "";
}

