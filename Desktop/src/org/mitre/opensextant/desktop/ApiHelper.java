/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import javax.swing.JFrame;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Queue;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.swing.JOptionPane;
import org.mitre.opensextant.apps.OpenSextantRunner;

//TODO class name is quickly becoming nonsensical
//TODO should break up threads and OpenSextant container classes
public class ApiHelper implements Runnable {
  public static final String BASE_PATH = "output"; //TODO: Make relative 
  public static final String TMP_ADDENDUM = "tmp";
  public static final long HALF_GIGABYTE = 536870912L;
  public static final int NOT_ON_LIST = -1;
  
  private static Logger log = LoggerFactory.getLogger(ApiHelper.class);
    
  // Need to keep track of memory usage of the JVM
  private static Runtime runtime = Runtime.getRuntime(); 
 
  // Keep track of the number of raw text entries to avoid overwriting each
  private static int textCount = 0;
  private static boolean initialized = false;
  
  // Keep track of the ordering on the gui list
  private int guiEntry = NOT_ON_LIST;
  
  // Variables for the current running of this thread
  private String inputFile = null;
  private String outputType = null;
  private String outputLocation = null;
  
  // Handle the OpenSextantRunner and save it for reuse
  private boolean inUse = false;
  private OpenSextantRunner runner;
  
  // Just using a simple list and queue to maintain threads
  // Should not need a thread-safe collection since the spawning goes through
  //   a single static entry point...probably
  public static ArrayList<ApiHelper> threadList = new ArrayList<ApiHelper>();
  public static Queue<ApiHelper> waitingList = new LinkedList<ApiHelper>();
  
  public ApiHelper(String inputFile, String outputType, String outputLocation) {
    this.inputFile = inputFile;
    this.outputType = outputType;
    this.outputLocation = outputLocation;
  }
  
  // Getters and Setters
  public String getInputFile() { return inputFile; }
  public String getOutputType() { return outputType; }
  public String getOutputLocation() { return outputLocation; }
  public void setInputFile(String inputFile) { this.inputFile = inputFile; }
  public void setOutputType(String outputType) { this.outputType = outputType; }  
  public void setOutputLocation(String outputLocation) { 
    this.outputLocation = outputLocation; 
  } 
  
  // Want to set that the thread is used ASAP
  // Would be smarter to mutex lock it, but since this is humans going through
  // a load screen the chance of a collision is slim
  public void setInUse(){
     inUse = true;
  }
  
  public boolean isInUse() {
    return inUse;
  }
  
  // TODO: Split up this functionality to be clearer
  public void run() {
    boolean stillRun = true;
    while(stillRun) {
      try {
        // Craft an output file name from input file
        // Test both types of slashes in case it was entered by hand
        int lastSlash = inputFile.lastIndexOf('/');
        if(lastSlash < 0) lastSlash = inputFile.lastIndexOf('\\');
        int period = inputFile.lastIndexOf('.');
        if(lastSlash < 0) lastSlash = 0;
        else lastSlash ++;
        if(period < 0) period = inputFile.length();
        String dateStr = "_" + (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss"))
                       .format(new Date());
        String outName = inputFile.substring(lastSlash, period);
        
        // Put in the table gui table  
        String status = "Processing 1 file";
        String inType = "FILE";
        String outputTypePrime = outputType;
        if("KML".equals(outputType)) outputTypePrime = "KMZ";
        String fullLoc = outputLocation + File.separator+ (outName + dateStr) 
                       + "." + outputTypePrime;
        guiEntry = OpenSextant.addRow("Initializing..."
                                      , fullLoc
                                      , outName
                                      , inType
                                      , inputFile
                                      );
        outName += dateStr;

        // TODO: This should only happen once
        // Initialize the runner
       // if(runner == null) {
        while(!Initialize.getInitialized()) {
          try{Thread.sleep(500);}
          catch(Exception e){ e.printStackTrace(); }
        }
          runner = new OpenSextantRunner();
          runner.initialize();
       // }
        
        OpenSextant.updateProgress(guiEntry, "Processing...", 0);
      
       // } 
        
        // Actually run the mess
        runner.runOpenSextant( inputFile, outputType
                             , outputLocation + File.separator + outName );
        
      } catch (Exception e) { e.printStackTrace(); } //TODO:Fortify will dislike
      inUse = false;
      OpenSextant.updateProgress(guiEntry, "Finished", 100);
      
      // Test if something is in the queue and reuse the thread if it is
      ApiHelper tmp = waitingList.poll();
      if(tmp != null) { 
        stillRun = true;
        inputFile = tmp.getInputFile();
        outputType = tmp.getOutputType();
        outputLocation = tmp.getOutputLocation();
      } else stillRun = false;

    }
  }
  
  public static void processText(String text) { 
    String file = BASE_PATH + File.separator + TMP_ADDENDUM  + File.separator 
                + "textEntry" + (textCount++) + ".txt";
    boolean wroteFile = false;
    try {
      FileWriter outWriter = new FileWriter(file);
      PrintWriter out = new PrintWriter(outWriter);
      out.println(text);
      out.close();
      wroteFile = true;
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage());
    } catch (IOException ex) {
      log.error(ex.getMessage());
    }
    
    if(wroteFile) processFile(file);
  }
  
  public static ApiHelper freeInstance() {
    ApiHelper ret = null;    
    
    for( ApiHelper t : threadList)
      if(!t.isInUse()) ret = t;
    
    System.out.println("Returning: " + ret);
    
    if(ret != null) ret.setInUse();
    return ret;
  }
  
  public static void processFile(String file) {
    boolean canRunNow = false;

    // Check if we have free threads or memory to run in
    ApiHelper tmp = freeInstance();
    String outType = Config.getOutType();
    String outLoc = Config.getOutLocation();
    if( tmp != null){ 
        canRunNow = true;
        tmp.setInputFile(file);
        tmp.setOutputType(outType);
        tmp.setOutputLocation(outLoc);
    } else if(runtime.freeMemory() >= HALF_GIGABYTE) {
      tmp = new ApiHelper(file, outType, outLoc); 
      tmp.setInUse();
      threadList.add(tmp);
      canRunNow = true;
    }

    if(canRunNow) (new Thread(tmp)).start();
    else waitingList.offer(tmp);
  }
}
