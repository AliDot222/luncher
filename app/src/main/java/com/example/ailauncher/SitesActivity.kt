package com.example.ailauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

// Per-profile start page: buttons for your saved sites (shared list, editable).
class SitesActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("accounts", MODE_PRIVATE) }
    private val sites = mutableListOf<JSONObject>()
    private lateinit var adapter: BaseAdapter
    private lateinit var profileId: String
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val defaults = listOf(
        "Google Account" to "https://accounts.google.com",
        "ChatGPT" to "https://chatgpt.com",
        "DeepSeek" to "https://chat.deepseek.com",
        "Claude" to "https://claude.ai",
        "ClickUp" to "https://app.clickup.com",
        "Copilot" to "https://copilot.microsoft.com",
        "Gemini" to "https://gemini.google.com",
        "AI Studio" to "https://aistudio.google.com",
        "NotebookLM" to "https://notebooklm.google.com",
        "Qwen" to "https://chat.qwen.ai",
        "Kimi" to "https://www.kimi.com",
        "Z.ai" to "https://chat.z.ai"
    )

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        profileId = intent.getStringExtra("id") ?: return finish()
        val saved = prefs.getString("sites", null)
        val arr = if (saved == null) {
            JSONArray().also { a -> defaults.forEach { a.put(JSONObject().put("name", it.first).put("url", it.second)) } }
        } else JSONArray(saved)
        for (i in 0 until arr.length()) sites.add(arr.getJSONObject(i))
        persist()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(16))
        }
        val head = TextView(this).apply {
            text = intent.getStringExtra("name") ?: ""
            textSize = 22f
            setPadding(0, 0, 0, dp(12))
        }
        val add = Button(this).apply { text = "+ افزودن سایت"; setOnClickListener { showEdit(null) } }
        val grid = GridView(this).apply {
            numColumns = 2
            verticalSpacing = dp(10)
            horizontalSpacing = dp(10)
        }
        adapter = object : BaseAdapter() {
            override fun getCount() = sites.size
            override fun getItem(p: Int) = sites[p]
            override fun getItemId(p: Int) = p.toLong()
            override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                val name = sites[p].getString("name")
                val hue = (name.hashCode() and 0x7fffffff) % 360
                return TextView(this@SitesActivity).apply {
                    text = name
                    gravity = Gravity.CENTER
                    textSize = 16f
                    maxLines = 1
                    setTextColor(Color.WHITE)
                    setPadding(dp(8), dp(20), dp(8), dp(20))
                    background = GradientDrawable().apply {
                        cornerRadius = dp(14).toFloat()
                        setColor(Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.5f, 0.55f)))
                    }
                }
            }
        }
        grid.adapter = adapter
        grid.setOnItemClickListener { _, _, p, _ ->
            startActivity(
                Intent(this, BrowserActivity::class.java)
                    .putExtra("id", profileId)
                    .putExtra("url", sites[p].getString("url"))
            )
        }
        grid.setOnItemLongClickListener { _, _, p, _ ->
            AlertDialog.Builder(this).setItems(arrayOf("ویرایش", "حذف")) { _, which ->
                if (which == 0) showEdit(p) else { sites.removeAt(p); refresh() }
            }.show()
            true
        }
        root.addView(head)
        root.addView(add)
        root.addView(grid, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    private fun persist() {
        prefs.edit().putString("sites", JSONArray(sites).toString()).apply()
    }

    private fun refresh() { persist(); adapter.notifyDataSetChanged() }

    private fun showEdit(p: Int?) {
        val old = p?.let { sites[it] }
        val nameEt = EditText(this).apply {
            hint = "نام (مثلاً ChatGPT)"
            setSingleLine()
            setText(old?.getString("name") ?: "")
        }
        val urlEt = EditText(this).apply {
            hint = "آدرس (مثلاً chatgpt.com)"
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setText(old?.getString("url") ?: "")
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(nameEt); addView(urlEt)
        }
        AlertDialog.Builder(this)
            .setTitle(if (old == null) "سایت جدید" else "ویرایش سایت")
            .setView(box)
            .setPositiveButton("ذخیره") { _, _ ->
                var u = urlEt.text.toString().trim()
                if (u.isNotEmpty()) {
                    if (!u.contains("://")) u = "https://$u"
                    val n = nameEt.text.toString().trim()
                        .ifEmpty { u.substringAfter("://").removePrefix("www.").substringBefore("/") }
                    val o = JSONObject().put("name", n).put("url", u)
                    if (p == null) sites.add(o) else sites[p] = o
                    refresh()
                }
            }
            .setNegativeButton("لغو", null).show()
    }
}
