
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

 SolrTextTagger    -- a text tagging solution for high-volume word lists or data sets

 GISCore   -- a peer project; this API manages GIS data formats

 OpenSextantToolbox
 Gazetteer
 LanguageResources

 additional content:
  doc          -- Documentation, user manuals, developer guides
  Testing      -- (RELEASE TBD) test data and programs to give you ideas of the possible.
  GeocoderEval -- (RELEASE TBD) we've developed a framework and ground truth for  evaluating OpenSextant and other geotaggers



 Getting Started Using OpenSextant
 *********************************

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

  mvn install

  ## Commons, FlexPax, and Xponents are build using Maven; more modules to come.

  cd ./OpenSextantToolbox

  # see that things compile
  ant

  # the release step compiles all modules and prepares a release.
  ant release



 Building Offline
 ****************
 Some of OpenSextant's components are built using Maven.  Maven downloads dependencies and build tools on the fly which
 is convenient for development, but not on a system disconnected from the Internet.  Fortunately it is possible to
 download all of the dependencies beforehand and copy them onto the disconnected system manually.

 From a system with a working Maven 3 installation and a connection to the MII take the following steps:
 1. Go to the trunk directory of the opensextant checkout.
 2. mkdir offline_deps
 3. mvn -Dmaven.repo.local=offline_deps --fail-fast clean install dependency:copy-dependencies

 This will download all of the jars necessary for building the project and place them into offline_deps.  This folder
 can then be used as a local repository for building the ui-ajaxsolr project.  By default the local repository belongs
 in ~/.m2/repository/ .  If you copy the contents of offline_deps into ~/.m2/repository/ then you can build the project
 by using --offline argument when running Maven.

 Note that there is a plugin for downloading dependencies, org.apache.maven.plugins:maven-dependency-plugin:2.5.1:go-offline,
 however it often misses plugin dependencies and thus is not useful for our purposes.

