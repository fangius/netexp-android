package net.tetsuo83.netexp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class RecordNetworkInfoService extends Service {
	
	private static final long DURATION = 60*2*(1000000000L);
	private static final long FLUSH_FREQ = 60000000000L;
	private static final int CLOCK_TIMER=5000;
	private static final String filename="networkInfo";
	
	NotificationManager mNM;
    RunningServices rs;
	AlarmManager mgr;
	PendingIntent pi;
	ActivityManager am;
	
	NetworkInfo ni;
	
	WifiInfo info;
	DhcpInfo d_info;
	WifiManager wifi;
	ConnectivityManager cm;

	FileWriter fw;
	BufferedWriter bfw;
	File file;
	String file_name=filename+getDateTime()+".txt";
	
	BroadcastReceiver mBatteryReceiver;
	
	int mCurrentBatteryLevel;
	long applicationStart;
	long applicationEnd;
	long lastFlush;
	
	boolean terminated;
    
    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy.HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	 public String intToIP(int i) 
	 {
		    return ( i & 0xFF) + "." +
		        (( i >> 8 ) & 0xFF) + "." +
		        (( i >> 16 ) & 0xFF) + "." +
		        (( i >> 24 ) & 0xFF);
	}
	
	 @Override
	    public void onCreate() {
	     initIntents();
		 file=new File(Environment.getExternalStorageDirectory(),file_name);
		 try{
			 fw=new FileWriter(file,true);			 
			 bfw = new BufferedWriter(fw,4096);
		 }
		 catch(IOException e){
			 Log.i("FileWriter error",e.getMessage());
		 }
		 mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), CLOCK_TIMER, pi);
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 wifi = (WifiManager) getSystemService(WIFI_SERVICE);
	 }
	 
	 
	 private void initIntents() {
		 
		 mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		 mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		 Intent i=new Intent(this, RecordNetworkInfoService.class);

		 pi=PendingIntent.getService(this, 0, i, 0);
		 
		 mBatteryReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mCurrentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);	
				}
			};
		 registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		 applicationStart = System.nanoTime();
		 applicationEnd = applicationStart + DURATION;
	}
	 
	@Override
	 public int onStartCommand(Intent intent, int flags, int startId) { 
		 info = wifi.getConnectionInfo();	
		 d_info=wifi.getDhcpInfo();
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 ni=cm.getActiveNetworkInfo();
		 long now = System.nanoTime();
		 boolean terminating = applicationEnd < now ;
		 
		 String o_string="\n"
				 + now
				 + "\t" + mCurrentBatteryLevel
				 + "\t" + "\"" + info.toString() + "\""
				 + "\t" + "\"" + ni.toString() + "\""
				 + "\t" +TrafficStats.getTotalRxBytes()
				 + "\t" +TrafficStats.getTotalTxBytes()
				 + "\t" +TrafficStats.getTotalRxPackets()
				 + "\t" +TrafficStats.getTotalTxPackets()
				 + "\t" +TrafficStats.getMobileRxBytes();
//				 + "\t" + info.getNetworkId()
//		 		 + "\t" + info.getBSSID()	 
//		 		 + "\t" + info.getSSID()
//		 		 + "\t" + info.getHiddenSSID()
//		 		 + "\t" + info.getRssi()
//		 		 + "\t" + info.getMacAddress()
//		 		 + "\t" + info.getLinkSpeed();

	    	try{
				bfw.append(o_string);
				if (lastFlush < 0 || lastFlush < System.nanoTime() - FLUSH_FREQ || terminating) 
				{
					
					bfw.flush();
					lastFlush = System.nanoTime();
					Log.i("Network info", "FLUSHING: " + file_name);
					
					if (terminating)
					{
						bfw.close();
						terminated = true;
						removeIntents();
					}
				}
				

			}
			catch(IOException e){	
			}
			Log.i("Network Info",o_string);
		 return START_STICKY;
	 }	 
		
	public void removeIntents()
	{
		unregisterReceiver(mBatteryReceiver);
		mgr.cancel(pi);
	}
	
	 @Override
	 public void onDestroy() {
		 try{
			 removeIntents();
			 bfw.flush();
			 bfw.close();
		 }
		 catch(IOException e){
			 Log.i("message",e.getMessage());
		 }
	 Log.i("msg","Alarm stopped");
	 mgr.cancel(pi);
	 }
	
}
