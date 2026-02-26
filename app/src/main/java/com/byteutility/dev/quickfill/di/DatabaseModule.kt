package com.byteutility.dev.quickfill.di

import android.content.Context
import androidx.room.Room
import com.byteutility.dev.quickfill.data.local.SnippetDao
import com.byteutility.dev.quickfill.data.local.SnippetDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SnippetDatabase {
        return Room.databaseBuilder(
            context,
            SnippetDatabase::class.java,
            SnippetDatabase.Companion.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideSnippetDao(database: SnippetDatabase): SnippetDao {
        return database.snippetDao()
    }
}