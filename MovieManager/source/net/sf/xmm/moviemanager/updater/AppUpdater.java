package net.sf.xmm.moviemanager.updater;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.http.IMDB;
import net.sf.xmm.moviemanager.http.IMDbLib;
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
        	
        	// IMDb is newer
        	if (numericVersion < IMDbLib.getNumericVersion()) {
        		numericVersion = IMDbLib.getNumericVersion();
        	}
        	
        	ApplicationInfo ap = new ApplicationInfo(
                    SysUtil.getUserDir(),
                    SysUtil.getUserDir(),
                    "" + numericVersion,
                    version);
            new Updater("http://xmm.sf.net/updates/update.xml", ap, this).actionDisplay();
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
}