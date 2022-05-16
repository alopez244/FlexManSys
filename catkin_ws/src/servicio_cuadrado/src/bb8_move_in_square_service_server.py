#! /usr/bin/env python

# Este codigo genera un servidor de ROS-python. Lo llamara el cliente denominado
# bb8_move_in_square_service_client.py y se encargara de ejecutar un servicio.
# El cliente estara esperando hasta que el servicio haya terminado, el cual en este caso
# sera en el siguiente orden:
# 1. Logear por pantalla que se ha llamado al servicio servidor correctamente
# 2. Definir al objeto movebb8_object como un objeto de clase "MoveBB8" definido en move_bb8.oy
# 3. Ejecutar la funcion dentro de la clase "MoveBB8" que hace move en cuadrado al robot
# 4. Logear por pantalla que el servicio llamado ha terminado su servicio
#

import rospy
from std_srvs.srv import Empty, EmptyResponse

# Importamos la clase MoveBB8 desde move_bb8.py
from move_bb8 import MoveBB8

def my_callback(request):
    rospy.loginfo("The Service move_bb8_in_square has been called")
    movebb8_object = MoveBB8()
    movebb8_object.move_square()
    rospy.loginfo("Finished service move_bb8_in_square that was called called")
    return EmptyResponse() # the service Response class, in this case EmptyResponse
    #return MyServiceResponse(len(request.words.split()))

# Iniciamos el nodo servicio servidor del movimiento en cuadrado del BB8
rospy.init_node('service_move_bb8_in_square_server')
# El nodo publicara el servicio al "topico" /move_bb8_in_square
my_service = rospy.Service('/move_bb8_in_square', Empty , my_callback)
# Definimos por pantalla que el servicio /move_bb8_in_square esta preparado
rospy.loginfo("Service /move_bb8_in_square Ready")
rospy.spin() # mantain the service open.