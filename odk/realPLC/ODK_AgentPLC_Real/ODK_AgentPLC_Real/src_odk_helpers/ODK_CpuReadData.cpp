#include "stdafx.h"
#include "ODK_CpuReadData.h"

//
// ODK_CpuReadData.cpp, implementation of the CODK_CpuReadData class
//
// The read-only data access helper class serves as a wrapper to the input 
// buffer passed into the Execute function. You can use functions in this 
// class to access the data in the input buffer as STEP 7 data types.
// It takes the raw input from the ODK Module Execute function and provides 
// structured access to the input data area.  Multibyte values are converted 
// from little endian to big endian as necessary.  S7 data types are 
// converted to C++ data types in the read operation.  This class provides 
// only Read access to the data area.
//

//
// CODK_CpuReadData : Class constructor, initializes the input data area to 0
//
CODK_CpuReadData::CODK_CpuReadData()
    : m_nBytes(0), 
      m_data(0)
{
}

//
// CODK_CpuReadData : Class constructor. Initializes the output data area
//
CODK_CpuReadData::CODK_CpuReadData(const ODK_CLASSIC_DB *db) // Pointer to a classic db structure
{
    SetBuffer(db->Len, (ODK_BYTE *)db->Data);
}

//
// CODK_CpuReadData : Class constructor, initializes the input data area
//
CODK_CpuReadData::CODK_CpuReadData(long      nBytes, // Size, in bytes, of the data buffer
                                   ODK_BYTE *data)   // Pointer to the data buffer
{
    SetBuffer(nBytes, data);
}

//
// ~CODK_CpuReadData : Class destructor
//
CODK_CpuReadData::~CODK_CpuReadData()
{
}

//
// ODK_SetBuffer : Initializes the input data area and data size
//
void CODK_CpuReadData::SetBuffer(long     nBytes, 
                                 ODK_BYTE data[])
{
    if (0 == data)
    {
        m_nBytes = 0;
    }
    else
    {
        m_nBytes = nBytes;
    }
    m_data = data;
}

//
// ReadUINT8 : Reads a generic 8-bit value from the data area (used by public read methods)
//
bool CODK_CpuReadData::ReadUINT8(const long  byteOffset, // Buffer index to the data
                                 ODK_UINT8  &value)      // Variable to store the read value
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT8) > m_nBytes))
    {
        return false;
    }

    // Read, convert, and return the data
    value = reinterpret_cast<ODK_UINT8&>(m_data[byteOffset]);

    return true;
}

//
// ReadUINT16 : Reads a generic 16-bit value from the data area. Used by public read methods
//
bool CODK_CpuReadData::ReadUINT16(const long  byteOffset, // Buffer index to the data
                                  ODK_UINT16 &value)      // Variable to store the read value
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT16) > m_nBytes))
    {
        return false;
    }

    // Read, convert, and return the data
    ODK_UINT16 word = reinterpret_cast<ODK_UINT16&>(m_data[byteOffset]);
    value = (((word & 0x00FFU) << 8) |
             ((word & 0xFF00U) >> 8));

    return true;
}

//
// ReadUINT32 : Reads a generic 32-bit value from the data area. Used by public read methods
//
bool CODK_CpuReadData::ReadUINT32(const long   byteOffset, // Buffer index to the data
                                  ODK_UINT32 &value)       // Variable to store the read value
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT32) > m_nBytes))
    {
        return false;
    }

    // Read, convert, and return the data
    ODK_UINT32 dword = reinterpret_cast<ODK_UINT32&>(m_data[byteOffset]);
    value = (((dword & 0x000000FFUL) << 24) |
             ((dword & 0x0000FF00UL) <<  8) |
             ((dword & 0x00FF0000UL) >>  8) |
             ((dword & 0xFF000000UL) >> 24));

    return true;
}

//
// ReadUINT64 : Reads a generic 64-bit value from the data area. Used by public read methods
//
bool CODK_CpuReadData::ReadUINT64(const long  byteOffset, // Buffer index to the data
                                  ODK_UINT64 &value)      // Variable to store the read value
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT64) > m_nBytes))
    {
        return false;
    }

    // Read, convert, and return the data
    ODK_UINT64 qword = reinterpret_cast<ODK_UINT64&>(m_data[byteOffset]);
    value = (((qword & 0x00000000000000FFULL) << 56) |
             ((qword & 0x000000000000FF00ULL) << 40) |
             ((qword & 0x0000000000FF0000ULL) << 24) |
             ((qword & 0x00000000FF000000ULL) <<  8) |
             ((qword & 0x000000FF00000000ULL) >>  8) |
             ((qword & 0x0000FF0000000000ULL) >> 24) |
             ((qword & 0x00FF000000000000ULL) >> 40) |
             ((qword & 0xFF00000000000000ULL) >> 56));

    return true;
}

