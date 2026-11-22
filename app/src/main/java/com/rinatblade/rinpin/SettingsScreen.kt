@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rinatblade.rinpin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val SAMPLE = "Съешь ещё этих мягких французских булок"

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val c = MaterialTheme.colorScheme
    val current = Themes.byId(vm.themeId)
    var showDark by remember { mutableStateOf(current.dark) }
    val list = if (showDark) Themes.dark else Themes.light

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("Настройки", "Тема, шрифт и подключение к Pinduoduo") }

        // ---- Источник результатов ----
        item { SectionTitle("Откуда брать результаты") }
        item { SourceCard(vm) }

        // ---- Подключение ----
        item { SectionTitle("Ключи Pinduoduo API (для официального API)") }
        item { ApiCard(vm) }

        // ---- Темы ----
        item { SectionTitle("Тема оформления") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Segment("Тёмные · 20", showDark, Modifier.weight(1f)) { showDark = true }
                Segment("Светлые · 20", !showDark, Modifier.weight(1f)) { showDark = false }
            }
        }
        items(list.chunked(4), key = { it.first().id }) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { th ->
                    ThemeSwatch(th, selected = th.id == vm.themeId, modifier = Modifier.weight(1f)) { vm.pickTheme(th.id) }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        // ---- Шрифты ----
        item { SectionTitle("Шрифт (${vm.fonts.size - 1} русских + системный)") }
        items(vm.fonts, key = { it.file }) { f ->
            val selected = f.file == vm.fontFile
            Card(
                onClick = { vm.pickFont(f.file) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (selected) c.primaryContainer else c.surface),
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(f.title, style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant)
                        Text(
                            SAMPLE, fontFamily = f.family, fontSize = 18.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = c.primary)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Segment(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Surface(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(50),
        color = if (selected) c.primary else c.surface,
        contentColor = if (selected) c.onPrimary else c.onSurface,
        border = BorderStroke(1.dp, if (selected) c.primary else c.outlineVariant),
    ) {
        Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(text, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun ThemeSwatch(th: RinTheme, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)).background(th.bg)
                .border(if (selected) 3.dp else 1.dp, if (selected) c.primary else c.outline, RoundedCornerShape(16.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(th.surface).padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("Аа", color = th.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Dot(th.primary, 12)
                    Dot(th.secondary, 12)
                }
            }
            if (selected) {
                Icon(
                    Icons.Default.Check, null, tint = th.primary,
                    modifier = Modifier.align(Alignment.TopEnd).size(16.dp),
                )
            }
        }
        Text(
            th.name, fontSize = 11.sp, lineHeight = 13.sp, textAlign = TextAlign.Center, maxLines = 2,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ApiCard(vm: MainViewModel) {
    val c = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (vm.hasKeys) "Ключи заданы: результаты показываются в приложении." else "Ключи не заданы: приложение переведёт запрос и откроет поиск в Pinduoduo.",
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
            )
            Text(
                "Ключи выдаёт партнёрская программа Pinduoduo «多多进宝» (Duoduojinbao). Нужны Client ID и Client Secret; PID (推广位) — для партнёрских ссылок на товары.",
                style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
            )
            OutlinedTextField(
                value = vm.clientId, onValueChange = { vm.saveClientId(it) }, singleLine = true,
                label = { Text("Client ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            )
            OutlinedTextField(
                value = vm.clientSecret, onValueChange = { vm.saveClientSecret(it) }, singleLine = true,
                label = { Text("Client Secret") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                visualTransformation = PasswordVisualTransformation(),
            )
            OutlinedTextField(
                value = vm.pid, onValueChange = { vm.savePid(it) }, singleLine = true,
                label = { Text("PID (необязательно)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            )
            FilledTonalButton(onClick = { ctx.openLink(null, "https://jinbao.pinduoduo.com/") }) {
                Text("Где взять ключи")
            }
        }
    }
}
