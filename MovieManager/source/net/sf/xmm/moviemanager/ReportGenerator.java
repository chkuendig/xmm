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

    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();
    private JLabel exampleLabel = new JLabel();
    private JLabel descriptionLabel = new JLabel();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private JPanel jPanel3 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JPanel panelOptions = new JPanel();
    private JPanel panelProgress = new JPanel();
    private JPanel panelReport = new JPanel();
    private JButton btnAction = new JButton();
    private JButton btnClose = new JButton();
    private JList layoutList = new JList();
    private JSplitPane splitPane = new JSplitPane();
    private JScrollPane scrollPaneList = new JScrollPane();
    private JProgressBar progressBar = new JProgressBar();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private BorderLayout borderLayout4 = new BorderLayout();
    private CardLayout cardLayout1 = new CardLayout();
    private FlowLayout flowLayout1 = new FlowLayout();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private Border border1 = BorderFactory.createEmptyBorder(0, 3, 10, 3);

    public ReportGenerator() {
        this(null);
    }

    public ReportGenerator(Frame frame) {
        if (instance != null) { // on second instantiation, just show the first
            instance.setState(Frame.NORMAL);
            instance.toFront();
        }
        else {
            setTitle((frame != null ? frame.getTitle() : "") + " - Report Generator");
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
        btnClose.setText("Close");
        btnClose.addActionListener(this);
        btnAction.setText("Generate");
        btnAction.addActionListener(this);
        btnAction.setEnabled(false);
        panelOptions.setLayout(borderLayout1);
        jLabel1.setText(" Layout");
        jPanel2.setLayout(borderLayout2);
        jLabel2.setText(" Example");
        jPanel3.setLayout(borderLayout3);
        jPanel4.setLayout(cardLayout1);
        panelReport.setLayout(borderLayout4);
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        panelProgress.setLayout(gridBagLayout1);
        exampleLabel.setBackground(UIManager.getColor("controlShadow"));
        exampleLabel.setForeground(UIManager.getColor("controlHighlight"));
        exampleLabel.setOpaque(true);
        exampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionLabel.setBackground(UIManager.getColor("controlShadow"));
        jLabel3.setText("Generating report... Please wait");
        layoutList.addListSelectionListener(this);
        descriptionLabel.setBorder(border1);
        descriptionLabel.setOpaque(true);
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionLabel.setText(" ");
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(btnAction);
        jPanel1.add(btnClose);
        splitPane.add(jPanel3, JSplitPane.LEFT);
        splitPane.add(jPanel2, JSplitPane.RIGHT);
        splitPane.setDividerLocation(200);
        jPanel3.add(scrollPaneList, java.awt.BorderLayout.CENTER);
        jPanel3.add(jLabel1, java.awt.BorderLayout.NORTH);
        scrollPaneList.getViewport().add(layoutList);
        panelOptions.add(splitPane, java.awt.BorderLayout.CENTER);
        jPanel4.add(panelOptions, "options");
        jPanel4.add(panelProgress, "progress");
        jPanel4.add(panelReport, "report");
        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);
        panelProgress.add(progressBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 0, 30), 0, 0));
        panelProgress.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 10, 30), 0, 0));
        jPanel2.add(exampleLabel, java.awt.BorderLayout.CENTER);
        jPanel2.add(descriptionLabel, java.awt.BorderLayout.SOUTH);
        jPanel2.add(jLabel2, java.awt.BorderLayout.NORTH);
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
        btnAction.setEnabled(false);
        btnClose.setEnabled(false);
        cardLayout1.show(jPanel4, "progress");
        try {
            jLabel3.setText("Generating report... Please wait");
            HashMap parms = new HashMap();
            parms.put("logo", getImageURL("/images/filmFolder.png").toString());
            ReportGeneratorDataSource ds = new ReportGeneratorDataSource((DefaultMutableTreeNode)MovieManager.getIt().getMoviesList().getModel().getRoot(), selectedLayout.episodes, selectedLayout.sortField, progressBar, getImageURL("/images/movie.png"), false);
            JasperPrint print = JasperFillManager.fillReport("reports/" + selectedLayout.filename, parms, ds);
            JRViewer viewerPanel = new JRViewer(print);
            panel.removeAll();
            panel.add(viewerPanel, BorderLayout.CENTER);
            cardLayout1.show(jPanel4, "report");
            viewerPanel.setFitWidthZoomRatio();
        }
        catch (Exception ex) {
            jLabel3.setText("Error generating report");
            progressBar.setValue(0);
            Logger.getRootLogger().error("Error generating report", ex);
        }
        btnAction.setText("Select Layout");
        btnAction.setEnabled(true);
        btnClose.setEnabled(true);
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
                int height = exampleLabel.getHeight() - PREVIEW_MARGIN;
                int width = (height * icon.getIconWidth()) / icon.getIconHeight();
                if (width > exampleLabel.getWidth() - PREVIEW_MARGIN) {
                    width = exampleLabel.getWidth() - PREVIEW_MARGIN;
                    height = (width * icon.getIconHeight()) / icon.getIconWidth();
                }

                Image image = selectedLayout.exampleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);
                exampleLabel.setIcon(icon);
                exampleLabel.setText("");
            }
            else {
                exampleLabel.setIcon(null);
                exampleLabel.setText("No example");
            }

            descriptionLabel.setText(selectedLayout.description != null ? selectedLayout.description : "No description");
        }
        else {
            exampleLabel.setIcon(null);
            exampleLabel.setText("Select layout");
            descriptionLabel.setText(" ");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClose) {
            dispose();
        }
        else if (e.getSource() == btnAction) {
            if (btnAction.getText().equalsIgnoreCase("Generate")) {
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
                btnAction.setText("Generate");
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
                selectedLayout = (LayoutItem)layoutList.getSelectedValue();
                selectedLayout.fetchInfo();
                btnAction.setEnabled(true);
            }
            else {
                selectedLayout = null;
                btnAction.setEnabled(false);
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
