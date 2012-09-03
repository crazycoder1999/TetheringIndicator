package com.pestohacks.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//All the code is GPL 3.0 and comes from http://pestohacks.blogspot.com
//Made by crazycoder1999
public class BtUtility {

	//private static final int REQUEST_ENABLE_BT = 100;
	BluetoothListener btListener;
	Context ctx;
	int visibility = 360;
	
	public BtUtility(BluetoothListener btListener,Context ctx) {
		this.btListener = btListener;
		this.ctx = ctx;
	}
	
	//how many seconds this device need to be visible from others.
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	
	// get the bluetooth device reference
	public BluetoothAdapter findMyBT(BluetoothAdapter myBt){
    	myBt = BluetoothAdapter.getDefaultAdapter();
    	if (myBt!= null) {
    		Log.i("BTDEV","BT Device Found!");
    		Log.i("BTDEV","" + myBt.getName());
    		return myBt;
    	} else {
    		Log.i("BTDEV","No BT Device Found!");
    		return null;
    	}
    }
    
	//ask to enable BT to user.. what Happenend if it answer NO? Should I have to check that REQUEST_ENABLE_BT?
	//only if it is enable.
    public void enableMyBT(BluetoothAdapter myBt) {
    	if ( ! myBt.isEnabled()) {
    	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	    enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    ctx.startActivity(enableBtIntent);
    	}	
    }
    
    //ask to be visible for <visibility> seconds.
    public void beVisible() {
    	Log.i("BTDEV","Let me be visible. thanks.");
    	Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    	discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, visibility);
    	discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	ctx.startActivity(discoverableIntent);
    }
    
}
