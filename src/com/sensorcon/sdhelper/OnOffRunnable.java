package com.sensorcon.sdhelper;

/**
 * An interface that extends Runnable. We will use this so we can
 * have enable/disable capabilities
 * 
 * @author Mark Rudolph, Sensorcon, Inc.
 *
 */
public interface OnOffRunnable extends Runnable {
	
	@Override
	public void run();
	/*
	 * Used to stop the runnable
	 */
	public void disable();
	/*
	 * Used to enable the runnable
	 */
	public void enable();

}
