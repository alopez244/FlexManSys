#! /usr/bin/env python

import rospy

from transport_flexmansys_buttons import ButtonsTopicReader

'''''
Codigo el cual nos permite simular la actividad de manipulacion del brazo mediante
el boton B0 del kobuki
'''''

class ManipulationActivitySimulation(object):

    def __init__(self):

        self.ButtonObject = ButtonsTopicReader()
        self.ActiviyFinalished = False

    def ManipulationActivityFinalished(self):

        self.ButtonObject.button_read()

        if self.ButtonObject.ButtonPressed_B0 == True:

            self.ActiviyFinalished = True
            self.ButtonObject.ButtonPressed_B0 = False

        elif self.ButtonObject.ButtonPressed_B0 == False:

            self.ActiviyFinalished = False






