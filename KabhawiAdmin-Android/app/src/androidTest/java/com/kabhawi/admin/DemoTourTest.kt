package com.kabhawi.admin

import android.content.Intent
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

/**
 * جولة تلقائية في التطبيق تُستخدم لتسجيل فيديو العرض (انظر demo/record_demo.sh).
 * تتطلب خادماً تجريبياً على http://10.0.2.2:8080 (demo/mock_server.py).
 */
@RunWith(AndroidJUnit4::class)
class DemoTourTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val context = instrumentation.targetContext
    private val shotsDir = File(context.getExternalFilesDir(null), "shots")
    private var shotIndex = 0

    @Before
    fun setUp() {
        Configurator.getInstance().waitForIdleTimeout = 500
        Configurator.getInstance().actionAcknowledgmentTimeout = 500
        shotsDir.mkdirs()
    }

    @Test
    fun tour() {
        launchApp()

        // ---------- تسجيل الدخول ----------
        find(By.res("login_server"))
        pause(2500)
        shot("login")
        find(By.res("login_server")).text = "http://10.0.2.2:8080"
        pause(800)
        tap(By.res("login_test"))
        find(By.text("تم الاتصال بالخادم بنجاح"))
        pause(1500)
        find(By.res("login_username")).text = "admin@kabhawi.com"
        pause(600)
        find(By.res("login_password")).text = "Kabhawi@2026"
        pause(1000)
        tap(By.res("login_submit"))

        // ---------- لوحة التحكم ----------
        find(By.text("أحدث الطلبات"), 30_000)
        pause(4000)
        shot("dashboard")
        swipeUp()
        pause(2500)
        shot("dashboard_more")
        swipeUp()
        pause(2500)
        swipeDown()
        swipeDown()
        pause(1200)

        // ---------- الطلبات ----------
        tap(By.res("nav_ORDERS"))
        find(By.text("طلب #1012"))
        pause(2000)
        tap(By.text("طلب #1010"))
        find(By.text("تأكيد الشحن"))
        pause(3000)
        shot("order_detail")
        tap(By.text("تأكيد الشحن"))
        pause(1500)
        shot("order_confirm")
        tap(By.text("تأكيد"))
        pause(3000)
        tap(By.textStartsWith("جديد ("))
        pause(2500)
        shot("orders_filtered")
        tap(By.textStartsWith("الكل ("))
        pause(1200)

        // ---------- المنتجات ----------
        tap(By.res("nav_PRODUCTS"))
        find(By.text("قلادة ذهب بتصميم الهلال"))
        pause(3500)
        shot("products")
        tap(By.text("خواتم"))
        pause(2500)
        tap(By.text("كل الأقسام"))
        pause(1500)
        tap(By.text("قلادة ذهب بتصميم الهلال"))
        find(By.text("حفظ الكمية"))
        pause(3000)
        shot("product_detail")
        tap(By.desc("زيادة"))
        pause(600)
        tap(By.desc("زيادة"))
        pause(1200)
        tap(By.text("حفظ الكمية"))
        pause(3000)
        tap(By.desc("إغلاق"))
        pause(1500)
        tap(By.text("إضافة منتج"))
        find(By.text("المعلومات الأساسية"))
        pause(3500)
        shot("product_editor")
        tap(By.text("إلغاء"))
        pause(1500)

        // ---------- الأقسام ----------
        tap(By.res("nav_CATEGORIES"))
        find(By.text("أطقم العرائس"))
        pause(3500)
        shot("categories")

        // ---------- المستخدمون ----------
        tap(By.res("nav_USERS"))
        find(By.text("admin@kabhawi.com"))
        pause(3500)
        shot("users")

        // ---------- الإعدادات ----------
        tap(By.res("nav_SETTINGS"))
        find(By.text("إعدادات العرض"))
        pause(3000)
        shot("settings")

        // ---------- الوضع العمودي ----------
        tap(By.res("nav_DASHBOARD"))
        find(By.text("أحدث الطلبات"))
        pause(1500)
        device.setOrientationLeft()
        pause(3500)
        shot("dashboard_portrait")
        tap(By.res("nav_ORDERS"))
        pause(3000)
        shot("orders_portrait")
        device.setOrientationNatural()
        pause(2500)
        tap(By.res("nav_DASHBOARD"))
        pause(3000)
        device.unfreezeRotation()
    }

    private fun launchApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 20_000)
    }

    private fun find(selector: BySelector, timeout: Long = 15_000): UiObject2 =
        device.wait(Until.findObject(selector), timeout)
            ?: throw AssertionError("Element not found on screen: $selector")

    private fun tap(selector: BySelector) {
        find(selector).click()
    }

    private fun pause(millis: Long) = SystemClock.sleep(millis)

    private fun shot(name: String) {
        shotIndex += 1
        device.takeScreenshot(File(shotsDir, String.format(Locale.US, "%02d_%s.png", shotIndex, name)))
    }

    private fun swipeUp() {
        val x = (device.displayWidth * 0.45).toInt()
        device.swipe(x, (device.displayHeight * 0.78).toInt(), x, (device.displayHeight * 0.30).toInt(), 60)
    }

    private fun swipeDown() {
        val x = (device.displayWidth * 0.45).toInt()
        device.swipe(x, (device.displayHeight * 0.30).toInt(), x, (device.displayHeight * 0.78).toInt(), 30)
    }
}
