package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.util.EventObject;

//Declare the event. It must extend EventObject.
public class AddSelectedFilesEvent extends EventObject {
    public AddSelectedFilesEvent(Object source) {
        super(source);
    }
}
