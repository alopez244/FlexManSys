;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::TransportPrivateState)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'TransportPrivateState (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::TRANSPORTPRIVATESTATE")
  (make-package "CUSTOM_MESSAGES::TRANSPORTPRIVATESTATE"))

(in-package "ROS")
;;//! \htmlinclude TransportPrivateState.msg.html


(defclass custom_messages::TransportPrivateState
  :super ros::object
  :slots (_transport_state _transport_docked ))

(defmethod custom_messages::TransportPrivateState
  (:init
   (&key
    ((:transport_state __transport_state) "")
    ((:transport_docked __transport_docked) nil)
    )
   (send-super :init)
   (setq _transport_state (string __transport_state))
   (setq _transport_docked __transport_docked)
   self)
  (:transport_state
   (&optional __transport_state)
   (if __transport_state (setq _transport_state __transport_state)) _transport_state)
  (:transport_docked
   (&optional __transport_docked)
   (if __transport_docked (setq _transport_docked __transport_docked)) _transport_docked)
  (:serialization-length
   ()
   (+
    ;; string _transport_state
    4 (length _transport_state)
    ;; bool _transport_docked
    1
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; string _transport_state
       (write-long (length _transport_state) s) (princ _transport_state s)
     ;; bool _transport_docked
       (if _transport_docked (write-byte -1 s) (write-byte 0 s))
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; string _transport_state
     (let (n) (setq n (sys::peek buf ptr- :integer)) (incf ptr- 4) (setq _transport_state (subseq buf ptr- (+ ptr- n))) (incf ptr- n))
   ;; bool _transport_docked
     (setq _transport_docked (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;;
   self)
  )

(setf (get custom_messages::TransportPrivateState :md5sum-) "670f3bb83518dec928ea7b995f722845")
(setf (get custom_messages::TransportPrivateState :datatype-) "custom_messages/TransportPrivateState")
(setf (get custom_messages::TransportPrivateState :definition-)
      "string transport_state
bool transport_docked

")



(provide :custom_messages/TransportPrivateState "670f3bb83518dec928ea7b995f722845")


