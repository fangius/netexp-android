package net.tetsuo83.netexp;

import java.io.Serializable;


public class ExperimentSchedule implements Serializable
{
	
	private 			long								start;
	private				int								iterations;	
	private 			long 								itLength;
	private				int								currentIteration = 0;
	private 			boolean						experimentClosed = false;
	
	public ExperimentSchedule(long start, int iterations, long itLength)
	{
		this.start = start;
		this.iterations = iterations;
		this.itLength = itLength;
	}

	public int iterations()
	{
		return iterations;
	}
	public long getStart() {
		return start;
	}
	
	public int getCurrentIteration()
	{
		return currentIteration;
	}
	
	public int advanceIteration()
	{
		currentIteration += 1;
		if (currentIteration > iterations) experimentClosed = true;
		
		return currentIteration;
	}
	
	public boolean end(long now)
	{
		return now >= iterationEnd(iterations);
	}
	
	public int iterationWindow(long now)
	{
		long diff = now - start;
		int cIt = (int)Math.floor(new Double(diff)/new Double(itLength));
		
		if (cIt > iterations) return -1;
		
		return cIt;
		
	}
	
	public long iterationStart(int i)
	{
		return start + itLength*i;
	}
	
	public long iterationEnd(int i)
	{
		return start + itLength*(i+1);
	}
	
	public boolean isClosed()
	{
		return experimentClosed;
	}
	
	@Override
	public String toString()
	{
		return "Started: " + start + " Ends: " + iterationEnd(iterations) + "Current Iteration: " + currentIteration +  " Iterations: " + iterations; 
	}
	

}
