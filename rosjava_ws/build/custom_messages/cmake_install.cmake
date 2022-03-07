# Install script for directory: /home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/install")
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
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/custom_messages/msg" TYPE FILE FILES
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/Prueba.msg"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiGeneral.msg"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiObstacle.msg"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/KobukiPosition.msg"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TimeDate.msg"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportPrivateState.msg"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/msg/TransportUnitState.msg"
    )
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/custom_messages/cmake" TYPE FILE FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/build/custom_messages/catkin_generated/installspace/custom_messages-msg-paths.cmake")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include" TYPE DIRECTORY FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/devel/include/custom_messages")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/roseus/ros" TYPE DIRECTORY FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/devel/share/roseus/ros/custom_messages")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/common-lisp/ros" TYPE DIRECTORY FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/devel/share/common-lisp/ros/custom_messages")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/gennodejs/ros" TYPE DIRECTORY FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/devel/share/gennodejs/ros/custom_messages")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  execute_process(COMMAND "/usr/bin/python2" -m compileall "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/devel/lib/python2.7/dist-packages/custom_messages")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/python2.7/dist-packages" TYPE DIRECTORY FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/devel/lib/python2.7/dist-packages/custom_messages")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig" TYPE FILE FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/build/custom_messages/catkin_generated/installspace/custom_messages.pc")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/custom_messages/cmake" TYPE FILE FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/build/custom_messages/catkin_generated/installspace/custom_messages-msg-extras.cmake")
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/custom_messages/cmake" TYPE FILE FILES
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/build/custom_messages/catkin_generated/installspace/custom_messagesConfig.cmake"
    "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/build/custom_messages/catkin_generated/installspace/custom_messagesConfig-version.cmake"
    )
endif()

if(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/custom_messages" TYPE FILE FILES "/home/borjartime/IdeaProjects/FlexManSys/rosjava_ws/src/custom_messages/package.xml")
endif()

