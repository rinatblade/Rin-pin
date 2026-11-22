package com.rinatblade.rinpin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Читает экран приложения Pinduoduo (и только его: пакет задан в accessibility_config.xml),
 * пока идёт поиск из Rin-pin. Прокручивает выдачу, собирает товары и возвращает пользователя в Rin-pin.
 */
class PddAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val collected = LinkedHashMap<String, Offer>()
    private var session = 0L
    private var round = 0
    private var idle = 0
    private var seen = 0
    private var tickPending = false

    private val tick = Runnable {
        tickPending = false
        step()
    }
    private val watchdog = Runnable { if (Bridge.request != null) finish() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Bridge.connected = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Bridge.connected = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Bridge.connected = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || Bridge.request == null) return
        if (event.packageName?.toString() != PDD) return
        if (Bridge.session != session) newSession()
        if (!tickPending) {
            tickPending = true
            handler.postDelayed(tick, if (round == 0) 3500L else 2000L)
        }
    }

    private fun newSession() {
        session = Bridge.session
        handler.removeCallbacksAndMessages(null)
        collected.clear()
        round = 0
        idle = 0
        seen = 0
        tickPending = false
        handler.postDelayed(watchdog, 35_000L)
    }

    private fun step() {
        if (Bridge.request == null) return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != PDD) return

        val texts = ArrayList<String>()
        walk(root, texts, 0)
        seen += texts.size
        Bridge.lastDump = texts.take(400)

        val before = collected.size
        ScreenParser.parse(texts).forEach { collected.putIfAbsent(it.key, it) }
        round++
        idle = if (collected.size == before) idle + 1 else 0

        val enough = collected.size >= 30 || (collected.isNotEmpty() && idle >= 2)
        if (round >= 6 || enough) {
            finish()
            return
        }
        if (!scroll(root)) {
            finish()
            return
        }
        tickPending = true
        handler.postDelayed(tick, 2200L)
    }

    private fun finish() {
        val top = Pdd.pickTop(collected.values.toList(), 5)
        val n = seen
        Bridge.request = null
        handler.removeCallbacksAndMessages(null)
        tickPending = false
        Bridge.results.tryEmit(Bridge.Result(top, n))
        try {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
            )
        } catch (e: Exception) {
            // Если система не дала вернуться сама, результаты уже ждут в Rin-pin.
        }
    }

    private fun walk(n: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
        if (n == null || depth > 50 || out.size > 1500) return
        if (n.isVisibleToUser) {
            val t = (n.text ?: n.contentDescription)?.toString()?.trim()
            if (!t.isNullOrEmpty()) out.add(t)
        }
        for (i in 0 until n.childCount) walk(n.getChild(i), out, depth + 1)
    }

    private fun findScrollable(n: AccessibilityNodeInfo?, depth: Int): AccessibilityNodeInfo? {
        if (n == null || depth > 50) return null
        if (n.isScrollable) return n
        for (i in 0 until n.childCount) {
            findScrollable(n.getChild(i), depth + 1)?.let { return it }
        }
        return null
    }

    private fun scroll(root: AccessibilityNodeInfo): Boolean {
        val ok = findScrollable(root, 0)?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        return ok || swipe()
    }

    private fun swipe(): Boolean {
        val dm = resources.displayMetrics
        val p = Path().apply {
            moveTo(dm.widthPixels / 2f, dm.heightPixels * 0.75f)
            lineTo(dm.widthPixels / 2f, dm.heightPixels * 0.30f)
        }
        val g = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(p, 0, 350)).build()
        return dispatchGesture(g, null, null)
    }

    companion object {
        const val PDD = "com.xunmeng.pinduoduo"
    }
}
