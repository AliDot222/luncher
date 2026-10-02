package com.example.ailauncher

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebRequestError

// One profile = one Gecko "contextId" = its own persistent cookies/storage.
class BrowserActivity : Activity() {
    private lateinit var session: GeckoSession
    private var canBack = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val id = intent.getStringExtra("id") ?: return finish()
        val pad = (12 * resources.displayMetrics.density).toInt()

        val bar = EditText(this).apply {
            hint = "آدرس سایت"
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(pad, pad, pad, pad)
            setText(intent.getStringExtra("url") ?: "")
        }
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
        }
        val gv = GeckoView(this)

        session = GeckoSession(GeckoSessionSettings.Builder().contextId(id).build())
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(s: GeckoSession, canGoBack: Boolean) { canBack = canGoBack }
            override fun onNewSession(s: GeckoSession, uri: String): GeckoResult<GeckoSession>? {
                session.loadUri(uri); return null
            }
            // Show a readable error page (with code) instead of a blank screen.
            override fun onLoadError(s: GeckoSession, uri: String?, error: WebRequestError): GeckoResult<String>? {
                val html = "<html><body dir='rtl' style='font-family:sans-serif;padding:24px'>" +
                    "<h3>صفحه باز نشد</h3>" +
                    "<p dir='ltr'>" + Html.escapeHtml(uri ?: "") + "</p>" +
                    "<p dir='ltr'>error code: ${error.code} / category: ${error.category}</p>" +
                    "<p>شبکه یا VPN را بررسی کن و دوباره امتحان کن.</p></body></html>"
                return GeckoResult.fromValue("data:text/html," + Uri.encode(html))
            }
        }
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(s: GeckoSession, url: String) { progress.visibility = View.VISIBLE }
            override fun onProgressChange(s: GeckoSession, p: Int) {
                progress.progress = p
                progress.visibility = if (p in 1..99) View.VISIBLE else View.GONE
            }
            override fun onPageStop(s: GeckoSession, success: Boolean) { progress.visibility = View.GONE }
        }
        session.open(Gecko.get(this))
        gv.setSession(session)

        fun go() {
            var u = bar.text.toString().trim()
            if (u.isEmpty()) return
            if (!u.contains("://")) u = "https://$u"
            session.loadUri(u)
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(bar.windowToken, 0)
        }
        bar.setOnEditorActionListener { _, _, _ -> go(); true }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(LinearLayout(this@BrowserActivity).apply {
                setPadding(0, pad * 3, 0, 0)
                addView(Button(this@BrowserActivity).apply {
                    text = "⌂"
                    setOnClickListener { finish() }
                })
                addView(bar, LinearLayout.LayoutParams(0, -2, 1f))
            }, LinearLayout.LayoutParams(-1, -2))
            addView(progress, LinearLayout.LayoutParams(-1, -2))
            addView(gv, LinearLayout.LayoutParams(-1, 0, 1f))
        })
        if (bar.text.isNotEmpty()) go()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (canBack) session.goBack() else super.onBackPressed() }

    override fun onDestroy() { session.close(); super.onDestroy() }
}
