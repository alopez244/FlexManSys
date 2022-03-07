# generated from genmsg/cmake/pkg-genmsg.cmake.em

message(STATUS "Project: 1 messages, 0 services")

set(MSG_I_FLAGS "-IProject:/home/borjartime/rosjava_ws/src/custom_messages/msg;-Istd_msgs:/opt/ros/kinetic/share/std_msgs/cmake/../msg")

# Find all generators
find_package(gencpp REQUIRED)
find_package(geneus REQUIRED)
find_package(genjava REQUIRED)
find_package(genlisp REQUIRED)
find_package(gennodejs REQUIRED)
find_package(genpy REQUIRED)

add_custom_target(Project_generate_messages ALL)

# verify that message/service dependencies have not changed since configure



get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_custom_target(_Project_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "Project" "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" ""
)

#
#  langs = gencpp;geneus;genjava;genlisp;gennodejs;genpy
#

### Section generating for lang: gencpp
### Generating Messages
_generate_msg_cpp(Project
  "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/Project
)

### Generating Services

### Generating Module File
_generate_module_cpp(Project
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/Project
  "${ALL_GEN_OUTPUT_FILES_cpp}"
)

add_custom_target(Project_generate_messages_cpp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_cpp}
)
add_dependencies(Project_generate_messages Project_generate_messages_cpp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(Project_generate_messages_cpp _Project_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(Project_gencpp)
add_dependencies(Project_gencpp Project_generate_messages_cpp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS Project_generate_messages_cpp)

### Section generating for lang: geneus
### Generating Messages
_generate_msg_eus(Project
  "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/Project
)

### Generating Services

### Generating Module File
_generate_module_eus(Project
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/Project
  "${ALL_GEN_OUTPUT_FILES_eus}"
)

add_custom_target(Project_generate_messages_eus
  DEPENDS ${ALL_GEN_OUTPUT_FILES_eus}
)
add_dependencies(Project_generate_messages Project_generate_messages_eus)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(Project_generate_messages_eus _Project_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(Project_geneus)
add_dependencies(Project_geneus Project_generate_messages_eus)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS Project_generate_messages_eus)

### Section generating for lang: genjava
### Generating Messages
_generate_msg_java(Project
  "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/Project
)

### Generating Services

### Generating Module File
_generate_module_java(Project
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/Project
  "${ALL_GEN_OUTPUT_FILES_java}"
)

add_custom_target(Project_generate_messages_java
  DEPENDS ${ALL_GEN_OUTPUT_FILES_java}
)
add_dependencies(Project_generate_messages Project_generate_messages_java)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(Project_generate_messages_java _Project_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(Project_genjava)
add_dependencies(Project_genjava Project_generate_messages_java)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS Project_generate_messages_java)

### Section generating for lang: genlisp
### Generating Messages
_generate_msg_lisp(Project
  "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/Project
)

### Generating Services

### Generating Module File
_generate_module_lisp(Project
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/Project
  "${ALL_GEN_OUTPUT_FILES_lisp}"
)

add_custom_target(Project_generate_messages_lisp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_lisp}
)
add_dependencies(Project_generate_messages Project_generate_messages_lisp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(Project_generate_messages_lisp _Project_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(Project_genlisp)
add_dependencies(Project_genlisp Project_generate_messages_lisp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS Project_generate_messages_lisp)

### Section generating for lang: gennodejs
### Generating Messages
_generate_msg_nodejs(Project
  "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/Project
)

### Generating Services

### Generating Module File
_generate_module_nodejs(Project
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/Project
  "${ALL_GEN_OUTPUT_FILES_nodejs}"
)

add_custom_target(Project_generate_messages_nodejs
  DEPENDS ${ALL_GEN_OUTPUT_FILES_nodejs}
)
add_dependencies(Project_generate_messages Project_generate_messages_nodejs)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(Project_generate_messages_nodejs _Project_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(Project_gennodejs)
add_dependencies(Project_gennodejs Project_generate_messages_nodejs)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS Project_generate_messages_nodejs)

### Section generating for lang: genpy
### Generating Messages
_generate_msg_py(Project
  "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/Project
)

### Generating Services

### Generating Module File
_generate_module_py(Project
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/Project
  "${ALL_GEN_OUTPUT_FILES_py}"
)

add_custom_target(Project_generate_messages_py
  DEPENDS ${ALL_GEN_OUTPUT_FILES_py}
)
add_dependencies(Project_generate_messages Project_generate_messages_py)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/custom_messages/msg/Prueba.msg" NAME_WE)
add_dependencies(Project_generate_messages_py _Project_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(Project_genpy)
add_dependencies(Project_genpy Project_generate_messages_py)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS Project_generate_messages_py)



if(gencpp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/Project)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/Project
    DESTINATION ${gencpp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_cpp)
  add_dependencies(Project_generate_messages_cpp std_msgs_generate_messages_cpp)
endif()

if(geneus_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/Project)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/Project
    DESTINATION ${geneus_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_eus)
  add_dependencies(Project_generate_messages_eus std_msgs_generate_messages_eus)
endif()

if(genjava_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/Project)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/Project
    DESTINATION ${genjava_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_java)
  add_dependencies(Project_generate_messages_java std_msgs_generate_messages_java)
endif()

if(genlisp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/Project)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/Project
    DESTINATION ${genlisp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_lisp)
  add_dependencies(Project_generate_messages_lisp std_msgs_generate_messages_lisp)
endif()

if(gennodejs_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/Project)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/Project
    DESTINATION ${gennodejs_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_nodejs)
  add_dependencies(Project_generate_messages_nodejs std_msgs_generate_messages_nodejs)
endif()

if(genpy_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/Project)
  install(CODE "execute_process(COMMAND \"/usr/bin/python2\" -m compileall \"${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/Project\")")
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/Project
    DESTINATION ${genpy_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_py)
  add_dependencies(Project_generate_messages_py std_msgs_generate_messages_py)
endif()
