package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.util.EventListener;

//A class must implement this interface to get MyEvents.
public interface AddSelectedFilesEventListener extends EventListener {
	public void addSelectedFilesEventOccurred(AddSelectedFilesEvent evt);
}
