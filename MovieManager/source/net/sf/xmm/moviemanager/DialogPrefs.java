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
import net.sf.xmm.moviemanager.extentions.*;
import net.sf.xmm.moviemanager.models.*;
import net.sf.xmm.moviemanager.util.*;

public class DialogPrefs extends JDialog implements ActionListener, ItemListener {

    static Logger log = Logger.getRootLogger();

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

    private JSlider rowHeightSlider;
    private JTree exampleTree;
    private MovieManagerConfig exampleConfig = new MovieManagerConfig();

    public DialogPrefs() {
	/* Dialog creation...*/
	super(MovieManager.getIt());

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

	if (MovieManager.getConfig().getUseRegularSeenIcon())
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

	if (MovieManager.getConfig().isRegularToolButtonsUsed())
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

	if (MovieManager.getConfig().getDefaultLookAndFeelDecorated())
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

	int numberOfLookAndFeels = MovieManager.getConfig().getNumberOfLookAndFeels();
	String [] lookAndFeelStrings = new String [numberOfLookAndFeels];

	for (int i = 0; i < numberOfLookAndFeels; i++) {
	    lookAndFeelStrings[i] = installedLookAndFeels[i].getName();
	}
	lafChooser = new JComboBox(lookAndFeelStrings);
	enableLafChooser = new JCheckBox(Localizer.getString("dialogprefs.panel.look-and-feel.enable-custom-laf.text")); //$NON-NLS-1$
	enableLafChooser.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
	lafGroup.add(enableLafChooser);

	String currentLookAndFeel = MovieManager.getConfig().getLookAndFeelString();

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
	    skinlfThemePackChooser.setSelectedItem(MovieManager.getConfig().getSkinlfThemePack());
	    skinlfThemePackChooser.setEnabled(false);

	    String currentSkinlfThemePack = MovieManager.getConfig().getSkinlfThemePack();

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
	    oyoahaThemePackChooser.setSelectedItem(MovieManager.getConfig().getOyoahaThemePack());
	    oyoahaThemePackChooser.setEnabled(false);

	    String currentOyoahaThemePack = MovieManager.getConfig().getOyoahaThemePack();

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

	if ((skinlfThemePackList != null) && (MovieManager.getConfig().getLookAndFeelType() == 1)) {
	    enableSkinlf.setSelected(true);
	    skinlfThemePackChooser.setEnabled(true);
	}

	else if ((oyoahaThemePackList != null) && (MovieManager.getConfig().getLookAndFeelType() == 2)) {
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
	proxyType.setSelectedItem(MovieManager.getConfig().getProxyType());
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

	if (((temp = MovieManager.getConfig().getProxyHost()) != null) && !temp.equals("null")) //$NON-NLS-1$
	    hostTextField.setText(temp);

	if (((temp = MovieManager.getConfig().getProxyPort()) != null) && !temp.equals("null")) //$NON-NLS-1$
	    portTextField.setText(temp);

	if (((temp = MovieManager.getConfig().getProxyUser()) != null)&& !temp.equals("null")) //$NON-NLS-1$
	    userNameTextField.setText(temp);

	if (((temp = MovieManager.getConfig().getProxyPassword()) != null) && !temp.equals("null")) //$NON-NLS-1$
	    passwordTextField.setText(temp);

	if (MovieManager.getConfig().getProxyEnabled())
	    enableProxyButton.setSelected(true);

	if (MovieManager.getConfig().getAuthenticationEnabled())
	    enableAuthenticationButton.setSelected(true);


	/* Miscellaneous panel */
	JPanel miscPanel = new JPanel();
	miscPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.miscellaneous.title")), BorderFactory.createEmptyBorder(12,1,16,1))); //$NON-NLS-1$
	miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.PAGE_AXIS));


	JPanel miscCheckBoxes = new JPanel(new GridLayout(3, 1));
	miscCheckBoxes.setBorder(BorderFactory.createEmptyBorder(10,15,35,5));


	loadDatabaseOnStartUp = new JCheckBox(Localizer.getString("dialogprefs.panel.miscellaneous.auto-load-database")); //$NON-NLS-1$
	loadDatabaseOnStartUp.setActionCommand("Load databse"); //$NON-NLS-1$

