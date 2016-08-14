package com.music.msd;

import android.R.integer;

public class Mp3Decore {
	
	static {  
        System.loadLibrary("mad");  
    } 
	 //声明库函数
	 public native int initAudioPlayer(String file,long StartAddr);   
	 public native int getAudioBuf(short[] audioBuffer, int numSamples);  
	 public native void closeAduioFile();  
	 public native int getAudioSamplerate();  
	 //获取文件时间
	 public native int getAudiotime();
	 
	 //获得文件大小
	 public native int getAduioSize();
	 
	 //获得比特率
	 public native int getAduioBitrate();
	 //获得打开文件的当前位置
	 public native int getfileposition();
}
