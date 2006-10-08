package net.sf.xmm.moviemanager;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

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
    private Iterator iterator;
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
     * @param includeEpisodes boolean - if true, episodes are included in the
     *   report, otherwise they are omitted.
     * @param sortField String - name of field to sort list by - "none" for no
     *   sorting.
     * @param progressBar JProgressBar - progressbar to update during
     *   generation of report
     * @param defaultCoverImageURL URL - default image for movies without cover
     * @param testmode boolean - if true only dummydata is returned
     */
    public ReportGeneratorDataSource(List movies, String sortField, JProgressBar progressBar, URL defaultCoverImageURL, boolean testmode) {
        this.progressBar = progressBar;
        this.defaultCoverImageURL = defaultCoverImageURL;
        this.testmode = testmode;
        this.mySQL = MovieManager.getIt().getDatabase() instanceof DatabaseMySQL;
        this.coversFolder = MovieManager.getIt().getConfig().getCoversFolder();

        if (sortField != null && sortField.length() > 0 && !sortField.equalsIgnoreCase("none")) {
            Collections.sort(movies, new MovieComparator(sortField));
        }

        if (progressBar != null) {
            progressBar.setMinimum(0);
            progressBar.setMaximum(movies.size());
            progressBar.setValue(0);
        }
        iterator = movies.iterator();
    }

    /**
     * next - called when report generator is moving to next entry
     *
     * @return boolean - true as long as there are more records
     * @throws JRException
     */
    public boolean next() throws JRException {
        if (iterator.hasNext()) {
            entry = (ModelEntry) iterator.next();
            int key = entry.getKey();
            if (key >= 0) {
                if (entry instanceof ModelMovie) {
                    entry = MovieManager.getIt().getDatabase().getMovie(key, true);
                }
                else {
                    entry = MovieManager.getIt().getDatabase().getEpisode(key, true);
                }
                if (progressBar != null) {
                    progressBar.setValue(count++);
                }
                Thread.yield();
            }
            return true;
        }
        return false;
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
        // General fields

        if (name.equalsIgnoreCase("Cover")) {
            if (!testmode && entry.getCover() != null && entry.getCover().length() > 0) {
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
            return testmode ? "" : entry.getPlot();
        }
        else if (name.equalsIgnoreCase("WrittenBy")) {
            return entry.getWrittenBy();
        }
        else if (name.equalsIgnoreCase("SoundMix")) {
            return entry.getWebSoundMix();
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
            return testmode ? "Movie title " + count : entry.getTitle();
        }
        else if (name.equalsIgnoreCase("Aka")) {
            return testmode ? "Aka " + count : entry.getAka();
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

        // Additional fields

        ModelAdditionalInfo a = null;
        if (entry.getHasAdditionalInfoData()) {
            a = entry.getAdditionalInfo();
        }
        else {
            if (entry instanceof ModelMovie) {
                a = MovieManager.getIt().getDatabase().getAdditionalInfo(entry.getKey(), false);
            }
            else {
                a = MovieManager.getIt().getDatabase().getAdditionalInfo(entry.getKey(), true);
            }
            entry.setAdditionalInfo(a);
        }
        if (a != null) {
            if (name.equalsIgnoreCase("Subtitles")) {
                return a.getSubtitles();
            }
            else if (name.equalsIgnoreCase("Duration")) {
                int tempInt = a.getDuration();

                if (tempInt != -1) {
                    int hours = tempInt / 3600;
                    int mints = tempInt / 60 - hours * 60;
                    int secds = tempInt - hours * 3600 - mints * 60;
                    return hours + ":" + mints + "." + secds;
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("Filesize")) {
                int tempInt = a.getFileSize();
                if (tempInt != -1) {
                    return tempInt + " MB";
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("CDs")) {
                int tempInt = a.getCDs();
                if (tempInt != -1) {
                    return "" + tempInt;
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("CDCases")) {
                double tempDouble = a.getCDCases();
                if (tempDouble > 0) {
                    return String.valueOf(tempDouble);
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("Resolution")) {
                return a.getResolution();
            }
            else if (name.equalsIgnoreCase("VideoCodec")) {
                return a.getVideoCodec();
            }
            else if (name.equalsIgnoreCase("VideoRate")) {
                String vRate = a.getVideoRate();
                if (!vRate.equals("")) {
                    return vRate + " fps";
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("VideoBitrate")) {
                String vRate = a.getVideoBitrate();
                if (!vRate.equals("")) {
                    return vRate + " kbps";
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("AudioCodec")) {
                return a.getAudioCodec();
            }
            else if (name.equalsIgnoreCase("AudioRate")) {
                String aRate = a.getAudioRate();
                if (!aRate.equals("")) {
                    return aRate + " Hz";
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("AudioBitrate")) {
                String aRate = a.getAudioBitrate();
                if (!aRate.equals("")) {
                    return aRate + " kbps";
                }
                else {
                    return "";
                }
            }
            else if (name.equalsIgnoreCase("AudioChannels")) {
                return a.getAudioChannels();
            }
            else if (name.equalsIgnoreCase("FileLocation")) {
                return a.getFileLocation();
            }
            else if (name.equalsIgnoreCase("FileCount")) {
                return new Integer(a.getFileCount());
            }
            else if (name.equalsIgnoreCase("Container")) {
                return a.getContainer();
            }
            else if (name.equalsIgnoreCase("MediaType")) {
                return a.getMediaType();
            }

            // Extra info
            else {
                ArrayList extra = a.getExtraInfoFieldNames();
                if (extra != null) {
                    for (int i = 0; i < extra.size(); i++) {
                        if (name.equalsIgnoreCase( (String) extra.get(i))) {
                            return a.getExtraInfoFieldValue(i);
                        }
                    }
                }
            }
        }

        return "unknown field " + name;
    }

    /**
     * Movie comparater, sorts movies by named field
     */
    private class MovieComparator implements Comparator {
        private String sortField;

        public MovieComparator(String sortField) {
            System.out.println("sorting by " + sortField);
            this.sortField = sortField;

        }

        public int compare(Object o1, Object o2) {
            ModelEntry m1 = (ModelEntry) o1;
            ModelEntry m2 = (ModelEntry) o2;
            int result = 0;

            if (sortField.equalsIgnoreCase("Genre")) {
                result = m1.getGenre().compareToIgnoreCase(m2.getGenre());
            }
            else if (sortField.equalsIgnoreCase("Awards")) {
                result = m1.getAwards().compareToIgnoreCase(m2.getAwards());
            }
            else if (sortField.equalsIgnoreCase("Notes")) {
                result = m1.getNotes().compareToIgnoreCase(m2.getNotes());
            }
            else if (sortField.equalsIgnoreCase("Mpaa")) {
                result = m1.getMpaa().compareToIgnoreCase(m2.getMpaa());
            }
            else if (sortField.equalsIgnoreCase("Cast")) {
                result = m1.getCast().compareToIgnoreCase(m2.getCast());
            }
            else if (sortField.equalsIgnoreCase("Date")) {
                result = m1.getDate().compareToIgnoreCase(m2.getDate());
            }
            else if (sortField.equalsIgnoreCase("IMDB")) {
                result = m1.getUrlKey().compareToIgnoreCase(m2.getUrlKey());
            }
            else if (sortField.equalsIgnoreCase("DirectedBy")) {
                result = m1.getDirectedBy().compareToIgnoreCase(m2.getDirectedBy());
            }
            else if (sortField.equalsIgnoreCase("Plot")) {
                result = m1.getPlot().compareToIgnoreCase(m2.getPlot());
            }
            else if (sortField.equalsIgnoreCase("WrittenBy")) {
                result = m1.getWrittenBy().compareToIgnoreCase(m2.getWrittenBy());
            }
            else if (sortField.equalsIgnoreCase("SoundMix")) {
                result = m1.getCast().compareToIgnoreCase(m2.getCast());
            }
            else if (sortField.equalsIgnoreCase("Language")) {
                result = m1.getLanguage().compareToIgnoreCase(m2.getLanguage());
            }
            else if (sortField.equalsIgnoreCase("Genre")) {
                result = m1.getGenre().compareToIgnoreCase(m2.getGenre());
            }
            else if (sortField.equalsIgnoreCase("Colour")) {
                result = m1.getColour().compareToIgnoreCase(m2.getColour());
            }
            else if (sortField.equalsIgnoreCase("Seen")) {
                if (!m1.getSeen() && m1.getSeen()) {
                    result = -1;
                }
                else if (m1.getSeen() && !m1.getSeen()) {
                    result = 1;
                }
            }
            else if (sortField.equalsIgnoreCase("Country")) {
                result = m1.getCountry().compareToIgnoreCase(m2.getCountry());
            }
            else if (sortField.equalsIgnoreCase("Title")) {
                result = m1.getTitle().compareToIgnoreCase(m2.getTitle());
            }
            else if (sortField.equalsIgnoreCase("Aka")) {
                result = m1.getAka().compareToIgnoreCase(m2.getAka());
            }
            else if (sortField.equalsIgnoreCase("WebRuntime")) {
                result = m1.getWebRuntime().compareToIgnoreCase(m2.getWebRuntime());
            }
            else if (sortField.equalsIgnoreCase("Rating")) {
                result = m1.getRating().compareToIgnoreCase(m2.getRating());
            }
            else if (sortField.equalsIgnoreCase("Certification")) {
                result = m1.getCertification().compareToIgnoreCase(m2.getCertification());
            }

            // Additional fields

            if (m1.getHasAdditionalInfoData() && m2.getHasAdditionalInfoData()) {
                ModelAdditionalInfo a1 = m1.getAdditionalInfo();
                ModelAdditionalInfo a2 = m2.getAdditionalInfo();
                if (a1 != null && a2 != null) {
                    if (sortField.equalsIgnoreCase("Subtitles")) {
                        result = a1.getSubtitles().compareToIgnoreCase(a2.getSubtitles());
                    }
                    else if (sortField.equalsIgnoreCase("Duration")) {
                        result = new Integer(a1.getDuration()).compareTo(new Integer(a2.getDuration()));
                    }
                    else if (sortField.equalsIgnoreCase("Filesize")) {
                        result = new Integer(a1.getFileSize()).compareTo(new Integer(a2.getFileSize()));
                    }
                    else if (sortField.equalsIgnoreCase("CDs")) {
                        result = new Integer(a1.getCDs()).compareTo(new Integer(a2.getCDs()));
                    }
                    else if (sortField.equalsIgnoreCase("CDCases")) {
                        result = new Double(a1.getCDCases()).compareTo(new Double(a2.getCDCases()));
                    }
                    else if (sortField.equalsIgnoreCase("Resolution")) {
                        result = a1.getResolution().compareToIgnoreCase(a2.getResolution());
                    }
                    else if (sortField.equalsIgnoreCase("VideoCodec")) {
                        result = a1.getVideoCodec().compareToIgnoreCase(a2.getVideoCodec());
                    }
                    else if (sortField.equalsIgnoreCase("VideoRate")) {
                        result = a1.getVideoRate().compareToIgnoreCase(a2.getVideoRate());
                    }
                    else if (sortField.equalsIgnoreCase("VideoBitrate")) {
                        result = a1.getVideoBitrate().compareToIgnoreCase(a2.getVideoBitrate());
                    }
                    else if (sortField.equalsIgnoreCase("AudioCodec")) {
                        result = a1.getAudioCodec().compareToIgnoreCase(a2.getAudioCodec());
                    }
                    else if (sortField.equalsIgnoreCase("AudioRate")) {
                        result = a1.getAudioRate().compareToIgnoreCase(a2.getAudioRate());
                    }
                    else if (sortField.equalsIgnoreCase("AudioBitrate")) {
                        result = a1.getAudioBitrate().compareToIgnoreCase(a2.getAudioBitrate());
                    }
                    else if (sortField.equalsIgnoreCase("AudioChannels")) {
                        result = a1.getAudioChannels().compareToIgnoreCase(a2.getAudioChannels());
                    }
                    else if (sortField.equalsIgnoreCase("FileLocation")) {
                        result = a1.getFileLocation().compareToIgnoreCase(a2.getFileLocation());
                    }
                    else if (sortField.equalsIgnoreCase("FileCount")) {
                        result = new Integer(a1.getFileCount()).compareTo(new Integer(a2.getFileCount()));
                    }
                    else if (sortField.equalsIgnoreCase("Container")) {
                        result = a1.getContainer().compareToIgnoreCase(a2.getContainer());
                    }
                    else if (sortField.equalsIgnoreCase("MediaType")) {
                        result = a1.getMediaType().compareToIgnoreCase(a2.getMediaType());
                    }

                    // Extra info
                    else {
                        ArrayList extra1 = a1.getExtraInfoFieldNames();
                        ArrayList extra2 = a2.getExtraInfoFieldNames();
                        if (extra1 != null && extra2 != null) {
                            for (int i = 0; i < extra1.size(); i++) {
                                if (sortField.equalsIgnoreCase( (String) extra1.get(i))) {
                                    result = a1.getExtraInfoFieldValue(i).compareToIgnoreCase(a2.getExtraInfoFieldValue(i));
                                }
                            }
                        }
                    }
                }
            }

            if (result == 0) { // equal, sort by title as secondary sortoption
                result = m1.getTitle().compareToIgnoreCase(m2.getTitle());
            }

            return result;
        }
    }
}
