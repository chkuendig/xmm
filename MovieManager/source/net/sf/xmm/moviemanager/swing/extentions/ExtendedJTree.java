/**
 * @(#)ExtendedJTree.java 1.0 07.10.05 (dd.mm.yy)
 *
 * Copyright (2003) BRo3
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

package net.sf.xmm.moviemanager.swing.extentions;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig.LookAndFeelType;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

/**
 * This class is a type of JTree that uses Drag and Drop, and performs auto-scrolling when dragging images beyond
 * the bounds of the JTrees JScrollPane.
 */
public class ExtendedJTree extends JTree implements Autoscroll /*, DragGestureListener, DragSourceListener*/ {  

	Logger log = Logger.getLogger(getClass());

	private static final int AUTOSCROLL_MARGIN = 25;
	private Insets autoscrollInsets = new Insets( 0, 0, 0, 0 );    // AutoScroll methods.
	private DefaultTreeModel dtModel;
	private DefaultMutableTreeNode root;
	public static final boolean displayFilesInTree = false;
	
	ExtendedJTree extendedTree = this;
	
	int firstShiftRow = 0;
	int currentRow = 0;
	
	
   JTree getTree() {
	   return this;
   }
   

   /**
    * Constructor.
    * @param root The Root node for the JTree.
    */
   public ExtendedJTree() {
	   super();

	   addMouseListener(new TreeMouseListener());
	   setDragEnabled(false);
	   
	   addTreeSelectionListener(new TreeSelectionListener() {
		   public void valueChanged( TreeSelectionEvent e ) {
			   DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) getLastSelectedPathComponent();
			   getChildren(node);
			   
			  // Update last selected
			   if (getSelectionCount() == 1) {
				   
				   setCurrentRow(getSelectionRows()[0]);
				   firstShiftRow = currentRow;
			   }
		   }
	   });
	   
