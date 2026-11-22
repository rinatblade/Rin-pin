package com.rinatblade.rinpin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private val EXAMPLES = listOf("беспроводные наушники", "чехол для телефона", "платье женское", "кроссовки", "органайзер для дома")

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(vm: MainViewModel, onOpenSettings: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val focus = LocalFocusManager.current
    val startSearch = { text: String ->
        focus.clearFocus()
        vm.search(text)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("Rin-pin", "Поиск товаров на Pinduoduo по-русски") }

        item {
            OutlinedTextField(
                value = vm.query,
                onValueChange = { vm.query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                placeholder = { Text("Что ищем? Например: наушники") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.query = "" }) { Icon(Icons.Default.Clear, contentDescription = "Очистить") }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { startSearch(vm.query) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = c.surface, unfocusedContainerColor = c.surface,
                ),
            )
        }

        item {
            Button(
                onClick = { startSearch(vm.query) },
                enabled = vm.query.isNotBlank() && !vm.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
            ) { Text("Найти на Pinduoduo") }
        }

        if (vm.queryZh.isNotBlank()) {
            item {
                Text(
                    "Ищу по-китайски: " + vm.queryZh,
                    style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant,
                )
            }
        }

        if (vm.loading) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 3.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(if (vm.waitingApp) "Жду результаты из Pinduoduo…" else "Ищу лучшие магазины…")
                    }
                    if (vm.waitingApp) {
                        Text(
                            "Приложение Pinduoduo откроется само, Rin-pin прокрутит выдачу и вернётся сюда. Если не вернулся — переключитесь на Rin-pin вручную.",
                            style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        TextButton(onClick = { vm.cancelWait() }) { Text("Отмена") }
                    }
                }
            }
        }

        if (vm.needAccessibility) {
            item {
                InfoCard {
                    Text("Чтобы Rin-pin мог читать результаты из приложения Pinduoduo, включите его в Специальных возможностях. Он читает только экран Pinduoduo и только во время поиска.", style = MaterialTheme.typography.bodyMedium)
                    Text("Если переключатель серый: Информация о приложении → ⋮ → «Разрешить ограниченные настройки».", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { ctx.openAccessibilitySettings() }, modifier = Modifier.weight(1f)) { Text("Включить", maxLines = 1) }
                        FilledTonalButton(onClick = { ctx.openAppInfo() }, modifier = Modifier.weight(1f)) { Text("О приложении", maxLines = 1) }
                    }
                    FilledTonalButton(
                        onClick = { vm.pickSource(SRC_WEB); vm.search() },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Искать через сайт Pinduoduo") }
                }
            }
        }

        if (vm.noKeys) {
            item {
                InfoCard {
                    Text("Запрос переведён. Чтобы список магазинов показывался прямо здесь, нужны ключи Pinduoduo API (Настройки → Подключение).", style = MaterialTheme.typography.bodyMedium)
                    Text("Пока ключей нет, можно открыть этот поиск в самом приложении Pinduoduo.", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { ctx.openLink(Pdd.searchApp(vm.queryZh), Pdd.searchWeb(vm.queryZh)) }, modifier = Modifier.weight(1f)) {
                            Text("Открыть в Pinduoduo", maxLines = 1)
                        }
                        FilledTonalButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) { Text("Ключи API", maxLines = 1) }
                    }
                }
            }
        }

        vm.error?.let { msg ->
            item {
                InfoCard {
                    Text(msg, style = MaterialTheme.typography.bodyMedium)
                    if (vm.queryZh.isNotBlank()) {
                        FilledTonalButton(onClick = { ctx.openLink(Pdd.searchApp(vm.queryZh), Pdd.searchWeb(vm.queryZh)) }) {
                            Text("Открыть поиск в Pinduoduo")
                        }
                    }
                }
            }
        }

        if (vm.empty) {
            item {
                InfoCard {
                    Text("По этому запросу ничего не нашлось. Попробуйте другие слова.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        itemsIndexed(vm.offers, key = { _, o -> o.key }) { index, offer ->
            OfferCard(
                offer = offer, rank = index + 1,
                inCart = vm.inCart(offer), onCart = { vm.toggleCart(offer) },
            )
        }

        if (vm.offers.isNotEmpty() && vm.queryZh.isNotBlank()) {
            item {
                FilledTonalButton(
                    onClick = { ctx.openLink(Pdd.searchApp(vm.queryZh), Pdd.searchWeb(vm.queryZh)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Весь поиск в Pinduoduo") }
            }
        }

        if (!vm.loading && vm.offers.isEmpty() && !vm.noKeys && vm.error == null && !vm.empty && vm.queryZh.isBlank()) {
            item {
                Text(
                    "Введите товар по-русски — найду его на Pinduoduo и покажу 5 магазинов с высоким рейтингом и большим числом продаж.",
                    style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant,
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EXAMPLES.forEach { ex ->
                        AssistChip(onClick = { startSearch(ex) }, label = { Text(ex) })
                    }
                }
            }
        }

        item { Box(Modifier.height(8.dp)) }
    }
}
