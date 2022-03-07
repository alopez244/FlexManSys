#! /usr/bin/env python

from math import sqrt
from math import pow
from math import fabs

from transport_flexmansys_odom import OdomTopicReader
from transport_flexmansys_coordinates_bagfile import StationCoordinates

'''''
Este script tiene como objetivo el calcular la distancia entre la posicion actual del robot
y la posicion de la estacion introducida en el topico /flexmansys/coordinate. Directamente,
calculara la longitud de la recta que une ambos puntos mediante la hipotenusa que forman
'''''

class DistanceCalculator(object):

    def __init__(self, sCoordinate):

        # Cuando se instancia esta clase, hay que introducirle la coordenada la cual el AGV
        # quiera desplazarse, de modo que en funcion de la lectura de la odometria actual del robot
        # y el valor de las coordenadas x e y de la clase StationCoordinates se calcule la distancia
        # a la coordenada introducida.

        self.DistanceOdomObject = OdomTopicReader()
        self.CoordinateObject = StationCoordinates()
        self.CoordinateObject.CoordinatesBagFile(sCoordinate)

        self.distance_x = self.CoordinateObject.x
        self.distance_y = self.CoordinateObject.y
        self.distance_to_coordinate()

    def distance_to_coordinate(self):

        # Se hace un calculo simple, sin medir realmente la longitud del trayecto que debe de trazar
        # la unidad de transporte hasta la coordenada introducida.

        # En esencia, se hace uso de trigonometria, calculando el modulo de la hipotenusa que forma
        # el triangulo recto que une el punto actual en el que se encuentra la unidad de transporte
        # (punto proporcionado por la Odometria) y el punto de la coordenada introducida.

        dif_x = fabs(self.distance_x-self.DistanceOdomObject._distancia_x)
        dif_y = fabs(self.distance_y-self.DistanceOdomObject._distancia_y)
        self.distance_robot_coordinate=sqrt(pow(dif_x,2.0)+pow(dif_y,2.0))

