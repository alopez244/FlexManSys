#ifndef ODK_STRINGHELPER_H
#define ODK_STRINGHELPER_H

#include "ODK_Types.h"

#if _MSC_VER > 1000
#pragma warning(disable:4514) 
#endif

#ifdef __cplusplus
extern "C"
{
#endif
//
// Converts PLC string type to C/C++ string type char array
// returns -1 if maxDestLen <= length of source buffer or the count of copied characters.
//
int Convert_S7STRING_to_SZSTR(const ODK_S7STRING* const src, // PLC type string to be copied to destination string
                              char* dest,                    // C/C++ type string(char array) which the source is going to be copied into
                              const int maxDestLen);         // Maximum size of destination buffer. It must also include the null termination character.

//
// Converts  C/C++ string type char array to PLC string type
// returns -1 if length of destination < length of source .or the count of copied characters.
//
int Convert_SZSTR_to_S7STRING(const char* const src, // C/C++ type string(char array) which is going to be copied to destination string
                              ODK_S7STRING* dest);   // PLC type string  which the source is going to be copied into

//
// Gets the current length of a PLC string type.
//
int Get_S7STRING_Length(const ODK_S7STRING* const src);

//
// Gets the maximum length of a PLC string type.
//
int Get_S7STRING_MaxLength(const ODK_S7STRING* const src);

//
// Converts PLC wstring type to C/C++ wstring type wchar_t array
// returns -1 if maxDestLen <= length of source buffer.or the count of copied characters
//
int Convert_S7WSTRING_to_SZWSTR(const ODK_S7WSTRING* const src, // PLC type wstring to be copied to destination wstring
                                wchar_t* dest,                  // C/C++ type wstring(wchar_t array) which the source is going to be copied into
                                const int maxDestLen);          // Maximum size of destination buffer. It must also include the null termination character

//
// Converts  C/C++ wstring type wchar_t array to PLC string type
// returns -1 if length of destination < length of source or the count of copied characters
//
int Convert_SZWSTR_to_S7WSTRING(const wchar_t* const src, // C/C++ type wstring(wchar_t array) which is going to be copied to destination wstring
                                ODK_S7WSTRING* dest);     // PLC type wstring  which the source is going to be copied into

//
// Gets the current length of a PLC wstring type
//
int Get_S7WSTRING_Length(const ODK_S7WSTRING* const src);

//
// Gets the maximum length of a PLC wstring type
//
int Get_S7WSTRING_MaxLength(const ODK_S7WSTRING* const src);

#ifdef __cplusplus
}
#endif

#endif