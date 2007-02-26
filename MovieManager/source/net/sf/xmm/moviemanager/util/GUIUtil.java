/**
 * @(#)GUIUtil.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.util;

import org.apache.log4j.Logger;

import javax.swing.*;

import java.awt.Component;
import java.awt.Container;

public class GUIUtil {
    
    static Logger log = Logger.getRootLogger();
    
    public static void show(final Container container, final boolean visible) {
        
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                container.setVisible(visible);
            }
        });
    }
    
    public static void showAndWait(final Container container, final boolean visible) {
        
        if (SwingUtilities.isEventDispatchThread()) {
	    container.setVisible(visible);
        }
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run() {
                        container.setVisible(visible);
                    }
                });
            } catch (InterruptedException i) {
                log.error("showAndWait error:", i);
            } catch (java.lang.reflect.InvocationTargetException i) {
                log.error("showAndWait error:", i);
            }
        }
    }
   
    
    public static void invokeLater(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }
    
} 
