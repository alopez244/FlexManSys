;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::TransportUnitState)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'TransportUnitState (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::TRANSPORTUNITSTATE")
  (make-package "CUSTOM_MESSAGES::TRANSPORTUNITSTATE"))

(in-package "ROS")
;;//! \htmlinclude TransportUnitState.msg.html


(defclass custom_messages::TransportUnitState
  :super ros::object
  :slots (_kobuki_general _kobuki_obstacle _kobuki_position _odroid_date ))

(defmethod custom_messages::TransportUnitState
  (:init
   (&key
    ((:kobuki_general __kobuki_general) (instance custom_messages::KobukiGeneral :init))
    ((:kobuki_obstacle __kobuki_obstacle) (instance custom_messages::KobukiObstacle :init))
    ((:kobuki_position __kobuki_position) (instance custom_messages::KobukiPosition :init))
    ((:odroid_date __odroid_date) (instance custom_messages::TimeDate :init))
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
    ;; custom_messages/KobukiGeneral _kobuki_general
    (send _kobuki_general :serialization-length)
    ;; custom_messages/KobukiObstacle _kobuki_obstacle
    (send _kobuki_obstacle :serialization-length)
    ;; custom_messages/KobukiPosition _kobuki_position
    (send _kobuki_position :serialization-length)
    ;; custom_messages/TimeDate _odroid_date
    (send _odroid_date :serialization-length)
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; custom_messages/KobukiGeneral _kobuki_general
       (send _kobuki_general :serialize s)
     ;; custom_messages/KobukiObstacle _kobuki_obstacle
       (send _kobuki_obstacle :serialize s)
     ;; custom_messages/KobukiPosition _kobuki_position
       (send _kobuki_position :serialize s)
     ;; custom_messages/TimeDate _odroid_date
       (send _odroid_date :serialize s)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; custom_messages/KobukiGeneral _kobuki_general
     (send _kobuki_general :deserialize buf ptr-) (incf ptr- (send _kobuki_general :serialization-length))
   ;; custom_messages/KobukiObstacle _kobuki_obstacle
     (send _kobuki_obstacle :deserialize buf ptr-) (incf ptr- (send _kobuki_obstacle :serialization-length))
   ;; custom_messages/KobukiPosition _kobuki_position
     (send _kobuki_position :deserialize buf ptr-) (incf ptr- (send _kobuki_position :serialization-length))
   ;; custom_messages/TimeDate _odroid_date
     (send _odroid_date :deserialize buf ptr-) (incf ptr- (send _odroid_date :serialization-length))
   ;;
   self)
  )

(setf (get custom_messages::TransportUnitState :md5sum-) "4a9f169d9b464fbeb767cabba27149f7")
(setf (get custom_messages::TransportUnitState :datatype-) "custom_messages/TransportUnitState")
(setf (get custom_messages::TransportUnitState :definition-)
      "KobukiGeneral kobuki_general
KobukiObstacle kobuki_obstacle
KobukiPosition kobuki_position
TimeDate odroid_date


================================================================================
MSG: custom_messages/KobukiGeneral
string transport_unit_name
string transport_unit_state
float32 battery
================================================================================
MSG: custom_messages/KobukiObstacle
bool detected_obstacle_bumper
bool detected_obstacle_camera
================================================================================
MSG: custom_messages/KobukiPosition
bool transport_in_dock
string recovery_point
float64 odom_x
float64 odom_y
float64 rotation
================================================================================
MSG: custom_messages/TimeDate
int32 year
int32 month
int32 day
int32 hour
int32 minute
int32 seconds


")



(provide :custom_messages/TransportUnitState "4a9f169d9b464fbeb767cabba27149f7")


