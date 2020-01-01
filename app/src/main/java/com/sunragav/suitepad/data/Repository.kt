package com.sunragav.suitepad.data

import android.net.Uri
import io.reactivex.Single


interface Repository {
    fun getString(uri: Uri): Single<String>
}