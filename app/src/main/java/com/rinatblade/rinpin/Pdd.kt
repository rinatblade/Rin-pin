package com.rinatblade.rinpin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.TreeMap

class PddException(message: String) : Exception(message)

/**
 * Клиент официального API Pinduoduo «多多进宝» (Duoduojinbao, gw-api.pinduoduo.com).
 * Нужны ключи client_id и client_secret; pid (推广位) нужен для партнёрских ссылок.
 */
object Pdd {
    private const val ENDPOINT = "https://gw-api.pinduoduo.com/api/router"

    // ---- ссылки, которые работают и без ключей ----
    fun searchWeb(zh: String) = "https://mobile.yangkeduo.com/search_result.html?search_key=" + Net.enc(zh)
    fun searchApp(zh: String) = "pinduoduo://com.xunmeng.pinduoduo/search_result.html?search_key=" + Net.enc(zh)
    private fun goodsWeb(id: String) = "https://mobile.yangkeduo.com/goods.html?goods_id=$id"
    private fun goodsApp(id: String) = "pinduoduo://com.xunmeng.pinduoduo/goods.html?goods_id=$id"

    private fun md5(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private fun call(type: String, extra: Map<String, String>, clientId: String, secret: String): JSONObject {
        val p = TreeMap<String, String>()
        p["type"] = type
        p["client_id"] = clientId
        p["timestamp"] = (System.currentTimeMillis() / 1000).toString()
        p["data_type"] = "JSON"
        p.putAll(extra)
        val sb = StringBuilder(secret)
        for ((k, v) in p) sb.append(k).append(v)
        sb.append(secret)
        val form = HashMap<String, String>(p)
        form["sign"] = md5(sb.toString()).uppercase()

        val json = JSONObject(Net.post(ENDPOINT, form))
        json.optJSONObject("error_response")?.let { e ->
            val msg = e.optString("sub_msg").ifBlank { e.optString("error_msg") }.ifBlank { "неизвестная ошибка" }
            throw PddException("${e.optString("error_code")}: $msg")
        }
        return json
    }

    /** Ищет товары по китайскому запросу и возвращает до 5 лучших магазинов. */
    suspend fun search(zh: String, clientId: String, secret: String, pid: String, shops: Int = 5): List<Offer> =
        withContext(Dispatchers.IO) {
            val all = ArrayList<Offer>()
            for (page in 1..2) {
                val extra = HashMap<String, String>()
                extra["keyword"] = zh
                extra["page"] = page.toString()
                extra["page_size"] = "100"
                extra["sort_type"] = "6" // по убыванию продаж
                if (pid.isNotBlank()) extra["pid"] = pid
                val resp = call("pdd.ddk.goods.search", extra, clientId, secret)
                val list = resp.optJSONObject("goods_search_response")?.optJSONArray("goods_list") ?: break
                for (i in 0 until list.length()) parse(list.getJSONObject(i))?.let { all.add(it) }
                if (list.length() < 100) break
            }
            pickTop(all, shops)
        }

    /** По одному лучшему товару от магазина; сначала рейтинг ≥ 4.7, потом ≥ 4.5, потом остальные; внутри — по продажам. */
    private fun pickTop(all: List<Offer>, n: Int): List<Offer> {
        val perShop = all.groupBy { it.mallId.ifBlank { it.shop } }
            .map { (_, items) -> items.maxByOrNull { it.salesNum }!! }
        val result = ArrayList<Offer>()
        val tiers = listOf<(Offer) -> Boolean>({ it.rating >= 4.7 }, { it.rating >= 4.5 }, { true })
        for (tier in tiers) {
            perShop.filter(tier).filter { o -> result.none { it.key == o.key } }
                .sortedByDescending { it.salesNum }
                .forEach { if (result.size < n) result.add(it) }
            if (result.size >= n) break
        }
        return result
    }

    private fun parse(o: JSONObject): Offer? {
        val sign = o.optString("goods_sign")
        val id = o.optString("goods_id")
        val key = id.ifBlank { sign }
        if (key.isBlank()) return null
        val title = o.optString("goods_name")
        val ratings = listOf("desc_txt", "serv_txt", "lgst_txt").mapNotNull { o.optString(it).toDoubleOrNull() }.filter { it > 0 }
        val tip = o.optString("sales_tip")
        val image = o.optString("goods_image_url").ifBlank { o.optString("goods_thumbnail_url") }
        return Offer(
            key = key, goodsSign = sign, mallId = o.optString("mall_id"),
            title = title, titleRu = "", shop = o.optString("mall_name"),
            image = image.replace("http://", "https://"),
            price = o.optLong("min_group_price", 0) / 100.0,
            salesNum = parseSales(tip), salesPlus = tip.contains("+"),
            rating = if (ratings.isEmpty()) 0.0 else ratings.average(),
            appLink = if (id.isNotBlank()) goodsApp(id) else "",
            webLink = if (id.isNotBlank()) goodsWeb(id) else searchWeb(title),
        )
    }

    private fun parseSales(tip: String): Long {
        val m = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*([万亿]?)").find(tip) ?: return 0
        val n = m.groupValues[1].toDoubleOrNull() ?: return 0
        val mul = when (m.groupValues[2]) {
            "万" -> 10_000.0
            "亿" -> 100_000_000.0
            else -> 1.0
        }
        return (n * mul).toLong()
    }

    /** Если задан pid — заменяет ссылки на партнёрские (короткая ссылка + схема для открытия приложения). */
    suspend fun withLinks(o: Offer, clientId: String, secret: String, pid: String): Offer =
        withContext(Dispatchers.IO) {
            if (pid.isBlank() || o.goodsSign.isBlank()) return@withContext o
            try {
                val extra = mapOf(
                    "p_id" to pid,
                    "goods_sign_list" to JSONArray().put(o.goodsSign).toString(),
                    "generate_short_url" to "true",
                    "generate_schema_url" to "true",
                )
                val resp = call("pdd.ddk.goods.promotion.url.generate", extra, clientId, secret)
                val item = resp.optJSONObject("goods_promotion_url_generate_response")
                    ?.optJSONArray("goods_promotion_url_list")?.optJSONObject(0)
                    ?: return@withContext o
                val web = listOf("mobile_short_url", "short_url", "mobile_url", "url")
                    .map { item.optString(it) }.firstOrNull { it.isNotBlank() } ?: o.webLink
                val app = item.optString("schema_url").ifBlank { o.appLink }
                o.copy(webLink = web, appLink = app)
            } catch (e: Exception) {
                o
            }
        }
}
