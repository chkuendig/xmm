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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.xmm.moviemanager.LookAndFeelManager;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.MovieManagerConfig.InternalConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import static net.sf.xmm.moviemanager.MovieManagerConfig.LookAndFeelType;

import org.apache.log4j.Logger;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;



public class DialogPrefs extends JDialog implements ActionListener, ItemListener {

	Logger log = Logger.getLogger(getClass());
	
	private static MovieManagerConfig config = MovieManager.getConfig();

	InternalConfig disabledFeatures = MovieManager.getConfig().getInternalConfig();
	
	private Container contentPane;
	private JTabbedPane all;

	private JComboBox lafChooser;
	private JCheckBox enableLafChooser;
	private JComboBox skinlfThemePackChooser;
	private JCheckBox enableSkinlf;
	
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


	// IMDb login
	private JCheckBox enableIMDbAuthenticationButton;

	private JTextField IMDbUserNameTextField;
	private JTextField IMDbPasswordTextField;

	private JLabel IMDbUserNameLabel;
	private JLabel IMDbPasswordLabel;

	private JCheckBox loadDatabaseOnStartUp;
	private JCheckBox enableAutoMoveThe;
	private JCheckBox enableAutoMoveAnAndA;


	private JCheckBox storeAllAvailableAkaTitles;
	private JCheckBox includeAkaLanguageCodes;
	private JCheckBox useLanguageSpecificTitle;

	private JComboBox languageCodeSelector;

	private JCheckBox displayQueriesInTree;

	private JCheckBox checkForProgramUpdates;
	private JCheckBox enablePlayButton;	
	private JCheckBox checkEnableHTMLViewDebugMode;
	private JCheckBox checkEnableMySQLSocketTimeout;
		
	private JCheckBox enableSeenEditable;
	private JCheckBox enableRightclickByCtrl;
	private JCheckBox enableLoadLastUsedList;
	private JCheckBox enableAddNewMoviesToCurrentLists;
	
	private JCheckBox enableUseJTreeIcons;
	private JCheckBox enableUseJTreeCovers;

	private JCheckBox enablePreserveCoverRatioEpisodesOnly;
	private JCheckBox enablePreserveCoverRatio;

	private JRadioButton pumaCover;
	private JRadioButton jaguarCover;

	private JCheckBox enableStoreCoversLocally;

	private JTextField makeBackupEveryLaunchField;
	private JTextField deleteOldestWhenSizeExcedesMBField;
	private JTextField backupDirField;



	private JButton browserBrowse;
	private JTextField mediaPlayerPathField;
	private JTextField mediaPlayerCmdArgument;
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

	private JButton externCommandInfo;
	private JCheckBox externalCmd;
	
	private JSlider rowHeightSlider;
	private JTree exampleTree;
	private MovieManagerConfig exampleConfig = new MovieManagerConfig(true);


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
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
		getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

		setTitle(Localizer.getString("dialogprefs.title")); //$NON-NLS-1$
		setModal(true);
		setResizable(false);

		/* LookAndFeel panel */
		JPanel layoutPanel = new JPanel(new BorderLayout());

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

		
		JPanel buttonOptions = new JPanel(new GridLayout(3, 3));
		buttonOptions.setBorder(BorderFactory.createEmptyBorder(0,30,50,20));
		
		buttonOptions.add(seenIconLabel);
		buttonOptions.add(regularSeenIcon);
		buttonOptions.add(currentLookAndFeelIcon);
		

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

		buttonOptions.add(toolBarButtonLabel);
		buttonOptions.add(regularToolBarButtons);
		buttonOptions.add(currentLookAndFeelButtons);
				

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
		
		buttonOptions.add(defaultLafDecoratedLabel);
		buttonOptions.add(regularDecoratedButton);
		buttonOptions.add(defaultLafDecoratedButton);
		
		layoutPanel.add(buttonOptions, BorderLayout.NORTH);

		/* Laf choosers */

		installedLookAndFeels = UIManager.getInstalledLookAndFeels();

		JPanel lafChooserPanel = new JPanel(new GridLayout(0, 1));
		
		/* Group the radio buttons. */
		ButtonGroup lafGroup = new ButtonGroup();

		int numberOfLookAndFeels = config.getNumberOfLookAndFeels();

		String [] lookAndFeelStrings = new String[numberOfLookAndFeels-1];

		int indexCount = 0;
		for (int i = 0; i < numberOfLookAndFeels; i++) {

			// Skip the ugly motif L&F
			if (installedLookAndFeels[i].getName().equals("CDE/Motif")) {
				continue;
			}

			lookAndFeelStrings[indexCount] = installedLookAndFeels[i].getName();
			indexCount++;
		}
		lafChooser = new JComboBox(lookAndFeelStrings);
		enableLafChooser = new JCheckBox(Localizer.getString("dialogprefs.panel.look-and-feel.enable-custom-laf.text")); //$NON-NLS-1$
		enableLafChooser.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
		lafGroup.add(enableLafChooser);

