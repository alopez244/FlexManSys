1.- NodePub: Publicista ROS sin roscore ni rosrun
2.- NodePubCore: Publicista ROS con roscore pero sin rosrun
3.- NodePubCoreRun: Publicista ROS no ejecutable con roscore y rosrun
4.- NodePubMain: Main para ejecutar a NodePubCoreRun
5.- NodePubCoreRunMain: Publicista ROS ejecutable con roscore y rosrun
6.- NodePubRunMainJadeGW_v1: Clase que arranca un publicista ROS y un JadeGateway (cada uno va a su bola)
7.- NodePubRunMainJadeGW_v2: Clase que recibe mensajes ACL a trav�s de GWagentROS y publica su contenido en ROS. Depende de dos clases:
7.a.- GWagentROS: Agente pasarela que constituye la puerta desde ROS hacia JADE. Recibe comandos del Nodo ROS y mensajes ACL del agente transporte.
7.b.- StructCommand: Estructura de datos para la comunicaci�n entre el nodo ROS y el agente pasarela. Se compone de dos campos de tipo fijo: acci�n y contenido, ambos de tipo String.
8.- TransporAgent: Agente transporte dummy para NodePubRunMainJadeGW_v2
9.- NodePubMsg: Publicista ROS sin roscore ni rosrun que hace uso de un tipo de mensaje ROS personalizado (transp_state.msg) que tiene la siguiente estructura:
Header header
int8 entero
string cadena
10.- NodePubMsgRunMainJadeGW: como NodePubRunMainJadeGW_v2, pero haciendo uso de transp_state.msg. Depende de tres clases:
10.a.- GWagentROSmsg: Agente pasarela que constituye la puerta desde ROS hacia JADE. Recibe comandos del Nodo ROS y mensajes ACL del agente transporte.
10.b.- StructCommandMsg: Estructura de datos para la comunicaci�n entre el nodo ROS y el agente pasarela. Se compone de dos campos: acci�n, de tipo String, y contenido, de tipo gen�rico. De esta forma, cada acci�n puede tener asociado un tipo de contenido diferente. Por ejemplo, en este caso el comando "recv" del agente pasarela se ha asociado a contenido de tipo StructTranspState.
10.c.- StructTranspState: Estructura de datos para la representaci�n del msg ROS de tipo transp_state en Java.
11.- TransporAgentMsg: Agente transporte dummy para NodePubMsgRunMainJadeGW

COMENTARIOS/DUDAS

Nota: hay que renombrar el criterio "position" y la acci�n "supplyConsumables" asociadas a las negociaciones de transporte

Posibles usos de la informaci�n que puede conseguir Borja del Kobuki:
transport_unit_state: sustituir� al assetLiveness.
battery: indica el voltaje. Si no podemos obtener el SOC, ser�a bueno saber al menos el rango de la bater�a para poder hacer estimaciones (podr�a ser un criterio adicional de negociaci�n).
detected_obstacle_bumper: me interesa solo si puedo saber d�nde est� el obst�culo (para poder avisar a los dem�s transportes).
transport_in_dock: se puede a�adir al estado de los transportes en el SMA. As� se puede consultar si la estaci�n est� ocupada.
recovery_point: suena interesante, preguntar ma�ana.
odom_x,odom_y_rotation: informaci�n �til a bajo nivel, pero no para el agente. �Servir�a para extrapolar en qu� posici�n del mapa estoy?
odroid_date: �Se podr�a usar para calcular los timeStamps?

A PARTIR DE AHORA VOY A APUNTAR AQU� LOS CAMBIOS QUE VOY A HACER EN EL C�DIGO

TransporAgent de FlexManSys:
�Qu� hace a d�a 14/02/2022?
Declara 6 variables (Servicios Transporte (no se usa), xPos, yPos, bater�a, posiciones clave y pila de tareas)
Lee 4 argumentos (Nombre del recurso, xPos, yPos, Nivel de bater�a)
Se rellena el hashMap de posiciones clave a pi��n
Se a�ade una tarea a la pila (ejemplo)
Se inicializa el functionalityInstance con un objeto de tipo TransportFunctionality

    �Qu� deber�a hacer la versi�n final del TransportAgent?
        Declarar 4 variables (currentPos, bater�a, posiciones clave y pila de tareas)
        Leer 3 argumentos (Nombre del recurso, posiciones clave y pila de tareas)
        Inicializar el functionalityInstance con un objeto de tipo TransportFunctionality

