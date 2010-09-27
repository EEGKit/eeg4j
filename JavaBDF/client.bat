@echo off
IF %1.==. (SET host=127.0.0.1) 	ELSE (SET host=%1)
IF %2.==. (SET hostport=778) 	ELSE (SET hostport=%2)
IF %3.==. (SET channels=32) 	ELSE (SET channels=%3)

java -cp ./lib/JavaBDF.jar it.hakvoort.bdf.network.BDFClient %host% %hostport% %channels%