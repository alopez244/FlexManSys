;; Auto-generated. Do not edit!


(when (boundp 'turtlebot_transport_flexmansys::Prueba)
  (if (not (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
    (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
  (shadow 'Prueba (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS")))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::PRUEBA")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::PRUEBA"))

(in-package "ROS")
;;//! \htmlinclude Prueba.msg.html


(defclass turtlebot_transport_flexmansys::Prueba
  :super ros::object
  :slots (_numero_prueba ))

(defmethod turtlebot_transport_flexmansys::Prueba
  (:init
   (&key
    ((:numero_prueba __numero_prueba) 0)
    )
   (send-super :init)
   (setq _numero_prueba (round __numero_prueba))
   self)
  (:numero_prueba
   (&optional __numero_prueba)
   (if __numero_prueba (setq _numero_prueba __numero_prueba)) _numero_prueba)
  (:serialization-length
   ()
   (+
    ;; int32 _numero_prueba
    4
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; int32 _numero_prueba
       (write-long _numero_prueba s)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; int32 _numero_prueba
     (setq _numero_prueba (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;;
   self)
  )

(setf (get turtlebot_transport_flexmansys::Prueba :md5sum-) "5d1d253d7f30569507dbcd46fe13058c")
(setf (get turtlebot_transport_flexmansys::Prueba :datatype-) "turtlebot_transport_flexmansys/Prueba")
(setf (get turtlebot_transport_flexmansys::Prueba :definition-)
      "int32 numero_prueba




")



(provide :turtlebot_transport_flexmansys/Prueba "5d1d253d7f30569507dbcd46fe13058c")


