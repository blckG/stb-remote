package fr.enseirb.odroidx.remote_client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import fr.enseirb.odroidx.remote_client.UI.IPAddressKeyListener;
import fr.enseirb.odroidx.remote_client.communication.Commands;
import fr.enseirb.odroidx.remote_client.communication.STBCommunication;

public class MainActivity extends Activity implements OnClickListener {

	private static final String PREFS_NAME = "IPSTORAGE";
	private static final String TAG = "MainActivity";
    private boolean isConnectedToSTB = false;
    private STBCommunication STBCom = null;
    private static final int COMMUNICATION_PORT = 2000;
    
    private EditText edIP;
	private LinearLayout buttons_layout;
	private ImageView button_connect;
	private ImageView button_play;
	private ImageView button_pause;
	private ImageView button_rewind;
	private ImageView button_forward;
	private ImageView button_previous;
	private ImageView button_next;
	private ImageView button_up;
	private ImageView button_down;
	private ImageView button_right;
	private ImageView button_left;
	private ImageView button_select;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
	    // loading view components
	    buttons_layout = (LinearLayout) findViewById(R.id.buttons_layout);
		edIP = (EditText) findViewById(R.id.EditTextIp);
	    button_connect = (ImageView) findViewById(R.id.button_connect);
	    button_play = (ImageView) findViewById(R.id.button_play);
	    button_pause = (ImageView) findViewById(R.id.button_pause);
	    button_rewind = (ImageView) findViewById(R.id.button_rewind);
	    button_forward = (ImageView) findViewById(R.id.button_forward);
	    button_previous = (ImageView) findViewById(R.id.button_previous);
	    button_next = (ImageView) findViewById(R.id.button_next);
	    button_up = (ImageView) findViewById(R.id.button_up);
	    button_down = (ImageView) findViewById(R.id.button_down);
	    button_left = (ImageView) findViewById(R.id.button_left);
	    button_right = (ImageView) findViewById(R.id.button_right);
	    button_select = (ImageView) findViewById(R.id.button_select);
	    
	    // setting listenners
		button_connect.setOnClickListener(this); 
	    button_play.setOnClickListener(this);
		button_pause.setOnClickListener(this);
		button_rewind.setOnClickListener(this);
		button_forward.setOnClickListener(this);
		button_previous.setOnClickListener(this);
		button_next.setOnClickListener(this);
		button_up.setOnClickListener(this);
		button_down.setOnClickListener(this);
		button_left.setOnClickListener(this);
		button_right.setOnClickListener(this);	
		button_select.setOnClickListener(this);
		
		// load Previous IP used
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    String ip = settings.getString("ip", "192.168.0.1");
	    edIP.setText(ip);
	    edIP.setKeyListener(IPAddressKeyListener.getInstance());
	    
	    // hide buttons while not connected
	    buttons_layout.setVisibility(View.GONE);
	    
	    // initialize STBCom
	    STBCom = new STBCommunication();
	}
	
    @Override
    protected void onStop(){
    	super.onStop();

    	// save IP field for future executions
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("ip", edIP.getText().toString());
    	editor.commit();
    }

	@Override
	public void onClick(View v) {
		Log.v(TAG, "click event on a button");
		v.setBackgroundResource(R.color.blue_light);
		
		if(v==button_play) sendMessageToSTB(Commands.VIDEO_PLAY);
		else if(v==button_pause) sendMessageToSTB(Commands.VIDEO_PAUSE);
		else if(v==button_forward) sendMessageToSTB(Commands.VIDEO_FORWARD);
		else if(v==button_rewind) sendMessageToSTB(Commands.VIDEO_REWIND);
		else if(v==button_previous) sendMessageToSTB(Commands.VIDEO_PREVIOUS);
		else if(v==button_next) sendMessageToSTB(Commands.VIDEO_NEXT);
		else if(v==button_up) sendMessageToSTB(Commands.MOVE_UP);
		else if(v==button_down) sendMessageToSTB(Commands.MOVE_DOWN);
		else if(v==button_left) sendMessageToSTB(Commands.MOVE_LEFT);
		else if(v==button_right) sendMessageToSTB(Commands.MOVE_RIGHT);
		else if(v==button_select) sendMessageToSTB(Commands.SELECT);
	    else if(v==button_connect) {
			if (! isConnectedToSTB) {
				connectToTheSTB();
			}
			else {
				disconnectFromTheSTB();
			}
		}
	}
	
	private void connectToTheSTB() {
		new sendMessageToSTBAsyncTask(this).execute("connect");
	}
	
	private void disconnectFromTheSTB() {
		new sendMessageToSTBAsyncTask(this).execute("disconnect");
	}
	
	private void sendMessageToSTB(String msg) {
		new sendMessageToSTBAsyncTask(this).execute("command", msg);
	}
	
	private class sendMessageToSTBAsyncTask extends AsyncTask<String, String, Integer> {

		private MainActivity mParentActivity = null;
		
		
	    public sendMessageToSTBAsyncTask(MainActivity parentActivity) {
	        mParentActivity = parentActivity;
	    }
		
		@Override
		protected Integer doInBackground(String... params) {
			
			boolean success;
			String type = params[0];
			
			if (type.equals("connect")) {
				String ip = mParentActivity.edIP.getText().toString();
				success = mParentActivity.STBCom.stb_connect(ip, COMMUNICATION_PORT);
				
				if (! success) {
					publishProgress("Cannot connect to the STB (check your WiFi, IP, network configuration...)");
				} else {
					publishProgress("Connected to the STB", "connected");
				}
			}
			else if (type.equals("disconnect")) {
				success = mParentActivity.STBCom.stb_disconnect();
				
				if (! success) {
					publishProgress("Cannot disconnect from the STB");
				}
				else {
					publishProgress("Disconnected from the STB", "disconnected");
				}
			}
			else if (type.equals("command")) {
				String cmd = params[1];
				success =  mParentActivity.STBCom.stb_send(cmd);
				
				if (! success) publishProgress("Error while sending the command to the STB");
			}
			
			return null;
		}
		
		protected void onProgressUpdate(String... params) {
			
			Toast.makeText(mParentActivity.getApplicationContext(), params[0], 5).show();
			
			if (params.length == 2) {
				if (params[1].equals("connected")) {
					mParentActivity.buttons_layout.setVisibility(View.VISIBLE);
					mParentActivity.isConnectedToSTB = true;
				} else {
					mParentActivity.buttons_layout.setVisibility(View.GONE);
					mParentActivity.isConnectedToSTB = false;
				}
			}
	    }
		
		protected void onPostExecute(Integer result) {
			
			mParentActivity.button_play.setBackgroundResource(R.color.black);
			mParentActivity.button_pause.setBackgroundResource(R.color.black);
			mParentActivity.button_forward.setBackgroundResource(R.color.black);
			mParentActivity.button_rewind.setBackgroundResource(R.color.black);
			mParentActivity.button_previous.setBackgroundResource(R.color.black);
			mParentActivity.button_next.setBackgroundResource(R.color.black);
			mParentActivity.button_up.setBackgroundResource(R.color.black);
			mParentActivity.button_down.setBackgroundResource(R.color.black);
			mParentActivity.button_left.setBackgroundResource(R.color.black);
			mParentActivity.button_right.setBackgroundResource(R.color.black);
			mParentActivity.button_select.setBackgroundResource(R.color.black);
			mParentActivity.button_connect.setBackgroundResource(R.color.black);
		}
	}
}