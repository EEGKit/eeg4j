#!/bin/bash
if [ -n "$1" ]; then host=$1; else host=127.0.0.1; fi
if [ -n "$2" ]; then hostport=$2; else hostport=778; fi
if [ -n "$3" ]; then channels=$3; else channels=32; fi
if [ -n "$4" ]; then port=$4; else port=1778; fi

java -cp ./lib/JavaBDF.jar it.hakvoort.bdf.network.BDFBroadcast $host $hostport $channels $port