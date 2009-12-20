package net.sf.xmm.moviemanager.updater;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.util.SysUtil;

import com.panayotis.jupidator.ApplicationInfo;
import com.panayotis.jupidator.UpdatedApplication;
import com.panayotis.jupidator.Updater;
import com.panayotis.jupidator.UpdaterException;

public class AppUpdater implements UpdatedApplication {

    public AppUpdater() {
        try {
        	
            ApplicationInfo ap = new ApplicationInfo(
                    SysUtil.getUserDir(),
                    SysUtil.getUserDir(),
                    MovieManager.getConfig().sysSettings.getNumericalVersion(),
                    MovieManager.getConfig().sysSettings.getVersion());
            new Updater("http://xmm.sf.net/updates/update.xml", ap, this).actionDisplay();
        } catch (UpdaterException ex) {
            ex.printStackTrace();
        }
    }

    public boolean requestRestart() {
    	DialogQuestion q = new DialogQuestion("Restart", "May I restart!");
    	q.setVisible(true);
    	
    	if (q.getAnswer())
    		return true;
    	else
    		return false;
    }

    public void receiveMessage(String message) {
        System.err.println(message);
    }
}