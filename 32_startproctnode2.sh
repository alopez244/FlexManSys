java -Dlog4j.configurationFile="log4j2.xml" -cp "./lib/jade:./lib/log4j/log4j-api-2.3.jar:./lib/log4j/log4j-core-2.3.jar:./lib/commons/commons-codec-1.3.jar:./lib/commons/commons-io-2.6.jar:./classes" jade.Boot -container -host 192.168.2.17 -local-host 192.168.2.20 auxnode2:es.ehu.platform.agents.ProcNodeAgent\("System=system","description=description","refServID=id57"\)

