package net.tetsuo83.netexp.zip;

import java.io.IOException;
import java.util.Collections;

import android.util.Log;

public class ZipFiles implements Runnable
{
	
	private static final String ZIP_COMMAND = "zip";
	
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
			String[] execArray = new String[param.getInputFiles().size()+ 2];
			Process proc;
			
			int i=2;
			int rst;
			
			//TODO: verify whether outputfile already exist and whether input files exist
			execArray[0] = ZIP_COMMAND;
			execArray[1] = param.outputFile;
			for (String s : param.getInputFiles())
				execArray[i++] = s;
			proc = Runtime.getRuntime().exec(ZIP_COMMAND);
			rst = proc.waitFor();
			
		} catch (IOException e)
		{
			Log.e(ZipFiles.class.getCanonicalName(), "IOError while compressing: " + " thread: " + Thread.currentThread());
		} catch (InterruptedException e)
		{
			Log.e(ZipFiles.class.getCanonicalName(), "Interrupted while compressing: " + " thread: " + Thread.currentThread());
		}
 		
	}
	
	
}
