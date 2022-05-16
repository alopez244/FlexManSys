#!/usr/bin/env python

import rospy
import time
from sensor_msgs.msg import LaserScan
from turtlebot_project_rosbasic.srv import CrashDirection, CrashDirectionResponse
from std_srvs.srv import Trigger, TriggerResponse

# No hace falta en si crear un nuevo tipo de mensaje y servicio, en este caso CrashDirection
# dentro de los dados por defecto de ROS existe el Trigger, que tiene una misma estructura
# con la salvedad de que se emplea "message" en vez de "data" para devolver el string
# o comando de velocidad

class LaserTopicReader(object):

    def __init__(self):

        # MUY IMPORTANTE, hay que declarar la clase en la que se recoge el callback, en este
        # caso _laserdata de suscribirnos a cualquier topico

        self._laserdata = LaserScan()
        self._sub = rospy.Subscriber('/scan', LaserScan, self.topic_callback)
        rospy.sleep(0.3)
        self._front = 0.0
        self._right = 0.0
        self._left = 0.0
        rospy.sleep(0.3)


    def topic_callback(self, msg):
        self._laserdata = msg

        # La siguiente funcion logea el mensaje y su tipo con severidad del tipo DEBUG
        # es decir, en la capa mas exterior de la rosquilla de los LOGS

        rospy.logdebug(self._laserdata)

    def get_laserdata(self):

        # Esta funcion directamente lo que hace es devolverte el objeto self._laserdata
        # el cual cuenta con el mensaje LaserScan, que tiene todos los siguientes campos:

        """
        Returns the newest odom data
        std_msgs/Header header
        uint32 seq
        time stamp
        string frame_id
        float32 angle_min
        float32 angle_max
        float32 angle_increment
        float32 time_increment
        float32 scan_time
        float32 range_min
        float32 range_max
        float32[] ranges
        float32[] intensities
        """

        return self._laserdata

    def crash_detector(self):

        # Por alguna extrania razon, la posicion 360 de ranges siempre da NaN, es por ello
        # que se pillan varias posiciones y se coge la minima de todas ellas para tener siempre
        # al menos un numero

        self._front = self._laserdata.ranges[350:370]
        self._front = min(self._front)
        self._right = self._laserdata.ranges[0]
        self._left = self._laserdata.ranges[630]

        rospy.loginfo("Front Distance == "+str(self._front))
        rospy.loginfo("Left Distance == "+str(self._left))
        rospy.loginfo("Right Distance == "+str(self._right))

        if self._left < 1.0:
            direction_command = "right"
        elif self._right < 1.0:
            direction_command = "left"
        elif self._front < 0.7:
            direction_command = "backwards"
        else:
            direction_command = "forwards"

        # direction_command = "forwards"

        return direction_command

    def deteccion_bloqueo(self):

        # self.crash_detector()

        if self._front < 0.8:

            rospy.loginfo("Kobuki se va a chocar")

            return True

        elif self._front >= 0.8:

            return False

        else:
            return False



def definir_servicio(request):

    lectura_lidar = LaserTopicReader()
    # print "hello"
    response =CrashDirectionResponse()
    response.data = lectura_lidar.crash_detector()
    response.success = lectura_lidar.deteccion_bloqueo()
    return response

if __name__ == "__main__":
    rospy.init_node('servidor_lectura_lidar')
    my_service = rospy.Service('/scan_service', CrashDirection, definir_servicio)
    rospy.loginfo("Servicio Lectura LiDAR Operativo")
    rospy.Rate(0.5)
    rospy.spin()  # mantain the service open.

"""
            
        if self._left < 1.0 and self._front > 1.0 and self._left > 1.0:
            direction_command = "right"
        elif self._right < 1.0 and self._front > 1.0 and self._right > 1.0:
            direction_command = "left"
        elif self._front < 1.0 and self._right > 1.0 and self._left > 1.0:
            direction_command = "backwards"
        elif self._front < 1.0 and self._right < 1.0:
            # Es decir, el robot se encuentra en una esquina |__ y tiene que girar a izquierdas
           direction_command = "left"
        elif self._front < 1.0 and self._left < 1.0:
            # Es decir, el robot se encuentra en una esquina __|  y tiene que girar a derechas
           direction_command = "right"
        else:
            direction_command = "forwards"

            
            
        if self._left < 0.7:
            direction_command = "right"
        elif self._right < 0.7:
            direction_command = "left"
        elif self._front < 1.0:
            direction_command = "backwards"
        else:
            direction_command = "forwards"
"""