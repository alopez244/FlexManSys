#! /usr/bin/env python

# Este programa crea un objeto publisher que publicara comandos de velocidad a un
# mundo de gazebo totalmente vacio. Cada vez que se publiquen nuevos datos en el
# topico, el Turtlebot reaccionara y se movera un poco, es decir, que no retiene
# en memoria lo suscrito en el topico y lo ejecuta de manera interna como un loop

import rospy
# Usando rostopic list, vemos que el topico que gestiona la velocidad puede ser
# /mobile_base/commands/velocity , por lo que hacemos un rostopic echo a ese topico
# Sale que ese topico trabaja con mensajes del tipo /geometry_msgs.msg, concretamente
# con los de tipo Twist, por lo que los mensajes que vayan a publicarse en ese topico
# al cual esta suscrito gazebo, es mediante Twist
from geometry_msgs.msg import Twist

rospy.init_node('comandos_velocidad')  # Iniciamos un nodo llamado comandos_velocidad
#  Con pub creamos un objeto publisher, que publicara en el topico /mobile_base/commands/velocity
pub = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1)
rate = rospy.Rate(1)  # Rate de publicacion de 1 Hz
move_cmd = Twist()  # Creamos una variable del tipo Twist, propias del Turtlebot
move_cmd.linear.x = 0.5  # En x nos movemos de manera lineal sobre el plano
move_cmd.angular.z = 0.1  # En z nos movemos de manera angular sobre el plano

while not rospy.is_shutdown():  # Creamos un loop
    # Publicamos el mensaje con la variable count, en el topico /mobile_base/commands/velocity
    pub.publish(move_cmd)
    rate.sleep()  # Esperamos a que se cumplan 2 Hz para seguir con el loop.

