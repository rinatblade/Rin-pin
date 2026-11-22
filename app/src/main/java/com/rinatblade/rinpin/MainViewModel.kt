package com.rinatblade.rinpin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    val fonts: List<RinFont> = Fonts.load(app)

    // ---- оформление ----
    var themeId by mutableIntStateOf(store.themeId)
        private set
    var fontFile by mutableStateOf(store.fontFile)
        private set

    fun currentFont(): RinFont = fonts.firstOrNull { it.file == fontFile } ?: fonts.first()
    fun pickTheme(id: Int) { themeId = id; store.themeId = id }
    fun pickFont(file: String) { fontFile = file; store.fontFile = file }

    // ---- ключи API ----
    var clientId by mutableStateOf(store.clientId)
        private set
    var clientSecret by mutableStateOf(store.clientSecret)
        private set
    var pid by mutableStateOf(store.pid)
        private set

    fun saveClientId(v: String) { clientId = v; store.clientId = v }
    fun saveClientSecret(v: String) { clientSecret = v; store.clientSecret = v }
    fun savePid(v: String) { pid = v; store.pid = v }

    val hasKeys: Boolean get() = clientId.isNotBlank() && clientSecret.isNotBlank()

    // ---- поиск ----
    var query by mutableStateOf("")
    var queryZh by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var offers by mutableStateOf<List<Offer>>(emptyList())
        private set
    var noKeys by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var empty by mutableStateOf(false)
        private set

    fun search(text: String = query) {
        val q = text.trim()
        if (q.isEmpty() || loading) return
        query = q
        loading = true
        offers = emptyList()
        noKeys = false
        error = null
        empty = false
        queryZh = ""
        viewModelScope.launch {
            try {
                val zh = Translate.translate(q, "ru", "zh-CN")
                queryZh = zh
                if (!hasKeys) {
                    noKeys = true
                    return@launch
                }
                val id = clientId
                val secret = clientSecret
                val pidNow = pid
                val found = Pdd.search(zh, id, secret, pidNow)
                if (found.isEmpty()) {
                    empty = true
                    return@launch
                }
                offers = coroutineScope {
                    found.map { o ->
                        async {
                            val ru = Translate.translate(o.title, "zh-CN", "ru")
                            Pdd.withLinks(o.copy(titleRu = ru), id, secret, pidNow)
                        }
                    }.awaitAll()
                }
            } catch (e: PddException) {
                error = "Pinduoduo ответил ошибкой: ${e.message}"
            } catch (e: Exception) {
                error = "Не удалось выполнить поиск. Проверьте интернет и попробуйте ещё раз."
            } finally {
                loading = false
            }
        }
    }

    // ---- корзина ----
    val cart = mutableStateListOf<Offer>().apply { addAll(store.loadCart()) }

    fun inCart(o: Offer) = cart.any { it.key == o.key }

    fun toggleCart(o: Offer) {
        val i = cart.indexOfFirst { it.key == o.key }
        if (i >= 0) cart.removeAt(i) else cart.add(0, o)
        store.saveCart(cart.toList())
    }

    fun clearCart() {
        cart.clear()
        store.saveCart(emptyList())
    }
}
