package org.mitre.opensextant.processing.output;

import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.os.ABIOutputStream;

public class ABIFormatter extends GISDataFormatter {

    
    /**
    *
    * @throws ProcessingException
    */
   public ABIFormatter() throws ProcessingException {
       this.outputExtension = ".none";
       this.outputType = "ABI";
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
       String name = FilenameUtils.getBaseName(getOutputFilepath());

       this.os = new ABIOutputStream(name);
   }

}
