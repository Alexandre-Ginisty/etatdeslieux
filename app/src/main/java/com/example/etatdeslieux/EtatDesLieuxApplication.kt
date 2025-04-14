package com.example.etatdeslieux

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

@HiltAndroidApp
class EtatDesLieuxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ImageLoader.Builder(this)
                .components {
            add(Interceptor { chain ->
                    val data = chain.request.data
                if (data is File) {
                    val bufferedSource = BufferedInputStream(FileInputStream(data))
                    chain.proceed(chain.request.newBuilder().data(bufferedSource).build())
                } else {
                    chain.proceed(chain.request)
                }
            })
        }
            .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
                .let { Coil.setImageLoader(it) }
    }
}