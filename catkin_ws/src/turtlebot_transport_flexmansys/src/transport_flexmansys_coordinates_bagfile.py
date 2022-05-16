#! /usr/bin/env python

import rospy
from std_msgs.msg import String

'''''
Este script tiene como objetivo recoger todas las coordenadas del mapa y asociarlas
al nombre de la coordenada que se publica en el topico /flexmansys/coordinate
'''''

# ************* COORDENADAS DE ESTADO DE UNIDAD DE TRANSPORTE **************

# X - Coordenada de calibracion
# E - Coordenada para indicar paro de emergencia de la unidad de transporte
# STOP - Coordenada para indicar el fin de la actividad de la unidad de transporte

# ************* COORDENADAS DE MOVIMIENTO DE UNIDAD DE TRANSPORTE **************

# FREEWAY - Coordenada para indicar obstaculo retirado del camino

# ************* COORDENADAS DE LAS ESTACIONES DE LA PLANTA **************

# A1 -
# B1 -
# C1 -

# NOTAS:
# Para cambiar la resolucion de la exactitud con la que el planificador de move_base
# se situa sobre cada uno de los puntos, hay que ir al archivo planner.yaml correspondiente
# invocado en el launchfile de move_base. Dentro de ese .yaml, hay que tocar dos parametros
# tanto en el planificador global como en el local:
#

class StationCoordinates (object):

    def __init__(self):

        self.CoodinatesList = {1: "A",
                               2: "B",
                               3: "C",
                               4: "D1",
                               5: "E1",
                               6: "DOCK"}

    def CoordinatesBagFile(self, sCoordinate):

        self.CoordinateObject = sCoordinate

        if self.CoordinateObject == "DOCK":

            self.x = 0.0
            self.y = 0.0
            self.z = 0.0
            self.qx = 0.0
            self.qy = 0.0
            self.qz = 0.0
            self.qw = 1.0

        elif self.CoordinateObject == "A":

            self.x = 0.7510
            self.y = -1.8840
            self.z = 0.0
            self.qx = 0.0
            self.qy = 0.0
            self.qz = -0.6742
            self.qw = 0.7385

        elif self.CoordinateObject == "B":
            self.x = 0.03793
            self.y = 3.4614
            self.z = 0.0
            self.qx = 0.0
            self.qy = 0.0
            self.qz = -1.0
            self.qw = 0.0

        elif self.CoordinateObject == "C":
            self.x = 0.5812
            self.y = 8.3548
            self.z = 0.0
            self.qx = 0.0
            self.qy = 0.0
            self.qz = 0.7440
            self.qw = 0.6781

        elif self.CoordinateObject == "D1":
            self.x = 0.0
            self.y = 0.20
            self.z = 0.0
            self.qx = 0.0
            self.qy = 0.0
            self.qz = 0.7071
            self.qw = 0.7071

        elif self.CoordinateObject == "E1":
            self.x = 0.60
            self.y = 0.30
            self.z = 0.0
            self.qx = 0.0
            self.qy = 0.0
            self.qz = 0.7071
            self.qw = 0.7071

