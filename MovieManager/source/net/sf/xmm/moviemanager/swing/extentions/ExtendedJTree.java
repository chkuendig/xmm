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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

/**
 * This class is a type of JTree that uses Drag and Drop, and performs auto-scrolling when dragging images beyond
 * the bounds of the JTrees JScrollPane.
 */
public class ExtendedJTree extends JTree implements Autoscroll, DragGestureListener, DragSourceListener {  

	Logger log = Logger.getLogger(getClass());

	static int dummy = 0;
	private static final int AUTOSCROLL_MARGIN = 25;
	private Insets autoscrollInsets = new Insets( 0, 0, 0, 0 );    // AutoScroll methods.
	private DefaultTreeModel dtModel;
	private DefaultMutableTreeNode root;
	public static final boolean displayFilesInTree = false;
	
	ExtendedJTree extendedTree = this;
	
	int firstShiftRow = 0;
	int currentRow = 0;
	
	void setCurrentRow(int val) {
		currentRow = val;
	}
	
	private void setLastButtonPushed(boolean shift) {
		if (!shift) {
			firstShiftRow = currentRow;
		}
	}
	
   JTree getTree() {
	   return this;
   }
      
   class TreeMouseListener extends MouseAdapter {
	   
	   /**
	    * Handles selection on nodes with CTRL + SHIFT keys + right click menu
	    **/
	   public void mousePressed(MouseEvent event) {
		   
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
   
   /**
    * Constructor.
    * @param root The Root node for the JTree.
    */
   public ExtendedJTree() {
	   super();

	   addMouseListener(new TreeMouseListener());

	   setDragEnabled(false);
	   
	    
	   /*
	   DragSource dragSource = DragSource.getDefaultDragSource();

	   dragSource.createDefaultDragGestureRecognizer(this, // component where drag originates
			   DnDConstants.ACTION_MOVE, // actions
			   this ); // drag gesture recognizer

	   new DropTarget( this, getTargetListener() );
*/
	   
	   addTreeSelectionListener( new TreeSelectionListener() {
		   public void valueChanged( TreeSelectionEvent e ) {
			   DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) getLastSelectedPathComponent();
			   getChildren( node );
		   }
	   });
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

	//////////////////////////////////////////////////////////////////
	// Drag Listeners                                               //
	//////////////////////////////////////////////////////////////////
	public void dragGestureRecognized(DragGestureEvent e) {


		// drag anything ...
		DefaultMutableTreeNode [] nodes = getSelectedNodes();

		//void startDrag(DragGestureEvent trigger, Cursor dragCursor, Transferable transferable, DragSourceListener dsl)

		boolean drag = false;

		if (nodes != null) {
			for (int i = 0; i < nodes.length; i++) {
				//log.debug("<dragGestureRecognized> Start: " + nodes[i].getUserObject());

				if (nodes[i].getUserObject() instanceof ModelEpisode)
					drag = true;
				else
					nodes[i] = null;
			}

			/* Feature not yet ready */
			drag = false;

			//_movieManager.getImage("/images/serie.png").getScaledInstance(25,25, Image.SCALE_SMOOTH))

			if (drag)
				e.startDrag(Toolkit.getDefaultToolkit().createCustomCursor(FileUtil.getImage("/images/movie.png"), new Point(0, 0), "MoveDrop"), new TransferableNode(nodes), this);
			//e.startDrag(DragSource.DefaultMoveDrop, new TransferableNode(nodes), this);
		}
	}

	public void dragDropEnd (DragSourceDropEvent e) {
		DefaultMutableTreeNode [] nodes = getSelectedNodes();

		if (nodes != null)
			for (int i = 0; i < nodes.length; i++)
				log.debug( "<dragDropEnd> End: " + nodes[i].getUserObject());

	}

	public void dragEnter(DragSourceDragEvent e) {
		log.debug( "<dragEnter> Enter" );
	}

	public void dragExit(DragSourceEvent e) {
		log.debug( "<dragExit> Exit" );
	}

	public void dragOver(DragSourceDragEvent e) {
		//log.debug( "<dragOver> Over" );
	}

	public void dropActionChanged(DragSourceDragEvent e) {
		log.debug( "<dropActionChanged> Action Changed" );
	}




	////////////////////
	// DROP LISTENERS //
	////////////////////
	/**
	 * Creates a 'Drop' (from Drag and Drop)  Listener for the JTree.
	 * @return The DropTargetListener for this JTree.
	 */
	public DropTargetListener getTargetListener() {

		DropTargetListener dtl = new DropTargetAdapter() {
			/**
			 * Gets the current node in the JTree that the object is being dragged over.
			 * @param dtde The DropTargetDragEvent.
			 */
			public void dragOver(final DropTargetDragEvent dragEvent)
			{ /*
		    Timer timerHover = new Timer( 1000, new ActionListener()
		    {
		    public void actionPerformed( ActionEvent e )
		    {
			 */
				System.err.println("dragOver");
				
				try {

					//if (1 == 1)
						//return;

					int x = dragEvent.getLocation().x;
					int y = dragEvent.getLocation().y;
					TreePath path = getClosestPathForLocation( x, y );
					log.debug( "OVER - " + x + "," + y );

					DropTargetDropEvent tempDTDropEvent = new DropTargetDropEvent(dragEvent.getDropTargetContext(), dragEvent.getLocation(), 0, 0);
					Transferable dragData = tempDTDropEvent.getTransferable();

					DefaultMutableTreeNode [] nodes = (DefaultMutableTreeNode []) dragData.getTransferData(TransferableNode.NODE_FLAVOR);

					DefaultMutableTreeNode node = null;
					for (int i = 0; i < nodes.length && node == null; i++)
						node = nodes[i];

					//

					if (path != null && node != null && ((DefaultMutableTreeNode) path.getLastPathComponent()).isNodeSibling(node)) {

						setSelectionPath( path );
					}
					else {

						//dragEvent.rejectDrag();

						//tempDTDropEvent.rejectDrag(); 
						//tempDTDropEvent.rejectDrop();

						//dragEvent.getDropTargetContext().setCursor(DragSource.DefaultMoveDrop);

						//DragSource.DefaultMoveNoDrop
					}

					/*
		      }
		      } );
		      timerHover.start();
					 */

				} catch (Exception e) {
					log.error("", e);
				}
			}

			/**
			 * Handles the 'drop' of the object into a node on the JTree.
			 * @param dtde The DropTargetDropEvent.
			 */
			
			public void drop2(DropTargetDropEvent dtde) {

				try {

					// Ok, get the dropped object and try to figure out what it is.
					Transferable tr = dtde.getTransferable();

					//DefaultMutableTreeNode [] nodes = (DefaultMutableTreeNode []) tr.getTransferData(TransferableNode.NODE_FLAVOR);
					Object [] nodes = (Object []) tr.getTransferData(TransferableNode.NODE_FLAVOR);

					
					
					if (nodes != null)
						for (int i = 0; i < nodes.length; i++)
							log.debug("dragged: " + nodes[i]);


					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for( int i = 0; i < flavors.length; i++ )
					{
						log.debug( "Possible flavor: " + flavors[i].getMimeType() );
						// Check for file lists specifically.
						if( flavors[i].isFlavorJavaFileListType() )
						{
							// Great! Accept copy drops...
							dtde.acceptDrop( DnDConstants.ACTION_MOVE );
							///////////
							
//					      DefaultMutableTreeNode parentNode = ( DefaultMutableTreeNode ) getLastPathComponent();
//					      if( draggedNode.isRoot() || parentNode == draggedNode.getParent() || draggedNode.isNodeDescendant( parentNode ) )
//					      {
//					      target.setSelectionPath( null );
//					      return ( false );
//					      }
//					      else
//					      {
//					      target.setSelectionPath( pathTarget );
//					      return ( true );
//					      }
							
							///////////
							log.debug( "Successful file list drop.\n\n" );
							// And add the list of file names to our text area
							java.util.List list = ( java.util.List ) tr.getTransferData( flavors[i] );

							// Destination directory
							// DefaultMutableTreeNode [] nodes = getSelectedNodes();

//							if (nodes != null)
//							for (int u = 0; u < nodes.length; u++)
//							log.debug( "<dragDropEnd> End: " + nodes[u].getUserObject());

							String directory = "";
							File dir = new File( directory );

							for( int j = 0; j < list.size(); j++ )
							{
								// Source File
								File file = ( File ) list.get( j );
								File newPath = new File( dir, file.getName() );
								// Move file to new directory
								boolean success = file.renameTo( newPath );
								if( success )
								{
									log.debug( "Moved " + list.get( j ) + " to " + directory + "\n" );
									//log.debug( newPath.getAbsolutePath() );
									//refreshTree( newPath );
								}
								else
								{
									log.debug( "<ERROR>: Could not move " + list.get( j ) + " to " + directory );
								}
							}
							// If we made it this far, everything worked.
							dtde.dropComplete( true );
							log.debug( "Thank you for using Drag and Drop." );
							return;
						}
					}
					// Hmm, the user must not have dropped a file list.
					log.debug( "Drop failed: " + dtde );
					dtde.rejectDrop();
				}
				catch(Exception e) {
					log.error(e.getMessage(), e);
					dtde.rejectDrop();  
				}
			}
			
			
			
			  public void drop(java.awt.dnd.DropTargetDropEvent evt) {
				  
				  log.debug("FileDrop: drop event." );
                  
				  try { 
					  
					  DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
					  
					  System.err.println("parentNode:" + parentNode);
					  
					  // Get whatever was dropped
                      java.awt.datatransfer.Transferable tr = evt.getTransferable();

                      // Is it a file list?
                      if (tr.isDataFlavorSupported (java.awt.datatransfer.DataFlavor.javaFileListFlavor))
                      {
                          // Say we'll take it.
                          //evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                          evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY );
                          log.debug("FileDrop: file list accepted.");

                          // Get a useful list
                          java.util.List fileList = (java.util.List) 
                              tr.getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                          java.util.Iterator iterator = fileList.iterator();

                          // Convert list to array
                          java.io.File[] filesTemp = new java.io.File[ fileList.size() ];
                          fileList.toArray( filesTemp );
                          final java.io.File[] files = filesTemp;

                          // Alert listener to drop.
                       //   if (listener != null )
                         //     listener.filesDropped( files );

                          // Mark that drop is completed.
                          evt.getDropTargetContext().dropComplete(true);
                          log.debug("FileDrop: drop complete.");
                      }   // end if: file list
                      else // this section will check for a reader flavor.
                      {
                          // Thanks, Nathan!
                          // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                          DataFlavor[] flavors = tr.getTransferDataFlavors();
                          boolean handled = false;
                          for (int zz = 0; zz < flavors.length; zz++) {
                        	  
                        	  System.err.println("getReaderForText:" + flavors[zz].getReaderForText(tr));
                        	  
                              if (flavors[zz].isRepresentationClassReader()) {
                                  // Say we'll take it.
                                  //evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                                  evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                                  log.debug("FileDrop: reader accepted.");

                                  Reader reader = flavors[zz].getReaderForText(tr);

                                  BufferedReader br = new BufferedReader(reader);
                                  
                                //  if (listener != null)
                                  //    listener.filesDropped(createFileArray(br, out));
                                  
                                  // Mark that drop is completed.
                                  evt.getDropTargetContext().dropComplete(true);
                                  log.debug("FileDrop: drop complete.");
                                  handled = true;
                                  break;
                              }
                          }
                          if(!handled){
                        	  log.debug("FileDrop: not a file list or reader - abort.");
                              evt.rejectDrop();
                          }
                          // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                      }   // end else: not a file list
                  }   // end try
                  catch ( java.io.IOException io) {
                	  log.debug("FileDrop: IOException - abort:" );
                     // io.printStackTrace( out );
                      evt.rejectDrop();
                  }   // end catch IOException
                  catch (java.awt.datatransfer.UnsupportedFlavorException ufe) 
                  {   log.debug("FileDrop: UnsupportedFlavorException - abort:");
                  evt.rejectDrop();
                  }   // end catch: UnsupportedFlavorException
                  finally
                  {
                      // If it's a Swing component, reset its border
                   /*
                	  if( c instanceof javax.swing.JComponent )
                      {   javax.swing.JComponent jc = (javax.swing.JComponent) c;
                          jc.setBorder( normalBorder );
                          log.debug("FileDrop: normal border restored." );
                      }   // end if: JComponent
                   */
                  }   // end finally
              }   // end drop
			  
			  
		};
		return dtl;
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
		//log.debug( "Count: "+(++dummy) );
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


	/**
     * Implement this inner interface to listen for when files are dropped. For example
     * your class declaration may begin like this:
     * <code><pre>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( java.io.File[] files )
     *      {
     *          ...
     *      }   // end filesDropped
     *      ...
     * </pre></code>
     *
     * @since 1.1
     */
    public static interface FileDropListener {
       
        /**
         * This method is called when files have been successfully dropped.
         *
         * @param files An array of <tt>File</tt>s that were dropped.
         * @since 1.0
         */
        public abstract void filesDropped( java.io.File[] files );
        
        
    }   // end inner-interface Listener
    
    
    public void setTreeDropListener(FileDropListener listener) {
    	setTransferHandler(new FSTransfer(listener));
    }
    
}


class FSTransfer extends TransferHandler {
	
	static Logger log = Logger.getLogger(FSTransfer.class);
	
	ExtendedJTree.FileDropListener listener = null;
	
	FSTransfer(ExtendedJTree.FileDropListener listener) {
		this.listener = listener;
	}
	
	
	public boolean importData(JComponent comp, Transferable t) {
		
		System.err.println("importData:" + comp);
		
		
		// Make sure we have the right starting points
		if (!(comp instanceof ExtendedJTree)) {
			return false;
		}
		
		
		// DataFlavor[mimetype=text/uri-list;representationclass=java.io.Reader]
		// DataFlavor[mimetype=text/uri-list;representationclass=java.lang.String]
		// DataFlavor[mimetype=x-special/gnome-icon-list;representationclass=java.io.InputStream]
		
		
		// Grab the tree, its model and the root node
		/*
		ExtendedJTree tree = (ExtendedJTree) comp;
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
		 */
		
		try {
			
			//java.util.List data = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
			
			/*
			Iterator i = data.iterator();
			
			while (i.hasNext()) {
				File f = (File)i.next();
				
				System.err.println("f:" + f);
				//root.add(new DefaultMutableTreeNode(f.getName()));
			}
			*/
		//	model.reload();
			
					
			DataFlavor[] flavors = t.getTransferDataFlavors();
		
			boolean handled = false;

			//System.err.println("flavors.length:" + flavors.length);
			
			for (int i = 0; i < flavors.length; i++) {

				System.err.println("flavors["+i+"]:" + flavors[i]);
				
				if (flavors[i].equals(DataFlavor.javaFileListFlavor)) {
					System.err.println("is javaFileListFlavor");
					
					
					java.util.List data = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
					
					Iterator iter = data.iterator();
					
					ArrayList files = new ArrayList();
					
					while (iter.hasNext()) {
						File f = (File) iter.next();
						files.add(f);						
					}
					
					if (files.size() > 0) {
					
						File [] filesArray = new File[files.size()];
						
						files.toArray(filesArray);
						
						if (listener != null)
							listener.filesDropped(filesArray);
					}
				}
				else {

					System.err.println("isRepresentationClassReader:" + flavors[i].isRepresentationClassReader());
					System.err.println("isRepresentationClassInputStream:" + flavors[i].isRepresentationClassInputStream());
					System.err.println("isRepresentationClassCharBuffer:" + flavors[i].isRepresentationClassCharBuffer());
					System.err.println("isRepresentationClassByteBuffer:" + flavors[i].isRepresentationClassByteBuffer());
					
					Class resprClass = flavors[i].getRepresentationClass();
					
					System.err.println("resprClass:" + resprClass);
					
					
					// only for java.io.Reader
					if (flavors[i].isRepresentationClassReader() ||
							flavors[i].isRepresentationClassInputStream() ||
							flavors[i].isRepresentationClassCharBuffer() ||
							flavors[i].isRepresentationClassByteBuffer()) {
						
						Reader reader = flavors[i].getReaderForText(t);

						BufferedReader br = new BufferedReader(reader);

						File [] files = createFileArray(br);
												
						if (listener != null)
							 listener.filesDropped(files);

						handled = true;
					}
					
				}
				
				if (!handled){
					//System.err.println("not handles");
					//     log( out, "FileDrop: not a file list or reader - abort." );
					//evt.rejectDrop();
					//return false;
				}
			}
			
			return false;
		}
		catch (UnsupportedFlavorException ufe) {
			System.err.println("Ack! we should not be here.\nBad Flavor.");
			ufe.printStackTrace();
		}
		catch (IOException ioe) {
			System.out.println("Something failed during import:\n" + ioe);
		}
		catch (Exception ioe) {
			log.warn("Exception:" + ioe.getMessage(), ioe);
		}
		return false;
	}

	// We only support file lists on FSTrees...
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		
		System.err.println("canImport");	
		
		if (true)
		return true;
		
		if (comp instanceof ExtendedJTree) {
			
			for (int i = 0; i < transferFlavors.length; i++) {
				System.err.println("transferFlavors[i]:" + transferFlavors[i]);
								
				System.err.println("javaFileListFlavor:" + transferFlavors[i].equals(DataFlavor.javaFileListFlavor));
				System.err.println("isRepresentationClassReader:" + transferFlavors[i].isRepresentationClassReader());
				
				if (transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
					return true;
				}
				else if (transferFlavors[i].isRepresentationClassReader())
					return true;
				else
					return false;
				
			}
			System.err.println("return true");
			//return true;
		}
		return false;
	}
	
	
	/*
	 // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                            DataFlavor[] flavors = tr.getTransferDataFlavors();
                            boolean handled = false;
                            for (int zz = 0; zz < flavors.length; zz++) {
                                if (flavors[zz].isRepresentationClassReader()) {
                                    // Say we'll take it.
                                    //evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                                    evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                                    log(out, "FileDrop: reader accepted.");

                                    Reader reader = flavors[zz].getReaderForText(tr);

                                    BufferedReader br = new BufferedReader(reader);

                                    if(listener != null)
                                        listener.filesDropped(createFileArray(br, out));

                                    // Mark that drop is completed.
                                    evt.getDropTargetContext().dropComplete(true);
                                    log(out, "FileDrop: drop complete.");
                                    handled = true;
                                    break;
                                }
                            }
                            if(!handled){
                                log( out, "FileDrop: not a file list or reader - abort." );
                                evt.rejectDrop();
                            }
                            // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
	 
	 */
	
	
	
	
	 // Nathan Blomquist -- Linux (KDE/Gnome) support added.
    private static String ZERO_CHAR_STRING = "" + (char) 0;
    
    private static File[] createFileArray(BufferedReader bReader)
    {
       try { 
           java.util.List list = new java.util.ArrayList();
           java.lang.String line = null;
                      
           while ((line = bReader.readLine()) != null) {
               try {
            	   
                   // kde seems to append a 0 char to the end of the reader
                   if (ZERO_CHAR_STRING.equals(line)) 
                	   continue; 
                   
                   System.err.println("line:" + line);
                   
                   java.io.File file = new java.io.File(new java.net.URI(line));
                   list.add(file);
               } catch (Exception ex) {
                   log.warn("Error with " + line + ": " + ex.getMessage());
               }
           }
           return (java.io.File[]) list.toArray(new File[list.size()]);
       } catch (IOException ex) {
           log.warn("FileDrop: IOException");
       }
       return new File[0];
    }
}



class TransferableNode implements Transferable {

	public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Node");

	private DefaultMutableTreeNode [] nodes;

	private DataFlavor[] flavors = { NODE_FLAVOR };

	public TransferableNode(DefaultMutableTreeNode nd) {
		//node = new DefaultMutableTreeNode[]{};
		nodes = new DefaultMutableTreeNode[]{nd};
	}

	public TransferableNode(DefaultMutableTreeNode [] nd) {
		nodes = nd;
	}

	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor == NODE_FLAVOR) {
			return nodes;
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}			
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(flavors).contains(flavor);
	}







}


//import java.awt.Rectangle;

//import javax.swing.JTree;
//import javax.swing.tree.TreePath;
//import javax.swing.tree.*;
//import javax.swing.event.*;

//import java.lang.reflect.*;
//import java.util.*;

//public class ExtendedJTree extends JTree {

///* Resets the value of the X- coordinate */
//public void scrollPathToVisible2(TreePath path, int xCoordinate) {

//if(path != null) {
//makeVisible(path);
//Rectangle bounds = getPathBounds(path);

//if(bounds != null) {

//if (xCoordinate != -1)
//bounds.setRect(xCoordinate, bounds.getY(), bounds.getWidth(), bounds.getHeight());

//scrollRectToVisible(bounds);

//if (accessibleContext != null) {
//((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
//}
//}
//}
//}
//}
