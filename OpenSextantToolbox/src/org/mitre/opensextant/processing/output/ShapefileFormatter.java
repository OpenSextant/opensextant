/** 
 Copyright 2009-2013 The MITRE Corporation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


 * **************************************************************************
 *                          NOTICE
 * This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
**/
package org.mitre.opensextant.processing.output;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.opensextant.giscore.DocumentType;
import org.opensextant.giscore.GISFactory;
import org.mitre.opensextant.processing.ProcessingException;

/**
 * A results formatter for shapefile output.
 *
 * @author Rich Markeloff, MITRE Corp. Initial version created on Jan 6, 2012
 */
public final class ShapefileFormatter extends FolderGISDataFormatter {

    /**
     *
     * @throws ProcessingException
     */
    public ShapefileFormatter() throws ProcessingException {
        this.outputExtension = "_shp";
        this.doc_type = DocumentType.Shapefile;
        this.outputType = "Shapefile";
    }

    /**
     * Create the output stream appropriate for the output type.
     * @param outFilename 
     * @throws Exception 
     */
    @Override
    protected void createOutputStreams() throws Exception {

        File shp = new File(getOutputFilepath());

        checkOverwrite(shp); // cleanly delete first.        
        shp.mkdirs();          // now recreate.

        _temp = createTempFolder(this.outputType);
        File zipfile = new File(_temp + File.separator + shp.getName() + ".zip");
        FileOutputStream fos = new FileOutputStream(zipfile);

        this.zos = new ZipOutputStream(fos);

        this.os = GISFactory.getOutputStream(DocumentType.Shapefile, this.zos, shp);
    }
}
