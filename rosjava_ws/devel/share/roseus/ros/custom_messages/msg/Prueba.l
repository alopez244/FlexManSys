;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::Prueba)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'Prueba (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::PRUEBA")
  (make-package "CUSTOM_MESSAGES::PRUEBA"))

(in-package "ROS")
;;//! \htmlinclude Prueba.msg.html


(defclass custom_messages::Prueba
  :super ros::object
  :slots (_numero_prueba ))

(defmethod custom_messages::Prueba
  (:init
   (&key
    ((:numero_prueba __numero_prueba) 0)
    )
   (send-super :init)
   (setq _numero_prueba (round __numero_prueba))
   self)
  (:numero_prueba
   (&optional __numero_prueba)
   (if __numero_prueba (setq _numero_prueba __numero_prueba)) _numero_prueba)
  (:serialization-length
   ()
   (+
    ;; int32 _numero_prueba
    4
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; int32 _numero_prueba
       (write-long _numero_prueba s)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; int32 _numero_prueba
     (setq _numero_prueba (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;;
   self)
  )

(setf (get custom_messages::Prueba :md5sum-) "5d1d253d7f30569507dbcd46fe13058c")
(setf (get custom_messages::Prueba :datatype-) "custom_messages/Prueba")
(setf (get custom_messages::Prueba :definition-)
      "int32 numero_prueba




")



(provide :custom_messages/Prueba "5d1d253d7f30569507dbcd46fe13058c")


