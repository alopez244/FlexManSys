java -Dlog4j.configurationFile="log4j2.xml" -cp "./lib/jade:./lib/log4j/log4j-api-2.3.jar:./lib/log4j/log4j-core-2.3.jar:./lib/commons/commons-codec-1.3.jar:./lib/Gson/gson-2.8.6.jar:./lib/commons/commons-io-2.6.jar:./lib/commons/commons-collections4-4.4.jar:./lib/commons/commons-lang3-3.8.1.jar:./lib/rosjade/rosjade-0.1.3.jar:./classes" jade.Boot -container -host 192.168.137.17 -local-host 192.168.137.120 auxnode1:es.ehu.platform.agents.ProcNodeAgent\("System=system","description=description","HostedElements="\)

