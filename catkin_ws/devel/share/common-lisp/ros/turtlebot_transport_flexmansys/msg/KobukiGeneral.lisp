; Auto-generated. Do not edit!


(cl:in-package turtlebot_transport_flexmansys-msg)


;//! \htmlinclude KobukiGeneral.msg.html

(cl:defclass <KobukiGeneral> (roslisp-msg-protocol:ros-message)
  ((transport_unit_name
    :reader transport_unit_name
    :initarg :transport_unit_name
    :type cl:string
    :initform "")
   (transport_unit_state
    :reader transport_unit_state
    :initarg :transport_unit_state
    :type cl:string
    :initform "")
   (battery
    :reader battery
    :initarg :battery
    :type cl:float
    :initform 0.0))
)

(cl:defclass KobukiGeneral (<KobukiGeneral>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <KobukiGeneral>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'KobukiGeneral)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name turtlebot_transport_flexmansys-msg:<KobukiGeneral> is deprecated: use turtlebot_transport_flexmansys-msg:KobukiGeneral instead.")))

(cl:ensure-generic-function 'transport_unit_name-val :lambda-list '(m))
(cl:defmethod transport_unit_name-val ((m <KobukiGeneral>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-msg:transport_unit_name-val is deprecated.  Use turtlebot_transport_flexmansys-msg:transport_unit_name instead.")
  (transport_unit_name m))

(cl:ensure-generic-function 'transport_unit_state-val :lambda-list '(m))
(cl:defmethod transport_unit_state-val ((m <KobukiGeneral>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-msg:transport_unit_state-val is deprecated.  Use turtlebot_transport_flexmansys-msg:transport_unit_state instead.")
  (transport_unit_state m))

(cl:ensure-generic-function 'battery-val :lambda-list '(m))
(cl:defmethod battery-val ((m <KobukiGeneral>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-msg:battery-val is deprecated.  Use turtlebot_transport_flexmansys-msg:battery instead.")
  (battery m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <KobukiGeneral>) ostream)
  "Serializes a message object of type '<KobukiGeneral>"
  (cl:let ((__ros_str_len (cl:length (cl:slot-value msg 'transport_unit_name))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) __ros_str_len) ostream))
  (cl:map cl:nil #'(cl:lambda (c) (cl:write-byte (cl:char-code c) ostream)) (cl:slot-value msg 'transport_unit_name))
  (cl:let ((__ros_str_len (cl:length (cl:slot-value msg 'transport_unit_state))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) __ros_str_len) ostream))
  (cl:map cl:nil #'(cl:lambda (c) (cl:write-byte (cl:char-code c) ostream)) (cl:slot-value msg 'transport_unit_state))
  (cl:let ((bits (roslisp-utils:encode-single-float-bits (cl:slot-value msg 'battery))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) bits) ostream))
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <KobukiGeneral>) istream)
  "Deserializes a message object of type '<KobukiGeneral>"
    (cl:let ((__ros_str_len 0))
      (cl:setf (cl:ldb (cl:byte 8 0) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:slot-value msg 'transport_unit_name) (cl:make-string __ros_str_len))
      (cl:dotimes (__ros_str_idx __ros_str_len msg)
        (cl:setf (cl:char (cl:slot-value msg 'transport_unit_name) __ros_str_idx) (cl:code-char (cl:read-byte istream)))))
    (cl:let ((__ros_str_len 0))
      (cl:setf (cl:ldb (cl:byte 8 0) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:slot-value msg 'transport_unit_state) (cl:make-string __ros_str_len))
      (cl:dotimes (__ros_str_idx __ros_str_len msg)
        (cl:setf (cl:char (cl:slot-value msg 'transport_unit_state) __ros_str_idx) (cl:code-char (cl:read-byte istream)))))
    (cl:let ((bits 0))
      (cl:setf (cl:ldb (cl:byte 8 0) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) bits) (cl:read-byte istream))
    (cl:setf (cl:slot-value msg 'battery) (roslisp-utils:decode-single-float-bits bits)))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<KobukiGeneral>)))
  "Returns string type for a message object of type '<KobukiGeneral>"
  "turtlebot_transport_flexmansys/KobukiGeneral")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'KobukiGeneral)))
  "Returns string type for a message object of type 'KobukiGeneral"
  "turtlebot_transport_flexmansys/KobukiGeneral")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<KobukiGeneral>)))
  "Returns md5sum for a message object of type '<KobukiGeneral>"
  "dd826a4a104ac611941e72978f2a882c")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'KobukiGeneral)))
  "Returns md5sum for a message object of type 'KobukiGeneral"
  "dd826a4a104ac611941e72978f2a882c")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<KobukiGeneral>)))
  "Returns full string definition for message of type '<KobukiGeneral>"
  (cl:format cl:nil "string transport_unit_name~%string transport_unit_state~%float32 battery~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'KobukiGeneral)))
  "Returns full string definition for message of type 'KobukiGeneral"
  (cl:format cl:nil "string transport_unit_name~%string transport_unit_state~%float32 battery~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <KobukiGeneral>))
  (cl:+ 0
     4 (cl:length (cl:slot-value msg 'transport_unit_name))
     4 (cl:length (cl:slot-value msg 'transport_unit_state))
     4
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <KobukiGeneral>))
  "Converts a ROS message object to a list"
  (cl:list 'KobukiGeneral
    (cl:cons ':transport_unit_name (transport_unit_name msg))
    (cl:cons ':transport_unit_state (transport_unit_state msg))
    (cl:cons ':battery (battery msg))
))
