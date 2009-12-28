/**
 * @(#)IMDB.java 1.0 29.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.http;

import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.models.imdb.*;

import org.apache.log4j.Logger;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IMDB /*extends IMDB_if */{
  
	static Logger log = Logger.getLogger(IMDB.class);
    
	public final static String version = "1.0.0";
	public final static String numveric_version = "100";
	
    private HttpUtil httpUtil = new HttpUtil();
    
    private HttpSettings settings = null;
    
    private ModelIMDbEntry lastDataModel;
    
    public final String [] movieHitCategory = {"Popular Titles", "Titles (Exact Matches)", "Titles (Partial Matches)", "Titles (Approx Matches)"};
	    
    public IMDB() throws Exception {
    	this(null, null, null);		
    }
    
    public IMDB(String urlID) throws Exception {
    	this(urlID, null, null);		
    }
    
    public IMDB(String urlID, StringBuffer data) throws Exception {
    	this(urlID, data, null);		
    }
    
    public IMDB(String urlID, HttpSettings settings) throws Exception {
    	this(urlID, null, settings);	
    }
    
    public IMDB(HttpSettings settings) throws Exception {
    	this(null, null, settings);	
    }
         
    /*
    public IMDB getIMDB() throws Exception {
    	return new IMDB();
    }
    
    public IMDB getIMDB(String urlID) throws Exception {
    	return new IMDB(urlID);
    }
    
    public IMDB getIMDB(Strin g urlID, StringBuffer data) throws Exception {
    	return new IMDB(urlID, data);
    }
    
    public IMDB getIMDB(String urlID, HttpSettings settings) throws Exception {
    	return new IMDB(urlID, settings);
    }
    
    public IMDB getIMDB(HttpSettings settings) throws Exception {
    	return new IMDB(settings);
    }
    */
    
    
    /**
     * The constructor. Initializes all vars (read from the net) for
     * the movie with key.
     **/
    public IMDB(String urlID, StringBuffer data, HttpSettings settings) throws Exception {
	
		this.settings = settings;
		httpUtil = new HttpUtil(settings);
		
		if (urlID != null || data != null)
			grabInfo(urlID, data);
    }
      
    public ModelIMDbEntry grabInfo(String urlID) throws Exception {
    	return grabInfo(urlID, null);	
    }
    
    public StringBuffer getURLData(String urlID) throws Exception {
    	
    	if (urlID == null)
    		throw new Exception("Movie ID is empty");
    
    	URL url = new URL("http://akas.imdb.com/title/tt"+ urlID +"/");
		HTTPResult res = httpUtil.readData(url);
		return res.data;
    }
    
    public ModelIMDbEntry grabInfo(String urlID, StringBuffer data) throws Exception {
    	
    	//long time = System.currentTimeMillis();
    	
    	if (urlID == null && data == null)
    		throw new Exception("Input data is null.");
    	
    	if (urlID != null) {
    		data = getURLData(urlID);
    	}
    	
    	if (data == null) {
    		throw new Exception("Error occured when reading data.(urlID:"+ urlID +")");
		}
    	
    	return parseData(urlID, data);
    }
    
    /*
     * If urlID is null, no extra plot will be retrieved
     */
    private ModelIMDbEntry parseData(String urlID, StringBuffer data) throws Exception {
		
        String date = "", title = "", directedBy = "", writtenBy = "", genre = "", rating = "", colour = "", aka = "", 
        country = "", language = "", mpaa = "", soundMix = "", runtime = "", certification = "", awards = "", plot = "", cast = "", 
        coverURL = "", coverName = "", seasonNumber = "", episodeNumber = "";
    	  
    	//long time = System.currentTimeMillis();
    	
		int start = 0;
		int end = 0;
	
		Object [] tmpArray;
	
		boolean isEpisode = false;
		boolean isSeries = false;
		
		net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/imdb.html", data);
			
		try {
			/* Processes the data... */

			if (data.indexOf("Full Episode List") != -1)
				isEpisode = true;
			
			/* Gets the title... */
			if ((start = data.indexOf("<div id=\"tn15title\">", start)) != -1 &&
					(end = data.indexOf("</div>", start)) != -1) {

				tmpArray = HttpUtil.decodeHTMLtoArray(data.substring(start, end));

				if (isEpisode) {
					title = (String) tmpArray[1];
					date = (String) tmpArray[2];
					
					if (date.startsWith("(") && date.endsWith(")"))
						date = date.substring(1, date.length() -1);
					
				}
				else { 
					title = (String) tmpArray[0];
					date = (String) tmpArray[2];
				}
				
				if (!isEpisode && title.startsWith("\""))
					isSeries = true;
			}	
			else
				throw new Exception("Title could not be found");
	    
			ModelIMDbEntry tmpModel = null;
			
			if (isSeries)
				tmpModel = new ModelIMDbSeries();
			
			else if (isEpisode)
				tmpModel = new ModelIMDbEpisode();
			
			else
				tmpModel = new ModelIMDbMovie();
			
			// Must be accessible from within inner class
			final ModelIMDbEntry dataModel = tmpModel;
			
			dataModel.setTitle(title);
			dataModel.setDate(date);
			dataModel.setUrlID(urlID);
			
			coverURL = null;
						
			boolean getCover = false;
			
			/* Gets the cover url... */
			if ((start = data.indexOf("<div class=\"photo\">")) != -1 && 
				(end = data.indexOf("</div>", start)) != -1) {
	    	
				String tmp = data.substring(start, end);
				
				if (tmp.indexOf("Poster Not Submitted") == -1) {
	    	
					if ((start = data.indexOf("src=\"", start) +5) !=4 &&
						(end = data.indexOf("\"", start)) != -1) {
						coverURL = HttpUtil.decodeHTML(data.substring(start, end));
						
						getCover = true;
						
						dataModel.setCoverURL(coverURL);
						
						start = coverURL.lastIndexOf(".");

						if (start != 0 && start != -1) {
							coverName = urlID + coverURL.substring(start, coverURL.length());
							dataModel.setCoverName(coverName);
						}
						
						// Get id of big cover
												
						Pattern p = Pattern.compile("href=\".*/media/(rm\\d+/tt\\d+)\"");
												
						Matcher m = p.matcher(tmp);
						
						if (m.find()) {
						
							String g = m.group();
						
							//System.err.println("g:" + m.group(0));
							//System.err.println("g1:" + m.group(1));
							
							dataModel.bigCoverUrlId = m.group(1);
							
						}
						//rm3535314176/tt0093773
					}
				}
			}
	    				
			final ReentrantLock lock = new ReentrantLock();
			
			if (getCover) {
								
				Thread t = new Thread(new Runnable() {
					public void run() {
						try {
							//long coverTime = System.currentTimeMillis();
							lock.lock();
							retrieveCover(dataModel);
						} finally {
							lock.unlock();
						}
					}
				});
				t.start();
			}
			
			start = 0;
			end = 0;
			
			/* Gets the rating... */
			if ((start = data.indexOf("User Rating:", start)) != -1 && 
					((start = data.indexOf("general rating", start)) != -1) &&
					(end = data.indexOf("/10</b>",start)) != -1 &&
					(start = data.indexOf("<b>",end-9) +3) != 2) {

				rating = HttpUtil.decodeHTML(data.substring(start, end));
				dataModel.setRating(rating);
			}
	     
			start = 0;
			end = 0;
			
			
			// Gets the directed by... 
			
			HashMap<String, String> classInfo = decodeClassInfo(data);
			
			String tmp = "";
			ArrayList <String> list;
			
			if (classInfo.containsKey("Director:")) {
				directedBy = getDecodedClassInfo("Director", (String) classInfo.get("Director:"));
			}
			else if (classInfo.containsKey("Directors:")) {
				
				tmp = (String) classInfo.get("Directors:");
				list = getLinkContentName(tmp);
		    	 
				while (!list.isEmpty()) {
					if (!directedBy.equals(""))
						directedBy += ", ";
		    			
					directedBy += list.remove(0);
				}
			}
				
			dataModel.setDirectedBy(directedBy);
			
			tmp = null;
			
			//Set<String> keys = classInfo.keySet();
			//for (String key : keys)
			//	System.err.println("key:" + key);
			
			// Gets the Writer or Writers (Writer matches both Writer: and Writers:)
			if (classInfo.containsKey("Writer:")) {
				//tmp = getClassInfo(data, "Writer:");
				tmp = classInfo.get("Writer:");
			}
			if (classInfo.containsKey("Writers:")) {
				//tmp = getClassInfo(data, "Writers:");
				tmp = classInfo.get("Writers:");
			}
			else if (classInfo.containsKey("Creator:")) {
				tmp = classInfo.get("Creator:");
				//tmp = getClassInfo(data, "Creator:");
			}
			
			if (tmp != null) {
				//tmp = tmp.substring(tmp.indexOf(":")+1, tmp.length());
				
				list = getLinkContentName(tmp);

				while (!list.isEmpty()) {
					if (!writtenBy.equals(""))
						writtenBy += ", ";

					writtenBy += list.remove(0);
				}

				dataModel.setWrittenBy(writtenBy);
			}
			
			if (classInfo.containsKey("Genre:")) {
				genre = getDecodedClassInfo("Genre:", (String) classInfo.get("Genre:"));
				genre = genre.replaceAll("(more)$", "");
				dataModel.setGenre(genre);
			}
			
			if (classInfo.containsKey("Plot:"))
				plot = getDecodedClassInfo("Plot:", (String) classInfo.get("Plot:"));
			          
			
			cast = getDecodedClassInfo("class=\"cast\">", data);
			cast = cast.replaceAll(" \\.\\.\\.", ",");
			cast = HttpUtil.decodeHTML(cast);
			
			dataModel.setCast(cast);
			
			if (classInfo.containsKey("Also Known As:")) {
				aka = getDecodedClassInfo("Also Known As:", (String) classInfo.get("Also Known As:"));
				aka = aka.trim();
				dataModel.setAka(aka);
			}
			
			mpaa = getDecodedClassInfo("<a href=\"/mpaa\">MPAA</a>:", data);
			dataModel.setMpaa(mpaa);
			
			if (classInfo.containsKey("Runtime:")) {
				runtime = getDecodedClassInfo("Runtime:", (String) classInfo.get("Runtime:"));
				dataModel.setWebRuntime(runtime);
			}
			
			if (classInfo.containsKey("Country:")) {
				country = getDecodedClassInfo("Country:", (String) classInfo.get("Country:"));
				dataModel.setCountry(country);
			}
			
			if (classInfo.containsKey("Language:")) {
				language = getDecodedClassInfo("Language:", (String) classInfo.get("Language:"));
				dataModel.setLanguage(language);
			}
			
			if (classInfo.containsKey("Color:")) {
				colour = getDecodedClassInfo("Color:", (String) classInfo.get("Color:"));
				dataModel.setColour(colour);
			}
			
			if (classInfo.containsKey("Sound Mix:")) {
				soundMix = getDecodedClassInfo("Sound Mix:", (String) classInfo.get("Sound Mix:"));
				dataModel.setWebSoundMix(soundMix);
			}
			
			if (classInfo.containsKey("Certification:")) {
				certification = getDecodedClassInfo("Certification:", (String) classInfo.get("Certification:"));
				dataModel.setCertification(certification);
			}
			
			if (classInfo.containsKey("Awards:")) {
				awards = getDecodedClassInfo("Awards:", (String) classInfo.get("Awards:"));
				awards = awards.replaceAll("(more)$", "");
				dataModel.setAwards(awards);
			}
			
			String airdateContent = null;
			
			if (classInfo.containsKey("Original Air Date:"))
				airdateContent = getDecodedClassInfo("Original Air Date:", (String) classInfo.get("Original Air Date:"));
						
			// Ex   29 April 2002 (Season 3, Episode 19)
			// Ex: 5 October 1999 (Season 1, Episode 1)
			if (airdateContent != null) {
				Pattern p = Pattern.compile("(.+)?\\s\\(.+?(\\d+?),\\s?.+?(\\d+?)\\)");
				Matcher m = p.matcher(airdateContent);
								
				if (m.find()) {

					int gCount = m.groupCount();

					if (gCount == 3) {
							
						//String airdat = m.group(1);
						String season = m.group(2);
						String episode = m.group(3);
	
						seasonNumber = season;
					    episodeNumber = episode;
					    
					    ((ModelIMDbEpisode) dataModel).setSeasonNumber(seasonNumber);
					    ((ModelIMDbEpisode) dataModel).setEpisodeNumber(episodeNumber);
					}
				}
			}
			
			/* Gets a bigger plot (if it exists...) */
			URL url = new URL("http://akas.imdb.com/title/tt"+ urlID +"/plotsummary");
	    
			HTTPResult result = httpUtil.readData(url);
			data = result.data;
			
			/* Processes the data... */
			start = 0;
			end = 0;
	    					
			if (data != null) {
					
				if ((start = data.indexOf("class=\"plotpar\">",start)+16) != 15 &&
						(end=data.indexOf("</p>",start)) != -1) {
					plot = HttpUtil.decodeHTML(data.substring(start, end));

					if (plot.indexOf("Written by") != -1)
						plot = plot.substring(0, plot.indexOf("Written by"));
				}
			}
			
			if (plot != null) {
				plot = plot.trim();
				plot = plot.replaceAll("(more)$", "");
				dataModel.setPlot(plot);
			}
			
			lock.tryLock((long) 10, TimeUnit.SECONDS);
			
			lastDataModel = dataModel;
			
			return dataModel;
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return null;
    }

    
    
    private ModelIMDbEntry parseData2(String urlID, StringBuffer data) throws Exception {
		
        String date = "", title = "", plot = "", coverURL = "", coverName = "";
    	  
    	//long time = System.currentTimeMillis();
    	
		int start = 0;
		int end = 0;
	
		Object [] tmpArray;
	
		boolean isEpisode = false;
		boolean isSeries = false;
		
		net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/imdb.html", data);
			
		try {
			/* Processes the data... */

			if (data.indexOf("Full Episode List") != -1)
				isEpisode = true;
			
			/* Gets the title... */
			if ((start = data.indexOf("<div id=\"tn15title\">", start)) != -1 &&
					(end = data.indexOf("</div>", start)) != -1) {

				tmpArray = HttpUtil.decodeHTMLtoArray(data.substring(start, end));

				if (isEpisode) {
					title = (String) tmpArray[1];
					date = (String) tmpArray[2];
					
					if (date.startsWith("(") && date.endsWith(")"))
						date = date.substring(1, date.length() -1);
					
				}
				else { 
					title = (String) tmpArray[0];
					date = (String) tmpArray[2];
				}
				
				if (!isEpisode && title.startsWith("\""))
					isSeries = true;
			}	
			else
				throw new Exception("Title could not be found");
	    
			ModelIMDbEntry tmpModel = null;
			
			if (isSeries)
				tmpModel = new ModelIMDbSeries();
			
			else if (isEpisode)
				tmpModel = new ModelIMDbEpisode();
			
			else
				tmpModel = new ModelIMDbMovie();
			
			// Must be accessible from within inner class
			final ModelIMDbEntry dataModel = tmpModel;
			
			dataModel.setTitle(title);
			dataModel.setDate(date);
			dataModel.setUrlID(urlID);
			
			coverURL = null;
						
			boolean getCover = false;
			
			/* Gets the cover url... */
			if ((start = data.indexOf("<div class=\"photo\">")) != -1 && 
				(end = data.indexOf("</div>", start)) != -1) {
	    	
				String tmp = data.substring(start, end);
				
				if (tmp.indexOf("Poster Not Submitted") == -1) {
	    	
					if ((start = data.indexOf("src=\"", start) +5) !=4 &&
						(end = data.indexOf("\"", start)) != -1) {
						coverURL = HttpUtil.decodeHTML(data.substring(start, end));
						
						getCover = true;
						
						dataModel.setCoverURL(coverURL);
						
						start = coverURL.lastIndexOf(".");

						if (start != 0 && start != -1) {
							coverName = urlID + coverURL.substring(start, coverURL.length());
							dataModel.setCoverName(coverName);
						}
						
						// Get id of big cover
												
						Pattern p = Pattern.compile("href=\".*/media/(rm\\d+/tt\\d+)\"");
						
						Matcher m = p.matcher(tmp);
						
						if (m.find()) {
						
							String g = m.group();
						
							//System.err.println("g:" + m.group(0));
							//System.err.println("g1:" + m.group(1));
							
							dataModel.bigCoverUrlId = m.group(1);
							
						}
						//rm3535314176/tt0093773
					}
				}
			}
	    				
			final ReentrantLock lock = new ReentrantLock();
			
			if (getCover) {
								
				Thread t = new Thread(new Runnable() {
					public void run() {
						try {
							//long coverTime = System.currentTimeMillis();
							lock.lock();
							retrieveCover(dataModel);
						} finally {
							lock.unlock();
						}
					}
				});
				t.start();
			}
			
			
			parseDataUsingXPath(dataModel, data, urlID);
			
			
			start = 0;
			end = 0;
			
			/*
			
			
			if (classInfo.containsKey("Original Air Date:"))
				airdateContent = getDecodedClassInfo("Original Air Date:", (String) classInfo.get("Original Air Date:"));
						
			// Ex   29 April 2002 (Season 3, Episode 19)
			// Ex: 5 October 1999 (Season 1, Episode 1)
			if (airdateContent != null) {
				Pattern p = Pattern.compile("(.+)?\\s\\(.+?(\\d+?),\\s?.+?(\\d+?)\\)");
				Matcher m = p.matcher(airdateContent);
								
				if (m.find()) {

					int gCount = m.groupCount();

					if (gCount == 3) {
							
						//String airdat = m.group(1);
						String season = m.group(2);
						String episode = m.group(3);
	
						seasonNumber = season;
					    episodeNumber = episode;
					    
					    ((ModelIMDbEpisode) dataModel).setSeasonNumber(seasonNumber);
					    ((ModelIMDbEpisode) dataModel).setEpisodeNumber(episodeNumber);
					}
				}
			}
			*/	
			
			/* Gets a bigger plot (if it exists...) */
			URL url = new URL("http://akas.imdb.com/title/tt"+ urlID +"/plotsummary");
	    
			HTTPResult result = httpUtil.readData(url);
			data = result.data;
			
			/* Processes the data... */
			start = 0;
			end = 0;
	    					
			if (data != null) {
					
				if ((start = data.indexOf("class=\"plotpar\">",start)+16) != 15 &&
						(end=data.indexOf("</p>",start)) != -1) {
					plot = HttpUtil.decodeHTML(data.substring(start, end));

					if (plot.indexOf("Written by") != -1)
						plot = plot.substring(0, plot.indexOf("Written by"));
				}
			}
			
			if (plot != null && !plot.equals("")) {
				plot = plot.trim();
				plot = plot.replaceAll("(more)$", "");
				dataModel.setPlot(plot);
			}
			
			lock.tryLock((long) 10, TimeUnit.SECONDS);
			
			lastDataModel = dataModel;
			
			return dataModel;
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return null;
    }
    

    void parseDataUsingXPath(ModelIMDbEntry dataModel, StringBuffer data, String uri) {

    	try {
    		SimpleUserAgentContext context = new SimpleUserAgentContext();
    		context.setScriptingEnabled(false);
    		context.setExternalCSSEnabled(false);
    		
			DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
    		//Document document = dbi.parse(new StringReader(data.toString()));
    		
    		//Document document = dbi.parse(new InputSourceImpl(in, url, "ISO-8859-1")) ;
    		Document document = dbi.parse(new InputSourceImpl(new StringReader(data.toString()), "http://akas.imdb.com/title/tt" + uri)) ;
    		
    		if (true) {
    			XPath xpath = XPathFactory.newInstance().newXPath();
    			String eval;
    			
    			//eval = "html//div[@class='info-offset']/table[@class='cast']";
    			//eval = "html//table[@class='cast']";

    			eval = "html//div[@class='info']|html//div[@class='info-offset']";
    			ArrayList<Element> res = getElements(xpath, document, eval);

    			for (Element e : res) {
    				parseXPathResult(dataModel, e);
    			}
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
  
    
    
    String parseXPathResult(ModelIMDbEntry dataModel, Element e) {

 	   Pattern p = Pattern.compile("\\s*(.+?):\\s*(.+)");
 	   Matcher m = p.matcher(e.getTextContent());

 	   if (m.find()) {

 		   int gCount = m.groupCount();
 		  
 		   if (gCount == 2) {

 			   String name = m.group(1);
 			   String info = m.group(2).trim();

 			   if (name.equals("Also Known As")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(2).getChildNodes();
 				   
 				   String aka = "";
 				   
 				   for (int i = 0; i < children.getLength(); i++) {
 					   Node n = children.item(i);
 					   //System.err.println(n.getLocalName() + ":" + n.getTextContent());
 					   String tmp = n.getTextContent();
 					   
 					   if (!tmp.trim().equals("") && !tmp.trim().equals("more")) {
 						   aka += tmp.trim() + "\n";
 					   }
 				   }
 				   aka = StringUtil.removeDoubleSpace(aka);
 				  //System.err.println("Also Known As:" + aka);
 				   dataModel.setAka(aka);
 			   }
 			   else if (name.equals("Sound Mix")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();
 				   
 				   String sound = getNodesContent(children);
 				   sound = sound.replaceAll("\\(", " (");
 				   //sound = sound.replaceAll("\\|", ", ");
 				   
 				  //System.err.println("sound:" + sound);
 				   dataModel.setWebSoundMix(sound);
 			   }
 			   else if (name.equals("Plot")) {
 				  
 				   int index = info.indexOf("full summary");
 				   if (index != -1) {
 					   info = info.substring(0, index);
 				   }
 				  dataModel.setPlot(info);
 			   }
 			   else if (name.equals("Certification")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();
 				   String certification = getNodesContent(children);
 				   certification = certification.replaceAll("\\(", " (");
 				   certification = StringUtil.removeDoubleSpace(certification);
 				   //System.err.println("Certification:" + certification);
 				   dataModel.setCertification(certification);
 			   }
 			   else if (name.equals("Language")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();
 				   
 				   String language = getNodesContent(children);
 				   //language = language.replaceAll("\\|", ", ");
 				  //System.err.println("Language:" + language);
 				   dataModel.setLanguage(language);
 			   }
 			   else if (name.startsWith("Country")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();

 				   String country = getNodesContent(children);
 				   //country = country.replaceAll("\\|", ", ");
 				  //System.err.println("Country:" + country);
 				   dataModel.setCountry(country);
 			   }
 			   else if (name.startsWith("User Rating")) {
 				  //System.err.println("Rating:" + info);
 				   // On the format 8.5/10
 				   info = info.substring(0, info.indexOf("/"));
 				   dataModel.setRating(info);
 			   }
 			   else if (name.startsWith("Writer") || name.startsWith("Creator") || name.startsWith("Director")) {

 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();

 				   String writer = "";
 				   //String writer = getNodesContent(children);

 				   for (int i = 0; i < children.getLength(); i++) {
 					   Node n = children.item(i);
 					   String tmp = n.getTextContent().trim();

 					   if (!tmp.equals("") && !tmp.equals("more") && !tmp.startsWith("(written by)") && !tmp.startsWith("(co-creator)")) {
 						   
 						   if (writer.length() > 0)
 							  writer += ", ";
 							   
 						   writer += removeNewLines(tmp);
 					   }
 				   }
 				   writer = writer.replaceAll("\\|", ", ");

 				  if (name.startsWith("Director"))
 					 dataModel.setDirectedBy(info);
 				  else
 					 dataModel.setWrittenBy(writer);
 				   
 			   }
 			   else if (name.startsWith("Creator")) {
 				   //System.err.println("Director:" + info);
 				   dataModel.setWrittenBy(info);
 			   }
 			   else if (name.startsWith("Director")) {
 				   //System.err.println("Director:" + info);
 				   dataModel.setDirectedBy(info);
 			   }
			   else if (name.startsWith("Runtime")) {
 				   info = StringUtil.removeDoubleSpace(info);
 				   String runtime =  info.replaceAll("\\s\\|", ",");
 				   //System.err.println("Runtime:" + runtime);
 				   dataModel.setWebRuntime(runtime);
 			   }
 			   else if (name.startsWith("Genre")) {
 				   String genre =  info.replaceAll("\\s\\|", ",");

 				   genre = StringUtil.removeAtEnd(genre, "more");
 				   //System.err.println("Genre:" + genre);
 				   genre = genre.trim();
 				   dataModel.setGenre(genre);
 			   }
 			   else if (name.startsWith("MPAA")) {
 				  //System.err.println("MPAA:" + info);
 				   dataModel.setMpaa(info);
 			   }
 			   else if (name.startsWith("Awards")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();

 				   String awards = getNodesContent(children);
 				   awards = awards.replaceAll("&", " & ");
 				   
 				   awards = StringUtil.removeAtEnd(awards, "more");
 				   //System.err.println("Awards:" + info);
 				   dataModel.setAwards(awards);
 			   }
 			   else if (name.startsWith("Color")) {
 				  //System.err.println("Color:" + info);
 				   dataModel.setColour(info);
 			   }
 			   else {
 				  // System.err.println("name:" + name);
 				   //System.err.println("info:" + info);
 			   }
 		   }
 	   }
 	   // Cast
 	   else {
 		   		   
 		   NodeList children = e.getChildNodes();
 		   children = children.item(1).getChildNodes();
 		   
 		   String cast = "";
 		   
 		   for (int i = 0; i < children.getLength(); i++) {
 			   Node n = children.item(i);
 			   String tmp = n.getTextContent();
 			   			   
 			   if (!tmp.trim().equals("")) {
 				
 				   String [] split = tmp.split("\\.\\.\\.");
 				   
 				   if (split.length != 2)
 					   continue;
 				   
 				   if (cast.length() > 0)
 					   cast += ", ";
 				   
 				   cast += split[0].trim() + " (" + split[1].trim() + ")";
 			   }
 		   }
 		   cast = cast.replaceAll("\\s\\|", ",");
 		   
 		  //System.err.println("Cast:" + cast);
 		   dataModel.setCast(cast);
 	   }
 	   return null;
    }
    
    
    ArrayList<Element> getElements(XPath xpath, Document document, String path) throws XPathExpressionException {

    	long time = System.currentTimeMillis();
    	NodeList nodeList = (NodeList) xpath.evaluate(path, document, XPathConstants.NODESET);
    	time = System.currentTimeMillis() - time;
    	//System.err.println("time:" + time);

    	ArrayList<Element> result = new ArrayList<Element>();

    	int length = nodeList.getLength();
    	for(int i = 0; i < length; i++) {
    		Element element = (Element) nodeList.item(i);
    		result.add(element);
    	}
    	return result;
    }


    String getNodesContent(NodeList nodes) {

    	String result = "";

    	for (int i = 0; i < nodes.getLength(); i++) {
    		Node n = nodes.item(i);
    		String tmp = n.getTextContent();

    		if (!tmp.trim().equals("")) {
    			result += removeNewLines(tmp.trim());
    		}
    	}
    	result = result.replaceAll("\\|", ", ");

    	return result;
    }
    
    String removeNewLines(String input) {
 	   return input.replaceAll("\\n", "");
    }
  
    
   
    
    public StringBuffer getEpisodesStream(ModelIMDbSearchHit modelSeason) {
	
		StringBuffer data = null;
		
		String urlType = "http://akas.imdb.com/title/tt"+ modelSeason.getUrlID() +"/episodes";
	
		try {

			URL url = new URL(urlType);

			try {
				HTTPResult res = httpUtil.readData(url);
	    		data = res.data;
			} catch (SocketTimeoutException s) {
				log.error("Exception: " + s.getMessage());
				data = null;
			}

			if (data == null) {
				return null;
			}

			//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/episodeStream.html", data);
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 

		return data;
	}
    
    
    
    public static ArrayList<ModelIMDbSearchHit> getEpisodes(ModelIMDbSearchHit modelSeason, StringBuffer stream) {

    	ArrayList<ModelIMDbSearchHit> hits = new ArrayList<ModelIMDbSearchHit>();
		
		try {

			String classContent = getDivClass("season-filter-all filter-season-" + modelSeason.getSeasonNumber(), stream);
			
			StringBuffer data = new StringBuffer(classContent);
			
			//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/episodeStream"+modelSeason.getSeasonNumber()+".html", data);
						
			Pattern p = Pattern.compile("<h3>(.+)\\s?<a href=\"/title/tt(\\d+)/\">(.+?)</a>");
			Matcher m = p.matcher(data);
							
			while (m.find()) {

				int gCount = m.groupCount();

				if (gCount == 3) {
					
					String episode = m.group(1);
					String key = m.group(2);
					String title = m.group(3);

					title = HttpUtil.decodeHTML(title);
					
					title = episode + title;
					
					hits.add(new ModelIMDbSearchHit(key, title, modelSeason.getSeasonNumber()));
				}
			}

		} catch (Exception e) {
			log.error("", e);
		}
	
		return hits;
	}
    
    
    
	public ArrayList<ModelIMDbSearchHit> getSeasons(ModelIMDbSearchHit modelSeries) {

		ArrayList<ModelIMDbSearchHit> hits = new ArrayList<ModelIMDbSearchHit>();
				
		String urlString = "http://akas.imdb.com/title/tt" + modelSeries.getUrlID();
				
		try {

			URL url = new URL(urlString);
			HTTPResult res = httpUtil.readData(url);
			StringBuffer data = res.data;

			//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("seasonsOutput.html", data);

			String title = "";

			int start = data.indexOf("Seasons:");
			
			/* No season....?. */
			if (start != -1) {
				
				int end = data.indexOf("</div>", start);
				
				String seasons = data.substring(start, end);
				
				int seasonCount = 1;
				String season = "episodes#season-";
				
				while (seasons.indexOf(season + seasonCount) != -1) {
					title = modelSeries.getTitle()+ " - Season "+ seasonCount;
					hits.add(new ModelIMDbSearchHit(modelSeries.getUrlID(), title, seasonCount));
					seasonCount++;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} 

		return hits;
	}
	
	
	public ModelIMDbEntry getEpisodeInfo(ModelIMDbSearchHit episode) throws Exception {
		
		ModelIMDbEntry model = grabInfo(episode.getUrlID());
		episode.setDataModel(model);
		
		return model;
	}
	

	public ArrayList<ModelIMDbSearchHit> getSeriesMatches(String title) {

		ArrayList<ModelIMDbSearchHit> all = null;
				
		try {
			all = getSimpleMatches(title);
			
			for (int i = 0; i < all.size(); i++) {

				ModelIMDbSearchHit imdb = all.get(i);

				if (!imdb.getTitle().startsWith("\"")) {
					all.remove(i);
					i--;
				}
			}
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
		return all;
	}

	/**
	 * Returns simple matches list...
	 * @throws UnsupportedEncodingException 
	 **/
	public ArrayList<ModelIMDbSearchHit> getSimpleMatches(String title) throws UnsupportedEncodingException {
					
		//System.out.println("UTF-8:" + java.net.URLEncoder.encode(title, "UTF-8"));
		//System.out.println("US-ASCII:" + java.net.URLEncoder.encode(title, "US-ASCII"));
		//System.out.println("ISO-8859-1:" + java.net.URLEncoder.encode(title, "ISO-8859-1"));
		
		log.debug("getSimpleMatches:" + title);
		
		return getMatches("http://akas.imdb.com/find?s=tt&q="+ java.net.URLEncoder.encode(title, "ISO-8859-1"));
	}

	
    private ArrayList<ModelIMDbSearchHit> getMatches(String strUrl) {

    	try {
    		    		
    		URL url = new URL(strUrl);

    		log.debug("getMatches:" + url);
    		
    		HTTPResult res = httpUtil.readData(url);
			StringBuffer data = res.data;

    		if (data == null) {
    			log.warn("Failed to retrieve data from :" + url);
    			return new ArrayList<ModelIMDbSearchHit>();
    		}

    		return getMatches(data);

    	} catch (Exception e) {
    		log.warn("Exception:" + e.getMessage(), e);
    	}
    	return null;
    }

    protected ArrayList<ModelIMDbSearchHit> getMatches(StringBuffer data) {

    	ArrayList<ModelIMDbSearchHit> listModel = new ArrayList<ModelIMDbSearchHit>();

    	try {

    		//new java.io.File("HTML-debug").mkdir();
    		//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/imdb-search.html", data);

    		int start = 0;
			String key = "";
			String movieTitle = "", aka = "";
			int titleSTart, titleEnd;
			int movieCount = 0;
			
			/* If there's only one movie for that title it goes directly to that site...  */
			if (!data.substring(data.indexOf("<title>")+7, data.indexOf("<title>")+11).equals("IMDb")) {
			
				/* Gets the title... */
				titleSTart = data.indexOf("<title>", start)+7;
				titleEnd = data.indexOf("</title>", titleSTart);
				movieTitle = HttpUtil.decodeHTML(data.substring(titleSTart, titleEnd));
				
				if ((start=data.indexOf("title/tt",start) + 8) != 7) {
					key = HttpUtil.decodeHTML(data.substring(start, start + 7));
				}
				
				aka = getDecodedClassInfo("Also Known As:", data);
				listModel.add(new ModelIMDbSearchHit(key, movieTitle, aka));
				
				return listModel;
			}
				
			// Insert newline before each href, as dot in regex will not match newline
			int index = 0;
			while ((index = data.indexOf("<a href", index)) != -1) {
				data.insert(index, "\n");
				index += 2;
			}
			
			int [] movieHitCategoryIndex = new int[4];
			boolean empty = true;
			
			int startIndex = -1;
			
			for (int u = 0; u < movieHitCategory.length; u++) {
				movieHitCategoryIndex[u] = data.indexOf(movieHitCategory[u]);
				
				if (movieHitCategoryIndex[u] != -1)  {
					empty = false;
					
					if (startIndex == -1) {
						startIndex = movieHitCategoryIndex[u];
						data.delete(0, startIndex); // remove the top html 
						movieHitCategoryIndex[u] = data.indexOf(movieHitCategory[u]);
					}
				}
			}
			
			// NO results, returning empty list
			if (empty) {
				return listModel;
			}
			
			// <a href="/title/tt0496424/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0496424/';">&#34;30 Rock&#34;</a> (2006) <small>(TV series)</small>     <div style="font-size: small">&#160;&#45;&#160;Season 3, Episode 11: 
			// <a href="/title/tt0074853/">The Man in the Iron Mask</a> (1977) (TV)</td></tr>
			// <a href="/title/tt0120744/">The Man in the Iron Mask</a> (1998/I)</td></tr>
			// <a href="/title/tt0103064/">Terminator 2: Judgment Day</a> (1991)<br>&#160;aka <em>"Terminator 2 - Le jugement dernier"</em> - France<br>&#160;aka <em>"T2 - Terminator 2: Judgment Day"</em></td></tr>
			
			// <a href="/title/tt0822832/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0822832/';">Marley &#x26; Me</a> (2008)     </td></tr></table> 
			
			
			// should match strings like the above
			
			Pattern p = Pattern.compile("<a\\shref=\"/title/tt(\\d{5,})/\".*?>(.+?)</a>.+?\\((\\d+(/I*)?)\\).*?(;aka\\s<em>.+?</em>)*?(?:</td></tr>|\\n)"); // last group matches series that do not end with </td></tr>
			Matcher m = p.matcher(data);
			
			while (m.find()) {
			
				//int gCount = m.groupCount();
				
				key = m.group(1);
				String title = m.group(2);
				String year = m.group(3);
				
				title = HttpUtil.decodeHTML(title);
				
				if (title.equals(""))
					continue;
				
				//Video game
				if (m.group(0).indexOf("VG") != -1) {
					continue;	
				}
				
				title += " (" + year + ")"; 
				
				// Aka
				aka = grabAkaTitlesFromSearchHit(m.group(0));
				
				int matchIndex = m.start();
				
				String category = null;
				
				for (int i = 0; i < movieHitCategoryIndex.length; i++) {
					
					if (movieHitCategoryIndex[i] != -1 && matchIndex > movieHitCategoryIndex[i])
						category = movieHitCategory[i];
				}
				
				listModel.add(new ModelIMDbSearchHit(key, title, aka, category));
				
				movieCount++;
			}
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	
		/* Returns the model... */
		return listModel;
    }
    
    
    
    public static String grabAkaTitlesFromSearchHit(String substring) {
		
    	String aka = " ";
    	String akas [] = substring.split("&#160;aka");
    	
    	// skips the first index which is the original title
    	for (int i = 1; i < akas.length; i++) {
    		
    		if (!aka.equals(" "))
    			aka += "\r\n ";
    		
    		aka += HttpUtil.decodeHTML(akas[i]);
    	}
    	    	
		return aka;
    }
  
   // <div.*?class=\"info.+?<.+?>(.*?)(\(?:.*?\))?<.+?>(.+?)</div>
   // <div.*?class="info">.*?<.+?>(.+?)</.+?>.*?<
    protected HashMap<String, String> decodeClassInfo(StringBuffer data) {
    		
    	HashMap<String, String> classInfo = new HashMap<String, String>();
    	
    	//Pattern p = Pattern.compile("<div.*?class=\"info\">.*?<.+?>(.+?)</.+?>.*?<div\\sclass=\"info-content\">(.+?)</div>", Pattern.DOTALL);
    	Pattern p = Pattern.compile("<div.*?class=\"info\">.*?<.+?>(.+?)(?:\\(.*?)?</.+?>.*?<div\\sclass=\"info-content\">(.+?)</div>", Pattern.DOTALL);
		Matcher m = p.matcher(data);
		 		
		while (m.find()) {
		
			//int gCount = m.groupCount();
			
			String className = m.group(1);
			String info = m.group(2);
			
			//System.err.println("className:" + className + "|");
			//System.err.println("info:" + info);
			
			if (className != null && info != null) {
				className = className.trim();
				if (!className.endsWith(":"))
					className += ":";
				
				//System.err.println("put=" + className + "|");
				
				if (className.equals("Writers:")) {
					//System.err.println("Writers put info:" + info);
				}
				classInfo.put(className, info);
			}
		}
		
		return classInfo;
    }
    
    
    
    /**
     * Grabs the content of a class info containing the classname
     **/
    
    protected static String getClassInfo(StringBuffer data, String className) {
    	String tmp = "";

    	int start = 0;
    	int end = 0;

    	while ((start = data.indexOf("<div class=\"info", end)) != -1 && 
    			(end = data.indexOf("</div>", start)) != -1) {

    		tmp = data.substring(start, end);	

    		if (tmp.indexOf(className) != -1) {
    			start = tmp.indexOf(className) + className.length();
    			tmp = tmp.substring(start, tmp.length());	
    			tmp = tmp.trim();
    			break;
    		}
    		tmp = "";
    	}
    	return tmp;
    }
    
    
    protected static String getDecodedClassInfo(String className, StringBuffer data) throws Exception {
    	String tmp = getClassInfo(data, className);
    	return getDecodedClassInfo(className, tmp);
    }
    
    /**
     * Grabs the content of a class info containing the classname
     * and cleans it up by removing html and paranthesis.
     * @throws Exception 
     **/
    protected static String getDecodedClassInfo(String className, String classInfo) throws Exception {
    	String decoded = "";
    	String tmp = classInfo;

    	if (tmp == null)
    		throw new Exception("classInfo cannot be null!");
    	
    	if (className == null)
    		className = "";
    	
    	try {
    		int end = 0;
    		
    		//tmp = getClassInfo(data, className);
    		    		    		
    		end = tmp.indexOf("<a class=\"tn15more");
    			
    		// Link to "more" will be removed
    		if (end != -1) {
    			tmp = tmp.substring(0, end);
    		} 
    				
			if (className.equals("Also Known As:")) {
				decoded = decodeAka(tmp);
			}
			else if (className.equals("class=\"cast\">")) {
				decoded = decodeCast(tmp).trim();
			}
			else {
				decoded = HttpUtil.decodeHTML(tmp);
				decoded = decoded.replaceAll("\\|", ",");
				decoded = decoded.replaceAll("\r\n|\n|\r", " ");
				
				// Removes all space before comma
				while (decoded.indexOf(" ,") != -1) {
					decoded = decoded.replaceAll("\\s,", ",");
				}
				
				decoded = StringUtil.removeDoubleSpace(decoded);
				decoded = decoded.trim();
			}
    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage(), e);
    	} 
    	/* Returns the decoded string... */
    	return decoded;
    }
    
    
    
    
    /**
     * Returns the name of the link; <a href="">name</a>
     **/
    protected static ArrayList<String> getLinkContentName(String toDecode) {
    	ArrayList<String> decoded = new ArrayList<String>();
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
			log.error("Exception:" + e.getMessage(), e);
		} 
		/* Returns the decoded string... */
		return decoded;
    }
    
    
    /**
     * Returns the aka titles.
     **/
    protected static String decodeAka(String toDecode) {
		String decoded = " ";
		
		try {
			String [] akaTitles = toDecode.split("<br>");
		
			for (int i = 0; i < akaTitles.length; i++) {
				
				if (!decoded.equals(" "))
					decoded += System.getProperty("line.separator");
					
				decoded += HttpUtil.decodeHTML(akaTitles[i].trim());
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 
		/* Returns the decoded string... */
		return decoded;
    }
    
    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static String decodeCast(String toDecode) {
    	
    	StringBuffer decoded = new StringBuffer();
    	
    	try {
    		try {
    			String [] castSplit = toDecode.split("<td class=\"hs\">");
    		 	Pattern p = Pattern.compile("<a\\shref=\"/name/nm\\d+/.+?castlist.+?>(.+?)</a>.+?\\.\\.\\..+?<td\\sclass=\"char\">(?:<a\\shref=\"/character/ch\\d+/\">)?(.+?)</td>");
    			    			
    			for (int i = 0; i < castSplit.length; i++) {
    				
    				//System.err.println("castSplit[i]:" + castSplit[i]);
    				
    				String nmClass = getCustomElementClass("td", "nm", new StringBuffer(castSplit[i]));
    				String charClass = getCustomElementClass("td", "char", new StringBuffer(castSplit[i]));
    				
    				if (nmClass == null || charClass == null)
    					continue;
    					
    				ArrayList<String> nm = getLinkContentName(nmClass);
    				ArrayList<String> charResult = getLinkContentName(charClass);
    				
    				if (nm.size() != 1 || charResult.size() != 1) {
    					continue;
    				}
    				
    				String name = nm.get(0);
					String character = charResult.get(0);
					decoded.append(name);
					decoded.append(" (" + character + "), ");
					
    				/*
    				Matcher m = p.matcher(castSplit[i]);
    								
    				if (m.find()) {

    					int gCount = m.groupCount();
	
    					if (gCount == 2) {
    							
    						String name = m.group(1);
    						String character = m.group(2);
    						System.err.println("name:" + name);
    						decoded.append(name);
        					decoded.append(" (" + character + "), ");
    					}
    				}
    				*/
    			}
    			
    			// Removes spaces at end
    			while (decoded.length() > 0 && decoded.charAt(decoded.length() - 1) == ' ')
    				decoded = decoded.deleteCharAt(decoded.length() - 1);

    			// Replace comma at the end
    			if (decoded.length() > 0 && decoded.charAt(decoded.length() - 1) == ',') 
    				decoded = decoded.replace(decoded.length() - 1, decoded.length(), "");

    		} catch (Exception e) {
    			log.error("Exception:" + e.getMessage(), e);
    		} 
    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage(), e);
    	}
    	/* Returns the decoded string... */
    	return decoded.toString();
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
    
    /**
     * Get the content of the <div class="XX">
     * @param classname
     * @param buffer
     * @return
     */
    public static String getDivClass(String classname, StringBuffer buffer) {
    
    	int safety = 10000;
    	
    	String searchStr = "<div class=\""+ classname + "\">";
    	
    	int start = buffer.indexOf(searchStr);
    	
    	if (start == -1)
    		return null;
    	
    	start += 3;
    	
    	int end = start;
    	
    	int div_count = 1;
    	
    	while (div_count > 0) {
    	
    		if (safety-- == 0)
    			break;
    		
    		int i = buffer.indexOf("div", end);
    		
    		if (i != -1) {
    			
    			if (buffer.charAt(i-1) == '<')
    				div_count++;
    			else if (buffer.charAt(i-1) == '/')
    				div_count--;

    			end = i + 3;
    		}
    		else
    			break;
    	}
     	
    	if (start > 0 && end < buffer.length() && start < end)
    		return buffer.substring(start, end);
    	else
    		return null;
    }
    
        
    
  
    public static String getCustomElementClass(String element, String classname, StringBuffer buffer) {
        	
    	String searchStr = "<" +element+ " class=\""+ classname + "\">";
    	String elementEnd = "</" +element+ ">";
    	
    	int start = buffer.indexOf(searchStr);
    	
    	if (start == -1)
    		return null;
    	
    	int end = buffer.indexOf(elementEnd, start);
     	
    	if (end == -1)
    		return null;
    	
    	if (start > 0 && end < buffer.length() && start < end)
    		return buffer.substring(start, end);
    	else
    		return null;
    }
    
    
    public ModelIMDbEntry getLastDataModel() {
    	return lastDataModel;
    }
    	
    /**
     * Gets the key.
     **/
    public String getUrlID() {
		return lastDataModel == null ? null : lastDataModel.getUrlID();
    }

    /**
     * Gets the date.
     **/
    public String getDate() {
		return lastDataModel == null ? null : lastDataModel.getDate();
    }
  
       
    
    /**
     * Gets the title.
     **/
    public String getIMDbTitle() {
		return lastDataModel == null ? null : lastDataModel.getTitle();
    }
  
 
    /**
     * Returns the title where 'The', 'A' and 'An' are moved to the end of the title, If the settings are set.
     * @return
     */
    public String getCorrectedTitle(String title) {
    	
    	if (settings != null) {
    		
    		if (settings.getAutoMoveThe() && title.startsWith("The ")) {
    			title = title.substring(title.indexOf(" ")+1, title.length())+ ", The";
    		}
    		else if (settings.getAutoMoveAnAndA() && (title.startsWith("A ") || title.startsWith("An "))) {
    			title = title.substring(title.indexOf(" ")+1, title.length())+ ", "+ title.substring(0, title.indexOf(" "));
    		} 
    	}
    	return title;
    }
    
    /**
     * Gets the directed by.
     **/
    public String getDirectedBy() {
		return lastDataModel == null ? null : lastDataModel.getDirectedBy();
    }
  
    /**
     * Gets the written by.
     **/
    public String getWrittenBy() {
		return lastDataModel == null ? null : lastDataModel.getWrittenBy();
    }
  
    /**
     * Gets the genre.
     **/
    public String getGenre() {
		return lastDataModel == null ? null : lastDataModel.getGenre();
    }
  
    /**
     * Gets the rating.
     **/
    public String getRating() {
		return lastDataModel == null ? null : lastDataModel.getRating();
    }
  
    /**
     * Gets the colour.
     **/
    public String getColour() {
		return lastDataModel == null ? null : lastDataModel.getColour();
    }

    /**
     * Gets the country.
     **/
    public String getCountry() {
		return lastDataModel == null ? null : lastDataModel.getCountry();
    }

    /**
     * Gets the language.
     **/
    public String getLanguage() {
		return lastDataModel == null ? null : lastDataModel.getLanguage();
    }
    
    /**
     * Gets the plot.
     **/
    public String getPlot() {
		return lastDataModel == null ? null : lastDataModel.getPlot();
    }
  
    /**
     * Gets the cast.
     **/
    public String getCast() {
		return lastDataModel == null ? null : lastDataModel.getCast();
    }
    
    /**
     * Gets the aka.
     **/
    public String getAka() {
		return lastDataModel == null ? null : lastDataModel.getAka();
    }
    
    /**
     * Gets the mpaa.
     **/
    public String getMpaa() {
		return lastDataModel == null ? null : lastDataModel.getMpaa();
    }
    
    /**
     * Gets the Sound Mix.
     **/
    public String getSoundMix() {
		return lastDataModel == null ? null : lastDataModel.getWebSoundMix();
    }

    /**
     * Gets the Runtime.
     **/
    public String getRuntime() {
		return lastDataModel == null ? null : lastDataModel.getWebRuntime();
    }
    
    /**
     * Gets the Certification.
     **/
    public String getCertification() {
		return lastDataModel == null ? null : lastDataModel.getCertification();
    }

    /**
     * Gets the Awards.
     **/
    public String getAwards() {
		return lastDataModel == null ? null : lastDataModel.getAwards();
    }
    
    
    /**
     * Gets the cover url.
     **/
    public String getCoverName() {
		return lastDataModel == null ? null : lastDataModel.getCoverName();
    }
    
    /**
     * Gets the cover url.
     **/
    public String getCoverURL() {
		return lastDataModel == null ? null : lastDataModel.getCoverURL();
    }
  
    public byte [] getCover() {
    	return lastDataModel == null ? null : lastDataModel.getCoverData();
    }
    
    public byte [] getBigCover() {
    	return lastDataModel == null ? null : lastDataModel.getBigCoverData();
    }
    
    
    
    /**
     * Gets the cover.
     **/
    private boolean retrieveSmallCover(ModelIMDbEntry dataModel) {
      
		byte[] coverData = null;
		
		try {
			if (dataModel.getCoverURL() != null && !dataModel.getCoverURL().equals("")) {
				coverData = httpUtil.readDataToByteArray(new URL(dataModel.getCoverURL()));
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 
	
		dataModel.setCoverData(coverData);
		/* Returns the data... */
		return coverData != null;
    }


    private boolean retrieveCover(ModelIMDbEntry dataModel) {
    	
    	if (!retrieveBiggerCover(dataModel))
    		retrieveSmallCover(dataModel);
    	else {
    		dataModel.setCoverData(dataModel.getBigCoverData());
    	}
    	return true;    	
    }
    
    public boolean retrieveBiggerCover(ModelIMDbEntry dataModel) {
   	
    	URL url;

    	byte [] coverData = null;
    	
    	try {
    		url = new URL("http://akas.imdb.com/media/" + dataModel.bigCoverUrlId);
    		
    		//System.err.println("url:" + url);
    		
    		HTTPResult res = httpUtil.readData(url);
    		StringBuffer data = res.data;

    		int imgIndex = data.indexOf("<img oncontextmenu");

    		if (imgIndex != -1) {

    			String tmp = data.substring(imgIndex, data.indexOf(">", imgIndex));

    			//System.err.println("tmp:" + tmp);
    			
    			//src="http://ia.media-imdb.com/images/M/MV5BMTI4ODg5MjkwMl5BMl5BanBnXkFtZTcwNTkzMjYyMQ@@._V1._SX307_SY400_.jpg">

    			Pattern p = Pattern.compile("src=\"(.+)\"");

    			Matcher m = p.matcher(tmp);

    			if (m.find()) {

    				//String g = m.group();

    				//System.err.println("BC g:" + m.group(0));
    				//System.err.println("BC g1:" + m.group(1));

    				coverData = httpUtil.readDataToByteArray(new URL(m.group(1)));
    				
    				//dataModel.bigCoverUrlId = m.group(1);
    			}

    		}

    	} catch (SocketTimeoutException s) {
    		log.error("Exception: " + s.getMessage());
    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage(), e);
    	}

    	dataModel.setBigCoverData(coverData);
    	
    	//http://ia.media-imdb.com/images/M/MV5BMTI4ODg5MjkwMl5BMl5BanBnXkFtZTcwNTkzMjYyMQ@@._V1._SX307_SY400_.jpg

    	return coverData != null;
    }

    /**
     * Returns true if the last cover reading went ok..
     **/
    public boolean getCoverOK() {
    	return lastDataModel == null ? false : lastDataModel.hasCover();
    }

    public boolean getBigCoverOK() {
    	return lastDataModel == null ? false : lastDataModel.hasBigCover();
    }

}
