;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::KobukiGeneral)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'KobukiGeneral (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::KOBUKIGENERAL")
  (make-package "CUSTOM_MESSAGES::KOBUKIGENERAL"))

(in-package "ROS")
;;//! \htmlinclude KobukiGeneral.msg.html


(defclass custom_messages::KobukiGeneral
  :super ros::object
  :slots (_transport_unit_name _transport_unit_state _battery ))

(defmethod custom_messages::KobukiGeneral
  (:init
   (&key
    ((:transport_unit_name __transport_unit_name) "")
    ((:transport_unit_state __transport_unit_state) "")
    ((:battery __battery) 0.0)
    )
   (send-super :init)
   (setq _transport_unit_name (string __transport_unit_name))
   (setq _transport_unit_state (string __transport_unit_state))
   (setq _battery (float __battery))
   self)
  (:transport_unit_name
   (&optional __transport_unit_name)
   (if __transport_unit_name (setq _transport_unit_name __transport_unit_name)) _transport_unit_name)
  (:transport_unit_state
   (&optional __transport_unit_state)
   (if __transport_unit_state (setq _transport_unit_state __transport_unit_state)) _transport_unit_state)
  (:battery
   (&optional __battery)
   (if __battery (setq _battery __battery)) _battery)
  (:serialization-length
   ()
   (+
    ;; string _transport_unit_name
    4 (length _transport_unit_name)
    ;; string _transport_unit_state
    4 (length _transport_unit_state)
    ;; float32 _battery
    4
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; string _transport_unit_name
       (write-long (length _transport_unit_name) s) (princ _transport_unit_name s)
     ;; string _transport_unit_state
       (write-long (length _transport_unit_state) s) (princ _transport_unit_state s)
     ;; float32 _battery
       (sys::poke _battery (send s :buffer) (send s :count) :float) (incf (stream-count s) 4)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; string _transport_unit_name
     (let (n) (setq n (sys::peek buf ptr- :integer)) (incf ptr- 4) (setq _transport_unit_name (subseq buf ptr- (+ ptr- n))) (incf ptr- n))
   ;; string _transport_unit_state
     (let (n) (setq n (sys::peek buf ptr- :integer)) (incf ptr- 4) (setq _transport_unit_state (subseq buf ptr- (+ ptr- n))) (incf ptr- n))
   ;; float32 _battery
     (setq _battery (sys::peek buf ptr- :float)) (incf ptr- 4)
   ;;
   self)
  )

(setf (get custom_messages::KobukiGeneral :md5sum-) "dd826a4a104ac611941e72978f2a882c")
(setf (get custom_messages::KobukiGeneral :datatype-) "custom_messages/KobukiGeneral")
(setf (get custom_messages::KobukiGeneral :definition-)
      "string transport_unit_name
string transport_unit_state
float32 battery
")



(provide :custom_messages/KobukiGeneral "dd826a4a104ac611941e72978f2a882c")


