1. File README cho Ứng dụng Di động (Mobile App - Android)
Lưu file với tên README.md ở thư mục gốc của dự án Android.

Markdown
# 📱 AI Video Generator - Android Mobile App

Ứng dụng di động trên nền tảng Android, cho phép người dùng sáng tạo video hoàn chỉnh từ hình ảnh và văn bản nhờ sức mạnh của AI. Đây là Client chính kết nối trực tiếp với hệ sinh thái backend và AI server.

## 🚀 Tính năng cốt lõi
*   **Đăng nhập thông minh:** Tích hợp xác thực an toàn qua Google Sign-In (OAuth 2.0).
*   **AI Video Studio:** Khởi tạo yêu cầu render video, lồng tiếng, lên kịch bản chỉ với 1 bức ảnh và ý tưởng ngắn.
*   **Ví Credit & Thanh toán:** Theo dõi số dư, nạp gói tín dụng (Credit Packages) và sử dụng mã khuyến mãi.
*   **Theo dõi thời gian thực:** Quản lý trạng thái xử lý video (Pending, Processing, Success).
*   **UX/UI Tối ưu:** Hỗ trợ đa ngôn ngữ, Dark Mode và cơ chế chặn spam request khi mạng yếu.

## 🛠 Ngăn xếp công nghệ (Tech Stack)
*   **Ngôn ngữ:** Kotlin
*   **Kiến trúc/Mô hình:** MVVM / MVC (Tùy thuộc vào thiết kế của bạn)
*   **Networking:** Retrofit2, OkHttp3
*   **Xử lý bất đồng bộ:** Kotlin Coroutines
*   **Trình phát Media:** Android Media3 / ExoPlayer
*   **Bảo mật:** Biến môi trường ẩn qua `local.properties` và `BuildConfig`

## ⚙️ Hướng dẫn cài đặt (Chạy môi trường phát triển)
1. Clone dự án về máy: `git clone <repo_url>`
2. Mở dự án bằng **Android Studio**.
3. Tạo file `local.properties` ở thư mục gốc và cấu hình Client ID:
   ```properties
   GOOGLE_CLIENT_ID=your_google_client_id_here
Bấm Sync Project with Gradle Files.

Chọn thiết bị ảo (Emulator) hoặc máy thật và bấm Run (Shift + F10).

Dự án thuộc Hệ sinh thái AI Video Generator.


---

### 2. File README cho Web Admin (ReactJS)
*Lưu file với tên `README.md` ở thư mục gốc của dự án Web Admin.*

```markdown
# 💻 AI Video Generator - Admin Dashboard

Cổng thông tin quản trị trung tâm (Web Admin Portal) dành cho người điều hành hệ thống AI Video Generator. Được xây dựng dưới dạng ứng dụng SPA (Single Page Application) hiện đại.

## 🚀 Tính năng cốt lõi
*   **Bảng điều khiển (Dashboard):** Thống kê doanh thu, số lượng người dùng mới và giám sát tình trạng (Health Check) của AI Server.
*   **Quản lý Khách hàng:** Theo dõi lịch sử giao dịch, khóa/mở khóa tài khoản (Ban/Unban) và cộng/trừ Credit thủ công.
*   **Chiến dịch Kinh doanh:** Khởi tạo, bật/tắt (Toggle) các gói nạp Credit theo thời gian thực (Real-time sync to Mobile).
*   **Mã Khuyến mãi:** Quản lý số lượng và thời hạn sử dụng của Promo Code.
*   **Thiết kế Chuyên nghiệp:** Layout Layout chia tách chuẩn (Sticky Sidebar không cuộn, Main Content cuộn độc lập) đem lại trải nghiệm cao cấp.

## 🛠 Ngăn xếp công nghệ (Tech Stack)
*   **Core:** ReactJS, Vite (Build Tool)
*   **UI Framework:** Ant Design (AntD)
*   **Routing:** React Router v6
*   **Networking:** Axios (với cơ chế Interceptors gắn JWT Token)

## ⚙️ Hướng dẫn cài đặt
1. Clone dự án: `git clone <repo_url>`
2. Cài đặt các gói phụ thuộc (Dependencies):
   ```bash
   npm install
Tạo file .env ở thư mục gốc để trỏ API về Backend Node.js:

Đoạn mã
VITE_API_BASE_URL=http://localhost:3000/api
Chạy môi trường phát triển:

Bash
npm run dev
Dự án thuộc Hệ sinh thái AI Video Generator.


---

### 3. File README cho Core Backend (Node.js)
*Lưu file với tên `README.md` ở thư mục gốc của dự án `video-ai-backend`.*

```markdown
# ⚙️ AI Video Generator - Core Backend API

