#! /usr/bin/env python

import rospy

# Al igual que con los topicos, hay que importar tambien el tipo de mensaje
from gazebo_msgs.srv import DeleteModel, DeleteModelRequest
import sys

# Inicializamos un nodo ROS con el nombre "service_client"
rospy.init_node('service_client')
# Esperamos a que el servicio /gazebo/delete_model este activo
rospy.wait_for_service('/gazebo/delete_model')
# Se crea la conexion con el sercicio /gazebo/delete_model
delete_model_service = rospy.ServiceProxy('/gazebo/delete_model', DeleteModel)
# Se crea un objeto de tipo DeleteModelRequest
delete_model_object = DeleteModelRequest()
# Se rellena el "model_name" correspondiente al tipo DeleteModelRequest con el valor deseado
delete_model_object.model_name = 'Dumpster'
# Se envia a traves de la conexion con el servicio el nombre del objeto a ser eliminado por el servicio
result = delete_model_service(delete_model_object)
print result