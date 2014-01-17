package net.tetsuo83.netexp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

public class RecordTCPDumpService extends Service {
	NotificationManager mNM;
    RunningServices rs;
	AlarmManager mgr;
	PendingIntent pi;
	ActivityManager am;
	int CLOCK_TIMER=5000;
	TCPDumpThread tdt;
	Boolean threadStarted=false;
	ConnectivityManager cm;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	 @Override
	    public void onCreate() {

	    cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    //	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	     mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		 mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
	   }
	 @Override
	 public int onStartCommand(Intent intent, int flags, int startId) {
		 Log.i("alarm status","running");
		 if(!threadStarted){
			 String dumptype=intent.getStringExtra("dump");
			 tdt=new TCPDumpThread(dumptype,cm);
			 threadStarted=true;
		 }
		 return START_STICKY;
	 }	 
		
	 @Override
	 public void onDestroy() {
	 tdt.myStop();
	 Thread t = tdt;
	 tdt=null;
	 t.interrupt();
	 Log.i("msg","Alarm stopped");
	 mgr.cancel(pi);
	 }
	
}
