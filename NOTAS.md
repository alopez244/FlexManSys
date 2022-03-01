1.- NodePub: Publicista ROS sin roscore ni rosrun
2.- NodePubCore: Publicista ROS con roscore pero sin rosrun
3.- NodePubCoreRun: Publicista ROS no ejecutable con roscore y rosrun
4.- NodePubMain: Main para ejecutar a NodePubCoreRun
5.- NodePubCoreRunMain: Publicista ROS ejecutable con roscore y rosrun
6.- NodePubRunMainJadeGW_v1: Clase que arranca un publicista ROS y un JadeGateway (cada uno va a su bola)
7.- NodePubRunMainJadeGW_v2: Clase que recibe mensajes ACL a través de GWagentROS y publica su contenido en ROS. Depende de dos clases:
7.a.- GWagentROS: Agente pasarela que constituye la puerta desde ROS hacia JADE. Recibe comandos del Nodo ROS y mensajes ACL del agente transporte.
7.b.- StructCommand: Estructura de datos para la comunicación entre el nodo ROS y el agente pasarela. Se compone de dos campos de tipo fijo: acción y contenido, ambos de tipo String.
8.- TransporAgent: Agente transporte dummy para NodePubRunMainJadeGW_v2
9.- NodePubMsg: Publicista ROS sin roscore ni rosrun que hace uso de un tipo de mensaje ROS personalizado (transp_state.msg) que tiene la siguiente estructura:
Header header
int8 entero
string cadena
10.- NodePubMsgRunMainJadeGW: como NodePubRunMainJadeGW_v2, pero haciendo uso de transp_state.msg. Depende de tres clases:
10.a.- GWagentROSmsg: Agente pasarela que constituye la puerta desde ROS hacia JADE. Recibe comandos del Nodo ROS y mensajes ACL del agente transporte.
10.b.- StructCommandMsg: Estructura de datos para la comunicación entre el nodo ROS y el agente pasarela. Se compone de dos campos: acción, de tipo String, y contenido, de tipo genérico. De esta forma, cada acción puede tener asociado un tipo de contenido diferente. Por ejemplo, en este caso el comando "recv" del agente pasarela se ha asociado a contenido de tipo StructTranspState.
10.c.- StructTranspState: Estructura de datos para la representación del msg ROS de tipo transp_state en Java.
11.- TransporAgentMsg: Agente transporte dummy para NodePubMsgRunMainJadeGW

COMENTARIOS/DUDAS

Nota: hay que renombrar el criterio "position" y la acción "supplyConsumables" asociadas a las negociaciones de transporte

Posibles usos de la información que puede conseguir Borja del Kobuki:
transport_unit_state: sustituirá al assetLiveness.
battery: indica el voltaje. Si no podemos obtener el SOC, sería bueno saber al menos el rango de la batería para poder hacer estimaciones (podría ser un criterio adicional de negociación).
detected_obstacle_bumper: me interesa solo si puedo saber dónde está el obstáculo (para poder avisar a los demás transportes).
transport_in_dock: se puede añadir al estado de los transportes en el SMA. Así se puede consultar si la estación está ocupada.
recovery_point: suena interesante, preguntar mañana.
odom_x,odom_y_rotation: información útil a bajo nivel, pero no para el agente. ¿Serviría para extrapolar en qué posición del mapa estoy?
odroid_date: ¿Se podría usar para calcular los timeStamps?

A PARTIR DE AHORA VOY A APUNTAR AQUÍ LOS CAMBIOS QUE VOY A HACER EN EL CÓDIGO

TransporAgent de FlexManSys:
¿Qué hace a día 14/02/2022?
Declara 6 variables (Servicios Transporte (no se usa), xPos, yPos, batería, posiciones clave y pila de tareas)
Lee 4 argumentos (Nombre del recurso, xPos, yPos, Nivel de batería)
Se rellena el hashMap de posiciones clave a piñón
Se añade una tarea a la pila (ejemplo)
Se inicializa el functionalityInstance con un objeto de tipo TransportFunctionality

    ¿Qué debería hacer la versión final del TransportAgent?
        Declarar 4 variables (currentPos, batería, posiciones clave y pila de tareas)
        Leer 3 argumentos (Nombre del recurso, posiciones clave y pila de tareas)
        Inicializar el functionalityInstance con un objeto de tipo TransportFunctionality