Máy chủ cốt lõi xử lý toàn bộ logic nghiệp vụ, xác thực bảo mật, quản lý cơ sở dữ liệu và điều phối luồng xử lý tới AI Server. Được xây dựng theo kiến trúc RESTful API.

## 🚀 Trách nhiệm chính (Core Responsibilities)
*   **Xác thực (Authentication):** Xử lý luồng đăng nhập Google OAuth 2.0, cấp phát và xác minh JWT Token (RBAC - Role Based Access Control).
*   **Toàn vẹn Dữ liệu Tài chính:** Sử dụng Database Transactions để xử lý việc nạp, trừ Credit, đảm bảo quy tắc ACID (Không bao giờ trừ tiền nếu lịch sử giao dịch chưa được ghi lại).
*   **Điều phối AI (Worker Coordination):** Nhận lệnh tạo video từ Mobile, ghi nhận trạng thái Pending vào DB, sau đó trigger HTTP Request sang AI Server để kết xuất đồ họa.
*   **Webhook Handler:** Mở cổng Webhook nhận thông báo từ AI Server để cập nhật trạng thái Video (Success/Failed) và thực hiện hoàn tiền tự động nếu render lỗi.

## 🛠 Ngăn xếp công nghệ (Tech Stack)
*   **Runtime:** Node.js
*   **Framework:** Express.js
*   **Database ORM:** Prisma
*   **Database:** PostgreSQL (Khuyến nghị chạy qua Docker)
*   **Bảo mật:** JWT (JSON Web Token), bcrypt

## ⚙️ Hướng dẫn cài đặt
1. Clone dự án: `git clone <repo_url>`
2. Cài đặt thư viện: `npm install`
3. Tạo file `.env` (Tham khảo `.env.sample`) với thông tin kết nối DB và JWT Secret.
4. Chạy cấu trúc Database (Migrations):
   ```bash
   npx prisma migrate dev
Khởi động Server:

Bash
npm start
(Server sẽ chạy mặc định ở cổng 3000).

Dự án thuộc Hệ sinh thái AI Video Generator.


---

### 4. File README cho AI Server Worker (Python)
*Lưu file với tên `README.md` ở thư mục gốc của dự án Python AI.*

```markdown
# 🧠 AI Video Generator - Python Processing Engine

Máy chủ độc lập (Worker) chịu trách nhiệm thực thi các tác vụ tính toán nặng nề nhất: Xử lý trí tuệ nhân tạo, kết xuất đồ họa (render) và xử lý âm thanh. Thiết kế tách biệt giúp Backend Node.js không bao giờ bị nghẽn (Block).

## 🚀 Trách nhiệm chính (Core Responsibilities)
*   **Biên kịch AI:** Gọi API Google Gemini phân tích hình ảnh đầu vào và tự động sáng tạo kịch bản/Prompt ngắn gọn, chuẩn xác.
*   **Lồng tiếng (TTS):** Tích hợp Edge-TTS để chuyển kịch bản văn bản thành giọng đọc tự nhiên.
*   **Render Hình ảnh (Video Gen):** Giao tiếp với Google Veo API để tạo video động từ hình ảnh và Prompt.
*   **Hậu kỳ tự động:** Dùng MoviePy ghép Audio và Video lại với nhau, cắt thời lượng cho đồng bộ.
*   **Tiến trình ngầm (Background Tasks):** Sử dụng `asyncio` để không block API Request, tự động gọi Webhook về Node.js khi hoàn tất.

## 🛠 Ngăn xếp công nghệ (Tech Stack)
*   **Ngôn ngữ:** Python 3.10+
*   **Framework:** FastAPI, Uvicorn
*   **AI Integration:** Google GenAI SDK (Gemini & Veo)
*   **Media Processing:** MoviePy, Edge-TTS
*   **Networking:** HTTPX / Requests

## ⚙️ Hướng dẫn cài đặt
1. Clone dự án: `git clone <repo_url>`
2. Khởi tạo môi trường ảo (Virtual Environment):
   ```bash
   python -m venv venv
   source venv/bin/activate  # Trên Windows dùng: venv\Scripts\activate
Cài đặt thư viện (Yêu cầu phải có file requirements.txt):

Bash
pip install -r requirements.txt
Đặt khóa Google Gemini API vào file .env:

Đoạn mã
GEMINI_API_KEY=your_gemini_api_key_here
Chạy AI Server:

Bash
uvicorn main:app --reload --port 8000
Dự án thuộc Hệ sinh thái AI Video Generator.


**💡 Lời khuyên khi đưa lên GitHub:**
