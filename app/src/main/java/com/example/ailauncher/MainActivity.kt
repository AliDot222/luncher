package com.example.ailauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("accounts", MODE_PRIVATE) }
    private val items = mutableListOf<JSONObject>()
    private lateinit var adapter: BaseAdapter
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val arr = JSONArray(prefs.getString("list", "[]"))
        for (i in 0 until arr.length()) items.add(arr.getJSONObject(i))

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(16))
        }
        val add = Button(this).apply { text = "ایجاد پروفایل"; setOnClickListener { showAdd() } }
        val grid = GridView(this).apply { numColumns = 3; verticalSpacing = dp(16) }

        adapter = object : BaseAdapter() {
            override fun getCount() = items.size
            override fun getItem(p: Int) = items[p]
            override fun getItemId(p: Int) = p.toLong()
            override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                val name = items[p].getString("name")
                val hue = (name.hashCode() and 0x7fffffff) % 360
                val circle = TextView(this@MainActivity).apply {
                    text = name.take(2).uppercase()
                    gravity = Gravity.CENTER; textSize = 22f; setTextColor(Color.WHITE)
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.55f, 0.65f)))
                    }
                    layoutParams = LinearLayout.LayoutParams(dp(72), dp(72))
                }
                val label = TextView(this@MainActivity).apply {
                    text = name; gravity = Gravity.CENTER; maxLines = 1
                    setPadding(0, dp(6), 0, 0)
                }
                return LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                    addView(circle); addView(label)
                }
            }
        }
        grid.adapter = adapter
        grid.setOnItemClickListener { _, _, p, _ ->
            startActivity(Intent(this, BrowserActivity::class.java).putExtra("id", items[p].getString("id")))
        }
        grid.setOnItemLongClickListener { _, _, p, _ -> confirmDelete(p); true }

        root.addView(add)
        root.addView(grid, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    private fun save() {
        prefs.edit().putString("list", JSONArray(items).toString()).apply()
        adapter.notifyDataSetChanged()
    }

    private fun showAdd() {
        val et = EditText(this).apply { hint = "یک اسم انتخاب کنید (مثلاً chatgpt_1)"; setSingleLine() }
        AlertDialog.Builder(this).setTitle("پروفایل جدید").setView(et)
            .setPositiveButton("ساخت") { _, _ ->
                val name = et.text.toString().trim().ifEmpty { "profile_${items.size + 1}" }
                val id = "p_${System.currentTimeMillis()}"
                items.add(JSONObject().put("id", id).put("name", name))
                prefs.edit().putString("url_$id", "https://accounts.google.com").apply()
                save()
                startActivity(Intent(this, BrowserActivity::class.java).putExtra("id", id))
            }
            .setNegativeButton("لغو", null).show()
    }

    private fun confirmDelete(p: Int) {
        val o = items[p]
        AlertDialog.Builder(this).setTitle("حذف ${o.getString("name")}؟")
            .setMessage("لاگین ذخیره‌شدهٔ این پروفایل پاک می‌شود.")
            .setPositiveButton("حذف") { _, _ ->
                runCatching { Gecko.get(this).storageController.clearDataForSessionContext(o.getString("id")) }
                prefs.edit().remove("url_${o.getString("id")}").apply()
                items.removeAt(p); save()
            }
            .setNegativeButton("لغو", null).show()
    }
}
