package com.jayanth.jmoney.wear.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseTransaction(
    val id: String,
    val amount: Double,
    val description: String? = null,
    @SerialName("transaction_timestamp") val transactionTimestamp: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("payee_id") val payeeId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    val type: String,
    @SerialName("user_id") val userId: String,
    @SerialName("product_link") val productLink: String? = null,
    val tid: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val categories: CategoryRelation? = null,
    val payees: PayeeRelation? = null,
    @SerialName("transaction_groups") val transactionGroups: GroupRelation? = null
)

/**
 * Write payload for transactions. The database owns `tid`; omitting it lets
 * the server sequence/default assign the cursor instead of accidentally
 * inserting the local placeholder value (commonly zero).
 */
@Serializable
data class SupabaseTransactionUpsert(
    val id: String,
    val amount: Double,
    val description: String? = null,
    @SerialName("transaction_timestamp") val transactionTimestamp: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("payee_id") val payeeId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    val type: String,
    @SerialName("user_id") val userId: String,
    @SerialName("product_link") val productLink: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CategoryRelation(
    val name: String,
    val icon: String? = null,
    @SerialName("app_icon") val appIcon: String? = null
)

@Serializable
data class PayeeRelation(
    val name: String,
    val logo: String? = null
)

@Serializable
data class GroupRelation(
    val name: String
)

@Serializable
data class SupabaseCategory(
    val id: String,
    val name: String,
    val type: String,
    val icon: String? = null,
    @SerialName("app_icon") val appIcon: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("is_living_cost") val isLivingCost: Int = 0,
    val priority: Int = 0
)

@Serializable
data class SupabasePayee(
    val id: String,
    val name: String,
    val logo: String? = null,
    @SerialName("user_id") val userId: String,
    val priority: Int = 0
)

@Serializable
data class SupabaseQuickTransaction(
    val id: String,
    val name: String,
    val type: String,
    val amount: Double? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("payee_id") val payeeId: String? = null,
    val description: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("product_link") val productLink: String? = null,
    val priority: Int = 0,
    val identifier: String? = null
)

@Serializable
data class SupabaseBudget(
    val id: String,
    val name: String,
    val logo: String? = null,
    val amount: Double,
    val interval: String,
    @SerialName("start_date") val startDate: String,
    val categories: List<String> = emptyList(),
    @SerialName("user_id") val userId: String
)

@Serializable
data class SupabaseGoal(
    val id: String,
    val name: String,
    val logo: String? = null,
    @SerialName("goal_amount") val goalAmount: Double,
    @SerialName("current_amount") val currentAmount: Double,
    @SerialName("user_id") val userId: String
)

@Serializable
data class SupabaseGroup(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("user_id") val userId: String,
    val priority: Int = 0
)
