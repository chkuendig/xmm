/**
 * @(#)ModelImportExportSettings.java 1.0 26.01.06 (dd.mm.yy)
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

import java.io.File;
import java.util.ArrayList;


import net.sf.xmm.moviemanager.util.Localizer;

public class ModelImportExportSettings {
	
	public final static int IMPORT_MODE_COUNT = 5;
	
	public final static int IMPORT_MODE_TEXT = 0;
	public final static int IMPORT_MODE_CSV = 1;
    public final static int IMPORT_MODE_EXCEL = 2;
	//public final static int IMPORT_MODE_XML = 3;
	public final static int IMPORT_MODE_XML_DATABASE = 3;
    public final static int IMPORT_MODE_EXTREME = 4;

    public final static int EXPORT_MODE_COUNT = 4;
	
	public final static int EXPORT_MODE_CSV = 0;
    public final static int EXPORT_MODE_EXCEL = 1;
	//public final static int EXPORT_MODE_XML = 2;
	public final static int EXPORT_MODE_XML_DATABASE = 2;
    public final static int EXPORT_MODE_HTML = 3;
	
    public static String [] encodings = new String[] {"US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"};
    
    // Stores in array for easy access through indexes.
    public static final String [] importTypes = new String[ModelImportExportSettings.IMPORT_MODE_COUNT];
    public static final String [] exportTypes = new String[ModelImportExportSettings.EXPORT_MODE_COUNT];
      
    void setImportTypeValues() {
    	importTypes[ModelImportExportSettings.IMPORT_MODE_TEXT] = "Text File";
    	importTypes[ModelImportExportSettings.IMPORT_MODE_CSV] = "CSV File";
    	importTypes[ModelImportExportSettings.IMPORT_MODE_EXCEL] = "Excel Spreadsheet";
    	//importTypes[ModelImportExportSettings.IMPORT_MODE_XML] = "XML File";
    	importTypes[ModelImportExportSettings.IMPORT_MODE_XML_DATABASE] = "XML Database";
    	importTypes[ModelImportExportSettings.IMPORT_MODE_EXTREME] = "Extreme Movie Manager";
    } 
    
    
    void setExportTypeValues() {
    	exportTypes[ModelImportExportSettings.EXPORT_MODE_CSV] = "CSV File";
    	exportTypes[ModelImportExportSettings.EXPORT_MODE_EXCEL] = "Excel Spreadsheet";
    	//exportTypes[ModelImportExportSettings.EXPORT_MODE_XML] = Localizer.getString("DialogExport.panel-xml-export.title");
    	exportTypes[ModelImportExportSettings.EXPORT_MODE_XML_DATABASE] = "XML Database";
    	exportTypes[ModelImportExportSettings.EXPORT_MODE_HTML] = Localizer.getString("DialogExport.panel-html-export.title");
    } 
    
    
    
    public ModelImportExportSettings() {
    	setImportTypeValues();
    	setExportTypeValues();
    }
    
        
    public enum ImdbImportOption {off, displayList, selectFirst, selectFirstOrAddToSkippedList, 
    	selectIfOnlyOneHit, selectIfOnlyOneHitOrAddToSkippedList}
       
    
   // public int multiAddIMDbSelectOption = -1;
  
    public ImdbImportOption multiAddIMDbSelectOption = ImdbImportOption.off;
    
    public boolean isIMDbEnabled() {
    	return multiAddIMDbSelectOption != ImdbImportOption.off;
    }
    
    public int mode = IMPORT_MODE_TEXT;
    public boolean overwriteWithImdbInfo = false;
    
    public String addToThisList = null;
     
    
    public String skippedListName = "Importer-skipped";
    
    public String filePath = "";
    private File file = null;
    public boolean extremeOriginalLanguage = true;
    public String coverPath = "";
    
    public char csvSeparator = ',';
    public String textEncoding = null;
    
    public String htmlTitle = null;
    public boolean htmlAlphabeticSplit = false;
    public boolean htmlSimpleMode = false;
    
    public String getFilePath() {
    	return filePath;	
    }
    
    public File getFile() {
    	if (file == null)
    		file = new File(filePath);
    	
    	return file;
    }
    
    public void setFile(File f) {
    	file = f;
    }
    
    public String getHTMLTitle() {
    	return htmlTitle;	
    }
    
    public boolean getHTMLAlphabeticSplit() {
    	return htmlAlphabeticSplit;	
    }
        
    public boolean getIsHTMLSimpleMode() {
    	return htmlSimpleMode;	
    }
    
    public char getCSVSeparator() {
    	return csvSeparator;	
    }
    
    public String getTextEncoding() {
    	return textEncoding;	
    }
    
    public ArrayList<String> getAddToThisList() {
    	ArrayList<String> l = new ArrayList<String>();
    	
    	if (addToThisList != null)
    		l.add(addToThisList);
    	
    	return l;
    }
    
}

