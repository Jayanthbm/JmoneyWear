package com.jayanth.jmoney.wear.presentation.form

import android.app.RemoteInput
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.wear.input.RemoteInputIntentHelper
import com.jayanth.jmoney.wear.data.database.CategoryEntity
import com.jayanth.jmoney.wear.data.database.PayeeEntity
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

data class TransactionFormData(
    val amount: Double,
    val type: String,
    val category: CategoryEntity?,
    val payee: PayeeEntity?,
    val description: String
)

@Composable
fun TransactionForm(
    initialData: TransactionFormData,
    categories: List<CategoryEntity>,
    payees: List<PayeeEntity>,
    onConfirm: (TransactionFormData) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf(initialData.amount) }
    var type by remember { mutableStateOf(initialData.type) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(initialData.category) }
    var selectedPayee by remember { mutableStateOf<PayeeEntity?>(initialData.payee) }
    var description by remember { mutableStateOf(initialData.description) }
    
    var showCategorySelection by remember { mutableStateOf(false) }
    var showPayeeSelection by remember { mutableStateOf(false) }
    var showNumericKeypad by remember { mutableStateOf(false) }
    
    val listState = rememberTransformingLazyColumnState()
    
    // Description input launcher
    val descInputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val results = RemoteInput.getResultsFromIntent(result.data)
        val desc = results?.getCharSequence("description")?.toString()
        if (desc != null) {
            description = desc
        }
    }
    
    val launchDescInput = {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInputs = listOf(
            RemoteInput.Builder("description")
                .setLabel("Description (optional)")
                .build()
        )
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
        descInputLauncher.launch(intent)
    }
    
    BackHandler(onBack = onCancel)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            showNumericKeypad -> {
                NumericKeypad(
                    value = amount,
                    onValueChange = { amount = it },
                    onBack = { showNumericKeypad = false },
                    onConfirm = { showNumericKeypad = false }
                )
            }
            showCategorySelection -> {
                CategorySelector(
                    categories = categories,
                    transactionType = type,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onDismiss = { showCategorySelection = false }
                )
            }
            showPayeeSelection -> {
                PayeeSelector(
                    payees = payees,
                    selectedPayee = selectedPayee,
                    onPayeeSelected = { selectedPayee = it },
                    onDismiss = { showPayeeSelection = false }
                )
            }
            else -> {
                TransformingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        val headerTitle = when (type) {
                            "Income" -> "Log Income"
                            else -> "Log Expense"
                        }
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Amount Card
                    item {
                        Card(
                            onClick = { showNumericKeypad = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Amount (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatCurrency(amount),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // Category Card
                    item {
                        Card(
                            onClick = { showCategorySelection = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Category (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedCategory?.name ?: "Tap to Select",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Payee Card
                    item {
                        Card(
                            onClick = { showPayeeSelection = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Payee (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedPayee?.name ?: "None",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedPayee == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Description Card
                    item {
                        Card(
                            onClick = { launchDescInput() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Description (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (description.isBlank()) "(empty)" else description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (description.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Action Buttons
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {
                            Button(
                                onClick = onCancel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    if (amount <= 0.0) {
                                        Toast.makeText(context, "Please enter an amount > 0", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onConfirm(
                                            TransactionFormData(
                                                amount = amount,
                                                type = type,
                                                category = selectedCategory,
                                                payee = selectedPayee,
                                                description = description
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "OK",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
