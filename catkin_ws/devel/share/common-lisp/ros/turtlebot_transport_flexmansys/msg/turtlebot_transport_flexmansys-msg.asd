
(cl:in-package :asdf)

(defsystem "turtlebot_transport_flexmansys-msg"
  :depends-on (:roslisp-msg-protocol :roslisp-utils )
  :components ((:file "_package")
    (:file "KobukiGeneral" :depends-on ("_package_KobukiGeneral"))
    (:file "_package_KobukiGeneral" :depends-on ("_package"))
    (:file "KobukiObstacle" :depends-on ("_package_KobukiObstacle"))
    (:file "_package_KobukiObstacle" :depends-on ("_package"))
    (:file "KobukiPosition" :depends-on ("_package_KobukiPosition"))
    (:file "_package_KobukiPosition" :depends-on ("_package"))
    (:file "TimeDate" :depends-on ("_package_TimeDate"))
    (:file "_package_TimeDate" :depends-on ("_package"))
    (:file "TransportPrivateState" :depends-on ("_package_TransportPrivateState"))
    (:file "_package_TransportPrivateState" :depends-on ("_package"))
    (:file "TransportUnitState" :depends-on ("_package_TransportUnitState"))
    (:file "_package_TransportUnitState" :depends-on ("_package"))
  ))