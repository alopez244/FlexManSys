#! /usr/bin/env python

import rospy

from transport_flexmansys_movement_manager import MovementManager

'''''
Este script tiene como objetivo unicamente calibrar el AMCL nada mas arrancar el nodo
transport_flexmansys_node para que cuando se le introduzca un goal al paquete move_base
el robot este ya calibrado y localizado mediante el metodo Monte Carlo, o como lo llama
en move_base: AMCL (Adaptative Monte Carlo Localization).
'''''

class Localization(object):

    def __init__(self):

        self.MovementManager = MovementManager()

    def TransportLocalization (self):

        rospy.loginfo("**************** AMCL CALIBRATION ****************")
        rospy.loginfo("Unidad de transporte Leonardo calibrandose")

        # Previamente se hace una traslacion lineal recta de 0.3 m para que la unidad de
        # transporte salga de su estacion de carga.

        self.MovementManager.InitialTranslation()

        # Posteriormente, se efectuan dos rotaciones de poco menos de 360 grados, una hacia
        # la izquierda y otra hacia la derecha. El orden de rotacion es indiferente.

        self.MovementManager.InitialRotation()

        # Tras ello, ya se estima que la calibracion mediante AMCL ya es lo suficientemente
        # precisa, por lo que se detiene la unidad de transporte y se da paso al estado de
        # la maquina de ACTIVE.

        self.MovementManager.StopTransport()

        rospy.loginfo("Localizacion Finalizada")

'''
if __name__ == "main":
    rospy.init_node('transport_flexmansys_localization')
    flexmansys_localization_object = Localization()
    rate = rospy.Rate(1)
    rospy.spin()

'''