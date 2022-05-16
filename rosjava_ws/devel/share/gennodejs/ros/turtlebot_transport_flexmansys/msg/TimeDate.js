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

class TimeDate {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.year = null;
      this.month = null;
      this.day = null;
      this.hour = null;
      this.minute = null;
      this.seconds = null;
    }
    else {
      if (initObj.hasOwnProperty('year')) {
        this.year = initObj.year
      }
      else {
        this.year = 0;
      }
      if (initObj.hasOwnProperty('month')) {
        this.month = initObj.month
      }
      else {
        this.month = 0;
      }
      if (initObj.hasOwnProperty('day')) {
        this.day = initObj.day
      }
      else {
        this.day = 0;
      }
      if (initObj.hasOwnProperty('hour')) {
        this.hour = initObj.hour
      }
      else {
        this.hour = 0;
      }
      if (initObj.hasOwnProperty('minute')) {
        this.minute = initObj.minute
      }
      else {
        this.minute = 0;
      }
      if (initObj.hasOwnProperty('seconds')) {
        this.seconds = initObj.seconds
      }
      else {
        this.seconds = 0;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type TimeDate
    // Serialize message field [year]
    bufferOffset = _serializer.int32(obj.year, buffer, bufferOffset);
    // Serialize message field [month]
    bufferOffset = _serializer.int32(obj.month, buffer, bufferOffset);
    // Serialize message field [day]
    bufferOffset = _serializer.int32(obj.day, buffer, bufferOffset);
    // Serialize message field [hour]
    bufferOffset = _serializer.int32(obj.hour, buffer, bufferOffset);
    // Serialize message field [minute]
    bufferOffset = _serializer.int32(obj.minute, buffer, bufferOffset);
    // Serialize message field [seconds]
    bufferOffset = _serializer.int32(obj.seconds, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type TimeDate
    let len;
    let data = new TimeDate(null);
    // Deserialize message field [year]
    data.year = _deserializer.int32(buffer, bufferOffset);
    // Deserialize message field [month]
    data.month = _deserializer.int32(buffer, bufferOffset);
    // Deserialize message field [day]
    data.day = _deserializer.int32(buffer, bufferOffset);
    // Deserialize message field [hour]
    data.hour = _deserializer.int32(buffer, bufferOffset);
    // Deserialize message field [minute]
    data.minute = _deserializer.int32(buffer, bufferOffset);
    // Deserialize message field [seconds]
    data.seconds = _deserializer.int32(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    return 24;
  }

  static datatype() {
    // Returns string type for a message object
    return 'turtlebot_transport_flexmansys/TimeDate';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return 'd2fd2933667b73a2a097c5705da48ffb';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    int32 year
    int32 month
    int32 day
    int32 hour
    int32 minute
    int32 seconds
    
    
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new TimeDate(null);
    if (msg.year !== undefined) {
      resolved.year = msg.year;
    }
    else {
      resolved.year = 0
    }

    if (msg.month !== undefined) {
      resolved.month = msg.month;
    }
    else {
      resolved.month = 0
    }

    if (msg.day !== undefined) {
      resolved.day = msg.day;
    }
    else {
      resolved.day = 0
    }

    if (msg.hour !== undefined) {
      resolved.hour = msg.hour;
    }
    else {
      resolved.hour = 0
    }

    if (msg.minute !== undefined) {
      resolved.minute = msg.minute;
    }
    else {
      resolved.minute = 0
    }

    if (msg.seconds !== undefined) {
      resolved.seconds = msg.seconds;
    }
    else {
      resolved.seconds = 0
    }

    return resolved;
    }
};

module.exports = TimeDate;
