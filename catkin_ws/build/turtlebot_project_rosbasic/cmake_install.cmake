# Install script for directory: /home/borjartime/catkin_ws/src/turtlebot_project_rosbasic

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
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic/srv" TYPE FILE FILES "/home/borjartime/catkin_ws/src/turtlebot_project_rosbasic/srv/CrashDirection.srv")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic/action" TYPE FILE FILES "/home/borjartime/catkin_ws/src/turtlebot_project_rosbasic/action/OdometryActionMsg.action")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic/msg" TYPE FILE FILES
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgAction.msg"
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgActionGoal.msg"
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgActionResult.msg"
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgActionFeedback.msg"
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgGoal.msg"
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgResult.msg"
    "/home/borjartime/catkin_ws/devel/share/turtlebot_project_rosbasic/msg/OdometryActionMsgFeedback.msg"
    )
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic/cmake" TYPE FILE FILES "/home/borjartime/catkin_ws/build/turtlebot_project_rosbasic/catkin_generated/installspace/turtlebot_project_rosbasic-msg-paths.cmake")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/include/turtlebot_project_rosbasic")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/roseus/ros" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/share/roseus/ros/turtlebot_project_rosbasic")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/common-lisp/ros" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/share/common-lisp/ros/turtlebot_project_rosbasic")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/gennodejs/ros" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/share/gennodejs/ros/turtlebot_project_rosbasic")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  execute_process(COMMAND "/usr/bin/python2" -m compileall "/home/borjartime/catkin_ws/devel/lib/python2.7/dist-packages/turtlebot_project_rosbasic")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/python2.7/dist-packages" TYPE DIRECTORY FILES "/home/borjartime/catkin_ws/devel/lib/python2.7/dist-packages/turtlebot_project_rosbasic")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig" TYPE FILE FILES "/home/borjartime/catkin_ws/build/turtlebot_project_rosbasic/catkin_generated/installspace/turtlebot_project_rosbasic.pc")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic/cmake" TYPE FILE FILES "/home/borjartime/catkin_ws/build/turtlebot_project_rosbasic/catkin_generated/installspace/turtlebot_project_rosbasic-msg-extras.cmake")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic/cmake" TYPE FILE FILES
    "/home/borjartime/catkin_ws/build/turtlebot_project_rosbasic/catkin_generated/installspace/turtlebot_project_rosbasicConfig.cmake"
    "/home/borjartime/catkin_ws/build/turtlebot_project_rosbasic/catkin_generated/installspace/turtlebot_project_rosbasicConfig-version.cmake"
    )
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/turtlebot_project_rosbasic" TYPE FILE FILES "/home/borjartime/catkin_ws/src/turtlebot_project_rosbasic/package.xml")
endif()

