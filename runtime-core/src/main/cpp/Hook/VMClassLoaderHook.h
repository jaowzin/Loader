



#ifndef CARROMRUNTIME_VMCLASSLOADERHOOK_H
#define CARROMRUNTIME_VMCLASSLOADERHOOK_H


#include "BaseHook.h"
#include <jni.h>

class VMClassLoaderHook : public BaseHook {
public:
    static void hideXposed();
    static void init(JNIEnv *env);
};


#endif 
