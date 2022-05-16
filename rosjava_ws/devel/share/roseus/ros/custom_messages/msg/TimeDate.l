;; Auto-generated. Do not edit!


(when (boundp 'custom_messages::TimeDate)
  (if (not (find-package "CUSTOM_MESSAGES"))
    (make-package "CUSTOM_MESSAGES"))
  (shadow 'TimeDate (find-package "CUSTOM_MESSAGES")))
(unless (find-package "CUSTOM_MESSAGES::TIMEDATE")
  (make-package "CUSTOM_MESSAGES::TIMEDATE"))

(in-package "ROS")
;;//! \htmlinclude TimeDate.msg.html


(defclass custom_messages::TimeDate
  :super ros::object
  :slots (_year _month _day _hour _minute _seconds ))

(defmethod custom_messages::TimeDate
  (:init
   (&key
    ((:year __year) 0)
    ((:month __month) 0)
    ((:day __day) 0)
    ((:hour __hour) 0)
    ((:minute __minute) 0)
    ((:seconds __seconds) 0)
    )
   (send-super :init)
   (setq _year (round __year))
   (setq _month (round __month))
   (setq _day (round __day))
   (setq _hour (round __hour))
   (setq _minute (round __minute))
   (setq _seconds (round __seconds))
   self)
  (:year
   (&optional __year)
   (if __year (setq _year __year)) _year)
  (:month
   (&optional __month)
   (if __month (setq _month __month)) _month)
  (:day
   (&optional __day)
   (if __day (setq _day __day)) _day)
  (:hour
   (&optional __hour)
   (if __hour (setq _hour __hour)) _hour)
  (:minute
   (&optional __minute)
   (if __minute (setq _minute __minute)) _minute)
  (:seconds
   (&optional __seconds)
   (if __seconds (setq _seconds __seconds)) _seconds)
  (:serialization-length
   ()
   (+
    ;; int32 _year
    4
    ;; int32 _month
    4
    ;; int32 _day
    4
    ;; int32 _hour
    4
    ;; int32 _minute
    4
    ;; int32 _seconds
    4
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; int32 _year
       (write-long _year s)
     ;; int32 _month
       (write-long _month s)
     ;; int32 _day
       (write-long _day s)
     ;; int32 _hour
       (write-long _hour s)
     ;; int32 _minute
       (write-long _minute s)
     ;; int32 _seconds
       (write-long _seconds s)
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; int32 _year
     (setq _year (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;; int32 _month
     (setq _month (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;; int32 _day
     (setq _day (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;; int32 _hour
     (setq _hour (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;; int32 _minute
     (setq _minute (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;; int32 _seconds
     (setq _seconds (sys::peek buf ptr- :integer)) (incf ptr- 4)
   ;;
   self)
  )

(setf (get custom_messages::TimeDate :md5sum-) "d2fd2933667b73a2a097c5705da48ffb")
(setf (get custom_messages::TimeDate :datatype-) "custom_messages/TimeDate")
(setf (get custom_messages::TimeDate :definition-)
      "int32 year
int32 month
int32 day
int32 hour
int32 minute
int32 seconds


")



(provide :custom_messages/TimeDate "d2fd2933667b73a2a097c5705da48ffb")


