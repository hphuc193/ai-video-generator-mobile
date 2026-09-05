package com.hp.aiitvideo

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.api.UpdateProfileRequest
import com.hp.aiitvideo.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private var selectedAvatarUri: Uri? = null
    private var dialogAvatarImageView: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri
            dialogAvatarImageView?.let {
                it.imageTintList = null
                it.setPadding(0, 0, 0, 0)
                it.background = null

                Glide.with(this).load(uri).circleCrop().into(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)

        updateProfileUI()

        binding.btnShortcutCredit.setOnClickListener {
            val intent = Intent(this, CreditActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        binding.btnReload.setOnClickListener {
            syncProfileFromServer()
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        val isDarkMode = sharedPref.getBoolean("DARK_MODE", false)
        binding.switchDarkMode.isChecked = isDarkMode

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("DARK_MODE", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnLanguage.setOnClickListener {
            showLanguageDialog()
        }
        binding.btnLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Tiếng Việt", "English")
        val langCodes = arrayOf("vi", "en")

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        val currentLang = sharedPref.getString("LANGUAGE", "vi")
        val checkedItem = langCodes.indexOf(currentLang)

        AlertDialog.Builder(this)
            .setTitle("Chọn ngôn ngữ (Select Language)")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLangCode = langCodes[which]

                sharedPref.edit().putString("LANGUAGE", selectedLangCode).apply()

                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(selectedLangCode)
                )
                dialog.dismiss()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun updateProfileUI() {
        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        binding.tvFullName.text = sharedPref.getString("FULL_NAME", "Người dùng")
        binding.tvEmail.text = sharedPref.getString("EMAIL", "Chưa có email")

        val avatarUrl = sharedPref.getString("AVATAR", null)
        if (!avatarUrl.isNullOrEmpty()) {
            binding.imgAvatar.imageTintList = null
            binding.imgAvatar.setPadding(0, 0, 0, 0)
            binding.imgAvatar.background = null

            val fixedUrl = avatarUrl.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
            Glide.with(this)
                .load(fixedUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .circleCrop()
                .into(binding.imgAvatar)
        }
    }
    private fun syncProfileFromServer() {
        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        val token = "Bearer ${sharedPref.getString("TOKEN", "")}"

        lifecycleScope.launch {
            try {
                Toast.makeText(this@ProfileActivity, "Đang đồng bộ...", Toast.LENGTH_SHORT).show()
                val response = ApiClient.apiService.getProfile(token)

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sharedPref.edit()
                        .putString("FULL_NAME", user.fullName)
                        .putString("EMAIL", user.email)
                        .putString("AVATAR", user.avatar)
                        .putString("DOB", user.dob)
                        .putInt("CREDIT_BALANCE", user.creditBalance)
                        .apply()

                    updateProfileUI()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Lỗi đồng bộ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        selectedAvatarUri = null

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val edtName = dialogView.findViewById<EditText>(R.id.edtEditName)
        val edtDob = dialogView.findViewById<EditText>(R.id.edtEditDob)
        val edtPassword = dialogView.findViewById<EditText>(R.id.edtEditPassword)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveProfile)

        val layoutEditAvatar = dialogView.findViewById<FrameLayout>(R.id.layoutEditAvatar)
        dialogAvatarImageView = dialogView.findViewById<ImageView>(R.id.imgEditAvatar)

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)

        edtName.setText(sharedPref.getString("FULL_NAME", ""))
        edtDob.setText(sharedPref.getString("DOB", ""))

        val currentAvatarUrl = sharedPref.getString("AVATAR", null)
        if (!currentAvatarUrl.isNullOrEmpty() && dialogAvatarImageView != null) {
            dialogAvatarImageView!!.imageTintList = null
            dialogAvatarImageView!!.setPadding(0, 0, 0, 0)
            dialogAvatarImageView!!.background = null

            val fixedUrl = currentAvatarUrl.replace("127.0.0.1", "10.0.2.2")
            Glide.with(this).load(fixedUrl).circleCrop().into(dialogAvatarImageView!!)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        layoutEditAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        edtDob.setOnClickListener {
            showDatePickerDialog(edtDob)
        }

        btnSave.setOnClickListener {
            val newName = edtName.text.toString().trim()
            val newDob = edtDob.text.toString().trim()
            val newPass = edtPassword.text.toString().trim()

            val token = "Bearer ${sharedPref.getString("TOKEN", "")}"

            lifecycleScope.launch {
                try {
                    btnSave.text = "ĐANG LƯU..."
                    btnSave.isEnabled = false

                    var finalAvatarUrl: String? = null
                    if (selectedAvatarUri != null) {
                        val file = uriToFile(selectedAvatarUri!!)
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("images", file.name, requestFile)

                        val uploadRes = ApiClient.apiService.uploadImage(token, imagePart)
                        if (uploadRes.isSuccessful && uploadRes.body() != null) {
                            finalAvatarUrl = uploadRes.body()!!.urls[0].replace("10.0.2.2", "127.0.0.1")
                        } else {
                            throw Exception("Lỗi Upload ảnh!")
                        }
                    }

                    val reqName = if (newName.isNotEmpty()) newName else null
                    val reqPass = if (newPass.isNotEmpty()) newPass else null
                    val reqDob = if (newDob.isNotEmpty()) newDob else null

                    val requestBody = UpdateProfileRequest(reqName, reqPass, finalAvatarUrl, reqDob)
                    val response = ApiClient.apiService.updateProfile(token, requestBody)

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@ProfileActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        syncProfileFromServer()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Lỗi cập nhật hồ sơ!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, e.message ?: "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                } finally {
                    btnSave.text = "LƯU THAY ĐỔI"
                    btnSave.isEnabled = true
                }
            }
        }

        dialog.show()
    }


    private fun showDatePickerDialog(edtDob: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            val formattedDay = String.format("%02d", selectedDay)
            edtDob.setText("$selectedYear-$formattedMonth-$formattedDay")
        }, year, month, day).show()
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = contentResolver
        val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    override fun onResume() {
        super.onResume()
        com.hp.aiitvideo.utils.BottomNavHelper.setupBottomNav(this, binding.bottomNav, R.id.nav_profile)
    }
}