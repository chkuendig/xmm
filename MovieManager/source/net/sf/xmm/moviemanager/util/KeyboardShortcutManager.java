package net.sf.xmm.moviemanager.util;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;

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

public class KeyboardShortcutManager {

	static Logger log = Logger.getLogger(KeyboardShortcutManager.class);
	
	HashMap<Integer, KeyMapping> map = new HashMap<Integer, KeyMapping>();
	
	JDialog jDialog = null;
	JFrame jFrame = null;
	JRootPane rootPane;
	
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
	
	
	void registerShowKeysKey() {
		registerKeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK), "CTRL+k", 
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
							
					System.err.println("bTip.isVisible():" + bTip.isVisible());
					
					bTip.setVisible(!bTip.isVisible());
				}
			}
		});
	}
	
	public void registerKeyboardShortcut(KeyStroke key, String actionName, String shortcutString, Action action) {
		
		if (map.containsKey(key.getKeyCode())) {
			log.warn("already contains shortcut:" + key);
			return;
		}
		
		map.put(key.getKeyCode(), new KeyMapping(key, actionName, shortcutString));
		
		registerKeyboardShortcut(key, actionName, action, rootPane);
	}

	public static void registerKeyboardShortcut(KeyStroke key, String actionName, 
			final Action action, JRootPane rootPane) {
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, actionName); //$NON-NLS-1$
		rootPane.getActionMap().put(actionName, action); //$NON-NLS-1$
	}
	
	class KeyMapping {
		
		KeyStroke key;
		String actionName;
		String shortcutString;
		
		KeyMapping(KeyStroke key, String actionName, String shortcutString) {
			this.key = key;
			this.actionName = actionName;
			this.shortcutString = shortcutString;
		}
	}

		
	String getShortCutsString() {
		
		// only K is registered
		if (map.size() == 1)
			return null;
			
		String text = "<html><table>";
				
		text += "<tr>";
		text += "<th colspan=2>" + "<font size=4>Keyboard shortcuts</font>" + "</th>";
		text += "</tr>";
				
		Collection<KeyMapping> c = map.values();
		
		for (KeyMapping k : c) {
			text += "<tr>";
			text += "<td>" + k.actionName + "</td><td>-</td><td>" + k.shortcutString + "</td>";
			text += "</tr>";
		}
		
		text += "</table></html>";
		
		return text;
	}
}
