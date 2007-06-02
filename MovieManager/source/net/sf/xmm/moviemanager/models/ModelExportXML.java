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

import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.xml.*;

import java.util.*;


public class ModelExportXML implements XMLClassDescriptor {

    public ArrayList movies;
    public ArrayList series;
    
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
        System.err.println("getAttributeDescriptors()");
        // TODO Auto-generated method stub
        return null;
    }

    public XMLFieldDescriptor getContentDescriptor() {
        System.err.println("getContentDescriptor()");
        // TODO Auto-generated method stub
        return null;
    }

    public XMLFieldDescriptor[] getElementDescriptors() {
        System.err.println("getElementDescriptors()");
        // TODO Auto-generated method stub
        return null;
    }

    public XMLFieldDescriptor getFieldDescriptor(String arg0, String arg1, NodeType arg2) {
        System.err.println("getFieldDescriptor");
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameSpacePrefix() {
        System.err.println("getNameSpacePrefix()");
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameSpaceURI() {
        System.err.println("getNameSpaceURI()");
        // TODO Auto-generated method stub
        return null;
    }

    public TypeValidator getValidator() {
        System.err.println("getValidator()");
        // TODO Auto-generated method stub
        return null;
    }

    public String getXMLName() {
        System.err.println("getXMLName()");
        // TODO Auto-generated method stub
        return null;
    }

    public boolean canAccept(String arg0, String arg1, Object arg2) {
        System.err.println("canAccept");
        // TODO Auto-generated method stub
        return false;
    }

    public Class getJavaClass() {
        System.err.println("getJavaClass()");
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDescriptor[] getFields() {
        System.err.println("getFields()");
        // TODO Auto-generated method stub
        return null;
    }

    public ClassDescriptor getExtends() {
        System.err.println("getExtends()");
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDescriptor getIdentity() {
        System.err.println("getIdentity()");
        // TODO Auto-generated method stub
        return null;
    }

    public AccessMode getAccessMode() {
        System.err.println("getAccessMode()");
        // TODO Auto-generated method stub
        return null;
    }
}
