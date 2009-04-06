package net.sf.xmm.moviemanager.swing.extentions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandPlay;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandPrint;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandRemove;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSearch;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandAdd;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandEdit;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerPlayHandler;

import org.apache.log4j.Logger;

public class ExtendedToolBar extends JToolBar implements MouseListener, MouseMotionListener {

	Logger log = Logger.getLogger(getClass());

	private JToolBar toolBar;
	public JLabel entriesCount;

	private JButton buttonAdd = null;
	private JButton buttonRemove = null;
	private JButton buttonEdit = null;
	private JButton buttonSearch = null;

	private JButton buttonPlay = null;
	private JButton buttonPrint = null;

	private JPanel panelEntries = null;
	
	private boolean addButtonlegal = true;
	private boolean editButtonlegal = true;
	private boolean removeButtonlegal = true;
	private boolean searchButtonlegal = true;
	private boolean playButtonlegal = true;
	private boolean printButtonlegal = true;
	
	private boolean buttonPopupEnabled = true;
	
	private boolean showEntries = true;
	
	public static Dimension separatorDim = new Dimension(4, 0);

	private int toolBarWidth = 0;
	private int toolBarHeight = 55;

	public boolean getPLayButtonVisible() {
		return buttonPlay.isVisible();
	}
	
	public boolean getPrintButtonVisible() {
		return buttonPrint.isVisible();
	}
	
	public boolean getAddButtonVisible() {
		return buttonAdd.isVisible();
	}
	
	public boolean getRemoveButtonVisible() {
		return buttonRemove.isVisible();
	}
	
	public boolean getEditButtonVisible() {
		return buttonEdit.isVisible();
	}
	
	public boolean getSearchButtonVisible() {
		return buttonSearch.isVisible();
	}
	
	public boolean getShowEntriesCount() {
		return showEntries;
	}
	
	public void setShowEntriesCount(boolean showEntries) {
		
		this.showEntries = showEntries;
		
		if (showEntries)			
			toolBarWidth += panelEntries.getSize().width;
		else 	
			toolBarWidth -= panelEntries.getSize().width;
		
		panelEntries.setVisible(showEntries);
		
	}
	
	public void setEnableButtonPopup(boolean enable) {
		buttonPopupEnabled = enable;
	}
	
	
	public ExtendedToolBar() {
		super(SwingConstants.HORIZONTAL);
		toolBar = this;
		construct();
	}

	public ExtendedToolBar(int arg0) {
		super(arg0);

		toolBar = this;
		construct();
	}

	public ExtendedToolBar(String arg0, int arg1) {
		super(arg0, arg1);

		toolBar = this;
		construct();
	}

	public ExtendedToolBar(String arg0) {
		super(arg0);

		toolBar = this;
		construct();
	}

	
	
	/**
     * Sets the entries variable and uppdates the showEntries Label with the new number
     **/
    public void setAndShowEntries(int entries) {
         
        String value;;
        
        if (entries < 10)
            value = "    "; //$NON-NLS-1$
        else if (entries < 100)
            value = "  "; //$NON-NLS-1$
        else
            value = " "; //$NON-NLS-1$
        
        if (entries != -1) {
            value += String.valueOf(entries);
        }
        
        entriesCount.setText(value);
        entriesCount.updateUI();
    }
    

	public void addSeparator(Dimension size) {
		toolBarWidth += size.width;
		super.addSeparator(size);
	}

	public Component add(Component comp) {
		System.err.println("toolbar add");
		toolBarWidth += comp.getPreferredSize().width;
			
		comp.addMouseListener(this);
		comp.addMouseMotionListener(this);

		return super.add(comp); 
	}
	
	public Component add(Component comp, int index) {

		toolBarWidth += comp.getPreferredSize().width;
			
		comp.addMouseListener(this);
		comp.addMouseMotionListener(this);

		return super.add(comp, index); 
	}

	public void remove(int index) {
		
		toolBarWidth -= getComponentAtIndex(index).getWidth();
		
		System.err.println("remove new width:" + toolBarWidth);
		
		super.remove(index);
	}

