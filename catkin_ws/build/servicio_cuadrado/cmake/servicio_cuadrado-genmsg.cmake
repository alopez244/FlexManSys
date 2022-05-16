# generated from genmsg/cmake/pkg-genmsg.cmake.em

message(STATUS "servicio_cuadrado: 0 messages, 1 services")

set(MSG_I_FLAGS "-Istd_msgs:/opt/ros/kinetic/share/std_msgs/cmake/../msg")

# Find all generators
find_package(gencpp REQUIRED)
find_package(geneus REQUIRED)
find_package(genjava REQUIRED)
find_package(genlisp REQUIRED)
find_package(gennodejs REQUIRED)
find_package(genpy REQUIRED)

add_custom_target(servicio_cuadrado_generate_messages ALL)

# verify that message/service dependencies have not changed since configure



get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_custom_target(_servicio_cuadrado_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "servicio_cuadrado" "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" ""
)

#
#  langs = gencpp;geneus;genjava;genlisp;gennodejs;genpy
#

### Section generating for lang: gencpp
### Generating Messages

### Generating Services
_generate_srv_cpp(servicio_cuadrado
  "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/servicio_cuadrado
)

### Generating Module File
_generate_module_cpp(servicio_cuadrado
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/servicio_cuadrado
  "${ALL_GEN_OUTPUT_FILES_cpp}"
)

