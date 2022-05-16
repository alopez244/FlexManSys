#! /usr/bin/env python

import rospy

from std_msgs.msg import String

from transport_flexmansys_led_manager import LEDManager
from transport_flexmansys_battery import BatteryLevel
from transport_flexmansys_sound_manager import SoundManager

'''''

Este sript tiene como objetivo el crear una clase que gestione el modo IDLE de la maquina
de estados. 

'''''

class IdleState(object):

    def __init__(self):

        # En modo Idle, unicamente se puede pasar al estado de LOCALIZATION o STOP, por
        # lo que en esencia se comprueba en bucle lo publicado en el topico /flexmansys/coordenada
        # /leonardo. Si se ha introducido una X, se pasa al estado de calibracion, si se ha
        # introducido un STOP, detendremos la ejecucion del programa.

        self.TransportState = "Idle"
        self.Idle_to_Localization = False
        self.Idle_to_Stop = False

        self.BatteryObject = BatteryLevel()
        rospy.sleep(2)

        rospy.loginfo("**************** " + "TRANSPORT IDLE "  " ****************")
        rospy.loginfo("La unidad de transporte leonardo esta en modo idle")
        rospy.loginfo("Nivel de Bateria: " + str(self.BatteryObject.battery_level) + " VDC")

        self.SoundObject = SoundManager(self.TransportState)

    def IdleOperation(self):

        # Hasta que el kobuki no active el buzzer y se encienda de "naranja" (es verde clarito)
        # el LED 1 y el LED 2 este apagado, no leera ninguna coordenada introducida.

        self.led_object = LEDManager(self.TransportState)

        self.GetRequest()

    def GetRequest(self):

        self._sub = rospy.Subscriber('/flexmansys/coordenada/leonardo', String, self.ReadRequest)
        rospy.sleep(0.5)

    def ReadRequest(self, sCoordinate):

        self.Coordinate = sCoordinate.data

        # En caso de leerse una X, pasar al estado de localizacion de la maquina de estados.

        if self.Coordinate == "X":

            self.Idle_to_Localization = True

        elif self.Coordinate == "STOP":

            self.Idle_to_Stop = True

        elif self.Coordinate != "X" and self.Coordinate != "STOP":

            self.Idle_to_Localization = False
            self.Idle_to_Stop = False
