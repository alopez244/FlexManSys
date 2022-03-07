#! /usr/bin/env python

# Este programa crea un objeto publisher que publicara a un topico denominado
# "/counter" el contenido de una variable "count", la cual ira sumando +1 cada 2 Hz
import rospy
from std_msgs.msg import Int32  # Importamos el mensaje tipo Int de 32 bits de la liberia de ROS

rospy.init_node('topic_publisher')  # Iniciamos un nodo llamado topic_publisher
#  Con pub creamos un objeto publisher, que publicara en el topico /counter topic
pub = rospy.Publisher('/counter', Int32, queue_size=1)
rate = rospy.Rate(2)  # Rate de publicacion de 2 Hz
count = Int32()  # Creamos una variable del tipo Int32, es decir, un mensje tipo ROS de Int32 bits
count.data = 0  # Inicializamos su contenido a 0, el contenido de la variable "count"

while not rospy.is_shutdown():  # Creamos un loop
    pub.publish(count)  # Publicamos el mensaje con la variable count, en el topico /counter
    count.data += 1  # Incrementamos el valor de la variable count
    rate.sleep()  # Esperamos a que se cumplan 2 Hz para seguir con el loop.