add_custom_target(servicio_cuadrado_generate_messages_cpp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_cpp}
)
add_dependencies(servicio_cuadrado_generate_messages servicio_cuadrado_generate_messages_cpp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_dependencies(servicio_cuadrado_generate_messages_cpp _servicio_cuadrado_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(servicio_cuadrado_gencpp)
add_dependencies(servicio_cuadrado_gencpp servicio_cuadrado_generate_messages_cpp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS servicio_cuadrado_generate_messages_cpp)

### Section generating for lang: geneus
### Generating Messages

### Generating Services
_generate_srv_eus(servicio_cuadrado
  "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/servicio_cuadrado
)

### Generating Module File
_generate_module_eus(servicio_cuadrado
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/servicio_cuadrado
  "${ALL_GEN_OUTPUT_FILES_eus}"
)

add_custom_target(servicio_cuadrado_generate_messages_eus
  DEPENDS ${ALL_GEN_OUTPUT_FILES_eus}
)
add_dependencies(servicio_cuadrado_generate_messages servicio_cuadrado_generate_messages_eus)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_dependencies(servicio_cuadrado_generate_messages_eus _servicio_cuadrado_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(servicio_cuadrado_geneus)
add_dependencies(servicio_cuadrado_geneus servicio_cuadrado_generate_messages_eus)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS servicio_cuadrado_generate_messages_eus)

### Section generating for lang: genjava
### Generating Messages

### Generating Services
_generate_srv_java(servicio_cuadrado
  "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/servicio_cuadrado
)

### Generating Module File
_generate_module_java(servicio_cuadrado
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/servicio_cuadrado
  "${ALL_GEN_OUTPUT_FILES_java}"
)

add_custom_target(servicio_cuadrado_generate_messages_java
  DEPENDS ${ALL_GEN_OUTPUT_FILES_java}
)
add_dependencies(servicio_cuadrado_generate_messages servicio_cuadrado_generate_messages_java)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_dependencies(servicio_cuadrado_generate_messages_java _servicio_cuadrado_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(servicio_cuadrado_genjava)
add_dependencies(servicio_cuadrado_genjava servicio_cuadrado_generate_messages_java)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS servicio_cuadrado_generate_messages_java)

### Section generating for lang: genlisp
### Generating Messages

### Generating Services
_generate_srv_lisp(servicio_cuadrado
  "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/servicio_cuadrado
)

### Generating Module File
_generate_module_lisp(servicio_cuadrado
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/servicio_cuadrado
  "${ALL_GEN_OUTPUT_FILES_lisp}"
)

add_custom_target(servicio_cuadrado_generate_messages_lisp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_lisp}
)
add_dependencies(servicio_cuadrado_generate_messages servicio_cuadrado_generate_messages_lisp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_dependencies(servicio_cuadrado_generate_messages_lisp _servicio_cuadrado_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(servicio_cuadrado_genlisp)
add_dependencies(servicio_cuadrado_genlisp servicio_cuadrado_generate_messages_lisp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS servicio_cuadrado_generate_messages_lisp)

### Section generating for lang: gennodejs
### Generating Messages

### Generating Services
_generate_srv_nodejs(servicio_cuadrado
  "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/servicio_cuadrado
)

### Generating Module File
_generate_module_nodejs(servicio_cuadrado
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/servicio_cuadrado
  "${ALL_GEN_OUTPUT_FILES_nodejs}"
)

add_custom_target(servicio_cuadrado_generate_messages_nodejs
  DEPENDS ${ALL_GEN_OUTPUT_FILES_nodejs}
)
add_dependencies(servicio_cuadrado_generate_messages servicio_cuadrado_generate_messages_nodejs)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_dependencies(servicio_cuadrado_generate_messages_nodejs _servicio_cuadrado_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(servicio_cuadrado_gennodejs)
add_dependencies(servicio_cuadrado_gennodejs servicio_cuadrado_generate_messages_nodejs)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS servicio_cuadrado_generate_messages_nodejs)

### Section generating for lang: genpy
### Generating Messages

### Generating Services
_generate_srv_py(servicio_cuadrado
  "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/servicio_cuadrado
)

### Generating Module File
_generate_module_py(servicio_cuadrado
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/servicio_cuadrado
  "${ALL_GEN_OUTPUT_FILES_py}"
)

add_custom_target(servicio_cuadrado_generate_messages_py
  DEPENDS ${ALL_GEN_OUTPUT_FILES_py}
)
add_dependencies(servicio_cuadrado_generate_messages servicio_cuadrado_generate_messages_py)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/catkin_ws/src/servicio_cuadrado/srv/MyCustomServiceMessage.srv" NAME_WE)
add_dependencies(servicio_cuadrado_generate_messages_py _servicio_cuadrado_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(servicio_cuadrado_genpy)
add_dependencies(servicio_cuadrado_genpy servicio_cuadrado_generate_messages_py)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS servicio_cuadrado_generate_messages_py)



if(gencpp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/servicio_cuadrado)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/servicio_cuadrado
    DESTINATION ${gencpp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_cpp)
  add_dependencies(servicio_cuadrado_generate_messages_cpp std_msgs_generate_messages_cpp)
endif()

if(geneus_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/servicio_cuadrado)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/servicio_cuadrado
    DESTINATION ${geneus_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_eus)
  add_dependencies(servicio_cuadrado_generate_messages_eus std_msgs_generate_messages_eus)
endif()

if(genjava_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/servicio_cuadrado)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/servicio_cuadrado
    DESTINATION ${genjava_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_java)
  add_dependencies(servicio_cuadrado_generate_messages_java std_msgs_generate_messages_java)
endif()

if(genlisp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/servicio_cuadrado)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/servicio_cuadrado
    DESTINATION ${genlisp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_lisp)
  add_dependencies(servicio_cuadrado_generate_messages_lisp std_msgs_generate_messages_lisp)
endif()

if(gennodejs_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/servicio_cuadrado)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/servicio_cuadrado
    DESTINATION ${gennodejs_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_nodejs)
  add_dependencies(servicio_cuadrado_generate_messages_nodejs std_msgs_generate_messages_nodejs)
endif()

if(genpy_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/servicio_cuadrado)
  install(CODE "execute_process(COMMAND \"/usr/bin/python2\" -m compileall \"${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/servicio_cuadrado\")")
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/servicio_cuadrado
    DESTINATION ${genpy_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_py)
  add_dependencies(servicio_cuadrado_generate_messages_py std_msgs_generate_messages_py)
endif()
