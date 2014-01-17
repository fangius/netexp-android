package net.tetsuo83.netexp;

import java.util.List;

import com.ms.networkmonitor.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ServicesActivity extends Activity {
	String selected;
	Button getChoice;
	ListView listView;
	NetworkConfigService networkConfigService;
	boolean bound;
	public static final String SELECTED_OPTIONS="com.ms.networkmonitor.ServicesActivity.SELECTED_OPTIONS";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_services);
		
		// Show the Up button in the action bar.
		listView = (ListView) findViewById(R.id.mylist);
		getChoice =(Button) findViewById(R.id.startMonitoring);
		String[] values=null;
		values=getRunningProcesses();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    listView.setItemChecked(2,true);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, values);		    
	    listView.setAdapter(adapter);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    getRunningProcesses();
	    getChoice.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                selected = "";
                int cntChoice = listView.getCount();
                SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();
                for(int i = 0; i < cntChoice; i++){
                   if(sparseBooleanArray.get(i)) {
                        selected += listView.getItemAtPosition(i).toString() + " ";
                    }
                }
                Log.i("selected",selected);
                callService(selected);
            }});
	}
	protected void callService(String selected){
		Intent intent=new Intent(this,NetworkConfigService.class);
		intent.putExtra(SELECTED_OPTIONS, selected);
		startService(intent);
	}

	 protected String[] getRunningProcesses(){
	    	ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);;
	    	List<RunningAppProcessInfo> process_list=am.getRunningAppProcesses();
	    	String array_list[]=new String[process_list.size()];
	    	for(int i=0;i<process_list.size();i++)
	    		array_list[i]=new String(process_list.get(i).processName);
	    	return array_list;
	    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_services, menu);
		return true;
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    public void callService(View view){
    	Intent i=new Intent(this,NetworkConfigService.class);
    	startService(i);
    }

    public void stopService(View view)
    {
    	Intent intent=new Intent(this,NetworkConfigService.class);
    	stopService(intent);
    }

}
