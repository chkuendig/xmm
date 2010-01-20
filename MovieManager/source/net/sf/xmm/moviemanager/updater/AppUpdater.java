package net.sf.xmm.moviemanager.updater;

import java.net.URL;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.http.HttpUtil;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.http.IMDbLib;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import com.panayotis.jupidator.ApplicationInfo;
import com.panayotis.jupidator.UpdatedApplication;
import com.panayotis.jupidator.Updater;
import com.panayotis.jupidator.UpdaterException;

public class AppUpdater implements UpdatedApplication {

	static Logger log = Logger.getLogger(AppUpdater.class);
	
    public AppUpdater() {
        try {
        	
        	System.err.println("numveric version:" + MovieManager.getConfig().sysSettings.getNumericalVersion());
        	System.err.println("getVersion:" + MovieManager.getConfig().sysSettings.getVersion());
        	
        	int numericVersion = MovieManager.getConfig().sysSettings.getNumericalVersion();
        	String version = MovieManager.getConfig().sysSettings.getVersion() + " - " + IMDbLib.getVersionString();
        	
        	System.err.println("VersionString:" + version);
        	
        	// IMDb lib is newer
        	if (numericVersion < IMDbLib.getNumericVersion()) {
        		numericVersion = IMDbLib.getNumericVersion();
        	}
        	
        	ApplicationInfo ap = new ApplicationInfo(
                    SysUtil.getUserDir(),
                    SysUtil.getUserDir(),
                    "" + numericVersion,
                    version);
        	
        	Updater updater = new Updater("http://xmm.sourceforge.net/updates/update.xml", ap, this);
        	updater.getGUI().setProperty("About", "false");
        	updater.actionDisplay();
        
        } catch (UpdaterException ex) {
            ex.printStackTrace();
        }
    }

    public boolean requestRestart() {
    	
    	DialogQuestion q = new DialogQuestion("Restart", "Restart necessary for changes to take effect. Restart now?");
    	q.setVisible(true);
    	
    	if (q.getAnswer())
    		return true;
    	else
    		return false;
    }

    public void receiveMessage(String message) {
        log.debug("Update message::" + message);
    }
    
    
    public static void handleVersionUpdate() {
    	
    	final MovieManagerConfig config = MovieManager.getConfig();
    	
    	if (!config.getCheckForProgramUpdates())
    		return;

    	Thread t = new Thread() {

    		public void run() { 
	
    			Thread.currentThread().setPriority(MIN_PRIORITY);
    			
    			try {
					//Thread.sleep(3000);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    							
				// Unable to write to install dir
				if (!SysUtil.canWriteToInstallDir()) {
					
					String alertTitle = "Update alert";
					String alertMessage = "<html>Application is not allowed to write to the install directory.<br> Updates will not be installed.</html>";
					
					if (SysUtil.isWindowsVista() || SysUtil.isWindows7()) {
						alertMessage = "<html>Application must be run as administrator to be able to update.</html>";
					}
					
					DialogAlert alert = new DialogAlert(MovieManager.getDialog(), alertTitle, alertMessage, true);
					GUIUtil.show(alert, true);
					return;
				}
				
    			new AppUpdater();
    			    			
    			if (true)
    				return;
    			
    			try {
    				HttpUtil httpUtil = new HttpUtil(config.getHttpSettings());
    				
    				String buf = httpUtil.readData(new URL("http://xmm.sourceforge.net/LatestVersion.txt")).getData().toString();

    				String [] lines = buf.split("\n|\r\n?");

    				if (lines == null || lines.length == 0)
    					return;

    				if (lines[1].length() > 0 && !lines[1].trim().equals(config.sysSettings.getVersion())) {

    					String currentVersion = config.sysSettings.getVersion().replaceAll("\\.", "").trim();
    					String newVersion = lines[1].replaceAll("\\.", "").trim();

    					// check if only digits. If not, aborted (won't notice about betas)
    					for (int i = 0; i < newVersion.length(); i++) {
    						if (!Character.isDigit(newVersion.charAt(i)))
    							log.debug("Aborting version check. New version contains non-digits:" + newVersion);
    					}
    					
    					// Cut string at first non-digit character
    					for (int i = 0; i < currentVersion.length(); i++) {
    						if (!Character.isDigit(currentVersion.charAt(i))) {
    							currentVersion = currentVersion.substring(0, i);
    							break;
    						}
    					}
    					    					
    					int currentLength = currentVersion.length();
    					int newLength = newVersion.length();
		
    					if (currentLength > newLength) {
    						while (newVersion.length() < currentLength)
    							newVersion += "0";
    					}
    					else if (currentLength < newLength) {
    						while (currentVersion.length() < newLength)
    							currentVersion += "0";
    					}
    				
    					// Checks if the version on the home page is newer than the current version
    					if (Double.parseDouble(newVersion) > Double.parseDouble(currentVersion)) {
    						log.debug("New version available:" + lines[1]);
    						MovieManager.getDialog().newVersionAvailable(lines[1], buf);
    					}
    				}
    			} catch (Exception e) {
    				log.warn("CheckForProgramUpdates aborted:" + e.getMessage());
    			}
    			
    			log.debug("handleVersionUpdate finished.");
    		}
    	};
    	t.start();
    }

}