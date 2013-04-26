export PENTAHO_DI_JAVA_OPTIONS="-Xmx1500m"

dir=`dirname $0`
GAZ_HOME=`cd -P $dir/..; pwd`

# pick a favorite install
MY_KETTLE=/app/Kettle/data-integration
# set MY_KETTLE=C:\app\Kettle\data-integration
#export MY_KETTLE=$HOME/Tools/Kettle/data-integration


OUTPUT=$GAZ_HOME/GazeteerETL/GeoData/Merged
if [ -d $OUTPUT ] ; then 
   echo "Creating $OUTPUT to store output gazetteer"
   mkdir -p $OUTPUT
fi

GAZ_ETL=$GAZ_HOME/GazetteerETL/BuildMergedGazetteer.kjb
$MY_KETTLE/kitchen.sh -file $GAZ_ETL
