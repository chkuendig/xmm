/**
 * @(#)IMDbInfoUpdater.java 1.0 26.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.commands.MovieManagerCommandDialogIMDB;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


public class IMDbInfoUpdater {
    
    static Logger log = Logger.getRootLogger();
    
    private int lengthOfTask = 0;
    private int current = -1;
    private boolean done = false;
    private boolean canceled = false;
    private ArrayList transferred = new ArrayList();
    
    Database database = MovieManager.getIt().getDatabase();
    
    boolean skipEntriesWithIMDbID = false;
    
   /* 0 = No, 1 = Yes, 2 = Yes, but only if empty */
    
    public int title = 0;
    public int cover = 0;
    public int date = 0;
    public int colour = 0;
    public int directedBy = 0;
    public int writtenBy = 0;
    public int genre = 0;
    public int rating = 0;
    public int country = 0;
    public int language = 0;
    public int plot = 0;
    public int cast = 0;
    public int aka = 0;
    public int soundMix = 0;
    public int runtime = 0;
    public int awards = 0;
    public int mpaa = 0;
    public int certification = 0;
    
    
    public void go() {
        final SwingWorker worker = new SwingWorker() {
		public Object construct() {
		    current = -1;
		    done = false;
		    canceled = false;
		    
		    execute();
		    return this;
		}
	    };
	worker.start();
    }
    
    public int getLengthOfTask() {
        return lengthOfTask;
    }
    
    /*Returns the current position in the array*/
    public int getCurrent() {
        return current;
    }
    
    /* Stops the importing process */
    public synchronized void stop() {
    	canceled = true;
    }
    
    public boolean isDone() {
        return done;
    }
    
    /* Returns the arraylist transferred which contains all the finished database entries */
    public ArrayList getTransferred() {
	return transferred;
    }
    
    /**
     * The actual database import task.
     * This runs in a SwingWorker thread.
     */
    
    static boolean ready = true;
    static int threadCount = 0;
    
    static int successes = 0;
    
    /**
     * Executes the command.
     **/
    public void execute() {
	    
	/* Setting the priority of the thread to 4 to give the GUI room to update more often */
	Thread.currentThread().setPriority(4);
	    
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
	ModelEntry model;
	DefaultMutableTreeNode node;
	
	Enumeration enumeration = root.children();
	    
	lengthOfTask = root.getChildCount();
	
	int failCounter = 0;
	
	//final long time = System.currentTimeMillis();
	
	try {
	    
	    final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	
	    connectionManager.getParams().setDefaultMaxConnectionsPerHost(2);

	    final HttpClient client = new HttpClient(connectionManager);
	
	    final MovieManagerCommandDialogIMDB commandIMDB = new MovieManagerCommandDialogIMDB();
	    
	    while (enumeration.hasMoreElements()) {
		    
		if (canceled)
		    break;

		while (getThreadCount() > 4)
		    Thread.sleep(500);
		
		
		node = ((DefaultMutableTreeNode) enumeration.nextElement());
		
		model = (ModelEntry) node.getUserObject();
		
		class GetInfo extends Thread {
		    
		    boolean httpclient = true;
            
            ModelMovieInfo modelInfo;
		    ModelEntry model;

		    InputStream stream;
		    StringBuffer data = null;
		    int buffer;
		    
		    GetMethod method;

		    GetInfo(ModelEntry model) {
                modelInfo = new ModelMovieInfo((ModelMovie) model);
                this.model = model;
		    }
		    
		    public void run() {
				
			try {
				
			    while (!isReady())
			    	Thread.sleep(1000);

			    if (canceled)
			    	return;
			    
			    IMDB imdb;
								
			    if (model.getUrlKey().equals("")) {
                   log.info("UrlKey is empty");
                   
                   String urlKey = commandIMDB.getIMDBKey(model.getTitle());
                   
                   if (commandIMDB.cancelAll) {
                	   canceled = true;
                	   return;
                   }
                    
                   if (commandIMDB.cancel)
                	   return;
                	   
                   if (urlKey == null || urlKey.equals(""))
                	   return;
                   
                   model.setUrlKey(urlKey);
			    }
			    else if (skipEntriesWithIMDbID) {
			    	return;
			    }
			    
			    for (int i = 0; i < 5; i++) {
				    
				if (httpclient) {
					
				    method = new GetMethod("http://akas.imdb.com/title/tt"+ model.getUrlKey() +"/");
					
				    int statusCode = client.executeMethod(method);
					
				    if (statusCode != HttpStatus.SC_OK) {
				    	log.warn("Method failed: " + method.getStatusLine());
				    }
				    else {
					
					stream = method.getResponseBodyAsStream();
					/* Saves the page data in a string buffer... */
					data = new StringBuffer();
					    
					while ((buffer = stream.read()) != -1) {
					    data.append((char) buffer);
					}
					stream.close();
				    }
					
				    if (data != null) {
					    
					boolean error = false;
					    
					try {
					    
					    imdb = new IMDB(model.getUrlKey(), data);
					    
					    if (title == 1 || (title == 2 && model.getTitle().equals(""))) {
						model.setTitle(imdb.getTitle());
						 }
                        
					    if (date == 1 || (date == 2 && model.getDate().equals(""))) {
						model.setDate(imdb.getDate());
						 }
                        
					    if (colour == 1 || (colour == 2 && model.getColour().equals(""))) {
						model.setColour(imdb.getColour());
						 }
                        
					    if (directedBy == 1 || (directedBy == 2 && model.getDirectedBy().equals(""))) {
model.setDirectedBy(imdb.getDirectedBy());
						 }
                        
					    if (writtenBy == 1 || (writtenBy == 2 && model.getWrittenBy().equals(""))) {
						model.setWrittenBy(imdb.getWrittenBy());
						 }
                        
					    if (genre == 1 || (genre == 2 && model.getGenre().equals(""))) {
						model.setGenre(imdb.getGenre());
						 }
                        
					    if (rating == 1 || (rating == 2 && model.getRating().equals(""))) {
						model.setRating(imdb.getRating());
						 }
                        
					    if (country == 1 || (country == 2 && model.getCountry().equals(""))) {
						model.setCountry(imdb.getCountry());
						 }
                        
					    if (language == 1 || (language == 2 && model.getLanguage().equals(""))) {
						model.setLanguage(imdb.getLanguage());
						 }
                        
					    if (plot == 1 || (plot == 2 && model.getPlot().equals(""))) {
						model.setPlot(imdb.getPlot());
						 }
                        
					    if (cast == 1 || (cast == 2 && model.getCast().equals(""))) {
						model.setCast(imdb.getCast());
						}
                        
					    if (aka == 1 || (aka == 2 && model.getAka().equals(""))) {
					        model.setAka(imdb.getAka());
					        modelInfo.executeTitleModification(imdb.getTitle());
                         }
                        
					    if (soundMix == 1 || (soundMix == 2 && model.getWebSoundMix().equals(""))) {
						model.setWebSoundMix(imdb.getSoundMix());
						}
                        
					    if (runtime == 1 || (runtime == 2 && model.getWebRuntime().equals(""))) {
						model.setWebRuntime(imdb.getRuntime());
						}
                        
					    if (awards == 1 || (awards == 2 && model.getAwards().equals(""))) {
						model.setAwards(imdb.getAwards());
						 }
                        
					    if (mpaa == 1 || (mpaa == 2 && model.getMpaa().equals(""))) {
						model.setMpaa(imdb.getMpaa());
						 }
                        
					    if (certification == 1 || (certification == 2 && model.getCertification().equals(""))) {
						model.setCertification(imdb.getCertification());
						 }

					    
					    if (cover == 1 || (cover == 2 && model.getCover().equals(""))) {
					        
					        try {
					            
					            /* Gets the covers folder... */
					            String coversFolder = database.getCoversFolder();
					            
					            byte [] coverData = imdb.getCover();
					            
					            if (imdb.getCoverOK()) {
					                model.setCoverData(coverData);
					                model.setCover(imdb.getCoverName());
					                
					                
					                if (!((MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) && !MovieManager.getConfig().getStoreCoversLocally())
					                        && (imdb.getCoverURL().indexOf("/") != -1)) {
					                    
					                    /* Creates the new file... */
					                    File coverFile = new File(coversFolder, imdb.getCoverName());
					                    
					                    if (coverFile.exists()) {
					                        if (!coverFile.delete() && !coverFile.createNewFile()) {
					                            throw new Exception("Cannot delete old cover file and create a new one.");
					                        }
					                    } else {
					                        if (!coverFile.createNewFile()) {
					                            throw new Exception("Cannot create cover file.");
					                        }
					                    }
					                    
					                    /* Copies the cover to the covers folder... */
					                    OutputStream outputStream = new FileOutputStream(coverFile);
					                    outputStream.write(coverData);
					                    outputStream.close();
					                }
                                }
					        } catch (Exception e) {
					            log.error("", e);
					        }
                        }
                        
					    modelInfo.saveToDatabase(null);
                                               
					} catch (Exception e) {
					    log.fatal("", e);
					    error = true;
					}
					
					data = null;
					
					if (!error)
					    break;
					
					method.releaseConnection();
					method = null;
				    }
				    else {
					method.releaseConnection();
					method = null;
					    
					setReady(false);
					Thread.sleep(4000);
					setReady(true);
				    }
				}
				else {
				    try {
					imdb = new IMDB(model.getUrlKey());
					
				    } catch (Exception e) {
					log.error("Exception: " + e.getMessage());
					
					setReady(false);
					Thread.sleep(4000);
					setReady(true);
				    } 
				}
			    }
			
			} catch (HttpException e) {
			    log.error("Fatal protocol violation: " + e.getMessage());
			} catch (IOException e) {
			    log.error("Fatal transport error: " + e.getMessage());
			} catch (InterruptedException e) {
			    log.error("Fatal interrupted error: " + e.getMessage());
			} finally {
			    /* Release the connection. */
			    if (httpclient && method != null) {
			    	method.releaseConnection();
			    	method = null;
			    }
			    
			    decreaseThreadCount();
			    encreaseSuccess();
			}
			
			addTransferred(model.getTitle());
			return;
		    }
		}
		    
		/* Creating a Object wrapped in a Thread */
		Thread t = new Thread(new GetInfo(model));
		encreaseThreadCount();
		t.start();
		    
		//connectionManager.deleteClosedConnections();
	    }
	    
	    
	    log.debug("Thread count:" + getThreadCount());
	    
	    /* Waits untill all the threads are finished */
	    while (getThreadCount() > 0) {
		Thread.sleep(400);
		//log.debug("Thread count:" + getThreadCount());
	    }
	    
	    log.debug("Done updating list!");
	    
	    done = true;
	    
	    connectionManager.shutdown();
	    
	    
	} catch (InterruptedException e) {
	    log.error("Fatal interrupted error: " + e.getMessage());
	} 
	
	
	log.debug("Total fails:" + failCounter);
    }
    
    synchronized int setGeneralInfo(ModelMovie model) {
	return database.setGeneralInfo(model);
    }
    
    synchronized void addTransferred(String transfer) {
	transferred.add(transfer); 
    }

    synchronized static boolean isReady() {
	return ready;
    }
    
    synchronized static void setReady(boolean rdy) {
	ready = rdy;
    }
    
    synchronized static int getThreadCount() {
	return threadCount;
    }
    
    synchronized static void encreaseThreadCount() {
	threadCount++;
    }
    
    synchronized static void decreaseThreadCount() {
	threadCount--;
    }
    
    synchronized static void encreaseSuccess() {
	successes++;
    }
}
