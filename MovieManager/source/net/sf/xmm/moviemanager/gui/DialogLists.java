/**
 * @(#)DialogLists.java 1.0 28.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class DialogLists extends JDialog {
  
	Logger log = Logger.getLogger(getClass());
    
    private java.util.List _toRemove = new ArrayList();
  
    private java.util.List _toAdd = new ArrayList();
  
    private java.util.List _originalList;
  
    private JList listExistingFields;
    
    public DialogLists(Dialog parent) {
        super(parent, true);
        construct();
    }
    
    public DialogLists(Frame parent) {
        super(parent, true);
        construct();
    }
    
    /**
     * The Constructor.
     **/
    public void construct() {
        /* Close dialog... */
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    dispose();
		}
	    });
    
	/*Enables dispose when pushing escape*/
	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	Action escapeAction = new AbstractAction()
	    {
		public void actionPerformed(ActionEvent e) {
		    dispose();
		}
	    };
    
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
	getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
    
	/* Dialog properties...*/
	setTitle(Localizer.getString("DialogLists.title")); //$NON-NLS-1$
	setModal(true);
	setResizable(false);
	/* Initializes the original List... */
	_originalList = MovieManager.getIt().getDatabase().getListsColumnNames();
	/* Creates the list model... */
	DefaultListModel list = new DefaultListModel();
	for(int i=0; i<_originalList.size(); i++) {
	    list.addElement(_originalList.get(i));
	}
    
	/* Existing Fields panel...*/
	JPanel panelExistingFields = new JPanel();
	panelExistingFields.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogLists.panel-existing-list.title")), //$NON-NLS-1$
									 BorderFactory.createEmptyBorder(5,5,5,5)));
	panelExistingFields.setLayout(new GridBagLayout());
	GridBagConstraints constraints;
	listExistingFields = new JList();
	listExistingFields.setFont(new Font(listExistingFields.getFont().getName(),Font.PLAIN,listExistingFields.getFont().getSize()));
	listExistingFields.setLayoutOrientation(JList.VERTICAL);
	listExistingFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listExistingFields.setModel(list);
	JScrollPane scrollPaneExistingFields = new JScrollPane(listExistingFields);
	scrollPaneExistingFields.setPreferredSize(new Dimension(242,110));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelExistingFields.add(scrollPaneExistingFields,constraints);
	JButton buttonRemove = new JButton(Localizer.getString("DialogLists.button.remove.text")); //$NON-NLS-1$
	buttonRemove.setToolTipText(Localizer.getString("DialogLists.button.remove.tooltip")); //$NON-NLS-1$
	buttonRemove.setActionCommand("AdditionalInfoFields - Remove"); //$NON-NLS-1$
	buttonRemove.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    executeCommandRemove();
		}});
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.SOUTHWEST;
	panelExistingFields.add(buttonRemove,constraints);
	/* Add New Field panel...*/
	JPanel panelAddNewField = new JPanel();
	panelAddNewField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("DialogLists.panel-add-new-list.title")), //$NON-NLS-1$
								      BorderFactory.createEmptyBorder(5,5,5,5)));
	panelAddNewField.setLayout(new GridBagLayout());
	JTextField textFieldAdd = new JTextField(22);
	textFieldAdd.setDocument(new DocumentRegExp("[^\\p{Punct}]*")); //$NON-NLS-1$
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelAddNewField.add(textFieldAdd,constraints);
	JButton buttonAdd = new JButton(Localizer.getString("DialogLists.button.add.text")); //$NON-NLS-1$
	buttonAdd.setToolTipText(Localizer.getString("DialogLists.button.add.tooltip")); //$NON-NLS-1$
	buttonAdd.setActionCommand("AdditionalInfoFields - Add"); //$NON-NLS-1$
	buttonAdd.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    executeCommandAdd();
		}});
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelAddNewField.add(buttonAdd,constraints);
	/* All stuff together... */
	JPanel all = new JPanel();
	all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
	all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
	all.add(panelExistingFields);
	all.add(panelAddNewField);
	/* Buttons panel...*/
	JPanel panelButtons = new JPanel();
	panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
	JButton buttonSave = new JButton(Localizer.getString("DialogLists.button.save.text")); //$NON-NLS-1$
	buttonSave.setToolTipText(Localizer.getString("DialogLists.button.save.tooltip")); //$NON-NLS-1$
	buttonSave.setActionCommand("AdditionalInfoFields - Save"); //$NON-NLS-1$
	buttonSave.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		    executeCommandSave();
		}});
	panelButtons.add(buttonSave);
	JButton buttonCancel = new JButton(Localizer.getString("DialogLists.button.cancel.text")); //$NON-NLS-1$
	buttonCancel.setToolTipText(Localizer.getString("DialogLists.button.cancel.tooltip")); //$NON-NLS-1$
	buttonCancel.setActionCommand("AdditionalInfoFields - Cancel"); //$NON-NLS-1$
	buttonCancel.addActionListener(new CommandDialogDispose(this));
	panelButtons.add(buttonCancel);
	/* Adds all and buttonsPanel... */    
	getContentPane().add(all,BorderLayout.NORTH);
	getContentPane().add(panelButtons,BorderLayout.SOUTH);
	/* Packs and sets location... */
	pack();
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
    }

    /**
     * Returns the JList listExistingFields.
     **/
    protected JList getExistingFields() {
	return (JList)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
    }

    /**
     * Returns the JTextField textFieldAdd.
     **/
    protected JTextField getFieldToAdd() {
	return
	    (JTextField)
	    ((JPanel)
	     ((JPanel)
	      getContentPane().getComponent(0)).getComponent(1)).getComponent(0);
    }
  
    /**
     * Adds field to the remove list...
     **/
    private void executeCommandRemove() {
    	/* If any field is selected.. */
    	if (getExistingFields().getSelectedIndex()!=-1) {
    		/* Gets the field to remove... */
    		String field = (String)((DefaultListModel)getExistingFields().getModel()).getElementAt(getExistingFields().getSelectedIndex());
    		/* Adds the field to the remove list if it exists in the original... */
    		if (!_toRemove.contains(field) && _originalList.contains(field)) {
    			_toRemove.add(field);
    		}
    		/* Removes the field from the add list... */
    		if (_toAdd.contains(field)) {
    			_toAdd.remove(field);
    		}
    		/* Removes the field from the existing fields list */
    		((DefaultListModel)getExistingFields().getModel()).remove(getExistingFields().getSelectedIndex());
    		
    		if (((DefaultListModel)getExistingFields().getModel()).getSize() != 0)
    			listExistingFields.setSelectedIndex(0);
    	}
    }

    /**
     * Adds field to the add list...
     **/
    private void executeCommandAdd() {
	/* Gets the field to add... */
	String field = getFieldToAdd().getText().trim();
	
	/* If it isn't an empty string and does not exist in the existing list... */
	if (!field.equals("") && !containsIgnoreCase(getExistingFields(), field)) { //$NON-NLS-1$
	    /* Adds the field to the add list if it doesn't exists in the original... */
	    if(!_toAdd.contains(field) && !_originalList.contains(field)) {
		_toAdd.add(field);
	    }
	    /* Removes it from the remove list if it exists in the original list... */
	    if(_toRemove.contains(field)) {
		_toRemove.remove(field);
	    }
	    /* Adds the field to the existing fields list */
	    ((DefaultListModel)getExistingFields().getModel()).addElement(field);
	    /* Clears the textField... */
	    getFieldToAdd().setText(null);
	}
    }
    
    private boolean containsIgnoreCase(JList list, String field) {
	
	Object[] existingFields = ((DefaultListModel) list.getModel()).toArray();
	
	for (int i = 0; i < existingFields.length; i++) {
	    if (((String) existingFields[i]).equalsIgnoreCase(field))
		return true;
	}
	return false;
    }

    /**
     * Saves and exits...
     **/
    private void executeCommandSave() {
	
	boolean reload = false;
	
	/* Removes from database... */
	for (int i=0; i<_toRemove.size(); i++) {
	    MovieManager.getIt().getDatabase().removeListsColumn((String)_toRemove.get(i));
	    
	    if (((String)_toRemove.get(i)).equals(MovieManager.getConfig().getCurrentList())) {
		MovieManager.getConfig().setCurrentList("Show All"); //$NON-NLS-1$
		reload = true;
	    }
	}
	
	/* Adds to database... */
	for (int i=0; i<_toAdd.size(); i++)
	    MovieManager.getIt().getDatabase().addListsColumn((String)_toAdd.get(i));
	    
	/* Loading the listmenu with the existing lists */
	MovieManager.getDialog().loadMenuLists(MovieManager.getIt().getDatabase());
	
	if (reload)
	    MovieManagerCommandSelect.executeAndReload(-1);
	dispose();
    }
}
