package com.hp.aiitvideo.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    // API Đăng nhập
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/v1/users/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    // 1. Upload Ảnh\
    @Multipart
    @POST("api/v1/upload/images")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part images: MultipartBody.Part
    ): Response<UploadResponse>

    // 2. Tạo Dự án Video
    @POST("api/v1/projects")
    suspend fun createProject(
        @Header("Authorization") token: String,
        @Body request: CreateProjectRequest
    ): Response<CreateProjectResponse>

    // Lấy danh sách video của user
    @GET("api/v1/projects")
    suspend fun getAllProjects(
        @Header("Authorization") token: String
    ): Response<ProjectListResponse>

    // Thêm API lấy chi tiết 1 Project
    @GET("api/v1/projects/{id}")
    suspend fun getProjectById(
        @Header("Authorization") token: String,
        @Path("id") projectId: Int
    ): Response<ProjectDetailResponse>

    @POST("api/v1/credits/redeem")
    suspend fun redeemPromoCode(
        @Header("Authorization") token: String,
        @Body request: PromoRequest
    ): retrofit2.Response<PromoResponse>

    // API Lấy danh sách gói
    @GET("api/v1/credits/packages")
    suspend fun getPackages(@Header("Authorization") token: String): retrofit2.Response<PackageResponse>

    // API Mua gói
    @POST("api/v1/credits/buy")
    suspend fun buyPackage(@Header("Authorization") token: String, @Body request: BuyPackageRequest): retrofit2.Response<BuyPackageResponse>

    // API Xem Khuyến mãi đang có
    @GET("api/v1/credits/promotions")
    suspend fun getPromotions(@Header("Authorization") token: String): retrofit2.Response<PromotionResponse>

    // API Nhập mã Khuyến mãi
    @POST("api/v1/credits/promotions/apply")
    suspend fun applyPromotion(@Header("Authorization") token: String, @Body request: PromoRequest): retrofit2.Response<PromoResponse>

    // API Lịch sử
    @GET("api/v1/users/me/credits")
    suspend fun getTransactionHistory(@Header("Authorization") token: String): retrofit2.Response<HistoryResponse>

    // Cập nhật hồ sơ cá nhân
    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): retrofit2.Response<UpdateProfileResponse>

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): retrofit2.Response<LoginResponse>

    @DELETE("api/v1/projects/{id}")
    suspend fun deleteProject(
        @Header("Authorization") token: String,
        @Path("id") projectId: Int
    ): retrofit2.Response<MessageResponse>


}
