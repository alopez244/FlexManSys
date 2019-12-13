@rem set reg=0
@:inicio
java -Dlog4j.configurationFile=file:log4j2.xml -cp bin;lib\log4j-api-2.3.jar;lib\log4j-core-2.3.jar;log4j-api-2.3.jar;lib\log4j-core-2.3.jar;lib\commons-codec-1.3.jar;lib\commons-io-2.6.jar jade.Boot -container sa:es.ehu.SystemModelAgent
TIMEOUT /T 0
@goto inicio
