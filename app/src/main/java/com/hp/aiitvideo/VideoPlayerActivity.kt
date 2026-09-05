package com.hp.aiitvideo

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.databinding.ActivityVideoPlayerBinding
import com.hp.aiitvideo.databinding.ItemLoadingStepBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private var isPolling = false
    private var currentVideoUrl: String? = null

    private var currentFakeProgress = 0
    private val loadingMessages = listOf(
        "Đang phân tích ý tưởng (Prompt)...",
        "Đang khởi tạo máy chủ AI...",
        "Đang tải tài nguyên hình ảnh...",
        "Đang tính toán chuyển động (Motion)...",
        "Đang kết xuất các khung hình đầu tiên...",
        "Gần xong rồi, hệ thống đang ghép nối âm thanh...",
        "Đang thực hiện bước tinh chỉnh cuối (Upscale)...",
        "Quá trình này có thể mất 1-3 phút, vui lòng giữ máy..."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val projectId = intent.getIntExtra("EXTRA_ID", -1)
        val initialStatus = intent.getStringExtra("EXTRA_STATUS") ?: "PENDING"
        currentVideoUrl = intent.getStringExtra("EXTRA_URL")
        binding.tvPromptDescription.text = "Ý tưởng của bạn:\n${intent.getStringExtra("EXTRA_PROMPT")}"

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCancelLoading.setOnClickListener {
            isPolling = false
            Toast.makeText(this, "Đã hủy tiến trình xem trước.", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupStepsText()

        updateUIBasedOnStatus(initialStatus, projectId)

        binding.btnShare.setOnClickListener {
            if (currentVideoUrl != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Tuyệt đỉnh! Xem video AI tôi vừa tạo nè: \n$currentVideoUrl")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ video qua..."))
            }
        }

        binding.btnDownload.setOnClickListener {
            if (currentVideoUrl != null) {
                val fileName = "AI_Video_${System.currentTimeMillis()}.mp4"
                downloadVideo(currentVideoUrl!!, fileName)
            }
        }
    }

    private fun setupStepsText() {
        binding.step1.tvStepTitle.text = "Phân tích kịch bản"
        binding.step1.tvStepDesc.text = "Hiểu rõ nội dung và ý tưởng của bạn"

        binding.step2.tvStepTitle.text = "Khởi tạo khung cảnh"
        binding.step2.tvStepDesc.text = "Xây dựng chuỗi hình ảnh trực quan"

        binding.step3.tvStepTitle.text = "Kết xuất (Render)"
        binding.step3.tvStepDesc.text = "Kết xuất chi tiết các khung hình video"

        binding.step4.tvStepTitle.text = "Hoàn thiện"
        binding.step4.tvStepDesc.text = "Đánh bóng và đóng gói dữ liệu"
    }

    private fun updateUIBasedOnStatus(status: String, projectId: Int) {
        when (status) {
            "SUCCESS" -> {
                isPolling = false
                animateProgress(100)

                binding.layoutLoading.visibility = View.GONE
                binding.layoutPlayerResult.visibility = View.VISIBLE

                if (currentVideoUrl != null) setupPlayer(currentVideoUrl!!)
            }
            "FAILED" -> {
                isPolling = false
                binding.layoutLoading.visibility = View.GONE
                Toast.makeText(this, "Tạo video thất bại! Đã hoàn lại Credit.", Toast.LENGTH_LONG).show()
                finish()
            }
            else -> {
                binding.layoutPlayerResult.visibility = View.GONE
                binding.layoutLoading.visibility = View.VISIBLE

                currentFakeProgress = 0
                updateStepsUI(0)

                if (!isPolling && projectId != -1) {
                    startPollingVideoStatus(projectId)
                }
            }
        }
    }

    private fun startPollingVideoStatus(projectId: Int) {
        isPolling = true
        lifecycleScope.launch {
            val token = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE).getString("TOKEN", "") ?: ""
            var messageIndex = 0

            while (isPolling) {
                if (currentFakeProgress < 98) {
                    val targetProgress = currentFakeProgress + (4..12).random()
                    val finalTarget = if (targetProgress > 98) 98 else targetProgress

                    animateProgress(finalTarget)

                    binding.tvStatusMessage.text = loadingMessages[messageIndex % loadingMessages.size]
                    messageIndex++
                }

                try {
                    val response = ApiClient.apiService.getProjectById("Bearer $token", projectId)
                    if (response.isSuccessful && response.body() != null) {
                        val project = response.body()!!.project
                        currentVideoUrl = project.videoUrl

                        if (project.status == "SUCCESS" || project.status == "FAILED") {
                            updateUIBasedOnStatus(project.status, projectId)
                        }
                    }
                } catch (e: Exception) {
                }

                delay(3500)
            }
        }
    }

    private fun animateProgress(targetProgress: Int) {
        val animator = ValueAnimator.ofInt(currentFakeProgress, targetProgress)
        animator.duration = 2500

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int

            binding.tvProgressCircle.text = "$progress%"
            binding.tvProgressLinear.text = "$progress%"
            binding.progressCircle.progress = progress
            binding.progressLinear.progress = progress

            binding.tvSubStatus.text = when (progress) {
                in 0..25 -> "Đang đọc kịch bản..."
                in 26..50 -> "Đang tạo khung hình..."
                in 51..85 -> "Đang ghép cảnh..."
                else -> "Hoàn tất dữ liệu..."
            }

            binding.tvTimeEstimate.text = when (progress) {
                in 0..40 -> "Khoảng 2 phút nữa"
                in 41..80 -> "Khoảng 1 phút nữa"
                else -> "Sắp xong rồi..."
            }

            updateStepsUI(progress)
        }
        animator.start()
        currentFakeProgress = targetProgress
    }

    private fun updateStepsUI(progress: Int) {
        fun setStepState(step: ItemLoadingStepBinding, isPassed: Boolean, isActive: Boolean) {
            if (isPassed) {
                step.imgStepIcon.setImageResource(android.R.drawable.checkbox_on_background)
                step.imgStepIcon.setColorFilter(Color.parseColor("#A78BFA"))
                step.tvStepTitle.setTextColor(Color.WHITE)
                step.tvStepDesc.setTextColor(Color.parseColor("#A0A0A0"))
            } else if (isActive) {
                step.imgStepIcon.setImageResource(android.R.drawable.presence_online)
                step.imgStepIcon.setColorFilter(Color.parseColor("#A78BFA"))
                step.tvStepTitle.setTextColor(Color.WHITE)
                step.tvStepDesc.setTextColor(Color.parseColor("#A0A0A0"))
            } else {
                step.imgStepIcon.setImageResource(android.R.drawable.radiobutton_off_background)
                step.imgStepIcon.setColorFilter(Color.parseColor("#4A4D5A"))
                step.tvStepTitle.setTextColor(Color.parseColor("#4A4D5A"))
                step.tvStepDesc.setTextColor(Color.parseColor("#4A4D5A"))
            }
        }

        setStepState(binding.step1, isPassed = progress > 25, isActive = progress in 0..25)
        setStepState(binding.step2, isPassed = progress > 50, isActive = progress in 26..50)
        setStepState(binding.step3, isPassed = progress > 85, isActive = progress in 51..85)
        setStepState(binding.step4, isPassed = progress == 100, isActive = progress in 86..99)
    }

    private fun setupPlayer(rawUrl: String) {
        val fixedUrl = rawUrl.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer
        exoPlayer?.setMediaItem(MediaItem.fromUri(fixedUrl))
        exoPlayer?.prepare()
        exoPlayer?.volume = 1f
        exoPlayer?.playWhenReady = true
    }

    private fun downloadVideo(rawUrl: String, fileName: String) {
        try {
            val fixedUrl = rawUrl.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
            val request = android.app.DownloadManager.Request(android.net.Uri.parse(fixedUrl))
                .setTitle("AI Video")
                .setDescription("Đang tải video về máy...")
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_MOVIES, fileName)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this, "Đang tải video! Kiểm tra thanh thông báo.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi tải video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }
}