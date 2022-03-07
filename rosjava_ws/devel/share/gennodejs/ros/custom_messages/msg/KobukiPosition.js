// Auto-generated. Do not edit!

// (in-package custom_messages.msg)


"use strict";

const _serializer = _ros_msg_utils.Serialize;
const _arraySerializer = _serializer.Array;
const _deserializer = _ros_msg_utils.Deserialize;
const _arrayDeserializer = _deserializer.Array;
const _finder = _ros_msg_utils.Find;
const _getByteLength = _ros_msg_utils.getByteLength;

//-----------------------------------------------------------

class KobukiPosition {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.transport_in_dock = null;
      this.recovery_point = null;
      this.odom_x = null;
      this.odom_y = null;
      this.rotation = null;
    }
    else {
      if (initObj.hasOwnProperty('transport_in_dock')) {
        this.transport_in_dock = initObj.transport_in_dock
      }
      else {
        this.transport_in_dock = false;
      }
      if (initObj.hasOwnProperty('recovery_point')) {
        this.recovery_point = initObj.recovery_point
      }
      else {
        this.recovery_point = '';
      }
      if (initObj.hasOwnProperty('odom_x')) {
        this.odom_x = initObj.odom_x
      }
      else {
        this.odom_x = 0.0;
      }
      if (initObj.hasOwnProperty('odom_y')) {
        this.odom_y = initObj.odom_y
      }
      else {
        this.odom_y = 0.0;
      }
      if (initObj.hasOwnProperty('rotation')) {
        this.rotation = initObj.rotation
      }
      else {
        this.rotation = 0.0;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type KobukiPosition
    // Serialize message field [transport_in_dock]
    bufferOffset = _serializer.bool(obj.transport_in_dock, buffer, bufferOffset);
    // Serialize message field [recovery_point]
    bufferOffset = _serializer.string(obj.recovery_point, buffer, bufferOffset);
    // Serialize message field [odom_x]
    bufferOffset = _serializer.float64(obj.odom_x, buffer, bufferOffset);
    // Serialize message field [odom_y]
    bufferOffset = _serializer.float64(obj.odom_y, buffer, bufferOffset);
    // Serialize message field [rotation]
    bufferOffset = _serializer.float64(obj.rotation, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type KobukiPosition
    let len;
    let data = new KobukiPosition(null);
    // Deserialize message field [transport_in_dock]
    data.transport_in_dock = _deserializer.bool(buffer, bufferOffset);
    // Deserialize message field [recovery_point]
    data.recovery_point = _deserializer.string(buffer, bufferOffset);
    // Deserialize message field [odom_x]
    data.odom_x = _deserializer.float64(buffer, bufferOffset);
    // Deserialize message field [odom_y]
    data.odom_y = _deserializer.float64(buffer, bufferOffset);
    // Deserialize message field [rotation]
    data.rotation = _deserializer.float64(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    let length = 0;
    length += object.recovery_point.length;
    return length + 29;
  }

  static datatype() {
    // Returns string type for a message object
    return 'custom_messages/KobukiPosition';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return 'fa959bfdcde117615672bd484119c1ee';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    bool transport_in_dock
    string recovery_point
    float64 odom_x
    float64 odom_y
    float64 rotation
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new KobukiPosition(null);
    if (msg.transport_in_dock !== undefined) {
      resolved.transport_in_dock = msg.transport_in_dock;
    }
    else {
      resolved.transport_in_dock = false
    }

    if (msg.recovery_point !== undefined) {
      resolved.recovery_point = msg.recovery_point;
    }
    else {
      resolved.recovery_point = ''
    }

    if (msg.odom_x !== undefined) {
      resolved.odom_x = msg.odom_x;
    }
    else {
      resolved.odom_x = 0.0
    }

    if (msg.odom_y !== undefined) {
      resolved.odom_y = msg.odom_y;
    }
    else {
      resolved.odom_y = 0.0
    }

    if (msg.rotation !== undefined) {
      resolved.rotation = msg.rotation;
    }
    else {
      resolved.rotation = 0.0
    }

    return resolved;
    }
};

module.exports = KobukiPosition;
