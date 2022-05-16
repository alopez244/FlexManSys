; Auto-generated. Do not edit!


(cl:in-package custom_messages-msg)


;//! \htmlinclude KobukiPosition.msg.html

(cl:defclass <KobukiPosition> (roslisp-msg-protocol:ros-message)
  ((transport_in_dock
    :reader transport_in_dock
    :initarg :transport_in_dock
    :type cl:boolean
    :initform cl:nil)
   (recovery_point
    :reader recovery_point
    :initarg :recovery_point
    :type cl:string
    :initform "")
   (odom_x
    :reader odom_x
    :initarg :odom_x
    :type cl:float
    :initform 0.0)
   (odom_y
    :reader odom_y
    :initarg :odom_y
    :type cl:float
    :initform 0.0)
   (rotation
    :reader rotation
    :initarg :rotation
    :type cl:float
    :initform 0.0))
)

(cl:defclass KobukiPosition (<KobukiPosition>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <KobukiPosition>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'KobukiPosition)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name custom_messages-msg:<KobukiPosition> is deprecated: use custom_messages-msg:KobukiPosition instead.")))

(cl:ensure-generic-function 'transport_in_dock-val :lambda-list '(m))
(cl:defmethod transport_in_dock-val ((m <KobukiPosition>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:transport_in_dock-val is deprecated.  Use custom_messages-msg:transport_in_dock instead.")
  (transport_in_dock m))

(cl:ensure-generic-function 'recovery_point-val :lambda-list '(m))
(cl:defmethod recovery_point-val ((m <KobukiPosition>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:recovery_point-val is deprecated.  Use custom_messages-msg:recovery_point instead.")
  (recovery_point m))

(cl:ensure-generic-function 'odom_x-val :lambda-list '(m))
(cl:defmethod odom_x-val ((m <KobukiPosition>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:odom_x-val is deprecated.  Use custom_messages-msg:odom_x instead.")
  (odom_x m))

(cl:ensure-generic-function 'odom_y-val :lambda-list '(m))
(cl:defmethod odom_y-val ((m <KobukiPosition>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:odom_y-val is deprecated.  Use custom_messages-msg:odom_y instead.")
  (odom_y m))

(cl:ensure-generic-function 'rotation-val :lambda-list '(m))
(cl:defmethod rotation-val ((m <KobukiPosition>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:rotation-val is deprecated.  Use custom_messages-msg:rotation instead.")
  (rotation m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <KobukiPosition>) ostream)
  "Serializes a message object of type '<KobukiPosition>"
  (cl:write-byte (cl:ldb (cl:byte 8 0) (cl:if (cl:slot-value msg 'transport_in_dock) 1 0)) ostream)
  (cl:let ((__ros_str_len (cl:length (cl:slot-value msg 'recovery_point))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) __ros_str_len) ostream))
  (cl:map cl:nil #'(cl:lambda (c) (cl:write-byte (cl:char-code c) ostream)) (cl:slot-value msg 'recovery_point))
  (cl:let ((bits (roslisp-utils:encode-double-float-bits (cl:slot-value msg 'odom_x))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 32) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 40) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 48) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 56) bits) ostream))
  (cl:let ((bits (roslisp-utils:encode-double-float-bits (cl:slot-value msg 'odom_y))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 32) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 40) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 48) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 56) bits) ostream))
  (cl:let ((bits (roslisp-utils:encode-double-float-bits (cl:slot-value msg 'rotation))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 32) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 40) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 48) bits) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 56) bits) ostream))
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <KobukiPosition>) istream)
  "Deserializes a message object of type '<KobukiPosition>"
    (cl:setf (cl:slot-value msg 'transport_in_dock) (cl:not (cl:zerop (cl:read-byte istream))))
    (cl:let ((__ros_str_len 0))
      (cl:setf (cl:ldb (cl:byte 8 0) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:slot-value msg 'recovery_point) (cl:make-string __ros_str_len))
      (cl:dotimes (__ros_str_idx __ros_str_len msg)
        (cl:setf (cl:char (cl:slot-value msg 'recovery_point) __ros_str_idx) (cl:code-char (cl:read-byte istream)))))
    (cl:let ((bits 0))
      (cl:setf (cl:ldb (cl:byte 8 0) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 32) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 40) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 48) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 56) bits) (cl:read-byte istream))
    (cl:setf (cl:slot-value msg 'odom_x) (roslisp-utils:decode-double-float-bits bits)))
    (cl:let ((bits 0))
      (cl:setf (cl:ldb (cl:byte 8 0) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 32) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 40) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 48) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 56) bits) (cl:read-byte istream))
    (cl:setf (cl:slot-value msg 'odom_y) (roslisp-utils:decode-double-float-bits bits)))
    (cl:let ((bits 0))
      (cl:setf (cl:ldb (cl:byte 8 0) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 32) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 40) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 48) bits) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 56) bits) (cl:read-byte istream))
    (cl:setf (cl:slot-value msg 'rotation) (roslisp-utils:decode-double-float-bits bits)))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<KobukiPosition>)))
  "Returns string type for a message object of type '<KobukiPosition>"
  "custom_messages/KobukiPosition")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'KobukiPosition)))
  "Returns string type for a message object of type 'KobukiPosition"
  "custom_messages/KobukiPosition")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<KobukiPosition>)))
  "Returns md5sum for a message object of type '<KobukiPosition>"
  "fa959bfdcde117615672bd484119c1ee")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'KobukiPosition)))
  "Returns md5sum for a message object of type 'KobukiPosition"
  "fa959bfdcde117615672bd484119c1ee")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<KobukiPosition>)))
  "Returns full string definition for message of type '<KobukiPosition>"
  (cl:format cl:nil "bool transport_in_dock~%string recovery_point~%float64 odom_x~%float64 odom_y~%float64 rotation~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'KobukiPosition)))
  "Returns full string definition for message of type 'KobukiPosition"
  (cl:format cl:nil "bool transport_in_dock~%string recovery_point~%float64 odom_x~%float64 odom_y~%float64 rotation~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <KobukiPosition>))
  (cl:+ 0
     1
     4 (cl:length (cl:slot-value msg 'recovery_point))
     8
     8
     8
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <KobukiPosition>))
  "Converts a ROS message object to a list"
  (cl:list 'KobukiPosition
    (cl:cons ':transport_in_dock (transport_in_dock msg))
    (cl:cons ':recovery_point (recovery_point msg))
    (cl:cons ':odom_x (odom_x msg))
    (cl:cons ':odom_y (odom_y msg))
    (cl:cons ':rotation (rotation msg))
))
