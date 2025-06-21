package com.grappim.taigamobile.core.storage.di

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.core.storage.server.ServerStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface StorageModule {

    @[Binds Singleton]
    fun bindServerStorage(impl: ServerStorageImpl): ServerStorage
}
