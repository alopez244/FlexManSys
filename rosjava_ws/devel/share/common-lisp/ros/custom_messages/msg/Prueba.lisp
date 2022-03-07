; Auto-generated. Do not edit!


(cl:in-package custom_messages-msg)


;//! \htmlinclude Prueba.msg.html

(cl:defclass <Prueba> (roslisp-msg-protocol:ros-message)
  ((numero_prueba
    :reader numero_prueba
    :initarg :numero_prueba
    :type cl:integer
    :initform 0))
)

(cl:defclass Prueba (<Prueba>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <Prueba>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'Prueba)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name custom_messages-msg:<Prueba> is deprecated: use custom_messages-msg:Prueba instead.")))

(cl:ensure-generic-function 'numero_prueba-val :lambda-list '(m))
(cl:defmethod numero_prueba-val ((m <Prueba>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader custom_messages-msg:numero_prueba-val is deprecated.  Use custom_messages-msg:numero_prueba instead.")
  (numero_prueba m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <Prueba>) ostream)
  "Serializes a message object of type '<Prueba>"
  (cl:let* ((signed (cl:slot-value msg 'numero_prueba)) (unsigned (cl:if (cl:< signed 0) (cl:+ signed 4294967296) signed)))
    (cl:write-byte (cl:ldb (cl:byte 8 0) unsigned) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 8) unsigned) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 16) unsigned) ostream)
    (cl:write-byte (cl:ldb (cl:byte 8 24) unsigned) ostream)
    )
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <Prueba>) istream)
  "Deserializes a message object of type '<Prueba>"
    (cl:let ((unsigned 0))
      (cl:setf (cl:ldb (cl:byte 8 0) unsigned) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 8) unsigned) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 16) unsigned) (cl:read-byte istream))
      (cl:setf (cl:ldb (cl:byte 8 24) unsigned) (cl:read-byte istream))
      (cl:setf (cl:slot-value msg 'numero_prueba) (cl:if (cl:< unsigned 2147483648) unsigned (cl:- unsigned 4294967296))))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<Prueba>)))
  "Returns string type for a message object of type '<Prueba>"
  "custom_messages/Prueba")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'Prueba)))
  "Returns string type for a message object of type 'Prueba"
  "custom_messages/Prueba")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<Prueba>)))
  "Returns md5sum for a message object of type '<Prueba>"
  "5d1d253d7f30569507dbcd46fe13058c")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'Prueba)))
  "Returns md5sum for a message object of type 'Prueba"
  "5d1d253d7f30569507dbcd46fe13058c")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<Prueba>)))
  "Returns full string definition for message of type '<Prueba>"
  (cl:format cl:nil "int32 numero_prueba~%~%~%~%~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'Prueba)))
  "Returns full string definition for message of type 'Prueba"
  (cl:format cl:nil "int32 numero_prueba~%~%~%~%~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <Prueba>))
  (cl:+ 0
     4
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <Prueba>))
  "Converts a ROS message object to a list"
  (cl:list 'Prueba
    (cl:cons ':numero_prueba (numero_prueba msg))
))
