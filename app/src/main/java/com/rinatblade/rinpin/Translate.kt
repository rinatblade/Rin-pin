package com.rinatblade.rinpin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Бесплатный перевод без ключей. Если все сервисы недоступны — возвращает исходный текст. */
object Translate {

    suspend fun translate(text: String, from: String, to: String): String = withContext(Dispatchers.IO) {
        val clean = text.trim()
        if (clean.isEmpty()) return@withContext clean
        google(clean, from, to) ?: myMemory(clean, from, to) ?: clean
    }

    private fun google(text: String, from: String, to: String): String? = try {
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$from&tl=$to&dt=t&q=" + Net.enc(text)
        val root = JSONArray(Net.get(url))
        val parts = root.getJSONArray(0)
        val sb = StringBuilder()
        for (i in 0 until parts.length()) sb.append(parts.getJSONArray(i).optString(0))
        sb.toString().trim().ifEmpty { null }
    } catch (e: Exception) {
        null
    }

    private fun myMemory(text: String, from: String, to: String): String? = try {
        val pair = Net.enc("$from|$to")
        val url = "https://api.mymemory.translated.net/get?q=" + Net.enc(text) + "&langpair=" + pair
        val t = JSONObject(Net.get(url)).getJSONObject("responseData").optString("translatedText")
        t.trim().ifEmpty { null }
    } catch (e: Exception) {
        null
    }
}
