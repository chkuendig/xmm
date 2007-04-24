/**
 * @(#)TVDOTCOM.java 1.0 19.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.http;

import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelSearchHit;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

import java.net.SocketTimeoutException;
import java.net.URL;

import javax.swing.DefaultListModel;

public class TVDOTCOM {
  
    static Logger log = Logger.getRootLogger();
    
    /**
     * The imdb key for the movie.
     **/
    private String _key = "";
  
    /**
     * The date of the movie
     **/
    private String _date = "";
  
    /**
     * The dateof the movie.
     **/
    private String _title = "";
  
    /**
     * Directed by.
     **/
    private String _directedBy = "";
   
    /**
     * Written by.
     **/
    private String _writtenBy = "";
   
    /**
     * Genre.
     **/
    private String _genre = "";
  
    /**
     * The rating.
     **/
    private String _rating = "";
   
    /**
     * The colour of the movie
     **/
    private String _colour = "";
    
    /**
     * The country of the movie
     **/
    private String _country = "";
    
    /**
     * The language of the movie
     **/
    private String _language = "";
    
    /**
     * The plot.
     **/
    private String _plot = "";
   
    /**
     * The cast.
     **/
    private String _cast = "";
   
    /**
     * The cover url.
     **/
    private String _coverURL = "";
  
    
    /*stores the last exception message*/
    private static String exception;
    
    /**
     * The constructor.
     **/
    protected TVDOTCOM(String key) {
	;
    }
    
    /**
     * Gets the key.
     **/
    protected String getKey() {
	return _key;
    }

    /**
     * Gets the date.
     **/
    protected String getDate() {
	return _date;
    }
  
    /**
     * Gets the title.
     **/
    protected String getTitle() {
	return _title;
    }
  
    /**
     * Gets the durected by.
     **/
    protected String getDirectedBy() {
	return _directedBy;
    }
  
    /**
     * Gets the written by.
     **/
    protected String getWrittenBy() {
	return _writtenBy;
    }
  
    /**
     * Gets the genre.
     **/
    protected String getGenre() {
	return _genre;
    }
  
    /**
     * Gets the rating.
     **/
    protected String getRating() {
	return _rating;
    }
  
    /**
     * Gets the colour.
     **/
    protected String getColour() {
	return _colour;
    }

    /**
     * Gets the country.
     **/
    protected String getCountry() {
	return _country;
    }

    /**
     * Gets the language.
     **/
    protected String getLanguage() {
	return _language;
    }
    
    /**
     * Gets the plot.
     **/
    protected String getPlot() {
	return _plot;
    }
  
    /**
     * Gets the cast.
     **/
    protected String getCast() {
	return _cast;
    }
  
    /**
     * Gets the cover url.
     **/
    protected String getCoverURL() {
	return _coverURL;
    }
  
    /**
     * Gets the cover.
     **/
    public static byte[] getSeriesCover(ModelSearchHit modelSeries) {

	String urlType = "http://www.tv.com/"+ modelSeries.getUrlTitle() +"/show/"+ modelSeries.getShowKey() +"/summary.html";
	String coverUrl = "";
	
	try {
	    //URL url = makeURL(urlType);
	    URL url = new URL(urlType);
	    
		log.debug("coverUrl:"+coverUrl);
		
	    StringBuffer data = HttpUtil.readDataToStringBuffer(url);
	    
	    //FileUtil.writeToFile("episode.html", data);
	    
	    int index = data.indexOf("http://image.com.com/tv/images/processed/thumb/");
	    int index2 = data.indexOf("\"", index);
	    
	    if (index == -1 || index2 == -1)
		return null;
	    
	    coverUrl = data.substring(index, index2);
	    
	    modelSeries.setCoverExtension(coverUrl.substring(coverUrl.lastIndexOf(".")));
	    
	} catch (Exception e) {
	    log.error("", e);
	}
	
	byte[] coverData = {-1};
	
	try {
	    if (!coverUrl.equals("")) {
		
		URL url = new URL(coverUrl);
		
		coverData = HttpUtil.readDataToByteArray(url);
		
		//URLConnection connection = url.openConnection();
		
		//urlconn.setRequestProperty("referer","www.domain.com");
		//urlconn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0");
	
		//connection.setRequestProperty("referer","xmmwww.domain.com");
		//connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0");
		
	    }
	} catch (Exception e) {
	    log.error("", e);
	    return null;
	} 
	/* Returns the data... */
	return coverData;
    }

    
    public static DefaultListModel getSeriesMatches(String title) {
	DefaultListModel listModel = new DefaultListModel();
	exception = null;
        
	String urlType = "http://www.tv.com/search.php?type=11&stype=program&qs=";
	
	try {
	
	    URL url = new URL(urlType + title.replaceAll("[\\p{Blank}]+","%20"));
	    
	    StringBuffer data;
	    
	    try {
		data = HttpUtil.readDataToStringBuffer(url);
	    } catch (SocketTimeoutException s) {
		log.error("Exception: " + s.getMessage());
		data = null;
	    }
	    
	    if (data == null) {
		return null;
	    }
	    
	    //FileUtil.writeToFile("seriesMatches.html", data);
	    
	    String temp = "";
	    
	    int startlimit = 0;
	    int start = 0;
	    int stop = 0;
	    	    
	    startlimit = data.indexOf("search-results");
	    
	    if (startlimit == -1)
		return null;
		
	    String urlTitle = "";
	    String showNumber = "";
	    String showTitle = "";
	    
	    
	    while (true) {
	    
		start = data.indexOf(">Show:", startlimit)+1;
		stop = data.indexOf("</a>", start);
		
		if (stop < startlimit)
		    break;
		
		startlimit = stop;
		
		temp = data.substring(start, stop);
		
		if (temp.indexOf("summary.html") != -1) {
		    
		    if (!(temp.substring(temp.indexOf(">")+1, temp.length())).equals("")) {	
			
			urlTitle = temp.substring(temp.indexOf("tv.com/")+7, temp.indexOf("/show/"));
			
			showNumber = temp.substring(temp.indexOf("show/") +5, temp.indexOf("/summary"));
			
			showTitle = temp.substring(temp.indexOf(">")+1, temp.length());
			
			listModel.addElement(new ModelSearchHit(showNumber, urlTitle, showTitle));
		    }
		}
	    }
	    
	} catch (Exception e) {
	    log.error("", e);
	    
	    if (exception == null || !exception.equals("Server redirected too many  times"))
		exception = e.getMessage();
	    
	    listModel = null;
	} 
	/* Returns the model... */
	return listModel;
    }
    
    
    
    public static DefaultListModel getSeasons(ModelSearchHit modelSeries) {
	
	DefaultListModel listModel = new DefaultListModel();
	exception = null;
        
	String urlType = "http://www.tv.com/"+ modelSeries.getUrlTitle() +"/show/"+ 
	    modelSeries.getShowKey() +"/episode_guide.html";
	
	try {
	    
	    URL url = new URL(urlType);
	    
	    StringBuffer data = HttpUtil.readDataToStringBuffer(url);

	    //FileUtil.writeToFile("seasonsOutput.html", data);
	    
	    int startlimit = 0;
	    int start = 0;
	    int stop = 0;
	    int counter = 1;
	    
	    String title = "";
	    
	    startlimit = data.indexOf("Trivia Guide");
	    
	    start = data.indexOf("<option value=", startlimit) +1;
	    startlimit = start;
	    
	    /* No drop-down means only one season. */
	    if (data.indexOf("<b>Choose Season:</b>") == -1) {
		title = modelSeries.getTitle()+ " - Season "+ counter;
		listModel.addElement(new ModelSearchHit(modelSeries.getShowKey(), modelSeries.getUrlTitle(), title, counter));
	    }
	    else {
		/* Drop-down means multiple seasons */
		
		int end = data.indexOf(">All Seasons</option>") + 22;

		while (((start = data.indexOf("<option value=", startlimit)) != -1) && 
		       ((stop = data.indexOf("</option>", start)) != -1)) {
		    
		    if (start > end)
			break;
		    
		    startlimit = stop;
		
		    title = modelSeries.getTitle()+ " - Season "+ counter;
		    
		    listModel.addElement(new ModelSearchHit(modelSeries.getShowKey(), modelSeries.getUrlTitle(), title, counter++));
		}
	    }
	    
	} catch (Exception e) {
	    log.error("", e);
	    
	    if (exception == null || !exception.equals("Server redirected too many  times"))
		exception = e.getMessage();
	} 
	/* Returns the model... */
	
	return listModel;
    }
    
    public static StringBuffer [] getEpisodesStream(ModelSearchHit modelSeason) {
	
	StringBuffer data = null;
	StringBuffer [] streams = null;
	 
	String urlType = "http://www.tv.com/"+ modelSeason.getUrlTitle() +"/show/"+ modelSeason.getShowKey() +"/episode_guide.html&season="+ modelSeason.getSeasonNumber();
	
	try {
	    
	    URL url = new URL(urlType);
	    
	    try {
		data = HttpUtil.readDataToStringBuffer(url);
	    } catch (SocketTimeoutException s) {
		log.error("Exception: " + s.getMessage());
		data = null;
	    }
	    
	    if (data == null) {
		return null;
	    }
	    
	    //FileUtil.writeToFile("episodeStream.html", data);
	    
	    int counter = 1;
	     
	    /* Get number of episodes */
	    while (true) {
		if (data.indexOf("&amp;pg_episodes="+ (counter+1)) != -1)
		    counter++;
		else
		    break;
	    }
	     
	    streams = new StringBuffer[counter];
	    streams[0] = data;
	     
	    for (int i = 1; i < counter; i++) {
		 
		urlType = "http://www.tv.com/"+ modelSeason.getUrlTitle() +"/show/"+ modelSeason.getShowKey() +"/episode_guide.html&season="+modelSeason.getSeasonNumber()+ "&pg_episodes="+ (i+1);
		 
		url = new URL(urlType);
		
		try {
		    data = HttpUtil.readDataToStringBuffer(url);
		} catch (SocketTimeoutException s) {
		    log.error("Exception:" + s.getMessage());
		    data = null;
		}
		
		streams[i] = data;
	    }
	     
	} catch (Exception e) {
	    log.error("", e);
	     
	    if (exception == null || !exception.equals("Server redirected too many  times"))
		exception = e.getMessage();
	} 
	
	return streams;
    }
    
    public static DefaultListModel getEpisodes(ModelSearchHit modelSeason, StringBuffer [] streams) {
	
	DefaultListModel listModel = new DefaultListModel();
	StringBuffer data;
	exception = null;
        
	try {
	    
	    String temp = "";
	    String temp2 = "";
	    
	    int startlimit = 0;
	    int start = 0;
	    int stop = 0;
	    
	    String searchTitle = "";
	    
	    int safety = 0;
	    
	    int startCheck = 0;
	    

	    for (int i = 0; i < streams.length; i++) {
		
		//FileUtil.writeToFile("getEpisodes"+i, streams[i]);
		
		data = streams[i];
		startCheck = 0;
		
		//FileUtil.writeToFile("episodeOutput.html", data);
		
		startlimit = data.indexOf("Trivia Guide");
		
		String offset = "<h1 class=\"f-18 f-666\">";
		
		while (safety++ < 1000) {
		    
		    start = data.indexOf(offset, startlimit) + offset.length();
		    stop = data.indexOf("</a>", start);
		    startlimit = stop;
		    
		    if (start < startCheck) {
			break;
		    }
		    else
			startCheck = start;
		    
		    temp = data.substring(start, stop);
		    
		    if (temp.indexOf("summary.html") != -1) {
			
			temp = temp.trim();
			
			searchTitle = temp;
			
			temp2 = temp.substring(0, temp.indexOf(" ")) + " ";
			
			temp = temp.substring(temp.indexOf(">")+1, temp.length());
			
			/* Title */
			temp = temp2.concat(temp);
			
			listModel.addElement(new ModelSearchHit(modelSeason.getShowKey(), modelSeason.getUrlTitle(), temp, searchTitle, modelSeason.getSeasonNumber(), i));
			
		    }
		}
	    }
	    
	} catch (Exception e) {
	    log.error("", e);
	    
	    if (exception == null || !exception.equals("Server redirected too many  times"))
		exception = e.getMessage();
	} 
	/* Returns the model... */
	return listModel;
    }
    
    
    public static ModelEpisode getEpisodeInfo(ModelSearchHit episode, StringBuffer [] streams) {
	
	StringBuffer data = streams[episode.getStreamNumber()];
	
	String date = "";
	String writer = "";
	String director = "";
	String guestStar = "";
	String plot = "";
	String rating = "";
	String episodeUrlKey = "";
	int episodeNumber = 0;

	//FileUtil.writeToFile("episodeStream.html", data);
	
	try {
	
	    String temp = "";
	    String temp2 = "";
	    
	    int startlimit = 0;
	    int start = 0;
	    int stop = 0;
	    int end = 0;
	    
	    temp = encodeHTMLTitle(episode.getTitle());
	    
	    if (temp.indexOf(".") != -1) {
		/* I few episodes doesn't have a number. e.g pilots. */
		try {
		    episodeNumber = Integer.parseInt(temp.substring(0, temp.indexOf(".")));
		}
		catch (NumberFormatException e) {
		    ;
		}
	    }
	    
	    /* Finding the correct place in the stream by searching for the title */
	    temp = episode.getSearchTitle();
	    
	    startlimit = data.indexOf(temp);
	    	    
	    start = temp.indexOf("<a href=\"http://www.tv.com/");
	    stop = temp.indexOf("summary.html");
	    
	    temp2 = temp.substring(start + 26, stop);
	    
	    /* Getting episodeUrlKey */
	    episodeUrlKey = temp2;
	    
	    /* Gettimg rating */
	    start = data.indexOf("Community Score", startlimit);
	    
	    if (start > startlimit) {
		
		stop = data.indexOf("</a>", start);
		start = data.indexOf("\">", stop -10) +2;
		
		rating = data.substring(start, stop);
	    }
	    
	    start = data.indexOf("First aired:", startlimit) + 13;
	    
	    if (start > startlimit) {
		
		temp = HttpUtil.decodeHTML(data.substring(start, data.indexOf(" ", start)).trim());
		startlimit = start;
		
		/* Getting date */
		date = temp.substring(temp.lastIndexOf("/")+1, temp.length());
		date += "/" + temp.substring(0, temp.indexOf("/"));
		date += temp.substring(temp.indexOf("/"), temp.lastIndexOf("/"));
		
	    }
	    
	    /* Getting plot */
	    start = data.indexOf("<p>", startlimit)+3;
	    
	    if (start > startlimit) {
		stop = data.indexOf("</p>", start);
		startlimit = stop;
		
		plot = data.substring(start, stop).trim();
	    }
	    
	    /* Getting Writer */
	    start = data.indexOf("Writer:", startlimit) +14;
	    
	    if (start > startlimit) {
		
		end = data.indexOf("Director:", start);
		startlimit = start;
		
		while (0 < data.indexOf("<a href=", start) && data.indexOf("<a href=", start) < end) {
		    
		    start = data.indexOf("<a href=", start);
		    start = data.indexOf(">", start)+1;
		    stop = data.indexOf("</a>", start);
		    
		    if (!writer.equals(""))
			writer += ", ";
		    
		    writer += data.substring(start, stop).trim();
		    
		    startlimit = start = stop;
		}
	    }
	    
	    /* Getting Director */
	    start = data.indexOf("Director:", startlimit)+ 16;
	    
	    if (start > startlimit) {
		startlimit = start;
		
		end = data.indexOf("Guest star:", start);
		
		if (end > data.indexOf("Story:", startlimit) && data.indexOf("Story:", startlimit) != -1)
		    end = data.indexOf("Story:", startlimit);
		
		
		while (0 < data.indexOf("<a href=", start) && data.indexOf("<a href=", start) < end) {
		    
		    start = data.indexOf("<a href=", start);
		    start = data.indexOf(">", start)+1;
		    stop = data.indexOf("</a>", start);
		    
		    if (!director.equals(""))
			director += ", ";
		    
		    director += data.substring(start, stop).trim();
		    
		    stop += 4;
		    startlimit = start = stop;
		}
	    }
	    
	    /* Getting Story */
	    start = data.indexOf("Story:", startlimit)+ 18;

	    if (start > startlimit) {
		stop = data.indexOf("</a>", start);
		
		end = data.indexOf("Guest star:", startlimit);
		
		if (start != 17 && end > start) {
		    
		    writer += ", Story: ";
		    
		    while (0 < data.indexOf("<a href=", start) && data.indexOf("<a href=", start) < end) {
			
			start = data.indexOf("<a href=", start);
			start = data.indexOf(">", start)+1;
			stop = data.indexOf("</a>", start);
			
			if (!writer.equals(""))
			    writer += ", ";
			
			writer += data.substring(start, stop).trim();
			
			stop += 4;
			
			startlimit = start = stop;
		    }
		}
	    }
	    
	    /* Getting Guest Star */
	    start = data.indexOf("Guest star:", startlimit)+ 18;
	    
	    if (start > startlimit) {
		startlimit = start;
		end = data.indexOf("<div class=\"divider\"></div>", startlimit);
	    
		while (0 < data.indexOf("<a href=", start) && data.indexOf("<a href=", start) < end) {
		
		    start = data.indexOf("<a href=", start);
		    start = data.indexOf(">", start)+1;
		    stop = data.indexOf("</a>", start);
		
		    if (!guestStar.equals(""))
			guestStar += ", ";
		    else
			guestStar = "Guest stars:";
		    
		    guestStar += data.substring(start, stop).trim();
		
		    stop += 4;
		    startlimit = start = stop;
		}
	    }
	    
	} catch (Exception e) {
	    log.error("", e);
	    
	    if (exception == null || !exception.equals("Server redirected too many  times"))
		exception = e.getMessage();
	
	} 
	
	/* Returns the model... */
	return new ModelEpisode(-1, -1, episodeNumber, episodeUrlKey, "", date, episode.getTitle(), director, writer, "", rating, plot, guestStar, "", false, "", "", "", "", "", "", "", "", "");
    }
    
    
    protected static String cleanTitle(String toClean) {
	
	if (toClean.indexOf("&") != -1)
	    toClean = toClean.replaceAll("&amp;", "&");
	
	/* Returns the decoded string... */
	return toClean;
    }
    
    protected static String encodeHTMLTitle(String toEncode) {
	String encoded = "";
	
	encoded = toEncode.replaceAll("&", "&amp;");
	
	/* Returns the encoded string... */
	return encoded;
    }
    
    
    public static String getException() {
	return exception;
    }
    
    static void setException(String e) {
	exception = e;
    }
}