//
// ReadS7BOOL : Reads a boolean value from the data area
//
bool CODK_CpuReadData::ReadS7BOOL(const long  byteOffset, // Buffer index to the data
                                  int         bitNo,      // Bit number within the byte to read
                                  bool       &value)      // Variable to store the read value
{
    ODK_UINT8 temp;

    // Verify valid bit index
    if ((bitNo < 0) || (bitNo > 7))
    {
        return false;
    }

    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT8) > m_nBytes))
    {
        return false;
    }

    // read the byte containing the bit
    if (ReadUINT8(byteOffset, temp) == false)
    {
        return false;
    }

    // Shift the bit to the first location
    temp = static_cast<ODK_UINT8>(temp >> bitNo);

    // Isolate the value
    if ((temp & 1) == 1)
    {
        value = true;
    }
    else
    {
        value = false;
    }
    return true;
}

//
// ReadS7STRING_LEN : Reads the string length information for an S7 String in the data area
//
bool CODK_CpuReadData::ReadS7STRING_LEN(const long  byteOffset, // Buffer index to the data
                                        ODK_UINT8  &maxLen,     // Variable to store the maximum length of the string
                                        ODK_UINT8  &curLen)     // Variable to store the current length of the string
{
    if (ReadUINT8(byteOffset, maxLen) == false)
    {
        return false;
    }

    if (ReadUINT8(byteOffset+1, curLen) == false)
    {
        return false;
    }

    return true;
}

//
// ReadS7STRING : Reads an S7 string from the data area, and returns it as a C++ character string
//
bool CODK_CpuReadData::ReadS7STRING(const long  byteOffset, // Buffer index to the data
                                    ODK_UINT8   readMax,    // Maximum characters to read including the string termination character
                                    ODK_CHAR   *string)     // Buffer to receive the string characters
{
    ODK_UINT8  count   = 0; // Current index being read
    ODK_UINT8  curLen  = 0; // Current length of the string
    ODK_UINT8  maxLen  = 0; // Maximum length of the string
    ODK_UINT8  stop    = 0; // Maximum bytes to read
    ODK_UINT16 strData = 0; // Offset to the string data (characters)
    ODK_UINT8  temp    = 0; // Current byte read

    // must receive a valid buffer for the string
    if (0 == string)
    {
        return false;
    }

    // must have space for 1 character and the string terminator
    if (readMax < 2)
    {
        return false;
    }

    // Get the current string length
    if (ReadS7STRING_LEN(byteOffset, maxLen, curLen) == false)
    {
        return false;
    }

    // Verify offset not out of bounds
    if ((byteOffset < 0) || (byteOffset + static_cast<long>(maxLen) + ODK_SIZEBIT16 > m_nBytes))
    {
        return false;
    }

    // Set Offset to String characters
    strData = static_cast<ODK_UINT16>(byteOffset + ODK_SIZEBIT16);

    // Get the value to read to
    if (curLen < readMax)
    {
        stop = curLen;
    }
    else
    {
        stop = static_cast<ODK_UINT8>(readMax - 1);
    }

    // Read the value
    for (count = 0; count < stop; count++)
    {
        ReadUINT8(strData + count, temp);
        string[count] = static_cast<char>(temp);
    }
    string[count] = '\0';  // terminate the string

    return true;
}

//
// ReadS7DATE_AND_TIME : Reads a generic Date and Time area
//
bool CODK_CpuReadData::ReadS7DATE_AND_TIME(long      byteOffset, // Offset to begin reading
                                           ODK_DTL &timeData)   // Date and time structure read
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT64) > m_nBytes))
    {
        return false;
    }

    // Read, convert, and return the data
    ReadUINT16(byteOffset, timeData.Year);
    byteOffset += ODK_SIZEBIT16;
    ReadUINT8(byteOffset, timeData.Month);
    byteOffset += ODK_SIZEBIT8;
    ReadUINT8(byteOffset, timeData.Day);
    byteOffset += ODK_SIZEBIT8;
    ReadUINT8(byteOffset, timeData.Weekday);
    byteOffset += ODK_SIZEBIT8;
    ReadUINT8(byteOffset, timeData.Hour);
    byteOffset += ODK_SIZEBIT8;
    ReadUINT8(byteOffset, timeData.Minute);
    byteOffset += ODK_SIZEBIT8;
    ReadUINT8(byteOffset, timeData.Second);
    byteOffset += ODK_SIZEBIT8;
    ReadUINT32(byteOffset, timeData.Nanosecond);

    return true;
}
