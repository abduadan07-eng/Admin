package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    fun getCurrentUserUid(): String? = auth.currentUser?.uid
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    // -------------------------------------------------------------
    // Administrators & Authorization
    // -------------------------------------------------------------
    suspend fun getAdministrator(adminId: String): Administrator? {
        val path = "administrators/$adminId"
        return try {
            val doc = db.collection("administrators").document(adminId).get().await()
            if (doc.exists()) doc.toObject(Administrator::class.java) else null
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    suspend fun bootstrapInitialOwner(uid: String, email: String, displayName: String) {
        val path = "administrators/$uid"
        try {
            val admin = hashMapOf(
                "id" to uid,
                "email" to email,
                "displayName" to displayName.ifBlank { "Owner / Super Admin" },
                "role" to AdminRole.SUPER_ADMIN.roleKey,
                "permissions" to listOf("all"),
                "isActive" to true,
                "assignedBy" to "system_initial",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("administrators").document(uid).set(admin).await()
            try {
                recordAudit("admin_bootstrapped", uid, "administrator", "Owner account provisioned")
            } catch (ignored: Exception) {
                // Non-critical audit failure
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    fun observeAdministrators(): Flow<List<Administrator>> = flow {
        val path = "administrators"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Administrator::class.java)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeAdministrators error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun createAdministrator(
        targetUid: String,
        email: String,
        displayName: String,
        role: AdminRole
    ) {
        val path = "administrators/$targetUid"
        try {
            val currentAdminEmail = getCurrentUserEmail() ?: "unknown"
            val data = hashMapOf(
                "id" to targetUid,
                "email" to email,
                "displayName" to displayName,
                "role" to role.roleKey,
                "permissions" to listOf(role.roleKey),
                "isActive" to true,
                "assignedBy" to currentAdminEmail,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("administrators").document(targetUid).set(data).await()
            recordAudit("admin_created", targetUid, "administrator", "Assigned role: ${role.displayName} to $email")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    suspend fun updateAdministratorStatus(adminId: String, isActive: Boolean, reason: String) {
        val path = "administrators/$adminId"
        try {
            db.collection("administrators").document(adminId).update(
                mapOf(
                    "isActive" to isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit(
                if (isActive) "admin_activated" else "admin_deactivated",
                adminId,
                "administrator",
                reason
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // -------------------------------------------------------------
    // Users Management
    // -------------------------------------------------------------
    fun observeUsers(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(UserProfile::class.java)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeUsers error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun updateUserStatus(userId: String, newStatus: String, reason: String) {
        val path = "users/$userId"
        try {
            db.collection("users").document(userId).update(
                mapOf(
                    "status" to newStatus,
                    "suspensionReason" to reason,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit("user_status_changed", userId, "user", "Status changed to $newStatus. Reason: $reason")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // -------------------------------------------------------------
    // Creator Applications
    // -------------------------------------------------------------
    fun observeCreatorApplications(): Flow<List<CreatorApplication>> = flow {
        val path = "creator_applications"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CreatorApplication::class.java)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeCreatorApplications error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun reviewCreatorApplication(
        applicationId: String,
        userId: String,
        approve: Boolean,
        reason: String
    ) {
        val path = "creator_applications/$applicationId"
        try {
            val adminEmail = getCurrentUserEmail() ?: "admin"
            val newStatus = if (approve) "approved" else "rejected"
            db.collection("creator_applications").document(applicationId).update(
                mapOf(
                    "status" to newStatus,
                    "rejectionReason" to reason,
                    "reviewedBy" to adminEmail,
                    "reviewedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (approve && userId.isNotBlank()) {
                db.collection("users").document(userId).update(
                    mapOf("isCreator" to true, "updatedAt" to FieldValue.serverTimestamp())
                ).await()
            }

            recordAudit("creator_application_reviewed", applicationId, "creator_application", "Status: $newStatus, Reason: $reason")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // -------------------------------------------------------------
    // Templates Management
    // -------------------------------------------------------------
    fun observeTemplates(): Flow<List<CvTemplate>> = flow {
        val path = "templates"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CvTemplate::class.java)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeTemplates error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun updateTemplateStatus(templateId: String, newStatus: String, reason: String) {
        val path = "templates/$templateId"
        try {
            db.collection("templates").document(templateId).update(
                mapOf(
                    "status" to newStatus,
                    "rejectionReason" to reason,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit("template_status_changed", templateId, "template", "Status: $newStatus, Reason: $reason")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // -------------------------------------------------------------
    // Reports & Complaints
    // -------------------------------------------------------------
    fun observeReports(): Flow<List<SupportReport>> = flow {
        val path = "reports"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SupportReport::class.java)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeReports error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun updateReportStatus(reportId: String, status: String, notes: String) {
        val path = "reports/$reportId"
        try {
            val adminEmail = getCurrentUserEmail() ?: "admin"
            db.collection("reports").document(reportId).update(
                mapOf(
                    "status" to status,
                    "resolutionNotes" to notes,
                    "resolvedBy" to adminEmail,
                    "resolvedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordAudit("report_resolved", reportId, "report", "Status: $status, Notes: $notes")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // -------------------------------------------------------------
    // Audit Logs
    // -------------------------------------------------------------
    fun observeAuditLogs(): Flow<List<AuditLog>> = flow {
        val path = "audit_logs"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AuditLog::class.java)
                        .sortedByDescending { it.timestamp?.seconds ?: 0L }
                        .take(100)
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeAuditLogs error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun recordAudit(action: String, targetId: String, targetType: String, details: String) {
        val adminId = auth.currentUser?.uid ?: "system"
        val adminEmail = auth.currentUser?.email ?: "system"
        val path = "audit_logs"
        try {
            val data = hashMapOf(
                "adminId" to adminId,
                "adminEmail" to adminEmail,
                "action" to action,
                "targetId" to targetId,
                "targetType" to targetType,
                "details" to details,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("audit_logs").add(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    // -------------------------------------------------------------
    // App Settings
    // -------------------------------------------------------------
    fun observeAppSettings(): Flow<AppSettings> = flow {
        val path = "app_settings/general"
        emitAll(
            db.collection("app_settings").document("general")
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(AppSettings::class.java) ?: AppSettings()
                    } else {
                        AppSettings()
                    }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeAppSettings error: ${error.message}")
                    emit(AppSettings())
                }
        )
    }

    suspend fun updateAppSettings(settings: AppSettings) {
        val path = "app_settings/general"
        try {
            val adminEmail = getCurrentUserEmail() ?: "admin"
            val data = hashMapOf(
                "maintenanceMode" to settings.maintenanceMode,
                "announcementText" to settings.announcementText,
                "announcementActive" to settings.announcementActive,
                "communitySubmissionsEnabled" to settings.communitySubmissionsEnabled,
                "supportEmail" to settings.supportEmail,
                "appVersion" to settings.appVersion,
                "updatedBy" to adminEmail,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("app_settings").document("general").set(data).await()
            recordAudit("settings_updated", "general", "app_settings", "Maintenance: ${settings.maintenanceMode}, Announcement: ${settings.announcementActive}")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // -------------------------------------------------------------
    // Notifications & Broadcasts
    // -------------------------------------------------------------
    fun observeNotifications(): Flow<List<SystemNotification>> = flow {
        val path = "notifications"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SystemNotification::class.java)
                        .sortedByDescending { it.sentAt?.seconds ?: 0L }
                }
                .catch { error ->
                    Log.e("AdminRepository", "Safe observeNotifications error: ${error.message}")
                    emit(emptyList())
                }
        )
    }

    suspend fun sendNotification(title: String, body: String, audience: String, targetUserId: String = "") {
        val path = "notifications"
        try {
            val adminEmail = getCurrentUserEmail() ?: "admin"
            val data = hashMapOf(
                "title" to title,
                "body" to body,
                "targetAudience" to audience,
                "targetUserId" to targetUserId,
                "sentBy" to adminEmail,
                "sentAt" to FieldValue.serverTimestamp(),
                "deliveryCount" to 1L
            )
            db.collection("notifications").add(data).await()
            recordAudit("notification_sent", audience, "notification", "Title: $title to $audience")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }
}
