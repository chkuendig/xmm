/**
 * @(#)DatabaseImporter.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.DialogImportTable.FieldModel;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMovies;
import net.sf.xmm.moviemanager.database.DatabaseExtreme;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.*;

import org.apache.log4j.Logger;
import org.castor.mapping.MappingUnmarshallListener;
import org.castor.mapping.MappingUnmarshaller;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.util.DTDResolver;
import org.exolab.castor.xml.UnmarshalListener;
import org.exolab.castor.xml.Unmarshaller;

import java.awt.Dialog;
import java.io.*;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;



public class DatabaseImporter {
    
    static Logger log = Logger.getRootLogger();
    
    private int lengthOfTask = 0;
    private int current = -1;
    private boolean done = false;
    private boolean canceled = false;
    private ArrayList transferred;
    private ModelImportSettings importSettings;
    private Dialog parent;
    
    public DatabaseImporter(Dialog parent, ModelImportSettings importSettings) {
        this.importSettings = importSettings;
        this.parent = parent;
    }
    
    public void go() {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                current = -1;
                done = false;
                canceled = false;
                
                Importer importer = null;
                
                try {
                	importer =  new Importer(importSettings);
                }
                catch (Exception e) {
                    log.warn("Exception:" + e.getMessage(), e);   
                }
                return importer;
            }
        };
        worker.start();
    }
    
    public int getLengthOfTask() {
        return lengthOfTask;
    }
    
   
    public int getCurrent() {
        return current;
    }
    
    /*Stops the importing process*/
    public void stop() {
        canceled = true;
    }
    
    public boolean isDone() {
        return done;
    }
    
    /* Returns the array transferred which contains all the finished database entries */
    public ArrayList getTransferred() {
        return transferred;
    }
    
    
    
    /**
     * The actual database import task.
     * This runs in a SwingWorker thread.
     */
    class Importer extends MovieManagerCommandAddMultipleMovies {
        
        int multiAddSelectOption;
        int importMode;
        boolean overwriteWithImdbInfo;
         
        String addToThisList;
        String filePath;
        boolean originalLanguage = true;
        String coverPath;
        
        ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
        
        ArrayList movielist = null;
        
        int extraInfoFieldsCount;
        
        Importer(ModelImportSettings importSettings) {
            
        	multiAddSelectOption = importSettings.multiAddSelectOption;
            importMode = importSettings.importMode;
            overwriteWithImdbInfo = importSettings.overwriteWithImdbInfo;
            addToThisList = importSettings.addToThisList;
            filePath = importSettings.filePath;
            originalLanguage = importSettings.extremeOriginalLanguage;
            coverPath = importSettings.coverPath;
            
            /* Setting the priority of the thread to 4 to give the GUI room to update more often */
            Thread.currentThread().setPriority(3);
            
            if (filePath != null) {
                
                try {
                	movielist = getMovieList();
                      
                    if (movielist == null)
                        throw new Exception("An error occured while reading file");
                    
                } catch (Exception e) {
                	 JDialog alert = new DialogAlert(parent, "Error", e.getMessage());
                    GUIUtil.showAndWait(alert, true);
                    return;
                }
                 
                transferred = new ArrayList(lengthOfTask);
                
                String title = "";
                 
                extraInfoFieldsCount = MovieManager.getIt().getDatabase().getExtraInfoFieldNames().size();
                
                for (int i = 0; i < movielist.size(); i++) {
                    
                    cancel = false;
                    
                    title = getNextTitle(i);
                    
                    if (title != null && !title.equals("")) {
                        
                        /* First resetting the info already present */
                        
                        modelMovieInfo.clearModel();
                        
                        if (multiAddSelectOption != -1) {
                            
                            executeCommandGetIMDBInfoMultiMovies(title, title, multiAddSelectOption);
                            
                            /* If the user cancels the imdb */
                            if (dropImdbInfo)
                                modelMovieInfo.clearModel();
                            
                            dropImdbInfo = false;
                        }
                        
                        // cancellAll and cancel can be set from DlalogIMDB run from  executeCommandGetIMDBInfoMultiMovie
                        if (cancelAll || canceled)
                            break;
                        
                        if (!cancel) {
                            
                            int ret = -1; 
                            
                            /* excel */
                            if (importMode == ModelImportSettings.IMPORT_EXCEL || importMode == ModelImportSettings.IMPORT_CSV) {
                            	ret = addTableMovie(i);
                            }
                            /* Extreme movie manager */
                            else if (importMode == ModelImportSettings.IMPORT_EXTREME) {
                            	ret = addExtremeMovie(i);
                            }/* XML */
                            else if (importMode == ModelImportSettings.IMPORT_XML) {
                            	ret = addXMLMovie(i);
                                
                            } /* Text file */
                            else if (importMode == ModelImportSettings.IMPORT_TEXT) {
                            	ret = addTextMovie(i);
                            }

                           
                            if (ret  == -1 && MovieManager.getIt().getDatabase().getErrorMessage().equals("Data truncation cover")) {
                                modelMovieInfo.setCover("", null);
                                    
                                    try {
                                        ret = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
                                    } catch (Exception e) {
                                        log.error("Saving to database failed.", e);
                                    }
                            }
                            
                            if (ret == -1)
                                transferred.add("Failed to import: " + title);
                            else if (title != null && !title.equals(""))
                            	transferred.add(title);
                            
                            current++;
                        }
                    }
                    else {
                    	transferred.add("Empty entry");
                    	current++;
                    }
                }
                done = true;
            }
        }
        
        
        String getNextTitle(int i) {
        	
        	String title = null;        	
        	// Text
            if (importMode == importSettings.IMPORT_TEXT) {
            	title = ((String) movielist.get(i));
            }
            else if (importMode == importSettings.IMPORT_EXCEL || importMode == importSettings.IMPORT_CSV) {
            	title = ((ModelMovie) movielist.get(i)).getTitle();
            }
            else if (importMode == importSettings.IMPORT_XML) {

            	if (movielist.get(i) instanceof ModelMovie)
            		title = ((ModelMovie) movielist.get(i)).getTitle();
            	else if (movielist.get(i) instanceof ModelSeries) {
            		title = ((ModelSeries) movielist.get(i)).getMovie().getTitle();
            	}
            } 
            /* Extreme movie manager */
            else if (importMode == importSettings.IMPORT_EXTREME) {
            	title = ((ModelExtremeMovie) movielist.get(i)).title;
            }
           
        	return title;
        }
        
        
        int addTextMovie(int index) {
        	
        	int key = 1;
        	String title = (String) movielist.get(index);
        	
        	modelMovieInfo.clearModel();
        	modelMovieInfo.setTitle(title);
        	
        	 try {
        		 key = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
             } catch (Exception e) {
                 log.error("Saving to database failed.", e);
                 key = -1; 
             }
        		 
        	return key;
        }
        
        
        int addXMLMovie(int i) {
            
        	int key = -1;
            Object model = movielist.get(i);
            
            if (model instanceof ModelMovie) {
            	modelMovieInfo.setModel((ModelMovie) model, false, false);
                
                try {
                	key = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
                } catch (Exception e) {
                    log.error("Saving to database failed.", e);
                }
            }
            else if (model instanceof ModelSeries) {
            	
                ModelSeries seriesTmp = (ModelSeries) model;
                ModelEpisode episodeTmp;
                
                modelMovieInfo.setModel(seriesTmp.getMovie(), false, false);
                
                try {
                	key = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
                } catch (Exception e) {
                    log.error("Saving to database failed.", e);
                }
                
                int movieKey = modelMovieInfo.model.getKey();
                
                for (int u = 0; u < seriesTmp.episodes.size(); u++) {
                    episodeTmp = (ModelEpisode) seriesTmp.episodes.get(u);
                    episodeTmp.setMovieKey(movieKey);
                    
                    modelMovieInfo.setModel(episodeTmp, false, false);
                    
                    try {
                    	(modelMovieInfo.saveToDatabase(null)).getKey();
                    } catch (Exception e) {
                        log.error("Saving to database failed.", e);
                    }
                }
            }
            
            return key;
        }
        
        // Add movie retrieved from the DialogImportTable
        int addTableMovie(int i) {
           
            int ret = -1;
            Object tmp = movielist.get(i);
             
            modelMovieInfo.setModel((ModelMovie) tmp, false, false);
            
            try {
            	ret = modelMovieInfo.saveToDatabase(addToThisList).getKey();
            } catch (Exception e) {
                log.error("Saving to database failed.", e);
            }
            
            return ret;
        }
        
        
        
        protected ArrayList getMovieList() throws Exception {
            	
	    ArrayList movieList = null;
            
            /* Textfile */
            if (importMode == ModelImportSettings.IMPORT_TEXT) {
                
                File textFile = new File(filePath);
                
                if (!textFile.isFile()) {
                    throw new Exception("Text file does not exist.");
                }
                
                movieList = new ArrayList(10);
                
                try {
                    
                    FileReader reader = new FileReader(textFile);
                    BufferedReader stream = new BufferedReader(reader);
                    
                    String line;
                    while ((line = stream.readLine()) != null) {
                        movieList.add(line.trim());
                    }
                    lengthOfTask = movieList.size();
                }
                catch (Exception e) {
                    log.error("", e);
                }
                
                /* Excel spreadsheet and CSV text file */
            } else if (importMode == ModelImportSettings.IMPORT_EXCEL ||
            		importMode == ModelImportSettings.IMPORT_CSV) {

            	try {

            		movieList = new ArrayList(10);

            		JTable table = importSettings.table;

            		TableModel tableModel = table.getModel();
            		TableColumnModel columnModel = table.getColumnModel();
            		int columnCount = table.getModel().getColumnCount();

            		TableColumn tmpColumn;
            		FieldModel fieldModel;
                    String tableValue;
                    ModelMovie tmpMovie;
                    boolean valueStored = false;
                    
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        
                    	tmpMovie = new ModelMovie();
                    	valueStored = false;
                    	
                        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                            
                            tmpColumn = columnModel.getColumn(columnIndex);
                            Object val = tmpColumn.getHeaderValue();
                            
                            if (!(val instanceof FieldModel)) {
                                continue;
                            }
                             
                            fieldModel = (FieldModel) val;
                            
                            // column has been assigned an info field 
                            if (!fieldModel.toString().trim().equals("")) {
                                
                                tableValue = (String) table.getModel().getValueAt(row, columnIndex);
                                fieldModel.setValue(tableValue);
                                
                                fieldModel.validateValue();
                                
                                if (tmpMovie.setValue(fieldModel)) {
                                	valueStored = true;
                                }
                            }
                        }
                                                
						if (valueStored && tmpMovie.getTitle() != null && !tmpMovie.equals("")) {
                        	movieList.add(tmpMovie);
                        }
                    }
                    
                    lengthOfTask = movieList.size();
                }
                catch (Exception e) {
                    log.error("", e);
                }	
                
                /* extreme movie manager */
            } else if (importMode == ModelImportSettings.IMPORT_EXTREME) {
                DatabaseExtreme extremeDb = new DatabaseExtreme(filePath);
                extremeDb.setUp();
                
                movieList = extremeDb.getMovies();
                lengthOfTask = movieList.size();
                
                /* XML */
            } else if (importMode == ModelImportSettings.IMPORT_XML) {
                
            	File xmlFile = new File(filePath);
            	
            	if (!xmlFile.isFile()) {
            		log.error("XML file not found:" + xmlFile.getAbsolutePath());
                    return null;
                }
            	
                Mapping mapping = new Mapping();
                mapping.loadMapping(FileUtil.getFileURL("mapping.xml"));
                
                String encoding = "UTF-8";
                
                Unmarshaller unmarshaller = new Unmarshaller(ModelExportXML.class);
                //unmarshaller.setDebug(true);
                unmarshaller.setWhitespacePreserve(true);
                unmarshaller.setMapping(mapping);
                
                Object tmp = null;
                
                Reader reader = new InputStreamReader(new FileInputStream(xmlFile), encoding);
                
                //UnmarshalListener listener = new MappingUnmarshallListener(new MappingUnmarshaller(), mapping, new DTDResolver());
                
                tmp = unmarshaller.unmarshal(reader);

                if (tmp instanceof ModelExportXML) {
                	movieList = ((ModelExportXML) tmp).getCombindedList();
                	lengthOfTask = ((ModelExportXML) tmp).getAllEntriesCount();
                }
            }
           
            return movieList;
        }

        
        
        int addExtremeMovie(int i) {
            
            int ret = -1;
            
            /* Title */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getTitle().equals("")) && !((ModelExtremeMovie) movielist.get(i)).title.equals("")) {
                modelMovieInfo.model.setTitle(((ModelExtremeMovie) movielist.get(i)).title);
            }
            
            /* Date */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getDate().equals("")) && !((ModelExtremeMovie) movielist.get(i)).date.equals(""))
                modelMovieInfo.model.setDate(((ModelExtremeMovie) movielist.get(i)).date);
            
            /* Colour */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getColour().equals("")) && !((ModelExtremeMovie) movielist.get(i)).colour.equals(""))
                modelMovieInfo.model.setColour(((ModelExtremeMovie) movielist.get(i)).colour);
            
            /* Directed by */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getDirectedBy().equals("")) && !((ModelExtremeMovie) movielist.get(i)).directed_by.equals(""))
                modelMovieInfo.model.setDirectedBy(((ModelExtremeMovie) movielist.get(i)).directed_by);
            
            /* Written by */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getWrittenBy().equals("")) && !((ModelExtremeMovie) movielist.get(i)).written_by.equals(""))
                modelMovieInfo.model.setWrittenBy(((ModelExtremeMovie) movielist.get(i)).written_by);
            
            /* Genre */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getGenre().equals("")) && !((ModelExtremeMovie) movielist.get(i)).genre.equals(""))
                modelMovieInfo.model.setGenre(((ModelExtremeMovie) movielist.get(i)).genre);
            
            /* Rating */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getRating().equals("")) && !((ModelExtremeMovie) movielist.get(i)).rating.equals(""))
                modelMovieInfo.model.setRating(((ModelExtremeMovie) movielist.get(i)).rating);
            
            /* Country */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getCountry().equals("")) && !((ModelExtremeMovie) movielist.get(i)).country.equals(""))
                modelMovieInfo.model.setCountry(((ModelExtremeMovie) movielist.get(i)).country);
            
            /* Seen */
            modelMovieInfo.model.setSeen(((ModelExtremeMovie) movielist.get(i)).seen);
            
            /* Language */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getLanguage().equals("")) && !((ModelExtremeMovie) movielist.get(i)).language.equals("")) {
                if (originalLanguage)
                    modelMovieInfo.model.setLanguage(((ModelExtremeMovie) movielist.get(i)).originalLanguage);
                else
                    modelMovieInfo.model.setLanguage(((ModelExtremeMovie) movielist.get(i)).language);
            }
            
            /* Plot */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getPlot().equals("")) && !((ModelExtremeMovie) movielist.get(i)).plot.equals(""))
                modelMovieInfo.model.setPlot(((ModelExtremeMovie) movielist.get(i)).plot);
            
            /* Cast */
            if (!(overwriteWithImdbInfo && !modelMovieInfo.model.getCast().equals("")) && !((ModelExtremeMovie) movielist.get(i)).cast.equals(""))
                modelMovieInfo.model.setCast(((ModelExtremeMovie) movielist.get(i)).cast);
            
            /* Notes */
            modelMovieInfo.model.setNotes(((ModelExtremeMovie) movielist.get(i)).notes);
            
            /* Runntime */
            modelMovieInfo.model.setWebRuntime(((ModelExtremeMovie) movielist.get(i)).length);
            
            /* If Aka doesn't equal the title */
            if (!((ModelExtremeMovie) movielist.get(i)).aka.equalsIgnoreCase(((ModelExtremeMovie) movielist.get(i)).title))
                modelMovieInfo.model.setAka(((ModelExtremeMovie) movielist.get(i)).aka);
            
            /* MPAA */
            modelMovieInfo.model.setMpaa(((ModelExtremeMovie) movielist.get(i)).mpaa);
            
            modelMovieInfo.setFieldValues(new ArrayList());
            
            /* Subtitles */
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).subtitles);
            
            String duration;
            try {
                int length;
                duration = IMDB.extractTime(((ModelExtremeMovie) movielist.get(i)).length);
                
                if (!duration.equals("")) {
                    length = Integer.parseInt(duration);
                    length = length*60;
                    duration = String.valueOf(length);
                }
            }
            catch (Exception e) {
                duration = "";
            }
            
            modelMovieInfo.getFieldValues().add(duration); /* Duration */
            modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).filesize, 0));
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).cds); /* CDs */ 
            modelMovieInfo.getFieldValues().add("1"); /* CD cases */
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).resolution);
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).codec);
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).videoRate);
            modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).bitrate, 0));
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).audioCodec);
            modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).sampleRate, 0));
            modelMovieInfo.getFieldValues().add(StringUtil.cleanInt(((ModelExtremeMovie) movielist.get(i)).audioBitrate, 0));
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).channels);
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).filePath); /* Location */
            modelMovieInfo.getFieldValues().add(String.valueOf(((ModelExtremeMovie) movielist.get(i)).fileCount));
            modelMovieInfo.getFieldValues().add(""); /* Container */
            modelMovieInfo.getFieldValues().add(((ModelExtremeMovie) movielist.get(i)).media);
            
            /* Adding empty values for the extra info fields */
            for (int u = 0; u < extraInfoFieldsCount; u++)
                modelMovieInfo.getFieldValues().add("");
            
            if (((ModelExtremeMovie) movielist.get(i)).scriptUsed.indexOf("IMDB.COM") != -1)
                modelMovieInfo.model.setUrlKey(((ModelExtremeMovie) movielist.get(i)).imdb);
            
            if (!((ModelExtremeMovie) movielist.get(i)).cover.equals("none.jpg")) {
                File tempFile = new File(coverPath + ((ModelExtremeMovie) movielist.get(i)).cover);
                
                if (tempFile.isFile()) {
                    
                    try {
                        /* Reading cover to memory */
                        InputStream inputStream = new FileInputStream(tempFile);
                        byte [] coverData = new byte[inputStream.available()];
                        inputStream.read(coverData);
                        inputStream.close();
                        
                        /* Setting cover */
                        modelMovieInfo.setCover(((ModelExtremeMovie) movielist.get(i)).cover, coverData);
                    }
                    catch (Exception e) {
                        log.error("", e);
                    }
                }
                else
                    log.warn(tempFile.getAbsolutePath() + " does not exist");
            }
         
            try {
                ret = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
            } catch (Exception e) {
                log.error("Saving to database failed.", e);
            }
            
            return ret;
        }
        
        
        /**
         * Gets the IMDB info for movies (multiAdd)
         **/
        public void executeCommandGetIMDBInfoMultiMovies(String searchString, String filename, int multiAddSelectOption) {
            
            /* Checks the movie title... */
            log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
            if (!searchString.equals("")) { //$NON-NLS-1$
                DialogIMDB dialogIMDB = new DialogIMDB(modelMovieInfo, searchString, filename, this, multiAddSelectOption, null);
            } else {
                DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.getString("DialogMovieInfo.alert.title.alert"), Localizer.getString("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
                GUIUtil.showAndWait(alert, true);
            }
        }
    }
}

