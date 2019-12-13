@:inicio
java -Dlog4j.configurationFile=file:log4j2.xml -cp bin;lib\log4j-api-2.3.jar;lib\log4j-core-2.3.jar;lib\commons-codec-1.3.jar jade.Boot -container reg2:es.ehu.Registerer
TIMEOUT /T 2
@goto inicio