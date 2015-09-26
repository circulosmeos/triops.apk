LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := com.example.triops

LOCAL_SRC_FILES := \
    chacha20/chacha.c \
    chacha20/api.c \
    keccak/hash.c \
    keccak/keccak.c \
    triops.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/chacha20/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/chacha20/e/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/chacha20/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/keccak/

LOCAL_CFLAGS    := -iquote $(LOCAL_PATH) -O3

#LOCAL_CFLAGS   := -iquote $(LOCAL_PATH) -g -O3
#LOCAL_LDLIBS   := -llog

include $(BUILD_SHARED_LIBRARY)