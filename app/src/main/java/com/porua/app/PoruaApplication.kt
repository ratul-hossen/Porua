package com.porua.app

import android.app.Application
import com.porua.app.data.auth.AuthDataStore
import com.porua.app.data.local.AppDatabase
import com.porua.app.data.repository.AuthRepository
import com.porua.app.data.repository.LocalAuthRepository
import com.porua.app.data.repository.PoruaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

import kotlinx.coroutines.launch
import com.porua.app.data.ai.AiHelper

class PoruaApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { PoruaRepository(database) }
    val authDataStore by lazy { AuthDataStore(this) }
    val authRepository: AuthRepository by lazy { LocalAuthRepository(authDataStore, repository) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            authDataStore.geminiApiKey.collect { key ->
                AiHelper.userGoogleApiKey = key
            }
        }
        applicationScope.launch {
            authDataStore.grokApiKey.collect { key ->
                AiHelper.userGrokApiKey = key
            }
        }
    }
}
