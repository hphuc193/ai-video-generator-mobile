package com.hp.aiitvideo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hp.aiitvideo.api.ApiClient
import com.hp.aiitvideo.api.BuyPackageRequest
import com.hp.aiitvideo.api.CreditPackage
import com.hp.aiitvideo.api.PromoRequest
import com.hp.aiitvideo.api.Transaction
import com.hp.aiitvideo.databinding.ActivityCreditBinding
import com.hp.aiitvideo.databinding.ItemCreditPackageBinding
import com.hp.aiitvideo.databinding.ItemTransactionBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CreditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreditBinding

    private var packageAdapter: PackageAdapter? = null
    private var historyAdapter: HistoryAdapter? = null

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupActions()

        updateBalanceUI()

        showTopTab(isPackageTab = true)

        fetchData()
    }

    // UI SETUP
    private fun setupRecyclerViews() {

        binding.rvPackages.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.rvPackages.setHasFixedSize(true)

        binding.rvHistory.layoutManager =
            LinearLayoutManager(this)

        binding.rvHistory.setHasFixedSize(true)
    }

    private fun setupActions() {

        // Tab Nạp Credit
        binding.tabPackages.setOnClickListener {
            showTopTab(true)
        }

        // Tab Lịch sử
        binding.tabHistory.setOnClickListener {
            showTopTab(false)
        }

        // Refresh toàn bộ dữ liệu
        binding.btnRefresh.setOnClickListener {
            fetchData()
        }

        // Áp dụng mã khuyến mãi
        binding.btnApplyPromo.setOnClickListener {

            val code = binding.edtPromoCode.text
                .toString()
                .trim()
                .uppercase(Locale.getDefault())

            if (code.isEmpty()) {
                binding.edtPromoCode.error = "Vui lòng nhập mã"
                return@setOnClickListener
            }

            redeemCode(code)
        }

        // Xem danh sách mã khuyến mãi
        binding.tvViewPromos.setOnClickListener {
            fetchAndShowPromotions()
        }

        // Xóa nội dung mã promo
        binding.btnClearPromo.setOnClickListener {
            binding.edtPromoCode.text.clear()
        }
    }

    private fun showTopTab(isPackageTab: Boolean) {

        if (isPackageTab) {

            binding.tabPackages.isSelected = true
            binding.tabHistory.isSelected = false

            binding.layoutPackages.visibility = View.VISIBLE
            binding.layoutHistory.visibility = View.GONE

        } else {

            binding.tabPackages.isSelected = false
            binding.tabHistory.isSelected = true

            binding.layoutPackages.visibility = View.GONE
            binding.layoutHistory.visibility = View.VISIBLE
        }
    }

    // BALANCE

    private fun updateBalanceUI() {

        val savedCredit = getSharedPreferences(
            "VideoAppPrefs",
            Context.MODE_PRIVATE
        ).getInt("CREDIT_BALANCE", 0)

        binding.tvCurrentCredit.text = formatCredit(savedCredit)
    }

    private fun formatCredit(value: Int): String {
        return NumberFormat
            .getNumberInstance(Locale("vi", "VN"))
            .format(value)
    }

    // API DATA
    private fun fetchData() {

        if (isLoading) return

        isLoading = true

        val token = getToken()

        setLoading(true)

        lifecycleScope.launch {

            try {

                // Lấy danh sách gói Credit
                val pkgRes = ApiClient.apiService.getPackages(token)

                if (pkgRes.isSuccessful && pkgRes.body() != null) {

                    val listPackages =
                        pkgRes.body()!!.data ?: emptyList()

                    packageAdapter =
                        PackageAdapter(listPackages) { selectedPkg ->
                            buyPackage(selectedPkg)
                        }

                    binding.rvPackages.adapter = packageAdapter

                    binding.tvPackageCount.text = getString(R.string.packages_count_format, listPackages.size)

                    binding.emptyPackages.visibility =
                        if (listPackages.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE
                }


                // Lấy lịch sử giao dịch
                val histRes =
                    ApiClient.apiService.getTransactionHistory(token)

                if (histRes.isSuccessful && histRes.body() != null) {

                    val listHistory =
                        histRes.body()!!.data ?: emptyList()

                    historyAdapter =
                        HistoryAdapter(listHistory)

                    binding.rvHistory.adapter = historyAdapter

                    binding.tvHistoryCount.text =
                        if (listHistory.isEmpty()) {
                            getString(R.string.no_have_transaction)
                        } else {
                            "${listHistory.size} ${getString(R.string.num_transactions)}"
                        }

                    binding.emptyHistory.visibility =
                        if (listHistory.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE
                }

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    this@CreditActivity,
                    "Không thể tải dữ liệu. Kiểm tra kết nối mạng.",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {

                isLoading = false
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {

        binding.progressBar.visibility =
            if (loading) View.VISIBLE else View.GONE

        binding.btnRefresh.isEnabled = !loading
    }

    private fun getToken(): String {

        return "Bearer ${
            getSharedPreferences(
                "VideoAppPrefs",
                Context.MODE_PRIVATE
            ).getString("TOKEN", "")
        }"
    }

    // SYNC BALANCE
    private suspend fun syncBalanceFromServer(token: String) {

        try {

            val res =
                ApiClient.apiService.getProfile(token)

            if (res.isSuccessful && res.body() != null) {

                val currentBalance =
                    res.body()!!.creditBalance

                getSharedPreferences(
                    "VideoAppPrefs",
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putInt(
                        "CREDIT_BALANCE",
                        currentBalance
                    )
                    .apply()

                updateBalanceUI()
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // BUY CREDIT PACKAGE
    private fun buyPackage(pkg: CreditPackage) {

        AlertDialog.Builder(this)
            .setTitle("Xác nhận nạp Credit")
            .setMessage(
                "Bạn muốn mua ${formatCredit(pkg.credits)} Credit " +
                        "với giá ${formatPrice(pkg.price.toDouble())}?"
            )
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xác nhận") { _, _ ->

                performBuyPackage(pkg)
            }
            .show()
    }

    private fun performBuyPackage(pkg: CreditPackage) {

        val token = getToken()

        lifecycleScope.launch {

            try {

                binding.btnRefresh.isEnabled = false

                Toast.makeText(
                    this@CreditActivity,
                    "Đang xử lý giao dịch...",
                    Toast.LENGTH_SHORT
                ).show()

                val response =
                    ApiClient.apiService.buyPackage(
                        token,
                        BuyPackageRequest(pkg.id)
                    )

                if (response.isSuccessful &&
                    response.body() != null
                ) {

                    Toast.makeText(
                        this@CreditActivity,
                        response.body()!!.message,
                        Toast.LENGTH_LONG
                    ).show()

                    // Đồng bộ số dư
                    syncBalanceFromServer(token)

                    // Cập nhật lịch sử
                    fetchData()

                } else {

                    Toast.makeText(
                        this@CreditActivity,
                        "Mua gói thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@CreditActivity,
                    "Lỗi kết nối đến máy chủ",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {

                binding.btnRefresh.isEnabled = true
            }
        }
    }

    // PROMOTION
    private fun redeemCode(code: String) {

        val token = getToken()

        lifecycleScope.launch {

            try {

                binding.btnApplyPromo.isEnabled = false

                val response =
                    ApiClient.apiService.applyPromotion(
                        token,
                        PromoRequest(code)
                    )

                if (response.isSuccessful &&
                    response.body() != null
                ) {

                    Toast.makeText(
                        this@CreditActivity,
                        response.body()!!.message,
                        Toast.LENGTH_LONG
                    ).show()

                    binding.edtPromoCode.text.clear()

                    // Đồng bộ số dư
                    syncBalanceFromServer(token)

                    // Refresh lịch sử
                    fetchData()

                } else {

                    val errorBody =
                        response.errorBody()?.string()

                    Toast.makeText(
                        this@CreditActivity,
                        "Mã không hợp lệ hoặc đã hết hạn",
                        Toast.LENGTH_LONG
                    ).show()

                    errorBody?.let {
                        println("Promo error: $it")
                    }
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@CreditActivity,
                    "Không thể kết nối đến máy chủ",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {

                binding.btnApplyPromo.isEnabled = true
            }
        }
    }

    // SHOW PROMOTIONS
    private fun fetchAndShowPromotions() {

        val token = getToken()

        lifecycleScope.launch {

            try {

                val response =
                    ApiClient.apiService.getPromotions(token)

                if (response.isSuccessful &&
                    response.body() != null
                ) {

                    val promos =
                        response.body()!!.data ?: emptyList()

                    if (promos.isEmpty()) {

                        Toast.makeText(
                            this@CreditActivity,
                            getString(R.string.no_promotions_available),
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    val promoTexts =
                        promos.map {
                            "${it.code}  •  +${it.rewardCredits} Credit"
                        }.toTypedArray()

                    AlertDialog.Builder(this@CreditActivity)
                        .setTitle(getString(R.string.promo_code))
                        .setItems(promoTexts) { _, which ->

                            val selectedPromo =
                                promos[which]

                            binding.edtPromoCode.setText(
                                selectedPromo.code
                            )

                            binding.edtPromoCode.requestFocus()

                            // Tự động chuyển về tab nạp
                            showTopTab(true)
                        }
                        .setNegativeButton(getString(R.string.close_tab), null)
                        .show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@CreditActivity,
                    getString(R.string.unable_load_promotions_),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    // COPY PROMO CODE
    private fun copyPromoCode(code: String) {

        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE)
                    as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                "Promotion Code",
                code
            )
        )

        Toast.makeText(
            this,
            getString(R.string.copied_code, code),
            Toast.LENGTH_SHORT
        ).show()
    }

    // FORMAT
    private fun formatPrice(price: Double): String {

        return NumberFormat
            .getCurrencyInstance(Locale("vi", "VN"))
            .format(price)
    }

    // BOTTOM NAV
    override fun onResume() {

        super.onResume()

        com.hp.aiitvideo.utils.BottomNavHelper.setupBottomNav(
            this,
            binding.bottomNav,
            R.id.nav_credit
        )

        updateBalanceUI()
    }

    // PACKAGE ADAPTER
    inner class PackageAdapter(
        private val packages: List<CreditPackage>,
        private val onBuyClick: (CreditPackage) -> Unit
    ) : RecyclerView.Adapter<PackageAdapter.PkgViewHolder>() {

        inner class PkgViewHolder(
            val binding: ItemCreditPackageBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(pkg: CreditPackage) {

                binding.tvCreditAmount.text =
                    formatCredit(pkg.credits)

                binding.tvPrice.text =
                    formatPrice(pkg.price.toDouble())

                binding.tvPackageDescription.text =
                    getString(R.string.credit_used_for_ai)

                binding.btnBuy.setOnClickListener {
                    onBuyClick(pkg)
                }

                binding.root.setOnClickListener {
                    onBuyClick(pkg)
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PkgViewHolder {

            return PkgViewHolder(
                ItemCreditPackageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(
            holder: PkgViewHolder,
            position: Int
        ) {

            holder.bind(packages[position])
        }

        override fun getItemCount() =
            packages.size
    }

    // ============================================================
    // HISTORY ADAPTER
    // ============================================================

    inner class HistoryAdapter(
        private val historyList: List<Transaction>
    ) : RecyclerView.Adapter<HistoryAdapter.HistViewHolder>() {

        inner class HistViewHolder(
            val binding: ItemTransactionBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(transaction: Transaction) {

                binding.tvDesc.text =
                    transaction.reason

                binding.tvDate.text =
                    transaction.createdAt
                        .take(16)
                        .replace("T", " ")

                val isAdd =
                    transaction.type == "ADD"

                if (isAdd) {
                    binding.tvAmount.text =
                        "+${formatCredit(transaction.amount)}"

                    binding.tvAmount.setTextColor(
                        Color.parseColor("#2E7D32")
                    )

                    binding.tvType.text = "CREDIT"

                    binding.imgIcon.setColorFilter(
                        Color.parseColor("#2E7D32")
                    )

                } else {
                    binding.tvAmount.text =
                        "-${formatCredit(transaction.amount)}"

                    binding.tvAmount.setTextColor(
                        Color.parseColor("#C62828")
                    )

                    binding.tvType.text = getString(R.string.use_btn)

                    binding.imgIcon.setColorFilter(
                        Color.parseColor("#C62828")
                    )
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): HistViewHolder {

            return HistViewHolder(
                ItemTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(
            holder: HistViewHolder,
            position: Int
        ) {

            holder.bind(historyList[position])
        }

        override fun getItemCount() =
            historyList.size
    }
}