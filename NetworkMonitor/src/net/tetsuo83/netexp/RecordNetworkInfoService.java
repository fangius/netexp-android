package net.tetsuo83.netexp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.tetsuo83.netexp.console.ReadCommandThread;
import net.tetsuo83.nrlexpupload.NrlExpUploadService;
import net.tetsuo83.nrlexpupload.UploadEntry;
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
	
	private static final String INIT = System.nanoTime() +"";
	
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

	FileWriter configFw;
	BufferedWriter configBfw;
	File configfile;
	String configFileName="netconf"+ INIT +".txt";
	
	
	FileOutputStream ipConfFw;
	BufferedOutputStream ipConfBfw;
	File ipConffile;
	String ipConfFileName="ipConfig"+ INIT +".txt";
	
	FileOutputStream netstatFw;
	BufferedOutputStream netstatBfw;
	File netstatfile;
	String netstatFileName="netstat"+ INIT +".txt";
	
	BroadcastReceiver mBatteryReceiver;
	BroadcastReceiver cWifiReceiver;
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
		 configfile= new File(Environment.getExternalStorageDirectory(),configFileName);
		 ipConffile= new File(Environment.getExternalStorageDirectory(), ipConfFileName);
		 netstatfile= new File(Environment.getExternalStorageDirectory(), netstatFileName);
		 
		 try{
			 configFw=new FileWriter(configfile,true);	
			 configBfw = new BufferedWriter(configFw,4096);
			 
			 ipConfFw= new FileOutputStream(ipConffile);
			 ipConfBfw= new BufferedOutputStream(ipConfFw);
			 
			 netstatFw= new FileOutputStream(netstatfile);
			 netstatBfw= new BufferedOutputStream(ipConfFw);
			 
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
					writeLogLine(false,true);
				}
			};
		 registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		 cWifiReceiver =  new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					writeLogLine(true,false);
				}
			};
		 registerReceiver(cWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		 
		 applicationStart = System.nanoTime();
		 applicationEnd = applicationStart + DURATION;
	}
	 
	@Override
	 public int onStartCommand(Intent intent, int flags, int startId) { 
		writeLogLine(false,false);
		
		return START_STICKY;
	}
	
	private void writeLogLine(boolean connectivityChange, boolean batteryChange)
	{
		 info = wifi.getConnectionInfo();	
		 d_info=wifi.getDhcpInfo();
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 ni=cm.getActiveNetworkInfo();
		 long now = System.nanoTime();
		 boolean terminating = applicationEnd < now ;
		 ReadCommandThread ipThread = new ReadCommandThread(ipConfBfw, new String[] {"netcfg"}, 0, 1, now+"");
		 ReadCommandThread netstatThread = new ReadCommandThread(netstatBfw, new String[] {"netstat"}, 0, 1, now+"");
		 
		 
		 String o_string="\n"
				 + now
				 + "\t" + mCurrentBatteryLevel
				 + "\t" + "\"" + info.toString() + "\""
				 + "\t" + "\"" + ni.toString() + "\""
				 + "\t" + intToIP(d_info.ipAddress)
				 + "\t" + intToIP(d_info.serverAddress)
				 + "\t" + intToIP(d_info.gateway)
		 		+ "\t" + intToIP(d_info.leaseDuration)
		 		+ "\t" + intToIP(d_info.netmask)
		 		+ "\t" + intToIP(d_info.dns1)
		 		+ "\t" + intToIP(d_info.dns1)
				 + "\t" +TrafficStats.getTotalRxBytes()
				 + "\t" +TrafficStats.getTotalTxBytes()
				 + "\t" +TrafficStats.getTotalRxPackets()
				 + "\t" +TrafficStats.getTotalTxPackets()
				 + "\t" +TrafficStats.getMobileRxBytes()
		 		 + "\t" + connectivityChange
		 		 + "\t" + batteryChange;

	    	try{
	    			ipThread.start();
	   		 	netstatThread.start();
	    			Log.i(RecordNetworkInfoService.class.toString(), "Logging Network Status");
				configBfw.append(o_string);
				if (lastFlush < 0 || lastFlush < System.nanoTime() - FLUSH_FREQ || terminating) 
				{
					
					configBfw.flush();
					ipConfBfw.flush();
					netstatBfw.flush();
					lastFlush = System.nanoTime();
					Log.i("Network info", "FLUSHING: " + configFileName);
					
					if (terminating)
					{
						removeIntents();
						ipThread.join();
						netstatThread.join();
						 configBfw.flush();
						 ipConfBfw.flush();
						 netstatBfw.flush();
						 configBfw.close();
						 ipConfBfw.close();
						netstatBfw.close();
						terminated = true;
						sendData();
						this.stopSelf();
					}
				}
			} catch(IOException e)
			{	
				Log.e("Network Info", "Error while closing log files: " + e.getMessage());
			} catch(InterruptedException e)
			{
				Log.e("Network Info", "Error while waiting for thread to end");
			}
			
		 
	 }	 
		
	private void sendData() {
		
		
		Intent intent=new Intent(this,NrlExpUploadService.class);
		intent.putExtra(UploadEntry.DELETE_AFTER,false);
		intent.putExtra(UploadEntry.FILE,configfile.getAbsolutePath());
		intent.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent.putExtra(UploadEntry.ONLY_WIFI,false);
		intent.putExtra(UploadEntry.NAME,configFileName);
		
		Intent intent2=new Intent(this,NrlExpUploadService.class);
		intent2.putExtra(UploadEntry.DELETE_AFTER,false);
		intent2.putExtra(UploadEntry.FILE, ipConffile.getAbsolutePath());
		intent2.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent2.putExtra(UploadEntry.ONLY_WIFI,false);
		intent2.putExtra(UploadEntry.NAME,ipConfFileName);
		
		Intent intent3=new Intent(this,NrlExpUploadService.class);
		intent3.putExtra(UploadEntry.DELETE_AFTER,false);
		intent3.putExtra(UploadEntry.FILE, netstatfile.getAbsolutePath());
		intent3.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent3.putExtra(UploadEntry.ONLY_WIFI,false);
		intent3.putExtra(UploadEntry.NAME,netstatFileName);
		startService(intent);
		startService(intent2);
		startService(intent3);
	}

	public void removeIntents()
	{
		unregisterReceiver(mBatteryReceiver);
		unregisterReceiver(cWifiReceiver);
		mgr.cancel(pi);
	}
	
}
