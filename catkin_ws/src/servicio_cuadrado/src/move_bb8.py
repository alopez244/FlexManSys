#!/usr/bin/env python

import rospy
from geometry_msgs.msg import Twist
import time
import math


class MoveBB8():

    def __init__(self):
        # Con def __init__ lo que hacemos es definir una clase de python. Se podria poner simplemente
        # sin el (self), sin embargo, el self nos permite hacer variables locales de esa misma clase.
        # Es decir, si tenemos una funcion que suma dos numeros a y b, y no estamos usando el self, esas
        # dos variables a y b seran "globales", es decir, que una vez ejecutada la clase no se borran
        # de memoria. Con self, podemos generar los objetos self.a y self.b, los cuales son variables
        # locales. Puede llamarse de cualquier manera, los programadores simplemente lo llaman self

        # Definimos un objeto local de la clase como aquel que publica al topico /mobile_base/commands/velocity
        self.bb8_vel_publisher = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1)

        # Definimos otro objeto booleano simplemente pasa saber si se ha cancelado la ejecucion por terminal
        # no se trata de ninguna funcion especial del objeto MoveBB8 ni de rospy.
        self.ctrl_c = False

        # on_shutdown lo que hace es registrar una funcion del programa para que se le llame en caso de que
        # el programa se termine de manera expontanea. En este caso, llama a la funcion local de este programa
        # shutdown hook
        rospy.on_shutdown(self.shutdownhook)

        # esto lo que hace es generar un loop de 10 Hz, es decir, que duerme el loop para que se ejecute
        # cada 10 Hz
        self.rate = rospy.Rate(10)  # 10hz

    def publish_once_in_cmd_vel(self, cmd):
        """
        This is because publishing in topics sometimes fails the first time you publish.
        In continuos publishing systems there is no big deal but in systems that publish only
        once it IS very important.
        """
        while not self.ctrl_c:
            connections = self.bb8_vel_publisher.get_num_connections()
            if connections > 0:
                self.bb8_vel_publisher.publish(cmd)
                rospy.loginfo("Cmd Published")
                break
            else:
                self.rate.sleep()

    def shutdownhook(self):
        # works better than the rospy.is_shut_down()
        # Basicamente lo le diferencia de rospy.is_shut_down es que te permite asignar acciones
        # que debe de realizar el robot una vez se para la ejecucion de manera espontanea
        # al ser una funcion de creacion hecha por el programador

        self.stop_bb8()
        self.ctrl_c = True

    def stop_bb8(self):
        # Simplemente manda el comando de parar al robot en caso de que se haya dado un paro
        # se llama a traces de la funcion local shutdownhook, que a su vez esta asignada al
        # registro de on_shutdown

        rospy.loginfo("shutdown time! Stop the robot")
        cmd = Twist()
        cmd.linear.x = 0.0
        cmd.angular.z = 0.0
        self.publish_once_in_cmd_vel(cmd)

    def move_x_time(self, moving_time, linear_speed=0.2, angular_speed=0.2):

        cmd = Twist()
        cmd.linear.x = linear_speed
        cmd.angular.z = angular_speed

        rospy.loginfo("Moving Forwards")
        self.publish_once_in_cmd_vel(cmd)
        time.sleep(moving_time)
        self.stop_bb8()
        rospy.loginfo("######## Finished Moving Forwards")

    def move_square(self, side=0.2):

        i = 0
        # More Speed, more time to stop
        time_magnitude = side / 0.2

        while not self.ctrl_c and i < 4:
            # Move Forwards
            self.move_x_time(moving_time=2.0 * time_magnitude, linear_speed=2, angular_speed=0.0)
            # Stop
            self.move_x_time(moving_time=4.0, linear_speed=0.0, angular_speed=0.0)
            # Turn, the turning is not afected by the length of the side we want
            self.move_x_time(moving_time=4.0, linear_speed=0.0, angular_speed=2)
            # Stop
            self.move_x_time(moving_time=0.1, linear_speed=0.0, angular_speed=0.0)

            i += 1

        rospy.loginfo("######## Finished Moving in a Square")


if __name__ == '__main__':

    # El main del .py de la clase MoveBB8, basicamente inicia el nodo bajo el nombre move_bb8_test
    # y genera un objeto denominado movebb8_object de clase MoveBB8, el cual se importara desde
    # los servidores clientes.

    rospy.init_node('move_bb8_test', anonymous=True)
    movebb8_object = MoveBB8()


    # La estructura try y except nos permiten generar acciones para las cuales el programa puede
    # responder en caso de que se produzca una excepcion durante la ejecucion de este codigo
    # De forma que si hay una excepcion registra la excepcion y puede llegarse a mostrarse por pantalla
    # indicandole que si hay una excepcion intente realizar la funcion move_square en caso de excepcion
    # y si no, pues nada, indicar excepcion.

    try:

        movebb8_object.move_square(side=0.6)

    # ROSInterruptException es una clase propia de rospy para excepciones internas
    except rospy.ROSInterruptException:
        # aqui con un return 'frase que sea o valor que queramos' podemos devolver por pantalla
        # lo que queramos
        # en resumen, una pequena trampa para que no salgan errores a la hora ejecutar rollo warnings
        pass