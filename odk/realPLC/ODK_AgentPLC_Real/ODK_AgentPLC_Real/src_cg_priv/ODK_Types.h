/* 
 * This file is AUTO GENERATED - DO NOT MODIFY this file. 
 * This file contains the data types of ODK 1500S.
 *
 * File created by ODK_CodeGenerator version 205.100.101.18 
 * at Mon March 15 10:24:55 2021 
 */

#if !defined ODK_Types_H
#define ODK_Types_H

#include <stdint.h>

#define  ODK_SUCCESS         0x0000
#define  ODK_USER_ERROR_BASE 0xF000

#define  ODK_TRUE            1
#define  ODK_FALSE           0

typedef  double              ODK_DOUBLE;
typedef  float               ODK_FLOAT;
typedef  int64_t             ODK_INT64;
typedef  int32_t             ODK_INT32;
typedef  int16_t             ODK_INT16;
typedef  int8_t              ODK_INT8;
typedef  uint64_t            ODK_UINT64;
typedef  uint32_t            ODK_UINT32;
typedef  uint16_t            ODK_UINT16;
typedef  uint8_t             ODK_UINT8;
typedef  uint64_t            ODK_LWORD;
typedef  uint32_t            ODK_DWORD;
typedef  uint16_t            ODK_WORD;
typedef  uint8_t             ODK_BYTE;
typedef  uint8_t             ODK_BOOL;
typedef  int64_t             ODK_LTIME;
typedef  int32_t             ODK_TIME;
typedef  uint64_t            ODK_LDT;
typedef  uint64_t            ODK_LTOD;
typedef  uint32_t            ODK_TOD;
typedef  uint8_t             ODK_S7STRING;
typedef  uint16_t            ODK_S7WSTRING;
typedef  wchar_t             ODK_WCHAR;
typedef  char                ODK_CHAR;
typedef  uint16_t            ODK_RESULT;

#pragma pack(push,1)
typedef struct ODK_DTL_s
{
  ODK_UINT16      Year;
  ODK_UINT8       Month;
  ODK_UINT8       Day;
  ODK_UINT8       Weekday;
  ODK_UINT8       Hour;
  ODK_UINT8       Minute;
  ODK_UINT8       Second;
  ODK_UINT32      Nanosecond;
} ODK_DTL;
#pragma pack(pop)

// VARIANT
typedef struct ODK_VARIANT_s
{
  ODK_UINT32    Len;
  ODK_UINT8*    pData;
} ODK_VARIANT;


#pragma pack(push,1)
typedef struct
{
  ODK_BOOL Control_Flag_New_Service;
  ODK_BOOL Control_Flag_Item_Completed;
  ODK_BOOL Control_Flag_Service_Completed;
}control_flags;
#pragma pack(pop)

#pragma pack(push,1)
typedef struct
{
  ODK_UINT32 Id_Machine_Reference;
  ODK_UINT32 Id_Order_Reference;
  ODK_UINT32 Id_Batch_Reference;
  ODK_UINT32 Id_Ref_Subproduct_Type;
  ODK_UINT32 Operation_Ref_Service_Type;
  ODK_UINT8 Operation_No_of_Items;
}agent2plc;
#pragma pack(pop)

#pragma pack(push,1)
typedef struct
{
  ODK_UINT32 Id_Machine_Reference;
  ODK_UINT32 Id_Order_Reference;
  ODK_UINT32 Id_Batch_Reference;
  ODK_UINT32 Id_Ref_Subproduct_Type;
  ODK_UINT32 Id_Ref_Service_Type;
  ODK_UINT8 Id_Item_Number;
  ODK_LDT Data_Initial_Time_Stamp;
  ODK_LDT Data_Final_Time_Stamp;
  ODK_LDT Data_Service_Time_Stamp;
}plc2agent;
#pragma pack(pop)

#endif // ODK_Types_H