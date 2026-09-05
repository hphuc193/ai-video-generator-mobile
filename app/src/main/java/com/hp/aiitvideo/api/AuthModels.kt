package com.hp.aiitvideo.api
import com.google.gson.annotations.SerializedName
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Int,
    val fullName: String?,
    val email: String,
    val role: String,
    val avatar: String?,
    val creditBalance: Int
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val promoCode: String? = null
)

data class RegisterResponse(
    val message: String,
    val userId: Int,
    val creditBalance: Int
)

data class UserProfileResponse(
    val id: Int,
    val email: String,
    val fullName: String?,
    val creditBalance: Int,
    val role: String,
    val avatar: String?,
    val dob: String?
)

data class UploadResponse(
    val message: String,
    val urls: List<String>
)

data class ProjectSettings(
    val model: String = "veo-3.1-lite-generate-preview",
    val duration_seconds: Int = 4,
    val resolution: String = "720p",
    val aspect_ratio: String = "16:9"
)

data class CreateProjectRequest(
    val prompt_idea: String,
    val images: List<String>,
    val settings: ProjectSettings
)

data class CreateProjectResponse(
    val message: String,
    val project: VideoProject,
    val remainingBalance: Int
)
data class ProjectDetailResponse(
    val project: VideoProject
)

data class VideoProject(
    val id: Int,
    val prompt: String,
    val status: String,
    val videoUrl: String?,
    val creditCost: Int,
    val createdAt: String
)

data class ProjectListResponse(
    val projects: List<VideoProject>
)

data class CreditPackage(val id: Int, val name: String, val credits: Int, val price: Float)
data class PackageResponse(@SerializedName("data") val data: List<CreditPackage>?)

data class BuyPackageRequest(val packageId: Int)
data class BuyPackageResponse(val message: String)

data class Promotion(val id: Int, val code: String, val rewardCredits: Int)
data class PromotionResponse(@SerializedName("data") val data: List<Promotion>?)

data class PromoRequest(val code: String)
data class PromoResponse(val message: String)

data class Transaction(val id: Int, val amount: Int, val type: String, val reason: String, val createdAt: String)
data class HistoryResponse(@SerializedName("data") val data: List<Transaction>?)

data class UpdateProfileRequest(
    val fullName: String?,
    val password: String?,
    val avatar: String?,
    val dob: String?
)

data class UpdateProfileResponse(
    val message: String,
    val user: UserProfileResponse
)

data class GoogleLoginRequest(val idToken: String)

data class MessageResponse(val message: String)