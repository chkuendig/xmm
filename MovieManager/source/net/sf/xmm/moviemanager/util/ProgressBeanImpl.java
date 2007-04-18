package net.sf.xmm.moviemanager.util;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

//import spin.demo.Assert;

/**
 * Implementation of a progress.
 */
public class ProgressBeanImpl implements ProgressBean {

	public PropertyChangeListener listener = null;
  
	private boolean cancelled = false;
  private double  status;
  private String message = "";
  
  /**
   * Constructor.
   */
  public ProgressBeanImpl() {
  }

  /**
   * Add a listener to property changes.
   * 
   * @param listener listener to add
   */
  synchronized public void addPropertyChangeListener(PropertyChangeListener listener) {
    //Assert.isNotEDT();
 
	  GUIUtil.isNotEDT();
	  
	  this.listener = listener;
	  return;
  }
  
  /**
   * Start.
   */
  public void start() {
	  
  }

  /**
   * Cancel the progress.
   */
  public synchronized void cancel() {
	 
	  GUIUtil.isNotEDT();

    cancelled = true;
  }

  /**
   * Get the current status.
   *
   * @return    status of progress
   */
  public double getStatus() {
	  GUIUtil.isNotEDT();
	  
    return status;
  }
  
  public String getMessage() {
	  GUIUtil.isNotEDT();
	  
	  return message;
  }
  
  public synchronized boolean getCancelled() {
	  
	  GUIUtil.isNotEDT();
	  return cancelled;
  }
}

