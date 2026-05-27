package com.byteutility.dev.quickfill.di

import android.content.Context
import androidx.room.Room
import com.byteutility.dev.quickfill.BuildConfig
import com.byteutility.dev.quickfill.data.local.SnippetDao
import com.byteutility.dev.quickfill.data.local.SnippetDatabase
import com.byteutility.dev.quickfill.data.repository.AutofillSettingsRepository
import com.byteutility.dev.quickfill.data.repository.DefaultAutofillSettingsRepository
import com.byteutility.dev.quickfill.data.repository.DefaultSnippetRepository
import com.byteutility.dev.quickfill.data.repository.SnippetRepository
import com.byteutility.dev.quickfill.util.SecurityManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindSnippetRepository(
        defaultSnippetRepository: DefaultSnippetRepository
    ): SnippetRepository

    @Binds
    @Singleton
    abstract fun bindAutofillSettingsRepository(
        defaultAutofillSettingsRepository: DefaultAutofillSettingsRepository
    ): AutofillSettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
            securityManager: SecurityManager
        ): SnippetDatabase {
            val builder = Room.databaseBuilder(
                context,
                SnippetDatabase::class.java,
                SnippetDatabase.Companion.DATABASE_NAME
            )

            if (!BuildConfig.DEBUG) {
                SQLiteDatabase.loadLibs(context)
                val factory = SupportFactory(securityManager.getDatabaseEncryptionKey())
                builder.openHelperFactory(factory)
            }

            return builder.build()
        }

        @Provides
        fun provideSnippetDao(database: SnippetDatabase): SnippetDao {
            return database.snippetDao()
        }
    }
}
