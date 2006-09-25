package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionListener;
import net.sf.xmm.moviemanager.ReportGenerator;
import net.sf.xmm.moviemanager.MovieManager;
import java.awt.event.ActionEvent;

public class MovieManagerCommandReportGenerator implements ActionListener {

    /**
     * Executes the command.
     **/
    protected static void execute() {
        ReportGenerator dialogPrint = new ReportGenerator(MovieManager.getIt());
    }

    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
        MovieManager.log.debug("ActionPerformed: " + event.getActionCommand());
        execute();
    }
}
