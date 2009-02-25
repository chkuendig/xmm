/**
 * @(#)FileTree.java 1.0 26.09.06 (dd.mm.yy)
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

/**
 * Based on code by Matthew Robinson, Pavel Vorobiev, Swing, Second Edition
 */


package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

public class FileTree extends JPanel {

	static Logger log = Logger.getLogger(FileTree.class);

	public AddSelectedFilesEventHandler eventHandler = new AddSelectedFilesEventHandler();

	public static final ImageIcon ICON_DISK = getImageIcon("Disk.png");
	public static final ImageIcon ICON_DISK_INCLUDE_CONTENT = getImageIcon("Disk_include_content.png");
	public static final ImageIcon ICON_DISK_INCLUDE_ALL = getImageIcon("Disk_include_all.png");
	public static final ImageIcon ICON_DISK_EXCLUDE_ALL = getImageIcon("Disk_exclude_all.png");

	public static final ImageIcon ICON_FOLDER = getImageIcon("Folder.png");
	public static final ImageIcon ICON_FOLDER_INCLUDE_CONTENT = getImageIcon("Folder_include_content.png");
	public static final ImageIcon ICON_FOLDER_INCLUDE_ALL = getImageIcon("Folder_include_all.png");
	public static final ImageIcon ICON_FOLDER_EXCLUDE_ALL = getImageIcon("Folder_exclude_all.png");

	public static final ImageIcon ICON_MEDIA_FILE = getImageIcon("add.png");

	protected JTree fileTree;
	protected DefaultTreeModel modelTree;
	protected JTextField m_display;

	MatchingOptions matchOptions = new MatchingOptions();
	
	private Color colorMatch = new Color(152, 225, 120);
	private Color colorNoMatch = new Color(233, 180, 180);
	private Color colorExists = new Color(0, 0, 0);

		
	HashMap existingMediaFiles = null;
	boolean filterOutDuplicateFiles = true;
	
	
	private boolean threadLock = false;
	
