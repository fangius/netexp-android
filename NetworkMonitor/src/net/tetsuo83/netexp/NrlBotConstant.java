package net.tetsuo83.netexp;

import java.io.File;

import android.os.Environment;

public class NrlBotConstant 
{
	 
	public final static String LAUNCH_FRAMEWORK = NrlBotConstant.class.getPackage() + ".LAUNCHING_NRLBOT";
	public final static String FRAMEWORK_DIR = Environment.getExternalStorageDirectory() + File.separator + "NETINFO";
	
}
