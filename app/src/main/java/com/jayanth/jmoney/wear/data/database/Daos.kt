package com.jayanth.jmoney.wear.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND deleted = 0 ORDER BY transaction_timestamp DESC")
    fun getTransactionsFlow(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND deleted = 0 AND date >= :startDate AND date <= :endDate ORDER BY transaction_timestamp DESC")
    fun getTransactionsForDateRangeFlow(userId: String, startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND deleted = 0 ORDER BY transaction_timestamp DESC")
    suspend fun getTransactions(userId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsyncedTransactions(userId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("UPDATE transactions SET tid = :tid, sync_status = 0 WHERE id = :id")
    suspend fun updateTidAndSyncStatus(id: String, tid: Int)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE transactions SET deleted = 1, sync_status = 1 WHERE id = :id AND user_id = :userId")
    suspend fun softDelete(id: String, userId: String)

    @Query("DELETE FROM transactions WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("SELECT MAX(tid) FROM transactions WHERE user_id = :userId")
    suspend fun getMaxTid(userId: String): Int?

    @Query("SELECT SUM(CASE WHEN type = 'Income' THEN amount ELSE -amount END) FROM transactions WHERE user_id = :userId AND deleted = 0")
    fun getNetWorthFlow(userId: String): Flow<Double?>

    @Query("SELECT SUM(CASE WHEN type = 'Income' THEN amount ELSE -amount END) FROM transactions WHERE user_id = :userId AND deleted = 0")
    suspend fun getNetWorth(userId: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Expense' AND date = :todayDate AND deleted = 0")
    fun getSpentTodayFlow(userId: String, todayDate: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Expense' AND date >= :startDate AND date <= :endDate AND deleted = 0")
    fun getMonthlyExpenseFlow(userId: String, startDate: String, endDate: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Income' AND date >= :startDate AND date <= :endDate AND deleted = 0")
    fun getMonthlyIncomeFlow(userId: String, startDate: String, endDate: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Expense' AND date >= :startDate AND date <= :endDate AND deleted = 0")
    suspend fun getExpenseSummary(userId: String, startDate: String, endDate: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Income' AND date >= :startDate AND date <= :endDate AND deleted = 0")
    suspend fun getIncomeSummary(userId: String, startDate: String, endDate: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Expense' AND date >= :startDate AND date <= :endDate AND category_id IN (:categoryIds) AND deleted = 0")
    suspend fun getBudgetSpending(userId: String, categoryIds: List<String>, startDate: String, endDate: String): Double?
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY priority, name")
    fun getCategoriesFlow(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY priority, name")
    suspend fun getCategories(userId: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsyncedCategories(userId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(categories: List<CategoryEntity>)

    @Query("UPDATE categories SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("DELETE FROM categories WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface PayeeDao {
    @Query("SELECT * FROM payees WHERE user_id = :userId ORDER BY priority, name")
    fun getPayeesFlow(userId: String): Flow<List<PayeeEntity>>

    @Query("SELECT * FROM payees WHERE user_id = :userId ORDER BY priority, name")
    suspend fun getPayees(userId: String): List<PayeeEntity>

    @Query("SELECT * FROM payees WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsyncedPayees(userId: String): List<PayeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(payees: List<PayeeEntity>)

    @Query("UPDATE payees SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("DELETE FROM payees WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface QuickTransactionDao {
    @Query("SELECT * FROM quick_transactions WHERE user_id = :userId AND deleted = 0 ORDER BY priority ASC, name")
    fun getQuickTransactionsFlow(userId: String): Flow<List<QuickTransactionEntity>>

    @Query("SELECT * FROM quick_transactions WHERE user_id = :userId AND deleted = 0 ORDER BY priority ASC, name")
    suspend fun getQuickTransactions(userId: String): List<QuickTransactionEntity>

    @Query("SELECT * FROM quick_transactions WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsynced(userId: String): List<QuickTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(quickTransactions: List<QuickTransactionEntity>)

    @Query("UPDATE quick_transactions SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("DELETE FROM quick_transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE quick_transactions SET deleted = 1, sync_status = 1 WHERE id = :id AND user_id = :userId")
    suspend fun softDelete(id: String, userId: String)

    @Query("DELETE FROM quick_transactions WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE user_id = :userId AND deleted = 0 ORDER BY name")
    fun getBudgetsFlow(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND deleted = 0 ORDER BY name")
    suspend fun getBudgets(userId: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsyncedBudgets(userId: String): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(budgets: List<BudgetEntity>)

    @Query("UPDATE budgets SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE budgets SET deleted = 1, sync_status = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("DELETE FROM budgets WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE user_id = :userId AND deleted = 0 ORDER BY name")
    fun getGoalsFlow(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE user_id = :userId AND deleted = 0 ORDER BY name")
    suspend fun getGoals(userId: String): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsyncedGoals(userId: String): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(goals: List<GoalEntity>)

    @Query("UPDATE goals SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE goals SET deleted = 1, sync_status = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("DELETE FROM goals WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface TransactionGroupDao {
    @Query("SELECT * FROM transaction_groups WHERE user_id = :userId ORDER BY priority")
    fun getGroupsFlow(userId: String): Flow<List<TransactionGroupEntity>>

    @Query("SELECT * FROM transaction_groups WHERE user_id = :userId ORDER BY priority")
    suspend fun getGroups(userId: String): List<TransactionGroupEntity>

    @Query("SELECT * FROM transaction_groups WHERE user_id = :userId AND sync_status = 1")
    suspend fun getUnsyncedGroups(userId: String): List<TransactionGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(groups: List<TransactionGroupEntity>)

    @Query("UPDATE transaction_groups SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("DELETE FROM transaction_groups WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
