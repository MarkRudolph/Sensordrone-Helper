package com.sensorcon.sdhelper;

import android.os.Handler;
import android.os.HandlerThread;

import com.sensorcon.sensordrone.Drone;
import com.sensorcon.sdhelper.OnOffRunnable;

/**
 * This class implements our OnOffRunnable Interface.
 * 
 * When enabled, it will take a measurement via the Sensordrone API's
 * quickSystem at the specified time interval (ms).
 * 
 * @author Mark Rudolph, Sensorcon, Inc.
 */
public class SDStreamer implements OnOffRunnable {

	private Drone myDrone; // The Drone

	private boolean mEnabled = false; // Off by default
	private int quickSystemInt; // the QS_SENSOR_TYPE to measure

	private HandlerThread mHandlerThread;
	// A handler to request measurements at an interval
	public Handler streamHandler;

	@Override
	public void run() {
		// Don't do anything if we're not connected
		if (!myDrone.isConnected) {
			disable();
		}
		// If we're enabled, take a measurement!
		if (mEnabled) {
			// Take a measurement
			myDrone.quickMeasure(quickSystemInt);
		}
	}

	@Override
	public void disable() {
		mEnabled = false; // Disable the streamer

		// Shut down the Handler
		streamHandler.removeCallbacksAndMessages(null);

		mHandlerThread.quit();
	}

	// Constructors
	public SDStreamer(Drone drone, int qsInt) {
		myDrone = drone; // The Drone we're measuring
		quickSystemInt = qsInt; // The QS_SENSOR_TYPE
	}

	@Override
	public void enable() {
		mEnabled = true; // Enable the streamer

		mHandlerThread = new HandlerThread("Sensor Drone Streamer");
		mHandlerThread.start();
		streamHandler = new Handler(mHandlerThread.getLooper());
	}
}
