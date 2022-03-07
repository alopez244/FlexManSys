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

class KobukiGeneral {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.transport_unit_name = null;
      this.transport_unit_state = null;
      this.battery = null;
    }
    else {
      if (initObj.hasOwnProperty('transport_unit_name')) {
        this.transport_unit_name = initObj.transport_unit_name
      }
      else {
        this.transport_unit_name = '';
      }
      if (initObj.hasOwnProperty('transport_unit_state')) {
        this.transport_unit_state = initObj.transport_unit_state
      }
      else {
        this.transport_unit_state = '';
      }
      if (initObj.hasOwnProperty('battery')) {
        this.battery = initObj.battery
      }
      else {
        this.battery = 0.0;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type KobukiGeneral
    // Serialize message field [transport_unit_name]
    bufferOffset = _serializer.string(obj.transport_unit_name, buffer, bufferOffset);
    // Serialize message field [transport_unit_state]
    bufferOffset = _serializer.string(obj.transport_unit_state, buffer, bufferOffset);
    // Serialize message field [battery]
    bufferOffset = _serializer.float32(obj.battery, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type KobukiGeneral
    let len;
    let data = new KobukiGeneral(null);
    // Deserialize message field [transport_unit_name]
    data.transport_unit_name = _deserializer.string(buffer, bufferOffset);
    // Deserialize message field [transport_unit_state]
    data.transport_unit_state = _deserializer.string(buffer, bufferOffset);
    // Deserialize message field [battery]
    data.battery = _deserializer.float32(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    let length = 0;
    length += object.transport_unit_name.length;
    length += object.transport_unit_state.length;
    return length + 12;
  }

  static datatype() {
    // Returns string type for a message object
    return 'custom_messages/KobukiGeneral';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return 'dd826a4a104ac611941e72978f2a882c';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    string transport_unit_name
    string transport_unit_state
    float32 battery
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new KobukiGeneral(null);
    if (msg.transport_unit_name !== undefined) {
      resolved.transport_unit_name = msg.transport_unit_name;
    }
    else {
      resolved.transport_unit_name = ''
    }

    if (msg.transport_unit_state !== undefined) {
      resolved.transport_unit_state = msg.transport_unit_state;
    }
    else {
      resolved.transport_unit_state = ''
    }

    if (msg.battery !== undefined) {
      resolved.battery = msg.battery;
    }
    else {
      resolved.battery = 0.0
    }

    return resolved;
    }
};

module.exports = KobukiGeneral;
