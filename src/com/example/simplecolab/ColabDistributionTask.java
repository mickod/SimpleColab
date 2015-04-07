package com.example.simplecolab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class ColabDistributionTask extends AsyncTask<String, String, Long> {
	/* This Class is an AsynchTask to send a numbers file chunk to a helper app over a 
	 * socket connection.
	 * 
	 */
	
	private ColabDistributionTaskListener thisTaskListener;
	private int chunkNumber;
	
	public ColabDistributionTask(ColabDistributionTaskListener ourListener) {
		//Constructor
		Log.d("ColabDistributionTask","constructor");
		
		//Set the listener
		thisTaskListener = ourListener;
	}
	
    @Override
    protected Long doInBackground(String... params) {
    	//This the key method that is executed in the Asynch task -it sends the numbers file chunk to the helper app
    	//over a socket connection.
    	Log.d("ColabDistributionTask","doInBackground");
    	
    	//Get the local numbers file path from the parameters
    	String numbersChunkFileName;
    	String helperIPAddress = null;
    	final int helperPort = 8080;
    	if (params.length == 3) {
	    	numbersChunkFileName = params[0];
	    	chunkNumber = Integer.parseInt(params[1]);
	    	helperIPAddress = params[2];
	    	Log.d("ColabDistributionTask doInBackground","numbersChunkFileName: " + numbersChunkFileName);
	    	Log.d("ColabDistributionTask doInBackground","chunkNumber: " + chunkNumber);	
	    	Log.d("ColabDistributionTask doInBackground","helperIPAddress: " + helperIPAddress);	
    	} else {
    		//One or all of the params are not present - log an error and return
    		Log.d("ColabDistributionTask doInBackground","One or all of the params are not present");
    		return null;
    	}
    	
    	//Send the numbers file to helper over a Socket connection 
    	Socket helperSocket = null;
    	   
    	try {
    		Log.d("ColabDistributionTask doInBackground","connecting to: " + helperIPAddress + ":" + helperPort);
    	    helperSocket = new Socket(helperIPAddress, helperPort);
    	    BufferedOutputStream helperSocketBOS = new BufferedOutputStream(helperSocket.getOutputStream());
    	    byte[] buffer = new byte[4096];
    	    
    	    //Write the numbers chunk file to the output stream
    	    File numbersChunkFIle = new File(numbersChunkFileName);
    	    BufferedInputStream chunkFileIS = new BufferedInputStream(new FileInputStream(numbersChunkFIle));
    	    
    	    //First send a long with the file length - wrap the BufferedOutputStream  in a DataOuputStream to
    	    //allow us send a long directly
    	    DataOutputStream helperSocketDOS = new DataOutputStream(
    	    	     new BufferedOutputStream(helperSocket.getOutputStream()));
    	    long chunkLength = numbersChunkFIle.length();
    	    helperSocketDOS.writeLong(chunkLength);
    	    Log.d("ColabDistributionTask doInBackground","chunkLength: " + chunkLength);
    	    
    	    //Now loop through the numbers chunk file sending it to the helper via the socket - note this will simply 
    	    //do nothing if the file is empty
    	    int readCount = 0;
    	    int totalReadCount = 0;
    	    while(totalReadCount < chunkLength) {
    	    	//write the buffer to the output stream of the socket
    	    	readCount = chunkFileIS.read(buffer);
    	    	helperSocketDOS.write(buffer, 0, readCount);
    	    	totalReadCount += readCount;
    	    }
    	    
    	    Log.d("ColabDistributionTask doInBackground","file sent");
    	    chunkFileIS.close();
    	    helperSocketDOS.flush();
    	} catch (UnknownHostException e) {
    		Log.d("ColabDistributionTask doInBackground","unknown host");
    	    e.printStackTrace();
    	    return null;
    	} catch (IOException e) {
    		Log.d("ColabDistributionTask doInBackground","IO exceptiont");
    	    e.printStackTrace();
    	    return null;
    	}
    	
    	//File has been sent - now need to wait for a response
    	long result = 0;
    	try {
    		Log.d("ColabDistributionTask doInBackground","Waiting for response");
			DataInputStream helperSocketDIS = new DataInputStream(helperSocket.getInputStream());
			
		    //The first part of the message should be the result 
		    result = helperSocketDIS.readLong();
		    Log.d("ColabDistributionTask doInBackground","result: " + result);
		    
		    //Tidy up
		    helperSocketDIS.close();			    
    	    helperSocket.close();
    	    
    	} catch (IOException e) {
    		Log.d("ColabDistributionTask doInBackground","IO exception getting response");
    	    e.printStackTrace();
    	} finally{
    		//Tidy up
    	    if(helperSocket != null){
    	    	try {
    	    		helperSocket.close();
    	    	} catch (IOException e) {
    	    		Log.d("ColabDistributionTask doInBackground","Error closing socket");
    	    		e.printStackTrace();
    	    	}
    	    }
    	}

    	//return the name of the compressed chunk file
    	return result;
    }
    
    @Override
    protected void onPostExecute(Long result) {
    	// Update the listener with the compressed video path
    	thisTaskListener.onChunkResultReady(chunkNumber, result);
    }

}

