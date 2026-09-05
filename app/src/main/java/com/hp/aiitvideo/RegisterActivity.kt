package com.hp.aiitvideo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.api.RegisterRequest
import com.hp.aiitvideo.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.edtFullName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val promoCode = binding.edtPromoCode.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ Tên, Email và Mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    binding.btnRegister.text = "ĐANG XỬ LÝ..."
                    binding.btnRegister.isEnabled = false

                    val finalPromo = if (promoCode.isNotEmpty()) promoCode else null
                    val request = RegisterRequest(fullName, email, password, finalPromo)

                    val response = ApiClient.apiService.register(request)

                    if (response.isSuccessful && response.body() != null) {
                        val message = response.body()!!.message
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()

                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Email đã tồn tại hoặc mã KM sai!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.btnRegister.text = "ĐĂNG KÝ"
                    binding.btnRegister.isEnabled = true
                }
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}