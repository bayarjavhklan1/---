package com.bayrjavhlanmw.finance.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_entries WHERE (:type IS NULL OR type = :type) ORDER BY dateEpochDay DESC, id DESC")
    fun observeEntries(type: EntryType?): Flow<List<FinanceEntry>>

    @Query("SELECT * FROM finance_entries WHERE id = :id")
    fun observeEntry(id: Long): Flow<FinanceEntry?>

    @Insert suspend fun insert(entry: FinanceEntry): Long
    @Update suspend fun update(entry: FinanceEntry): Int
    @Query("DELETE FROM finance_entries WHERE id = :id")
    suspend fun delete(id: Long)
}

