#! /usr/bin/env python

import rospy
import actionlib

from actionlib import ActionServer

from transport_flexmansys_coordinates_bagfile import StationCoordinates

from turtlebot_transport_flexmansys.srv import TransportServiceMessage, TransportServiceMessageResponse
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal, MoveBaseResult, MoveBaseFeedback

'''''

Este script tiene como objetivo el crear un nodo servidor del nodo principal
flexmansys_transport_node_NombreUnidadTransporte, el cual a su vez se tratara 
de un cliente de accion del paquete move_base enviando como "goal" las coordenadas 
asociadas a la estacion publicada en el topico "/flexmansys/coordinate/NombreUnidadTransporte"

'''''

class FlexmansysTransportNodeServer(object):

    def __init__(self):

        # Previemante, hay que iniciar un nodo que sea el servidor del nodo main, en este caso
        # 'flexmansys_transport_node_leonardo', siendo este a su vez cliente del GateWay Agent,
        # que tiene comunicacion directa con los agentes de la plataforma JADE.

        rospy.init_node('flexmansys_transport_node_server_leonardo')
        rospy.sleep(0.5)

        self.TransportServiceResponse()

    def TransportServiceResponse(self):

        # Al iniciar el nodo de servicio, se inicia un servicio denominado /flexmansys/transport
        # /service/leonardo. Una vez se cree este servicio, el nodo main, el cliente, sabra que
        # el servidor estara listo para recibir peticiones.

        rospy.Service('/flexmansys/transport/service/leonardo', TransportServiceMessage, self.TransportServiceCallback)
        rospy.sleep(0.1)

    def TransportServiceCallback(self, request):

        # Una vez desde el nodo cliente se llame al servidor, se entrara en esta funcion, la
        # cual recoje el request del cliente para convertirlo en el goal de la accion que
        # efectuara el paquete move_base

        rospy.loginfo("Se ha llamado al servicio /flexmansys/transport/service/leonardo")

        # Se convierte el request del cliente en el goal

        self.TransportGoal= request.coordinate

        # Se crea un cliente a partir del nodo flexmansys_transport_node_server_leonardo, cuyo
        # servidor en la accion sera el nodo move_base.

        self.NavigationActionClient()

        # Se indica que el servicio se ha efectuado correctamente, pasando un flag True al
        # cliente que ha invocado al servicio.

        response = TransportServiceMessageResponse()
        response.success = True
        return response

    def NavigationActionClient(self):

        # Se crea un cliente de accion con respecto al nodo move_base

        self.TransportClient = actionlib.SimpleActionClient('/move_base', MoveBaseAction)

        # Se espera a que el servidor, move_base, este activo

        self.TransportClient.wait_for_server()

        # Se indica cual es el goal que se va a transmitir al cliente move_base en la accion
        # y la tf o frame que debe de tomar como referencia, es decir, como sistema de coordenadas.
        # En este caso, las coordenadas se referencian al sistema de coordenadas /map, es por ello,
        # que los AGV deben de inicializarse siempre desde su pose = 0.0, o bien crear una tf desde
        # su lugar de launch y el eje de coordenadas inicial del mapa.

        goal = MoveBaseGoal()
        goal.target_pose.header.frame_id = 'map'

        self.CoordinateObject = StationCoordinates()
        self.CoordinateObject.CoordinatesBagFile(self.TransportGoal)

        rospy.loginfo("Desplazando a coordenada: " + str(self.TransportGoal))
        goal.target_pose.pose.position.x = self.CoordinateObject.x
        goal.target_pose.pose.position.y = self.CoordinateObject.y
        goal.target_pose.pose.position.z = self.CoordinateObject.z
        goal.target_pose.pose.orientation.x = self.CoordinateObject.qx
        goal.target_pose.pose.orientation.y = self.CoordinateObject.qy
        goal.target_pose.pose.orientation.z = self.CoordinateObject.qz
        goal.target_pose.pose.orientation.w = self.CoordinateObject.qw

        # Si responde move_base tras mandarle el goal, se da por sentado que la comunicacion se ha hecho
        # correctamente, por lo que indicamos que el desplazamiento ya ha comenzado por terminal.

        self.TransportClient.send_goal(goal, feedback_cb=self.NavigationCallback())

        # Esperamos a que el result o resultado de la accion sea True, es decir, a que el servidor de la
        # accion haya terminado sus cosas. Asi, sabremos que se ha llegado a la coordenada introducida.

        self.TransportClient.wait_for_result()

    def NavigationCallback(feedback):

        rospy.loginfo('Unidad de Transporte en Desplazamiento')

if __name__ == '__main__':

    objeto_navegacion_cliente = FlexmansysTransportNodeServer()
    rospy.Rate(0.5)
    rospy.spin()


