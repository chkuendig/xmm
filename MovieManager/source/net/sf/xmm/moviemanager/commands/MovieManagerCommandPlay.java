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

import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.SimpleMailbox;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStreamerHandler;

import org.apache.log4j.Logger;

public class MovieManagerCommandPlay implements ActionListener {

	static Logger log = Logger.getLogger(MovieManagerCommandPlay.class);

	public void actionPerformed(ActionEvent e) {

		log.debug("ActionPerformed: " + e.getActionCommand());
		
		try {
			execute();
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
					if (cwd != null) {
						log.debug("Execute file:" + command);
						p = Runtime.getRuntime().exec(command, null, cwd);
					} else if (args == null) {
						log.debug("Execute command:" + command);
						p = Runtime.getRuntime().exec(command);
					} else {
						String str = "";
						for (int i = 0; i < args.length; i++) {
							str += args[i] + " ";
						}
						
						log.debug("Execute default player:" + str);
						p = Runtime.getRuntime().exec(args);
					}
										
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage(), e);
					
					log.debug("Cause:" + e.getCause());
					
					
					if (mailbox != null) {
						if (e.getMessage().indexOf("not found") != -1) {
							mailbox.setMessage("");
						}
					}
				}
				finally {
					if (mailbox != null)
						mailbox.setMessage("exec done");
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
								
				if (MovieManager.getConfig().getExecuteExternalPlayCommand()) {
					File mediaFile = new File(files[0]);
					
					File [] dirFiles = mediaFile.getParentFile().listFiles();
					
					if (dirFiles != null) {
						for (int i = 0; i < dirFiles.length; i++) {

							if (dirFiles[i].getName().endsWith(".xmm.sh") || dirFiles[i].getName().endsWith(".xmm.bat")) {

								int rating = StringUtil.compareFileNames(dirFiles[i].getName(), mediaFile.getName());

								// File names are similar
								if (rating > 2) {
									final File dirFile = dirFiles[i];
									final LaunchPlayer player = new LaunchPlayer(null, dirFile.getAbsolutePath(), mediaFile.getParentFile());
									player.start();
									return;
								}
							}
						}
					}
				}
				
				if (SysUtil.isWindows() && mmc.getUseDefaultWindowsPlayer()) {
					cmd = "cmd.exe /C  ";

					// Can only have one file as argument. The second is ignored
					for (int i = 0; i < 1; i++) {
						cmd +=  "\"" + files[i] + "\"";
					}
				}
				else {
					// each file is enclosed by quotes.	
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
						String [] args = playArg.split(" ");
						
						for (int i = 0; i < args.length; i++)
							commandList.add(args[i]);
					}
										
					
					for (int i= 0; i < files.length; i++) {
						commandList.add(files[i]);
					}
					
					command = new String[commandList.size()];
					command = (String[]) commandList.toArray(command);
				}
					
				final String windowsDefault = cmd;
				
				/* Creating a Object wrapped in a Thread */
				Thread t = new Thread(new LaunchPlayer(command, windowsDefault, null));
				t.start();
			}
		}
	}
}


