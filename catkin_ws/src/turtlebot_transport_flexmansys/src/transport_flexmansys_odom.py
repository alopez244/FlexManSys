#! /usr/bin/env python

import rospy
from nav_msgs.msg import Odometry

'''''

Este script tiene como objetivo crear una suscripcion al topico /odom, la cual
permite obtener tanto la posicion cartesiana del robot en x,y,z y la posicion
angular en cuaterniones qx,qy,qz y qw

'''''

class OdomTopicReader(object):

    def __init__(self):
        self._odomdata = Odometry()
        self._sub = rospy.Subscriber('/odom', Odometry, self.topic_callback)
        rospy.sleep(0.2)

        self._distancia_x = self._odomdata.pose.pose.position.x
        self._distancia_y = self._odomdata.pose.pose.position.y
        self._distancia_z = self._odomdata.pose.pose.position.z
        self._rotacion_x = self._odomdata.pose.pose.orientation.x
        self._rotacion_y = self._odomdata.pose.pose.orientation.y
        self._rotacion_z = self._odomdata.pose.pose.orientation.z
        self._rotacion_w = self._odomdata.pose.pose.orientation.w

        self.orientation = self._odomdata.pose.pose.orientation


    def topic_callback(self, Odomdata):
        self._odomdata = Odomdata

        self._distancia_x = self._odomdata.pose.pose.position.x
        self._distancia_y = self._odomdata.pose.pose.position.y
        self._distancia_z = self._odomdata.pose.pose.position.z
        self._rotacion_x = self._odomdata.pose.pose.orientation.x
        self._rotacion_y = self._odomdata.pose.pose.orientation.y
        self._rotacion_z = self._odomdata.pose.pose.orientation.z
        self._rotacion_w = self._odomdata.pose.pose.orientation.w

    def get_odomdata(self):
        """
        Returns the newest odom data
        std_msgs/Header header
        uint32 seq
        time stamp
        string frame_id
        string child_frame_id
        geometry_msgs/PoseWithCovariance pose
        geometry_msgs/Pose pose
        geometry_msgs/Point position
        float64 x
        float64 y
        float64 z
        geometry_msgs/Quaternion orientation
        float64 x
        float64 y
        float64 z
        float64 w
        float64[36] covariance
        geometry_msgs/TwistWithCovariance twist
        geometry_msgs/Twist twist
        geometry_msgs/Vector3 linear
        float64 x
        float64 y
        float64 z
        geometry_msgs/Vector3 angular
        float64 x
        float64 y
        float64 z
        float64[36] covariance
        """

        return self._odomdata


if __name__ == "__main__":
    rospy.init_node('transport_flexmansys_odom', log_level=rospy.INFO)
    OdomTopicReader()
    rate = rospy.Rate(10)
    rospy.spin()