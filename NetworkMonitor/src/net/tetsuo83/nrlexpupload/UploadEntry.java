package net.tetsuo83.nrlexpupload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

public class UploadEntry implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2655583067196235900L;
	
	public static final String DELETE_AFTER = "DELETE_AFTER";
	public static final String FILE = "FILE";
	public static final String TOKEN = "TOKEN";
	public static final String NAME = "NAME";


	public boolean isDeleteAfterwards() {
		return deleteAfterwards;
	}

	public void setDeleteAfterwards(boolean deleteAfterwards) {
		this.deleteAfterwards = deleteAfterwards;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	boolean deleteAfterwards;
	String file = new String();
	String token;
	String name;
	
	public UploadEntry()
	{
		
	}
	
	public UploadEntry(Intent intent) {
		
		this();
		deleteAfterwards =  intent.getBooleanExtra(DELETE_AFTER, false);
		file = intent.getStringExtra(FILE);
		token = intent.getStringExtra(TOKEN);
		name = intent.getStringExtra(NAME);
	}
	
	
		
}
