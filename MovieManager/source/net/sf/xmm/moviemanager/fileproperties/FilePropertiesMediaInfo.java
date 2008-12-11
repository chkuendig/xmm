/**
 * @(#)FilePropertiesMediaInfo.java 1.0 26.01.06 (dd.mm.yy)
 *
 * Copyright (2003) bro3
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License aloSng with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.fileproperties;

import java.io.File;
import java.io.RandomAccessFile;

import net.sf.xmm.moviemanager.mediainfodll.MediaInfo;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.LibPathHacker;

class FilePropertiesMediaInfo extends FileProperties {
    
    private String filePath;
    
    FilePropertiesMediaInfo(String filePath) throws Exception {
	this.filePath = filePath;
	
 	File jnative = new File((FileUtil.getFile("lib\\JNativeCpp.dll")).getPath());
	File mediaInfo = new File((FileUtil.getFile("lib\\MediaInfo.dll")).getPath());
	
	if (jnative.exists() && mediaInfo.exists()) {
	    LibPathHacker.addDir(FileUtil.getFile("lib").getAbsolutePath());
	    System.load(mediaInfo.getAbsolutePath());
	}
	else {
	    String error = "";
	    
	    if (!jnative.exists())
		error += "JNativeCpp.dll";
	    
	    if (!mediaInfo.exists()) {
		
		if (!error.equals(""))
		    error += ", ";
		
		error += "MediaInfo.dll";
	    }
	    
	    error = "Following libraries are missing:" + error;
	    
	    throw new Exception(error);
	}
    }
    
    protected void process(RandomAccessFile nodeUsed) {
	
	try {
	    
	    MediaInfo mi = new MediaInfo();
	    
	    int open = mi.Open(filePath);
	    String tmp;
	 
	    if (open > 0) {
	    	
	    	supported = true;
	    	
		String audioCodec = "";
		String audioChannels = "";
		String audioRate = "";
		String audioBitrate = "";
    
		setContainer(mi.Get(MediaInfo.Stream_General, 0, "Format", MediaInfo.Info_Text, MediaInfo.Info_Name));
		
		String dur = mi.Get(0, 0, "PlayTime", 1, 0);
		
		if (!"".equals(dur))
		    setDuration((int) (Double.parseDouble(dur)/1000));
		
		String codecName = mi.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Text, MediaInfo.Info_Name);
		
		codecName  = findName(FileUtil.getResourceAsStream("/codecs/FOURCCvideo.txt"), codecName);
		
		String library = mi.Get(MediaInfo.Stream_Video, 0, "Encoded_Library", MediaInfo.Info_Text, MediaInfo.Info_Name);
		
		if (!library.equals("")) {
		    tmp = findName(FileUtil.getResourceAsStream("/codecs/videoExtended.txt"), library);
		    
		    if (!tmp.equals("")) {
			codecName = tmp;
		    }
		}
		
		setVideoCodec(codecName);
		
		String vBitrate = mi.Get(MediaInfo.Stream_Video, 0, "BitRate", MediaInfo.Info_Text, MediaInfo.Info_Name);
		
		if (vBitrate.length() > 2)
		    setVideoBitrate(vBitrate.substring(0, vBitrate.length() - 3));
		
		setVideoResolution(mi.Get(MediaInfo.Stream_Video, 0, "Width", MediaInfo.Info_Text, MediaInfo.Info_Name) + "x" + mi.Get(MediaInfo.Stream_Video, 0, "Height", MediaInfo.Info_Text, MediaInfo.Info_Name));
		
		setVideoRate(mi.Get(MediaInfo.Stream_Video, 0, "FrameRate", MediaInfo.Info_Text, MediaInfo.Info_Name));
		
		tmp = mi.Get(MediaInfo.Stream_Audio, 0, "StreamCount", MediaInfo.Info_Text, MediaInfo.Info_Name);
		
		int audioCount = 0;
		
		if (!"".equals(tmp))
		    audioCount = Integer.parseInt(tmp);
		
		log.debug("audioCount:" + audioCount);
		
		for (int i = 0; i < audioCount; i++) {
		    
		    if (!audioCodec.equals(""))
			audioCodec += ", ";
			
		    String value = mi.Get(MediaInfo.Stream_Audio, i, "Codec", MediaInfo.Info_Text, MediaInfo.Info_Name);
		    
		    log.debug("value:" + value);

		    if (!value.equals("")) {
			StringBuffer buffer = new StringBuffer("0x");
			int u = 4 - value.length();
			while (u-- > 0) {
			    buffer.append('0');
			}
			buffer.append(value);
			
			value = findName(FileUtil.getResourceAsStream("/codecs/FOURCCaudio.txt"), buffer.toString());
		    }
		    
		    if (!value.equals(""))
			audioCodec += value;
		    else
			audioCodec += mi.Get(MediaInfo.Stream_Audio, i, "Codec/String", MediaInfo.Info_Text, MediaInfo.Info_Name);
		    
		    
		    if (!audioBitrate.equals(""))
			audioBitrate += ", ";
		    
		    String aBitrate = mi.Get(MediaInfo.Stream_Audio, i, "BitRate", MediaInfo.Info_Text, MediaInfo.Info_Name);
		    
		    if (aBitrate.length() > 2)
			audioBitrate += aBitrate.substring(0, aBitrate.length() - 3);
		    
		    if (!audioChannels.equals(""))
			audioChannels += ", ";
		    
		    audioChannels += mi.Get(MediaInfo.Stream_Audio, i, "Channel(s)", MediaInfo.Info_Text, MediaInfo.Info_Name);
		    
		    if (!audioRate.equals(""))
			audioRate += ", ";
		    
		    audioRate += mi.Get(MediaInfo.Stream_Audio, i, "SamplingRate", MediaInfo.Info_Text, MediaInfo.Info_Name);
		    
		}
		
		setAudioCodec(audioCodec);
		/* Sets the audio channels... */
		setAudioChannels(audioChannels);
		/* Sets the audio rate... */
		setAudioRate(audioRate);
		/* Sets the audio bit rate... */
		setAudioBitrate(audioBitrate);

		mi.Close();
	    }
	    
	    mi.Delete();
	    
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	}
    }
}
