/**
 * @(#)MovieManagerCommandSaveChangedNotes.java 1.0 26.09.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.*;
import javax.swing.tree.*;
import java.util.*;

import java.io.*;
import java.net.*;

import org.exolab.castor.mapping.*;

import org.exolab.castor.xml.Marshaller;

import java.io.OutputStreamWriter;




public class MovieManagerCommandExportToXML {
    
    /**
     * Executes the command.
     **/
    public static void execute(String outputFile) {
	
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
	ModelEntry model;
	DefaultMutableTreeNode node;
	
	Enumeration enumeration = root.children();
	
	try {
	    
	    Mapping mapping = new Mapping(new ModelMovie().getClass().getClassLoader());
	    
	    URL mappingFile = FileUtil.getFileURL("mapping.xml");
	    
	    ModelExportXML exportXML = new ModelExportXML(root.getChildCount());
        
            // 1. Load the mapping information from the file
            mapping.loadMapping(mappingFile);
   
            String encoding = "UTF-8";
             
            // 4. marshal the data with the total price back and print the XML in the console 
            Marshaller marshaller = new Marshaller(new OutputStreamWriter(new FileOutputStream(outputFile), encoding));
            marshaller.setEncoding(encoding);
            //marshaller.setMarshalAsDocument(true);
            marshaller.setMapping(mapping);
	
	    while (enumeration.hasMoreElements()) {
		
	        node = ((DefaultMutableTreeNode) enumeration.nextElement());
	        model = (ModelEntry) node.getUserObject();
	    
	        if (!model.getHasGeneralInfoData()) {
	        	model.updateGeneralInfoData();
	        }
	        
	        if (!model.getHasAdditionalInfoData()) {
	        	model.updateAdditionalInfoData();
	        }
	       	        		
	        /* Has no children */
	        if (node.isLeaf()) {
	        	exportXML.addModelMovie((ModelMovie) model);
	        }
	        else {
	            ModelSeries serie = new ModelSeries((ModelMovie) model);
            
	            Enumeration children = node.children();
            
	            while (children.hasMoreElements()) {
	                serie.addEpisode((ModelEpisode) ((DefaultMutableTreeNode) children.nextElement()).getUserObject());
	            }
                
                exportXML.addModelSerie(serie);
	        }
        }
        
        marshaller.marshal(exportXML);
        	    
	} catch (Exception e) {
	    System.out.println(e);
	    return;
	}
    }
    
}
