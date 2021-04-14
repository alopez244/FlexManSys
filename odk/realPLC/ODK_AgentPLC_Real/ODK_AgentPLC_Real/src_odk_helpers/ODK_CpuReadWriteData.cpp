#include "stdafx.h"
#include "ODK_CpuReadWriteData.h"

//
// ODK_CpuReadWriteData.cpp, implementation for the class CODK_CpuReadWriteData
//
// The read/write data access helper class serves as a wrapper to the output 
// buffer passed into the Execute function. You can use the functions in 
// this class to read and write STEP 7 data types to the output buffer.
// It takes the raw input from the ODK Module Execute function and provides 
// structured access to the output data area.  Multibyte values are 
// converted from little endian to big endian formats as necessary.  C++ 
// data types are converted to S7 data types in the write operation.  This 
// class provides write access and inherits read access (from CODK_CpuReadData) 
// to the data area.
//

//
// CODK_CpuReadWriteData : Class constructor, initializes the output data area to 0
//
CODK_CpuReadWriteData::CODK_CpuReadWriteData()
    : CODK_CpuReadData(),
      m_LowIndex(0),
      m_HighIndex(0)
{
}

//
// CODK_CpuReadWriteData : Class constructor. Initializes the output data area
//
CODK_CpuReadWriteData::CODK_CpuReadWriteData(const ODK_CLASSIC_DB *db) // Pointer to a classic db structure
{
    SetBuffer(db->Len, (ODK_BYTE *)db->Data);
}

//
// CODK_CpuReadWriteData : Class constructor. Initializes the output data area
//
CODK_CpuReadWriteData::CODK_CpuReadWriteData(long     nBytes, // Number of bytes in the data buffer
                                             ODK_BYTE data[]) // Array pointer to the data buffer
{
    SetBuffer(nBytes, data);
}

//
// ~CODK_CpuReadWriteData : Class destructor
//
CODK_CpuReadWriteData::~CODK_CpuReadWriteData()
{
}

//
// SetBuffer : Initializes the output data area and data size
//
void CODK_CpuReadWriteData::SetBuffer(long     nBytes, // Number of bytes in the data buffer
                                      ODK_BYTE data[]) // Array pointer to the data buffer
{
      if (0 == data)
      {
            m_nBytes = 0;
            m_LowIndex = 0;
            m_HighIndex = 0;
      }
      else
      {
            m_nBytes = nBytes;
            m_LowIndex = nBytes;
            m_HighIndex = -1;
      }
      m_data = data;
}

//
// WriteUINT8 : Writes a generic 8-bit value to the data area
//
bool CODK_CpuReadWriteData::WriteUINT8(const long      byteOffset, // Index into the data buffer
                                       const ODK_UINT8 value)      // Value to write into the buffer
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT8) > m_nBytes))
    {
        return false;
    }

    // Update highest index accessed
    if ((byteOffset + ODK_SIZEBIT8 - 1) > m_HighIndex)
    {
        m_HighIndex = byteOffset + ODK_SIZEBIT8 - 1;
    }

    // Update lowest index accessed
    if (byteOffset < m_LowIndex)
    {
        m_LowIndex = byteOffset;
    }

    // Convert and write the data
    m_data[byteOffset] = (ODK_UINT8)value; 

    return true;
}

//
// WriteUINT16 : Writes a generic 16-bit value to the data area
//
bool CODK_CpuReadWriteData::WriteUINT16(const long       byteOffset, // Index into the data buffer
                                        const ODK_UINT16 value)      // Value to write into the buffer
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT16) > m_nBytes))
    {
        return false;
    }

    // Update highest index accessed
    if ((byteOffset + ODK_SIZEBIT16 - 1) > m_HighIndex)
    {
        m_HighIndex = byteOffset + ODK_SIZEBIT16 - 1;
    }

    // Update lowest index accessed
    if (byteOffset < m_LowIndex)
    {
        m_LowIndex = byteOffset;
    }

    // Convert and write the data
    ODK_UINT16 word = (((value & 0x00FFU) << 8) |
                       ((value & 0xFF00U) >> 8));

    reinterpret_cast<ODK_UINT16&>(m_data[byteOffset]) = word;

    return true;
}

//
// WriteUINT32 : Writes a generic 32-bit value to the data area
//
bool CODK_CpuReadWriteData::WriteUINT32(const long       byteOffset, // Index into the data buffer
                                        const ODK_UINT32 value)      // Value to write into the buffer
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT32) > m_nBytes))
    {
        return false;
    }

    // Update highest index accessed
    if ((byteOffset + ODK_SIZEBIT32 - 1) > m_HighIndex)
    {
        m_HighIndex = byteOffset + ODK_SIZEBIT32 - 1;
    }

    // Update lowest index accessed
    if (byteOffset < m_LowIndex)
    {
        m_LowIndex = byteOffset;
    }

    // Convert and write the data
    ODK_UINT32 dword = (((value & 0x000000FFUL) << 24) |
                        ((value & 0x0000FF00UL) <<  8) |
                        ((value & 0x00FF0000UL) >>  8) |
                        ((value & 0xFF000000UL) >> 24));

    reinterpret_cast<ODK_UINT32&>(m_data[byteOffset]) = dword;

    return true;
}

