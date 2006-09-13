
package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.util.*;

import info.clearthought.layout.TableLayout;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class SimpleProgressBar extends JDialog {
    
    JProgressBar progressBar;
    JLabel label;
    
    SwingWorker swingWorker;
    
    public SimpleProgressBar(Dialog parent, boolean modal, SwingWorker worker) {
        super(parent, modal);
        createProgressBar(parent, worker);
    }
    
    
    public SimpleProgressBar(Frame parent, boolean modal, SwingWorker worker) {
        super(parent, modal);
        createProgressBar(parent, worker);
    }
    
    
    public void createProgressBar(Window parent, SwingWorker worker) {
          
	swingWorker = worker;
	
	JPanel panel = new JPanel();
	//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	
	double size[][] = {{10, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, 10}, {5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 5}};
	
	panel.setLayout(new TableLayout(size));
	
	progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
	progressBar.setPreferredSize(new Dimension(220, 40));
	//progressBar.setMaximumSize(new Dimension(220, 40));
	//progressBar.setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
	
	label = new JLabel();
	label.setPreferredSize(new Dimension(40, 35));
	//label.setBorder(BorderFactory.createEmptyBorder(10,5,5,10));
	
	label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), 20));
	
	JButton abortButton = new JButton("Abort");
	//abortButton.setBorder(BorderFactory.createEmptyBorder(10,60,5,20));
	
	abortButton.setPreferredSize(new Dimension(150, 30));

	abortButton.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
		    swingWorker.interrupt();
		    dispose();
		}
	    });
	
	JPanel buttonPanel = new JPanel(new BorderLayout());
	buttonPanel.add(abortButton, BorderLayout.NORTH);
	
	panel.add(label, "1, 1, 4, 1");
	panel.add(progressBar, "1, 3, 3, 3");
	//panel.add(abortButton, "2, 5");

	getContentPane().add(panel);
		
	pack();
	
	setSize(400, 170);
	//setSize(panel.getPreferredSize());
	
	setLocationRelativeTo(parent);

    }

    void setString(String str) {
	label.setText(str);
    }
    
    void close() {
	dispose();
	    
	    if (swingWorker != null) {
		swingWorker.interrupt();
	    }
    }
    
}
