package net.tetsuo83.netexp;

import net.tetsuo83.nrlexpupload.NrlExpUploadService;

import com.ms.networkmonitor.R;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

public class MainActivity extends Activity {
	ImageView image;
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
    		getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void startRecordingNetworkInfo(View view)
    {
    		Intent intent=new Intent(this,NetworkInfoService.class);
    		startService(intent);
    		Intent intents=new Intent(this,NrlExpUploadService.class);
    		startService(intents);
    }
    
    public void startNetworkUse()
    {
    	
    }
    
    public void stopNetworkUse()
    {
    	
    }
    
    public void stopRecordingNetworkInfo(View view)
    {
    	Intent intent=new Intent(this,NetworkInfoService.class);
    		stopService(intent);
    }
}