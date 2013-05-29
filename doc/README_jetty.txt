
Running with Jetty or Tomcat
============================


setup your server:

  For development purposes on any platform there is no standard location for Jetty. 
  Since this is an installed version of Jetty with a specific runtime setup,  Unzip and rename the Jetty distro:

  NOTE: Gazetteer/build.xml offers all this as "create-server" target

  mkdir <myapps>/runtime
  unzip jetty-<distro>.zip -d <myapps>/runtime/jetty

  copy solr.war to jetty/webapps (current Solr WAR from Gazetteer/solr)

  copy JTS JAR to jetty/lib/ext


