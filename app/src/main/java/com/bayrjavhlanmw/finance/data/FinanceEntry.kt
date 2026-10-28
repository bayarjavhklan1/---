package com.bayrjavhlanmw.finance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class EntryType { INCOME, EXPENSE }
enum class EntryFilter(val type: EntryType?) {
    ALL(null), INCOME(EntryType.INCOME), EXPENSE(EntryType.EXPENSE)
}

@Entity(tableName = "finance_entries")
data class FinanceEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: EntryType,
    val name: String,
    val amountMinor: Long,
    val dateEpochDay: Long
)

class EntryConverters {
    @TypeConverter fun fromType(value: EntryType): String = value.name
    @TypeConverter fun toType(value: String): EntryType = EntryType.valueOf(value)
}
