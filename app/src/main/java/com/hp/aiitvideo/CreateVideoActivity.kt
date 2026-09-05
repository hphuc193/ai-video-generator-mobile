package com.hp.aiitvideo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.api.CreateProjectRequest
import com.hp.aiitvideo.api.ProjectSettings
import com.hp.aiitvideo.databinding.ActivityCreateVideoBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CreateVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateVideoBinding
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imgPreview.setPadding(0, 0, 0, 0)
            Glide.with(this).load(uri).into(binding.imgPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bắt sự kiện nút Quay lại (Back)
        binding.btnBack.setOnClickListener { finish() }

        // Khởi tạo giao diện (Dropdown và Ràng buộc)
        setupUIConstraints()

        // Chạm vào ảnh
        binding.imgPreview.setOnClickListener {
            pickMedia.launch("image/*")
        }

        // Bấm TẠO VIDEO
        binding.btnSubmitCreate.setOnClickListener {
            val prompt = binding.edtPrompt.text.toString().trim()
            if (selectedImageUri == null) {
                Toast.makeText(this, getString(R.string.Please_select_photo), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (prompt.isEmpty()) {
                Toast.makeText(this, getString(R.string.Please_enter_idea), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            processVideoCreation(prompt)
        }
    }

    private fun setupUIConstraints() {
        val models = arrayOf("veo-3.1-lite-generate-preview", "veo-3.1-fast-generate-preview", "veo-3.1-generate-preview")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, models)

        binding.spinnerModel.setAdapter(adapter)
        binding.spinnerModel.setText(models[0], false)

        binding.rgDuration.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb4s || checkedId == R.id.rb6s) {
                binding.rb720p.isChecked = true
                binding.rb1080p.isEnabled = false
                binding.rb4k.isEnabled = false
            } else if (checkedId == R.id.rb8s) {
                binding.rb1080p.isEnabled = true
                binding.rb4k.isEnabled = true
            }
        }
    }

    private fun processVideoCreation(prompt: String) {
        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", null) ?: return

        lifecycleScope.launch {
            try {
                binding.btnSubmitCreate.text = getString(R.string.uploading)
                binding.btnSubmitCreate.isEnabled = false

                // UPLOAD ẢNH
                val file = uriToFile(selectedImageUri!!)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("images", file.name, requestFile)
                val uploadRes = ApiClient.apiService.uploadImage("Bearer $token", imagePart)

                if (!uploadRes.isSuccessful || uploadRes.body() == null) throw Exception("Lỗi Upload ảnh!")
                val uploadedUrl = uploadRes.body()!!.urls[0].replace("10.0.2.2", "127.0.0.1")

                binding.btnSubmitCreate.text = getString(R.string.activating_veo)

                val selectedModel = binding.spinnerModel.text.toString()

                val selectedDuration = when (binding.rgDuration.checkedRadioButtonId) {
                    R.id.rb6s -> 6
                    R.id.rb8s -> 8
                    else -> 4
                }

                val selectedResolution = when (binding.rgResolution.checkedRadioButtonId) {
                    R.id.rb1080p -> "1080p"
                    R.id.rb4k -> "4k"
                    else -> "720p"
                }

                val selectedRatio = if (binding.rb916.isChecked) "9:16" else "16:9"

                val settings = ProjectSettings(
                    model = selectedModel,
                    duration_seconds = selectedDuration,
                    resolution = selectedResolution,
                    aspect_ratio = selectedRatio
                )

                val projectReq = CreateProjectRequest(
                    prompt_idea = prompt,
                    images = listOf(uploadedUrl),
                    settings = settings
                )

                val projectRes = ApiClient.apiService.createProject("Bearer $token", projectReq)

                if (projectRes.isSuccessful && projectRes.body() != null) {
                    val createdProject = projectRes.body()!!.project
                    sharedPref.edit().putInt("CREDIT_BALANCE", projectRes.body()!!.remainingBalance).apply()
                    Toast.makeText(this@CreateVideoActivity, "Đang xử lý Video...", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@CreateVideoActivity, VideoPlayerActivity::class.java).apply {
                        putExtra("EXTRA_ID", createdProject.id)
                        putExtra("EXTRA_URL", createdProject.videoUrl)
                        putExtra("EXTRA_STATUS", createdProject.status)
                        putExtra("EXTRA_PROMPT", createdProject.prompt)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreateVideoActivity, "Số dư không đủ hoặc cấu hình lỗi!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateVideoActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnSubmitCreate.text = "TẠO VIDEO"
                binding.btnSubmitCreate.isEnabled = true
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File(cacheDir, "temp_upload_image.jpg")
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        return tempFile
    }
}