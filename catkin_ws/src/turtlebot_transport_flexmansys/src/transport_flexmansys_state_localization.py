#! /usr/bin/env python

import rospy

from transport_flexmansys_led_manager import LEDManager
from transport_flexmansys_localization import Localization

'''''

Este sript tiene como objetivo el crear una clase que gestione el modo LOCALIZATION de la maquina
de estados. 

'''''

class LocalizationState(object):

    def __init__(self):

        self.TransportState = "Localization"
        self.localization_object = Localization()

    def LocalizationOperation(self):

        self.led_object = LEDManager(self.TransportState)

        # En esencia, se hace uso de la clase Localization, que a su vez se instancia al
        # MovementManager para efectuar la localizacion.

        self.localization_object.TransportLocalization()

        rospy.loginfo("**************** " + "TRANSPORT ACTIVE " + " ****************")
        rospy.loginfo("La unidad de transporte leonardo esta en estado Active")

