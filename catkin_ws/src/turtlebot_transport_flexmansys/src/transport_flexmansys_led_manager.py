#! /usr/bin/env python

import rospy

from kobuki_msgs.msg import Led


'''''

Este script tiene como objetivo el crear una clase que publique a los nodos encargados de
gestionar el alumbrado y apagado de los LED 1 y LED 2 del Turtlebot2. El LED0 se queda para
uso exclusivo del paquete turtlebot_bringup

'''''


class LEDManager(object):

    def __init__(self, sLEDValue):

        # EL argumento sLEDValue sirve como argumento de entrada tipo String para pasarle a la
        # clase LEDManager el estado en el que se encuetra la unidad de transporte, y sepa como
        # debe de iluminar los LEDs.

        self.LEDValue = str(sLEDValue)

        self.LEDPublisher1 = rospy.Publisher('/mobile_base/commands/led1', Led, queue_size=1)
        self.LEDPublisher2 = rospy.Publisher('/mobile_base/commands/led2', Led, queue_size=1)

        self.LEDValueType = Led()

        self.ctrl_c = False

        self.LEDValuePublisher()

    def LEDValuePublisher(self):

        # Color LED ( 0 = off 1 = green 2 = orange 3 = red )

        # Cada uno de los estados de la maquina de stados cuenta con su prpia combinacion de colores
        # de LEDs. En fucion de la inicializacion de la clase y el argumento de entrada introducido
        # la clase comprobara el como tiene que iluminar ambos LEDs.

        if self.LEDValue == "Idle":

            self.LEDValueType.value = 2
            self.LED1 = self.LEDValueType.value

            self.LEDValueType.value = 0
            self.LED2 = self.LEDValueType.value

            self.LEDValuePublisherLED1()
            self.LEDValuePublisherLED2()

        if self.LEDValue == "Localization":

            self.LEDValueType.value = 0
            self.LED1 = self.LEDValueType.value

            self.LEDValueType.value = 2
            self.LED2 = self.LEDValueType.value

            self.LEDValuePublisherLED1()
            self.LEDValuePublisherLED2()

        if self.LEDValue == "Active":

            self.LEDValueType.value = 1
            self.LED1 = self.LEDValueType.value

            self.LEDValueType.value = 0
            self.LED2 = self.LEDValueType.value

            self.LEDValuePublisherLED1()
            self.LEDValuePublisherLED2()

        if self.LEDValue == "Operative":

            self.LEDValueType.value = 1
            self.LED1 = self.LEDValueType.value

            self.LEDValueType.value = 1
            self.LED2 = self.LEDValueType.value

            self.LEDValuePublisherLED1()
            self.LEDValuePublisherLED2()

        if self.LEDValue == "Error":

            self.LEDValueType.value = 3
            self.LED1 = self.LEDValueType.value

            self.LEDValueType.value = 0
            self.LED2 = self.LEDValueType.value

            self.LEDValuePublisherLED1()
            self.LEDValuePublisherLED2()

        if self.LEDValue == "Stop":

            self.LEDValueType.value = 0
            self.LED1 = self.LEDValueType.value

            self.LEDValueType.value = 0
            self.LED2 = self.LEDValueType.value

            self.LEDValuePublisherLED1()
            self.LEDValuePublisherLED2()


    def LEDValuePublisherLED1(self):

        while not self.ctrl_c:

            # Si se va a publicar una unica vez en un topico, es importante obtener el numero de
            # conexiones exitosas que se han dado para asegurarnos de que la informacion a
            # publicar se ha efecturado correctamente.

            self.connections_led1 = self.LEDPublisher1.get_num_connections()
            rospy.sleep(0.1)

            if self.connections_led1 > 0:
                self.LEDPublisher1.publish(self.LED1)
                rospy.sleep(0.1)
                break

            else:
                pass

    def LEDValuePublisherLED2(self):

        while not self.ctrl_c:

            self.connections_led2 = self.LEDPublisher1.get_num_connections()
            rospy.sleep(0.1)

            if self.connections_led2 > 0:
                self.LEDPublisher2.publish(self.LED2)
                rospy.sleep(0.1)
                break

            else:
                pass
