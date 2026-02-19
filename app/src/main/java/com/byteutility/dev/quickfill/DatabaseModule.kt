package com.byteutility.dev.quickfill

import android.content.Context
import androidx.room.Room
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
            SnippetDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideSnippetDao(database: SnippetDatabase): SnippetDao {
        return database.snippetDao()
    }
}