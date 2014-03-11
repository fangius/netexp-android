package net.tetsuo83.netexp;

import java.util.Queue;

import net.tetsuo83.netexp.util.SimpleBinder;
import net.tetsuo83.netexp.util.SimpleServiceConnection;
import net.tetsuo83.nrlexpupload.NrlExpUploadService;
import net.tetsuo83.nrlexpupload.PersistedQueue;
import net.tetsuo83.nrlexpupload.UploadEntry;

import com.ms.networkmonitor.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity 
{
	private				SimpleServiceConnection<NetworkInfoService>		conExp;
	private				SimpleServiceConnection<NrlExpUploadService>	conUpd;
	private				ProgressBar									prBar;
	private				TextView										prText;
	private				TextView										upText;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        	setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
    		conExp = new SimpleServiceConnection<NetworkInfoService>();
    		conUpd = new SimpleServiceConnection<NrlExpUploadService>();
    		getMenuInflater().inflate(R.menu.activity_main, menu);
    		prBar = (ProgressBar) findViewById(R.id.experimentBar);
    		prText = (TextView) findViewById(R.id.experimentText);
    		upText = (TextView) findViewById(R.id.updatesText);
    		createBond();
    		updateProgress();
        return true;
    }

    
    public void refreshClick(View view)
    {
    		updateProgress();
    }
    
	private void createBond() 
    {
    		
    		Intent intent=new Intent(this,NetworkInfoService.class);
		bindService(intent, conExp , BIND_AUTO_CREATE);
		
		Intent intent2=new Intent(this,NrlExpUploadService.class);
		bindService(intent2, conUpd , BIND_AUTO_CREATE);
	}
    
	private void updateProgress()
	{
		if (conExp.bond())
		{
			ExperimentSchedule eS = conExp.getService().getSchedule();
			prBar.setMax(eS.iterations());
			prBar.setProgress(eS.getCurrentIteration());
			prText.setText("Number of iterations completed " + eS.getCurrentIteration() + " of " + eS.iterations());

		}
		
		if (conUpd.bond())
		{
			PersistedQueue<UploadEntry> queue = conUpd.getService().queue();
			upText.setText("Number of files to upload " + queue.size());
		}
	}
	
	public void startRecordingNetworkInfo(View view)
    {
    		Intent intent=new Intent(this,NetworkInfoService.class);
    		startService(intent);
    		Intent intents=new Intent(this,NrlExpUploadService.class);
    		startService(intents);
    }
    
    public void stopRecordingNetworkInfo(View view)
    {
    		Intent intent=new Intent(this,NetworkInfoService.class);
    		stopService(intent);
    		Intent intents=new Intent(this,NrlExpUploadService.class);
    		stopService(intents);
    }
}