// Generated by gencpp from file turtlebot_transport_flexmansys/TransportPrivateState.msg
// DO NOT EDIT!


#ifndef TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_TRANSPORTPRIVATESTATE_H
#define TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_TRANSPORTPRIVATESTATE_H


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
struct TransportPrivateState_
{
  typedef TransportPrivateState_<ContainerAllocator> Type;

  TransportPrivateState_()
    : transport_state()
    , transport_docked(false)  {
    }
  TransportPrivateState_(const ContainerAllocator& _alloc)
    : transport_state(_alloc)
    , transport_docked(false)  {
  (void)_alloc;
    }



   typedef std::basic_string<char, std::char_traits<char>, typename ContainerAllocator::template rebind<char>::other >  _transport_state_type;
  _transport_state_type transport_state;

   typedef uint8_t _transport_docked_type;
  _transport_docked_type transport_docked;





  typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> > Ptr;
  typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> const> ConstPtr;

}; // struct TransportPrivateState_

typedef ::turtlebot_transport_flexmansys::TransportPrivateState_<std::allocator<void> > TransportPrivateState;

typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TransportPrivateState > TransportPrivateStatePtr;
typedef boost::shared_ptr< ::turtlebot_transport_flexmansys::TransportPrivateState const> TransportPrivateStateConstPtr;

// constants requiring out of line definition



template<typename ContainerAllocator>
std::ostream& operator<<(std::ostream& s, const ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> & v)
{
ros::message_operations::Printer< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >::stream(s, "", v);
return s;
}

} // namespace turtlebot_transport_flexmansys

namespace ros
{
namespace message_traits
{



// BOOLTRAITS {'IsFixedSize': False, 'IsMessage': True, 'HasHeader': False}
// {'turtlebot_transport_flexmansys': ['/home/borjartime/catkin_ws/src/turtlebot_transport_flexmansys/msg'], 'std_msgs': ['/opt/ros/kinetic/share/std_msgs/cmake/../msg']}

// !!!!!!!!!!! ['__class__', '__delattr__', '__dict__', '__doc__', '__eq__', '__format__', '__getattribute__', '__hash__', '__init__', '__module__', '__ne__', '__new__', '__reduce__', '__reduce_ex__', '__repr__', '__setattr__', '__sizeof__', '__str__', '__subclasshook__', '__weakref__', '_parsed_fields', 'constants', 'fields', 'full_name', 'has_header', 'header_present', 'names', 'package', 'parsed_fields', 'short_name', 'text', 'types']




template <class ContainerAllocator>
struct IsFixedSize< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
  : FalseType
  { };

template <class ContainerAllocator>
struct IsFixedSize< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> const>
  : FalseType
  { };

template <class ContainerAllocator>
struct IsMessage< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
  : TrueType
  { };

template <class ContainerAllocator>
struct IsMessage< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> const>
  : TrueType
  { };

template <class ContainerAllocator>
struct HasHeader< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
  : FalseType
  { };

template <class ContainerAllocator>
struct HasHeader< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> const>
  : FalseType
  { };


template<class ContainerAllocator>
struct MD5Sum< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
{
  static const char* value()
  {
    return "670f3bb83518dec928ea7b995f722845";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator>&) { return value(); }
  static const uint64_t static_value1 = 0x670f3bb83518dec9ULL;
  static const uint64_t static_value2 = 0x28ea7b995f722845ULL;
};

template<class ContainerAllocator>
struct DataType< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
{
  static const char* value()
  {
    return "turtlebot_transport_flexmansys/TransportPrivateState";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator>&) { return value(); }
};

template<class ContainerAllocator>
struct Definition< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
{
  static const char* value()
  {
    return "string transport_state\n\
bool transport_docked\n\
";
  }

  static const char* value(const ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator>&) { return value(); }
};

} // namespace message_traits
} // namespace ros

namespace ros
{
namespace serialization
{

  template<class ContainerAllocator> struct Serializer< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
  {
    template<typename Stream, typename T> inline static void allInOne(Stream& stream, T m)
    {
      stream.next(m.transport_state);
      stream.next(m.transport_docked);
    }

    ROS_DECLARE_ALLINONE_SERIALIZER
  }; // struct TransportPrivateState_

} // namespace serialization
} // namespace ros

namespace ros
{
namespace message_operations
{

template<class ContainerAllocator>
struct Printer< ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator> >
{
  template<typename Stream> static void stream(Stream& s, const std::string& indent, const ::turtlebot_transport_flexmansys::TransportPrivateState_<ContainerAllocator>& v)
  {
    s << indent << "transport_state: ";
    Printer<std::basic_string<char, std::char_traits<char>, typename ContainerAllocator::template rebind<char>::other > >::stream(s, indent + "  ", v.transport_state);
    s << indent << "transport_docked: ";
    Printer<uint8_t>::stream(s, indent + "  ", v.transport_docked);
  }
};

} // namespace message_operations
} // namespace ros

#endif // TURTLEBOT_TRANSPORT_FLEXMANSYS_MESSAGE_TRANSPORTPRIVATESTATE_H