		String currentLookAndFeel = config.getCustomLookAndFeel();

		lafChooser.setSelectedItem(currentLookAndFeel);
		lafChooser.setEnabled(false);
		lafChooser.addActionListener(this);
		enableLafChooser.addActionListener(this);

		lafChooser.setPreferredSize(new Dimension(200, (int) lafChooser.getPreferredSize().getHeight()));
		
		JPanel customLafChooserPanel = new JPanel(new BorderLayout());
		customLafChooserPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

		customLafChooserPanel.add(enableLafChooser, BorderLayout.WEST);
		customLafChooserPanel.add(lafChooser, BorderLayout.EAST);

		customLafChooserPanel.setMaximumSize(new Dimension(250,30));
		customLafChooserPanel.setPreferredSize(new Dimension(250,30));

		lafChooserPanel.add(customLafChooserPanel, BorderLayout.SOUTH);

		
		
		/* Skinlf */

		String [] skinlfThemePackList = LookAndFeelManager.getSkinlfThemepackList();

		if (skinlfThemePackList != null) {
			enableSkinlf = new JCheckBox(Localizer.getString("dialogprefs.panel.look-and-feel.enable-skinlf.text")); //$NON-NLS-1$
			enableSkinlf.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
			lafGroup.add(enableSkinlf);

			// Prettify the names by removing zip
			for (int i = 0; i < skinlfThemePackList.length; i++) {
				skinlfThemePackList[i] = skinlfThemePackList[i].replace(".zip", "");
			}
			
			Arrays.sort(skinlfThemePackList);
			
			skinlfThemePackChooser = new JComboBox(skinlfThemePackList);
			skinlfThemePackChooser.setEnabled(false);
						
			String currentSkinlfThemePack = config.getSkinlfThemePack().replace(".zip", "");
			skinlfThemePackChooser.setSelectedItem(currentSkinlfThemePack);
			
			if (skinlfThemePackChooser.getSelectedIndex() == -1)
				skinlfThemePackChooser.setSelectedIndex(0);
			
			
			JPanel skinlfPanel = new JPanel(new BorderLayout());
			skinlfPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

			skinlfPanel.add(enableSkinlf, BorderLayout.WEST);
			skinlfPanel.add(skinlfThemePackChooser, BorderLayout.EAST);

			lafChooserPanel.add(skinlfPanel);
			skinlfThemePackChooser.addActionListener(this);
			enableSkinlf.addActionListener(this);
		}


		setLafChooserPreferredSize();

		if ((skinlfThemePackList != null) && (config.getLookAndFeelType() == LookAndFeelType.SkinlfLaF)) {
			enableSkinlf.setSelected(true);
			skinlfThemePackChooser.setEnabled(true);
		}
		else {
			enableLafChooser.setSelected(true);
			lafChooser.setEnabled(true);

			if (skinlfThemePackList == null)
				enableLafChooser.setEnabled(false);
		}


		layoutPanel.add(lafChooserPanel, BorderLayout.SOUTH);
		
		
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

