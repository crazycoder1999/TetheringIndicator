package com.pestohacks.crazycoder1999;

import com.pestohacks.crazycoder1999.connectioncheck.Connectivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//All the code is GPL 3.0 and comes from http://pestohacks.blogspot.com
//Made by crazycoder1999

// TODO uncouple the exit of the activity (
// TODO block the rotation of the activity.
public class TetheringIndicatorActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	TextView tview;
	final static int sleepTime = 3000;
	Button startMe,closeAll;
    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tview = (TextView)findViewById(R.id.editTextLog);
        startMe = (Button)findViewById(R.id.startBtn);
        startMe.setOnClickListener(this);
        closeAll = (Button)findViewById(R.id.closeBtn);
        closeAll.setOnClickListener(this);
        printIt(Connectivity.ConnectionStatus(this));
    }
    
	@Override
	public void onClick(View v) {
		Button btn = (Button)v;
		if(btn.getText().equals("Start Me")) {
			//send message to service for starting bluetooth.
			startMe.setText("Stop Me");
			askToStartBtService();
			printIt("Started");
		} else if(btn.getText().equals("Stop Me")) {
			//send message to service for starting bluetooth.
			startMe.setText("Start Me");
			
			askToStopBtService();
			printIt("Stopped.");
		} else if(btn.getText().equals("Close All")) {
			printIt("Services closed..");
			doStopService();
		}
	}
	
	//a methoed for simplify events tracking.
	private void printIt(String msg){
		if(tview != null)
			tview.append(msg+"\n");
		else
			Log.i("BTINFO","msg");
	}
	
	
    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BlueServices.MSG_KEEP_ALIVE:
                    tview.setText("KeepAlive received: " + msg.arg1);
                    break;
                case BlueServices.MSG_BT_START:
                	printIt("Service started");
                	break;
                case BlueServices.MSG_BT_STOP:
                	printIt("Service stopped.");
                	break;
                case BlueServices.MSG_BT_STATUS:
                	printIt("Hey a bt status!");
                	break;
                default:
                	tview.append("Unhandled message");
                    super.handleMessage(msg);
            }
        }
    }
	
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            printIt("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        BlueServices.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                //msg = Message.obtain(null,      BlueServices.MSG_BT_S, this.hashCode(), 0);
               // mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(TetheringIndicatorActivity.this, "Collegato",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            printIt("Activity Disconnessa.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(TetheringIndicatorActivity.this, "non connesso..",
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
    	Intent t = new Intent(TetheringIndicatorActivity.this, 
                BlueServices.class);
    	startService(t);
        bindService(t, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        printIt("Binding.");
    }
    
    void doStopService(){
    	Intent t = new Intent(TetheringIndicatorActivity.this, 
                BlueServices.class);
    	 if (mIsBound) {
             // If we have received the service, and hence registered with
             // it, then now is the time to unregister.
             if (mService != null) {
                 try {
                     Message msg = Message.obtain(null,
                     		BlueServices.MSG_STOP_ME);
                     msg.replyTo = mMessenger;
                     mService.send(msg);
                 } catch (RemoteException e) {
                     // There is nothing special we need to do if the service
                     // has crashed.
                 }
             }

             // Detach our existing connection.
             unbindService(mConnection);
             mIsBound = false;
             printIt("Unbinding.");
         }
    	stopService(t);
    	
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                    		BlueServices.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            printIt("Unbinding.");
        }
    }


    void askToStartBtService() {
    	if(mIsBound) {
    		if(mService != null) {
    			  try {
                      Message msg = Message.obtain(null,
                      		BlueServices.MSG_BT_START);
                      msg.replyTo = mMessenger;
                      mService.send(msg);
                  } catch (RemoteException e) {
                      // There is nothing special we need to do if the service
                      // has crashed.
                  }
    		}
    	}
    }
    
    void askToStopBtService() {
    	if(mIsBound) {
    		if(mService != null) {
    			  try {
                      Message msg = Message.obtain(null,
                      		BlueServices.MSG_BT_STOP);
                      msg.replyTo = mMessenger;
                      mService.send(msg);
                  } catch (RemoteException e) {
                      // There is nothing special we need to do if the service
                      // has crashed.
                  }
    		}
    	}
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		this.doBindService();
		super.onStart();
	}

	@Override
	protected void onStop() {

		doUnbindService();
		super.onStop();
	}
	
	
}