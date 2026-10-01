package com.example.ailauncher

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

// One profile = one Gecko "contextId" = its own persistent cookies/storage (like a Chrome profile).
class BrowserActivity : Activity() {
    private lateinit var session: GeckoSession
    private var canBack = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val id = intent.getStringExtra("id") ?: return finish()
        val prefs = getSharedPreferences("accounts", MODE_PRIVATE)
        val pad = (12 * resources.displayMetrics.density).toInt()

        val bar = EditText(this).apply {
            hint = "آدرس سایت هوش مصنوعی را وارد کنید"
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(pad, pad, pad, pad)
            setText(prefs.getString("url_$id", ""))
        }
        val gv = GeckoView(this)

        session = GeckoSession(GeckoSessionSettings.Builder().contextId(id).build())
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(s: GeckoSession, canGoBack: Boolean) { canBack = canGoBack }
            // Popups (e.g. "Sign in with Google") load in the same tab.
            override fun onNewSession(s: GeckoSession, uri: String): GeckoResult<GeckoSession>? {
                session.loadUri(uri); return null
            }
        }
        session.open(Gecko.get(this))
        gv.setSession(session)

        fun go() {
            var u = bar.text.toString().trim()
            if (u.isEmpty()) return
            if (!u.contains("://")) u = "https://$u"
            session.loadUri(u)
            prefs.edit().putString("url_$id", u).apply()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(bar.windowToken, 0)
        }
        bar.setOnEditorActionListener { _, _, _ -> go(); true }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(LinearLayout(this@BrowserActivity).apply {
                setPadding(0, pad * 3, 0, 0)
                addView(bar, LinearLayout.LayoutParams(0, -2, 1f))
                addView(android.widget.Button(this@BrowserActivity).apply {
                    text = "G"
                    setOnClickListener { bar.setText("accounts.google.com"); go() }
                })
            }, LinearLayout.LayoutParams(-1, -2))
            addView(gv, LinearLayout.LayoutParams(-1, 0, 1f))
        })
        if (bar.text.isNotEmpty()) go()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (canBack) session.goBack() else super.onBackPressed() }

    override fun onDestroy() { session.close(); super.onDestroy() }
}
