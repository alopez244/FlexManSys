#! /usr/bin/env python

import rospy

from math import atan
from math import fabs
from math import pi

from std_msgs.msg import String
from geometry_msgs.msg import Twist

from transport_flexmansys_imu import ImuTopicReader
from transport_flexmansys_odom import OdomTopicReader
from transport_flexmansys_bumper import BumperTopicReader
from transport_flexmansys_sound_manager import SoundManager
from transport_flexmansys_coordinates_bagfile import StationCoordinates

'''''
Este script tiene como objetivo gestionar todas las peticiones de movimiento sobre
el Kobuki que no sean directamente gestionadas por el paquete move_base. Es decir,
este script no tiene ningun efecto sobre el movimiento del Kobuki durante la 
ejecucion del global y local planner de move_base
'''''

class MovementManager(object):

    def __init__(self):

        # El Turtlebot2 recibe sus comandos de movimiento en el topico /mobile_base/commands/velocity,
        # es posible que en funcion del archivo launch empleado para arrancar el Kobuki, o bien el
        # modelo empleado en Gazebo, el topico sea diferente.

        self.publicar_velocidad = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=10)
        self.velocidad = Twist()
        self.ImuObject = ImuTopicReader()
        self.OdomObject = OdomTopicReader()
        self.BumperObject = BumperTopicReader()
        self.CoordinatesObject = StationCoordinates()

        self.Alpha = 0.0

    def InitialTranslation(self):

        # Esta funcion permite trasladarse 0.3 m en linea recta a la unidad de transporte.

        while self.OdomObject._distancia_x < 0.3:

            if self.BumperObject.BumperStopRequirement == True:

                # Si alguno de los Bumpers ha sido presionado, significa que hay algun obstaculo
                # en el camino. Detiene al AGV y entra en modo RECOVERY. (No es un estado, no confundir).

                self.StopTransport()
                self.BumperRecovery()

            elif self.BumperObject.BumperStopRequirement == False:

                self.velocidad.linear.x = 0.05
                self.velocidad.angular.z = 0.0
                self.publicar_velocidad.publish(self.velocidad)

    def InitialRotation(self):

        # Esta funcion determina si la rotacion debe de hacerse primero hacia la izquierda o
        # hacia la derecha, puesto que en funcion de cual haya sido el posicionamiento inicial
        # del Kobuki y su propia dinamica, puede haber rotado un cierto angulo en su traslacion
        # inicial.

        if self.ImuObject.currentOrientation <= 180.00:
            self.LeftRotation360()
            self.RightRotation360()

        elif self.ImuObject.currentOrientation > 180.00:
            self.RightRotation360()
            self.LeftRotation360()

    def LeftRotation360(self):

        # Funcion que rota unos 355.0 grados a izquierdas a la unidad de transporte

        while self.ImuObject.currentOrientation < 355.00:
            self.velocidad.linear.x = 0.0
            self.velocidad.angular.z = 0.4
            self.publicar_velocidad.publish(self.velocidad)

        self.StopTransport()
        rospy.sleep(1)

    def RightRotation360(self):

        # Funcion que rota unos 355.0 grados a derechas a la unidad de transporte

        while self.ImuObject.currentOrientation > 5.00:
            self.velocidad.linear.x = 0.0
            self.velocidad.angular.z = -0.4
            self.publicar_velocidad.publish(self.velocidad)

        self.StopTransport()
        rospy.sleep(1)


    def StopTransport(self):

        # Funcion que detiene a la unidad de transporte.

        self.velocidad.linear.x = 0.0
        self.velocidad.linear.y = 0.0
        self.velocidad.angular.z = 0.0
        self.publicar_velocidad.publish(self.velocidad)

    def GiroDerecha(self):

        # Funcion que efectua un giro a derechas

        self.velocidad.linear.x = 0.0
        self.velocidad.angular.z = -0.4
        self.publicar_velocidad.publish(self.velocidad)

    def GiroIzquierda(self):

        # Funcion que efectua un giro a izquierdas

        self.velocidad.linear.x = 0.0
        self.velocidad.angular.z = 0.4
        self.publicar_velocidad.publish(self.velocidad)

    def BumperRecovery(self):

        # Esta funcion hace que el kobuki retroceda una cierta distancia cuando
        # se choca contra un objeto para que posteriormente le de margen a maniobrar
        # y esquivarlo o bien se retire por un operario.

        # El AGV no saldra de esta funcion hasta que se introduzca por terminal la coordenada "FREEWAY".

        self.SoundObject = SoundManager("Obstacle")

        self.EntryOdom_x = self.OdomObject._distancia_x

        self.RecoverDistance = abs(self.OdomObject._distancia_x - self.EntryOdom_x)

        while self.RecoverDistance < 0.1:

            # Muevete 0.1 m hacia atras

            self.velocidad.linear.x = - 0.05
            self.velocidad.angular.z = 0.0
            self.publicar_velocidad.publish(self.velocidad)

            # Comprobar la distancia entre el punto en el que detecta el obstaculo
            # y la distancia recorrida hacia atras

            self.RecoverDistance = abs(self.OdomObject._distancia_x - self.EntryOdom_x)

        # Verifica junto con el Bumper Manager si el objeto sigue bloqueando al Kobuki

        self.BumperObject.bumper_recovery_petition()

        self.Coordinate = "NONE"

        while self.Coordinate != "FREEWAY":

           self._sub = rospy.Subscriber('/flexmansys/coordenada/leonardo', String, self.EnsureFreeWayCommand)
           rospy.sleep(0.5)


    def EnsureFreeWayCommand(self, sCoordinate):

        self.Coordinate = sCoordinate.data

    def RotationAdjustment(self, RequiredCoodinateInput):

        # Esta funcion, determinara el cuadrante en el que se encuentra la coordenada la cual queremos
        # desplazarnos con respecto al eje de coordenadas de base_link. Dado que el planificador local
        # del paquete move_base funciona mejor con coordenadas positivas (es decir, corrdenadas que se
        # encuentren delante del robot), es necesario hacer un pequeno ajuste rotacional de forma que
        # el robot encare la coordenada especificada. Se le asignara una distancia angular (grados) los
        # cuales tendra que girar el robot, sumando los grados respectivos del cuadrante en el que se
        # encuentra el punto y el el angulo que forman la arcotangente de la posicion actual del
        # kobuki a la hora de girar y el punto de la coordenada

        self.RequiredCoordinate = RequiredCoodinateInput

        self.CoordinatesObject.CoordinatesBagFile(self.RequiredCoordinate)

        # Eje X base_link = rojo
        # Eje Y base_link = verde
        # Eje Z base_link = azul

        #       0.0 <- X -> 360.0

        #            <---
        #              E2
        #              |
        #       C1     |    C4
        #              |
        #              X
        #              |
        # E3 -----Y----0------------ E1
        #              |
        #              |
        #       C2     |    C3
        #              |
        #              E4
        #            --->

        if self.CoordinatesObject.x > self.OdomObject._distancia_x:

            if self.CoordinatesObject.y > self.OdomObject._distancia_y:

                self.Cuadrante = "C1"
                self.Cuadrante_Rotation = 0.0

            if self.CoordinatesObject.y < self.OdomObject._distancia_y:

                self.Cuadrante = "C4"
                self.Cuadrante_Rotation = 270.0

        elif self.CoordinatesObject.x < self.OdomObject._distancia_x:

            if self.CoordinatesObject.y > self.OdomObject._distancia_y:

                self.Cuadrante = "C2"
                self.Cuadrante_Rotation = 90.0

            if self.CoordinatesObject.y < self.OdomObject._distancia_y:

                self.Cuadrante = "C3"
                self.Cuadrante_Rotation = 180.0

        elif self.CoordinatesObject.x == self.OdomObject._distancia_x:

            if self.CoordinatesObject.y > self.OdomObject._distancia_y:

                self.Cuadrante = "E2"
                self.Cuadrante_Rotation = 0.0

            if self.CoordinatesObject.y < self.OdomObject._distancia_y:

                self.Cuadrante = "E4"
                self.Cuadrante_Rotation = 180.0

        elif self.CoordinatesObject.y == self.OdomObject._distancia_y:

            if self.CoordinatesObject.x > self.OdomObject._distancia_x:

                self.Cuadrante = "E1"
                self.Cuadrante_Rotation = 270.0

            if self.CoordinatesObject.x < self.OdomObject._distancia_x:

                self.Cuadrante = "E3"
                self.Cuadrante_Rotation = 90.0

        self.AlphaCalculation(self.CoordinatesObject.x, self.CoordinatesObject.y,
                              self.OdomObject._distancia_x, self.OdomObject._distancia_y,
                              self.Cuadrante)

        self.RequiredRotation(self.Alpha, self.Cuadrante_Rotation)

    def AlphaCalculation(self, RC_X, RC_Y, CP_X, CP_Y, Cuadrante):

        # Calculo de la arcotangente entre el punto en el que se encuentra actualmente el
        # kobuki y el punto de la coordenada introducida

        # RC = Required Coordinate
        # CP = Current Position

        self.Opuesto = fabs(RC_X - CP_X)
        self.Contiguo = fabs(RC_Y - CP_Y)

        self.Alpha = atan(self.Opuesto/self.Contiguo)

        if Cuadrante == "C1" or Cuadrante == "C3":

            self.Alpha = self.Alpha * (180.0 / pi)
            self.Alpha = 90.0 - self.Alpha

        elif Cuadrante == "C2" or Cuadrante == "C4":

            self.Alpha = self.Alpha * (180.0/pi)


    def RequiredRotation(self, Alpha, Cuadrante_Rotation):

        # Esta funcion determinara si es necesario hacer un ajuste rotacional o no para la coordenada
        # introducida. Para ello, sumara 20 grados hacia su izquierda y restara 20 grados hacia su derecha,
        # es decir, que si el punto se encuentra en un rango de 40 frontales con respecto a su posicion
        # actual, el kobuki no gire. Si la rotacion es de 0.0 y el punto se encuentra en el rango de 20.0
        # y 340.0 grados, no se considera necesario hacer un ajuste rotacional.

        self.RequiredRotationValue = Cuadrante_Rotation + Alpha

        if self.ImuObject.currentOrientation >= 340.0:

            # Si le sumamos 20 grados a una posicion angular superior a 340, volvemos a 0 grados

            self.LeftLimit = 0.0 + 20.0 - (360 - self.ImuObject.currentOrientation)
            self.RightLimit = self.ImuObject.currentOrientation - 20.0

        elif self.ImuObject.currentOrientation < 340.0 and self.ImuObject.currentOrientation >= 20.0 :

            self.LeftLimit = self.ImuObject.currentOrientation + 20.0
            self.RightLimit = self.ImuObject.currentOrientation - 20.0

        elif self.ImuObject.currentOrientation < 20.0:

            # Si le restamos 20 grados a una posicion angular menor a 20, volvemos a 360 grados

            self.LeftLimit = self.ImuObject.currentOrientation + 20.0
            self.RightLimit = 360.0 - 20.0 + (20.0 - self.ImuObject.currentOrientation)

         # Si la coordenada requerida se encuentra dentro de un rango de 40 grados, 20 negativos y
         # 20 positivos, se indica que no es necesario un ajuste rotacional para mandar el goal
         # a move_base

        if self.RequiredRotationValue < self.LeftLimit and self.RequiredRotationValue > self.RightLimit:

            rospy.loginfo("Proceso de ajuste rotacional innecesario")

        else:

            rospy.loginfo("Proceso de ajuste rotacional necesario")

            # Cuando se hace un ajuste rotacional, el kobuki girara hasta encontrarse en un rango de
            # 5 grados entre el limite superior y el inferior del angulo el cual debe girar el AGV
            # para encarar la coordenada introducida. Es decir, el el Kobuki ha de girar hasta obtener
            # una rotacion de 300 grados, se considerara que el AGV ha encarado la coordenada si
            # su rotacion se encuentra entre 297.5 y 302.5 grados.

            # Dado que no hay valores negativos de rotacion y se encuentran todos en un rango de
            # 0 a 360 grados, es necesario detectar cuando al sumar o restar 2.5 grados podemos obtener
            # valores superiores a 360 grados o negativos inferiores a 0.0 grados.

            if self.RequiredRotationValue < 2.5:

                self.RRV_UpperLimit = self.RequiredRotationValue + 5.0
                self.RRV_LowerLimit = 0.0

                self.SimpleRotation(self.RRV_UpperLimit, self.RRV_LowerLimit)

            elif self.RequiredRotationValue >= 2.5 and self.RequiredRotationValue <= 357.50:

                self.RRV_UpperLimit = self.RequiredRotationValue + 2.5
                self.RRV_LowerLimit = self.RequiredRotationValue - 2.5

                self.SimpleRotation(self.RRV_UpperLimit, self.RRV_LowerLimit)

            elif self.RequiredRotationValue > 357.50:

                self.RRV_UpperLimit = 360.0
                self.RRV_LowerLimit = self.RequiredRotationValue - 5.0

                self.SimpleRotation(self.RRV_UpperLimit, self.RRV_LowerLimit)


    def SimpleRotation(self, UpperLimit, LowerLimit):

        # Hay que definir si el punto rotacional del kobuki y el de la coordenada requerida se encuentran
        # en el mismo plano de 180 grados, es decir, si sus cuadrantes son colindantes. En funcion de eso,
        # la diferencia angular (distancia angular) entre ambos puntos tomando un sentido a derechas
        # o a izquierdas podra ser mayor o menor. En funcion de si son colindantes sus cuadrantes o no
        # y de si su diferencia a derechas es superior o inferior a izquierdas, el sentido de giro mas
        # corto podra ser a derechas o a izquierdas para llegar a la posicion requerida.

        # En pocas palabras, se define cual es el camino mas corto para llegar a la coordenada introducida,
        # si girando a izquierdas o a derechas, y posteriormente se efectua el giro a medida que se lee
        # el valor rotacional del AGV hasta estar entre el LowerLimit y UpperLimit previamente calculado,
        # es decir, en un rango de 5 grados con respecto a la rotacion que ha de adquiir para
        # encarar la coordenara introducida.

        self.Difference_Current_Required = fabs(self.ImuObject.currentOrientation - self.RequiredRotationValue)

        if self.Difference_Current_Required >= 180.0:

            self.Current_Required = self.ImuObject.currentOrientation-self.RequiredRotationValue
            self.Required_Current = self.RequiredRotationValue - self.ImuObject.currentOrientation

            if self.Current_Required <= self.Required_Current:

                while not LowerLimit < self.ImuObject.currentOrientation < UpperLimit:

                    self.GiroDerecha()

                rospy.loginfo("Proceso de calibracion rotacional finalizado")

                self.StopTransport()
                rospy.sleep(1)

            elif self.Current_Required > self.Required_Current:

                while not LowerLimit < self.ImuObject.currentOrientation < UpperLimit:

                    self.GiroIzquierda()

                rospy.loginfo("Proceso de calibracion rotacional finalizado")

                self.StopTransport()
                rospy.sleep(1)

        elif self.Difference_Current_Required < 180.0:

            self.Current_Required = self.ImuObject.currentOrientation - self.RequiredRotationValue
            self.Required_Current = self.RequiredRotationValue - self.ImuObject.currentOrientation

            if self.Current_Required <= self.Required_Current:

                while not LowerLimit < self.ImuObject.currentOrientation < UpperLimit:
                    self.GiroIzquierda()

                rospy.loginfo("Proceso de calibracion rotacional finalizado")

                self.StopTransport()
                rospy.sleep(1)

            elif self.Current_Required > self.Required_Current:

                while not LowerLimit < self.ImuObject.currentOrientation < UpperLimit:
                    self.GiroDerecha()

                rospy.loginfo("Proceso de calibracion rotacional finalizado")

                self.StopTransport()
                rospy.sleep(1)



