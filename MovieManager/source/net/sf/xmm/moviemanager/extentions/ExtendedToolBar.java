package net.sf.xmm.moviemanager.extentions;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.ShowGUI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class ExtendedToolBar extends JToolBar implements MouseListener, MouseMotionListener {

    private JToolBar toolBar;
    public JLabel showEntries;
    
    private JButton buttonAdd = null;
    private JButton buttonRemove = null;
    private JButton buttonEdit = null;
    private JButton buttonSearch = null;
    
    private JButton buttonPlay = null;
    private JButton buttonPrint = null;
    
    Dimension dim = new Dimension(3, 0);
    
    int toolBarWidth = 0;
    int toolBarHeight = 55;
    
    public ExtendedToolBar() {
        super();
        
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

    
    
    public void addSeparator(Dimension size) {
        
        toolBarWidth += size.width;
        
        super.addSeparator(size);
    }
    
    public Component add(Component comp) {
        
        toolBarWidth += comp.getPreferredSize().width;
       
        comp.addMouseListener(this);
        comp.addMouseMotionListener(this);
        
        return super.add(comp); 
    }
    
    
    public Dimension getPreferredSize() {
        
        Dimension dim = super.getPreferredSize();
             
        dim.width = toolBarWidth;
        dim.height = toolBarHeight;
        
        return dim;
    }
    
    public Dimension getMaximumSize() {
        
        Dimension dim = super.getMaximumSize();
        return dim;
    }
    
    public Dimension getMinimumSize() {

	Dimension dim = super.getMinimumSize();
    
	dim.width = toolBarWidth;
	dim.height = toolBarHeight;

	return dim;
    }

    public void setEnablePlayButton(boolean enable) {
        buttonPlay.setEnabled(enable);
    }


    public void setEnableButtons(boolean enable) {
        buttonAdd.setEnabled(enable);
        buttonRemove.setEnabled(enable);
        buttonEdit.setEnabled(enable);
        buttonSearch.setEnabled(enable);
        buttonPlay.setEnabled(enable);
        buttonPrint.setEnabled(enable);
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
        toolBar.setRollover(true);
        
        //  toolBar.setLayout(new ModifiedFlowLayout(SwingConstants.HORIZONTAL, 4, 4));

	//toolBar.putClientProperty(new String("JToolBar.isRollover"), Boolean.TRUE);

        //toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), BorderFactory.createEmptyBorder(0,0,0,0)));

        //toolBar.setMinimumSize(new Dimension(254, 50));
        //toolBar.setPreferredSize(new Dimension(254, 60));

        toolBar.setFloatable(false);

        toolBar.addMouseListener((ExtendedToolBar) toolBar);
        toolBar.addMouseMotionListener((ExtendedToolBar) toolBar);
        
        
        
        /* The Add button. */
        buttonAdd = new JButton(new ImageIcon(FileUtil.getImage("/images/add.png").getScaledInstance(24, 24, Image.SCALE_SMOOTH))); //$NON-NLS-1$

        buttonAdd.setPreferredSize(new Dimension(39, 39));
        buttonAdd.setMaximumSize(new Dimension(45, 45));
        buttonAdd.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

        buttonAdd.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-add")); //$NON-NLS-1$
        buttonAdd.setActionCommand("Add"); //$NON-NLS-1$
        buttonAdd.setMnemonic('A');
        buttonAdd.addActionListener(new MovieManagerCommandAdd());

        add(buttonAdd);
        
        /* A separator. */
        toolBar.addSeparator(dim);

        /* The Remove button. */
        buttonRemove = new JButton(new ImageIcon(FileUtil.getImage("/images/remove.png").getScaledInstance(26,26,Image.SCALE_SMOOTH))); //$NON-NLS-1$

        //buttonRemove.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), buttonRemove.getBorder()));

        buttonRemove.setPreferredSize(new Dimension(39, 39));
        buttonRemove.setMaximumSize(new Dimension(44, 45));
        buttonRemove.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-remove")); //$NON-NLS-1$
        buttonRemove.setActionCommand("Remove"); //$NON-NLS-1$
        buttonRemove.setMnemonic('R');
        buttonRemove.addActionListener(new MovieManagerCommandRemove());
        toolBar.add(buttonRemove);

        /* A separator. */
        toolBar.addSeparator(dim);

        /* The Edit button. */
        buttonEdit = new JButton(new ImageIcon(FileUtil.getImage("/images/edit.png").getScaledInstance(23,23,Image.SCALE_SMOOTH))); //$NON-NLS-1$

        buttonEdit.setMargin(new Insets(0,10,0,10));
        
        buttonEdit.setPreferredSize(new Dimension(39, 39));
        buttonEdit.setMaximumSize(new Dimension(44, 45));
        buttonEdit.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-edit")); //$NON-NLS-1$
        buttonEdit.setActionCommand("Edit"); //$NON-NLS-1$
        buttonEdit.setMnemonic('E');
        buttonEdit.addActionListener(new MovieManagerCommandEdit());
        toolBar.add(buttonEdit);

        /* A separator. */
        toolBar.addSeparator(dim);

        /* The Search button. */
        buttonSearch = new JButton(new ImageIcon(FileUtil.getImage("/images/search.png").getScaledInstance(25,25,Image.SCALE_SMOOTH))); //$NON-NLS-1$

        buttonSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,10), buttonSearch.getBorder()));

        buttonSearch.setPreferredSize(new Dimension(39, 39));
        buttonSearch.setMaximumSize(new Dimension(45, 45));
        buttonSearch.setToolTipText(Localizer.getString("moviemanager.listpanel-toolbar-search")); //$NON-NLS-1$
        buttonSearch.setActionCommand("Search"); //$NON-NLS-1$
        buttonSearch.setMnemonic('S');
        buttonSearch.addActionListener(new MovieManagerCommandSearch());
        toolBar.add(buttonSearch);

        toolBar.addSeparator(dim);
             
        
        buttonPlay = constructPlayButton();
        toolBar.add(buttonPlay);
        
        if (!MovieManager.getConfig().getDisplayPlayButton()) {
            buttonPlay.setVisible(false);
            toolBarWidth -= buttonPlay.getPreferredSize().width;
        }
        
        toolBar.addSeparator(dim);
        
        buttonPrint = constructPrintButton();
        
        toolBar.add(buttonPrint);
        
        if (!MovieManager.getConfig().getDisplayPrintButton()) {
            buttonPrint.setVisible(false);
            toolBarWidth -= buttonPrint.getPreferredSize().width;
        }
        
        /* A separator. */
        toolBar.add(new JToolBar.Separator(new Dimension(8, 3)));
        
        JPanel panelEntries = new JPanel();
        panelEntries.setLayout(new BoxLayout(panelEntries, BoxLayout.X_AXIS));

        panelEntries.setBorder(new CompoundBorder(new EmptyBorder(0,0,0,10), new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(6,4,5,5))));

        panelEntries.setMaximumSize(new Dimension(56, 33));
        panelEntries.setPreferredSize(new Dimension(56, 33));
        //panelEntries.setSize(new Dimension(56, 33));

        showEntries = new JLabel("    "); //$NON-NLS-1$
        showEntries.setFont(new Font(showEntries.getFont().getName(), Font.PLAIN, MovieManager.getIt().fontSize + 1));

        panelEntries.add(showEntries);

        toolBar.add(panelEntries);

        //toolBar.setPreferredSize(new Dimension(toolBar.getMaximumSize()));
        //toolBar.setMinimumSize(toolBar.getPreferredSize());
    }
    
    JButton constructPlayButton() {
        JButton buttonPlay = new JButton( 
					 new ImageIcon(
						       FileUtil.getImage("/images/play.gif").
						       getScaledInstance(25,25,Image.SCALE_SMOOTH)));
                   
        buttonPlay.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createEmptyBorder(0,10,0,10),
								buttonPlay.getBorder()));
                         
	buttonPlay.setPreferredSize(new Dimension(39, 39));
	buttonPlay.setMaximumSize(new Dimension(45, 45));
	buttonPlay.setToolTipText("Play");
	buttonPlay.setActionCommand("Play");
	buttonPlay.setMnemonic('P');
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
	buttonPrint.setToolTipText("Play");
	buttonPrint.setActionCommand("Play");
	buttonPrint.setMnemonic('P');
	buttonPrint.addActionListener(new MovieManagerCommandPrint());
        
	return buttonPrint;
    }
  
    
    
    public void constructPopup(int x, int y, MouseEvent event) {
        
        
        JPopupMenu popupMenu = new JPopupMenu();
        
        JCheckBoxMenuItem playButtonItem = new JCheckBoxMenuItem("Play");
        JCheckBoxMenuItem printButtonItem = new JCheckBoxMenuItem("Print");
        
        popupMenu.add(playButtonItem);
        popupMenu.add(printButtonItem);
        
        if (MovieManager.getConfig().getDisplayPlayButton())
            playButtonItem.setState(true);
        else    
            playButtonItem.setState(false);
        
        playButtonItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
                
		    if (MovieManager.getConfig().getDisplayPlayButton()) {
			buttonPlay.setVisible(false);
			toolBarWidth -= buttonPlay.getPreferredSize().width;
		    } else {
			buttonPlay.setVisible(true);
			toolBarWidth += buttonPlay.getPreferredSize().width;
		    }
                
		    MovieManager.getConfig().setDisplayPlayButton(!MovieManager.getConfig().getDisplayPlayButton());
		}
	    });
    
    
	if (MovieManager.getConfig().getDisplayPrintButton())
	    printButtonItem.setState(true);
	else
	    printButtonItem.setState(false);
        
	printButtonItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
            
		    if (MovieManager.getConfig().getDisplayPrintButton()) {
			buttonPrint.setVisible(false);
			toolBarWidth -= buttonPrint.getPreferredSize().width;
		    }
		    else {
			buttonPrint.setVisible(true);
			toolBarWidth += buttonPrint.getPreferredSize().width;
		    }
            
		    MovieManager.getConfig().setDisplayPrintButton(!MovieManager.getConfig().getDisplayPrintButton());
                        
		}                
	    });
	
	popupMenu.show((Component) event.getSource(), x, y);
	ShowGUI.show(popupMenu, true);
    }



    public void mouseClicked(MouseEvent event) {
	
        /* Button 2 */
        if (SwingUtilities.isRightMouseButton(event)) {
	    constructPopup(event.getX(), event.getY(), event);
        }
        
        
    }

    public void mousePressed(MouseEvent arg0) {}

    public void mouseReleased(MouseEvent arg0) {}

    public void mouseEntered(MouseEvent arg0) {}

    public void mouseExited(MouseEvent arg0) {}
  
    public void mouseDragged(MouseEvent arg0) {}

    public void mouseMoved(MouseEvent event) {}
        
        
    public void updateToolButtonBorder() {
	if (MovieManager.getConfig().isRegularToolButtonsUsed())
	    updateToolButtonBorderToRegular();
	else
	    updateToolButtonBorderToCurrentLaf();
    }


    void updateToolButtonBorderToCurrentLaf() {
	
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

    void updateToolButtonBorderToRegular() {

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



