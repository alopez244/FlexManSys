; Auto-generated. Do not edit!


(cl:in-package custom_messages-msg)


;//! \htmlinclude TransportPrivateState.msg.html

(cl:defclass <TransportPrivateState> (roslisp-msg-protocol:ros-message)
  ((transport_state
    :reader transport_state
    :initarg :transport_state
    :type cl:string
    :initform "")
   (transport_docked
    :reader transport_docked
    :initarg :transport_docked
    :type cl:boolean
    :initform cl:nil))
)

(cl:defclass TransportPrivateState (<TransportPrivateState>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <TransportPrivateState>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'TransportPrivateState)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name custom_messages-msg:<TransportPrivateState> is deprecated: use custom_messages-msg:TransportPrivateState instead.")))

(cl:ensure-generic-function 'transport_state-val :lambda-list '(m))
(cl:defmethod transport_state-val ((m <TransportPrivateState>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:transport_state-val is deprecated.  Use custom_messages-msg:transport_state instead.")
  (transport_state m))

(cl:ensure-generic-function 'transport_docked-val :lambda-list '(m))
(cl:defmethod transport_docked-val ((m <TransportPrivateState>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:transport_docked-val is deprecated.  Use custom_messages-msg:transport_docked instead.")
  (transport_docked m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <TransportPrivateState>) ostream)
  "Serializes a message object of type '<TransportPrivateState>"
  (cl:let ((__ros_str_len (cl:length (cl:slot-value msg 'transport_state))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) __ros_str_len) ostream))
  (cl:map cl:nil #'(cl:lambda (c) (cl:write-byte (cl:char-code c) ostream)) (cl:slot-value msg 'transport_state))
  (cl:write-byte (cl:ldb (cl:byte 8 0) (cl:if (cl:slot-value msg 'transport_docked) 1 0)) ostream)
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <TransportPrivateState>) istream)
  "Deserializes a message object of type '<TransportPrivateState>"
    (cl:let ((__ros_str_len 0))
      (cl:setf (cl:ldb (cl:byte 8 0) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:slot-value msg 'transport_state) (cl:make-string __ros_str_len))
      (cl:dotimes (__ros_str_idx __ros_str_len msg)
        (cl:setf (cl:char (cl:slot-value msg 'transport_state) __ros_str_idx) (cl:code-char (cl:read-byte istream)))))
    (cl:setf (cl:slot-value msg 'transport_docked) (cl:not (cl:zerop (cl:read-byte istream))))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<TransportPrivateState>)))
  "Returns string type for a message object of type '<TransportPrivateState>"
  "custom_messages/TransportPrivateState")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'TransportPrivateState)))
  "Returns string type for a message object of type 'TransportPrivateState"
  "custom_messages/TransportPrivateState")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<TransportPrivateState>)))
  "Returns md5sum for a message object of type '<TransportPrivateState>"
  "670f3bb83518dec928ea7b995f722845")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'TransportPrivateState)))
  "Returns md5sum for a message object of type 'TransportPrivateState"
  "670f3bb83518dec928ea7b995f722845")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<TransportPrivateState>)))
  "Returns full string definition for message of type '<TransportPrivateState>"
  (cl:format cl:nil "string transport_state~%bool transport_docked~%~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'TransportPrivateState)))
  "Returns full string definition for message of type 'TransportPrivateState"
  (cl:format cl:nil "string transport_state~%bool transport_docked~%~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <TransportPrivateState>))
  (cl:+ 0
     4 (cl:length (cl:slot-value msg 'transport_state))
     1
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <TransportPrivateState>))
  "Converts a ROS message object to a list"
  (cl:list 'TransportPrivateState
    (cl:cons ':transport_state (transport_state msg))
    (cl:cons ':transport_docked (transport_docked msg))
))
