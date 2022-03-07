// Auto-generated. Do not edit!

// (in-package turtlebot_transport_flexmansys.msg)


"use strict";

const _serializer = _ros_msg_utils.Serialize;
const _arraySerializer = _serializer.Array;
const _deserializer = _ros_msg_utils.Deserialize;
const _arrayDeserializer = _deserializer.Array;
const _finder = _ros_msg_utils.Find;
const _getByteLength = _ros_msg_utils.getByteLength;

//-----------------------------------------------------------

class KobukiObstacle {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.detected_obstacle_bumper = null;
      this.detected_obstacle_camera = null;
    }
    else {
      if (initObj.hasOwnProperty('detected_obstacle_bumper')) {
        this.detected_obstacle_bumper = initObj.detected_obstacle_bumper
      }
      else {
        this.detected_obstacle_bumper = false;
      }
      if (initObj.hasOwnProperty('detected_obstacle_camera')) {
        this.detected_obstacle_camera = initObj.detected_obstacle_camera
      }
      else {
        this.detected_obstacle_camera = false;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type KobukiObstacle
    // Serialize message field [detected_obstacle_bumper]
    bufferOffset = _serializer.bool(obj.detected_obstacle_bumper, buffer, bufferOffset);
    // Serialize message field [detected_obstacle_camera]
    bufferOffset = _serializer.bool(obj.detected_obstacle_camera, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type KobukiObstacle
    let len;
    let data = new KobukiObstacle(null);
    // Deserialize message field [detected_obstacle_bumper]
    data.detected_obstacle_bumper = _deserializer.bool(buffer, bufferOffset);
    // Deserialize message field [detected_obstacle_camera]
    data.detected_obstacle_camera = _deserializer.bool(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    return 2;
  }

  static datatype() {
    // Returns string type for a message object
    return 'turtlebot_transport_flexmansys/KobukiObstacle';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return '2aef80677f5e7bb119c67140618dc301';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    bool detected_obstacle_bumper
    bool detected_obstacle_camera
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new KobukiObstacle(null);
    if (msg.detected_obstacle_bumper !== undefined) {
      resolved.detected_obstacle_bumper = msg.detected_obstacle_bumper;
    }
    else {
      resolved.detected_obstacle_bumper = false
    }

    if (msg.detected_obstacle_camera !== undefined) {
      resolved.detected_obstacle_camera = msg.detected_obstacle_camera;
    }
    else {
      resolved.detected_obstacle_camera = false
    }

    return resolved;
    }
};

module.exports = KobukiObstacle;
