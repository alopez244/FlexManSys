#include "stdafx.h"
#include "ODK_StringHelper.h"

int Convert_S7STRING_to_SZSTR(const ODK_S7STRING* const src,  char* dest, const int maxDestLen)
{
    int ret = -1;
    int currentLen = src[1];

    if (currentLen < maxDestLen)
    {
        memcpy(dest, &(src[2]), currentLen); 
        dest[currentLen] = '\0';
        ret = currentLen;
    }
    return ret;
}

int Convert_SZSTR_to_S7STRING(const char* const src, ODK_S7STRING* dest)
{
    int ret = -1;
    size_t currentLen = strlen(src);

    if (dest[0] >= currentLen)
    {
        memcpy(&(dest[2]), src, currentLen);
        dest[1] = (ODK_S7STRING) currentLen;
        ret = dest[1];
    }
    return ret;
}

int Get_S7STRING_Length(const ODK_S7STRING* const src)
{
    return src[1];
}

int Get_S7STRING_MaxLength(const ODK_S7STRING* const src)
{
    return src[0];
}

int Convert_S7WSTRING_to_SZWSTR(const ODK_S7WSTRING* const src, wchar_t* dest, int maxDestLen)
{
    int ret = -1;
    int currentLen = src[1];
    if (currentLen < maxDestLen)
    {
        memcpy(dest, &(src[2]), currentLen * sizeof(wchar_t));
        dest[currentLen] = '\0';
        ret = currentLen;
    }
    return ret;
}

int Convert_SZWSTR_to_S7WSTRING(const wchar_t* const src, ODK_S7WSTRING* dest)
{ 
    int ret = -1;
    size_t currentLen = wcslen(src);

    if (dest[0] >= currentLen)
    {
        memcpy(&(dest[2]), src, currentLen * sizeof(wchar_t));
        dest[1] = (ODK_S7WSTRING) currentLen;
        ret =  dest[1];
    }
    return ret;
}

int Get_S7WSTRING_Length(const ODK_S7WSTRING* const src)
{
    return src[1];
}

int Get_S7WSTRING_MaxLength(const ODK_S7WSTRING* const src)
{
    return src[0];
}
