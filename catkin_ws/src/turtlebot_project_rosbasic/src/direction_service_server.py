import rospy
from std_srvs.srv import Trigger, TriggerResponse
from laser_sub import LaserTopicReader
import time


class CrashDirectionService(object):
    def __init__(self, srv_name='/crash_direction_service'):
        self._srv_name = srv_name
        self._laser_reader_object = LaserTopicReader()
        self.detection_dict = {"front":0.0, "left":0.0, "right":0.0}
        self._my_service = rospy.Service(self._srv_name, Trigger , self.srv_callback)

    def srv_callback(self, request):
        self.detection_dict = self._laser_reader_object.crash_detector()
        message = self.direction_to_move()
        rospy.logdebug("DIRECTION ==> "+message)
        response = TriggerResponse()
        """
        ---
        bool success
        # indicate if crashed
        string message # Direction
        """
        response.success = self.potential_crash()
        response.message = message
        return response

    def potential_crash(self):
        if self.detection_dict["front"] < 0.8:
            return True
        else:
            return False

    def direction_to_move(self):
        if self.detection_dict["right"] > self.detection_dict["left"]:
            message = "right"
        elif self.detection_dict["right"] < self.detection_dict["left"]:
            message = "left"

        return message