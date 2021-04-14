#ifndef ODK_CPUREADWRITEDATA_H
#define ODK_CPUREADWRITEDATA_H

#include "ODK_Types.h"
#include "ODK_CpuReadData.h"

#if _MSC_VER > 1000
#pragma warning(disable:4514) 
#endif

//
// Declarations for the CODK_CpuReadWriteData class
//
// The read/write data access helper class serves as a wrapper to the 
// output buffer passed into the Execute function. You can use the 
// functions in this class to read and write STEP 7 data types to the 
// output buffer. It takes the raw input from the ODK Module Execute 
// function and provides structured access to the output data area.  
// Multibyte values are converted from little endian to big endian formats 
// as necessary.  C++ data types are converted to S7 data types in the write
// operation.  This class provides write access and inherits read access 
// (from CODK_CpuReadData) to the data area.
//

class CODK_CpuReadWriteData : public CODK_CpuReadData
{
public:

    //
    // CODK_CpuReadWriteData : Class constructor, initializes the output data area to 0
    //
    CODK_CpuReadWriteData();

    //
    // CODK_CpuReadWriteData : Class constructor, initializes the output data area
    //
    CODK_CpuReadWriteData(const ODK_CLASSIC_DB *db);  // Pointer to a classic db structure

    //
    // CODK_CpuReadWriteData : Class constructor, initializes the output data area
    //
    CODK_CpuReadWriteData(long     nBytes,  // Number of bytes in the data area
                          ODK_BYTE data[]); // Array reference to the data area

    //
    // ~CODK_CpuReadWriteData : Class destructor
    //
    virtual ~CODK_CpuReadWriteData();

    //
    // SetBuffer : Initializes the output data area and data size
    //
    void SetBuffer(long     nBytes,  // Size, in bytes, of the data buffer
                   ODK_BYTE data[]); // Array pointer to the data buffer

    //
    // LastByteChanged : Retrieves the index of the last byte changed in the data area
    //
    long LastByteChanged(void) const
    {
        if (m_HighIndex < m_LowIndex)
        {
            return -1;  // Case of no write operations
        }
        return m_HighIndex;
    }

    //
    // FirstByteChanged : Retrieves the index of the first byte changed in the data area
    //
    long FirstByteChanged(void) const
    {
        if (m_LowIndex > m_HighIndex)
        {
            return 0;  // Case of no write operations
        }
        return m_LowIndex;
    }

    //
    // WriteS7BYTE : Writes a byte value to the data area
    //
    bool WriteS7BYTE(const long      byteOffset, // Offset to begin writing
                     const ODK_UINT8 value)      // 8-bit unsigned value to write
    {
        return WriteUINT8(byteOffset, value);
    }

    //
    // WriteS7WORD : Writes a word (2 bytes) value to the data area
    //
    bool WriteS7WORD(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT16 value)      // 16-bit unsigned value to write
    {
        return WriteUINT16(byteOffset, value);
    }

    //
    // WriteS7DWORD : Writes a double word (4 bytes) value to the data area
    //
    bool WriteS7DWORD(const long       byteOffset, // Offset to begin writing
                      const ODK_UINT32 value)      // 32-bit unsigned value to write
    {
        return WriteUINT32(byteOffset, value);
    }

    //
    // WriteS7LWORD : Writes a long word (8 bytes) value to the data area
    //
    bool WriteS7LWORD(const long       byteOffset, // Offset to begin writing
                      const ODK_LWORD value)      // 64-bit unsigned value to write
    {
        return WriteUINT64(byteOffset, value);
    }

    //
    // WriteS7SINT : Writes an short integer (1 byte) value to the data area
    //
    bool WriteS7SINT(const long     byteOffset, // Offset to begin writing
                     const ODK_INT8 value)      // 8-bit signed value to write
    {
        return WriteUINT8(byteOffset, static_cast<ODK_UINT8>(value));
    }

    //
    // WriteS7INT : Writes an integer (2 bytes) value to the data area
    //
    bool WriteS7INT(const long      byteOffset, // Offset to begin writing
                    const ODK_INT16 value)      // 16-bit signed value to write
    {
        return WriteUINT16(byteOffset, static_cast<ODK_UINT16>(value));
    }

    //
    // WriteS7DINT : Writes a double integer (4 bytes) value to the data area
    //
    bool WriteS7DINT(const long      byteOffset, // Offset to begin writing
                     const ODK_INT32 value)      // 32-bit signed value to write
    {
        return WriteUINT32(byteOffset, static_cast<ODK_UINT32>(value));
    }

    //
    // WriteS7USINT : Writes an unsigned short integer (1 byte) value to the data area
    //
    bool WriteS7USINT(const long     byteOffset, // Offset to begin writing
                      const ODK_UINT8 value)      // 8-bit signed value to write
    {
        return WriteUINT8(byteOffset, value);
    }

    //
    // WriteS7UINT : Writes an unsigned integer (2 bytes) value to the data area
    //
    bool WriteS7UINT(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT16 value)      // 16-bit signed value to write
    {
        return WriteUINT16(byteOffset, value);
    }

