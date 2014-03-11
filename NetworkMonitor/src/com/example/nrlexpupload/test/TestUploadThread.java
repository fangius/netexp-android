package com.example.nrlexpupload.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.tetsuo83.nrlexpupload.PersistedQueue;
import net.tetsuo83.nrlexpupload.UploadEntry;
import net.tetsuo83.nrlexpupload.UploadThread;

public class TestUploadThread {
 
	public static void main(String[] args) throws Exception
	{
		AtomicBoolean sync = new AtomicBoolean();
		
		String file = "/Users/fabio/Desktop/notes.rtf";
		String testAddress = "http://localhost:8080";
		PersistedQueue<UploadEntry> wQueue = new PersistedQueue<UploadEntry>(new File("/tmp/"), "uploadqueue");
		UploadEntry entry = new UploadEntry();
		entry.setDeleteAfterwards(false);
		entry.setFile(file);
		entry.setToken("ef2be8dd60981603904b4d1c18972a8cd6c6e7ac");
		entry.setName("upload");
		wQueue.add(entry);
		UploadThread updt = new UploadThread(sync, wQueue, "http://localhost:8080");
		Thread th = new Thread(updt);
		th.start();
		th.join();

	}

}
