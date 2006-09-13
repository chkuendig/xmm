/**
 * @(#)ModelSearchHit.java 1.0 19.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Bro
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

public class ModelSearchHit {

    /**
     * The show number used in the url.
     **/
    private String showKey;
  
    /**
     * The title-part of the url .
     **/
    private String urlTitle;
    
    /**
     * The title.
     **/
    private String title;
    
    /**
     * The string that is used to search for the episode when parsing the html code.
     **/
    private String searchTitle;
    
    /**
     * The season number
     **/
    private int seasonNumber;
    
    /**
     * The episode number
     **/
    private int streamNumber;
    
    
    private String coverExtension;
    
    /**
     * The constructor.
     **/
    public ModelSearchHit(String showKey, String urlTitle, String title) {
	this.showKey = showKey; 
	this.urlTitle = urlTitle;
	this.title = title;
    }
    
    /**
     * The constructor.
     **/
      public ModelSearchHit(String showKey, String urlTitle, String title, int seasonNumber) {
	this.showKey = showKey; 
	this.urlTitle = urlTitle;
	this.title = title;
	this.seasonNumber = seasonNumber;
    }
    
    /**
     * The constructor.
     **/
    public ModelSearchHit(String showKey, String urlTitle, String title, String searchTitle, int seasonNumber, int streamNumber) {
	this.showKey = showKey; 
	this.urlTitle = urlTitle;
	this.title = title;
	this.searchTitle = searchTitle;
	this.seasonNumber = seasonNumber;
	this.streamNumber = streamNumber;
    }
    
    /**
     * Gets the key.
     **/
    public String getShowKey() {
	return showKey; 
    }
    
    /**
     * Gets the title.
     **/
    public String getUrlTitle() {
	return urlTitle;
    }
    
    /**
     * Gets the title.
     **/
    public String getTitle() {
	return title;
    }
    
     /**
     * Gets the title.
     **/
    public String getSearchTitle() {
	return searchTitle;
    }

    /**
     * Gets the title.
     **/
    public int getSeasonNumber() {
	return seasonNumber;
    }
    
    /**
     * Gets the title.
     **/
    public int getStreamNumber() {
	return streamNumber;
    }
    
    /**
     * Gets the title.
     **/
    public String getCoverExtension() {
	return coverExtension;
    }
    
     /**
      * Gets the title.
      **/
    public void setCoverExtension(String coverExtension) {
	this.coverExtension = coverExtension;
    }
    
    /**
     * Gets the title.
     **/
    protected void setTitle(String title) {
	this.title = title;
    }
    
    /**
     * Returns the title.
     **/
    public String toString() {
	return title;
    }
}
