/**
 * @(#)DialogPrefs.java 1.0 26.09.06 (dd.mm.yy)
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

import java.io.*;
import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.apache.log4j.*;
import com.l2fprod.gui.plaf.skin.*;
import com.oyoaha.swing.plaf.oyoaha.*;
import net.sf.xmm.moviemanager.commands.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.swing.extentions.*;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.*;

public class DialogPrefs extends JDialog implements ActionListener, ItemListener {

    static Logger log = Logger.getRootLogger();
    private static MovieManagerConfig config = MovieManager.getConfig();
    
    private Container contentPane;
    private JTabbedPane all;

    private JComboBox lafChooser;
    private JCheckBox enableLafChooser;
    private JComboBox skinlfThemePackChooser;
    private JCheckBox enableSkinlf;
    private JComboBox oyoahaThemePackChooser;
    private JCheckBox enableOyoaha;

    private UIManager.LookAndFeelInfo[] installedLookAndFeels;

    private JRadioButton regularToolBarButtons;
    private JRadioButton currentLookAndFeelButtons;

    private JRadioButton regularSeenIcon;
    private JRadioButton currentLookAndFeelIcon;

    private JRadioButton regularDecoratedButton;
    private JRadioButton defaultLafDecoratedButton;

    private JCheckBox enableProxyButton;
    private JCheckBox enableAuthenticationButton;
    private JComboBox proxyType = new JComboBox();
    private JTextField portTextField;
    private JTextField userNameTextField;
    private JTextField passwordTextField;
    private JTextField hostTextField;

    private JLabel proxyTypeLabel;
    private JLabel hostLabel;
    private JLabel portLabel;
    private JLabel userNameLabel;
    private JLabel passwordLabel;

    private JCheckBox loadDatabaseOnStartUp;
    private JCheckBox enableAutoMoveThe;
    private JCheckBox enableAutoMoveAnAndA;

    
    private JCheckBox storeAllAvailableAkaTitles;
    private JCheckBox noDuplicateAkaTitles;
    private JCheckBox includeAkaLanguageCodes;
    private JCheckBox useLanguageSpecificTitle;
    
    private JComboBox languageCodeSelector;
    
    private JCheckBox displayQueriesInTree;

    private JCheckBox enableSeenEditable;
    private JCheckBox enableRightclickByCtrl;
    private JCheckBox enableLoadLastUsedList;
    private JCheckBox enableUseJTreeIcons;
    private JCheckBox enableUseJTreeCovers;
    private JCheckBox enableHighlightEntireRow;

    private JCheckBox enablePreserveCoverRatioEpisodesOnly;
    private JCheckBox enablePreserveCoverRatio;

    private JRadioButton pumaCover;
    private JRadioButton jaguarCover;

    private JCheckBox enableStoreCoversLocally;

   
    private JButton browserBrowse;
    private JTextField mediaPlayerPathField;
    private JTextField customBrowserPathField;
    
    private JRadioButton browserOptionOpera;
    private JRadioButton browserOptionMozilla;
    private JRadioButton browserOptionFirefox;
    private JRadioButton browserOptionNetscape;
    private JRadioButton browserOptionSafari;
    private JRadioButton browserOptionIE;
    
    
    private JCheckBox enableUseDefaultWindowsPlayer;
    private JRadioButton enableUseDefaultWindowsBrowser;
    private JRadioButton enableCustomBrowser;
    
    private JSlider rowHeightSlider;
    private JTree exampleTree;
    private MovieManagerConfig exampleConfig = new MovieManagerConfig();

        
    public DialogPrefs() {
	/* Dialog creation...*/
	super(MovieManager.getDialog());

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
		public void actionPerformed(ActionEvent e)
		{
		    dispose();
		}
	    };

	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
	getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

	setTitle(Localizer.getString("dialogprefs.title")); //$NON-NLS-1$
	setModal(true);
	setResizable(false);

	/* LookAndFeel panel */
	JPanel layoutPanel = new JPanel(new GridLayout(0, 1));

	layoutPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.look-and-feel.title")), BorderFactory.createEmptyBorder(12,0,16,0))); //$NON-NLS-1$


	/* Seen button icon */
	JLabel seenIconLabel = new JLabel(Localizer.getString("dialogprefs.panel.look-and-feel.seen-unseen-icon")); //$NON-NLS-1$
	regularSeenIcon = new JRadioButton(Localizer.getString("dialogprefs.panel.look-and-feel.seen-unseen-icon.regular")); //$NON-NLS-1$
	currentLookAndFeelIcon = new JRadioButton(Localizer.getString("dialogprefs.panel.look-and-feel.seen-unseen-icon.look-and-feel") + ": "); //$NON-NLS-1$
	regularSeenIcon.setActionCommand("SeenIcon"); //$NON-NLS-1$
	currentLookAndFeelIcon.setActionCommand("SeenIcon"); //$NON-NLS-1$

	ButtonGroup seenIconGroup = new ButtonGroup();
	seenIconGroup.add(regularSeenIcon);
	seenIconGroup.add(currentLookAndFeelIcon);

	regularSeenIcon.addActionListener(this);
	currentLookAndFeelIcon.addActionListener(this);

	if (config.getUseRegularSeenIcon())
	    regularSeenIcon.setSelected(true);
	else
	    currentLookAndFeelIcon.setSelected(true);

	JPanel seenIconPanel = new JPanel(new BorderLayout());
	seenIconPanel.setBorder(BorderFactory.createEmptyBorder(0,20,7,20));

	seenIconPanel.add(seenIconLabel, BorderLayout.WEST);
	seenIconPanel.add(regularSeenIcon, BorderLayout.CENTER);
	seenIconPanel.add(currentLookAndFeelIcon, BorderLayout.EAST);

	layoutPanel.add(seenIconPanel);


	/* Toolbar button */
	JLabel toolBarButtonLabel = new JLabel(Localizer.getString("dialogprefs.panel.look-and-feel.toolbar-buttons-look")); //$NON-NLS-1$
	regularToolBarButtons = new JRadioButton(Localizer.getString("dialogprefs.panel.look-and-feel.toolbar-buttons-look.regular")); //$NON-NLS-1$
	currentLookAndFeelButtons = new JRadioButton(Localizer.getString("dialogprefs.panel.look-and-feel.toolbar-buttons-look.look-and-feel") + ": "); //$NON-NLS-1$
	regularToolBarButtons.setActionCommand("ToolBarButton"); //$NON-NLS-1$
	currentLookAndFeelButtons.setActionCommand("ToolBarButton"); //$NON-NLS-1$

	ButtonGroup toolBarButtonGroup = new ButtonGroup();
	toolBarButtonGroup.add(regularToolBarButtons);
	toolBarButtonGroup.add(currentLookAndFeelButtons);

	regularToolBarButtons.addActionListener(this);
	currentLookAndFeelButtons.addActionListener(this);

	if (config.isRegularToolButtonsUsed())
	    regularToolBarButtons.setSelected(true);
	else
	    currentLookAndFeelButtons.setSelected(true);

	JPanel toolBarButtonPanel = new JPanel(new BorderLayout());
	toolBarButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,20,7,20));

	toolBarButtonPanel.add(toolBarButtonLabel, BorderLayout.WEST);
	toolBarButtonPanel.add(regularToolBarButtons, BorderLayout.CENTER);
	toolBarButtonPanel.add(currentLookAndFeelButtons, BorderLayout.EAST);

	layoutPanel.add(toolBarButtonPanel);


	/* DefaultLookAndFeelDecorated */
	JLabel defaultLafDecoratedLabel = new JLabel(Localizer.getString("dialogprefs.panel.look-and-feel.title-bar-decoration") + ": "); //$NON-NLS-1$
	regularDecoratedButton = new JRadioButton(Localizer.getString("dialogprefs.panel.look-and-feel.title-bar-decoration.regular")); //$NON-NLS-1$
	defaultLafDecoratedButton = new JRadioButton(Localizer.getString("dialogprefs.panel.look-and-feel.title-bar-decoration.look-and-feel")); //$NON-NLS-1$
	regularDecoratedButton.setActionCommand("DefaultLafDecorated"); //$NON-NLS-1$
	defaultLafDecoratedButton.setActionCommand("DefaultLafDecorated"); //$NON-NLS-1$

	ButtonGroup defaultLafDecoratedGroup = new ButtonGroup();
	defaultLafDecoratedGroup.add(regularDecoratedButton);
	defaultLafDecoratedGroup.add(defaultLafDecoratedButton);

	regularDecoratedButton.addActionListener(this);
	defaultLafDecoratedButton.addActionListener(this);

	if (config.getDefaultLookAndFeelDecorated())
	    defaultLafDecoratedButton.setSelected(true);
	else
	    regularDecoratedButton.setSelected(true);

	JPanel defaultLafDecoratedPanel = new JPanel(new BorderLayout());
	defaultLafDecoratedPanel.setBorder(BorderFactory.createEmptyBorder(0,20,7,20));

	defaultLafDecoratedPanel.add(defaultLafDecoratedLabel, BorderLayout.WEST);
	defaultLafDecoratedPanel.add(regularDecoratedButton, BorderLayout.CENTER);
	defaultLafDecoratedPanel.add(defaultLafDecoratedButton, BorderLayout.EAST);

	layoutPanel.add(defaultLafDecoratedPanel);


	/* Laf choosers */

	installedLookAndFeels = UIManager.getInstalledLookAndFeels();

	/* Group the radio buttons. */
	ButtonGroup lafGroup = new ButtonGroup();

	int numberOfLookAndFeels = config.getNumberOfLookAndFeels();
	String [] lookAndFeelStrings = new String [numberOfLookAndFeels];

	for (int i = 0; i < numberOfLookAndFeels; i++) {
	    lookAndFeelStrings[i] = installedLookAndFeels[i].getName();
	}
	lafChooser = new JComboBox(lookAndFeelStrings);
	enableLafChooser = new JCheckBox(Localizer.getString("dialogprefs.panel.look-and-feel.enable-custom-laf.text")); //$NON-NLS-1$
	enableLafChooser.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
	lafGroup.add(enableLafChooser);

	String currentLookAndFeel = config.getLookAndFeelString();

	lafChooser.setSelectedItem(currentLookAndFeel);
	lafChooser.setEnabled(false);
	lafChooser.addActionListener(this);
	enableLafChooser.addActionListener(this);

	lafChooser.setPreferredSize(new Dimension(200, (int) lafChooser.getPreferredSize().getHeight()));

	JPanel lafChooserPanel = new JPanel(new BorderLayout());
	lafChooserPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

	lafChooserPanel.add(enableLafChooser, BorderLayout.WEST);
	lafChooserPanel.add(lafChooser, BorderLayout.EAST);

	lafChooserPanel.setMaximumSize(new Dimension(250,30));
	lafChooserPanel.setPreferredSize(new Dimension(250,30));

	layoutPanel.add(lafChooserPanel);

	/* Skinlf */

	String [] skinlfThemePackList = LookAndFeelManager.getSkinlfThemepackList();

	if (skinlfThemePackList != null) {
	    enableSkinlf = new JCheckBox(Localizer.getString("dialogprefs.panel.look-and-feel.enable-skinlf.text")); //$NON-NLS-1$
	    enableSkinlf.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
	    lafGroup.add(enableSkinlf);

	    skinlfThemePackChooser = new JComboBox(skinlfThemePackList);
	    skinlfThemePackChooser.setSelectedItem(config.getSkinlfThemePack());
	    skinlfThemePackChooser.setEnabled(false);

	    String currentSkinlfThemePack = config.getSkinlfThemePack();

	    for (int i = 0; i < skinlfThemePackList.length; i++) {
		if (skinlfThemePackList[i].equals(currentSkinlfThemePack)) {
		    skinlfThemePackChooser.setSelectedItem(currentSkinlfThemePack);
		    break;
		}
		if (i == skinlfThemePackList.length-1)
		    skinlfThemePackChooser.setSelectedIndex(0);
	    }

	    JPanel skinlfPanel = new JPanel(new BorderLayout());
	    skinlfPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

	    skinlfPanel.add(enableSkinlf, BorderLayout.WEST);
	    skinlfPanel.add(skinlfThemePackChooser, BorderLayout.EAST);

	    layoutPanel.add(skinlfPanel);
	    skinlfThemePackChooser.addActionListener(this);
	    enableSkinlf.addActionListener(this);
	}

	String [] oyoahaThemePackList = LookAndFeelManager.getOyoahaThemepackList();

	if (oyoahaThemePackList != null) {

	    enableOyoaha = new JCheckBox(Localizer.getString("dialogprefs.panel.look-and-feel.endable-oyoaha-laf.text")); //$NON-NLS-1$
	    enableOyoaha.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
	    lafGroup.add(enableOyoaha);

	    oyoahaThemePackChooser = new JComboBox(oyoahaThemePackList);
	    oyoahaThemePackChooser.setSelectedItem(config.getOyoahaThemePack());
	    oyoahaThemePackChooser.setEnabled(false);

	    String currentOyoahaThemePack = config.getOyoahaThemePack();

	    if (oyoahaThemePackList != null) {
		for (int i = 0; i < oyoahaThemePackList.length; i++) {
		    if (oyoahaThemePackList[i].equals(currentOyoahaThemePack)) {
			oyoahaThemePackChooser.setSelectedItem(currentOyoahaThemePack);
			break;
		    }
		}
	    }

	    JPanel oyoahaPanel = new JPanel(new BorderLayout());
	    oyoahaPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

	    oyoahaPanel.add(enableOyoaha, BorderLayout.WEST);
	    oyoahaPanel.add(oyoahaThemePackChooser, BorderLayout.EAST);

	    if ("1.5".compareTo(System.getProperty("java.version")) == 1) { //$NON-NLS-1$ //$NON-NLS-2$

		layoutPanel.add(oyoahaPanel);
		oyoahaThemePackChooser.addActionListener(this);
		enableOyoaha.addActionListener(this);
		enableOyoaha.addItemListener(this);
	    }
	}

	setLafChooserPreferredSize();

	if ((skinlfThemePackList != null) && (config.getLookAndFeelType() == 1)) {
	    enableSkinlf.setSelected(true);
	    skinlfThemePackChooser.setEnabled(true);
	}

	else if ((oyoahaThemePackList != null) && (config.getLookAndFeelType() == 2)) {
	    enableOyoaha.setSelected(true);
	    oyoahaThemePackChooser.setEnabled(true);
	}
	else {
	    enableLafChooser.setSelected(true);
	    lafChooser.setEnabled(true);

	    if ((skinlfThemePackList == null) && (oyoahaThemePackList == null))
		enableLafChooser.setEnabled(false);
	}



	/* Proxy settings */
	enableProxyButton = new JCheckBox(Localizer.getString("dialogprefs.panel.proxy.enable-proxy")); //$NON-NLS-1$
	enableProxyButton.setActionCommand("Enable Proxy"); //$NON-NLS-1$
	enableProxyButton.addItemListener(this);

	String[] proxyTypeString = { "HTTP", "SOCKS" }; //$NON-NLS-1$ //$NON-NLS-2$
	proxyType = new JComboBox(proxyTypeString);
	proxyType.setSelectedItem(config.getProxyType());
	proxyType.setEnabled(false);
	proxyTypeLabel = new JLabel(Localizer.getString("dialogprefs.panel.proxy.proxy-type") + ": "); //$NON-NLS-1$
	proxyTypeLabel.setEnabled(false);


	hostLabel = new JLabel(Localizer.getString("dialogprefs.panel.proxy.host") + ": "); //$NON-NLS-1$
	hostLabel.setEnabled(false);
	hostTextField = new JTextField(18);
	hostTextField.setText(""); //$NON-NLS-1$
	hostTextField.setEnabled(false);

	JPanel hostPanel = new JPanel();
	hostPanel.add(hostLabel);
	hostPanel.add(hostTextField);

	portLabel = new JLabel(Localizer.getString("dialogprefs.panel.proxy.port") + ": "); //$NON-NLS-1$
	portLabel.setEnabled(false);
	portTextField = new JTextField(4);
	portTextField.setText(""); //$NON-NLS-1$
	portTextField.setEnabled(false);
	portTextField.setDocument(new DocumentRegExp("(\\d)*",5)); //$NON-NLS-1$

	JPanel portPanel = new JPanel();
	portPanel.add(portLabel);
	portPanel.add(portTextField);

	JPanel proxyServerPanel = new JPanel(new GridBagLayout());

	GridBagConstraints constraints;

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,12,5);
	proxyServerPanel.add(enableProxyButton,constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 2;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,12,10);
	constraints.anchor = GridBagConstraints.EAST;
	proxyServerPanel.add(proxyTypeLabel,constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,1,12,5);
	constraints.anchor = GridBagConstraints.EAST;
	proxyServerPanel.add(proxyType,constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 3;
	constraints.insets = new Insets(1,5,1,5);
	proxyServerPanel.add(hostPanel,constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,1,1,1);
	constraints.anchor = GridBagConstraints.EAST;
	proxyServerPanel.add(portPanel,constraints);

	enableAuthenticationButton = new JCheckBox(Localizer.getString("dialogprefs.panel.proxy.enable-authentication")); //$NON-NLS-1$
	enableAuthenticationButton.setActionCommand("Enable Authentication"); //$NON-NLS-1$
	enableAuthenticationButton.setEnabled(false);
	enableAuthenticationButton.addItemListener(this);

	JPanel enableAuthenticationPanel = new JPanel();
	enableAuthenticationPanel.add(enableAuthenticationButton);

	userNameLabel = new JLabel(Localizer.getString("dialogprefs.panel.proxy.username") + ": "); //$NON-NLS-1$
	userNameLabel.setEnabled(false);
	userNameTextField = new JTextField(7);
	userNameTextField.setText(""); //$NON-NLS-1$
	userNameTextField.setEnabled(false);

	JPanel userNamePanel = new JPanel();
	userNamePanel.add(userNameLabel);
	userNamePanel.add(userNameTextField);

	passwordLabel = new JLabel(Localizer.getString("dialogprefs.panel.proxy.password") + ": "); //$NON-NLS-1$
	passwordLabel.setEnabled(false);
	passwordTextField = new JTextField(7);
	passwordTextField.setText(""); //$NON-NLS-1$
	passwordTextField.setEnabled(false);

	JPanel passwordPanel = new JPanel() ;
	passwordPanel.add(passwordLabel);
	passwordPanel.add(passwordTextField);

	JPanel authenticationPanel = new JPanel(new GridBagLayout());
	authenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.proxy.authentication")), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.FIRST_LINE_START;
	authenticationPanel.add(enableAuthenticationPanel, constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.LAST_LINE_START;
	authenticationPanel.add(userNamePanel, constraints);

	constraints = new GridBagConstraints();
	constraints.gridx = 3;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.insets = new Insets(1,5,1,5);
	constraints.anchor = GridBagConstraints.LAST_LINE_END;
	authenticationPanel.add(passwordPanel,constraints);


	JPanel proxyPanel = new JPanel(new GridLayout(0, 1));
	proxyPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.proxy.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

	proxyPanel.add(proxyServerPanel);
	proxyPanel.add(authenticationPanel);

	String temp;

	if (((temp = config.getProxyHost()) != null) && !temp.equals("null")) //$NON-NLS-1$
	    hostTextField.setText(temp);

	if (((temp = config.getProxyPort()) != null) && !temp.equals("null")) //$NON-NLS-1$
	    portTextField.setText(temp);

	if (((temp = config.getProxyUser()) != null)&& !temp.equals("null")) //$NON-NLS-1$
	    userNameTextField.setText(temp);

	if (((temp = config.getProxyPassword()) != null) && !temp.equals("null")) //$NON-NLS-1$
	    passwordTextField.setText(temp);

	if (config.getProxyEnabled())
	    enableProxyButton.setSelected(true);

	if (config.getAuthenticationEnabled())
	    enableAuthenticationButton.setSelected(true);


	/* Miscellaneous panel */
	JPanel miscPanel = new JPanel();
	miscPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.miscellaneous.title")), BorderFactory.createEmptyBorder(12,1,16,1))); //$NON-NLS-1$
	miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.PAGE_AXIS));


	JPanel miscCheckBoxes = new JPanel(new GridLayout(3, 1));
	miscCheckBoxes.setBorder(BorderFactory.createEmptyBorder(10,15,35,5));


	loadDatabaseOnStartUp = new JCheckBox(Localizer.getString("dialogprefs.panel.miscellaneous.auto-load-database")); //$NON-NLS-1$
	loadDatabaseOnStartUp.setActionCommand("Load databse"); //$NON-NLS-1$

	if (config.getLoadDatabaseOnStartup())
	    loadDatabaseOnStartUp.setSelected(true);

	miscCheckBoxes.add(loadDatabaseOnStartUp);

	/* Enable seen editable */
	enableSeenEditable = new JCheckBox(Localizer.getString("dialogprefs.panel.miscellaneous.enable-seen-editable-main-window")); //$NON-NLS-1$

	enableSeenEditable.setActionCommand("Enable Seen"); //$NON-NLS-1$

	if (config.getSeenEditable())
	    enableSeenEditable.setSelected(true);

	miscCheckBoxes.add(enableSeenEditable);


	displayQueriesInTree = new JCheckBox(Localizer.getString("dialogprefs.panel.miscellaneous.use-directory-structure-to-group-queries")); //$NON-NLS-1$

	if (config.getDisplayQueriesInTree())
	    displayQueriesInTree.setSelected(true);

	miscCheckBoxes.add(displayQueriesInTree);

	miscPanel.add(miscCheckBoxes);

    
    
	JPanel titlePanel = new JPanel();
    
	//titlePanel.setLayout(new GridLayout(0,1));
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
	titlePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Imported Movie Titles"), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

    
    
	JPanel autoMovieToEndOfTitlePanel = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 3;
    
	JLabel autoMoveLabel = new JLabel(Localizer.getString("dialogprefs.panel.miscellaneous.auto-move-to-end-of-title") + ":");
    	
	autoMovieToEndOfTitlePanel.add(autoMoveLabel, c);
    
	/* Enable Automatic placement of 'The' at the end of title */
	enableAutoMoveThe = new JCheckBox("'The '"); //$NON-NLS-1$
	enableAutoMoveThe.setActionCommand("Enable auto move"); //$NON-NLS-1$

	if (config.getAutoMoveThe())
	    enableAutoMoveThe.setSelected(true);

	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 1;
    
	autoMovieToEndOfTitlePanel.add(enableAutoMoveThe, c);

	/* Enable Automatic placement of 'The' at the end of title */
	enableAutoMoveAnAndA = new JCheckBox("'A ' and 'An '"); //$NON-NLS-1$
	enableAutoMoveAnAndA.setActionCommand("Enable auto move"); //$NON-NLS-1$

	if (config.getAutoMoveAnAndA())
	    enableAutoMoveAnAndA.setSelected(true);

	c.gridx = 1;
	c.gridy = 1;
	c.gridwidth = 1;
    
	autoMovieToEndOfTitlePanel.add(enableAutoMoveAnAndA, c);
	autoMovieToEndOfTitlePanel.setMaximumSize(new Dimension((int) autoMovieToEndOfTitlePanel.getMaximumSize().getWidth(), (int) autoMovieToEndOfTitlePanel.getPreferredSize().getHeight()));
    
	titlePanel.add(autoMovieToEndOfTitlePanel);

	/* Insert title in specific language */
    
	JPanel akaTitlePanel = new JPanel();
	//akaTitlePanel.setLayout(new BoxLayout(akaTitlePanel, BoxLayout.Y_AXIS));
	akaTitlePanel.setLayout(new GridLayout(0, 1));
    
	storeAllAvailableAkaTitles = new JCheckBox("Store all available aka titles");
	noDuplicateAkaTitles = new JCheckBox("No duplicate aka titles");
	includeAkaLanguageCodes = new JCheckBox("Include comments and language codes");
	useLanguageSpecificTitle = new JCheckBox("Replace original title with aka title (with languageCode):");
    
    
	ArrayList langCodesList = new ArrayList(150);
	int index = 0;
    
	try {
    
	    InputStream inputStream = FileUtil.getResourceAsStream("/codecs/LanguageCodes.txt");
        
        //URL url = FileUtil.getFileURL("codecs/LanguageCodes.txt");
        
	    BufferedInputStream stream = new BufferedInputStream(inputStream);

	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    
    
	    String currentLangCode = config.getTitleLanguageCode();
    
	    if (currentLangCode.equals(""))
            currentLangCode = System.getProperty("user.language");
        
	    String line;
    
	    while ((line = reader.readLine()) != null) {
		
		if (line.startsWith(currentLangCode))
		    index = langCodesList.size();
        
		line = " " + line.replaceFirst("\t", " - ");
        
		langCodesList.add(line);
	    }
	    reader.close();
    
	} catch (IOException e) {
	    log.warn(e);
	}
    
    akaTitlePanel.add(storeAllAvailableAkaTitles);
    akaTitlePanel.add(noDuplicateAkaTitles);
    akaTitlePanel.add(includeAkaLanguageCodes);
    akaTitlePanel.add(useLanguageSpecificTitle);
    
	Object [] languageCodes = langCodesList.toArray();
    
    if (languageCodes.length > 0) {
    
	languageCodeSelector = new JComboBox(languageCodes);
    
	languageCodeSelector.setMaximumSize(languageCodeSelector.getPreferredSize());
    
    if (languageCodeSelector.getItemCount() > index)  
        languageCodeSelector.setSelectedIndex(index);
    
	storeAllAvailableAkaTitles.addItemListener(this);
	useLanguageSpecificTitle.addItemListener(this);
    
	if (!config.getStoreAllAkaTitles()) {
	    noDuplicateAkaTitles.setEnabled(false);
	}
    
	if (config.getStoreAllAkaTitles())
	    storeAllAvailableAkaTitles.setSelected(true);
        
	if (config.getNoDuplicateAkaTitles())
	    noDuplicateAkaTitles.setSelected(true);
   
	if (config.getIncludeAkaLanguageCodes())
	    includeAkaLanguageCodes.setSelected(true);
    
	if (config.getUseLanguageSpecificTitle()) {
	    useLanguageSpecificTitle.setSelected(true);
	}
	else
	    languageCodeSelector.setEnabled(false);
    
	akaTitlePanel.add(languageCodeSelector);
    }
    
	titlePanel.add(akaTitlePanel);
	miscPanel.add(titlePanel);
    
	/* Cover settings */
	JPanel coverPanel = new JPanel();
	coverPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.cover-settings.title")), BorderFactory.createEmptyBorder(12,30,16,0))); //$NON-NLS-1$
	coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.PAGE_AXIS));

	enablePreserveCoverRatio = new JCheckBox(Localizer.getString("dialogprefs.panel.cover-settings.preserve-aspect-ratio")); //$NON-NLS-1$
	enablePreserveCoverRatio.setActionCommand("Preserve Cover ratio"); //$NON-NLS-1$
	enablePreserveCoverRatio.addItemListener(this);

	if (config.getPreserveCoverAspectRatio() == 1)
	    enablePreserveCoverRatio.setSelected(true);

	coverPanel.add(enablePreserveCoverRatio);

	enablePreserveCoverRatioEpisodesOnly = new JCheckBox(Localizer.getString("dialogprefs.panel.cover-settings.preserve-aspect-ratio-episodes-only")); //$NON-NLS-1$
	enablePreserveCoverRatioEpisodesOnly.setActionCommand("Preserve Cover ratio episodes"); //$NON-NLS-1$
	enablePreserveCoverRatioEpisodesOnly.addItemListener(this);

	if (config.getPreserveCoverAspectRatio() == 2) {
	    enablePreserveCoverRatioEpisodesOnly.setSelected(true);
	    enablePreserveCoverRatio.setSelected(false);
	}

	coverPanel.add(enablePreserveCoverRatioEpisodesOnly);

	JPanel nocoverImagePanel = new JPanel();
	nocoverImagePanel.setLayout(new BoxLayout(nocoverImagePanel, BoxLayout.PAGE_AXIS));
	nocoverImagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,63,20,5), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.cover-settings.nocover.title")), BorderFactory.createEmptyBorder(0,5,5,5)))); //$NON-NLS-1$

	ButtonGroup nocoverGroup = new ButtonGroup();

	pumaCover = new JRadioButton(Localizer.getString("dialogprefs.panel.cover-settings.nocover.use-puma")); //$NON-NLS-1$
	jaguarCover = new JRadioButton(Localizer.getString("dialogprefs.panel.cover-settings.nocover.use-jaguar")); //$NON-NLS-1$

	if (config.getNoCover().equals("nocover_jaguar.png")) //$NON-NLS-1$
	    jaguarCover.setSelected(true);
	else
	    pumaCover.setSelected(true);

	nocoverGroup.add(pumaCover);
	nocoverGroup.add(jaguarCover);

	nocoverImagePanel.add(pumaCover);
	nocoverImagePanel.add(jaguarCover);

	coverPanel.add(nocoverImagePanel);

	if (MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) {

	    enableStoreCoversLocally = new JCheckBox(Localizer.getString("dialogprefs.panel.cover-settings.store-covers-locally")); //$NON-NLS-1$
	    enableStoreCoversLocally.setActionCommand("Store covers locally"); //$NON-NLS-1$

	    if (config.getStoreCoversLocally())
		enableStoreCoversLocally.setSelected(true);

	    coverPanel.add(enableStoreCoversLocally);
	}



	/* Movie List Options  */

	JPanel movieListPanel = new JPanel();
	movieListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.movie-list.title")), BorderFactory.createEmptyBorder(0,10,5,10))); //$NON-NLS-1$
	movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.PAGE_AXIS));

	/* Enable rightclick by ctrl key */
	enableRightclickByCtrl = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.enable-right-click-by-ctrl-in-movie-list")); //$NON-NLS-1$
	enableRightclickByCtrl.setActionCommand("Enable right click by ctrl"); //$NON-NLS-1$

	if (config.getEnableCtrlMouseRightClick())
	    enableRightclickByCtrl.setSelected(true);

	movieListPanel.add(enableRightclickByCtrl);

	/* Enable load last used list */
	enableLoadLastUsedList = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.load-last-used-list")); //$NON-NLS-1$
	enableLoadLastUsedList.setActionCommand("Enable lastLoadedList"); //$NON-NLS-1$

	if (config.getLoadLastUsedListAtStartup())
	    enableLoadLastUsedList.setSelected(true);

	movieListPanel.add(enableLoadLastUsedList);


	/* Enable Use JTree Icons */
	enableUseJTreeIcons = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.enable-icons-in-movie-list")); //$NON-NLS-1$
	enableUseJTreeIcons.setActionCommand("Enable JTree Icons"); //$NON-NLS-1$
        enableUseJTreeIcons.addActionListener(this);

	if (config.getUseJTreeIcons())
	    enableUseJTreeIcons.setSelected(true);

	movieListPanel.add(enableUseJTreeIcons);

        /* Enable Use JTree Covers */
        enableUseJTreeCovers = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.enable-covers-in-movie-list")); //$NON-NLS-1$
        enableUseJTreeCovers.setActionCommand("Enable JTree Covers"); //$NON-NLS-1$
        enableUseJTreeCovers.addActionListener(this);

        if (config.getUseJTreeCovers())
            enableUseJTreeCovers.setSelected(true);

        movieListPanel.add(enableUseJTreeCovers);

	/* Enable highlight entire row */
	enableHighlightEntireRow = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.highlight-entire-rom-in-movie-list")); //$NON-NLS-1$
	enableHighlightEntireRow.setActionCommand("Enable highlight row"); //$NON-NLS-1$

	if (config.getMovieListHighlightEntireRow())
	    enableHighlightEntireRow.setSelected(true);

	movieListPanel.add(enableHighlightEntireRow);

        // Rowheight including example
        JPanel rowHeightPanel = new JPanel();
        rowHeightPanel.setLayout(new BorderLayout());
        rowHeightPanel.setMinimumSize(new Dimension(0, 150));
        rowHeightPanel.setPreferredSize(new Dimension(0, 150));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root"); //$NON-NLS-1$
        root.add(new DefaultMutableTreeNode(new ModelMovie(-1, "Example 1", null, null, "Year"))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new DefaultMutableTreeNode(new ModelMovie(-1, "Example 2", null, null, "Year"))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new DefaultMutableTreeNode(new ModelMovie(-1, "Example 3", null, null, "Year"))); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(new DefaultMutableTreeNode(new ModelMovie(-1, "Example 4", null, null, "Year"))); //$NON-NLS-1$ //$NON-NLS-2$
        exampleTree = new JTree(root);
        JScrollPane scroller = new JScrollPane(exampleTree);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroller.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        exampleTree.setRootVisible(false);
        exampleTree.setShowsRootHandles(true);
        exampleTree.setCellRenderer(new ExtendedTreeCellRenderer(MovieManager.getDialog(), scroller, exampleConfig));
        rowHeightSlider = new JSlider(6, 100, config.getMovieListRowHeight());
        rowHeightSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    updateRowHeightExample();
		}
	    });
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Scale"), BorderLayout.WEST); //$NON-NLS-1$
        p.add(rowHeightSlider, BorderLayout.CENTER);
        rowHeightPanel.add(p, BorderLayout.SOUTH);
        rowHeightPanel.add(scroller, BorderLayout.CENTER);
        movieListPanel.add(rowHeightPanel);
        updateRowHeightExample();

        
        
        /* Player panel */
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new GridBagLayout());
                
        playerPanel.setBorder(BorderFactory.createCompoundBorder(
								 BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Player Path "),
								 BorderFactory.createEmptyBorder(12,1,16,1)));
        
        enableUseDefaultWindowsPlayer = new JCheckBox("Use the default Windows player");
        enableUseDefaultWindowsPlayer.setSelected(config.getUseDefaultWindowsPlayer());
                
        if (!MovieManager.isWindows())
            enableUseDefaultWindowsPlayer.setEnabled(false);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        playerPanel.add(enableUseDefaultWindowsPlayer, c);
        
        JLabel playerLabel = new JLabel("Player Location:");
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        playerPanel.add(playerLabel, c);
        
        
        
        mediaPlayerPathField = new JTextField(32);
        mediaPlayerPathField.setText(config.getMediaPlayerPath());
                
        JButton mediaPlayerBrowse = new JButton("Browse");
        
        mediaPlayerBrowse.setActionCommand("Browse Player Path");
        mediaPlayerBrowse.addActionListener(new ActionListener() {
            
		public void actionPerformed(ActionEvent arg0) {
		    // check if there is a file selected

		    JFileChooser chooser = new JFileChooser();
		    int returnVal = chooser.showDialog(null, "Choose");
		    if (returnVal != JFileChooser.APPROVE_OPTION)
		        return;

		    try {
			String location = chooser.getSelectedFile().getCanonicalPath();
                
			if (location != null)
			    mediaPlayerPathField.setText(location);
		    } catch (IOException e) {
			log.warn("Failed to retrieve player path");
		    }
                
		}});
                
        JPanel mediaPlayerFilePanel = new JPanel ();
        mediaPlayerFilePanel.setLayout(new BoxLayout(mediaPlayerFilePanel, BoxLayout.X_AXIS));
        mediaPlayerFilePanel.add(mediaPlayerPathField);
        //mediaPlayerFilePanel.add(Box.createHorizontalGlue());
        mediaPlayerFilePanel.add(mediaPlayerBrowse);
        
        c.gridx = 0;
        c.gridy = 1;
        playerPanel.add(mediaPlayerFilePanel, c);
        
        /* Browser Path */
        JPanel browserPanel = new JPanel();
        browserPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        
        browserPanel.setBorder(BorderFactory.createCompoundBorder(
                                 BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Web Browser "),
                                 BorderFactory.createEmptyBorder(12,3,16,3)));
        
        
        
        JPanel browserOptionPanel = new JPanel(new GridLayout(4, 2));
        
        
        enableUseDefaultWindowsBrowser = new JRadioButton("Windows default");
        enableCustomBrowser =            new JRadioButton("Custom browser");
        browserOptionOpera =             new JRadioButton("Opera");
        browserOptionFirefox =           new JRadioButton("Firefox");
        browserOptionMozilla =           new JRadioButton("Mozilla");
        browserOptionSafari =            new JRadioButton("Safari");
        browserOptionNetscape =          new JRadioButton("Netscape");
        browserOptionIE =                new JRadioButton("IE (Internet Explorer)");
                
        
        String browser = config.getSystemWebBrowser();
        
        if (browser.equals("Default"))
            enableUseDefaultWindowsBrowser.setSelected(true);
        else if (browser.equals("Custom"))
            enableCustomBrowser.setSelected(true);
        else if (browser.equals("Opera"))
            browserOptionOpera.setSelected(true);
        else if (browser.equals("Firefox"))
            browserOptionFirefox.setSelected(true);
        else if (browser.equals("Mozilla"))
            browserOptionMozilla.setSelected(true);
        else if (browser.equals("Safari"))
            browserOptionSafari.setSelected(true);
        else if (browser.equals("Netscape"))
            browserOptionNetscape.setSelected(true);
        else if (browser.equals("IE"))
            browserOptionIE.setSelected(true);
                                
                                
        ButtonGroup browserOptionGroup = new ButtonGroup();
        browserOptionGroup.add(browserOptionOpera);
        browserOptionGroup.add(browserOptionMozilla);
        browserOptionGroup.add(browserOptionFirefox);
        browserOptionGroup.add(browserOptionNetscape);
        browserOptionGroup.add(browserOptionSafari);
        browserOptionGroup.add(browserOptionIE);
        browserOptionGroup.add(enableCustomBrowser);
        browserOptionGroup.add(enableUseDefaultWindowsBrowser);
         
        browserOptionPanel.add(browserOptionOpera);
        browserOptionPanel.add(browserOptionMozilla);
        browserOptionPanel.add(browserOptionFirefox);
        browserOptionPanel.add(browserOptionNetscape);
        browserOptionPanel.add(browserOptionSafari);
        browserOptionPanel.add(browserOptionIE);
        browserOptionPanel.add(enableCustomBrowser);
        browserOptionPanel.add(enableUseDefaultWindowsBrowser);
        
        
        
       browserOptionOpera.addItemListener(this);
       browserOptionMozilla.addItemListener(this);
       browserOptionFirefox.addItemListener(this);
        browserOptionNetscape.addItemListener(this);
        browserOptionSafari.addItemListener(this);
        browserOptionIE.addItemListener(this);
        enableCustomBrowser.addItemListener(this);
        enableUseDefaultWindowsBrowser.addItemListener(this);
        
        //browserPanel.add(browserOptionPanel);
        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        browserPanel.add(browserOptionPanel, c);
        
        
        JLabel browserLabel = new JLabel("Browser Location:");
        
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        browserPanel.add(browserLabel, c);
        
       
        
        customBrowserPathField = new JTextField(32);
        customBrowserPathField.setText(config.getBrowserPath());
        
        browserBrowse = new JButton("Browse");
        browserBrowse.setActionCommand("Browse Path");
        
        browserBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            // check if there is a file selected

            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showDialog(null, "Choose");
            if (returnVal != JFileChooser.APPROVE_OPTION)
            return;

            try {
            String location = chooser.getSelectedFile().getCanonicalPath();
                
            if (location != null)
                customBrowserPathField.setText(location);
            } catch (IOException e) {
            log.warn("Failed to retrieve browser path");
            }
                
        }});
                
        JPanel browserFilePanel = new JPanel();
        browserFilePanel.setLayout(new BoxLayout(browserFilePanel, BoxLayout.X_AXIS));
        browserFilePanel.add(customBrowserPathField);
        browserFilePanel.add(Box.createHorizontalGlue());
        browserFilePanel.add(browserBrowse);
        
        c.gridx = 0;
        c.gridy = 2;
        browserPanel.add(browserFilePanel, c);
        browserPanel.add(Box.createVerticalGlue());
        
        
        setBrowserComponentsEnabled();
        
        /* Program Paths */
        
        JPanel programPathsPanel = new JPanel();
        programPathsPanel.setLayout(new BoxLayout(programPathsPanel, BoxLayout.Y_AXIS));
        
        programPathsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " External Programs "),
                BorderFactory.createEmptyBorder(12,3,16,3)));
        
        programPathsPanel.add(playerPanel);
        
        programPathsPanel.add(browserPanel);
        
        
	/* OK panel */
	JPanel okPanel = new JPanel();
	okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	okPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));

	JButton buttonOk = new JButton(Localizer.getString("dialogprefs.panel.button-ok.text")); //$NON-NLS-1$
	buttonOk.setToolTipText(Localizer.getString("dialogprefs.panel.button-ok.tooltip")); //$NON-NLS-1$
	buttonOk.addActionListener(this);
	okPanel.add(buttonOk);

	all = new JTabbedPane();
	all.setBorder(BorderFactory.createEmptyBorder(8,8,5,8));
	all.add(Localizer.getString("dialogprefs.tab"), layoutPanel); //$NON-NLS-1$
	all.add(Localizer.getString("dialogprefs.panel.proxy.title"), proxyPanel); //$NON-NLS-1$
	all.add(Localizer.getString("dialogprefs.panel.miscellaneous.title"), miscPanel); //$NON-NLS-1$
	all.add(Localizer.getString("dialogprefs.panel.cover-settings.title"), coverPanel); //$NON-NLS-1$
	all.add(Localizer.getString("dialogprefs.panel.movie-list.title"), movieListPanel); //$NON-NLS-1$
	all.add("Program Paths", programPathsPanel);
    
	all.setSelectedIndex(config.getLastPreferencesTabIndex());

	contentPane = getContentPane();
	contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
    
    
	contentPane.add(all);
	contentPane.add(okPanel);

	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	/*Display the window.*/
	pack();
	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
		    (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

        updateEnabling();
    }

    
    void setBrowserComponentsEnabled() {
        
        if (!MovieManager.isMac()) {
            browserOptionSafari.setEnabled(false);
        
            if (!MovieManager.isWindows())
                browserOptionIE.setEnabled(false);
        }
        
        if (enableCustomBrowser.isSelected()) {
            customBrowserPathField.setEnabled(true);
            browserBrowse.setEnabled(true);
        }
        else {
            customBrowserPathField.setEnabled(false);
            browserBrowse.setEnabled(false);
        }
        
        if (!MovieManager.isWindows())
            enableUseDefaultWindowsBrowser.setEnabled(false);
    }
    
    private void updateRowHeightExample() {
        int rowHeight = rowHeightSlider.getValue();
        exampleConfig.setMovieListRowHeight(rowHeight);
        exampleConfig.setUseJTreeIcons(enableUseJTreeIcons.isSelected());
        exampleConfig.setUseJTreeCovers(enableUseJTreeCovers.isSelected());
        exampleConfig.setMovieListHighlightEntireRow(enableHighlightEntireRow.isSelected());
        exampleConfig.setStoreCoversLocally(config.getStoreCoversLocally());
        exampleTree.setRowHeight(rowHeight + 2);
        exampleTree.updateUI();
    }

    
    boolean saveSettings() {
  
	/* Saving the tab index */
	config.setLastPreferencesTabIndex(all.getSelectedIndex());

	/* Save proxy settings */
	config.setProxyType((String) proxyType.getSelectedItem());
	config.setProxyHost(hostTextField.getText());
	config.setProxyPort(portTextField.getText());
	config.setProxyUser(userNameTextField.getText());
	config.setProxyPassword(passwordTextField.getText());

	if (enableProxyButton.isSelected())
	    config.setProxyEnabled(true);
	else
	    config.setProxyEnabled(false);

	if (enableAuthenticationButton.isSelected())
	    config.setAuthenticationEnabled(true);
	else
	    config.setAuthenticationEnabled(false);


	config.setMediaPlayerPath(mediaPlayerPathField.getText());
	config.setBrowserPath(customBrowserPathField.getText());

    if (enableUseDefaultWindowsPlayer.isSelected())
        config.setUseDefaultWindowsPlayer(true);
    else
        config.setUseDefaultWindowsPlayer(false);
        
    
    /* Web Browser */
    if (enableUseDefaultWindowsBrowser.isSelected())
        config.setSystemWebBrowser("Default");
    else if (browserOptionOpera.isSelected())
        config.setSystemWebBrowser("Opera");
    else if (browserOptionFirefox.isSelected())
        config.setSystemWebBrowser("Firefox");
    else if (browserOptionMozilla.isSelected())
        config.setSystemWebBrowser("Mozilla");
    else if (browserOptionSafari.isSelected())
        config.setSystemWebBrowser("Safari");
    else if (browserOptionNetscape.isSelected())
        config.setSystemWebBrowser("Netscape");
    else if (browserOptionIE.isSelected())
        config.setSystemWebBrowser("IE");
    else if (enableCustomBrowser.isSelected()) {
        config.setSystemWebBrowser("Custom");
        
        File browser = new File(customBrowserPathField.getText());
        
        if (!browser.isFile()) {
            DialogAlert alert = new DialogAlert(this, Localizer.getString("dialogprefs.alert.title.alert"), "The custom browser path is invalid."); //$NON-NLS-1$ 
            GUIUtil.showAndWait(alert, true);
            return false;
        }
        
        config.setBrowserPath(browser.getAbsolutePath());
    }
    
    
	/* automatic move of 'The' */
	if (enableAutoMoveThe.isSelected())
	    config.setAutoMoveThe(true);
	else
	    config.setAutoMoveThe(false);

	/* automatic move of 'An and A' */
	if (enableAutoMoveAnAndA.isSelected())
	    config.setAutoMoveAnAndA(true);
	else
	    config.setAutoMoveAnAndA(false);
    
    
	if (storeAllAvailableAkaTitles.isSelected())
	    config.setStoreAllAkaTitles(true);
	else
	    config.setStoreAllAkaTitles(false);
    
    
	if (noDuplicateAkaTitles.isSelected())
	    config.setNoDuplicateAkaTitles(true);
	else
	    config.setNoDuplicateAkaTitles(false);
    
   
	if (includeAkaLanguageCodes.isSelected())
	    config.setIncludeAkaLanguageCodes(true);
	else
	    config.setIncludeAkaLanguageCodes(false);
    
    
	if (useLanguageSpecificTitle.isSelected()) {
	    config.setUseLanguageSpecificTitle(true);
        
	    String value = (String) languageCodeSelector.getSelectedItem();
	    value = value.substring(1, 3);
	    config.setTitleLanguageCode(value);
	}
	else
	    config.setUseLanguageSpecificTitle(false);
    
    
	/* automatically load database at startup */
	if (loadDatabaseOnStartUp.isSelected())
	    config.setLoadDatabaseOnStartup(true);
	else
	    config.setLoadDatabaseOnStartup(false);

	/* seen editable */
	if (enableSeenEditable.isSelected())
	    config.setSeenEditable(true);
	else
	    config.setSeenEditable(false);

	/* Display Queries In JTree */
	if (displayQueriesInTree.isSelected()) {
	    config.setUseDisplayQueriesInTree(true);
        }
        else {
            config.setUseDisplayQueriesInTree(false);
	}

	/* rightclick by ctrl */
	if (enableRightclickByCtrl.isSelected())
	    config.setEnableCtrlMouseRightClick(true);
	else
	    config.setEnableCtrlMouseRightClick(false);

	/* Load Last List At Startup */
	if (enableLoadLastUsedList.isSelected())
	    config.setLoadLastUsedListAtStartup(true);
	else
	    config.setLoadLastUsedListAtStartup(false);

	/* Icons in JTree */
	if (enableUseJTreeIcons.isSelected())
	    config.setUseJTreeIcons(true);
	else
	    config.setUseJTreeIcons(false);

        /* Covers in JTree */
        if (enableUseJTreeCovers.isSelected())
	    config.setUseJTreeCovers(true);
        else
	    config.setUseJTreeCovers(false);

	/* Highlight entire row */
	if (enableHighlightEntireRow.isSelected()) {
	    config.setMovieListHighlightEntireRow(true);
	}
	else {
	    config.setMovieListHighlightEntireRow(false);
	}

        /* Rowheight */
        config.setMovieListRowHeight(rowHeightSlider.getValue());

	if (enablePreserveCoverRatioEpisodesOnly.isSelected())
	    config.setPreserveCoverAspectRatio(2);
	else if (enablePreserveCoverRatio.isSelected())
	    config.setPreserveCoverAspectRatio(1);
	else
	    config.setPreserveCoverAspectRatio(0);

	if (pumaCover.isSelected())
	    config.setNoCover("nocover_puma.png"); //$NON-NLS-1$
	else
	    config.setNoCover("nocover_jaguar.png"); //$NON-NLS-1$

	if (enableStoreCoversLocally != null && enableStoreCoversLocally.isSelected()) {

	    if (!(new File(config.getCoversFolder()).isDirectory())) {

		DialogAlert alert = new DialogAlert(this, Localizer.getString("dialogprefs.alert.title.alert"), Localizer.getString("dialogprefs.alert.message.covers-dir-not-existing")); //$NON-NLS-1$ //$NON-NLS-2$
		GUIUtil.showAndWait(alert, true);
		
		DialogFolders dialogFolders = new DialogFolders();
		GUIUtil.showAndWait(dialogFolders, true);
		
		if (!(new File(config.getCoversFolder()).isDirectory())) {
		    enableStoreCoversLocally.setSelected(false);
		}
	    }

	    if (enableStoreCoversLocally.isSelected())
		config.setStoreCoversLocally(true);
	    else
		config.setStoreCoversLocally(false);
	}
	else
	    config.setStoreCoversLocally(false);

	return true;
    }


    void setCustomLookAndFeel(int counter) {

	String selectedItem = (String) lafChooser.getSelectedItem();
	String selectedItemClass = ""; //$NON-NLS-1$

	try {
	    for (int i = 0; i < installedLookAndFeels.length; i++) {
		if (installedLookAndFeels[i].getName().equals(selectedItem)) {
		    selectedItemClass = installedLookAndFeels[i].getClassName();
		    break;
		}
	    }

	    if (selectedItemClass.equals("")) { //$NON-NLS-1$
		return;
	    }

	    UIManager.setLookAndFeel(selectedItemClass);
	    updateLookAndFeel();

	} catch (Exception e) {
	    log.error("", e); //$NON-NLS-1$

	    String lafName = (String) lafChooser.getSelectedItem();
	    lafChooser.setSelectedItem(config.getLookAndFeelString());

	    /* Calls itself recursively to restore the old look and feel */
	    if (counter < 1) {
		showErrorMessage(e.getMessage(), lafName);
		setCustomLookAndFeel(counter+1);
	    }
	    else {
		showErrorMessage("You're advised to restart the application",""); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    return;
	}
	config.setLookAndFeelString(selectedItem);
	//MovieManager.getIt().addSplitPaneMouseListeners();
    }

    void setSkinlfLookAndFeel() {
	String selectedItem = (String) skinlfThemePackChooser.getSelectedItem();
	String skinlfThemePackPath = config.getSkinlfThemePackDir() + selectedItem;

	try {
	    Skin skin = null;

	    if (MovieManager.isApplet())
	    	skin = SkinLookAndFeel.loadThemePack(FileUtil.getAppletFile(skinlfThemePackPath).toURL());
	    else {
			skin = SkinLookAndFeel.loadThemePack(skinlfThemePackPath);
	    }
	    SkinLookAndFeel.setSkin(skin);

	    LookAndFeel laf = new SkinLookAndFeel();
	    UIManager.setLookAndFeel(laf);
	    UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel"); //$NON-NLS-1$

	    updateLookAndFeel();

	} catch (Exception e) {
	    log.error("Exception: "+ e.getMessage()); //$NON-NLS-1$
	    showErrorMessage(e.getMessage(), "SkinLF"); //$NON-NLS-1$
	    return;
	}
	config.setSkinlfThemePack(selectedItem);
    }

    void setOyoahaLookAndFeel() {
	String selectedItem = (String) oyoahaThemePackChooser.getSelectedItem();
	String oyoahaThemePackPath = config.getOyoahaThemePackDir() + selectedItem;

	try {
	    File file = new File(oyoahaThemePackPath);
	    OyoahaLookAndFeel lnf = new OyoahaLookAndFeel();

	    /*If the index 0 is selected the default theme is loaded*/
	    if(file.exists() && oyoahaThemePackChooser.getSelectedIndex() != 0)
		lnf.setOyoahaTheme(file);

	    UIManager.setLookAndFeel(lnf);
	    updateLookAndFeel();

	} catch (Exception e) {
	    log.error("Exception: "+ e.getMessage()); //$NON-NLS-1$
	    showErrorMessage(e.getMessage(),"Oyoaha"); //$NON-NLS-1$
	    return;
	}
	config.setOyoahaThemePack(selectedItem);
	//MovieManager.getIt().addSplitPaneMouseListeners();
    }

    void showErrorMessage(String error, String name) {

	String message = Localizer.getString("dialogprefs.alert.message.laf-improperly-installed-or-not-supported-by-jre")+ System.getProperty("java.version"); //$NON-NLS-1$ //$NON-NLS-2$

	if (name.equals("")) //$NON-NLS-1$
	    message = Localizer.getString("dialogprefs.alert.message.laf.this")+ message; //$NON-NLS-1$
	else
	    message = name + message;

	if (error != null && error.indexOf("not supported on this platform") != -1) //$NON-NLS-1$
	    message = Localizer.getString("dialogprefs.alert.message.laf-not-supported"); //$NON-NLS-1$

	if (error != null && error.indexOf("You're advised to restart the application") != -1) { //$NON-NLS-1$
	    message = Localizer.getString("dialogprefs.alert.message.advised-to-restart-application"); //$NON-NLS-1$
	    error = ""; //$NON-NLS-1$
	}


	DialogAlert alert = new DialogAlert(this, Localizer.getString("dialogprefs.alert.title.laf-error"), message, error); //$NON-NLS-1$
	GUIUtil.showAndWait(alert, true);
    }

    void setLafChooserPreferredSize() {

	if (skinlfThemePackChooser != null && oyoahaThemePackChooser != null) {
	    double width = lafChooser.getPreferredSize().getWidth() -  oyoahaThemePackChooser.getPreferredSize().getWidth();
	    width = oyoahaThemePackChooser.getPreferredSize().getWidth()+ (width/2);
	    skinlfThemePackChooser.setPreferredSize(new Dimension((int) width, (int) skinlfThemePackChooser.getPreferredSize().getHeight()));
	}
	else if (skinlfThemePackChooser != null && oyoahaThemePackChooser == null) {
	    skinlfThemePackChooser.setPreferredSize(new Dimension(lafChooser.getPreferredSize()));
	}
    }

    void updateLookAndFeel() throws Exception {
	MovieManager.getDialog().toolBar.updateToolButtonBorder();

	SwingUtilities.updateComponentTreeUI(MovieManager.getDialog());
	MovieManager.getDialog().toolBar.updateToolButtonBorder();

	MovieManager.getDialog().getDateField().setOpaque(false);
	MovieManager.getDialog().getDateField().setBorder(null);

	MovieManager.getDialog().getTitleField().setOpaque(false);
	MovieManager.getDialog().getTitleField().setBorder(null);

	MovieManager.getDialog().getDirectedByField().setOpaque(false);
	MovieManager.getDialog().getDirectedByField().setBorder(null);

	MovieManager.getDialog().getWrittenByField().setOpaque(false);
	MovieManager.getDialog().getWrittenByField().setBorder(null);

	MovieManager.getDialog().getGenreField().setOpaque(false);
	MovieManager.getDialog().getGenreField().setBorder(null);

	MovieManager.getDialog().getRatingField().setOpaque(false);
	MovieManager.getDialog().getRatingField().setBorder(null);

	MovieManager.getDialog().getCountryTextField().setOpaque(false);
	MovieManager.getDialog().getCountryTextField().setBorder(null);

	MovieManager.getDialog().getLanguageTextField().setOpaque(false);
	MovieManager.getDialog().getLanguageTextField().setBorder(null);

	/*If the search dialog is opened it will be updated*/
	if (DialogSearch.getDialogSearch() != null) {
	    SwingUtilities.updateComponentTreeUI(DialogSearch.getDialogSearch());
	    DialogSearch.getDialogSearch().pack();
	}

	SwingUtilities.updateComponentTreeUI(this);
	pack();
	setLafChooserPreferredSize();
	MovieManager.getDialog().toolBar.updateToolButtonBorder();

	MovieManager.getDialog().validate();
    }


    void setDefaultLookAndFeelDecoration(boolean decorated) {

	if (decorated) {
	    config.setDefaultLookAndFeelDecorated(true);

	    javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
	    javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);

	    MovieManager.getDialog().dispose();
	    MovieManager.getDialog().setUndecorated(true);
	    MovieManager.getDialog().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
	    GUIUtil.show(MovieManager.getDialog(), true);
	    
	    /* Updating DialogSearch if open */
	    if (DialogSearch.getDialogSearch() != null) {
		DialogSearch.getDialogSearch().dispose();
		DialogSearch.getDialogSearch().setUndecorated(true);
		DialogSearch.getDialogSearch().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.PLAIN_DIALOG);
		DialogSearch.getDialogSearch().pack();
		
		GUIUtil.show(DialogSearch.getDialogSearch(), true);
	    }

	    /* This */
	    dispose();
	    setUndecorated(true);
	    getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
	    pack();
	    GUIUtil.show(this, true);
	}
	else {
	    config.setDefaultLookAndFeelDecorated(false);

	    javax.swing.JFrame.setDefaultLookAndFeelDecorated(false);
	    javax.swing.JDialog.setDefaultLookAndFeelDecorated(false);

	    MovieManager.getDialog().dispose();
	    MovieManager.getDialog().setUndecorated(false);
	    MovieManager.getDialog().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
	    GUIUtil.show(MovieManager.getDialog(), true);

	    /* Updating DialogSearch if open */
	    if (DialogSearch.getDialogSearch() != null) {
		DialogSearch.getDialogSearch().dispose();
		DialogSearch.getDialogSearch().setUndecorated(false);
		DialogSearch.getDialogSearch().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
		DialogSearch.getDialogSearch().pack();
		GUIUtil.show(DialogSearch.getDialogSearch(), true);
	    }

	    /* This */
	    dispose();
	    setUndecorated(false);
	    getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
	    pack();
	    
	    GUIUtil.show(this, true);
	}
    }

    private void updateEnabling() {
	if(enableUseJTreeIcons.isSelected()) {
	    enableUseJTreeCovers.setEnabled(true);
	}
	else {
	    enableUseJTreeCovers.setEnabled(false);
	    enableUseJTreeCovers.setSelected(false);
	}
    }

    public void actionPerformed(ActionEvent event) {
	log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

	/* OK - Saves settings */
	if (event.getActionCommand().equals("OK")) { //$NON-NLS-1$
	    
       if (saveSettings()) {
           dispose();
           
           MovieManager.getDialog().updateJTreeIcons();

           /* Necessary to update the icons in the movielist */
           MovieManager.getDialog().getMoviesList().updateUI();
           MovieManagerCommandSelect.execute();
           
           return;
       }
    }
        
	if (event.getActionCommand().equals("Enable JTree Icons") || event.getActionCommand().equals("Enable JTree Covers")) { //$NON-NLS-1$ //$NON-NLS-2$
	    updateEnabling();
	    updateRowHeightExample();
	}

	if (event.getActionCommand().equals("Enable LookAndFeel")) { //$NON-NLS-1$

	    if (enableSkinlf != null && enableSkinlf.isSelected()) {

		skinlfThemePackChooser.setEnabled(true);
		lafChooser.setEnabled(false);

		if (oyoahaThemePackChooser != null)
		    oyoahaThemePackChooser.setEnabled(false);

		config.setLookAndFeelType(1);
		setSkinlfLookAndFeel();
	    }

	    else if(enableOyoaha != null && enableOyoaha.isSelected()) {

		oyoahaThemePackChooser.setEnabled(true);
		lafChooser.setEnabled(false);

		if (skinlfThemePackChooser != null)
		    skinlfThemePackChooser.setEnabled(false);

		config.setLookAndFeelType(2);
		setOyoahaLookAndFeel();

		MovieManager.getDialog().toolBar.updateToolButtonBorder();
	    }

	    else {
		lafChooser.setEnabled(true);

		if (oyoahaThemePackChooser != null)
		    oyoahaThemePackChooser.setEnabled(false);
		if (skinlfThemePackChooser != null)
		    skinlfThemePackChooser.setEnabled(false);

		config.setLookAndFeelType(0);
		setCustomLookAndFeel(0);


	    }
	}


	if (event.getActionCommand().equals("ToolBarButton")) { //$NON-NLS-1$

	    if (regularToolBarButtons.isSelected())
		config.setRegularToolButtonsUsed(true);
	    else
		config.setRegularToolButtonsUsed(false);

	    MovieManager.getDialog().toolBar.updateToolButtonBorder();
	}


	if (event.getActionCommand().equals("SeenIcon")) { //$NON-NLS-1$

	    if (regularSeenIcon.isSelected()) {
		MovieManager.getDialog().getSeen().setIcon(new ImageIcon(FileUtil.getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
		MovieManager.getDialog().getSeen().setSelectedIcon(new ImageIcon(FileUtil.getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
		config.setUseRegularSeenIcon(true);

	    }
	    else {
		MovieManager.getDialog().getSeen().setIcon(null);
		MovieManager.getDialog().getSeen().setSelectedIcon(null);
		config.setUseRegularSeenIcon(false);
	    }

	    MovieManager.getDialog().getSeen().updateUI();
	}

	if (event.getActionCommand().equals("DefaultLafDecorated")) { //$NON-NLS-1$
	    setDefaultLookAndFeelDecoration(defaultLafDecoratedButton.isSelected());
	}


	/*Layout*/
	if (event.getSource().equals(lafChooser)) {
	    setCustomLookAndFeel(0);
	    MovieManager.getDialog().updateLookAndFeelValues();
	}
	if (event.getSource().equals(skinlfThemePackChooser)) {
	    setSkinlfLookAndFeel();
	    MovieManager.getDialog().updateLookAndFeelValues();
	}

	if (event.getSource().equals(oyoahaThemePackChooser)) {
	    setOyoahaLookAndFeel();
	    MovieManager.getDialog().updateLookAndFeelValues();
	}

	MovieManager.getDialog().getMoviesList().requestFocus(true);
    }


    public void itemStateChanged(ItemEvent event) {

	Object source = event.getItemSelectable();

	if (source.equals(enableProxyButton)) {

	    if (enableProxyButton.isSelected()) {
		proxyTypeLabel.setEnabled(true);
		proxyType.setEnabled(true);
		hostTextField.setEnabled(true);
		hostLabel.setEnabled(true);
		portTextField.setEnabled(true);
		portLabel.setEnabled(true);
		enableAuthenticationButton.setEnabled(true);

		if (enableAuthenticationButton.isSelected()) {
		    userNameTextField.setEnabled(true);
		    userNameLabel.setEnabled(true);
		    passwordTextField.setEnabled(true);
		    passwordLabel.setEnabled(true);
		}
		else {
		    userNameTextField.setEnabled(false);
		    userNameLabel.setEnabled(false);
		    passwordTextField.setEnabled(false);
		    passwordLabel.setEnabled(false);
		}
	    }
	    else {
		proxyTypeLabel.setEnabled(false);
		proxyType.setEnabled(false);
		hostTextField.setEnabled(false);
		hostLabel.setEnabled(false);
		portTextField.setEnabled(false);
		portLabel.setEnabled(false);

		enableAuthenticationButton.setEnabled(false);
		userNameTextField.setEnabled(false);
		userNameLabel.setEnabled(false);
		passwordTextField.setEnabled(false);
		passwordLabel.setEnabled(false);
	    }
	}

	if (source.equals(enableAuthenticationButton)) {

	    if (enableProxyButton.isSelected() && enableAuthenticationButton.isSelected()) {
		userNameTextField.setEnabled(true);
		userNameLabel.setEnabled(true);
		passwordTextField.setEnabled(true);
		passwordLabel.setEnabled(true);
	    }
	    else {
		userNameTextField.setEnabled(false);
		userNameLabel.setEnabled(false);
		passwordTextField.setEnabled(false);
		passwordLabel.setEnabled(false);
	    }
	}

	if (source.equals(enablePreserveCoverRatioEpisodesOnly)) {

	    if (enablePreserveCoverRatioEpisodesOnly.isSelected()) {
		enablePreserveCoverRatio.setSelected(false);
		enablePreserveCoverRatio.setEnabled(false);
	    }
	    else
		enablePreserveCoverRatio.setEnabled(true);
	}
       
    
	/* Misc - Aka title checkboxes */
    
	if (source.equals(storeAllAvailableAkaTitles)) {
	    if (storeAllAvailableAkaTitles.isSelected()) {
		noDuplicateAkaTitles.setEnabled(true);
            }
	    else {
		noDuplicateAkaTitles.setEnabled(false);
	    }
	}
    
	if (source.equals(useLanguageSpecificTitle)) {
    
	    if (useLanguageSpecificTitle.isSelected())
		languageCodeSelector.setEnabled(true);
	    else
	        languageCodeSelector.setEnabled(false);
	}
	
	
	if (source.equals(browserOptionOpera)) {
        setBrowserComponentsEnabled();
	}
	
	if (source.equals(browserOptionMozilla)) {
        setBrowserComponentsEnabled();
	}
	
	if (source.equals(browserOptionFirefox)) {
        setBrowserComponentsEnabled();
	}
	
	if (source.equals(browserOptionNetscape)) {
        setBrowserComponentsEnabled();
	}
	
	if (source.equals(browserOptionSafari)) {
        setBrowserComponentsEnabled();
	}
	
	if (source.equals(enableCustomBrowser)) {
        setBrowserComponentsEnabled();
	}   
	if (source.equals(enableUseDefaultWindowsBrowser)) {
        setBrowserComponentsEnabled();
	} 
    }
}

