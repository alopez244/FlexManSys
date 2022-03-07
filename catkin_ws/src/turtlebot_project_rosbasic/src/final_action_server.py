#!/usr/bin/env python

import rospy
import actionlib
from turtlebot_project_rosbasic.msg import OdometryActionMsgAction, OdometryActionMsgGoal, OdometryActionMsgFeedback, OdometryActionMsgResult
from odom_topic_subscriber import OdomTopicReader
from std_msgs.msg import Empty


class OdomServer(object):

    _feedback = OdometryActionMsgFeedback()
    _result = OdometryActionMsgResult()

    def __init__(self):
        self.init_action_server()
        self.OdomData = OdomTopicReader()

    def init_action_server(self):
        self._as = actionlib.SimpleActionServer("/action_server_odom", OdometryActionMsgAction,self.goal_callback, False)
        # Inicializa el servidor de la accion definido como self._as
        self._as.start()

    def goal_callback(self,goal):

        success = True
        r = rospy.Rate(1)

        forward_or_backward = goal.goal

        r.sleep()

        i = 0
        for i in xrange(0, 1):

            self.OdomData = OdomTopicReader()

            if self._as.is_preempt_requested():
                rospy.loginfo('The goal has been cancelled/preempted')
                # the following line, sets the client in preempted state (goal cancelled)
                self._as.set_preempted()
                success = False
                break

            if self.OdomData._distancia_x > 2.25 and self.OdomData._distancia_y < -0.5:
                self._feedback.feedback = 'You did exit the maze!'
            elif self.OdomData._distancia_x < 2.25 and self.OdomData._distancia_y > -0.5:
                self._feedback.feedback = 'You did not exit the maze'
            else:
                self._feedback.feedback = 'You did not exit the maze'

            print self.OdomData._distancia_x


            # this is a probe
            # rospy.loginfo(str(self.OdomData._distancia_x))

            r.sleep()

        if success:
            self._result = Empty()
            self._as.set_succeeded(self._result)


if __name__ == '__main__':
    rospy.init_node('servidor_accion_odometria')
    OdomServer()
    rospy.spin()
