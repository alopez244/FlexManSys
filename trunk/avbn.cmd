@:inicio
java -Dlog4j.configurationFile=file:log4j2.xml -cp bin;lib\log4j-api-2.3.jar;lib\log4j-core-2.3.jar;log4j-api-2.3.jar;lib\log4j-core-2.3.jar;lib\commons-codec-1.3.jar jade.Boot -container avbm:es.ehu.AvailabilityManager
TIMEOUT /T 1
@goto inicio

152.78
