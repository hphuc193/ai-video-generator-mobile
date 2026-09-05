package com.hp.aiitvideo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hp.aiitvideo.adapter.VideoAdapter
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var videoAdapter: VideoAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("TOKEN", null)

        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        videoAdapter = VideoAdapter(
            videoList = emptyList(),
            onItemClick = { selectedVideo ->
                val intent = Intent(this, VideoPlayerActivity::class.java).apply {
                    putExtra("EXTRA_ID", selectedVideo.id)
                    putExtra("EXTRA_URL", selectedVideo.videoUrl)
                    putExtra("EXTRA_STATUS", selectedVideo.status)
                    putExtra("EXTRA_PROMPT", selectedVideo.prompt)
                }
                startActivity(intent)
            },
            onDeleteClick = { projectToDelete ->
                deleteProjectFromApi(projectToDelete.id)
            }
        )
        binding.rvVideos.layoutManager = LinearLayoutManager(this)
        binding.rvVideos.adapter = videoAdapter

        binding.tvGreeting.text = "Hi, ${sharedPref.getString("FULL_NAME", "Người dùng")}"
        binding.tvCreditBalance.text = sharedPref.getInt("CREDIT_BALANCE", 0).toString()

        binding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.primary_blue, theme))
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchLatestProfile("Bearer $token")
        }
        fetchLatestProfile("Bearer $token")

        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(this, ProjectListActivity::class.java))
        }

        binding.btnCreateVideo.setOnClickListener {
            startActivity(Intent(this, CreateVideoActivity::class.java))
        }

        binding.btnDarkMode.setOnClickListener {
            val isDarkMode = sharedPref.getBoolean("DARK_MODE", false)
            val newMode = !isDarkMode

            sharedPref.edit().putBoolean("DARK_MODE", newMode).apply()

            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        com.hp.aiitvideo.utils.BottomNavHelper.setupBottomNav(this, binding.bottomNav, R.id.nav_home)

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        binding.tvGreeting.text = "Hi, ${sharedPref.getString("FULL_NAME", "Người dùng")}"
        binding.tvCreditBalance.text = sharedPref.getInt("CREDIT_BALANCE", 0).toString()
    }

    private fun fetchLatestProfile(bearerToken: String) {
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                val profileRes = ApiClient.apiService.getProfile(bearerToken)

                if (profileRes.isSuccessful && profileRes.body() != null) {
                    val userProfile = profileRes.body()!!

                    binding.tvGreeting.text = "Hi, ${userProfile.fullName ?: "Người dùng"} !"
                    binding.tvCreditBalance.text = userProfile.creditBalance.toString()

                    val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit()
                        .putString("FULL_NAME", userProfile.fullName)
                        .putInt("CREDIT_BALANCE", userProfile.creditBalance)
                        .putString("EMAIL", userProfile.email)
                        .apply()

                    val videoRes = ApiClient.apiService.getAllProjects(bearerToken)
                    if (videoRes.isSuccessful && videoRes.body() != null) {
                        val allProjects = videoRes.body()!!.projects
                        val recentProjects = allProjects.take(5)
                        videoAdapter.updateData(recentProjects)
                    }
                } else if (profileRes.code() == 401 || profileRes.code() == 403) {
                    Toast.makeText(this@MainActivity, "Phiên đăng nhập hết hạn!", Toast.LENGTH_LONG).show()
                    getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lỗi kết nối khi cập nhật dữ liệu!", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun deleteProjectFromApi(projectId: Int) {
        val currentToken = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE).getString("TOKEN", null) ?: return
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.deleteProject("Bearer $currentToken", projectId)
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Đã xóa dự án thành công!", Toast.LENGTH_SHORT).show()
                    fetchLatestProfile("Bearer $currentToken")
                } else {
                    Toast.makeText(this@MainActivity, "Lỗi khi xóa dự án", Toast.LENGTH_SHORT).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}