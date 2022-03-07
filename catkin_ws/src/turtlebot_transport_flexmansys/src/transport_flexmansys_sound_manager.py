#! /usr/bin/env python

import rospy

from kobuki_msgs.msg import Sound


'''''

Este script tiene como objetivo el crear una clase que publique al topico encargado
de gestionar los sonidos efectuados por la base del Kobuki.

'''''


class SoundManager(object):

    def __init__(self, sTransportState):

        # Al igual que con la clase LEDManager, se debe de introducir el estado en el que se encuentra
        # el AGV, de modo que en funcion del estado en el que se encuentre, hara sonar o no el buzzer
        # del Kobuki de una manera u otra.

        self.SoundState = str(sTransportState)

        self.SoundPublisher = rospy.Publisher('/mobile_base/commands/sound', Sound, queue_size=1)

        self.SoundType = Sound()

        self.ctrl_c = False

        self.SoundPublisherValue()

    def SoundPublisherValue(self):

        # En esencia, ademas de los sonidos que hace por defecto el Kobuki al cargar la bateria
        # o arrancar el paquete turtlebot_bringup, el Kobuki solo sonara en tres ocasiones:
        # En modo Idle, una unica vez, para indicar que el transporte ya disponible para recibir
        # coordenadas por parte de los agentes; en modo Error, lo que hara que suene 10 veces
        # para indicar a la persona operaria que una unidad de transporte esta en modo de error; o
        # cuando detecta un obstaculo mediante el bumper.

        if self.SoundState == "Error":
            self.TRansportErrorSound = self.SoundType.value = 1
            self.SoundTopicPublisherError()

        if self.SoundState == "Idle":
            self.TRansportErrorSound = self.SoundType.value = 1
            self.SoundTopicPublisher()

        if self.SoundState == "Obstacle":
            self.TRansportErrorSound = self.SoundType.value = 6
            self.SoundTopicPublisher()

    def SoundTopicPublisher(self):

        while not self.ctrl_c:

            # Si se va a publicar una unica vez en un topico, es importante obtener el numero de
            # conexiones exitosas que se han dado para asegurarnos de que la informacion a
            # publicar se ha efecturado correctamente.

            self.connections_sound= self.SoundPublisher.get_num_connections()
            rospy.sleep(1.0)

            if self.connections_sound > 0:

                self.SoundPublisher.publish(self.TRansportErrorSound)
                rospy.sleep(0.5)
                break

            else:
                pass

    def SoundTopicPublisherError(self):

        while not self.ctrl_c:

            self.connections_sound= self.SoundPublisher.get_num_connections()
            rospy.sleep(1.0)

            if self.connections_sound > 0:

                # Si la unidad e transporte se encuentra en estado de error, haz sonar el buzzer
                # 10 veces.

                for i in range(10):
                    self.SoundPublisher.publish(self.TRansportErrorSound)
                    rospy.sleep(0.5)
                break

            else:
                pass
