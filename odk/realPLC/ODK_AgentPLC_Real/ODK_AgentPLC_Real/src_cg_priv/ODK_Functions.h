/*
 * This file is AUTO GENERATED - DO NOT MODIFY this file. 
 * This file contains the function prototypes of ODK 1500S.
 *
 * File created by ODK_CodeGenerator version 200.0.3002.1
 * at lun marzo 29 11:33:17 2021 
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
  /*OUT*/agent2plc& str2,
  /*OUT*/ODK_BOOL& tRecv,
  /*OUT*/ODK_BOOL& tData,
  /*INOUT*/control_flags& str1); //Receive the Agent message and parses the JSON to structure
#define _ODK_FUNCTION_RECVAGENT  ODK_RESULT recvAgent (/*OUT*/agent2plc& str2, /*OUT*/ODK_BOOL& tRecv, /*OUT*/ODK_BOOL& tData, /*INOUT*/control_flags& str1)

ODK_RESULT sendConf (
);
#define _ODK_FUNCTION_SENDCONF  ODK_RESULT sendConf ()

#endif