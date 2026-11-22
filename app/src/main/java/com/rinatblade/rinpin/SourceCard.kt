@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rinatblade.rinpin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private data class SourceOpt(val id: Int, val title: String, val desc: String)

private val OPTIONS = listOf(
    SourceOpt(SRC_APP, "Через приложение Pinduoduo", "Rin-pin открывает поиск в приложении Pinduoduo, читает экран, выбирает 5 самых продаваемых и возвращается."),
    SourceOpt(SRC_WEB, "Через сайт Pinduoduo в Rin-pin", "Окно сайта внутри Rin-pin: вход в аккаунт один раз, есть настоящие ссылки на товары."),
    SourceOpt(SRC_API, "Официальный API (нужны ключи)", "Самый чистый вариант: рейтинг, продажи, партнёрские ссылки. Нужны Client ID и Secret."),
)

@Composable
fun SourceCard(vm: MainViewModel) {
    val c = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    var connected by remember { mutableStateOf(Bridge.connected) }
    LaunchedEffect(Unit) {
        while (true) {
            connected = Bridge.connected
            delay(1000)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OPTIONS.forEach { o ->
            val selected = vm.source == o.id
            Card(
                onClick = { vm.pickSource(o.id) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) c.primaryContainer else c.surface),
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(o.title, fontWeight = FontWeight.SemiBold)
                        Text(o.desc, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                    }
                    if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = c.primary)
                }
            }
        }

        if (vm.source == SRC_APP) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface),
                border = BorderStroke(1.dp, c.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (connected) "Чтение экрана Pinduoduo: включено ✓" else "Чтение экрана Pinduoduo: выключено",
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!connected) {
                        Text(
                            "Включите «Rin-pin» в Специальных возможностях. Rin-pin читает только экран приложения Pinduoduo и только во время поиска.",
                            style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
                        )
                        Text(
                            "Если переключатель серый: Информация о приложении → ⋮ (три точки) → «Разрешить ограниченные настройки», потом снова включите.",
                            style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = { ctx.openAccessibilitySettings() }, modifier = Modifier.weight(1f)) {
                                Text("Спец. возможности", maxLines = 1)
                            }
                            FilledTonalButton(onClick = { ctx.openAppInfo() }, modifier = Modifier.weight(1f)) {
                                Text("О приложении", maxLines = 1)
                            }
                        }
                    }
                    TextButton(onClick = {
                        val dump = Bridge.lastDump.joinToString("\n")
                        ctx.copyText(
                            dump.ifBlank { "(пусто: Rin-pin ещё ничего не прочитал в Pinduoduo)" },
                            "Диагностика скопирована",
                        )
                    }) { Text("Скопировать диагностику") }
                }
            }
        }
    }
}
