:inicio
java -Dlog4j.configurationFile=file:log4j2.xml -cp bin;lib\log4j-api-2.3.jar;lib\log4j-core-2.3.jar;log4j-api-2.3.jar;lib\log4j-core-2.3.jar;lib\commons-codec-1.3.jar jade.Boot -container reg-%random%:es.ehu.platform.agents.ProcNodeAgent("System=system","description=description","refServID=id55")
TIMEOUT /T 4
goto inicio