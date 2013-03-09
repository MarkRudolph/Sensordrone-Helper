package com.sensorcon.sdhelper;

import java.util.Set;

import com.sensorcon.sensordrone.Drone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This is a class of helpful methods to do some things with your Sensordrone.
 * 
 * It is intended to be helpful, and get your projects up and running quickly, but
 * is not part of the official Sensordrone API. Your mileage may vary :-)
 * 
 * This is built against Android-10 (Gingerbread).
 * 
 * @author Mark Rudolph, Sensorcon, Inc.
 *
 */
public class SDHelper {

	private String MAC; // The MAC address we will connect to
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver btReceiver;
	private IntentFilter btFilter;
	private Dialog scanDialog; // The Dialog we will display results in
	private AlertDialog.Builder dBuilder; // A builder for the Dialog
	
	/**
	 * We will use this to scan for MAC addresses
	 *
	 * @param drone The Drone to connect
	 * @param context Your context (for displaying a Dialog)
	 * @param includePairedDevices True if you want to include the phone's paired devices in the list
	 */
	public void scanToConnect(final Drone drone, final Activity activity, final Context context, boolean includePairedDevices) {

		// Set up our Bluetooth Adapter
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		// Is Bluetooth on?
		boolean isOn = isBTEnabled(context, btAdapter);

		if (!isOn) {
			// Don't proceed until the user turns Bluetooth on.
			return;
		}

		// Make sure MAC String is initialized and empty
		MAC = "";

		// Set up our Dialog and Builder
		scanDialog = new Dialog(context);
		dBuilder = new AlertDialog.Builder(context);
		dBuilder.setTitle("Bluetooth Devices");

		// Set up our ListView to hold the items
		ListView macList = new ListView(context);
		// Set the overlay to transparent (can sometimes obscure text)
		macList.setCacheColorHint(Color.TRANSPARENT);
		// The text will be black, so we need to change the BG color
		macList.setBackgroundColor(Color.WHITE);

		// Set up our ArrayAdapter
		final ArrayAdapter<String> macAdapter = new ArrayAdapter<String>(
				context,
				android.R.layout.simple_list_item_1);
		macList.setAdapter(macAdapter);



		// Add in the paired devices if asked
		if (includePairedDevices) {
			// Get the list of paired devices
			Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
			// Add paired devices to the list
			if (pairedDevices.size() > 0){
				for (BluetoothDevice device : pairedDevices){
					// We only want Sensordrones
					if (device.getName().contains("drone")){
						// Add the Name and MAC
						macAdapter.add(device.getName() + "\n" + device.getAddress());
					}
				}
			}
		}


		// What to do when we find a device
		btReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// We only want Sensordrones
					try {
						if (device.getName().contains("drone")){
							// Add the Name and MAC
							macAdapter.add(device.getName() + "\n" + device.getAddress());
						} 
					} catch (NullPointerException n) {
						// Some times getName() will return null, which doesn't parse very well :-)
						// Catch it here
						Log.d("SDHelper", "Found MAC will null string");
						// You can still add it to the list if you want, it just might not
						// be a Sensordrone...
						//macAdapter.add("nullDevice" + "\n" + device.getAddress());
					}
				}
			}
		};

		// Set up our IntentFilters
		btFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(btReceiver, btFilter);
		// Don't forget to unregister when done!


		// Start scanning for Bluetooth devices
		btAdapter.startDiscovery();

		// Finish displaying the menu
		dBuilder.setView(macList);
		scanDialog = dBuilder.create();
		scanDialog.show();

		// Handle the Bluetooth device selection
		macList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				// Once an item is selected...

				// We don't need to scan anymore
				btAdapter.cancelDiscovery();
				// Unregister the receiver
				context.unregisterReceiver(btReceiver);

				// Get the MAC address
				MAC = macAdapter.getItem(arg2);
				int MACLength = MAC.length();
				MAC = MAC.substring(MACLength-17, MACLength);

				// Dismiss the dialog
				scanDialog.dismiss();

			}
		});

		// Things to do when the Dialog is dismissed:
		// (When an item is selected, OR the user cancels)
		// Don't forget that when a Dialog is canceled, it is also dismissed.
		scanDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				// Don't connect if the MAC is not set
				// This protects against trying to connect if the user cancels
				if (MAC != "") {
					// Display a message if the connect fails
					if (!drone.btConnect(MAC)) {
						activity.runOnUiThread(new Runnable() {
							public void run() {
								AlertDialog.Builder alert = new AlertDialog.Builder(context);
								alert.setTitle("Couldn't connect");
								alert.setMessage("Connection was not successful.\nPlease try again!");
								alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										// If you wanted to try again directly, you could add that
										// here (and add a "cancel button" to not scan again).
									}
								});
								
								alert.show();
							}
						});
					
					}
				}

			}
		});

		// Things to do if the dialog is canceled
		scanDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				// Shut off discovery and unregister the receiver
				// if the user backs out
				btAdapter.cancelDiscovery();
				context.unregisterReceiver(btReceiver);
				// Clear the MAC so we don't try and connect
				MAC = "";
			}
		});

	}

	/*
	 * A method to check if Bluetooth is enabled.
	 */
	public boolean isBTEnabled(Context context, BluetoothAdapter btAdapter) {
		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			context.startActivity(enableBtIntent);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * A function to flash the LEDs
	 * 
	 * Note There are Thread.sleep() calls for delay in between flashing. If you call
	 * this immediately before disconnecting, all of the blinks might not be processed!
	 * 
	 * @param drone The Drone
	 * @param nFlashes How many times do we flash?
	 * @param delay_ms Delay between flashes in ms
	 * @param Red 0-255
	 * @param Green 0-255
	 * @param Blue 0-255
	 */
	public void flashLEDs(Drone drone, int nFlashes, int delay_ms, int Red, int Green, int Blue) {
		for (int i=0; i < nFlashes; i++) {
			drone.setRightLED(0, 0, 0);
			drone.setLeftLED(Red, Green, Blue);
			try {
				Thread.sleep(delay_ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			drone.setRightLED(Red, Green, Blue);
			drone.setLeftLED(0, 0, 0);
			try {
				Thread.sleep(delay_ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i == nFlashes-1) {
				drone.setLEDs(0, 0, 0);
				try {
					Thread.sleep(delay_ms);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}





	/**
	 * Our default constructor
	 */
	public SDHelper() {

	}

}

