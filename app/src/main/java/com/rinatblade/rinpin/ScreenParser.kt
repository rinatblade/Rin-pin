package com.rinatblade.rinpin

/**
 * Превращает список строк, прочитанных с экрана Pinduoduo, в товары.
 * Порядок на карточке предполагается такой: название → цена / «已拼N件» → магазин.
 * Если Pinduoduo устроит экран иначе, разбор подстраивается по диагностике (Настройки).
 */
object ScreenParser {
    private val CJK = Regex("[\\u4e00-\\u9fff]")
    private const val NUM = "([0-9]+(?:\\.[0-9]+)?\\s*[万亿]?\\+?)"
    private val SALES1 = Regex("(?:已拼|已售|销量|已团)\\s*$NUM")
    private val SALES2 = Regex(NUM + "\\s*(?:人已拼|人已买|人付款|人已购|件已售)")
    private val PRICE = Regex("[¥￥]\\s*([0-9]+(?:\\.[0-9]+)?)")
    private val NUMONLY = Regex("^[0-9]+(?:\\.[0-9]+)?\\u0024")
    private val SHOP = Regex("(店|官方|工厂|商行|直营|专营|专卖)\\u0024")
    private val SKIP = listOf("拼单", "搜索", "综合", "销量", "价格", "筛选", "全部", "发货", "好评", "领券", "百亿补贴", "免拼", "多多买菜", "自营")

    private class Card(val title: String) {
        var price = 0.0
        var sales = 0L
        var plus = false
        var shop = ""
    }

    private fun salesOf(t: String): String? =
        SALES1.find(t)?.groupValues?.get(1) ?: SALES2.find(t)?.groupValues?.get(1)

    private fun isShop(t: String) = t.length <= 24 && SHOP.containsMatchIn(t)

    private fun isTitle(t: String): Boolean =
        t.length >= 8 && CJK.containsMatchIn(t) &&
            salesOf(t) == null && !PRICE.containsMatchIn(t) &&
            SKIP.none { t.startsWith(it) } && !isShop(t)

    fun parse(texts: List<String>): List<Offer> {
        val cards = ArrayList<Card>()
        var cur: Card? = null
        fun flush() {
            cur?.let { if (it.sales > 0 || it.price > 0) cards.add(it) }
            cur = null
        }
        for (i in texts.indices) {
            val t = texts[i]
            val sales = salesOf(t)
            val price = PRICE.find(t)?.groupValues?.get(1)?.toDoubleOrNull()
                ?: if ((t == "¥" || t == "￥") && texts.getOrNull(i + 1)?.matches(NUMONLY) == true) texts[i + 1].toDoubleOrNull() else null
            when {
                sales != null -> cur?.let { c ->
                    if (c.sales == 0L) {
                        c.sales = Pdd.parseSales(sales)
                        c.plus = sales.contains("+")
                    }
                }
                price != null -> cur?.let { c -> if (c.price == 0.0) c.price = price }
                isShop(t) -> cur?.let { c -> if (c.shop.isEmpty()) c.shop = t }
                isTitle(t) -> {
                    flush()
                    cur = Card(t)
                }
            }
        }
        flush()
        return cards.map { c ->
            val q = c.title.take(30)
            Offer(
                key = "scr" + c.title.hashCode(), goodsSign = "", mallId = "", title = c.title, titleRu = "",
                shop = c.shop, image = "", price = c.price, salesNum = c.sales, salesPlus = c.plus, rating = 0.0,
                appLink = Pdd.searchApp(q), webLink = Pdd.searchWeb(q),
            )
        }
    }
}
