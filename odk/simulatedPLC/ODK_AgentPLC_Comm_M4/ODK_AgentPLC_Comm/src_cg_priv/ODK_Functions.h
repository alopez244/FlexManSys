/*
 * This file is AUTO GENERATED - DO NOT MODIFY this file. 
 * This file contains the function prototypes of ODK 1500S.
 *
 * File created by ODK_CodeGenerator version 205.100.101.18
 * at Thu March 4 12:10:05 2021 
*/

#if !defined ODK_Functions_H
#define ODK_Functions_H

#include "ODK_Types.h"

#ifdef DLL_EXPORT
  #define EXPORT_API extern "C" __declspec(dllexport)
#else
  #define EXPORT_API extern "C"
#endif

ODK_RESULT initAgent (
);
#define _ODK_FUNCTION_INITAGENT  ODK_RESULT initAgent ()

//Agent Init
ODK_RESULT sendAgent (
  /*IN*/const control_flags& str1,
  /*IN*/const plc2agent& str2); //Sends the Agent message   
#define _ODK_FUNCTION_SENDAGENT  ODK_RESULT sendAgent (/*IN*/const control_flags& str1, /*IN*/const plc2agent& str2)

ODK_RESULT recvAgent (
  /*OUT*/control_flags& str1,
  /*OUT*/agent2plc& str2,
  /*OUT*/ODK_BOOL& tRecv,
  /*OUT*/ODK_BOOL& tData); //Receive the Agent message and parses the JSON to structure
#define _ODK_FUNCTION_RECVAGENT  ODK_RESULT recvAgent (/*OUT*/control_flags& str1, /*OUT*/agent2plc& str2, /*OUT*/ODK_BOOL& tRecv, /*OUT*/ODK_BOOL& tData)

ODK_RESULT sendConf (
);
#define _ODK_FUNCTION_SENDCONF  ODK_RESULT sendConf ()

ODK_RESULT GetTrace (
  /*IN*/const ODK_INT16& TraceCount,
  /*OUT*/ODK_S7STRING TraceBuffer[256][127]);
#define _ODK_FUNCTION_GETTRACE  ODK_RESULT GetTrace (/*IN*/const ODK_INT16& TraceCount, /*OUT*/ODK_S7STRING TraceBuffer[256][127])

#endif