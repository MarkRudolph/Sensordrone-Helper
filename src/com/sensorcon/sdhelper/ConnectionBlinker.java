package com.sensorcon.sdhelper;

import android.os.Handler;

import com.sensorcon.sensordrone.Drone;
import com.sensorcon.sdhelper.OnOffRunnable;

/**
 * This is a class that will blink the LEDs at a defined interval
 * 
 * @author Mark Rudolph, Sensorcon, Inc.
 *
 */
public class ConnectionBlinker implements OnOffRunnable {

	// Settings and such
	private Drone myDrone;
	private boolean onOff;
	private boolean LEDonOff;
	private int rate;
	private Handler blinkHandler = new Handler();
	private int myRed;
	private int myGreen;
	private int myBlue;

	/*
	 * Our constructor sets all of the dettings, but we provide methods to chang them later as well.
	 */
	public ConnectionBlinker(Drone drone, int msDelay, int Red, int Green, int Blue) {
		myDrone = drone;
		rate = msDelay;
		myRed = Red;
		myGreen = Green;
		myBlue = Blue;
	}

	/*
	 * Set the rate
	 */
	public void setRate(int msDelay) {
		rate = msDelay;
	}
	
	/*
	 * Set the LED colors
	 */
	public void setColors(int Red, int Green, int Blue){
		myRed = Red;
		myGreen = Green;
		myBlue = Blue;
	}


	@Override
	public void run() {
		// Are we enabled?
		if (onOff) {
			// Toggle
			if (LEDonOff) {
				myDrone.setLEDs(myRed, myGreen, myBlue);
			} else {
				myDrone.setLEDs(0, 0, 0);
			}
			
			LEDonOff = !LEDonOff; // Flip-Flop
			
			// Do again at specified rate
			blinkHandler.postDelayed(this, rate);
		}
	}

	@Override
	public void disable() {
		onOff = false; // Disable
		
		// Shut down the handler
		blinkHandler.removeCallbacksAndMessages(null);
		
		// Make sure the LEDs are off (in case we were disabled mid-blink).
		myDrone.setLEDs(0, 0, 0);
	}

	@Override
	public void enable() {
		onOff = true; // Enable
		LEDonOff = true; // Set ready to blink on
	}

}


