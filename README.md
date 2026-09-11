# 📱 AI Video Generator - Android Mobile App
 
Ứng dụng di động trên nền tảng Android, cho phép người dùng sáng tạo video hoàn chỉnh từ hình ảnh và văn bản nhờ sức mạnh của AI. Đây là Client chính kết nối trực tiếp với hệ sinh thái backend và AI server.

 <img width="1772" height="985" alt="Image" src="https://github.com/user-attachments/assets/44ed8618-af82-4d14-b40e-b5dca954788b" />
 
## 🚀 Tính năng cốt lõi
- **Đăng nhập thông minh:** Tích hợp xác thực an toàn qua Google Sign-In (OAuth 2.0).
- **AI Video Studio:** Khởi tạo yêu cầu render video, lồng tiếng, lên kịch bản chỉ với 1 bức ảnh và ý tưởng ngắn.
- **Ví Credit & Thanh toán:** Theo dõi số dư, nạp gói tín dụng (Credit Packages) và sử dụng mã khuyến mãi.
- **Theo dõi thời gian thực:** Quản lý trạng thái xử lý video (Pending, Processing, Success).
- **UX/UI Tối ưu:** Hỗ trợ đa ngôn ngữ, Dark Mode và cơ chế chặn spam request khi mạng yếu.
## 🛠 Ngăn xếp công nghệ (Tech Stack)
- **Ngôn ngữ:** Kotlin
- **Kiến trúc/Mô hình:** MVVM / MVC (tùy thuộc vào thiết kế của bạn)
- **Networking:** Retrofit2, OkHttp3
- **Xử lý bất đồng bộ:** Kotlin Coroutines
- **Trình phát Media:** Android Media3 / ExoPlayer
- **Bảo mật:** Biến môi trường ẩn qua `local.properties` và `BuildConfig`
## ⚙️ Hướng dẫn cài đặt (Chạy môi trường phát triển)
 
1. Clone dự án về máy:
```bash
   git clone <repo_url>
```
 
2. Mở dự án bằng **Android Studio**.
3. Tạo file `local.properties` ở thư mục gốc và cấu hình Client ID:
```properties
   GOOGLE_CLIENT_ID=your_google_client_id_here
```
 
4. Bấm **Sync Project with Gradle Files**.
5. Chọn thiết bị ảo (Emulator) hoặc máy thật và bấm **Run** (Shift + F10).
---
 
*Dự án thuộc Hệ sinh thái AI Video Generator.*

https://github.com/hphuc193/video-ai-backend

https://github.com/hphuc193/ai-video-python-server

https://github.com/hphuc193/video-ai-admin

