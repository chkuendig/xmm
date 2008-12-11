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

	static Logger log = Logger.getRootLogger();
	
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
}
