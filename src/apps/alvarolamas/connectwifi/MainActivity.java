package apps.alvarolamas.connectwifi;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener 
{
	private static final String TAG = "MainActivity";
	
	private static final String SSID = "\"IANUA2\"";
	private static final String PASSWORD = "\"0123456789022\"";	
	private static final String DIT_SSID = "\"DIT\"";
	
	private Button activity_button;
	private Button reset_button;
	private Button scan_button;
	private TextView ssid;
	private TextView password;
	private TextView gateway;
	private TextView ip_address;
	private TextView connecting;
	private ProgressBar progBar;
	
	
	private WifiManager wifiManager;
	private ConnectivityManager conMan;
	private BroadcastReceiver receiver;
	
	private boolean networkConfigured;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		Log.d(TAG,"*****     onCreate()     *****");
		
		activity_button = (Button) findViewById(R.id.activity_button);
		activity_button.setOnClickListener(this);
		reset_button = (Button) findViewById(R.id.reset_button);
		reset_button.setOnClickListener(this);
		scan_button = (Button) findViewById(R.id.scan_button);
		scan_button.setOnClickListener(this);
		
		ssid = (TextView) findViewById(R.id.ssid);
		password = (TextView) findViewById(R.id.password);
		gateway = (TextView) findViewById(R.id.gateway);
		ip_address = (TextView) findViewById(R.id.ip);
		connecting = (TextView) findViewById(R.id.connecting);
		progBar = (ProgressBar) findViewById(R.id.progbar);
		
		networkConfigured = false;
		
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//receiver to get notified when connected to a WiFi Network
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {			
				
				String action = intent.getAction();
				Log.d(TAG,"Received: " + action);
				
				if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
				{
					Log.i(TAG,conMan.getNetworkInfo(1).toString());
					
					if(conMan.getNetworkInfo(1).getState().equals(NetworkInfo.State.CONNECTED))
					{
						progBar.setVisibility(View.INVISIBLE);
						connecting.setVisibility(View.INVISIBLE);
						
						DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
						//gateway.setText(dhcpInfo.gateway);
						Log.d(TAG,"Gateway: " + StringUtils.int_to_ip(dhcpInfo.gateway));
						Log.d(TAG,"IP: "+ StringUtils.int_to_ip(dhcpInfo.ipAddress));
						//ip_address.setText(dhcpInfo.ipAddress);
						gateway.setText(StringUtils.int_to_ip(dhcpInfo.gateway));
						ip_address.setText(StringUtils.int_to_ip(dhcpInfo.ipAddress));
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(receiver, filter);
	}

	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		unregisterReceiver(receiver);
	}
	
	
	@Override
	public void onClick(View v) 
	{
		
		List <WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		
		if(v.equals(activity_button))
		{
			Log.d(TAG,"activity_button onClick()");
			if(!networkConfigured)
			{
				Log.d(TAG,"Configuring " + SSID);
				WifiConfiguration conf = new WifiConfiguration();
				conf.SSID = SSID;
				conf.preSharedKey = PASSWORD;
				conf.status = WifiConfiguration.Status.ENABLED;
				conf.priority = 26;
				conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//				conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//				conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				
				int resAdd = wifiManager.addNetwork(conf);
				Log.d(TAG,"addNetwork result: " + resAdd); //Return networkId
				
				networkConfigured = true;
				
				activity_button.setText(R.string.connect);
				
				ssid.setText(conf.SSID);
				password.setText(conf.preSharedKey);
			}
			else
			{
				Log.d(TAG,"Connecting to " + SSID);	
				
				if(!wifiManager.isWifiEnabled())
				{
					Toast.makeText(this, "Enable WiFi first", Toast.LENGTH_LONG).show();
					return;
				}
				
				Log.i(TAG,"wifiManager.getConnectionInfo(): " + wifiManager.getConnectionInfo().toString());
				Log.i(TAG,"connectivityMannager.getNetworkIngo(): " + conMan.getNetworkInfo(1).toString());
				
				//Check which WiFi network the device is connected to
				Log.d(TAG,"Device connected to SSID: " + wifiManager.getConnectionInfo().getSSID());
				
				// If not connected to MTX WiFi..
				if(wifiManager.getConnectionInfo().getSSID().equals(SSID) &&
					conMan.getNetworkInfo(1).getState().equals(NetworkInfo.State.CONNECTED))
				{
					Log.d(TAG,"Already connected to " + SSID);
					Toast.makeText(this, "Already connected to " + SSID, Toast.LENGTH_LONG).show();					
					//Immediatilly set gateway and ip_address
				}
				else
				{
					progBar.setVisibility(View.VISIBLE);
					connecting.setVisibility(View.VISIBLE);
					gateway.setText("...");
					ip_address.setText("...");
					
					// Get the list of configured WiFi networks
					for( WifiConfiguration wificonf : configuredNetworks ) 
					{
						Log.i(TAG, wificonf.SSID);
					    if(wificonf.SSID.equals(SSID)) 
						{
					    	//Disconnect-reconnect? Is what we should do?
					    	wifiManager.disconnect();
					    	
					    	//wifiManager.enableNetwork(int networkId, boolean disableOthers);
					        boolean resEn = wifiManager.enableNetwork(wificonf.networkId, true);
					        //Wouldn't it be better if disableOthers = false?				        
					        Log.d(TAG, "Enable network returned: " + resEn);	
					        
					        //boolean resSav = wifiManager.saveConfiguration();
					        //Log.d(TAG, "Save Configuration returned: " + resSav);
					        
					        wifiManager.reconnect();     
					        break;
					    }           
					}
				}
			}
		}
		else if(v.equals(reset_button))
		{
			Log.d(TAG,"reset_button onClick()");
			for(WifiConfiguration conf : configuredNetworks)
			{
				Log.i(TAG,conf.SSID);
				if(conf.SSID.equals(SSID))
				{
					if(wifiManager.getConnectionInfo().getSSID().equals(SSID))
					{
						Log.w(TAG,"Connected to: "+ SSID + ". Disconnecting...");
						wifiManager.disconnect();
					}
					boolean resRem = wifiManager.removeNetwork(conf.networkId);
					Log.d(TAG,"removeNetwork result: " + resRem);
					if(resRem)
					{
						ssid.setText(R.string.no_configuration);
						password.setText(R.string.no_configuration);
						
						networkConfigured = false;
						
						activity_button.setText(R.string.configure);
						progBar.setVisibility(View.INVISIBLE);
						connecting.setVisibility(View.INVISIBLE);
						
						break;
					}
				}
			}
			if(conMan.getNetworkInfo(1).getState().equals(NetworkInfo.State.DISCONNECTED))
			{	
				Log.w(TAG,"Network state DISCONNECTED. Reconnecting...");
				List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
				for( WifiConfiguration wificonf : list ) 
				{
					boolean resEn = wifiManager.enableNetwork(wificonf.networkId, false);
					Log.d(TAG,"enableNetwork result: " + resEn);
				}
				wifiManager.reconnect();
			}
		}
		else if(v.equals(scan_button))
		{
			Log.d(TAG,"scan_button onClick()");
			for(WifiConfiguration wificonf : configuredNetworks)
			{
				if(wificonf.SSID.equals(SSID))
				{
					//Configure getters
					Log.w(TAG,SSID + "WiFi Configuration");
					Log.w(TAG,wificonf.toString());
					
					Log.i(TAG,"SSID: "+wificonf.SSID);
					Log.i(TAG,"presharedkey: "+wificonf.preSharedKey);
					Log.i(TAG,"priority: "+Integer.toString(wificonf.priority));
					Log.i(TAG,"allowAuthAlgorithms: "+wificonf.allowedAuthAlgorithms.toString());
					Log.i(TAG,"allowedGroupCiphers: "+wificonf.allowedGroupCiphers.toString());
					Log.i(TAG,"allowedKeyManagement: "+wificonf.allowedKeyManagement.toString());
					Log.i(TAG,"sllowPairwiseCiphers: "+wificonf.allowedPairwiseCiphers.toString());
					Log.i(TAG,"allowedPrococols: "+wificonf.allowedProtocols.toString());
					Log.i(TAG,"hiddenSSID: "+Boolean.toString(wificonf.hiddenSSID));
					Log.i(TAG,"BSSID: "+wificonf.BSSID);
					Log.i(TAG,"status: "+Integer.toString(wificonf.status));
				}
				else if (wificonf.SSID.equals(DIT_SSID))
				{
					Log.v(TAG,"DIT info");
					Log.v(TAG, wificonf.toString());
				}
			}
		}
	}
}

