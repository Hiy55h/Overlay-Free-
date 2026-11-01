package com.hiy55h.overlayfree

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SidebarService : Service() {
    private lateinit var wm: WindowManager
    private var handle: View? = null
    private var sidebar: View? = null
    private var notes: View? = null
    private val channelId = "overlay_free_service"

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotification()
        startForeground(1, buildNotification())
        showHandle()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Overlay Free", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, channelId)
                .setContentTitle("Overlay Free")
                .setContentText("サイドバーが動作中")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Overlay Free")
                .setContentText("サイドバーが動作中")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build()
        }
    }

    private fun overlayParams(width: Int, height: Int, gravity: Int = Gravity.TOP or Gravity.START): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            width, height,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = 12
            y = 180
        }
    }

    private fun showHandle() {
        if (handle != null) return
        val b = TextView(this).apply {
            text = "☰"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF315B9A.toInt())
            setPadding(18, 14, 18, 14)
            setOnClickListener { toggleSidebar() }
            setOnTouchListener(object : View.OnTouchListener {
                var downX = 0f
                var downY = 0f
                var startX = 0
                var startY = 0
                override fun onTouch(v: View, e: MotionEvent): Boolean {
                    val lp = v.layoutParams as WindowManager.LayoutParams
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = e.rawX; downY = e.rawY
                            startX = lp.x; startY = lp.y
                            return false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            lp.x = startX + (e.rawX - downX).toInt()
                            lp.y = startY + (e.rawY - downY).toInt()
                            wm.updateViewLayout(v, lp)
                            return true
                        }
                    }
                    return false
                }
            })
        }
        handle = b
        wm.addView(b, overlayParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT))
    }

    private fun toggleSidebar() {
        if (sidebar != null) {
            remove(sidebar); sidebar = null
        } else showSidebar()
    }

    private fun showSidebar() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 14)
            setBackgroundColor(0xF0182438.toInt())
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "Overlay Free"
            textSize = 20f
            setTextColor(Color.WHITE)
        }
        val close = Button(this).apply {
            text = "×"
            setOnClickListener { remove(sidebar); sidebar = null }
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(close)
        panel.addView(header)

        val notesButton = Button(this).apply {
            text = "📝 メモをウィンドウで開く"
            setOnClickListener { showNotes() }
        }
        panel.addView(notesButton)

        val label = TextView(this).apply {
            text = "インストール済みアプリ"
            textSize = 16f
            setTextColor(0xFFBBD2F4.toInt())
            setPadding(4, 12, 4, 8)
        }
        panel.addView(label)

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val apps = packageManager.getInstalledApplications(0)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
        for (app in apps) {
            val name = packageManager.getApplicationLabel(app).toString()
            val button = Button(this).apply {
                text = name
                isAllCaps = false
                setOnClickListener { launchApp(app) }
            }
            list.addView(button)
        }
        scroll.addView(list)
        panel.addView(scroll, LinearLayout.LayoutParams(300, 0, 1f))
        sidebar = panel
        wm.addView(panel, overlayParams(340, 560))
    }

    private fun launchApp(app: ApplicationInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { startActivity(intent) } catch (_: Exception) { }
        remove(sidebar); sidebar = null
    }

    private fun showNotes() {
        if (notes != null) {
            remove(notes); notes = null
            return
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFF202B3D.toInt())
        }
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val heading = TextView(this).apply {
            text = "メモ"
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        val close = Button(this).apply {
            text = "×"
            setOnClickListener { remove(notes); notes = null }
        }
        bar.addView(heading, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(close)
        box.addView(bar)
        val edit = EditText(this).apply {
            hint = "ここにメモを入力"
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB0BEC5.toInt())
            minLines = 7
            gravity = Gravity.TOP
            setBackgroundColor(0xFF34445B.toInt())
        }
        box.addView(edit, LinearLayout.LayoutParams(320, 260))
        notes = box
        val p = overlayParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        p.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        p.x = 380; p.y = 200
        wm.addView(box, p)
    }

    private fun remove(view: View?) {
        if (view != null) try { wm.removeView(view) } catch (_: Exception) { }
    }

    override fun onDestroy() {
        remove(sidebar); remove(notes); remove(handle)
        sidebar = null; notes = null; handle = null
        super.onDestroy()
    }
}
