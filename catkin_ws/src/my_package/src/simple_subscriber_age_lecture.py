#! /usr/bin/env python

# Este codigo se suscribe al topico correspondiente que publica los datos de odometria
# del turtlebot en gazebo
import rospy
from my_package.msg import Age

#Creacion del publicista
rospy.init_node('edad_kobuki')  # Iniciamos un nodo llamado edad kobuki
#  Con pub creamos un objeto publisher, que publicara en el topico /mobile_base/commands/velocity
pub = rospy.Publisher('edad_kobuki', Age, queue_size=1)
rate = rospy.Rate(2)
edad=Age()
edad.years = 8.0
edad.months = 10.0
edad.days = 230.0

while not rospy.is_shutdown():
    #rospy.loginfo(edad)
    pub.publish(edad)
    rate.sleep()
