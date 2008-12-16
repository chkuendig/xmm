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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandDialogIMDB;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.SwingWorker;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;


public class IMDbInfoUpdater {

	static Logger log = Logger.getRootLogger();

	private int lengthOfTask = 0;
	private int current = -1;
	private boolean done = false;
	private boolean canceled = false;
	private ArrayList transferred = new ArrayList();

	Database database = MovieManager.getIt().getDatabase();

	public boolean skipEntriesWithIMDbID = false;
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

	/* Returns the arraylist transferred which contains all the finished database entries */
	public ArrayList getTransferred() {
		return transferred;
	}


	static boolean ready = true;
	static int threadCount = 0;

	static int successes = 0;

	
	public void execute() {

		/* Setting the priority of the thread to 4 to give the GUI room to update more often */
		Thread.currentThread().setPriority(4);

		DefaultMutableTreeNode node;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
		Enumeration enumeration = root.children();
		
		
		ModelEntry model;
		
		
		lengthOfTask = root.getChildCount();

		int failCounter = 0;

		//final long time = System.currentTimeMillis();

		try {

			ModelMovieInfo modelInfo = new ModelMovieInfo();
			IMDB imdb = new IMDB(MovieManager.getConfig().getHttpSettings());

			while (enumeration.hasMoreElements()) {

				if (canceled)
					break;

				while (getThreadCount() > 15)
					Thread.sleep(500);

				node = ((DefaultMutableTreeNode) enumeration.nextElement());

				model = (ModelEntry) node.getUserObject();

				/* wrapping each movie in a thread */
				Thread t = new Thread(new GetInfo(modelInfo, model, imdb));
				encreaseThreadCount();
				t.start();
			}

			log.debug("Thread count:" + getThreadCount());

			/* Waits until all the threads are finished */
			while (getThreadCount() > 0) {
				Thread.sleep(400);
			}

			log.debug("Done updating list!");

			done = true;

			//connectionManager.shutdown();

		} catch (InterruptedException e) {
			log.error("Fatal interrupted error: " + e.getMessage());
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage());
		}
		


		log.debug("Total fails:" + failCounter);
	}

	class GetInfo extends Thread {

		ModelMovieInfo modelInfo;
		ModelEntry model;
		IMDB imdb;

		InputStream stream;
		StringBuffer data = null;
		int buffer;
		boolean error = false;

		GetMethod method;

		GetInfo(ModelMovieInfo modelInfo, ModelEntry model, IMDB imdb) {
			this.modelInfo = modelInfo;
			this.model = model;
			this.imdb = imdb;
		}

		public void run() {

			try {

				while (!isReady())
					Thread.sleep(1000);

				if (canceled)
					return;

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

					try {

						ModelIMDbEntry movie = imdb.grabInfo(model.getUrlKey());

						if (title == 1 || (title == 2 && model.getTitle().equals(""))) {
							model.setTitle(imdb.getCorrectedTitle(movie.getTitle()));
						}

						if (date == 1 || (date == 2 && model.getDate().equals(""))) {
							model.setDate(movie.getDate());
						}

						if (colour == 1 || (colour == 2 && model.getColour().equals(""))) {
							model.setColour(movie.getColour());
						}

						if (directedBy == 1 || (directedBy == 2 && model.getDirectedBy().equals(""))) {
							model.setDirectedBy(movie.getDirectedBy());
						}

						if (writtenBy == 1 || (writtenBy == 2 && model.getWrittenBy().equals(""))) {
							model.setWrittenBy(movie.getWrittenBy());
						}

						if (genre == 1 || (genre == 2 && model.getGenre().equals(""))) {
							model.setGenre(movie.getGenre());
						}

						if (rating == 1 || (rating == 2 && model.getRating().equals(""))) {
							model.setRating(movie.getRating());
						}

						if (country == 1 || (country == 2 && model.getCountry().equals(""))) {
							model.setCountry(movie.getCountry());
						}

						if (language == 1 || (language == 2 && model.getLanguage().equals(""))) {
							model.setLanguage(movie.getLanguage());
						}

						if (plot == 1 || (plot == 2 && model.getPlot().equals(""))) {
							model.setPlot(movie.getPlot());
						}

						if (cast == 1 || (cast == 2 && model.getCast().equals(""))) {
							model.setCast(movie.getCast());
						}

						if (aka == 1 || (aka == 2 && model.getAka().equals(""))) {
							model.setAka(movie.getAka());
							model.setTitle(imdb.getCorrectedTitle(movie.getTitle()));
							ModelMovieInfo.executeTitleModification(model);
						}

						if (soundMix == 1 || (soundMix == 2 && model.getWebSoundMix().equals(""))) {
							model.setWebSoundMix(movie.getWebSoundMix());
						}

						if (runtime == 1 || (runtime == 2 && model.getWebRuntime().equals(""))) {
							model.setWebRuntime(movie.getWebRuntime());
						}

						if (awards == 1 || (awards == 2 && model.getAwards().equals(""))) {
							model.setAwards(movie.getAwards());
						}

						if (mpaa == 1 || (mpaa == 2 && model.getMpaa().equals(""))) {
							model.setMpaa(movie.getMpaa());
						}

						if (certification == 1 || (certification == 2 && model.getCertification().equals(""))) {
							model.setCertification(movie.getCertification());
						}

						if (cover == 1 || (cover == 2 && model.getCover().equals(""))) {

							try {

								//System.err.println("coversfolder:" + coversFolder);
								
								//byte [] coverData = imdb.getCover();
								byte [] coverData = movie.getCoverData();

								if (coverData != null) {
									model.setCoverData(coverData);
									model.setCover(imdb.getCoverName());

									if (!((MovieManager.getIt().getDatabase().isMySQL()) && !MovieManager.getConfig().getStoreCoversLocally())
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
										FileUtil.writeToFile(coverData, coverFile);
									}
								}
							} catch (Exception e) {
								log.error("Exception:" + e.getMessage(), e);
							}
						}

						long time = System.currentTimeMillis();
						//System.err.println("save:" + getName());
						modelInfo.saveToDatabase(model, true, null);
						System.err.println("save " + getName() + ":" + System.currentTimeMillis());

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

				encreaseSuccess();
				
			} catch (InterruptedException e) {
				log.error("Fatal interrupted error: " + e.getMessage());
			} finally {
				decreaseThreadCount();
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
