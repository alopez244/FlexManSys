#! /usr/bin/env python

import rospy

from std_msgs.msg import String

from transport_flexmansys_led_manager import LEDManager
from transport_flexmansys_battery import BatteryLevel


'''''

Este script tiene como objetivo el crear una clase que gestione el modo ACTIVE de la 
maquina de estados.

'''''


class ActiveState(object):

    def __init__(self):

        # Tras inicializar el objeto, se inicializan los flags que dan paso al estado OPERATIVE
        # o STOP a false.

        self.TransportState = "Active"
        self.Active_to_Stop = False
        self.Active_to_Operative = False

        self.BatteryObject = BatteryLevel()
        rospy.sleep(0.5)

        # Se debe de emplear una variable auxiliar, en este caso CoordinatePrevious, la cual nos
        # ayudara para saber si la coordenada leida del topico /flexmansys/coordenada/leonardo es la
        # misma a la introducida anteriormente.

        self.CoordinatePrevious = 'NONE'

    def ActiveOperation(self):

        self.led_object = LEDManager(self.TransportState)
        self.GetRequest()

    def GetRequest(self):

        self._sub = rospy.Subscriber('/flexmansys/coordenada/leonardo', String, self.ReadRequest)
        rospy.sleep(0.5)

    def ReadRequest(self, sCoordinate):

        # Obtenemos el valor de la coordenada publicada en /flexmansys/coordenada/leonardo y la
        # guardamos en Coordinate = sCoodrinate.data

        self.Coordinate = sCoordinate.data

        # La coordenada introducida, hay que compararla con la anteriormente introducida y leida
        # del topico.  Si es la misma, se ignora, si son distintas coordenadas, se da paso.

        if self.Coordinate != self.CoordinatePrevious:

            self.CoordinatePrevious = self.Coordinate

            # En estado ACTIVE, la unidad de transporte se da por hecho que solo recibira dos tipos
            # de coordenadas: La de detencion de la unidad de transporte, STOP, que detendra la ejecucion
            # del nodo; o una coordenada de movimiento a algun punto del mapa. Luego, si se recibe una
            # coordenada que no es STOP, pasar al estado de Operative.

            if self.Coordinate == "STOP":

                self.Active_to_Stop = True
                self.Active_to_Operative = False

            elif self.Coordinate != "STOP":

                self.Active_to_Operative = True
                self.Active_to_Stop = False

                self.RequestedCoordinate = self.Coordinate






