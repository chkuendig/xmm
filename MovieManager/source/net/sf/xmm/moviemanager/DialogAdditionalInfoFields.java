/**
 * @(#)DialogAdditionalInfoFields.java 1.0 27.01.06 (dd.mm.yy) 
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

package net.sf.xmm.moviemanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.util.DocumentRegExp;

import org.apache.log4j.Logger;

public class DialogAdditionalInfoFields extends JDialog {
  
    static Logger log = Logger.getRootLogger();
       
    private java.util.List _toRemove = new ArrayList();
  
    private java.util.List _toAdd = new ArrayList();
  
    private java.util.List _originalExtraList;
    
    private ArrayList fieldsList;
    
    /**
     * The Constructor.
     **/
    public DialogAdditionalInfoFields() {
	/* Dialog creation...*/
	super(MovieManager.getIt());
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
    
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", escapeAction);
    
	/* Dialog properties...*/
	setTitle("Additional Info Fields");
	setModal(true);
	setResizable(false);
		
	/* fieldsList contains all the additional info fields  */
	fieldsList = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
	
	/* Contains all the existing extra info fields */
	_originalExtraList = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();
	
	/* Contains the number of each of the active fields */
	int [] activeFields = MovieManager.getIt().getActiveAdditionalInfoFields();
	
	/* adds all the extra info fields to fieldsList */
	for (int i = 0; i < _originalExtraList.size(); i++) {
	    fieldsList.add(_originalExtraList.get(i));
	}
	
	/* Creates the list model... */
	DefaultListModel listActive = new DefaultListModel();
	DefaultListModel listInactive = new DefaultListModel();
	
	Object[] fieldsListArray =  fieldsList.toArray();
	
	/* Adds the active fields to the active list */
	for (int i = 0; i < activeFields.length; i++) {
	    
	    if (fieldsListArray[activeFields[i]] != null) {
		listActive.addElement(fieldsListArray[activeFields[i]]);
		fieldsListArray[activeFields[i]] = null;
	    }
	    else
		log.warn("Null entry in additional info active list.");
	}
	
	/* Adds the inactive-fields to the inactive-list */
	for (int i = 0; i < fieldsListArray.length; i++) {
	    if (fieldsListArray[i] != null)
		listInactive.addElement(fieldsListArray[i]);
	}
	
	/* Inactive Fields panel */
	JPanel panelInactiveFields = new JPanel();
	panelInactiveFields.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Hidden Fields "),
									 BorderFactory.createEmptyBorder(5,5,5,5)));
	panelInactiveFields.setLayout(new GridBagLayout());
	GridBagConstraints constraints;
	JList listInactiveFields = new JList();
	listInactiveFields.setFont(new Font(listInactiveFields.getFont().getName(),Font.PLAIN,listInactiveFields.getFont().getSize()));
	listInactiveFields.setLayoutOrientation(JList.VERTICAL);
	listInactiveFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listInactiveFields.setModel(listInactive);
	listInactiveFields.setSelectedIndex(0);
	
	JScrollPane scrollPaneInactiveFields = new JScrollPane(listInactiveFields);
	scrollPaneInactiveFields.setPreferredSize(new Dimension(242,130));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelInactiveFields.add(scrollPaneInactiveFields, constraints);
    
	JButton buttonMakeActive = new JButton("Make Active");
	buttonMakeActive.setToolTipText("Remove field");
	buttonMakeActive.setActionCommand("AdditionalInfoFields - Make Active");
	buttonMakeActive.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
		    executeCommandMakeActive();
		}});
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.SOUTHWEST;
	panelInactiveFields.add(buttonMakeActive, constraints);
    
    
	/* Active Fields panel...*/
	JPanel panelActiveFields = new JPanel();
	panelActiveFields.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Active Fields "),
								       BorderFactory.createEmptyBorder(5,5,5,5)));
	panelActiveFields.setLayout(new GridBagLayout());
    
	JList listActiveFields = new JList();
	listActiveFields.setFont(new Font(listActiveFields.getFont().getName(),Font.PLAIN,listActiveFields.getFont().getSize()));
	listActiveFields.setLayoutOrientation(JList.VERTICAL);
	listActiveFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	listActiveFields.setModel(listActive);
	listActiveFields.setSelectedIndex(0);
    
	JScrollPane scrollPaneActiveFields = new JScrollPane(listActiveFields);
	scrollPaneActiveFields.setPreferredSize(new Dimension(242,160));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridheight = 4;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelActiveFields.add(scrollPaneActiveFields,constraints);
	
	JButton buttonMoveUp = new JButton("Move Up");
	buttonMoveUp.setToolTipText("Move selected field up");
	buttonMoveUp.setActionCommand("AdditionalInfoFields - Move Up");
	buttonMoveUp.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
		    executeCommandMoveUp();
		}});
    
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 1;
	constraints.insets = new Insets(5,5,5,5);
	constraints.weighty = 1.0;
	constraints.weightx = 1.0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.SOUTHWEST;
	panelActiveFields.add(buttonMoveUp, constraints);
    
	JButton buttonMoveDown = new JButton("Move Down");
	buttonMoveDown.setToolTipText("Move selected field down");
	buttonMoveDown.setActionCommand("AdditionalInfoFields - Move Down");
	buttonMoveDown.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
		    executeCommandMoveDown();
		}});
    
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.weighty = 1.0;
	constraints.weightx = 1.0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.NORTHWEST;
	panelActiveFields.add(buttonMoveDown, constraints);
	
	JButton buttonRemove = new JButton("Remove");
	buttonRemove.setToolTipText("Remove selected field");
	buttonRemove.setActionCommand("AdditionalInfoFields - Remove");
	buttonRemove.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
		    executeCommandRemove();
		}});
    
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.insets = new Insets(5,5,5,5);
	constraints.weighty = 1.0;
	constraints.weightx = 1.0;
	
	constraints.anchor = GridBagConstraints.SOUTHWEST;
	panelActiveFields.add(buttonRemove, constraints);
	
	JButton buttonHide = new JButton("Hide");
	buttonHide.setToolTipText("Hide selected field");
	buttonHide.setActionCommand("AdditionalInfoFields - Hide");
	buttonHide.setPreferredSize(buttonRemove.getPreferredSize());
	buttonHide.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
		    executeCommandHide();
		}});
    
	constraints = new GridBagConstraints();
	constraints.gridx = 1;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	
	constraints.weighty = 1.0;
	constraints.weightx = 1.0;
	
	constraints.anchor = GridBagConstraints.NORTHWEST;
	panelActiveFields.add(buttonHide, constraints);
	
	/* Add New Field panel...*/
	JPanel panelAddNewField = new JPanel();
	panelAddNewField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Add New Field "),
								      BorderFactory.createEmptyBorder(5,5,5,5)));
	panelAddNewField.setLayout(new GridBagLayout());
	
	JTextField textFieldAdd = new JTextField(22);
	textFieldAdd.setDocument(new DocumentRegExp("([^\\p{Punct}]|[_-])*"));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.insets = new Insets(5,5,5,5);
	constraints.anchor = GridBagConstraints.WEST;
	panelAddNewField.add(textFieldAdd,constraints);
	
	JButton buttonAdd = new JButton("    Add    ");
	buttonAdd.setToolTipText("Add new field");
	buttonAdd.setActionCommand("AdditionalInfoFields - Add");
	buttonAdd.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
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
	all.add(panelInactiveFields);
	all.add(panelActiveFields);
	all.add(panelAddNewField);
	
	/* Buttons panel...*/
	JPanel panelButtons = new JPanel();
	panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
	
	JButton buttonSave = new JButton("Save");
	buttonSave.setToolTipText("Save changes");
	buttonSave.setActionCommand("AdditionalInfoFields - Save");
	buttonSave.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    log.debug("ActionPerformed: "+event.getActionCommand());
		    executeCommandSave();
		}});
	panelButtons.add(buttonSave);
	JButton buttonCancel = new JButton("Cancel");
	buttonCancel.setToolTipText("Discard changes");
	buttonCancel.setActionCommand("AdditionalInfoFields - Cancel");
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
     * Returns the JList listActiveFields.
     **/
    protected JList getInactiveFields() {
	return (JList)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
    }
    
    /**
     * Returns the JList listActiveFields.
     **/
    protected JList getActiveFields() {
	return (JList)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       getContentPane().getComponent(0)).getComponent(1)).getComponent(0)).getViewport().getComponent(0);
    }

    /**
     * Returns the JTextField textFieldAdd.
     **/
    protected JTextField getFieldToAdd() {
	return
	    (JTextField)
	    ((JPanel)
	     ((JPanel)
	      getContentPane().getComponent(0)).getComponent(2)).getComponent(0);
    }
    
    private void executeCommandMoveDown() {
	
	int selectedIndex = getActiveFields().getSelectedIndex();
	
	if (selectedIndex != -1 && selectedIndex != ((DefaultListModel)getActiveFields().getModel()).size()-1) {
	    
	    String field = (String)((DefaultListModel)getActiveFields().getModel()).remove(selectedIndex);
	    ((DefaultListModel)getActiveFields().getModel()).add(selectedIndex +1, field);
	    getActiveFields().setSelectedIndex(selectedIndex +1);
	    getActiveFields().ensureIndexIsVisible(selectedIndex +1);
	}
    }
    
    private void executeCommandMoveUp() {
	
	int selectedIndex = getActiveFields().getSelectedIndex();
	
	if (selectedIndex != -1 && selectedIndex != 0) {
	    
	    String field = (String)((DefaultListModel)getActiveFields().getModel()).remove(selectedIndex);
	    ((DefaultListModel)getActiveFields().getModel()).add(selectedIndex -1, field);
	    getActiveFields().setSelectedIndex(selectedIndex -1);
	    getActiveFields().ensureIndexIsVisible(selectedIndex -1);
	}
    }
    
    
     /**
     * Moves the field to the inactive list...
     **/
    private void executeCommandHide() {
	
	int activeSelectedIndex = getActiveFields().getSelectedIndex();
	/* If any field is selected.. */
	if (activeSelectedIndex != -1) {
	    /* Gets the field to remove... */
	    String field = (String)((DefaultListModel)getActiveFields().getModel()).getElementAt(activeSelectedIndex);
	    
	    /* Removes the field from the active fields list */
	    ((DefaultListModel)getActiveFields().getModel()).remove(activeSelectedIndex);
	    if (activeSelectedIndex == ((DefaultListModel)getActiveFields().getModel()).size())
		getActiveFields().setSelectedIndex(--activeSelectedIndex);
	    else
		getActiveFields().setSelectedIndex(activeSelectedIndex);
	    
	    getActiveFields().ensureIndexIsVisible(activeSelectedIndex);
	    
	    /* Add the field to the active fields list */
	    ((DefaultListModel)getInactiveFields().getModel()).addElement(field);
	    getInactiveFields().setSelectedIndex(((DefaultListModel)getInactiveFields().getModel()).size()-1);
	    getInactiveFields().ensureIndexIsVisible(((DefaultListModel)getInactiveFields().getModel()).size()-1);
	}
    }
    
    /**
     * Moves the field to the active list...
     **/
    private void executeCommandMakeActive() {
	
	int inactiveSelectedIndex = getInactiveFields().getSelectedIndex();
	/* If any field is selected.. */
	if (inactiveSelectedIndex != -1) {
	    /* Gets the field to remove... */
	    String field = (String)((DefaultListModel)getInactiveFields().getModel()).getElementAt(inactiveSelectedIndex);
	    /* Removes the field from the inactive fields list */
	    ((DefaultListModel)getInactiveFields().getModel()).remove(inactiveSelectedIndex);
	    
	    if (inactiveSelectedIndex == ((DefaultListModel)getInactiveFields().getModel()).size())
		getInactiveFields().setSelectedIndex(--inactiveSelectedIndex);
	    else
		getInactiveFields().setSelectedIndex(inactiveSelectedIndex);
	    
	    getInactiveFields().ensureIndexIsVisible(inactiveSelectedIndex);
	    
	    /* Add the field to the active fields list */
	    ((DefaultListModel)getActiveFields().getModel()).addElement(field);
	    getActiveFields().setSelectedIndex(((DefaultListModel)getActiveFields().getModel()).size()-1);
	    getActiveFields().ensureIndexIsVisible(((DefaultListModel)getActiveFields().getModel()).size()-1);
	}
    }
    
    
    /**
     * Adds field to the remove list...
     **/
    private void executeCommandRemove() {
	
	int selectedIndex = getActiveFields().getSelectedIndex();
	/* If any field is selected.. */
	if (selectedIndex != -1) {
	    /* Gets the field to remove... */
	    String field = (String)((DefaultListModel)getActiveFields().getModel()).getElementAt(selectedIndex);
	     /* Removes the field from the add list... */
	    if(_toAdd.contains(field)) {
		_toAdd.remove(field);
	    }
	    
	    /* Adds the field to the remove list if it exists in the original... */
	    if(!_toRemove.contains(field) && _originalExtraList.contains(field)) {
		_toRemove.add(field);
		
		/* Removes the field from the active fields list */
		((DefaultListModel)getActiveFields().getModel()).remove(selectedIndex);
		
		if (selectedIndex == ((DefaultListModel)getActiveFields().getModel()).size())
		    getActiveFields().setSelectedIndex(--selectedIndex);
		else
		    getActiveFields().setSelectedIndex(selectedIndex);
		
		getActiveFields().ensureIndexIsVisible(selectedIndex);
	    }
	}
    }

    /**
     * Adds field to the add list...
     **/
    private void executeCommandAdd() {
	
	/* Gets the field to add... */
	String field = getFieldToAdd().getText().trim();
	/* If it isn't an empty string and does not exist in the active list... */
	if (!field.equals("") && !containsIgnoreCase(getActiveFields(), field)) {
	    /* Adds the field to the add list if it doesn't exists in the original... */
	    if(!_toAdd.contains(field) && !_originalExtraList.contains(field)) {
		_toAdd.add(field);
	    }
	    /* Removes it from the remove list if it exists in the original list... */
	    if(_toRemove.contains(field)) {
		_toRemove.remove(field);
	    }
	    /* Adds the field to the active fields list */
	    ((DefaultListModel)getActiveFields().getModel()).addElement(field);
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
	
	DefaultListModel listActive = (DefaultListModel) getActiveFields().getModel();
	
	/* Removes from database... */
	for (int i = 0; i < _toRemove.size(); i++) {
	    MovieManager.getIt().getDatabase().removeExtraInfoFieldName((String)_toRemove.get(i));
	    fieldsList.remove(_toRemove.get(i));

	}
	
	/* Adds to database... */
	for (int i = 0; i < _toAdd.size(); i++) {
	    if ((MovieManager.getIt().getDatabase().addExtraInfoFieldName((String)_toAdd.get(i))) == 1)
		fieldsList.add(_toAdd.get(i));
	    else {
		MovieManager.getIt().getDatabase().removeExtraInfoFieldName((String)_toAdd.get(i));
		listActive.removeElement(_toAdd.get(i));
	    }
	}
	
	/* Trims to remove any empty spaces */
	fieldsList.trimToSize();
	
	/* Updating the field order */
	
	int [] activeAdditionalInfoFields = new int[listActive.size()];
	
	for (int i = 0; i < listActive.size(); i++)
	    activeAdditionalInfoFields[i] = fieldsList.indexOf(listActive.get(i));
	
	MovieManager.getIt().setActiveAdditionalInfoFields(activeAdditionalInfoFields);
	MovieManager.getIt().getDatabase().setActiveAdditionalInfoFields(activeAdditionalInfoFields);
	
	if (_toRemove.size() != 0 || _toAdd.size() != 0) {
	    ModelAdditionalInfo.hasOldExtraInfoFieldNames = true;
	}
	MovieManagerCommandSelect.execute();
	dispose();
    }
}
