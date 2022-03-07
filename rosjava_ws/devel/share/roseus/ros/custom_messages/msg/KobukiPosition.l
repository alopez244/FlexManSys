;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::KobukiPosition)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'KobukiPosition (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::KOBUKIPOSITION")
  (make-package "CUSTOM_MESSAGES::KOBUKIPOSITION"))

(in-package "ROS")
;;//! \htmlinclude KobukiPosition.msg.html


(defclass custom_messages::KobukiPosition
  :super ros::object
  :slots (_transport_in_dock _recovery_point _odom_x _odom_y _rotation ))

(defmethod custom_messages::KobukiPosition
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

(setf (get custom_messages::KobukiPosition :md5sum-) "fa959bfdcde117615672bd484119c1ee")
(setf (get custom_messages::KobukiPosition :datatype-) "custom_messages/KobukiPosition")
(setf (get custom_messages::KobukiPosition :definition-)
      "bool transport_in_dock
string recovery_point
float64 odom_x
float64 odom_y
float64 rotation
")



(provide :custom_messages/KobukiPosition "fa959bfdcde117615672bd484119c1ee")


