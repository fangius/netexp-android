package net.tetsuo83.netexp;

import net.tetsuo83.nrlexpupload.NrlExpUploadService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

public class MainBroadcastReceiver extends BroadcastReceiver   
{

	public static final Class[] BOOTSTRAP_SERVICES = {NetworkInfoService.class, NrlExpUploadService.class};
	
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
	    		bootstrap(context);
		}
	}

	private void bootstrap(Context context) 
	{
		
		for (Class c : BOOTSTRAP_SERVICES)
		{
			Intent intent = new Intent(context,c);
    			intent.setAction(NrlBotConstant.LAUNCH_FRAMEWORK);
    			context.startService(intent);
		}
		
	}
}
