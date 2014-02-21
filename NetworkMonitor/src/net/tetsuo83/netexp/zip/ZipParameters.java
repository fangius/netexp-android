package net.tetsuo83.netexp.zip;

import java.util.ArrayList;
import java.util.List;

public class ZipParameters 
{
	String outputFile;
	List<String> inputFiles;
	
	public ZipParameters(String o, List<String> i)
	{
		assert i != null;
		this.inputFiles = i;
		this.outputFile = o;
	}
	
	public ZipParameters(String o)
	{
		this(o, new ArrayList<String>());
	}
	
	public String getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public List<String> getInputFiles()
	{
		return inputFiles;
	}
	
}
