package de.paarfinanz.app

import android.app.Application
import de.paarfinanz.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class PaarfinanzApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@PaarfinanzApplication) }
    }
}
