package net.sf.xmm.moviemanager;



import net.sf.xmm.moviemanager.models.ModelImportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.swing.util.table.*;
import net.sf.xmm.moviemanager.util.GUIUtil;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class DialogImportTable extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    JTable table;
    File file;    
    int currentColumn = 0;
    JTableHeader tableHeader;
    
    ArrayList generalInfoFieldNames = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
    ArrayList additionalInfoFieldNames = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
    ArrayList extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames();
    
    MouseAdapter headerPopupListener = new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
           
           TableColumnModel columnModel = tableHeader.getColumnModel();
           
           TableColumn newColumn = columnModel.getColumn(currentColumn);
           TableColumn oldColumn;
           
           TableStringCheckBoxMenuItem src = (TableStringCheckBoxMenuItem) e.getSource();
           newColumn.setHeaderValue(src.getTableString());
           //String name = src.getText();
           
           /* Already asigned */
           if (src.getState()) {
                              
               Enumeration enumeration = columnModel.getColumns();
               
               while (enumeration.hasMoreElements()) {
                   oldColumn = (TableColumn) enumeration.nextElement();
                   
                   if (oldColumn.getHeaderValue().equals(src.getTableString())) {
                       
                       /* Removing the title */
                       if (oldColumn.equals(newColumn))
                           newColumn.setHeaderValue("");
                       else {
                           oldColumn.setHeaderValue("");
                           src.setState(false);   
                       }
                           
                       break;
                   }
               }
           }           
           tableHeader.updateUI();
       }
    };
    
    JPopupMenu headerPopupMenu = makeHeaderPopupMenu();
    
    
    MouseAdapter tablePopupListener = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            
            System.err.println("table mousePressed:" + ((JMenuItem) e.getSource()).getActionCommand());
            
            if ("Delete".equals(((JMenuItem) e.getSource()).getActionCommand())) {
                int [] rows = table.getSelectedRows();
                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                
                System.err.println("getRowCount:"  + tableModel.getRowCount());
                System.err.println("selectedRows:"  + rows.length);
                
                for (int i = rows.length-1; i >= 0; i--) {
                    tableModel.removeRow(rows[i]);  
                    System.err.println("deleted row:" + rows[i]);
                }
                
                table.updateUI();
            }
            else if (1 == 0){
                /* Putting row values in the header */
                
                int columnCount = table.getModel().getColumnCount();
                int row = table.getSelectedRow();
                
                System.err.println("columnCount:" + columnCount);
                
                TableColumnModel columnModel = table.getColumnModel();
                
                TableColumn column;
                String tempVal;
                
                GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
                GroupableTableHeader header = (GroupableTableHeader)table.getTableHeader();
                ColumnGroup g_name;
                
                for (int i = 0; i < columnCount; i++) {
                    column = columnModel.getColumn(i);
                    tempVal = (String) table.getModel().getValueAt(row, i);
                
                    //column.setHeaderValue(tempVal);
                    g_name = new ColumnGroup(tempVal);
                    g_name.add(cm.getColumn(i));
                    //cm.addColumnGroup(g_name);
                    
                    cm.addColumnGroup(g_name);
                    
                    //System.err.println(tempVal);
                }
                
                header.setColumnModel(cm);
                
                table.getTableHeader().resizeAndRepaint();
                table.updateUI();
                      
                //pack();
                
                // Setup Column Groups
                /*
                GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
                ColumnGroup g_name = new ColumnGroup( "Name");
                g_name.add(cm.getColumn(1));
                
                ColumnGroup g_token = new ColumnGroup(new GroupableTableCellRenderer(), "Second");
                g_name.add(g_token);
                g_token.add(cm.getColumn(2));
                g_token.add(cm.getColumn(3));
                ColumnGroup g_lang = new ColumnGroup("Language");
                g_lang.add(cm.getColumn(4));
                ColumnGroup g_other = new ColumnGroup("Others");
                g_other.add(cm.getColumn(5));
               
                GroupableTableHeader header = (GroupableTableHeader)table.getTableHeader();
                cm.addColumnGroup(g_name);
                cm.addColumnGroup(g_lang);
                */
                
                
            }
        }
    };
    
    JPopupMenu tablePopupMenu = makeTablePopupMenu();
    
    
    public DialogImportTable(Dialog parent, File file) {
        
        super(parent);
        
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
            public void actionPerformed(ActionEvent e) {
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
            
            Workbook workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            
            DefaultTableModel dm = new DefaultTableModel();
            
            Object [][] data = getTableData(sheet);
            Object [] emptyColumnNames = new Object[data[0].length];
            
            for (int i = 0; i < emptyColumnNames.length; i++)
                emptyColumnNames[i] = " ";
                
            dm.setDataVector(data, emptyColumnNames);
            
//          Setup table
            table = new JTable( /*dm, new GroupableTableColumnModel()*/);
            //table.setColumnModel(new GroupableTableColumnModel());
            //table.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel) table.getColumnModel()));
            table.setModel(dm);
            
            
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
                    
                    
                    System.err.println("col:" + col);
                    currentColumn = col;
                    
                    setHeaderPopupVisible(event.getX(), event.getY(), event, col);
                }
              }
            });
            
           
            
            JScrollPane scroll = new JScrollPane( table );
            
            content.add(scroll);
            
            
            
            JPanel panelButtons = new JPanel();
            
            JButton buttonDone = new JButton("Import Data");
            
            buttonDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            }
            );
            
            panelButtons.add(buttonDone);
            
            getContentPane().add(content, BorderLayout.CENTER);
            getContentPane().add(panelButtons, BorderLayout.SOUTH);
            
            pack();
            setSize( 800, 200 );
                            
            setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
                    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
            
        }
        catch (Exception e) {
            log.error("", e);
        }
    }
    
    public void setHeaderPopupVisible(final int x, final int y, final MouseEvent event, int column) {
        // GUIUtil.show(popupMenu, true);
        
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
        
        /*
        if (table.getSelectedRowCount() > 1)
            tablePopupMenu.getComponent(1).setEnabled(false);
        else
            tablePopupMenu.getComponent(1).setEnabled(true);
        */
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
                
        /*
        JMenuItem originalColumnTitles = new JMenuItem("Original Column Titles");
        originalColumnTitles.addMouseListener(tablePopupListener);
        popupMenu.add(originalColumnTitles);
        */
        
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
                    //System.err.println(cells[u].getContents());
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
    
    void importData() {
        
        //ModelImportSettings settings = new ModelImportSettings(table);
        
        TableModel tableModel = table.getModel();
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = table.getModel().getColumnCount();
        
        ModelMovieInfo movieInfo = new ModelMovieInfo(false, true);
        TableColumn tmpColumn;
        FieldModel fieldModel;
        String tableValue;
        ModelMovie tmpMovie;
        boolean valueStored = false;
        
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            
            tmpMovie = new ModelMovie();
            
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                
                tmpColumn = columnModel.getColumn(columnIndex);
                Object val = tmpColumn.getHeaderValue();
                
                if (! (val instanceof FieldModel)) {
                    System.err.println("val not instanceof FieldModel:" + val);
                    continue;
                }
                
                //System.err.println("VAL instanceof FieldModel:" + val);
                
                fieldModel = (FieldModel) val;
                
                // column has been assigned an info field 
                if (!fieldModel.toString().trim().equals("")) {
                    
                    tableValue = (String) table.getModel().getValueAt(row, columnIndex);
                    fieldModel.setValue(tableValue);
                    
                    if (tmpMovie.setValue(fieldModel)) {
                        valueStored = true;
                        System.err.println("Saved:" + fieldModel);
                    }
                   // System.err.println(fieldModel);
                }
            }
            
            /* At least one value has been saved */
            if (valueStored) {
                movieInfo.setModel(tmpMovie, false, false);
                
                try {
                    movieInfo.saveToDatabase(null);
                } catch (Exception e) {
                    log.error("Failed to save movie:" + tmpMovie.getTitle(), e);
                }
                }
        }
    }

    public class FieldModel {
        
        String table;
        String field;
        String value;
        
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
           this.value = value;
        }
        
        public String getValue() {
            return value;
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
        }
        
        public FieldModel getTableString() {
            return fieldModel;
        }
    }
}
