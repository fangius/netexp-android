package net.tetsuo83.netexp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class TCPDumpThread extends Thread {
	String TCPDump="";
	Process proc;
	FileWriter fw;
	File file;
	String display_string;
	String filename="TCPDumpTxt";
	String filebinary="TCPDumpBinary";
	Boolean flag;
	static Boolean dumpbinarytype=true;
	ConnectivityManager cm;
	NetworkInfo ni;
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy.HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
    TCPDumpThread(String dumptype,ConnectivityManager cm){
    	this.cm=cm;
         flag=true;
		 try{
			 if(dumptype.equals("binary"))
				 dumpbinarytype=true;
			 else{
				 String file_name=filename+getDateTime()+".txt";
		    	 file=new File(Environment.getExternalStorageDirectory(),file_name);
				 fw=new FileWriter(file,true);	
				 dumpbinarytype=false;
			 }
				 
		 }
		 catch(IOException e){
			 Log.i("FileWriter error",e.getMessage());
		 }
		start();
		Log.i("tcpdump service","started");

	}
	
	public void run(){
		try{
			if(!dumpbinarytype){
				proc = Runtime.getRuntime().exec(new String[]{"su","-c","tcpdump"});			
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
				String line;
			
				while(flag)  
				{
					line=reader.readLine();
					TCPDump="\n"+line;
					try{
						if(line!=null)
							fw.append(TCPDump);
						else{
							TCPDump="\n No Wireless Connection available";
							ni=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
							if(ni.isConnected()){
								Log.i("connection status","rconnected");
								proc = Runtime.getRuntime().exec(new String[]{"su","-c","tcpdump"});			
								reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));								
							}
						}
						Log.i("TCPDump message",TCPDump);
					}
					catch(IOException e){
						Log.i("TCPDump writing error", TCPDump);
					}
				}		
			}
			else{
				String file_name=filebinary+getDateTime();
				proc = Runtime.getRuntime().exec(new String[]{"su","-c","tcpdump -w /sdcard/"+file_name});		 
			}
				
		}
		 catch(IOException e){
			 Log.i("error",e.getMessage());
		 }	
	}
	public void myStop(){
		 try{
			 if(!dumpbinarytype){
				flag=false;
				fw.flush();
				fw.close();
			 }
		}
		 catch(IOException e){
			 Log.i("TCPDump",e.getMessage());
		 }
		 finally{
			 destroyProcess(proc);
		 }
	}
	private static void destroyProcess(Process process) {
        try {
            if(dumpbinarytype){
            	try{
            		Process p=Runtime.getRuntime().exec("killall tcpdump");
            		Log.i("tcpdump","killed");
            	}
            	catch(IOException e){
            		Log.i("error", e.getMessage());
            	}
            	
            }
            else if (process != null) {
                // use exitValue() to determine if process is still running. 
                process.exitValue();
                process.destroy();
                Log.i("process status","killed");
               
            }
        } catch (IllegalThreadStateException e) {
            // process is still running, kill it.
            
        }
    
	}
}
