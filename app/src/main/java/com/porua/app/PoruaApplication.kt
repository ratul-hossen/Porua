package com.porua.app

import android.app.Application
import com.porua.app.data.local.AppDatabase
import com.porua.app.data.repository.PoruaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PoruaApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { PoruaRepository(database) }
}
