# generated from genmsg/cmake/pkg-genmsg.cmake.em

message(STATUS "nombre_paquete: 1 messages, 0 services")

set(MSG_I_FLAGS "-Inombre_paquete:/home/borjartime/rosjava_ws/src/nombre_paquete/msg;-Istd_msgs:/opt/ros/kinetic/share/std_msgs/cmake/../msg")

# Find all generators
find_package(gencpp REQUIRED)
find_package(geneus REQUIRED)
find_package(genjava REQUIRED)
find_package(genlisp REQUIRED)
find_package(gennodejs REQUIRED)
find_package(genpy REQUIRED)

add_custom_target(nombre_paquete_generate_messages ALL)

# verify that message/service dependencies have not changed since configure



get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_custom_target(_nombre_paquete_generate_messages_check_deps_${_filename}
  COMMAND ${CATKIN_ENV} ${PYTHON_EXECUTABLE} ${GENMSG_CHECK_DEPS_SCRIPT} "nombre_paquete" "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" ""
)

#
#  langs = gencpp;geneus;genjava;genlisp;gennodejs;genpy
#

### Section generating for lang: gencpp
### Generating Messages
_generate_msg_cpp(nombre_paquete
  "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/nombre_paquete
)

### Generating Services

### Generating Module File
_generate_module_cpp(nombre_paquete
  ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/nombre_paquete
  "${ALL_GEN_OUTPUT_FILES_cpp}"
)

