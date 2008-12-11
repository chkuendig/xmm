package net.sf.xmm.moviemanager.swing.extentions.filetree;

//Add the event registration and notification code to a class.
public class AddSelectedFilesEventHandler {
   
	// Create the listener list
    protected javax.swing.event.EventListenerList listenerList =
        new javax.swing.event.EventListenerList();

    // This methods allows classes to register for AddSelectedFilesEvents
    public void addAddSelectedFilesEventListener(AddSelectedFilesEventListener listener) {
        listenerList.add(AddSelectedFilesEventListener.class, listener);
    }

    // This methods allows classes to unregister for AddSelectedFilesEvents
    public void removeAddSelectedFilesEventListener(AddSelectedFilesEventListener listener) {
        listenerList.remove(AddSelectedFilesEventListener.class, listener);
    }
  
    // This private class is used to fire AddSelectedFilesEvents
    void fireAddSelectedFilesEvent(AddSelectedFilesEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == AddSelectedFilesEventListener.class) {
                ((AddSelectedFilesEventListener)listeners[i+1]).addSelectedFilesEventOccurred(evt);
            }
        }
    }
}