//
// WriteUINT64 : Writes a generic 64-bit value to the data area
//
bool CODK_CpuReadWriteData::WriteUINT64(const long       byteOffset, // Index into the data buffer
                                        const ODK_UINT64 value)      // Value to write into the buffer
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT64) > m_nBytes))
    {
        return false;
    }

    // Update highest index accessed
    if ((byteOffset + ODK_SIZEBIT64 - 1) > m_HighIndex)
    {
        m_HighIndex = byteOffset + ODK_SIZEBIT64 - 1;
    }

    // Update lowest index accessed
    if (byteOffset < m_LowIndex)
    {
        m_LowIndex = byteOffset;
    }

    // Convert and write the data
    ODK_UINT64 qword = (((value & 0x00000000000000FFULL) << 56) |
                        ((value & 0x000000000000FF00ULL) << 40) |
                        ((value & 0x0000000000FF0000ULL) << 24) |
                        ((value & 0x00000000FF000000ULL) <<  8) |
                        ((value & 0x000000FF00000000ULL) >>  8) |
                        ((value & 0x0000FF0000000000ULL) >> 24) |
                        ((value & 0x00FF000000000000ULL) >> 40) |
                        ((value & 0xFF00000000000000ULL) >> 56));

    reinterpret_cast<ODK_UINT64&>(m_data[byteOffset]) = qword;

    return true;
}

//
// WriteS7BOOL : Writes a boolean value to the data area
//
bool CODK_CpuReadWriteData::WriteS7BOOL(const long         byteOffset, // Index into the data buffer
                                        const unsigned int bitNo,      // Number of the bit to write (index from right to left)
                                        const bool         value)      // Value of the bit to write
{
    // verify correct bit range
    if (bitNo > 7)
    {
        return false;
    }

    // Bit indexing: least significant bit is 0 (i.e. go right to left)

    ODK_UINT8 temp;  // variable for the byte's current value

    // Get the current byte so the other bit values can be maintained
    if (ReadUINT8(byteOffset, temp) == false)
    {
        return false;
    }

    // Set the bit in the byte to be written
    if (true == value)
    {
        temp = static_cast<ODK_UINT8>(temp | (1U << bitNo));
    }
    else
    {
        temp = static_cast<ODK_UINT8>(temp & (~(1U << bitNo)));
    }
    // Write the byte
    return WriteUINT8(byteOffset, temp);
}

//
// WriteS7STRING : Writes a string to the data area
//
bool CODK_CpuReadWriteData::WriteS7STRING(const long      byteOffset, // Index into the data buffer
                                          const ODK_CHAR *string)     // Pointer to a string buffer containing the string to write
{
    ODK_UINT8 maxLen = 0;
    ODK_UINT8 curLen = 0;
    short count = 0;
    short strDataIndex = 0;

    // verify valid pointer 
    if (0 == string)
    {
        return false;
    }

    // get the maximum length of the string
    ReadUINT8(byteOffset, maxLen);

    // get the current size of the string
    curLen = static_cast<ODK_UINT8>(strlen(string));

    // return error if string is too large for the buffer (STEP 7 string or output data buffer)
    if ((curLen > maxLen) || ((byteOffset + static_cast<long>(curLen) + ODK_SIZEBIT16) > m_nBytes))
    {
        return false;
    }

    // set the current length
    WriteUINT8(byteOffset + ODK_SIZEBIT8, curLen);
    // Copy the string
    strDataIndex = static_cast<short>(byteOffset + ODK_SIZEBIT16);
    count = 0;
    while (string[count] != '\0')
    {
        WriteUINT8(strDataIndex + count, string[count]);
        count++;
    }

    return true;
}

//
// WriteS7DATE_AND_TIME : Writes Date and Time data to  Date and Time area
//
bool CODK_CpuReadWriteData::WriteS7DATE_AND_TIME(long           byteOffset, // Index into the data buffer
                                                 const ODK_DTL &timeData)   // Date and time structure to write
{
    // Verify offset is within data area
    if ((byteOffset < 0) || ((byteOffset + ODK_SIZEBIT64) > m_nBytes))
    {
        return false;
    }

    // Update highest index accessed
    if ((byteOffset + ODK_SIZEBIT64 - 1) > m_HighIndex)
    {
        m_HighIndex = byteOffset + ODK_SIZEBIT64 - 1;
    }

    // Update lowest index accessed
    if (byteOffset < m_LowIndex)
    {
        m_LowIndex = byteOffset;
    }

    // Convert and write the data
    WriteUINT16(byteOffset, timeData.Year);
    byteOffset += ODK_SIZEBIT16;
    WriteUINT8(byteOffset, timeData.Month);
    byteOffset += ODK_SIZEBIT8;
    WriteUINT8(byteOffset, timeData.Day);
    byteOffset += ODK_SIZEBIT8;
    WriteUINT8(byteOffset, timeData.Weekday);
    byteOffset += ODK_SIZEBIT8;
    WriteUINT8(byteOffset, timeData.Hour);
    byteOffset += ODK_SIZEBIT8;
    WriteUINT8(byteOffset, timeData.Minute);
    byteOffset += ODK_SIZEBIT8;
    WriteUINT8(byteOffset, timeData.Second);
    byteOffset += ODK_SIZEBIT8;
    WriteUINT32(byteOffset, timeData.Nanosecond);

    return true;
}
