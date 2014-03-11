package net.tetsuo83.netexp.util;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class SimpleServiceConnection<S> implements ServiceConnection
{
		S								service;
		boolean					bond = false;
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder b) {
		bond = true;
		service = ((SimpleBinder<S>)b).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) 
	{
		bond = false;
	}
	
	public S getService()
	{
		return service;
	}
	
	public boolean bond()
	{
		return bond;
	}
	
}