#! /usr/bin/env python
# Esta primera linea asegura que el interprete que vamos a usar es el
# adecuado para la aplicacion, es decir, es el .exe de python respectivo

# Importamos la libreria de ROS respectiva para python
import rospy

rospy.init_node('ObiWan')  # Iniciamos un nodo llamado ObiWan
rate = rospy.Rate(2)  # Creamos un obtejo Rate de 2 Hz
while not rospy.is_shutdown():
    print "Help me Obi-Wan, you're my only hope"  # Mensaje que imprime por pantalla
    rate.sleep()  # Dormimos lo necesario para cumplir con el rate de 2H z
    