	public Dimension getPreferredSize() {

		Dimension dim = new Dimension();
		
		try {
			dim.width = toolBarWidth;
			dim.height = toolBarHeight;
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return dim;
	}

	public Dimension getMaximumSize() {
		Dimension dim = super.getMaximumSize();
		return dim;
	}

	public Dimension getMinimumSize() {

		Dimension dim = new Dimension();
			
		try {
			dim.width = toolBarWidth;
			dim.height = toolBarHeight;
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return dim;
	}

	
//	 Makes the button visible in the toolbar
	
	public void showButton(JButton button, boolean enable) {
							
		if (button.isVisible() == enable) {
			log.warn("Button is already in the requested state.");
		}
		
		if (enable)			
			toolBarWidth += button.getPreferredSize().width;
		else 	
			toolBarWidth -= button.getPreferredSize().width;
						
		button.setVisible(enable);
	}
	
	
	// Makes the play button visible in the toolbar
	public void showPlayButton(boolean enable) {
		
		if (enable && getPLayButtonVisible()) {
			return;
		} else if (!enable && !getPLayButtonVisible())
			return;
				
		if (!playButtonlegal && enable) {
			log.warn("Play button is not legal.");
			return;
		}
					
		//showButton(buttonPlay, enable);
		buttonPlay.setVisible(enable);
		
		if (enable) {
			add(new JToolBar.Separator(separatorDim), 7);
			add(buttonPlay, 8);
		}
		else {// Remove the button and a separator
			remove(7);
			remove(7);
		}
				
		revalidate();
	}

//	 Makes the print button visible in the toolbar
	public void showPrintButton(boolean enable) {

		if (enable && getPrintButtonVisible()) {
			return;
		} else if (!enable && !getPrintButtonVisible())
			return;
			
		if (!printButtonlegal && enable) {
			log.warn("Print button is not legal.");
			return;
		}
				
		//showButton(buttonPrint, enable);
		buttonPrint.setVisible(enable);
		
		int index = 7;
		
		if (buttonPlay.isVisible())
			index = 9;
		
		if (enable) {
			add(buttonPrint, index);
			add(new JToolBar.Separator(separatorDim), index);
		}
		else {// Remove the button and a separator
			remove(index);
			remove(index);
		}
	
		revalidate();
	}
	
//	 Makes the add button visible in the toolbar
	public void showAddButton(boolean enable) {

		if (enable && getAddButtonVisible()) {
			return;
		} else if (!enable && !getAddButtonVisible())
			return;
		
		if (!addButtonlegal && enable) {
			log.warn("Add button is not legal.");
			return;
		}
				
		showButton(buttonAdd, enable);
	}
	
	
//	 Makes the edit button visible in the toolbar
	public void showEditButton(boolean enable) {

		if (enable && getEditButtonVisible()) {
			return;
		} else if (!enable && !getEditButtonVisible())
			return;
		
		if (!editButtonlegal && enable) {
			log.warn("Edit button is not legal.");
			return;
		}
				
		showButton(buttonEdit, enable);
	}
	
//	 Makes the remove button visible in the toolbar
	public void showRemoveButton(boolean enable) {

		if (enable && getRemoveButtonVisible()) {
			return;
		} else if (!enable && !getRemoveButtonVisible())
			return;
		
		if (!removeButtonlegal && enable) {
			log.warn("Remove button is not legal.");
			return;
		}
				
		showButton(buttonRemove, enable);
	}
	
//	 Makes the search button visible in the toolbar
	public void showSearchButton(boolean enable) {

		if (enable && getSearchButtonVisible()) {
			return;
		} else if (!enable && !getSearchButtonVisible())
			return;
		
		if (!searchButtonlegal && enable) {
			log.warn("Search button is not legal.");
			return;
		}
				
		showButton(buttonSearch, enable);
	}
	
	
	
	

	public void setPlayButtonLegal(boolean enable) {
		playButtonlegal = enable;
	}
	
	public void setPrintButtonLegal(boolean enable) {
		printButtonlegal = enable;
	}
	
	public void setAddButtonLegal(boolean enable) {
		addButtonlegal = enable;
		
		if (!enable)
			showAddButton(false);
	}
	
	public void setEditButtonLegal(boolean enable) {
		editButtonlegal = enable;
		
		if (!enable)
			showEditButton(false);
	}
	
	public void setRemoveButtonLegal(boolean enable) {
		editButtonlegal = enable;
		
		if (!enable)
			showRemoveButton(false);
	}
	
	public void setSearchButtonLegal(boolean enable) {
		searchButtonlegal = enable;
		
		if (!enable)
			showSearchButton(false);
	}

	public void setEnablePlayButton(boolean enable) {
		buttonPlay.setEnabled(enable);
	}
		
	
	public void setEnableButtons(boolean enable, boolean applet) {
		
		if (addButtonlegal)
			buttonAdd.setEnabled(enable && !applet);
		
		if (removeButtonlegal)
			buttonRemove.setEnabled(enable && !applet);
		
		if (editButtonlegal)
			buttonEdit.setEnabled(enable && !applet);
		
		if (searchButtonlegal)
			buttonSearch.setEnabled(enable);
		
		buttonPlay.setEnabled(false); // Only enable if a valid path to a media file
		
		if (printButtonlegal)
			buttonPrint.setEnabled(enable && !applet);
	}

	public JButton getAddButton() {
		return buttonAdd;
	}

	public JButton getRemoveButton() {
		return buttonRemove;
	}

	public JButton getEditButton() {
		return buttonEdit;
	}

	public JButton getSearchButton() {
		return buttonSearch;
	}

	public JButton getPlayButton() {
		return buttonPlay;
	}
	
	public JButton getPrintButton() {
		return buttonPrint;
	}

	void construct() {

		try {

			toolBar.setRollover(true);
			toolBar.putClientProperty(new String("JToolBar.isRollover"), Boolean.TRUE);
			toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), BorderFactory.createEmptyBorder(0,10,0,10)));
			toolBar.setFloatable(false);
			
			toolBar.addMouseListener((ExtendedToolBar) toolBar);
			toolBar.addMouseMotionListener((ExtendedToolBar) toolBar);

			//toolBar.setBorder(new EmptyBorder(0,0,0,10));

			/* The Add button. */
			buttonAdd = new JButton(new ImageIcon(FileUtil.getImage("/images/add.png").getScaledInstance(27, 27, Image.SCALE_SMOOTH))); //$NON-NLS-1$

			buttonAdd.setPreferredSize(new Dimension(45, 45));
			buttonAdd.setMinimumSize(new Dimension(45, 45));
			buttonAdd.setMaximumSize(new Dimension(45, 45));
			buttonAdd.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

			buttonAdd.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-add")); //$NON-NLS-1$
			buttonAdd.setActionCommand("Add"); //$NON-NLS-1$
			buttonAdd.setMnemonic('A');
			buttonAdd.addActionListener(new MovieManagerCommandAdd());

			add(buttonAdd);
			
			/* A separator. */
			addSeparator(separatorDim);

			/* The Remove button. */
			buttonRemove = new JButton(new ImageIcon(FileUtil.getImage("/images/remove.png").getScaledInstance(27,27,Image.SCALE_SMOOTH))); //$NON-NLS-1$

			buttonRemove.setPreferredSize(new Dimension(40, 40));
			buttonRemove.setMaximumSize(new Dimension(44, 45));
			buttonRemove.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-remove")); //$NON-NLS-1$
			buttonRemove.setActionCommand("Remove"); //$NON-NLS-1$
			buttonRemove.setMnemonic('R');
			buttonRemove.addActionListener(new MovieManagerCommandRemove());
			
			add(buttonRemove);

			
			/* A separator. */
			addSeparator(separatorDim);

			/* The Edit button. */
			buttonEdit = new JButton(new ImageIcon(FileUtil.getImage("/images/edit.png").getScaledInstance(26,26,Image.SCALE_SMOOTH))); //$NON-NLS-1$

			buttonEdit.setMargin(new Insets(0,10,0,10));

			buttonEdit.setPreferredSize(new Dimension(40, 40));
			buttonEdit.setMaximumSize(new Dimension(44, 45));
			buttonEdit.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-edit")); //$NON-NLS-1$
			buttonEdit.setActionCommand("Edit"); //$NON-NLS-1$
			buttonEdit.setMnemonic('E');
			buttonEdit.addActionListener(new MovieManagerCommandEdit());
			
			add(buttonEdit);
			
			/* A separator. */
			addSeparator(separatorDim);

			/* The Search button. */
			buttonSearch = new JButton(new ImageIcon(FileUtil.getImage("/images/search.png").getScaledInstance(28,28,Image.SCALE_SMOOTH))); //$NON-NLS-1$

			buttonSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), buttonSearch.getBorder()));

			buttonSearch.setPreferredSize(new Dimension(40, 40));
			buttonSearch.setMaximumSize(new Dimension(45, 45));
			buttonSearch.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-search")); //$NON-NLS-1$
			buttonSearch.setActionCommand("Search"); //$NON-NLS-1$
			buttonSearch.setMnemonic('S');
			buttonSearch.addActionListener(new MovieManagerCommandSearch());
			
			add(buttonSearch);
			
			
			buttonPlay = constructPlayButton();
			buttonPlay.setVisible(false);
			
			buttonPrint = constructPrintButton();
			buttonPrint.setVisible(false);
			
			
			/* Panel entries */
			
			panelEntries = new JPanel();
			panelEntries.setLayout(new BoxLayout(panelEntries, BoxLayout.X_AXIS));

			panelEntries.setBorder(new CompoundBorder(new EmptyBorder(0,0,0,0)	, new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(6,4,5,5))));

			panelEntries.setMaximumSize(new Dimension(56, 33));
			panelEntries.setPreferredSize(new Dimension(56, 33));
			
			entriesCount = new JLabel("    "); //$NON-NLS-1$
			entriesCount.setFont(new Font(entriesCount.getFont().getName(), Font.PLAIN, MovieManager.getIt().getFontSize() + 1));

			panelEntries.add(entriesCount);
		
			if (!MovieManager.getConfig().getInternalConfig().isEntriesCountDisabled()) {
				addSeparator(new Dimension(20, 3));
				add(panelEntries);
				toolBarWidth += 10;
			}
		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
    }

	JButton constructPlayButton() {
		JButton buttonPlay = new JButton( 
				new ImageIcon(
						FileUtil.getImage("/images/play.png").
						getScaledInstance(25,25,Image.SCALE_SMOOTH)));

		buttonPlay.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0,10,0,10),
				buttonPlay.getBorder()));

		buttonPlay.setPreferredSize(new Dimension(39, 39));
		buttonPlay.setMaximumSize(new Dimension(45, 45));
		buttonPlay.setToolTipText("Play");
		buttonPlay.setActionCommand("Play");
		buttonPlay.setMnemonic('P');

		// Uses plugin playhandler is it exists
		MovieManagerPlayHandler playHandler = MovieManager.getConfig().getPlayHandler();

		if (playHandler != null)
			buttonPlay.addActionListener(playHandler); 
		else
			buttonPlay.addActionListener(new MovieManagerCommandPlay());

		return buttonPlay;
	}

    JButton constructPrintButton() {
    	
    	JButton buttonPrint = new JButton( 
    			new ImageIcon(
    					FileUtil.getImage("/images/printer.png").
    					getScaledInstance(25,25,Image.SCALE_SMOOTH)));

    	buttonPrint.setBorder(BorderFactory.createCompoundBorder(
    			BorderFactory.createEmptyBorder(0,10,0,10),
    			buttonPrint.getBorder()));

    	buttonPrint.setPreferredSize(new Dimension(39, 39));
    	buttonPrint.setMaximumSize(new Dimension(45, 45));
    	buttonPrint.setToolTipText("Print");
    	buttonPrint.setActionCommand("Print");
    	buttonPrint.setMnemonic('P');
    	buttonPrint.addActionListener(new MovieManagerCommandPrint());
    	return buttonPrint;
    }


    
    
    public void constructPopup(int x, int y, MouseEvent event) {

    	JPopupMenu popupMenu = new JPopupMenu();

    	final JCheckBoxMenuItem playButtonItem = new JCheckBoxMenuItem("Play");
    	final JCheckBoxMenuItem printButtonItem = new JCheckBoxMenuItem("Print");

    	popupMenu.add(playButtonItem);
    	popupMenu.add(printButtonItem);

    	playButtonItem.setState(buttonPlay.isVisible());
    	playButtonItem.setEnabled(playButtonlegal);
    	
    	playButtonItem.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			showPlayButton(playButtonItem.isSelected());
    		}
    	});

    	printButtonItem.setState(buttonPrint.isVisible());
    	printButtonItem.setEnabled(printButtonlegal);
    	
    	printButtonItem.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			showPrintButton(printButtonItem.isSelected());    			
    		}                
    	});
    	
    	popupMenu.show((Component) event.getSource(), x, y);
    }



    public void mouseClicked(MouseEvent event) {

    	System.err.println("mouseClicked");
    	
    	/* Button 2 */
    	if (SwingUtilities.isRightMouseButton(event)) {
    
    		if (buttonPopupEnabled)
    			constructPopup(event.getX(), event.getY(), event);
    	}
    }

    public void mousePressed(MouseEvent arg0) {}

    public void mouseReleased(MouseEvent arg0) {}

    public void mouseEntered(MouseEvent arg0) {}

    public void mouseExited(MouseEvent arg0) {}

    public void mouseDragged(MouseEvent arg0) {}

    public void mouseMoved(MouseEvent event) {}


    public void updateToolButtonBorderToCurrentLaf() {

    	getAddButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,15,3,15), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
    	getRemoveButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,15,3,15), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
    	getEditButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,13,3,13), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
    	getSearchButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,13,3,13), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));

    	buttonPlay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,13,3,13), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));
    	buttonPrint.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,13,3,13), BorderFactory.createEmptyBorder(-1,-5,-1,-5)));


    	Dimension dim = new Dimension(45, 45);

    	getAddButton().setMaximumSize(dim);
    	getRemoveButton().setMaximumSize(dim);
    	getEditButton().setMaximumSize(dim);
    	getSearchButton().setMaximumSize(dim);

    	buttonPlay.setMaximumSize(dim);
    	buttonPrint.setMaximumSize(dim);

    	getAddButton().setPreferredSize(dim);
    	getRemoveButton().setPreferredSize(dim);
    	getEditButton().setPreferredSize(dim);
    	getSearchButton().setPreferredSize(dim);

    	buttonPlay.setPreferredSize(dim);
    	buttonPrint.setPreferredSize(dim);

    }

   public void updateToolButtonBorderToRegular() {

    	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,0), BorderFactory.createEmptyBorder(0,0,0,0)));

    	getAddButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
    	getRemoveButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
    	getEditButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
    	getSearchButton().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));

    	buttonPlay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));
    	buttonPrint.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));


    	Dimension dim = new Dimension(40, 40);

    	getAddButton().setPreferredSize(dim);
    	getRemoveButton().setPreferredSize(dim);
    	getEditButton().setPreferredSize(dim);
    	getSearchButton().setPreferredSize(dim);

    	buttonPlay.setPreferredSize(dim);
    	buttonPrint.setPreferredSize(dim);

    	getAddButton().setMaximumSize(dim);
    	getRemoveButton().setMaximumSize(dim);
    	getEditButton().setMaximumSize(dim);
    	getSearchButton().setMaximumSize(dim);

    	buttonPlay.setMaximumSize(dim);
    	buttonPrint.setMaximumSize(dim);
    }
}   



