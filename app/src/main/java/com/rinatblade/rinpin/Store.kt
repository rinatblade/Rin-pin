package com.rinatblade.rinpin

import android.content.Context
import org.json.JSONArray

const val SRC_API = 0
const val SRC_APP = 1
const val SRC_WEB = 2

/** Настройки и корзина — всё хранится на телефоне в SharedPreferences. */
class Store(context: Context) {
    private val sp = context.getSharedPreferences("rinpin", Context.MODE_PRIVATE)

    var themeId: Int
        get() = sp.getInt("theme", 0)
        set(v) { sp.edit().putInt("theme", v).apply() }

    var fontFile: String
        get() = sp.getString("font", "") ?: ""
        set(v) { sp.edit().putString("font", v).apply() }

    /** Откуда брать результаты: 0 — API Pinduoduo, 1 — приложение Pinduoduo, 2 — сайт Pinduoduo в Rin-pin. */
    var source: Int
        get() = sp.getInt("source", SRC_APP)
        set(v) { sp.edit().putInt("source", v).apply() }

    var clientId: String
        get() = sp.getString("client_id", "") ?: ""
        set(v) { sp.edit().putString("client_id", v.trim()).apply() }

    var clientSecret: String
        get() = sp.getString("client_secret", "") ?: ""
        set(v) { sp.edit().putString("client_secret", v.trim()).apply() }

    var pid: String
        get() = sp.getString("pid", "") ?: ""
        set(v) { sp.edit().putString("pid", v.trim()).apply() }

    fun loadCart(): List<Offer> {
        val raw = sp.getString("cart", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Offer.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCart(items: List<Offer>) {
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        sp.edit().putString("cart", arr.toString()).apply()
    }
}
