package com.example.simplecolab;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class SimpleComputeTask extends AsyncTask<String, String, Double> {
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
    protected Double doInBackground(String... params) {
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
    	
    	//Make sure the numbers file actually exists
    	if(!numbersFile.exists()) {
    		Log.d("SimpleComputeTask","doInBackground numbers file does not exist");
    		return null;
    	}
    	
	    BufferedInputStream numbersFileIS;
	    Double result = 0.0;
		try {
			numbersFileIS = new BufferedInputStream(new FileInputStream(numbersFile));
    	
	    	//Do the computation
	    	int numbersFileSize = safeLongToInt(numbersFile.length());
	    	
	    	for(int i=0; i<numbersFileSize; i++) {
	    		byte thisByte[] = new byte[1];
	    		int readResult = numbersFileIS.read(thisByte);
	    		if(readResult < 1) {
	    			Log.d("SimpleComputeTask","doInBackground error reading numbers file");
	    			return null;
	    		}
	    		result = (result + thisByte[0]) * 0.99999999;
	    	}
		} catch (FileNotFoundException e) {
			Log.d("SimpleComputeTask","doInBackground numbersFile: FileNotFoundException");
		} catch (IOException e) {
			Log.d("SimpleComputeTask","doInBackground numbersFile: IOException");
		}
    	

    	return(result);
    }
    
    @Override
    protected void onProgressUpdate(String... compressedFilePath) {
    	// Do nothing
    }
    
    @Override
    protected void onPostExecute(Double result) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onSimpleComputeFinished(result.doubleValue());
    }
    
    private static int safeLongToInt(long l) {
    	//See: http://stackoverflow.com/a/1590842/334402
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

}
