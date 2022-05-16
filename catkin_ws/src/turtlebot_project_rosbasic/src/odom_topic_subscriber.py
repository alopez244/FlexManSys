#! /usr/bin/env python

import rospy
import time
from nav_msgs.msg import Odometry
import math

class OdomTopicReader(object):

    def __init__(self):

        # MUY IMPORTANTE, hay que declarar la clase en la que se recoge el callback, en este
        # caso _odomdata ANTES de suscribirnos a cualquier topico

        self._odomdata = Odometry()
        self._sub = rospy.Subscriber('/odom', Odometry, self.topic_callback)
        rospy.sleep(0.2)
        self._distancia_x = self._odomdata.pose.pose.position.x
        #print self._distancia_x
        self._distancia_y = self._odomdata.pose.pose.position.y
        # rospy.loginfo("jeje " + str(self._distancia_x))



    def topic_callback(self, msg):
        self._odomdata = msg
        #print msg
        # Logear informacion del mensaje con severidad tipo debug
        rospy.logdebug(self._odomdata)

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

    rospy.init_node('odom_topic_subscriber', log_level=rospy.INFO)
    OdomTopicReader()
    rate = rospy.Rate(1)
    rospy.spin()