    //
    // WriteS7UDINT : Writes a double unsigned integer (4 bytes) value to the data area
    //
    bool WriteS7UDINT(const long       byteOffset, // Offset to begin writing
                      const ODK_UINT32 value)      // 32-bit signed value to write
    {
        return WriteUINT32(byteOffset, value);
    }

    //
    // WriteS7S5TIME : Writes a 2 byte time value to the data area
    //
    bool WriteS7S5TIME(const long       byteOffset, // Offset to begin writing
                       const ODK_UINT16 value)      // 16-bit unsigned value to write
    {
        return WriteUINT16(byteOffset, value);
    }

    //
    // WriteS7TIME : Writes a 4 byte time value to the data area
    //
    bool WriteS7TIME(const long      byteOffset, // Offset to begin writing
                     const ODK_INT32 value)      // 32-bit signed value to write
    {
        return WriteUINT32(byteOffset, static_cast<ODK_UINT32>(value));
    }

    //
    // WriteS7DATE : Writes a date value (2 bytes) to the data area
    //
    bool WriteS7DATE(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT16 value)      // 16-bit unsigned value to write
    {
        return WriteUINT16(byteOffset, value);
    }

    //
    // WriteS7TIME_OF_DAY : Writes the time of day (4 bytes) to the data area
    //
    bool WriteS7TIME_OF_DAY(const long       byteOffset, // Offset to begin writing
                            const ODK_UINT32 value)      // 32-bit unsigned value to write
    {
        return WriteUINT32(byteOffset, value);
    }

    //
    // WriteS7CHAR : Writes a character (1 byte) to the data area
    //
    bool WriteS7CHAR(const long     byteOffset, // Offset to begin writing
                     const ODK_CHAR value)      // Character to write
    {
        return WriteUINT8(byteOffset, static_cast<ODK_UINT8>(value));
    }

    //
    // WriteS7REAL : Writes a real number (4 bytes) to the data area
    //
    bool WriteS7REAL(const long      byteOffset, // Offset to begin writing
                     const ODK_FLOAT value)      // Real value to write
    {
        return WriteUINT32(byteOffset,reinterpret_cast<const ODK_UINT32&>(value));
    }

    //
    // WriteS7LREAL : Writes a real number (8 bytes) to the data area
    //
    bool WriteS7LREAL(const long      byteOffset, // Offset to begin writing
                     const ODK_DOUBLE value)      // long real value to write
    {
    	return WriteUINT64(byteOffset,reinterpret_cast<const ODK_UINT64&>(value));
    }
    //
    // WriteS7LINT : Writes a integer number (8 bytes) to the data area
    //
    bool WriteS7LINT(const long      byteOffset, // Offset to begin writing
                     const ODK_INT64 value)      // 64-bitinteger value to write
    {
    	return WriteUINT64(byteOffset,static_cast<ODK_UINT64>(value));
    }
    //
    // WriteS7ULINT : Writes a unsigned integer number (8 bytes) to the data area
    //
    bool WriteS7ULINT(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT64 value)      // 64-bit unsigned value to write
    {
        return WriteUINT64(byteOffset, value);
    }
    //
    // WriteS7BOOL : Writes a boolean value (1 bit) to the data area
    //
    bool WriteS7BOOL(const long         byteOffset, // Offset to begin writing
                     const unsigned int bitNo,      // Bit index to change (bit 0 is the least significant bit)
                     const bool         value);     // Value to set the bit to

    //
    // WriteS7STRING : Writes a string to the data area
    //
    bool WriteS7STRING(const long      byteOffset, // Offset to begin writing
                       const ODK_CHAR *string);    // String to write

    //
    // WriteS7DATE_AND_TIME : Writes Date and Time data to Date and Time area
    //
    bool WriteS7DATE_AND_TIME(long           byteOffset, // Offset to begin writing
                              const ODK_DTL &timeData);  // Date and time structure to write

protected:

    //
    // WriteBYTE : Writes a generic 8-bit value to the data area
    //
    bool WriteUINT8(const long      byteOffset, // Offset to begin writing
                    const ODK_UINT8 value);     // 8-bit value to write

    //
    // WriteWORD : Writes a generic 16-bit value to the data area
    //
    bool WriteUINT16(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT16 value);     // 16-bit value to write

    //
    // WriteDWORD : Writes a generic 32-bit value to the data area
    //
    bool WriteUINT32(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT32 value);     // 32-bit value to write

    //
    // WriteUINT64 : Writes a generic 64-bit value to the data area
    //
    bool WriteUINT64(const long       byteOffset, // Offset to begin writing
                     const ODK_UINT64 value);     // 64-bit value to write

    //
    // Class data
    //
    long m_LowIndex;  // Lowest array index accessed
    long m_HighIndex; // Highest array index accessed (index + size of data - 1)

private:

    CODK_CpuReadWriteData(const CODK_CpuReadWriteData& right);
    CODK_CpuReadWriteData& operator=(const CODK_CpuReadWriteData& right);
};
#endif
