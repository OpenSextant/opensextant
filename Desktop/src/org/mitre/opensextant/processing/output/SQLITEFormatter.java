package org.mitre.opensextant.processing.output;

import java.io.File;

import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.os.SQLITEGISOutputStream;

public class SQLITEFormatter extends GISDataFormatter {

    
    /**
    *
    * @throws ProcessingException
    */
   public SQLITEFormatter() throws ProcessingException {
       this.outputExtension = ".db";
       this.outputType = "SQLITE";
       this.includeOffsets = true;
       this.includeCoordinate = true;
   }

   /**
    * Create the output stream appropriate for the output type.
    * @param outFilename 
    * @throws Exception 
    */
   @Override
   protected void createOutputStreams() throws Exception {

       File sqlite = new File(getOutputFilepath());
       checkOverwrite(sqlite);
       
       this.os = new SQLITEGISOutputStream(sqlite);
   }

}
