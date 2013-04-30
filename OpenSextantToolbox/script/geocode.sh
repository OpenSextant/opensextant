#!/bin/sh 
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

usage() {
   echo "Usage: $0 <input> <output> <format>"
   echo "  where "
   echo "    input is an input file or folder"
   echo "    output is an output file or folder; depends on format"
   echo "    format is the format of your output: one of GDB, CSV, Shapefile, WKT, KML"
}

dir=`dirname $0`
install=`cd -P $dir/..; echo $PWD`

DATA_IN=$1
GEOCODE_OUT=$2
FORMAT=$3

if [ -z "$DATA_IN" -o -z "$GEOCODE_OUT" -o -z "$FORMAT" ] ; then 
   usage;
fi

ant -f ${install}/script/opensextant-ant.xml -Dinputfile=$DATA_IN -Doutputfile=$GEOCODE_OUT -Dformat=$FORMAT  run-solr

