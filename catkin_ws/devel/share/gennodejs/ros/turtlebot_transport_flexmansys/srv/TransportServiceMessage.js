// Auto-generated. Do not edit!

// (in-package turtlebot_transport_flexmansys.srv)


"use strict";

const _serializer = _ros_msg_utils.Serialize;
const _arraySerializer = _serializer.Array;
const _deserializer = _ros_msg_utils.Deserialize;
const _arrayDeserializer = _deserializer.Array;
const _finder = _ros_msg_utils.Find;
const _getByteLength = _ros_msg_utils.getByteLength;

//-----------------------------------------------------------


//-----------------------------------------------------------

class TransportServiceMessageRequest {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.coordinate = null;
    }
    else {
      if (initObj.hasOwnProperty('coordinate')) {
        this.coordinate = initObj.coordinate
      }
      else {
        this.coordinate = '';
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type TransportServiceMessageRequest
    // Serialize message field [coordinate]
    bufferOffset = _serializer.string(obj.coordinate, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type TransportServiceMessageRequest
    let len;
    let data = new TransportServiceMessageRequest(null);
    // Deserialize message field [coordinate]
    data.coordinate = _deserializer.string(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    let length = 0;
    length += object.coordinate.length;
    return length + 4;
  }

  static datatype() {
    // Returns string type for a service object
    return 'turtlebot_transport_flexmansys/TransportServiceMessageRequest';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return 'c65ef28c342c0d5eee21ae18aad1bb8d';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    string coordinate
    
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new TransportServiceMessageRequest(null);
    if (msg.coordinate !== undefined) {
      resolved.coordinate = msg.coordinate;
    }
    else {
      resolved.coordinate = ''
    }

    return resolved;
    }
};

class TransportServiceMessageResponse {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.success = null;
    }
    else {
      if (initObj.hasOwnProperty('success')) {
        this.success = initObj.success
      }
      else {
        this.success = false;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type TransportServiceMessageResponse
    // Serialize message field [success]
    bufferOffset = _serializer.bool(obj.success, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type TransportServiceMessageResponse
    let len;
    let data = new TransportServiceMessageResponse(null);
    // Deserialize message field [success]
    data.success = _deserializer.bool(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    return 1;
  }

  static datatype() {
    // Returns string type for a service object
    return 'turtlebot_transport_flexmansys/TransportServiceMessageResponse';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return '358e233cde0c8a8bcfea4ce193f8fc15';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    bool success
    
    
    
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new TransportServiceMessageResponse(null);
    if (msg.success !== undefined) {
      resolved.success = msg.success;
    }
    else {
      resolved.success = false
    }

    return resolved;
    }
};

module.exports = {
  Request: TransportServiceMessageRequest,
  Response: TransportServiceMessageResponse,
  md5sum() { return '8ddb36dd3d52323557949d25fae2e281'; },
  datatype() { return 'turtlebot_transport_flexmansys/TransportServiceMessage'; }
};
