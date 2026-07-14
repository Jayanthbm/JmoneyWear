package com.jayanth.jmoney.wear.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val description: String?,
    @ColumnInfo(name = "transaction_timestamp") val transactionTimestamp: String,
    val date: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon") val categoryIcon: String?,
    @ColumnInfo(name = "category_app_icon") val categoryAppIcon: String?,
    @ColumnInfo(name = "payee_id") val payeeId: String?,
    @ColumnInfo(name = "payee_name") val payeeName: String?,
    @ColumnInfo(name = "payee_logo") val payeeLogo: String?,
    val type: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "product_link") val productLink: String?,
    val tid: Int,
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "group_id") val groupId: String?,
    @ColumnInfo(name = "group_name") val groupName: String?,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    val deleted: Int = 0
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val icon: String?,
    @ColumnInfo(name = "app_icon") val appIcon: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "is_living_cost") val isLivingCost: Int = 0,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0,
    val priority: Int = 0
)

@Entity(tableName = "payees")
data class PayeeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0,
    val priority: Int = 0
)

@Entity(tableName = "quick_transactions")
data class QuickTransactionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val amount: Double?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "payee_id") val payeeId: String?,
    val description: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "product_link") val productLink: String?,
    val priority: Int = 0,
    val identifier: String?,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0,
    val deleted: Int = 0
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String?,
    val amount: Double,
    val interval: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    val categories: String, // Stringified JSON array of category IDs
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0,
    val deleted: Int = 0
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String?,
    @ColumnInfo(name = "goal_amount") val goalAmount: Double,
    @ColumnInfo(name = "current_amount") val currentAmount: Double,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0,
    val deleted: Int = 0
)

@Entity(tableName = "transaction_groups")
data class TransactionGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    val priority: Int = 0,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = 0
)
