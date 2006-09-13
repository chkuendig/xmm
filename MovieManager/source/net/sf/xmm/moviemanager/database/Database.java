/**
 * @(#)Database.java 1.0 29.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.database;

import net.sf.xmm.moviemanager.DialogDatabase;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.*;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;

abstract public class Database {
    
    static Logger log = Logger.getRootLogger();
    
    SQL _sql;
    
    protected boolean _initialized = false;
    
    protected boolean setUp = false;
    
    protected String _path;
    
    protected String databaseType = ""; /* Values: "MSAccess", "HSQL" ,"MySQL" */

    protected String errorMessage = "";
    
    protected boolean fatalError = false;

    /* Used to return the record count. */
    private int recordCount = 0;
    
    /* Can be either: '\"' (doublequote) or: '`' (backtick) */
    protected String quote = "\"";
    
    protected String generalInfoString = "General Info";
    protected String additionalInfoString = "Additional Info";
    protected String extraInfoString = "Extra Info";
    
    protected String quotedGeneralInfoString = quote + generalInfoString + quote;
    protected String quotedAdditionalInfoString = quote + additionalInfoString + quote;
    protected String quotedExtraInfoString = quote + extraInfoString + quote;
    
    protected String generalInfoEpisodeString = "General Info Episodes";
    protected String additionalInfoEpisodeString = "Additional Info Episodes";
    protected String extraInfoEpisodeString = "Extra Info Episodes";
    
    protected String quotedGeneralInfoEpisodeString = quote + generalInfoEpisodeString + quote;
    protected String quotedAdditionalInfoEpisodeString = quote + additionalInfoEpisodeString + quote;
    protected String quotedExtraInfoEpisodeString = quote + extraInfoEpisodeString + quote;
    
    
    /**
     * The constructor. Initialized _sql;
     *
     * filePath should contain the file path for the database.
     **/
    protected Database(String filePath) {
	_path = filePath;
    }
    
    public boolean isSetUp() {
	return setUp;
    }
    
    public String getErrorMessage() {
	return errorMessage;
    }
    
    public void resetError() {
	errorMessage = "";
    }
    
    public String getDatabaseType() {
	return databaseType;
    }
    
    /**
     * SetUp...
     **/
    public boolean setUp() {
			
	try {
	    if (!_initialized) {
		_sql.setUp();
		_initialized = true;
		setUp = true;
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    errorMessage = e.getMessage();
	    _initialized = false;
	}
	
	return _initialized;
    }
    
    
    /**
     * Finalize...
     **/
    public void finalize() {
	try {
	    _sql.finalize();
	    _initialized = false;
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	}
    }
    
    
    /**
     * Returns _initialized...
     **/
    public boolean isInitialized() {
	return _initialized;
    }
    
    
    public boolean getFatalError() {
	return fatalError;
    }

    /**
     * Returns the path...
     **/
    public String getPath() {
	return _path;
    }
    
    
    /**
     * Returns the recordCount...
     **/
    public int getRecordCount() {
	return recordCount;
    }    
    
    
    /**
     * Not really in use
     **/
    String getSQLReservedKeywords() {
	
	DatabaseMetaData metaData = _sql.getMetaData();
	
	String sqlKeywords = "";
	
	if (metaData == null)
	    return "";
	
	try {
	    sqlKeywords = metaData.getSQLKeywords();
	}
	catch (SQLException e) {
	    log.error("Exception:" + e.getMessage());
	    checkErrorMessage(e.getMessage());
	}
	return sqlKeywords;
    }
    
    
    /**
     * Returns the result of the query (formated) in a string.
     **/
    public String getQueryResult(String query) {
	
	//long t1 = System.currentTimeMillis();
	String queryResult = "";
	StringBuffer data = new StringBuffer(10000);
	String tempData = "";
	
	recordCount = 0;	

	try {
	    ResultSet resultSet = _sql.executeQueryForwardOnly(query);
	    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
	    ArrayList names = new ArrayList();
	    
	    data.append("  |-----\n");
	    
	    for (int i=1; i<=resultSetMetaData.getColumnCount(); i++) {
		names.add(resultSetMetaData.getColumnName(i));
	    }
	    //synchronized (data) {
	    if (resultSet.next()) {
		
		do {
		    recordCount++;
		    
		    for (int i=0; i<names.size(); i++) {
			data.append("  |   ");
			data.append((String)names.get(i));
			data.append(": ");
			    
			if ((tempData = resultSet.getString(i+1)) != null)
			    data.append(tempData);
			data.append("\n");
		    }
		    data.append("  |-----\n");
		} while (resultSet.next());
	    } else {
		for (int i=0; i<names.size(); i++) {
		    data.append("  |   ");
		    data.append((String)names.get(i));
		    data.append(": \n");
		}
		data.append("  |-----\n");
	    }
	    
	    queryResult = data.toString();
	    
	} catch (Exception e) {
	    log.error("Exception: "+ e.getMessage());
	    checkErrorMessage(e.getMessage());
	    queryResult = e.getMessage();
	    recordCount = 0;
	} finally {
	    
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: "+ e.getMessage());
	    }
	}
	/* Returns the data... */
	return queryResult;
    }
    
    
    /**
     * Returns the number of rows in the Extra Info id column
     **/
    public int getDatabaseSize() {
    
	int size = -1;
	
	try {
	    /* Gets the number of rows */
	    ResultSet resultSet = _sql.executeQuery("SELECT COUNT(*) FROM (SELECT id FROM \"Extra Info\") "+
						    ";");
	    
	    if (resultSet.next())
		size = resultSet.getInt(1);
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    size = -1;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	return size;
    }
    

    void checkErrorMessage(String message) {
	
	if (message == null) {
	    errorMessage = "";
	    return;
	}
	
	if (message.indexOf("Connection reset") != -1) {
	    errorMessage = "Connection reset";
	    MovieManager.getIt().processDatabaseError();
	}
	else if ((message.indexOf("is full") != -1) || (message.indexOf("Error writing file") != -1)) {
	    errorMessage = "MySQL server is out of space";
	    MovieManager.getIt().processDatabaseError();
	}
	
	else if ((message.indexOf("Data truncation: Data too long for column 'CoverData'") != -1)) {
	    errorMessage = "Data truncation cover";
	}
	
	else if ((message.indexOf("You have an error in your SQL syntax") != -1)) {
	    errorMessage = "SQL Syntax error";
	}
	else if (message.indexOf("settings' doesn't exist") != -1) {
	    errorMessage = message;
	    fatalError = true;
	}
	else
	    errorMessage = "";
	
	if (!errorMessage.equals(""))
	    DialogDatabase.showDatabaseMessage(MovieManager.getIt(), this, "");
    }

    
    /**
     * Returns the Resolution with index index named name...
     **/
    public String getString(String query, String field) {
	
	String data = "";
	try {
	    ResultSet resultSet = _sql.executeQuery(query);
	    
	    if (resultSet.next() && resultSet.getString(field) != null) {
		data = resultSet.getString(field);
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the Duration with index index named name...
     **/
    public int getInt(String query, String field) {
       
	int data = -1;
	try {
	    ResultSet resultSet = _sql.executeQuery(query);
	    
	    if (resultSet.next() && resultSet.getString(field) != null) {
		data = resultSet.getInt(field);
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the data... */
	return data;
    }
    
    

    /**
     * Returns the if the movie at the specific index is a member of the specified list with name name...
     **/
    public boolean getBoolean(String query, String field) {
	
	boolean data = false;
	try {
	    ResultSet resultSet = _sql.executeQuery(query);
	    
	    if (resultSet.next() && resultSet.getString(field) != null) {
		data = resultSet.getBoolean(field);
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the data... */
	return data;
    }
    
    
    /**
     * Returns the CD_Cases with index index named name...
     **/
    public double getDouble(String query, String field) {
      
	double data = -1;
	try {
	    ResultSet resultSet = _sql.executeQuery(query);
	    
	    if (resultSet.next() && resultSet.getString(field) != null) {
		data = resultSet.getDouble(field);
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the data... */
	return data;
    }



    /**
     * Returns the active additional info fields.
     **/
    public int [] getActiveAdditionalInfoFields() {
	
	String data = "";
	String columnName = "Active Additional Info Fields";
	
	try {
	    ResultSet resultSet = _sql.executeQuery("SELECT \"Settings\".\""+ columnName +"\" "+
						    "FROM \"Settings\" "+
						    "WHERE \"Settings\".id=1;");
	    
	    if (resultSet.next() && resultSet.getString(columnName) != null) {
		data = resultSet.getString(columnName);
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	StringTokenizer tokenizer = new StringTokenizer(data, ":");
	int [] activeFields = new int[tokenizer.countTokens()];
    
	if (activeFields.length == 0) {
	    int counter = getAdditionalInfoFieldNames().size() + getExtraInfoFieldNames().size();
	
	    activeFields = new int[counter];
	
	    for (int i = 0; i < counter; i++) {
		activeFields[i] = i;
	    }
	}
	else {
	    int counter = 0;
	    while (tokenizer.hasMoreTokens()) {
		activeFields[counter++] = Integer.parseInt(tokenizer.nextToken());
	    }	    
	}
	/* Returns the data... */
	return activeFields;
    }
    
    
    /**
     * Returns the additional info result set...
     * _sql.clear() is not performed and is the responsibility of the
     * method calling this.
     **/
    protected ResultSet getAdditionalInfoMovieResultSet(int index) {
	
	ResultSet resultSet = null;
	
	try {
	    /* Gets the fixed additional info... */
	    resultSet = _sql.executeQuery("SELECT \"Additional Info\".* "+
					  "FROM \"Additional Info\" "+
					  "WHERE \"Additional Info\".\"ID\"="+index+";");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e);
	    checkErrorMessage(e.getMessage());
	} 
	
	/* Returns the data... */
	return resultSet;
    }
    
    /**
     * Returns the additional_info with index index...
     * _sql.clear() is not performed and is the responsibility of the
     * method calling this.
     **/
    protected ResultSet getAdditionalInfoEpisodeResultSet(int index) {
	
	ResultSet resultSet = null;
	
	try {
	    /* Gets the fixed additional info... */
	    resultSet = _sql.executeQuery("SELECT \"Additional Info Episodes\".* "+
					  "FROM \"Additional Info Episodes\" "+
					  "WHERE \"Additional Info Episodes\".\"ID\"="+index+";");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} 
	
	/* Returns the data... */
	return resultSet;
    }
    
    /**
     * Returns the additional_info with index index...
     * _sql.clear() is not performed and is the responsibility of the
     * method calling this.
     **/
    protected ResultSet getExtraInfoMovieResultSet(int index) {
	
	ResultSet resultSet = null;
	
	try {
	    /* Gets the fixed additional info... */
	    resultSet = _sql.executeQuery("SELECT \"Extra Info\".* "+
					  "FROM \"Extra Info\" "+
					  "WHERE \"Extra Info\".\"ID\"="+index+";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	}
	
	/* Returns the data... */
	return resultSet;
    }
    
    /**
     * Returns the additional_info with index index...
     * _sql.clear() is not performed and is the responsibility of the
     * method calling this.
     **/
    protected ResultSet getExtraInfoEpisodeResultSet(int index) {
	
	ResultSet resultSet = null;
	
	try {
	    /* Gets the fixed additional info... */
	    resultSet = _sql.executeQuery("SELECT \"Extra Info Episodes\".* "+
					  "FROM \"Extra Info Episodes\" "+
					  "WHERE \"Extra Info Episodes\".\"ID\"="+index+";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} 
	
	/* Returns the data... */
	return resultSet;
    }

    
    /**
     * Returns the additional_info with index index...
     **/
    public String getAdditionalInfoString(ModelAdditionalInfo model) {
	
	if (model == null)
	    return "";
	
	//long time = System.currentTimeMillis();
	
	StringBuffer data = new StringBuffer("");
	
	try {
	    /* Gets the fixed additional info... */
	    
	    int [] activeAdditionalInfoFields = MovieManager.getIt().getActiveAdditionalInfoFields();
	    
	    for (int i = 0; i < activeAdditionalInfoFields.length; i++) {
		
		switch (activeAdditionalInfoFields[i]) {
		    
		case 0: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("Subtitles: ");
		    
		    data.append(model.getSubtitles());
		    break;
		}
		
		case 1: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("Duration: ");
		    
		    int duration = model.getDuration();
		    
		    if (duration > 0) {
			
			int hours = duration / 3600;
			int mints = duration / 60 - hours * 60;
			int secds = duration - hours * 3600 - mints *60;
			
			data.append(hours + ":");
			
			if (mints < 10)
			    data.append("0"); 
			data.append(mints + ":");
			
			if (secds < 10)
			    data.append("0"); 
			data.append(secds);
		    }
		    break;
		}
		    
		case 2: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("File Size: ");
		    
		    if (model.getFileSize() > 0) {
			data.append(model.getFileSize());
			data.append(" MB");
		    }
		    break;
		}    
		
		case 3: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("CDs: ");
		    
		    if (model.getCDs() > 0)
			data.append(model.getCDs());
		    break;
		}
		
		case 4: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("CD Cases: ");
		    
		    if (model.getCDCases() > 0)
			data.append(model.getCDCases());
		    break;
		}
		
		case 5: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("Resolution: ");
		    
		    data.append(model.getResolution());
		    break;
		}
		
		case 6: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("Video Codec: ");
		    
		    data.append(model.getVideoCodec());
		    break;
		}
		    
		case 7: {
		    if (data.length() != 0)
			data.append("\n");
		    data.append("Video Rate: ");
		    
		    if (!"".equals(model.getVideoRate())) {
			data.append(model.getVideoRate());
			data.append(" fps");
		    }
		    break;
		}
		    
		case 8: {
		    if (data.length() != 0)
			data.append("\n");
		     
		    data.append("Video Bit Rate: ");
		    
		    if (!"".equals(model.getVideoBitrate())) {
			data.append(model.getVideoBitrate());
			data.append(" kbps");
		    }
		    break;
		}
		
		case 9: {
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Audio Codec: ");
		    data.append(model.getAudioCodec());
		    break;
		}
		
		case 10: {
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Audio Rate: ");
		    
		    if (!"".equals(model.getAudioRate())) {
			data.append(model.getAudioRate());
			data.append(" Hz");
		    }
		    break;
		}
		   
		case 11: {
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Audio Bit Rate: ");
		    
		    if (!"".equals(model.getAudioBitrate())) {
			data.append(model.getAudioBitrate());
			data.append(" kbps");
		    }
		    break;
		}
		
		case 12: {
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Audio Channels: ");
		    data.append(model.getAudioChannels());
		    
		    break;
		}
		    
		case 13: {
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Location: ");
		    data.append(model.getFileLocation());
		    
		    break;
		}
		    
		case 14: {
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("File Count: ");
		    if (model.getFileCount() > 0)
			data.append(String.valueOf(model.getFileCount()));
		    break;
		}
		
		case 15: {
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Container: ");
		    data.append(model.getContainer());
		    break;
		}
		
		case 16: {
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append("Media Type: ");
		    data.append(model.getMediaType());
		    break;
		}
		    
		default : {
		    
		    int columnIndex = activeAdditionalInfoFields[i]-17;
		    
		    if (data.length() != 0)
			data.append("\n");
		    
		    data.append(model.getExtraInfoFieldName(columnIndex) + ": ");
		    data.append(model.getExtraInfoFieldValue(columnIndex));
		}
		}
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the database... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	
	/* Returns the data... */
	return data.toString();
    }
    
    /** 
     * Returns a ModelAdditionalInfo on a specific movie/episode
     **/
    public ModelAdditionalInfo getAdditionalInfo(int index, boolean episode) {
	ModelAdditionalInfo additionalInfo = null;
	
	String subtitles = "";
	int duration = 0;
	int fileSize = 0;
	int cDs = 0;
	double cDCases = 0;	
	String resolution = "";
	String videoCodec = "";
	String videoRate = "";
	String videoBitrate = "";
	String audioCodec = "";
	String audioRate ="";
	String audioBitrate = "";
	String audioChannels = "";
	String fileLocation = "";
	int fileCount = 0;
	String container = "";
	String mediaType = "";
	
	try {
	    
	    ResultSet resultSet;
	    
	    if (episode)
		resultSet = getAdditionalInfoEpisodeResultSet(index);
	    else
		resultSet = getAdditionalInfoMovieResultSet(index);
	    
	    /* Processes the result set till the end... */
	    if (resultSet.next()) {
	    
		if ((subtitles = resultSet.getString("Subtitles")) == null)
		    subtitles = "";
	    
		duration = resultSet.getInt("Duration");
		fileSize = resultSet.getInt("File Size");
		cDs = resultSet.getInt("CDs");
		cDCases = resultSet.getInt("CD Cases");
	    	    
		if ((resolution = resultSet.getString("Resolution")) == null)
		    resolution = "";
	    
		if ((videoCodec = resultSet.getString("Video Codec")) == null)
		    videoCodec = "";
	    
		if ((videoRate = resultSet.getString("Video Rate")) == null)
		    videoRate = "";
	    
		if ((videoBitrate = resultSet.getString("Video Bit Rate")) == null)
		    videoBitrate = "";
	    
		if ((audioCodec = resultSet.getString("Audio Codec")) == null)
		    audioCodec = "";
	    
		if ((audioRate = resultSet.getString("Audio Rate")) == null)
		    audioRate = "";
	    
		if ((audioBitrate = resultSet.getString("Audio Bit Rate")) == null)
		    audioBitrate = "";
	    
		audioChannels = resultSet.getString("Audio Channels");
	    
		if ((fileLocation = resultSet.getString("File Location")) == null)
		    fileLocation = "";
	    
		fileCount = resultSet.getInt("File Count");
	    
		if ((container = resultSet.getString("Container")) == null)
		    container = "";
	    
		if ((mediaType = resultSet.getString("Media Type")) == null)
		    mediaType = "";
	    
	    }
	    
	    /* Getting extra info fields */
	    
	    _sql.clear();
	    
	    ArrayList extraInfoFieldNames = new ArrayList();
	    ArrayList extraInfoFieldValues = new ArrayList();
	    
	    if (episode)
		resultSet = getExtraInfoEpisodeResultSet(index);
	    else
		resultSet = getExtraInfoMovieResultSet(index);
	    
	    ResultSetMetaData metaData = resultSet.getMetaData();
		
	    String tempName;
	    String tempValue;
		
	    ArrayList extraFieldnames = getExtraInfoFieldNames();
		
	    boolean next = resultSet.next();
		
	    /* Getting the value for each field */
	    for (int i = 0; next && i < extraFieldnames.size(); i++) {
		    
		tempName = (String) extraFieldnames.get(i);
		    
		/* First column after the ID column is at index 2 */
		tempValue = resultSet.getString(i+2);
		
		if (tempValue == null)
		    tempValue = "";
		    
		extraInfoFieldNames.add(tempName);
		extraInfoFieldValues.add(tempValue);
	    }
	    
	    additionalInfo = new ModelAdditionalInfo(subtitles, duration, fileSize, cDs, cDCases, resolution, videoCodec, videoRate, videoBitrate, audioCodec, audioRate, audioBitrate, audioChannels, fileLocation, fileCount, container, mediaType);
	    
	    additionalInfo.setExtraInfoFieldValues(extraInfoFieldValues);
	    ModelAdditionalInfo.setExtraInfoFieldNames(extraFieldnames);
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}

	/* Returns the list model... */
	return additionalInfo;
    }
    
    
    public boolean listColumnExist(String columnName) {
	
	ArrayList columnNames = getListsColumnNames();
	
	while (!columnNames.isEmpty()) {
	    if (columnNames.get(0).equals(columnName))
		return true;
	    columnNames.remove(0);
	}
	return false;
    }
    
    
    /**
     * Returns the extra info field names in a ArrayList.
     **/                
    public ArrayList getListsColumnNames() {
	ArrayList list = new ArrayList();
	try {
	    ResultSetMetaData metaData = _sql.executeQuery("SELECT "+ quote + "Lists" +quote + ".* FROM "+ quote + "Lists"+ quote + " WHERE 1=0;").getMetaData();
	    
	    for (int i = 1; i <= metaData.getColumnCount(); i++) {
		if (!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
		    list.add(metaData.getColumnName(i));
		}
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return list;
    }
    
    
    /**
     * Returns the Extra Info field names in a ArrayList.
     **/
    public ArrayList getExtraInfoFieldNames() {
      
	ArrayList list = new ArrayList();
	try {
	    String query = "SELECT " + quotedExtraInfoString +".* "+ "FROM "+ quotedExtraInfoString +" WHERE 1=0;";
	    
	    ResultSetMetaData metaData = _sql.executeQuery(query).getMetaData();
	    
	    for (int i = 1; i <= metaData.getColumnCount(); i++) {
		if (!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
		    list.add(metaData.getColumnName(i));
		}
	    }
      
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	    /* Returns the list model... */
	}
	return list;
    }
    
    
    /**
     * Returns the additional info field names in a ArrayList.
     **/
    public ArrayList getAdditionalInfoFieldNames() {
	ArrayList list = new ArrayList();
	String query = "SELECT " + quotedAdditionalInfoString + ".* "+ "FROM "+ quotedAdditionalInfoString +" WHERE 1=0;";
	
	try {
	    ResultSetMetaData metaData = _sql.executeQuery(query).getMetaData();
	    
	    for (int i = 1; i <= metaData.getColumnCount(); i++) {
		if(!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
		    list.add(metaData.getColumnName(i));
		}
	    }
      
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	    /* Returns the list model... */
	    
	}
	return list;
    }
    
    
    /**
     * Returns the additional info field names in a ArrayList.
     **/
    public ArrayList getGeneralInfoMovieFieldNames() {
	
	ArrayList list = new ArrayList();
	String query = "SELECT " + quotedGeneralInfoString +".* "+ "FROM "+ quotedGeneralInfoString +" WHERE 1=0;";
	
	try {
	    //_sql.clear();
	    
	    ResultSet rs = _sql.executeQuery(query);
	    
	    ResultSetMetaData metaData = rs.getMetaData();
	    
	    for (int i = 1; i <= metaData.getColumnCount(); i++) {
		if(!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
		    list.add(metaData.getColumnName(i));
		}
	    }
      
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	    /* Returns the list model... */
	    
	}
	return list;
    }
    
    
    /**
     * Returns the table names.
     **/
    public ArrayList getTableNames() {
	ArrayList list = new ArrayList();
	try {
	    
	    String[] tableTypes = { "TABLE" };
	    DatabaseMetaData dbMetadata = _sql.getMetaData();
	    
	    ResultSet allTables = dbMetadata.getTables(null, null, null, tableTypes);
	    
	    while (allTables.next())
		list.add(allTables.getString("TABLE_NAME"));
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	return list;
    }
    
    
    /**
     * Adds the fields of movie with 'index' to the \"Additional Info\" table and
     * returns the number of updated rows.
     **/
    public int addAdditionalInfo(ModelAdditionalInfo model) {
	return addAdditionalInfo(model.getKey(), model);
    }
    
    
    /**
     * Adds the fields of movie with 'index' to the \"Additional Info\" table and
     * returns the number of updated rows.
     **/
    public int addAdditionalInfo(int index, ModelAdditionalInfo model) {
	int value = 0;
	
	try {
	    
	    PreparedStatement statement;
	    
	    if (databaseType.equals("MySQL")) {
		statement = _sql.prepareStatement("INSERT INTO Additional_Info "+
						  "(ID,Subtitles,Duration,File_Size,CDs,CD_Cases,"+
						  "Resolution,Video_Codec,Video_Rate,"+
						  "Video_Bit_Rate,Audio_Codec,Audio_Rate,"+
						  "Audio_Bit_Rate,Audio_Channels,File_Location,"+
						  "File_Count,Container, Media_Type) "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
	    }
	    else {
		statement = _sql.prepareStatement("INSERT INTO \"Additional Info\" "+
						  "(\"ID\",\"Subtitles\",\"Duration\",\"File Size\",\"CDs\",\"CD Cases\","+
						  "\"Resolution\",\"Video Codec\",\"Video Rate\","+
						  "\"Video Bit Rate\",\"Audio Codec\",\"Audio Rate\","+
						  "\"Audio Bit Rate\",\"Audio Channels\",\"File Location\","+
						  "\"File Count\",\"Container\", \"Media Type\") "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
	    }
	    
	    statement.setInt(1, index);
	    statement.setString(2, model.getSubtitles());
     
	    if (model.getDuration() != -1) {
		statement.setInt(3, model.getDuration());
	    } else {
		statement.setNull(3, Types.INTEGER);
	    }
	    if (model.getFileSize() != -1) {
		statement.setInt(4, model.getFileSize());
	    } else {
		statement.setNull(4, Types.INTEGER);
	    }
	    if (model.getCDs() != -1) {
		statement.setInt(5, model.getCDs());
	    } else {
		statement.setNull(5, Types.INTEGER);
	    }
	    if (model.getCDCases() != -1) {
		statement.setDouble(6, model.getCDCases());
	    } else {
		statement.setNull(6, Types.DOUBLE);
	    }
	    statement.setString(7, model.getResolution());
	    statement.setString(8, model.getVideoCodec());
	    statement.setString(9, model.getVideoRate());
	    statement.setString(10, model.getVideoBitrate());
	    statement.setString(11, model.getAudioCodec());
	    statement.setString(12, model.getAudioRate());
	    statement.setString(13, model.getAudioBitrate());
	    statement.setString(14, model.getAudioChannels());
	    statement.setString(15, model.getFileLocation());
	    
	    if (model.getFileCount() != -1) {
		statement.setDouble(16, model.getFileCount());
	    } else {
		statement.setNull(16, Types.DOUBLE);
	    }
	    statement.setString(17, model.getContainer());
	    statement.setString(18, model.getMediaType());
	    
	    value = statement.executeUpdate();
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    public int addAdditionalInfoEpisode(ModelAdditionalInfo model) {
	return addAdditionalInfoEpisode(model.getKey(), model);
    }
    
    
    /**
     * Adds the fields of movie with 'index' to the additional info table and
     * returns the number of updated rows.
     **/
    public int addAdditionalInfoEpisode(int index, ModelAdditionalInfo model) {
	int value = 0;
	
	try {
	    PreparedStatement statement;
	    
	    if (databaseType.equals("MySQL")) {
		statement = _sql.prepareStatement("INSERT INTO Additional_Info_Episodes "+
						  "(ID, Subtitles,Duration,File_Size,CDs, "+
						  "CD_Cases,Resolution,Video_Codec,"+
						  "Video_Rate,Video_Bit_Rate,Audio_Codec, "+
						  "Audio_Rate,Audio_Bit_Rate,Audio_Channels, "+
						  "File_Location, File_Count, Container, Media_Type) "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
	    }
	    else {
		statement = _sql.prepareStatement("INSERT INTO \"Additional Info Episodes\" "+
						  "(\"ID\", \"Subtitles\",\"Duration\",\"File Size\",\"CDs\", "+
						  "\"CD Cases\",\"Resolution\",\"Video Codec\","+
						  "\"Video Rate\",\"Video Bit Rate\",\"Audio Codec\", "+
						  "\"Audio Rate\",\"Audio Bit Rate\",\"Audio Channels\", "+
						  "\"File Location\", \"File Count\", \"Container\", \"Media Type\") "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
	    }
	    
	    statement.setInt(1, index);
	    statement.setString(2, model.getSubtitles());
	    
	    if (model.getDuration() != -1)
		statement.setInt(3, model.getDuration());
	    else
		statement.setNull(3, Types.INTEGER);
	    
	    if (model.getFileSize() != -1)
		statement.setInt(4, model.getFileSize());
	    else 
		statement.setNull(4, Types.INTEGER);
	    
	    if (model.getCDs() != -1)
		statement.setInt(5, model.getCDs());
	    else
		statement.setNull(5, Types.INTEGER);
	    
	    if (model.getCDCases() != -1)
		statement.setDouble(6, model.getCDCases());
	    else
		statement.setNull(6, Types.DOUBLE);
	    
	    statement.setString(7, model.getResolution());
	    statement.setString(8, model.getVideoCodec());
	    statement.setString(9, model.getVideoRate());
	    statement.setString(10, model.getVideoBitrate());
	    statement.setString(11, model.getAudioCodec());
	    statement.setString(12, model.getAudioRate());
	    statement.setString(13, model.getAudioBitrate());
	    statement.setString(14, model.getAudioChannels());
	    statement.setString(15, model.getFileLocation());
	    
	    if (model.getFileCount() != -1)
		statement.setInt(16, model.getFileCount());
	    else
		statement.setNull(16, Types.INTEGER);
	    
	    statement.setString(17, model.getContainer());
	    statement.setString(18, model.getMediaType());
	    
	    value = statement.executeUpdate();
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
  
    
    
    /**
     * Sets the fields of movie index to the \"Additional Info\" table and returns the number of
     * updated rows.
     **/
    public int setAdditionalInfo(int index, ModelAdditionalInfo model) {
	int value = 0;
	
	try {

	    PreparedStatement statement;
	    
	    if (databaseType.equals("MySQL")) {
		
		statement = _sql.prepareStatement("UPDATE Additional_Info "+
						  "SET Additional_Info.Subtitles=?, "+
						  "Additional_Info.Duration=?, "+
						  "Additional_Info.File_Size=?, "+
						  "Additional_Info.CDs=?, "+
						  "Additional_Info.CD_Cases=?, "+
						  "Additional_Info.Resolution=?, "+
						  "Additional_Info.Video_Codec=?, "+
						  "Additional_Info.Video_Rate=?, "+
						  "Additional_Info.Video_Bit_Rate=?, "+
						  "Additional_Info.Audio_Codec=?, "+
						  "Additional_Info.Audio_Rate=?, "+
						  "Additional_Info.Audio_Bit_Rate=?, "+
						  "Additional_Info.Audio_Channels=?, "+
						  "Additional_Info.File_Location=?, "+
						  "Additional_Info.File_Count=?, "+
						  "Additional_Info.Container=?, "+
						  "Additional_Info.Media_Type=? "+
						  "WHERE Additional_Info.ID=?;");
	    
	    }
	    else {
		statement = _sql.prepareStatement("UPDATE \"Additional Info\" "+
						  "SET \"Additional Info\".\"Subtitles\"=?, "+
						  "\"Additional Info\".\"Duration\"=?, "+
						  "\"Additional Info\".\"File Size\"=?, "+
						  "\"Additional Info\".\"CDs\"=?, "+
						  "\"Additional Info\".\"CD Cases\"=?, "+
						  "\"Additional Info\".\"Resolution\"=?, "+
						  "\"Additional Info\".\"Video Codec\"=?, "+
						  "\"Additional Info\".\"Video Rate\"=?, "+
						  "\"Additional Info\".\"Video Bit Rate\"=?, "+
						  "\"Additional Info\".\"Audio Codec\"=?, "+
						  "\"Additional Info\".\"Audio Rate\"=?, "+
						  "\"Additional Info\".\"Audio Bit Rate\"=?, "+
						  "\"Additional Info\".\"Audio Channels\"=?, "+
						  "\"Additional Info\".\"File Location\"=?, "+
						  "\"Additional Info\".\"File Count\"=?, "+
						  "\"Additional Info\".\"Container\"=?, "+
						  "\"Additional Info\".\"Media Type\"=? "+
						  "WHERE \"Additional Info\".\"ID\"=?;");
	    }
	    
	    statement.setString(1, model.getSubtitles());
	    
	    if (model.getDuration() != -1) {
		statement.setInt(2, model.getDuration());
	    } else {
		statement.setNull(2, Types.INTEGER);
	    }
	    if (model.getFileSize() != -1) {
		statement.setInt(3, model.getFileSize());
	    } else {
		statement.setNull(3, Types.INTEGER);
	    }
	    if (model.getCDs() != -1) {
		statement.setInt(4, model.getCDs());
	    } else {
		statement.setNull(4, Types.INTEGER);
	    }
	    if (model.getCDCases() != -1) {
		statement.setDouble(5, model.getCDCases());
	    } else {
		statement.setNull(5, Types.DOUBLE);
	    }
	    statement.setString(6, model.getResolution());
	    statement.setString(7, model.getVideoCodec());
	    statement.setString(8, model.getVideoRate());
	    statement.setString(9, model.getVideoBitrate());
	    statement.setString(10, model.getAudioCodec());
	    statement.setString(11, model.getAudioRate());
	    statement.setString(12, model.getAudioBitrate());
	    statement.setString(13, model.getAudioChannels());
	    statement.setString(14, model.getFileLocation());
	    
	    if (model.getFileCount() != -1) {
		statement.setDouble(15, model.getFileCount());
	    } else {
		statement.setNull(15, Types.INTEGER);
	    }
	    statement.setString(16, model.getContainer());
	    statement.setString(17, model.getMediaType());
	    statement.setInt(18, index);
	    
	    value = statement.executeUpdate();
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    /**
     * Sets the fields of movie index to the \"Additional Info\" table and returns the number of
     * updated rows.
     **/
    public int setAdditionalInfoEpisode(int index, ModelAdditionalInfo model) {
	int value = 0;
	
	try {
	    PreparedStatement statement;
	    
	    if (databaseType.equals("MySQL")) {
		statement = _sql.prepareStatement("UPDATE Additional_Info_Episodes "+
						  "SET Additional_Info_Episodes.Subtitles=?, "+
						  "Additional_Info_Episodes.Duration=?, "+
						  "Additional_Info_Episodes.File_Size=?, "+
						  "Additional_Info_Episodes.CDs=?, "+
						  "Additional_Info_Episodes.CD_Cases=?, "+
						  "Additional_Info_Episodes.Resolution=?, "+
						  "Additional_Info_Episodes.Video_Codec=?, "+
						  "Additional_Info_Episodes.Video_Rate=?, "+
						  "Additional_Info_Episodes.Video_Bit_Rate=?, "+
						  "Additional_Info_Episodes.Audio_Codec=?, "+
						  "Additional_Info_Episodes.Audio_Rate=?, "+
						  "Additional_Info_Episodes.Audio_Bit_Rate=?, "+
						  "Additional_Info_Episodes.Audio_Channels=?, "+
						  "Additional_Info_Episodes.File_Location=?, "+
						  "Additional_Info_Episodes.File_Count=?, "+
						  "Additional_Info_Episodes.Container=?, "+
						  "Additional_Info_Episodes.Media_Type=? "+
						  "WHERE Additional_Info_Episodes.ID=?;");
	    }
	    else {
		statement = _sql.prepareStatement("UPDATE \"Additional Info Episodes\" "+
						  "SET \"Additional Info Episodes\".\"Subtitles\"=?, "+
						  "\"Additional Info Episodes\".\"Duration\"=?, "+
						  "\"Additional Info Episodes\".\"File Size\"=?, "+
						  "\"Additional Info Episodes\".\"CDs\"=?, "+
						  "\"Additional Info Episodes\".\"CD Cases\"=?, "+
						  "\"Additional Info Episodes\".\"Resolution\"=?, "+
						  "\"Additional Info Episodes\".\"Video Codec\"=?, "+
						  "\"Additional Info Episodes\".\"Video Rate\"=?, "+
						  "\"Additional Info Episodes\".\"Video Bit Rate\"=?, "+
						  "\"Additional Info Episodes\".\"Audio Codec\"=?, "+
						  "\"Additional Info Episodes\".\"Audio Rate\"=?, "+
						  "\"Additional Info Episodes\".\"Audio Bit Rate\"=?, "+
						  "\"Additional Info Episodes\".\"Audio Channels\"=?, "+
						  "\"Additional Info Episodes\".\"File Location\"=?, "+
						  "\"Additional Info Episodes\".\"File Count\"=?, "+
						  "\"Additional Info Episodes\".\"Container\"=?, "+
						  "\"Additional Info Episodes\".\"Media Type\"=? "+
						  "WHERE \"Additional Info Episodes\".\"ID\"=?;");
	    }
	    
	    statement.setString(1,  model.getSubtitles());
	    
	    if (model.getDuration() != -1) {
		statement.setInt(2, model.getDuration());
	    } else {
		statement.setNull(2, Types.INTEGER);
	    }
	    if (model.getFileSize() != -1) {
		statement.setInt(3, model.getFileSize());
	    } else {
		statement.setNull(3, Types.INTEGER);
	    }
	    if (model.getCDs() != -1) {
		statement.setInt(4, model.getCDs());
	    } else {
		statement.setNull(4, Types.INTEGER);
	    }
	    if (model.getCDCases() != -1) {
		statement.setDouble(5,model.getCDCases());
	    } else {
		statement.setNull(5, Types.DOUBLE);
	    }
	    statement.setString(6, model.getResolution());
	    statement.setString(7, model.getVideoCodec());
	    statement.setString(8, model.getVideoRate());
	    statement.setString(9, model.getVideoBitrate());
	    statement.setString(10, model.getAudioCodec());
	    statement.setString(11, model.getAudioRate());
	    statement.setString(12, model.getAudioBitrate());
	    statement.setString(13, model.getAudioChannels());
	    statement.setString(14, model.getFileLocation());
	    
	    if (model.getFileCount() != -1) {
		statement.setDouble(15, model.getFileCount());
	    } else {
		statement.setNull(15, Types.INTEGER);
	    }
	    statement.setString(16, model.getContainer());
	    statement.setString(17, model.getMediaType());
	    statement.setInt(18, index);
	    
	    value = statement.executeUpdate();
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
 

    
    /**
     * Removes the movie from the database and returns number of updated rows.
     **/
    public abstract int removeMovie(int index);
    
    public abstract int removeEpisode(int index);
    
    /**
     * Deletes the database files.
     **/
    public abstract void deleteDatabase();
    
    /**
     * Checks if the current database is outdated.
     **/
    public abstract boolean isDatabaseOld();
    
    /**
     * Updates the database. Returns 1 if successfull.
     **/
    public abstract int makeDatabaseUpToDate();
    
    /**
     *Makes a backup of the database file(s)
     **/
    public abstract int makeDatabaseBackup();
    
    
    /**
     * Used when converting database to keep the same ID values
     * Adds the fields to the general info table and returns the index added
     **/
    protected int addGeneralInfo(int index, String title, String cover, byte [] coverData, String IMDB,
				 String date, String directedBy, String writtenBy,
				 String genre, String rating, boolean seen, String aka, 
				 String country, String language, String colour,
				 String plot, String cast, String notes, String certification, 
				 String mpaa, String webSoundMix, String webRuntime, String awards) {
	
	try {
	    
	    _sql.clear();
	    
	    /* Adds the info... */      
	    if (index != -1) {
		
		PreparedStatement statement;
		statement = _sql.prepareStatement("INSERT INTO \"General Info\" "+
						  "(\"ID\",\"Title\",\"Cover\",\"Imdb\",\"Date\",\"Directed By\",\"Written By\",\"Genre\",\"Rating\",\"Seen\",\"Aka\",\"Country\",\"Language\",\"Colour\",\"Plot\",\"Cast\",\"Notes\",\"Certification\",\"Mpaa\",\"Sound Mix\",\"Web Runtime\",\"Awards\") "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
		
		statement.setInt(1,index);
		statement.setString(2,title);
		statement.setString(3,cover);
		statement.setString(4,IMDB);
		statement.setString(5,date);
		statement.setString(6,directedBy);
		statement.setString(7,writtenBy);
		statement.setString(8,genre);
		
		if (this instanceof DatabaseAccess)
		    statement.setString(9, rating);
		else {
		    try {
			statement.setDouble(9,Double.parseDouble(rating));
		    }
		    catch (NumberFormatException e) {
			statement.setDouble(9,-1);
		    }
		}
		
		statement.setBoolean(10,seen);
		statement.setString(11,aka);
		statement.setString(12,country);
		statement.setString(13,language);
		statement.setString(14,colour);
		statement.setString(15,plot);
		statement.setString(16,cast);
		statement.setString(17,notes);
		statement.setString(18,certification);
		statement.setString(19,mpaa);
		statement.setString(20,webSoundMix);
		statement.setString(21,webRuntime);
		statement.setString(22,awards);
		statement.executeUpdate();
	    }
      
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    index = -1;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return index;
    }
    
    
    /**
     * Adds the fields to the general info table and returns the index added or
     * -1 if insert failed.
     **/
    public int addGeneralInfo(ModelMovie model) {
	int index = -1;
	
	try {
	    /* Gets the next index... */
	    ResultSet resultSet = _sql.executeQuery("SELECT MAX(ID) "+
						    "FROM " + quotedGeneralInfoString + ";");
	    
	    if (resultSet.next()) {
		index = resultSet.getInt(1)+1;
	    } else {
		index = 0;
	    }
	    _sql.clear();
	    /* Adds the info... */      
	    if (index != -1) {
		
		PreparedStatement statement;
		statement = _sql.prepareStatement("INSERT INTO \"General Info\" "+
						  "(\"ID\",\"Title\",\"Cover\",\"Imdb\",\"Date\",\"Directed By\",\"Written By\",\"Genre\",\"Rating\",\"Seen\",\"Aka\",\"Country\",\"Language\",\"Colour\",\"Plot\",\"Cast\",\"Notes\",\"Certification\",\"Mpaa\",\"Sound Mix\",\"Web Runtime\",\"Awards\") "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
		
		statement.setInt(1, index);
		statement.setString(2, model.getTitle());
		statement.setString(3, model.getCover());
		statement.setString(4, model.getUrlKey());
		statement.setString(5, model.getDate());
		statement.setString(6, model.getDirectedBy());
		statement.setString(7, model.getWrittenBy());
		statement.setString(8, model.getGenre());
		
		if (this instanceof DatabaseAccess)
		    statement.setString(9, model.getRating());
		else {
		    
		    try {
			statement.setDouble(9, Double.parseDouble(model.getRating()));
		    }
		    catch (NumberFormatException e) {
			statement.setDouble(9, -1);
		    }
		}
		
		statement.setBoolean(10, model.getSeen());
		statement.setString(11, model.getAka());
		statement.setString(12, model.getCountry());
		statement.setString(13, model.getLanguage());
		statement.setString(14, model.getColour());
		statement.setString(15, model.getPlot());
		statement.setString(16, model.getCast());
		statement.setString(17, model.getNotes());
		statement.setString(18, model.getCertification());
		statement.setString(19, model.getMpaa());
		statement.setString(20, model.getWebSoundMix());
		statement.setString(21, model.getWebRuntime());
		statement.setString(22, model.getAwards());
		statement.executeUpdate();
	    }
      
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    index = -1;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return index;
    }
    
    /**
     * Adds the fields to the general info table and returns the index added
     * Returns -1 if insert failed.
     **/
    public int addGeneralInfoEpisode(ModelEpisode model) {
	
	int index = -1;
	
	try {
	    /* Gets the next index... */
	    ResultSet resultSet = _sql.executeQuery("SELECT MAX(ID) "+
						    "FROM " + quote + generalInfoEpisodeString + quote + ";");
	    if (resultSet.next()) {
		index = resultSet.getInt(1) + 1;
	    } else {
		index = 0;
	    }
	    _sql.clear();
	    
	    /* Adds the info... */      
	    if (index != -1) {
		
		PreparedStatement statement;
		statement = _sql.prepareStatement("INSERT INTO \"General Info Episodes\" "+
						  "(\"ID\",\"Title\",\"Cover\",\"UrlKey\",\"Date\",\"Directed By\",\"Written By\",\"Genre\",\"Rating\",\"Seen\",\"Aka\",\"Country\",\"Language\",\"Colour\",\"Plot\",\"Cast\",\"Notes\",\"movieID\",\"episodeNr\",\"Certification\",\"Sound Mix\",\"Web Runtime\",\"Awards\") "+
						  "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
		
statement.setInt(1, index);
		statement.setString(2, model.getTitle());
		statement.setString(3, model.getCover());
		statement.setString(4, model.getUrlKey());
		statement.setString(5, model.getDate());
		statement.setString(6, model.getDirectedBy());
		statement.setString(7, model.getWrittenBy());
		statement.setString(8, model.getGenre());
		
		if (this instanceof DatabaseAccess)
		    statement.setString(9, model.getRating());
		else {
		    
		    try {
			statement.setDouble(9,Double.parseDouble(model.getRating()));
		    }
		    catch (NumberFormatException e) {
			statement.setDouble(9, -1);
		    }
		}
		
		statement.setBoolean(10, model.getSeen());
		statement.setString(11, model.getAka());
		statement.setString(12, model.getCountry());
		statement.setString(13, model.getLanguage());
		statement.setString(14, model.getColour());
		statement.setString(15, model.getPlot());
		statement.setString(16, model.getCast());
		statement.setString(17, model.getNotes());
		statement.setInt(18, model.getMovieKey());
		statement.setInt(19, model.getEpisodeNumber());
		statement.setString(20, model.getCertification());
		statement.setString(21, model.getWebSoundMix());
		statement.setString(22, model.getWebRuntime());
		statement.setString(23, model.getAwards());
		statement.executeUpdate();
	    }
      
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    index = -1;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return index;
    }
    
    
  
    

    public synchronized int setGeneralInfo(ModelMovie model) {
	return setGeneralInfo(model.getKey(), model);
    }
    
     /**
     * Sets the fields of movie index to the general info table and returns the number of
     * updated rows.
     **/
    public synchronized int setGeneralInfo(int index, ModelMovie model) {
	int value = 0;
	try {
	    PreparedStatement statement;
	    statement = _sql.prepareStatement("UPDATE \"General Info\" "+
					      "SET \"General Info\".\"Title\"=?, "+
					      "\"General Info\".\"Cover\"=?, "+
					      "\"General Info\".\"Imdb\"=?, "+
					      "\"General Info\".\"Date\"=?, "+
					      "\"General Info\".\"Directed By\"=?, "+
					      "\"General Info\".\"Written By\"=?, "+
					      "\"General Info\".\"Genre\"=?, "+
					      "\"General Info\".\"Rating\"=?, "+
					      "\"General Info\".\"Seen\"=?, "+
					      "\"General Info\".\"Aka\"=?, "+
					      "\"General Info\".\"Country\"=?, "+
					      "\"General Info\".\"Language\"=?, "+
					      "\"General Info\".\"Colour\"=?, "+
					      "\"General Info\".\"Plot\"=?, "+
					      "\"General Info\".\"Cast\"=?, "+
					      "\"General Info\".\"Notes\"=?, "+
					      "\"General Info\".\"Certification\"=?, "+
					      "\"General Info\".\"Mpaa\"=?, "+
					      "\"General Info\".\"Sound Mix\"=?, "+
					      "\"General Info\".\"Web Runtime\"=?, "+
					      "\"General Info\".\"Awards\"=? "+
					      "WHERE \"General Info\".\"ID\"=?;");
	    
	    statement.setString(1, model.getTitle());
	    statement.setString(2, model.getCover());
	    statement.setString(3, model.getUrlKey());
	    statement.setString(4, model.getDate());
	    statement.setString(5, model.getDirectedBy());
	    statement.setString(6, model.getWrittenBy());
	    statement.setString(7, model.getGenre());
	    
	    if (this instanceof DatabaseAccess)
		statement.setString(8,  model.getRating());
	    else {
		
		try {
		    statement.setDouble(8,Double.parseDouble(model.getRating()));
		}
		catch (NumberFormatException e) {
		    statement.setDouble(8,-1);
		}
	    }
	    
	    statement.setBoolean(9, model.getSeen());
	    statement.setString(10, model.getAka());
	    statement.setString(11, model.getCountry());
	    statement.setString(12, model.getLanguage());
	    statement.setString(13, model.getColour());
	    statement.setString(14, model.getPlot());
	    statement.setString(15, model.getCast());
	    statement.setString(16, model.getNotes());
	    statement.setString(17, model.getCertification());
	    statement.setString(18, model.getMpaa());
	    statement.setString(19, model.getWebSoundMix());
	    statement.setString(20, model.getWebRuntime());
	    statement.setString(21, model.getAwards());
	    statement.setInt(22, index);
	    
	    value = statement.executeUpdate();
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    

 
    
    
    public int setGeneralInfoEpisode(ModelEpisode model) {
	return setGeneralInfoEpisode(model.getKey(), model);
    }
    
    /**
     * Sets the fields of movie index to the general info table and returns the number of
     * updated rows.
     **/
    public int setGeneralInfoEpisode(int index, ModelEpisode model) {
	int value = 0;
	try {
	    PreparedStatement statement;
	    statement = _sql.prepareStatement("UPDATE \"General Info Episodes\" "+
					      "SET \"General Info Episodes\".\"Title\"=?, "+
					      "\"General Info Episodes\".\"Cover\"=?, "+
					      "\"General Info Episodes\".\"UrlKey\"=?, "+
					      "\"General Info Episodes\".\"Date\"=?, "+
					      "\"General Info Episodes\".\"Directed By\"=?, "+
					      "\"General Info Episodes\".\"Written By\"=?, "+
					      "\"General Info Episodes\".\"Genre\"=?, "+
					      "\"General Info Episodes\".\"Rating\"=?, "+
					      "\"General Info Episodes\".\"Seen\"=?, "+
					      "\"General Info Episodes\".\"Aka\"=?, "+
					      "\"General Info Episodes\".\"Country\"=?, "+
					      "\"General Info Episodes\".\"Language\"=?, "+
					      "\"General Info Episodes\".\"Colour\"=?, "+
					      "\"General Info Episodes\".\"Plot\"=?, "+
					      "\"General Info Episodes\".\"Cast\"=?, "+
					      "\"General Info Episodes\".\"Notes\"=?, "+
					      "\"General Info Episodes\".\"movieID\"=?, "+
					      "\"General Info Episodes\".\"episodeNr\"=?, "+
					      "\"General Info Episodes\".\"Certification\"=?, "+
					      "\"General Info Episodes\".\"Sound Mix\"=?, "+
					      "\"General Info Episodes\".\"Web Runtime\"=?, "+
					      "\"General Info Episodes\".\"Awards\"=? "+
					      "WHERE \"General Info Episodes\".\"ID\"=?;");
	    
	    statement.setString(1, model.getTitle());
	    statement.setString(2, model.getCover());
	    statement.setString(3, model.getUrlKey());
	    statement.setString(4, model.getDate());
	    statement.setString(5, model.getDirectedBy());
	    statement.setString(6, model.getWrittenBy());
	    statement.setString(7, model.getGenre());
	    
	    if (this instanceof DatabaseAccess)
		statement.setString(8, model.getRating());
	    else {
		
		try {
		    statement.setDouble(8,Double.parseDouble(model.getRating()));
		}
		catch (NumberFormatException e) {
		    statement.setDouble(8,-1);
		}
	    }
	    
	    statement.setBoolean(9, model.getSeen());
	    statement.setString(10, model.getAka());
	    statement.setString(11, model.getCountry());
	    statement.setString(12, model.getLanguage());
	    statement.setString(13, model.getColour());
	    statement.setString(14, model.getPlot());
	    statement.setString(15, model.getCast());
	    statement.setString(16, model.getNotes());
	    statement.setInt(17, model.getMovieKey());
	    statement.setInt(18, model.getEpisodeNumber());
	    statement.setString(19, model.getCertification());
	    statement.setString(20, model.getWebSoundMix());
	    statement.setString(21, model.getWebRuntime());
	    statement.setString(22, model.getAwards());
	    statement.setInt(23, index);
	    
	    value = statement.executeUpdate();
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
 
    
    
    /**
     * Adds the values in fieldValuesList with names in fieldNamesList to movie
     * index in the Extra Info table.
     **/
    public void addExtraInfoMovie(int index, ArrayList fieldNamesList, ArrayList fieldValuesList) {
	
	if (fieldNamesList == null || fieldNamesList.size() == 0)
	    return;

	try {
	    String query = "INSERT INTO " + quotedExtraInfoString + 
		"(ID) "+
		"VALUES("+index+");";
	    
	    /* Creates an empty row... */
	    int value = _sql.executeUpdate(query);
      
	    _sql.clear();
	    
	    if (value == 0) {
		throw new Exception("Can't add row.");
	    }
	    
	    if (setExtraInfoMovie(index, fieldNamesList, fieldValuesList) != 1) {
		throw new Exception("Error occured while updating extra info fields");
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
    }
    
    
    /**
     * Sets the fieldName of movie index in the Extra Info table to fieldValue and
     * returns the number of updated rows.
     **/
    public int setExtraInfoMovie(int index, ArrayList fieldNamesList, ArrayList fieldValuesList) {
     
	int value = 1;
	PreparedStatement statement;
	String query = "";
	
	try {
	    
	    for (int i = 0; i < fieldNamesList.size(); i++) {
		
		_sql.clear();
		
		query = "UPDATE " + quotedExtraInfoString + " "+
		    "SET " + quotedExtraInfoString + "." + 
		    quote + (String) fieldNamesList.get(i) + quote + "=? "+
		    "WHERE " + quotedExtraInfoString + ".ID=?;";
		
		statement = _sql.prepareStatement(query);
		
		statement.setString(1, (String) fieldValuesList.get(i));
		statement.setInt(2, index);
		value = statement.executeUpdate();
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    value = 0;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    /**
     * Adds the values in fieldValuesList with names in fieldNamesList to movie
     * index in the Extra Info table.
     **/
    public void addExtraInfoEpisode(int index, ArrayList fieldNamesList, ArrayList fieldValuesList) {
	
	if (fieldNamesList == null || fieldNamesList.size() == 0)
	    return;
	    
	try {
	    /* Creates an empty row... */
	    int value = _sql.executeUpdate("INSERT INTO " + quotedExtraInfoEpisodeString + " "+
					   "(ID) "+
					   "VALUES("+index+");");
      
	    _sql.clear();
	    
	    if (value == 0) {
		throw new Exception("Can't add row.");
	    }
	    
	    if (setExtraInfoEpisode(index, fieldNamesList, fieldValuesList) == 0)
		throw new Exception("Error occured while adding info to extra info fields");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
    }
    
    
    /**
     * Sets the fieldName of movie index in the Extra Info table to fieldValue and
     * returns the number of updated rows.
     **/
    public int setExtraInfoEpisode(int index, ArrayList fieldNamesList, ArrayList fieldValuesList) {
	
	int value = 0;
	PreparedStatement statement;
	
	try {
	    
	    for (int i = 0; i < fieldNamesList.size(); i++) {
		
		_sql.clear();
		
		statement = _sql.prepareStatement("UPDATE " + quotedExtraInfoEpisodeString + " "+
						  "SET " + quotedExtraInfoEpisodeString + ".\""+ (String) fieldNamesList.get(i) +"\"=? "+
						  "WHERE " + quotedExtraInfoEpisodeString + ".\"ID\"=?;");
		
		statement.setString(1, (String) fieldValuesList.get(i));
		statement.setInt(2, index);
		value = statement.executeUpdate();
	    }
	
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    /**
     * Adds the values in fieldValuesList with names in fieldNamesList to movie
     * index in the extra info table.
     **/
    public void addLists(int index, ArrayList columnNamesList, ArrayList fieldValuesList) {
	try {
	    /* Creates an empty row... */
	    int value = _sql.executeUpdate("INSERT INTO " + quote + "Lists" + quote + " "+
					   "(" + quote + "ID" + quote +") "+
					   "VALUES("+index+");");
	    _sql.clear();
	    
	    if (value == 0) {
		throw new Exception("Can't add row.");
	    }
	    
	    for (int i = 0; i < columnNamesList.size(); i++) {
		
		if (setLists(index,(String) columnNamesList.get(i),(Boolean) fieldValuesList.get(i)) == 0) {
		    throw new Exception("Can't add field name "+(String) columnNamesList.get(i)+".");
		}
	    }     
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
    }
    
    
    /**
     * Sets the fieldName of movie index in the extra info table to fieldValue and
     * returns the number of updated rows.
     **/
    public int setLists(int index, String fieldName, Boolean fieldValue) {
	int value = 0;
	
	try {
	    PreparedStatement statement = _sql.prepareStatement("UPDATE " + quote + "Lists" + quote + " "+
								"SET " + quote + "Lists" + quote + ".\""+ fieldName +"\"=? "+
								"WHERE " + quote + "Lists" + quote + ".\"ID\"=?;");
	    statement.setBoolean(1, fieldValue.booleanValue());
	    statement.setInt(2, index);
	    value = statement.executeUpdate();
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    /**
     * Sets the fields of movie index to the general info table and returns the number of
     * updated rows.
     **/
    public int setSeen(int index, boolean seen) {
	
	int value = 0;
	try {
	    PreparedStatement statement = _sql.prepareStatement("UPDATE \"General Info\" "+
								"SET \"General Info\".\"Seen\"=? "+
								"WHERE \"General Info\".\"ID\"=?;");
	    statement.setBoolean(1,seen);
	    statement.setInt(2,index);
	    
	    value = statement.executeUpdate();
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    /**
     * Sets the fields of movie index to the general info table and returns the number of
     * updated rows.
     **/
    public int setSeenEpisode(int index, boolean seen) {
	
	int value = 0;
	try {
	    PreparedStatement statement = _sql.prepareStatement("UPDATE \"General Info Episodes\" "+
								"SET \"General Info Episodes\".\"Seen\"=? "+
								"WHERE \"General Info Episodes\".\"ID\"=?;");
	    statement.setBoolean(1,seen);
	    statement.setInt(2,index);
	    
	    value = statement.executeUpdate();
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    /**
     * Adds and Extra Info field from the database with name field
     * and returns number of updated rows (none (-1)...).
     **/
    public int addListsColumn(String field) {
	int value = 0;
	
	String fieldType = "BOOLEAN";
	
	if (this instanceof DatabaseAccess)
	    fieldType = "BIT";
	    
	try {
	    value = _sql.executeUpdate("ALTER TABLE " + quote + "Lists" + quote + " "+
				       "ADD COLUMN " + quote +field+ quote +" "+ fieldType +";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of altered rows... */
	return value;
    }
    
    /**
     * Removes and Extra Info field from the database with name field
     * and returns number of updated rows (none (-1)...).
     **/
    public int removeListsColumn(String field) {
      
	int value = 0;
	try {
	    value = _sql.executeUpdate("ALTER TABLE " + quote + "Lists" + quote + " "+
				       "DROP COLUMN "+ quote +field+ quote +";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of altered rows... */
	return value;
    }


    public int addExtraInfoFieldName(String field) {
	
	if (addExtraInfoMovieFieldName(field) == -2)
	    return -1;
	
	if (addExtraInfoEpisodeFieldName(field) == -2)
	    return -2;
	
	return 1;
    }
    
    
    /**
     * Adds an Extra Info field from the database with name field
     * and returns -2 if an exception occurs.
     **/
    protected int addExtraInfoMovieFieldName(String field) {
	int value = 0;
	
	String fieldType = "TEXT";
	
	if (this instanceof DatabaseHSQL)
	    fieldType = "LONGVARCHAR";
	
	try {
	    value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoString + " "+
				       "ADD COLUMN " +quote+ field +quote+" "+ fieldType +";");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    value = -2;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of altered rows... */
	return value;
    }
    
    /**
     * Adds and Extra Info field from the database with name field
     * and returns -2 if an exception occurs.
     **/
    protected int addExtraInfoEpisodeFieldName(String field) {
	int value = 0;
	
	String fieldType = "TEXT";
	
	if (this instanceof DatabaseHSQL)
	    fieldType = "LONGVARCHAR";

	try {
	    value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoEpisodeString + " "+
				       "ADD COLUMN " +quote+ field +quote+" "+ fieldType +";");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    value = -2;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of altered rows... */
	return value;
    }
    
    
    
    public void removeExtraInfoFieldName(String field) {
	removeExtraInfoMovieFieldName(field);
	removeExtraInfoEpisodeFieldName(field);
    }

    
    /**
     * Removes an Extra Info field from the database with name field
     * and returns number of updated rows (none (-1)...).
     **/
    protected int removeExtraInfoMovieFieldName(String field) {
      
	int value = 0;
	try {
	    value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoString + " "+
				       "DROP COLUMN " +quote+ field +quote+";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of altered rows... */
	return value;
    }
    
    /**
     * Removes and Extra Info field from the database with name field
     * and returns number of updated rows (none (-1)...).
     **/
    protected int removeExtraInfoEpisodeFieldName(String field) {
      
	int value = 0;
	try {
	    value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoEpisodeString + " "+
				       "DROP COLUMN " +quote+ field +quote+";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of altered rows... */
	return value;
    }
 
    
    /**
     * Sets the active additional info fields.
     **/
    public void setActiveAdditionalInfoFields(int [] activeAdditionalInfoFields) {
	
	String activeFields = "";
	
	String columnName = "\"Active Additional Info Fields\"";
	String settings = quote + "Settings" + quote;
	
	if (this instanceof DatabaseMySQL)
	    columnName = "Active_Additional_Info_Fields";
	
	for (int i = 0; i < activeAdditionalInfoFields.length; i++) {
	    if (!activeFields.equals(""))
		activeFields += ":";
	    activeFields += "" + activeAdditionalInfoFields[i];
	}
	
	try {
	    /* Tries to find if it should be an insert or an update... */
	    ResultSet resultSet = _sql.executeQuery("SELECT " + settings +".* "+
						    "FROM " + settings + " "+
						    "WHERE " + settings + ".ID=1;");
	    
	    _sql.clear();
	    
	    if(resultSet.next()) {
		/* It's an update... */
		PreparedStatement statement = _sql.prepareStatement("UPDATE " + settings +" "+
								    "SET " + settings + "."+ columnName +"=? "+
								    "WHERE " + settings + ".ID=1;");
		statement.setString(1, activeFields);
		statement.executeUpdate();
	    } else {
		/* It's an insert...*/
		PreparedStatement statement = _sql.prepareStatement("INSERT INTO " + settings + " "+
								    "(ID,"+ columnName +") "+
								    "VALUES(1,?)");
		statement.setString(1, activeFields);
		statement.executeUpdate();
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
    }
    
    
    /**
     * Sets the folders Covers and Queries for this database.
     **/
    public int setFolders(String coversFolder, String queriesFolder) {
	
	int value = 0;
	String folders = quote + "Folders" + quote;
	String covers = quote + "Covers" + quote;
	String queries = quote + "Queries" + quote;
	
	try {
	    /* Tries to find if it's an insert or an update... */
	    ResultSet resultSet = _sql.executeQuery("SELECT " + folders + ".* "+
						    "FROM " + folders + " "+
						    "WHERE " + folders + ".ID=1;");
	    
	    if (resultSet.next()) {
		_sql.clear();
		/* Is an update... */
		PreparedStatement statement = _sql.prepareStatement("UPDATE " + folders +" "+
								    "SET " + folders + "." + covers + "=?, " + folders + "."+ queries + "=? "+
								    "WHERE " + folders + ".ID=1;");
		statement.setString(1, coversFolder);
		statement.setString(2, queriesFolder);
		value = statement.executeUpdate();
	    } else {
		_sql.clear();
		/* Ist's a insert...*/
		PreparedStatement statement = _sql.prepareStatement("INSERT INTO " + folders +" "+
								    "(ID,"+ covers +","+ queries +") "+
								    "VALUES(1,?,?)");
		statement.setString(1, coversFolder);
		statement.setString(2, queriesFolder);
		value = statement.executeUpdate();
	    }
	} catch (Exception e) {
	
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the updated records... */
	return value;
    }
    
    
    /**
     * Returns the Covers folder for this database.
     **/
    public String getCoversFolder() {
      
	String data = getString("SELECT " + quote + "Folders" + quote +"." + quote + "Covers" + quote + " "+
				"FROM " + quote + "Folders" + quote + " "+
				"WHERE " + quote + "Folders" + quote + ".ID=1;", "Covers");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the Queries folder for this database.
     **/
    public String getQueriesFolder() {
     
	String data = getString("SELECT " + quote + "Folders" + quote + "." + quote + "Queries" + quote + " "+
				"FROM " + quote + "Folders" + quote + " "+
				"WHERE " + quote + "Folders" + quote + ".ID=1;", "Queries");
	/* Returns the data... */
	return data;
    }
    
    
    /**
     * Returns a List of MovieModels that contains all the movies in the
     * current database sorted by the orderBy string.
     **/
    public DefaultListModel getMoviesList(String orderBy) {
	
	DefaultListModel listModel = new DefaultListModel();
	
	String sqlQuery = "SELECT \"General Info\".\"ID\", "+
	    "\"General Info\".\"Imdb\", "+
	    "\"General Info\".\"Cover\", "+
	    "\"General Info\".\"Date\", "+
	    "\"General Info\".\"Title\", "+
	    "\"General Info\".\"Directed By\", "+
	    "\"General Info\".\"Written By\", "+
	    "\"General Info\".\"Genre\", "+
	    "\"General Info\".\"Rating\", "+
	    "\"General Info\".\"Plot\", "+
	    "\"General Info\".\"Cast\", "+
	    "\"General Info\".\"Notes\", "+
	    "\"General Info\".\"Seen\", "+
	    "\"General Info\".\"Aka\", "+
	    "\"General Info\".\"Country\", "+
	    "\"General Info\".\"Language\", "+
	    "\"General Info\".\"Colour\", "+
	    "\"General Info\".\"Certification\", "+ 
	    "\"General Info\".\"Mpaa\", "+ 
	    "\"General Info\".\"Sound Mix\", "+ 
	    "\"General Info\".\"Web Runtime\", "+ 
	    "\"General Info\".\"Awards\" "+
	    "FROM \"General Info\" ";
	
	if (orderBy.equals("Duration")) {
	    sqlQuery +=
		"INNER JOIN \"Additional Info\" ON \"General Info\".\"ID\" = \"Additional Info\".\"ID\" "+
		"ORDER BY \"Additional Info\".\"Duration\", \"General Info\".\"Title\";";
	}
	else {
	    sqlQuery += 
		"ORDER BY \"General Info\".\""+ orderBy +"\", \"General Info\".\"Title\";";
	}
	
	try {
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery(sqlQuery);
	    
	    /* Processes the result set till the end... */
	    while (resultSet.next()) {
		listModel.addElement(new ModelMovie(resultSet.getInt("ID"), resultSet.getString("Imdb"), resultSet.getString("Cover"), resultSet.getString("Date"), resultSet.getString("Title"), resultSet.getString("Directed By"), resultSet.getString("Written By"), resultSet.getString("Genre"), resultSet.getString("Rating"), resultSet.getString("Plot"), resultSet.getString("Cast"), resultSet.getString("Notes"), resultSet.getBoolean("Seen"), resultSet.getString("Aka"), resultSet.getString("Country"), resultSet.getString("Language"), resultSet.getString("Colour"), resultSet.getString("Certification"), resultSet.getString("Mpaa"), resultSet.getString("Sound Mix"), resultSet.getString("Web Runtime"), resultSet.getString("Awards")));
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return listModel;
    }
    
    

    /**
     * Returns a List of MovieModels that contains all the movies in the
     * current database sorted by the orderBy string.
     **/
    public DefaultListModel getMoviesList(String orderBy, String listsColumn) {
	
	DefaultListModel listModel = new DefaultListModel();
	
	String sqlQuery = 
	    "SELECT \"General Info\".\"ID\", "+
	    "\"General Info\".\"Imdb\", "+
	    "\"General Info\".\"Cover\", "+
	    "\"General Info\".\"Date\", "+
	    "\"General Info\".\"Title\", "+
	    "\"General Info\".\"Directed By\", "+
	    "\"General Info\".\"Written By\", "+
	    "\"General Info\".\"Genre\", "+
	    "\"General Info\".\"Rating\", "+
	    "\"General Info\".\"Plot\", "+
	    "\"General Info\".\"Cast\", "+
	    "\"General Info\".\"Notes\", "+
	    "\"General Info\".\"Seen\", "+
	    "\"General Info\".\"Aka\", "+
	    "\"General Info\".\"Country\", "+
	    "\"General Info\".\"Language\", "+
	    "\"General Info\".\"Colour\", "+
	    "\"General Info\".\"Certification\", "+ 
	    "\"General Info\".\"Mpaa\", "+ 
	    "\"General Info\".\"Sound Mix\", "+ 
	    "\"General Info\".\"Web Runtime\", "+ 
	    "\"General Info\".\"Awards\" ";
	
	sqlQuery += "FROM \"General Info\" ";
	
	if (orderBy.equals("Duration")) {
	    
	    if (databaseType.equals("MSAccess")) {
		
		sqlQuery += 
		    "INNER JOIN (\"Additional Info\" INNER JOIN \"Lists\" ON \"Additional Info\".ID=\"Lists\".ID) "+
		    "ON \"General Info\".ID=\"Additional Info\".ID ";
	    }
	    else {
		sqlQuery += 
		    "INNER JOIN \"Additional Info\" ON \"General Info\".ID = \"Additional Info\".ID "+
		    "INNER JOIN \"Lists\" ON \"Additional Info\".ID = \"Lists\".ID ";
	    }
	    
	    sqlQuery += 
		"WHERE \"Lists\".\""+ listsColumn +"\"=1 "+
		"ORDER BY \"Additional Info\".\"Duration\", \"General Info\".\"Title\""+
		";";
	}
	else {
	    sqlQuery += 
		"INNER JOIN \"Lists\" ON \"General Info\".\"ID\" = \"Lists\".\"ID\" "+
		"WHERE \"Lists\".\""+ listsColumn +"\"=True "+
		"ORDER BY \"General Info\".\""+ orderBy +"\", \"General Info\".\"Title\""+
		";";
	}
	
	
	try {
	    
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery(sqlQuery);
	    
	    /* Processes the result set till the end... */
	    while (resultSet.next()) {
		
		listModel.addElement(new ModelMovie(resultSet.getInt("ID"), resultSet.getString("Imdb"), resultSet.getString("Cover"), resultSet.getString("Date"), resultSet.getString("Title"), resultSet.getString("Directed By"), resultSet.getString("Written By"), resultSet.getString("Genre"), resultSet.getString("Rating"), resultSet.getString("Plot"), resultSet.getString("Cast"), resultSet.getString("Notes"), resultSet.getBoolean("Seen"), resultSet.getString("Aka"), resultSet.getString("Country"), resultSet.getString("Language"), resultSet.getString("Colour"), resultSet.getString("Certification"), resultSet.getString("Mpaa"), resultSet.getString("Sound Mix"), resultSet.getString("Web Runtime"), resultSet.getString("Awards")));
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return listModel;
    }
    
    

    /**
     * Used by getMoviesList(ModelDatabaseSearch options)
     */
    private Object[] getFilterValues(String filter) {
	
	ArrayList matchValues = new ArrayList(10);
	
	/* The regular expression will divide by every white space except if (multiple) words are capsulated by " and " or { and } */
	//Pattern pattern = Pattern.compile("([\\p{Graph}&&[^\"]]+?)\\s|(\".+?\"+)");
	
	Pattern pattern = Pattern.compile("(\\{.+?\\})|([\\p{Graph}&&[^\"]]+?)\\s|(\".*?\"+)");
	
	String tmp = "";

	for (Matcher m = pattern.matcher(filter+" "); m.find();) {
	    
	    tmp = (m.group(0)).trim();
	    
	    if (tmp.charAt(0) == '"' && tmp.charAt(tmp.length()-1) == '"') {
		tmp = tmp.substring(1, tmp.length()-1);
	    }
	    matchValues.add(tmp);
	}
	
	return matchValues.toArray();
    }


    /**
     * Used by getMoviesList(ModelDatabaseSearch options)
     */
    private String processFilterValues(String table, String filterColumn, Object [] values, ModelDatabaseSearch options, boolean recursive) {
	
	String queryTemp = "";
	String value = "";
	
	for (int i = 0; i < values.length; i++) {
		
	    value = (String) values[i];
		
	    if (value.equals("AND") || value.equals("OR") || value.equals("XOR") || value.equals("NOT")) {
		    
		log.debug("value:" + value);
		log.debug("values[i+1]:" + values[i+1]);
		
		/* Checking if next value is invalid */
		if ((values.length-1 > i) && !values[i+1].equals(")") && 
		    !values[i+1].equals("AND") && !values[i+1].equals("OR") && !values[i+1].equals("XOR")) { 
			
		    if (values[i+1].equals("(") && (values.length-2 > i)) {
			    
			/* If i+2 value is valid */
			if (!(values[i+2].equals(")") || values[i+2].equals("AND") || 
			      values[i+2].equals("OR") || values[i+2].equals("XOR")))
			    queryTemp += " " + value + " ";
		    }
		    else if (!values[i+1].equals("(")) {
			    
			if (value.equals("NOT")) {
			    if ((i > 0) && !(values[i-1].equals("AND") || values[i-1].equals("OR") || values[i-1].equals(")"))) {
				queryTemp += " " + options.getDefaultOperator();
			    }
			}
			queryTemp += " " + value + " ";
		    }
		}
		else {
		    errorMessage = "Syntax error (parantheses)";
		    return null;
		}
	    }
		
	    else if (value.equals("("))
		queryTemp += " ( ";

	    else if (value.equals(")"))
		queryTemp += " ) ";
		

	    else if (value.startsWith("{")) {
		    
		value = value.substring(1, value.length()-1);
		
		/* Invalid */
		if (value.indexOf(":") == -1) {
		    errorMessage = "Syntax error (missing ':')";
		    return null;
		}
		    
		    
		String [] tableField = value.substring(0, value.indexOf(":")).split(",");
		Object [] values2 = getFilterValues(value.substring(value.indexOf(":")+1, value.length()));
		    
		for (int u = 0; u < tableField.length; u++)
		    log.debug("tablefield:" + tableField[u] + ":");
		    
		for (int u = 0; u < values2.length; u++)
		    log.debug("values:" + values2[u] + ":");
		
		log.debug("tableField.length:" + tableField.length);
		
		/* Must look up the alias and find the table and column */
		if (tableField.length == 1) {
		    
		    log.debug("containsValue("+tableField[0] +"):"+ options.getSearchAlias().containsValue(tableField[0]));
		    
		    if (options.getSearchAlias().containsValue(tableField[0])) {
			
			Set map = (options.getSearchAlias()).entrySet();
			String setkey;
			String setValue;
			
			for (Iterator iterator = map.iterator(); iterator.hasNext();) {
			    
			    Map.Entry entry = (Map.Entry) iterator.next();
			    setkey = (String) entry.getKey();
			    setValue = (String) entry.getValue();
			    
			    if (setValue.equals(tableField[0])) {
				String table2 = setkey.substring(0, setkey.indexOf("."));
				String column2 = setkey.substring(setkey.indexOf(".")+1, setkey.length());
				
				if (table2.replaceAll("_", " ").equalsIgnoreCase("General Info"))
				    table2 = generalInfoString;
				else if (table2.replaceAll("_", " ").equalsIgnoreCase("Additional Info"))
				    table2 = additionalInfoString;
				else if (table2.replaceAll("_", " ").equalsIgnoreCase("Extra Info"))
				    table2 = extraInfoString;
				
				if (databaseType.equals("MySQL")) {
				    
				    if (!table2.equals("Extra_Info"))
					column2 = column2.replaceAll(" ", "_");
				    else
					column2 = quote+ column2 +quote;
				}
				else {
				    table2 = quote+ table2 +quote;
				    column2 = quote+ column2 +quote;
				}
				tableField = new String[]{table2, column2};
			    }
				
			}
		    }
		    else {
			errorMessage = "Alias '"+ tableField[0] + "' is invalid";
			return null;
		    }
		}
		
		int len;
		String filterValues = null;
		
		if (tableField.length > 0) {
		    tableField[0] = tableField[0].trim();
		    len = tableField[0].length();
		    
		    if (len > 0 && tableField[0].charAt(0) == '\"' && tableField[0].charAt(len-1) == '\"')
			tableField[0] = quote + tableField[0].substring(1, len-1) + quote;
		    
		    if (tableField.length > 1) {
			tableField[1] = tableField[1].trim();
			len = tableField[1].length();
			
			if (len > 0 && tableField[1].charAt(0) == '\"' && tableField[1].charAt(len-1) == '\"')
			    tableField[1] = quote + tableField[1].substring(1, len-1) + quote;
		    }
		    
		    filterValues = processFilterValues(tableField[0].trim(), tableField[1].trim(), values2, options, true);
		}
		
		if (filterValues == null) {
		    errorMessage = "Syntax error (parantheses)";
		    return null;
		}
		
		
		if ((i > 0) && !(values[i-1].equals("AND") || values[i-1].equals("OR") || 
				 values[i-1].equals("XOR") || values[i-1].equals("NOT") || 
				 values[i-1].equals("(") || values[i-1].equals(")"))) {
		    queryTemp += " " + options.getDefaultOperator() + " ";
		}
		
		queryTemp += " ( " + filterValues + " ) ";
	    }
	    else {
		    
		log.debug("i:" + i);
		if (i > 0)
		    log.debug("values[i-1]:" + values[i-1]);
		    
		    
		if ((i > 0) && !(values[i-1].equals("AND") || values[i-1].equals("OR") || 
				 values[i-1].equals("XOR") || values[i-1].equals("NOT") || 
				 values[i-1].equals("(") || values[i-1].equals(")"))) {
		    queryTemp += " " + options.getDefaultOperator() + " ";
		}
		    
		boolean caseinsensitive = true;
		
		if (value.equals("")) {
		    
		    queryTemp += "("+ table +"."+ filterColumn +" LIKE '' ";
		    
		    /* If include aka titles */
		    if (options.getIncludeAkaTitlesInFilter() && filterColumn.equals("Title") && !recursive) {
			queryTemp += "OR "+ table +".Aka LIKE '' ";
		    }
		    queryTemp += ") ";
		}
		else if (caseinsensitive && databaseType.equals("HSQL")) {
		    
		    queryTemp += "(UPPER("+ table + "." + filterColumn + ") LIKE '%"+ value.toUpperCase() +"%' ";
		    
		    /* If include aka titles */
		    if (options.getIncludeAkaTitlesInFilter() && filterColumn.equals("Title") && !recursive) {
			queryTemp += "OR UPPER("+ table +".Aka) LIKE '%"+ value.toUpperCase() +"%' ";
		    }
		    queryTemp += ") ";
		}
		else {
		    queryTemp += "(" + table + "."+ filterColumn +" LIKE '%"+ value +"%' ";
		    
		    /* If include aka titles */
		    if (options.getIncludeAkaTitlesInFilter() && !recursive) {
			queryTemp += "OR "+ table +".Aka LIKE '%"+ value +"%' ";
		    }
		    queryTemp += ") ";
		}
	    }
	}
	return queryTemp;
    }
    
    
    
    private String processFilter(ModelDatabaseSearch options, boolean where) {
	
	String table = quotedGeneralInfoString;
	String filterColumn = quote + options.getFilterCategory() + quote;
	
	if (databaseType.equals("MySQL"))
	    filterColumn = filterColumn.replaceAll(" ", "_");
	
	String filter = options.getFilterString();
		
	filter = filter.replaceAll("\\("," \\( ");
	filter = filter.replaceAll("\\)"," \\) ");
	    
	if (databaseType.equals("HSQL") && filter.indexOf(" XOR ") != -1) {
	    errorMessage = "XOR is not a supported operator in HSQLDB";
	    return null;
	}
	    
	/* Remove all double white space */
	StringBuffer stringBuff = new StringBuffer();
	for(int x = 0; x < filter.length(); x++)
	    if (filter.charAt(x) != ' ' || (filter.length() != x+1 && filter.charAt(x+1) != ' '))
		stringBuff = stringBuff.append(filter.charAt(x));
	    
	filter = stringBuff.toString().trim();
	    
	if (filter.startsWith("AND ") || filter.startsWith("OR "))
	    filter = filter.substring(filter.indexOf(" ")+1, filter.length());
	    
	if (filter.endsWith(" AND") || filter.endsWith(" OR") || filter.endsWith(" NOT"))
	    filter = filter.substring(0, filter.lastIndexOf(" "));
	    
	    
	/* Check if number and placement of parantheses is correct */
	    
	int par1 = 0; /* '(' */
	int par2 = 0; /* ')' */
	    
	for (int i = 0; i < filter.length(); i++) {
		
	    if (filter.charAt(i) == '(') {
		par1++;
	    }
	    else if (filter.charAt(i) == ')') {
		if (par1 > par2)
		    par2++;
		else {
		    errorMessage = "Syntax error (parantheses)";
		    return null;
		}
	    }
	}
	    
	if (par1 > par2) {
	    errorMessage = "Syntax error (parantheses)";
	    return null;
	}
	    
	Object[] values = getFilterValues(filter);
	String filterTemp = processFilterValues(table, filterColumn, values, options, false);
	    
	if (filterTemp == null)
	    return null;
	
	if ((filterTemp.indexOf(" AND ") != -1) || (filterTemp.indexOf(" OR ") != -1) || (filterTemp.indexOf(" XOR ") != -1))
	    filterTemp = "( "+ filterTemp +") ";
	
	if (options.where)
	    filter = "AND " + filterTemp;
	else
	    filter = "WHERE " + filterTemp;
	
	return filter;
    }
    
    
    private String setTableJoins(String filter, ModelDatabaseSearch options) {
	
	String selectAndJoin = "SELECT " + quotedGeneralInfoString + ".ID, " + quotedGeneralInfoString + "." + quote + "Title" + quote + ", " + quotedGeneralInfoString + "." + quote + "Imdb" + quote + " FROM " + quotedGeneralInfoString + " ";
	
	String orderBy = options.getOrderCategory();
	String joinTemp = "";
	
	if (databaseType.equals("MSAccess") && ((options.getListOption() == 1) || orderBy.equals("Duration") || (filter.indexOf(additionalInfoString) != -1) || (filter.indexOf(extraInfoString) != -1))) {
	    orderBy = "\"Additional Info\".\"Duration\"";
	    
	    if (orderBy.equals("Duration") || filter.indexOf(additionalInfoString) != -1)
		joinTemp = "\"General Info\" INNER JOIN \"Additional Info\" ON \"General Info\".ID=\"Additional Info\".ID ";
	    
	    if (options.getListOption() == 1) {
		
		if (joinTemp.equals(""))
		    joinTemp += "\"General Info\" INNER JOIN \"Lists\" ON \"General Info\".ID=\"Lists\".ID ";
		else 
		    joinTemp += "INNER JOIN ("+ joinTemp +") ON \"General Info\".ID=\"Additional Info\".ID ";
	    }
	    
	    if (filter.indexOf(extraInfoString) != -1) {
		
		if (joinTemp.equals(""))
		    joinTemp += "\"General Info\" INNER JOIN \"Extra Info\" ON \"General Info\".ID=\"Extra Info\".ID ";
		else
		    joinTemp += "INNER JOIN ("+ joinTemp +") ON \"General Info\".ID=\"Extra Info\".ID ";
	    }
	    
	    selectAndJoin += joinTemp;
	}
	else {
	    
	    if (options.getListOption() == 1) {
		selectAndJoin += "INNER JOIN "+ quote+ "Lists"+ quote+ " ON "+ quotedGeneralInfoString + ".ID = " + quote + "Lists" + quote + ".ID ";
	    }
	    
	    if (orderBy.equals("Duration") || filter.indexOf(additionalInfoString) != -1) {
		orderBy = quotedAdditionalInfoString + "." + quote + "Duration" + quote;
		//sqlQuery += "INNER JOIN \"Additional Info\" ON \"General Info\".ID = \"Additional Info\".ID ";
		selectAndJoin += "INNER JOIN "+ quotedAdditionalInfoString + " ON "+ quotedGeneralInfoString + ".ID = "+ quotedAdditionalInfoString +".ID ";
	    }
	    
	    if (filter.indexOf(extraInfoString) != -1) {
		selectAndJoin += "INNER JOIN " + quotedExtraInfoString + " ON " + quotedGeneralInfoString + ".ID=" + quotedExtraInfoString + ".ID ";
	    }
	}
	return selectAndJoin;
    }
    

    private String processAdvancedOptions(ModelDatabaseSearch options) {
	
	String sqlQuery = "";
	String listColumn = quote + options.getListName() + quote;
	int option = 0;

	/* List */
	if ((option = options.getListOption()) == 1 && !listColumn.equals("")) {
	    
	    if (!options.where)
		sqlQuery += "WHERE ";

	    sqlQuery += quote + "Lists"+ quote + "." + listColumn + "=1 ";
	    
	    options.where = true;
	}
	
	/* seen */
	if ((option = options.getSeen()) > 1) {
	    
	    if (options.where)
		sqlQuery += "AND ";
	    else
		sqlQuery += "WHERE ";
	    
	    if (option == 2)
		sqlQuery += "("+ quotedGeneralInfoString +"." +quote+ "Seen" +quote+ " = 1) ";
	    else
		sqlQuery += "("+ quotedGeneralInfoString +"." +quote+ "Seen" +quote+ " = 0) ";
	    
	    options.where = true;
	}
	
	/* Rating */
	if ((option = options.getRatingOption()) > 1) {
	    
	    double rating = options.getRating();
	    
	    if (options.where)
		sqlQuery += "AND ";
	    else
		sqlQuery += "WHERE ";
	    
	    /* if MSAccess, have to convert rating with the Val function */
	    if (databaseType.equals("MSAccess")) {
		
		if (option == 2)
		    sqlQuery += "(Val(Rating) >= "+rating+") ";
		else
		    sqlQuery += "(Val(Rating) <= "+rating+") ";
	    }
	    else {
		if (option == 2)
		    sqlQuery += "(" +quote+ "Rating" +quote+ " >= "+rating+") ";
		else
		    sqlQuery += "(" +quote+ "Rating" +quote+ " <= "+rating+") ";
	    }
	    
	    options.where = true;
	}
	
	/* Date */
	if ((option = options.getDateOption()) > 1) {
	    
	    String date = options.getDate();
	    
	    if (!date.equals("")) {
		
		if (options.where)
		    sqlQuery += "AND ";
		else
		    sqlQuery += "WHERE ";
		
		/* if MSAccess, have to convert date with the Val function */
		if (databaseType.equals("MSAccess")) {
		    if (option == 2)
			sqlQuery += "(Val(Date) >= "+ date +") ";
		    else
			sqlQuery += "(Val(Date) <= "+ date +") ";
		}
		else {
		    if (option == 2)
			sqlQuery += "(" +quote+ "Date" +quote+ " >= "+ date +") ";
		    else
			sqlQuery += "(" +quote+ "Date" +quote+ " <= "+ date +") ";
		}
		
		options.where = true;
	    }
	}
	return sqlQuery;
    }
    

    /**
     * Returns a List of MovieModels according to the search options
     **/
    public DefaultListModel getMoviesList(ModelDatabaseSearch options) {
	
	DefaultListModel listModel = new DefaultListModel();
	
	String sqlAdcanvedOptions = processAdvancedOptions(options);
	
	/* Should only be one instance of "WHERE" in the sql query. If "WHERE" is used once this is set to true */
	boolean where = options.where;
	
	/* Filter */
	String sqlFilter = "";
	
	if (!options.getFilterString().trim().equals("")) {
	    sqlFilter = processFilter(options, where);
	    
	    if (sqlFilter == null)
		return listModel;
	}
	
	/* Sets the right table joins */
	String selectAndJoin = setTableJoins(sqlFilter, options);
	
	String sqlQuery = selectAndJoin + " " + sqlAdcanvedOptions + " " + sqlFilter + " ";
	
	String orderBy = options.getOrderCategory();
	
	if (databaseType.equals("MySQL"))
	    orderBy = orderBy.replaceAll(" ", "_");
	
	/* if MSAccess, have to convert rating with the Val function */
	if (databaseType.equals("MSAccess") && orderBy.equals("Rating")) {
	    sqlQuery += "ORDER BY Val("+ orderBy +"), \"Title\"";
	}
	else 
	    sqlQuery += "ORDER BY " +quote+ orderBy +quote+ ", " +quote+ "Title" +quote;
	
	sqlQuery += ";";
	

	log.debug("sqlQuery:" + sqlQuery);

	try {
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery(sqlQuery);
	    
	    /* Processes the result set till the end... */
	    while (resultSet.next()) {
		//listModel.addElement(new ModelMovie(resultSet.getInt("ID"), resultSet.getString("Title")));
		listModel.addElement(new ModelMovie(resultSet.getInt("ID"), resultSet.getString("Title"), resultSet.getString("Imdb")));
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	    
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return listModel;
    }



    /** 
     * Returns a DefaultListModel that contains all the movies in the
     * current database.
     **/
    public ArrayList getEpisodeList(String orderBy) {
	ArrayList list = new ArrayList(100);
	
	try {
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery("SELECT \"General Info Episodes\".\"ID\","+
						    "\"General Info Episodes\".\"movieID\","+
						    "\"General Info Episodes\".\"episodeNr\","+
						    "\"General Info Episodes\".\"UrlKey\","+
						    "\"General Info Episodes\".\"Cover\","+
						    "\"General Info Episodes\".\"Date\","+
						    "\"General Info Episodes\".\"Title\","+
						    "\"General Info Episodes\".\"Directed By\","+
						    "\"General Info Episodes\".\"Written By\","+
						    "\"General Info Episodes\".\"Genre\","+
						    "\"General Info Episodes\".\"Rating\","+
						    "\"General Info Episodes\".\"Plot\","+
						    "\"General Info Episodes\".\"Cast\","+
						    "\"General Info Episodes\".\"Notes\","+
						    "\"General Info Episodes\".\"Seen\","+
						    "\"General Info Episodes\".\"Aka\","+
						    "\"General Info Episodes\".\"Country\","+
						    "\"General Info Episodes\".\"Language\","+
						    "\"General Info Episodes\".\"Colour\","+
						    "\"General Info Episodes\".\"Certification\","+ 
						    "\"General Info Episodes\".\"Sound Mix\","+ 
						    "\"General Info Episodes\".\"Web Runtime\","+ 
						    "\"General Info Episodes\".\"Awards\" "+ 
						    "FROM \"General Info Episodes\" "+
						    "ORDER BY \"General Info Episodes\".\""+ orderBy +"\", \"General Info Episodes\".\"episodeNr\";");
	    
	    
	    /* Processes the result set till the end... */
	    while (resultSet.next()) {
		
		list.add(new ModelEpisode(resultSet.getInt("ID"), resultSet.getInt("movieID"), resultSet.getInt("episodeNr"), resultSet.getString("UrlKey"), resultSet.getString("Cover"), resultSet.getString("Date"), resultSet.getString("Title"), resultSet.getString("Directed By"), resultSet.getString("Written By"), resultSet.getString("Genre"), resultSet.getString("Rating"), resultSet.getString("Plot"), resultSet.getString("Cast"), resultSet.getString("Notes"), resultSet.getBoolean("Seen"), resultSet.getString("Aka"), resultSet.getString("Country"), resultSet.getString("Language"), resultSet.getString("Colour"), resultSet.getString("Certification"), resultSet.getString("Sound Mix"), resultSet.getString("Web Runtime"), resultSet.getString("Awards")));
	    }
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return list;
    }
    
    
    /** Returns a MovieModel that contains the movie at the specified index
     * in the database.
     **/
    public ModelMovie getMovie(int index, boolean notUsed) {
	ModelMovie movie = null;
	
	try {
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery("SELECT \"General Info\".\"ID\", "+
						    "\"General Info\".\"Imdb\", "+
						    "\"General Info\".\"Cover\", "+
						    "\"General Info\".\"Date\", "+
						    "\"General Info\".\"Title\", "+
						    "\"General Info\".\"Directed By\", "+
						    "\"General Info\".\"Written By\", "+
						    "\"General Info\".\"Genre\", "+
						    "\"General Info\".\"Rating\", "+
						    "\"General Info\".\"Plot\", "+
						    "\"General Info\".\"Cast\", "+
						    "\"General Info\".\"Notes\", "+
						    "\"General Info\".\"Seen\", "+
						    "\"General Info\".\"Aka\", "+
						    "\"General Info\".\"Country\", "+
						    "\"General Info\".\"Language\", "+
						    "\"General Info\".\"Colour\", "+
						    "\"General Info\".\"Certification\", "+ 
						    "\"General Info\".\"Mpaa\", "+ 
						    "\"General Info\".\"Sound Mix\", "+ 
						    "\"General Info\".\"Web Runtime\", "+ 
						    "\"General Info\".\"Awards\" "+ 
						    "FROM \"General Info\" "+ 
						    "WHERE \"General Info\".\"ID\"="+index+";");
	    
	    /* Processes the result set till the end... */
	    resultSet.next();
	    
	    movie = new ModelMovie(resultSet.getInt("id"), resultSet.getString("Imdb"), resultSet.getString("Cover"), resultSet.getString("Date"), resultSet.getString("Title"), resultSet.getString("Directed By"), resultSet.getString("Written By"), resultSet.getString("Genre"), resultSet.getString("Rating"), resultSet.getString("Plot"), resultSet.getString("Cast"), resultSet.getString("Notes"), resultSet.getBoolean("Seen"), resultSet.getString("Aka"), resultSet.getString("Country"), resultSet.getString("Language"), resultSet.getString("Colour"), resultSet.getString("Certification"), resultSet.getString("Mpaa"), resultSet.getString("Sound Mix"), resultSet.getString("Web Runtime"), resultSet.getString("Awards"));
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return movie;
    }


    /** 
     * Returns a ModelMovie with the general info on a specific episode
     **/
    public ModelEpisode getEpisode(int index, boolean notUsed) {
	ModelEpisode episode = null;
	
	try {
	    
	    /* Gets the list in a result set... */
	    ResultSet resultSet = _sql.executeQuery("SELECT \"General Info Episodes\".\"ID\","+
						    "\"General Info Episodes\".\"movieID\","+
						    "\"General Info Episodes\".\"episodeNr\","+
						    "\"General Info Episodes\".\"UrlKey\","+
						    "\"General Info Episodes\".\"Cover\","+
						    "\"General Info Episodes\".\"Date\","+
						    "\"General Info Episodes\".\"Title\","+
						    "\"General Info Episodes\".\"Directed By\","+
						    "\"General Info Episodes\".\"Written By\","+
						    "\"General Info Episodes\".\"Genre\","+
						    "\"General Info Episodes\".\"Rating\","+
						    "\"General Info Episodes\".\"Plot\","+
						    "\"General Info Episodes\".\"Cast\","+
						    "\"General Info Episodes\".\"Notes\","+
						    "\"General Info Episodes\".\"Seen\","+
						    "\"General Info Episodes\".\"Aka\","+
						    "\"General Info Episodes\".\"Country\","+
						    "\"General Info Episodes\".\"Language\","+
						    "\"General Info Episodes\".\"Colour\", "+
						    "\"General Info Episodes\".\"Certification\", "+ 
						    "\"General Info Episodes\".\"Sound Mix\", "+ 
						    "\"General Info Episodes\".\"Web Runtime\", "+ 
						    "\"General Info Episodes\".\"Awards\" "+ 
						    "FROM \"General Info Episodes\" "+ "WHERE \"General Info Episodes\".\"ID\"="+index+";");
	    
	    /* Processes the result set till the end... */
	    resultSet.next();
	    
	    episode = new ModelEpisode(resultSet.getInt("ID"), resultSet.getInt("movieID"), resultSet.getInt("episodeNr"), resultSet.getString("UrlKey"), resultSet.getString("Cover"), resultSet.getString("Date"), resultSet.getString("Title"), resultSet.getString("Directed By"), resultSet.getString("Written By"), resultSet.getString("Genre"), resultSet.getString("Rating"), resultSet.getString("Plot"), resultSet.getString("Cast"), resultSet.getString("Notes"), resultSet.getBoolean("Seen"), ""/*("Aka")*/, resultSet.getString("Country"), resultSet.getString("Language"), resultSet.getString("Colour"), resultSet.getString("Certification"), resultSet.getString("Sound Mix"), resultSet.getString("Web Runtime"), resultSet.getString("Awards"));
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    checkErrorMessage(e.getMessage());
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the list model... */
	return episode;
    }

    
    /**
     * Returns the Extra Info field value with index index named name...
     **/
    public String getExtraInfoMovieField(int index, String name) {
      
	String data = getString("SELECT \"Extra Info\".\""+name+"\" "+
				"FROM \"Extra Info\" "+
				"WHERE \"Extra Info\".\"ID\"="+index+";", name);
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the Extra Info field with index index named name...
     **/
    public String getExtraInfoEpisodeField(int index, String name) {
      
	String data = getString("SELECT \"Extra Info Episodes\".\""+name+"\" "+
				"FROM \"Extra Info Episodes\" "+
				"WHERE \"Extra Info Episodes\".\"ID\"="+index+";", name);
	/* Returns the data... */
	return data;
    }
    
    
    /**
     * Returns true if the movie at the specific index is a member of the specified list with name name...
     **/
    protected boolean getList(int index, String name) {
	
	boolean data = getBoolean("SELECT \"Lists\".\""+name+"\" "+
				  "FROM \"Lists\" "+
				  "WHERE \"Lists\".\"ID\"="+index+";", name);
	/* Returns the data... */
	return data;
    }
    
    
    
    
    
    /* Following methods are not used and are implemented in MySQL */


    /**
     * Returns the title with index index...
     **/
    public String getMovieTitle2(int index) {
	
	String data = getString("SELECT \"General Info\".\"Title\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Title");
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the cover with index index...
     **/
    public String getCover2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Cover\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Cover");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the imdb with index index...
     **/
    public String getUrlKey2(int index) {
     
	String data = getString("SELECT \"General Info\".\"Imdb\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Imdb");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the date with index index...
     **/
    public String getDate2(int index) {

	String data = getString("SELECT \"General Info\".\"Date\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Date");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the \"Directed By\" with index index...
     **/
    protected String getDirectedBy2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Directed By\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Directed By");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the \"Written By\" with index index...
     **/
    protected String getWrittenBy2(int index) {
	
	String data = getString("SELECT \"General Info\".\"Written By\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Written By");
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the genre with index index...
     **/
    protected String getGenre2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Genre\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Genre");
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the rating with index index...
     **/
    public String getRating2(int index) {
	
	String data = getString("SELECT \"General Info\".\"Rating\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Rating");
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the seen with index index...
     **/
    public boolean getSeen2(int index) {
	
	boolean data = getBoolean("SELECT \"General Info\".\"Seen\" "+
				  "FROM \"General Info\" "+
				  "WHERE \"General Info\".\"ID\"="+index+";", "Seen");
	/* Returns the data... */
	return data;
    }
    
    
    /**
     * Returns the plot with index index...
     **/
    public String getPlot2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Plot\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Plot");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the cast with index index...
     **/
    protected String getCast(int index) {
       
	String data = getString("SELECT \"General Info\".\"Cast\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Cast");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the notes with index index...
     **/
    protected String getNotes2(int index) {
       
	String data = getString("SELECT \"General Info\".\"Notes\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Notes");
	/* Returns the data... */
	return data;
    }
    
    
    /**
     * Returns the Also Known As with index index...
     **/
    protected String getAka2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Aka\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Aka");
	/* Returns the data... */
	return data;
    }

    /**
     * Returns the country with index index...
     **/
    protected String getCountry2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Country\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Country");
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the language with index index...
     **/
    protected String getLanguage2(int index) {
       
	String data = getString("SELECT \"General Info\".\"Language\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Language");
	/* Returns the data... */
	return data;
    }
    
    /**
     * Returns the Colour with index index...
     **/
    protected String getColour2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Colour\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Colour");
	/* Returns the data... */
	return data;
    }

    
    /**
     * Returns the Colour with index index...
     **/
    protected String getCertification2(int index) {
      
	String data = getString("SELECT \"General Info\".\"Certification\" "+
				"FROM \"General Info\" "+
				"WHERE \"General Info\".\"ID\"="+index+";", "Certification");
	/* Returns the data... */
	return data;
    }
    
    
   
}
