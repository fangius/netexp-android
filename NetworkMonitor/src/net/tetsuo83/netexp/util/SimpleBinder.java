package net.tetsuo83.netexp.util;

import net.tetsuo83.netexp.NetworkInfoService;
import android.os.Binder;

public class SimpleBinder<S> extends Binder
{
	private			S						service;
	
	public SimpleBinder(S s)
	{
		super();
		this.service = s;
		
	}
	
	public S getService()
	{
		return service;
	}
	
	
	
}
