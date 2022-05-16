# Install script for directory: /home/borjartime/catkin_ws/src/creacion_accion

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/home/borjartime/catkin_ws/install")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "1")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/creacion_accion/action" TYPE FILE FILES "/home/borjartime/catkin_ws/src/creacion_accion/action/CustomActionMsg.action")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/creacion_accion/msg" TYPE FILE FILES
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgAction.msg"
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgActionGoal.msg"
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgActionResult.msg"
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgActionFeedback.msg"
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgGoal.msg"
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgResult.msg"
    "/home/borjartime/catkin_ws/devel/share/creacion_accion/msg/CustomActionMsgFeedback.msg"
    )
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/creacion_accion/cmake" TYPE FILE FILES "/home/borjartime/catkin_ws/build/creacion_accion/catkin_generated/installspace/creacion_accion-msg-paths.cmake")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/include/creacion_accion")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/roseus/ros" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/share/roseus/ros/creacion_accion")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/common-lisp/ros" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/share/common-lisp/ros/creacion_accion")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/gennodejs/ros" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/share/gennodejs/ros/creacion_accion")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  execute_process(COMMAND "/usr/bin/python2" -m compileall "/home/borjartime/catkin_ws/devel/lib/python2.7/dist-packages/creacion_accion")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/python2.7/dist-packages" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/lib/python2.7/dist-packages/creacion_accion")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig" TYPE FILE FILES "/home/borjartime/catkin_ws/build/creacion_accion/catkin_generated/installspace/creacion_accion.pc")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/creacion_accion/cmake" TYPE FILE FILES "/home/borjartime/catkin_ws/build/creacion_accion/catkin_generated/installspace/creacion_accion-msg-extras.cmake")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/creacion_accion/cmake" TYPE FILE FILES
    "/home/borjartime/catkin_ws/build/creacion_accion/catkin_generated/installspace/creacion_accionConfig.cmake"
    "/home/borjartime/catkin_ws/build/creacion_accion/catkin_generated/installspace/creacion_accionConfig-version.cmake"
    )
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/creacion_accion" TYPE FILE FILES "/home/borjartime/catkin_ws/src/creacion_accion/package.xml")
endif()

