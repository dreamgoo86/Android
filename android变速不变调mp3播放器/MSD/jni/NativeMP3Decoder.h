#include <jni.h>  
#include <stdlib.h>  
  
  
  
int    NativeMP3Decoder_init(char * filepath,unsigned long start/*,unsigned long size*/);  
  
int NativeMP3Decoder_readSamples(short *target, int size);  
  
void  NativeMP3Decoder_closeAduioFile();  
  
int NativeMP3Decoder_getAduioSamplerate();

long NativeMP3Decoder_getAduioBitrate();

int NativeMP3Decoder_getAduioTime();
