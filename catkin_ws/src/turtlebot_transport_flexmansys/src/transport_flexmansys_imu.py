#! /usr/bin/env python

import rospy

from math import degrees
from sensor_msgs.msg import Imu
from tf.transformations import euler_from_quaternion

'''''

Este script tiene como objetivo crear una suscripcion al topico /imu, la cual
permite la posicion angular en quaterniones qx,qy,qz y qw directamente del propio
giroscopio o IMU del Kobuki, el cual le permite crear sus homologos cuaterniones
en el topico /odom

'''''

class ImuTopicReader(object):

    def __init__(self):

        self._sub = rospy.Subscriber('/mobile_base/sensors/imu_data', Imu, self.imu_callback)
        rospy.sleep(0.2)

    def imu_callback(self, ImuData):

        # Se emplean las siguientes lineas para convertir de cuaternios a grados la rotacion de la
        # unidad de transporte, para hacer mas facil su lectura y manipulacion a lo largo del nodo.

        quat = ImuData.orientation
        q = [quat.x, quat.y, quat.z, quat.w]

        _, _, yaw =euler_from_quaternion(q)
        self.currentOrientation = degrees(yaw)

        # Nos aseguramos de que el valor de rotacion siempre sea positivo, de forma que el valor de
        # rotacion vaya siempre de 0.0 grados a 360.0 grados, entendiendo como rotacion positiva
        # una rotacion anti-horaria (a iziquierdas sobre el eje Z)

        if(self.currentOrientation <0):
            self.currentOrientation += 360.00


    def get_imudata(self):

        '''
        header:
          seq: 0
          stamp:
            secs:
            nsecs:
          frame_id: "gyro_link"
        orientation:
          x: 0.0
          y: 0.0
          z: 0.0
          w: 1.0
        orientation_covariance: [1.7976931348623157e+308, 0.0, 0.0, 0.0, 1.7976931348623157e+308, 0.0, 0.0, 0.0, 0.05]
        angular_velocity:
          x: 0.0
          y: 0.0
          z: 0.0
        angular_velocity_covariance: [1.7976931348623157e+308, 0.0, 0.0, 0.0, 1.7976931348623157e+308, 0.0, 0.0, 0.0, 0.05]
        linear_acceleration:
          x: 0.0
          y: 0.0
          z: 0.0
        linear_acceleration_covariance: [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        '''

        return self.currentOrientation

if __name__ == "__main__":
    rospy.init_node('transport_flexmansys_imu', log_level=rospy.INFO)
    ImuTopicReader()
    rate = rospy.Rate(10)
    rospy.spin()