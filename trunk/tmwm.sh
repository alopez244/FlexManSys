while true; do java -D"log4j.configurationFile=file:log4j2.xml" -cp "bin:lib/log4j-api-2.3.jar:lib/log4j-core-2.3.jar:log4j-api-2.3.jar:lib/log4j-core-2.3.jar:lib/commons-codec-1.3.jar:lib\TcJavaToAds.jar" jade.Boot -host 192.168.0.1 -local-host 192.168.0.1 -container tmwm:es.ehu.ThreadedMiddlewareManager; sleep 3; done;
TIMEOUT /T 1
@goto inicio

