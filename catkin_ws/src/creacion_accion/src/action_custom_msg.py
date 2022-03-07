#! /usr/bin/env python

import rospy
import time
# Esta es la libreria correspondiente a las acciones de ROS, es decir, la que proporciona
# todos los recursos respectivos a las acciones
import actionlib

# Importamos nuestro tipo de datos de accion previamente creadas
from creacion_accion.msg import CustomActionMsgFeedback, CustomActionMsgResult, CustomActionMsgAction, CustomActionMsgGoal
from geometry_msgs.msg import Twist
from std_msgs.msg import Empty


class CustomActionMsgClass(object):
    # Se crean dos objetos que esten relacionados directamente con los parametros de feedback y
    # result de la accion respectiva

    _feedback = CustomActionMsgFeedback()
    _result = CustomActionMsgResult()

    def __init__(self):
        # Con SimpleActionServer se crea un servidor de la accion
        self._as = actionlib.SimpleActionServer("action_custom_msg_as", CustomActionMsgAction,
                                                self.goal_callback,False)
        # Inicializa el servidor de la accion definido como self._as
        self._as.start()

    def goal_callback(self, goal):
        # se ejecuta esta funcion cuando se le llama al servidor de la accion
        # esta funcion es la que define si el kobuki va a ir hacia delante o
        # hacia atras segun el goal que recoja
        # Posteriormente, devuelve la secuencia seguida al nodo que llamo
        # al servidor de la accion

        # helper variables
        success = True
        r = rospy.Rate(1)

        # Definimos los dos publicistas que tendremos en la accion servidor, uno para movernos
        # hacia delante y otro para movernos hacia atras

        # Publicista para movernos hacia delante
        self._pub_forward = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1)
        self._pub_forward_msg = Twist()
        self._pub_forward_msg.linear.x = 2.0
        self._pub_forward_msg.linear.y = 0.0
        self._pub_forward_msg.angular.z = 0.0

        # Publicista para movernos hacia atras
        self._pub_backward = rospy.Publisher('/mobile_base/commands/velocity', Twist, queue_size=1)
        self._pub_backward_msg = Twist()
        self._pub_backward_msg.linear.x = -2.0
        self._pub_backward_msg.linear.y = 0.0
        self._pub_backward_msg.angular.z = 0.0

        # Objeto para guardar si el objetivo (goal) de la accion es moverse hacia delante o
        # hacia atras
        forward_or_backward = goal.goal

        i = 0
        for i in xrange(0, 4):

            # Nos aseguramos que no se ha dado una cancelacion de la accion mediante el preempt
            # cancelation
            if self._as.is_preempt_requested():
                rospy.loginfo('The goal has been cancelled/preempted')
                # the following line, sets the client in preempted state (goal cancelled)
                self._as.set_preempted()
                success = False
                # finalizamos de hacer lo que estabamos ejecutando, en este caso, hacemos que
                # el kobuki se pare
                break

            # Logica que mueve el kobuki hacia delante o hacia atras segun lo que haya recibido el goal
            if forward_or_backward == 'FORWARD':
                self._pub_forward.publish(self._pub_forward_msg)
                self._feedback.feedback = 'Going FORWARD'
                self._as.publish_feedback(self._feedback)

            if forward_or_backward == 'BACKWARD':
                self._pub_backward.publish(self._pub_backward_msg)
                self._feedback.feedback = 'Going BACKWARD'
                self._as.publish_feedback(self._feedback)

            # con este comando se realiza la accion cada 1 Hz
            r.sleep()

        # at this point, either the goal has been achieved (success==true)
        # or the client preempted the goal (success==false)
        # If success, then we publish the final result
        # If not success, we do not publish anything in the result

        # Esta parte del codigo es importante, puesto que en funcion de si se ha cancelado
        # la llamada del servicio de la accion, publicaremos los resultados o no
        # Para saber si ha habido una cancelacion (preempt cancelation) lo comprobamos
        # mediante la variable succes que se comprueba en nada mas definir los publicistas

        # Sin embargo, dado que nuestra variable de resultado no esta definida en nuestro
        # .action, esto de aqui abajo realmente no hara nada, simplemente se queda como
        # esqueleto para futuras aplicaciones

        if success:
            self._result = Empty()
            self._as.set_succeeded(self._result)


if __name__ == '__main__':
    rospy.init_node('action_custom_msg')
    # No es necesario definir ningun objeto respectivo a la clase "accion" que acabamos
    # de crear, puesto que no hay ningun otro paquete que vaya importar ninguna clase
    CustomActionMsgClass()
    rospy.spin()