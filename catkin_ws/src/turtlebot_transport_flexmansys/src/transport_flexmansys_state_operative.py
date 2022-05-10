#! /usr/bin/env python

import rospy

from std_msgs.msg import String
from turtlebot_transport_flexmansys.srv import TransportServiceMessage, TransportServiceMessageRequest

from transport_flexmansys_kinect import KinectManager
from transport_flexmansys_led_manager import LEDManager
from transport_flexmansys_movement_manager import MovementManager
from transport_flexmansys_coordinates_bagfile import StationCoordinates
from transport_flexmansys_distance_calculator import DistanceCalculator
from transport_flexmansys_manipulation_activity_simulation import ManipulationActivitySimulation

'''''

Este sript tiene como objetivo el crear una clase que gestione el modo OPERATIVE de la maquina
de estados. 

'''''

class OperativeState(object):

    def __init__(self):
        self.TransportState = "Operative"
        # self.KinectObject = KinectManager()
        self.MovementObjectOperative = MovementManager()
        self.ManipulationSimulationObject = ManipulationActivitySimulation()

        self.Operative_to_Stop = False
        self.Operative_to_Active = False
        self.SuccessfulDocking = False

        self.OperativeCounter = 0
        self.goal_number = 0

        self.OperativeValidCoordinatesObject = StationCoordinates()

    def OperativeOperation(self):

        self.led_object = LEDManager(self.TransportState)

        self.GetRequest()

    def GetRequest(self):

        self._sub = rospy.Subscriber('/flexmansys/coordenada/leonardo', String, self.ReadRequest)
        rospy.sleep(0.5)

    def ReadRequest(self, sCoordinate):

        # Al pasar al estado de OPERATIVE, el nodo main volvera a leer la coordenada introducida en el
        # topico /flexmansys/coordenada/leonardo. Puede darse el caso, que el estado ACTIVE haya interpretado
        # que se trata de una coordenada valida, o bien que en el paso de ACTIVE a OPERATIVE se haya cambiado
        # la coordenada introducida. Para ello, se vuelve a comprobar si la coordenada es STOP, si no es asi,
        # se comprueba si la coordenada introducida se encuentra dentro del diccionario que recoge todas las
        # coordendas en la clase StationCoordinates, donde se recogen los valores cartesianos de todas las
        # coordenadas validas.

        self.Coordinate = sCoordinate
        self.led_object = LEDManager(self.TransportState)

        if self.Coordinate == "STOP":
            self.Operative_to_Stop = True

        for CoordinateBrowser in self.OperativeValidCoordinatesObject.CoodinatesList.values():

            if self.Coordinate in CoordinateBrowser:

                # Unicamente se entra aqui, si la coordenada publicada en el topico se encuentra dentro
                # de la clase StationCoordinates.

                self.SuccessfulDocking = False

                # Se indica cual es su numero de operacion, o el numero de Goals que se han pasado a
                # move_base

                self.goal_number = self.goal_number + 1
                rospy.loginfo("**************** " + "GOAL NUMBER " + str(self.goal_number) + " ****************")
                rospy.loginfo("Coordenada Obtenida: " + str(self.Coordinate))

                self.DistanceObject = DistanceCalculator(self.Coordinate)
                rospy.loginfo("Distancia Robot-Coordenada: " + str(self.DistanceObject.distance_robot_coordinate)
                              + " m")

                # Antes de pasar el goal a move_base, se hace un ajuste rotacional para encarar la coordenada
                # introducida. Esto, se debe a que el DWAPlanner, el planificador local empleado por move_base,
                # ha sido indicado por muchos usuarios que suele tener problemas para generar paths a puntos que
                # requieran de una rotacion de 180 grados. Para evitar este problema, el cual se ha validado que
                # efectivamente pasa, se hace un ajuste rotacional para que el Kobuki siempre encare la coordenada
                # a la cual desplazarse antes de hacer la planificacion por parte de move_base

                self.MovementObjectOperative.RotationAdjustment(self.Coordinate)

                # Se crea un servicio cliente, en el cual el servidor sera el nodo
                # 'flexmansys_transport_node_server_leonardo'

                self.TransportServiceRequest()

                # Estas lineas simulan la finalizacion de la actividad de manipulacion en caso de ser necesaria.
                # Saldra del bucle siempre y cuando se presione el boton B0 de la unidad de transporte.

                while self.ManipulationSimulationObject.ActivityFinalished == False:
                    self.ManipulationSimulationObject.ManipulationActivityFinalished()

                rospy.loginfo("Actividad de manipulacion finalizada")

                self.ManipulationSimulationObject.ActivityFinalished = False

                # self.ManipulationSimulationObject.ActiviyFinalished = False

                # Aqui iria la funcion de checkeo de que el transporte se encuentra en su estacion de carga
                # self.TransportDockingCheck()

        self.Operative_to_Active = True


    def TransportServiceRequest(self):

        # Esperar a que el servicio /flexmansys/transport/service este activo

        rospy.wait_for_service('/flexmansys/transport/service/leonardo')

        # Realizar conexion con el servicio /flexmansys/transport/service

        transport_service_request_client = rospy.ServiceProxy('/flexmansys/transport/service/leonardo',
                                                              TransportServiceMessage)

        transport_service_request_object = TransportServiceMessageRequest()

        # Le pasamos al servicio como argumento de entrada la coordenada introducida

        transport_service_request_object.coordinate = str(self.Coordinate)

        result = transport_service_request_client(transport_service_request_object)

        if result:
            rospy.loginfo("Desplazamiento de la unidad de transporte finalizado")
        else:
            rospy.loginfo("Desplazamiento de la unidad de transporte no ha sido finalizado")


    '''''
    def TransportDockingCheck(self):

          if self.Coordinate == "DOCK":

              while self.KinectObject.ARCodeID != 0:

                  for i in range(5):
                      self.KinectObject.read_kinect()

                  if self.KinectObject.ARCodeID == 0:

                      self.SuccessfulDocking = True

                  elif self.KinectObject.ARCodeID != 0:

                      self.SuccessfulDocking = False
                      self.MovementObjectOperative.LeftRotation360()

              rospy.loginfo("Unidad de transporte en estacion de carga")
              self.KinectObject.ARCodeID = 1000
    '''