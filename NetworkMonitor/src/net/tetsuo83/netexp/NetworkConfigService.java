package net.tetsuo83.netexp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class NetworkConfigService extends Service {
	NotificationManager mNM;
    RunningServices rs;
	AlarmManager mgr;
	PendingIntent pi;
	ActivityManager am;
	int count;
	List<RunningAppProcessInfo> list;
	ConnectivityManager cm;
	NetworkInfo ni;
	WifiManager wifi; 
	DhcpInfo info;
	List<RunningAppProcessInfo> list_running_process;
	int CLOCK_TIMER=5000;
	FileWriter fw;
	File file;
	Date date;
	SimpleDateFormat sdf;
	String display_string,selectedOptions;
	String filename="BGServices";
	
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy.HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	    public void onCreate() {
		 rs=new RunningServices();
	     mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		 mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		 Intent i=new Intent(this, NetworkConfigService.class);
		 pi=PendingIntent.getService(this, 0, i, 0);
		 count=0;	
		 mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), CLOCK_TIMER, pi);
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		 String file_name=filename+getDateTime()+".txt";
	     file=new File(Environment.getExternalStorageDirectory(),file_name);
	     selectedOptions="";
		    
		 try{
			 fw=new FileWriter(file,true);		
			 display_string="Timestamp \t  Process Name \t Received TCP Bytes \t Transmitted TCP Bytes ";
			 fw.append(display_string);
		 }
		 catch(IOException e){
			 Log.i("FileWriter error",e.getMessage());
		 }
	 }
	 public String intToIP(int i) {
		    return ( i & 0xFF) + "." +
		        (( i >> 8 ) & 0xFF) + "." +
		        (( i >> 16 ) & 0xFF) + "." +
		        (( i >> 24 ) & 0xFF);
		}
	 protected List<RunningAppProcessInfo> getRunningProcesses(){
	    	ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);;
	    	List<RunningAppProcessInfo> process_list=am.getRunningAppProcesses();
	    	return process_list;
	    }
	 
	 @Override
	 public int onStartCommand(Intent intent, int flags, int startId) {
		 	list_running_process=getRunningProcesses();
		 	ni=cm.getActiveNetworkInfo();
		    info = wifi.getDhcpInfo();
		    date = new Date();
			sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a"); 
		    String formattedDate = sdf.format(date);
		    String currentDate=sdf.format(date);
		    if(intent.hasExtra(ServicesActivity.SELECTED_OPTIONS))
		    	selectedOptions=intent.getStringExtra(ServicesActivity.SELECTED_OPTIONS);
		    //String display_string="\n Type: "+ni.getTypeName()+"\n NetworkInfo: "+ni.toString()+"\n";
		    
		    for(int i=0;i<list_running_process.size();i++){
		    	if(TrafficStats.getUidTcpRxBytes(list_running_process.get(i).uid)>=0){
		    		if(selectedOptions.contains(list_running_process.get(i).processName)){
		    			display_string="\n"+currentDate+"\t"+list_running_process.get(i).processName+"\t"+TrafficStats.getUidTcpRxBytes(list_running_process.get(i).uid)/1024+"KB \t "+TrafficStats.getUidTcpTxBytes(list_running_process.get(i).uid)/1024+" KB";
			    		try{
							fw.append(display_string);
						}
						catch(IOException e){	
						}
					
			    		Log.i(list_running_process.get(i).processName,currentDate+"\t"+list_running_process.get(i).processName+"\t"+TrafficStats.getUidTcpRxBytes(list_running_process.get(i).uid)/1024+"KB \t "+TrafficStats.getUidTcpTxBytes(list_running_process.get(i).uid)/1024+" KB");		    		
			    	
		    		}
		    	}
		    }
	   return START_STICKY;
	 }	 
		
	 @Override
	 public void onDestroy() {
		 try{
			 fw.flush();
			 fw.close();
		 }
		 catch(IOException e){
			 Log.i("BGServices",e.getMessage());
		 }
	 Log.i("msg","Alarm stopped");
	 mgr.cancel(pi);
	 }
	
}
