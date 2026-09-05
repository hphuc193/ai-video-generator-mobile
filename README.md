# 📱 AI Video Generator - Android Mobile App

Ứng dụng di động Android cho phép người dùng tạo video bằng AI từ hình ảnh và nội dung mô tả. Mobile App đóng vai trò là client chính, kết nối với Core Backend và hệ thống AI Processing Server.

---

## 🚀 Tính năng

- 🔐 **Đăng nhập Google**
  - Xác thực người dùng thông qua Google Sign-In.
  - Sử dụng OAuth 2.0.

- 🎬 **AI Video Studio**
  - Tạo video từ hình ảnh và mô tả bằng văn bản.
  - Gửi yêu cầu xử lý video đến hệ thống backend.
  - Theo dõi quá trình tạo video.

- 💳 **Credit & Thanh toán**
  - Hiển thị số dư Credit.
  - Nạp Credit thông qua các gói được hệ thống cung cấp.
  - Hỗ trợ sử dụng Promo Code.

- 📊 **Theo dõi tiến trình**
  - Theo dõi trạng thái xử lý:
    - `Pending`
    - `Processing`
    - `Success`
    - `Failed`

- 🌍 **Trải nghiệm người dùng**
  - Giao diện hiện đại.
  - Hỗ trợ đa ngôn ngữ.
  - Hỗ trợ Dark Mode.
  - Xử lý các trường hợp mạng không ổn định.
  - Hạn chế gửi request trùng lặp.

- ▶️ **Video Player**
  - Phát video trực tiếp trên ứng dụng.
  - Sử dụng Android Media3 / ExoPlayer.

---

## 🛠 Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| Kiến trúc | MVVM / MVC |
| UI | Android XML |
| Networking | Retrofit 2 + OkHttp 3 |
| Async | Kotlin Coroutines |
| Video Player | Android Media3 / ExoPlayer |
| Authentication | Google Sign-In / OAuth 2.0 |
| Security | `local.properties`, `BuildConfig` |

---

## 📁 Project Structure

```text
app/
├── src/
│   └── main/
│       ├── java/
│       │   └── ...
│       ├── res/
│       │   ├── drawable/
│       │   ├── layout/
│       │   ├── values/
│       │   └── values-vi/
│       └── AndroidManifest.xml
│
├── build.gradle.kts
└── proguard-rules.pro
