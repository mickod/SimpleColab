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

public class SimpleComputeTask extends AsyncTask<String, Integer, Double> {
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
    	int computeIterations = 0;
    	if (params.length == 2) {
    		numbersFileName = params[0];
    		numbersFile = new File( Environment.getExternalStorageDirectory() + "/" + numbersFileName);
    		String iterationsString = params[1];
    		computeIterations = Integer.parseInt(iterationsString);
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
	    	Log.d("SimpleComputeTask","doInBackground numberfile length:" + numbersFile.length());
	    	Log.d("SimpleComputeTask","doInBackground numberfile size:" + numbersFileSize);
	    	
	    	for(int i=0; i<numbersFileSize; i++) {
	    		//Report progress every 100 loops
	    		if( i % 50 == 0 ){
	    			Log.d("SimpleComputeTask","doInBackground reporting progess i:" + i);
	    			publishProgress(i);
	    		}
	    		
	    		byte thisByte[] = new byte[1];
	    		int readResult = numbersFileIS.read(thisByte);
	    		Log.d("SimpleComputeTask","doInBackground i" + i + "thisByte[0]: " + thisByte[0]);
	    		if(readResult < 1) {
	    			Log.d("SimpleComputeTask","doInBackground error reading numbers file");
	    			return null;
	    		}
	    		double interim = 0;
	    		for(int j=0; j<computeIterations; j++){
	    			interim = Math.pow(thisByte[0], 2);
	    		}
	    		result = result + interim;
	    	}
		} catch (FileNotFoundException e) {
			Log.d("SimpleComputeTask","doInBackground numbersFile: FileNotFoundException");
		} catch (IOException e) {
			Log.d("SimpleComputeTask","doInBackground numbersFile: IOException");
		}
    	

    	return(result);
    }
    
    @Override
    protected void onProgressUpdate(Integer... iterationCount) {
    	//Report progress to listener
    	thisTaskListener.onSimpleCommputePorgressUpdate(iterationCount[0]);
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
