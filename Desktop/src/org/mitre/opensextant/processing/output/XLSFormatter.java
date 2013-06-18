package org.mitre.opensextant.processing.output;

import java.io.File;

import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.GISDataFormatter;
import org.mitre.opensextant.processing.output.os.ExcelGISOutputStream;

public class XLSFormatter extends GISDataFormatter {

	protected boolean isIdentifiers = false;
    
    /**
    *
    * @throws ProcessingException
    */
   public XLSFormatter() throws ProcessingException {
       this.outputExtension = ".xls";
//       this.doc_type = 
       this.outputType = "XLS";
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

       File xls = new File(getOutputFilepath());

       checkOverwrite(xls);
       
       this.os = new ExcelGISOutputStream(xls, "data", isIdentifiers);
   }
	
}
