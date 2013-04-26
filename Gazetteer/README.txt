 
 	How to use Kettle to process and populate the OpenSextant Gazetteer
 	
 	OpenSextant use the Kettle application (officially called "Pentaho Data Integration Community Edition" ) to process and transform
 	the publicly available gazetteer data into a clean consistent form suitable to be ingested into a Solr repository and used by the OpenSextant geotagger.
 	
 	Here's how to do that: 
 
 	1) Get and install Kettle
 		 Get it from http://kettle.pentaho.com/
 		 Download and unzip anywhere handy.
 		(Developed/tested with version "4.2.0-stable")
 	 You may want to give Kettle some more memory. To do so, edit <KETTLE_HOME>/Spoon.bat (or spoon.sh, whichever you intend to use) to increase the Java heap space
 			In either file, change 
 				PENTAHO_DI_JAVA_OPTIONS="-Xmx512m"
 			to
 				PENTAHO_DI_JAVA_OPTIONS="-Xmx1200m"	
 		
 	2) edit the build.xml
 	a) set the "opensextant.home" parameter to where you want the Solr gazetteer to be built. 	
 	b) set the "NGA_date" and "USGS_date" parameters (see build.xml for details)
 	c) set the "kettle.home" parameter to where you installed Kettle from step #1 above 
 	
 		
 	3) do the build: ant
 	 This will go fetch the data from the two websites (NGA and USGS), unpack and rename the files and place them in their respective
 	 subdirectories of Gazetteer/GazetteerETL/GeoData/. It will then the Kettle script (BuildMergedGazetteer.kjb) which will in clean, transform 
 and output the finished gazetteer data in Gazetteer/GazetteerETL/GeoData/Merged/Merged.txt (see Resources/UniversalGazetterModel.xlsx for the structure of this file)
 When this file is created, build.xml will then create a empty Solr Home, populate it with the contents of Merged.txt, build the required indices and then build
 the specialized matcher index (the FST) used by the geotagger. When that is all done (which can take 1-2 hrs for everything) you will have a ready-to-use OpenSextant gazetteer.
 
 	 	
 			 
 Structure of Gazetteer Project
 
 GazetteerETL 
 	BuildMergedGazetteer.kjb - the Kettle Job that does everything, it runs the Transformations below.
 	NGA to Universal.ktr - Kettle Transformation that cleans and transforms the NGA gazeteer data into GeoData/Merged/NGA.txt
 	USGS to Universal.ktr - Kettle Transformation that cleans and transforms the USGS gazeteer data into GeoData/Merged/USGS.txt
 	AdHoc to Universal.ktr - Kettle Transformation that cleans and transforms the user defined gazeteer data into GeoData/Merged/AdHoc.txt
 	EstimateBiases.ktr - Kettle Transformation that merges results of above three Transformations and adds some needed statistical mesasures to each gazetteer record.
 
 	GeoData - input (raw) gazetteer data
		AdHoc - An Excel spreadsheet with few entries to patch a hole in the big official gazetteers. Also, an example of adding your own gazeetter data
 		NGA - The data from NGA GeoNames (http://earth-info.nga.mil/gns/html/namefiles.htm) the "World File"
 		USGS - The data from USGS GNIS (http://geonames.usgs.gov/domestic/download_data.htm) in three separate files:
 			a) The "National File" (could also use one of the single state files)
 			b) The "Government Units" Topical Gazetteer
 			c) The "All Names" Topical gazetteer 	
	
	lib - a couple of jars we use in the processing
	
 	Logs - empty directory where output logs go. Each of the data transformation steps (NGA,USGS and AdHoc) will create logs for any duplicate and error(malformed/invalid) records found.
	
	Resources - data used in the cleaning, transformation and statistical estimation processes
 
		
		
solr - this directory contains an empty solr home which is copied and populated by the build process to create a ready-to-run solr based gazetteer 
 				
 