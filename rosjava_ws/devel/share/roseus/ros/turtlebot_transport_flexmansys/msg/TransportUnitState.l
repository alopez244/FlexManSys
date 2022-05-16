;; Auto-generated. Do not edit!


(when (boundp 'turtlebot_transport_flexmansys::TransportUnitState)
  (if (not (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
    (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS"))
  (shadow 'TransportUnitState (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS")))
(unless (find-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTUNITSTATE")
  (make-package "TURTLEBOT_TRANSPORT_FLEXMANSYS::TRANSPORTUNITSTATE"))

(in-package "ROS")
;;//! \htmlinclude TransportUnitState.msg.html


(defclass turtlebot_transport_flexmansys::TransportUnitState
  :super ros::object
  :slots (_kobuki_general _kobuki_obstacle _kobuki_position _odroid_date ))

(defmethod turtlebot_transport_flexmansys::TransportUnitState
  (:init
   (&key
    ((:kobuki_general __kobuki_general) (instance turtlebot_transport_flexmansys::KobukiGeneral :init))
    ((:kobuki_obstacle __kobuki_obstacle) (instance turtlebot_transport_flexmansys::KobukiObstacle :init))
    ((:kobuki_position __kobuki_position) (instance turtlebot_transport_flexmansys::KobukiPosition :init))
    ((:odroid_date __odroid_date) (instance turtlebot_transport_flexmansys::TimeDate :init))
    )
   (send-super :init)
   (setq _kobuki_general __kobuki_general)
   (setq _kobuki_obstacle __kobuki_obstacle)
   (setq _kobuki_position __kobuki_position)
   (setq _odroid_date __odroid_date)
   self)
  (:kobuki_general
   (&rest __kobuki_general)
   (if (keywordp (car __kobuki_general))
       (send* _kobuki_general __kobuki_general)
     (progn
       (if __kobuki_general (setq _kobuki_general (car __kobuki_general)))
       _kobuki_general)))
  (:kobuki_obstacle
   (&rest __kobuki_obstacle)
   (if (keywordp (car __kobuki_obstacle))
       (send* _kobuki_obstacle __kobuki_obstacle)
     (progn
       (if __kobuki_obstacle (setq _kobuki_obstacle (car __kobuki_obstacle)))
       _kobuki_obstacle)))
  (:kobuki_position
   (&rest __kobuki_position)
   (if (keywordp (car __kobuki_position))
       (send* _kobuki_position __kobuki_position)
     (progn
       (if __kobuki_position (setq _kobuki_position (car __kobuki_position)))
       _kobuki_position)))
  (:odroid_date
   (&rest __odroid_date)
   (if (keywordp (car __odroid_date))
       (send* _odroid_date __odroid_date)
     (progn
       (if __odroid_date (setq _odroid_date (car __odroid_date)))
       _odroid_date)))
  (:serialization-length
   ()
   (+
    ;; turtlebot_transport_flexmansys/KobukiGeneral _kobuki_general
    (send _kobuki_general :serialization-length)
    ;; turtlebot_transport_flexmansys/KobukiObstacle _kobuki_obstacle
    (send _kobuki_obstacle :serialization-length)
    ;; turtlebot_transport_flexmansys/KobukiPosition _kobuki_position
    (send _kobuki_position :serialization-length)
    ;; turtlebot_transport_flexmansys/TimeDate _odroid_date
    (send _odroid_date :serialization-length)
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; turtlebot_transport_flexmansys/KobukiGeneral _kobuki_general
       (send _kobuki_general :serialize s)
     ;; turtlebot_transport_flexmansys/KobukiObstacle _kobuki_obstacle
       (send _kobuki_obstacle :serialize s)
     ;; turtlebot_transport_flexmansys/KobukiPosition _kobuki_position
       (send _kobuki_position :serialize s)
     ;; turtlebot_transport_flexmansys/TimeDate _odroid_date
       (send _odroid_date :serialize s)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; turtlebot_transport_flexmansys/KobukiGeneral _kobuki_general
     (send _kobuki_general :deserialize buf ptr-) (incf ptr- (send _kobuki_general :serialization-length))
   ;; turtlebot_transport_flexmansys/KobukiObstacle _kobuki_obstacle
     (send _kobuki_obstacle :deserialize buf ptr-) (incf ptr- (send _kobuki_obstacle :serialization-length))
   ;; turtlebot_transport_flexmansys/KobukiPosition _kobuki_position
     (send _kobuki_position :deserialize buf ptr-) (incf ptr- (send _kobuki_position :serialization-length))
   ;; turtlebot_transport_flexmansys/TimeDate _odroid_date
     (send _odroid_date :deserialize buf ptr-) (incf ptr- (send _odroid_date :serialization-length))
   ;;
   self)
  )

(setf (get turtlebot_transport_flexmansys::TransportUnitState :md5sum-) "4a9f169d9b464fbeb767cabba27149f7")
(setf (get turtlebot_transport_flexmansys::TransportUnitState :datatype-) "turtlebot_transport_flexmansys/TransportUnitState")
(setf (get turtlebot_transport_flexmansys::TransportUnitState :definition-)
      "KobukiGeneral kobuki_general
KobukiObstacle kobuki_obstacle
KobukiPosition kobuki_position
TimeDate odroid_date


================================================================================
MSG: turtlebot_transport_flexmansys/KobukiGeneral
string transport_unit_name
string transport_unit_state
float32 battery
================================================================================
MSG: turtlebot_transport_flexmansys/KobukiObstacle
bool detected_obstacle_bumper
bool detected_obstacle_camera
================================================================================
MSG: turtlebot_transport_flexmansys/KobukiPosition
bool transport_in_dock
string recovery_point
float64 odom_x
float64 odom_y
float64 rotation
================================================================================
MSG: turtlebot_transport_flexmansys/TimeDate
int32 year
int32 month
int32 day
int32 hour
int32 minute
int32 seconds


")



(provide :turtlebot_transport_flexmansys/TransportUnitState "4a9f169d9b464fbeb767cabba27149f7")


