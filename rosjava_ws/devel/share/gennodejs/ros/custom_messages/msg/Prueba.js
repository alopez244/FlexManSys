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

class Prueba {
  constructor(initObj={}) {
    if (initObj === null) {
      // initObj === null is a special case for deserialization where we don't initialize fields
      this.numero_prueba = null;
    }
    else {
      if (initObj.hasOwnProperty('numero_prueba')) {
        this.numero_prueba = initObj.numero_prueba
      }
      else {
        this.numero_prueba = 0;
      }
    }
  }

  static serialize(obj, buffer, bufferOffset) {
    // Serializes a message object of type Prueba
    // Serialize message field [numero_prueba]
    bufferOffset = _serializer.int32(obj.numero_prueba, buffer, bufferOffset);
    return bufferOffset;
  }

  static deserialize(buffer, bufferOffset=[0]) {
    //deserializes a message object of type Prueba
    let len;
    let data = new Prueba(null);
    // Deserialize message field [numero_prueba]
    data.numero_prueba = _deserializer.int32(buffer, bufferOffset);
    return data;
  }

  static getMessageSize(object) {
    return 4;
  }

  static datatype() {
    // Returns string type for a message object
    return 'custom_messages/Prueba';
  }

  static md5sum() {
    //Returns md5sum for a message object
    return '5d1d253d7f30569507dbcd46fe13058c';
  }

  static messageDefinition() {
    // Returns full string definition for message
    return `
    int32 numero_prueba
    
    
    
    
    `;
  }

  static Resolve(msg) {
    // deep-construct a valid message object instance of whatever was passed in
    if (typeof msg !== 'object' || msg === null) {
      msg = {};
    }
    const resolved = new Prueba(null);
    if (msg.numero_prueba !== undefined) {
      resolved.numero_prueba = msg.numero_prueba;
    }
    else {
      resolved.numero_prueba = 0
    }

    return resolved;
    }
};

module.exports = Prueba;
