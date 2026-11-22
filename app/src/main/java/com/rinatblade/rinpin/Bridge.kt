package com.rinatblade.rinpin

import kotlinx.coroutines.flow.MutableSharedFlow

/** Мост между службой чтения экрана Pinduoduo и приложением. */
object Bridge {
    data class Result(val offers: List<Offer>, val seenTexts: Int)

    @Volatile var connected = false          // служба «Специальных возможностей» включена
    @Volatile var request: String? = null    // китайский запрос, пока идёт поиск в приложении Pinduoduo
    @Volatile var session = 0L
    @Volatile var lastDump: List<String> = emptyList()  // последний прочитанный с экрана текст (для диагностики)

    val results = MutableSharedFlow<Result>(extraBufferCapacity = 1)

    fun begin(zh: String) {
        session++
        request = zh
    }

    fun cancel() {
        request = null
    }
}
