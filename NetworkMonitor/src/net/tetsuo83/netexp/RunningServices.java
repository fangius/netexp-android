package net.tetsuo83.netexp;

import java.util.List;

import com.ms.networkmonitor.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

public class RunningServices extends Activity {
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_services);
        startMonitoring();
		}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void startMonitoring()
	{
    		networkStats();
	}
	public String intToIP(int i) {
	    return ( i & 0xFF) + "." +
	        (( i >> 8 ) & 0xFF) + "." +
	        (( i >> 16 ) & 0xFF) + "." +
	        (( i >> 24 ) & 0xFF);
	}
	public void networkStats(){
    	TextView textView=(TextView)findViewById(R.id.display_message);
    	ConnectivityManager cm=(ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);
		NetworkInfo ni=cm.getActiveNetworkInfo();
		String display_string="\n Type: "+ni.getTypeName()+"\n NetworkInfo: "+ni.toString()+"\n";
	    
		WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE); 
		DhcpInfo info = wifi.getDhcpInfo();
		display_string+="\n DNS info: "+info.toString();
		display_string+="\n Current IP address: "+intToIP(info.ipAddress);
		
		List<RunningAppProcessInfo> list_running_process=getRunningProcesses();
	    display_string+="\nTotal Rx Bytes"+TrafficStats.getTotalRxBytes()/1024/1024+"MB \n Total Tx Bytes"+TrafficStats.getTotalTxBytes()/1024/1024+" MB";
	    for(int i=0;i<list_running_process.size();i++){
	    		display_string+="\n "+list_running_process.get(i).processName+"\nProcess No."+i+""+list_running_process.get(i).uid+"TrafficInfo"+TrafficStats.getUidRxPackets(list_running_process.get(i).uid)+"\n";
	    }
	    
	    textView.setMovementMethod(new ScrollingMovementMethod());	
	    textView.setText(display_string);
	}
	 protected List<RunningAppProcessInfo> getRunningProcesses(){
	    	ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);;
	    	List<RunningAppProcessInfo> process_list=am.getRunningAppProcesses();
	    	return process_list;
	    }
}
