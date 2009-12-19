/**
 * @(#)MovieManagerCommandPlay.java 1.0 15.11.08 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SimpleMailbox;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.StringUtil.FilenameCloseness;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStreamerHandler;

import org.apache.log4j.Logger;

public class MovieManagerCommandPlay implements ActionListener {

	static Logger log = Logger.getLogger(MovieManagerCommandPlay.class);

	public void actionPerformed(ActionEvent e) {

		log.debug("ActionPerformed: " + e.getActionCommand());
		
		SimpleMailbox mailbox = new SimpleMailbox();
		
		try {
			execute(mailbox);
			mailbox.wait_for_message();
			
			String msg = mailbox.getMessage();
			
			// Not successfull
			if (!msg.equals("exec done")) {
				
				if (msg.equals("Permission denied")) {
					
					DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Permission denied", "Insufficient privileges to execute command");
					GUIUtil.show(alert, true);
				}
				
			}
			
		} catch (IOException e1) {
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void execute() throws IOException, InterruptedException {
		execute(null);
	}
	
		
	public static void execute(final SimpleMailbox mailbox) throws IOException, InterruptedException {
			
		/**
		 * Runs the command using Runtimes exec method.
		 * @author Bro
		 */
		class LaunchPlayer extends Thread {

			String [] args;
			String command;
			File cwd;
			
			public Process p;
			
			// cwd = Current working directory
			LaunchPlayer(String [] args, String command, File cwd) {
				this.args = args;
				this.command = command;
				this.cwd = cwd;
			}
			
			public void run() {

				try {
							
					if (args != null && cwd != null) {
						log.debug("Execute command from cwd:" + cwd);
						printCommand("Command executed:", args);
						
						p = Runtime.getRuntime().exec(args, null, cwd);
					}
					else if (cwd != null) {
						log.debug("Execute file:" + command);
						p = Runtime.getRuntime().exec(command, null, cwd);
					} else if (args == null) {
						log.debug("Execute command:" + command);
						p = Runtime.getRuntime().exec(command);
					} else {
						printCommand("Execute default player:", args);
						p = Runtime.getRuntime().exec(args);
					}
										
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage(), e);
					log.debug("Cause:" + e.getCause());
										
					if (mailbox != null) {
						try {
							if (e.getMessage().indexOf("not found") != -1) {
								mailbox.setMessage("not found");
							}
							else if (e.getMessage().indexOf("Permission denied") != -1) {
								mailbox.setMessage("Permission denied");
							}
						} catch (InterruptedException e1) {
							log.error("Exception: " + e1.getMessage(), e1);
						}
					}
				}
				finally {
					if (mailbox != null)
						try {
							mailbox.setMessage("exec done");
						} catch (InterruptedException e) {
							log.error("Exception: " + e.getMessage(), e);
						}
				}

				if (p == null)
					return;
				
				// Clear input/error streams to avoid dead lock in subprocess
				SysUtil.cleaStreams(p);
			}
		}
		
		// check if there is a file selected
		int listIndex = -1;
		DialogMovieManager movieManagerInstance = MovieManager.getDialog();
		TreeModel moviesListTreeModel = movieManagerInstance.getMoviesList().getModel();

		// check whether movies list has entries
		if (moviesListTreeModel.
				getChildCount(moviesListTreeModel.getRoot()) > 0) {

			listIndex = movieManagerInstance.getMoviesList().getLeadSelectionRow();

			if (listIndex == -1)
				listIndex = movieManagerInstance.getMoviesList().getMaxSelectionRow();

			if (movieManagerInstance.getMoviesList().getSelectionCount() > 1) {
				movieManagerInstance.getMoviesList().setSelectionRow(listIndex);
			}
		}

		if (listIndex != -1) {

			ModelEntry selected = ((ModelEntry) ((DefaultMutableTreeNode) movieManagerInstance.getMoviesList().
					getLastSelectedPathComponent()).getUserObject());

			if (selected.getKey() != -1) {

				String [] command = null;

				MovieManagerConfig mmc = MovieManager.getConfig();
				String fileLocation = selected.getAdditionalInfo().getFileLocation();
				MovieManagerStreamerHandler streamerHandler = MovieManager.getConfig().getStreamerHandler();
        			
				if (streamerHandler != null) {
					String extraInfoField = streamerHandler.getDatabaseUrlField();
					fileLocation = selected.getAdditionalInfo().getExtraInfoFieldValue(extraInfoField);
				}
				
				if (fileLocation.trim().equals(""))
					return;
				
				String cmd = null;
	
				String [] files = fileLocation.split("\\*");
				
				FilenameCloseness [] closeness = {FilenameCloseness.almostidentical, FilenameCloseness.much, FilenameCloseness.some, FilenameCloseness.litte};
				
				if (MovieManager.getConfig().getExecuteExternalPlayCommand()) {
					File mediaFile = new File(files[0]);
					
					File [] dirFiles = mediaFile.getParentFile().listFiles();
					
					if (dirFiles != null) {
						for (int u = 0; u < closeness.length; u++) {

							for (int i = 0; i < dirFiles.length; i++) {

								if ((!SysUtil.isWindows() && dirFiles[i].getName().endsWith(".xmm.sh")) || 
										(SysUtil.isWindows() && dirFiles[i].getName().endsWith(".xmm.bat"))) {

									FilenameCloseness closeness_result = StringUtil.compareFileNames(dirFiles[i].getName(), mediaFile.getName());

									// File names are similar
									if (closeness_result == closeness[u]) {
										final File dirFile = dirFiles[i];
										final LaunchPlayer player = new LaunchPlayer(null, dirFile.getAbsolutePath(), mediaFile.getParentFile());
										player.start();
										return;
									}
								}
							}
						}
					}
				}
				
				File cwd = null;
				
				if (SysUtil.isWindows() && mmc.getUseDefaultWindowsPlayer()) {
					cmd = "cmd.exe /C  ";

					// Can only have one file as argument. The second is ignored
					for (int i = 0; i < 1; i++) {
						cmd +=  "\"" + files[i] + "\"";
					}
				}
				else {
					
					ArrayList<String> commandList = new ArrayList<String>();
					
					cmd = mmc.getMediaPlayerPath();
					
					if (cmd != null && "".equals(cmd)){

						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showDialog(null, "Launch");
						if(returnVal != JFileChooser.APPROVE_OPTION)
							return;

						cmd = chooser.getSelectedFile().getCanonicalPath();
						mmc.setMediaPlayerPath(cmd);
					}
					
					if (cmd != null && !"".equals(cmd)) {
						commandList.add(cmd);
					}
					
				
					
					String playArg = mmc.getMediaPlayerCmdArgument();
					
					if (playArg != null && !playArg.equals("")) {
						List<String> args = getArguments(playArg);
												
						for (int i = 0; i < args.size(); i++)
							commandList.add(args.get(i));
					}
										
					for (int i= 0; i < files.length; i++) {
												
						
						String filePath = files[i];
						String parentPath = new File(filePath).getParentFile().getAbsolutePath();
						
						// If the parent path contains spaces, use parent path as cwd (current working directory)
						if (parentPath.indexOf(" ") != -1) {
							
							if (cwd == null)
								cwd = new File(parentPath);
							
							commandList.add(new File(filePath).getName());
						}
						else
							commandList.add(filePath);
					}
					
					command = new String[commandList.size()];
					command = (String[]) commandList.toArray(command);
				}
					
				final String windowsDefault = cmd;
				
				/* Creating a Object wrapped in a Thread */
				Thread t = new Thread(new LaunchPlayer(command, windowsDefault, cwd));
				t.start();
			}
		}
	}
	
	public static void printCommand(String intro, String [] args) {
				
		String str = intro;
		
		for (int i = 0; i < args.length; i++)
			str += SysUtil.getLineSeparator() + "args["+i+"]:" + args[i];
			
		log.debug(str);
	}
	
	public static String getCombined(String [] args) {
		String str = args[0];
		
		for (int i = 1; i < args.length; i++)
			str += " " + args[i];
		
		return str;
	}
	
	
	/**
	 * Splits the string on spaces, except when enclosed in single or double quotes
	 * @param arg
	 * @return
	 */
	public static List<String> getArguments(String arg) {
		
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(arg);

		while (regexMatcher.find()) {
						
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			} else if (regexMatcher.group(2) != null) {
				// Add single-quoted string without the quotes
				matchList.add(regexMatcher.group(2));
			} else {
				// Add unquoted word
				matchList.add(regexMatcher.group(0));
			}
		}
		return matchList;
	}
}


