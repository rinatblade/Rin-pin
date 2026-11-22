package com.rinatblade.rinpin

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily

/** Шрифт из assets/fonts. Файл загружается лениво — только когда шрифт реально нужен. */
class RinFont(private val context: Context, val file: String, val title: String) {
    val family: FontFamily by lazy {
        if (file.isEmpty()) {
            FontFamily.Default
        } else {
            try {
                FontFamily(Typeface.createFromAsset(context.assets, "fonts/$file"))
            } catch (e: Exception) {
                FontFamily.Default
            }
        }
    }
}

object Fonts {
    /** «Системный» + все шрифты, которые лежат в assets/fonts (на сборке туда кладутся 40 русских шрифтов). */
    fun load(context: Context): List<RinFont> {
        val files = try {
            context.assets.list("fonts")?.filter { it.endsWith(".ttf") || it.endsWith(".otf") }?.sorted()
        } catch (e: Exception) {
            null
        } ?: emptyList()
        val list = files.map { RinFont(context, it, it.substringBeforeLast('.').replace('_', ' ')) }
        return listOf(RinFont(context, "", "Системный")) + list
    }
}
