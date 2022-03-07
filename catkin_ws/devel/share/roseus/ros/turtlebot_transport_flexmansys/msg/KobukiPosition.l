;; Auto-generated. Do not edit!


(when (boundp 'turtlebot_transport_flexmansys::KobukiPosition)
  (if (not (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
    (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
  (shadow 'KobukiPosition (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS")))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::KOBUKIPOSITION")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::KOBUKIPOSITION"))

(in-package "ROS")
;;//! \htmlinclude KobukiPosition.msg.html


(defclass turtlebot_transport_flexmansys::KobukiPosition
  :super ros::object
  :slots (_transport_in_dock _recovery_point _odom_x _odom_y _rotation ))

(defmethod turtlebot_transport_flexmansys::KobukiPosition
  (:init
   (&key
    ((:transport_in_dock __transport_in_dock) nil)
    ((:recovery_point __recovery_point) "")
    ((:odom_x __odom_x) 0.0)
    ((:odom_y __odom_y) 0.0)
    ((:rotation __rotation) 0.0)
    )
   (send-super :init)
   (setq _transport_in_dock __transport_in_dock)
   (setq _recovery_point (string __recovery_point))
   (setq _odom_x (float __odom_x))
   (setq _odom_y (float __odom_y))
   (setq _rotation (float __rotation))
   self)
  (:transport_in_dock
   (&optional __transport_in_dock)
   (if __transport_in_dock (setq _transport_in_dock __transport_in_dock)) _transport_in_dock)
  (:recovery_point
   (&optional __recovery_point)
   (if __recovery_point (setq _recovery_point __recovery_point)) _recovery_point)
  (:odom_x
   (&optional __odom_x)
   (if __odom_x (setq _odom_x __odom_x)) _odom_x)
  (:odom_y
   (&optional __odom_y)
   (if __odom_y (setq _odom_y __odom_y)) _odom_y)
  (:rotation
   (&optional __rotation)
   (if __rotation (setq _rotation __rotation)) _rotation)
  (:serialization-length
   ()
   (+
    ;; bool _transport_in_dock
    1
    ;; string _recovery_point
    4 (length _recovery_point)
    ;; float64 _odom_x
    8
    ;; float64 _odom_y
    8
    ;; float64 _rotation
    8
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; bool _transport_in_dock
       (if _transport_in_dock (write-byte -1 s) (write-byte 0 s))
     ;; string _recovery_point
       (write-long (length _recovery_point) s) (princ _recovery_point s)
     ;; float64 _odom_x
       (sys::poke _odom_x (send s :buffer) (send s :count) :double) (incf (stream-count s) 8)
     ;; float64 _odom_y
       (sys::poke _odom_y (send s :buffer) (send s :count) :double) (incf (stream-count s) 8)
     ;; float64 _rotation
       (sys::poke _rotation (send s :buffer) (send s :count) :double) (incf (stream-count s) 8)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; bool _transport_in_dock
     (setq _transport_in_dock (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;; string _recovery_point
     (let (n) (setq n (sys::peek buf ptr- :integer)) (incf ptr- 4) (setq _recovery_point (subseq buf ptr- (+ ptr- n))) (incf ptr- n))
   ;; float64 _odom_x
     (setq _odom_x (sys::peek buf ptr- :double)) (incf ptr- 8)
   ;; float64 _odom_y
     (setq _odom_y (sys::peek buf ptr- :double)) (incf ptr- 8)
   ;; float64 _rotation
     (setq _rotation (sys::peek buf ptr- :double)) (incf ptr- 8)
   ;;
   self)
  )

(setf (get turtlebot_transport_flexmansys::KobukiPosition :md5sum-) "fa959bfdcde117615672bd484119c1ee")
(setf (get turtlebot_transport_flexmansys::KobukiPosition :datatype-) "turtlebot_transport_flexmansys/KobukiPosition")
(setf (get turtlebot_transport_flexmansys::KobukiPosition :definition-)
      "bool transport_in_dock
string recovery_point
float64 odom_x
float64 odom_y
float64 rotation
")



(provide :turtlebot_transport_flexmansys/KobukiPosition "fa959bfdcde117615672bd484119c1ee")


