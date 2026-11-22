package com.rinatblade.rinpin

import org.json.JSONObject
import java.util.Locale

/** Один найденный товар (и одновременно запись в корзине). */
data class Offer(
    val key: String,          // goods_id, а если его нет — goods_sign
    val goodsSign: String,
    val mallId: String,
    val title: String,        // название на китайском
    val titleRu: String,      // перевод на русский (если получилось)
    val shop: String,
    val image: String,
    val price: Double,        // цена в юанях
    val salesNum: Long,
    val salesPlus: Boolean,
    val rating: Double,       // средняя оценка магазина, 0 если неизвестна
    val appLink: String,      // ссылка, открывающая приложение Pinduoduo
    val webLink: String,      // обычная https-ссылка (запасная и для копирования)
) {
    val displayTitle: String get() = if (titleRu.isNotBlank() && titleRu != title) titleRu else title

    fun toJson(): JSONObject = JSONObject().apply {
        put("key", key); put("sign", goodsSign); put("mall", mallId)
        put("title", title); put("titleRu", titleRu); put("shop", shop); put("image", image)
        put("price", price); put("sales", salesNum); put("plus", salesPlus); put("rating", rating)
        put("app", appLink); put("web", webLink)
    }

    companion object {
        fun fromJson(o: JSONObject) = Offer(
            key = o.optString("key"), goodsSign = o.optString("sign"), mallId = o.optString("mall"),
            title = o.optString("title"), titleRu = o.optString("titleRu"), shop = o.optString("shop"),
            image = o.optString("image"), price = o.optDouble("price", 0.0), salesNum = o.optLong("sales", 0),
            salesPlus = o.optBoolean("plus", false), rating = o.optDouble("rating", 0.0),
            appLink = o.optString("app"), webLink = o.optString("web"),
        )
    }
}

fun formatSales(n: Long, plus: Boolean): String {
    if (n <= 0) return "Продано: нет данных"
    val s = String.format(Locale.forLanguageTag("ru"), "%,d", n)
    return "Продано: $s" + if (plus) "+" else ""
}

fun formatPrice(p: Double): String = "¥ " + String.format(Locale.US, "%.2f", p).removeSuffix(".00")
