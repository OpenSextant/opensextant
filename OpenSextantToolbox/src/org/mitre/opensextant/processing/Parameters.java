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
package org.mitre.opensextant.processing;

import java.util.HashSet;
import java.util.Set;
import org.mitre.opensextant.util.TextUtils;

/**
 * A property sheet.
 * 
 * For now I'm using attributes directly to facilitate compile-time stuff.
 * But a property sheet of k,v pairs may help later 
 * 
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class Parameters extends java.util.Properties {

    public boolean tag_countries = true;
    public boolean tag_places = true;
    public boolean tag_coordinates = true;
    /** By default Country Names will not be included in GIS products 
     *  They should appear in CSV, though.
     */
    public boolean output_countries = true;
    public boolean output_places = true;
    public boolean output_coordinates = true;

    /** Is the concept of duplicate filtering more general than for just coords?
     */
    public boolean output_coordinate_duplicates = false;
    
    public String tempDir = "/tmp";
    public String outputDir = ".";
    // This is basically the file name for the output.
    private String jobName = null;
    public String inputFile = null;
    public String outputFile = null;

    private Set<String> formats = new HashSet<>();
    /**  You the caller must explicitly set isdefault = false;
     *   forcing you to actually look at these parameters.
     */
    public boolean isdefault = true;
    public final static int FLAG_EXTRACT_CONTEXT = 0x10;
    public final static int FLAG_NO_COORDINATES  = 0x20;  
    
    /* DEFAULT RUNTIME FLAGS: */
    public static int RUNTIME_FLAGS = FLAG_EXTRACT_CONTEXT;

    /** Processing will support multiple output formats 
     */
    public void addOutputFormat(String fmt) {
        formats.add(fmt);
    }

    public Set<String> getOutputFormats() {
        return formats;
    }

    public final static String INVALID_FCNAME_CHAR = "\\/+-.;, $&"; // Basically only ASCII A-Z, 0-9 are valid.
    
    public void setJobName(String nm) {
        jobName = TextUtils.fast_replace(nm, INVALID_FCNAME_CHAR, "_");
    }
    
    public String getJobName(){
        return jobName;
    }
}
