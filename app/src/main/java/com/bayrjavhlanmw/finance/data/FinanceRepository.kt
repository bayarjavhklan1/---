package com.bayrjavhlanmw.finance.data

import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun observeEntries(type: EntryType?): Flow<List<FinanceEntry>>
    fun observeEntry(id: Long): Flow<FinanceEntry?>
    suspend fun save(entry: FinanceEntry): Long
    suspend fun delete(id: Long)
}

class OfflineFinanceRepository(private val dao: FinanceDao) : FinanceRepository {
    override fun observeEntries(type: EntryType?) = dao.observeEntries(type)
    override fun observeEntry(id: Long) = dao.observeEntry(id)
    override suspend fun save(entry: FinanceEntry): Long {
        require(entry.name.isNotBlank() && entry.amountMinor > 0)
        return if (entry.id == 0L) dao.insert(entry) else {
            check(dao.update(entry) == 1) { "Бүртгэл олдсонгүй." }
            entry.id
        }
    }
    override suspend fun delete(id: Long) = dao.delete(id)
}
