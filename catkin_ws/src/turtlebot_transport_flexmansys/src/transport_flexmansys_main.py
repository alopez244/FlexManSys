#! /usr/bin/env python

import rospy
import rosnode

from turtlebot_transport_flexmansys.msg import TransportPrivateState

from transport_flexmansys_state_idle import IdleState
from transport_flexmansys_state_localization import LocalizationState
from transport_flexmansys_state_active import ActiveState
from transport_flexmansys_state_operative import OperativeState
from transport_flexmansys_movement_manager import MovementManager
from transport_flexmansys_led_manager import LEDManager
from transport_flexmansys_sound_manager import SoundManager
from transport_flexmansys_buttons import ButtonsTopicReader
from transport_flexmansys_battery import BatteryLevel


'''''

Codigo main de la unidad de transporte LEONARDO

'''''

class FlexmansysTransportNode(object):

    def __init__(self):

        # Iniciamos un nodo nada mas declarar la clase FlexmansysTransportNode
        # Este nodo, sera flexmansys_transport_node_leonardo, encargado de la gestion del
        # transporte y navegacion de la unidad de transporte Leonardo

        rospy.init_node('flexmansys_transport_node_leonardo')
        rospy.sleep(2.0)

        # Se inicializan instancias a los objetos que el nodo de gestion de Leonardo accedera
        # a lo largo de la ejecucion del programa. Los mas importantes, los cuatro primeros
        # que definiran instancias a los objetos que definen el funcionamiento de la unidad
        # cuando se encuentra en el estado de Idle, Localization, Active y Operative.

        self.IdleObject = IdleState()
        self.LocalizationObject = LocalizationState()
        self.ActiveObject = ActiveState()
        self.OperativeObject = OperativeState()
        self.MovementObject = MovementManager()
        self.ButtonObject = ButtonsTopicReader()
        self.BatteryObject = BatteryLevel()

        # Una vez se inicializa el nodo, la unidad de transporte se encuentra en estado Idle,
        # sin importar el punto del mapa en el que se encuentre. Si no se encuentra en la estacion
        # de carga una vez se inicializa, o el punto 0,0 del mapa, no funcionara correctamente.

        self.TransportMachineState = "Idle"

        # El nodo flexmansys_transport_node_leonardo se comunica a traves del topico
        # /flexmansys/private/state/leonardo con el nodo que se encarga de publicar el estado
        # de la unidad de transporte.

        self.PrivateTransportUnit = TransportPrivateState()
        self.PrivateTransportUnit.transport_state = self.TransportMachineState
        self.PrivateTransportUnit.transport_docked = False

        self.PrivateStatePublish = rospy.Publisher('/flexmansys/private/state/leonardo',
                                                   TransportPrivateState, queue_size=1)

        # Definimos una funcion de apagado o shutdown, la cual el programa ejecutara cuando
        # se de un suceso que obligue a detener el nodo ROS, bien sea por una excepcion en el
        # programa, una peticion de STOP por el agente o una peticion de parada de emergencia

        self.ctrl_c = False
        rospy.on_shutdown(self.TransportNodeShutdown)

        # Inicializamos la maquina de estados

        self.FlexmansysTransportNodeMachineState()


    def FlexmansysTransportNodeMachineState(self):

        # La maquina de estados funcionara en bucle siempre y cuando no se llame a la funcion de
        # shutdown del nodo, que se activara cuando se de una peticion de paro de ejecucion

        while not self.ctrl_c:

            # En cada iteracion, el nodo main informara al nodo publicista del estado de la unidad
            # de transporte el estado en el que se encuentra la unidad.

            self.PrivateTransportUnit.transport_state = self.TransportMachineState
            self.PrivateTransportUnit.transport_docked = self.OperativeObject.SuccessfulDocking
            self.PrivateStatePublish.publish(self.PrivateTransportUnit)

            # ********************** IDLE **********************
            if self.TransportMachineState == "Idle":

                self.IdleObject.IdleOperation()

                if self.IdleObject.Idle_to_Localization == True:
                    self.TransportMachineState = "Localization"
                    self.IdleObject.Idle_to_Localization = False

                elif self.IdleObject.Idle_to_Stop == True:
                    self.TransportMachineState = "Stop"

            # ********************** LOCALIZATION **********************

            elif self.TransportMachineState == "Localization":

                self.LocalizationObject.LocalizationOperation()
                self.TransportMachineState = "Active"

            # ********************** ACTIVE **********************

            elif self.TransportMachineState == "Active":

                self.ActiveObject.ActiveOperation()

                if self.ActiveObject.Active_to_Operative == True:
                    self.TransportMachineState = "Operative"
                    self.ActiveObject.Active_to_Operative = False

                elif self.ActiveObject.Active_to_Stop == True:
                    self.TransportMachineState = "Stop"

            # ********************** OPERATIVE **********************

            elif self.TransportMachineState == "Operative":

                # Se define un contador para que el nodo no este continuamente suscribiendose
                # al topico /flexmansys/coordenada/leonardo y enviando coordenadas al paquete
                # move_base. Nos aseguramos de que estre una unica vez cada vez que se activa
                # el estado operative.

                if self.OperativeObject.OperativeCounter == 0:
                    self.OperativeObject.ReadRequest(self.ActiveObject.RequestedCoordinate)
                    self.OperativeObject.OperativeCounter = self.OperativeObject.OperativeCounter + 1

                else:

                    if self.OperativeObject.Operative_to_Stop == True:
                        self.TransportMachineState = "Stop"

                    elif self.OperativeObject.Operative_to_Active == True:

                        # Una vez termina sus tareas en el estado de Operative, volveremos a Active,
                        # estado en el cual esperara a la siguiente instruccion.

                        self.TransportMachineState = "Active"
                        self.OperativeObject.Operative_to_Active = False
                        self.OperativeObject.OperativeCounter = 0

            # ********************** STOP **********************

            elif self.TransportMachineState == "Stop":

                self.PrivateStatePublish.publish(self.PrivateTransportUnit)

                break

    def TransportNodeShutdown(self):

        # Tal y como se ha comentado anteriormente, esta es la funcion a la cual entrara el nodo siempre
        # que se de una peticion de detener la ejecucion del nodo, bien sea por usuario o por fallo en la
        # ejecucion.

        # Primeramente, se cambia el flag ctrl_c a True para que no vuelva a ejecutar la maquina de estados

        self.ctrl_c = True

        # Si la peticion ha sido por usuario y/o agente publicando la coordenada "STOP" en el topico
        # /flexmansys/coordenada/leonardo, entra a la condicion Stop.

        if self.TransportMachineState == "Stop":

            # Detenemos la ejecucion de todos los nodos involucrados en la navegacion, de modo que la unidad
            # de transporte se quede inmobil en el punto el cual se encuentre cuando se introduce el comando
            # de STOP.

            # La maquina de estados no puede entrar a STOP si se encuentra en movimiento, es decir, si esta
            # en estado LOCALIZATION y OPERATIVE. Para detenerla, habra que introducir el comando "E" de
            # emergencia o ERROR.

            rosnode.kill_nodes(['/transport_flexmansys_node_server_leonardo',
                                '/transport_flexmansys_state_publisher_leonardo',
                                '/transport_flexmansys_emergency_stop_node_leonardo',
                                '/move_base', '/map_server', '/amcl'])

            # Detenemos la unidad de transporte

            self.MovementObject.StopTransport()

            # Apagamos los LED 1 y 2 para indicar que la unidad de transporte esta en estado de STOP.

            self.LEDObject = LEDManager(self.TransportMachineState)

            rospy.loginfo("**************** " + "STOP TRANSPORT "  " ****************")
            rospy.loginfo("Unidad de transporte Leonardo ha sido detenida correctamente.")
            rospy.loginfo("Finalice la ejecucion del nodo introduciendo ctr + c en el terminal")
            rospy.loginfo("Compruebe si la unidad de transporte se encuentra correctamente")
            rospy.loginfo("posicionada y orientada sobre su estacion de carga")

        # Si el nodo ROS ha entrado a esta funcion por cualquier otra razon que no haya sido el recibir
        # una coordenada de STOP, entendera que ha sido por fallo en la ejecucion, peticion de emergencia
        # o paro de la ejecucion a traves del terminal. Es decir, estado de Emergencia o ERROR.

        else:

            # ********************** ERROR **********************

            # Igualmente, se detendra la ejecucion de todos los nodos implicados en la navegacion y transporte

            rosnode.kill_nodes(['/transport_flexmansys_node_server_leonardo',
                                '/transport_flexmansys_state_publisher_leonardo',
                                '/transport_flexmansys_emergency_stop_node_leonardo',
                                '/move_base', '/map_server', '/amcl'])

            # En Error no matamos el nodo /transport_flexmansys_state_publisher_leonardo
            # ya que queremos que los agentes sepan el estado en el que se encuentra la unidad
            # de transporte

            self.MovementObject.StopTransport()

            # La unidad de transporte indicara que se ha entrado en estado de ERROR con 10 pitidos
            # que efectuara la base movil, ademas de encender el LED 1 a rojo.

            self.TransportMachineState = "Error"
            self.LEDObject = LEDManager(self.TransportMachineState)

            rospy.loginfo("**************** " + "FORCED SHUTDOWN "  " ****************")
            rospy.loginfo("Unidad de transporte Leonardo se ha detenido forzosamente mientras")
            rospy.loginfo("la unidad operaba. Leonardo, ha entrado al estado de " + self.TransportMachineState)
            rospy.loginfo("Para reiniciar, la unidad de transporte debe volver a su estacion")
            rospy.loginfo("de origen y detener todos los demas nodos restantes, incluido roscore")

            self.SoundObject = SoundManager(self.TransportMachineState)


if __name__ == '__main__':

    objeto_navegacion = FlexmansysTransportNode()



