#! /usr/bin/env python

# Este codigo genera un cliente el cual espera al servidor /move_bb8_in_square
# una vez lo encuentra inicializado entre la lista de los servicios que tiene presentes
# realizara la conexion con el servicio /move_bb8_in_square y pedira al servidor que
# haga lo que tiene que hacer, que en ese caso, sera  mover el BB8 (O kOBUKI)

import rospy

# La libreria de rospkg nos permite generar dependencias entre scripts de python y cpp
# es decir, puede guardar como variable el directorio de un fichero o generar objetos
# que esten asociados a x directorio que queramos

import rospkg

# Importamos el tipo de mensaje de servicio comun, el Empty
from std_srvs.srv import Empty, EmptyRequest

# Initialise a ROS node with the name service_client
rospy.init_node('service_move_bb8_in_square_client')
# Wait for the service client /move_bb8_in_square to be running
rospy.wait_for_service('/move_bb8_in_square')
# Create the connection to the service
move_bb8_in_square_service_client = rospy.ServiceProxy('/move_bb8_in_square', Empty)
# Create an object of type EmptyRequest
move_bb8_in_square_request_object = EmptyRequest()

# Send through the connection the path to the trajectory file to be executed
result = move_bb8_in_square_service_client(move_bb8_in_square_request_object)
# Print the result given by the service called
print result