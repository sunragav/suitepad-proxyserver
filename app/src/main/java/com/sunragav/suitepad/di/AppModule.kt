package com.sunragav.suitepad.di

import android.app.Application
import android.content.Context
import com.sunragav.suitepad.proxyserver.ProxyWebServer
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: Application): Context

    @ContributesAndroidInjector
    internal abstract fun contributesProxyWebServer(): ProxyWebServer

}