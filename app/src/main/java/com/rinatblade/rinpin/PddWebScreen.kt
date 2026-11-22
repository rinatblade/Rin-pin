package com.rinatblade.rinpin

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONTokener
import kotlin.coroutines.resume

private const val LOGIN_MSG = "Войдите в аккаунт Pinduoduo — после входа результаты заберутся сами."

/** JS, который достаёт товары со страницы выдачи: сначала из данных страницы, затем из разметки. */
private const val EXTRACT_JS = """
(function () {
  var out = [], seen = {}, budget = 300000;
  function str(v) { return (v === undefined || v === null) ? '' : String(v); }
  function yuan(v) {
    if (v === undefined || v === null || v === '') return 0;
    var s = String(v).replace(/[^0-9.]/g, '');
    var n = parseFloat(s);
    if (isNaN(n)) return 0;
    return s.indexOf('.') >= 0 ? n : n / 100;
  }
  function add(id, name, price, sales, shop, mall, img) {
    id = str(id);
    if (!id || !name || seen[id]) return;
    seen[id] = 1;
    out.push({id: id, name: str(name), price: price, sales: str(sales), shop: str(shop), mall: str(mall), img: str(img)});
  }
  function fromObj(x) {
    var id = x.goods_id || x.goodsId;
    var name = x.goods_name || x.goodsName || x.short_name || x.shortName;
    if (!id || !name || typeof name !== 'string') return;
    var price = x.price !== undefined ? x.price : (x.min_group_price !== undefined ? x.min_group_price : (x.group && x.group.price));
    var sales = x.sales_tip || x.salesTip || x.sales || x.cnt || x.sold_quantity || x.sales_num || '';
    add(id, name, yuan(price), sales, x.mall_name || x.mallName || '', x.mall_id || x.mallId || '',
        x.hd_thumb_url || x.thumb_url || x.image_url || x.goods_image_url || '');
  }
  function walk(x, d) {
    if (!x || d > 14 || budget-- < 0) return;
    if (Array.isArray(x)) { for (var i = 0; i < x.length; i++) walk(x[i], d + 1); return; }
    if (typeof x !== 'object') return;
    fromObj(x);
    for (var k in x) { try { walk(x[k], d + 1); } catch (e) {} }
  }
  try {
    var keys = Object.keys(window);
    for (var i = 0; i < keys.length; i++) {
      var v;
      try { v = window[keys[i]]; } catch (e) { continue; }
      var t = Object.prototype.toString.call(v);
      if (t === '[object Object]' || t === '[object Array]') walk(v, 0);
    }
  } catch (e) {}
  if (out.length === 0) {
    var nodes = document.querySelectorAll('a[href*="goods_id"], [data-goods-id]');
    for (var j = 0; j < nodes.length; j++) {
      var a = nodes[j];
      var href = a.getAttribute('href') || '';
      var m = /goods_id=(\d+)/.exec(href) || [];
      var id = m[1] || a.getAttribute('data-goods-id');
      var lines = (a.innerText || '').split('\n').map(function (s) { return s.trim(); }).filter(Boolean);
      var name = '', sales = '', price = 0;
      for (var q = 0; q < lines.length; q++) {
        var l = lines[q];
        if (/已拼|已售|人已拼/.test(l)) { sales = l; }
        else if (/[¥￥]/.test(l)) { var pm = /([0-9]+(\.[0-9]+)?)/.exec(l); if (pm && !price) price = parseFloat(pm[1]); }
        else if (l.length > name.length) { name = l; }
      }
      var im = a.querySelector('img');
      add(id, name, price, sales, '', '', im ? (im.getAttribute('src') || im.getAttribute('data-src')) : '');
    }
  }
  return JSON.stringify(out.slice(0, 120));
})()
"""

private fun parseWebJson(raw: String?): List<Offer> {
    if (raw == null) return emptyList()
    val s = JSONTokener(raw).nextValue() as? String ?: return emptyList()
    val arr = JSONArray(s)
    val list = ArrayList<Offer>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val id = o.optString("id")
        val name = o.optString("name")
        if (id.isBlank() || name.isBlank()) continue
        val sales = o.optString("sales")
        var img = o.optString("img")
        if (img.startsWith("//")) img = "https:$img"
        list.add(
            Offer(
                key = id, goodsSign = "", mallId = o.optString("mall"),
                title = name, titleRu = "", shop = o.optString("shop"),
                image = img.replace("http://", "https://"),
                price = o.optDouble("price", 0.0),
                salesNum = Pdd.parseSales(sales), salesPlus = sales.contains("+"),
                rating = 0.0,
                appLink = Pdd.goodsApp(id), webLink = Pdd.goodsWeb(id),
            ),
        )
    }
    return list
}

private suspend fun extractOffers(w: WebView): List<Offer> = suspendCancellableCoroutine { cont ->
    w.evaluateJavascript(EXTRACT_JS) { raw ->
        val list = try {
            parseWebJson(raw)
        } catch (e: Exception) {
            emptyList()
        }
        if (cont.isActive) cont.resume(list)
    }
}

/** Сайт Pinduoduo внутри Rin-pin: вход в аккаунт один раз, дальше выдача читается автоматически. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PddWebScreen(vm: MainViewModel) {
    val zh = vm.webZh ?: return
    val c = MaterialTheme.colorScheme
    val searchUrl = remember(zh) { Pdd.searchWeb(zh) }
    val holder = remember { arrayOfNulls<WebView>(1) }
    var status by remember { mutableStateOf("Открываю Pinduoduo…") }
    val scope = rememberCoroutineScope()

    BackHandler {
        val w = holder[0]
        if (w != null && w.canGoBack()) w.goBack() else vm.closeWeb()
    }

    DisposableEffect(Unit) {
        onDispose {
            holder[0]?.let {
                it.stopLoading()
                it.destroy()
            }
            holder[0] = null
        }
    }

    LaunchedEffect(Unit) {
        while (holder[0] == null) delay(200)
        val w = holder[0] ?: return@LaunchedEffect
        var n = 0
        while (n < 60) {
            delay(2000)
            n++
            if (n == 3 || n == 6) w.evaluateJavascript("window.scrollBy(0, 1400);", null)
            val found = extractOffers(w)
            if (found.size >= 8 || (found.isNotEmpty() && n >= 8)) {
                vm.onWebResults(found)
                return@LaunchedEffect
            }
        }
        status = "Ничего не нашёл. Войдите в аккаунт, дождитесь выдачи и нажмите «Забрать сейчас»."
    }

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.closeWeb() }) { Icon(Icons.Default.Clear, contentDescription = "Закрыть") }
            Text("Pinduoduo", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                val w = holder[0] ?: return@TextButton
                scope.launch {
                    val found = extractOffers(w)
                    if (found.isNotEmpty()) vm.onWebResults(found)
                    else status = "Пока на странице нет товаров. Дождитесь выдачи и попробуйте снова."
                }
            }) { Text("Забрать сейчас") }
        }
        Text(
            status, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    var sawLogin = false
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val s = request.url.scheme ?: return true
                            return !(s == "http" || s == "https")
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            val u = url ?: return
                            if (u.contains("login", ignoreCase = true)) {
                                sawLogin = true
                                status = LOGIN_MSG
                            } else {
                                status = "Читаю результаты…"
                                if (sawLogin && !u.contains("search_result")) {
                                    sawLogin = false
                                    view.loadUrl(searchUrl)
                                }
                            }
                        }
                    }
                    holder[0] = this
                    loadUrl(searchUrl)
                }
            },
        )
    }
}
