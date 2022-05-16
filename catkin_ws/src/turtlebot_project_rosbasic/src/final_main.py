#!/usr/bin/env python
import actionlib
import rospy
from final_publicista_velocidad import CmdVelPub
from turtlebot_project_rosbasic.srv import CrashDirection, CrashDirectionRequest
from turtlebot_project_rosbasic.msg import OdometryActionMsgAction, OdometryActionMsgGoal, OdometryActionMsgFeedback, OdometryActionMsgResult
# from std_srvs.srv import Trigger, TriggerRequest

class ControlTurtlebot(object):

    def __init__(self):
        self._kobuki_move = CmdVelPub()
        self.exit_maze()

    def obtain_direction(self):

        # Inicializamos un cliente de servicio
        rospy.wait_for_service('/scan_service')
        obtain_direction_service_client = rospy.ServiceProxy('/scan_service', CrashDirection)
        obtain_direction_request_object = CrashDirectionRequest()
        result = obtain_direction_service_client(obtain_direction_request_object)

        self._direction_command = result.data
        self._kobuki_crashed = result.success

        pass

    def init_action_client(self):
        self._action_client = actionlib.SimpleActionClient('/action_server_odom', OdometryActionMsgAction)
        # Esperamos hasta que se ha inicializado el servidor de la accion
        rospy.loginfo('Esperando al servidor de la accion')
        self._action_client.wait_for_server()
        rospy.loginfo('Servidor de accion encontrado')
        self._goal_accion = OdometryActionMsgGoal()
        self._goal_accion.goal = 'accion'

    def send_goal_action(self):
        self._action_client.send_goal(self._goal_accion, feedback_cb=self.adquire_feedback_action)

    def adquire_feedback_action(self, feedback):
        rospy.loginfo("Feedback obtenido: "+str(feedback))

    def exit_maze(self):
        rospy.init_node('kobuki_salir_maze', anonymous=True)
        self._rate = rospy.Rate(0.2)  # Rate de publicacion de 0.5 Hz, 2 s
        self.init_action_client()

        try:

            while not rospy.is_shutdown():  # Creamos un loop

                #kobuki_scan = LaserTopicReader()
                #comando_velocidad = kobuki_scan.crash_detector
                self.obtain_direction()
                self._kobuki_move.move_robot(self._direction_command)
                self._rate.sleep()  # Esperamos a que se cumpla 2s para seguir con el loop.
                self.send_goal_action()

        except rospy.ROSInterruptException:
            rospy.loginfo('Ejecucion finalizada, Kobuki resting')
            pass

if __name__ == '__main__':

    salir_del_laberinto = ControlTurtlebot()





