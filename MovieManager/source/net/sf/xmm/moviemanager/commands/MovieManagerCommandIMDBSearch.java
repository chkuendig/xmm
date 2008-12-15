package net.sf.xmm.moviemanager.commands;

/**
 * @(#)MovieManagerCommandIMDBSearch.java 1.0 26.10.08 (dd.mm.yy)
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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogTVSeries;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEpisode;
import net.sf.xmm.moviemanager.swing.extentions.events.ModelUpdatedEvent.IllegalEventTypeException;
import net.sf.xmm.moviemanager.util.AdvancedMailbox;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SwingWorker;

public class MovieManagerCommandIMDBSearch {

	static Logger log = Logger.getRootLogger();
	
	ModelMovieInfo movieInfoModel;
	DialogTVSeries dialogTVSeries;
	
	IMDB imdb = null;
	
	 /* The current mode (Season/episode */
    private static int mode = 0;
    private ArrayList streamsArray = new ArrayList();
    
    private JDialog parent;
    
    public boolean multipleEpisodesAdded = false;
        
    public void closing() {
    	
    }
	
	public MovieManagerCommandIMDBSearch(ModelMovieInfo movieInfoModel) {
		this.movieInfoModel = movieInfoModel;
		
		 	try {
	        	imdb = new IMDB(MovieManager.getConfig().getHttpSettings()); 
	        } catch (Exception e) {
	        	log.error("Exception:" + e.getMessage());
	        }
	}
	
	public void execute(JDialog parent) {
		
		this.parent = parent;
		mode = 0;
		
		dialogTVSeries = new DialogTVSeries(movieInfoModel.model.getTitle(), parent);
		
		dialogTVSeries.getButtonSelect().addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent event) {
	                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	                executeCommandSelect();
	            }});
		
		dialogTVSeries.buttonOk.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent event) {
	                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
	                dialogTVSeries.dispose();
	            }});
		
				
		GUIUtil.show(dialogTVSeries, true);
		
		SwingWorker worker = new SwingWorker() {
            public Object construct() {
                try {
                    DefaultListModel list = imdb.getSeriesMatches(movieInfoModel.getModel().getTitle());
                    
                    System.err.println("match size:" + list.getSize());
                    
                    if (list == null) {
                        final DefaultListModel model = new DefaultListModel();
                        model.addElement(new ModelIMDbSearchHit(null, Localizer.getString("DialogTVDOTCOM.list-item.message.no-matches-found"), null)); //$NON-NLS-1$
                        
                        System.err.println("adding not found string:" + Localizer.getString("DialogTVDOTCOM.list-item.message.no-matches-found"));
                        
                        Runnable updateProgres = new Runnable() {
                            public void run() {
                                try {
                                	dialogTVSeries.getMoviesList().setModel(model);  
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                                
                            }};
                            
                            SwingUtilities.invokeLater(updateProgres);
                            return this;
                    }
                    
                    if (list.getSize() == 0)
                        executeErrorMessage();
                    else 
                    	dialogTVSeries.getMoviesList().setModel(list);
                    
                    dialogTVSeries.getMoviesList().setSelectedIndex(0);
                    
                    //getButtonMore().setEnabled(true);
                    dialogTVSeries.getButtonSelect().setEnabled(true);
                }
                catch (Exception e) {
                    return ""; //$NON-NLS-1$
                }
                return ""; //$NON-NLS-1$
            }
        };
        worker.start();
	}
	

	/**
	 * Gets more or less info...
	 **/
	private void executeCommandSelect() {

		try {

			System.err.println("executeCommandSelect:" + mode);
			
			int index = dialogTVSeries.getMoviesList().getSelectedIndex();

			final DefaultListModel listModel = (DefaultListModel) dialogTVSeries.getMoviesList().getModel();

			/* Get seasons */
			if (mode == 0) {

				dialogTVSeries.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				dialogTVSeries.getMoviesList().setModel(imdb.getSeasons((ModelIMDbSearchHit) listModel.getElementAt(index)));
				dialogTVSeries.getMoviesList().setSelectedIndex(0);

				dialogTVSeries.getMoviesList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				dialogTVSeries.buttonSelectAll.setEnabled(true);
				
				dialogTVSeries.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			/* Get episodes */
			else if (mode == 1) {

				dialogTVSeries.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				ModelIMDbSearchHit selected = null;
				StringBuffer episodesStream;

				int episodes = 0;

				Object [] selectedValues = dialogTVSeries.getMoviesList().getSelectedValues();
				int seasons = selectedValues.length;

				for( int i = 0; i < seasons; i++) {
					selected = (ModelIMDbSearchHit) selectedValues[i];
					episodesStream = imdb.getEpisodesStream(selected);
					streamsArray.add(imdb.getEpisodes(selected, episodesStream));
				}

				DefaultListModel allEpisodes = new DefaultListModel();
				DefaultListModel tempList = new DefaultListModel();

				int indexInList = 0;
				
				for (Iterator it = streamsArray.iterator(); it.hasNext();) {
					tempList = (DefaultListModel) it.next();
					episodes = tempList.getSize();

					for (int i = 0; i < episodes; i++) {
						
						ModelIMDbSearchHit tmp = (ModelIMDbSearchHit) tempList.getElementAt(i);
						tmp.index = indexInList++;
						allEpisodes.addElement(tmp);
					}
				}

				dialogTVSeries.getMoviesList().setModel(allEpisodes);

				dialogTVSeries.getMoviesList().setSelectedIndex(0);
				dialogTVSeries.getMoviesList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

				dialogTVSeries.buttonSelectAll.setEnabled(true);
				dialogTVSeries.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			/* Get episode info */
			else if (mode == 2) {

				final int movieKey = ((ModelEpisode) movieInfoModel.getModel()).getMovieKey();

				final Object [] selectedValues = dialogTVSeries.getMoviesList().getSelectedValues();

				if (selectedValues.length > 1)
					multipleEpisodesAdded = true;

				final boolean multipleEpisodes = multipleEpisodesAdded;
				
				final AdvancedMailbox mailbox = new AdvancedMailbox();

				for (int i = 0; i < selectedValues.length; i++)
					mailbox.addElement(selectedValues[i]);

				System.err.println("mailbox created:" + mailbox.getSize());
				
				
				
				final Thread adder = new Thread(new Runnable() {

					public void run() {

						dialogTVSeries.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						
						try {

							int mailbox_size = mailbox.getSize();
							
							while (mailbox_size-- > 0) {

								while (mailbox.getThreadCount() > 6)
									Thread.sleep(100);

								System.err.println("Size:" + mailbox.getSize() + " count:" + 
										mailbox.getThreadCount() + "  total:" + 
										mailbox.getTotalThreadCount());
							
								
								//getThreadCount
								Thread oneEpisode = new Thread(new Runnable() {

									public void run() {	

										try {
											Thread.currentThread().setPriority(4);
																						
											final ModelIMDbSearchHit searchHit = (ModelIMDbSearchHit) mailbox.pop();
											
											ModelIMDbEntry entry = imdb.getEpisodeInfo(searchHit);
											final ModelEntry modelEntry = saveData(entry, movieKey, multipleEpisodes);

											/* Adding each entry to the movie list */
											final boolean expandAndExecute = mailbox.getSize() == 0;
											
											
											System.err.println("EGG episodeKey:" + ((ModelEpisode) modelEntry).getEpisodeKey());
											System.err.println("EGG getEpisodeNumber:" + ((ModelEpisode) modelEntry).getEpisodeNumber());
											
											SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													System.err.println("4 " + ((ModelEpisode) modelEntry).getEpisodeKey() + " title:" + ((ModelEpisode) modelEntry).getEpisodeTitle() );
													
													
													MovieManagerCommandSelect.executeAndReload(modelEntry, false, true, expandAndExecute);
													System.err.println("5 " + ((ModelEpisode) modelEntry).getEpisodeKey() + " title:" + ((ModelEpisode) modelEntry).getEpisodeTitle() );
													
													
													System.err.println("IGG episodeKey:" + ((ModelEpisode) modelEntry).getEpisodeKey());
													System.err.println("IGG getEpisodeNumber:" + ((ModelEpisode) modelEntry).getEpisodeNumber());
													
												}
											});
											
											mailbox.decreaseThreadCount();
											
											searchHit.processed = true;
											
											SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													listModel.set(searchHit.index, searchHit);
												}
											});
											
											
											System.err.println("Size:" + mailbox.getSize() + " THcount:" + 
													mailbox.getThreadCount() + "  total:" + 
													mailbox.getTotalThreadCount());
											
										} catch (Exception e) {
											log.warn("Exception:" + e.getMessage(), e);
										}
									}
								});

								oneEpisode.start();
								mailbox.increaseThreadCount();				
								
								boolean execute = false;

							}	//String coverFileName =  movieKey +((ModelSearchHit) selectedValues[0]).getCoverExtension();

							mailbox.waitOnThreads();

							dialogTVSeries.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							
							dialogTVSeries.dispose();
							parent.dispose();

							try {
								movieInfoModel.modelChanged(this, "GeneralInfo");
							} catch (IllegalEventTypeException e) {
								log.error("IllegalEventTypeException:" + e.getMessage());
							}

						} catch (Exception e) {
							log.warn("Exception:" + e.getMessage(), e);
						}
					}});
				adder.start();
			}
			mode++;

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
	
	public synchronized ModelEntry saveData(ModelIMDbEntry entry, int movieKey, boolean multipleEpisodes) {

		ModelEpisode episode = new ModelEpisode();
		
		copyData(entry, episode);

		//System.err.println("saveData 1:" + ((ModelEpisode) episode).getEpisodeKey());
		
		episode.setMovieKey(movieKey); 
		//System.err.println("saveData 2:" + ((ModelEpisode) episode).getEpisodeKey());
		
		movieInfoModel.setModel(episode, false, false);

		//System.err.println("saveData 3:" + ((ModelEpisode) episode).getEpisodeKey());
		
		System.err.println("3 " + ((ModelEpisode) episode).getEpisodeKey() + " title:" + ((ModelEpisode) episode).getEpisodeTitle() );
				
		//System.err.println(entry.getTitle() + "COVER:" + (episode.getCoverData() != null));
		//System.err.println("entry title:" + entry.getTitle());
		//System.err.println("episode title:" + episode.getTitle());

		// The cover... 
		if (episode.getCoverData() != null) {
			movieInfoModel.setCover(entry.getCoverName(), episode.getCoverData(), !multipleEpisodes);
			movieInfoModel.setSaveCover(true);
		} else {
			movieInfoModel.setCover("", null, !multipleEpisodes);
			movieInfoModel.setSaveCover(false);
		}

		ModelEntry tmpEntry = null;
		
		try {
			tmpEntry = movieInfoModel.saveToDatabase(null);
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
		}

		try {
			movieInfoModel.saveCoverToFile();
		} catch (Exception e) {
			log.error("Saving cover file failed.", e);
		}
	
		return tmpEntry;
	}
	

	
	
	public void copyData(ModelIMDbEntry imdbEntry, ModelEntry entry) {
		
		entry.setUrlKey(imdbEntry.getUrlID());
		entry.setCover(imdbEntry.getCoverName());
		entry.setDate(imdbEntry.getDate());
		entry.setTitle(imdbEntry.getTitle());
		entry.setDirectedBy(imdbEntry.getDirectedBy());
		entry.setWrittenBy(imdbEntry.getWrittenBy());
		entry.setGenre(imdbEntry.getGenre());
		entry.setRating(imdbEntry.getRating());
		entry.setPlot(imdbEntry.getPlot());
		entry.setCast(imdbEntry.getCast());
		entry.setAka(imdbEntry.getAka());
		entry.setCountry(imdbEntry.getCountry()); 
		entry.setLanguage(imdbEntry.getLanguage());
		entry.setColour(imdbEntry.getColour());
		entry.setCertification(imdbEntry.getCertification());
		entry.setMpaa(imdbEntry.getMpaa());
		entry.setWebSoundMix(imdbEntry.getWebSoundMix());
		entry.setWebRuntime(imdbEntry.getWebRuntime());
		entry.setAwards(imdbEntry.getAwards());
		
		entry.setCoverData(imdbEntry.getCoverData());
		
		//System.err.println("imdbEntry.getCoverData():" + imdbEntry.getCoverData());
		
		System.err.println("imdbEntry.isEpisode:" + imdbEntry.isEpisode());
		System.err.println("entry.isEpisode:" + entry.isEpisode());
		
		System.err.println("setting episode key:" + (imdbEntry.isEpisode() && entry.isEpisode()));
		
		if (imdbEntry.isEpisode() && entry.isEpisode()) {
			
			//System.err.println("getSeasonNumber:" + ((ModelIMDbEpisode) imdbEntry).getSeasonNumber());
			
			try {
				int episodeNumber = Integer.parseInt(((ModelIMDbEpisode) imdbEntry).getSeasonNumber());
				episodeNumber *= 10000;
				episodeNumber += Integer.parseInt(((ModelIMDbEpisode) imdbEntry).getEpisodeNumber());
				
				//int episodeNumber = Integer.parseInt( + ((ModelIMDbEpisode) imdbEntry).getEpisodeNumber());
				((ModelEpisode) entry).setEpisodeKey(episodeNumber);
				
			} catch (NumberFormatException e) {
				log.warn("NumberFormatException:" + e.getMessage());
			}
		}
		
		System.err.println("1 " + ((ModelEpisode) entry).getEpisodeKey() + " data set");
		System.err.println("2 " + ((ModelEpisode) entry).getEpisodeKey() + " title:" + ((ModelEpisode) entry).getEpisodeTitle() );
		
	}
	
	
	 /* Alerts the user of different error messages from proxy servers*/
    void executeErrorMessage() {
       /*
        String exception = TVDOTCOM.getException();
        
        if (exception == null)
            return;
        
        if (exception.startsWith("Server returned HTTP response code: 407")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.authentication-requeried"), Localizer.getString("DialogTVDOTCOM.alert.message.proxy-server-requires-authentication")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection timed out")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.connection-timed.out"), Localizer.getString("DialogTVDOTCOM.alert.message.connection-timed.out")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Connection reset")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.connection-reset"), Localizer.getString("DialogTVDOTCOM.alert.message.connection-reset-by-server")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        
        if (exception.startsWith("Server redirected too many  times")) { //$NON-NLS-1$
            DialogAlert alert = new DialogAlert(this, Localizer.getString("DialogTVDOTCOM.alert.title.access-denied"), Localizer.getString("DialogTVDOTCOM.alert.message.username-or-password-invalid")); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showAndWait(alert, true);
        }
        */
    }
}
