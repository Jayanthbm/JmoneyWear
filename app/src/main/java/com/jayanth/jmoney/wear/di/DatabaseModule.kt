package com.jayanth.jmoney.wear.di

import android.content.Context
import androidx.room.Room
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.BudgetDao
import com.jayanth.jmoney.wear.data.database.CategoryDao
import com.jayanth.jmoney.wear.data.database.GoalDao
import com.jayanth.jmoney.wear.data.database.PayeeDao
import com.jayanth.jmoney.wear.data.database.QuickTransactionDao
import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.database.TransactionGroupDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "jmoney.db"
        ).build()
    }

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun providePayeeDao(db: AppDatabase): PayeeDao = db.payeeDao()

    @Provides
    fun provideQuickTransactionDao(db: AppDatabase): QuickTransactionDao = db.quickTransactionDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideGroupDao(db: AppDatabase): TransactionGroupDao = db.groupDao()
}
