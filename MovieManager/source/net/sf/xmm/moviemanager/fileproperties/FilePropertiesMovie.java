/**
 * @(#)FilePropertiesMovie.java 1.0 26.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.fileproperties;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;

public class FilePropertiesMovie {
    
    static Logger log = Logger.getRootLogger();
    
    /**
     * The filesize.
     **/
    private String _subtitles = "";
    
    /**
     * The filesize.
     **/
    private int _fileSize = -1;
    
    /**
     * The resolution (width x heigth).
     **/
    private String _videoResolution = "";

    /**
     * The video codec (vids handler).
     **/
    private String _videoCodec = "";
    
    /**
     * The video codec (vids handler).
     **/
    private String _codecLibraryIdentifier = "";
   
    /**
     * The video rate (fps).
     **/
    private String _videoRate = "";

    /**
     * The video bit rate (kbps).
     **/
    private String _videoBitrate = "";

    /**
     * The video duration (seconds).
     **/
    private int _duration = -1;
  
    /**
     * The audio codec (auds handler).
     **/
    private String _audioCodec = "";

    /**
     * The audio rate (Hz).
     **/
    private String _audioRate = "";

    /**
     * The audio bit rate (kbps).
     **/
    private String _audioBitrate = "";

    /**
     * The audio channels.
     **/
    private String _audioChannels = "";
    
    /**
     * The file location.
     **/
    private String _location = "";
    
    /**
     * The file container.
     **/
    private String _container = "";
    
    private ArrayList metaData;
    
    protected String fileName = "";
    
    private boolean infoAvailable = false;
    
	private boolean supported = false;
       
    /**
     * The Constructor. Gets all info available (reads contents from file).
     *
     * @param filePath A path for the file.
     * @param useMediaInfo , 0 = no, 1 = yes if no java parser avaliable, 2 = yes 
     **/
    public FilePropertiesMovie(String filePath, int useMediaInfo) throws Exception {
	
	FileProperties fileProperties = null;
	
	try {
	    /* The known magic header bytes... */
	    final int[][] MAGIC_BYTES = {
		{0x00, 0x00, 0x01, 0xb3}, // MPEG (video)
		{0x00, 0x00, 0x01, 0xba}, // MPEG (video)
		{0x52, 0x49, 0x46, 0x46}, // RIFF (WAV / audio, AVI / video)
		{0x4f, 0x67, 0x67, 0x53}, // OGG ,OGM 
		{0x44, 0x56, 0x44, 0x56}, // IFO (DVD)
		//{0x1a, 0x45, 0xdf, 0xa3}, // MKV
	    };
	    
	    /**
	     * The respective objects.
	     **/
	    final FileProperties[] FORMATS = {
		new FilePropertiesMPEG(),
		new FilePropertiesMPEG(),
		new FilePropertiesRIFF(),
		new FilePropertiesOGM(),
		new FilePropertiesIFO(),
		//new FilePropertiesMKV(),
	    };
      
	    _fileSize = Math.round((new File(filePath).length()) / 1024F / 1024F);
	   
	    /* The input stream... */
	    RandomAccessFile dataStream = new RandomAccessFile(filePath, "r");
	    
	    /* Gets the header for filetype identification... */
	    int[] header = new int[4];
	    
	    for (int i=0; i<header.length; i++)
		header[i] = dataStream.readUnsignedByte();
	    
	    /* Finds the right object... */
	    int format;
	    for (format = 0; format < FORMATS.length; format++) {
		if (Arrays.equals(header, MAGIC_BYTES[format])) {
		    break;
		}
	    }
	    
	    boolean done = false;
	    
	    int next = 1;

	    while (!done) {
		
		try {
		    
		    switch (useMediaInfo) {
		    
		    case 0: {
			next = 0;
		    }
		    
		    case 1: {
			if (format < FORMATS.length) {
			
			    if (format < FORMATS.length) {
				fileProperties = FORMATS[format];
				break;
			    }
			}
		    
			if (next == 1 && useMediaInfo == 1) {
			    next = 0;
			    useMediaInfo = 2;
			}
			else
			    done = true;
			
			break;
		    }
		    
		    case 2: {
		    
			if (MovieManager.isWindows()) {
			    try {
				fileProperties = new FilePropertiesMediaInfo(filePath);
				break;
			    } catch (Exception e) {
				fileProperties = null;
				log.error("Exception: " + e.getMessage());
			    }
			}
			
			if (next == 1) {
			    next = 0;
			    useMediaInfo = 0;
			}
			else
			    done = true;
			
			break;
			
		    }
		    }
		
            
		    if (fileProperties != null) {
			
			/* Starts parsing the file...*/
			fileProperties.process(dataStream);
		    
            supported = fileProperties.isSupported();
            
            if (supported)
                infoAvailable = true;
            else
                throw new Exception("Info not available");
                        
			/* Gets the processed info... */
			_subtitles = fileProperties.getSubtitles();
			_videoResolution = fileProperties.getVideoResolution();
			_videoCodec = fileProperties.getVideoCodec();
			_videoRate = fileProperties.getVideoRate();
			_videoBitrate = fileProperties.getVideoBitrate();
			_duration = fileProperties.getDuration();
			_audioCodec = fileProperties.getAudioCodec();
			_audioRate = fileProperties.getAudioRate();
			_audioBitrate = fileProperties.getAudioBitrate();
			_audioChannels = fileProperties.getAudioChannels();
			_location = filePath;
			_container = fileProperties.getContainer();
			metaData = fileProperties.getMetaData();
			_codecLibraryIdentifier = fileProperties.getVideoCodecLibraryIdentifier();
			
			fileName = new File(filePath).getName();
			
			done = true;
		    }
		
		    /* Closes the input stream... */
		    dataStream.close();
		
		} catch (Exception e) {
		    log.error("Exception: " + e.getMessage());
		    
		    if (next == 1) {
			if (useMediaInfo == 2)
			    useMediaInfo = 0;
			else
			    useMediaInfo = 2;
			
			next = 0;
		    }
		    else {
			/* The file is corrupted, tries to save the info that may have been found */
			if (fileProperties != null) {
                _subtitles = fileProperties.getSubtitles();
                _videoResolution = fileProperties.getVideoResolution();
                _videoCodec = fileProperties.getVideoCodec();
                _videoRate = fileProperties.getVideoRate();
                _videoBitrate = fileProperties.getVideoBitrate();
                _duration = fileProperties.getDuration();
                _audioCodec = fileProperties.getAudioCodec();
                _audioRate = fileProperties.getAudioRate();
                _audioBitrate = fileProperties.getAudioBitrate();
                _audioChannels = fileProperties.getAudioChannels();
                _location = filePath;
                _container = fileProperties.getContainer();
                metaData = fileProperties.getMetaData();
                _codecLibraryIdentifier = fileProperties.getVideoCodecLibraryIdentifier();
                
                fileName = new File(filePath).getName();
			}
			throw new Exception("File could be corrupted. Some info may have been saved.");
		    }
		}
	    }
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    
	    if (!supported) {
		throw new Exception("File format not supported.");
	    }
	}
    }
	
    /**
     * The Constructor.
     **/
    protected FilePropertiesMovie(int fileSize, String videoResolution, String videoCodec, String videoRate,
				  int duration, String audioCodec, String audioRate, String audioChannels) {
	_fileSize = fileSize;
	_videoResolution = videoResolution;
	_videoCodec = videoCodec;
	_videoRate = videoRate;
	_duration = duration;
	_audioCodec = audioCodec;
	_audioRate = audioRate;
	_audioChannels = audioChannels;
    }

    /**
     * Returns all the components in a string.
     **/
    public String toString() {
	return "MovieProperties [ fileSize:"+_fileSize+", "+
	    "vResolution:"+_videoResolution+", "+
	    "vCodec:"+_videoCodec+", "+
	    "vRate:"+_videoRate+", "+
	    "vBitrate:"+_videoBitrate+", "+
	    "duration:("+_duration+") ,"+
	    "aCoded:"+_audioCodec+", "+
	    "aRate:"+_audioRate+", "+
	    "aBitrate:"+_audioBitrate+", "+
	    "aChannels:"+_audioChannels+" ]";
    }
    
    

    public ArrayList getMetaData() {
	return metaData;
    }
    
    /*
     * Returns the the tag info if it exists
     */
    public String getMetaDataTagInfo(String tag) {
	
	String temp = "";
	
	if (metaData != null) {
	    
	    for (int i = 0; i < metaData.size(); i++) {
		
		temp = ((String) metaData.get(i));
		
		if (temp.startsWith(tag)) {
		    return temp.substring(tag.length()+1, temp.length());
		}
	    }
	}
	return "";
    }
    
	public boolean getInfoAvailable() {
	return infoAvailable;
    }

    public boolean getFileFormatSupported() {
	return supported;
    }
    
    /**
     * Returns the subtitles.
     **/
    public String getSubtitles() {
	return _subtitles;
    }
    
    /**
     * Returns the filesize.
     **/
    public int getFileSize() {
	return _fileSize;
    }
    
    /**
     * Returns the Media Type.
     **/
    public String getFileName() {
	return fileName;
    }
    
    public void setFileName(String newName) {
        fileName = newName;
    }

    /**
     * Returns the resolution.
     **/
    public String getVideoResolution() {
	return _videoResolution;
    }

    /**
     * Returns the video codec (vids handler).
     **/
    public String getVideoCodec() {
	return _videoCodec;
    }
    
    public String getVideoCodecLibraryIdentifier() {
	return _codecLibraryIdentifier;
    }
    
    
    /**
     * Returns the video rate.
     **/
    public String getVideoRate() {
	return _videoRate;
    }

    /**
     * Returns the video bit rate.
     **/
    public String getVideoBitrate() {
	return _videoBitrate;
    }

    /**
     * Returns the duration.
     **/
    public int getDuration() {
	return _duration;
    }
  
    /**
     * Returns the audio codec (auds handler).
     **/
    public String getAudioCodec() {
	return _audioCodec;
    }

    /**
     * Returns the audio rate.
     **/
    public String getAudioRate() {
	return _audioRate;
    }

    /**
     * Returns the audio bit rate.
     **/
    public String getAudioBitrate() {
	return _audioBitrate;
    }

    /**
     * Returns the audio channels.
     **/
    public String getAudioChannels() {
	return _audioChannels;
    }
    
    /**
     * Sets the subtitles.
     **/
    protected void setSubtitles(String subtitles) {
	_subtitles = subtitles;
    }
    
    /**
     * Sets the length.
     **/
    protected void setFileSize(int fileSize) {
	_fileSize = fileSize;
    }

    /**
     * Sets the resolution.
     **/
    protected void setVideoResolution(String videoResolution) {
	_videoResolution = videoResolution;
    }

    /**
     * Sets the video codec (vids handler).
     **/
    protected void setVideoCodec(String videoCodec) {
	_videoCodec=videoCodec;
    }

    /**
     * Sets the video rate.
     **/
    protected void setVideoRate(String videoRate) {
	_videoRate=videoRate;
    }
    
    /**
     *  Sets the video bit rate.
     **/
    protected void setVideoBitrate(String videoBitrate) {
	_videoBitrate = videoBitrate; 
    }
    
    /**
     * Sets the duration.
     **/
    protected void setDuration(int duration) {
	_duration = duration;
    }
  
    /**
     * Sets the audio codec (auds handler).
     **/
    protected void setAudioCodec(String audioCodec) {
	_audioCodec=audioCodec;
    }

    /**
     * Sets the audio rate.
     **/
    protected void setAudioRate(String audioRate) {
	_audioRate=audioRate;
    }

    /**
     * Sets the audio channels.
     **/
    protected void setAudioChannels(String audioChannels) {
	_audioChannels=audioChannels;
    }
    
    /**
     * Sets the file location.
     **/
    protected void setFileLocatinon(String location) {
	_location = location;
    }
    
    /**
     * Sets the file container.
     **/
    public String getLocation() {
	return _location;
    }
    
    /**
     * Sets the file container.
     **/
    protected void setContainer(String container) {
	_container = container;
    }

    /**
     * Returns the file container.
     **/
    public String getContainer() {
	return _container;
    }
}
