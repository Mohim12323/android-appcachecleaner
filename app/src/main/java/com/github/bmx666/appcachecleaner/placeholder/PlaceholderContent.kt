package com.github.bmx666.appcachecleaner.placeholder

import android.app.usage.StorageStats
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*

object PlaceholderContent {

    val ITEMS: MutableList<PlaceholderPackage> = ArrayList()
    val DISPLAYED_ITEMS: MutableList<PlaceholderPackage> = ArrayList()

    fun contains(pkgInfo: PackageInfo): Boolean {
        return ITEMS.any { it.name == pkgInfo.packageName }
    }

    fun reset() {
        DISPLAYED_ITEMS.clear()
        ITEMS.forEach{ it.ignore = true }
    }

    fun sort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sortByCacheSize()
        else
            sortByLabel()
        DISPLAYED_ITEMS.addAll(ITEMS.filter { !it.ignore })
    }

    private fun sortByLabel() {
        ITEMS.sortWith(compareBy<PlaceholderPackage> { !it.checked }.thenBy { it.label })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByCacheSize() {
        ITEMS.sortWith(compareBy<PlaceholderPackage> { !it.checked }
            .thenByDescending { it.stats?.cacheBytes ?: 0 }.thenBy { it.label })
    }

    fun addItem(pkgInfo: PackageInfo, label: String, locale: Locale, icon: Drawable?,
                checked: Boolean, stats: StorageStats?) {
        ITEMS.add(
            PlaceholderPackage(
                pkgInfo = pkgInfo,
                name = pkgInfo.packageName,
                label = label,
                locale = locale,
                icon = icon,
                stats = stats,
                checked = checked,
                ignore = false))
    }

    fun updateStats(pkgInfo: PackageInfo, stats: StorageStats?) {
        ITEMS.find { it.name == pkgInfo.packageName }?.let {
            it.stats = stats; it.ignore = false
        }
    }

    fun isSameLabelLocale(pkgInfo: PackageInfo, locale: Locale): Boolean {
        ITEMS.find { it.name == pkgInfo.packageName }?.let {
            return it.locale == locale
        }
        return false
    }

    fun updateLabel(pkgInfo: PackageInfo, label: String, locale: Locale) {
        ITEMS.find { it.name == pkgInfo.packageName }?.let {
            it.label = label; it.locale = locale; it.ignore = false
        }
    }

    data class PlaceholderPackage(val pkgInfo: PackageInfo, val name: String,
                                  var label: String, var locale: Locale,
                                  val icon: Drawable?, var stats: StorageStats?,
                                  var checked: Boolean, var ignore: Boolean) {
        override fun toString(): String = name
    }
}