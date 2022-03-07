
(cl:in-package :asdf)

(defsystem "turtlebot_transport_flexmansys-srv"
  :depends-on (:roslisp-msg-protocol :roslisp-utils )
  :components ((:file "_package")
    (:file "TransportServiceMessage" :depends-on ("_package_TransportServiceMessage"))
    (:file "_package_TransportServiceMessage" :depends-on ("_package"))
  ))