package net.tetsuo83.netexp.console;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
	
    public ReadCommandThread(OutputStream out, String file, String[] command, int interval, int repeats)
    {
    		this(out, file, command, interval, repeats, "----------------------");
    		
	}
    
    public ReadCommandThread(OutputStream out, String file, String[] command, int interval, int repeats, String separator)
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

				byte[] buf = new byte[4096];
				int read;
				
				while(iterations < repeats)
				{
					out.writeUTF("\n" +separator + "-" + iterations + "\n");
					proc = Runtime.getRuntime().exec(command);		
					Log.i("RISULTATO FINALE" , proc.waitFor()+"");
					InputStream reader = proc.getInputStream(); 
					boolean flag = false;
					
					while ((read = reader.read(buf)) > 0)
					{
							if (read > 0) out.write(buf,0, read);
					}
					iterations++;
					if (repeats > 1) Thread.sleep(interval);
				}
		} catch(IOException e)
		{
			 normallyEnded = false;
			 Log.e(ReadCommandThread.class+"", e.toString() + " " + e.getMessage());
		} catch (InterruptedException e)
		{
			normallyEnded = false;
			Log.e(ReadCommandThread.class+"", "Thread Interrupted while waiting for next iteration");
		}
		normallyEnded = true;
	}

	public boolean isNormallyEnded() {
		return normallyEnded;
	}
}
