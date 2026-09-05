package com.hp.aiitvideo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hp.aiitvideo.adapter.VideoAdapter
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.api.VideoProject
import com.hp.aiitvideo.databinding.ActivityProjectListBinding
import kotlinx.coroutines.launch

class ProjectListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectListBinding
    private lateinit var videoAdapter: VideoAdapter
    private var isGridMode = false

    private var allProjectsList: List<VideoProject> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

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

        updateLayoutManager()
        binding.rvAllProjects.adapter = videoAdapter

        binding.btnToggleView.setOnClickListener {
            isGridMode = !isGridMode
            updateLayoutManager()
        }

        binding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.primary_blue, theme))
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchAllProjects()
        }

        fetchAllProjects()

        binding.rgStatusFilter.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }

        binding.edtSearch.addTextChangedListener {
            applyFilters()
        }
    }

    private fun updateLayoutManager() {
        if (isGridMode) {
            binding.rvAllProjects.layoutManager = GridLayoutManager(this, 2)
            binding.btnToggleView.setImageResource(R.drawable.baseline_list_alt_24)
        } else {
            binding.rvAllProjects.layoutManager = LinearLayoutManager(this)
            binding.btnToggleView.setImageResource(R.drawable.baseline_grid_view_24)
        }
        videoAdapter.isGridMode = isGridMode
    }

    override fun onResume() {
        super.onResume()
        com.hp.aiitvideo.utils.BottomNavHelper.setupBottomNav(this, binding.bottomNav, R.id.nav_projects)
    }

    private fun fetchAllProjects() {
        val token = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE).getString("TOKEN", null) ?: return

        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getAllProjects("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    allProjectsList = response.body()!!.projects
                    applyFilters()
                } else {
                    Toast.makeText(this@ProjectListActivity, "Lỗi lấy dữ liệu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProjectListActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    // Hàm Lọc & Tìm kiếm
    private fun applyFilters() {
        val keyword = binding.edtSearch.text.toString().trim().lowercase()

        // Lọc Text
        var filteredList = if (keyword.isEmpty()) {
            allProjectsList
        } else {
            allProjectsList.filter { it.prompt.lowercase().contains(keyword) }
        }

        filteredList = when (binding.rgStatusFilter.checkedRadioButtonId) {
            R.id.rbSuccess -> filteredList.filter { it.status == "SUCCESS" }
            R.id.rbPending -> filteredList.filter { it.status == "PENDING" }
            R.id.rbFailed -> filteredList.filter { it.status == "FAILED" }
            else -> filteredList
        }

        videoAdapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            binding.rvAllProjects.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvAllProjects.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        }
    }

    private fun deleteProjectFromApi(projectId: Int) {
        val token = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE).getString("TOKEN", null) ?: return

        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.deleteProject("Bearer $token", projectId)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectListActivity, "Đã xóa dự án thành công!", Toast.LENGTH_SHORT).show()
                    fetchAllProjects()
                } else {
                    Toast.makeText(this@ProjectListActivity, "Lỗi khi xóa dự án", Toast.LENGTH_SHORT).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProjectListActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}