/**
 * @(#)DialogImportTable.java 29.01.06 (dd.mm.yy)
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.swing.util.table.ColumnGroup;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableColumnModel;

import org.apache.log4j.Logger;

public class DialogTableExport extends DialogTableData {

	static Logger log = Logger.getRootLogger();
	
	DefaultTableModel tableModel;
	Object [][] databaseData;
	Object [][] tableData;
	
	public int titleColumnIndex = -1;
	
	public DialogTableExport(JFrame parent, Object [][] data, ModelImportExportSettings settings) {

		super(parent, settings);
		this.databaseData = data;
		
		setTitle("Export " + ModelImportExportSettings.importTypes[settings.mode]);
		setModal(true);

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());        

		try {

			int rowLen = data.length;
			int colsLen = data[0].length;

			Object [] emptyColumnNames = new Object[rowLen];

			for (int i = 0; i < emptyColumnNames.length; i++)
				emptyColumnNames[i] = " ";

			tableData = new Object[rowLen][colsLen];
			tableModel = new DefaultTableModel();
			//tableModel.setDataVector(data, emptyColumnNames);
			tableModel.setDataVector(tableData, emptyColumnNames);

			table.setModel(tableModel);


			GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
			ColumnGroup tmpGroup;

			for (int i = 0; i < colsLen; i++) {
				tmpGroup = new ColumnGroup(" ");
				tmpGroup.add(cm.getColumn(i));
				cm.addColumnGroup(tmpGroup);
			}

			buttonDone.setText("Export Data");
			buttonDone.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
					/*
					GroupableTableColumnModel columnModel = (GroupableTableColumnModel) tableHeader.getColumnModel();
					int columnCount = table.getModel().getColumnCount();
					
					TableColumn newColumn = columnModel.getColumn(currentColumn);
					boolean titleFound = false;
					
					for (int i = 0; i < columnCount; i++ ) {
						newColumn = columnModel.getColumn(i);
						
						if ("Title".equals(newColumn.getHeaderValue().toString()))
							titleFound = true;
					}
					
					if (!titleFound) {
						DialogAlert alert = new DialogAlert(dialogImportTable, "Title column missing", "Title column must be specified");
						GUIUtil.show(alert, true);
						return;
					}
					*/
					
					dispose();
				}
			}
			);
		}
		catch (Exception e) {
			log.error("", e);
		}
	}
	

	
	
	
	
	public Object [][] retrieveValuesFromTable() {

		Object [][] output = null;
		
		try {
			
			TableModel tableModel = table.getModel();
			TableColumnModel columnModel = table.getColumnModel();
			int columnCount = table.getModel().getColumnCount();

			TableColumn tmpColumn;
			FieldModel fieldModel;
			
			// Finding columns with values
			
			ArrayList columns = new ArrayList();
			
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				tmpColumn = columnModel.getColumn(columnIndex);
				
				Object o = tmpColumn.getHeaderValue();
				
				if (o instanceof String) {
					continue;
				}
					
				fieldModel = (FieldModel) o;

				// column has been assigned an info field 
				if (!fieldModel.toString().trim().equals("")) {
					columns.add(new Integer(columnIndex));
					
					if (fieldModel.toString().trim().equals("Title")) {
						titleColumnIndex = columnIndex;
					}
				}
			}
			
			output = new String[tableModel.getRowCount()][columns.size()];
			
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
				
				for (int row = 0; row < tableModel.getRowCount(); row++) {
					int colIndex = ((Integer) columns.get(columnIndex)).intValue();
					output[row][columnIndex] = (String) table.getModel().getValueAt(row, colIndex);
				
					if (colIndex == titleColumnIndex)
						titleColumnIndex = columnIndex;
				}
			}
		}
		catch (Exception e) {
			log.error("", e);
		}	
		
		return output;
	}
	
	
	
	
	
	public void updateColumnData(int oldModelIndex, int currentColumn, int initialColumnindex) {
		
		System.err.println("updateColumnData");
		
		
		
		if (oldModelIndex != -1) {
			// Removes data in old column
			for (int i = 0; i < databaseData.length; i++) {
				tableModel.setValueAt("", i, oldModelIndex);
			}
		}
		
		System.err.println("databaseData:" + databaseData);
		System.err.println("databaseData size:" + databaseData.length);
		
//		 Adds data to new column
		for (int i = 0; i < databaseData.length; i++) {
			System.err.println("set value:" + databaseData[i][initialColumnindex]);
			tableModel.setValueAt(databaseData[i][initialColumnindex], i, currentColumn);
		}
	}
}
