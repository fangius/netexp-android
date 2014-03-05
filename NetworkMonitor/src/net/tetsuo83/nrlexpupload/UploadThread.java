package net.tetsuo83.nrlexpupload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

public class UploadThread implements Runnable 
{

	private static final int MAX_ATTEMPTS = 3;
	private static final String CRLF = "\r\n";
	private static final String HYPHENS = "--";
	
	AtomicBoolean sync;
	private PersistedQueue<UploadEntry> queue;
	private Random rnd = new Random();
	private String address;
	byte[] buf = new byte[4096];
	
	public UploadThread(AtomicBoolean sync, PersistedQueue<UploadEntry> queue, String address)
	{
		this.queue = queue;
		this.address = address;
		this.sync = sync;
	}
	
	@Override
	public void run() 
	{
		UploadEntry e;		
		try
		{
			boolean flag = true;
			sync.set(true);			
			while (queue.size() > 0 && flag)
			{	
				e = queue.peek();
					flag = send(e);
					if (flag) 
					{
						queue.poll();
						File f = new File(e.getFile());
						if (e.isDeleteAfterwards() && f.exists()) 
						{
							f.delete();
						}
					}
			}
		} catch (Exception ex)
		{
			Log.e(UploadThread.class.getCanonicalName(), ex.getMessage());
		}
		finally
		{
			sync.set(false);
		}
		
	}
	
	private boolean send(UploadEntry e)
	{
		long rndToken = rnd.nextLong();
		int attempts = 0;
		boolean tooManyAttempts = false;
		boolean success = false;
		String bnd = "----------" + rndToken +  "----------";
		
		HttpURLConnection conn = null;
		BufferedOutputStream out;
		BufferedInputStream in;
		DataOutputStream dout;
		
		while (!success && (!tooManyAttempts))
		{
			try
			{
				int rsp = 0;
				String s = e.getFile();
				File f = new File(s);
				int length = (int)f.length();
				URL url = new URL(address);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				//conn.setRequestProperty("Content-Length", length+"");
				conn.setRequestProperty(UploadEntry.TOKEN, e.getToken());
				conn.setRequestProperty(UploadEntry.NAME, e.getName());
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + bnd);
				//out = new BufferedOutputStream(conn.getOutputStream());
				in = new BufferedInputStream(new FileInputStream(f));
				dout = new DataOutputStream(conn.getOutputStream());
				
				//dout.writeBytes(""+HYPHENS +bnd + CRLF);
				sendStream(buf, in, dout);
				//dout.writeBytes(""+CRLF + HYPHENS +bnd + HYPHENS + CRLF);
				dout.flush();
				rsp = conn.getResponseCode();
				
				if (rsp >= 200 && rsp < 300)
				{ 
					success = true;
				} else 
				{
					attempts++;
					tooManyAttempts = attempts >= MAX_ATTEMPTS;
				}
			} catch (Exception ex) //TODO EXTEND LIST OF EXCEPTIONS
			{
				attempts++;
				tooManyAttempts = attempts >= MAX_ATTEMPTS;
				ex.printStackTrace();
				//Log.e("exception", "an error append sending a file: "+ ex.getMessage());
			} finally 
			{
				conn.disconnect();
			}
				
		}
		return !tooManyAttempts;
	}

	private void sendStream(byte[] buf, BufferedInputStream in, OutputStream out) throws IOException
	{
		int read;
		while((read = in.read(buf)) > 0)
		{
			out.write(buf, 0,read);
			out.flush();
		}
		
	}
}
