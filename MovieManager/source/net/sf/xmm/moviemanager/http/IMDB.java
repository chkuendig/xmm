/**
 * @(#)IMDB.java 1.0 29.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.http;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;
import net.sf.xmm.moviemanager.models.ModelIMDB;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

public class IMDB {
  
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
     * Also known as of the movie
     **/
    private String _aka = "";
    
    /**
     * The country of the movie
     **/
    private String _country = "";
    
    /**
     * The language of the movie
     **/
    private String _language = "";
    
    /**
     * The Sound Mix of the movie
     **/
    private String _mpaa = "";
    
    /**
     * The Sound Mix of the movie
     **/
    private String _soundMix = "";
    
    /**
     * The Sound Mix of the movie
     **/
    private String _runtime = "";
    
    /**
     * The Sound Mix of the movie
     **/
    private String _certification = "";
    
    /**
     * The Sound Mix of the movie
     **/
    private String _awards = "";
    
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
  
    
    /**
     * The cover url.
     **/
    private String _coverName = "";
    
    /**
     * Reding ok...
     **/
    private boolean _coverOK = false;
    
    /* stores the last exception message */
    private static String exception;
    
    /**
     * The constructor. Initializes all vars (read from the net) for
     * the movie with key.
     **/
    public IMDB(String key) throws Exception {
	
	_key = key;
	
	/* Gets the url... */
	URL url = makeURL("http://akas.imdb.com/title/tt"+key+"/");
	
	/* Saves the page data in a string buffer... */
	StringBuffer data = HttpUtil.readDataToStringBuffer(url);
	
    if (data == null) {
        throw new Exception("Invalid HTTP link");
    }
    
    //FileUtil.writeToFile("imdb.html", data);
    parseData(data);
    }
    
    
    public IMDB(String key, StringBuffer data) throws Exception {
	
	_key = key;
	
	//FileUtil.writeToFile("imdb.html", data);
	
	parseData(data);
    }
    
    
    private void parseData(StringBuffer data) throws Exception {
	
	int start = 0;
	int stop = 0;
	int end = 0;
	
	Object [] tmpArray;
	
	try {
		/* Processes the data... */

		/* Gets the title... */
		if ((start = data.indexOf("<div id=\"tn15title\">", start)) != -1 &&
				(end = data.indexOf("</div>", start)) != -1) {

			System.err.println("title and date:" + data.substring(start, end));
			
			tmpArray = HttpUtil.decodeHTMLtoArray(data.substring(start, end));

			//_title = HttpUtil.decodeHTML(data.substring(start, end)).trim();

			_title = (String) tmpArray[0];

			System.err.println("_title:" + _title);

			if (MovieManager.getConfig().getAutoMoveThe() && _title.startsWith("The ")) {
				_title = _title.substring(_title.indexOf(" ")+1, _title.length())+ ", The";
			}
			else if (MovieManager.getConfig().getAutoMoveAnAndA() && (_title.startsWith("A ") || _title.startsWith("An "))) {
				_title = _title.substring(_title.indexOf(" ")+1, _title.length())+ ", "+ _title.substring(0, _title.indexOf(" "));
			}
			
			System.err.println("_title:" + _title);

			_date = (String) tmpArray[2];
		}

	    System.err.println("_date:" + _date);
	 	    
	    /* Gets the cover url... */
	    if ((start = data.indexOf("<div class=\"photo\">")) != -1 && 
	    		(end = data.indexOf("</div>", start)) != -1) {
	    	
	    	if (data.substring(start, end).indexOf("Poster Not Submitted") == -1) {
	    	
	    		if ((start = data.indexOf("src=\"",start) +5) !=4 &&
	    			(end = data.indexOf("\"", start)) != -1) {
	    				_coverURL = HttpUtil.decodeHTML(data.substring(start, end));
	    			}
	    			
	    			System.err.println("_coverURL:" + _coverURL);

	    			start = _coverURL.lastIndexOf(".");

	    			if (start != 0 && start != -1)
	    				_coverName = _key + _coverURL.substring(start, _coverURL.length());
	    	}
	    }
	    
	    System.err.println("_coverName:" + _coverName);
	    
	    start = 0;
	    stop = 0;
	    end = 0;
	    /* Gets the rating... */
	    if ((start = data.indexOf("User Rating:", start)+ 12) != 11 &&
	    		(end = data.indexOf("/10</b>",start)) != -1 &&
	    		(start = data.indexOf("<b>",end-9) +3) != 2) {
		
	    	_rating = HttpUtil.decodeHTML(data.substring(start, end));
	    }
	    
	    System.err.println("_rating:" + _rating);
	    
	    
	    start = 0;
	    stop = 0;
	    end = 0;
	    /* Gets the directed by... */
	    if ((start = data.indexOf("Directed by", start)) != -1 &&
		(stop = data.indexOf("<br/>", start)) != -1) {
	    	
	    	//System.err.println("start:" + start);
	    	//System.err.println("stop:" + stop);
	    	
	    	
	    	//System.err.println("Directed by:" + data.substring(start, stop));
	    	
	    	ArrayList list = getLinkContentName(data.substring(start, stop));
	    	
	    	//System.err.println("list:" + list.size());
	    	 
	    	while (!list.isEmpty()) {
	    		if (!_directedBy.equals(""))
	    			_directedBy += ", ";
	    			
	    		_directedBy += list.remove(0);
	    	}
	    }
	
	    System.err.println("_directedBy:" + _directedBy);
	    
	    
	    start = 0;
	    stop = 0;
	    end = 0;
	    
	    String tmp;

	    /* Gets the written by... */
	    if (((start = data.indexOf("Writing credits", start)) != -1) &&
	    		(stop = data.indexOf("<br/>", start)) != -1) {
	    	
	    	ArrayList list = getLinkContentName(data.substring(start, stop));
	    	
	    	while (!list.isEmpty()) {
	    		
	    			if (!_writtenBy.equals(""))
	    			_writtenBy += ", ";
	    			
	    		_writtenBy += list.remove(0);
	    	}
	    }
	    	
	    System.err.println("_writtenBy:" + _writtenBy);
	    	    
	    _genre = getClassInfo(data, "Genre:");
	    
	    System.err.println("_genre:" + _genre);
	    
	    _plot = getClassInfo(data, "Plot Outline:");
	    
	    System.err.println("_plot:" + _plot);
	    
	    _cast = getClassInfo(data, "class=\"cast\">");
	    
	    _cast = _cast.replaceAll(" \\.\\.\\.", ",");
	    
	    System.err.println("_cast:" + _cast);
	    
	    
	    _aka = getClassInfo(data, "Also Known As:");
	    
	    System.err.println("_aka:" + _aka);
	    
	    
	    _mpaa = getClassInfo(data, "<a href=\"/mpaa\">MPAA</a>:");
	    	    
	    System.err.println("_mpaa:" + _mpaa);
	    
	    _runtime = getClassInfo(data, "Runtime:");
    
	    _country = getClassInfo(data, "Country:");
	    
	    _language = getClassInfo(data, "Language:");
	    
	    _colour = getClassInfo(data, "Color:");
	    
	    _soundMix = getClassInfo(data, "Sound Mix:");
	    
	    _certification = getClassInfo(data, "Certification:");
	    
	    _awards = getClassInfo(data, "Awards:");
	    
	    System.err.println("_awards:" + _awards);
	    
	    
	    if (1 == 1) {

	    /* Gets a bigger plot (if it exists...)
	       /* Creates the url... */
	    URL url = new URL("http://akas.imdb.com/title/tt"+_key+"/plotsummary");
	    
	    data = HttpUtil.readDataToStringBuffer(url);   
	    
	    /* Processes the data... */
	    start = 0;
	    end = 0;
	    
	    if ((start = data.indexOf("class=\"plotpar\">",start)+16) != 15 &&
		(end=data.indexOf("</p>",start)) != -1) {
		_plot = HttpUtil.decodeHTML(data.substring(start, end));
	    }
	    
	    System.err.println("_plot:" + _plot);
		   
	    }
	} catch (Exception e) {
	    log.error("", e);
	}
    }

    /**
     * Gets the key.
     **/
    public String getKey() {
	return _key;
    }

    /**
     * Gets the date.
     **/
    public String getDate() {
	return _date;
    }
  
    /**
     * Gets the title.
     **/
    public String getTitle() {
	return _title;
    }
  
    /**
     * Gets the durected by.
     **/
    public String getDirectedBy() {
	return _directedBy;
    }
  
    /**
     * Gets the written by.
     **/
    public String getWrittenBy() {
	return _writtenBy;
    }
  
    /**
     * Gets the genre.
     **/
    public String getGenre() {
	return _genre;
    }
  
    /**
     * Gets the rating.
     **/
    public String getRating() {
	return _rating;
    }
  
    /**
     * Gets the colour.
     **/
    public String getColour() {
	return _colour;
    }

    /**
     * Gets the country.
     **/
    public String getCountry() {
	return _country;
    }

    /**
     * Gets the language.
     **/
    public String getLanguage() {
	return _language;
    }
    
    /**
     * Gets the plot.
     **/
    public String getPlot() {
	return _plot;
    }
  
    /**
     * Gets the cast.
     **/
    public String getCast() {
	return _cast;
    }
    
    /**
     * Gets the aka.
     **/
    public String getAka() {
	return _aka;
    }
    
    /**
     * Gets the mpaa.
     **/
    public String getMpaa() {
	return _mpaa;
    }
    
    /**
     * Gets the Sound Mix.
     **/
    public String getSoundMix() {
	return _soundMix;
    }

    /**
     * Gets the Runtime.
     **/
    public String getRuntime() {
	return _runtime;
    }
    
    /**
     * Gets the Certification.
     **/
    public String getCertification() {
	return _certification;
    }

    /**
     * Gets the Awards.
     **/
    public String getAwards() {
	return _awards;
    }
    
    
    /**
     * Gets the cover url.
     **/
    public String getCoverName() {
    return _coverName;
    }
    
    /**
     * Gets the cover url.
     **/
    public String getCoverURL() {
	return _coverURL;
    }
  
    /**
     * Gets the cover.
     **/
    public byte[] getCover() {
      
	byte[] coverData = {-1};
	try {
	    
	    if (!_coverURL.equals("")) {
		
		URL url = new URL(_coverURL);
		coverData = HttpUtil.readDataToByteArray(url);
		
		_coverOK = true;
	    }
	} catch (Exception e) {
	    log.error("", e);
	    _coverOK = false;
	} 
	
	/* Returns the data... */
	return coverData;
    }
    
    
    /**
     * Returns true if the last cover reading went ok..
     **/
    public boolean getCoverOK() {
	return _coverOK;
    }

    /**
     * Returns simple matches list...
     **/
    public static DefaultListModel getSimpleMatches(String title) throws Exception {
	
	return getMatches("http://akas.imdb.com/find?s=tt&q="+ title);
	                  // http://akas.imdb.com/find?s=tt&q=Predator 
	                  // http://akas.imdb.com/find?s=all&q=predator
	//	return getMatches("http://www.imdb.com/find?tt=on;q=" + title);
	//return getMatches("http://www.imdb.com/find?tt=on;q=",title);
    }

    /**
     * Returns extended matches list...
     **/
    public static DefaultListModel getExtendedMatches(String title) throws Exception {
	return getMatches("http://akas.imdb.com/find?more=tt;q=" + title);
    }
    
    /**
     * Returns a DefaultListModel with ModelMovie's of the movies that IMDB
     * returned for the searched title.
     *
     * urlType = http://www.imdb.com/find?tt=on;q= or
     *           http://www.imdb.com/find?more=tt;q=
     **/
    private static DefaultListModel getMatches(String urlType) throws Exception {
	DefaultListModel listModel = new DefaultListModel();
	exception = null;
    
	try {
	
	    URL url = new URL(urlType.replaceAll("[\\p{Blank}]+","%20"));
	    
	    StringBuffer data = HttpUtil.readDataToStringBuffer(url);
	    
        FileUtil.writeToFile("direct-simple", data);
        
	    int start = 0, end = 0, stop = 0;
	    String key = "";
	    String movieTitle = "", aka = "";
	    int titleSTart, titleEnd;
	    int otherResults = data.indexOf("Other Results");
	    int partialMatches = data.indexOf("Titles (Partial Matches)");
	    
	    /* If there's only one movie for that title it goes directly to that site...  */
	    if (!data.substring(data.indexOf("<title>")+7, data.indexOf("<title>")+11).equals("IMDb")) {
		/* Gets the title... */
	    
		titleSTart = data.indexOf("<title>", start)+7;
		titleEnd = data.indexOf("</title>", titleSTart);
		movieTitle = HttpUtil.decodeHTML(data.substring(titleSTart, titleEnd));
	    
		if ((start=data.indexOf("title/tt",start) + 8) != 7) {
            key = HttpUtil.decodeHTML(data.substring(start, start + 7));
		}
		
		/* Getting aka titles */
		if ((start = data.indexOf("Also Known As:")) != -1) {
		    start += 18;
	  
		    stop = data.indexOf("<b class=", start)-5;
	  	  
		    while (true) {
			if (start >= stop) break;
			start += 4;
			end = data.indexOf("<br>", start);
			if (!aka.equals(""))
			    aka += FileUtil.getLineSeparator(); // windows == "\r\n";, linuz == \n
			aka += HttpUtil.decodeHTML(data.substring(start, end));
			start = data.indexOf("<br>", end);
		    }
		    aka = MovieManagerCommandAddMultipleMoviesByFile.performExcludeParantheses(aka, false);
		}
		
		listModel.addElement(new ModelIMDB(key, movieTitle, aka));
		return listModel;
	    }
	    /* Processes the data... */
	    start = 0;
	    end = 0;
	    while (true) {
 
		aka = "";

		if ((start = data.indexOf("/title/tt", start)+9) == 8) break;
		
		if ((end = data.indexOf("/\">", start)) == -1) break;
	    
		/* the string "Other Results only occurs with the simplematches url,
		   therefore the variable otherResults will contain -1 when 
		   using the extended matches url*/
	    
		if ((otherResults != -1) && (partialMatches != -1) && (start > partialMatches)) 
		    break;
		
		key = HttpUtil.decodeHTML(data.substring(start, start+7));
	    
		start += key.length() + 3;
		if ((end = data.indexOf("</", start)) == -1) 
		    break;
		
		titleSTart = data.indexOf(">", start)+1;
		titleEnd = data.indexOf("</a>", titleSTart);
		movieTitle = HttpUtil.decodeHTML(data.substring(titleSTart, titleEnd));
	    
		/* Skipping the </a> and adds the year to the movieTitle*/
		titleSTart = titleEnd+4;
		titleEnd += 11;
	    
		movieTitle = movieTitle.concat(data.substring(titleSTart, titleEnd));
		
		start += movieTitle.length() + 2;
		
		end = data.indexOf("</li>", start);
		
		/* Parses the aka-titles... */
		while (data.indexOf(";aka", start) < end) {
		    titleSTart = data.indexOf(";aka", start)+1; 
		    titleEnd = data.indexOf("<br>", titleSTart);
		    
		    if (titleEnd < titleSTart || titleEnd > end)
			titleEnd = data.indexOf("</li>", titleSTart);
		    
		    if (titleEnd > end)
			break;
		    
		    if (titleSTart > 0 && titleEnd > 0) {
			start = titleEnd;
			
			if (!aka.equals("")) {
			    aka += FileUtil.getLineSeparator();
			}
			aka += HttpUtil.decodeHTML(data.substring(titleSTart, titleEnd));
		    }
		    else
			break;
		}
		
		/* Adds to the list model... */
		boolean insert = true;
		for (int i = 0; i < listModel.size(); i++) {
			if (((ModelIMDB)listModel.elementAt(i)).getKey().equals(key)) {
				insert = false;
				break;
			}
		}
		if (insert) {
			listModel.addElement(new ModelIMDB(key, movieTitle, aka));
		}
	    }
		
		
	} catch (IndexOutOfBoundsException i) {
		log.warn(i.getMessage());
		
	} catch (MalformedURLException m) {
        log.warn(m.getMessage());
	}
	
	/* Returns the model... */
	return listModel;
    }
    

    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static String getClassInfo(StringBuffer data, String className) {
    	String decoded = null;
    	String tmp = "";

    	try {
    		int start = 0;
    		int end = 0;
    		boolean found = false;
    		
    		while ((start = data.indexOf("<div class=\"info\">", end)) != -1 && 
    				(end = data.indexOf("</div>", start)) != -1) {

    			tmp = data.substring(start, end);	

    			if (tmp.indexOf(className) != -1) {
    				
    				//System.err.println("start1:" + start);
    				
    				start = tmp.indexOf(className) + className.length();
    				tmp = tmp.substring(start, tmp.length());	
    				
    				//System.err.println("tmp:" + tmp);
    				
    				//System.err.println("start2:" + start);
    				
    				//System.err.println("Found:" + className);
    				found = true;
    				break;
    			}
    		}

    		if (!found)
    			return "";
    		
    		
    		//end = tmp.indexOf("<a class=\"tn15more inline");
    		end = tmp.indexOf("<a class=\"tn15more\"");
    		
    		
    		if (end != -1) {
    			tmp = tmp.substring(0, end);
    		}
    		
    		
    		
    		if (className.equals("<a href=\"/mpaa\">MPAA</a>")) {
    			System.err.println("MPAA-tmp:" + tmp);
    		}	
    		
    		if (className.equals("Also Known As:")) {
    			System.err.println("Also Known As found");
    			System.err.println("aka-tmp:" + tmp);
    			
    			decoded = decodeAka(tmp);
    		}
    		else if (className.equals("class=\"cast\">")) {
    			decoded = decodeCast(tmp);
    		}
    		else
    			decoded = HttpUtil.decodeHTML(tmp);
    			
    		
    	} catch (Exception e) {
    		log.error("", e);
    	} 
    	/* Returns the decoded string... */
    	return decoded.trim();
    }
    
    
    
    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static ArrayList getLinkContentName(String toDecode) {
    	ArrayList decoded = new ArrayList();
    	String tmp = "";
		
	try {
		int start = 0;
	    int end = 0;
	    
	    while ((start = toDecode.indexOf("<a href=", start)) != -1) {
	    	
	    	start = toDecode.indexOf(">", start) +1;
	    	end = toDecode.indexOf("</a>", start);
	    	
	    	tmp = toDecode.substring(start, end);
	    	decoded.add(HttpUtil.decodeHTML(tmp.trim()));
	    }
	} catch (Exception e) {
	    log.error("", e);
	} 
	/* Returns the decoded string... */
	return decoded;
    }
    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static String decodeAka(String toDecode) {
	String decoded = "";
		
	try {
		String [] akaTitles = toDecode.split("<br>");
		
		for (int i = 0; i < akaTitles.length; i++) {
			decoded += HttpUtil.decodeHTML(akaTitles[i]) + "\r\n";
		}
			
	} catch (Exception e) {
	    log.error("", e);
	} 
	/* Returns the decoded string... */
	return decoded.trim();
    }
    
    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static String decodeCast(String toDecode) {
	StringBuffer decoded = new StringBuffer();
		
	try {
		String [] castSplit = toDecode.split("<td class=\"hs\">");
		String [] tmp;
		
		for (int i = 0; i < castSplit.length; i++) {
			
			tmp = HttpUtil.decodeHTML(castSplit[i]).split(" \\.\\.\\.");
			
			if (tmp.length == 2) {
				decoded.append(tmp[0].trim());
				decoded.append(" (" + tmp[1].trim() + "), ");
			}
			
				//decoded += decodeHTML(akaTitles[i]) + "\r\n";
		}
		
		System.err.println("cast decoded:" + decoded);
			
	} catch (Exception e) {
	    log.error("", e);
	} 
	/* Returns the decoded string... */
	return decoded.toString();
    }
    
    
    
    /* Creates the URL and sets the appropriate proxy values */
    protected static URL makeURL(String url) {
	
	MovieManagerConfig mm = MovieManager.getConfig();
	URL urlData = null;
	
	try {
	    if (mm.getProxyEnabled()) {
		
		String host = mm.getProxyHost();
		String port = mm.getProxyPort();
		
		/*Adds proxy settings*/
		java.util.Properties systemSettings = System.getProperties();
		
		if (mm.getProxyType().equals("HTTP")) {
		    systemSettings.put("proxySet", "true");
		    systemSettings.put("proxyHost", host);
		    systemSettings.put("proxyPort", port);
		}
		else {
		    systemSettings.put("socksProxySet", "true");
		    systemSettings.put("socksProxyHost", host);
		    systemSettings.put("socksProxyPort", port);
		}
		
		/*Saves proxy settings*/
		System.setProperties(systemSettings);
		
		if (mm.getAuthenticationEnabled()) {
		    String user = mm.getProxyUser();
		    String password = mm.getProxyPassword();
		    
		    /*Adds authentication*/
		    Authenticator.setDefault(new MyAuth(user, password));
		}
		else
		    Authenticator.setDefault(null);/*Removes authentication*/
	    }
	    else if (!MovieManager.isApplet()){
		/*Removes proxy settings*/
		java.util.Properties systemSettings = System.getProperties();
		systemSettings.remove("proxySet");
		systemSettings.remove("proxyHost"); 
		systemSettings.remove("proxyPort");
		
		systemSettings.remove("socksProxySet");
		systemSettings.remove("socksProxyHost"); 
		systemSettings.remove("socksProxyPort");
		System.setProperties(systemSettings);
		
		/*Removes authentication*/
		Authenticator.setDefault(null);
	    }
	    urlData = new URL(url);
	}
	catch (Exception e) {
	    log.error("", e);
	}
	return urlData;
    }
    
    public static String getException() {
	return exception;
    }
    
    static void setException(String e) {
	exception = e;
    }
    
    public static String extractTime(String toBeCleaned) {
	
	boolean start = false;
	String result = "";
	
	for (int i = 0; i < toBeCleaned.length(); i++) {
	    
	    if (Character.isDigit(toBeCleaned.charAt(i)))
		start = true;
	    
	    if (start) {
		
		if (Character.isDigit(toBeCleaned.charAt(i))) {
		    result += toBeCleaned.charAt(i);
		}
		else {
		    break;
		}
	    }
	}
	return result;
    }
    
    
    
}
