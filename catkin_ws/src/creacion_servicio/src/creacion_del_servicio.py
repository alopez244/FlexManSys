#! /usr/bin/env python

import rospy

# Con la siguiente linea se importa el mensaje servicio comun definido por ROS para
# que python pueda generar tipos correspondientes a ello

from std_srvs.srv import Empty, EmptyResponse


def my_callback(request):
    print "My_callback has been called"
    # Debe devolver la clase del tipo del servicio, en este caso EmptyResponse
    return EmptyResponse()

    # Toda respuesta de un servicio puede solicitarse a través de Python mediante
    # la siguiente estructura:
    # return MyServiceResponse(len(request.words.split()))


rospy.init_node('service_server')
my_service = rospy.Service('/my_service', Empty, my_callback)
rospy.spin()  # Mantiene el servicio abierto a nuevas peticiones
