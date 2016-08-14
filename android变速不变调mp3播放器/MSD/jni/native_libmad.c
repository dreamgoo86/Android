#define TAG "native_libmad"

#include "FileSystem.h"  
#include <stdlib.h>  
#include <jni.h>  
#include <android/log.h>  
  
  
 extern int NativeMP3Decoder_readSamples( short *target, int size);  
   
 extern void  NativeMP3Decoder_closeAduioFile();  
   
 extern int NativeMP3Decoder_getAduioSamplerate();  
   
 extern int NativeMP3Decoder_init(char * filepath,unsigned long start);
  
extern int NativeMP3Decoder_getAduioTime();

//获得比特率
extern long NativeMP3Decoder_getAduioBitrate();

//获得文件大小
extern long NativeMP3Decoder_getAduioSize();
//获得文件当前位置
extern int NativeMP3Decoder_getfileposition();

  
 jint Java_com_music_msd_Mp3Decore_initAudioPlayer(JNIEnv *env, jobject obj, jstring file, jlong startAddr)
{  
      
     char * fileString = (*env)->GetStringUTFChars(env,file, NULL);
      
    return  NativeMP3Decoder_init(fileString,startAddr);  
  
}  
  
 jint Java_com_music_msd_Mp3Decore_getAudioBuf(JNIEnv *env, jobject obj ,jshortArray audioBuf,jint len)
{  
    int bufsize = 0;  
    int ret = 0;  
    if (audioBuf != NULL) {  
        bufsize = (*env)->GetArrayLength(env, audioBuf);  
        jshort *_buf = (*env)->GetShortArrayElements(env, audioBuf, 0);  
        memset(_buf, 0, bufsize*2);  
        ret = NativeMP3Decoder_readSamples(_buf, len);  
        (*env)->ReleaseShortArrayElements(env, audioBuf, _buf, 0);  
    }  
    else{  
  
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "getAudio failed");  
        }  
    return ret;  
}  
   
 jint Java_com_music_msd_Mp3Decore_getAudioSamplerate()
{  
    return NativeMP3Decoder_getAduioSamplerate();  
}  
  //NativeMP3Decoder_getAduioTime
 jint Java_com_music_msd_Mp3Decore_getAudiotime()
 {
     return NativeMP3Decoder_getAduioTime();
 }


 //获得文件大小
jint Java_com_music_msd_Mp3Decore_getAduioSize()
{
    return NativeMP3Decoder_getAduioSize();
}


//获得比特率
jint Java_com_music_msd_Mp3Decore_getAduioBitrate()
{
   return NativeMP3Decoder_getAduioBitrate();
}
//extern int NativeMP3Decoder_getfileposition();
//获得文件当前位置
jint Java_com_music_msd_Mp3Decore_getfileposition()
{
   return NativeMP3Decoder_getfileposition();
}

 void Java_com_music_msd_Mp3Decore_closeAduioFile( )
  
{  
    NativeMP3Decoder_closeAduioFile();  
  
}  