	if (MovieManager.getConfig().getLoadDatabaseOnStartup())
	    loadDatabaseOnStartUp.setSelected(true);

	miscCheckBoxes.add(loadDatabaseOnStartUp);

	/* Enable seen editable */
	enableSeenEditable = new JCheckBox(Localizer.getString("dialogprefs.panel.miscellaneous.enable-seen-editable-main-window")); //$NON-NLS-1$

	enableSeenEditable.setActionCommand("Enable Seen"); //$NON-NLS-1$

	if (MovieManager.getConfig().getSeenEditable())
	    enableSeenEditable.setSelected(true);

	miscCheckBoxes.add(enableSeenEditable);


	displayQueriesInTree = new JCheckBox(Localizer.getString("dialogprefs.panel.miscellaneous.use-directory-structure-to-group-queries")); //$NON-NLS-1$

	if (MovieManager.getConfig().getDisplayQueriesInTree())
	    displayQueriesInTree.setSelected(true);

	miscCheckBoxes.add(displayQueriesInTree);

	miscPanel.add(miscCheckBoxes);


	JPanel autoMovieToEndOfTitlePanel = new JPanel(new GridBagLayout());
	autoMovieToEndOfTitlePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.miscellaneous.auto-move-to-end-of-title")), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

	/* Enable Automatic placement of 'The' at the end of title */
	enableAutoMoveThe = new JCheckBox("'The '"); //$NON-NLS-1$
	enableAutoMoveThe.setActionCommand("Enable auto move"); //$NON-NLS-1$

	if (MovieManager.getConfig().getAutoMoveThe())
	    enableAutoMoveThe.setSelected(true);

	autoMovieToEndOfTitlePanel.add(enableAutoMoveThe);

	/* Enable Automatic placement of 'The' at the end of title */
	enableAutoMoveAnAndA = new JCheckBox("'A ' and 'An '"); //$NON-NLS-1$
	enableAutoMoveAnAndA.setActionCommand("Enable auto move"); //$NON-NLS-1$

	if (MovieManager.getConfig().getAutoMoveAnAndA())
	    enableAutoMoveAnAndA.setSelected(true);

	autoMovieToEndOfTitlePanel.add(enableAutoMoveAnAndA);
	autoMovieToEndOfTitlePanel.setMaximumSize(new Dimension((int) autoMovieToEndOfTitlePanel.getMaximumSize().getWidth(), (int) autoMovieToEndOfTitlePanel.getPreferredSize().getHeight()));


	miscPanel.add(autoMovieToEndOfTitlePanel);

