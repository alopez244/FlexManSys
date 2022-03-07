; Auto-generated. Do not edit!


(cl:in-package custom_messages-msg)


;//! \htmlinclude TransportUnitState.msg.html

(cl:defclass <TransportUnitState> (roslisp-msg-protocol:ros-message)
  ((kobuki_general
    :reader kobuki_general
    :initarg :kobuki_general
    :type custom_messages-msg:KobukiGeneral
    :initform (cl:make-instance 'custom_messages-msg:KobukiGeneral))
   (kobuki_obstacle
    :reader kobuki_obstacle
    :initarg :kobuki_obstacle
    :type custom_messages-msg:KobukiObstacle
    :initform (cl:make-instance 'custom_messages-msg:KobukiObstacle))
   (kobuki_position
    :reader kobuki_position
    :initarg :kobuki_position
    :type custom_messages-msg:KobukiPosition
    :initform (cl:make-instance 'custom_messages-msg:KobukiPosition))
   (odroid_date
    :reader odroid_date
    :initarg :odroid_date
    :type custom_messages-msg:TimeDate
    :initform (cl:make-instance 'custom_messages-msg:TimeDate)))
)

(cl:defclass TransportUnitState (<TransportUnitState>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <TransportUnitState>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'TransportUnitState)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name custom_messages-msg:<TransportUnitState> is deprecated: use custom_messages-msg:TransportUnitState instead.")))

(cl:ensure-generic-function 'kobuki_general-val :lambda-list '(m))
(cl:defmethod kobuki_general-val ((m <TransportUnitState>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:kobuki_general-val is deprecated.  Use custom_messages-msg:kobuki_general instead.")
  (kobuki_general m))

(cl:ensure-generic-function 'kobuki_obstacle-val :lambda-list '(m))
(cl:defmethod kobuki_obstacle-val ((m <TransportUnitState>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:kobuki_obstacle-val is deprecated.  Use custom_messages-msg:kobuki_obstacle instead.")
  (kobuki_obstacle m))

(cl:ensure-generic-function 'kobuki_position-val :lambda-list '(m))
(cl:defmethod kobuki_position-val ((m <TransportUnitState>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:kobuki_position-val is deprecated.  Use custom_messages-msg:kobuki_position instead.")
  (kobuki_position m))

(cl:ensure-generic-function 'odroid_date-val :lambda-list '(m))
(cl:defmethod odroid_date-val ((m <TransportUnitState>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:odroid_date-val is deprecated.  Use custom_messages-msg:odroid_date instead.")
  (odroid_date m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <TransportUnitState>) ostream)
  "Serializes a message object of type '<TransportUnitState>"
  (roslisp-msg-protocol:serialize (cl:slot-value msg 'kobuki_general) ostream)
  (roslisp-msg-protocol:serialize (cl:slot-value msg 'kobuki_obstacle) ostream)
  (roslisp-msg-protocol:serialize (cl:slot-value msg 'kobuki_position) ostream)
  (roslisp-msg-protocol:serialize (cl:slot-value msg 'odroid_date) ostream)
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <TransportUnitState>) istream)
  "Deserializes a message object of type '<TransportUnitState>"
  (roslisp-msg-protocol:deserialize (cl:slot-value msg 'kobuki_general) istream)
  (roslisp-msg-protocol:deserialize (cl:slot-value msg 'kobuki_obstacle) istream)
  (roslisp-msg-protocol:deserialize (cl:slot-value msg 'kobuki_position) istream)
  (roslisp-msg-protocol:deserialize (cl:slot-value msg 'odroid_date) istream)
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<TransportUnitState>)))
  "Returns string type for a message object of type '<TransportUnitState>"
  "custom_messages/TransportUnitState")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'TransportUnitState)))
  "Returns string type for a message object of type 'TransportUnitState"
  "custom_messages/TransportUnitState")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<TransportUnitState>)))
  "Returns md5sum for a message object of type '<TransportUnitState>"
  "4a9f169d9b464fbeb767cabba27149f7")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'TransportUnitState)))
  "Returns md5sum for a message object of type 'TransportUnitState"
  "4a9f169d9b464fbeb767cabba27149f7")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<TransportUnitState>)))
  "Returns full string definition for message of type '<TransportUnitState>"
  (cl:format cl:nil "KobukiGeneral kobuki_general~%KobukiObstacle kobuki_obstacle~%KobukiPosition kobuki_position~%TimeDate odroid_date~%~%~%================================================================================~%MSG: custom_messages/KobukiGeneral~%string transport_unit_name~%string transport_unit_state~%float32 battery~%================================================================================~%MSG: custom_messages/KobukiObstacle~%bool detected_obstacle_bumper~%bool detected_obstacle_camera~%================================================================================~%MSG: custom_messages/KobukiPosition~%bool transport_in_dock~%string recovery_point~%float64 odom_x~%float64 odom_y~%float64 rotation~%================================================================================~%MSG: custom_messages/TimeDate~%int32 year~%int32 month~%int32 day~%int32 hour~%int32 minute~%int32 seconds~%~%~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'TransportUnitState)))
  "Returns full string definition for message of type 'TransportUnitState"
  (cl:format cl:nil "KobukiGeneral kobuki_general~%KobukiObstacle kobuki_obstacle~%KobukiPosition kobuki_position~%TimeDate odroid_date~%~%~%================================================================================~%MSG: custom_messages/KobukiGeneral~%string transport_unit_name~%string transport_unit_state~%float32 battery~%================================================================================~%MSG: custom_messages/KobukiObstacle~%bool detected_obstacle_bumper~%bool detected_obstacle_camera~%================================================================================~%MSG: custom_messages/KobukiPosition~%bool transport_in_dock~%string recovery_point~%float64 odom_x~%float64 odom_y~%float64 rotation~%================================================================================~%MSG: custom_messages/TimeDate~%int32 year~%int32 month~%int32 day~%int32 hour~%int32 minute~%int32 seconds~%~%~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <TransportUnitState>))
  (cl:+ 0
     (roslisp-msg-protocol:serialization-length (cl:slot-value msg 'kobuki_general))
     (roslisp-msg-protocol:serialization-length (cl:slot-value msg 'kobuki_obstacle))
     (roslisp-msg-protocol:serialization-length (cl:slot-value msg 'kobuki_position))
     (roslisp-msg-protocol:serialization-length (cl:slot-value msg 'odroid_date))
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <TransportUnitState>))
  "Converts a ROS message object to a list"
  (cl:list 'TransportUnitState
    (cl:cons ':kobuki_general (kobuki_general msg))
    (cl:cons ':kobuki_obstacle (kobuki_obstacle msg))
    (cl:cons ':kobuki_position (kobuki_position msg))
    (cl:cons ':odroid_date (odroid_date msg))
))
