/**
 * @(#)DialogQueries.java 1.0 28.01.06 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.models.ModelQuery;

import org.apache.log4j.Logger;


public class DialogQueries extends JDialog {
    
    static Logger log = Logger.getRootLogger();
    
    JPanel panelResult;
    
    /**
     * The Constructor.
     **/
    
    public DialogQueries() {
        
        /* Dialog creation...*/
        super(MovieManager.getIt());
        /* Close dialog... */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        /* Dialog properties...*/
        setTitle("Queries");
        setModal(true);
        setResizable(false);
        
        /*Enables dispose when pushing escape*/
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
        
        /* Execute panel...*/
        JPanel panelExecute = new JPanel();
        panelExecute.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Execute "),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
	if (MovieManager.getConfig().getDisplayQueriesInTree()) {
            createExecuteAsTree(panelExecute);
        }
        else {
           createExecuteAsList(panelExecute); 
        }        
	
        /* Result panel...*/
        panelResult = new JPanel();
        panelResult.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Result "),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        JTextArea textAreaResult = new JTextArea();
        textAreaResult.setEditable(false);
        textAreaResult.setLineWrap(false);
        JScrollPane scrollPaneResult = new JScrollPane(textAreaResult);
        scrollPaneResult.setPreferredSize(new Dimension(400,350));
        panelResult.add(scrollPaneResult);
        
        
        /* All stuff together... */
        JPanel all = new JPanel();
        all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        all.setLayout(new GridBagLayout());
        all.add(panelExecute);
        all.add(panelResult);
        /* Buttons panel...*/
        JPanel panelButtons = new JPanel();
        panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonOk = new JButton("OK");
        buttonOk.setToolTipText("Close the Queries dialog");
        buttonOk.setActionCommand("Queries - OK");
        buttonOk.addActionListener(new CommandDialogDispose(this));
        panelButtons.add(buttonOk);
        /* Adds all and buttonsPanel... */
        getContentPane().add(all,BorderLayout.NORTH);
        getContentPane().add(panelButtons,BorderLayout.SOUTH);
        /* Packs and sets location... */
        pack();
        setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
                (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
    }
    
    private void createExecuteAsList(JPanel panelExecute) {
        
        JList listExecute = new JList();
        listExecute.setFont(new Font(listExecute.getFont().getName(),Font.PLAIN,listExecute.getFont().getSize()));
        listExecute.setLayoutOrientation(JList.VERTICAL);
        listExecute.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listExecute.setModel(createListModel());
        listExecute.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
		    log.debug("ActionPerformed: Queries - Execute");
                    
                    String query = ((ModelQuery)getExecuteList().getModel().getElementAt(getExecuteList().getSelectedIndex())).getQuery();
                    executeCommandQuery(query);
                }
            }});
            listExecute.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent event) {
                    log.debug("ActionPerformed: Queries - Execute");
		    
                    String query = ((ModelQuery)getExecuteList().getModel().getElementAt(getExecuteList().getSelectedIndex())).getQuery();
                    executeCommandQuery(query);
                }});
                JScrollPane scrollPaneExecute = new JScrollPane(listExecute);
                scrollPaneExecute.setPreferredSize(new Dimension(200,350));
                panelExecute.add(scrollPaneExecute);
                
    }
    
    private void createExecuteAsTree(JPanel panelExecute) {
        
        JTree jTree = new JTree();
        jTree.setFont(new Font(jTree.getFont().getName(),Font.PLAIN,jTree.getFont().getSize()));
	
	jTree.setRootVisible(false);
	jTree.setShowsRootHandles(true);

        jTree.setModel(createTreeModel());
        
        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent event) {
		log.debug("ActionPerformed: Queries(from tree) - Execute");
                String query = getExecuteQuery();
                
                executeCommandQuery(query);
            }
        });
        
        JScrollPane scrollPaneExecute = new JScrollPane(jTree);
        scrollPaneExecute.setPreferredSize(new Dimension(200,350));
        panelExecute.add(scrollPaneExecute);
    }
    
    
    /**
     * Returns the JList listExecute. 
     **/
    
    protected JList getExecuteList() {
        Object jObj;
        jObj = ((JScrollPane)
                ((JPanel)
		 ((JPanel)
		  getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
	return (JList)jObj; 
    }
   
    /**
     * Returns the Query to Execute. 
     **/
    protected String getExecuteQuery() {
        try {
            Object jObj;
            jObj = ((JScrollPane)
                    ((JPanel)
		     ((JPanel)
		      getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getViewport().getComponent(0);
            if (jObj instanceof JList) {
                return "";   //(JList)jObj;
            }
            else if (jObj instanceof JTree) {     
                JTree jTree = (JTree)jObj;
                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
		
		if (currentNode != null && (currentNode.getUserObject() instanceof ModelQuery)) {
		    ModelQuery mq = (ModelQuery) currentNode.getUserObject();  
		    return mq.getQuery();
		}
	    }
	    else {
		return ""; // if we come here we have an error.
            }
        }
        catch (Exception e) {
	    log.error("Exception: "+ e);
	}
        return "";
    }
    
    /**
     * Returns JTextArea textAreaResult.
     **/
    protected JTextArea getResult() {
        return
	    (JTextArea)
	    ((JScrollPane)
	     ((JPanel)
	      ((JPanel)
	       getContentPane().getComponent(0)).getComponent(1)).getComponent(0)).getViewport().getComponent(0);
    }
    

    
    private DefaultListModel getDefaultQueries() { 
	
	BufferedReader reader;
	String name, query, line;
	
	DefaultListModel listModel = new DefaultListModel();
	
	/* Gets the default queries... */
	String[] defaultQueries = {"CountCDCases.qry",
				   "CountCDs.qry",
				   "CountMovies.qry",
				   "SumDurationsAccess.qry",
				   "SumGB.qry",
				   "MoviesWithSubTitles.qry",
				   "UnseenMovies.qry",
				   "UnseenMoviesWithSubTitles.qry"};
	    
	String queriesPath = "/queries/";
            
	/* Need a different sum duration query for hsql database */
	if (MovieManager.getIt().getDatabase() instanceof DatabaseHSQL)
	    defaultQueries[3] = "SumDurationsHSQL.qry";
	else if (MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) {
	    /* Need a different queries for mysql database */
	    queriesPath = "/queries/MySQL/";
	    defaultQueries[3] = "SumDurationsMySQL.qry";
	}
	
	try {
	    /* Adding the default queries to the list */
	    for (int i = 0; i < defaultQueries.length; i++) {
                
		reader = new BufferedReader(new InputStreamReader(MovieManager.getIt().getResourceAsStream(queriesPath + defaultQueries[i])));
		name = reader.readLine();
		query = "";
                
		while ((line = reader.readLine()) != null) {
		    query = query + line;
		    if (!query.endsWith(" ")) {
			query = query + " ";
		    }
		}
		listModel.addElement(new ModelQuery(name, query));
	    }
	
	} catch (Exception e) {
	    log.error("Exception: "+ e);
        }
	
	return listModel;
    }
    
    
    /**
     * Creates a new model for the list execute.
     **/
    private DefaultListModel createListModel() {
        DefaultListModel listModel = new DefaultListModel();
        try {
            /* Used vars... */
            BufferedReader reader;
            String name, query, line;
	    
	    /* Getting default queries */
            DefaultListModel defaultQueries = getDefaultQueries();
	    
	    for (int i = 0; i < defaultQueries.size(); i++)
		listModel.addElement(defaultQueries.get(i));
	    
            /* Gets the queries dir... */
	    File directory = new File(MovieManager.getConfig().getQueriesPath());
	    
	    /* Checks if it is a directory... */
            if (directory.isDirectory()) {
                
                /* Gets an array of the files in the dir... */
                File[] queryFiles = directory.listFiles();
                
                /* Creates the model... */
                for (int i = 0; i < queryFiles.length; i++) {
                    
		    if (queryFiles[i].isFile() && queryFiles[i].canRead() && queryFiles[i].getName().endsWith(".qry")) {
                        reader = new BufferedReader(new FileReader(queryFiles[i]));
                        name = reader.readLine();
                        query = "";
                        while ((line = reader.readLine()) != null) {
                            query = query + line;
                            if (!query.endsWith(" ")) {
                                query = query + " ";
                            }
                        }
                        listModel.addElement(new ModelQuery(name,query));
                    }
                }
            }
            
            /* Sorts the list... */
            for (int i=1; i<listModel.getSize(); i++) {
                Object qry = listModel.get(i);
                int j = i;
                while (j > 0 && listModel.get(j-1).toString().compareTo(qry.toString()) >= 0) {
                    listModel.set(j,listModel.get(--j));
                }
                listModel.set(j, qry);
            }
        } catch (Exception e) {
	    log.error("Exception: "+ e);
        }
        /* Returns the model... */
        return listModel;
        
    }
    
    
    /**
     * Creates a new model for the list execute.
     **/
    private DefaultTreeModel createTreeModel() {
	DefaultMutableTreeNode top = new DefaultMutableTreeNode("Queries");
        
	DefaultMutableTreeNode defaultNode = new DefaultMutableTreeNode("Default");
	
	/* Getting the default queries */
	DefaultListModel defaultQueries = getDefaultQueries();
	
	for (int i = 0; i < defaultQueries.size(); i++)
	    defaultNode.add(new DefaultMutableTreeNode(defaultQueries.get(i)));
	
	top.add(defaultNode);
	
        File directory;
	
	directory = new File(MovieManager.getConfig().getQueriesPath());
	
	if (directory != null && directory.isDirectory()) {
            createTreeModel_listDirContents(top, directory, 0);
        }
	
        DefaultTreeModel t = new DefaultTreeModel(top);
      
        return t;
    }
    
    private void createTreeModel_listDirContents(DefaultMutableTreeNode pTop, File someDirectory, int depth) {
        if (depth > 10) return;     // arbitary safeguard - nested folders, no more then 10 deep
        String[] fileOrDirName = someDirectory.list(); // list of files and dirs?
        java.util.Arrays.sort(fileOrDirName);     // ought to have all folders together at the top
            
        for (int i = 0; i < fileOrDirName.length; i++) {
            
            File f = new File(someDirectory, fileOrDirName[i]);
            if (f.isDirectory()) {
                DefaultMutableTreeNode top1 
                        = new DefaultMutableTreeNode(new ModelQuery(f.getName(), ""));
                pTop.add(top1);
                createTreeModel_listDirContents(top1, f, depth+1); // Recursively list contents of dir
            } 
            else {    
                pTop.add(new DefaultMutableTreeNode(
                        new ModelQuery(
                            f.getName().toString().substring(0, f.getName().toString().length()-4),
                            loadQuery(f)
                        )));                    
            }
        }
    }
        
    private String loadQuery(File f) {
        String query = "";
        if ( f.canRead() && f.getName().endsWith(".qry")) {
            try {
                BufferedReader reader;
                String line;                    
                reader = new BufferedReader(new FileReader(f));
                reader.readLine(); // reading name
                query = "";
                while ((line = reader.readLine()) != null) {
                    query = query + line;
                    if (!query.endsWith(" ")) {
                        query = query + " ";
                    }
                }
            }
            catch (Exception e) {
		log.error("Exception: "+ e);
            }
        }
        return query; 
    }
    
    /**
     * Executes the selected query.
     **/
    private void executeCommandQuery(String query) {
        
        
        /* if no query then set results to blank */
        if (query == "") {
            getResult().setText("");
            panelResult.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Result "),
                    BorderFactory.createEmptyBorder(5,5,5,5)));            
            return;
        }
	
        getResult().setText(MovieManager.getIt().getDatabase().getQueryResult(query));
        getResult().setCaretPosition(0);
	
        if (MovieManager.getIt().getDatabase().getRecordCount() != 1) {
            panelResult.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												      " Result - " + 
												      MovieManager.getIt().getDatabase().getRecordCount() + 
												      " records returned"),
								     BorderFactory.createEmptyBorder(5,5,5,5)));
        } else {
            panelResult.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Result - " + 
												      MovieManager.getIt().getDatabase().getRecordCount() + 
												      " record returned"),
								     BorderFactory.createEmptyBorder(5,5,5,5)));
        }
    }
}
