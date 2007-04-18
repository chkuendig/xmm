
package net.sf.xmm.moviemanager;

import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.ProgressBean;
import net.sf.xmm.moviemanager.util.ProgressBeanImpl;

import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class SimpleProgressBar extends JDialog implements PropertyChangeListener {

	JProgressBar progressBar;
	JLabel label;
	ProgressBean progressBean;
	boolean done = false;

	public SimpleProgressBar(Dialog parent, String title, boolean modal, ProgressBean progressBean) {
		super(parent, modal);
		this.progressBean = progressBean;
		setTitle(title);
		progressBean.addPropertyChangeListener(this);	
		createProgressBar(parent, title);
	}


	public SimpleProgressBar(Frame parent, String title, boolean modal, ProgressBean progressBean) {
		super(parent, modal);
		this.progressBean = progressBean;
		setTitle(title);
		progressBean.addPropertyChangeListener(this);
		createProgressBar(parent, title);
	}


	public void createProgressBar(Window parent, String title) {

		JPanel panel = new JPanel();
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		double size[][] = {{10, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, 10}, {5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 5}};

		panel.setLayout(new TableLayout(size));

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(220, 40));

		label = new JLabel(title);
		label.setPreferredSize(new Dimension(40, 35));

		label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), 20));

		JButton abortButton = new JButton("Abort");

		abortButton.setPreferredSize(new Dimension(150, 30));

		abortButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				progressBean.cancel();
				dispose();
			}
		});

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(abortButton, BorderLayout.NORTH);

		panel.add(label, "1, 1, 4, 1");
		panel.add(progressBar, "1, 3, 3, 3");

		getContentPane().add(panel);

		pack();
		setSize(400, 170);
		setLocationRelativeTo(parent);
	}

	void setString(String str) {
		label.setText(str);
	}

	void close() {
		dispose();
	}


	/**
	 * @see java.beans.PropertyChangeListener
	 */
	public void propertyChange(final PropertyChangeEvent evt) {

		//GUIUtil.isNotEDT();

		if ("value".equals(evt.getPropertyName())) {

			try {

				GUIUtil.invokeLater(new Runnable() {

					public void run() {
						Object status = evt.getNewValue();

						// Done
						if (status == null) {
							done = true;
							dispose();
						}
						else
							setString((String)status);

					}});
			} catch (Exception e) {
				;
			}
		}
	}
}
