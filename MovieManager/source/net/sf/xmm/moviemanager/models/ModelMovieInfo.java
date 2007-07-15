/**
 * @(#)ModelMovieInfo.java 29.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.fileproperties.FilePropertiesMovie;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.ModelUpdatedEventListener;
import net.sf.xmm.moviemanager.util.ModelUpdatedHandler;
import net.sf.xmm.moviemanager.util.ModelUpdatedEvent.IllegalEventTypeException;

import org.apache.log4j.Logger;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class ModelMovieInfo {
    
    private ModelUpdatedHandler modelUpdatedHandler = new ModelUpdatedHandler();
    
    static Logger log = Logger.getRootLogger();
    
    public boolean _edit = false;
    private boolean _saveCover = false;
    public boolean _hasReadProperties = false;
    public boolean isEpisode = false;
    
    private int _lastFieldIndex = -1;
    public List _saveLastFieldValue = new ArrayList();
    private List _fieldNames = new ArrayList();
    private List _fieldValues = new ArrayList();
    
    public boolean saveAdditionalInfo = true;
    
    public int EXTRA_START = 17;
    
    /* Used when multiadding movies, if adding fileinfo
     to existing movie */
    private File multiAddFile; 
    
    public ModelEntry model;
    
    public ModelSeries modelSeries = null;
    
    /* Loading an empty episode model (adding episode) */
    public ModelMovieInfo(ModelSeries modelSeries) {
        this.modelSeries = modelSeries;
    	     	
        isEpisode = true;
        model = new ModelEpisode(modelSeries.getMovieKey());
        
        initializeAdditionalInfo(true);
	}
    
    
    /* Loading an empty model (movie or episode) */
    public ModelMovieInfo(boolean episode) {
    	this(episode, true);
    }
    
    /* Loading an empty model  (movie or episode) */
    public ModelMovieInfo(boolean episode, boolean loadEmptyAdditionalInfoFields) {
        
        isEpisode = episode;
        
        if (isEpisode)
            model = new ModelEpisode();
        else
            model = new ModelMovie();
        
        initializeAdditionalInfo(loadEmptyAdditionalInfoFields);
    }
    
    /* Initializes with the info from a model (Editing entry) */
    public ModelMovieInfo(ModelEntry model, boolean loadEmptyAdditionalInfoFields) {
        
        if (model instanceof ModelEpisode) {
            this.model = new ModelEpisode((ModelEpisode) model);
            isEpisode = true;
        }
        else {
            this.model = new ModelMovie((ModelMovie) model);
        }
        
        initializeAdditionalInfo(loadEmptyAdditionalInfoFields);
    }
    
    /* Edit a movie without additional info (special case) */
    public ModelMovieInfo(ModelMovie model) {
        _edit = true;
        this.model = model;
        saveAdditionalInfo = false;
    }
    
    
    public List getFieldNames() {
    	
    	if (model.getHasAdditionalInfoData());
    		model.updateAdditionalInfoData();
    	
    	return  _fieldNames;    	
    }
    
    public void setFieldNames(List fieldNames) {
    	_fieldNames = fieldNames;
    }
    
    public List getFieldValues() {
    	
    	if (model.getHasAdditionalInfoData());
			model.updateAdditionalInfoData();	
    	
    	return _fieldValues;
    }
    
    public void setFieldValues(List fieldValues) {
    	 _fieldValues = fieldValues;
    }
    
    public void addModelChangedEventListenener(ModelUpdatedEventListener listener) {
        modelUpdatedHandler.addModelChangedEventListenener(listener);
    }
    
    public void modelChanged(Object source, String type) throws IllegalEventTypeException {
        modelUpdatedHandler.modelChanged(source, type);
    }
    
    
    public void setLastFieldIndex(int index) {
    	_lastFieldIndex = index;
    }
    
    public int getLastFieldIndex() {
    	return _lastFieldIndex;
    }
    
    public Image getCoverImage() {
        
        Image image = null;
        
        try {
            
            if (model.getCoverData() != null) {
                image = Toolkit.getDefaultToolkit().createImage(model.getCoverData()).getScaledInstance(97,145, Image.SCALE_SMOOTH);
            }
            else if (MovieManager.getIt().getDatabase().getDatabaseType().equals("MySQL") && 
                    !MovieManager.getConfig().getStoreCoversLocally()) {
                
                model.updateCoverData();
                byte [] coverData = model.getCoverData();
                
                if (coverData != null)
                    image = Toolkit.getDefaultToolkit().createImage(model.getCoverData());
                else
                    log.warn("Cover data not available."); //$NON-NLS-1$
            }
            else if (!model.getCover().equals("")) {
                
                File cover = new File(MovieManager.getConfig().getCoversPath(), model.getCover());
                
                if ((cover.exists())) {
                    /* Loads the image...*/
                    image = FileUtil.getImage(cover.getAbsolutePath()).getScaledInstance(97,145, Image.SCALE_SMOOTH);
                }
            }
            
            if (image == null) {
                image = FileUtil.getImage("/images/" + MovieManager.getConfig().getNoCover()).getScaledInstance(97,97,Image.SCALE_SMOOTH); //$NON-NLS-1$
            }
        } catch (Exception e) {
            log.error("", e); //$NON-NLS-1$
        }
        
        if (image == null) 
            log.warn("Cover file not found."); //$NON-NLS-1$
        
        return image;
    }
    
    public Image getSeenImage() {
        
        Image image = null;
        
        if (model.getSeen()) 
            image = FileUtil.getImage("/images/seen.png"); //$NON-NLS-1$
        else
            image = FileUtil.getImage("/images/unseen.png"); //$NON-NLS-1$
        
        return image.getScaledInstance(18,18,Image.SCALE_SMOOTH);    
    }
    
    
    /*Used when multiadding*/
    public File getMultiAddFile() {
        return multiAddFile;
    }
    
    /*Used when multiadding*/
    public void setMultiAddFile(File multiAddFile) {
        this.multiAddFile = multiAddFile;
    }
    
    public void setCover(String cover, byte[] coverData) {
    	model.setCover(cover);
        model.setCoverData(coverData);
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }	
    
    public void setTitle(String title) {
        model.setTitle(title);
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void setSeen(boolean seen) {
        model.setSeen(seen);
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void setGeneralInfoFieldsEmpty() {
       
    	model.setKey(-1);
        model.setUrlKey("");
        model.setCover("");
        model.setCoverData(null);
        model.setDate("");
        model.setDirectedBy("");
        model.setWrittenBy("");
        model.setGenre("");
        model.setRating("");
        model.setPlot("");
        model.setCast("");
        model.setNotes("");
        model.setSeen(false);
        model.setAka("");
        model.setCountry("");
        model.setLanguage("");
        model.setColour("");
        model.setMpaa("");
        model.setCertification("");
        model.setWebSoundMix("");
        model.setWebRuntime("");
        model.setAwards("");
        
        if (isEpisode) {
            ((ModelEpisode) model).setMovieKey(-1);
            ((ModelEpisode) model).setEpisodeNumber(-1);
        }
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    /**
     * Empties all the additional fields values stored in the _fieldValues arrayList
     **/
    public void setAdditionalInfoFieldsEmpty() {
        
        try {
            for (int i = 0; i < _fieldValues.size(); i++)
                _fieldValues.set(i,""); //$NON-NLS-1$
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
            e.printStackTrace();
            _hasReadProperties = false;
        }
        
        try {
        	modelChanged(this, "AdditionalInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void saveAdditionalInfoData() {
        
    	if (!saveAdditionalInfo)
    		return;
    	
        ModelAdditionalInfo additionalInfo = model.getAdditionalInfo();
        
        /* Sets the additional info... */
        
        // Duration
        if (!((String) _fieldValues.get(1)).equals("")) { //$NON-NLS-1$
            additionalInfo.setDuration(Integer.parseInt((String) _fieldValues.get(1)));
        }
        
        // file size
        if (!((String) _fieldValues.get(2)).equals("")) { //$NON-NLS-1$
            additionalInfo.setFileSize(Integer.parseInt((String) _fieldValues.get(2)));
        }
        
        // Cds
        if (!((String) _fieldValues.get(3)).equals("")) { //$NON-NLS-1$
            additionalInfo.setCDs(Integer.parseInt((String) _fieldValues.get(3)));
        }	 
        
        // CD cases
        if (!((String) _fieldValues.get(4)).equals("")) { //$NON-NLS-1$
            additionalInfo.setCDCases(Double.parseDouble((String) _fieldValues.get(4)));
        }
        
        // File count
        if (!((String) _fieldValues.get(14)).equals("")) { //$NON-NLS-1$
            additionalInfo.setFileCount(Integer.parseInt((String) _fieldValues.get(14)));
            
        }
		
        additionalInfo.setSubtitles((String) _fieldValues.get(0));
        additionalInfo.setResolution((String) _fieldValues.get(5));
        additionalInfo.setVideoCodec((String) _fieldValues.get(6));
        additionalInfo.setVideoRate((String) _fieldValues.get(7));
        additionalInfo.setVideoBitrate((String) _fieldValues.get(8));
        additionalInfo.setAudioCodec((String) _fieldValues.get(9));
        additionalInfo.setAudioRate((String) _fieldValues.get(10));
        additionalInfo.setAudioBitrate((String) _fieldValues.get(11));
        additionalInfo.setAudioChannels((String) _fieldValues.get(12));
        additionalInfo.setFileLocation((String) _fieldValues.get(13));
        
        additionalInfo.setContainer((String) _fieldValues.get(15));
        additionalInfo.setMediaType((String) _fieldValues.get(16));
        
        
        ArrayList extraFieldValuesList = new ArrayList();
        
        for (int i = EXTRA_START; i <  _fieldNames.size(); i++) {
            extraFieldValuesList.add( _fieldValues.get(i));
        }
        additionalInfo.setExtraInfoFieldValues(extraFieldValuesList);
        
    }
    
    
    public ModelEntry saveToDatabase(String listName) throws Exception {
        
        saveAdditionalInfoData();
        
        Database database = MovieManager.getIt().getDatabase();
        ModelAdditionalInfo additionalInfo = model.getAdditionalInfo();
        
        if (isEpisode) {
            
            if (_edit) {
                /* Editing episode */
                
                database.setGeneralInfoEpisode(model.getKey(), (ModelEpisode) model);
                database.setAdditionalInfoEpisode(model.getKey(), additionalInfo);
                	
                //Must save to extra info even though there are no extra info fields. Tge rows must still be created in the database
                database.setExtraInfoEpisode(model.getKey(), ModelAdditionalInfo.getExtraInfoFieldNames(), additionalInfo.getExtraInfoFieldValues());
               
            } else {
                
                if (((ModelEpisode) model).getMovieKey() == -1)
                    throw new Exception("Cannot add episode with MovieKey: -1");
                
                /* Adding episode */
                
                int episodeindex = database.addGeneralInfoEpisode((ModelEpisode) model);
                 
                if (episodeindex != -1) {
                    
                    int ret;
                    
                    ((ModelEpisode) model).setKey(episodeindex);
                    
                    /* Adds the additional info... */
                    ret = database.addAdditionalInfoEpisode(episodeindex, additionalInfo);
                     
                    //Must save to extra info even though there are no extra info fields. Tge rows must still be created in the database
                    ret = database.addExtraInfoEpisode(episodeindex, ModelAdditionalInfo.getExtraInfoFieldNames(), additionalInfo.getExtraInfoFieldValues());
                      
                }
            }
        } else {
            
            if (_edit) {
                
                /* Editing movie */
                
                database.setGeneralInfo((ModelMovie) model);
                
                if (saveAdditionalInfo) {
                    database.setAdditionalInfo(model.getKey(), additionalInfo);
                    
					// Must save to extra info even though there are no extra info fields. Tge rows must still be created in the database
					database.setExtraInfoMovie(model.getKey(), ModelAdditionalInfo.getExtraInfoFieldNames(), additionalInfo.getExtraInfoFieldValues());
				}
                
            } else {
                /* Adding new movie */
                
                int index = MovieManager.getIt().getDatabase().addGeneralInfo((ModelMovie) model);
                
                model.setKey(index);
                
                if (index != -1) {
                    
                    database.addAdditionalInfo(model.getKey(), additionalInfo);
                    
					// Must save to extra info even though there are no extra info fields. The rows must still be created in the database
					database.addExtraInfoMovie(model.getKey(), ModelAdditionalInfo.getExtraInfoFieldNames(), additionalInfo.getExtraInfoFieldValues());
                    
                    
                    /* Add new row in Lists table with default values */
                    ArrayList listNames = database.getListsColumnNames();
                    ArrayList listValues = new ArrayList();
                    
                    String applyToThisList = ""; //$NON-NLS-1$
                    
                    if (listName != null)
                        applyToThisList = listName;
                    
                    for (int i = 0; i < listNames.size(); i++) {
                        if (listNames.get(i).equals(applyToThisList))
                            listValues.add(new Boolean(true));
                        else
                            listValues.add(new Boolean(false));
                    }
                    database.addLists(model.getKey(), listNames, listValues);
                }
            }
        }
        
        return model;
    }
    
    
    /**
     * Sets _saveCover.
     **/
    public void setSaveCover(boolean saveCover) {
        
        if (!((MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) && !MovieManager.getConfig().getStoreCoversLocally()))
            _saveCover = saveCover;
        else
            _saveCover = false;
    }
    
    /**
     * Gets the file properties...
     **/
    public void getFileInfo(File [] file) {
        
        for (int i = 0; i < file.length; i++) {
            
            if (!file[i].isFile())
                continue;
            
            try {
                /* Reads the info... */
            	FilePropertiesMovie properties = new FilePropertiesMovie(file[i].getAbsolutePath(), MovieManager.getConfig().getUseMediaInfoDLL());
                
                getFileInfo(properties);
                
            } catch (Exception e) {
                log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
            }
        }
    }    
    
    
    public void getFileInfo(FilePropertiesMovie properties) {
        
        try {
            
            if (!properties.getSubtitles().equals("")) //$NON-NLS-1$
                _fieldValues.set(0, properties.getSubtitles());
            
            /* Saves info... */
            int duration = properties.getDuration();
             
            if (_hasReadProperties && duration != -1 && !((String) _fieldValues.get(1)).equals("")) { //$NON-NLS-1$
            	duration += Integer.parseInt((String) _fieldValues.get(1));
            }
            
            if (duration != -1) {
                _fieldValues.set(1, String.valueOf(duration));
                _saveLastFieldValue.set(1, new Boolean(false));
            } else {
                _fieldValues.set(1, ""); //$NON-NLS-1$
            }
            
            int fileSize = properties.getFileSize();
            
            if (_hasReadProperties && fileSize != -1 && !((String) _fieldValues.get(2)).equals("")) { //$NON-NLS-1$
                fileSize += Integer.parseInt((String) _fieldValues.get(2));
            }
            
            if (fileSize != -1) {
                _fieldValues.set(2,String.valueOf(fileSize));
                _saveLastFieldValue.set(2, new Boolean(false));
            } else {
                _fieldValues.set(2,""); //$NON-NLS-1$
            }
            
            if (fileSize != -1) {
                int cds = (int)Math.ceil(fileSize / 702.0);
                _fieldValues.set(3,String.valueOf(cds));
                _saveLastFieldValue.set(3, new Boolean(false));
            }
            
            if (((String) _fieldValues.get(4)).equals("")) { //$NON-NLS-1$
                _fieldValues.set(4,String.valueOf(1));
                _saveLastFieldValue.set(4, new Boolean(false));
            }
            
            _fieldValues.set(5,properties.getVideoResolution());
            _fieldValues.set(6,properties.getVideoCodec());
            _fieldValues.set(7,properties.getVideoRate());
            _fieldValues.set(8,properties.getVideoBitrate());
            _fieldValues.set(9,properties.getAudioCodec());
            _fieldValues.set(10,properties.getAudioRate());
            _fieldValues.set(11,properties.getAudioBitrate());
            
            String audioChannels = properties.getAudioChannels();
            _fieldValues.set(12, audioChannels);
            
             String location = properties.getLocation();

             String currentValue = (String) _fieldValues.get(13);

             if (!currentValue.equals("") && _hasReadProperties) {

            	 StringTokenizer tokenizer = new StringTokenizer(currentValue, "*"); //$NON-NLS-1$
            	 boolean fileAlreadyAdded = false;

            	 while (tokenizer.hasMoreTokens()) {
            		 if (tokenizer.nextToken().equals(location))
            			 fileAlreadyAdded = true;
            	 }

            	 if (fileAlreadyAdded)
            		 log.warn("file already added"); //$NON-NLS-1$
            	 else {
            		 currentValue += "*" + location; //$NON-NLS-1$
            		 _fieldValues.set(13, currentValue);
            	 }
             }
             else 
            	 _fieldValues.set(13, location);

             
            int fileCount = 1;
            if (_hasReadProperties && !((String) _fieldValues.get(14)).equals("")) { //$NON-NLS-1$
                fileCount += Integer.parseInt((String) _fieldValues.get(14));
            }
            
            _fieldValues.set(14, String.valueOf(fileCount));
            
            _fieldValues.set(15, properties.getContainer());
            
            _fieldValues.set(16, model.getAdditionalInfo().getMediaType());
            
            /* Sets title if title field is empty... */
            if (model.getTitle().equals("")) { //$NON-NLS-1$
                
                if (!properties.getMetaDataTagInfo("INAM").equals("")) //$NON-NLS-1$ //$NON-NLS-2$
                    setTitle(properties.getMetaDataTagInfo("INAM")); //$NON-NLS-1$
                
                else {
                    String title = properties.getFileName();
                    
                    if (title.lastIndexOf(".") != -1) //$NON-NLS-1$
                        title = title.substring(0, title.lastIndexOf('.'));
                    
                    setTitle(title);
                }
            }
            
            if (_lastFieldIndex != -1)
            /* The value currently in the selected index will not be saved, but be replaced by the new info */
            _saveLastFieldValue.set(_lastFieldIndex, new Boolean(false).toString());
            
            for (int u = 0; u < _saveLastFieldValue.size(); u++) {
                _saveLastFieldValue.set(u, new Boolean(true));
            }
            
            _hasReadProperties = true;
            
            modelChanged(this, "AdditionalInfo");
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage(), e); //$NON-NLS-1$
        }
        
    }
    
    
    public void initializeAdditionalInfo(boolean loadEmpty) {
       
        ModelAdditionalInfo additionalInfo = null;
        
        _saveLastFieldValue = new ArrayList();
        _fieldNames = new ArrayList();
        _fieldValues = new ArrayList();
        
        if (!loadEmpty) {
                   	
            if (!model.getHasAdditionalInfoData())
                model.updateAdditionalInfoData();
            
            additionalInfo = model.getAdditionalInfo();
        }   
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Subtitles"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getSubtitles());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Duration"); //$NON-NLS-1$
        
        
        if (!loadEmpty && additionalInfo.getDuration() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getDuration()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
         
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("File Size"); //$NON-NLS-1$
        
	   
        if (!loadEmpty && additionalInfo.getFileSize() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getFileSize()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("CDs"); //$NON-NLS-1$
        
        if (!loadEmpty && additionalInfo.getCDs() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getCDs()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("CD Cases"); //$NON-NLS-1$
        
        if (!loadEmpty && additionalInfo.getCDCases() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getCDCases()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Resolution"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getResolution());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Video Codec"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getVideoCodec());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Video Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getVideoRate());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Video Bit Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getVideoBitrate());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Audio Codec"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getAudioCodec());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Audio Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getAudioRate());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Audio Bit Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getAudioBitrate());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Audio Channels"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getAudioChannels());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Location"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getFileLocation());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("File Count"); //$NON-NLS-1$
        
        
        if (!loadEmpty && additionalInfo.getFileCount() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getFileCount()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Container"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getContainer());
        
        _saveLastFieldValue.add(new Boolean(true));
        _fieldNames.add("Media Type"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else
            _fieldValues.add(additionalInfo.getMediaType());
        
        
        ArrayList extraFieldNames = ModelAdditionalInfo.getExtraInfoFieldNames();
        ArrayList extraFieldValues = null;
        
        if (!loadEmpty)
            extraFieldValues = additionalInfo.getExtraInfoFieldValues();
       
        
        for (int i = 0; i < extraFieldNames.size(); i++) {
            
            _fieldNames.add(extraFieldNames.get(i));
            
            if (loadEmpty)
                _fieldValues.add("");
            else
                _fieldValues.add(extraFieldValues.get(i));
        }
        
        for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
            _saveLastFieldValue.add(new Boolean(true));
            
            if (loadEmpty) {
                _fieldValues.add("");
            }
            else {
                if (isEpisode)
                    _fieldValues.add(MovieManager.getIt().getDatabase().getExtraInfoEpisodeField(model.getKey(),(String)_fieldNames.get(i)));
                else
                    _fieldValues.add(MovieManager.getIt().getDatabase().getExtraInfoMovieField(model.getKey(),(String)_fieldNames.get(i)));
            }        
        }
    }
    
    
    
    public boolean saveCoverToFile() throws Exception {
       	
		if (!_saveCover)
			return false;
	
        byte [] cover = model.getCoverData();
        String coverName = model.getCover();
        
        if (cover == null || coverName == null || coverName.equals("")) {
        	throw new Exception("Unable to save cover file:" + coverName);
        }
        
        /* Saves the cover... */      
        
        String coversFolder = MovieManager.getConfig().getCoversPath();
        File coverFile = new File(coversFolder, coverName);
        
        if (coverFile.exists()) {
            if (!coverFile.delete() && !coverFile.createNewFile()) {
                throw new Exception("Cannot delete old cover file and create a new one."); //$NON-NLS-1$
            }
        } else {
            if (!coverFile.createNewFile()) {
                throw new Exception("Cannot create cover file:" + coverFile.getAbsolutePath()); //$NON-NLS-1$
            }
        }
        
        /* Copies the cover to the covers folder... */
        OutputStream outputStream = new FileOutputStream(coverFile);
        outputStream.write(cover);
        outputStream.close();
        
        return true;
    }
    

    /**
     * It's hard to understand everything going on in this method. I'm not even sure I understand it myself.
     * makes changes to the aka titles, and/or the main title according to the settings i the preferences.
     * 
     * @param originalTitle
     */
    public void executeTitleModification(String originalTitle) {

    	boolean removeDuplicates = !MovieManager.getConfig().getIncludeAkaLanguageCodes();

    	ArrayList akaKeys = new ArrayList();
    	ArrayList akaValues = new ArrayList();

    	String akaTitles = model.getAka();
    	String newAkaTitles = "";

    	StringTokenizer tokenizer = new StringTokenizer(akaTitles, "\r\n", false);

    	String value;
    	String tmp;
    	String key = "";
    	int index = 0;
    	String languageCode = MovieManager.getConfig().getTitleLanguageCode();

    	while (tokenizer.hasMoreTokens()) {

    		value = tokenizer.nextToken();
    		key = MovieManagerCommandAddMultipleMoviesByFile.performExcludeParantheses(value, false).trim();

    		if (MovieManager.getConfig().getUseLanguageSpecificTitle() && value.indexOf("[" + languageCode + "]") != -1) {

    			if (MovieManager.getConfig().getIncludeAkaLanguageCodes()) {
    				akaKeys.add(0, model.getTitle());
    				akaValues.add(0, originalTitle + " (Original)");
    			}
    			else if (removeDuplicates && akaKeys.indexOf(model.getTitle()) != -1) {
    				
    				akaKeys.remove(model.getTitle());
    				akaValues.remove(model.getTitle());
    				
    				akaKeys.add(0, model.getTitle());
    				akaValues.add(0, model.getTitle());
    			}
    			else  {
    				akaKeys.add(0, originalTitle);	
    				akaValues.add(0, originalTitle);
    			}	
    			setTitle(key);
    		}

    		boolean allAkaTitles = MovieManager.getConfig().getStoreAllAkaTitles();

    		if (!(!allAkaTitles && value.indexOf("[") != -1) || removeDuplicates) {

    			index = akaKeys.indexOf(key);
		
    			//  Adds the language code to the existing title 
    			if (removeDuplicates && index != -1) {

    				if (value.indexOf("[") != -1) {
    					value =  value.replaceFirst(key, "");
    					tmp = akaValues.get(index) + " " + value.trim();
    					akaValues.set(index, tmp);
    				}   
    			}
    			else if (!(!allAkaTitles && value.indexOf("[") != -1)) {
    				
    				/* Removes comments and language code */
    				if (!MovieManager.getConfig().getIncludeAkaLanguageCodes()) {
    					value = key;
    				}
    					
    				if (!value.equals(model.getTitle())) {
    					akaKeys.add(key);
    					akaValues.add(value);
    				}
    				
    			}
    		}
    	}

    	while (!akaValues.isEmpty()) {
    		newAkaTitles += akaValues.remove(0) + "\r\n";
    	}   

    	model.setAka(newAkaTitles.trim());

    	try {
    		modelChanged(this, "GeneralInfo");
    	} catch (IllegalEventTypeException e) {
    		log.error("IllegalEventTypeException:" + e.getMessage());
    	}

    }

    
    public void clearModel() {
    	clearModel(false);
    }
    
    /**
     * *
     * * @param addNewEpisode     true is a new episode will be added
     */
    public void clearModel(boolean addNewEpisode) {
		
        if (isEpisode) {
			
        	if (addNewEpisode && !_edit) {
        		model = new ModelEpisode(modelSeries.getMovieKey());
        		model.setTitle(modelSeries.getMovie().getTitle());
        	}
        	else
        		model = new ModelEpisode();
        }
        else
            model = new ModelMovie();
               
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void setModel(ModelEntry model, boolean copyKey, boolean modelChanged) {
        
    	if (model.isEpisode()) {
			
            if (copyKey) {
            	if (_edit)
            		((ModelEpisode) model).setKey(this.model.getKey());
            	else
            		((ModelEpisode) model).setMovieKey(modelSeries.getMovieKey());
            }
            isEpisode = true;
        }
        else
            isEpisode = false;
        
        this.model = model;
                
        initializeAdditionalInfo(false);
	
        if (modelChanged) {
        	try {
            	modelChanged(this, "GeneralInfo");
            } catch (IllegalEventTypeException e) {
            	log.error("IllegalEventTypeException:" + e.getMessage());
            }
        }
    }
}
