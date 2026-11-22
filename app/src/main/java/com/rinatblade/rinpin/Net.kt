package com.rinatblade.rinpin

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Net {
    fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    fun get(url: String, timeoutMs: Int = 12000): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = timeoutMs
            c.readTimeout = timeoutMs
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) Rin-pin")
            return read(c)
        } finally {
            c.disconnect()
        }
    }

    fun post(url: String, form: Map<String, String>, timeoutMs: Int = 15000): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = "POST"
            c.doOutput = true
            c.connectTimeout = timeoutMs
            c.readTimeout = timeoutMs
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            val body = form.entries.joinToString("&") { enc(it.key) + "=" + enc(it.value) }
            c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            return read(c)
        } finally {
            c.disconnect()
        }
    }

    private fun read(c: HttpURLConnection): String {
        val stream = if (c.responseCode in 200..299) c.inputStream else (c.errorStream ?: c.inputStream)
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
