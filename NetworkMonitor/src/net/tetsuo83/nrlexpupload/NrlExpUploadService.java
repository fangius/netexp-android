package net.tetsuo83.nrlexpupload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import net.tetsuo83.netexp.NrlBotConstant;
import net.tetsuo83.netexp.util.SimpleBinder;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class NrlExpUploadService extends Service
{
	public static final boolean onlyOnWifi = true;
	public static final String address = "http://independence.cs.ucla.edu:443/";
	
	ConnectivityManager  cm;
	WifiManager wm;
	BroadcastReceiver cWifiReceiver;
	
	PersistedQueue<UploadEntry> wQueue; 
	AtomicBoolean threadUp = new AtomicBoolean();
	Thread uThread;
	
	String dataDirName;
	File dataDir;
	
	@Override
	public void onCreate()
	{
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 wm = (WifiManager) getSystemService(WIFI_SERVICE);
		 initIntents();
		setDirectories();
		wQueue = new PersistedQueue(new File(NrlBotConstant.FRAMEWORK_DIR), "uploadQueue");
	}
	
	private void initIntents() {
		 cWifiReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent i = new Intent(context,NrlExpUploadService.class);
				context.startService(i);
			}
		};
		registerReceiver(cWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
	}
	
	private void removeIntents()
	{
		unregisterReceiver(cWifiReceiver);
	}

	private void setDirectories() {
		
		String dataDirName = NrlBotConstant.FRAMEWORK_DIR + File.separator + "UPLOAD_DIR";
		dataDir = new File(dataDirName);
		dataDir.mkdirs();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
	    try
	    {
	    		String action = intent.getAction();
	    		if (action != null)
	    		{
		    		if (action.equals("ADD"))
				{
		    			UploadEntry entry = new UploadEntry(intent);
		    			wQueue.add(entry);
				}
	    		}
	    		connectIf();
	    } catch (Exception e)
	    {
	    		Log.e(NrlExpUploadService.class+"", "Error while Starting the service");
	    }
	    return START_STICKY;
	}
	
	public void onDestroy()
	{
		removeIntents();
		stopLoop();
	}

	private void connectIf()
	{
		boolean wifi = wm.isWifiEnabled();
		if (wifi || !onlyOnWifi) 
		{
			startLoop();
		} else 
		{
			stopLoop();
		}
	}
	private void startLoop() 
	{
		boolean lock = threadUp.compareAndSet(false, true);
		if (!lock) return;
		uThread = new Thread(new UploadThread(threadUp,wQueue, address));
		uThread.start();
	}
	
	private void stopLoop() 
	{
		try
		{
			boolean lock = threadUp.compareAndSet(true, true);
			if (!lock) return;
			if (uThread != null) uThread.interrupt();
			 threadUp.set(false);
		} catch (Exception e)
		{
			Log.e(NrlExpUploadService.class.getCanonicalName(), e.getMessage());
		}
		
	}

	public PersistedQueue<UploadEntry> queue()
	{
		return wQueue;
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return new SimpleBinder<NrlExpUploadService>(this);
	}

}