TransportFunctionality de FlexManSys:
�Qu� hace a d�a 15/02/2022?
init:
Se identifica al gatewayAgent asociado al agente transporte (variable duplicada en el Transport Functionality y en el MWAgent. Crear GatewayAgentID por similitud al MachineFunctionality)
Se leen los argumentos del agente y se comprueba si estos argumentos incluyen el id (para saber si es el agente auxiliar o el definitivo)
Si no es el caso, se registra el agente y se crea un nuevo agente transporte pas�ndole como argumento el nuevo id asignado por el SystemModelAgent.

        execute:
            Recibe un mensaje enviado desde un MachineAgent con una petici�n. Procesa la petici�n con el m�todo processData y responde envi�ndole un mensaje.

        calculateNegotiationValue:
            No hace nada �til: lee unos valores y luego devuelve el valor de memoria (que no tiene nada que ver)

        checkNegotiation:
            Compara el valor recibido con el valor propio. 
            En caso de ganar, llama al m�todo processData y responde con el resultado al agente m�quina que inici� la negociaci�n.

        sendDataToDevice:
            Se comprueba si el transporte est� libre (si est� ocupado no mando nuevas tareas)
            Se comprueba si hay operaciones en el plan del transporte (si no tengo trabajo, no hago nada)
            Si hay una operaci�n, se coge la primera y se le env�a al transporte. Luego, se pone a true el flag de transporte ocupado

        rcvDataFromDevice:
            Recibe el mensaje del gatewayAgent y lo transforma a la estructura correspondiente
            Comprueba de qu� tipo de mensaje se trata y lo procesa

        terminate:
            No hace nada
            
    �Qu� deber�a hacer la versi�n final del TransportFunctionality?
        init: 
            Se identifica al gatewayAgent asociado al agente transporte (variable GatewayAgentID)
            Se leen los argumentos del agente y se comprueba si estos argumentos incluyen el id (para saber si es el agente auxiliar o el definitivo)
            Si no es el caso, se registra el agente y se crea un nuevo agente transporte pas�ndole como argumento el nuevo id asignado por el SystemModelAgent. 
            (se a�aden como atributos en el registro la posici�n y el nivel de bater�a actuales.)

        execute:
            Recibe operaciones y las a�ade a su listado de tareas. Puede ser invocado por recepci�n de una tarea asignada "a dedo" o como resultado de una negociaci�n (?)
            Se van a recibir mensajes que tengan la estructura "positions=A1,B2 requester=batchagent1 receiver=machine1" o similar.
            Pueden recibirse varias operaciones juntas, separadas por un &. Ejemplo positions=A1,B2 & positions=C3,D4 ESTO ES IMPORTANTE, HAY QUE CAMBIARLO EN EL MACHINE FUNCTIONALITY

        calculateNegotiationValue:
            Tendr� que calcular el tiempo o la distancia (a�n por decidir) que necesita un transporte para poder realizar el servicio

        checkNegotiation:
            Compara el valor recibido con el valor propio. 
            En caso de ganar, llama al m�todo processData y responde con el resultado al agente m�quina que inici� la negociaci�n.

        sendDataToDevice:
            Se comprueba si el transporte est� libre utilizando el flag workInProgress (si est� ocupado no mando nuevas tareas)
            Se comprueba si hay operaciones en el plan del transporte (si no tengo trabajo, me vuelvo a la estaci�n de carga)
            Si hay una operaci�n, se coge la primera y se le env�a al transporte. Luego, se pone a true el flag de transporte ocupado

        rcvDataFromDevice:
            Recibe el mensaje del gatewayAgent y filtra por performativa (IMPORTANT�SIMO: HAY QUE SUSTITUIR LA PERFORMATIVA "REQUEST" POR "CONFIRM" EN EL TEMPLATE DEL RECEIVETASKBEHAVIOUR)
                Nota: esto afectar� al agente m�quina tambi�n
            Si es mensaje de confirmaci�n, leo la bater�a y la posici�n y los actualizo en el SMA
            Si es mensaje de resultados, hago lo mismo, pero adem�s elimino la operaci�n del plan, informo a la m�quina y reseteo el workInProgress

        terminate:
            No hace nada (de momento)



COMANDOS (README DE OSKAR)
javac -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/libs/turtlebot2-0.1.0.jar" NodePubMain.java
ERROR java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/libs/turtlebot2-0.1.0.jar" NodePubMain
ERROR java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/libs/turtlebot2-0.1.0.jar:." NodePubMain
ERROR java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/libs/turtlebot2-0.1.0.jar:./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/rosjava-0.3.7.jar:." NodePubMain
ERROR java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/libs/turtlebot2-0.1.0.jar:./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/rosjava-0.3.7.jar:./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/guava-12.0.jar:." NodePubMain
java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/*:." NodePubMain

java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/*" com.github.rosjava.fms_transp.turtlebot2.NodePubCoreRunMain

javac -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/*:." TransportAgent.java
java -cp "./rosjava_ws/src/fms_transp/turtlebot2/build/install/turtlebot2/lib/*:." jade.Boot -container agente:TransportAgent