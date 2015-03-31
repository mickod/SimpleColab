package com.example.simplecolab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, SimpleComputeTaskListener{
	
	private Button localCompute;
	private Button colabCompute;
	private Button generateFile;
	private long simpleComputeStartTime;
	private long colabComputeStartTime;
	private long totalElapsedTime;
	private final String numberFileName = "numberFile.txt";
	private final int numberOfHelpers = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Add Button Listeners
        localCompute = (Button) findViewById(R.id.local_compute_button);
        colabCompute = (Button) findViewById(R.id.colab_compute_button);
        generateFile = (Button) findViewById(R.id.generate_file_button);
        localCompute.setOnClickListener(this);
        colabCompute.setOnClickListener(this);
        generateFile.setOnClickListener(this);
        
        //Set the result, progress and time to blank
        clearDisplays();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
    public void onClick(View v) {
		//Handle all button clicks on this fragment
    	Log.d("MainActivity","onClick");
    	
    	if(v == findViewById(R.id.local_compute_button)) {
    		//local compute Button - start timing and local compute
    		Log.d("MainActivity","onClick local compute Button");
    		simpleComputeStartTime = System.nanoTime();
    		totalElapsedTime = 0;
    		clearDisplays();
			SimpleComputeTask simpleComputeTask = new SimpleComputeTask(this, this);
			simpleComputeTask.execute(numberFileName);
		} else if (v == findViewById(R.id.colab_compute_button)) {
			//Colaborative compute button
			Log.d("MainActivity","onClick colaborative compute Button");
    		colabComputeStartTime = System.nanoTime();
    		totalElapsedTime = 0;
    		clearDisplays();
    		
    		File numbersFile = new File(Environment.getExternalStorageDirectory()+ numberFileName);
    		long fileLength = numbersFile.length();
    		if (fileLength > ((Integer.MAX_VALUE - 1) * numberOfHelpers)) {
    			//File size is too big as chunks are restricted to the max size of Integer
    			Log.d("MainActivity onClick","File size is too big as chunks are restricted to the max size of Integer");
    			return;
    		}
    		
    		//Split file into chunks
    		int chunkSize = (int) fileLength/numberOfHelpers;
		    long totalCount = 0;
		    int previousChunkEnd = 0;
		    int[] chunkBoundary = new int[numberOfHelpers];
    		for (int i=0; i < numberOfHelpers; i++) {
    			try { 			    
    			    //Create the Chunk file
    			    String numbersChunkFileName = "numbersChunk" + i + ".mp4";
    				File numbersChunkFile = new File(Environment.getExternalStorageDirectory() +  "/" + numbersChunkFileName);
    				if(numbersChunkFile.exists()) {
    					//Delete the file and create a new one
    					boolean fileDeleted = numbersChunkFile.delete();
    					if (!fileDeleted) {
    						//log error and return
    						Log.d("MainActivity onClick","numbersChunkFile: old file not deleted");
    						return;
    					}
    				}
    				
    				//Read in the bytes for this chunk file
    	    		chunkBoundary[i] = chunkSize * i;
    				BufferedInputStream numbersBIS = new BufferedInputStream(new FileInputStream(numbersFile));
    				final int bufferSize = 1024;
    			    byte[] bytes = new byte[bufferSize];
    				BufferedOutputStream chunkFileBOS = new BufferedOutputStream(new FileOutputStream(numbersChunkFile));
    			    int thisReadCount = 0;
    			    while ( (thisReadCount = numbersBIS.read(bytes, previousChunkEnd, chunkSize)) > 0) {
    			    	chunkFileBOS.write(bytes);
    			    }
    			    previousChunkEnd = chunkBoundary[i];

    			    Log.d("MainActivity onClick","chunkBoundary[i]: " + chunkBoundary[i]);
    			    Log.d("MainActivity onClick","fileName: " + numbersChunkFileName);
    			    Log.d("MainActivity onClick","fileSize: " + numbersChunkFile.length());

    			    //Tidy up
    			    chunkFileBOS.flush();
    			    chunkFileBOS.close();
    			    numbersBIS.close();	
    			    
    			    //Distribute this file to the helper
    			    ColabDistributionTask colabDistributionTask = new ColabDistributionTask(this, this);
    				colabDistributionTask.execute(numberFileName);
    			    
    			} catch (IOException e) {
    	    		Log.d("MainActivity onClick","IO exception creating chunk files");
    	    	    e.printStackTrace();
    	    	}
    		} 
		}
	}
	
	private void clearDisplays() {
		//Clear the timing display
        TextView resultTextView = (TextView) findViewById(R.id.result);
        resultTextView.setText("");
        TextView timeTextView = (TextView) findViewById(R.id.time);
        timeTextView.setText("");
        TextView progressTextView = (TextView) findViewById(R.id.progress);
        progressTextView.setText("");
	}


	@Override
	public void onSimpleComputeFinished(int result) {
		//Compute finished
		
		//Stop timing and update time
		long endTime = System.nanoTime();
		totalElapsedTime = endTime - simpleComputeStartTime;
		TextView timeTextView = (TextView) findViewById(R.id.time);
    	String computeTimeString = new DecimalFormat("0.000000").format(totalElapsedTime/1000000000.0);
    	timeTextView.setText(computeTimeString);
        
        //Update the result
        TextView resultTextView = (TextView) findViewById(R.id.result);
        resultTextView.setText(result);
	}


	@Override
	public void onSimpleCommputePorgressUpdate() {
		// Ignore
		
	}

    
}
