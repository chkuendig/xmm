package net.sf.xmm.moviemanager.swing.util;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.BalloonTip.AttachLocation;
import net.java.balloontip.BalloonTip.Orientation;
import net.java.balloontip.styles.ModernBalloonStyle;
import net.sf.xmm.moviemanager.util.SysUtil;

public class KeyboardShortcutManager {

	static Logger log = Logger.getLogger(KeyboardShortcutManager.class);
	
	LinkedHashMap<String, KeyMapping> map = new LinkedHashMap<String, KeyMapping>();
	
	JDialog jDialog = null;
	JFrame jFrame = null;
	JRootPane rootPane;
	
	String macCmdChar = "⌘";
	
	JComponent tooltipAreaComponent;
	
	ModernBalloonStyle toolTipLook = null;
	BalloonTip bTip = null;	
	
	public KeyboardShortcutManager(JDialog window) {
		jDialog = window;
		rootPane = window.getRootPane();
		registerShowKeysKey();
	}
	
	public KeyboardShortcutManager(JFrame window) {
		jFrame = window;
		rootPane = window.getRootPane();
		registerShowKeysKey();
	}
	
	public boolean isJDialog() {
		return jDialog != null;
	}
	
	public JDialog getJDialog() {
		return jDialog;
	}
	
	public void setKeysToolTipComponent(JComponent c) {
		tooltipAreaComponent = c;
	}
	
	public static int getToolbarShortcutMask() {
		
		int value = InputEvent.ALT_MASK;
		
		if (SysUtil.isMac())
			value = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			
		return value;
	}
	
	void registerShowKeysKey() {
		//System.err.println("getMenuShortcutKeyMask:" + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		//System.err.println("getToolbarShortcutMask:" + getToolbarShortcutMask());
		//System.err.println("CTRL_MASK:" + InputEvent.CTRL_MASK);
		//System.err.println("ALT_MASK:" + InputEvent.ALT_MASK);
		//System.err.println("META_MASK:" + InputEvent.META_MASK);
				
		registerKeyboardShortcut(
				KeyStroke.getKeyStroke(KeyEvent.VK_K, getToolbarShortcutMask()),
				"Show available shortcuts for this window", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				
				if (tooltipAreaComponent != null) {
					
					// No shortcuts available for this dialog
					if (getShortCutsString() == null)
						return;
										
					if (bTip == null) {
						toolTipLook = new ModernBalloonStyle(13, 13, new Color(153, 153, 255), new Color(255, 255, 255), new Color(102, 102, 255));
						bTip = new BalloonTip(tooltipAreaComponent, getShortCutsString(), toolTipLook, Orientation.LEFT_ABOVE, AttachLocation.NORTHWEST, 0, 0, true);
						bTip.setCloseButtonActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								bTip.setVisible(false);
							}
						});
						bTip.setVisible(false);
					}
					
					bTip.setVisible(!bTip.isVisible());
				}
			}
		});
	}
	
	public KeyMapping registerKeyboardShortcut(KeyStroke key, String shortcutString, Action action) {
				
		if (map.containsKey(key.toString())) {
			log.warn("already contains shortcut:" + key);
			return null;
		}
		
		KeyMapping keyMapping = new KeyMapping(key, shortcutString);
		map.put(key.toString(), keyMapping);
		
		registerKeyboardShortcut(key, action, rootPane);
		return keyMapping;
	}

	public static void registerKeyboardShortcut(KeyStroke key, final Action action, JRootPane rootPane) {
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, key.toString()); //$NON-NLS-1$
		rootPane.getActionMap().put(key.toString(), action); //$NON-NLS-1$
	}
	
	
	public class KeyMapping {
		
		KeyStroke key;
		String shortcutString;
		
		KeyMapping(KeyStroke key, String shortcutString) {
			this.key = key;
			this.shortcutString = shortcutString;
		}
				
		public String getDisplayName() {
			
			String name = "";
			
			if ((key.getModifiers() & InputEvent.CTRL_MASK) != 0) {
				name += "CTRL";
			}
			if ((key.getModifiers() & InputEvent.ALT_MASK) != 0) {
				
				if (name.length() > 0)
					name += "+";
				
				name += "ALT";
			}
			if ((key.getModifiers() & InputEvent.META_MASK) != 0) {
				
				if (name.length() > 0)
					name += "+";
				
				if (SysUtil.isMac()){
					name += macCmdChar;
				}
				else
					name += "META";
			}
			
			if (name.length() > 0)
				name += "+";
						
			if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
				name += "Escape";
			}
			else if (key.getKeyChar() < 128) {
				name += key.getKeyChar();
			}// Invalid character, use code instead
			else if (key.getKeyCode() < 128) {
				name += (char) key.getKeyCode();
			}		
			
			return name;
		}
	}

		
	String getShortCutsString() {
		
		// only K is registered
		if (map.size() == 1)
			return null;
			
		String text = "<html><table>";
				
		text += "<tr>";
		text += "<th colspan=3>" + "<font size=4>Keyboard shortcuts</font>" + "</th>";
		text += "</tr>";
				
		Collection<KeyMapping> c = map.values();
		
		for (KeyMapping k : c) {
			text += "<tr>";
			//text += "<td>" + k.actionName + "</td><td>-</td><td>" + k.shortcutString + "</td>";
			text += "<td>" + k.getDisplayName() + "</td><td>-</td><td>" + k.shortcutString + "</td>";
						
			text += "</tr>";
		}
		
		text += "</table></html>";
		
		return text;
	}
}
