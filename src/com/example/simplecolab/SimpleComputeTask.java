package com.example.simplecolab;

import java.io.File;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class SimpleComputeTask extends AsyncTask<String, Integer, String> {
	/* This Class is an AsynchTask to compress a video on a background thread
	 * 
	 */
	
	private SimpleComputeTaskListener thisTaskListener;
	private Context appContext;
	
	public SimpleComputeTask(Context appCon, SimpleComputeTaskListener ourListener) {
		//Set the listener
		thisTaskListener = ourListener;
		this.appContext = appCon;
	}

    @Override
    protected String doInBackground(String... params) {
    	//Compress the video in the background
    	Log.d("SimpleComputeTask","doInBackground");
    	
    	//Get the the path of the numbers file
    	String numbersFilePath;
    	String numbersFileName;
    	File numbersFile;
    	if (params.length == 1) {
    		numbersFileName = params[0];
    		numbersFile = new File( Environment.getExternalStorageDirectory() + numbersFileName);
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("SimpleComputeTask","doInBackground wrong number of params");
    		return null;
    	}
    	
    	//Make sure the video to compress actually exists
    	if(!numbersFile.exists()) {
    		Log.d("SimpleComputeTask","doInBackground numbers file does not exist");
    		return null;
    	}
    	
    	//Report the compressed file path
    	String compressedFilePath = Environment.getExternalStorageDirectory() + "/Comp_" + videoFileName;
    	Log.d("VideoCompressionTask","doInBackground compressedFilePath: " + compressedFilePath);
    	publishProgress(compressedFilePath);
    	
    	//Do the computation
    	

    	return(result);
    }
    
    @Override
    protected void onProgressUpdate(String... compressedFilePath) {
    	// Do nothing
    }
    
    @Override
    protected void onPostExecute(int result) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onComoutationFinished(result);
    }

}
