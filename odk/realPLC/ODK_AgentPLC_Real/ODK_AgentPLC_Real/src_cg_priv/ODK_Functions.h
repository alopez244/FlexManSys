/*
 * This file is AUTO GENERATED - DO NOT MODIFY this file. 
 * This file contains the function prototypes of ODK 1500S.
 *
 * File created by ODK_CodeGenerator version 205.100.101.18
 * at Mon March 15 10:24:55 2021 
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
ODK_RESULT SampleWrite (
  /*IN*/const control_flags& flags,
  /*IN*/const plc2agent& str_out); //Sends the Agent message   
#define _ODK_FUNCTION_SAMPLEWRITE  ODK_RESULT SampleWrite (/*IN*/const control_flags& flags, /*IN*/const plc2agent& str_out)

ODK_RESULT SampleRead (
  /*OUT*/agent2plc& str_in,
  /*OUT*/ODK_BOOL& tRecv,
  /*OUT*/ODK_BOOL& tData,
  /*INOUT*/control_flags& flags); //Receive the Agent message and parses the JSON to structure
#define _ODK_FUNCTION_SAMPLEREAD  ODK_RESULT SampleRead (/*OUT*/agent2plc& str_in, /*OUT*/ODK_BOOL& tRecv, /*OUT*/ODK_BOOL& tData, /*INOUT*/control_flags& flags)

ODK_RESULT sendConf (
);
#define _ODK_FUNCTION_SENDCONF  ODK_RESULT sendConf ()

#endif