		JPanel proxyAuthenticationPanel = new JPanel(new GridBagLayout());
		proxyAuthenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.proxy.authentication")), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		proxyAuthenticationPanel.add(enableAuthenticationPanel, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_START;
		proxyAuthenticationPanel.add(userNamePanel, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_END;
		proxyAuthenticationPanel.add(passwordPanel,constraints);


		JPanel proxyPanel = new JPanel(new GridLayout(0, 1));
		proxyPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.proxy.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		proxyPanel.add(proxyServerPanel);
		proxyPanel.add(proxyAuthenticationPanel);

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

		if (config.getIMDbAuthenticationEnabled())
			enableAuthenticationButton.setSelected(true);





		/* IMDb login panel */


		enableIMDbAuthenticationButton = new JCheckBox("Enable authentication"); //$NON-NLS-1$
		enableIMDbAuthenticationButton.setActionCommand("Enable Authentication"); //$NON-NLS-1$
		enableIMDbAuthenticationButton.addItemListener(this);

		JPanel enableIMDbAuthenticationPanel = new JPanel();
		enableIMDbAuthenticationPanel.add(enableIMDbAuthenticationButton);

		IMDbUserNameLabel = new JLabel("Username" + ": "); //$NON-NLS-1$
		IMDbUserNameLabel.setEnabled(false);
		IMDbUserNameTextField = new JTextField(7);
		IMDbUserNameTextField.setEnabled(false);

		JPanel IMDbUserNamePanel = new JPanel();
		IMDbUserNamePanel.add(IMDbUserNameLabel);
		IMDbUserNamePanel.add(IMDbUserNameTextField);

		IMDbPasswordLabel = new JLabel("Password" + ": "); //$NON-NLS-1$
		IMDbPasswordLabel.setEnabled(false);
		IMDbPasswordTextField = new JTextField(7);
		IMDbPasswordTextField.setEnabled(false);

		JPanel IMDbPasswordPanel = new JPanel() ;
		IMDbPasswordPanel.add(IMDbPasswordLabel);
		IMDbPasswordPanel.add(IMDbPasswordTextField);

		JPanel IMDbAuthenticationPanel = new JPanel(new GridBagLayout());

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		IMDbAuthenticationPanel.add(enableIMDbAuthenticationButton, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_START;
		IMDbAuthenticationPanel.add(IMDbUserNamePanel, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_END;
		IMDbAuthenticationPanel.add(IMDbPasswordPanel,constraints);


		IMDbAuthenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"IMDb Authentication"), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

		if (((temp = config.getIMDbAuthenticationUser()) != null) && !temp.equals("null")) //$NON-NLS-1$
			IMDbUserNameTextField.setText(temp);

		if (((temp = config.getIMDbAuthenticationPassword()) != null) && !temp.equals("null")) //$NON-NLS-1$
			IMDbPasswordTextField.setText(temp);

		if (config.getIMDbAuthenticationEnabled())
			enableIMDbAuthenticationButton.setSelected(true);



//		title options

		JPanel titlePanel = new JPanel();

		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Imported Movie Titles"), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

		JPanel autoMovieToEndOfTitlePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

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
		akaTitlePanel.setLayout(new GridLayout(4, 1));

		storeAllAvailableAkaTitles = new JCheckBox("Store all available aka titles");
		includeAkaLanguageCodes = new JCheckBox("Include comments and language codes");
		useLanguageSpecificTitle = new JCheckBox("Replace original title with aka title with the following language code:");

		ArrayList<String> langCodesList = new ArrayList<String>(150);
		int index = 0;

		try {

			InputStream inputStream = FileUtil.getResourceAsStream("/codecs/LanguageCodes.txt");

			BufferedInputStream stream = new BufferedInputStream(inputStream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

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

			if (config.getStoreAllAkaTitles())
				storeAllAvailableAkaTitles.setSelected(true);

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




		JPanel IMDbPanel = new JPanel(new GridLayout(0, 1));
		IMDbPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"IMDb settings"), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		IMDbPanel.add(IMDbAuthenticationPanel);
		IMDbPanel.add(titlePanel);



		/* Miscellaneous panel */
		JPanel miscPanel = new JPanel();
		miscPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.getString("dialogprefs.panel.miscellaneous.title")), BorderFactory.createEmptyBorder(12,1,16,1))); //$NON-NLS-1$
		//miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.PAGE_AXIS));
		miscPanel.setLayout(new BorderLayout());

		//miscPanel.setBackground(Color.black);

		JPanel miscCheckBoxes = new JPanel(new GridLayout(5, 1));
		miscCheckBoxes.setBorder(BorderFactory.createEmptyBorder(10,15,35,5));
		miscCheckBoxes.setLayout(new BoxLayout(miscCheckBoxes, BoxLayout.PAGE_AXIS));
		//miscCheckBoxes.setLayout(new BorderLayout());


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

		checkForProgramUpdates = new JCheckBox("Check for version updates at startup"); //$NON-NLS-1$

		if (config.getCheckForProgramUpdates())
			checkForProgramUpdates.setSelected(true);

		miscCheckBoxes.add(checkForProgramUpdates);

		
		checkEnableHTMLViewDebugMode = new JCheckBox("Enable debug mode for HTML View"); //$NON-NLS-1$

		if (config.getHTMLViewDebugMode())
			checkEnableHTMLViewDebugMode.setSelected(true);

