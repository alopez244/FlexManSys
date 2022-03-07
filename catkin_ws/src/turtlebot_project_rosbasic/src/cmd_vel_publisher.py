#! /usr/bin/env python

import rospy
from geometry_msgs.msg import Twist

class CmdVelPub(object):

    def __init__(self):
        self.publicar_velocidad = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1 )
        self.velocidad = Twist()
        self.velocidad_lineal = 0.3
        self.velocidad_angular = 0.5

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

        self.publicar_velocidad.publish(self.velocidad)

if __name__ == "main":
    rospy.init_node ('cmd_vel_publisher_node')
    cmd_publisher_object = CmdVelPub()

    rate = rospy.Rate(1)

    def shutdownhook():
        global ctrl_c
        global twist_object
