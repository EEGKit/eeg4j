#!/bin/bash
if [ -n "$1" ]; then arg1=$1; else arg1="./data/bdf/example.bdf"; fi
if [ -n "$2" ]; then arg2=$2; else arg2=1778; fi

java -cp ./lib/JavaBDF.jar it.hakvoort.bdf.network.BDFServer $arg1 $arg2 $3 $4