TransportFunctionality de FlexManSys:
¿Qué hace a día 15/02/2022?
init:
Se identifica al gatewayAgent asociado al agente transporte (variable duplicada en el Transport Functionality y en el MWAgent. Crear GatewayAgentID por similitud al MachineFunctionality)
Se leen los argumentos del agente y se comprueba si estos argumentos incluyen el id (para saber si es el agente auxiliar o el definitivo)
Si no es el caso, se registra el agente y se crea un nuevo agente transporte pasándole como argumento el nuevo id asignado por el SystemModelAgent.

        execute:
            Recibe un mensaje enviado desde un MachineAgent con una petición. Procesa la petición con el método processData y responde enviándole un mensaje.

        calculateNegotiationValue:
            No hace nada útil: lee unos valores y luego devuelve el valor de memoria (que no tiene nada que ver)

        checkNegotiation:
            Compara el valor recibido con el valor propio. 
            En caso de ganar, llama al método processData y responde con el resultado al agente máquina que inició la negociación.

        sendDataToDevice:
            Se comprueba si el transporte está libre (si está ocupado no mando nuevas tareas)
            Se comprueba si hay operaciones en el plan del transporte (si no tengo trabajo, no hago nada)
            Si hay una operación, se coge la primera y se le envía al transporte. Luego, se pone a true el flag de transporte ocupado

        rcvDataFromDevice:
            Recibe el mensaje del gatewayAgent y lo transforma a la estructura correspondiente
            Comprueba de qué tipo de mensaje se trata y lo procesa

        terminate:
            No hace nada
            
    ¿Qué debería hacer la versión final del TransportFunctionality?
        init: 
            Se identifica al gatewayAgent asociado al agente transporte (variable GatewayAgentID)
            Se leen los argumentos del agente y se comprueba si estos argumentos incluyen el id (para saber si es el agente auxiliar o el definitivo)
            Si no es el caso, se registra el agente y se crea un nuevo agente transporte pasándole como argumento el nuevo id asignado por el SystemModelAgent. 
            (se añaden como atributos en el registro la posición y el nivel de batería actuales.)

        execute:
            Recibe operaciones y las añade a su listado de tareas. Puede ser invocado por recepción de una tarea asignada "a dedo" o como resultado de una negociación (?)
            Se van a recibir mensajes que tengan la estructura "positions=A1,B2 requester=batchagent1 receiver=machine1" o similar.
            Pueden recibirse varias operaciones juntas, separadas por un &. Ejemplo positions=A1,B2 & positions=C3,D4 ESTO ES IMPORTANTE, HAY QUE CAMBIARLO EN EL MACHINE FUNCTIONALITY

        calculateNegotiationValue:
            Tendrá que calcular el tiempo o la distancia (aún por decidir) que necesita un transporte para poder realizar el servicio

        checkNegotiation:
            Compara el valor recibido con el valor propio. 
            En caso de ganar, llama al método processData y responde con el resultado al agente máquina que inició la negociación.

        sendDataToDevice:
            Se comprueba si el transporte está libre utilizando el flag workInProgress (si está ocupado no mando nuevas tareas)
            Se comprueba si hay operaciones en el plan del transporte (si no tengo trabajo, me vuelvo a la estación de carga)
            Si hay una operación, se coge la primera y se le envía al transporte. Luego, se pone a true el flag de transporte ocupado

        rcvDataFromDevice:
            Recibe el mensaje del gatewayAgent y filtra por performativa (IMPORTANTÍSIMO: HAY QUE SUSTITUIR LA PERFORMATIVA "REQUEST" POR "CONFIRM" EN EL TEMPLATE DEL RECEIVETASKBEHAVIOUR)
                Nota: esto afectará al agente máquina también
            Si es mensaje de confirmación, leo la batería y la posición y los actualizo en el SMA
            Si es mensaje de resultados, hago lo mismo, pero además elimino la operación del plan, informo a la máquina y reseteo el workInProgress

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