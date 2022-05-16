#! /usr/bin/env python

import rospy
import time
from sensor_msgs.msg import LaserScan

class LaserTopicReader(object):

    def __init__(self, topic_name='/scan'):
        self._topic_name = topic_name
        self._sub = rospy.Subscriber(self._topic_name, LaserScan, self.topic_callback)
        self._laserdata = LaserScan()
        self._front = 0.0
        self._right = 0.0
        self._left = 0.0

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

        self._left = self._laserdata.ranges[719]
        self._front = self._laserdata.ranges[360]
        self._right = self._laserdata.ranges[0]

        rospy.loginfo("Front Distance == "+str(self._front))
        rospy.loginfo("Left Distance == "+str(self._left))
        rospy.loginfo("Right Distance == "+str(self._right))


if __name__ == "__main__":
    rospy.init_node('laser_topic_subscriber', log_level=rospy.INFO)
    laser_reader_object = LaserTopicReader()
    time.sleep(2)
    rate = rospy.Rate(0.5)

    while not rospy.is_shutdown():  # Creamos un loop
        rate.sleep()  # Esperamos a que se cumplan 2 Hz para seguir con el loop. 
        
