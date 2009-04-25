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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class GUIUtil {

	static Logger log = Logger.getLogger(GUIUtil.class);

	public static void show(final java.awt.Container container, final boolean visible) {
		show(container, visible, null);
	}
	
	public static void show(final Container container, final boolean visible, Window appearOnLeftSide) {

		if (visible)
			adjustLocation(container, appearOnLeftSide);
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				container.setVisible(visible);
			}
		});
	}

	public static void showAndWait(final Container container, final boolean visible) {
		showAndWait(container, visible, null);
	}
	
	public static void showAndWait(final Container container, final boolean visible, Window appearOnLeftSide) {

		try {

			if (visible)
				adjustLocation(container, appearOnLeftSide);

			if (SwingUtilities.isEventDispatchThread()) {
				container.setVisible(visible);
			}
			else {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run() {
						container.setVisible(visible);
					}
				});
			}
		} catch (InterruptedException i) {
			log.error("InterruptedException:" + i.getMessage(), i);
		} catch (java.lang.reflect.InvocationTargetException i) {
			log.error("InvocationTargetException:" + i.getMessage(), i);
		} catch (Exception i) {
			log.error("Exception:" + i.getMessage(), i);
		}
	}

	private static void adjustLocation(Container container, Window appearOnLeftSide) {
		
		Point p = container.getLocation();

		Dimension size = container.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int wisthLocation = (int) (p.getX() + size.getWidth());
		int heightLocation = (int ) (p.getY() + size.getHeight());	

		if (appearOnLeftSide != null) {
			
			Point appearLeft = appearOnLeftSide.getLocation();
			
			int wSize = (int) size.getWidth();
			p.setLocation(appearLeft.getX()- wSize, p.getY());
		}
		
		if (wisthLocation > screenSize.getWidth()) {
			int diff = (int) (wisthLocation - screenSize.getWidth());
			p.setLocation((p.getX() - diff), p.getY());
		}

		if (heightLocation > screenSize.getHeight()) {
			int diff = (int) (heightLocation - screenSize.getHeight());
			p.setLocation(p.getX(), (p.getY() - diff));
		}

		if (p.getX() < 0) {
			p.setLocation(0.0, p.getY());
		}

		if (p.getY() < 0) {
			p.setLocation(p.getX(), 0.0);
		}

		container.setLocation(p);
	}

	public static void invokeLater(Runnable runnable) {

		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(runnable);
		else
			runnable.run();
	}

	public static void isEDT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new Error("assertion failed: not on EDT");
		}
	}

	/**
	 * Must not be executed on the EDT.
	 */  
	public static void isNotEDT() {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Error("assertion failed: on EDT");
		}
	}
	
	
	public static DefaultListModel toDefaultListModel(ArrayList<?> list) {

		DefaultListModel listModel = new DefaultListModel();

		for (int i = 0; i < list.size(); i++) {
			listModel.addElement(list.get(i));
		}

		return listModel;
	}
} 
