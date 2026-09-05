package com.hp.aiitvideo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.api.GoogleLoginRequest
import com.hp.aiitvideo.api.LoginRequest
import com.hp.aiitvideo.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import com.hp.aiitvideo.BuildConfig

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                sendGoogleTokenToBackend(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Hủy đăng nhập Google", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        val savedToken = sharedPref.getString("TOKEN", null)

        if (savedToken != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    binding.btnLogin.text = "ĐANG ĐĂNG NHẬP..."
                    binding.btnLogin.isEnabled = false

                    val request = LoginRequest(email, password)
                    val response = ApiClient.apiService.login(request)

                    if (response.isSuccessful && response.body() != null) {
                        val loginData = response.body()!!

                        saveUserSession(loginData.token, loginData.user.fullName, loginData.user.creditBalance)

                        Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                } finally {
                    binding.btnLogin.text = "ĐĂNG NHẬP"
                    binding.btnLogin.isEnabled = true
                }
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 1. Cấu hình Google Sign-In (Đã sử dụng biến từ BuildConfig)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 2. Bắt sự kiện bấm nút
        binding.btnGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleAuthLauncher.launch(signInIntent)
            }
        }
    }

    private fun sendGoogleTokenToBackend(idToken: String) {
        lifecycleScope.launch {
            try {
                val res = ApiClient.apiService.googleLogin(GoogleLoginRequest(idToken))
                if (res.isSuccessful && res.body() != null) {
                    val token = res.body()!!.token
                    val user = res.body()!!.user

                    val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit()
                        .putString("TOKEN", token)
                        .putString("FULL_NAME", user.fullName)
                        .putString("EMAIL", user.email)
                        .putString("AVATAR", user.avatar)
                        .apply()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Lỗi Server Node.js", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserSession(token: String, fullName: String?, credit: Int) {
        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("TOKEN", token)
            putString("FULL_NAME", fullName ?: "Người dùng")
            putInt("CREDIT_BALANCE", credit)
            apply() // Lưu bất đồng bộ
        }
    }
}