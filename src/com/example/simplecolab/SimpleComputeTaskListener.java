package com.example.simplecolab;

public interface SimpleComputeTaskListener {
	/*
	 * This class is a  listener for events from the compute asynch task
	 */
	
	public void onSimpleComputeFinished(int result);
	// called when the compression task has completed
	
	public void onSimpleCommputePorgressUpdate();

}