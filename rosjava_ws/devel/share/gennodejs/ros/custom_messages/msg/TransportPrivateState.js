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

class TransportPrivateState {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.transport_state = null;
      this.transport_docked = null;
    }
    else {
      if (initObj.hasOwnProperty('transport_state')) {
        this.transport_state = initObj.transport_state
      }
      else {
        this.transport_state = '';
      }
      if (initObj.hasOwnProperty('transport_docked')) {
        this.transport_docked = initObj.transport_docked
      }
      else {
        this.transport_docked = false;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type TransportPrivateState
    // Serialize message field [transport_state]
    bufferOffset = _serializer.string(obj.transport_state, buffer, bufferOffset);
    // Serialize message field [transport_docked]
    bufferOffset = _serializer.bool(obj.transport_docked, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type TransportPrivateState
    let len;
    let data = new TransportPrivateState(null);
    // Deserialize message field [transport_state]
    data.transport_state = _deserializer.string(buffer, bufferOffset);
    // Deserialize message field [transport_docked]
    data.transport_docked = _deserializer.bool(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    let length = 0;
    length += object.transport_state.length;
    return length + 5;
  }

  static datatype() {
    // Returns string type for a message object
    return 'custom_messages/TransportPrivateState';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return '670f3bb83518dec928ea7b995f722845';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    string transport_state
    bool transport_docked
    
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new TransportPrivateState(null);
    if (msg.transport_state !== undefined) {
      resolved.transport_state = msg.transport_state;
    }
    else {
      resolved.transport_state = ''
    }

    if (msg.transport_docked !== undefined) {
      resolved.transport_docked = msg.transport_docked;
    }
    else {
      resolved.transport_docked = false
    }

    return resolved;
    }
};

module.exports = TransportPrivateState;
