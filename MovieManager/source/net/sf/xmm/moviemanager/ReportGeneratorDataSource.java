package net.sf.xmm.moviemanager;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.apache.log4j.*;
import net.sf.jasperreports.engine.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.*;

/**
 * DataSource for Report Generator
 *
 * @author olba2
 */
public class ReportGeneratorDataSource implements JRDataSource {
    private Enumeration e;
    private ModelEntry entry;
    private JProgressBar progressBar;
    private int count = 0;
    private URL defaultCoverImageURL;
    private boolean mySQL;
    private String coversFolder;
    private boolean testmode;

    /**
     * Constructor
     *
     * @param root DefaultMutableTreeNode - pass MovieList root
     * @param progressBar JProgressBar - progressbar to update during
     *   generation of report
     * @param defaultCoverImageURL URL - default image for movies without cover
     * @param testmode boolean - if true only dummydata is returned
     */
    public ReportGeneratorDataSource(DefaultMutableTreeNode root, JProgressBar progressBar, URL defaultCoverImageURL, boolean testmode) {
        this.progressBar = progressBar;
        this.defaultCoverImageURL = defaultCoverImageURL;
        this.testmode = testmode;
        this.mySQL = MovieManager.getIt().getDatabase() instanceof DatabaseMySQL;
        this.coversFolder = MovieManager.getIt().getConfig().getCoversFolder();

        e = root.depthFirstEnumeration();
        int n = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            n++;
        }
        progressBar.setMinimum(0);
        progressBar.setMaximum(n);
        progressBar.setValue(0);
        e = root.depthFirstEnumeration();
    }

    /**
     * next - called when report generator is moving to next entry
     *
     * @return boolean - true as long as there are more records
     * @throws JRException
     */
    public boolean next() throws JRException {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
        entry = (ModelEntry) node.getUserObject();

        if (entry instanceof ModelMovie) {
            entry = MovieManager.getIt().getDatabase().getMovie(entry.getKey(), true);
        }
        else {
            entry = MovieManager.getIt().getDatabase().getEpisode(entry.getKey(), true);
        }
        progressBar.setValue(count++);
        Thread.yield();
        return e.hasMoreElements();
    }

    /**
     * getFieldValue - return value for a field from the current when report
     * generator asks for it by name.
     *
     * @param jRField JRField - identifies a field in the report
     * @return Object - value for field. Type must match definition in report
     *   layout.
     * @throws JRException
     */
    public Object getFieldValue(JRField jRField) throws JRException {
        String name = jRField.getName();
        if(!testmode) {
            if (name.equalsIgnoreCase("Cover")) {
                if (entry.getCover() != null && entry.getCover().length() > 0) {
                    String filename = coversFolder + "/" + entry.getCover();
                    if (mySQL && entry.getCoverData() != null) {
                        try {
                            File tempFile = File.createTempFile("xmm", filename.substring(filename.indexOf('.')));
                            tempFile.deleteOnExit();
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(entry.getCoverData());
                            fos.close();
                            return tempFile.getPath();
                        }
                        catch (Exception ex) {
                            Logger.getRootLogger().error("Error saving temporary coverfile for " + filename, ex);
                        }
                    }
                    if (new File(filename).exists()) {
                        return filename;
                    }
                }
                return defaultCoverImageURL.toString();
            }
            else if (name.equalsIgnoreCase("Genre")) {
                return entry.getGenre();
            }
            else if (name.equalsIgnoreCase("Awards")) {
                return entry.getAwards();
            }
            else if (name.equalsIgnoreCase("Notes")) {
                return entry.getNotes();
            }
            else if (name.equalsIgnoreCase("Mpaa")) {
                return entry.getMpaa();
            }
            else if (name.equalsIgnoreCase("Cast")) {
                return entry.getCast();
            }
            else if (name.equalsIgnoreCase("Date")) {
                return entry.getDate();
            }
            else if (name.equalsIgnoreCase("IMDB")) {
                return entry.getUrlKey();
            }
            else if (name.equalsIgnoreCase("DirectedBy")) {
                return entry.getDirectedBy();
            }
            else if (name.equalsIgnoreCase("Plot")) {
                return entry.getPlot();
            }
            else if (name.equalsIgnoreCase("WrittenBy")) {
                return entry.getWrittenBy();
            }
            else if (name.equalsIgnoreCase("SoundMix")) {
                return entry.getCast();
            }
            else if (name.equalsIgnoreCase("Language")) {
                return entry.getLanguage();
            }
            else if (name.equalsIgnoreCase("Genre")) {
                return entry.getGenre();
            }
            else if (name.equalsIgnoreCase("Colour")) {
                return entry.getColour();
            }
            else if (name.equalsIgnoreCase("Seen")) {
                return Boolean.valueOf(entry.getSeen());
            }
            else if (name.equalsIgnoreCase("Country")) {
                return entry.getCountry();
            }
            else if (name.equalsIgnoreCase("Title")) {
                return entry.getTitle();
            }
            else if (name.equalsIgnoreCase("Aka")) {
                return entry.getAka();
            }
            else if (name.equalsIgnoreCase("WebRuntime")) {
                return entry.getWebRuntime();
            }
            else if (name.equalsIgnoreCase("Rating")) {
                return entry.getRating();
            }
            else if (name.equalsIgnoreCase("Certification")) {
                return entry.getCertification();
            }
            return "unknown field " + name;
        }
        else { // testmode
            if (name.equalsIgnoreCase("Cover")) {
                return defaultCoverImageURL.toString();
            }
            else if (name.equalsIgnoreCase("Title")) {
                return "Movie title " + count;
            }
            else if (name.equalsIgnoreCase("Rating")) {
                return "9.9";
            }
            else if (name.equalsIgnoreCase("Seen")) {
                return Boolean.TRUE;
            }
            return name;
        }
    }
}