	   mouseSelectionListenerEnabled = MovieManager.getConfig().getTreeMouseSelectionListenerEnabled();
   }	

   /**
    * Constructor.
    * @param root The Root node for the JTree.
    */
   public ExtendedJTree(DefaultMutableTreeNode root) {

	   this();

	   this.root = root;

	   dtModel = new DefaultTreeModel(root);
	   setModel(dtModel);
   }
   
      
   // This makes sure the entire width of the tree rows is painted
   
   @Override
   protected void paintComponent(Graphics g) {
	   int[] rows = getSelectionRows();

	   if (rows != null && rows.length > 0) {
		   for (int i = 0; i < rows.length; i++) {
			   Rectangle b = getRowBounds(rows[i]);
			   g.setColor(UIManager.getColor("Tree.selectionBackground"));
			   g.fillRect(0, b.y, getWidth(), b.height);

			   /*
			   viewPortPrefWidth = getPreferredScrollableViewportSize().width;
			   treeWidth = getWidth();
			   treePrefWidth = getPreferredSize().width;
			   treeMaxWidth = getMaximumSize().width;
			   treeMinWidth = getMinimumSize().width;
		   	*/
		   }
	   }
	   super.paintComponent(g);
   }

   /*
   protected void paintComponent1(Graphics g) {
	  
	   viewPortPrefWidth = getPreferredScrollableViewportSize().width;
	   treeWidth = getWidth();
	   treePrefWidth = getPreferredSize().width;
	   treeMaxWidth = getMaximumSize().width;
	   treeMinWidth = getMinimumSize().width;
	   
	   super.paintComponent(g);
   }
   
    public int viewPortPrefWidth = -1;
   public int treeWidth = -1;
   public int treePrefWidth = -1;
   public int treeMaxWidth = -1;
   public int treeMinWidth = -1;
   
   */
   
  
  
   
   boolean mouseSelectionListenerEnabled = true;
   
   public void setMouseSelectionListenerEnabled(boolean val) {
	   System.err.println("setMouseSelectionListenerEnabled:" + val);
	   mouseSelectionListenerEnabled = val;
   }
   
   // Used for knowing which rows to use when selecting multiple rows with SHIFT key.
   void setCurrentRow(int val) {
		currentRow = val;
	}
	
	private void setLastButtonPushed(boolean shift) {
		if (!shift) {
			System.err.println("setLastButtonPushed:" + currentRow);
			firstShiftRow = currentRow;
		}
	}
   
   class TreeMouseListener extends MouseAdapter {
	   
	   /**
	    * Handles selection on nodes with CTRL + SHIFT keys + right click menu
	    **/
	   public void mousePressed(MouseEvent event) {
		
		   if (!mouseSelectionListenerEnabled)
			   return;
		   
		   ExtendedJTree movieList = (ExtendedJTree) MovieManager.getDialog().getMoviesList();
		   int rowForLocation = movieList.getRowForLocation(event.getX(), event.getY());
		   boolean outsideOfText = false;
		   
		   if (rowForLocation == -1) {
			   int width = movieList.getWidth();
			   outsideOfText = true;
			   // In JTree, the "row" stops where the text of the entry ends. Therefore we have to ignore the x value.
			   // Since the roothandle and the icon also disturbs the getRowForLocation method we have to try different values of X.
			   for (int i = 0; rowForLocation == -1 && i < width; i++)
				   rowForLocation = movieList.getRowForLocation(i, event.getY());
		   }
 		   
		   if (rowForLocation == -1) {
			   return;
		   }
		   
		   setCurrentRow(rowForLocation);
		   
		   // If right clicking an already selected row, no changes are done.
		   if (getTree().isRowSelected(rowForLocation) && SwingUtilities.isRightMouseButton(event) && 
				   !SysUtil.isCtrlPressed(event) && !SysUtil.isShiftPressed(event)) {
			   return;
		   }
		   		   		   
		   if (MovieManager.getConfig().getEnableCtrlMouseRightClick() && SysUtil.isCtrlPressed(event)) {
			   
			   if (!getTree().isRowSelected(rowForLocation)) {
				   getTree().addSelectionRow(rowForLocation);
			   }
		   }      
		   else {

			   if (SysUtil.isShiftPressed(event)) {
				 
				//   if (true)
				//	   return;
					   
				   // if substance laf, drop this. Thats because substance already handles selecting the entire row.
				   if (MovieManager.getConfig().getLookAndFeelType() == LookAndFeelType.Substance)
					   return;

				   // Cannot use setSelectionInterval if both are equal, see setSelectionInterval
				   if (rowForLocation == firstShiftRow) {
					   movieList.setSelectionRow(rowForLocation);
				   }
				   else
					   movieList.setSelectionInterval(rowForLocation, firstShiftRow);
			   }
			   else if (SysUtil.isCtrlPressed(event)) {

				   if (movieList.isRowSelected(rowForLocation)) {
					   
					   if (SwingUtilities.isLeftMouseButton(event)) {
						   if (outsideOfText)
							   movieList.removeSelectionRow(rowForLocation);
					   }
				   }
				   else {
					   if (outsideOfText || SwingUtilities.isRightMouseButton(event)) {
						   movieList.addSelectionRow(rowForLocation);
					   }
				   }
			   }
			   else {
				   movieList.setSelectionRow(rowForLocation);
			   }
		   }
		   setLastButtonPushed(SysUtil.isShiftPressed(event));
	   }
   }
   
     

   
   

   ////////////////////
   // AUTO SCROLLING //
   ////////////////////
   /**
    * Handles the scrolling of the JTree.
	 * @param location The location of the mouse.
	 */
	public void autoscroll(Point location)
	{
		int top = 0, left = 0, bottom = 0, right = 0;
		Dimension size = getSize();
		Rectangle rect = getVisibleRect();
		int bottomEdge = rect.y + rect.height;
		int rightEdge = rect.x + rect.width;
		if( location.y - rect.y <= AUTOSCROLL_MARGIN && rect.y > 0 ) top = AUTOSCROLL_MARGIN;
		if( location.x - rect.x <= AUTOSCROLL_MARGIN && rect.x > 0 ) left = AUTOSCROLL_MARGIN;
		if( bottomEdge - location.y <= AUTOSCROLL_MARGIN && bottomEdge < size.height ) bottom = AUTOSCROLL_MARGIN;
		if( rightEdge - location.x <= AUTOSCROLL_MARGIN && rightEdge < size.width ) right = AUTOSCROLL_MARGIN;
		rect.x += right - left;
		rect.y += bottom - top;
		scrollRectToVisible( rect );
	}

	/**
	 * Gets the insets used for the autoscroll.
	 * @return The insets.
	 */
	public Insets getAutoscrollInsets()
	{
		Dimension size = getSize();
		Rectangle rect = getVisibleRect();
		autoscrollInsets.top = rect.y + AUTOSCROLL_MARGIN;
		autoscrollInsets.left = rect.x + AUTOSCROLL_MARGIN;
		autoscrollInsets.bottom = size.height - ( rect.y + rect.height ) + AUTOSCROLL_MARGIN;
		autoscrollInsets.right = size.width - ( rect.x + rect.width ) + AUTOSCROLL_MARGIN;
		return autoscrollInsets;
	}

	
	////////////////////
	// MISC FUNCTIONS //
	////////////////////
	/**
	 * Returns the name of the current selected directory.
	 * @return The name of the current selected directory.
	 */
	public DefaultMutableTreeNode [] getSelectedNodes() {

		TreePath[] tp = getSelectionPaths();

		if( tp != null) {

			DefaultMutableTreeNode [] nodes = new DefaultMutableTreeNode[tp.length];

			for (int i = 0; i < tp.length; i++)
				nodes[i] = (DefaultMutableTreeNode) tp[i].getLastPathComponent();

			return nodes;
		}
		return null;
	}

	/**
	 * Gets all children for a parent node.
	 * @param parent The parent node.
	 */
	public void getChildren( DefaultMutableTreeNode parent ) {


		// // Get parent if it exists
		//     if( parent != null )
		//     {
		// 	DiskObject parentObj = ( DiskObject ) parent.getUserObject();

		//       // If parent can contain children (i.e. A drive or folder)
		//       if( parentObj.isDirectory() || parentObj.isDrive() )
		//       {
		//         // Remove all the parents' children ( Parent will grab new 'refreshed' view of its children below )
		//         int count = parent.getChildCount() - 1;
		//         for( int k = count; k >= 0; k-- )
		//         {
		//           dtModel.removeNodeFromParent( ( DefaultMutableTreeNode ) parent.getChildAt( k ) );
		//         }

		//         // Convert parent to a File and get all it's children  ( i.e. Will refresh view with most current children )
		//         File parentDir = new File( parentObj.getPath() );
		//         JExplorer.selectedDiskObjectName = parentDir.getAbsolutePath();
		//         File[] childFiles = parentDir.listFiles();

		//         // If parent has any children
		//         if( childFiles != null )
		//         {
		//           // If children are directories, add them to JTree
		//           int childCount = childFiles.length;
		//           for( int i = 0; i < childCount; i++ )
		//           {
		//             File child = childFiles[i];
		//             if( child.isDirectory() )
		//             {
		//               //log.debug( "ADD: " +child.getName()+" to "+parentObj.getPath() );
		//               addObject(  parent, new Local( DiskObject.DIRECTORY, child.getName(), child.getAbsolutePath() ) );
		//             }
		//           }
		//         }
		//      }
		//   }
	}

	////////////////////////////////////////////////////////////////////////////
	// Add/Remove Nodes
	////////////////////////////////////////////////////////////////////////////
	/** Remove all nodes except the root node. */
	public void clear()
	{
		root.removeAllChildren();
		dtModel.reload();
	}

	/** Remove the currently selected node. */
	public void removeCurrentNode()
	{
		TreePath currentSelection = getSelectionPath();
		if( currentSelection != null )
		{
			DefaultMutableTreeNode currentNode = ( DefaultMutableTreeNode )
			( currentSelection.getLastPathComponent() );
			MutableTreeNode parent = ( MutableTreeNode ) ( currentNode.getParent() );
			if( parent != null )
			{
				dtModel.removeNodeFromParent( currentNode );
				return;
			}
		}
	}

	/**
	 * Add child to the currently selected node.
	 * @param child The child node.
	 */
	public void addObject( Object child )
	{
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = getSelectionPath();

		if( parentPath == null )
		{
			parentNode = root;
		}
		else
		{
			parentNode = ( DefaultMutableTreeNode ) parentPath.getLastPathComponent();
		}
		addObject( parentNode, child, false);//true );
	}

	/**
	 * Add child to a parent node.
	 * @param child The child node.
	 * @param parent The parent node.
	 */
	public void addObject(DefaultMutableTreeNode parent, Object child)
	{
		addObject( parent, child, false );
	}

	/**
	 * Add child to a parent node.
	 * @param child The child node.
	 * @param parent The parent node.
	 * @param shouldBeVisible TRUE to expand the parent folder, FALSE to collapse the parent folder.
	 */
	public void addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible)
	{
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode( child );

		if( parent == null )
		{
			parent = root;
		}
		dtModel.insertNodeInto( childNode, parent, parent.getChildCount() );

		// Make sure the user can see the lovely new node.
		if( shouldBeVisible )
		{
			scrollPathToVisible(new TreePath( childNode.getPath()));
		}
		
	}


	/* Resets the value of the X- coordinate */
	public void scrollPathToVisible2(TreePath path, int xCoordinate) {

		if(path != null) {
			makeVisible(path);
			Rectangle bounds = getPathBounds(path);

			if(bounds != null) {

				if (xCoordinate != -1)
					bounds.setRect(xCoordinate, bounds.getY(), bounds.getWidth(), bounds.getHeight());

				scrollRectToVisible(bounds);

				if (accessibleContext != null) {
					((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
				}
			}
		}
	}
}

