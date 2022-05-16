#! /usr/bin/env python

import rospy

from kobuki_msgs.msg import BumperEvent

'''''

Este script tiene como objetivo detectar el flag generado por los Bumper cuando estos 
se presionan y detecta un objeto o pared delante.

'''''

class BumperTopicReader(object):

    def __init__(self):

        self._sub = rospy.Subscriber('/mobile_base/events/bumper', BumperEvent, self.bumper_callback)
        rospy.sleep(0.2)

        # Este flag hay que cambiarle el valor a True desde la clase que instancia a BumperTopicReader

        self.BumperStopRequirement = False

    def bumper_callback(self, BumperData):

        # EL topico /mobile_base/events/bumper solo notifica cuando se presiona un bumper o varios
        # y publica cual es el presionado y su estado

        # self.BumperData.bumper // 2 = Right Bumper 1 = Center Bumper 0 = Left Bumper

        self.BumperData = BumperData

        # No se diferencia entre los 3 bumpers que tiene el Kobuki, si se presiona uno cualquiera
        # el programa lo detectara, puesto que unicamente leera el campo ".state" del topico
        # /mobile_base/events/bumper, ignorando por completo el campo ".bumper"

        if self.BumperData.state == 1:

            self.BumperStopRequirement = True

    def bumper_recovery_petition(self):

        if self.BumperData.state == 0:

            self.BumperStopRequirement = False






        
