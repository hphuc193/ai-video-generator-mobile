package com.hp.aiitvideo.utils

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hp.aiitvideo.CreditActivity
import com.hp.aiitvideo.MainActivity
import com.hp.aiitvideo.ProfileActivity
import com.hp.aiitvideo.ProjectListActivity
import com.hp.aiitvideo.R

object BottomNavHelper {
    fun setupBottomNav(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        // Đánh dấu nút hiện tại đang được chọn (sáng lên)
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            // Nếu bấm vào nút đang đứng thì không làm gì cả
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.nav_home -> Intent(activity, MainActivity::class.java)
                R.id.nav_projects -> Intent(activity, ProjectListActivity::class.java)
                R.id.nav_credit -> Intent(activity, CreditActivity::class.java)
                R.id.nav_profile -> Intent(activity, ProfileActivity::class.java)
                else -> null
            }

            intent?.let {
                // TUYỆT CHIÊU: Mang màn hình cũ lên trên cùng thay vì tạo mới (Giữ nguyên vị trí cuộn)
                it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(it)
                // Tắt hiệu ứng trượt màn hình để trông giống như chỉ đổi Tab
                activity.overridePendingTransition(0, 0)
            }
            true
        }
    }
}