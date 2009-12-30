/**
 * @(#)IMDbInfoUpdater.java
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandDialogIMDB;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.log4j.Logger;


public class IMDbInfoUpdater {

	static Logger log = Logger.getRootLogger();

	private int lengthOfTask = 0;
	private int current = -1;
	private boolean done = false;
	private boolean canceled = false;
	private ArrayList<String> transferred = new ArrayList<String>();

	Database database = MovieManager.getIt().getDatabase();

	public boolean skipEntriesWithIMDbID = false;
	public boolean skipEntriesWithoutIMDbID = false;
	final MovieManagerCommandDialogIMDB commandIMDB = new MovieManagerCommandDialogIMDB();
	
	String coversFolder = MovieManager.getConfig().getCoversPath();
	
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

	public void setDone() {
		done = true;
	}
	
	/* Returns the arraylist transferred which contains all the finished database entries */
	public ArrayList<String> getTransferred() {
		return transferred;
	}

	static boolean ready = true;
	//static int successes = 0;

	final ThreadHandler threadHandler = new ThreadHandler();
	
	@SuppressWarnings("unchecked")
	public void execute() {

		/* Setting the priority of the thread to 4 to give the GUI room to update more often */
		Thread.currentThread().setPriority(4);

		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
		final Enumeration<DefaultMutableTreeNode> enumeration = root.children();
				
		lengthOfTask = root.getChildCount();
	
		//final long time = System.currentTimeMillis();

		try {

			Runnable threadRunner = new Runnable() {

				public void run() {

					DefaultMutableTreeNode node;
					ModelEntry model;

					ModelMovieInfo modelInfo = new ModelMovieInfo();
					IMDB imdb;
					try {
						imdb = new IMDB(MovieManager.getConfig().getHttpSettings());

						while (enumeration.hasMoreElements()) {
							
							if (canceled)
								break;

							if (threadHandler.getThreadCount() > 0) {
								threadHandler.waitForNextDecrease();
							}
							
							node = enumeration.nextElement();
							model = (ModelEntry) node.getUserObject();

							if (!model.getHasGeneralInfoData()) {
								model.updateGeneralInfoData();
							}

							if (!model.getHasAdditionalInfoData()) {
								model.updateAdditionalInfoData();
							}
	
							/* wrapping each movie in a thread */
							Thread t = new Thread(new GetInfo(modelInfo, model, imdb));
							
							t.start();
						}
						
						while (threadHandler.getThreadCount() > 0) {
							Thread.sleep(500);
						}
												
						setDone();
						
						log.debug("Done updating list!");
												
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			
			Thread t = new Thread(threadRunner);
			t.start();
			
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage());
		}
	}

	class GetInfo extends Thread {

		ModelMovieInfo modelInfo;
		ModelEntry model;
		IMDB imdb;

		private final int tryTimes = 4;
		
		InputStream stream;
		StringBuffer data = null;
		int buffer;
		boolean error = false;
		boolean skipped = false;
		
		GetInfo(ModelMovieInfo modelInfo, ModelEntry model, IMDB imdb) {
			this.modelInfo = modelInfo;
			this.model = model;
			this.imdb = imdb;
		}

		boolean changed = false;
		ModelIMDbEntry movie = null;// = imdb.grabInfo(model.getUrlKey());
		
		public ModelIMDbEntry getIMDbModel(String urlKey) throws Exception {
			
			changed = true;
			
			if (movie == null)
				movie =  imdb.grabInfo(urlKey);
			
			return movie;
		}
		
		public void run() {

			try {

				threadHandler.increaseThreadCount();
				
			//	while (!isReady())
				Thread.sleep(50);

				if (canceled)
					return;

								
				if (model.getUrlKey().equals("")) {
					log.debug("Empty UrlKey for " + model.getTitle());

					if (skipEntriesWithoutIMDbID) {
						skipped = true;
						return;
					}
											
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
					skipped = true;
					return;
				}

				for (int i = 0; i < tryTimes; i++) {

					error = false;
									
					try {
						
						if (title == 1 || (title == 2 && model.getTitle().equals(""))) {
							model.setTitle(getIMDbModel(model.getUrlKey()).getTitle());
						}

						if (date == 1 || (date == 2 && model.getDate().equals(""))) {
							model.setDate(getIMDbModel(model.getUrlKey()).getDate());
						}

						if (colour == 1 || (colour == 2 && model.getColour().equals(""))) {
							model.setColour(getIMDbModel(model.getUrlKey()).getColour());
						}

						if (directedBy == 1 || (directedBy == 2 && model.getDirectedBy().equals(""))) {
							model.setDirectedBy(getIMDbModel(model.getUrlKey()).getDirectedBy());
						}

						if (writtenBy == 1 || (writtenBy == 2 && model.getWrittenBy().equals(""))) {
							model.setWrittenBy(getIMDbModel(model.getUrlKey()).getWrittenBy());
						}

						if (genre == 1 || (genre == 2 && model.getGenre().equals(""))) {
							model.setGenre(getIMDbModel(model.getUrlKey()).getGenre());
						}

						if (rating == 1 || (rating == 2 && model.getRating().equals(""))) {							
							model.setRating(getIMDbModel(model.getUrlKey()).getRating());
						}

						if (country == 1 || (country == 2 && model.getCountry().equals(""))) {
							model.setCountry(getIMDbModel(model.getUrlKey()).getCountry());
						}

						if (language == 1 || (language == 2 && model.getLanguage().equals(""))) {
							model.setLanguage(getIMDbModel(model.getUrlKey()).getLanguage());
						}

						if (plot == 1 || (plot == 2 && model.getPlot().equals(""))) {
							model.setPlot(getIMDbModel(model.getUrlKey()).getPlot());
						}

						if (cast == 1 || (cast == 2 && model.getCast().equals(""))) {
							model.setCast(getIMDbModel(model.getUrlKey()).getCast());
						}

						if (aka == 1 || (aka == 2 && model.getAka().equals(""))) {							
							model.setAka(getIMDbModel(model.getUrlKey()).getAka());
							//model.setTitle(imdb.getModifiedTitle(getIMDbModel(model.getUrlKey()).getTitle()));
							ModelMovieInfo.executeTitleModification(model);
						}

						if (soundMix == 1 || (soundMix == 2 && model.getWebSoundMix().equals(""))) {
							model.setWebSoundMix(getIMDbModel(model.getUrlKey()).getWebSoundMix());
						}

						if (runtime == 1 || (runtime == 2 && model.getWebRuntime().equals(""))) {
							model.setWebRuntime(getIMDbModel(model.getUrlKey()).getWebRuntime());
						}

						if (awards == 1 || (awards == 2 && model.getAwards().equals(""))) {
							model.setAwards(getIMDbModel(model.getUrlKey()).getAwards());
						}

						if (mpaa == 1 || (mpaa == 2 && model.getMpaa().equals(""))) {
							model.setMpaa(getIMDbModel(model.getUrlKey()).getMpaa());
						}

						if (certification == 1 || (certification == 2 && model.getCertification().equals(""))) {
							model.setCertification(getIMDbModel(model.getUrlKey()).getCertification());
						}

						if (cover == 1 || (cover == 2 && (model.getCover().equals("") || 
								(MovieManager.getIt().getDatabase().isMySQL() && model.getCoverData() == null)))) {
														
							try {
	
								byte [] coverData = getIMDbModel(model.getUrlKey()).getCoverData();

								if (coverData != null) {
									model.setCoverData(coverData);
									model.setCover(imdb.getCoverName());

									if (!((MovieManager.getIt().getDatabase().isMySQL()) 
											&& !MovieManager.getConfig().getStoreCoversLocally()) 
											&& (imdb.getCoverURL().indexOf("/") != -1)) {

										if (new File(coversFolder).isDirectory()) {

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
											FileUtil.writeToFile(coverData, coverFile);
										}
									}
								}
								
							} catch (Exception e) {
								log.error("Exception:" + e.getMessage(), e);
							}
						}
							
						if (changed)
							modelInfo.saveToDatabase(model, true, null);
						else
							log.debug("Not saving " + model.getTitle());

					} catch (Exception e) {
						log.fatal("Exception:" + e.getMessage(), e);
						error = true;
					}

					data = null;

					if (!error)
						break;
					
					// Sleep before next try
					Thread.sleep(1000);
				}
								
			} catch (InterruptedException e) {
				log.error("Fatal interrupted error: " + e.getMessage());
			} finally {
				try {
					threadHandler.decreaseThreadCount();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (error) {
					addTransferred("Failed to retrive info on " + model.getTitle());
					log.warn("failed to retrieve info for entry " + model.getTitle());
				}
				else if (skipped || !changed)
					addTransferred("Skipping " + model.getTitle());
				else
					addTransferred(model.getTitle());	
			}
		}
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

/*	
	synchronized static void encreaseSuccess() {
		successes++;
	}*/
	
	public class ThreadHandler {

		// The number of currently active threads
		int threadCount = 0;
		
		// The total number of threads that have been used
		int totalThreads = 0;
		
		synchronized public void increaseThreadCount() {
			threadCount++;
			totalThreads++;
		}
		
		synchronized public void decreaseThreadCount() throws Exception  {
			threadCount--;
			notify();
		}
		
		synchronized public int getThreadCount() {
			return threadCount;
		}
		
		synchronized public int getTotalThreadCount() {
			return totalThreads;
		}
		
		
		public void waitForNextDecrease() throws Exception {
			
			synchronized(this) {
				wait();
			}
		}	
	}
}