	/* Cover settings */
	JPanel coverPanel = new JPanel();
	coverPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.cover-settings.title")), BorderFactory.createEmptyBorder(12,30,16,0))); //$NON-NLS-1$
	coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.PAGE_AXIS));

	enablePreserveCoverRatio = new JCheckBox(Localizer.getString("dialogprefs.panel.cover-settings.preserve-aspect-ratio")); //$NON-NLS-1$
	enablePreserveCoverRatio.setActionCommand("Preserve Cover ratio"); //$NON-NLS-1$
	enablePreserveCoverRatio.addItemListener(this);

	if (MovieManager.getConfig().getPreserveCoverAspectRatio() == 1)
	    enablePreserveCoverRatio.setSelected(true);

	coverPanel.add(enablePreserveCoverRatio);

	enablePreserveCoverRatioEpisodesOnly = new JCheckBox(Localizer.getString("dialogprefs.panel.cover-settings.preserve-aspect-ratio-episodes-only")); //$NON-NLS-1$
	enablePreserveCoverRatioEpisodesOnly.setActionCommand("Preserve Cover ratio episodes"); //$NON-NLS-1$
	enablePreserveCoverRatioEpisodesOnly.addItemListener(this);

	if (MovieManager.getConfig().getPreserveCoverAspectRatio() == 2) {
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

	if (MovieManager.getConfig().getNoCover().equals("nocover_jaguar.png")) //$NON-NLS-1$
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

	    if (MovieManager.getConfig().getStoreCoversLocally())
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

	if (MovieManager.getConfig().getEnableCtrlMouseRightClick())
	    enableRightclickByCtrl.setSelected(true);

	movieListPanel.add(enableRightclickByCtrl);

	/* Enable load last used list */
	enableLoadLastUsedList = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.load-last-used-list")); //$NON-NLS-1$
	enableLoadLastUsedList.setActionCommand("Enable lastLoadedList"); //$NON-NLS-1$

	if (MovieManager.getConfig().getLoadLastUsedListAtStartup())
	    enableLoadLastUsedList.setSelected(true);

	movieListPanel.add(enableLoadLastUsedList);


	/* Enable Use JTree Icons */
	enableUseJTreeIcons = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.enable-icons-in-movie-list")); //$NON-NLS-1$
	enableUseJTreeIcons.setActionCommand("Enable JTree Icons"); //$NON-NLS-1$
        enableUseJTreeIcons.addActionListener(this);

	if (MovieManager.getConfig().getUseJTreeIcons())
	    enableUseJTreeIcons.setSelected(true);

	movieListPanel.add(enableUseJTreeIcons);

        /* Enable Use JTree Covers */
        enableUseJTreeCovers = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.enable-covers-in-movie-list")); //$NON-NLS-1$
        enableUseJTreeCovers.setActionCommand("Enable JTree Covers"); //$NON-NLS-1$
        enableUseJTreeCovers.addActionListener(this);

        if (MovieManager.getConfig().getUseJTreeCovers())
            enableUseJTreeCovers.setSelected(true);

        movieListPanel.add(enableUseJTreeCovers);

	/* Enable highlight entire row */
	enableHighlightEntireRow = new JCheckBox(Localizer.getString("dialogprefs.panel.movie-list.highlight-entire-rom-in-movie-list")); //$NON-NLS-1$
	enableHighlightEntireRow.setActionCommand("Enable highlight row"); //$NON-NLS-1$

	if (MovieManager.getConfig().getMovieListHighlightEntireRow())
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
        exampleTree.setCellRenderer(new ExtendedTreeCellRenderer(MovieManager.getIt(), scroller, exampleConfig));
        rowHeightSlider = new JSlider(6, 100, MovieManager.getConfig().getMovieListRowHeight());
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

	all.setSelectedIndex(MovieManager.getConfig().getLastPreferencesTabIndex());

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

    private void updateRowHeightExample() {
        int rowHeight = rowHeightSlider.getValue();
        exampleConfig.setMovieListRowHeight(rowHeight);
        exampleConfig.setUseJTreeIcons(enableUseJTreeIcons.isSelected());
        exampleConfig.setUseJTreeCovers(enableUseJTreeCovers.isSelected());
        exampleConfig.setMovieListHighlightEntireRow(enableHighlightEntireRow.isSelected());
        exampleConfig.setStoreCoversLocally(MovieManager.getConfig().getStoreCoversLocally());
        exampleTree.setRowHeight(rowHeight + 2);
        exampleTree.updateUI();
    }

    void saveSettings() {

	/* Saving the tab index */
	MovieManager.getConfig().setLastPreferencesTabIndex(all.getSelectedIndex());

	/* Save proxy settings */
	MovieManager.getConfig().setProxyType((String) proxyType.getSelectedItem());
	MovieManager.getConfig().setProxyHost(hostTextField.getText());
	MovieManager.getConfig().setProxyPort(portTextField.getText());
	MovieManager.getConfig().setProxyUser(userNameTextField.getText());
	MovieManager.getConfig().setProxyPassword(passwordTextField.getText());

	if (enableProxyButton.isSelected())
	    MovieManager.getConfig().setProxyEnabled(true);
	else
	    MovieManager.getConfig().setProxyEnabled(false);

	if (enableAuthenticationButton.isSelected())
	    MovieManager.getConfig().setAuthenticationEnabled(true);
	else
	    MovieManager.getConfig().setAuthenticationEnabled(false);


	/* automatic move of 'The' */
	if (enableAutoMoveThe.isSelected())
	    MovieManager.getConfig().setAutoMoveThe(true);
	else
	    MovieManager.getConfig().setAutoMoveThe(false);

	/* automatic move of 'An and A' */
	if (enableAutoMoveAnAndA.isSelected())
	    MovieManager.getConfig().setAutoMoveAnAndA(true);
	else
	    MovieManager.getConfig().setAutoMoveAnAndA(false);

	/* automatically load database at startup */
	if (loadDatabaseOnStartUp.isSelected())
	    MovieManager.getConfig().setLoadDatabaseOnStartup(true);
	else
	    MovieManager.getConfig().setLoadDatabaseOnStartup(false);

	/* seen editable */
	if (enableSeenEditable.isSelected())
	    MovieManager.getConfig().setSeenEditable(true);
	else
	    MovieManager.getConfig().setSeenEditable(false);

	/* Display Queries In JTree */
	if (displayQueriesInTree.isSelected()) {
	    MovieManager.getConfig().setUseDisplayQueriesInTree(true);
        }
        else {
            MovieManager.getConfig().setUseDisplayQueriesInTree(false);
	}

	/* rightclick by ctrl */
	if (enableRightclickByCtrl.isSelected())
	    MovieManager.getConfig().setEnableCtrlMouseRightClick(true);
	else
	    MovieManager.getConfig().setEnableCtrlMouseRightClick(false);

	/* Load Last List At Startup */
	if (enableLoadLastUsedList.isSelected())
	    MovieManager.getConfig().setLoadLastUsedListAtStartup(true);
	else
	    MovieManager.getConfig().setLoadLastUsedListAtStartup(false);

	/* Icons in JTree */
	if (enableUseJTreeIcons.isSelected())
	    MovieManager.getConfig().setUseJTreeIcons(true);
	else
	    MovieManager.getConfig().setUseJTreeIcons(false);

        /* Covers in JTree */
        if (enableUseJTreeCovers.isSelected())
          MovieManager.getConfig().setUseJTreeCovers(true);
        else
          MovieManager.getConfig().setUseJTreeCovers(false);

	/* Highlight entire row */
	if (enableHighlightEntireRow.isSelected()) {
	    MovieManager.getConfig().setMovieListHighlightEntireRow(true);
	}
	else {
	    MovieManager.getConfig().setMovieListHighlightEntireRow(false);
	}

        /* Rowheight */
        MovieManager.getConfig().setMovieListRowHeight(rowHeightSlider.getValue());

	if (enablePreserveCoverRatioEpisodesOnly.isSelected())
	    MovieManager.getConfig().setPreserveCoverAspectRatio(2);
	else if (enablePreserveCoverRatio.isSelected())
	    MovieManager.getConfig().setPreserveCoverAspectRatio(1);
	else
	    MovieManager.getConfig().setPreserveCoverAspectRatio(0);

	if (pumaCover.isSelected())
	    MovieManager.getConfig().setNoCover("nocover_puma.png"); //$NON-NLS-1$
	else
	    MovieManager.getConfig().setNoCover("nocover_jaguar.png"); //$NON-NLS-1$

	if (enableStoreCoversLocally != null && enableStoreCoversLocally.isSelected()) {

	    if (!(new File(MovieManager.getConfig().getCoversFolder()).isDirectory())) {

		DialogAlert alert = new DialogAlert(this, Localizer.getString("dialogprefs.alert.title.alert"), Localizer.getString("dialogprefs.alert.message.covers-dir-not-existing")); //$NON-NLS-1$ //$NON-NLS-2$
		//alert.setVisible(true);
		ShowGUI.showAndWait(alert, true);
		
		DialogFolders dialogFolders = new DialogFolders();
		//dialogFolders.setVisible(true);
		ShowGUI.showAndWait(dialogFolders, true);
		
		if (!(new File(MovieManager.getConfig().getCoversFolder()).isDirectory())) {
		    enableStoreCoversLocally.setSelected(false);
		}
	    }

	    if (enableStoreCoversLocally.isSelected())
		MovieManager.getConfig().setStoreCoversLocally(true);
	    else
		MovieManager.getConfig().setStoreCoversLocally(false);
	}
	else
	    MovieManager.getConfig().setStoreCoversLocally(false);

	MovieManager.getIt().updateJTreeIcons();

	/* Necessary to update the icons in the movielist */
	MovieManager.getIt().getMoviesList().updateUI();
	MovieManagerCommandSelect.execute();
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
	    lafChooser.setSelectedItem(MovieManager.getConfig().getLookAndFeelString());

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
	MovieManager.getConfig().setLookAndFeelString(selectedItem);
	//MovieManager.getIt().addSplitPaneMouseListeners();
    }

    void setSkinlfLookAndFeel() {
	String selectedItem = (String) skinlfThemePackChooser.getSelectedItem();
	String skinlfThemePackPath = MovieManager.getConfig().getSkinlfThemePackDir() + selectedItem;

	try {
	    Skin skin = null;

	    if (MovieManager.applet == null)
		skin = SkinLookAndFeel.loadThemePack(skinlfThemePackPath);
	    else {
		skin = SkinLookAndFeel.loadThemePack(MovieManager.getIt().getAppletFile(skinlfThemePackPath).toURL());
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
	MovieManager.getConfig().setSkinlfThemePack(selectedItem);
    }

    void setOyoahaLookAndFeel() {
	String selectedItem = (String) oyoahaThemePackChooser.getSelectedItem();
	String oyoahaThemePackPath = MovieManager.getConfig().getOyoahaThemePackDir() + selectedItem;

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
	MovieManager.getConfig().setOyoahaThemePack(selectedItem);
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
	//alert.setVisible(true);
	ShowGUI.showAndWait(alert, true);
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
	MovieManager.getIt().updateToolButtonBorder();

	SwingUtilities.updateComponentTreeUI(MovieManager.getIt());
	MovieManager.getIt().updateToolButtonBorder();

	MovieManager.getIt().getDateField().setOpaque(false);
	MovieManager.getIt().getDateField().setBorder(null);

	MovieManager.getIt().getTitleField().setOpaque(false);
	MovieManager.getIt().getTitleField().setBorder(null);

	MovieManager.getIt().getDirectedByField().setOpaque(false);
	MovieManager.getIt().getDirectedByField().setBorder(null);

	MovieManager.getIt().getWrittenByField().setOpaque(false);
	MovieManager.getIt().getWrittenByField().setBorder(null);

	MovieManager.getIt().getGenreField().setOpaque(false);
	MovieManager.getIt().getGenreField().setBorder(null);

	MovieManager.getIt().getRatingField().setOpaque(false);
	MovieManager.getIt().getRatingField().setBorder(null);

	MovieManager.getIt().getCountryTextField().setOpaque(false);
	MovieManager.getIt().getCountryTextField().setBorder(null);

	MovieManager.getIt().getLanguageTextField().setOpaque(false);
	MovieManager.getIt().getLanguageTextField().setBorder(null);

	/*If the search dialog is opened it will be updated*/
	if (DialogSearch.getDialogSearch() != null) {
	    SwingUtilities.updateComponentTreeUI(DialogSearch.getDialogSearch());
	    DialogSearch.getDialogSearch().pack();
	}

	SwingUtilities.updateComponentTreeUI(this);
	pack();
	setLafChooserPreferredSize();
	MovieManager.getIt().updateToolButtonBorder();

	MovieManager.getIt().validate();
    }


    void setDefaultLookAndFeelDecoration(boolean decorated) {

	if (decorated) {
	    MovieManager.getConfig().setDefaultLookAndFeelDecorated(true);

	    javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
	    javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);

	    MovieManager.getIt().dispose();
	    MovieManager.getIt().setUndecorated(true);
	    MovieManager.getIt().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
	    //MovieManager.getIt().setVisible(true);
	    ShowGUI.show(MovieManager.getIt(), true);
	    
	    /* Updating DialogSearch if open */
	    if (DialogSearch.getDialogSearch() != null) {
		DialogSearch.getDialogSearch().dispose();
		DialogSearch.getDialogSearch().setUndecorated(true);
		DialogSearch.getDialogSearch().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.PLAIN_DIALOG);
		DialogSearch.getDialogSearch().pack();
		
		ShowGUI.show(DialogSearch.getDialogSearch(), true);
	    }

	    /* This */
	    dispose();
	    setUndecorated(true);
	    getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
	    pack();
	    //setVisible(true);
	    ShowGUI.show(this, true);
	}
	else {
	    MovieManager.getConfig().setDefaultLookAndFeelDecorated(false);

	    javax.swing.JFrame.setDefaultLookAndFeelDecorated(false);
	    javax.swing.JDialog.setDefaultLookAndFeelDecorated(false);

	    MovieManager.getIt().dispose();
	    MovieManager.getIt().setUndecorated(false);
	    MovieManager.getIt().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
	    //MovieManager.getIt().setVisible(true);
	    ShowGUI.show(MovieManager.getIt(), true);

	      /* Updating DialogSearch if open */
	    if (DialogSearch.getDialogSearch() != null) {
		DialogSearch.getDialogSearch().dispose();
		DialogSearch.getDialogSearch().setUndecorated(false);
		DialogSearch.getDialogSearch().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
		DialogSearch.getDialogSearch().pack();
		ShowGUI.show(DialogSearch.getDialogSearch(), true);
	    }

	    /* This */
	    dispose();
	    setUndecorated(false);
	    getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
	    pack();
	    //setVisible(true);
	    ShowGUI.show(this, true);
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
	    saveSettings();

	    dispose();
	    return;
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

		MovieManager.getConfig().setLookAndFeelType(1);
		setSkinlfLookAndFeel();
	    }

	    else if(enableOyoaha != null && enableOyoaha.isSelected()) {

		oyoahaThemePackChooser.setEnabled(true);
		lafChooser.setEnabled(false);

		if (skinlfThemePackChooser != null)
		    skinlfThemePackChooser.setEnabled(false);

		MovieManager.getConfig().setLookAndFeelType(2);
		setOyoahaLookAndFeel();

		MovieManager.getIt().updateToolButtonBorder();
	    }

	    else {
		lafChooser.setEnabled(true);

		if (oyoahaThemePackChooser != null)
		    oyoahaThemePackChooser.setEnabled(false);
		if (skinlfThemePackChooser != null)
		    skinlfThemePackChooser.setEnabled(false);

		MovieManager.getConfig().setLookAndFeelType(0);
		setCustomLookAndFeel(0);


	    }
	}


	if (event.getActionCommand().equals("ToolBarButton")) { //$NON-NLS-1$

	    if (regularToolBarButtons.isSelected())
		MovieManager.getConfig().setRegularToolButtonsUsed(true);
	    else
		MovieManager.getConfig().setRegularToolButtonsUsed(false);

	    MovieManager.getIt().updateToolButtonBorder();
	}


	if (event.getActionCommand().equals("SeenIcon")) { //$NON-NLS-1$

	    if (regularSeenIcon.isSelected()) {
		MovieManager.getIt().getSeen().setIcon(new ImageIcon(MovieManager.getIt().getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
		MovieManager.getIt().getSeen().setSelectedIcon(new ImageIcon(MovieManager.getIt().getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
		MovieManager.getConfig().setUseRegularSeenIcon(true);

	    }
	    else {
		MovieManager.getIt().getSeen().setIcon(null);
		MovieManager.getIt().getSeen().setSelectedIcon(null);
		MovieManager.getConfig().setUseRegularSeenIcon(false);
	    }

	    MovieManager.getIt().getSeen().updateUI();
	}

	if (event.getActionCommand().equals("DefaultLafDecorated")) { //$NON-NLS-1$
	    setDefaultLookAndFeelDecoration(defaultLafDecoratedButton.isSelected());
	}


	/*Layout*/
	if (event.getSource().equals(lafChooser)) {
	    setCustomLookAndFeel(0);
	    MovieManager.getIt().updateLookAndFeelValues();
	}
	if (event.getSource().equals(skinlfThemePackChooser)) {
	    setSkinlfLookAndFeel();
	    MovieManager.getIt().updateLookAndFeelValues();
	}

	if (event.getSource().equals(oyoahaThemePackChooser)) {
	    setOyoahaLookAndFeel();
	    MovieManager.getIt().updateLookAndFeelValues();
	}

	MovieManager.getIt().getMoviesList().requestFocus(true);
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
    }
}

