package com.sensorcon.sdhelper;

import android.os.Handler;
import android.os.HandlerThread;

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
	private boolean mEnabled;
	private boolean mLedEnabled;
	private int mRate;
	private HandlerThread mHandlerThread;
	private Handler mBlinkHandler;
	private int mRed;
	private int mGreen;
	private int mBlue;

	/*
	 * Our constructor sets all of the dettings, but we provide methods to chang
	 * them later as well.
	 */
	public ConnectionBlinker(Drone drone, int msDelay, int Red, int Green,
			int Blue) {
		myDrone = drone;
		mRate = msDelay;
		mRed = Red;
		mGreen = Green;
		mBlue = Blue;
	}

	/*
	 * Set the rate
	 */
	public void setRate(int msDelay) {
		mRate = msDelay;
	}

	/*
	 * Set the LED colors
	 */
	public void setColors(int Red, int Green, int Blue) {
		mRed = Red;
		mGreen = Green;
		mBlue = Blue;
	}

	@Override
	public void run() {
		// Are we enabled?
		if (mEnabled) {
			// Toggle
			if (mLedEnabled) {
				myDrone.setLEDs(mRed, mGreen, mBlue);
			} else {
				myDrone.setLEDs(0, 0, 0);
			}

			mLedEnabled = !mLedEnabled; // Flip-Flop

			// Do again at specified rate
			mBlinkHandler.postDelayed(this, mRate);
		}
	}

	@Override
	public void disable() {
		mEnabled = false; // Disable

		// Shut down the handler
		mBlinkHandler.removeCallbacksAndMessages(null);

		mHandlerThread.quit();

		// Make sure the LEDs are off (in case we were disabled mid-blink).
		myDrone.setLEDs(0, 0, 0);
	}

	@Override
	public void enable() {
		mEnabled = true; // Enable
		mLedEnabled = true; // Set ready to blink on
		mHandlerThread = new HandlerThread("ConnectionBlinker handler thread");
		mHandlerThread.start();
		mBlinkHandler = new Handler(mHandlerThread.getLooper());
	}

}
