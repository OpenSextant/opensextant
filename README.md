OpenSextant
===========
The Open Spatial Extraction and Tagging (OpenSextant) software provides an unstructured textual data geotagging and geocoding capability. The U.S. Government Joint Improvised Explosive Device Defeat Organization (JIEDDO) developed this capability in coordination with other U.S. government agencies and is pleased to provide this as open source software using an Apache 2.0 license. The software relies upon the open source General Architecture for Text Engineering (GATE) natural language processing software and the Apache Solr search software. Please see below for instructions on how to access the source code and binaries. 




OpenSextant Suite
=================

 This suite is various projects for geospatial and temporal extraction.
 The core module is OpenSextantToolbox which produces a GATE plugin and 
 a toolkit for controlling the overall extraction and geocoding pipeline using that plugin.

 Modules
 *******
 Commons     -- Common parent classes, data model and core utilities. TBD

 Xponents    -- Extractors
   + XText   document conversion (to plain text)
   + XCoord  coordinate extraction
   + XTemporal  date/time extraction
   + FlexPat

 OpenSextantToolbox  -- A GATE-based plugin and various main programs for geotagging/geocoding
 Gazetteer           -- A Solr-based gazetteer supporting mainly NGA Geonames, USGS place data, and adhoc catalogs.
 LanguageResources   -- Linguistic tuning data
 doc                 -- Documentation, user manuals, developer guides



 Peer Projects
 *************
 SolrTextTagger    -- A text tagging solution for high-volume word lists or data sets

 GISCore           -- An API manages GIS data formats.  
   + geodesy  geodetic primitives and routines used by OpenSextant and GISCore
   + giscore  the main GISCore API which supports IO and data manipulation on GIS data



 additional content:
  Testing      -- (RELEASE TBD) test data and programs to give you ideas of the possible.
  GeocoderEval -- (RELEASE TBD) we've developed a framework and ground truth for  evaluating OpenSextant and other geotaggers



Getting Started Using OpenSextant
==================================

  In the OpenSextant binary distribution you will find ./script/default.env
  It contains OPENSEXTANT_HOME and other useful shell settings.  WinOS version is TBD.

  To Geocode files and folders please use the reference script:

      $OPENSEXTANT_HOME/script/geocode.sh   <input> <output> <format>

  where 
    input is an input file or folder
    output is an output file or folder; depends on format
    format is the format of your output: one of GDB, CSV, Shapefile, WKT, KML

  
 Getting Started Integrating OpenSextant
 *********************************
 Javadoc is located at OPENSEXTANT_HOME/doc/javadoc ; 
 Typical adhoc integration will be through the o.m.o.apps.SimpleGeocoder class, which 
 leverages o.m.o.processing.TextInput on input and GeocodingResult/Geocoding as output classes.

 Integration documentation is in progress, as of April 2013.

 The main library JARs of interest are:

   OpenSextantToolbox.jar 
   opensextant-apps.jar
   opensextant-commons.jar

 And the various Xponents:
   xtext*jar
   xcoord*jar
   xtemporal*jar
   flexpat*jar
   
 As of release time 2013-Q1, we are working on documenting and honing dependencies with other
 libraries, as well as our internal dependencies.



 Getting Started Developing OpenSextant
 *********************************

  For more information see 
     ./doc/OpenSextantToolbox/doc/OpenSextant Developers Guide.docx

  ## Set your maven proxy settings;  see ./doc/developer/ for hints.
  ## Ensure that JAVA_HOME environment variable is pointed at a Java 7 JDK.
  ## Otherwise you may encounter Javadoc and/or compilation errors.

  ## In the source tree, run "ant".  This will build the various required components and build a release
  cd ./opensextant

  # see that things compile
  ant compile

  # the release step compiles all modules and prepares a release.
  ant release

  
  Alternatively, Maven can be used to build Commons, Xponents, and SolrTextTagger.  For example:

     cd Xponents
     mvn install 

  But complete Maven build support is not planned at this time.



