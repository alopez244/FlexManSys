/*
 * This file is AUTO GENERATED - DO NOT MODIFY this file. 
 * This file contains the execute function and trace helpers for ODK 1500S.
 *
 * File created by ODK_CodeGenerator version 205.100.101.18
 * at Mon March 15 10:24:55 2021
*/

#include "ODK_Functions.h"
#include "ODK_CpuReadData.h"
#include "ODK_CpuReadWriteData.h"

#define ODK_COMMAND_NOT_IMPLEMENTED    0x8098 
EXPORT_API ODK_UINT32 g_ODK1500sBuildVersion = (2 << 24) + (5 << 16) + (1 << 8) + 0;
 // first if

// declaration of the callbacks for testing them in Execute()
EXPORT_API ODK_RESULT OnLoad (void);
EXPORT_API ODK_RESULT OnUnload (void);
EXPORT_API ODK_RESULT OnRun (void);
EXPORT_API ODK_RESULT OnStop (void);

//command enums
typedef enum CommandHash_e
{
  FCT_HASH_initAgent = 0xEF7DB504,
  FCT_HASH_SampleWrite = 0xBC969089,
  FCT_HASH_SampleRead = 0x0B0BE76B,
  FCT_HASH_sendConf = 0x29FAF677
}CommandHash_t;

#pragma optimize( "", off )
EXPORT_API ODK_RESULT ExecuteV25 ( const ODK_UINT32 cmd
                                 , const ODK_UINT32 inLen   , const char* const in
                                 , const ODK_UINT32 inoutLen, const char*       inout
                                 , const ODK_UINT32 outLen  , const char*       out)
{
  switch (cmd)
  {
    case FCT_HASH_initAgent:
    {
      return initAgent ();
    }
    case FCT_HASH_SampleWrite:
    {
      return SampleWrite (*((control_flags*) &(in[0])), *((plc2agent*) &(in[3])));
    }
    case FCT_HASH_SampleRead:
    {
      return SampleRead (*((agent2plc*) &(out[0])), *((ODK_BOOL*) &(out[21])), *((ODK_BOOL*) &(out[22])), *((control_flags*) &(inout[0])));
    }
    case FCT_HASH_sendConf:
    {
      return sendConf ();
    }
    default:
    {
      typedef ODK_RESULT (*t_CallbackFunction)(void);
      #pragma warning ( disable: 4189 )
      // check the existence of the callback functions
      // (but suppress warning of not used variables)
      {
        t_CallbackFunction tmp1 = &OnLoad;
        t_CallbackFunction tmp2 = &OnUnload;
        t_CallbackFunction tmp3 = &OnRun;
        t_CallbackFunction tmp4 = &OnStop;
      }
      return ODK_COMMAND_NOT_IMPLEMENTED;
    }
  }
}
#pragma optimize( "", on )


