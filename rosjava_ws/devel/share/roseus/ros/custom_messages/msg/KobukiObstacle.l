;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::KobukiObstacle)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'KobukiObstacle (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::KOBUKIOBSTACLE")
  (make-package "CUSTOM_MESSAGES::KOBUKIOBSTACLE"))

(in-package "ROS")
;;//! \htmlinclude KobukiObstacle.msg.html


(defclass custom_messages::KobukiObstacle
  :super ros::object
  :slots (_detected_obstacle_bumper _detected_obstacle_camera ))

(defmethod custom_messages::KobukiObstacle
  (:init
   (&key
    ((:detected_obstacle_bumper __detected_obstacle_bumper) nil)
    ((:detected_obstacle_camera __detected_obstacle_camera) nil)
    )
   (send-super :init)
   (setq _detected_obstacle_bumper __detected_obstacle_bumper)
   (setq _detected_obstacle_camera __detected_obstacle_camera)
   self)
  (:detected_obstacle_bumper
   (&optional __detected_obstacle_bumper)
   (if __detected_obstacle_bumper (setq _detected_obstacle_bumper __detected_obstacle_bumper)) _detected_obstacle_bumper)
  (:detected_obstacle_camera
   (&optional __detected_obstacle_camera)
   (if __detected_obstacle_camera (setq _detected_obstacle_camera __detected_obstacle_camera)) _detected_obstacle_camera)
  (:serialization-length
   ()
   (+
    ;; bool _detected_obstacle_bumper
    1
    ;; bool _detected_obstacle_camera
    1
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; bool _detected_obstacle_bumper
       (if _detected_obstacle_bumper (write-byte -1 s) (write-byte 0 s))
     ;; bool _detected_obstacle_camera
       (if _detected_obstacle_camera (write-byte -1 s) (write-byte 0 s))
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; bool _detected_obstacle_bumper
     (setq _detected_obstacle_bumper (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;; bool _detected_obstacle_camera
     (setq _detected_obstacle_camera (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;;
   self)
  )

(setf (get custom_messages::KobukiObstacle :md5sum-) "2aef80677f5e7bb119c67140618dc301")
(setf (get custom_messages::KobukiObstacle :datatype-) "custom_messages/KobukiObstacle")
(setf (get custom_messages::KobukiObstacle :definition-)
      "bool detected_obstacle_bumper
bool detected_obstacle_camera
")



(provide :custom_messages/KobukiObstacle "2aef80677f5e7bb119c67140618dc301")


