java -Dlog4j.configurationFile="log4j2.xml" -cp ".\lib\jade;.\lib\log4j\log4j-api-2.3.jar;.\lib\log4j\log4j-core-2.3.jar;.\lib\commons\commons-codec-1.3.jar;.\lib\commons\commons-io-2.6.jar;.\classes" jade.Boot -container -host 192.168.137.250 -local-host 192.168.137.250 -port 1099 -local-port 1099 auxlocal2:es.ehu.platform.agents.ProcNodeAgent("System=system","description=description","HostedElements=")
