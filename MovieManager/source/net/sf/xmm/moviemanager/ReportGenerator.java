package net.sf.xmm.moviemanager;

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.apache.log4j.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.*;
import net.sf.xmm.moviemanager.database.*;
import net.sf.xmm.moviemanager.models.*;

/**
 * ReportGenerator using JasperReports. Current entries in movielist is used as
 * data.
 *
 * @author olba2
 */
public class ReportGenerator extends JFrame implements ActionListener, WindowListener, FilenameFilter, ListSelectionListener {
    private static final int PREVIEW_MARGIN = 20;
    private static ReportGenerator instance = null;

    private LayoutItem selectedLayout = null;

    private JLabel labelProgress = new JLabel();
    private JLabel labelExample = new JLabel();
    private JLabel labelDescription = new JLabel();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private JPanel jPanel3 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JPanel jPanel5 = new JPanel();
    private JPanel jPanel6 = new JPanel();
    private JPanel panelOptions = new JPanel();
    private JPanel panelProgress = new JPanel();
    private JPanel panelReport = new JPanel();
    private JButton buttonAction = new JButton();
    private JButton buttonClose = new JButton();
    private JList layoutList = new JList();
    private JRadioButton radioButtonCurrentList = new JRadioButton();
    private JRadioButton radioButtonSelectedMovies = new JRadioButton();
    private JRadioButton radioButtonAllMovies = new JRadioButton();
    private ButtonGroup buttonGroup = new ButtonGroup();
    private JCheckBox checkBoxEpisodes = new JCheckBox();
    private JSplitPane splitPane = new JSplitPane();
    private JScrollPane scrollPaneList = new JScrollPane();
    private JProgressBar progressBar = new JProgressBar();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private BorderLayout borderLayout4 = new BorderLayout();
    private BorderLayout borderLayout5 = new BorderLayout();
    private CardLayout cardLayout1 = new CardLayout();
    private FlowLayout flowLayout1 = new FlowLayout();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private Border border1 = BorderFactory.createEmptyBorder(0, 3, 10, 3);
    private Border border2 = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        " Content ",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font(layoutList.getFont().getName(), Font.BOLD, layoutList.getFont().getSize())),
        BorderFactory.createEmptyBorder(0, 5, 5, 5));
    private Border border3 = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        " Layout ",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font(layoutList.getFont().getName(), Font.BOLD, layoutList.getFont().getSize())),
        BorderFactory.createEmptyBorder(0, 5, 5, 5));
    private Border border4 = BorderFactory.createEmptyBorder(0, 5, 0, 3);

    public ReportGenerator() {
        this(null);
    }

    public ReportGenerator(Frame frame) {
        if (instance != null) { // on second instantiation, just show the first
            instance.setState(Frame.NORMAL);
            instance.toFront();
        }
        else {
            setTitle( (frame != null ? frame.getTitle() : "") + " - Report Generator");
            try {
                jbInit();
                setLocation(50, 50);
                setSize(640, 480);
                setVisible(true);

                loadReportLayouts();
            }
            catch (Exception ex) {
                Logger.getRootLogger().error("Error initializing Report Generator", ex);
            }
            instance = this;
        }
    }

    /**
     * jbInit - sets up window content
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        setIconImage(MovieManager.getIt().getIconImage());
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                this_componentResized(e);
            }
        });
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        buttonClose.setText("Close");
        buttonClose.addActionListener(this);
        buttonAction.setText("Generate");
        buttonAction.addActionListener(this);
        buttonAction.setEnabled(false);
        panelOptions.setLayout(borderLayout1);
        jPanel2.setLayout(borderLayout2);
        jPanel3.setLayout(borderLayout3);
        jPanel4.setLayout(cardLayout1);
        panelReport.setLayout(borderLayout4);
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        panelProgress.setLayout(gridBagLayout1);
        labelExample.setBackground(UIManager.getColor("controlShadow"));
        labelExample.setForeground(UIManager.getColor("controlHighlight"));
        labelExample.setOpaque(true);
        labelExample.setHorizontalAlignment(SwingConstants.CENTER);
        labelDescription.setBackground(UIManager.getColor("controlShadow"));
        labelProgress.setText("Generating report... Please wait");
        layoutList.addListSelectionListener(this);
        labelDescription.setBorder(border1);
        labelDescription.setOpaque(true);
        labelDescription.setHorizontalAlignment(SwingConstants.CENTER);
        labelDescription.setText(" ");
        jPanel5.setBorder(border2);
        jPanel5.setLayout(gridLayout1);
        radioButtonCurrentList.setText("Current movielist");
        radioButtonSelectedMovies.setText("Selected movies");
        radioButtonAllMovies.setText("All movies");
        radioButtonCurrentList.setSelected(true);
        buttonGroup.add(radioButtonCurrentList);
        buttonGroup.add(radioButtonSelectedMovies);
        buttonGroup.add(radioButtonAllMovies);
        checkBoxEpisodes.setText("Include episodes");
        checkBoxEpisodes.setSelected(true);
        gridLayout1.setRows(4);
        jPanel6.setLayout(borderLayout5);
        jPanel6.setBorder(border3);
        jPanel3.setBorder(border4);
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(buttonAction);
        jPanel1.add(buttonClose);
        splitPane.add(jPanel3, JSplitPane.LEFT);
        splitPane.add(jPanel2, JSplitPane.RIGHT);
        splitPane.setDividerLocation(200);
        panelOptions.add(splitPane, java.awt.BorderLayout.CENTER);
        jPanel4.add(panelOptions, "options");
        jPanel4.add(panelProgress, "progress");
        jPanel4.add(panelReport, "report");
        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);
        panelProgress.add(progressBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 0, 30), 0, 0));
        panelProgress.add(labelProgress, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 10, 30), 0, 0));
        jPanel2.add(labelExample, java.awt.BorderLayout.CENTER);
        jPanel2.add(labelDescription, java.awt.BorderLayout.SOUTH);
        jPanel3.add(jPanel5, java.awt.BorderLayout.SOUTH);
        jPanel5.add(radioButtonCurrentList);
        jPanel5.add(radioButtonSelectedMovies);
        jPanel5.add(radioButtonAllMovies);
        jPanel5.add(checkBoxEpisodes);
        scrollPaneList.getViewport().add(layoutList);
        jPanel6.add(scrollPaneList, java.awt.BorderLayout.CENTER);
        jPanel3.add(jPanel6, java.awt.BorderLayout.CENTER);
    }

    /**
     * loadReportLayouts - scans "reports" directory for jasper files and shows
     * the filenames in the JList.
     */
    private void loadReportLayouts() {
        File reportDir = new File("reports");
        String[] files = reportDir.list(this);
        if (files.length > 0) {
            LayoutItem[] layouts = new LayoutItem[files.length];
            for (int i = 0; i < files.length; i++) {
                layouts[i] = new LayoutItem(files[i]);
            }
            layoutList.setListData(layouts);
            layoutList.setSelectedIndex(0);
        }
    }

    /**
     * createReport - creates report using selected report layout
     *
     * @param panel JPanel - report preview will be added to this parent
     *   container.
     */
    private void createReport(JPanel panel) {
        buttonAction.setEnabled(false);
        buttonClose.setEnabled(false);
        cardLayout1.show(jPanel4, "progress");

        LinkedList movies = new LinkedList();
        boolean includeEpisodes = checkBoxEpisodes.isSelected();
        if (radioButtonAllMovies.isSelected()) { // load all movies from database
            labelProgress.setText("Loading movies... Please wait");
            progressBar.setValue(0);
            Database database = MovieManager.getIt().getDatabase();
            DefaultListModel moviesList = database.getMoviesList("Title");
            ArrayList episodesList = includeEpisodes ? database.getEpisodeList("movieID") : null;
            Object[] m = moviesList.toArray();
            for (int i = 0; i < m.length; i++) {
                movies.add(m[i]);
                if (includeEpisodes) { // add episodes
                    int tempKey = ( (ModelEntry) m[i]).getKey();
                    for (int u = 0; u < episodesList.size(); u++) {
                        if (tempKey == ( (ModelEpisode) episodesList.get(u)).getMovieKey()) {
                            movies.add(episodesList.get(u));
                        }
                    }
                }
            }
        }
        else { // tree contains movies
            boolean onlySelected = radioButtonSelectedMovies.isSelected();
            JTree tree = MovieManager.getIt().getMoviesList();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            int n = root.getChildCount();
            for (int i = 0; i < n; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
                boolean addnode = true;
                if (onlySelected) {
                    int row = tree.getRowForPath(new TreePath(node.getPath()));
                    if (!tree.isRowSelected(row)) {
                        addnode = false;
                    }
                }
                if (addnode) {
                    movies.add(node.getUserObject());
                }
                if (includeEpisodes) {
                    int episodeCount = node.getChildCount();
                    for (int j = 0; j < episodeCount; j++) {
                        DefaultMutableTreeNode episodeNode = (DefaultMutableTreeNode) node.getChildAt(j);
                        addnode = true;
                        if (onlySelected) {
                            int row = tree.getRowForPath(new TreePath(episodeNode.getPath()));
                            if (!tree.isRowSelected(row)) {
                                addnode = false;
                            }
                        }
                        if (addnode) {
                            movies.add(episodeNode.getUserObject());
                        }
                    }
                }
            }
        }

        try {
            labelProgress.setText("Generating report... Please wait");
            HashMap parms = new HashMap();
            parms.put("logo", getImageURL("/images/filmFolder.png").toString());
            ReportGeneratorDataSource ds = new ReportGeneratorDataSource(movies, selectedLayout.sortField, progressBar, getImageURL("/images/movie.png"), false);
            JasperPrint print = JasperFillManager.fillReport("reports/" + selectedLayout.filename, parms, ds);
            JRViewer viewerPanel = new JRViewer(print);
            panel.removeAll();
            panel.add(viewerPanel, BorderLayout.CENTER);
            cardLayout1.show(jPanel4, "report");
            viewerPanel.setFitWidthZoomRatio();
        }
        catch (Exception ex) {
            labelProgress.setText("Error generating report");
            progressBar.setValue(0);
            Logger.getRootLogger().error("Error generating report", ex);
        }
        buttonAction.setText("Select Layout");
        buttonAction.setEnabled(true);
        buttonClose.setEnabled(true);
    }

    /**
     * getImageURL
     *
     * @param filename String - image filename
     * @return URL for image
     */
    public URL getImageURL(String filename) {
        return getClass().getResource(filename);
    }

    /**
     * updateExample - updates example. Call when window changes size or layout
     * selection changes
     */
    private void updateExample() {
        if (selectedLayout != null) {

            // display example
            if (selectedLayout.exampleImage != null) {
                ImageIcon icon = new ImageIcon(selectedLayout.exampleImage); // getting size directly from image was unreliable
                int height = labelExample.getHeight() - PREVIEW_MARGIN;
                int width = (height * icon.getIconWidth()) / icon.getIconHeight();
                if (width > labelExample.getWidth() - PREVIEW_MARGIN) {
                    width = labelExample.getWidth() - PREVIEW_MARGIN;
                    height = (width * icon.getIconHeight()) / icon.getIconWidth();
                }

                Image image = selectedLayout.exampleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);
                labelExample.setIcon(icon);
                labelExample.setText("");
            }
            else {
                labelExample.setIcon(null);
                labelExample.setText("No example");
            }

            labelDescription.setText(selectedLayout.description != null ? selectedLayout.description : "No description");
        }
        else {
            labelExample.setIcon(null);
            labelExample.setText("Select layout");
            labelDescription.setText(" ");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonClose) {
            dispose();
        }
        else if (e.getSource() == buttonAction) {
            if (buttonAction.getText().equalsIgnoreCase("Generate")) {
                Thread t = new Thread() { // generate using thread - ensures update of progress bar
                    public void run() {
                        createReport(panelReport);
                    }
                };
                t.start();
            }
            else {
                panelReport.removeAll();
                cardLayout1.show(jPanel4, "options");
                buttonAction.setText("Generate");
            }
        }
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
        instance = null;
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * accept - filter for jasper filenames
     *
     * @param dir File - not used
     * @param name String - filename
     * @return boolean - true for accepted files
     */
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith("jasper");
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (layoutList.getSelectedValue() != null) {
                selectedLayout = (LayoutItem) layoutList.getSelectedValue();
                selectedLayout.fetchInfo();
                buttonAction.setEnabled(true);
                checkBoxEpisodes.setSelected(selectedLayout.episodes);
            }
            else {
                selectedLayout = null;
                buttonAction.setEnabled(false);
            }
            updateExample();
        }
    }

    /**
     * this_componentResized - updates example when window size changes
     *
     * @param e ComponentEvent
     */
    public void this_componentResized(ComponentEvent e) {
        updateExample();
    }

    /**
     * class keeping track of filenames and displayed names
     */
    private class LayoutItem {
        String filename;
        private String displayname;
        private String examplename;
        private String sourcename;

        boolean infoFetched = false;
        Image exampleImage;
        String description;
        String sortField;
        boolean episodes;

        public LayoutItem(String filename) {
            this.filename = filename;
            String prefix = filename.substring(0, filename.length() - 7);
            displayname = mixCase(prefix.replaceAll("_", " "));
            examplename = prefix + ".png";
            sourcename = prefix + ".jrxml";
        }

        public String toString() {
            return displayname;
        }

        public String mixCase(String text) {
            StringBuffer buf = new StringBuffer();
            int len = text.length();
            boolean blank = true;
            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (c == ' ' || c == ',') {
                    blank = true;
                    buf.append(c);
                }
                else
                if (blank) {
                    buf.append(Character.toUpperCase(c));
                    blank = false;
                }
                else {
                    buf.append(Character.toLowerCase(c));
                }
            }
            return new String(buf);
        }

        public void fetchInfo() {
            if (!infoFetched) {
                // example image
                String filename = "reports/" + examplename;
                if (new File(filename).exists()) {
                    exampleImage = Toolkit.getDefaultToolkit().getImage(filename);
                }

                // xmm custom property values
                filename = "reports/" + sourcename;
                if (new File(filename).exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(filename);
                        byte[] buffer = new byte[5000]; // 5000 is a safe margin since custom fields are placed first
                        fis.read(buffer, 0, buffer.length);
                        fis.close();
                        String bufferText = new String(buffer);
                        description = getCustomPropertyValue(bufferText, "xmm.description");
                        sortField = getCustomPropertyValue(bufferText, "xmm.sortfield");
                        String s = getCustomPropertyValue(bufferText, "xmm.episodes");
                        episodes = s != null ? s.equalsIgnoreCase("true") : false;
                    }
                    catch (Exception ex) {
                    }
                }

                infoFetched = true;
            }
        }

        private String getCustomPropertyValue(String bufferText, String name) {
            String searchText = "<property name=\"" + name + "\" value=\"";
            int pos = bufferText.indexOf(searchText);
            if (pos >= 0) {
                pos += searchText.length();
                int pos2 = bufferText.indexOf('"', pos);
                if (pos2 > pos) {
                    return bufferText.substring(pos, pos2);
                }
            }
            return null;
        }
    }
}
