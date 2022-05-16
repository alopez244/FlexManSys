# generated from genmsg/cmake/pkg-genmsg.cmake.em

message(STATUS "custom_messages: 7 messages, 0 services")

set(MSG_I_FLAGS "-Icustom_messages:/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg;-Istd_msgs:/opt/ros/kinetic/share/std_msgs/cmake/../msg")

# Find all generators
find_package(gencpp REQUIRED)
find_package(geneus REQUIRED)
find_package(genjava REQUIRED)
find_package(genlisp REQUIRED)
find_package(gennodejs REQUIRED)
find_package(genpy REQUIRED)

add_custom_target(custom_messages_generate_messages ALL)

# verify that message/service dependencies have not changed since configure



get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" "custom_messages/TimeDate:custom_messages/KobukiObstacle:custom_messages/KobukiPosition:custom_messages/KobukiGeneral"
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" ""
)

get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_custom_target(_custom_messages_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "custom_messages" "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" ""
)

#
#  langs = gencpp;geneus;genjava;genlisp;gennodejs;genpy
#

### Section generating for lang: gencpp
### Generating Messages
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)
_generate_msg_cpp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
)

### Generating Services

### Generating Module File
_generate_module_cpp(custom_messages
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
  "${ALL_GEN_OUTPUT_FILES_cpp}"
)

add_custom_target(custom_messages_generate_messages_cpp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_cpp}
)
add_dependencies(custom_messages_generate_messages custom_messages_generate_messages_cpp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_cpp _custom_messages_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(custom_messages_gencpp)
add_dependencies(custom_messages_gencpp custom_messages_generate_messages_cpp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS custom_messages_generate_messages_cpp)

### Section generating for lang: geneus
### Generating Messages
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)
_generate_msg_eus(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
)

### Generating Services

### Generating Module File
_generate_module_eus(custom_messages
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
  "${ALL_GEN_OUTPUT_FILES_eus}"
)

add_custom_target(custom_messages_generate_messages_eus
  DEPENDS ${ALL_GEN_OUTPUT_FILES_eus}
)
add_dependencies(custom_messages_generate_messages custom_messages_generate_messages_eus)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_eus _custom_messages_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(custom_messages_geneus)
add_dependencies(custom_messages_geneus custom_messages_generate_messages_eus)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS custom_messages_generate_messages_eus)

### Section generating for lang: genjava
### Generating Messages
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)
_generate_msg_java(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
)

### Generating Services

### Generating Module File
_generate_module_java(custom_messages
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
  "${ALL_GEN_OUTPUT_FILES_java}"
)

add_custom_target(custom_messages_generate_messages_java
  DEPENDS ${ALL_GEN_OUTPUT_FILES_java}
)
add_dependencies(custom_messages_generate_messages custom_messages_generate_messages_java)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_java _custom_messages_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(custom_messages_genjava)
add_dependencies(custom_messages_genjava custom_messages_generate_messages_java)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS custom_messages_generate_messages_java)

### Section generating for lang: genlisp
### Generating Messages
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)
_generate_msg_lisp(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
)

### Generating Services

### Generating Module File
_generate_module_lisp(custom_messages
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
  "${ALL_GEN_OUTPUT_FILES_lisp}"
)

add_custom_target(custom_messages_generate_messages_lisp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_lisp}
)
add_dependencies(custom_messages_generate_messages custom_messages_generate_messages_lisp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_lisp _custom_messages_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(custom_messages_genlisp)
add_dependencies(custom_messages_genlisp custom_messages_generate_messages_lisp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS custom_messages_generate_messages_lisp)

### Section generating for lang: gennodejs
### Generating Messages
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)
_generate_msg_nodejs(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
)

### Generating Services

### Generating Module File
_generate_module_nodejs(custom_messages
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
  "${ALL_GEN_OUTPUT_FILES_nodejs}"
)

add_custom_target(custom_messages_generate_messages_nodejs
  DEPENDS ${ALL_GEN_OUTPUT_FILES_nodejs}
)
add_dependencies(custom_messages_generate_messages custom_messages_generate_messages_nodejs)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_nodejs _custom_messages_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(custom_messages_gennodejs)
add_dependencies(custom_messages_gennodejs custom_messages_generate_messages_nodejs)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS custom_messages_generate_messages_nodejs)

### Section generating for lang: genpy
### Generating Messages
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
  "${MSG_I_FLAGS}"
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg;/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)
_generate_msg_py(custom_messages
  "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
)

### Generating Services

### Generating Module File
_generate_module_py(custom_messages
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
  "${ALL_GEN_OUTPUT_FILES_py}"
)

add_custom_target(custom_messages_generate_messages_py
  DEPENDS ${ALL_GEN_OUTPUT_FILES_py}
)
add_dependencies(custom_messages_generate_messages custom_messages_generate_messages_py)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})
get_filename_component(_filename "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg" NAME_WE)
add_dependencies(custom_messages_generate_messages_py _custom_messages_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(custom_messages_genpy)
add_dependencies(custom_messages_genpy custom_messages_generate_messages_py)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS custom_messages_generate_messages_py)



if(gencpp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/custom_messages
    DESTINATION ${gencpp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_cpp)
  add_dependencies(custom_messages_generate_messages_cpp std_msgs_generate_messages_cpp)
endif()

if(geneus_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/custom_messages
    DESTINATION ${geneus_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_eus)
  add_dependencies(custom_messages_generate_messages_eus std_msgs_generate_messages_eus)
endif()

if(genjava_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/custom_messages
    DESTINATION ${genjava_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_java)
  add_dependencies(custom_messages_generate_messages_java std_msgs_generate_messages_java)
endif()

if(genlisp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/custom_messages
    DESTINATION ${genlisp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_lisp)
  add_dependencies(custom_messages_generate_messages_lisp std_msgs_generate_messages_lisp)
endif()

if(gennodejs_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/custom_messages
    DESTINATION ${gennodejs_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_nodejs)
  add_dependencies(custom_messages_generate_messages_nodejs std_msgs_generate_messages_nodejs)
endif()

if(genpy_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages)
  install(CODE "execute_process(COMMAND \"/usr/bin/python2\" -m compileall \"${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages\")")
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/custom_messages
    DESTINATION ${genpy_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_py)
  add_dependencies(custom_messages_generate_messages_py std_msgs_generate_messages_py)
endif()
