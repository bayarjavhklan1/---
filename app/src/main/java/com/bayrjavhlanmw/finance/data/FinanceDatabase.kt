package com.bayrjavhlanmw.finance.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FinanceEntry::class], version = 1, exportSchema = true)
@TypeConverters(EntryConverters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile private var instance: FinanceDatabase? = null
        fun getDatabase(context: Context): FinanceDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, FinanceDatabase::class.java, "finance.db"
            ).build().also { instance = it }
        }
    }
}
