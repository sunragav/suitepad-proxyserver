package com.sunragav.suitepad.nativelib

class KeyStoreHelper {


    @Throws(IllegalArgumentException::class)
    external fun getPass(): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}