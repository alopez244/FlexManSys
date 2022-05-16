#! /usr/bin/env python

import rospy
import rosnode

from std_msgs.msg import String

from transport_flexmansys_led_manager import LEDManager
from transport_flexmansys_movement_manager import MovementManager

'''''

Este script contiene un codigo el cual inicia un nodo de emergencia para la unidad
de transporte donde se este ejecutando. Su funcion, es la de crear un nodo que este
continuamente suscribiendose al nodo el cual la unidad de transporte recibe sus 
coordenadas desde los agentes "/flexmansys/coordinate/NombreUnidadTransporte", y en
caso de que reciba una "E", matara todos los nodos implicados en el transporte del
demostrador de "flexmansys". Se trata de un nodo redundante, el cual asegura que 
todos los nodos detengan su ejecucion en caso de emergencia, sobre todo cuando la 
maquina de estados se encuentra en LOCALIZATION u OPERATIVE, donde no sera capaz
de detener a la unidad de transporte si recibe una coordenada de STOP.

'''''

class FlexmansysTransportNodeEmergency(object):

    def __init__(self):

        rospy.init_node('transport_flexmansys_emergency_stop_node_leonardo')
        rospy.sleep(2.0)

        self.MovementEmergencyObject = MovementManager()
        self.LEDEmergencyObject = LEDManager("NONE")
        self.Coordinate = "NONE"

        self.ctrl_c = False
        rospy.on_shutdown(self.TransportEmergencyNodeShutdown)

        self.EmergencyNodeSubscriber()

    def EmergencyNodeSubscriber(self):

        # Al igual que el nodo main, el nodo de emergencia de la unidad de transporte estara continuamente
        # suscribiendose al topico /flexmansys/coordenada/leonardo, en el cual se publican las coordenadas
        # de la unidad de transporte. Dejara de hacerlo una vez se introduzca la coordenada "E" o "STOP".

        # A diferencia del nodo main, el nodo de emergencia sera capaz de detener la ejecucion de todos los
        # nodos en cualquier momento, incluso cuando la unidad de transporte se encuentr en OPERATIVE o en
        # LOCALIZATION.

        # La existencia de este nodo, se debe a un nodo de cobertura cuya unica tarea sea el de encomendar
        # la seguridad de la unidad de transporte.

        while not self.ctrl_c:

            self._sub = rospy.Subscriber('/flexmansys/coordenada/leonardo', String, self.ReadNode)
            rospy.sleep(0.5)

            if self.Coordinate == "E":

                self.ctrl_c = True

            elif self.Coordinate == "STOP":

                self.ctrl_c = True

    def ReadNode(self, sCoordinate):

        self.Coordinate = sCoordinate.data

    def TransportEmergencyNodeShutdown(self):

        # Al igual que el nodo Main, se define una funcion de Shutdown para cuando se termine la ejecucion
        # de manera forzosa. Si se da una emergencia, el nodo entendera que el nodo main no puede detener
        # al AGV puesto que se encuentra en operacion gestionando su movimiento y percepcion del entorno,
        # por lo que sera tarea del nodo de emergencia el detener la ejecucion de todos los nodos.

        if self.Coordinate == "E":

            #Cambiar nodos implicados con el nombre de la unidad de transporte correspondiente

            rosnode.kill_nodes(['/transport_flexmansys_node_leonardo', 'transport_flexmansys_node_server_leonardo',
                                '/move_base', '/map_server', '/amcl'])

            self.MovementEmergencyObject.StopTransport()

            self.Coordinate = "Error"
            self.LEDEmergencyObject = LEDManager(self.Coordinate)

        elif self.Coordinate == "STOP":

            # Si lee un STOP, igualmente

            pass

if __name__ == '__main__':

    emergency_transport_node = FlexmansysTransportNodeEmergency()
