# Copyright 2009-2013 The MITRE Corporation.
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
#
# * **************************************************************************
# *                          NOTICE
# * This software was produced for the U. S. Government under Contract No.
# * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
# * Software and Noncommercial Computer Software Documentation Clause
# * 252.227-7014 (JUN 1995)
# *
# * (c) 2012 The MITRE Corporation. All Rights Reserved.
# * **************************************************************************
#


dir=`dirname $0`
# install=`cd -P $dir/..; echo $PWD`

OPENSEXTANT_HOME=/mitre/opensextant
SOLR_HOME=$OPENSEXTANT_HOME/solr
GATE_HOME=$OPENSEXTANT_HOME/gate

CLASSPATH=
for f in $OPENSEXTANT_HOME/lib/*jar ; do 
  CLASSPATH=${f}:$CLASSPATH
done

CLASSPATH=${GATE_HOME}:$CLASSPATH
export CLASSPATH

echo "Running with Gate @ $GATE_HOME"
echo "Using GAPP @  $GATE_HOME/OpenSextant_Solr.gapp"
if [ ! -f  $GATE_HOME/OpenSextant_Solr.gapp ] ; 
  then echo "File does not exist" ; exit 1;
fi


java -Xmx1500m \
 -Dgate.home=$GATE_HOME \
 -Dgate.user.config=$GATE_HOME\user-gate.xml \
 -Dgate.plugins.home=$GATE_HOME/plugins \
 -Dgate.user.session='x' \
 -Dsolr.solr.home=$SOLR_HOME\
 org.mitre.opensextant.apps.OpenSextantGUI \
 -d Line \
 -i ./test/test.txt \
 -o ./test/test.txt.csv \
 -t CSV \
 -g $GATE_HOME/OpenSextant_Solr.gapp
 
