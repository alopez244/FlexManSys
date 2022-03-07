#! /usr/bin/env python

import rospy
from ar_track_alvar_msgs.msg import AlvarMarkers

'''''

Elte script tiene como objetivo crear una clase que se suscriba al topico /ar_pose_marker, 
creado por el paquete ar_track_pose el cual procesa las imagenes publicadas en el topico
/camera/image/rgb_color por el paquete freenect_launch a traves de la camara Kinect
para detectar codigos AR.

'''''


class KinectManager(object):

    def __init__(self):

        self._sub = rospy.Subscriber('/ar_pose_marker', AlvarMarkers, self.topic_callback)
        rospy.sleep(0.5)

        # De todos los campos e informacion que provee el topico /ar_pose_marker solo nos interesan
        # la ID del codigo AR y la distancia con respecto a base_link a la que se encuentran.

        self.ARCodeID = 1000

        self._AR_distancia_x = 0.0
        self._AR_distancia_y = 0.0
        self._AR_distancia_z = 0.0

    def read_kinect(self):

        self._sub = rospy.Subscriber('/ar_pose_marker', AlvarMarkers, self.topic_callback)
        rospy.sleep(0.5)

    def topic_callback(self, DataKinect):

        # Cada codigo AR cuenta con su propia ID, en funcion de la conbinacion de cuadrados negros
        # y blancos que presenten en el dibujo.

        self.DataKinect = DataKinect

        self.ARCodeID = self.DataKinect.markers[0].id

        self._AR_distancia_x = self.DataKinect.markers[0].pose.pose.position.x
        self._AR_distancia_y = self.DataKinect.markers[0].pose.pose.position.y
        self._AR_distancia_z = self.DataKinect.markers[0].pose.pose.position.z

