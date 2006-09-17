/**
 * @(#)MovieManagerCommandExportToFullHTML.java 1.0 26.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.DialogAlert;
import net.sf.xmm.moviemanager.DialogQuestion;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.models.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

public class MovieManagerCommandExportToFullHTML {

    static Logger log = Logger.getRootLogger();
    
    static ArrayList startChar;
    static boolean divideAlphebetically;
    static String title;
    static DefaultListModel listModel;
    
    public MovieManagerCommandExportToFullHTML(boolean _divideAlphebetically, String _title, DefaultListModel _listModel) {
	divideAlphebetically = _divideAlphebetically;
	title = _title;
	listModel = _listModel;
    }
    
    /**
     * Exports the content of the database to html...
     **/
    protected static void export(File htmlFile, String coversPath, String coversRelativePath) {
	
	startChar = new ArrayList();
	char lastChar = ' ';
	int counter = 0;
	String filepath = htmlFile.getAbsolutePath();
	filepath = filepath.substring(0, filepath.lastIndexOf("."));
	
	if (divideAlphebetically) {
	    for (int i = 0; i < listModel.getSize(); i++) {
		
		if (Character.toUpperCase(((ModelMovie)listModel.elementAt(i)).getTitle().charAt(0)) != lastChar) {
		    
		    lastChar = Character.toUpperCase((((ModelMovie)listModel.elementAt(i)).getTitle().charAt(0)));
		    
		    if (lastChar >= 'A' && lastChar <= 'Z') {
			startChar.add(Character.toString(lastChar));
		    }
		    else if(i == 0) {
			startChar.add("");
		    }
		}
	    }
	}
	
	try {
	    
	    /* Creates the movielist file... */
	    FileWriter writer;
	    
	    if (divideAlphebetically) {
		if (!startChar.get(0).equals("")) {
		    
		    String path = filepath;
		    
		    path += "."+ startChar.get(0)+".html";
		    
		    htmlFile = new File(path);
		}
	    }
	    
	    writer = new FileWriter(htmlFile);
	    
	    /* The html header... */
	    writer = writeStart(filepath, writer, counter);
	    counter++;
	    
	    /* Used vars... */
	    String imdb = "imdb";
	    String cover = "cover";
	    String nocover = MovieManager.getConfig().getNoCover();
	    String rating = "rating";
	    String title = "title";
	    String duration = "duration";
	    String fileSize = "fileSize";
	    String cds = "cds";
	    String cdCases = "cdCases";
	    String vCodec = "vCodec";
	    String vRate = "vRate";
	    String vBitrate = "vBitrate";
	    String vResolution = "vResolution";
	    String aCodec = "aCodec";
	    String aRate = "aRate";
	    String aBitrate = "aBitrate";
	    String aChannels = "aChannels";
	    String plot = "plot";
	    String coversDBFolder;
	    coversDBFolder = MovieManager.getConfig().getCoversFolder();
	    
	    String lastStart = null;
	    
	    if (!coversDBFolder.endsWith(MovieManager.getDirSeparator()))
		coversDBFolder = coversDBFolder + MovieManager.getDirSeparator();
	    
	    File coverInputFile;
	    FileInputStream coverInputStream;
	    File coverOutputFile;
	    FileOutputStream coverOutputStream;
	    byte[] buffer;
	    int tempInt;
	    double tempDouble;
	    int coverHeight = 0;
	    
	    /* Creates the nocover file... */
	    try {
		coverOutputFile = new File(coversPath+ MovieManager.getDirSeparator() + MovieManager.getConfig().getNoCover());
		if (!coverOutputFile.createNewFile()) {
		    coverOutputFile.delete();
		}
		coverOutputStream = new FileOutputStream(coverOutputFile);
		buffer = MovieManager.getIt().getResourceAsByteArray("images/"+ MovieManager.getConfig().getNoCover());
		coverOutputStream.write(buffer);
		coverOutputStream.close();
	    
	    } catch (Exception e) {
		log.error("Exception: " + e);
	    }
	    
	    /* For each movie.... */
	    
	    for (int i = 0; i < listModel.getSize(); i++) {
		
		ModelMovie movie = (ModelMovie) listModel.elementAt(i);
		
		if (!movie.getHasGeneralInfoData())
		    movie.updateGeneralInfoData();
		
		//int index = ((ModelMovie)listModel.elementAt(i)).getKey();
		
		ModelAdditionalInfo additionalInfo = MovieManager.getIt().getDatabase().getAdditionalInfo(movie.getKey(), false);
		
		imdb = movie.getUrlKey();
		
		try {
		    cover = movie.getCover();
		    if (!cover.equals("")) {
			
			/* Verifies that the input file exists... */
			coverInputFile = new File(coversDBFolder+cover);
			
			if (!coverInputFile.exists()) {
			    throw new Exception("Cover file not found:" + coverInputFile.getAbsolutePath());
			}
			
			/* Creates the output file... */
			coverOutputFile = new File(coversPath + MovieManager.getDirSeparator() + cover);
			if (!coverOutputFile.createNewFile()) {
			    coverOutputFile.delete();
			}
			/* Copies the image...*/
			coverInputStream = new FileInputStream(coverInputFile);
			coverOutputStream = new FileOutputStream(coverOutputFile);
			buffer = new byte[coverInputStream.available()];
			coverInputStream.read(buffer);
			coverOutputStream.write(buffer);
			coverInputStream.close();
			coverOutputStream.close();
			
			coverHeight = 145;
		    } else {
			cover = nocover;
			coverHeight = 97;
		    }
		} catch (Exception e) {
		    log.error("Exception: " + e.getMessage());
		    cover = MovieManager.getConfig().getNoCover();
		}
		rating = movie.getRating();
		if (rating.equals("")) {
		    rating = "n/a";
		}
		
		title = movie.getTitle();
		//title = MovieManager.getIt().getDatabase().getMovieTitle(index)+" ("+MovieManager.getIt().getDatabase().getDate(index)+")";
		tempInt = additionalInfo.getDuration();
		
		if(tempInt!=-1) {
		    int hours = tempInt / 3600;
		    int mints = tempInt / 60 - hours * 60;
		    int secds = tempInt - hours * 3600 - mints *60;
		    duration = hours + ":" + mints + "." + secds;
		} else {
		    duration = "n/a";
		}
		tempInt = additionalInfo.getFileSize();
		if (tempInt!=-1) {
		    fileSize = tempInt + " MB";
		} else {
		    fileSize = "n/a";
		}
		tempInt = additionalInfo.getCDs();
		if (tempInt!=-1) {
		    cds = "" + tempInt;
		} else {
		    cds = "n/a";
		}
		tempDouble = additionalInfo.getCDCases();
		if (tempDouble!=-1) {
		    cdCases = "" + tempDouble;
		} else {
		    cdCases = "n/a";
		}
		vCodec = additionalInfo.getVideoCodec();
		if (vCodec.equals("")) {
		    vCodec = "n/a";
		}
		vRate = additionalInfo.getVideoRate();
		if (!vRate.equals("")) {
		    vRate = vRate + " fps";
		} else {
		    vRate = "n/a";
		}
		vBitrate = additionalInfo.getVideoBitrate();
		if (!vBitrate.equals("")) {
		    vBitrate = vBitrate + " kbps";
		} else {
		    vBitrate = "n/a";
		}
		vResolution = additionalInfo.getResolution();
		if (vResolution.equals("")) {
		    vResolution = "n/a";
		}
		aCodec = additionalInfo.getAudioCodec();
		if (aCodec.equals("")) {
		    aCodec = "n/a";
		}
		aRate = additionalInfo.getAudioRate();
		if (!aRate.equals("")) {
		    aRate = aRate + " Hz";
		} else {
		    aRate = "n/a";
		}
		aBitrate = additionalInfo.getAudioBitrate();
		if (!aBitrate.equals("")) {
		    aBitrate = aBitrate + " kbps";
		} else {
		    aBitrate = "n/a";
		}
		
		aChannels = additionalInfo.getAudioChannels();
		
		plot = movie.getPlot();
		
		if (divideAlphebetically) {
		    if (lastStart != null && !title.toLowerCase().startsWith(lastStart.toLowerCase()) && !Character.isDigit(title.charAt(0))) {
			/*If character not between A-Z the movie will be added to the existing file*/
			if (Character.toUpperCase(title.charAt(0)) >= 'A' && Character.toUpperCase(title.charAt(0)) <= 'Z') {
			    
			    lastStart = title.substring(0, 1);
			    
			    writeEnd(writer, filepath);
			    
			    String path = filepath;
			    path += "."+lastStart.toUpperCase() + ".html";
			    
			    writer = new FileWriter(path);
			    writer = writeStart(filepath, writer, counter++);
			}
		    }
		    else
			lastStart = title.substring(0, 1);
		}
		
		writer.write(
			     "<table border=\"0\" width=\"100%\" cellpadding=\"0\" cellspacing=\"2\" bgcolor=\"#9C9ACE\">\n"+
			     "  <tr>\n"+
			     "    <td>\n"+
			     "      <table border=\"0\" width=\"100%\" id=\""+imdb+"\" cellpadding=\"0\" cellspacing=\"5\" bgcolor=\"#FFFFFF\">\n"+
			     "        <tr>\n"+
			     "          <td width=\"100\" height=\"148\" rowspan=\"6\" valign=\"center\" align=\"center\">\n"+
			     "            <font face=\"Arial\" size=\"1\">\n");
		if (!imdb.equals("")) {
		    writer.write(
				 "              <a href=\"http://www.imdb.com/Title?"+imdb+"\" target=\"_new\" title=\"Jump to IMDB ("+imdb+")\">\n"+
				 "                <img src=\""+coversRelativePath+"/"+cover+"\" border=\"0\" width=\"97\" height=\"" +coverHeight+ "\">\n"+
				 "              </a>\n");
		} else {
		    writer.write(
				 "              <img src=\""+coversRelativePath+"/"+cover+"\" border=\"0\" width=\"97\" height=\"" +coverHeight+ "\">\n");
		}
		writer.write(
			     "            </font>\n"+
			     "          </td>\n"+                                                                    
			     "          <td valign=\"center\" align=\"center\" rowspan=\"6\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td colspan=\"6\" bgcolor=\"#9C9ACE\" align=\"center\">\n"+
			     "            <font face=\"Arial\" size=\"5\">\n"+
			     "              <b>\n"+
			     "                "+title+"\n"+
			     "              </b>\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td width=\"100%\" colspan=\"6\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              "+(i+1)+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Duration:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+duration+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Video Codec:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+vCodec+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Audio Codec:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+aCodec+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;File Size:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+fileSize+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Video Rate:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+vRate+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Audio Rate:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+aRate+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;CD\'s:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+cds+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Video Bit Rate:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+vBitrate+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Audio Bit Rate:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+aBitrate+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;CD Cases:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+cdCases+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Resolution:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+vResolution+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\" bgcolor=\"#CECFFF\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;Audio Channels:\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;"+aChannels+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td width=\"100\" valign=\"center\" align=\"center\">\n"+
			     "            <font face=\"Arial\" size=\"1\">\n"+
			     "              <b>"+rating+" @IMDb</b>\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td valign=\"center\" align=\"center\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td width=\"100\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "          <td>\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              &nbsp;\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "        <tr>\n"+
			     "          <td colspan=\"8\">\n"+
			     "            <font face=\"Arial\" size=\"2\">\n"+
			     "              "+plot+"\n"+
			     "            </font>\n"+
			     "          </td>\n"+
			     "        </tr>\n"+
			     "      </table>\n"+
			     "    </td>\n"+
			     "  </tr>\n"+
			     "</table>\n"+
			     "\n"+
			     "<br><br>\n"+
			     "\n");
	    }
	    
	    
	    /* The html ending...*/
	    writeEnd(writer, filepath);
	    
	} catch (Exception e) {
	    log.error("", e);
	}
    }
    
    static FileWriter writeStart(String filepath, FileWriter writer, int i) {
	
	String s = "";
	
	if (divideAlphebetically )
	    s = "("+ (String) startChar.get(i) +")";
	
	try {
	    writer.write("<html>\n"+
			 "\n"+
			 "<head>\n"+
			 "  <title>Movies List (Full View) - Generated by MeD's Movie Manager</title>\n"+
			 "</head>\n"+
			 "\n"+
			 "<body>\n"+
			 "\n"+
			 "<font face=\"arial\" size=\"2\">\n"+
			 "\n"+
			 "<br><CENTER><font size=\"+4\">"+ title +"  "+ "</font> <font size=\"+3\">" +s+ "</font></CENTER><br><br><br><br>\n"+
			 "\n"
			 );
	    
	    if (divideAlphebetically)
		writer = writeLinks(writer, filepath);
	    
	    writer.write("<br><br><!-- START Movies Description... -->\n"+"\n");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	}
	return writer;
    }
    
    
    static void writeEnd(FileWriter writer, String filepath) {
	
	try {
	    
	    writer.write("<!-- END Movies Description... -->\n"+
			 "\n"+
			 "</font>\n");
	    
	    if (divideAlphebetically)
		writeLinks(writer, filepath);
	    
	    writer.write("<br><br>\n"+
			 "</body>\n"+
			 "\n"+
			 "</html>\n");
	    
	    writer.close();
	    
	} catch (Exception e) {
	    log.error("", e);
	}
    }
    
    
    static FileWriter writeLinks(FileWriter writer, String filepath) {
	
	String s;
	
	filepath = filepath.substring(filepath.lastIndexOf(MovieManager.getDirSeparator())+1, filepath.length());
	
	
	String tempFileName;
	
	try {
	    writer.write("<CENTER>\n");
	    
	    for (int u = 0; u < startChar.size(); u++) {
		
		tempFileName = filepath;
		
		if (u == 0 && startChar.get(0).equals("")) {
		    tempFileName += ".html";
		    s = tempFileName;
		}
		else {
		    tempFileName += "."+  startChar.get(u) + ".html";
		    s = (String) startChar.get(u);
		}
		
		writer.write("<font size=\"+2\"><a href=\"" + tempFileName + "\">"+s+"</a></font> &nbsp;");
	    }
	    
	    writer.write("</CENTER>\n");
	    
	} catch (Exception e) {
	    log.error("", e);
	}
	
	return writer;
    }
    
    
    
    /**
     * Executes the command.
     **/
    public void execute() {
	
	/* Opens the Export to HTML dialog... */
	ExtendedFileChooser fileChooser = new ExtendedFileChooser();
	fileChooser.setFileFilter(new CustomFileFilter(new String[]{"htm","html"},new String("HTML Files (*.htm, *.html)")));
	if (MovieManager.getConfig().getLastMiscDir()!=null) {
	    fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastMiscDir());
	}
	
	fileChooser.setDialogType(ExtendedFileChooser.CUSTOM_DIALOG);
	fileChooser.setDialogTitle("Export to HTML - Full");
	fileChooser.setApproveButtonToolTipText("Export to file (a folder \'Covers\' will also be created)");
	
	fileChooser.setAcceptAllFileFilterUsed(false);
	int returnVal = fileChooser.showDialog(MovieManager.getIt(), "Export");
	
	while (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
	    /* Gets the path... */
	    String path = fileChooser.getSelectedFile().getAbsolutePath().replaceAll(fileChooser.getSelectedFile().getName(),"");
	    String fileName = fileChooser.getSelectedFile().getName();
	    if (!fileName.endsWith(".htm") && !fileName.endsWith(".html")) {
		fileName = fileName + ".html";
	    }
	    
	    String coversPath = path + fileName.substring(0,fileName.lastIndexOf(".")) + "_covers";
	    /* Relative path to covers dir... */
	    String coversRelativePath = fileName.substring(0,fileName.lastIndexOf(".")) + "_covers";
	    
	    File htmlFile = new File(path+fileName);
	    File coversDir = new File(coversPath);
	    
	    if (htmlFile.exists()) {
		DialogQuestion fileQuestion = new DialogQuestion("File already exists", "A file with the chosen filename already exists. Would you like to overwrite the old file?");
		fileQuestion.setVisible(true);
		
		if (fileQuestion.getAnswer()) {
		    if (coversDir.exists()) { 
			DialogQuestion coverQuestion = new DialogQuestion("Directory already exists.", "The directory to store covers already exists. Put cover images in the existing directory?");
			coverQuestion.setVisible(true);
			
			if (coverQuestion.getAnswer()) {
			    export(htmlFile, coversPath, coversRelativePath);
			    break;
			}
			else
			    returnVal = fileChooser.showOpenDialog(MovieManager.getIt());
			
		    }
		    else {
			htmlFile.delete();
			export(htmlFile, coversPath, coversRelativePath);
			break;
		    }
		}
		else 
		    returnVal = fileChooser.showOpenDialog(MovieManager.getIt());
		
	    } 
	    else if (coversDir.exists()) { 
		DialogQuestion coverQuestion = new DialogQuestion("Directory already exists.", "The directory to store covers already exists. Put cover files in the exisitng directory and overwrite existing files?");
		coverQuestion.setVisible(true);
		
		if (coverQuestion.getAnswer()) {
		    export(htmlFile, coversPath, coversRelativePath);
		    break;
		}
		else {
		    returnVal = fileChooser.showOpenDialog(MovieManager.getIt());
		}
		
	    }
	    else if(!coversDir.mkdir()) {
		DialogAlert coverAlert = new DialogAlert("Couldn't create directory.", "The directory to store covers could not be created.");
		coverAlert.setVisible(true);
	    }
	    else {
		export(htmlFile, coversPath, coversRelativePath);
		break;
	    }
	}
	/* Sets the last path... */
	MovieManager.getConfig().setLastMiscDir(fileChooser.getCurrentDirectory());
    }
}
