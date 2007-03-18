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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Method;

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
                boolean browserLauncherFailed = false;
                boolean customBrowser = false;
                
                String browser = MovieManager.getConfig().getSystemWebBrowser();
                 
                if (browser.equals("Custom"))
                    customBrowser = true;
                
                
                if (!customBrowser) {
                    
                    try {
                        BrowserLauncher launcher = new BrowserLauncher(null);
                        
                        if (browser.equals("Default"))
                            launcher.openURLinBrowser(url);
                        else
                            launcher.openURLinBrowser(browser, url);
                    } 
                    catch (Exception e) {
                        log.warn("BrowserLauncher2 failed");
                        browserLauncherFailed = true;
                    }
                }
                                
                if (browserLauncherFailed || customBrowser) {
                    
                    try {
                        
                        if (customBrowser && new File(MovieManager.getConfig().getBrowserPath()).isFile()) {
                            String cmd = MovieManager.getConfig().getBrowserPath() + " " + url;
                            Process p = Runtime.getRuntime().exec(cmd);
                        }
                        else {
                            
                            if (browser.equals("Default") && FileUtil.isWindows()) {
                                Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                            }
                            else if (browser.equals("Default") && FileUtil.isMac()) {
                                Class macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
                                Method openURL = macUtils.getDeclaredMethod("openURL", new Class[] {String.class});
                                openURL.invoke(null, new Object[] {url});
                            }
                            else {
                                
                                String cmd;
                                Process p;
                                
                                String remoteOpenURL = " -remote openURL" + "(" + url + ")";
                                
                                for (int i = 0; i < 5; i++) {
                                    
                                    if (i == 0)
                                        cmd = "opera" + remoteOpenURL; 
                                    else if (i == 1)
                                        cmd = "firefox" + remoteOpenURL;
                                    else if (i == 2)
                                        cmd = "mozilla" + remoteOpenURL;
                                    else if (i == 3)
                                        cmd = "konqueror" + remoteOpenURL;
                                    else 
                                        cmd = "netscape" + remoteOpenURL;
                                    
                                     
                                    p = Runtime.getRuntime().exec(cmd);
                                     
                                    try {
                                        int exitCode = p.waitFor();
                                        
                                        if (exitCode != 0) {
                                            /*Command failed, start up the browser*/
                                            p = Runtime.getRuntime().exec(cmd);
                                        }
                                        
                                        break;
                                    }
                                    
                                    catch(InterruptedException x) {
                                        log.error("Error bringing up browser, cmd='" + cmd + "'");
                                    }
                                }
                            }
                        }
                            
                            
                    } catch (Exception e) {
                        log.error("Exception: ", e);
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
