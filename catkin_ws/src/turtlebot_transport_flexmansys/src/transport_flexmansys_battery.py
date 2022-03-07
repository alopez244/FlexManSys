#! /usr/bin/env python


import roslib; roslib.load_manifest(('kobuki_testsuite'))
import rospy

from kobuki_msgs.msg import SensorState

'''''

Este script tiene como objetivo obtener el nivel de bateria del kobuki

'''''

# Cuidado con ejecutar este script demasiado pronto, hay que darle un tiempo a los nodos
# generados por el launch de turtlebot_bringup minimal.launch para que asiente todos los
# parametros. Si se lanza la lectura de la bateria demasiado pronto, nos saltara una
# excepcion.

class BatteryLevel(object):

    def __init__(self):

        self._odomdata = SensorState()
        self._sub = rospy.Subscriber('/mobile_base/sensors/core', SensorState, self.SensorCallback)
        rospy.sleep(0.5)

    def SensorCallback(self, fBattery):

        # El valor de la bateria se entrega en VDC en formato Float. El maximo esta en 16.4 VDC, el +
        # minimo en 12.5 VDC. Si da un valor menor a 12.5 VDC, o bien la lectura se esta haciendo mal
        # o bien la bateria esta tan degenerada que necesita un cambio.

        # No se trunca ni se redondea el valor de la bateria.

        self.battery_level = fBattery.battery/10.0

if __name__ == "__main__":
    rospy.init_node('transport_flexmansys_battery_checker', log_level=rospy.INFO)
    BatteryLevel()
    rate = rospy.Rate(1)
    rospy.spin()


