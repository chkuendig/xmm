/**
 * @(#)ModelEntry.java 29.01.06 (dd.mm.yy)
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

import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.xml.NodeType;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.UnmarshalState;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLClassDescriptor;
import org.exolab.castor.xml.XMLFieldDescriptor;


public class ModelExportXML implements XMLClassDescriptor {

	Logger log = Logger.getLogger(getClass());
	
	private String movieManagerVersion = MovieManager.getConfig().sysSettings.getVersion();
	
    private ArrayList movies;
    private ArrayList series;
    
    public ModelExportXML() {
    	movies = new ArrayList();
        series = new ArrayList();
    }
    
    public ModelExportXML(int size) {
    	movies = new ArrayList(size);
        series = new ArrayList(size);
    }
    
    public String getMovieManagerVersion() {
    	return movieManagerVersion;
    }
    
    public ArrayList getCombindedList() {
	
        ArrayList combinded = new ArrayList(movies.size() + series.size());
        
        combinded.addAll(movies);
        combinded.addAll(series);
        
        return combinded;
    }
    
    // Included all movies and episodes
    public int getAllEntriesCount() {
    	
    	int count = movies.size();
    	
    	for (int i = 0; i < series.size(); i++) {
    		count += ((ModelSeries) series.get(i)).getEpisodes().size();
    	}
    	return count;
    }
    
    public ArrayList getMovies() {
     return movies;   
    }
    
    public ArrayList getSeries() {
    	return series;   
    }

    public void setMovies(ArrayList movies) {
    	this.movies = movies;   
    }

    public void setSeries(ArrayList series) {
    	this.series = series;   
    }

    public void addModelMovie(ModelMovie movie) {
    	movies.add(movie);
    }

    
    public void addModelSerie(ModelSeries serie) {
        series.add(serie);
    }
    
   public void checkDescriptorForCorrectOrderWithinSequence(XMLFieldDescriptor elementDescriptor) throws ValidationException {
   // Does nothing
    }
    
    public void checkDescriptorForCorrectOrderWithinSequence(XMLFieldDescriptor edesc, UnmarshalState state, String s) throws ValidationException {
    	   // Does nothing
     }

    public XMLFieldDescriptor[] getAttributeDescriptors() {
        log.debug("getAttributeDescriptors()");
        // TODO Auto-generated method stub
        return null;
    }

    public XMLFieldDescriptor getContentDescriptor() {
        log.debug("getContentDescriptor()");
        // TODO Auto-generated method stub
        return null;
    }

    public XMLFieldDescriptor[] getElementDescriptors() {
        log.debug("getElementDescriptors()");
        // TODO Auto-generated method stub
        return null;
    }

    public XMLFieldDescriptor getFieldDescriptor(String arg0, String arg1, NodeType arg2) {
        log.debug("getFieldDescriptor");
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameSpacePrefix() {
        log.debug("getNameSpacePrefix()");
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameSpaceURI() {
        log.debug("getNameSpaceURI()");
        // TODO Auto-generated method stub
        return null;
    }

    public TypeValidator getValidator() {
        log.debug("getValidator()");
        // TODO Auto-generated method stub
        return null;
    }

    public String getXMLName() {
        log.debug("getXMLName()");
        // TODO Auto-generated method stub
        return null;
    }

    public boolean canAccept(String arg0, String arg1, Object arg2) {
		log.debug("canAccept");
        // TODO Auto-generated method stub
        return false;
    }

    public Class getJavaClass() {
        log.debug("getJavaClass()");
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDescriptor[] getFields() {
        log.debug("getFields()");
        // TODO Auto-generated method stub
        return null;
    }

    public ClassDescriptor getExtends() {
        log.debug("getExtends()");
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDescriptor getIdentity() {
        log.debug("getIdentity()");
        // TODO Auto-generated method stub
        return null;
    }

    public AccessMode getAccessMode() {
        log.debug("getAccessMode()");
        // TODO Auto-generated method stub
        return null;
    }

	public boolean isChoice() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/*
	 * Not currently used
	 */
	 void validateModel(ModelMovie movie) {
	    	
	    	removeInvalidXMLCharacters(movie.getTitle());
	    	removeInvalidXMLCharacters(movie.getAka());
	    	removeInvalidXMLCharacters(movie.getAwards());
	    	removeInvalidXMLCharacters(movie.getCast());
	    	removeInvalidXMLCharacters(movie.getCertification());
	    	removeInvalidXMLCharacters(movie.getColour());
	    	removeInvalidXMLCharacters(movie.getCompleteUrl());
	    	removeInvalidXMLCharacters(movie.getCountry());
	    	removeInvalidXMLCharacters(movie.getCover());
	    	removeInvalidXMLCharacters(movie.getDate());
	    	removeInvalidXMLCharacters(movie.getDirectedBy());
	    	removeInvalidXMLCharacters(movie.getGenre());
	    	removeInvalidXMLCharacters(movie.getLanguage());
	    	removeInvalidXMLCharacters(movie.getMpaa());
	    	removeInvalidXMLCharacters(movie.getNotes());
	    	removeInvalidXMLCharacters(movie.getPlot());
	    	removeInvalidXMLCharacters(movie.getRating());
	    	removeInvalidXMLCharacters(movie.getSortCategory());
	    	removeInvalidXMLCharacters(movie.getSortDate());
	    	removeInvalidXMLCharacters(movie.getUrlKey());
	    	removeInvalidXMLCharacters(movie.getWebRuntime());
	    	removeInvalidXMLCharacters(movie.getWebSoundMix());
	    	removeInvalidXMLCharacters(movie.getWrittenBy());
	    	
	    	ModelAdditionalInfo add = movie.getAdditionalInfo();
	        	
	    	removeInvalidXMLCharacters(add.getAdditionalInfoString());
	    }
	 
	 
	
	/**
	 * This method ensures that the output String has only valid XML unicode characters as specified by the
	 * XML 1.0 standard. For reference, please see the
	 * standard. This method will return an empty String if the input is null or empty.
	 *
	 * @author Donoiu Cristian, GPL
	 * @param  The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String removeInvalidXMLCharacters(String s) {

		StringBuilder out = new StringBuilder();                // Used to hold the output.
		int codePoint;                                          // Used to reference the current character.

		//String ss = "\ud801\udc00";                           // This is actualy one unicode character, represented by two code units!!!.
		
		//System.out.println(ss.codePointCount(0, ss.length()));// See: 1

		int i=0;

		while(i<s.length()) {

			//System.out.println("i=" + i);

			codePoint = s.codePointAt(i);                       // This is the unicode code of the character.

			if ((codePoint == 0x9) ||          				    // Consider testing larger ranges first to improve speed. 

					(codePoint == 0xA) ||

					(codePoint == 0xD) ||

					((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||

					((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||

					((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {

				out.append(Character.toChars(codePoint));

			}
			else {
				System.err.println("Not including:" + codePoint + " :");
				System.err.println("input:" + s);
			}

			i+= Character.charCount(codePoint);                 // Increment with the number of code units(java chars) needed to represent a Unicode char.  

		}
		return out.toString();
	} 
}
