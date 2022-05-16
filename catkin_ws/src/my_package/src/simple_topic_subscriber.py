#! /usr/bin/env python

import rospy
from std_msgs.msg import Int32

# Definimos una funcion que recibe un argumento de entrada denominada msg
def callback(msg):
    print msg.data

# Creacion del nodo
rospy.init_node('topic_subscriber')
# Creamos un objeto suscriptor a /counter
sub=rospy.Subscriber('counter', Int32, callback)
# Este comando mantiene el programa en bucle
rospy.spin()