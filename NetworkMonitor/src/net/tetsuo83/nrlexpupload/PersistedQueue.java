package net.tetsuo83.nrlexpupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.util.Log;

public class PersistedQueue<S extends Serializable>
{
	private static final long serialVersionUID = 1087947288222947815L;
	
	File dir;
	String prefix;
	
	File current;
	LinkedList<S> list = new LinkedList<S>();
	
	public PersistedQueue(File dir, String p)
	{
		this.dir = dir;
		this.prefix = p;
		loadFile();
	}

	public boolean add(S s)
	{
		boolean flag = list.add(s);
		if (flag) 
		{
			saveQueue();
		}
		return flag;
	}
	
	public synchronized S peek()
	{
		return list.peek();
	}
	public synchronized S remove()
	{
		S tmp = list.remove();
		if (tmp != null) saveQueue();
		return tmp;
	}
	
	public synchronized S poll()
	{
		S tmp = list.poll();
		if (tmp != null) saveQueue();
		return tmp;
	}
	
	public synchronized int size()
	{
		return list.size();
	}

	private void saveQueue() 
	{
		try
		{
			File f = new File (dir, prefix + "." + System.nanoTime());
			FileOutputStream out = new FileOutputStream(f);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(list);
			oout.flush();
			oout.close();
			if (current != null) current.delete();
			current = f;
			
		} catch (IOException e)
		{
			Log.e(PersistedQueue.class.getCanonicalName(), "Could not persist queue: " + prefix);
		}
	}
	
	private void loadFile() 
	{
		List<File> files = findLatest();
		
		if (files.size() == 0)
		{
			saveQueue();
		} else 
		{
			chooseLatest(files);
		}
 		
	}

	private void chooseLatest(List<File> l)
	{
		LinkedList<S> queue;
		for (int i=l.size() -1; i >= 0; i--)
		{
			File file = l.get(i);
			queue = readQueue(file);
			if (queue == null) 
			{
				file.delete();
			} else 
			{ 
				current = file;
				this.list = queue;
			}
			
		}
		
	}
	
	private LinkedList<S> readQueue(File f) 
	{	
		String path = null;
		try
		{
			path =  f.getCanonicalPath();
			FileInputStream input = new FileInputStream(f);
			ObjectInputStream oin = new ObjectInputStream(input);
			
			@SuppressWarnings("unchecked")
			LinkedList<S> queue = (LinkedList<S>) oin.readObject();
			oin.close();
			return queue;
		} catch (IOException e)
		{
			Log.e(PersistedQueue.class.getCanonicalName(), "Could not load queue from :"  + path);
		} catch (ClassNotFoundException e)
		{
			Log.e(PersistedQueue.class.getCanonicalName(), "Could not load queue from :"  + path);
		}
		
		return null;
		
	}

	private List<File> findLatest() 
	{
		File[] ls = dir.listFiles();
		List<File> results = new ArrayList<File>();
		for (File f : ls)
		{
			if (f.isFile() && f.getName().contains(prefix))
			{	
				results.add(f);
			}
		}
		Collections.sort(results);
		return results;
	}
	
	
	
	
	
	
}
