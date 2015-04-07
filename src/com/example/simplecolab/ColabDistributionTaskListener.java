package com.example.simplecolab;

public interface ColabDistributionTaskListener {
	//Listener interface for Colab distribution task
	
	public void onChunkResultReady(int chunkNumber, long result);

}