	public DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) fileTree.getModel();
	}

	/* 
	 * Key = filepath, obj = IconData
	 */
	private HashMap changedNodes = new HashMap();

	private HashMap visibleNodes = new HashMap();
	private HashMap expandedNodes = new HashMap();

	public void addVisibleNode(DefaultMutableTreeNode node) {
		visibleNodes.put(node, node);
	}
	
	public void removeVisibleNode(DefaultMutableTreeNode node) {
		visibleNodes.remove(node);
	}
	
	public void addExpandedNode(FileNode node) {
		expandedNodes.put(node, node);
	}
		
	protected JPopupMenu dir_popup;
	protected JPopupMenu file_popup;
	protected Action dir_action;
	protected Action file_action;
	protected TreePath clickedPath;
	protected JMenuItem fileItem;

	JMenuItem excludeAll, includeAll, folder, includeContent, addFiles = null;
		
	ArrayList validExtensions = new ArrayList();
	
	public void setValidExtension(ArrayList validExtensions) {
		this.validExtensions = validExtensions;
		updateNodes();
	}
		
	public void addValidExtension(String validExtension) {
		validExtensions.add(validExtension);
		//Update all expanded Nodes
		updateNodes();
	}
	
	public void removeValidExtension(String validExtension) {
		validExtensions.remove(validExtension);
		updateNodes();
	}
	
	public void updateNodes() {
			
		Iterator it = expandedNodes.keySet().iterator();
		
		while (it.hasNext()) {
			
			FileNode fileNode = (FileNode) it.next();
			// If children were changed
			DefaultMutableTreeNode node = fileNode.updateNodes(validExtensions);
			
			if (node != null) {
				((DefaultTreeModel) fileTree.getModel()).nodeChanged(node);
			}
		}
	}
	
	public ArrayList getValidExtension() {
		return validExtensions;
	}
	
	public void setFilterOutDuplicates(boolean fuplicatesEnabled) {
		filterOutDuplicateFiles = fuplicatesEnabled;
		updateCurrentCells();
	}
	
	public FileTree(HashMap existingMediaFiles) {
		this();
		this.existingMediaFiles = existingMediaFiles;
	}

	public FileTree() {
		
		setSize(600, 1000);

		DefaultMutableTreeNode node;
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(0, -1, new FileNode(new File("Computer"), this)));
		
		// If windows, all the hard drives are listed.
		// On Linux, only the home directory

		File[] roots = File.listRoots();

		// Getting home $HOME variable
		if (!SysUtil.isWindows()) {
			String home = System.getProperty("user.home");
			roots = new File[1];
			roots[0] = new File(home);
		}


		for (int k = 0; k < roots.length; k++) {

			IconData tmp = new IconData(0, -1, new FileNode(roots[k], this));
			tmp.setDisk(true);
			node = new DefaultMutableTreeNode(tmp);

			top.add(node);
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		modelTree = new DefaultTreeModel(top);
		fileTree = new JTree(modelTree);


		fileTree.putClientProperty("JTree.lineStyle", "Angled");
		fileTree.setCellRenderer(new IconCellRenderer(colorMatch, colorNoMatch, colorExists));
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		fileTree.addTreeExpansionListener(new DirExpansionListener());
		fileTree.addTreeSelectionListener(new DirSelectionListener());
		
		fileTree.setRootVisible(false);
		fileTree.setShowsRootHandles(true); 
		fileTree.setEditable(false);
		fileTree.setLargeModel(true);
		
		setLayout(new BorderLayout());

		JScrollPane treeScroll = new JScrollPane();
		treeScroll.getViewport().add(fileTree);
		add(treeScroll, BorderLayout.CENTER);
			
		// add file popup
		file_popup = new JPopupMenu();

		fileItem = new JMenuItem();
		fileItem.setIcon(getImageIcon("add.png", 20));
		file_popup.add(fileItem);

		fileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eventHandler.fireAddSelectedFilesEvent(new AddSelectedFilesEvent((Object) fileItem));
			}
		});
		
		
		dir_popup = new JPopupMenu();
		
		folder = new JMenuItem("Folder");
		folder.setIcon(getImageIcon("Folder.png", 20));
		folder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.repaint();
				setFolderIcon(IconData.REGULAR_FOLDER);
			}
		});
				
		dir_popup.add(folder);

		
		includeContent = new JMenuItem("Include only folder content");
		includeContent.setIcon(getImageIcon("Folder_include_content.png", 20));
		includeContent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				fileTree.repaint();
				setFolderIcon(IconData.INCLUDE_CONTENT);
			}
		});
		dir_popup.add(includeContent);
		
		includeAll = new JMenuItem("Include folder content and subdirectories");
		includeAll.setIcon(getImageIcon("Folder_include_all.png", 20));
		includeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				fileTree.repaint();
				setFolderIcon(IconData.INCLUDE_ALL);		
			}
		});
		
		dir_popup.add(includeAll);

		

		excludeAll = new JMenuItem("Exclude folder content and subdirectories");
		excludeAll.setIcon(getImageIcon("Folder_exclude_all.png", 20));
		excludeAll.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e)		{
				fileTree.repaint();
				setFolderIcon(IconData.EXCLUDE_ALL);					
			}
		});
		
		dir_popup.add(excludeAll);

		fileTree.add(dir_popup);
		fileTree.addMouseListener(new PopupTrigger());
	}

	

	void updateCurrentCells() {

		if (threadLock)
			return;
		
		threadLock = true;
		
		Runnable run = new Runnable() {

			public void run() {

				Set set = visibleNodes.keySet();
				Iterator it = set.iterator();

				while (it.hasNext()) {

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) visibleNodes.get(it.next());
					TreeNode [] p = node.getPath();
						
					DefaultTreeModel treeModel = (DefaultTreeModel) fileTree.getModel();

					TreePath tp = new TreePath(p);
					if (fileTree.isVisible(tp)) {
						treeModel.nodeChanged(node); 
					}
				}
				threadLock = false;
			}
		};
		
		Thread t = new Thread(run);
		t.start();
	}
		


	public static ImageIcon getImageIcon(String name) {
		return getImageIcon(name, 27);
	}

	public static ImageIcon getImageIcon(String name, int scale) {

		try {
			Image i = FileUtil.getImage("/images/" + name);
			return new ImageIcon(i.getScaledInstance(scale, scale, Image.SCALE_SMOOTH));
		} catch (Exception e) {
			log.error("Eception:" + e.getMessage(), e);
		}
		return null;
	}

	public FileNode [] getSelectedFiles1() {

		int selectCount = fileTree.getSelectionCount();
		FileNode [] selectedFiles = new FileNode[selectCount];
		TreePath [] selectPaths = fileTree.getSelectionPaths();

		for (int i = 0; i < selectPaths.length; i++) {
			FileNode node = getFileNode(selectPaths[i]);
			selectedFiles[i] = node;
		}

		return selectedFiles;
	}

	public ArrayList getSelectedFiles() {

		ArrayList selectedFiles = new ArrayList();
		TreePath [] selectPaths = fileTree.getSelectionPaths();

		for (int i = 0; i < selectPaths.length; i++) {
			FileNode node = getFileNode(selectPaths[i]);
			selectedFiles.add(node);
		}

		return selectedFiles;
	}


	/*
	 * Searches the entire directory tree and returns the files according to the users selections.
	 */
	public ArrayList getFilesFromDirectoryTree(boolean includeMatchesOnly) {

		ArrayList files = new ArrayList();
		Set keySet = changedNodes.keySet();
		Object [] keys = keySet.toArray();
		
		Arrays.sort(keys, new Comparator() {

			public int compare(Object s1, Object s2) {
				return ((String)s1).compareTo(((String)s2));
			}	

			public boolean equals(Object obj) {
				return this == obj;
			}
		});


		for (int i = 0; i < keys.length; i++) {
			IconData r = (IconData) changedNodes.get((String) keys[i]);
			addFiles(r, files, keys, false);
		}

		return files;
	}


	void addFiles(Object dir, ArrayList files, Object [] keys, boolean includeAll) {

		int iconType = IconData.REGULAR_FOLDER;
		File directory;

		if (dir instanceof IconData) {
			iconType = ((IconData) dir).getIconType();
			directory = ((IconData) dir).getFile();
		}
		else
			directory = (File) dir;

		// Include all media files in the directory only
		if (iconType == IconData.INCLUDE_CONTENT) {
			addValidMediaFiles(directory, files);
		} // Include media files in directory and all subdirectories
		else if (iconType == IconData.INCLUDE_ALL || (iconType == IconData.REGULAR_FOLDER && includeAll)) {

			File [] fileList = directory.listFiles();

//			Some directories e.g. "System Volume Information" will return null
			if (fileList == null)
				return;

//			Add media files in directory
			addValidMediaFiles(directory,  files);

			// GO through all subdirectories
			for (int i = 0; i< fileList.length; i++) {

				if (fileList[i].isDirectory()) {

					//Check to see if it's already marked
					IconData subIconData = (IconData) changedNodes.get(fileList[i].getAbsolutePath());

					// It's NOT saved as marked, i.e. it's a regular driectory
					if (subIconData == null) {
						addFiles(fileList[i], files, keys, true);
					}
				}
			}
		} // Will not proceed.
		else if (iconType == IconData.EXCLUDE_ALL) {

		}
	}

	// Adds all the media files with valid extension to the array list
	void addValidMediaFiles(File dir, ArrayList files) {
		
		File [] dirFiles = dir.listFiles();

		// Some directories e.g. "System Volume Information" will return null
		if (dirFiles == null)
			return;

		String tmp;

		for (int i = 0; i < dirFiles.length; i++) {

			if (!dirFiles[i].isFile())
				continue;

			tmp = dirFiles[i].getName();

			// Files doesn't have a file extension
			if (tmp.indexOf(".") == -1)
				continue;

			String ext = tmp.substring(tmp.lastIndexOf(".") +1, tmp.length());

			if (validExtensions.contains(ext.toLowerCase())) {
				
				int res = checkFileMatch(dirFiles[i]);
				
				if (res == FILE_REGULAR || res == FILE_MATCH) {	
					files.add(new FileNode(dirFiles[i], this));	
				}
			}
		}
	}

	public void setFolderIcon(int iconType) {

		TreePath[] paths = fileTree.getSelectionPaths();

		for (int i = 0; i < paths.length; i++) {

			IconData icon = (IconData) getIconData((paths[i]));

			if (icon != null) {

				if (!icon.isFolder())
					continue;

				icon.setIconType(iconType);

				File file = icon.getFile();

				if (iconType == IconData.REGULAR_FOLDER) {
					if (changedNodes.containsKey(file.getAbsolutePath()))
						changedNodes.remove(file.getAbsolutePath());
				}
				else {
					if (!changedNodes.containsKey(file.getAbsolutePath()))
						changedNodes.put(file.getAbsolutePath(), icon);
				}
			}
		}
	}


	IconData getIconData(TreePath path) {

		DefaultMutableTreeNode node = getTreeNode(path);
		Object obj = node.getUserObject();

		if (obj instanceof IconData)
			return (IconData) obj;
		else 
			return null;
	}

	FileNode getFileNode(TreePath path) {
		return getFileNode(getTreeNode(path));

	}

	DefaultMutableTreeNode getTreeNode(TreePath path)	{
		return (DefaultMutableTreeNode)(path.getLastPathComponent());
	}

	FileNode getFileNode(DefaultMutableTreeNode node)	{

		if (node == null)
			return null;

		Object obj = node.getUserObject();

		if (obj instanceof IconData) {
			obj = ((IconData)obj).getObject();
		}
		if (obj instanceof FileNode) {
			return (FileNode)obj;
		}
		else
			return null;
	}

	
	class PopupTrigger extends MouseAdapter	{

		public void mouseReleased(MouseEvent event)	{

			if (SwingUtilities.isRightMouseButton(event)) {
			
				int x = event.getX();
				int y = event.getY();

				TreePath path = fileTree.getPathForLocation(x, y);
	
				// Sets the row selected
				if (path == null) {
					int rowForLocation = fileTree.getRowForLocation(x, y);
					fileTree.setSelectionRow(rowForLocation);
				}
				
				if (path != null)	{
					IconData icon = getIconData(path);
	
					if (icon.isFolder()) {
						
						if (icon.isDisk()) {
							excludeAll.setIcon(ICON_DISK_EXCLUDE_ALL);
							includeAll.setIcon(ICON_DISK_INCLUDE_ALL);
							folder.setIcon(ICON_DISK);
							includeContent.setIcon(ICON_DISK_INCLUDE_CONTENT);
						}
						else {
							excludeAll.setIcon(ICON_FOLDER_EXCLUDE_ALL);
							includeAll.setIcon(ICON_FOLDER_INCLUDE_ALL);
							folder.setIcon(ICON_FOLDER);
							includeContent.setIcon(ICON_FOLDER_INCLUDE_CONTENT);	
						}
							
						int iconType = icon.getIconType();
						
						/*
						if (iconType != IconData.REGULAR_FOLDER)
							addFiles.setVisible(true);
						else
							addFiles.setVisible(false);
						*/
					/*
						if (iconType != IconData.REGULAR_FOLDER) {
							dir_popup.add(addFiles, 0);
						}
						else {
							dir_popup.remove(0);
						}		
						*/
						dir_popup.show(fileTree, x, y);
					}
					else {
						TreePath[] paths = fileTree.getSelectionPaths();

						if (paths == null)
							return;

						String txt = paths.length > 1 ? "Add files" : "Add file";

						fileItem.setText(txt);
						file_popup.show(fileTree, x, y);
					}

					if (!fileTree.getSelectionModel().isPathSelected(path))
						fileTree.setSelectionPath(path);

					clickedPath = path;
				}
			}
		}
	}

	// Make sure expansion is threaded and updating the tree model
	// only occurs within the event dispatching thread.
	class DirExpansionListener implements TreeExpansionListener {

		public void treeExpanded(TreeExpansionEvent event) {

			final DefaultMutableTreeNode node = getTreeNode(event.getPath());
			final FileNode fnode = getFileNode(node);

			Thread runner = new Thread()	{
				public void run() {
					
					if (fnode != null && fnode.expand(node, validExtensions)) 	{
						addExpandedNode(fnode);
						
						Runnable runnable = new Runnable() 	{
							public void run() 	{
								modelTree.reload(node);
							}
						};
						SwingUtilities.invokeLater(runnable);
					}
				}
			};
			runner.start();
		}

		public void treeCollapsed(TreeExpansionEvent event) {}
	}

	class DirSelectionListener 	implements TreeSelectionListener 	{
		public void valueChanged(TreeSelectionEvent event)	{
			//DefaultMutableTreeNode node = getTreeNode(event.getPath());
			//FileNode fnode = getFileNode(node);
		}
	}

	public IconData getNewIconData(int icon, FileNode f) {
		return new IconData(icon, f);
	}

	
	public class IconCellRenderer extends JLabel implements TreeCellRenderer {

		protected Color m_textSelectionColor;
		protected Color m_textNonSelectionColor;
		protected Color m_bkSelectionColor;
		protected Color m_bkNonSelectionColor;
		protected Color m_borderSelectionColor;

		protected boolean m_selected;
		
		Color noMatchColor;
		Color matchColor;
		Color colorExists;
		
		public IconCellRenderer(Color match, Color noMatch, Color colorExists)	{
			super();
			m_textSelectionColor = UIManager.getColor( "Tree.selectionForeground");
			m_textNonSelectionColor = UIManager.getColor( "Tree.textForeground");
			m_bkSelectionColor = UIManager.getColor(	"Tree.selectionBackground");
			m_bkNonSelectionColor = UIManager.getColor(  "Tree.textBackground");
			m_borderSelectionColor = UIManager.getColor(	"Tree.selectionBorderColor");
			setOpaque(false);

			this.matchColor = match;
			this.noMatchColor = noMatch;
			this.colorExists = colorExists;
		}

		public Component getTreeCellRendererComponent(JTree tree,  Object value, boolean sel, boolean expanded, boolean leaf, 
				int row, boolean hasFocus) 	{

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

			Object obj = node.getUserObject();


			boolean regularFile = false;

			setText(obj.toString());
			
			if (obj instanceof Boolean) {
				setText("Retrieving data...");
			}

			if (obj instanceof IconData) {

				IconData idata = (IconData)obj;

				regularFile = !idata.isFolder();

				if (expanded)
					setIcon(idata.getExpandedIcon());
				else
					setIcon(idata.getIcon());
			}
			else {
				setIcon(null);
			}

			setFont(tree.getFont());

			TreePath path = new TreePath(node.getPath());
			
//			 Do nothing if the row isn't visible
			if (tree.isVisible(path)) {

				if (regularFile) {
					
					int match = checkFileMatch((IconData) obj);

					switch (match) {
					case FILE_REGULAR: {
						setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor); break;}
					case FILE_MATCH: {setBackground(matchColor); break;}	
					case FILE_NO_MATCH: {setBackground(noMatchColor); break;}	
					case FILE_EXISTS: {setBackground(colorExists); break;}	
					}

				}
				else {
					setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
				}
				setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
			}
			m_selected = sel;

			return this;
		}
 

		public void paintComponent(Graphics g) 	{
			Color bColor = getBackground();
			Icon icon = getIcon();

			g.setColor(bColor);
			int offset = 0;

			if(icon != null && getText() != null) 
				offset = (icon.getIconWidth() + getIconTextGap());

			g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

			if (m_selected) 		{
				g.setColor(m_borderSelectionColor);
				g.drawRect(offset, 0, getWidth()-1-offset, getHeight()-1);
			}

			super.paintComponent(g);
		}

	}

	public static final int FILE_REGULAR    = 1;
	public static final int FILE_MATCH 		= 2;
	public static final int FILE_NO_MATCH 	= 3;
	public static final int FILE_EXISTS 	= 4;

	
	public int checkFileMatch(Object fileObj) {

		File file = null;
		
		if (fileObj instanceof FileNode) {
			// SHOULD NEWVER BE FILENODE
			file = ((FileNode) fileObj).getFile();
		} else if (fileObj instanceof File) {
			file = ((File) fileObj);
		} else if (fileObj instanceof IconData) {
			file = ((IconData) fileObj).getFile();
		} else {
			return 0;
		}
		
		if (filterOutDuplicateFiles) {
			if (existingMediaFiles != null && !existingMediaFiles.isEmpty()) {
				if (existingMediaFiles.containsKey(file.getAbsolutePath())) {
					return FILE_EXISTS;
				}
			}
		}
		
		boolean match;
		
		int regexResult = FILE_REGULAR;

		// Check regex
		if (matchOptions.regexPattern != null) {
			
			Matcher m = matchOptions.regexPattern.matcher(file.getName());

			match = m.matches();
			
			if (matchOptions.regexNegate)
				match = !match;
				
			regexResult = match ? FILE_MATCH : FILE_NO_MATCH;
		}

		// Check regular string match
		int regularStringResult = FILE_REGULAR;
	
		if (matchOptions.stringMatch != null) {
			
			// Divides the string.  one|two -> [one, two]
			String [] split = matchOptions.stringMatch.toLowerCase().split("\\|");
			String fileName = file.getName().toLowerCase();
			match = false;
				
			// Checking each string 
			for (int i = 0; i < split.length; i++) {
				if (fileName.indexOf(split[i]) != -1) {
					match = true;
					break;
				}
			}
			
			if (matchOptions.stringNegate)
				match = !match;
			
			regularStringResult = match ? FILE_MATCH : FILE_NO_MATCH;
		}
		
		if (regexResult == regularStringResult)
			return regexResult;
		else if (regexResult > regularStringResult)	
			return regexResult;
		else
			 return regularStringResult;
	}
	
	
	public class IconData {

		public static final int REGULAR_FOLDER = 0;
		public static final int INCLUDE_CONTENT = 1;
		public static final int INCLUDE_ALL = 2;
		public static final int EXCLUDE_ALL = 3;

		public static final int REGULAR_FILE = 4;

		public Icon   m_icon;
		public Icon   m_expandedIcon;

		protected FileNode m_data;

		// 0 = folder/disk, 1 = includeFolderContent, 2 = INCLUDE_ALL, 3 = EXCLUDE_ALL
		private int iconType = 0;
		private boolean isDisk = false;
		private boolean isFolder = false;

		public IconData(int icon, FileNode data)	{
			iconType = icon;
			m_data = data;
		}

		public IconData(int icon, int expandedIconNotUsed, FileNode data)	{
			iconType = icon;
			m_data = data;
		}


		public void setIconType(int i) {
			iconType = i;
		}

		public boolean isFolder() {
			return isFolder;
		}

		public void setFolder(boolean isFolder) {
			this.isFolder = isFolder;
		}

		public boolean isDisk() {
			return isDisk;
		}

		public void setDisk(boolean isDisk) {
			this.isDisk = isDisk;
			isFolder = isDisk;
		}

		public Icon getIcon() {

			Icon tmpIcon = null;

			switch (iconType) {

			case 0 : 
				tmpIcon = isDisk ? FileTree.ICON_DISK : FileTree.ICON_FOLDER;
				break;
			case 1 :
				tmpIcon = isDisk ? FileTree.ICON_DISK_INCLUDE_CONTENT : FileTree.ICON_FOLDER_INCLUDE_CONTENT;
				break;
			case 2 :
				tmpIcon = isDisk ? FileTree.ICON_DISK_INCLUDE_ALL : FileTree.ICON_FOLDER_INCLUDE_ALL;
				break;
			case 3 :
				tmpIcon = isDisk ? FileTree.ICON_DISK_EXCLUDE_ALL : FileTree.ICON_FOLDER_EXCLUDE_ALL;
				break;
			case 4 :
				tmpIcon = FileTree.ICON_MEDIA_FILE;
			}

			return tmpIcon;
		}

		public int getIconType() {
			return iconType;
		}

		public Icon getExpandedIcon() 	{ 
			return getIcon();
		}

		public File getFile() {
			return ((FileNode) m_data).getFile();
		}
		
		public FileNode getFileNode() {
			return m_data;
		}

		public Object getObject() 	{ 
			return m_data;
		}

		public String toString() 	{ 
			return m_data.toString();
		}
		
		String getFilePath() { 
			return ((FileNode) m_data).getFile().getAbsolutePath();
		}		
	}

		
	public void setRegexPattern(String expression) {
		
		try {
			
			if (expression != null) {
				Pattern compiledRegex = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
				matchOptions.regexPattern = compiledRegex;
			}
			else
				matchOptions.regexPattern = null;
			
			updateCurrentCells();
		} catch (Exception ex) {
			log.warn("Invalid regex expression:" + ex.getMessage());
		}
	}
	
	public void setRegexNegate(boolean regexNegate) {
		matchOptions.regexNegate = regexNegate;
		updateCurrentCells();
	}
	
	public void setStringPattern(String stringMatch) {
		
		if (stringMatch != null && stringMatch.trim().equals(""))
			matchOptions.stringMatch = null;
		else {
			matchOptions.stringMatch = stringMatch;
			updateCurrentCells();
		}
	}
	
	public void setStringNegate(boolean stringNegate) {
		matchOptions.stringNegate = stringNegate;
		updateCurrentCells();
	}
	
	private class MatchingOptions {
		
		Pattern regexPattern = null;
		boolean regexNegate = false;
		
		String stringMatch = null;
		boolean stringNegate = false;
				
	}
}
