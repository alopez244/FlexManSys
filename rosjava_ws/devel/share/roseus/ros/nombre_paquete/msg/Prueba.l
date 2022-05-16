;; Auto-generated. Do not edit!


(when (boundp 'nombre_paquete::Prueba)
  (if (not (find-package "NOMBRE_PAQUETE"))
    (make-package "NOMBRE_PAQUETE"))
  (shadow 'Prueba (find-package "NOMBRE_PAQUETE")))
(unless (find-package "NOMBRE_PAQUETE::PRUEBA")
  (make-package "NOMBRE_PAQUETE::PRUEBA"))

(in-package "ROS")
;;//! \htmlinclude Prueba.msg.html


(defclass nombre_paquete::Prueba
  :super ros::object
  :slots (_numero_prueba ))

(defmethod nombre_paquete::Prueba
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

(setf (get nombre_paquete::Prueba :md5sum-) "5d1d253d7f30569507dbcd46fe13058c")
(setf (get nombre_paquete::Prueba :datatype-) "nombre_paquete/Prueba")
(setf (get nombre_paquete::Prueba :definition-)
      "int32 numero_prueba




")



(provide :nombre_paquete/Prueba "5d1d253d7f30569507dbcd46fe13058c")


