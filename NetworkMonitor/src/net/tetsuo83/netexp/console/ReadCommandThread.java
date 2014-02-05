package net.tetsuo83.netexp.console;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class ReadCommandThread extends Thread 
{
	Process proc;
	DataOutputStream out;
	File file;
	String display_string;
	String[] command;
	int repeats;
	int interval;
	boolean normallyEnded;
	String separator;
	
    public ReadCommandThread(OutputStream out, String[] command, int interval, int repeats)
    {
    		this(out, command, interval, repeats, "----------------------");
    		
	}
    
    public ReadCommandThread(OutputStream out, String[] command, int interval, int repeats, String separator)
    {
		this.out = new DataOutputStream(out);
		this.command = command;
		this.repeats = repeats;
		this.interval = interval;
		this.separator = separator;
    }
	
	public void run(){
		try{
			
				int iterations = 0;
				proc = Runtime.getRuntime().exec(command);			
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
				String line;
				while(iterations < repeats)
				{
					out.writeChars(separator + "-" + iterations);
					while ((line=reader.readLine()) != null)
					{
							out.writeChars(line);
							out.write('\n');
							Log.i(ReadCommandThread.class+"","Writing line");
					}
					proc.destroy();
					if (repeats > 1) Thread.sleep(interval);
				}
				
		} catch(IOException e){
			 normallyEnded = false;
			 Log.e(ReadCommandThread.class+"",e.getMessage());
		} catch (InterruptedException e)
		{
			normallyEnded = true;
			Log.e(ReadCommandThread.class+"", "Thread Interrupted while waiting for next iteration");
		}
		normallyEnded = true;
	}

	public boolean isNormallyEnded() {
		return normallyEnded;
	}
}
