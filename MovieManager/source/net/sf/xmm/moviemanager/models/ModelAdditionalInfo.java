/**
 * @(#)ModelAdditionalInfo.java 00.09.06 (dd.mm.yy)
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

import java.util.*;

public class ModelAdditionalInfo {
    
    public static int additionalInfoFieldCount = 17;
    
    private int index = -1;
    
    private String subtitles = null;
    private int duration = 0;
    private int fileSize = 0;
    private int cds = 0;
    private double cdCases = 0;
    private String resolution = null;
    private String videoCodec = null;
    private String videoRate = null;
    private String videoBitrate = null;
    private String audioCodec = null;
    private String audioRate = null;
    private String audioBitrate = null;
    private String audioChannels = null;
    private String fileLocation = null;
    private int fileCount = 0;
    private String container = null;
    private String mediaType = null;
    
    protected static boolean hasOldExtraInfoFieldNames = true;
    static private ArrayList extraInfoFieldNames = null;
    private ArrayList extraInfoFieldValues = new ArrayList();
    
    public ModelAdditionalInfo() {
        
        if (extraInfoFieldNames != null) {
            for (int i = 0; i < extraInfoFieldNames.size(); i++)
                extraInfoFieldValues.add("");    
        }
    }

    /**
     * The constructor.
     **/
    public ModelAdditionalInfo(String subtitles, int duration, int fileSize, int cds, double cdCases, String resolution, String videoCodec, String videoRate, String videoBitrate, String audioCodec, String audioRate, String audioBitrate, String audioChannels, String fileLocation, int fileCount, String container, String mediaType) {
	
	this.subtitles = subtitles;
	this.duration = duration;
	this.fileSize = fileSize;
	this.cds = cds;
	this.cdCases = cdCases;
	this.resolution = resolution;
	this.videoCodec = videoCodec;
	this.videoRate = videoRate;
	this.videoBitrate = videoBitrate;
	this.audioCodec = audioCodec;
	this.audioRate = audioRate;
	this.audioBitrate = audioBitrate;
	this.audioChannels = audioChannels;
	this.fileLocation = fileLocation;
	this.fileCount = fileCount;
	this.container = container;
	this.mediaType = mediaType;
    }
    
    
    public int getKey() {
	return index;
    }
    
    public String getSubtitles() {
	if (subtitles == null)
	    return "";
	return subtitles;
    }
    
    public int getDuration() {
	return duration;
    }
    
    public int getFileSize() {
	return fileSize;
    }
    
    public int getCDs() {
	return cds;
    }
    
    public double getCDCases() {
	return cdCases;
    }
    
    public String getResolution() {
	if (resolution == null)
	    return "";
	return resolution;
    }
    
    public String getVideoCodec() {
	if (videoCodec == null)
	    return "";
	return videoCodec;
    }
    
    public String getVideoRate() {
	return videoRate;
    }
    
    public String getVideoBitrate() {
	if (videoBitrate == null)
	    return "";
	return videoBitrate;
    }
    
    public String getAudioCodec() {
	if (audioCodec == null)
	    return "";
	return audioCodec;
    }
    
    public String getAudioRate() {
	if (audioRate == null)
	    return "";
	return audioRate;
    }
    
    public String getAudioBitrate() {
	if (audioBitrate == null)
	    return "";
	return audioBitrate;
    }
    
    public String getAudioChannels() {
	if (audioChannels == null)
	    return "";
	return audioChannels;
    }
    
    public String getFileLocation() {
	if (fileLocation == null)
	    return "";
	return fileLocation;
    }
    
    public int getFileCount() {
	return fileCount;
    }
    
    public String getContainer() {
	if (container == null)
	    return "";
	return container;
    }
    
    public String getMediaType() {
	if (mediaType == null)
	    return "";
	return mediaType;
    }
    
    public String getExtraInfoFieldName(int index) {
	
	if (index >= extraInfoFieldNames.size())
	    return "";
	else
	    return (String) extraInfoFieldNames.get(index);
    }
    
    public String getExtraInfoFieldValue(int index) {
	
	if (index < 0 || index >= extraInfoFieldValues.size())
	    return "";
	else
	    return (String) extraInfoFieldValues.get(index);
    }
    
    public static ArrayList getExtraInfoFieldNames() {
	return extraInfoFieldNames;
    }

    public ArrayList getExtraInfoFieldValues() {
	return extraInfoFieldValues;
    }
    
    public static boolean hasOldExtraInfoFieldNames() {
	return hasOldExtraInfoFieldNames;
    }
    
    public static void setHasOldExtraInfoFieldNames(boolean hasOldExtraInfoFieldNames) {
	ModelAdditionalInfo.hasOldExtraInfoFieldNames = hasOldExtraInfoFieldNames;
    }
    
    public static void setExtraInfoFieldNames(ArrayList extraInfoFieldNames) {
	
	if (hasOldExtraInfoFieldNames) {
	    ModelAdditionalInfo.extraInfoFieldNames = extraInfoFieldNames;
	    hasOldExtraInfoFieldNames = false;
	}
    }
    
    public void setExtraInfoFieldValues(ArrayList extraInfoFieldValues) {
	this.extraInfoFieldValues = extraInfoFieldValues;
    }

    public void setAudioBitrate(String audioBitrate) {
	this.audioBitrate = audioBitrate;
    }

    public void setAudioChannels(String audioChannels) {
	this.audioChannels = audioChannels;
    }

    public void setAudioCodec(String audioCodec) {
	this.audioCodec = audioCodec;
    }

    public void setAudioRate(String audioRate) {
	this.audioRate = audioRate;
    }

    public void setCDCases(double cdCases) {
	this.cdCases = cdCases;
    }

    public void setCDs(int cds) {
	this.cds = cds;
    }

    public void setContainer(String container) {
	this.container = container;
    }

    public void setDuration(int duration) {
	this.duration = duration;
    }

    public void setFileCount(int fileCount) {
	this.fileCount = fileCount;
    }

    public void setFileLocation(String fileLocation) {
	this.fileLocation = fileLocation;
    }

    public void setFileSize(int fileSize) {
	this.fileSize = fileSize;
    }

    public void setIndex(int index) {
	this.index = index;
    }

    public void setMediaType(String mediaType) {
	this.mediaType = mediaType;
    }

    public void setResolution(String resolution) {
	this.resolution = resolution;
    }

    public void setSubtitles(String subtitles) {
	this.subtitles = subtitles;
    }

    public void setVideoBitrate(String videoBitrate) {
	this.videoBitrate = videoBitrate;
    }

    public void setVideoCodec(String videoCodec) {
	this.videoCodec = videoCodec;
    }

    public void setVideoRate(String videoRate) {
	this.videoRate = videoRate;
    }        
}
