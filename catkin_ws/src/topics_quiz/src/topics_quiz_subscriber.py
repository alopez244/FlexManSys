#! /usr/bin/env python

import rospy
from sensor_msgs.msg import LaserScan
from geometry_msgs.msg import Twist

rospy.init_node('obstacle_navigation')

pub = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1)
rate = rospy.Rate(2)
vel = Twist()
vel.linear.x = 0.5
vel.angular.z = 0
dist = 0.1
pub.publish(vel)


def turn_callback(msg):
    # El array donde se guardan los valores de lectura del scan cuenta con varias posiciones.
    # Al ser un scanner de solo 180 grados, es decir, los de la camanra frontal Kinect en
    # el modelo de gazebo, cuando se pierde el objeto frontal, el scanner ya no detecta una
    # distancia, es decir, detecta vacio. Al detectar vacio, el numero que devuelve es "NaN",
    # luego no entra al bucle y simplemente se dedica a ir hacia delante
    # Lo que hacemos es elegir una posicion correcta donde desde el inicio lea un valor que no
    # sea un NaN, pero claro, a medida que va realizando su navegacion, habra un momento en el que
    # en el mundo practicamente vacio deje de detectar objetos.

    dist = msg.ranges[1] #  posicion correspondiente 360 del array
    print dist
    if dist <6:
        vel.angular.z = 1
        vel.linear.x = -0.2
        pub.publish(vel)


while not rospy.is_shutdown():
    pub.publish(vel)
    rospy.Subscriber('/scan', LaserScan, turn_callback)
    # rospy.spin()
    rate.sleep()  # Esperamos a que se cumplan 2 Hz para seguir con el loop.