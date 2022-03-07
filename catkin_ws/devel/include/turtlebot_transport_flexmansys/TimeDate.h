// Generated by gencpp from file turtlebot_transport_flexmansys/TimeDate.msg
// DO NOT EDIT!


#ifndef TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_TIMEDATE_H
#define TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_TIMEDATE_H


#include <string>
#include <vector>
#include <map>

#include <ros/types.h>
#include <ros/serialization.h>
#include <ros/builtin_message_traits.h>
#include <ros/message_operations.h>


namespace turtlebot_transport_flexmansys
{
template <class ContainerAllocator>
struct TimeDate_
{
  typedef TimeDate_<ContainerAllocator> Type;

  TimeDate_()
    : year(0)
    , month(0)
    , day(0)
    , hour(0)
    , minute(0)
    , seconds(0)  {
    }
  TimeDate_(const ContainerAllocator& _alloc)
    : year(0)
    , month(0)
    , day(0)
    , hour(0)
    , minute(0)
    , seconds(0)  {
  (void)_alloc;
    }



   typedef int32_t _year_type;
  _year_type year;

   typedef int32_t _month_type;
  _month_type month;

   typedef int32_t _day_type;
  _day_type day;

   typedef int32_t _hour_type;
  _hour_type hour;

   typedef int32_t _minute_type;
  _minute_type minute;

   typedef int32_t _seconds_type;
  _seconds_type seconds;





  typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> > Ptr;
  typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> const> ConstPtr;

}; // struct TimeDate_

typedef ::turtlebot_transport_flexmansys::TimeDate_<std::allocator<void> > TimeDate;

typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TimeDate > TimeDatePtr;
typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TimeDate const> TimeDateConstPtr;

// constants requiring out of line definition



template<typename ContainerAllocator>
std::ostream& operator<<(std::ostream& s, const ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> & v)
{
ros::message_operations::Printer< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >::stream(s, "", v);
return s;
}

} // namespace turtlebot_transport_flexmansys

namespace ros
{
namespace message_traits
{



// BOOLTRAITS {'IsFixedSize': True, 'IsMessage': True, 'HasHeader': False}
// {'turtlebot_transport_flexmansys': ['/home/borjartime/catkin_ws/src/turtlebot_transport_flexmansys/msg'], 'std_msgs': ['/opt/ros/kinetic/share/std_msgs/cmake/../msg']}

// !!!!!!!!!!! ['__class__', '__delattr__', '__dict__', '__doc__', '__eq__', '__format__', '__getattribute__', '__hash__', '__init__', '__module__', '__ne__', '__new__', '__reduce__', '__reduce_ex__', '__repr__', '__setattr__', '__sizeof__', '__str__', '__subclasshook__', '__weakref__', '_parsed_fields', 'constants', 'fields', 'full_name', 'has_header', 'header_present', 'names', 'package', 'parsed_fields', 'short_name', 'text', 'types']




template <class ContainerAllocator>
struct IsFixedSize< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
  : TrueType
  { };

template <class ContainerAllocator>
struct IsFixedSize< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> const>
  : TrueType
  { };

template <class ContainerAllocator>
struct IsMessage< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
  : TrueType
  { };

template <class ContainerAllocator>
struct IsMessage< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> const>
  : TrueType
  { };

template <class ContainerAllocator>
struct HasHeader< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
  : FalseType
  { };

template <class ContainerAllocator>
struct HasHeader< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> const>
  : FalseType
  { };


template<class ContainerAllocator>
struct MD5Sum< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
{
  static const char* value()
  {
    return "d2fd2933667b73a2a097c5705da48ffb";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator>&) { return value(); }
  static const uint64_t static_value1 = 0xd2fd2933667b73a2ULL;
  static const uint64_t static_value2 = 0xa097c5705da48ffbULL;
};

template<class ContainerAllocator>
struct DataType< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
{
  static const char* value()
  {
    return "turtlebot_transport_flexmansys/TimeDate";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator>&) { return value(); }
};

template<class ContainerAllocator>
struct Definition< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
{
  static const char* value()
  {
    return "int32 year\n\
int32 month\n\
int32 day\n\
int32 hour\n\
int32 minute\n\
int32 seconds\n\
\n\
";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator>&) { return value(); }
};

} // namespace message_traits
} // namespace ros

namespace ros
{
namespace serialization
{

  template<class ContainerAllocator> struct Serializer< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
  {
    template<typename Stream, typename T> inline static void allInOne(Stream& stream, T m)
    {
      stream.next(m.year);
      stream.next(m.month);
      stream.next(m.day);
      stream.next(m.hour);
      stream.next(m.minute);
      stream.next(m.seconds);
    }

    ROS_DECLARE_ALLINONE_SERIALIZER
  }; // struct TimeDate_

} // namespace serialization
} // namespace ros

namespace ros
{
namespace message_operations
{

template<class ContainerAllocator>
struct Printer< ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator> >
{
  template<typename Stream> static void stream(Stream& s, const std::string& indent, const ::turtlebot_transport_flexmansys::TimeDate_<ContainerAllocator>& v)
  {
    s << indent << "year: ";
    Printer<int32_t>::stream(s, indent + "  ", v.year);
    s << indent << "month: ";
    Printer<int32_t>::stream(s, indent + "  ", v.month);
    s << indent << "day: ";
    Printer<int32_t>::stream(s, indent + "  ", v.day);
    s << indent << "hour: ";
    Printer<int32_t>::stream(s, indent + "  ", v.hour);
    s << indent << "minute: ";
    Printer<int32_t>::stream(s, indent + "  ", v.minute);
    s << indent << "seconds: ";
    Printer<int32_t>::stream(s, indent + "  ", v.seconds);
  }
};

} // namespace message_operations
} // namespace ros

#endif // TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_TIMEDATE_H
