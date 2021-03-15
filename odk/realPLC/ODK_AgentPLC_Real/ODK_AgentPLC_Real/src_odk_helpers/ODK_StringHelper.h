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
// Converts PLC string to C/C++ string.
// Truncate destination string, when source string is too long.
// returns -1    parameter invalid
//         0..n  number of copied characters
//
int Convert_S7STRING_to_SZSTR(const ODK_S7STRING* const src, // PLC string to be copied to destination string
                              char* dest,                    // C/C++ string (char array) which the source is going to be copied into
                              const int maxDestLen);         // Maximum size of destination buffer (including terminating zero)

//
// Converts C/C++ string to PLC string.
// Truncate destination string, when source string is too long.
// returns -1    parameter invalid
//         0..n  number of copied characters
//
int Convert_SZSTR_to_S7STRING(const char* const src, // C/C++ string (char array) which is going to be copied to destination string
                              ODK_S7STRING* dest);   // PLC string, which the source is going to be copied into

//
// Gets the current length of a PLC string.
// returns -1    parameter invalid
//         0..n  current number of characters in PLC string
//
int Get_S7STRING_Length(const ODK_S7STRING* const src);

//
// Gets the maximum length of a PLC string.
// returns -1    parameter invalid
//         0..n  maximal number of characters in PLC string
//
int Get_S7STRING_MaxLength(const ODK_S7STRING* const src);


//
// Converts PLC wstring to C/C++ wide string.
// Truncate destination string, when source string is too long.
// returns -1    parameter invalid
//         0..n  number of copied characters
//
int Convert_S7WSTRING_to_SZWSTR(const ODK_S7WSTRING* const src, // PLC wstring to be copied to destination wide string
                                wchar_t* dest,                  // C/C++ wstring (wchar_t array) which the source is going to be copied into
                                const int maxDestLen);          // Maximum size of destination buffer (includeing terminating zero)

//
// Converts C/C++ wide string to PLC wstring.
// Truncate destination string, when source string is too long.
// returns -1    parameter invalid
//         0..n  number of copied characters
//
int Convert_SZWSTR_to_S7WSTRING(const wchar_t* const src, // C/C++ wstring (wchar_t array) which is going to be copied to destination wstring
                                ODK_S7WSTRING* dest);     // PLC wstring, which the source is going to be copied into

//
// Gets the current length of a PLC wstring.
// returns -1    parameter invalid
//         0..n  current number of characters in PLC wstring
//
int Get_S7WSTRING_Length(const ODK_S7WSTRING* const src);

//
// Gets the maximum length of a PLC wstring.
// returns -1    parameter invalid
//         0..n  maximal number of characters in PLC wstring
//
int Get_S7WSTRING_MaxLength(const ODK_S7WSTRING* const src);

#ifdef __cplusplus
}
#endif

#endif