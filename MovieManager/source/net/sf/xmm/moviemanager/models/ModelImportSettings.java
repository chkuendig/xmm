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

    public JTable table;
    
    public ModelImportSettings() {
    
    }
    
    public ModelImportSettings(JTable table) {
        this.table = table;
        importMode = 1;
    }
        
    public int multiAddSelectOption = 0;
    public int importMode = 0;
    public boolean overwriteWithImdbInfo = false;
    public int excelTitleColumn = 0;
    public int excelLocationColumn = -1;
    public String addToThisList = "";
    public String filePath = "";
    public boolean extremeOriginalLanguage = true;
    public String coverPath = "";
}

