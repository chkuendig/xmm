/**
 * @(#)DatabaseExtreme.java 1.0 24.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.database;

import java.sql.ResultSet;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.models.ModelExtremeMovie;

import org.apache.log4j.Logger;

public class DatabaseExtreme {
    
	Logger log = Logger.getLogger(getClass());
    
    SQL _sql;
    
    private boolean _inicialized = false;
    private boolean setUp = false;
    
    public DatabaseExtreme(String filePath) {
	_sql = new SQL(filePath, "MSAccess");
    }
    
     protected boolean isSetUp() {
	return setUp;
    }
    
    /**
     * SetUp...
     **/
    public void setUp() {
	
	try {
	    if (!_inicialized) {
		_sql.setUp();
		_inicialized = true;
		setUp = true;
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    e.printStackTrace();
	    _inicialized = false;
	}
	
	
    }
    
    /** 
     * Returns a List of MovieModels that contains all the movies in the
     * current database.
     **/
    public ArrayList getMovies() {
	
	try {
	    _sql.clear();
	}
	catch (Exception e) {
	    log.error(e.getMessage());
	}
	
		int counter = 0;
	
	ArrayList list = new ArrayList(100);
	
	try {
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery("SELECT [Movies].[InternetID],"+
						    "[Movies].[ScriptUsed],"+
						    "[Movies].[Cover],"+
						    "[Movies].[Year],"+
						    "[Movies].[Title],"+
						    "[Movies].[OriginalTitle],"+
						    "[Movies].[Director],"+
						    "[Movies].[Writer],"+
						    "[Movies].[Genre],"+
						    "[Movies].[Subgenre],"+
						    "[Movies].[Rating],"+
						    "[Movies].[PersonalRating],"+
						    "[Movies].[Plot],"+
						    "[Movies].[Actors],"+
						    "[Movies].[Notes],"+
						    "[Movies].[Seen],"+ /* -1 = seen, 0 = unseen */
						    "[Movies].[Country],"+
						    "[Movies].[Language],"+
						    "[Movies].[MPAA],"+
						    "[Movies].[OriginalLanguage],"+
						    "[Movies].[Color],"+ /* 1 = color 0 = black&White  */
						    "[Movies].[Subtitles],"+
						    "[Movies].[Length],"+
						    "[Movies].[Disk],"+
						    "[Movies].[Resolution],"+
						    "[Movies].[Codec],"+
						    "[Movies].[FPS],"+
						    "[Movies].[Bitrate],"+
						    "[Movies].[AudioCodec],"+
						    "[Movies].[Filesize],"+
						    "[Movies].[SampleRate],"+
						    "[Movies].[AudioBitRate],"+
						    "[Movies].[Channels],"+
						    "[Movies].[Media],"+
						    "[Movies].[VideoDVD],"+
						    "[Movies].[AudioDVD],"+
						    "[Movies].[MovieFile1],"+
						    "[Movies].[MovieFile2],"+
						    "[Movies].[MovieFile3],"+
						    "[Movies].[MovieFile4],"+
						    "[Movies].[MovieFile5],"+
						    "[Movies].[MovieFile6] "+
						    "FROM [Movies];");
	    
	    /* Processes the result set till the end... */
	    while (resultSet.next()) {
	    	
		list.add(new ModelExtremeMovie(resultSet.getString("InternetID"), resultSet.getString("ScriptUsed"), resultSet.getString("Cover"), resultSet.getString("Year"), resultSet.getString("Title"), resultSet.getString("Director"), resultSet.getString("Writer"), resultSet.getString("Genre"), resultSet.getString("Subgenre"), resultSet.getString("Rating"), resultSet.getString("PersonalRating"), resultSet.getString("Plot"), resultSet.getString("Actors"), resultSet.getString("Notes"), resultSet.getBoolean("Seen"), resultSet.getString("OriginalTitle"), resultSet.getString("Country"), resultSet.getString("Language"), resultSet.getString("OriginalLanguage"), resultSet.getString("MPAA"), resultSet.getString("Color"), resultSet.getString("Subtitles"), resultSet.getString("Length"), resultSet.getString("Disk"), resultSet.getString("Codec"), resultSet.getString("Resolution"), resultSet.getString("FPS"), resultSet.getString("Bitrate"), resultSet.getString("AudioCodec"), resultSet.getString("Filesize"), resultSet.getString("SampleRate"), resultSet.getString("AudioBitRate"), resultSet.getString("Channels"), resultSet.getString("Media"), resultSet.getString("VideoDVD"), resultSet.getString("AudioDVD"), resultSet.getString("MovieFile1"), resultSet.getString("MovieFile2"), resultSet.getString("MovieFile3"), resultSet.getString("MovieFile4"), resultSet.getString("MovieFile5"), resultSet.getString("MovieFile5")));
		counter++;
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e);
	    }
	}
	
    /* Returns the list model... */
    return list;
    }
}
    
