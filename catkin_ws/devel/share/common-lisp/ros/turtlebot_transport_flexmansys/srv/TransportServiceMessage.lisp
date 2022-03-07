; Auto-generated. Do not edit!


(cl:in-package turtlebot_transport_flexmansys-srv)


;//! \htmlinclude TransportServiceMessage-request.msg.html

(cl:defclass <TransportServiceMessage-request> (roslisp-msg-protocol:ros-message)
  ((coordinate
    :reader coordinate
    :initarg :coordinate
    :type cl:string
    :initform ""))
)

(cl:defclass TransportServiceMessage-request (<TransportServiceMessage-request>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <TransportServiceMessage-request>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'TransportServiceMessage-request)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name turtlebot_transport_flexmansys-srv:<TransportServiceMessage-request> is deprecated: use turtlebot_transport_flexmansys-srv:TransportServiceMessage-request instead.")))

(cl:ensure-generic-function 'coordinate-val :lambda-list '(m))
(cl:defmethod coordinate-val ((m <TransportServiceMessage-request>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-srv:coordinate-val is deprecated.  Use turtlebot_transport_flexmansys-srv:coordinate instead.")
  (coordinate m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <TransportServiceMessage-request>) ostream)
  "Serializes a message object of type '<TransportServiceMessage-request>"
  (cl:let ((__ros_str_len (cl:length (cl:slot-value msg 'coordinate))))
    (cl:write-byte (cl:ldb (cl:byte 8 0) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) __ros_str_len) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) __ros_str_len) ostream))
  (cl:map cl:nil #'(cl:lambda (c) (cl:write-byte (cl:char-code c) ostream)) (cl:slot-value msg 'coordinate))
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <TransportServiceMessage-request>) istream)
  "Deserializes a message object of type '<TransportServiceMessage-request>"
    (cl:let ((__ros_str_len 0))
      (cl:setf (cl:ldb (cl:byte 8 0) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) __ros_str_len) (cl:read-byte istream))
      (cl:setf (cl:slot-value msg 'coordinate) (cl:make-string __ros_str_len))
      (cl:dotimes (__ros_str_idx __ros_str_len msg)
        (cl:setf (cl:char (cl:slot-value msg 'coordinate) __ros_str_idx) (cl:code-char (cl:read-byte istream)))))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<TransportServiceMessage-request>)))
  "Returns string type for a service object of type '<TransportServiceMessage-request>"
  "turtlebot_transport_flexmansys/TransportServiceMessageRequest")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'TransportServiceMessage-request)))
  "Returns string type for a service object of type 'TransportServiceMessage-request"
  "turtlebot_transport_flexmansys/TransportServiceMessageRequest")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<TransportServiceMessage-request>)))
  "Returns md5sum for a message object of type '<TransportServiceMessage-request>"
  "8ddb36dd3d52323557949d25fae2e281")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'TransportServiceMessage-request)))
  "Returns md5sum for a message object of type 'TransportServiceMessage-request"
  "8ddb36dd3d52323557949d25fae2e281")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<TransportServiceMessage-request>)))
  "Returns full string definition for message of type '<TransportServiceMessage-request>"
  (cl:format cl:nil "string coordinate~%~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'TransportServiceMessage-request)))
  "Returns full string definition for message of type 'TransportServiceMessage-request"
  (cl:format cl:nil "string coordinate~%~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <TransportServiceMessage-request>))
  (cl:+ 0
     4 (cl:length (cl:slot-value msg 'coordinate))
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <TransportServiceMessage-request>))
  "Converts a ROS message object to a list"
  (cl:list 'TransportServiceMessage-request
    (cl:cons ':coordinate (coordinate msg))
))
;//! \htmlinclude TransportServiceMessage-response.msg.html

(cl:defclass <TransportServiceMessage-response> (roslisp-msg-protocol:ros-message)
  ((success
    :reader success
    :initarg :success
    :type cl:boolean
    :initform cl:nil))
)

(cl:defclass TransportServiceMessage-response (<TransportServiceMessage-response>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <TransportServiceMessage-response>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'TransportServiceMessage-response)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name turtlebot_transport_flexmansys-srv:<TransportServiceMessage-response> is deprecated: use turtlebot_transport_flexmansys-srv:TransportServiceMessage-response instead.")))

(cl:ensure-generic-function 'success-val :lambda-list '(m))
(cl:defmethod success-val ((m <TransportServiceMessage-response>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-srv:success-val is deprecated.  Use turtlebot_transport_flexmansys-srv:success instead.")
  (success m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <TransportServiceMessage-response>) ostream)
  "Serializes a message object of type '<TransportServiceMessage-response>"
  (cl:write-byte (cl:ldb (cl:byte 8 0) (cl:if (cl:slot-value msg 'success) 1 0)) ostream)
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <TransportServiceMessage-response>) istream)
  "Deserializes a message object of type '<TransportServiceMessage-response>"
    (cl:setf (cl:slot-value msg 'success) (cl:not (cl:zerop (cl:read-byte istream))))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<TransportServiceMessage-response>)))
  "Returns string type for a service object of type '<TransportServiceMessage-response>"
  "turtlebot_transport_flexmansys/TransportServiceMessageResponse")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'TransportServiceMessage-response)))
  "Returns string type for a service object of type 'TransportServiceMessage-response"
  "turtlebot_transport_flexmansys/TransportServiceMessageResponse")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<TransportServiceMessage-response>)))
  "Returns md5sum for a message object of type '<TransportServiceMessage-response>"
  "8ddb36dd3d52323557949d25fae2e281")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'TransportServiceMessage-response)))
  "Returns md5sum for a message object of type 'TransportServiceMessage-response"
  "8ddb36dd3d52323557949d25fae2e281")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<TransportServiceMessage-response>)))
  "Returns full string definition for message of type '<TransportServiceMessage-response>"
  (cl:format cl:nil "bool success~%~%~%~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'TransportServiceMessage-response)))
  "Returns full string definition for message of type 'TransportServiceMessage-response"
  (cl:format cl:nil "bool success~%~%~%~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <TransportServiceMessage-response>))
  (cl:+ 0
     1
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <TransportServiceMessage-response>))
  "Converts a ROS message object to a list"
  (cl:list 'TransportServiceMessage-response
    (cl:cons ':success (success msg))
))
(cl:defmethod roslisp-msg-protocol:service-request-type ((msg (cl:eql 'TransportServiceMessage)))
  'TransportServiceMessage-request)
(cl:defmethod roslisp-msg-protocol:service-response-type ((msg (cl:eql 'TransportServiceMessage)))
  'TransportServiceMessage-response)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'TransportServiceMessage)))
  "Returns string type for a service object of type '<TransportServiceMessage>"
  "turtlebot_transport_flexmansys/TransportServiceMessage")