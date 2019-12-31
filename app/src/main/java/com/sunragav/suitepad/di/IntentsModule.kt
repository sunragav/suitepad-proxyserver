package com.sunragav.suitepad.di

import android.content.ComponentName
import android.content.Intent
import com.sunragav.suitepad.proxyserver.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey


@Module
object IntentsModule {
    @Provides
    @JvmStatic
    @IntoMap
    @StringKey("WebView")
    fun getWebViewIntent() = Intent().also {
        it.component = ComponentName(
            BuildConfig.WEBVIEW_APPLICATION_ID,
            BuildConfig.WEBVIEW_CLASSNAME
        )
        it.action = Intent.ACTION_MAIN
        it.addCategory(Intent.CATEGORY_LAUNCHER)
    }

    @Provides
    @JvmStatic
    @IntoMap
    @StringKey("FileProvider")
    fun getFileProviderIntent() = Intent().also {
        it.component = ComponentName(
            BuildConfig.FILEPROVIDER_APPLICATION_ID,
            BuildConfig.FILEPROVIDER_CLASSNAME
        )
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        it.action = BuildConfig.GET_URI_ACTION
    }


    @Provides
    @JvmStatic
    @IntoMap
    @StringKey("Broadcast")
    fun getBroadcastIntent() = Intent().apply {
        action =
            BuildConfig.BROADCAST_ACTION_PROXY_STARTED
        addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
    }

}