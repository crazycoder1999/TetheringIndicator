package com.pestohacks.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import com.pestohacks.crazycoder1999.connectioncheck.Connectivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;


//All the code is GPL 3.0 and comes from http://pestohacks.blogspot.com
//Made by crazycoder1999

/**
 * A class for setup bluetooth and manage connection.
 * @author crazycoder1999
 *
 */

public class BluetoothListener implements Runnable{
	ExchangeInfo exc;
    BtUtility btUtil;
	BluetoothAdapter myBT;
	BluetoothSocket bs;
	OutputStream out;
	Thread t;
	static boolean running = true;
	int sleepTime = 1500;
	String serviceName = "";
	Context ctx;
	TextView tview;
    public BluetoothListener(Context ctx,String serviceName) {
        exc = new ExchangeInfo();        
        this.ctx = ctx;
        this.serviceName = serviceName;
        setup();
    }

    
	/**
	 * Establish on how many times it checks for connection changed.
	 * @param sleepTime
	 */
	public void checkConnectionOnEvery(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	/*CODICE SETUP da rivedere */
	/*private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) { //smthng happened.
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            logIt(device.getName() + "\n" + device.getAddress());
	            
	            //qua ho il codice del client con cui avviene il collegamento
	            //PAIRING?
	        }
	    }
	};*/
	/*
	public void addBTReceiver(){
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		actv.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}*/
	
	//setup the Bluetooth Device for listening on an RFCOMM channel
	private void setup() {
		btUtil = new BtUtility(this,ctx);
		myBT = btUtil.findMyBT(myBT);
		btUtil.enableMyBT(myBT); //se non mi dice di si? come mi comporto?
    	//this.addBTReceiver(); // TODO SERVE!??! rivedere meglio questo prima di pubblicare
    	btUtil.setVisibility(sleepTime);
    	btUtil.beVisible();
    	running = true;
    	t = new Thread(this);
    	t.start();
	}
	
	public boolean isServiceRunning(){
		return running;
	}
	
	public void stopMe() {
		running = false;
	}
	

    /*CORE*/
    private void listenForConnection() {
		ExchangeInfo newExc = new ExchangeInfo();
		logIt("establish new connection " + this.serviceName);

		Random t = new Random();
    	try{
			BluetoothServerSocket bss = myBT.listenUsingRfcommWithServiceRecord(this.serviceName, BtConstants.btUUID);
			bs = bss.accept();
			bss.close();
			out = bs.getOutputStream();
			
			while(running) {
				int itcanchange = t.nextInt();
				newExc.setConnectionType(Connectivity.ConnectionStatus(this.ctx)); //get from system.. the data..
				newExc.setNotificationCount(itcanchange); //randomdata..just for have a different info everytime
				//Connectivity.SetConnectionStatus(this.ctx, newExc);
				
				if(exc.isEqual(newExc)) {
					logIt("Nothing changed.. newExc not updated.");
				} else {	
					logIt("Connection: "+newExc.getConnectionType()+" Count "+newExc.getNotificationCount());
					exc.setByAnother(newExc);
					out.write(exc.toPacket().getBytes()); //something is changed.. send it!
				}
				
				try{
					logIt("I'm tired... I sleep a bit.. don't bother me." + sleepTime);
					Thread.sleep(sleepTime);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			out.write("goodbye".getBytes()); //if you are closing the program..
			out.close();
			bs.close();
			logIt("close");
			//this.appendMe("Finish.");
			
		} catch (IOException e) {
			logIt("Error: I can't listen for new connection."); 
			e.printStackTrace();
		}
    }

    
    private void logIt(String msg){
    	/*if(tview != null)
    		tview.append(msg);
    	else*/
    	Log.i("BTINFO",msg);
    }
    
	@Override
	public void run() {
		listenForConnection();
	}
	
  /*  private void appendMe(String toAppend) {
    	Log.i("BTMSG",toAppend+ "\n");
    }*/
    
    
}