		miscCheckBoxes.add(checkEnableHTMLViewDebugMode);
				
		
		enablePlayButton = new JCheckBox("Enable play button in toolbar"); //$NON-NLS-1$
		enablePlayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MovieManager.getDialog().getToolBar().showPlayButton(enablePlayButton.isSelected());
			}
		});


		if (config.getDisplayPlayButton())
			enablePlayButton.setSelected(true);

		miscCheckBoxes.add(enablePlayButton);
		
		// Only if MySQL database
		if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isMySQL()) {
			checkEnableMySQLSocketTimeout = new JCheckBox("<html>Enable MySQL Socket timeout after 15 minutes <br>(requires reconnect to database)</html>"); //$NON-NLS-1$
			
			if (config.getMySQLSocketTimeoutEnabled())
				checkEnableMySQLSocketTimeout.setSelected(true);

			miscCheckBoxes.add(checkEnableMySQLSocketTimeout);
		}
		
		miscPanel.add(miscCheckBoxes, BorderLayout.WEST);

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

		if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isMySQL()) {

			enableStoreCoversLocally = new JCheckBox(Localizer.getString("dialogprefs.panel.cover-settings.store-covers-locally")); //$NON-NLS-1$
			enableStoreCoversLocally.setActionCommand("Store covers locally"); //$NON-NLS-1$

			if (config.getStoreCoversLocally())
				enableStoreCoversLocally.setSelected(true);

			// Disabled in applet mode
			if (MovieManager.isApplet())
				enableStoreCoversLocally.setEnabled(false);

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

		
		if (!MovieManager.getConfig().getInternalConfig().getDisableLoadLastUsedList())
			movieListPanel.add(enableLoadLastUsedList);

		
		/* Enable load last used list */
		enableAddNewMoviesToCurrentLists = new JCheckBox("Add new movies to the currently selected lists"); 
		enableAddNewMoviesToCurrentLists.setActionCommand("Enable Add new movies to current lists"); //$NON-NLS-1$

		if (config.getAddNewMoviesToCurrentLists())
			enableAddNewMoviesToCurrentLists.setSelected(true);
		
		movieListPanel.add(enableAddNewMoviesToCurrentLists);
		
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


		// Rowheight including example
		JPanel rowHeightPanel = new JPanel();
		rowHeightPanel.setLayout(new BorderLayout());
		rowHeightPanel.setMinimumSize(new Dimension(0, 150));
		rowHeightPanel.setPreferredSize(new Dimension(0, 150));

		JTree movieList = MovieManager.getDialog().getMoviesList();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) movieList.getModel().getRoot();

		int limit = movieList.getModel().getChildCount(root);

		if (limit > 7)
			limit = 7;

		DefaultMutableTreeNode exampleRoot = new DefaultMutableTreeNode("root"); //$NON-NLS-1$

		// Seems like a treenode cannot be added to more than one treemodel at a time.....???
		for (int i = 0; i < limit; i++)
			exampleRoot.add(new DefaultMutableTreeNode(new ModelMovie((ModelMovie) ((DefaultMutableTreeNode) movieList.getModel().getChild(root, i)).getUserObject()))); //$NON-NLS-1$ //$NON-NLS-2$

		exampleTree = new JTree(exampleRoot) {
			
			protected void paintComponent(Graphics g) {

    			int[] rows = getSelectionRows();

    			if (rows != null && rows.length > 0) {
    				Rectangle b = getRowBounds(rows[0]);

    				g.setColor(UIManager.getColor("Tree.selectionBackground"));
    				g.fillRect(0, b.y, getWidth(), b.height);
    			}
    			super.paintComponent(g);
    		}
		};

		JScrollPane scroller = new JScrollPane(exampleTree);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroller.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		exampleTree.setRootVisible(false);
		exampleTree.setShowsRootHandles(true);

		exampleConfig.setUseRelativeCoversPath(config.getUseRelativeCoversPath());

		exampleTree.setCellRenderer(new ExtendedTreeCellRenderer(exampleTree, scroller, exampleConfig));
		
		//JScrollPane scrollPane = new JScrollPane();
        //scrollPane.setViewportView(moviesList);
        
        exampleTree.setOpaque(false);
        
        //Avoids NullPointer on Synthetica L&F.
        //scrollPane.getViewport().setBackground(UIManager.getColor("ScrollPane.background"));
		
		rowHeightSlider = new JSlider(6, 300, config.getMovieListRowHeight());
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
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Media Player "),
				BorderFactory.createEmptyBorder(0,5,5,5)));

		enableUseDefaultWindowsPlayer = new JCheckBox("Use the default Windows player");
		enableUseDefaultWindowsPlayer.setSelected(config.getUseDefaultWindowsPlayer());

		if (!SysUtil.isWindows())
			enableUseDefaultWindowsPlayer.setEnabled(false);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		playerPanel.add(enableUseDefaultWindowsPlayer, c);

		JLabel playerLabel = new JLabel("Player Location:");

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		playerPanel.add(playerLabel, c);


		mediaPlayerPathField = new JTextField(30);
		mediaPlayerPathField.setText(config.getMediaPlayerPath());

		JButton mediaPlayerBrowse = new JButton("Browse");

		mediaPlayerBrowse.setActionCommand("Browse Player Path");
		mediaPlayerBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// check if there is a file selected

				String path = mediaPlayerPathField.getText();
				File parent = new File(path).getParentFile();

				if (parent != null && parent.isDirectory())
					path = parent.getParent();
				else 
					path = "";

				JFileChooser chooser = new JFileChooser(path);
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
		mediaPlayerFilePanel.add(mediaPlayerBrowse);

		c.gridx = 0;
		c.gridy = 2;
		playerPanel.add(mediaPlayerFilePanel, c);


		JLabel cmdArgLabel = new JLabel("Command line arguments:");
		mediaPlayerCmdArgument = new JTextField(15);
		mediaPlayerCmdArgument.setText(config.getMediaPlayerCmdArgument());

		JPanel cmdArg = new JPanel();
		cmdArg.add(cmdArgLabel);
		cmdArg.add(mediaPlayerCmdArgument);

		c.gridx = 0;
		c.gridy = 3;
		playerPanel.add(cmdArg, c);


		externalCmd = new JCheckBox("Enable");
		externalCmd.setSelected(config.getExecuteExternalPlayCommand());
		
		JLabel externalCmdLabel = new JLabel("<html>A command located in a file in the directory<br> of the media files will be executed.</html>");

		externCommandInfo = new JButton("Read more");

		externCommandInfo.addActionListener(this);


		// External command
		JPanel externalCommand = new JPanel();
		
		externalCommand.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Execute external command "),
				BorderFactory.createEmptyBorder(0,0,0,0)));
		externalCommand.add(externalCmd);
		externalCommand.add(externalCmdLabel);
		externalCommand.add(externCommandInfo);

		c.gridx = 0;
		c.gridy = 4;
		playerPanel.add(externalCommand, c);

		/* Browser Path */
		JPanel browserPanel = new JPanel();
		browserPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();

		browserPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Web Browser "),
				BorderFactory.createEmptyBorder(5,3,5,3)));

		JPanel browserOptionPanel = new JPanel(new GridLayout(3, 3));

		enableUseDefaultWindowsBrowser = new JRadioButton("Windows default");
		enableCustomBrowser =            new JRadioButton("Custom browser");
		browserOptionOpera =             new JRadioButton("Opera");
		browserOptionFirefox =           new JRadioButton("Firefox");
		browserOptionMozilla =           new JRadioButton("Mozilla");
		browserOptionSafari =            new JRadioButton("Safari");
		browserOptionNetscape =          new JRadioButton("Netscape");
		browserOptionIE =                new JRadioButton("Internet Explorer");


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

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		browserPanel.add(browserOptionPanel, c);


		JLabel browserLabel = new JLabel("Custom Browser Location:");

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		browserPanel.add(browserLabel, c);

		customBrowserPathField = new JTextField(30);
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
				BorderFactory.createEmptyBorder(5,3,5,3)));

		
		if (!disabledFeatures.isPreferencesExternalProgramsPlayerDisabled())
			programPathsPanel.add(playerPanel);

		programPathsPanel.add(browserPanel);



		/* Backup panel */
		JPanel backupSettingsPanel = new JPanel();
		backupSettingsPanel.setLayout(new GridBagLayout());


		JLabel makeBackupEveryLaunchLabel = new JLabel("Make backup every (X)th time the program is launched. X:");
		makeBackupEveryLaunchField = new JTextField(4);
		makeBackupEveryLaunchField.setToolTipText("Use 0 to disable database backup");

		JLabel deleteOldestWhenSizeExcedesMBLabel = new JLabel("Delete oldest backup when total backup size excedes (size) MB:");
		deleteOldestWhenSizeExcedesMBField = new JTextField(4);
		deleteOldestWhenSizeExcedesMBField.setToolTipText("Use 0 to disable this option");

		JPanel everyTimePanel = new JPanel();
		everyTimePanel.add(makeBackupEveryLaunchLabel);
		everyTimePanel.add(makeBackupEveryLaunchField);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		backupSettingsPanel.add(everyTimePanel, c);


		JPanel deleteOldest = new JPanel();
		deleteOldest.add(deleteOldestWhenSizeExcedesMBLabel);
		deleteOldest.add(deleteOldestWhenSizeExcedesMBField);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		backupSettingsPanel.add(deleteOldest, c);

		JLabel directoryLabel = new JLabel("Backup directory:");

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		backupSettingsPanel.add(directoryLabel, c);

		backupDirField = new JTextField(32);

		JButton backupDirBrowse = new JButton("Browse");

		backupDirBrowse.setActionCommand("Browse Back Directory");
		backupDirBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg) {

				// check if there is a file selected
				String path = backupDirField.getText();
				File parent = new File(path).getParentFile();

				if (parent != null && parent.isDirectory())
					path = parent.getParent();
				else 
					path = "";

				JFileChooser chooser = new JFileChooser(path);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int returnVal = chooser.showOpenDialog(contentPane);

				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				try {
					String location = chooser.getSelectedFile().getCanonicalPath();

					if (location != null)
						backupDirField.setText(location);
				} catch (IOException e) {
					log.warn("Failed to retrieve player path");
				}
			}});

		JPanel backupDirPanel = new JPanel();
		backupDirPanel.setLayout(new BoxLayout(backupDirPanel, BoxLayout.X_AXIS));
		backupDirPanel.add(backupDirField);
		backupDirPanel.add(backupDirBrowse);

		c.gridx = 0;
		c.gridy = 1;
		backupSettingsPanel.add(backupDirPanel, c);

		makeBackupEveryLaunchField.setText(config.getDatabaseBackupEveryLaunch());  
		deleteOldestWhenSizeExcedesMBField.setText(config.getDatabaseBackupDeleteOldest());
		backupDirField.setText(config.getDatabaseBackupDirectory());



		/* Backup settings */

		JPanel backupPanel = new JPanel();
		backupPanel.setLayout(new BoxLayout(backupPanel, BoxLayout.Y_AXIS));

		backupPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Backup Settings "),
				BorderFactory.createEmptyBorder(12,5,16,5)));

		backupPanel.add(backupSettingsPanel);



		/* OK panel */
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));

		JButton buttonOk = new JButton(Localizer.getString("dialogprefs.panel.button-ok.text")); //$NON-NLS-1$
		buttonOk.setToolTipText(Localizer.getString("dialogprefs.panel.button-ok.tooltip")); //$NON-NLS-1$
		buttonOk.addActionListener(this);
		buttonOk.setActionCommand("OK"); //$NON-NLS-1$
		buttonPanel.add(buttonOk);

		JButton buttonCancel = new JButton("Cancel"); //$NON-NLS-1$
		buttonCancel.setToolTipText("Close without saving settings"); //$NON-NLS-1$
		buttonCancel.addActionListener(this);
		buttonCancel.setActionCommand("Cancel"); //$NON-NLS-1$
		buttonPanel.add(buttonCancel);


		all = new JTabbedPane();
		all.setBorder(BorderFactory.createEmptyBorder(8,8,5,8));
								
									
		if (!disabledFeatures.isPreferencesLookAndFeelDisabled())
			all.add(Localizer.getString("dialogprefs.tab"), layoutPanel); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesProxySettingsDisabled())
			all.add(Localizer.getString("dialogprefs.panel.proxy.title"), proxyPanel); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesMiscellaneousDisabled())
			all.add(Localizer.getString("dialogprefs.panel.miscellaneous.title"), miscPanel); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesCoverSettingsDisabled())
			all.add(Localizer.getString("dialogprefs.panel.cover-settings.title"), coverPanel); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesMovieListDisabled())
			all.add(Localizer.getString("dialogprefs.panel.movie-list.title"), movieListPanel); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesExternalProgramsDisabled())
			all.add("External Programs", programPathsPanel);

		if (!disabledFeatures.isPreferencesDatabaseBackupDisabled())
			all.add("Database Backup", backupPanel);

		if (!disabledFeatures.isPreferencesIMDbSettingsDisabled())
			all.add("IMDb settings", IMDbPanel);

		
		int selectTab = config.getLastPreferencesTabIndex();
		
		if (selectTab == -1 || selectTab >= all.getTabCount())
			selectTab = 0;
			
		if (all.getComponentCount() > 0)
			all.setSelectedIndex(selectTab);

	
		// Disable some panels in applet mode
		//all.setEnabledAt(all.indexOfComponent(proxyPanel), false);
		//all.setEnabledAt(all.indexOfComponent(IMDbPanel), false);
		//all.setEnabledAt(all.indexOfComponent(backupPanel), false);

		contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));

		contentPane.add(all);
		contentPane.add(buttonPanel);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		/*Display the window.*/
		pack();
		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

		updateEnabling();
	}


	void setBrowserComponentsEnabled() {

		if (SysUtil.isLinux()) 
			browserOptionSafari.setEnabled(false);

		if (!SysUtil.isWindows())
			browserOptionIE.setEnabled(false);

		if (enableCustomBrowser.isSelected()) {
			customBrowserPathField.setEnabled(true);
			browserBrowse.setEnabled(true);
		}
		else {
			customBrowserPathField.setEnabled(false);
			browserBrowse.setEnabled(false);
		}

		if (!SysUtil.isWindows())
			enableUseDefaultWindowsBrowser.setEnabled(false);
	}

	private void updateRowHeightExample() {
		int rowHeight = rowHeightSlider.getValue();
		exampleConfig.setMovieListRowHeight(rowHeight);
		exampleConfig.setUseJTreeIcons(enableUseJTreeIcons.isSelected());
		exampleConfig.setUseJTreeCovers(enableUseJTreeCovers.isSelected());
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

		config.setProxyEnabled(enableProxyButton.isSelected());
		config.setProxyAuthenticationEnabled(enableAuthenticationButton.isSelected());

		if (config.getIMDbAuthenticationEnabled() != enableIMDbAuthenticationButton.isSelected() ||
				!config.getIMDbAuthenticationUser().equals(IMDbUserNameTextField.getText()) ||
				!config.getIMDbAuthenticationPassword().equals(IMDbPasswordTextField.getText())) {
			MovieManager.getConfig().resetIMDbAuth();
		}

		config.setIMDbAuthenticationEnabled(enableIMDbAuthenticationButton.isSelected());
		config.setIMDbAuthenticationUser(IMDbUserNameTextField.getText());
		config.setIMDbAuthenticationPassword(IMDbPasswordTextField.getText());

		config.setMediaPlayerPath(mediaPlayerPathField.getText());
		config.setMediaPlayerCmdArgument(mediaPlayerCmdArgument.getText());
		config.setBrowserPath(customBrowserPathField.getText());

		config.setUseDefaultWindowsPlayer(enableUseDefaultWindowsPlayer.isSelected());

		config.setExecuteExternalPlayCommand(externalCmd.isSelected());
		
		
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

		String tmp;
		tmp = makeBackupEveryLaunchField.getText();

		if (tmp.equals(""))
			tmp = "0";

		config.setDatabaseBackupEveryLaunch(tmp);

		tmp = deleteOldestWhenSizeExcedesMBField.getText();

		if (tmp.equals(""))
			tmp = "0";

		config.setDatabaseBackupDeleteOldest(tmp);

		config.setDatabaseBackupDirectory(backupDirField.getText());

		/* automatic move of 'The' */
		config.setAutoMoveThe(enableAutoMoveThe.isSelected());

		/* automatic move of 'An and A' */
		config.setAutoMoveAnAndA(enableAutoMoveAnAndA.isSelected());

		config.setStoreAllAkaTitles(storeAllAvailableAkaTitles.isSelected());

		config.setIncludeAkaLanguageCodes(includeAkaLanguageCodes.isSelected());

		if (useLanguageSpecificTitle.isSelected()) {
			config.setUseLanguageSpecificTitle(true);

			String value = (String) languageCodeSelector.getSelectedItem();
			value = value.substring(1, 3);
			config.setTitleLanguageCode(value);
		}
		else
			config.setUseLanguageSpecificTitle(false);


		/* automatically load database at startup */
		config.setLoadDatabaseOnStartup(loadDatabaseOnStartUp.isSelected());

		/* seen editable */
		config.setSeenEditable(enableSeenEditable.isSelected());

		/* Display Queries In JTree */
		config.setUseDisplayQueriesInTree(displayQueriesInTree.isSelected());
	
		/* Check for updates */
		config.setCheckForProgramUpdates(checkForProgramUpdates.isSelected());

		config.setDisplayPlayButton(enablePlayButton.isSelected());

		config.setHTMLViewDebugMode(checkEnableHTMLViewDebugMode.isSelected());
		
		if (checkEnableMySQLSocketTimeout != null)
			config.setMySQLSocketTimeoutEnabled(checkEnableMySQLSocketTimeout.isSelected());
		
		/* rightclick by ctrl */
		config.setEnableCtrlMouseRightClick(enableRightclickByCtrl.isSelected());

		/* Load Last List At Startup */
		config.setLoadLastUsedListAtStartup(enableLoadLastUsedList.isSelected());

		/* Add new movies to current lists */
		config.setAddNewMoviesToCurrentLists(enableAddNewMoviesToCurrentLists.isSelected());
		
		/* Icons in JTree */
		config.setUseJTreeIcons(enableUseJTreeIcons.isSelected());

		/* Covers in JTree */
		config.setUseJTreeCovers(enableUseJTreeCovers.isSelected());

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

				// need to save enabled option to allow DialogFolders to determine whether to check paths or not
				config.setStoreCoversLocally(true); 

				DialogFolders dialogFolders = new DialogFolders();
				GUIUtil.showAndWait(dialogFolders, true);

				if (!(new File(config.getCoversFolder()).isDirectory())) {
					enableStoreCoversLocally.setSelected(false);
				}
			}

			config.setStoreCoversLocally(enableStoreCoversLocally.isSelected());
		}
		else
			config.setStoreCoversLocally(false);

		return true;
	}
	
	
	void setLookAndFeel(final LookAndFeelType type) {

		final DialogPrefs prefs = this;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prefs.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				switch (type) {
					case CustomLaF: {setCustomLookAndFeel(0); break;}
					case SkinlfLaF: {setSkinlfLookAndFeel(); break;}
				}
				MovieManager.getDialog().updateLookAndFeelValues();
				prefs.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
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
			log.error("Exception:" + e.getMessage(), e); //$NON-NLS-1$

			String lafName = (String) lafChooser.getSelectedItem();
			lafChooser.setSelectedItem(config.getCustomLookAndFeel());

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
		config.setCustomLookAndFeel(selectedItem);
		//MovieManager.getIt().addSplitPaneMouseListeners();
	}

	void setSkinlfLookAndFeel() {
		String selectedItem = (String) skinlfThemePackChooser.getSelectedItem() + ".zip";
		String skinlfThemePackPath = config.getSkinlfThemePackDir() + selectedItem;

		try {
			Skin skin = null;

			if (MovieManager.isApplet())
				skin = SkinLookAndFeel.loadThemePack(FileUtil.getAppletFile(skinlfThemePackPath, DialogMovieManager.applet).toURI().toURL());
			else {
				skin = SkinLookAndFeel.loadThemePack(skinlfThemePackPath);
			}
			SkinLookAndFeel.setSkin(skin);

			LookAndFeel laf = new SkinLookAndFeel();
			UIManager.setLookAndFeel(laf);

			updateLookAndFeel();

		} catch (Exception e) {
			log.error("Exception: "+ e.getMessage()); //$NON-NLS-1$
			showErrorMessage(e.getMessage(), "SkinLF"); //$NON-NLS-1$
			return;
		}
		config.setSkinlfThemePack(selectedItem);
	}

	

	void showErrorMessage(String error, String name) {

		String message = Localizer.getString("dialogprefs.alert.message.laf-improperly-installed-or-not-supported-by-jre")+ 
		System.getProperty("java.version") + //$NON-NLS-1$ //$NON-NLS-2$
		" You're advised to restart the program.";

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
		
		if (skinlfThemePackChooser != null)
			skinlfThemePackChooser.setPreferredSize(new Dimension(lafChooser.getPreferredSize()));
	}

	void updateLookAndFeel() throws Exception {
		
		final DialogPrefs pref = this;
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				MovieManager.getDialog().updateToolButtonBorder();
				MovieManager.getDialog().updateLookAndFeelValues();
				
				SwingUtilities.updateComponentTreeUI(MovieManager.getDialog());

				MovieManager.getDialog().resetInfoFieldsDisplay();
				MovieManager.getDialog().updateToolButtonBorder();
				MovieManager.getDialog().validate();
				
				/*If the search dialog is opened it will be updated*/
				if (DialogSearch.getDialogSearch() != null) {
					SwingUtilities.updateComponentTreeUI(DialogSearch.getDialogSearch());
					DialogSearch.getDialogSearch().pack();
				}

				SwingUtilities.updateComponentTreeUI(pref);
				pack();
				setLafChooserPreferredSize();
			}
		});
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

		/* Cancel - close */
		if (event.getActionCommand().equals("Cancel")) { //$NON-NLS-1$
			dispose();
		}

		if (event.getActionCommand().equals("Enable JTree Icons") || event.getActionCommand().equals("Enable JTree Covers")) { //$NON-NLS-1$ //$NON-NLS-2$
			updateEnabling();
			updateRowHeightExample();
		}

		if (event.getActionCommand().equals("Enable LookAndFeel")) { //$NON-NLS-1$

			if (enableSkinlf != null && enableSkinlf.isSelected()) {

				skinlfThemePackChooser.setEnabled(true);
				lafChooser.setEnabled(false);

				config.setLookAndFeelType(LookAndFeelType.SkinlfLaF);
				setLookAndFeel(LookAndFeelType.SkinlfLaF);
				
			}
			
			else {
				lafChooser.setEnabled(true);

				if (skinlfThemePackChooser != null)
					skinlfThemePackChooser.setEnabled(false);

				config.setLookAndFeelType(LookAndFeelType.CustomLaF);
				setLookAndFeel(LookAndFeelType.CustomLaF);
			}
		}

		if (event.getSource().equals(externCommandInfo)) { //$NON-NLS-1$
			DialogInfo info = new DialogInfo(this, "Execute external command", 
					"<html>When clicking the play button, the default media player is usually started. <br> " +
					"With this option enabled, the directory of the media file is first checked for a file <br>" +
					"with the same name as the media file, but ending in \"xmm.sh\" or \"xmm.bat\". If such a file exists, <br>" +
					"the file content will be executed as it would be done on the command line. If no such file <br>" +
			"is found, the media player will be executed instead.</html>");
			GUIUtil.showAndWait(info, true);
		}

		if (event.getActionCommand().equals("ToolBarButton")) { //$NON-NLS-1$

			if (regularToolBarButtons.isSelected())
				config.setRegularToolButtonsUsed(true);
			else
				config.setRegularToolButtonsUsed(false);

			MovieManager.getDialog().updateToolButtonBorder();
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
			setLookAndFeel(LookAndFeelType.CustomLaF);
			
		}
		if (event.getSource().equals(skinlfThemePackChooser)) {
			setLookAndFeel(LookAndFeelType.SkinlfLaF);
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

		// IMDb authentication
		if (source.equals(enableIMDbAuthenticationButton)) {
			IMDbUserNameLabel.setEnabled(enableIMDbAuthenticationButton.isSelected());
			IMDbPasswordLabel.setEnabled(enableIMDbAuthenticationButton.isSelected());
			IMDbUserNameTextField.setEnabled(enableIMDbAuthenticationButton.isSelected());
			IMDbPasswordTextField.setEnabled(enableIMDbAuthenticationButton.isSelected());
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

