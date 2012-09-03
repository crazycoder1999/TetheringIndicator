package com.pestohacks.crazycoder1999;

import java.util.ArrayList;

import com.pestohacks.bluetooth.BluetoothListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

//TODO Flag Activity new task.
//TODO I should limit the number of connected client to 1.
//TODO How to stop a service and call destroy?
public class BlueServices extends Service implements Runnable {
	String TAGSERVICE = "TETHERINGINDICATOR.SERVICE";
	Thread t = new Thread(this);
	int count = 0;
	/** For showing and hiding our notification. */
    NotificationManager mNM;
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    boolean started = false;
	BluetoothListener btListener;
	Context ctx;

    static enum BTCONNECTION {NOTYET,CONNECTED,NOTCONNECTED};

    BTCONNECTION connectionState = BTCONNECTION.NOTYET;
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;
    

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    static final int MSG_KEEP_ALIVE = 3;
    
    /**
     * Start the Bluetooth setup if there is no connection established.
     */
    static final int MSG_BT_START = 4;
    
    /*
     * Stop the Bluetooth setup if there is no connection established. 
     */
    static final int MSG_BT_STOP = 5;
    
    /*
     * Get Bluetooth Status: Connected / Not Connected.
     */
    static final int MSG_BT_STATUS = 6;
    
    /*
     * Stop This Services.
     */
    static final int MSG_STOP_ME = 7;    

    int remote_service_started = 30;
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
    	this.ctx = this.getApplicationContext();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        showNotification();
    }
    
    private void engageNewThread(){
    	t = null;
    	t = new Thread(this);
    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                	Log.d(TAGSERVICE,"Registered CLient");
                    if( ! started ) {
                    	engageNewThread();
                    	t.start(); //start the tread
                    	started = true;
            		}	
            		mClients.add(msg.replyTo);
                    break;
                case MSG_STOP_ME:
                    mNM.cancel(remote_service_started);
                case MSG_UNREGISTER_CLIENT:
                	Log.d(TAGSERVICE,"Client removed");
                    mClients.remove(msg.replyTo);
                    started = false;
                    break;
                case MSG_BT_STATUS: 
                	sendBTStatus();
                	break;
                case MSG_BT_START:
                	if(btListener == null) {
                		//btListener = null;
                		btListener = new BluetoothListener(ctx,"TETHERINGINDICATOR");
                		btListener.checkConnectionOnEvery(5000);
                		Log.d(TAGSERVICE,"Check Every 5000. Bluetooth Started");
                		connectionState = BTCONNECTION.CONNECTED;
                	}
                	sendBTStatus();
                	break;
                case MSG_BT_STOP:
                	if(btListener!=null && btListener.isServiceRunning()){
                		connectionState = BTCONNECTION.NOTCONNECTED;
        				btListener.stopMe();
                	}
                	sendBTStatus();
                	break;
                case MSG_KEEP_ALIVE:
                   // mValue = msg.arg1; //value received from activity...
                    sendKeepAlive();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendBTStatus() {
    	try{
    		mClients.get(0).send(Message.obtain(null,MSG_BT_STATUS,connectionState.ordinal(),0 ));
    	} catch(RemoteException e) {
    		Log.d(TAGSERVICE,"Failed to send btstatus..");
    	}
    }
    
    public void sendKeepAlive() {
    	if(mClients.size()==0)
    		return;
    	
    	if(mClients.size()>1) {
    		Log.d(TAGSERVICE, "Something was wrong...");
    		return;
    	}
    	
        try {
            mClients.get(0).send(Message.obtain(null,
                    MSG_KEEP_ALIVE, count, 0));
            Log.d(TAGSERVICE, "Msg Sent...");
        } catch (RemoteException e) {
        	Log.d(TAGSERVICE, "is Client 0 connected?..Exception");
            // The client is dead.  Remove it from the list;
            // we are going through the list from back to front
            // so this is safe to do inside the loop.
            mClients.remove(0);
        }
    }

    @Override
    public void onDestroy() {
		// Cancel the persistent notification.
        //mNM.cancel(remote_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, "Remote Service Stopped", Toast.LENGTH_LONG).show();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
    	Log.d(TAGSERVICE,"START NOTIFICATION");
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Service is running!";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TetheringIndicatorActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "TetheringIndicator",
                       text, contentIntent);

        // Send the notification.
        mNM.notify(remote_service_started, notification);
    }

	@Override
	public void run() {
		try{
			while(started) {
				Log.d(TAGSERVICE, "now --> " + count);
				Thread.sleep(5000);
				count ++;
				sendKeepAlive();
			}
		} catch(Exception exc) {
		}
	}
	
	

}
