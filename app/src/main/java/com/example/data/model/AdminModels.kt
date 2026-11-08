package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AdminRole(val roleKey: String, val displayName: String, val description: String) {
    SUPER_ADMIN(
        "super_admin",
        "Owner / Super Admin",
        "Full authority, manage admins, security rules, global settings"
    ),
    USER_MANAGER(
        "user_manager",
        "User Manager",
        "Manage users, suspend & reactivate user accounts"
    ),
    TEMPLATE_MODERATOR(
        "template_moderator",
        "Template Moderator",
        "Review, approve, suspend & restore CV templates"
    ),
    CREATOR_MANAGER(
        "creator_manager",
        "Creator Manager",
        "Review applications, grant & suspend creator privileges"
    ),
    SUPPORT_ADMIN(
        "support_admin",
        "Support Admin",
        "Manage user complaints, tickets & abuse reports"
    ),
    FINANCE_MANAGER(
        "finance_manager",
        "Finance Manager",
        "Review payout records and platform commission"
    );

    companion object {
        fun fromKey(key: String): AdminRole {
            return entries.firstOrNull { it.roleKey == key } ?: SUPPORT_ADMIN
        }
    }
}

@IgnoreExtraProperties
data class Administrator(
    @DocumentId val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "support_admin",
    val permissions: List<String> = emptyList(),
    val isActive: Boolean = true,
    val assignedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val adminRole: AdminRole get() = AdminRole.fromKey(role)
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val status: String = "active", // active, suspended, banned
    val suspensionReason: String = "",
    val isCreator: Boolean = false,
    val cvCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val lastActiveAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CvRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val templateId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CreatorApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val applicantName: String = "",
    val applicantEmail: String = "",
    val portfolioUrl: String = "",
    val experience: String = "",
    val status: String = "pending", // pending, approved, rejected, suspended
    val rejectionReason: String = "",
    val reviewedBy: String = "",
    @ServerTimestamp val reviewedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CvTemplate(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "Professional", // Modern, Creative, Minimal, Executive, etc.
    val thumbnailUrl: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val status: String = "pending", // pending, approved, rejected, suspended
    val rejectionReason: String = "",
    val downloadsCount: Long = 0L,
    val rating: Double = 5.0,
    val isPremium: Boolean = false,
    val price: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupportReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val reporterEmail: String = "",
    val targetType: String = "template", // template, creator, user, general
    val targetId: String = "",
    val targetTitle: String = "",
    val reason: String = "",
    val description: String = "",
    val status: String = "pending", // pending, under_review, resolved, rejected
    val resolutionNotes: String = "",
    val resolvedBy: String = "",
    @ServerTimestamp val resolvedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val adminId: String = "",
    val adminEmail: String = "",
    val action: String = "", // user_suspended, template_approved, etc.
    val targetId: String = "",
    val targetType: String = "",
    val details: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

@IgnoreExtraProperties
data class AppSettings(
    val maintenanceMode: Boolean = false,
    val announcementText: String = "",
    val announcementActive: Boolean = false,
    val communitySubmissionsEnabled: Boolean = true,
    val supportEmail: String = "yahy38989@gmail.com",
    val appVersion: String = "1.0.0",
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val updatedBy: String = ""
)

@IgnoreExtraProperties
data class SystemNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val targetAudience: String = "all", // all, creators, specific_user
    val targetUserId: String = "",
    val sentBy: String = "",
    @ServerTimestamp val sentAt: Timestamp? = null,
    val deliveryCount: Long = 0L
)

data class DashboardStats(
    val totalUsers: Long = 0,
    val activeUsers: Long = 0,
    val newUsersToday: Long = 0,
    val totalCvsCreated: Long = 0,
    val totalTemplates: Long = 0,
    val pendingTemplates: Long = 0,
    val approvedTemplates: Long = 0,
    val pendingCreatorApps: Long = 0,
    val totalReports: Long = 0,
    val pendingReports: Long = 0
)
