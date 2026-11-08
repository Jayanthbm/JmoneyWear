package com.jayanth.jmoney.wear.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PayeeEntity::class,
        QuickTransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        TransactionGroupEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun payeeDao(): PayeeDao
    abstract fun quickTransactionDao(): QuickTransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun groupDao(): TransactionGroupDao
}
