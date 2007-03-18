/**
 * @(#)ModelAdditionalInfo.java 00.09.06 (dd.mm.yy)
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
import net.sf.xmm.moviemanager.DialogImportTable.FieldModel;

import java.util.ArrayList;

public class ModelAdditionalInfo {

	public static int additionalInfoFieldCount = 17;

	private int index = -1;

	private String subtitles = null;
	private int duration = 0;
	private int fileSize = 0;
	private int cds = 0;
	private double cdCases = 0;
	private String resolution = null;
	private String videoCodec = null;
	private String videoRate = null;
	private String videoBitrate = null;
	private String audioCodec = null;
	private String audioRate = null;
	private String audioBitrate = null;
	private String audioChannels = null;
	private String fileLocation = null;
	private int fileCount = 0;
	private String container = null;
	private String mediaType = null;

	protected static boolean hasOldExtraInfoFieldNames = true;
	static private ArrayList extraInfoFieldNames = null;
	private ArrayList extraInfoFieldValues = new ArrayList();

	public ModelAdditionalInfo() {

		if (extraInfoFieldNames != null) {
			for (int i = 0; i < extraInfoFieldNames.size(); i++)
				extraInfoFieldValues.add("");    
		}
	}

	/**
	 * The constructor.
	 **/
	public ModelAdditionalInfo(String subtitles, int duration, int fileSize, int cds, double cdCases, String resolution, String videoCodec, String videoRate, String videoBitrate, String audioCodec, String audioRate, String audioBitrate, String audioChannels, String fileLocation, int fileCount, String container, String mediaType) {

		this.subtitles = subtitles;
		this.duration = duration;
		this.fileSize = fileSize;
		this.cds = cds;
		this.cdCases = cdCases;
		this.resolution = resolution;
		this.videoCodec = videoCodec;
		this.videoRate = videoRate;
		this.videoBitrate = videoBitrate;
		this.audioCodec = audioCodec;
		this.audioRate = audioRate;
		this.audioBitrate = audioBitrate;
		this.audioChannels = audioChannels;
		this.fileLocation = fileLocation;
		this.fileCount = fileCount;
		this.container = container;
		this.mediaType = mediaType;

		if (extraInfoFieldNames != null) {
			for (int i = 0; i < extraInfoFieldNames.size(); i++)
				extraInfoFieldValues.add("");    
		}
	}


	public int getKey() {
		return index;
	}

	public String getSubtitles() {
		if (subtitles == null)
			return "";
		return subtitles;
	}

	public int getDuration() {
		return duration;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getCDs() {
		return cds;
	}

	public double getCDCases() {
		return cdCases;
	}

	public String getResolution() {
		if (resolution == null)
			return "";
		return resolution;
	}

	public String getVideoCodec() {
		if (videoCodec == null)
			return "";
		return videoCodec;
	}

	public String getVideoRate() {
		return videoRate;
	}

	public String getVideoBitrate() {
		if (videoBitrate == null)
			return "";
		return videoBitrate;
	}

	public String getAudioCodec() {
		if (audioCodec == null)
			return "";
		return audioCodec;
	}

	public String getAudioRate() {
		if (audioRate == null)
			return "";
		return audioRate;
	}

	public String getAudioBitrate() {
		if (audioBitrate == null)
			return "";
		return audioBitrate;
	}

	public String getAudioChannels() {
		if (audioChannels == null)
			return "";
		return audioChannels;
	}

	public String getFileLocation() {
		if (fileLocation == null)
			return "";
		return fileLocation;
	}

	public int getFileCount() {
		return fileCount;
	}

	public String getContainer() {
		if (container == null)
			return "";
		return container;
	}

	public String getMediaType() {
		if (mediaType == null)
			return "";
		return mediaType;
	}

	public String getExtraInfoFieldName(int index) {

		if (index >= extraInfoFieldNames.size())
			return "";
		else
			return (String) extraInfoFieldNames.get(index);
	}

	public String getExtraInfoFieldValue(int index) {

		if (index < 0 || index >= extraInfoFieldValues.size())
			return "";
		else
			return (String) extraInfoFieldValues.get(index);
	}

	public static ArrayList getExtraInfoFieldNames() {
		return extraInfoFieldNames;
	}

	public ArrayList getExtraInfoFieldValues() {
		return extraInfoFieldValues;
	}

	public static boolean hasOldExtraInfoFieldNames() {
		return hasOldExtraInfoFieldNames;
	}

	public static void setHasOldExtraInfoFieldNames(boolean hasOldExtraInfoFieldNames) {
		ModelAdditionalInfo.hasOldExtraInfoFieldNames = hasOldExtraInfoFieldNames;
	}

	public static void setExtraInfoFieldNames(ArrayList extraInfoFieldNames) {

		if (hasOldExtraInfoFieldNames) {
			ModelAdditionalInfo.extraInfoFieldNames = extraInfoFieldNames;
			hasOldExtraInfoFieldNames = false;
		}
	}

	public void setExtraInfoFieldValues(ArrayList extraInfoFieldValues) {
		 
		if (extraInfoFieldValues.size() !=  extraInfoFieldNames.size()) {
			
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 
		this.extraInfoFieldValues = extraInfoFieldValues;
	}

	public void setAudioBitrate(String audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	public void setAudioChannels(String audioChannels) {
		this.audioChannels = audioChannels;
	}

	public void setAudioCodec(String audioCodec) {
		this.audioCodec = audioCodec;
	}

	public void setAudioRate(String audioRate) {
		this.audioRate = audioRate;
	}

	public void setCDCases(double cdCases) {
		this.cdCases = cdCases;
	}

	public void setCDs(int cds) {
		this.cds = cds;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public void setSubtitles(String subtitles) {
		this.subtitles = subtitles;
	}

	public void setVideoBitrate(String videoBitrate) {
		this.videoBitrate = videoBitrate;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public void setVideoRate(String videoRate) {
		this.videoRate = videoRate;
	}        


	/* Convenience method for setting values */
	public boolean setValue(FieldModel fieldModel) {

		String fieldName = fieldModel.getField();
		String value = fieldModel.getValue();

		if (fieldModel.getTable().equals("Additional Info")) {

			if (fieldName.equalsIgnoreCase("SubTitles"))
				setSubtitles(value);
			else if (fieldName.equalsIgnoreCase("Duration"))
				setDuration(Integer.parseInt(value));
			else if (fieldName.equalsIgnoreCase("File Size"))
				setFileSize(Integer.parseInt(value));
			else if (fieldName.equalsIgnoreCase("CDs"))
				setCDs(Integer.parseInt(value));
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("CD Cases"))
				setCDCases(Integer.parseInt(value));
			else if (fieldName.equalsIgnoreCase("Resolution"))
				setResolution(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Video Codec"))
				setVideoCodec(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Video Rate"))
				setVideoRate(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Video Bit Rate"))
				setVideoBitrate(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Audio Codec"))
				setAudioCodec(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Audio Rate"))
				setAudioRate(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Audio Bit Rate"))
				setAudioBitrate(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Audio Channels"))
				setAudioChannels(value);
			else if (fieldName.equalsIgnoreCase("Container"))
				setContainer(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("File Location"))
				setFileLocation(value);
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("File Count"))
				setFileCount(Integer.parseInt(value));
			else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Media Type"))
				setMediaType(value);
			else
				return false;

			return true;
		}
		else if (fieldModel.getTable().equals("Extra Info")) {

			for (int i = 0; i < extraInfoFieldNames.size(); i++) {
				if (fieldName.equals(extraInfoFieldNames.get(i))) {
					extraInfoFieldValues.set(i, value);
					return true;
				}
			}
		}
		return false;
	}


	public String getAdditionalInfoString() {
		return getAdditionalInfoString(this);
	}


	/**
	 * Returns the additional_info string of this model
	 **/
	public static String getAdditionalInfoString(ModelAdditionalInfo model) {

		if (model == null)
			return "";

		//long time = System.currentTimeMillis();

		StringBuffer data = new StringBuffer("");

		try {
			/* Gets the fixed additional info... */

			int [] activeAdditionalInfoFields = MovieManager.getIt().getActiveAdditionalInfoFields();

			for (int i = 0; i < activeAdditionalInfoFields.length; i++) {

				switch (activeAdditionalInfoFields[i]) {

				case 0: {
					if (data.length() != 0)
						data.append("\n");
					data.append("Subtitles: ");

					data.append(model.getSubtitles());
					break;
				}

				case 1: {
					if (data.length() != 0)
						data.append("\n");
					data.append("Duration: ");

					int duration = model.getDuration();

					if (duration > 0) {

						int hours = duration / 3600;
						int mints = duration / 60 - hours * 60;
						int secds = duration - hours * 3600 - mints *60;

						data.append(hours + ":");

						if (mints < 10)
							data.append("0");
						data.append(mints + ":");

						if (secds < 10)
							data.append("0");
						data.append(secds);
					}
					break;
				}

				case 2: {
					if (data.length() != 0)
						data.append("\n");
					data.append("File Size: ");

					if (model.getFileSize() > 0) {
						data.append(model.getFileSize());
						data.append(" MB");
					}
					break;
				}

				case 3: {
					if (data.length() != 0)
						data.append("\n");
					data.append("CDs: ");

					if (model.getCDs() > 0)
						data.append(model.getCDs());
					break;
				}

				case 4: {
					if (data.length() != 0)
						data.append("\n");
					data.append("CD Cases: ");

					if (model.getCDCases() > 0)
						data.append(model.getCDCases());
					break;
				}

				case 5: {
					if (data.length() != 0)
						data.append("\n");
					data.append("Resolution: ");

					data.append(model.getResolution());
					break;
				}

				case 6: {
					if (data.length() != 0)
						data.append("\n");
					data.append("Video Codec: ");

					data.append(model.getVideoCodec());
					break;
				}

				case 7: {
					if (data.length() != 0)
						data.append("\n");
					data.append("Video Rate: ");

					if (!"".equals(model.getVideoRate())) {
						data.append(model.getVideoRate());
						data.append(" fps");
					}
					break;
				}

				case 8: {
					if (data.length() != 0)
						data.append("\n");

					data.append("Video Bit Rate: ");

					if (!"".equals(model.getVideoBitrate())) {
						data.append(model.getVideoBitrate());
						data.append(" kbps");
					}
					break;
				}

				case 9: {
					if (data.length() != 0)
						data.append("\n");

					data.append("Audio Codec: ");
					data.append(model.getAudioCodec());
					break;
				}

				case 10: {
					if (data.length() != 0)
						data.append("\n");

					data.append("Audio Rate: ");

					if (!"".equals(model.getAudioRate())) {
						data.append(model.getAudioRate());
						data.append(" Hz");
					}
					break;
				}

				case 11: {

					if (data.length() != 0)
						data.append("\n");

					data.append("Audio Bit Rate: ");

					if (!"".equals(model.getAudioBitrate())) {
						data.append(model.getAudioBitrate());
						data.append(" kbps");
					}
					break;
				}

				case 12: {

					if (data.length() != 0)
						data.append("\n");

					data.append("Audio Channels: ");
					data.append(model.getAudioChannels());

					break;
				}

				case 13: {

					if (data.length() != 0)
						data.append("\n");

					data.append("Location: ");
					data.append(model.getFileLocation());

					break;
				}

				case 14: {

					if (data.length() != 0)
						data.append("\n");

					data.append("File Count: ");
					if (model.getFileCount() > 0)
						data.append(String.valueOf(model.getFileCount()));
					break;
				}

				case 15: {

					if (data.length() != 0)
						data.append("\n");

					data.append("Container: ");
					data.append(model.getContainer());
					break;
				}

				case 16: {

					if (data.length() != 0)
						data.append("\n");

					data.append("Media Type: ");
					data.append(model.getMediaType());
					break;
				}

				default : {

					int columnIndex = activeAdditionalInfoFields[i]-17;

					if (data.length() != 0)
						data.append("\n");

					data.append(model.getExtraInfoFieldName(columnIndex) + ": ");
					data.append(model.getExtraInfoFieldValue(columnIndex));
				}	
				}
			}	

		} catch (Exception e) {
			MovieManager.log.error("Exception: ", e);
		}

		/* Returns the data... */
		return data.toString();
	}
}
