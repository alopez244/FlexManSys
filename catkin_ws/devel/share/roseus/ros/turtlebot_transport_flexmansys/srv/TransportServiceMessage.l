;; Auto-generated. Do not edit!


(when (boundp 'turtlebot_transport_flexmansys::TransportServiceMessage)
  (if (not (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
    (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
  (shadow 'TransportServiceMessage (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS")))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTSERVICEMESSAGE")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTSERVICEMESSAGE"))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTSERVICEMESSAGEREQUEST")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTSERVICEMESSAGEREQUEST"))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTSERVICEMESSAGERESPONSE")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTSERVICEMESSAGERESPONSE"))

(in-package "ROS")





(defclass turtlebot_transport_flexmansys::TransportServiceMessageRequest
  :super ros::object
  :slots (_coordinate ))

(defmethod turtlebot_transport_flexmansys::TransportServiceMessageRequest
  (:init
   (&key
    ((:coordinate __coordinate) "")
    )
   (send-super :init)
   (setq _coordinate (string __coordinate))
   self)
  (:coordinate
   (&optional __coordinate)
   (if __coordinate (setq _coordinate __coordinate)) _coordinate)
  (:serialization-length
   ()
   (+
    ;; string _coordinate
    4 (length _coordinate)
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; string _coordinate
       (write-long (length _coordinate) s) (princ _coordinate s)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; string _coordinate
     (let (n) (setq n (sys::peek buf ptr- :integer)) (incf ptr- 4) (setq _coordinate (subseq buf ptr- (+ ptr- n))) (incf ptr- n))
   ;;
   self)
  )

(defclass turtlebot_transport_flexmansys::TransportServiceMessageResponse
  :super ros::object
  :slots (_success ))

(defmethod turtlebot_transport_flexmansys::TransportServiceMessageResponse
  (:init
   (&key
    ((:success __success) nil)
    )
   (send-super :init)
   (setq _success __success)
   self)
  (:success
   (&optional __success)
   (if __success (setq _success __success)) _success)
  (:serialization-length
   ()
   (+
    ;; bool _success
    1
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; bool _success
       (if _success (write-byte -1 s) (write-byte 0 s))
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; bool _success
     (setq _success (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;;
   self)
  )

(defclass turtlebot_transport_flexmansys::TransportServiceMessage
  :super ros::object
  :slots ())

(setf (get turtlebot_transport_flexmansys::TransportServiceMessage :md5sum-) "8ddb36dd3d52323557949d25fae2e281")
(setf (get turtlebot_transport_flexmansys::TransportServiceMessage :datatype-) "turtlebot_transport_flexmansys/TransportServiceMessage")
(setf (get turtlebot_transport_flexmansys::TransportServiceMessage :request) turtlebot_transport_flexmansys::TransportServiceMessageRequest)
(setf (get turtlebot_transport_flexmansys::TransportServiceMessage :response) turtlebot_transport_flexmansys::TransportServiceMessageResponse)

(defmethod turtlebot_transport_flexmansys::TransportServiceMessageRequest
  (:response () (instance turtlebot_transport_flexmansys::TransportServiceMessageResponse :init)))

(setf (get turtlebot_transport_flexmansys::TransportServiceMessageRequest :md5sum-) "8ddb36dd3d52323557949d25fae2e281")
(setf (get turtlebot_transport_flexmansys::TransportServiceMessageRequest :datatype-) "turtlebot_transport_flexmansys/TransportServiceMessageRequest")
(setf (get turtlebot_transport_flexmansys::TransportServiceMessageRequest :definition-)
      "string coordinate
---
bool success


")

(setf (get turtlebot_transport_flexmansys::TransportServiceMessageResponse :md5sum-) "8ddb36dd3d52323557949d25fae2e281")
(setf (get turtlebot_transport_flexmansys::TransportServiceMessageResponse :datatype-) "turtlebot_transport_flexmansys/TransportServiceMessageResponse")
(setf (get turtlebot_transport_flexmansys::TransportServiceMessageResponse :definition-)
      "string coordinate
---
bool success


")



(provide :turtlebot_transport_flexmansys/TransportServiceMessage "8ddb36dd3d52323557949d25fae2e281")


