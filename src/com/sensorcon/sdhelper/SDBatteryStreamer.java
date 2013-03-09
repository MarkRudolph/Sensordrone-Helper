package com.sensorcon.sdhelper;

import android.os.Handler;

import com.sensorcon.sensordrone.Drone;
import com.sensorcon.sdhelper.OnOffRunnable;

/**
 * This class implements our OnOffRunnable Interface.
 * 
 * When enabled, it will measure the Sensordrone's battery voltage.
 * Measuring battery voltage is not part of the Sensordrone's quickSystem,
 * but you can see from this example that it is not difficult to set up
 * a streaming-like scheme.
 * 
 * @author Mark Rudolph, Sensorcon, Inc.
 */
public class SDBatteryStreamer implements OnOffRunnable {

	private Drone myDrone; // The Drone
	
	// We make this public so we can see if the streaming of battery voltage is
	// on or off, since there is no sensor enabled/disabled method for this
	public boolean OnOff = false; // Off by default
	
	// A handler to request measurements at an interval
	public Handler streamHandler = new Handler(); 

	@Override
	public void run() {
		// Don't do anything if we're not connected
		if (!myDrone.isConnected) {
			disable();
		}
		// If we're enabled, take a measurement!
		if(OnOff) {
			// Take a measurement
			myDrone.measureBatteryVoltage();
		}
	}

	@Override
	public void disable() {
		OnOff = false; // Disable the streamer

		// Shut down the Handler
		streamHandler.removeCallbacksAndMessages(null);
	}

	// Constructors
	public SDBatteryStreamer(Drone drone) {
		myDrone = drone; // The Drone we're measuring
	}


	@Override
	public void enable() {
		OnOff = true; // Enable the streamer
	}
}


