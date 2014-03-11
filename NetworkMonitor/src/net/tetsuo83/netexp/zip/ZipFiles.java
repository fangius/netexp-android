package net.tetsuo83.netexp.zip;

import java.io.IOException;
import java.util.Collections;

import android.util.Log;

public class ZipFiles implements Runnable
{
	
	private static final String ZIP_COMMAND = "gzip";
	
	
	boolean			success = false;
	ZipParameters param;
	
	public ZipFiles(ZipParameters param)
	{
		this.param = param;
	}
	 
	@Override
	public void run() 
	{
		try
		{
			String[] execArray = new String[2];
			Process proc;
			
			int i=2;
			int rst = -1;
			int attempts = 0;
			
			//TODO: verify whether outputfile already exists and whether input files exist
			execArray[0] = ZIP_COMMAND;
			
			for (String s : param.getInputFiles())
			{
				rst = -1;
				attempts = 0;
				while (rst != 0 && attempts < 3) //TODO: change number of attempts to a parameter in the constructor
				{
					execArray[1] = s;
					proc = Runtime.getRuntime().exec(execArray);
					rst = proc.waitFor();
					attempts++;
				}
			}
			success = (rst == 0);
		} catch (IOException e)
		{
			Log.e(ZipFiles.class.getCanonicalName(), "IOError while compressing: " + " thread: " + Thread.currentThread());
		} catch (InterruptedException e)
		{
			Log.e(ZipFiles.class.getCanonicalName(), "Interrupted while compressing: " + " thread: " + Thread.currentThread());
		}
 		
	}
	
	public boolean succeded()
	{
		return success;
	}
	
}
