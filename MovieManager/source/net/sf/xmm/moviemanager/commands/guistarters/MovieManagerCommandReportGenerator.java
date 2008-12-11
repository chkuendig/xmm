package net.sf.xmm.moviemanager.commands.guistarters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.ReportGenerator;

public class MovieManagerCommandReportGenerator implements ActionListener {

    /**
     * Executes the command.
     **/
    protected static void execute() {
        ReportGenerator dialogPrint = new ReportGenerator(MovieManager.getDialog());
    }

    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
        MovieManager.log.debug("ActionPerformed: " + event.getActionCommand());
        execute();
    }
}
