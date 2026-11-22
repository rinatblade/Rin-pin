package com.rinatblade.rinpin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

private fun Context.tryView(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: Exception) {
    false
}

/** Открывает ссылку (например pinduoduo://…); false, если подходящего приложения нет. */
fun Context.openApp(url: String): Boolean = tryView(url)

/** Сначала пробуем открыть приложение Pinduoduo, если его нет — обычную ссылку в браузере. */
fun Context.openLink(appLink: String?, webLink: String) {
    if (!appLink.isNullOrBlank() && tryView(appLink)) return
    if (!tryView(webLink)) Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
}

fun Context.copyText(text: String, message: String = "Ссылка скопирована") {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Rin-pin", text))
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.openAccessibilitySettings() {
    try {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(this, "Откройте Настройки → Специальные возможности", Toast.LENGTH_LONG).show()
    }
}

fun Context.openAppInfo() {
    try {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (e: Exception) {
        Toast.makeText(this, "Откройте Настройки → Приложения → Rin-pin", Toast.LENGTH_LONG).show()
    }
}
