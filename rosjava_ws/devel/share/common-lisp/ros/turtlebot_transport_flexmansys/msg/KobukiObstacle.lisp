; Auto-generated. Do not edit!


(cl:in-package turtlebot_transport_flexmansys-msg)


;//! \htmlinclude KobukiObstacle.msg.html

(cl:defclass <KobukiObstacle> (roslisp-msg-protocol:ros-message)
  ((detected_obstacle_bumper
    :reader detected_obstacle_bumper
    :initarg :detected_obstacle_bumper
    :type cl:boolean
    :initform cl:nil)
   (detected_obstacle_camera
    :reader detected_obstacle_camera
    :initarg :detected_obstacle_camera
    :type cl:boolean
    :initform cl:nil))
)

(cl:defclass KobukiObstacle (<KobukiObstacle>)
  ())

(cl:defmethod cl:initialize-instance :after ((m <KobukiObstacle>) cl:&rest args)
  (cl:declare (cl:ignorable args))
  (cl:unless (cl:typep m 'KobukiObstacle)
    (roslisp-msg-protocol:msg-deprecation-warning "using old message class name turtlebot_transport_flexmansys-msg:<KobukiObstacle> is deprecated: use turtlebot_transport_flexmansys-msg:KobukiObstacle instead.")))

(cl:ensure-generic-function 'detected_obstacle_bumper-val :lambda-list '(m))
(cl:defmethod detected_obstacle_bumper-val ((m <KobukiObstacle>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-msg:detected_obstacle_bumper-val is deprecated.  Use turtlebot_transport_flexmansys-msg:detected_obstacle_bumper instead.")
  (detected_obstacle_bumper m))

(cl:ensure-generic-function 'detected_obstacle_camera-val :lambda-list '(m))
(cl:defmethod detected_obstacle_camera-val ((m <KobukiObstacle>))
  (roslisp-msg-protocol:msg-deprecation-warning "Using old-style slot reader turtlebot_transport_flexmansys-msg:detected_obstacle_camera-val is deprecated.  Use turtlebot_transport_flexmansys-msg:detected_obstacle_camera instead.")
  (detected_obstacle_camera m))
(cl:defmethod roslisp-msg-protocol:serialize ((msg <KobukiObstacle>) ostream)
  "Serializes a message object of type '<KobukiObstacle>"
  (cl:write-byte (cl:ldb (cl:byte 8 0) (cl:if (cl:slot-value msg 'detected_obstacle_bumper) 1 0)) ostream)
  (cl:write-byte (cl:ldb (cl:byte 8 0) (cl:if (cl:slot-value msg 'detected_obstacle_camera) 1 0)) ostream)
)
(cl:defmethod roslisp-msg-protocol:deserialize ((msg <KobukiObstacle>) istream)
  "Deserializes a message object of type '<KobukiObstacle>"
    (cl:setf (cl:slot-value msg 'detected_obstacle_bumper) (cl:not (cl:zerop (cl:read-byte istream))))
    (cl:setf (cl:slot-value msg 'detected_obstacle_camera) (cl:not (cl:zerop (cl:read-byte istream))))
  msg
)
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql '<KobukiObstacle>)))
  "Returns string type for a message object of type '<KobukiObstacle>"
  "turtlebot_transport_flexmansys/KobukiObstacle")
(cl:defmethod roslisp-msg-protocol:ros-datatype ((msg (cl:eql 'KobukiObstacle)))
  "Returns string type for a message object of type 'KobukiObstacle"
  "turtlebot_transport_flexmansys/KobukiObstacle")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql '<KobukiObstacle>)))
  "Returns md5sum for a message object of type '<KobukiObstacle>"
  "2aef80677f5e7bb119c67140618dc301")
(cl:defmethod roslisp-msg-protocol:md5sum ((type (cl:eql 'KobukiObstacle)))
  "Returns md5sum for a message object of type 'KobukiObstacle"
  "2aef80677f5e7bb119c67140618dc301")
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql '<KobukiObstacle>)))
  "Returns full string definition for message of type '<KobukiObstacle>"
  (cl:format cl:nil "bool detected_obstacle_bumper~%bool detected_obstacle_camera~%~%"))
(cl:defmethod roslisp-msg-protocol:message-definition ((type (cl:eql 'KobukiObstacle)))
  "Returns full string definition for message of type 'KobukiObstacle"
  (cl:format cl:nil "bool detected_obstacle_bumper~%bool detected_obstacle_camera~%~%"))
(cl:defmethod roslisp-msg-protocol:serialization-length ((msg <KobukiObstacle>))
  (cl:+ 0
     1
     1
))
(cl:defmethod roslisp-msg-protocol:ros-message-to-list ((msg <KobukiObstacle>))
  "Converts a ROS message object to a list"
  (cl:list 'KobukiObstacle
    (cl:cons ':detected_obstacle_bumper (detected_obstacle_bumper msg))
    (cl:cons ':detected_obstacle_camera (detected_obstacle_camera msg))
))
