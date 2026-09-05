package com.hp.aiitvideo.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hp.aiitvideo.R
import com.hp.aiitvideo.api.VideoProject
import com.hp.aiitvideo.databinding.ItemVideoBinding
import com.hp.aiitvideo.databinding.ItemVideoGridBinding

class VideoAdapter(
    private var videoList: List<VideoProject>,
    private val onItemClick: (VideoProject) -> Unit,
    private val onDeleteClick: (VideoProject) -> Unit // Callback xử lý sự kiện Xóa
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Biến cờ hiệu: true = Lưới (Grid), false = Danh sách (List)
    var isGridMode = false
        set(value) {
            field = value
            notifyDataSetChanged() // Báo cho danh sách vẽ lại toàn bộ khi đổi chế độ
        }

    // Quy định loại View: 1 là Grid, 0 là List
    override fun getItemViewType(position: Int): Int {
        return if (isGridMode) 1 else 0
    }

    // Tùy theo ViewType mà bơm layout tương ứng
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            GridViewHolder(ItemVideoGridBinding.inflate(inflater, parent, false))
        } else {
            ListViewHolder(ItemVideoBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val video = videoList[position]
        if (holder is ListViewHolder) {
            holder.bind(video)
        } else if (holder is GridViewHolder) {
            holder.bind(video)
        }
    }

    override fun getItemCount(): Int = videoList.size

    fun updateData(newList: List<VideoProject>) {
        videoList = newList
        notifyDataSetChanged()
    }

    // ==========================================
    // 1. VIEWHOLDER CHO DẠNG DANH SÁCH (LIST)
    // ==========================================
    inner class ListViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: VideoProject) {
            binding.tvTitle.text = video.prompt
            binding.tvTime.text = video.createdAt.take(10)
            setupVideoStatus(video, binding.tvStatus, binding.imgThumbnail)

            // Sự kiện bấm vào cả Item để xem chi tiết
            binding.root.setOnClickListener { onItemClick(video) }

            // Sự kiện bấm vào nút 3 chấm để mở Menu Xóa
            binding.btnMore.setOnClickListener { view ->
                showDeleteMenu(view, video)
            }
        }
    }

    // ==========================================
    // 2. VIEWHOLDER CHO DẠNG LƯỚI (GRID)
    // ==========================================
    inner class GridViewHolder(private val binding: ItemVideoGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: VideoProject) {
            binding.tvTitle.text = video.prompt
            binding.tvTime.text = video.createdAt.take(10)
            setupVideoStatus(video, binding.tvStatus, binding.imgThumbnail)

            // Sự kiện bấm vào cả Item để xem chi tiết
            binding.root.setOnClickListener { onItemClick(video) }

            // Sự kiện bấm vào nút 3 chấm để mở Menu Xóa
            binding.btnMore.setOnClickListener { view ->
                showDeleteMenu(view, video)
            }
        }
    }

    // ==========================================
    // 3. HÀM HIỂN THỊ MENU VÀ HỘP THOẠI XÓA
    // ==========================================
    private fun showDeleteMenu(view: View, project: VideoProject) {
        val context = view.context
        val popup = PopupMenu(context, view)

        // Gọi string động đa ngôn ngữ
        popup.menu.add(Menu.NONE, 1, 1, context.getString(R.string.menu_delete))

        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == 1) {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.dialog_delete_title))
                    .setMessage(context.getString(R.string.dialog_delete_message))
                    .setPositiveButton(context.getString(R.string.btn_delete)) { _, _ ->
                        onDeleteClick(project)
                    }
                    .setNegativeButton(context.getString(R.string.btn_cancel), null)
                    .show()
                true
            } else {
                false
            }
        }
        popup.show()
    }

    // ==========================================
    // 4. HÀM XỬ LÝ TRẠNG THÁI VÀ ẢNH BÌA
    // ==========================================
    private fun setupVideoStatus(video: VideoProject, tvStatus: android.widget.TextView, imgThumb: android.widget.ImageView) {
        val context = imgThumb.context
        val iconPaddingPx = (24 * context.resources.displayMetrics.density).toInt()

        when (video.status) {
            "SUCCESS" -> {
                tvStatus.text = context.getString(R.string.status_success)
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))

                imgThumb.setPadding(0, 0, 0, 0)
                imgThumb.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                imgThumb.colorFilter = null // Xóa bỏ màu nhuộm icon (nếu có)

                val fixedUrl = video.videoUrl?.replace("127.0.0.1", "10.0.2.2")
                Glide.with(context)
                    .load(fixedUrl)
                    .centerCrop()
                    .into(imgThumb)
            }
            "FAILED" -> {
                tvStatus.text = context.getString(R.string.status_failed)
                tvStatus.setTextColor(Color.parseColor("#F44336"))

                Glide.with(context).clear(imgThumb)

                imgThumb.setPadding(iconPaddingPx, iconPaddingPx, iconPaddingPx, iconPaddingPx)
                imgThumb.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                imgThumb.setImageResource(R.drawable.baseline_cancel_presentation_24)
            }
            else -> {
                tvStatus.text = context.getString(R.string.status_pending)
                tvStatus.setTextColor(Color.parseColor("#2196F3"))

                Glide.with(context).clear(imgThumb)

                imgThumb.setPadding(iconPaddingPx, iconPaddingPx, iconPaddingPx, iconPaddingPx)
                imgThumb.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                imgThumb.setImageResource(R.drawable.baseline_running_with_errors_24)
            }
        }
    }
}