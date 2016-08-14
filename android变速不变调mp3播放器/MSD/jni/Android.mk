#ifeq ($(strip $(BUILD_WITH_GST)),true)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	version.c \
	fixed.c \
	bit.c \
	timer.c \
	stream.c \
	frame.c  \
	synth.c \
	decoder.c \
	layer12.c \
	layer3.c \
	huffman.c \
	FileSystem.c \
	NativeMP3Decoder.c \
	native_libmad.c


LOCAL_ARM_MODE := arm
LOCAL_MODULE:= libmad
LOCAL_C_INCLUDES := $(LOCAL_PATH)/android 
LOCAL_LDLIBS    += -llog
LOCAL_CFLAGS := -DHAVE_CONFIG_H -DFPM_ARM -ffast-math -O3


include $(BUILD_SHARED_LIBRARY)

#endif


include $(CLEAR_VARS)

LOCAL_MODULE := soundtouch

LOCAL_SRC_FILES := \
	SoundTouch/AAFilter.cpp \
	SoundTouch/BPMDetect.cpp \
	SoundTouch/cpu_detect_x86.cpp \
	SoundTouch/FIFOSampleBuffer.cpp \
	SoundTouch/FIRFilter.cpp \
	SoundTouch/mmx_optimized.cpp \
	SoundTouch/PeakFinder.cpp \
	SoundTouch/RateTransposer.cpp \
	SoundTouch/SoundTouch.cpp \
	SoundTouch/sse_optimized.cpp \
	SoundTouch/TDStretch.cpp \
	soundtouch_wapper.cpp \

include $(BUILD_SHARED_LIBRARY)
