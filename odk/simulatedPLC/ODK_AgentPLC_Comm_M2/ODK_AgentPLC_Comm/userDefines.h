#pragma once
#include "stdafx.h"
#include "ODK_Functions.h"
#include "jni.h"

#ifndef _ADR_DEFINES_H
#define _ADR_DEFINES_H

bool JNIinit(void);

JNIEnv *env;
JavaVM *jvm;
jclass cls;
jmethodID JNI_init, JNI_send, JNI_recv;
char traza[300];				//Array para formar el mensaje de la traza

#endif