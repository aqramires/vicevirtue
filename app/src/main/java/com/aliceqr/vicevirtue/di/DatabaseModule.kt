package com.aliceqr.vicevirtue.di

import android.content.Context
import androidx.room.Room
import com.aliceqr.vicevirtue.data.db.ViceVirtueDatabase
import com.aliceqr.vicevirtue.data.db.dao.EventDao
import com.aliceqr.vicevirtue.data.db.dao.ReminderDao
import com.aliceqr.vicevirtue.data.db.dao.TrackableDao
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
    fun provideDatabase(@ApplicationContext ctx: Context): ViceVirtueDatabase {
        return Room.databaseBuilder(ctx, ViceVirtueDatabase::class.java, "vicevirtue.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideTrackableDao(db: ViceVirtueDatabase): TrackableDao = db.trackableDao()

    @Provides
    fun provideEventDao(db: ViceVirtueDatabase): EventDao = db.eventDao()

    @Provides
    fun provideReminderDao(db: ViceVirtueDatabase): ReminderDao = db.reminderDao()

}
