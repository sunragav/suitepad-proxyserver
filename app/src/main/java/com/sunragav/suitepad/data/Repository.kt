package com.sunragav.suitepad.data

import android.net.Uri
import io.reactivex.disposables.CompositeDisposable


typealias successCallBack = (String) -> Unit

typealias failureCallback = (Throwable) -> Unit

interface Repository {
    fun getString(uri: Uri, disposable: CompositeDisposable): String
}