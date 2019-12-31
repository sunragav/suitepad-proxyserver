package com.sunragav.suitepad.di

import android.app.Application
import app.WebServerApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        DataModule::class,
        LocalDataModule::class,
        RemoteDataModule::class,
        AppModule::class,
        IntentsModule::class
    ]
)
interface AppComponent : AndroidInjector<WebServerApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent
    }

    override fun inject(app: WebServerApplication)
}