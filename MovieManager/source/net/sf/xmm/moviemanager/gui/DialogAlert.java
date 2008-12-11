/**
 * @(#)DialogAlert.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

public class DialogAlert extends JDialog {
    
    static Logger log = Logger.getRootLogger();

    public DialogAlert(Dialog parent, String title, String alertMsg, boolean html) {
    	super(parent, true);

    	if (html)
    		createHTMLDialog(parent, title, alertMsg);
    	else
    		createOneMessageAlert(title, alertMsg);
    }

    public DialogAlert(Frame parent, String title, String alertMsg, boolean html) {
    	super(parent, true);

    	if (html)
    		createHTMLDialog(parent, title, alertMsg);
    	else
    		createOneMessageAlert(title, alertMsg);
    }


    public DialogAlert(Dialog parent, String title, String alertMsg) {
    	super(parent, true);
    	createOneMessageAlert(title, alertMsg);
    }

    public DialogAlert(Frame parent, String title, String alertMsg) {
    	super(parent, true);
    	createOneMessageAlert(title, alertMsg);
    }



    public DialogAlert(Dialog parent, String title, String alertMsg, String alertMsg2) {
    	super(parent, true);
    	createTwoMessageAlert(title, alertMsg, alertMsg2);
    }

    public DialogAlert(Frame parent, String title, String alertMsg, String alertMsg2) {
    	super(parent, true);
    	createTwoMessageAlert(title, alertMsg, alertMsg2);
    }
    
    
    void createHTMLDialog(Window parent, String title, String alertMsg) {

	try {
	    
	    setTitle(title);

	    /* All stuff together... */
	    JPanel panelAlert = new JPanel(new BorderLayout());
	    panelAlert.setBorder(BorderFactory.createEmptyBorder(10,5,5,10));
	
	    JLabel labelIcon = new JLabel();
	    labelIcon.setBorder(BorderFactory.createEmptyBorder(5,5,5,8));
	    labelIcon.setIcon(new ImageIcon(FileUtil.getImage("/images/alert.png").getScaledInstance(50,50,Image.SCALE_SMOOTH)));
	    
	    JTextPane area = new JTextPane();
	    area.setOpaque(false);
	    area.setBorder(null);
	    area.setEditable(false);
	    area.setContentType("text/html");
	    area.setText(alertMsg);
	
	    area.setFocusable(true);
	
	    JScrollPane scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    
	    panelAlert.add(labelIcon, BorderLayout.WEST);
	    panelAlert.add(scrollPane, BorderLayout.EAST);
	
	    if (scrollPane.getPreferredSize().getHeight() > 200)
		scrollPane.setPreferredSize(new Dimension((int)scrollPane.getPreferredSize().getWidth(), 200));
	
	    makeRest(parent, panelAlert);
	
	} catch (Exception e) {
	    log.error("Exception:" + e.getMessage());
	}
    }
    
  
    
    /**
     * The Constructor.
     **/
    protected void createOneMessageAlert(String title, String alertMsg) {
	
	/* Dialog creation...*/
	
	try {
	    
	    setTitle(title);
	    
	    /* All stuff together... */
	    JPanel panelAlert = new JPanel(new BorderLayout());
	    panelAlert.setBorder(BorderFactory.createEmptyBorder(10,5,5,10));
	    
	    JLabel labelIcon = new JLabel();
	    labelIcon.setBorder(BorderFactory.createEmptyBorder(5,5,5,8));
	    labelIcon.setIcon(new ImageIcon(FileUtil.getImage("/images/alert.png").getScaledInstance(50,50,Image.SCALE_SMOOTH)));
	    
	    JTextArea area = new JTextArea(alertMsg);
	    area.setOpaque(false);
	    area.setBorder(null);
	    area.setEditable(false);
	    //area.setContentType("text/html");
	    
	    JScrollPane scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    
	    panelAlert.add(labelIcon, BorderLayout.WEST);
	    panelAlert.add(scrollPane, BorderLayout.EAST);
	    
	    //scrollPane.setPreferredSize(new Dimension((int)scrollPane.getPreferredSize().getWidth(), 300));
	    
	    if (scrollPane.getPreferredSize().getHeight() > 300)
		scrollPane.setPreferredSize(new Dimension((int)scrollPane.getPreferredSize().getWidth(), 300));
	    
	    makeRest(MovieManager.getDialog(), panelAlert);
	
	} catch (Exception e) {
	    log.error("Exception:" + e.getMessage());
	}
    }
    
  
    
	
    protected void createTwoMessageAlert(String title, String alertMsg, String alertMsg2) {
	/* Dialog creation...*/
		
	setTitle(title);
	
	/* All stuff together... */
	JPanel panelAlert = new JPanel(new BorderLayout());
	panelAlert.setBorder(BorderFactory.createEmptyBorder(10,5,5,10));
	
	JLabel labelIcon = new JLabel();
	labelIcon.setBorder(BorderFactory.createEmptyBorder(5,5,5,10));
	labelIcon.setIcon(new ImageIcon(FileUtil.getImage("/images/alert.png").getScaledInstance(50,50,Image.SCALE_SMOOTH)));
	
	JLabel labelAlert = new JLabel(alertMsg);
	JLabel labelAlert2 = new JLabel(alertMsg2);
	
	JPanel alert = new JPanel(new GridLayout(0,1));
	alert.add(labelAlert);
	alert.add(labelAlert2);
	
	panelAlert.add(labelIcon, BorderLayout.WEST);
	panelAlert.add(alert, BorderLayout.EAST);
	
	makeRest(MovieManager.getDialog(), panelAlert);
    }
    
    
    void makeRest(Window parent, JComponent panelAlert) {
	/* Dialog properties...*/
	
	setModal(true);
	setResizable(false);
	
	/* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
	
	/* Enables dispose when pushing escape */
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    dispose();
		}
	    };
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
	
	/* Buttons panel...*/
	JPanel panelButtons = new JPanel();
	panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
	JButton buttonOk = new JButton("OK");
	buttonOk.setActionCommand("Alert - OK");
	buttonOk.addActionListener(new CommandDialogDispose(this));
	panelButtons.add(buttonOk);
	/* Adds all and buttonsPanel... */    
	getContentPane().add(panelAlert, BorderLayout.NORTH);
	getContentPane().add(panelButtons, BorderLayout.SOUTH);
	/* Packs and sets location... */
	pack();
	
	//setSize(new Dimension((int)panelAlert.getPreferredSize().getWidth(), 200));
	
	//setLocationRelativeTo(parent);
	
	if (parent != null) {

		if ((parent.getLocation().getX() != 0) && (parent.getLocation().getY() != 0))
			setLocation((int) parent.getLocation().getX()+(parent.getWidth()-getWidth())/2,
					(int) parent.getLocation().getY()+(parent.getHeight()-getHeight())/2);
		else {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(dim.getWidth()-getWidth())/2, (int)(dim.getHeight()-getHeight())/2);
		}
	}
    }
}

    
