# generated from genmsg/cmake/pkg-genmsg.cmake.em

message(STATUS "turtlebot_transport_flexmansys: 6 messages, 1 services")

set(MSG_I_FLAGS "-Iturtlebot_transport_flexmansys:/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg;-Istd_msgs:/opt/ros/kinetic/share/std_msgs/cmake/../msg")

# Find all generators
find_package(gencpp REQUIRED)
find_package(geneus REQUIRED)
find_package(genjava REQUIRED)
find_package(genlisp REQUIRED)
find_package(gennodejs REQUIRED)
find_package(genpy REQUIRED)

add_custom_target(turtlebot_transport_flexmansys_generate_messages ALL)

# verify that message/service dependencies have not changed since configure



get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" "turtlebot_transport_flexmansys/KobukiGeneral:turtlebot_transport_flexmansys/KobukiPosition:turtlebot_transport_flexmansys/KobukiObstacle:turtlebot_transport_flexmansys/TimeDate"
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_custom_target(_turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "turtlebot_transport_flexmansys" "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" ""
)

#
#  langs = gencpp;geneus;genjava;genlisp;gennodejs;genpy
#

### Section generating for lang: gencpp
### Generating Messages
_generate_msg_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Services
_generate_srv_cpp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Module File
_generate_module_cpp(turtlebot_transport_flexmansys
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
  "${ALL_GEN_OUTPUT_FILES_cpp}"
)

add_custom_target(turtlebot_transport_flexmansys_generate_messages_cpp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_cpp}
)
add_dependencies(turtlebot_transport_flexmansys_generate_messages turtlebot_transport_flexmansys_generate_messages_cpp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(turtlebot_transport_flexmansys_gencpp)
add_dependencies(turtlebot_transport_flexmansys_gencpp turtlebot_transport_flexmansys_generate_messages_cpp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS turtlebot_transport_flexmansys_generate_messages_cpp)

### Section generating for lang: geneus
### Generating Messages
_generate_msg_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Services
_generate_srv_eus(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Module File
_generate_module_eus(turtlebot_transport_flexmansys
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
  "${ALL_GEN_OUTPUT_FILES_eus}"
)

add_custom_target(turtlebot_transport_flexmansys_generate_messages_eus
  DEPENDS ${ALL_GEN_OUTPUT_FILES_eus}
)
add_dependencies(turtlebot_transport_flexmansys_generate_messages turtlebot_transport_flexmansys_generate_messages_eus)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(turtlebot_transport_flexmansys_geneus)
add_dependencies(turtlebot_transport_flexmansys_geneus turtlebot_transport_flexmansys_generate_messages_eus)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS turtlebot_transport_flexmansys_generate_messages_eus)

### Section generating for lang: genjava
### Generating Messages
_generate_msg_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Services
_generate_srv_java(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Module File
_generate_module_java(turtlebot_transport_flexmansys
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
  "${ALL_GEN_OUTPUT_FILES_java}"
)

add_custom_target(turtlebot_transport_flexmansys_generate_messages_java
  DEPENDS ${ALL_GEN_OUTPUT_FILES_java}
)
add_dependencies(turtlebot_transport_flexmansys_generate_messages turtlebot_transport_flexmansys_generate_messages_java)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_java _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(turtlebot_transport_flexmansys_genjava)
add_dependencies(turtlebot_transport_flexmansys_genjava turtlebot_transport_flexmansys_generate_messages_java)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS turtlebot_transport_flexmansys_generate_messages_java)

### Section generating for lang: genlisp
### Generating Messages
_generate_msg_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Services
_generate_srv_lisp(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Module File
_generate_module_lisp(turtlebot_transport_flexmansys
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
  "${ALL_GEN_OUTPUT_FILES_lisp}"
)

add_custom_target(turtlebot_transport_flexmansys_generate_messages_lisp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_lisp}
)
add_dependencies(turtlebot_transport_flexmansys_generate_messages turtlebot_transport_flexmansys_generate_messages_lisp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(turtlebot_transport_flexmansys_genlisp)
add_dependencies(turtlebot_transport_flexmansys_genlisp turtlebot_transport_flexmansys_generate_messages_lisp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS turtlebot_transport_flexmansys_generate_messages_lisp)

### Section generating for lang: gennodejs
### Generating Messages
_generate_msg_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Services
_generate_srv_nodejs(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Module File
_generate_module_nodejs(turtlebot_transport_flexmansys
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
  "${ALL_GEN_OUTPUT_FILES_nodejs}"
)

add_custom_target(turtlebot_transport_flexmansys_generate_messages_nodejs
  DEPENDS ${ALL_GEN_OUTPUT_FILES_nodejs}
)
add_dependencies(turtlebot_transport_flexmansys_generate_messages turtlebot_transport_flexmansys_generate_messages_nodejs)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(turtlebot_transport_flexmansys_gennodejs)
add_dependencies(turtlebot_transport_flexmansys_gennodejs turtlebot_transport_flexmansys_generate_messages_nodejs)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS turtlebot_transport_flexmansys_generate_messages_nodejs)

### Section generating for lang: genpy
### Generating Messages
_generate_msg_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)
_generate_msg_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Services
_generate_srv_py(turtlebot_transport_flexmansys
  "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
)

### Generating Module File
_generate_module_py(turtlebot_transport_flexmansys
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
  "${ALL_GEN_OUTPUT_FILES_py}"
)

add_custom_target(turtlebot_transport_flexmansys_generate_messages_py
  DEPENDS ${ALL_GEN_OUTPUT_FILES_py}
)
add_dependencies(turtlebot_transport_flexmansys_generate_messages turtlebot_transport_flexmansys_generate_messages_py)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/srv/TransportServiceMessage.srv" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TimeDate.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/catkin_ws/src/turtlebot_transport_flexmansys/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(turtlebot_transport_flexmansys_generate_messages_py _turtlebot_transport_flexmansys_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(turtlebot_transport_flexmansys_genpy)
add_dependencies(turtlebot_transport_flexmansys_genpy turtlebot_transport_flexmansys_generate_messages_py)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS turtlebot_transport_flexmansys_generate_messages_py)



if(gencpp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/turtlebot_transport_flexmansys
    DESTINATION ${gencpp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_cpp)
  add_dependencies(turtlebot_transport_flexmansys_generate_messages_cpp std_msgs_generate_messages_cpp)
endif()

if(geneus_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/turtlebot_transport_flexmansys
    DESTINATION ${geneus_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_eus)
  add_dependencies(turtlebot_transport_flexmansys_generate_messages_eus std_msgs_generate_messages_eus)
endif()

if(genjava_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/turtlebot_transport_flexmansys
    DESTINATION ${genjava_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_java)
  add_dependencies(turtlebot_transport_flexmansys_generate_messages_java std_msgs_generate_messages_java)
endif()

if(genlisp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/turtlebot_transport_flexmansys
    DESTINATION ${genlisp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_lisp)
  add_dependencies(turtlebot_transport_flexmansys_generate_messages_lisp std_msgs_generate_messages_lisp)
endif()

if(gennodejs_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/turtlebot_transport_flexmansys
    DESTINATION ${gennodejs_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_nodejs)
  add_dependencies(turtlebot_transport_flexmansys_generate_messages_nodejs std_msgs_generate_messages_nodejs)
endif()

if(genpy_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys)
  install(CODE "execute_process(COMMAND \"/usr/bin/python2\" -m compileall \"${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys\")")
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/turtlebot_transport_flexmansys
    DESTINATION ${genpy_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_py)
  add_dependencies(turtlebot_transport_flexmansys_generate_messages_py std_msgs_generate_messages_py)
endif()
