#! /usr/bin/env python


import rospy
import roslib; roslib.load_manifest(('kobuki_testsuite'))
from datetime import datetime

from std_msgs.msg import String
from kobuki_msgs.msg import SensorState

from turtlebot_transport_flexmansys.msg import TransportUnitState
from turtlebot_transport_flexmansys.msg import TransportPrivateState


from math import degrees
from tf.transformations import euler_from_quaternion

from sensor_msgs.msg import Imu
from transport_flexmansys_odom import OdomTopicReader


'''''

Script que recoge el codigo encargado de la gestion del nodo publicista del estado y demas
datos de la unidad de transporte correspondiente. 

'''''

class TransportUnitStatePublisher(object):

    def __init__(self, initialize):

        self.TransportUnitStatePublisherObject = TransportUnitState()

        rospy.Subscriber('/flexmansys/private/state/leonardo', TransportPrivateState, self.TUSP_MachineState)
        rospy.sleep(0.2)

        if initialize == True:

            self.TransportUnitStatePublisherObject.kobuki_general.transport_unit_name = "Leonardo"
            self.TransportUnitStatePublisherObject.kobuki_general.transport_unit_state = "Undefined"
            self.TransportUnitStatePublisherObject.kobuki_general.battery = 0.0

            self.TransportUnitStatePublisherObject.kobuki_obstacle.detected_obstacle_bumper = False
            self.TransportUnitStatePublisherObject.kobuki_obstacle.detected_obstacle_camera = False

            self.TransportUnitStatePublisherObject.kobuki_position.transport_in_dock = True
            self.TransportUnitStatePublisherObject.kobuki_position.recovery_point = "NONE"
            self.TransportUnitStatePublisherObject.kobuki_position.odom_x = 0.0
            self.TransportUnitStatePublisherObject.kobuki_position.odom_y = 0.0
            self.TransportUnitStatePublisherObject.kobuki_position.rotation = 0.0

            self.TransportUnitStatePublisherObject.odroid_date.year = 0
            self.TransportUnitStatePublisherObject.odroid_date.month = 0
            self.TransportUnitStatePublisherObject.odroid_date.day = 0
            self.TransportUnitStatePublisherObject.odroid_date.hour = 0
            self.TransportUnitStatePublisherObject.odroid_date.minute = 0
            self.TransportUnitStatePublisherObject.odroid_date.seconds = 0

        elif initialize == False:
            pass

    def TUSP_MachineState(self, sState):

        self.TransportUnitStatePublisherObject.kobuki_general.transport_unit_state = sState.transport_state
        self.TransportUnitStatePublisherObject.kobuki_position.transport_in_dock = sState.transport_docked

    def TUSP_detected_obstacle_bumper(self, bObstacle):

        self.TransportUnitStatePublisherObject.kobuki_obstacle.detected_obstacle_bumper = bObstacle

    def TUSP_detected_obstacle_camera(self, bObstacle):

        self.TransportUnitStatePublisherObject.kobuki_obstacle.detected_obstacle_camera = bObstacle

    def TUSP_actual_time(self):

        self.Time = datetime.now()

        self.TransportUnitStatePublisherObject.odroid_date.year = self.Time.year
        self.TransportUnitStatePublisherObject.odroid_date.month = self.Time.month
        self.TransportUnitStatePublisherObject.odroid_date.day = self.Time.day
        self.TransportUnitStatePublisherObject.odroid_date.hour = self.Time.hour
        self.TransportUnitStatePublisherObject.odroid_date.minute = self.Time.minute
        self.TransportUnitStatePublisherObject.odroid_date.seconds = self.Time.second

    def TUSP_odom(self):

        self.OdomObject = OdomTopicReader()
        self.TransportUnitStatePublisherObject.kobuki_position.odom_x = self.OdomObject._distancia_x
        self.TransportUnitStatePublisherObject.kobuki_position.odom_y = self.OdomObject._distancia_y

        self._sub = rospy.Subscriber('/mobile_base/sensors/imu_data', Imu, self.imu_callback)
        rospy.sleep(0.3)

    def imu_callback(self, ImuData):
        quat = ImuData.orientation
        q = [quat.x, quat.y, quat.z, quat.w]

        _, _, yaw = euler_from_quaternion(q)
        self.currentOrientation = degrees(yaw)

        if (self.currentOrientation < 0):
            self.currentOrientation += 360.00

        self.TransportUnitStatePublisherObject.kobuki_position.rotation=self.currentOrientation

    def TSUP_battery(self):

        self.batteryobject = SensorState()
        rospy.Subscriber('/mobile_base/sensors/core', SensorState, self.SensorCallback)
        rospy.sleep(0.5)

    def SensorCallback(self, fBattery):
        self.battery_level = fBattery.battery / 10.0

        self.TransportUnitStatePublisherObject.kobuki_general.battery = self.battery_level

    def TSUP_Publisher(self):

        self.TSUPpublisher = rospy.Publisher('/flexmansys/state/leonardo', TransportUnitState, queue_size=1)
        self.TSUPpublisher.publish(self.TransportUnitStatePublisherObject)
        rospy.sleep(1)

def TSUP_Shutdown():

    rospy.loginfo("**************** " + "TSUP LEONARDO DETENIDO "  " ****************")

if __name__ == '__main__':

    TransportStateObject = TransportUnitStatePublisher(True)

    rospy.init_node('flexmansys_transport_node_leonardo_state')
    rospy.Rate(3)

    rospy.on_shutdown(TSUP_Shutdown)

    while not rospy.is_shutdown():

        TransportStateObject.TSUP_Publisher()
        TransportStateObject.TSUP_battery()
        TransportStateObject.TUSP_odom()
        TransportStateObject.TUSP_actual_time()

        rospy.Subscriber('/flexmansys/private/state/leonardo',
                         TransportPrivateState, TransportStateObject.TUSP_MachineState)
        rospy.sleep(0.2)










