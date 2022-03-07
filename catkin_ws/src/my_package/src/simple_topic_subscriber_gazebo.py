#! /usr/bin/env python

# Este codigo se suscribe al topico correspondiente que publica los datos de odometria
# del turtlebot en gazebo
import rospy
from nav_msgs.msg import Odometry

# Definimos una funcion que recibe un argumento de entrada denominada msg
def callback(msg):
    print msg

# Creacion del nodo
rospy.init_node('lectura_odometria')
# Creamos un objeto suscriptor a /odom
sub = rospy.Subscriber('odom', Odometry, callback)
# Este comando mantiene el programa en bucle
rate = rospy.Rate(1)  # Rate de suscripcion de 1 Hz

while not rospy.is_shutdown():  # Creamos un loop
    rate.sleep()  # Esperamos a que se cumplan 2 Hz para seguir con el loop.
