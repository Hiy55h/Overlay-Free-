package com.hiy55h.overlayfree

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(0xFF101827.toInt())
        }
        val title = TextView(this).apply {
            text = "Overlay Free"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
        }
        val info = TextView(this).apply {
            text = "無料のフローティングサイドバー\n\nオーバーレイ権限を許可して開始してください。"
            textSize = 16f
            setTextColor(0xFFD7E2F2.toInt())
            setPadding(0, 18, 0, 24)
        }
        val permission = Button(this).apply {
            text = "オーバーレイ権限を設定"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= 23) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                }
            }
        }
        val start = Button(this).apply {
            text = "サイドバーを開始"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startService(Intent(this@MainActivity, SidebarService::class.java))
                    info.text = "サイドバーを起動しました。画面端の小さなボタンを押してください。"
                } else {
                    info.text = "先にオーバーレイ権限を許可してください。"
                }
            }
        }
        root.addView(title)
        root.addView(info)
        root.addView(permission)
        root.addView(start)
        setContentView(root)
    }
}
