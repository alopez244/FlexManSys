#! /usr/bin/env python

import rospy

from kobuki_msgs.msg import ButtonEvent

'''''

Este script tiene como objetivo detectar el flag generado por cualquiera de los tres botones
B0, B1 y B2 del Kobuki cuando estos se presionan. 

'''''

class ButtonsTopicReader(object):

    def __init__(self):

        self._sub = rospy.Subscriber('/mobile_base/events/button', ButtonEvent, self.button_callback)
        rospy.sleep(0.2)

        # Las clases que instancien a ButtonsTopicReader deberan poner a False el flag que indique el
        # flanco de los botones, es decir, ButtonPressed_BX

        self.ButtonPressed_B0 = False
        self.ButtonPressed_B1 = False
        self.ButtonPressed_B2 = False

    def button_read(self):

        self._sub = rospy.Subscriber('/mobile_base/events/button', ButtonEvent, self.button_callback)
        rospy.sleep(0.2)

    def button_callback(self, ButtonData):

        self.ButtonData = ButtonData

        if self.ButtonData.button == 0 and self.ButtonData.state == 1:

            # Recordar resetear la variable en la clase instanciada

            self.ButtonPressed_B0 = True

        elif self.ButtonData.button == 1 and self.ButtonData.state == 1:

            # Recordar resetear la variable en la clase instanciada

            self.ButtonPressed_B1 = True

        elif self.ButtonData.button == 2 and self.ButtonData.state == 1:

            # Recordar resetear la variable en la clase instanciada

            self.ButtonPressed_B2 = True
