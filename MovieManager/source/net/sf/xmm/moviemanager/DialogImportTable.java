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

package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.models.ModelImportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.swing.util.table.ColumnGroup;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableColumnModel;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableHeader;
import net.sf.xmm.moviemanager.util.CSVParser;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.apache.log4j.Logger;

import com.Ostermiller.util.CSVParse;
import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.table.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class DialogImportTable extends JDialog {

	static Logger log = Logger.getRootLogger();

	final JDialog dialogImportTable = this;
	
	int importType;
	public boolean canceled = false;
	
	JTable table;
	File file;    
	int currentColumn = 0;
	JTableHeader tableHeader;

	
	ArrayList generalInfoFieldNames = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
	ArrayList additionalInfoFieldNames = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
	ArrayList extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();

	MouseAdapter headerPopupListener = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			
			GroupableTableColumnModel columnModel = (GroupableTableColumnModel) tableHeader.getColumnModel();
						
			TableColumn newColumn = columnModel.getColumn(currentColumn);
			TableStringCheckBoxMenuItem src = (TableStringCheckBoxMenuItem) e.getSource();

			newColumn.setHeaderValue(src.getTableString());
			
			/* Already assigned on a different column */
			if (src.getState()) {

				Enumeration enumeration = columnModel.getColumns();
				TableColumn oldColumn;

				while (enumeration.hasMoreElements()) {
					oldColumn = (TableColumn) enumeration.nextElement();

					if (oldColumn.getHeaderValue().equals(src.getTableString())) {
						
						/* Removing the title */
						if (oldColumn.equals(newColumn)) {
							newColumn.setHeaderValue("");
						}
						else {
							oldColumn.setHeaderValue("");
							src.setState(false);
						}
						break;
					}
				}
			}           
			tableHeader.repaint();
		}
	};

	JPopupMenu headerPopupMenu = makeHeaderPopupMenu();


	MouseAdapter tablePopupListener = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {

			if ("Delete".equals(((JMenuItem) e.getSource()).getActionCommand())) {
				int [] rows = table.getSelectedRows();
				DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

				for (int i = rows.length-1; i >= 0; i--) {
					tableModel.removeRow(rows[i]);  
				}

				table.updateUI();
			}
			else if ("OriginalColumnTitles".equals(((JMenuItem) e.getSource()).getActionCommand())) {
				/* Putting row values in the header */

				int columnCount = table.getModel().getColumnCount();
				int row = table.getSelectedRow();

				String tempVal;

				GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
				ColumnGroup tmpGroup;

				ArrayList columnGroups = cm.getColumnGroups();

				TableColumnModel columnModel = tableHeader.getColumnModel();
				
				for (int i = 0; i < columnCount; i++) {
					
					tmpGroup = (ColumnGroup) columnGroups.get(i);
					
					tempVal = (String) table.getModel().getValueAt(row, i);

					tmpGroup.setText(tempVal);
				}
			}
			tableHeader.repaint();
		}
	};

	JPopupMenu tablePopupMenu = makeTablePopupMenu();


	public DialogImportTable(Dialog parent, File file, ModelImportSettings settings) {

		super(parent);

		this.importType = settings.importMode;

		/* Close dialog... */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		/* Enables dispose when pushing escape */
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);

		setTitle("Import Movies");
		setModal(true);

		setTitle("Import");

		this.file = file;

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());        

		try {

			Object [][] data;
			int len = 0;

			if (importType == ModelImportSettings.IMPORT_EXCEL) {

				Workbook workbook = Workbook.getWorkbook(file);
				Sheet sheet = workbook.getSheet(0);
				data = getTableData(sheet);
				len = data[0].length;
			}
			else if (importType == ModelImportSettings.IMPORT_CSV) {

				CSVParse cvsParser;
				
				if (settings.csvSeparator.length() == 0)
					cvsParser = new CSVParser(new FileReader(file));
				else
					cvsParser = new CSVParser(new FileReader(file), settings.csvSeparator.charAt(0));
				//ExcelCSVParser cvsParser = new ExcelCSVParser(new FileReader(file));

				cvsParser = new LabeledCSVParser(cvsParser);
					
				data = cvsParser.getAllValues();
				len = data[0].length;
			}
			else {
				data = null;
			}

			Object [] emptyColumnNames = new Object[len];

			for (int i = 0; i < emptyColumnNames.length; i++)
				emptyColumnNames[i] = " ";

			DefaultTableModel dm = new DefaultTableModel();
			dm.setDataVector(data, emptyColumnNames);

			// Setup table
			table = new JTable( /*dm, new GroupableTableColumnModel()*/);
			table.setColumnModel(new GroupableTableColumnModel());
			table.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel) table.getColumnModel()));
			table.setModel(dm);


			GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();

			ColumnGroup tmpGroup;

			for (int i = 0; i < len; i++) {
				tmpGroup = new ColumnGroup(" ");
				tmpGroup.add(cm.getColumn(i));
				cm.addColumnGroup(tmpGroup);
			}

			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					setTablePopupVisible(event.getX(), event.getY(), event);
				}
			});

			tableHeader = table.getTableHeader();
			tableHeader.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					JTableHeader jth = (JTableHeader) event.getSource();
					int col = jth.columnAtPoint(event.getPoint());

					Rectangle r = jth.getHeaderRect(col);
					if (!jth.getCursor().equals(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR))) {
						currentColumn = col;
						setHeaderPopupVisible(event.getX(), event.getY(), event, col);
					}
				}
			});

			JPanel tablePanel = new JPanel();

			tablePanel.setLayout(new BorderLayout());
			tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
			tablePanel.add(table, BorderLayout.CENTER);

			tablePanel.add(table);
			JScrollPane scroll = new JScrollPane(tablePanel);

			content.add(scroll);

			JPanel panelButtons = new JPanel();

			JButton buttonDone = new JButton("Import Data");

			buttonDone.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
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
						alert.setVisible(true);
						return;
					}
										
					dispose();
				}
			}
			);

			JButton buttonCancel = new JButton("Cancel");
			buttonCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					canceled = true;
					dispose();
				}
			}
			);
			
			panelButtons.add(buttonDone);
			panelButtons.add(buttonCancel);
			
			getContentPane().add(content, BorderLayout.CENTER);
			getContentPane().add(panelButtons, BorderLayout.SOUTH);

			pack();
			setSize(MovieManager.getDialog().getMainSize());

			setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
					(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

		}
		catch (Exception e) {
			log.error("", e);
		}
	}

	public void setHeaderPopupVisible(final int x, final int y, final MouseEvent event, int column) {
		
		if (!SwingUtilities.isRightMouseButton(event))
			return;

		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				headerPopupMenu.show((JTableHeader) event.getSource(), x, y);
			}
		});
	}

	public JPopupMenu makeHeaderPopupMenu() {

		JPopupMenu popupMenu = new JPopupMenu();

		JCheckBoxMenuItem temp;

		JMenu movieInfoMenu = new JMenu("Movie Info");
		popupMenu.add(movieInfoMenu);

		for (int i = 0; i < generalInfoFieldNames.size(); i ++) {
			temp = new TableStringCheckBoxMenuItem(new FieldModel("General Info", (String) generalInfoFieldNames.get(i)));
			temp.addMouseListener(headerPopupListener);
			movieInfoMenu.add(temp);
		}

		popupMenu.add(new JPopupMenu.Separator());

		JMenu fileInfo = new JMenu("File Info");
		popupMenu.add(fileInfo);

		for (int i = 0; i < additionalInfoFieldNames.size(); i ++) {
			temp = new TableStringCheckBoxMenuItem(new FieldModel("Additional Info", (String) additionalInfoFieldNames.get(i)));
			temp.addMouseListener(headerPopupListener);
			fileInfo.add(temp);
		}

		popupMenu.add(new JPopupMenu.Separator());

		JMenu extraInfo = new JMenu("Extra Fields");
		popupMenu.add(extraInfo);

		for (int i = 0; i < extraInfoFieldNames.size(); i ++) {
			temp = new TableStringCheckBoxMenuItem(new FieldModel("Extra Info", (String) extraInfoFieldNames.get(i)));
			temp.addMouseListener(headerPopupListener);
			extraInfo.add(temp);
		}

		return popupMenu;
	}


	public void setTablePopupVisible(final int x, final int y, final MouseEvent event) {
		// GUIUtil.show(popupMenu, true);

		if (!SwingUtilities.isRightMouseButton(event))
			return;

		int row = table.rowAtPoint(new Point(x, y));

		if (!table.isRowSelected(row))
			return;

		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				tablePopupMenu.show((JTable) event.getSource(), x, y);
			}
		});
	}



	public JPopupMenu makeTablePopupMenu() {

		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem deleteRow = new JMenuItem("Delete row");
		deleteRow.addMouseListener(tablePopupListener);
		deleteRow.setActionCommand("Delete");
		popupMenu.add(deleteRow);

		JMenuItem originalColumnTitles = new JMenuItem("Original Column Titles");
		originalColumnTitles.addMouseListener(tablePopupListener);
		originalColumnTitles.setActionCommand("OriginalColumnTitles");
		popupMenu.add(originalColumnTitles);

		return popupMenu;
	}


	protected Object[][] getTableData(Sheet sheet) throws Exception {

		Object[][] tableData = null;

		try {

			tableData = new Object[sheet.getRows()][sheet.getColumns()];

			log.debug("tableData.length:" + tableData.length);

			Cell[] cells;

			for (int i = 0; i < sheet.getRows(); i++) {

				cells = sheet.getRow(i);

				for (int u = 0; u < cells.length; u++) {
					tableData[i][u] = cells[u].getContents();
				}
			}
		}
		catch (Exception e) {
			log.error("", e);
		}

		return tableData;
	}

	public ModelImportSettings getSettings() {
		return new ModelImportSettings(table);
	}


	public class FieldModel {

		String table;
		String field;
		String value;

		TableStringCheckBoxMenuItem checkBox = null;

		FieldModel(String table, String field) {
			this.table = table;
			this.field = field;
		}

		public String getTable() {
			return table;
		}

		public String getField() {
			return field;
		}

		public void setValue(String value) {
			this.value = value == null ? "" : value;
		}

		public String getValue() {
			return value;
		}

		public void validateValue() {
		
			if (table.equals("General Info")) {

				// Should be an integer
				if (!value.equals("") && field.equalsIgnoreCase("Imdb")) {
					try {
						Integer.parseInt(value);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a valid imdb id.\nValue ignored");
						value = "";
					}
					
				}
				// Rating should be a double
				else if (field.equalsIgnoreCase("Rating")) {
					try {
						Double.parseDouble(value);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a valid rating.\nValue ignored");
						value = "";
					}					
				}
			}

			else if (table.equals("Additional Info")) {

				// Should be a double
				if (!value.equals("") && (field.equalsIgnoreCase("Video Rate") || field.equalsIgnoreCase("Audio Rate"))) {
					try {
						Double.parseDouble(value);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a integer as it should be.");
						value = StringUtil.cleanInt(value, 0);
						log.warn("Value trimmed to:" + value);
					}	
				}
				// Should be an integer
				else if (field.equalsIgnoreCase("Duration") || 
						field.replaceFirst("_", " ").equalsIgnoreCase("File Size") ||
						field.equalsIgnoreCase("CDs") || 
						field.replaceFirst("_", " ").equalsIgnoreCase("CD Cases") || 
						field.replaceFirst("_", " ").equalsIgnoreCase("File Count") ||
						field.replaceFirst("_", " ").equalsIgnoreCase("Video Bit Rate") ||
						field.replaceFirst("_", " ").equalsIgnoreCase("Audio Bit Rate")) {

					String tmpVal = value;
					
					if (tmpVal.equals(""))
						tmpVal = "0";
					
					try {
						Integer.parseInt(tmpVal);
						
						// The fields Duration, File Size, CDs, CD Cases and File Count require a valid integer
						if (!field.replaceFirst("_", " ").equalsIgnoreCase("Video Bit Rate") &&
								!field.replaceFirst("_", " ").equalsIgnoreCase("Audio Bit Rate"))
							value = tmpVal;
						
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a integer as it should be.");
						value = StringUtil.cleanInt(value, 0);
						log.warn("Value trimmed to:" + value);
					}	
				}

				// Duration is most probably in Minutes, converts to seconds
				if (field.equalsIgnoreCase("Duration")) {

					int intVal = Integer.parseInt(value);	
					intVal *= 60;
					value = String.valueOf(intVal);
				}
			}
		}


		public TableStringCheckBoxMenuItem getCheckBoxMenuItem() {
			return checkBox;
		}


		public String toString() {
			return field;
		}
	}

	class TableStringCheckBoxMenuItem extends JCheckBoxMenuItem {

		FieldModel fieldModel;

		TableStringCheckBoxMenuItem(FieldModel tableString) {
			super(tableString.toString());
			this.fieldModel = tableString;
			tableString.checkBox = this;
		}

		public FieldModel getTableString() {
			return fieldModel;
		}
	}
}
