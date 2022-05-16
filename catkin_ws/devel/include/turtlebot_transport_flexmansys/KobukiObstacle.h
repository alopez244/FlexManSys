// Generated by gencpp from file turtlebot_transport_flexmansys/KobukiObstacle.msg
// DO NOT EDIT!


#ifndef TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_KOBUKIOBSTACLE_H
#define TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_KOBUKIOBSTACLE_H


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
struct KobukiObstacle_
{
  typedef KobukiObstacle_<ContainerAllocator> Type;

  KobukiObstacle_()
    : detected_obstacle_bumper(false)
    , detected_obstacle_camera(false)  {
    }
  KobukiObstacle_(const ContainerAllocator& _alloc)
    : detected_obstacle_bumper(false)
    , detected_obstacle_camera(false)  {
  (void)_alloc;
    }



   typedef uint8_t _detected_obstacle_bumper_type;
  _detected_obstacle_bumper_type detected_obstacle_bumper;

   typedef uint8_t _detected_obstacle_camera_type;
  _detected_obstacle_camera_type detected_obstacle_camera;





  typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> > Ptr;
  typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> const> ConstPtr;

}; // struct KobukiObstacle_

typedef ::turtlebot_transport_flexmansys::KobukiObstacle_<std::allocator<void> > KobukiObstacle;

typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::KobukiObstacle > KobukiObstaclePtr;
typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::KobukiObstacle const> KobukiObstacleConstPtr;

// constants requiring out of line definition



template<typename ContainerAllocator>
std::ostream& operator<<(std::ostream& s, const ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> & v)
{
ros::message_operations::Printer< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >::stream(s, "", v);
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
struct IsFixedSize< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
  : TrueType
  { };

template <class ContainerAllocator>
struct IsFixedSize< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> const>
  : TrueType
  { };

template <class ContainerAllocator>
struct IsMessage< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
  : TrueType
  { };

template <class ContainerAllocator>
struct IsMessage< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> const>
  : TrueType
  { };

template <class ContainerAllocator>
struct HasHeader< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
  : FalseType
  { };

template <class ContainerAllocator>
struct HasHeader< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> const>
  : FalseType
  { };


template<class ContainerAllocator>
struct MD5Sum< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
{
  static const char* value()
  {
    return "2aef80677f5e7bb119c67140618dc301";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator>&) { return value(); }
  static const uint64_t static_value1 = 0x2aef80677f5e7bb1ULL;
  static const uint64_t static_value2 = 0x19c67140618dc301ULL;
};

template<class ContainerAllocator>
struct DataType< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
{
  static const char* value()
  {
    return "turtlebot_transport_flexmansys/KobukiObstacle";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator>&) { return value(); }
};

template<class ContainerAllocator>
struct Definition< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
{
  static const char* value()
  {
    return "bool detected_obstacle_bumper\n\
bool detected_obstacle_camera\n\
";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator>&) { return value(); }
};

} // namespace message_traits
} // namespace ros

namespace ros
{
namespace serialization
{

  template<class ContainerAllocator> struct Serializer< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
  {
    template<typename Stream, typename T> inline static void allInOne(Stream& stream, T m)
    {
      stream.next(m.detected_obstacle_bumper);
      stream.next(m.detected_obstacle_camera);
    }

    ROS_DECLARE_ALLINONE_SERIALIZER
  }; // struct KobukiObstacle_

} // namespace serialization
} // namespace ros

namespace ros
{
namespace message_operations
{

template<class ContainerAllocator>
struct Printer< ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator> >
{
  template<typename Stream> static void stream(Stream& s, const std::string& indent, const ::turtlebot_transport_flexmansys::KobukiObstacle_<ContainerAllocator>& v)
  {
    s << indent << "detected_obstacle_bumper: ";
    Printer<uint8_t>::stream(s, indent + "  ", v.detected_obstacle_bumper);
    s << indent << "detected_obstacle_camera: ";
    Printer<uint8_t>::stream(s, indent + "  ", v.detected_obstacle_camera);
  }
};

} // namespace message_operations
} // namespace ros

#endif // TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_KOBUKIOBSTACLE_H