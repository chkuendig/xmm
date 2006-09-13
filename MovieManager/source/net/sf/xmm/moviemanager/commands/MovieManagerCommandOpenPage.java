/**
 * @(#)MovieManagerCommandOpenPage.java 1.0 10.10.05 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.*;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import edu.stanford.ejalbert.BrowserLauncher;

public class MovieManagerCommandOpenPage extends MouseAdapter implements ActionListener {
    
    static Logger log = Logger.getRootLogger();
    
    private String url;
    
    /**
     * Constructor. Initialises the _url var.
     **/
    public MovieManagerCommandOpenPage(String url) {
	this.url = url;
    } 
    
    
    void executeCommandOpenPage() {
	
	class LaunchBrowser extends Thread {
	    
	    public void run() {              
		boolean browserlauncherfailed = false;
		
		try {
		    BrowserLauncher launcher = new BrowserLauncher(null);
		    launcher.openURLinBrowser(url);
		}
		catch (Exception e) {
		    log.warn("BrowserLauncher2 failed");
		    browserlauncherfailed = true;
		}
		
		/* If BrowserLauncher fails, trying to launch using the old method. */
		
		if (browserlauncherfailed) {
		    try {
			
			if (MovieManager.isWindows()) {
			    Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+ url);
			}
			else if (MovieManager.isMac()) {
			    Class macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
			    Method openURL = macUtils.getDeclaredMethod("openURL", new Class[] {String.class});
			    openURL.invoke(null, new Object[] {url});
			}
			else {  
			    String cmd;
			    Process p;
			    
			    for (int i = 0; i < 5; i++) {
		  
				if (i == 0)
				    cmd = "opera"; 
				else if (i == 1)
				    cmd = "firefox";
				else if (i == 2)
				    cmd = "mozilla";
				else if (i == 3)
				    cmd = "konqueror";
				else 
				    cmd = "netscape";
		    
				p = Runtime.getRuntime().exec(cmd + " -remote openURL" + "(" + url + ")");
				
				//cmd = new String[]{"sh","-c","opera "+url+" || mozilla "+url+" || netscape "+url};
				//Process p = Runtime.getRuntime().exec(cmd);
				
				try {
				    int exitCode = p.waitFor();
				    if (exitCode != 0) {
					/*Command failed, start up the browser*/
					p = Runtime.getRuntime().exec(cmd + " " + url);
				    }
				    else
					break;
				}
		  
				catch(InterruptedException x) {
				    log.error("Error bringing up browser, cmd='" + cmd + "'");
				}
			    }
			}
		    } catch (Exception e) {
			log.error("Exception: " + e);
		    }
		}
	    }
	}
	
	/* Creating a Object wrapped in a Thread */
	Thread t = new Thread(new LaunchBrowser());
	t.start();
    }
    
    /**
     * Invoked when the mouse button has been clicked
     * (pressed and released) on a component.
     **/
    public void mouseClicked(MouseEvent event) {
	log.debug("ActionPerformed: OpenPage (Movie Page)");
	executeCommandOpenPage();
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: " + event.getActionCommand());
	executeCommandOpenPage();
    }
}
