package net.tetsuo83.netexp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;

import net.tetsuo83.netexp.console.ReadCommandThread;
import net.tetsuo83.netexp.util.SimpleBinder;
import net.tetsuo83.netexp.zip.ZipFiles;
import net.tetsuo83.netexp.zip.ZipParameters;
import net.tetsuo83.nrlexpupload.NrlExpUploadService;
import net.tetsuo83.nrlexpupload.UploadEntry;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkInfoService extends Service {
	
	private static final int ITERATIONS = 24*3;
	private static final long DURATION = 60L*(1000L)*60L;
	private static final int CLOCK_TIMER=10000;
	private static final long FLUSH_FREQ = 60000000000L;
	
	NotificationManager mNM;
	AlarmManager mgr;
	PendingIntent pi;
	ActivityManager am;
	
	NetworkInfo ni;
	
	WifiInfo info;
	DhcpInfo d_info;
	TelephonyManager tel;
	WifiManager wifi;
	ConnectivityManager cm;
	
	FileWriter configFw;
	BufferedWriter configBfw;
	File configfile;
	String configFileName;
	File configfileGzip;
	String configFileNameGzip;
	
	FileWriter processesFw;
	BufferedWriter processesBfw;
	File processesfile;
	String processesFileName;
	File processesfileGzip;
	String processesFileNameGzip;
	
	FileOutputStream ipConfFw;
	BufferedOutputStream ipConfBfw;
	File ipConffile;
	String ipConfFileName;
	File ipConffileGzip;
	String ipConfFileNameGzip;
	
	FileOutputStream apConfFw;
	BufferedOutputStream apConfBfw;
	File apConffile;
	String apConfFileName;
	File apConffileGzip;
	String apConfFileNameGzip;
	
	
	FileOutputStream netstatFw;
	BufferedOutputStream netstatBfw;
	File netstatfile;
	String netstatFileName;
	File netstatfileGzip;
	String netstatFileNameGzip;
	
	BroadcastReceiver mBatteryReceiver;
	BroadcastReceiver cWifiReceiver;
	
	int 				mCurrentBatteryLevel;
	long				lastFlush;
	
	
	ExperimentSchedule eS;
	
	boolean terminated;
    
	String			dataDirName;
	File				dataDir;		
	File				expFile;
	
    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy.HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return new SimpleBinder(this);
	}
	 public String intToIP(int i) 
	 {
		    return ( i & 0xFF) + "." +
		        (( i >> 8 ) & 0xFF) + "." +
		        (( i >> 16 ) & 0xFF) + "." +
		        (( i >> 24 ) & 0xFF);
	}
	
	 @Override
	public void onCreate() 
	 {
		 long now = System.currentTimeMillis();
		 
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 wifi = (WifiManager) getSystemService(WIFI_SERVICE);
	     tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		 loadExperiment();
		 setFiles();
		 initIntents();
	 }
	 
	 private synchronized void prepareIteration(long now)
	 {
		 while(eS.getCurrentIteration() < eS.iterationWindow(now) && !eS.isClosed())
		 {
			 Log.i(NetworkInfoService.class.getCanonicalName(), "preparing new iteration: " + eS.getCurrentIteration());
			 endIteration();
			 eS.advanceIteration();
			 saveExperiment(); // TODO if fails here state becomes incoherent
			 setFiles();
		 }
		 
	 }
	 
	 private void setFiles() 
	 {
		 closeFiles();
		 
		 configFileName ="netconf"+ eS.getStart() + "." + eS.getCurrentIteration();
		 configFileNameGzip ="netconf"+ eS.getStart() + "." + eS.getCurrentIteration() + ".gz";
		 processesFileName ="processes"+ eS.getStart() + "." + eS.getCurrentIteration();
		 processesFileNameGzip ="processes"+ eS.getStart() + "." + eS.getCurrentIteration() + ".gz";
		 ipConfFileName ="ipConfig"+ eS.getStart() + "." + eS.getCurrentIteration();
		 ipConfFileNameGzip ="ipConfig"+ eS.getStart() + "." + eS.getCurrentIteration() + ".gz";
		 netstatFileName="netstat"+ eS.getStart() + "." + eS.getCurrentIteration();
		 netstatFileNameGzip="netstat"+ eS.getStart() + "." + eS.getCurrentIteration() + ".gz";
		 apConfFileName ="apConfig"+ eS.getStart() + "." + eS.getCurrentIteration();
		 apConfFileNameGzip ="apConfig"+ eS.getStart() + "." + eS.getCurrentIteration() + ".gz";
		 
		 configfile= new File(dataDir,configFileName);
		 configfileGzip = new File(dataDir, configFileNameGzip);
		 ipConffile= new File(dataDir, ipConfFileName);
		 ipConffileGzip = new File(dataDir, ipConfFileNameGzip);
		 netstatfile= new File(dataDir, netstatFileName);
		 netstatfileGzip = new File(dataDir, netstatFileNameGzip);
		 processesfile = new File(dataDir, processesFileName);
		 processesfileGzip = new File(dataDir, processesFileNameGzip);
		 
		 apConffile= new File(dataDir, apConfFileName);
		 apConffileGzip = new File(dataDir, apConfFileNameGzip);
		 
		 try{
			 configFw=new FileWriter(configfile,true);	
			 configBfw = new BufferedWriter(configFw,4096);
			 
			 processesFw=new FileWriter(processesfile,true);	
			 processesBfw = new BufferedWriter(processesFw,4096);
			 
			 
			 ipConfFw= new FileOutputStream(ipConffile, true);
			 ipConfBfw= new BufferedOutputStream(ipConfFw);
			 
			 netstatFw= new FileOutputStream(netstatfile, true);
			 netstatBfw= new BufferedOutputStream(netstatFw);
			 
			 apConfFw= new FileOutputStream(apConffile, true);
			 apConfBfw= new BufferedOutputStream(apConfFw);
			 
		 }
		 catch(IOException e){
			 Log.i("FileWriter error",e.getMessage());
		 }
		
	}
	 
	private void loadExperiment() 
	{
		dataDirName = Environment.getExternalStorageDirectory() + File.separator + "NETINFO";
		dataDir = new File(dataDirName);
		
		if (!dataDir.exists())
		{
			dataDir.mkdirs();
		}
		
		expFile = new File(dataDir,"EXPINFO");
		boolean rst;
		
		if (!expFile.exists())
			rst = createExperiment();
		else
			rst = readExperiment();
		if (!rst) 
		{
			Log.e(NetworkInfo.class.getCanonicalName(), "Impossible to load/create experiment file, Safe Stop now");
			this.stopSelf();
		}
			
	}

	private boolean createExperiment()
	{
		eS = new ExperimentSchedule(System.currentTimeMillis(), ITERATIONS, DURATION);
		return saveExperiment();
	}
	
	private boolean readExperiment()
	{
		FileInputStream inS; 
		ObjectInputStream dIn;
		
		try
		{
			inS = new FileInputStream(expFile);
			dIn = new ObjectInputStream(inS);
			
			eS = (ExperimentSchedule) dIn.readObject();
			dIn.close();
			return true;
		} catch (Exception e)
		{
			Log.e(NetworkInfo.class.getCanonicalName(), "Impossible to read experiment file");
		}
		return false;
	}
	
	private boolean saveExperiment()
	{
		FileOutputStream outS;
		ObjectOutputStream dOut;
		
		try
		{
			if (expFile.exists())
				expFile.delete();
			outS = new FileOutputStream(expFile);
			dOut = new ObjectOutputStream(outS);
			dOut.writeObject(eS);
			dOut.flush();
			dOut.close();
			return true;
		} catch (IOException e) 
		{
			Log.e(NetworkInfo.class.getCanonicalName(), "Impossible to save experiment file, Safe Stop now");
			return false;
			
		}
		
	}
	
	private void initIntents() 
	 {
		 
		 mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		 mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		 Intent i=new Intent(this, NetworkInfoService.class);

		 pi=PendingIntent.getService(this, 0, i, 0);
		 mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), CLOCK_TIMER, pi);
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
	}
	 
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) { 
			writeLogLine(false,false);
			return START_STICKY;
		}
	
	 protected List<RunningAppProcessInfo> getRunningProcesses(){
	    	ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);;
	    	List<RunningAppProcessInfo> process_list=am.getRunningAppProcesses();
	    	return process_list;
	    }
	 
	 public void logProcesses(String now) {
			List<RunningAppProcessInfo> list_running_process=getRunningProcesses();

		    String display_string="\n--------------------" + now + "\n";
		    
		    try
		    {
		    		processesBfw.append(display_string);
			    for(int i = 0; i < list_running_process.size(); i++)
			    {
				    			display_string="\n" + list_running_process.get(i).processName
				    					+ " \t "+TrafficStats.getUidRxBytes(list_running_process.get(i).uid) 
				    					+ " \t "+TrafficStats.getUidTxBytes(list_running_process.get(i).uid)
		    							+ " \t "+TrafficStats.getUidRxBytes(list_running_process.get(i).uid) 
		    							+ " \t "+TrafficStats.getUidTxBytes(list_running_process.get(i).uid);
					    		processesBfw.append(display_string);	    	
			    }
		    } catch(IOException e)
				{
					Log.e(NetworkInfoService.class.toString(), "Error while logging service network use");
				}	
	 }	
	 
	 public void logNetwork(String now, boolean connectivityChange, boolean batteryChange)
	 {
		 info = wifi.getConnectionInfo();	
		 d_info=wifi.getDhcpInfo();
		 cm=(ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
		 ni=cm.getActiveNetworkInfo();
		 try
		 {
			 String o_string="\n"
					 + now
					 + "\t" + mCurrentBatteryLevel
					 + "\t" + "\"" + info.toString() + "\""
					 + "\t" + "\"" + (ni != null ? ni.toString() : "\"null\"") + "\""
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
			 configBfw.append(o_string);
		} catch (IOException e)
		{
				Log.e("Network Info", "Error while writing log file: " + e.getMessage());
		} 
	 }
	
	private synchronized void writeLogLine(boolean connectivityChange, boolean batteryChange)
	{

		 long now = System.currentTimeMillis();
		 ReadCommandThread ipThread = new ReadCommandThread(ipConfBfw, ipConffile.getAbsoluteFile().toString(), 
				 new String[] {"netcfg"},
				 0, 1, now+"");
		 ReadCommandThread netstatThread = new ReadCommandThread(netstatBfw, 
				 netstatfile.getAbsoluteFile().toString(), new String[] {"netstat"},
				 0, 1, now+"");

		 
	    	try{
				prepareIteration(now);
				
				if (eS.end(now) && eS.isClosed()	)
				{
					this.stopSelf();
				} else 
				{
					conditionalFlush(now);
		    			ipThread.start();
		   		 	netstatThread.start();
		   		 	logProcesses(now+"");
		   		 	logNetwork(now+"", connectivityChange, batteryChange);
		   		 	logAP(now+"");
		   		 	ipThread.join();
		   		 	netstatThread.join();
		    			Log.i(NetworkInfoService.class.toString(), "Logging");
				}
			} catch(InterruptedException e)
			{
				Log.e("Network Info", "Error while waiting for thread to end");
			}
	 }	 
		
	private void logAP(String now) 
	{
		
		DataOutputStream dout = new DataOutputStream(apConfBfw);
		String display_string="\n--------------------" + now + "\n";
		StringBuilder sbuild = new StringBuilder();
		try
		{
			logCellAp(sbuild, dout);
			logWifiAp(sbuild, dout);
			dout.writeChars(display_string);
			dout.writeChars(sbuild.toString());
		} catch (IOException e)
		{
			Log.e(NetworkInfoService.class.getCanonicalName(), "Could not log APs");
		}
		
	}
	
	private void logWifiAp(StringBuilder sbuild, DataOutputStream dout) 
	{
		List<ScanResult> infos = wifi.getScanResults();
		sbuild.append("WiFi:");
		sbuild.append("\n");
		if(infos != null)
		{
			for (ScanResult s : infos)
			{
				sbuild.append(s.toString());
				sbuild.append("\n");
			}
		} else 
		{
			sbuild.append("null");
			sbuild.append("\n");
		}
		
	}

	@SuppressLint("NewApi")
	private void logCellAp(StringBuilder sbuild, DataOutputStream dout)
	{
		List<CellInfo> infos = tel.getAllCellInfo();
		sbuild.append("Cells:");
		sbuild.append("\n");
		if (infos != null)
		{
			for (CellInfo c : infos)
			{
				sbuild.append(c.toString());
				sbuild.append("\n");
			}
		} else 
		{
			List<NeighboringCellInfo> ninfos = tel.getNeighboringCellInfo();
			if (ninfos != null)
			{
				for (NeighboringCellInfo c : ninfos)
				{
					sbuild.append(c.getCid());
					sbuild.append(" ");
					sbuild.append(c.getLac());
					sbuild.append(" ");
					sbuild.append(c.getNetworkType());
					sbuild.append(" ");
					sbuild.append(c.getPsc());
					sbuild.append(" ");
					sbuild.append(c.getRssi());
					sbuild.append("\n");
				}
			} else 
			{
				sbuild.append("null");
				sbuild.append("\n");
			}
		}
	}

	public void onDestroy()
	{
		if (eS.end(System.currentTimeMillis()))
		{
			endIteration();
			terminated = true;	
		}
		removeIntents();
	}
	private void endIteration()
	{
		closeFiles();
		zipFiles();
		sendData();
	}

	private void zipFiles() {
		try
		{
			ZipParameters param  = new ZipParameters(null); 
			param.getInputFiles().add(configfile.getAbsolutePath());
			param.getInputFiles().add(processesfile.getAbsolutePath());
			param.getInputFiles().add(ipConffile.getAbsolutePath());
			param.getInputFiles().add(netstatfile.getAbsolutePath());
			param.getInputFiles().add(apConffile.getAbsolutePath());
			ZipFiles zip = new ZipFiles(param);
			Thread thread = new Thread(zip);
			thread.run();
			thread.join();
		} catch (InterruptedException e)
		{
			Log.e(NetworkInfoService.class.getCanonicalName(), "Could not wait to compress files for: " + eS.advanceIteration());
		}
		
	}

	private void closeFiles() 
	{
		try
		{
			flushFiles();
			 if (configBfw != null) configBfw.close();
			 if (processesBfw != null) processesBfw.close();
			 if (ipConfBfw != null) ipConfBfw.close();
			 if (apConfBfw != null) apConfBfw.close();
			 if (netstatBfw != null) netstatBfw.close();
		} catch (IOException e)
		{
			Log.e("Network Info", "Error while closing log files: " + e.getMessage());
		}
		
	}
	
	private void conditionalFlush(long now)
	{
		if (lastFlush < 0 || lastFlush < System.currentTimeMillis() - FLUSH_FREQ) 
		{
			lastFlush = now;
			flushFiles();
		}
	}
	
	private void flushFiles() 
	{
		try
		{
			Log.i("Network info", "FLUSHING files: ");
			 if (configBfw != null) configBfw.flush();
			 if (ipConfBfw != null) ipConfBfw.flush();
			 if (apConfBfw != null) apConfBfw.flush();
			 if (netstatBfw != null) netstatBfw.flush();
			 if (processesBfw != null)  processesBfw.flush();
		} catch (IOException e)
		{
			Log.e("Network Info", "Error while closing log files: " + e.getMessage());
		}
		
	}

	private void sendData() 
	{	
		
		Intent intent=new Intent(this,NrlExpUploadService.class);
		intent.putExtra(UploadEntry.DELETE_AFTER,false);
		intent.putExtra(UploadEntry.FILE,configfileGzip.getAbsolutePath());
		intent.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent.putExtra(UploadEntry.NAME,configFileNameGzip);
		intent.setAction("ADD");
		
		Intent intent2=new Intent(this,NrlExpUploadService.class);
		intent2.putExtra(UploadEntry.DELETE_AFTER,false);
		intent2.putExtra(UploadEntry.FILE, ipConffileGzip.getAbsolutePath());
		intent2.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent2.putExtra(UploadEntry.NAME,ipConfFileNameGzip);
		intent2.setAction("ADD");
		
		Intent intent3=new Intent(this,NrlExpUploadService.class);
		intent3.putExtra(UploadEntry.DELETE_AFTER,false);
		intent3.putExtra(UploadEntry.FILE, netstatfileGzip.getAbsolutePath());
		intent3.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent3.putExtra(UploadEntry.NAME,netstatFileNameGzip);
		intent3.setAction("ADD");
		
		Intent intent4=new Intent(this,NrlExpUploadService.class);
		intent4.putExtra(UploadEntry.DELETE_AFTER,false);
		intent4.putExtra(UploadEntry.FILE, processesfileGzip.getAbsolutePath());
		intent4.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent4.putExtra(UploadEntry.NAME,processesFileNameGzip);
		intent4.setAction("ADD");
		
		Intent intent5=new Intent(this,NrlExpUploadService.class);
		intent5.putExtra(UploadEntry.DELETE_AFTER,false);
		intent5.putExtra(UploadEntry.FILE, apConffileGzip.getAbsolutePath());
		intent5.putExtra(UploadEntry.TOKEN,"ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		intent5.putExtra(UploadEntry.NAME,apConfFileNameGzip);
		intent5.setAction("ADD");
		
		startService(intent);
		startService(intent2);
		startService(intent3);
		startService(intent4);
		startService(intent5);
	}

	public ExperimentSchedule getSchedule()
	{
		return this.eS;
	}
	
	public void removeIntents()
	{
		unregisterReceiver(mBatteryReceiver);
		unregisterReceiver(cWifiReceiver);
		mgr.cancel(pi);
	}
	
}
