package com.example.simplecolab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Random;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, SimpleComputeTaskListener, ColabDistributionTaskListener {
	
	private Button localCompute;
	private Button colabCompute;
	private Button generateFile;
	private long simpleComputeStartTime;
	private long colabComputeStartTime;
	private long totalElapsedTime;
	private final String numberFileName = "numberFile.txt";
	private boolean[] chunkFinished;
	private long totalResult;
	private final int numberOfHelpers = 2;
	private final String helperIPAddresses[] = {"192.168.1.66", "192.168.1.171", "192.168.1.10"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Keep screen on
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        //Add Button Listeners
        localCompute = (Button) findViewById(R.id.local_compute_button);
        colabCompute = (Button) findViewById(R.id.colab_compute_button);
        generateFile = (Button) findViewById(R.id.generate_file_button);
        localCompute.setOnClickListener(this);
        colabCompute.setOnClickListener(this);
        generateFile.setOnClickListener(this);
        
        //Set the result, progress and time to blank
        clearDisplays();
        
        //Set the inital file size to 1000 and hide the keyboard
    	EditText fileSizeEditText = (EditText) findViewById(R.id.file_size);
    	fileSizeEditText.setText("1000");
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    	
        //Set the inital iteration count to 1000 and hide the keyboard
    	EditText iterationCountEditText = (EditText) findViewById(R.id.iteration_count);
    	iterationCountEditText.setText("10000");
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
        	EditText iterationCountEditText = (EditText) findViewById(R.id.iteration_count);
        	String computeIterations = iterationCountEditText.getText().toString();
			SimpleComputeTask simpleComputeTask = new SimpleComputeTask(this, this);
			simpleComputeTask.execute(numberFileName, computeIterations);
		} else if (v == findViewById(R.id.colab_compute_button)) {
			//Colaborative compute button
			Log.d("MainActivity","onClick colaborative compute Button");
    		colabComputeStartTime = System.nanoTime();
    		totalElapsedTime = 0;
    		totalResult = 0;
    		clearDisplays();
    		
    		File numbersFile = new File(Environment.getExternalStorageDirectory()+ numberFileName);
    		long fileLength = numbersFile.length();
    		if (fileLength > ((Integer.MAX_VALUE - 1) * numberOfHelpers)) {
    			//File size is too big as chunks are restricted to the max size of Integer
    			Log.d("MainActivity onClick","File size is too big as chunks are restricted to the max size of Integer");
    			return;
    		}
    		
    		//Split file into chunks
    		for (int i=0; i<numberOfHelpers; i++) {
    			//Reset the chunk finished flags
    			chunkFinished[i] = false;
    		}
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
    			    EditText iterationCountEditText = (EditText) findViewById(R.id.iteration_count);
    	        	String computeIterations = iterationCountEditText.getText().toString();
    			    ColabDistributionTask colabDistributionTask = new ColabDistributionTask(this);
    				colabDistributionTask.execute(numberFileName, String.valueOf(i), helperIPAddresses[i], computeIterations);
    			    
    			} catch (IOException e) {
    	    		Log.d("MainActivity onClick","IO exception creating chunk files");
    	    	    e.printStackTrace();
    	    	}
    		} 
		} else if (v == findViewById(R.id.generate_file_button)) {
			//Generate file button
			Log.d("MainActivity","onClick generate_file_button");
			
			//Create the numbers file
	    	String numbersFilePath;
	    	File numbersFile;
	    	numbersFile = new File( Environment.getExternalStorageDirectory() + "/" + numberFileName);
	    	if(numbersFile.exists()) {
				//Delete the file
				boolean fileDeleted = numbersFile.delete();
				if (!fileDeleted) {
					//log error and return
					Log.d("MainActivity onClick","numbersFile: old file not deleted");
					return;
				}
			}
	    	
	    	BufferedOutputStream numberFileBOS;
	    	try {
				numberFileBOS = new BufferedOutputStream(new FileOutputStream(numbersFile));
	    	} catch (IOException e) {
				Log.d("MainActivity onClick","numbersFile: error !!!!!! numbers file");
				e.printStackTrace();
			}
	    	
	    	//Create the new file
			try {
				numbersFile.createNewFile();
				Log.d("MainActivity onClick","numbersFile: created new numbers file");
			} catch (IOException e) {
				Log.d("MainActivity onClick","numbersFile: error creating numbers file");
				e.printStackTrace();
			}
	    	
	    	//Get the file size
	    	EditText fileSizeEditText = (EditText) findViewById(R.id.file_size);
	    	int numbersFileSize = Integer.parseInt(fileSizeEditText.getText().toString());
	    	
	    	//Fill numbers file with random numbers
	    	//BufferedOutputStream numberFileBOS;
			try {
				numberFileBOS = new BufferedOutputStream(new FileOutputStream(numbersFile));

		    	Random rand = new Random();
		    	for(int i =0; i < numbersFileSize; i++) {
		    		//Write random integer, between 0 and 255 to numbers file 
		    		int randInt = rand.nextInt(255 - 0 + 1) + 0; //random.nextInt(max - min + 1) + min
		    		//Write the value 11 to the numbers file in every position
		    		numberFileBOS.write((byte)11);
		    	}
		    	
		    	numberFileBOS.flush();
		    	numberFileBOS.close();
			} catch (FileNotFoundException e) {
				Log.d("MainActivity onClick","numbersFile: FileNotFoundException");
			} catch (IOException e) {
				Log.d("MainActivity onClick","numbersFile: IOException");
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
	public void onSimpleComputeFinished(double result) {
		//Compute finished
		
		//Stop timing and update time
		long endTime = System.nanoTime();
		totalElapsedTime = endTime - simpleComputeStartTime;
		TextView timeTextView = (TextView) findViewById(R.id.time);
    	String computeTimeString = new DecimalFormat("0.000000").format(totalElapsedTime/1000000000.0);
    	timeTextView.setText(computeTimeString);
        
        //Update the result
        TextView resultTextView = (TextView) findViewById(R.id.result);
        String resultString = new DecimalFormat("0.000000").format(result/1000000000.0);
        resultTextView.setText(resultString);
	}
	
	@Override
	public void onChunkResultReady(int chunkNumber, long result) {
		//Called when a result computed from a chunk of the number files is ready
		Log.d("MainActivity onChunkResultReady","chunkNumber: " + chunkNumber);
		Log.d("MainActivity onChunkResultReady","result: " + result);
		
		//Add the result and note that this chunk is finished
		if ( chunkNumber >= 0 && chunkNumber < numberOfHelpers) {
			chunkFinished[chunkNumber] = true; 
			totalResult += result;
		} else {
			//Invalid chunk number for some reason...
			Log.d("MainActivity onChunkResultReady","invlaid chunk number received");
			return;
		}
		
		//Check if we have all the chunks yet by - if not just return
		for (int j = 0; j < numberOfHelpers; j++) {
			if (chunkFinished[j] !=true) {
				Log.d("MainActivity onChunkResultReady","All chunk results not yet received - returning");
				return;
			}
		}
		
		//All Chunks received - display time and results. Time first...
		long endTime = System.nanoTime();
		totalElapsedTime = endTime - colabComputeStartTime;
		TextView timeTextView = (TextView) findViewById(R.id.time);
    	String colabComputeTimeString = new DecimalFormat("0.000000").format(totalElapsedTime/1000000000.0);
    	timeTextView.setText(colabComputeTimeString);
        
        //Update the result
        TextView resultTextView = (TextView) findViewById(R.id.result);
        String totalResultString = new DecimalFormat("0.000000").format(totalResult/1000000000.0);
        resultTextView.setText(totalResultString);
	}


	@Override
	public void onSimpleCommputePorgressUpdate(int iterationCount) {
		//Update the progess display
		Log.d("MainActivity onChunkResultReady","progess report interationCount:" + iterationCount);
        TextView progressTextView = (TextView) findViewById(R.id.progress);
        progressTextView.setText(String.valueOf(iterationCount));
		
	}

    
}
