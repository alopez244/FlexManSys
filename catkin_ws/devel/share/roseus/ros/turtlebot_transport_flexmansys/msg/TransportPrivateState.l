;; Auto-generated. Do not edit!


(when (boundp 'turtlebot_transport_flexmansys::TransportPrivateState)
  (if (not (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
    (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
  (shadow 'TransportPrivateState (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS")))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTPRIVATESTATE")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTPRIVATESTATE"))

(in-package "ROS")
;;//! \htmlinclude TransportPrivateState.msg.html


(defclass turtlebot_transport_flexmansys::TransportPrivateState
  :super ros::object
  :slots (_transport_state _transport_docked ))

(defmethod turtlebot_transport_flexmansys::TransportPrivateState
  (:init
   (&key
    ((:transport_state __transport_state) "")
    ((:transport_docked __transport_docked) nil)
    )
   (send-super :init)
   (setq _transport_state (string __transport_state))
   (setq _transport_docked __transport_docked)
   self)
  (:transport_state
   (&optional __transport_state)
   (if __transport_state (setq _transport_state __transport_state)) _transport_state)
  (:transport_docked
   (&optional __transport_docked)
   (if __transport_docked (setq _transport_docked __transport_docked)) _transport_docked)
  (:serialization-length
   ()
   (+
    ;; string _transport_state
    4 (length _transport_state)
    ;; bool _transport_docked
    1
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; string _transport_state
       (write-long (length _transport_state) s) (princ _transport_state s)
     ;; bool _transport_docked
       (if _transport_docked (write-byte -1 s) (write-byte 0 s))
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; string _transport_state
     (let (n) (setq n (sys::peek buf ptr- :integer)) (incf ptr- 4) (setq _transport_state (subseq buf ptr- (+ ptr- n))) (incf ptr- n))
   ;; bool _transport_docked
     (setq _transport_docked (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;;
   self)
  )

(setf (get turtlebot_transport_flexmansys::TransportPrivateState :md5sum-) "670f3bb83518dec928ea7b995f722845")
(setf (get turtlebot_transport_flexmansys::TransportPrivateState :datatype-) "turtlebot_transport_flexmansys/TransportPrivateState")
(setf (get turtlebot_transport_flexmansys::TransportPrivateState :definition-)
      "string transport_state
bool transport_docked

")



(provide :turtlebot_transport_flexmansys/TransportPrivateState "670f3bb83518dec928ea7b995f722845")


