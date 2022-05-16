#! /usr/bin/env python

import rospy
from geometry_msgs.msg import Twist
import time

class CmdVelPub(object):

    def __init__(self):

        self.publicar_velocidad = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1 )
        self.velocidad = Twist()
        self.velocidad_lineal = 0.3
        self.velocidad_angular = 0.5

        self.ctrl_c = False

        rospy.on_shutdown(self.shutdownhook)

    def move_robot (self, direction):
        if direction == "forwards":
            self.velocidad.linear.x = self.velocidad_lineal
            self.velocidad.angular.z = 0.0
        elif direction == "right":
            self.velocidad.linear.x = 0.0
            self.velocidad.angular.z = -self.velocidad_angular
        elif direction == "left":
            self.velocidad.linear.x = 0.0
            self.velocidad.angular.z = self.velocidad_angular
        elif direction == "backwards":
            self.velocidad.linear.x = -self.velocidad_lineal
            self.velocidad.angular.z = 0.0
        elif direction == "stop":
            self.velocidad.linear.x = 0.0
            self.velocidad.angular.z = 0.0
        else:
            pass

        # Se publica una vez el mensaje en el topico, luego tenemos que asegurarnos de que
        # ha llegado correctamente y se publica bien

        while not self.ctrl_c:
            connections = self.publicar_velocidad.get_num_connections()
            if connections > 0:

                # moving_time=0.5 # 500 ms le damos para moverse
                moving_time = 4.0
                self.publicar_velocidad.publish(self.velocidad)
                rospy.loginfo("Velocidad Publicada: " + direction)
                time.sleep(moving_time)
                self.stop_kobuki()
                break
            else:
                pass

    def shutdownhook(self):

        self.stop_kobuki()
        self.ctrl_c = True

    def stop_kobuki(self):

        # rospy.loginfo("Parar Kobuki")
        self.velocidad.linear.x = 0.0
        self.velocidad.angular.z = 0.0
        self.publicar_velocidad.publish(self.velocidad)

        self.velocidad_lineal = 0.3
        self.velocidad_angular = 0.5


if __name__ == "main":
    rospy.init_node ('cmd_vel_publisher_node')
    cmd_publisher_object = CmdVelPub()

    rate = rospy.Rate(1)

    def shutdownhook():
        global ctrl_c
        global twist_object
        
