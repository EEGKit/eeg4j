@echo off

IF %1.==. (SET arg1=./data/bdf/example.bdf) 	ELSE (SET arg1=%1)
IF %2.==. (SET arg2=778) 			ELSE (SET arg2=%2)

java -cp ./lib/JavaBDF.jar it.hakvoort.bdf.network.BDFServer %arg1% %arg2% %3 %4