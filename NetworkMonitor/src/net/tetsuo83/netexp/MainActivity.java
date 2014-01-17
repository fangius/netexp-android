package net.tetsuo83.netexp;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	
    	getMenuInflater().inflate(R.menu.activity_main, menu);
    	
        return true;
    }
    
    public void callService(View view)
    {
    		Intent i=new Intent(this,ServicesActivity.class);
    		startActivity(i);
    }

    public void callTCPDumpService(View view){
    	Intent i=new Intent(this,RecordTCPDumpService.class);
    	CheckBox chk=(CheckBox) findViewById(R.id.checkBox1);
    	if(chk.isChecked())
    		i.putExtra("dump", "binary");
    	else
    		i.putExtra("dump", "txt");
    		startService(i);
    }
    
    public void stopTCPDumpService(View view)
    {
    	Intent intent=new Intent(this,RecordTCPDumpService.class);
    		stopService(intent);
    }
    
    public void startRecordingNetworkInfo(View view)
    {
    		Intent intent=new Intent(this,RecordNetworkInfoService.class);
    		startService(intent);
    }
    
    public void stopRecordingNetworkInfo(View view)
    {
    	Intent intent=new Intent(this,RecordNetworkInfoService.class);
    		stopService(intent);
    }
}