package com.rinatblade.rinpin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

private fun Context.tryView(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: Exception) {
    false
}

/** Сначала пробуем открыть приложение Pinduoduo, если его нет — обычную ссылку в браузере. */
fun Context.openLink(appLink: String?, webLink: String) {
    if (!appLink.isNullOrBlank() && tryView(appLink)) return
    if (!tryView(webLink)) Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
}

fun Context.copyText(text: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Rin-pin", text))
    Toast.makeText(this, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
}
