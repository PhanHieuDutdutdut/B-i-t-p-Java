package org.Whispercpp;


public class WhisperJNI {
    static {
        System.loadLibrary("whisper-jni"); // Load thư viện native .dll hoặc .so
    }

    public native String transcribeAudio(String audioPath); // native method bạn tự định nghĩa trong whisper-jni.cpp
}
