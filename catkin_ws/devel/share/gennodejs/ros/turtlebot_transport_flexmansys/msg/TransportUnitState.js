// Auto-generated. Do not edit!

// (in-package turtlebot_transport_flexmansys.msg)


"use strict";

const _serializer = _ros_msg_utils.Serialize;
const _arraySerializer = _serializer.Array;
const _deserializer = _ros_msg_utils.Deserialize;
const _arrayDeserializer = _deserializer.Array;
const _finder = _ros_msg_utils.Find;
const _getByteLength = _ros_msg_utils.getByteLength;
let KobukiGeneral = require('./KobukiGeneral.js');
let KobukiObstacle = require('./KobukiObstacle.js');
let KobukiPosition = require('./KobukiPosition.js');
let TimeDate = require('./TimeDate.js');

//-----------------------------------------------------------

class TransportUnitState {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.kobuki_general = null;
      this.kobuki_obstacle = null;
      this.kobuki_position = null;
      this.odroid_date = null;
    }
    else {
      if (initObj.hasOwnProperty('kobuki_general')) {
        this.kobuki_general = initObj.kobuki_general
      }
      else {
        this.kobuki_general = new KobukiGeneral();
      }
      if (initObj.hasOwnProperty('kobuki_obstacle')) {
        this.kobuki_obstacle = initObj.kobuki_obstacle
      }
      else {
        this.kobuki_obstacle = new KobukiObstacle();
      }
      if (initObj.hasOwnProperty('kobuki_position')) {
        this.kobuki_position = initObj.kobuki_position
      }
      else {
        this.kobuki_position = new KobukiPosition();
      }
      if (initObj.hasOwnProperty('odroid_date')) {
        this.odroid_date = initObj.odroid_date
      }
      else {
        this.odroid_date = new TimeDate();
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type TransportUnitState
    // Serialize message field [kobuki_general]
    bufferOffset = KobukiGeneral.serialize(obj.kobuki_general, buffer, bufferOffset);
    // Serialize message field [kobuki_obstacle]
    bufferOffset = KobukiObstacle.serialize(obj.kobuki_obstacle, buffer, bufferOffset);
    // Serialize message field [kobuki_position]
    bufferOffset = KobukiPosition.serialize(obj.kobuki_position, buffer, bufferOffset);
    // Serialize message field [odroid_date]
    bufferOffset = TimeDate.serialize(obj.odroid_date, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type TransportUnitState
    let len;
    let data = new TransportUnitState(null);
    // Deserialize message field [kobuki_general]
    data.kobuki_general = KobukiGeneral.deserialize(buffer, bufferOffset);
    // Deserialize message field [kobuki_obstacle]
    data.kobuki_obstacle = KobukiObstacle.deserialize(buffer, bufferOffset);
    // Deserialize message field [kobuki_position]
    data.kobuki_position = KobukiPosition.deserialize(buffer, bufferOffset);
    // Deserialize message field [odroid_date]
    data.odroid_date = TimeDate.deserialize(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    let length = 0;
    length += KobukiGeneral.getMessageSize(object.kobuki_general);
    length += KobukiPosition.getMessageSize(object.kobuki_position);
    return length + 26;
  }

  static datatype() {
    // Returns string type for a message object
    return 'turtlebot_transport_flexmansys/TransportUnitState';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return '4a9f169d9b464fbeb767cabba27149f7';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    KobukiGeneral kobuki_general
    KobukiObstacle kobuki_obstacle
    KobukiPosition kobuki_position
    TimeDate odroid_date
    
    
    ================================================================================
    MSG: turtlebot_transport_flexmansys/KobukiGeneral
    string transport_unit_name
    string transport_unit_state
    float32 battery
    ================================================================================
    MSG: turtlebot_transport_flexmansys/KobukiObstacle
    bool detected_obstacle_bumper
    bool detected_obstacle_camera
    ================================================================================
    MSG: turtlebot_transport_flexmansys/KobukiPosition
    bool transport_in_dock
    string recovery_point
    float64 odom_x
    float64 odom_y
    float64 rotation
    ================================================================================
    MSG: turtlebot_transport_flexmansys/TimeDate
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
    const resolved = new TransportUnitState(null);
    if (msg.kobuki_general !== undefined) {
      resolved.kobuki_general = KobukiGeneral.Resolve(msg.kobuki_general)
    }
    else {
      resolved.kobuki_general = new KobukiGeneral()
    }

    if (msg.kobuki_obstacle !== undefined) {
      resolved.kobuki_obstacle = KobukiObstacle.Resolve(msg.kobuki_obstacle)
    }
    else {
      resolved.kobuki_obstacle = new KobukiObstacle()
    }

    if (msg.kobuki_position !== undefined) {
      resolved.kobuki_position = KobukiPosition.Resolve(msg.kobuki_position)
    }
    else {
      resolved.kobuki_position = new KobukiPosition()
    }

    if (msg.odroid_date !== undefined) {
      resolved.odroid_date = TimeDate.Resolve(msg.odroid_date)
    }
    else {
      resolved.odroid_date = new TimeDate()
    }

    return resolved;
    }
};

module.exports = TransportUnitState;