add_custom_target(nombre_paquete_generate_messages_cpp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_cpp}
)
add_dependencies(nombre_paquete_generate_messages nombre_paquete_generate_messages_cpp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_dependencies(nombre_paquete_generate_messages_cpp _nombre_paquete_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(nombre_paquete_gencpp)
add_dependencies(nombre_paquete_gencpp nombre_paquete_generate_messages_cpp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS nombre_paquete_generate_messages_cpp)

### Section generating for lang: geneus
### Generating Messages
_generate_msg_eus(nombre_paquete
  "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/nombre_paquete
)

### Generating Services

### Generating Module File
_generate_module_eus(nombre_paquete
  ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/nombre_paquete
  "${ALL_GEN_OUTPUT_FILES_eus}"
)

add_custom_target(nombre_paquete_generate_messages_eus
  DEPENDS ${ALL_GEN_OUTPUT_FILES_eus}
)
add_dependencies(nombre_paquete_generate_messages nombre_paquete_generate_messages_eus)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_dependencies(nombre_paquete_generate_messages_eus _nombre_paquete_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(nombre_paquete_geneus)
add_dependencies(nombre_paquete_geneus nombre_paquete_generate_messages_eus)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS nombre_paquete_generate_messages_eus)

### Section generating for lang: genjava
### Generating Messages
_generate_msg_java(nombre_paquete
  "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/nombre_paquete
)

### Generating Services

### Generating Module File
_generate_module_java(nombre_paquete
  ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/nombre_paquete
  "${ALL_GEN_OUTPUT_FILES_java}"
)

add_custom_target(nombre_paquete_generate_messages_java
  DEPENDS ${ALL_GEN_OUTPUT_FILES_java}
)
add_dependencies(nombre_paquete_generate_messages nombre_paquete_generate_messages_java)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_dependencies(nombre_paquete_generate_messages_java _nombre_paquete_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(nombre_paquete_genjava)
add_dependencies(nombre_paquete_genjava nombre_paquete_generate_messages_java)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS nombre_paquete_generate_messages_java)

### Section generating for lang: genlisp
### Generating Messages
_generate_msg_lisp(nombre_paquete
  "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/nombre_paquete
)

### Generating Services

### Generating Module File
_generate_module_lisp(nombre_paquete
  ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/nombre_paquete
  "${ALL_GEN_OUTPUT_FILES_lisp}"
)

add_custom_target(nombre_paquete_generate_messages_lisp
  DEPENDS ${ALL_GEN_OUTPUT_FILES_lisp}
)
add_dependencies(nombre_paquete_generate_messages nombre_paquete_generate_messages_lisp)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_dependencies(nombre_paquete_generate_messages_lisp _nombre_paquete_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(nombre_paquete_genlisp)
add_dependencies(nombre_paquete_genlisp nombre_paquete_generate_messages_lisp)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS nombre_paquete_generate_messages_lisp)

### Section generating for lang: gennodejs
### Generating Messages
_generate_msg_nodejs(nombre_paquete
  "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/nombre_paquete
)

### Generating Services

### Generating Module File
_generate_module_nodejs(nombre_paquete
  ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/nombre_paquete
  "${ALL_GEN_OUTPUT_FILES_nodejs}"
)

add_custom_target(nombre_paquete_generate_messages_nodejs
  DEPENDS ${ALL_GEN_OUTPUT_FILES_nodejs}
)
add_dependencies(nombre_paquete_generate_messages nombre_paquete_generate_messages_nodejs)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_dependencies(nombre_paquete_generate_messages_nodejs _nombre_paquete_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(nombre_paquete_gennodejs)
add_dependencies(nombre_paquete_gennodejs nombre_paquete_generate_messages_nodejs)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS nombre_paquete_generate_messages_nodejs)

### Section generating for lang: genpy
### Generating Messages
_generate_msg_py(nombre_paquete
  "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg"
  "${MSG_I_FLAGS}"
  ""
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/nombre_paquete
)

### Generating Services

### Generating Module File
_generate_module_py(nombre_paquete
  ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/nombre_paquete
  "${ALL_GEN_OUTPUT_FILES_py}"
)

add_custom_target(nombre_paquete_generate_messages_py
  DEPENDS ${ALL_GEN_OUTPUT_FILES_py}
)
add_dependencies(nombre_paquete_generate_messages nombre_paquete_generate_messages_py)

# add dependencies to all check dependencies targets
get_filename_component(_filename "/home/borjartime/rosjava_ws/src/nombre_paquete/msg/Prueba.msg" NAME_WE)
add_dependencies(nombre_paquete_generate_messages_py _nombre_paquete_generate_messages_check_deps_${_filename})

# target for backward compatibility
add_custom_target(nombre_paquete_genpy)
add_dependencies(nombre_paquete_genpy nombre_paquete_generate_messages_py)

# register target for catkin_package(EXPORTED_TARGETS)
list(APPEND ${PROJECT_NAME}_EXPORTED_TARGETS nombre_paquete_generate_messages_py)



if(gencpp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/nombre_paquete)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gencpp_INSTALL_DIR}/nombre_paquete
    DESTINATION ${gencpp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_cpp)
  add_dependencies(nombre_paquete_generate_messages_cpp std_msgs_generate_messages_cpp)
endif()

if(geneus_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/nombre_paquete)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${geneus_INSTALL_DIR}/nombre_paquete
    DESTINATION ${geneus_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_eus)
  add_dependencies(nombre_paquete_generate_messages_eus std_msgs_generate_messages_eus)
endif()

if(genjava_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/nombre_paquete)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genjava_INSTALL_DIR}/nombre_paquete
    DESTINATION ${genjava_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_java)
  add_dependencies(nombre_paquete_generate_messages_java std_msgs_generate_messages_java)
endif()

if(genlisp_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/nombre_paquete)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genlisp_INSTALL_DIR}/nombre_paquete
    DESTINATION ${genlisp_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_lisp)
  add_dependencies(nombre_paquete_generate_messages_lisp std_msgs_generate_messages_lisp)
endif()

if(gennodejs_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/nombre_paquete)
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${gennodejs_INSTALL_DIR}/nombre_paquete
    DESTINATION ${gennodejs_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_nodejs)
  add_dependencies(nombre_paquete_generate_messages_nodejs std_msgs_generate_messages_nodejs)
endif()

if(genpy_INSTALL_DIR AND EXISTS ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/nombre_paquete)
  install(CODE "execute_process(COMMAND \"/usr/bin/python2\" -m compileall \"${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/nombre_paquete\")")
  # install generated code
  install(
    DIRECTORY ${CATKIN_DEVEL_PREFIX}/${genpy_INSTALL_DIR}/nombre_paquete
    DESTINATION ${genpy_INSTALL_DIR}
  )
endif()
if(TARGET std_msgs_generate_messages_py)
  add_dependencies(nombre_paquete_generate_messages_py std_msgs_generate_messages_py)
endif()
