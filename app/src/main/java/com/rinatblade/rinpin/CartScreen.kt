package com.rinatblade.rinpin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CartScreen(vm: MainViewModel) {
    val c = MaterialTheme.colorScheme
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(
                "Корзина",
                if (vm.cart.isEmpty()) "Здесь хранятся товары со ссылками" else "Сохранено товаров: ${vm.cart.size}",
            )
        }

        if (vm.cart.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(64.dp), tint = c.primary.copy(alpha = 0.5f))
                    Text(
                        "Пока пусто.\nНажмите «В корзину» у понравившегося товара — ссылка сохранится, и вы сможете открыть его в Pinduoduo в любой момент.",
                        modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant,
                    )
                }
            }
        }

        items(vm.cart, key = { it.key }) { offer ->
            OfferCard(offer = offer, rank = null, inCart = true, onCart = { vm.toggleCart(offer) })
        }

        if (vm.cart.isNotEmpty()) {
            item {
                TextButton(onClick = { confirmClear = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Очистить корзину", color = c.error)
                }
            }
        }
        item { Box(Modifier.size(8.dp)) }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Очистить корзину?") },
            text = { Text("Все сохранённые товары и ссылки будут удалены.") },
            confirmButton = {
                TextButton(onClick = { vm.clearCart(); confirmClear = false }) { Text("Очистить") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Отмена") } },
        )
    }
}
